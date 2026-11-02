package com.fuyouwentian.planktonmall.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    cartRepo: CartRepository,
    userRepo: UserRepository,
) : ViewModel() {

    private val _systemEvent = MutableSharedFlow<SystemEvent>()
    val systemEvent: SharedFlow<SystemEvent> = _systemEvent.asSharedFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // 本地购物车总数量
    @OptIn(ExperimentalCoroutinesApi::class)
    val cartTotalQuantity: StateFlow<Int> = userRepo.getCurrentUserIdFlow()
        .flatMapLatest { userId -> // 实验性 API
            if (userId != null) {
                cartRepo.getTotalQuantityFlow(userId).map { it ?: 0 }
            } else {
                flowOf(0)
            }
        }
        .stateIn(
            scope = viewModelScope,
            // 屏幕隐藏 5 秒后停止收集，节省资源
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}