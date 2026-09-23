package com.fuyouwentian.planktonmall.ui.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    q: String? = null // 搜索条件，允许传入 null
) {
    if (q.isNullOrBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Missing search condition")
        }
    } else {
        Text("search condition: $q")
    }
}
