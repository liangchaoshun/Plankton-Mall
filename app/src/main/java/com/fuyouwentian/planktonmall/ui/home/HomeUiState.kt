package com.fuyouwentian.planktonmall.ui.home

import com.fuyouwentian.planktonmall.domain.model.ProductLite

data class HomeUiState(
    val carouselData: List<ProductLite> = emptyList(),
    val recommendProducts: List<ProductLite> = emptyList(),
    val loading: Boolean = false, // 仅用于首次加载（全屏转圈）
    val isRefreshing: Boolean = false, // 用于下拉刷新
    val loadingMore: Boolean = false, // 上划加载更多
    val hasMore: Boolean = true, // 是否还有下一页
    val exceptionInfo: String? = null,
)
