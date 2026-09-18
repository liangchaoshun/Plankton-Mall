package com.fuyouwentian.planktonmall.ui.category

import com.fuyouwentian.planktonmall.data.model.Category
import com.fuyouwentian.planktonmall.data.model.Series

data class CategoryUiState(
    val categoryList: List<Category> = emptyList(),
    val seriesList: List<Series> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
