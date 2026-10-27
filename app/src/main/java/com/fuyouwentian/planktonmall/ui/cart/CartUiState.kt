package com.fuyouwentian.planktonmall.ui.cart

data class CartUiState(
    val allSelected: Boolean = false,
    val hasToken: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
)
