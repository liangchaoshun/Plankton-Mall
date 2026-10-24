package com.fuyouwentian.planktonmall.ui.cart

data class CartUiState(
    val allSelected: Boolean = false,
    val isLoggedIn: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)
