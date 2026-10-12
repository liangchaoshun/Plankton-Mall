package com.fuyouwentian.planktonmall.navigation

import androidx.lifecycle.ViewModel
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    tokenRepo: TokenRepository
) : ViewModel() {
    val tokenUiEvent: SharedFlow<UiEvent> = tokenRepo.uiEvent
}