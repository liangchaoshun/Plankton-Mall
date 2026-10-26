package com.fuyouwentian.planktonmall.ui.components.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        style = MaterialTheme.typography.bodyLarge,
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
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp // 阴影
            )
        ) {
            AsyncImage(
                model = record.goodsIcon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .clickable { onProductClick(record.goodsId) },
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
        // TODO 蒙版
    }
}
