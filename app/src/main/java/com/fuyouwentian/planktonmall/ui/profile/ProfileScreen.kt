package com.fuyouwentian.planktonmall.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.model.UserProfile
import com.fuyouwentian.planktonmall.ui.components.favor.FavorContent
import com.fuyouwentian.planktonmall.ui.components.history.HistoryContent
import com.fuyouwentian.planktonmall.ui.components.order.OrderContent
import com.fuyouwentian.planktonmall.ui.components.userInfo.UserInfoContent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigate2Login: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initScreen() // 初始化
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        when {
            uiState.loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !uiState.error.isNullOrBlank() -> {
                // 加载失败，显示错误提示和重试按钮
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        TextButton(onClick = viewModel::initScreen) { Text("重试") }
                    }
                }
            }

            !uiState.isLoggedIn -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "请先登录，以获取数据",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        Button(
                            onClick = { onNavigate2Login() },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                        ) {
                            Text(
                                text = "去登录",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            else -> {
                ProfileContent(
                    uiState = uiState,
                    loadOrders = viewModel::loadOrders,
                    loadBrowsingHistory = viewModel::loadBrowsingHistory,
                    loadFavorites = viewModel::loadFavorites
                )
            }
        }

    }
}

@Composable
fun ProfileHeader(profile: UserProfile?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)))
            .background(Color.White)
            .padding(dimensionResource(R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = profile?.avatar,
            contentDescription = "avatar",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.default_image),
            placeholder = painterResource(R.drawable.default_image)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = profile?.nickname ?: "无敌三花小彩狸",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    loadOrders: () -> Unit = {},
    loadBrowsingHistory: () -> Unit = {},
    loadFavorites: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val tabs = listOf("订单", "足迹", "收藏", "个人信息")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // 监听页面切换，触发对应 Tab 的初始化请求
    // 利用 snapshotFlow 确保只有真正切换到该页才触发
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                when (page) {
                    0 -> loadOrders()
                    1 -> loadBrowsingHistory()
                    2 -> loadFavorites()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(
                start = dimensionResource(R.dimen.padding_small),
                end = dimensionResource(R.dimen.padding_small),
                top = dimensionResource(R.dimen.padding_small),
                bottom = 0.dp
            )
    ) {
        // 头部
        ProfileHeader(profile = uiState.profile)
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        // 中部
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        // 点击 Tab，平滑滚动到对应的 Pager 页面
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }

        // 底部：Pager 内容区
        // HorizontalPager 左右滑动，配合水平 Tabs，达到水平页面横向切换效果
        // VerticalPager 上下滑动，通常用于全屏沉浸式的内容，如：全屏看板、引导页、上下切换视频
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            // beyondViewportPageCount = 0 // 取消预加载组组件，避免没必要的网络请求，但会略微影响滑动流畅度
        ) { page ->
            when (page) {
                0 -> OrderContent()
                1 -> HistoryContent()
                2 -> FavorContent()
                3 -> UserInfoContent()
            }
        }
    }
}