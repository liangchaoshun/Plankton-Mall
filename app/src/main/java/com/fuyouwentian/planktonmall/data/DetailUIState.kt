package com.fuyouwentian.planktonmall.data

import com.fuyouwentian.planktonmall.model.Goods

data class DetailUIState(
    val goods: Goods? = null,
    val loading: Boolean = false,
    val error: String? = null
)
