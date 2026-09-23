package com.fuyouwentian.planktonmall.ui.home

import com.fuyouwentian.planktonmall.data.model.ProductLite
import com.fuyouwentian.planktonmall.data.model.ProductsData

private val productsDataDefault = ProductsData<ProductLite>(
    data = emptyList(),
    total = 0,
    pageIndex = 1,
    pageSize = 20
)

data class HomeUiState(
    val productsData: ProductsData<ProductLite> = productsDataDefault,
    val loading: Boolean = false,
    val error: String? = null
)
