package com.fuyouwentian.planktonmall.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.UiEvent


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    onHomeSearch: (String) -> Unit = {},
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

    HomeScreenContent(
        uiState = uiState,
        modifier = modifier,
        onClickRetry = { viewModel.initHandler() },
        onHomeSearch = onHomeSearch,
        onClickProduct = onProductClick
    )
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = {
                    onSearch(query)
                    keyboardController?.hide()
                }) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "搜索"
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        placeholder = { Text(stringResource(R.string.placeholder_search)) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(query)
                keyboardController?.hide()
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

@Composable
fun Carousel(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    // TODO
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit = {},
    onHomeSearch: (String) -> Unit = {},
    onClickProduct: (String) -> Unit = { productId -> Log.i("Product_Click", productId) }
) {
    // 根据状态展示不同 UI
    when {
        uiState.loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            // 加载失败，显示错误提示和重试按钮
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "加载失败: ${uiState.error}")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onClickRetry) { Text("重试") }
                }
            }
        }

        uiState.productsData.data.isEmpty() -> {
            // 请求成功但没有数据
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "暂无列表数据")
            }
        }

        else -> {
            // 搜索栏：跨整行
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                SearchBar(
                    Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    onSearch = onHomeSearch
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    modifier = modifier
                ) {
                    // 轮播图：跨整行
                    /*item(span = { GridItemSpan(maxLineSpan) }) {
                        Carousel(
                            products: List<Product>,
                            modifier: Modifier = Modifier
                        )
                    }*/
                    items(uiState.productsData.data) { product ->
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
        }
    }
}
