package com.fuyouwentian.planktonmall.ui.products

import com.fuyouwentian.planktonmall.domain.model.ProductLite

data class ProductsUiState(
    val productsData: List<ProductLite> = emptyList(),
    val loading: Boolean = false, // 首次或整页加载中
    val isRefreshing: Boolean = false, // 用于下拉刷新
    val loadingMore: Boolean = false, // 上滑加载更多
    val hasMore: Boolean = true, // 是否还有下一页
    val error: String? = null
)
