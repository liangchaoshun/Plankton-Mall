package com.fuyouwentian.planktonmall.ui.cart

import com.fuyouwentian.planktonmall.domain.model.CartProduct

data class CartUiState(
    val cartData: List<CartProduct> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
