package com.fuyouwentian.planktonmall.ui.products

import com.fuyouwentian.planktonmall.data.model.Product

data class ProductsUiState(
    val productsData: Product? = null,
    val loading: Boolean = false,
    val error: String? = null
)
