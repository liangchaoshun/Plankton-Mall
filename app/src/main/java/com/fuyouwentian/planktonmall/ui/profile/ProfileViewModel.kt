package com.fuyouwentian.planktonmall.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.auth.AuthManager
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
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
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val userId = authManager.getCurrentUserId(false)
                if (userId == null) {
                    _uiState.update { it.copy(hasToken = false) }
                    return@launch
                }
                _uiState.update { it.copy(loading = true) }
                userRepo.getCurrentLocalProfile()?.let { profile ->
                    _uiState.update { it.copy(profile = profile) }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
