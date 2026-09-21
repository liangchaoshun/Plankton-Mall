package com.fuyouwentian.planktonmall.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.data.model.Product

/**
 * 有状态版本（Stateful）：负责拿 ViewModel，传递给无状态版本
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 将 ViewModel 中的 StateFlow 转换为 Compose 可观察的 State
    // collectAsState: 前台/后台都收集
    // collectAsStateWithLifecycle: 后台自动停止收集，省电
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 这里根据 uiState 渲染 UI
    if (uiState.loading) {
        Text("loading")
    } else {
        /*Carousel(
            products: List<Product>,
            modifier: Modifier = Modifier
        )*/
        ProductGrid(
            uiState = uiState,
            modifier = modifier,
            onClickProduct = onProductClick
        )
    }
}

@Composable
fun Carousel(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    // TODO
}

/**
 * 无状态版本（Stateless）：纯 UI，方便预览和测试
 */
@Composable
fun ProductGrid(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onClickProduct: (String) -> Unit = { productId -> Log.i("Product_Click", productId) }
) {
    val products = uiState.productsData.data
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        modifier = modifier
    ) {
        items(products) { product ->
            AsyncImage(
                model = product.cover,
                contentDescription = product.name_zh,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onClickProduct(product.id) },
                contentScale = ContentScale.Crop
            )
        }
    }
}
