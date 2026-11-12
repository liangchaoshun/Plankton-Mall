package com.fuyouwentian.planktonmall.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.ProductLite
import com.fuyouwentian.planktonmall.ui.components.widgets.Searchbar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 将 ViewModel 中的 StateFlow 转换为 Compose 可观察的 State
    // collectAsState: app 位于前台/后台都收集
    // collectAsStateWithLifecycle: app 位于后台自动停止收集，省电
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timestamp by viewModel.updateTimestamp.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState() // 实验性 API

    // 初始化
    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    Column(
        modifier = modifier
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
            uiState.loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !uiState.exceptionInfo.isNullOrBlank() -> {
                // 加载失败，显示错误提示和重试按钮
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = uiState.exceptionInfo ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                    TextButton(onClick = viewModel::loadHomeData) {
                        Text(
                            text = "重试",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            else -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFF3E0),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "notice",
                            tint = Color(0xFFFF6B00),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "本应用所使用的商品信息，来源于网络，仅用于学习",
                            color = Color(0xFFFF6B00),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.loadHomeData(isRefresh = true) },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    key(timestamp) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselSection(
    carouselData: List<ProductLite>,
    onClickProduct: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.home_carousel_height) + 15.dp), // 🌟 内容高度 + 上下 padding
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
                    .height(dimensionResource(R.dimen.home_carousel_height))
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

@OptIn(FlowPreview::class)
@Composable
fun HomeContentGrid(
    uiState: HomeUiState,
    onLoadMore: () -> Unit = {},
    onClickProduct: (String) -> Unit = {},
    onClickAdd2Cart: (ProductLite) -> Unit = {}
) {
    val gridState = rememberLazyGridState()

    // 用 snapshotFlow 稳定监听网格的滚动状态
    // 可以抽离成一个扩展 >> https://chat.deepseek.com/share/eaujnyhujc1mqls8d7
    LaunchedEffect(
        gridState,
        uiState.recommendProducts.size,
        uiState.loadingMore,
        uiState.hasMore
    ) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            // 判断是否快滑到底部（倒数第 4 个） >> https://chat.deepseek.com/share/m7mgjry5ezc83nino1
            // 触底加载的阈值：lastVisibleItem != null && lastVisibleItem.index >= totalItems - 4

            /* 动态计算列数，设置触底加载的阈值 */
            // 当前显示的列数
            val currentColumnCount = layoutInfo.maxSpan.coerceAtLeast(1)
            val pageSize = 10 // 分页大小：先固定，Compose UI 不宜从 viewModel 取成员变量
            // 计算动态阈值：提前 2 行，但不能超过 pageSize 的 80%
            val threshold = (currentColumnCount * 2).coerceIn(2, (pageSize * 0.8).toInt())

            lastVisibleItem != null && lastVisibleItem.index >= totalItems - threshold
        }
            .distinctUntilChanged()
            // 加个 200ms 防抖，防止快速滑动时误触
            .debounce(200.milliseconds)
            .collect { isNearEnd ->
                // 符合条件：靠近底部 + 还有更多数据 + 没在加载更多 + 没在下拉刷新 + 没在首屏加载
                if (isNearEnd && uiState.hasMore && !uiState.loadingMore && !uiState.isRefreshing && !uiState.loading) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        state = gridState,
        // 设置每个卡片的最小宽度，比如 160.dp 或 180.dp
        columns = GridCells.Adaptive(minSize = 160.dp),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        // 轮播图
        if (uiState.carouselData.isNotEmpty() && !uiState.isRefreshing) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CarouselSection(
                    carouselData = uiState.carouselData,
                    onClickProduct = onClickProduct
                )
            }
        }

        // 商品列表
        if (uiState.recommendProducts.isEmpty() && !uiState.loading && !uiState.isRefreshing) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无推荐商品",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        } else {
            items(
                items = uiState.recommendProducts,
                key = { product -> product.id }
            ) { product ->
                ProductCard(product, onProductClick = onClickProduct, onAdd2Cart = onClickAdd2Cart)
            }
        }

        // 底部加载更多（无论上面有没有数据，只要在加载就显示）
        if (uiState.loadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "loading_more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        }

        // 到底了
        if (!uiState.hasMore && uiState.recommendProducts.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "no_more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "没有更多了",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
