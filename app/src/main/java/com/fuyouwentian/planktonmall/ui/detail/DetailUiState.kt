package com.fuyouwentian.planktonmall.ui.detail

import com.fuyouwentian.planktonmall.domain.model.Product

data class DetailUiState(
    val product: Product? = null,
    val committingOrder: Boolean = false,
    val isMakeOrderDone: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)
