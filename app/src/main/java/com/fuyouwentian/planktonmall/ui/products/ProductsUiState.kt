package com.fuyouwentian.planktonmall.ui.products

import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.ProductsResponseData

private val productsDataDefault = ProductsResponseData<ProductLite>(
    data = emptyList(),
    total = 0,
    pageIndex = 1,
    pageSize = 10
)

data class ProductsUiState(
    val productsData: ProductsResponseData<ProductLite> = productsDataDefault,
    val loading: Boolean = false, // 首次或整页加载中
    val loadingMore: Boolean = false, // 上滑加载更多
    val hasMore: Boolean = true, // 是否还有下一页
    val error: String? = null
)
