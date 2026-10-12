package com.fuyouwentian.planktonmall.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import com.fuyouwentian.planktonmall.mock.MockProducts
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
        // TODO 参数 user_id
        val id = ++requestId
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                val userId = userRepo.getLocalUserProfile()?.id ?: "pseudoId"
                // pseudoId 说明没登录账号，服务端会先校验 token，返回后跳转到登录页
//                val cartData = cartRepo.fetchData(userId)
                // mock data
                val cartData = MockProducts.CartProducts
                if (id != requestId) return@launch // 已过期
                _uiState.update { it.copy(cartData = cartData.data) }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
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
            else -> "未知错误：${e.message ?: "请稍后重试"}"
        }
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}
