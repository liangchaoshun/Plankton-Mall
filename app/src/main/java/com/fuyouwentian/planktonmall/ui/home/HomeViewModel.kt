package com.fuyouwentian.planktonmall.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.domain.model.Add2CartProductParams
import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val productRepo: ProductRepository,
    private val eventBus: GlobalEventBus,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(loading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentJob: Job? = null

    // 保存最后一次请求参数，供 loadMore 使用
    private var lastPageParams: PaginationParameter = PaginationParameter()

    // 请求版本号：刷新/搜索时自增，loadMore 返回时校验
    private var requestId = 0
    private val loadMoreMutex = Mutex()

    // 初始化加载/下拉刷新页面
    fun loadHomeData(isRefresh: Boolean = false) {
        val id = ++requestId
        currentJob?.cancel() // 重置
        lastPageParams = PaginationParameter() // 重置
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
                // Kotlin（以及 Java）的 try-finally 语义规定：
                //  无论 try 块是正常结束、还是因为 return、break、continue 或抛异常提前退出，
                //  finally 块都一定会被执行。
                // 所以，即使 if (id != requestId) return@launch，锁还是会解开的
                _uiState.update { it.copy(loadingMore = false) } // 统一清理
                loadMoreMutex.unlock()
            }
        }
    }

    fun add2Cart(product: ProductLite) {
        viewModelScope.launch {
            try {
                val userId = userRepo.getCurrentUserId()
                if (!tokenRepo.hasToken() || userId == null) {
                    eventBus.postEvent(SystemEvent.ShowToast("请先登录"))
                    return@launch
                }
                val bool = cartRepo.upsert(
                    userId = userId,
                    params = Add2CartProductParams(
                        id = product.id,
                        price = product.price,
                        stock = product.stock,
                        onSale = product.onSale,
                        nameZh = product.nameZh,
                        nameEn = product.nameEn,
                        iconUrl = product.iconUrl
                    )
                )
                if (bool) eventBus.postEvent(SystemEvent.ShowToast("加入购物车成功"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(exceptionInfo = message) }
    }
}