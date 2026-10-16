package com.fuyouwentian.planktonmall.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val userRepo: UserRepository,
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(CartUiState(loading = true))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // UI 事件
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // 防止竞态：fetchProductsData
    private var requestId = 0

    // 初始化加载/刷新（重置列表）
    fun fetchCartData() {
        val id = ++requestId
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                val isLoggedIn = userRepo.isLoggedIn() ?: false
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
                if (!isLoggedIn) return@launch // 跳出

                val localProfile = userRepo.getLocalUserProfile()
                if (localProfile == null) {
                    _uiState.update { it.copy(isLoggedIn = false, loading = false) }
                    _uiEvent.emit(UiEvent.ShowToast("无法加载个人信息，请重新登录"))
                    return@launch
                }

                val userId = localProfile.id
                Log.d("Cart", "UserId: $userId")
                val cartData = cartRepo.fetchData(userId)
                // mock data
                // val cartData = MockProducts.CartProducts
                if (id != requestId) return@launch // 已过期
                cartData?.let { _uiState.update { it.copy(cartData = cartData.data) } }
                // TODO Room cart
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    // 切换选中某项
    fun toggleSelect(productId: String, bool: Boolean) {
        _uiState.update { state ->
            state.copy(
                cartData = state.cartData.map { item ->
                    if (item.id == productId) item.copy(checked = bool)
                    else item
                }
            )
        }
        // TODO 更新 Room cart
    }

    // 切换全选/全不选
    fun toggleSelectAll() {
        val target = !_uiState.value.allSelected // 只有两种可能：全选和全不选
        _uiState.update { state ->
            state.copy(
                allSelected = target,
                cartData = state.cartData
                    .map {
                        // 无效的商品不用处理
                        val bool = if (it.isValid) target else it.checked
                        it.copy(checked = bool)
                    }
            )
        }
        // TODO 更新 Room cart
    }

    // 更新数量
    fun changeQuantity(productId: String, value: Int) {
        if (value !in 1 until 1000) return
        // 取旧值用于失败回滚
        val legacyValue = _uiState.value.cartData
            .firstOrNull { it.id == productId }?.quantity ?: return

        if (legacyValue == value) return

        toggleSelect(productId, true) // 改变数量，要求选中

        // 乐观更新：立即改 UI
        updateLocalQuantity(productId, value)
        _uiState.update { it.copy(updatingIds = it.updatingIds + productId) }

        // 提交服务端
        viewModelScope.launch {
            try {
                // cartRepo.updateQuantity(productId, value) TODO: 更新
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // 失败：回滚
                updateLocalQuantity(productId, legacyValue)
                handleError(e)
            } finally {
                _uiState.update { it.copy(updatingIds = it.updatingIds - productId) }
            }
        }
    }

    private fun updateLocalQuantity(productId: String, quantity: Int) {
        _uiState.update { state ->
            state.copy(
                cartData = state.cartData.map { item ->
                    if (item.id == productId) item.copy(quantity = quantity)
                    else item
                }
            )
        }
        // TODO 更新 Room cart
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
