package com.fuyouwentian.planktonmall.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.domain.model.PaginationWithQuery
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject


@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepo: ProductRepository,
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductsUiState(loading = true))
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    // 防止竞态：fetchProductsData 和 loadMore
    private var requestId = 0

    // 保存最后一次请求参数，供 loadMore 使用
    private var lastRequest: PaginationWithQuery = PaginationWithQuery()

    private val loadMoreMutex = Mutex()

    // 初始化加载/下拉刷新/搜索（重置列表）
    fun fetchProductsData(
        pagination: PaginationWithQuery = PaginationWithQuery(),
        isRefresh: Boolean = false,
    ) {
        if (_uiState.value.loadingMore) return
        val id = ++requestId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = !isRefresh,
                    isRefreshing = isRefresh,
                    error = null,
                )
            }
            try {
                lastRequest = pagination
                val productsData = productRepo.getProducts(pagination)
                // mock data
                // val productsData = emptyList()
                if (id != requestId) return@launch // 已过期
                productsData?.let {
                    _uiState.update {
                        it.copy(
                            productsData = productsData.data,
                            loading = false,
                            isRefreshing = false,
                            hasMore = productsData.data.size < productsData.total
                        )
                    }
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    // 加载更多
    fun loadMore() {
        val snapshot = _uiState.value
        // 防止重复触发
        if (snapshot.loading || snapshot.isRefreshing || snapshot.loadingMore || !snapshot.hasMore) return

        viewModelScope.launch {
            // 防止并发，上一次上滑请求返回的数据迟于当前下滑返回的数据导致数据错乱，
            // tryLock 非阻塞：拿不到锁说明已有加载在跑，直接返回
            if (!loadMoreMutex.tryLock()) return@launch
            // 问题：if (id != requestId) return@launch 会跳过 finally 之后的 unlock()？
            // 解答：不会 return@launch 会走 finally，unlock() 仍执行
            // https://chat.deepseek.com/share/i6r9hu4f3prgksds4d
            val id = requestId
            try {
                _uiState.update { it.copy(loadingMore = true, error = null) }
                val params = lastRequest.copy(pageIndex = lastRequest.pageIndex + 1)
                val newData = productRepo.getProducts(params)
                if (id != requestId) return@launch // 期间刷新过数据，丢弃
                newData?.let {
                    // 更新分页
                    lastRequest = lastRequest.copy(
                        pageIndex = newData.pageIndex,
                        pageSize = newData.pageSize,
                    )
                    _uiState.update { state ->
                        val mergedList = state.productsData + newData.data
                        state.copy(
                            productsData = mergedList,
                            hasMore = mergedList.size < newData.total
                        )
                    }
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                // 无条件清：避免过期请求把 loadingMore 挂住
                _uiState.update { it.copy(loadingMore = false) } // 统一清理
                loadMoreMutex.unlock()
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
