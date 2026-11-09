package com.fuyouwentian.planktonmall.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.common.Constants.ACCOUNT_REGEX
import com.fuyouwentian.planktonmall.common.Constants.PASSWORD_REGEX
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.domain.model.RegisterRequest
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val eventBus: GlobalEventBus,
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState(committing = false))
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onAccountChange(acc: String) {
        // 如果符合正则（或者为空，允许用户删除）
        _uiState.update {
            it.copy(
                account = acc,
                isAccountFmtError = acc.isNotEmpty() && !ACCOUNT_REGEX.matches(acc)
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                isPasswordFmtError = password.isNotEmpty() && !PASSWORD_REGEX.matches(
                    password
                )
            )
        }
    }

    fun register() {
        // 格式校验
        if (_uiState.value.isAccountFmtError || _uiState.value.isPasswordFmtError) {
            viewModelScope.launch { eventBus.postEvent(SystemEvent.ShowToast("未通过格式校验")) }
            return
        }
        // 非空校验
        if (_uiState.value.account.isBlank()) {
            viewModelScope.launch { eventBus.postEvent(SystemEvent.ShowToast("请输入账号")) }
            return
        }
        if (_uiState.value.password.isBlank()) {
            viewModelScope.launch { eventBus.postEvent(SystemEvent.ShowToast("请输入密码")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(committing = true, error = null) }
            try {
                val param = RegisterRequest(
                    account = _uiState.value.account,
                    password = _uiState.value.password,
                )
                val isOk = userRepo.register(param)
                if (isOk == true) {
                    eventBus.postEvent(SystemEvent.ShowToast("注册成功，即将跳转登录页"))
                    eventBus.postEvent(SystemEvent.RegisterSuccess)
                }
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _uiState.update { it.copy(committing = false) }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
