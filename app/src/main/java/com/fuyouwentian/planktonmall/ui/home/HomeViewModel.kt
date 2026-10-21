package com.fuyouwentian.planktonmall.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.Add2CartParams
import com.fuyouwentian.planktonmall.domain.model.PaginationParamsRequest
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
    private val cartRepo: CartRepository,
    private val userRepo: UserRepository,
    private val productRepo: ProductRepository,
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(HomeUiState(loading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // UI 事件（用于 Toast、导航等一次性动作）
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentJob: Job? = null

    // 保存最后一次请求参数，供 loadMore 使用
    private var lastPageParams: PaginationParamsRequest = PaginationParamsRequest()

    // 请求版本号：刷新/搜索时自增，loadMore 返回时校验
    private var requestId = 0
    private val loadMoreMutex = Mutex()

    // 初始化加载/下拉刷新页面
    fun initFetchData(isRefresh: Boolean = false) {
        val id = ++requestId
        currentJob?.cancel() // 重置
        lastPageParams = PaginationParamsRequest() // 重置
        currentJob = viewModelScope.launch {
            _uiState.update {
                // 下拉刷新，不清空数据，也不显示全屏 loading
                it.copy(
                    carouselData = if (isRefresh) it.carouselData else emptyList(),
                    recommendProducts = if (isRefresh) it.recommendProducts else productsDataDefault,
                    loading = !isRefresh,
                    loadingMore = false,
                    hasMore = true,
                    exceptionInfo = null,
                )
            }

            try {
                // 使用 async 开启并发请求
                // 任何一个请求失败都走外部 try-catch，所以 async 中不设置 try-catch 了
                val carouselDeferred = async { productRepo.getHomeCarousel() }
                val recommendDeferred = async { productRepo.getRecommendProducts() }

                // await() 会挂起当前协程，直到对应的 async 任务完成
                val carouselData = carouselDeferred.await()
                val recommendData = recommendDeferred.await()

                if (id != requestId) return@launch // 避免上一次请求回来的（过期）数据，丢弃
                _uiState.update {
                    it.copy(
                        carouselData = carouselData ?: emptyList(),
                        recommendProducts = recommendData ?: productsDataDefault,
                        exceptionInfo = null,
                    )
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                if (id == requestId)
                    _uiState.update { it.copy(loading = false, isRefreshing = false) }
            }
        }
    }

    // 加载更多
    fun loadMore() {
        val current = _uiState.value
        // 防止重复触发
        if (current.loading || current.loadingMore || !current.hasMore) return

        viewModelScope.launch {
            // 防止并发，上一次上滑请求返回的数据迟于当前下滑返回的数据导致数据错乱，
            // tryLock 非阻塞：拿不到锁说明已有加载在跑，直接返回
            if (!loadMoreMutex.tryLock()) return@launch
            val id = requestId // 捕获当前版本
            try {
                _uiState.update { it.copy(loadingMore = true, exceptionInfo = null) }
                val latest = _uiState.value
                val nextPage = latest.recommendProducts.pageIndex + 1
                val pageParams = lastPageParams.copy(pageIndex = nextPage)
                val newData = productRepo.getRecommendProducts(pageParams)
                if (id != requestId) return@launch // 期间刷新过，丢弃
                newData?.let {
                    _uiState.update { state ->
                        val mergedList = state.recommendProducts.data + newData.data
                        state.copy(
                            recommendProducts = state.recommendProducts.copy(
                                data = mergedList,
                                total = newData.total,
                                pageIndex = newData.pageIndex,
                                pageSize = newData.pageSize
                            ),
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

    fun add2Cart(product: ProductLite) {
        Log.d("Home", "add2Cart: $product")

        viewModelScope.launch {
            try {
                if (userRepo.isLoggedIn() == false) {
                    _uiEvent.emit(UiEvent.ShowToast("请先登录"))
                    return@launch
                }
                cartRepo.insert(
                    Add2CartParams(
                        id = product.id,
                        price = product.price,
                        stock = product.stock,
                        onSale = product.onSale,
                        nameZh = product.nameZh,
                        nameEn = product.nameEn,
                        iconUrl = product.iconUrl
                    )
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        if (e is CancellationException) throw e // 协程取消时抛出
        val msg = when (e) {
            /**
             * 都一起了：
             * 业务错误（status_code != 20000）
             * HTTP 协议错误（404, 500 等）
             * 网络错误（断网、超时）
             */
            is FriendlyException -> e.friendlyMessage
            else -> e.message ?: "请稍后重试"
        }
        Log.d("HandleError", msg)
        _uiState.update { it.copy(exceptionInfo = msg) }
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}