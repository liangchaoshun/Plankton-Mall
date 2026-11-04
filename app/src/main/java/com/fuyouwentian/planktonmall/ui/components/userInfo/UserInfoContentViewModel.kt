package com.fuyouwentian.planktonmall.ui.components.userInfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoContentViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val eventBus: GlobalEventBus,
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(UserInfoContentUiState(loading = false, commiting = false))
    val uiState: StateFlow<UserInfoContentUiState> = _uiState.asStateFlow()

    fun getRemoteUserProfile() {
        viewModelScope.launch {
            try {
                val profile = userRepo.getCurrentRemoteProfile()
                profile?.let {
                    Log.d("UserInfoContentViewModel", "$profile")
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                if (userRepo.logout() == true) {
                    tokenRepo.clearToken()
                    eventBus.postEvent(SystemEvent.ShowToast("退出登录成功"))
                    eventBus.postEvent(SystemEvent.Loggedout)
                    // 🌟 不要清空所有数据表
                    /*withContext(Dispatchers.IO) {
                        appDatabase.clearAllTables()
                    }*/
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
