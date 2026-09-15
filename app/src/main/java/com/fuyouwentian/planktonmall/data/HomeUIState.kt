package com.fuyouwentian.planktonmall.data

import com.fuyouwentian.planktonmall.model.Goods

data class HomeUIState(
    val goodsList: List<Goods> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
