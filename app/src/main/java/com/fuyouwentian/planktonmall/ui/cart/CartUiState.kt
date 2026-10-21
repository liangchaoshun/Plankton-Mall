package com.fuyouwentian.planktonmall.ui.cart

data class CartUiState(
    val updatingIds: Set<String> = emptySet(),  // 正在更新数量的商品 id
    val allSelected: Boolean = false,
    val isLoggedIn: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    // 同步失败的条目，用于 UI 展示同步 icon
    val syncFailedIds: Set<String> = emptySet()
)
