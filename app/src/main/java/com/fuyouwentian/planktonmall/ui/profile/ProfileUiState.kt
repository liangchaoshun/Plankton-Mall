package com.fuyouwentian.planktonmall.ui.profile

import com.fuyouwentian.planktonmall.domain.model.UserProfile


data class ProfileUiState(
    val profile: UserProfile? = null,
    val hasToken: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null
)
