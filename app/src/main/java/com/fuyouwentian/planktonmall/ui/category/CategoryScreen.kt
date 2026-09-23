package com.fuyouwentian.planktonmall.ui.category

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.data.model.Category
import com.fuyouwentian.planktonmall.data.model.Series
import com.fuyouwentian.planktonmall.ui.home.UiEvent

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    onSeriesClick: (String) -> Unit = {},
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
//    val categories = MockCategories.data // mock data
    val categories = uiState.categoryData.data

    LaunchedEffect(Unit) {
        if (categories.isNotEmpty() && selectedIndex >= categories.size) {
            selectedIndex = 0 // 重置为第一个
        }
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
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
                Text(text = "加载失败: ${uiState.error}")
                // 可以加一个重试按钮调用 viewModel.initHandler() 或重新请求 TODO
            }
        }

        categories.isEmpty() -> {
            // 请求成功但没有数据
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "暂无分类数据")
            }
        }

        else -> {
            // 正常展示数据
            Row(modifier = Modifier.fillMaxSize()) {
                CategoryMenu(
                    categories = categories,
                    selectedIndex = selectedIndex,
                    onCategorySelected = { newIndex -> selectedIndex = newIndex }
                )
                // 安全索引判断
                val currentSafeIndex = if (selectedIndex in categories.indices) selectedIndex else 0
                val currentSeries =
                    categories.getOrNull(currentSafeIndex)?.series_data ?: emptyList()

                SeriesArea(
                    series = currentSeries,
                    onClickSeries = onSeriesClick
                )
            }
        }
    }
}

@Composable
fun CategoryMenu(
    categories: List<Category>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
            .background(Color(0xFFF5F5F5))
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(if (isSelected) Color(0xFFFAF8FE) else Color.Transparent)
                    .clickable { onCategorySelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name_zh,
                    color = if (isSelected) Color.Red else Color.Black
                )
            }
        }
    }
}

@Composable
fun SeriesArea(
    series: List<Series>,
    onClickSeries: (String) -> Unit = { seriesId -> Log.i("Series_Click", seriesId) }
) {
    // 注意：不要在 LazyColumn 里面套 LazyVerticalGrid，否则会报错，两者选其一即可
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(series) { item ->
            Column(
                modifier = Modifier
                    .clickable { onClickSeries(item.id) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = item.icon_url,
                    contentDescription = item.name_zh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = item.name_zh,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    CategoryScreen()
}*/
