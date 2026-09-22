package com.fuyouwentian.planktonmall.ui.home

import com.fuyouwentian.planktonmall.data.model.HomeProduct
import com.fuyouwentian.planktonmall.data.model.ProductsData

private val productsDataDefault = ProductsData<HomeProduct>(
    data = emptyList(),
    total = 0,
    pageIndex = 1,
    pageSize = 20
)

data class HomeUiState(
    val productsData: ProductsData<HomeProduct> = productsDataDefault,
    val loading: Boolean = false,
    val error: String? = null
)

sealed class UiEvent {
    // 为何 ShowToast 不使用 object >> https://chat.deepseek.com/share/0e4k6fra0acquk242x
    data class ShowToast(val message: String) : UiEvent()
}
