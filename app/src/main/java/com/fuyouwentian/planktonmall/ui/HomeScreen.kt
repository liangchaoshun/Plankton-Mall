package com.fuyouwentian.planktonmall.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fuyouwentian.planktonmall.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 将 ViewModel 中的 StateFlow 转换为 Compose 可观察的 State
    val uiState by viewModel.uiState.collectAsState()

    // 这里根据 uiState 渲染 UI
    if (uiState.loading) {
        Text("loading")
    } else {
        // 渲染商品列表 uiState.goodsList
        Text("HomeScreen content")
    }
}