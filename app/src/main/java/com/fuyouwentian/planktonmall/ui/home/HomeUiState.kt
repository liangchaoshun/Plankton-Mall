package com.fuyouwentian.planktonmall.ui.home

import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.ProductsResponseData

val productsDataDefault = ProductsResponseData<ProductLite>(
    data = emptyList(),
    total = 0,
    pageIndex = 1,
    pageSize = 10
)

data class HomeUiState(
    val carouselData: List<ProductLite> = emptyList(),
    val recommendProducts: ProductsResponseData<ProductLite> = productsDataDefault,
    val loading: Boolean = false, // 仅用于首次加载（全屏转圈）
    val isRefreshing: Boolean = false, // 专门用于下拉刷新
    val loadingMore: Boolean = false, // 上滑加载更多
    val hasMore: Boolean = true, // 是否还有下一页
    val exceptionInfo: String? = null,
)
