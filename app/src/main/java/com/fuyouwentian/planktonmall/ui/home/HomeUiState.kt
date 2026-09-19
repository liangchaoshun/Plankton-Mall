package com.fuyouwentian.planktonmall.ui.home

import com.fuyouwentian.planktonmall.data.model.ProductsData

val productsDataDefault = ProductsData(
    data = emptyList(),
    total = 0,
    pageIndex = 1,
    pageSize = 20
)

data class HomeUiState(
    val productsData: ProductsData = productsDataDefault,
    val loading: Boolean = false,
    val error: String? = null
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}
