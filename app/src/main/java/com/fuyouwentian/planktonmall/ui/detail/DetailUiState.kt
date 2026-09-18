package com.fuyouwentian.planktonmall.ui.detail

import com.fuyouwentian.planktonmall.data.model.Product

data class DetailUiState(
    val product: Product? = null,
    val loading: Boolean = false,
    val error: String? = null
)
