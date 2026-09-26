package com.fuyouwentian.planktonmall.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 将 ViewModel 中的 StateFlow 转换为 Compose 可观察的 State
    // collectAsState: 前台/后台都收集
    // collectAsStateWithLifecycle: 后台自动停止收集，省电
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 初始化加载
    LaunchedEffect(Unit) {
        viewModel.fetchCarouselData() // 加载轮播图数据
        viewModel.fetchProductsData() // 加载列表数据
    }

    // LaunchedEffect 作用：在特定的 Key 变化时，才执行一次副作用（比如网络请求）
    // 订阅事件
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast
                        .makeText(
                            context, event.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            }
        }
    }

    Column(modifier = modifier) {
        Searchbar(onSearch = onSearch)
        CarouselSection(
            uiState = uiState,
            onClickRetry = viewModel::fetchCarouselData,
            onClickProduct = onProductClick
        )
        ProductGridSection(
            uiState = uiState,
            modifier = modifier.weight(1f),
            onClickRetry = viewModel::fetchProductsData,
            onLoadMore = viewModel::loadMore,
            onClickProduct = onProductClick
        )
    }
}

@Composable
fun Searchbar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        leadingIcon = if (query.isEmpty()) {
            { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
        } else null,
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = {
                    onSearch(query.trim())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselSection(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit = {},
    onClickProduct: (String) -> Unit = {}
) {
    val carouselHeight = 150.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(carouselHeight + 30.dp), // 内容高度 + 上下 padding
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.loadingCarousel -> {
                CircularProgressIndicator()
            }

            !uiState.carouselError.isNullOrBlank() -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant), // 淡灰，深色模式自动适配
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "加载失败: ${uiState.carouselError}")
                    TextButton(onClick = onClickRetry) { Text("重试") }
                }
            }

            uiState.carouselData.isEmpty() -> {
                // 空列表也占位，保持布局
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无推荐",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                HorizontalMultiBrowseCarousel(
                    state = rememberCarouselState { uiState.carouselData.count() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    preferredItemWidth = 150.dp,
                    itemSpacing = 8.dp,
                ) { i ->
                    val item = uiState.carouselData[i]
                    AsyncImage(
                        model = item.cover,
                        contentDescription = item.name_zh,
                        modifier = Modifier
                            .height(carouselHeight)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable { onClickProduct(item.id) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridSection(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onClickProduct: (String) -> Unit = {}
) {
    // 根据状态展示不同 UI
    when {
        uiState.loadingHomeList -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        !uiState.productsError.isNullOrBlank() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "加载失败: ${uiState.productsError}")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onClickRetry) { Text("重试") }
                }
            }
        }

        uiState.productsData.data.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "暂无商品")
            }
        }

        else -> {
            val gridState = rememberLazyGridState()
            // 触底加载更多
            val shouldLoadMore by remember {
                derivedStateOf {
                    val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                        ?: return@derivedStateOf false
                    val total = gridState.layoutInfo.totalItemsCount
                    total > 0 && last.index >= total - 3
                }
            }
            LaunchedEffect(gridState) {
                snapshotFlow { shouldLoadMore }
                    .distinctUntilChanged()
                    .filter { it }
                    .collect { onLoadMore() }
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                /*contentPadding = PaddingValues(
                    horizontal = dimensionResource(R.dimen.padding_small),
                    vertical = dimensionResource(R.dimen.padding_small)
                ),
                modifier = modifier*/
            ) {
                items(
                    items = uiState.productsData.data,
                    key = { product -> product.id }
                ) { product ->
                    AsyncImage(
                        model = product.cover,
                        contentDescription = product.name_zh,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onClickProduct(product.id) },
                        contentScale = ContentScale.Crop
                    )
                }

                if (uiState.loadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "loading_more") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.height(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                if (!uiState.hasMore && uiState.productsData.data.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "no_more") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("没有更多了", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
