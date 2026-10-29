package com.fuyouwentian.planktonmall.ui.components.favor

data class FavorContentUiState(
    val loading: Boolean = false,
    val isRefreshing: Boolean = false, // 用于下拉刷新
    val loadingMore: Boolean = false, // 上划加载更多
    val hasMore: Boolean = true, // 是否还有下一页
    val error: String? = null,
)
