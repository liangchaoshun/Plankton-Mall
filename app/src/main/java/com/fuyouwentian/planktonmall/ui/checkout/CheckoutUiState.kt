package com.fuyouwentian.planktonmall.ui.checkout

import com.fuyouwentian.planktonmall.domain.model.ProductLite

data class CheckoutUiState(
    val items: List<ProductLite> = emptyList(),
    val address: String? = null,
    val totalAmount: Double = 0.0,
    val submitting: Boolean = false,
    val error: String? = null,
)
