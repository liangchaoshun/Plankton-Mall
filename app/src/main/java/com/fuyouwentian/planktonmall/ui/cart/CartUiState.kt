package com.fuyouwentian.planktonmall.ui.cart

import com.fuyouwentian.planktonmall.data.model.Product

data class CartUiState(
    val cartList: List<Product> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
