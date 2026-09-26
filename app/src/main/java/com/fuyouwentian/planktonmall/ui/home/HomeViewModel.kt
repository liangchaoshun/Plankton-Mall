package com.fuyouwentian.planktonmall.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(HomeUiState(loadingHomeList = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // UI 事件（用于 Toast、导航等一次性动作）
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // 保存最后一次请求参数，供 loadMore 使用
    private var lastRequest: HomeProductsRequest = HomeProductsRequest()

    // 请求版本号：刷新/搜索时自增，loadMore 返回时校验
    private var requestId = 0
    private val loadMoreMutex = Mutex()

    fun fetchCarouselData() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingCarousel = true, carouselError = null) }
            try {
                val carouselData = productRepository.getHomeCarousel()
                _uiState.update {
                    it.copy(
                        // carouselData = emptyList(),
                        carouselData = carouselData,
                        loadingCarousel = false,
                        carouselError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadingCarousel = false) }
                handleError(e, isCarousel = true)
            }
        }
    }

    // 初始化加载/刷新/搜索（重置列表）
    fun fetchProductsData(params: HomeProductsRequest = HomeProductsRequest()) {
        lastRequest = params
        val id = ++requestId
        viewModelScope.launch {
            _uiState.update { it.copy(loadingHomeList = true, productsError = null) }
            try {
                val productsData = productRepository.getHomeProducts(params)
                if (id != requestId) return@launch // 过期则丢弃
                /*// mock data
                val productsData = ProductsData<ProductLite>(
                    data = emptyList(),
                    total = 0,
                    pageIndex = 1,
                    pageSize = 10
                )*/
                _uiState.update {
                    it.copy(
                        productsData = productsData,
                        loadingMore = false, // 顺带清掉可能残留的 loadMore 状态
                        hasMore = productsData.data.size < productsData.total,
                        productsError = null
                    )
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e, isCarousel = false)
            } finally {
                if (id == requestId) _uiState.update { it.copy(loadingHomeList = false) }
            }
        }
    }

    // 加载更多
    fun loadMore() {
        val current = _uiState.value
        // 防止重复触发
        if (current.loadingHomeList || current.loadingMore || !current.hasMore) return

        viewModelScope.launch {
            // 防止并发，上一次上滑请求返回的数据迟于当前下滑返回的数据导致数据错乱，
            // tryLock 非阻塞：拿不到锁说明已有加载在跑，直接返回
            if (!loadMoreMutex.tryLock()) return@launch
            val id = requestId // 捕获当前版本
            try {
                _uiState.update { it.copy(loadingMore = true) }
                val latest = _uiState.value
                val nextPage = latest.productsData.pageIndex + 1
                val params = lastRequest.copy(pageIndex = nextPage)
                val newData = productRepository.getHomeProducts(params)
                if (id != requestId) return@launch // 期间刷新过，丢弃
                _uiState.update { state ->
                    val mergedList = state.productsData.data + newData.data
                    state.copy(
                        productsData = state.productsData.copy(
                            data = mergedList,
                            total = newData.total,
                            pageIndex = newData.pageIndex,
                            pageSize = newData.pageSize
                        ),
                        hasMore = mergedList.size < newData.total
                    )
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e, isCarousel = false)
            } finally {
                // 无条件清：避免过期请求把 loadingMore 挂住
                _uiState.update { it.copy(loadingMore = false) } // 统一清理
                loadMoreMutex.unlock()
            }
        }
    }

    private suspend fun handleError(e: Exception, isCarousel: Boolean = false) {
        if (e is CancellationException) throw e // 协程取消时抛出
        val msg = when (e) {
            /**
             * 都一起了：
             * 业务错误（status_code != 20000）
             * HTTP 协议错误（404, 500 等）
             * 网络错误（断网、超时）
             */
            is FriendlyException -> e.friendlyMessage
            else -> "未知错误：${e.message ?: "请稍后重试"}"
        }
        _uiState.update {
            if (isCarousel) it.copy(carouselError = msg) else it.copy(productsError = msg)
        }
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}
