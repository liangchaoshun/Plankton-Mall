package com.fuyouwentian.planktonmall.ui.components.favor

import android.widget.Toast
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavorContent(
    modifier: Modifier = Modifier,
    viewModel: FavorContentViewModel = hiltViewModel(),
    onClickProductPicture: (String) -> Unit = {},
    onAdd2Cart: (FavorRecord) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favorItems by viewModel.favorItems.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var revealedProductId by remember { mutableStateOf<String?>(null) } // 记录当前滑出的商品 ID

    // 定义下拉刷新状态
    val pullToRefreshState = rememberPullToRefreshState() // 实验性 API
    val lazyColumnState = rememberLazyListState()

    // 触底加载更多
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = lazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            val total = lazyColumnState.layoutInfo.totalItemsCount
            total > 0 && last.index >= total - 2
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(lazyColumnState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it }
            .collect { viewModel.loadMore() }
    }

    // 监听 LazyColumn 的滚动状态，如果在滑动，就收起已滑出的 Item
    LaunchedEffect(lazyColumnState.isScrollInProgress) {
        if (lazyColumnState.isScrollInProgress) {
            revealedProductId = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !uiState.error.isNullOrBlank() -> {
                // 加载失败，显示错误提示和重试按钮
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        TextButton(onClick = viewModel::loadFavorites) {
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
                    onRefresh = { viewModel.loadFavorites(isRefresh = true) },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = lazyColumnState,
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        modifier = modifier
                            .fillMaxSize()
                            .padding(top = dimensionResource(R.dimen.padding_medium))
                    ) {
                        if (favorItems.isEmpty()) {
                            item(key = "no_data") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimensionResource(R.dimen.padding_small)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "暂无数据",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                        items(items = favorItems, key = { it.goodsId }) { favor ->
                            LazyColumnItem(
                                favor = favor,
                                onAdd2Cart = onAdd2Cart,
                                isRevealed = revealedProductId == favor.goodsId,
                                onRevealedChange = { isRevealed ->
                                    revealedProductId = if (isRevealed) favor.goodsId else null
                                },
                                cancelFavorite = viewModel::cancelFavorite,
                                onClickProductPicture = onClickProductPicture
                            )
                        }
                        // 底部加载更多（无论上面有没有数据，只要在加载就显示）
                        if (uiState.loadingMore) {
                            item(key = "loading_more") {
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
                        if (!uiState.hasMore && favorItems.isNotEmpty()) {
                            item(key = "no_more") {
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
            }
        }
    }
}

// 定义滑动状态
enum class DragValue {
    Settled, // 初始状态，未露出按钮
    Revealed // 露出按钮状态
}

@Composable
fun LazyColumnItem(
    favor: FavorRecord,
    isRevealed: Boolean = false,
    onRevealedChange: (Boolean) -> Unit,
    onAdd2Cart: (FavorRecord) -> Unit = {},
    cancelFavorite: (String) -> Unit = {},
    onClickProductPicture: (String) -> Unit = {},
) {
    val density = LocalDensity.current
    val actionWidth = 60.dp // 按钮区域的总宽度（例如一个按钮，宽 60dp）
    val actionWidthPx = with(density) { actionWidth.toPx() }

    // 创建 AnchoredDraggableState
    val dragState = remember(favor.goodsId) {
        AnchoredDraggableState(initialValue = DragValue.Settled)
    }

    // 设置锚点（Anchors），为何使用监听的方式：https://chat.deepseek.com/share/hv5sfio9udh1lzjygi
    // Settled 对应 offset = 0，Revealed 对应 offset = -actionWidthPx
    LaunchedEffect(actionWidthPx) {
        // 设置两个槽点
        dragState.updateAnchors(
            DraggableAnchors {
                DragValue.Settled at 0f
                DragValue.Revealed at -actionWidthPx
            }
        )
    }

    // 监听外部传入的 isRevealed，如果为 false 且当前是 Revealed，则收起
    LaunchedEffect(isRevealed) {
        if (!isRevealed && dragState.currentValue == DragValue.Revealed) {
            dragState.animateTo(DragValue.Settled)
        }
    }

    // 内部滑动结束后，通知外部
    LaunchedEffect(dragState.currentValue) {
        if (!dragState.isAnimationRunning) {
            val revealed = dragState.currentValue == DragValue.Revealed
            if (revealed != isRevealed) {
                onRevealedChange(revealed)
            }
        }
    }

    // 计算当前是否真的处于滑出状态（用于控制点击）
    // 注意：这里用 targetValue 而不是 currentValue，因为动画过程（包括处于锚点）中也要禁止点击
    val isCurrentlyRevealed = dragState.targetValue == DragValue.Revealed

    Box(modifier = Modifier.fillMaxWidth()) {
        val isInvalid =
            !favor.isValid || favor.goodsOnSale == 0 || favor.goodsStock < 1
        // 背景层
        Row(
            modifier = Modifier
                .matchParentSize() // 不参与 Box 的高度测量，而是等前景层测量完，直接强行和前景层一样大
                .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small))),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 取消收藏
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .background(Color(0xFFF40E21))
                    .clickable(enabled = isRevealed) { cancelFavorite(favor.goodsId) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "取消\n收藏",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        // 前景层，应用 anchoredDraggable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // 自动撑满最高子项
                .offset { // 根据偏移量移动
                    // 判断锚点是否已初始化，避免在 LaunchedEffect 执行前读取 offset 导致崩溃
                    val offset = if (dragState.anchors.size > 0) dragState.requireOffset() else 0f
                    IntOffset(if (offset.isNaN()) 0 else offset.roundToInt(), 0)
                }
                .anchoredDraggable( // 先接收手势
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                        state = dragState,
                        positionalThreshold = { distance ->
                            if (distance.isNaN() || distance == 0f) actionWidthPx / 2
                            else distance * 0.5f
                        },
                        animationSpec = spring()
                    )
                )
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White) // 确保放 .offset 之后，否则背景覆盖背景层
                .padding(vertical = dimensionResource(R.dimen.padding_small))
                .padding(
                    start = 0.dp,
                    end = dimensionResource(R.dimen.padding_small)
                ),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.size(100.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AsyncImage(
                        model = favor.goodsIcon,
                        contentDescription = favor.goodsNameZh,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                enabled = !isInvalid && !isCurrentlyRevealed,
                                onClick = { onClickProductPicture(favor.goodsId) }),
                        contentScale = ContentScale.Crop,
                    )
                    if (isInvalid) {
                        Box(
                            modifier = Modifier
                                .height(25.dp)
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.6f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (!favor.isValid) "无效" else if (favor.goodsOnSale == 0) "下架" else "无货",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = favor.goodsNameZh,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "￥${favor.goodsPrice}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!isInvalid) {
                        IconButton(
                            onClick = {
                                if (isRevealed) onRevealedChange(false) else onAdd2Cart(favor)
                            },
                            modifier = Modifier.size(20.dp) // 默认 48dp
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
    }
}