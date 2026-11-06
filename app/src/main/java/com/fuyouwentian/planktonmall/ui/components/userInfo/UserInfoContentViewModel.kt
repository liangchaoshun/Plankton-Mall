package com.fuyouwentian.planktonmall.ui.components.userInfo

import androidx.lifecycle.ViewModel
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class UserInfoContentViewModel @Inject constructor(
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(UserInfoContentUiState(loading = false, commiting = false))
    val uiState: StateFlow<UserInfoContentUiState> = _uiState.asStateFlow()

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
