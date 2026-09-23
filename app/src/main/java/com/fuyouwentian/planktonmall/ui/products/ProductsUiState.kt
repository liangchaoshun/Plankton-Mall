package com.fuyouwentian.planktonmall.ui.products

import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.ProductsData

private val productsDataDefault = ProductsData<ProductLite>(
    data = emptyList(),
    total = 0,
    pageIndex = 1,
    pageSize = 20
)

data class ProductsUiState(
    val productsData: ProductsData<ProductLite> = productsDataDefault,
    val loading: Boolean = false,
    val error: String? = null
)
