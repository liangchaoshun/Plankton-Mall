package com.fuyouwentian.planktonmall.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.auth.AuthManager
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
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val eventBus: GlobalEventBus,
    private val errorHandler: ErrorHandler,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(loading = false))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun initScreen() {
        _uiState.update {
            it.copy(
                profile = null,
                hasToken = true,
                loading = false,
                error = null
            )
        }
        loadUserInfo(true)
    }

    fun loadUserInfo(isInit: Boolean = false) {
        Log.d("loadUserInfo", "$isInit")
        viewModelScope.launch {
            try {
                val userId = authManager.getCurrentUserId(false)
                if (userId == null) {
                    _uiState.update { it.copy(hasToken = false) }
                    return@launch
                }
                _uiState.update { it.copy(loading = isInit) }
                userRepo.getCurrentRemoteProfile()?.let { profile ->
                    _uiState.update { it.copy(profile = profile) }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
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
