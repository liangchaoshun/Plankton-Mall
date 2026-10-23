package com.fuyouwentian.planktonmall.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
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

    private val syncTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var currentJob: Job? = null

    // 防止竞态：fetchProductsData
    private var requestId = 0

    init {
        viewModelScope.launch {
            syncTrigger
                .debounce(400.milliseconds)
                .collect {
                    cartRepo.syncLocal2Server()
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
                    updatingIds = emptySet(),
                    allSelected = false,
                    error = null
                )
            }

            try {
                val isLoggedIn = userRepo.isLoggedIn() ?: false
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
                if (!isLoggedIn) return@launch // 跳出
                cartRepo.fetchData() // 请求，成功后写入 Room cart
                if (id != requestId) return@launch // 已过期
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                if (id == requestId) _uiState.update { it.copy(loading = false) }
            }
        }
    }

    // 切换选中某项
    fun toggleSelect(id: String, bool: Boolean) {
        viewModelScope.launch {
            try {
                cartRepo.updateMultiChecked(ids = listOf(id), checked = bool)
                syncTrigger.tryEmit(Unit)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                handleError(e)
            }
        }
    }

    // 切换全选/全不选
    fun toggleSelectAll() {
        val target = !_uiState.value.allSelected // 只有两种可能：全选和全不选
        _uiState.update { state -> state.copy(allSelected = target) }
        viewModelScope.launch {
            try {
                val ids = cartItems.value.filter { it.isValid }.map { it.id }
                cartRepo.updateMultiChecked(ids = ids, checked = target)
                syncTrigger.tryEmit(Unit)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                handleError(e)
            }
        }
    }

    // 更新数量
    fun changeQuantity(id: String, value: Int) {
        if (value !in 1 until 1000) return
        // 取旧值用于失败回滚
        val legacyValue = cartItems.value
            .firstOrNull { it.id == id }?.quantity ?: return

        if (legacyValue == value) return
        viewModelScope.launch {
            try {
                cartRepo.updateQuantity(id, value)
                syncTrigger.tryEmit(Unit)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                handleError(e)
            } finally {
                _uiState.update { it.copy(updatingIds = it.updatingIds - id) }
            }
        }
    }

    // TODO 待完善，很多事件没有成功的提示文案
    fun deleteCartItems(ids: List<String>) {
        Log.d("CartViewModel", "deleteCartItems: $ids")
        viewModelScope.launch {
            try {
                if (cartRepo.delete(ids)) _uiEvent.emit(UiEvent.ShowToast("删除成功"))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                handleError(e)
            }
        }
    }

    fun syncData2Server(id: String) {
        Log.d("Cart", "[syncData2Server] id: $id")
        viewModelScope.launch {
            try {
                cartRepo.syncLocal2Server()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                handleError(e)
            }
        }
    }

    private suspend fun handleError(e: Exception) {
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
