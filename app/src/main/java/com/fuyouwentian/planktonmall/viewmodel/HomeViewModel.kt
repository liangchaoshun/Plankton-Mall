package com.fuyouwentian.planktonmall.viewmodel

import androidx.lifecycle.ViewModel
import com.fuyouwentian.planktonmall.data.HomeUIState
import com.fuyouwentian.planktonmall.mock.MockGoodsList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUIState(loading = true))
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    init {
        initEmailList()
    }

    private fun initEmailList() {
        val goodsList = MockGoodsList.allGoods
        _uiState.value = HomeUIState(
            goodsList = goodsList,
            loading = false
        )
    }
}