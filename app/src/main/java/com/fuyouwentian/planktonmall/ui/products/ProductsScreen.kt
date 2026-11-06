package com.fuyouwentian.planktonmall.ui.products

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.common.Constants
import com.fuyouwentian.planktonmall.domain.model.PaginationWithQuery
import com.fuyouwentian.planktonmall.ui.components.widgets.Searchbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    q: String = "",
    onProductClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val isObjectIdStr = Constants.OBJECT_ID_REGEX.matches(q)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf(if (!isObjectIdStr) q else "") }

    // LaunchedEffect 作用：在特定的 Key 变化时，才执行一次副作用（比如网络请求）
    LaunchedEffect(q) {
        // 1. ⚠ 能否从路由判断是从哪个页面跳转而来？请求的 api 不对~，以系列id 查询其下的商品，有专用 api，
        //  不过和“搜索框”统一使用 goods/list 也好，性能差了一些而已！
        // 2. 页面初始化，加载列表数据
        viewModel.fetchProductsData(PaginationWithQuery(q = searchQuery.ifBlank { q.trim() }))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
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

                    Searchbar(
                        query = searchQuery,
                        modifier = Modifier.weight(1f),
                        onValueChangeEvent = { searchQuery = it },
                        onSearch = { qs ->
                            if (qs.isNotBlank()) {
                                viewModel.fetchProductsData(PaginationWithQuery(q = qs.trim()))
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        val finalQuery = searchQuery.ifBlank { q.trim() } // 传入当前搜索参数
        ProductsScreenContent(
            uiState = uiState,
            modifier = modifier.padding(padding),
            onClickRetry = {
                viewModel.fetchProductsData(PaginationWithQuery(q = finalQuery))
            },
            onRefresh = {
                viewModel.fetchProductsData(
                    pagination = PaginationWithQuery(q = finalQuery),
                    isRefresh = true
                )
            },
            onLoadMore = viewModel::loadMore,
            onClickProduct = onProductClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreenContent(
    uiState: ProductsUiState,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onClickProduct: (String) -> Unit = { }
) {
    // 定义下拉刷新状态
    val pullToRefreshState = rememberPullToRefreshState() // 实验性 API
    // 列表上滑加载更多 >> https://chat.deepseek.com/share/0xjyg6gtptec33rjt9
    val lazyColumnState = rememberLazyListState() // 滚动状态控制

    // 监听滚动：当倒数第 3 项可见时预加载（类似 H5 的距底部 100px）
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = lazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = lazyColumnState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.hasMore) {
        if (shouldLoadMore && uiState.hasMore) {
            onLoadMore()
        }
    }

    // 根据状态展示不同 UI
    when {
        uiState.loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        !uiState.error.isNullOrBlank() -> {
            // 加载失败，显示错误提示和重试按钮
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                TextButton(onClick = onClickRetry) {
                    Text(
                        text = "重试",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        uiState.productsData.isEmpty() -> {
            // 请求成功但没有数据
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        else -> {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = lazyColumnState,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    modifier = modifier.padding(top = dimensionResource(R.dimen.padding_small))
                ) {
                    items(uiState.productsData) { product ->
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
                                model = product.iconUrl,
                                contentDescription = product.nameZh,
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
                                    text = product.nameZh,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "¥${product.price}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // 底部状态：加载更多 / 没有更多了
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                uiState.loadingMore -> CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )

                                !uiState.hasMore -> Text(
                                    text = "没有更多了",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
