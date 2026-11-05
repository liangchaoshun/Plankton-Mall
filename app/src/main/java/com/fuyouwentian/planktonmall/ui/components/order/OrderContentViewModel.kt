package com.fuyouwentian.planktonmall.ui.components.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationWithQuery
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import com.fuyouwentian.planktonmall.domain.repository.OrderRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class OrderContentViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val orderRepo: OrderRepository,
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderContentUiState(loading = true))
    val uiState: StateFlow<OrderContentUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val orderRecords: StateFlow<List<OrderRecord>> = userRepo.getCurrentUserIdFlow()
        .flatMapLatest { userId -> // 实验性 API
            if (userId != null) {
                orderRepo.getRecordsFlow(userId)
                    .map { list -> list.sortedByDescending { it.createTime } }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            // 屏幕隐藏 5 秒后停止收集，节省资源
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var currentJob: Job? = null

    // 保存最后一次请求参数，供 loadMore 使用
    private var lastPageParams: PaginationWithTotal = PaginationWithTotal()

    // 请求版本号：刷新/搜索时自增，loadMore 返回时校验
    private var requestId = 0
    private val loadMoreMutex = Mutex()

    fun loadData(
        isRefresh: Boolean = false,
        isLoadingMore: Boolean = false,
    ) {
        val id = ++requestId
        currentJob?.cancel() // 重置
        currentJob = viewModelScope.launch {
            // 防止并发，上一次上滑请求返回的数据迟于当前下滑返回的数据导致数据错乱，
            // tryLock 非阻塞：拿不到锁说明已有加载在跑，直接返回
            if (!loadMoreMutex.tryLock()) return@launch
            if (!isLoadingMore) lastPageParams = PaginationWithTotal() // 重置
            _uiState.update {
                // 下拉刷新，不清空数据，也不显示全屏 loading
                it.copy(
                    loading = !(isRefresh || isLoadingMore),
                    loadingMore = isLoadingMore,
                    hasMore = true,
                    error = null,
                )
            }
            try {
                var pageIndex = lastPageParams.pageIndex
                if (isLoadingMore) ++pageIndex
                val params = PaginationWithQuery(pageIndex = pageIndex, q = _uiState.value.q)
                val response = orderRepo.getRemoteRecords(params)
                if (id != requestId) return@launch // 避免上一次请求回来的（过期）数据，丢弃
                lastPageParams = response
                _uiState.update {
                    it.copy(
                        hasMore = (response.pageIndex * response.pageSize) < response.total,
                        error = null
                    )
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
                // 无条件清：避免过期请求把 loadingMore 挂住
                _uiState.update { it.copy(loadingMore = false) } // 统一清理
                loadMoreMutex.unlock()
            }
        }
    }

    fun loadMore() {
        val current = _uiState.value
        // 防止重复触发
        if (current.loading || current.loadingMore || !current.hasMore) return
        loadData(isLoadingMore = true)
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
