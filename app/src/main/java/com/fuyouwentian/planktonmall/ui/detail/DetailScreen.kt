package com.fuyouwentian.planktonmall.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    id: String? = null // 允许传入 null
) {
    if (id.isNullOrBlank()) {
        Text("id is missing, please check the params")
    } else {
        Text("product id: $id")
    }
}
