package com.fuyouwentian.planktonmall.ui.category

import com.fuyouwentian.planktonmall.domain.model.Category
import com.fuyouwentian.planktonmall.domain.model.CategoryData

private val CategoryDataDefault = CategoryData<Category>(
    data = emptyList(),
    total = 0
)

data class CategoryUiState(
    val categoryData: CategoryData<Category> = CategoryDataDefault,
    val loading: Boolean = false,
    val error: String? = null
)
