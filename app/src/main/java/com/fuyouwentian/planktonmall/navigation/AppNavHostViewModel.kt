package com.fuyouwentian.planktonmall.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
        /**
         * stateIn 已经自动包含了去重功能，Kotlin 协程中 StateFlow 在设计上已经对连续相同的值进行去重，
         * 其行为等价于已经在内部调用了 distinctUntilChanged()
         * 所以在 flatMapLatest 之前可以不用加 distinctUntilChanged
         */
        .stateIn(
            scope = viewModelScope,
            // 屏幕隐藏 5 秒后停止收集，节省资源
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}