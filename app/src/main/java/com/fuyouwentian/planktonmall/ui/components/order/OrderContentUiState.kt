package com.fuyouwentian.planktonmall.ui.components.order

data class OrderContentUiState(
    val loading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)