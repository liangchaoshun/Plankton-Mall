package com.fuyouwentian.planktonmall.ui.category

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.data.model.Series
import com.fuyouwentian.planktonmall.ui.home.UiEvent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.fuyouwentian.planktonmall.data.model.Category

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    onSeriesClick: (String) -> Unit = {},
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val categories = uiState.categoryData.data

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    Row(modifier = Modifier.fillMaxSize()) {
        LeftCategoryMenu(
            categories = categories,
            selectedIndex = selectedIndex,
            onCategorySelected = { newIndex -> selectedIndex = newIndex }
        )
        RightSeriesArea(
            series = categories[selectedIndex].series_data,
//            onClickSeries = onSeriesClick
        )
    }
}

@Composable
fun LeftCategoryMenu(
    categories: List<Category>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .width(100.dp) // 左侧固定宽度
            .fillMaxHeight()
            .background(Color(0xFFF5F5F5)) // 默认灰色背景
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(if (isSelected) Color.White else Color.Transparent) // 选中变白
                    .clickable { onCategorySelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name_zh,
                    color = if (isSelected) Color.Red else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RightSeriesArea(
    series: List<Series>,
    onClickSeries: (String) -> Unit = { seriesId -> Log.i("Series_Click", seriesId) }
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        maxItemsInEachRow = 3 // 每行 3 个
    ) {
        series.forEach { item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = item.icon_url,
                    contentDescription = item.name_zh,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onClickSeries(item.id) },
                    contentScale = ContentScale.Crop
                )
                Text(text = item.name_zh)
            }
        }
    }
}
