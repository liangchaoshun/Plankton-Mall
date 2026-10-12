package com.fuyouwentian.planktonmall.ui.checkout

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOrderSuccess: (orderId: String) -> Unit = {},
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    Text("结算页")
}