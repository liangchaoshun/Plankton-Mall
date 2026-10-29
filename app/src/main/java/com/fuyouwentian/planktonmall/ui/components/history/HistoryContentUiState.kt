package com.fuyouwentian.planktonmall.ui.components.history

import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import java.util.SortedMap

data class HistoryContentUiState(
    val histories: SortedMap<String, List<HistoryRecord>> = sortedMapOf(),
    val loading: Boolean = false,
    val error: String? = null
)