package com.fuyouwentian.planktonmall.data

import com.fuyouwentian.planktonmall.model.Category
import com.fuyouwentian.planktonmall.model.Series

data class CategoryUIState(
    val categoryList: List<Category> = emptyList(),
    val seriesList: List<Series> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
