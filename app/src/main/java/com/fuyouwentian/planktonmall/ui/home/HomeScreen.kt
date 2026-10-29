package com.fuyouwentian.planktonmall.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.ui.components.widgets.Searchbar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter


@OptIn(ExperimentalMaterial3Api::class)
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

    // 定义下拉刷新状态
    val pullToRefreshState = rememberPullToRefreshState() // 实验性 API

    // 初始化
    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

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

                else -> {}
            }
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFFF5F6FA))
            //.background(Color.Cyan)
            .padding(
                start = dimensionResource(R.dimen.padding_small),
                end = dimensionResource(R.dimen.padding_small),
                top = dimensionResource(R.dimen.padding_small),
                bottom = 0.dp
            )
    ) {
        var searchQuery by rememberSaveable { mutableStateOf("") }
        Searchbar(
            query = searchQuery,
            onValueChangeEvent = { searchQuery = it },
            onSearch = onSearch
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        when {
            uiState.loading && uiState.recommendProducts.data.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !uiState.exceptionInfo.isNullOrBlank() && uiState.recommendProducts.data.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = uiState.exceptionInfo!!)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::loadHomeData) {
                            Text(
                                text = "重试",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            else -> {
                // 实验性 API
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.loadHomeData(isRefresh = true) },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeContentGrid(
                        uiState = uiState,
                        onLoadMore = viewModel::loadMore,
                        onClickProduct = onProductClick,
                        onClickAdd2Cart = viewModel::add2Cart
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselSection(
    carouselData: List<ProductLite>,
    onClickProduct: (String) -> Unit = {}
) {
    val carouselHeight = 150.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(carouselHeight + 15.dp), // 🌟 内容高度 + 上下 padding
        contentAlignment = Alignment.Center
    ) {
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { carouselData.count() },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            preferredItemWidth = 150.dp,
            itemSpacing = 8.dp,
        ) { i ->
            val item = carouselData[i]
            AsyncImage(
                model = item.cover,
                contentDescription = item.nameZh,
                modifier = Modifier
                    .height(carouselHeight)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onClickProduct(item.id) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductLite,
    onProductClick: (String) -> Unit = {},
    onAdd2Cart: (ProductLite) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp // 阴影
        )
    ) {
        AsyncImage(
            model = product.cover,
            contentDescription = product.nameZh,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .clickable { onProductClick(product.id) },
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
            Text(
                text = product.nameZh,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = Color(0xFF333333)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "￥${product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onAdd2Cart(product) },
                    modifier = Modifier.size(28.dp) // 默认 48dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "加入购物车",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContentGrid(
    uiState: HomeUiState,
    onLoadMore: () -> Unit = {},
    onClickProduct: (String) -> Unit = {},
    onClickAdd2Cart: (ProductLite) -> Unit = {}
) {
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

    // LazyVerticalGrid 设计哲学是：一个可以按顺序添加无数块（item）的容器。
    // item 是顺序的、平级的、不互斥
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
    ) {
        // 轮播图
        if (uiState.carouselData.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CarouselSection(
                    carouselData = uiState.carouselData,
                    onClickProduct = onClickProduct
                )
            }
        }

        // 商品列表
        if (uiState.recommendProducts.data.isEmpty()) {
            // 如果商品为空，占据一整行显示提示
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), // 固定高度，避免下拉刷新时布局抖动
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无推荐商品",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        } else {
            items(
                items = uiState.recommendProducts.data,
                key = { product -> product.id }
            ) { product ->
                ProductCard(product, onProductClick = onClickProduct, onAdd2Cart = onClickAdd2Cart)
            }
        }

        // 底部加载更多（无论上面有没有数据，只要在加载就显示）
        if (uiState.loadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "loading_more") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        Modifier.height(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // 到底了（放在最外层）
        if (!uiState.hasMore && uiState.recommendProducts.data.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "no_more") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "没有更多了",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
