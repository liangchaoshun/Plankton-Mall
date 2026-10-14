package com.fuyouwentian.planktonmall.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.RegisterRequest
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(RegisterUiState(commiting = false))
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // UI 事件
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun onAccountChange(acc: String) {
        _uiState.update { it.copy(account = acc) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun register() {
        // 非空校验
        if (_uiState.value.account.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowToast("请输入账号")) }
            return
        }
        if (_uiState.value.password.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowToast("请输入密码")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(commiting = true, error = null) }
            try {
                val param = RegisterRequest(
                    account = _uiState.value.account,
                    password = _uiState.value.password,
                )
                val isOk = userRepo.register(param)
                if (isOk == true) _uiEvent.emit(UiEvent.RegisterSuccess)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(commiting = false) }
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
