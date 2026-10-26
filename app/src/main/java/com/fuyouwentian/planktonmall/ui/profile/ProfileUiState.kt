package com.fuyouwentian.planktonmall.ui.profile

import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import com.fuyouwentian.planktonmall.domain.model.UserProfile
import java.util.SortedMap


data class ProfileUiState(
    val orders: List<OrderRecord> = emptyList(),
    val histories: SortedMap<String, List<HistoryRecord>> = sortedMapOf(),
    val favors: List<FavorRecord> = emptyList(),
    val profile: UserProfile? = null,
    val isLoggedIn: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)
