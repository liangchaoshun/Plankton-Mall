package com.fuyouwentian.planktonmall.ui.checkout

import androidx.lifecycle.ViewModel
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(CheckoutUiState(submitting = false))
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    // UI 事件
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun sss() {
        //
    }
}