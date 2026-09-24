package com.fuyouwentian.planktonmall.ui.products

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.common.Constants
import com.fuyouwentian.planktonmall.domain.model.ProductsRequest
import com.fuyouwentian.planktonmall.domain.model.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    q: String = "",
    onProductClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val isQId = Constants.OBJECT_ID_REGEX.matches(q)
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf(if (!isQId) q else "") }

    // LaunchedEffect 作用：在特定的 Key 变化时，才执行一次副作用（比如网络请求）
    LaunchedEffect(q) {
        // 页面初始化
        viewModel.fetchDataHandler(ProductsRequest(q = q.trim()))
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                // shadowElevation = 2.dp, // 底部分层阴影
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    SearchBar(
                        query = searchQuery,
                        modifier = Modifier.weight(1f),
                        onChangeEvent = { searchQuery = it },
                        onSearch = { qs ->
                            viewModel.fetchDataHandler(
                                ProductsRequest(
                                    pageIndex = 1,
                                    pageSize = 10,
                                    q = qs.trim()
                                )
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        ProductsScreenContent(
            uiState = uiState,
            modifier = modifier.padding(padding),
            onClickRetry = {
                val finalQuery = searchQuery.ifBlank { q } // 传入当前搜索参数
                viewModel.fetchDataHandler(
                    ProductsRequest(
                        pageIndex = 1,
                        pageSize = 10,
                        q = finalQuery.trim()
                    )
                )
            },
            onClickProduct = onProductClick
        )
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String = "",
    onChangeEvent: (String) -> Unit = {},
    onSearch: (String) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = { str -> onChangeEvent(str.trim()) },
        leadingIcon = if (query.isEmpty()) {
            { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
        } else null,
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
                if (query.isNotBlank()) onSearch(query)
                keyboardController?.hide()
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

@Composable
fun ProductsScreenContent(
    uiState: ProductsUiState,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit = {},
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
                Text(text = "暂无数据")
            }
        }

        else -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                modifier = modifier.padding(top = dimensionResource(R.dimen.padding_medium))
            ) {
                items(uiState.productsData.data) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min) // 自动撑满最高子项
                            .clickable { onClickProduct(product.id) }
                            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        verticalAlignment = Alignment.Top
                    ) {
                        AsyncImage(
                            model = product.icon_url,
                            contentDescription = product.name_zh,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = product.name_zh,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "¥${product.price}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

