package com.fuyouwentian.planktonmall.ui.components.order

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderContent(
    modifier: Modifier = Modifier,
    viewModel: OrderContentViewModel = hiltViewModel(),
    onClickProductPicture: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val orderRecords by viewModel.orderRecords.collectAsStateWithLifecycle()

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
        viewModel.loadData()
    }

    LaunchedEffect(lazyColumnState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it }
            .collect { viewModel.loadMore() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                    TextButton(onClick = viewModel::loadData) {
                        Text(
                            text = "重试",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            else -> {
                // 实验性 API
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.loadData(isRefresh = true) },
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
                        if (orderRecords.isEmpty()) {
                            item(key = "no_data") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimensionResource(R.dimen.padding_small)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "暂无数据",
                                        color = MaterialTheme.colorScheme.outline,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                        items(items = orderRecords, key = { it.id }) { order ->
                            LazyColumnItem(
                                order = order,
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
                        if (!uiState.hasMore && orderRecords.isNotEmpty()) {
                            item(key = "no_more") {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
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
            }
        }
    }
}

@Composable
fun LazyColumnItem(
    order: OrderRecord,
    onClickProductPicture: (String) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // 自动撑满最高子项
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(vertical = dimensionResource(R.dimen.padding_small))
                .padding(
                    start = 0.dp,
                    end = dimensionResource(R.dimen.padding_small)
                ),
            verticalAlignment = Alignment.Top
        )
        {
            AsyncImage(
                model = order.goodsIcon,
                contentDescription = order.goodsNameZh,
                modifier = Modifier
                    .size(width = 100.dp, height = 110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClickProductPicture(order.goodsId) },
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Top,
                )
                {
                    Text(
                        text = order.goodsNameZh,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                    Column(
                        modifier = Modifier.width(100.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "￥${order.goodsPrice}",
                            color = Color(0xFF333333),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "共${order.quantity}件",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = if (order.paymentStatus == 0) "待付款" else "待发货",
                            color = if (order.paymentStatus == 0) Color(0xFFFF9800) else MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.Bottom,
                )
                {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (order.paymentStatus == 0) "应付款" else "实付款",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Column {
                                Row {
                                    Text(
                                        text = "合计",
                                        color = Color(0xFF333333),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "￥${order.actualPay}",
                                        color = Color(0xFF333333),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "订单编号",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = order.id,
                                color = Color(0xFF333333),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
