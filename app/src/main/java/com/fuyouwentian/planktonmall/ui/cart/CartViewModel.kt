package com.fuyouwentian.planktonmall.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import com.fuyouwentian.planktonmall.util.MongoIdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val favorRepo: FavorRepository,
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(CartUiState(loading = true))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // 订阅 Room cart data Flow
    val cartItems: StateFlow<List<CartProduct>> = cartRepo.getAllLocalCartItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // UI 事件
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
    private var currentJob: Job? = null

    // 防止竞态：fetchProductsData
    private var requestId = 0

    // 🌟 关于防抖说明：https://chat.deepseek.com/share/xhacrastm18ql8rii0
    // 选用防抖设置每个条目的选中（checked）、数量（quantity）做同步更新到服务器上，还是有点小问题
    // 问题主要在于网络的信号强弱带来的延迟性，导致本地可能已经变了但是上一次的请求延迟了返回结果将当前条目设置为已同步！
    private val syncTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            syncTrigger
                .debounce(400.milliseconds)
                .collect {
                    runCatching {
                        cartRepo.syncLocalUpdate2Server()
                    }.onFailure { e -> handleError(e) }
                }
        }
    }

    // 初始化加载/下拉刷新
    fun fetchCartData() {
        val id = ++requestId
        currentJob?.cancel() // 重置
        currentJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    allSelected = false,
                    error = null
                )
            }
            try {
                val hasToken = tokenRepo.hasToken()
                _uiState.update { it.copy(hasToken = hasToken) }
                if (!hasToken) return@launch // 跳出
                cartRepo.fetchData() // 请求，成功后写入 Room cart
                if (id != requestId) return@launch // 已过期
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                if (id == requestId) _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun safeUpdateAndSync(updateLocal: suspend () -> Unit) {
        try {
            updateLocal() // 先更新本地
            syncTrigger.tryEmit(Unit) // 再同步到服务器
        } catch (e: Throwable) {
            handleError(e)
        }
    }

    // 切换选中某项
    fun toggleSelect(id: String, bool: Boolean) {
        viewModelScope.launch {
            safeUpdateAndSync { cartRepo.updateMultiLocalChecked(ids = listOf(id), checked = bool) }
        }
    }

    // 切换全选/全不选
    fun toggleSelectAll() {
        val target = !_uiState.value.allSelected // 只有两种可能：全选和全不选
        _uiState.update { state -> state.copy(allSelected = target) }
        viewModelScope.launch {
            safeUpdateAndSync {
                cartRepo.updateMultiLocalChecked(
                    ids = cartItems.value.filter { it.isValid && it.goodsStock > 0 }.map { it.id },
                    checked = target
                )
            }
        }
    }

    // 更新数量
    fun changeQuantity(id: String, value: Int) {
        if (value !in 1 until 1000) return
        // 取旧值用于失败回滚
        val legacyValue = cartItems.value.firstOrNull { it.id == id }?.quantity ?: return
        if (legacyValue == value) return

        viewModelScope.launch {
            safeUpdateAndSync { cartRepo.updateLocalQuantity(id, value) }
        }
    }

    fun deleteCartItems(ids: List<String>) {
        Log.d("CartViewModel", "deleteCartItems: $ids")
        viewModelScope.launch {
            try {
                if (cartRepo.delete(ids)) _uiEvent.emit(UiEvent.ShowToast("删除成功"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun add2Favorites(product: CartProduct) {
        viewModelScope.launch {
            try {
                val userId = userRepo.getCurrentUserId()
                if (!tokenRepo.hasToken() || userId == null) {
                    _uiEvent.emit(UiEvent.ShowToast("请先登录"))
                    return@launch
                }
                if (favorRepo.isExist(userId = userId, goodsId = product.goodsId)) {
                    // 已存在，则只提示
                    _uiEvent.emit(UiEvent.ShowToast("加入收藏成功"))
                } else {
                    // 不存在，则加入收藏
                    val req = FavorRecord(
                        id = MongoIdGenerator.generate(),
                        userId = userId,
                        goodsId = product.goodsId,
                        isValid = product.isValid,
                        goodsNameZh = product.goodsNameZh,
                        goodsNameEn = product.goodsNameEn,
                        goodsIcon = product.goodsIcon,
                        goodsPrice = product.goodsPrice,
                        goodsStock = product.goodsStock,
                        goodsOnSale = product.goodsOnSale,
                        syncState = SyncRecordState.PENDING_INSERT
                    )
                    if (favorRepo.add2Favorites(request = req)) {
                        _uiEvent.emit(UiEvent.ShowToast("加入收藏成功"))
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun syncData2Server(id: String = "") {
        // Log.d("Cart", "[syncData2Server] id: $id")
        viewModelScope.launch {
            try {
                cartRepo.syncLocalUpdate2Server() // 全量（checked、quantity）提交的
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }


    private suspend fun handleError(e: Throwable) {
        if (e is CancellationException) throw e // 协程取消时会抛出
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
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}
