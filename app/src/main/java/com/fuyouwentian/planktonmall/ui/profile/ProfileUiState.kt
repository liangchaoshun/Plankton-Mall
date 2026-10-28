package com.fuyouwentian.planktonmall.ui.profile

import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import com.fuyouwentian.planktonmall.domain.model.UserProfile


data class ProfileUiState(
    val orders: List<OrderRecord> = emptyList(),
    val profile: UserProfile? = null,
    val hasToken: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null
)
