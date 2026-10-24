package com.fuyouwentian.planktonmall.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    cartRepo: CartRepository,
    tokenRepo: TokenRepository,
) : ViewModel() {

    // 本地购物车总数量
    val cartTotalQuantity: StateFlow<Int> = cartRepo.getTotalQuantityFlow()
        .map { it ?: 0 }
        .stateIn(
            scope = viewModelScope,
            // 屏幕隐藏 5 秒后停止收集，节省资源
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    val tokenUiEvent: SharedFlow<UiEvent> = tokenRepo.uiEvent
}