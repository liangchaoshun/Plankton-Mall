package com.fuyouwentian.planktonmall.ui.components.history

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.SortedMap

@Composable
fun HistoryContent(
    histories: SortedMap<String, List<HistoryRecord>> = sortedMapOf(),
    onClickProduct: (String) -> Unit = {},
) {
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(dimensionResource(R.dimen.padding_small)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
    ) {
        histories.forEach { (key, records) ->
            item(
                key = "header_$key",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_medium))
                ) {
                    Text(
                        text = if (key == today) "今天" else key,
                        color = Color(0xFF333333),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            items(
                items = records,
                key = { record -> "${key}_${record.goodsId}" }
            ) { record ->
                RecordCard(
                    record = record,
                    onProductClick = onClickProduct
                )
            }
        }
    }
}

@Composable
fun RecordCard(
    record: HistoryRecord,
    onProductClick: (String) -> Unit = {},
) {
    val isInvalid = !record.isValid || record.goodsOnSale == 0 || record.goodsStock < 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 阴影
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                AsyncImage(
                    model = record.goodsIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .clickable(enabled = !isInvalid) { onProductClick(record.goodsId) },
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = record.goodsNameZh,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                )
            }
            // 蒙版
            if (isInvalid) {
                Box(
                    modifier = Modifier
                        .matchParentSize() // 只能在 BoxScope 中使用（父级是 Box）
                        .clip(RoundedCornerShape(8.dp)) // 对应 Card 圆角
                        .background(Color.White.copy(0.7f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // 去除波纹
                        ) { Log.d("HistoryContentCard", "click cover") },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (!record.isValid) "无效" else if (record.goodsOnSale == 0) "下架" else "无货",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
