package com.fuyouwentian.planktonmall.ui.cart

import com.fuyouwentian.planktonmall.domain.model.CartProduct

data class CartUiState(
    val cartData: List<CartProduct> = emptyList(),
    val updatingIds: Set<String> = emptySet(),  // 正在更新数量的商品 id
    val allSelected: Boolean = false,
    val isLoggedIn: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    // 同步失败的条目，用于 UI 展示重试按钮
    val syncFailedIds: Set<String> = emptySet()
) {
    val checkedItems: List<CartProduct>
        get() = cartData.filter { it.isValid && it.checked }
}
