package com.fuyouwentian.planktonmall.ui.home

import com.fuyouwentian.planktonmall.data.model.Product

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
