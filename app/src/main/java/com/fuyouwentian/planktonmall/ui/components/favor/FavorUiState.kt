package com.fuyouwentian.planktonmall.ui.components.favor

import com.fuyouwentian.planktonmall.domain.model.FavorsPaginationState

data class FavorUiState(
    val pagination: FavorsPaginationState = FavorsPaginationState(),
    val loading: Boolean = false,
    val error: String? = null,
)
