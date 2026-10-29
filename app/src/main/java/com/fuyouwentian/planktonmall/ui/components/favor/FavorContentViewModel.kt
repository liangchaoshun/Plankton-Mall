package com.fuyouwentian.planktonmall.ui.components.favor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.PaginationParameter
import com.fuyouwentian.planktonmall.domain.model.PaginationWithTotal
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import com.fuyouwentian.planktonmall.util.MongoIdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class)
@HiltViewModel
class FavorContentViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val favorRepo: FavorRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavorContentUiState(loading = true))
    val uiState: StateFlow<FavorContentUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // 订阅 Room favor data Flow
    val favorItems: StateFlow<List<FavorRecord>> = favorRepo.getAllLocalFavorItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private var currentJob: Job? = null

    // 保存最后一次请求参数，供 loadMore 使用
    private var lastPageParams: PaginationWithTotal = PaginationWithTotal()

    // 请求版本号：刷新/搜索时自增，loadMore 返回时校验
    private var requestId = 0
    private val loadMoreMutex = Mutex()

    fun loadFavorites(
        isRefresh: Boolean = false,
        isLoadingMore: Boolean = false,
    ) {
        val id = ++requestId
        currentJob?.cancel() // 重置
        if (!isLoadingMore) lastPageParams = PaginationWithTotal() // 重置
        currentJob = viewModelScope.launch {
            // 防止并发，上一次上滑请求返回的数据迟于当前下滑返回的数据导致数据错乱，
            // tryLock 非阻塞：拿不到锁说明已有加载在跑，直接返回
            if (!loadMoreMutex.tryLock()) return@launch
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
                val response = favorRepo.getRemoteFavors(PaginationParameter(pageIndex = pageIndex))
                if (id != requestId) return@launch // 避免上一次请求回来的（过期）数据，丢弃
                response?.let { res ->
                    lastPageParams = res
                    _uiState.update {
                        it.copy(
                            hasMore = favorItems.value.size < res.total,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                if (id == requestId) _uiState.update { it.copy(loading = false) }
                // 无条件清：避免过期请求把 loadingMore 挂住
                _uiState.update { it.copy(loadingMore = false) } // 统一清理
                loadMoreMutex.unlock()
            }
        }
    }

    // 加载更多
    fun loadMore() {
        val current = _uiState.value
        // 防止重复触发
        if (current.loading || current.loadingMore || !current.hasMore) return
        loadFavorites(isLoadingMore = true)
    }

    fun cancelFavorite(productId: String) {
        Log.d("FavorContentViewModel", "[cancelFavorite]")
        viewModelScope.launch {
            try {
                if (!tokenRepo.hasToken()) {
                    _uiEvent.emit(UiEvent.ShowToast("请先登录"))
                    return@launch
                }
                val localUserProfile = userRepo.getLocalUserProfile()
                if (localUserProfile == null) {
                    _uiEvent.emit(UiEvent.ShowToast("缺失用户数据"))
                    return@launch
                }
                val userId = localUserProfile.id
                    // 取消收藏
                if (favorRepo.removeFromFavorites(userId = userId, goodsId = productId)) {
                    _uiEvent.emit(UiEvent.ShowToast("取消收藏成功"))
                }
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
        _uiState.update { it.copy(error = msg) }
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}
