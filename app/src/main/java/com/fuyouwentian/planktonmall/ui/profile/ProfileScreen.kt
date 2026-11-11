package com.fuyouwentian.planktonmall.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.UserProfile
import com.fuyouwentian.planktonmall.ui.components.favor.FavorContent
import com.fuyouwentian.planktonmall.ui.components.history.HistoryContent
import com.fuyouwentian.planktonmall.ui.components.order.OrderContent
import com.fuyouwentian.planktonmall.ui.components.userInfo.UserInfoContent
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigate2Login: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initScreen() // 初始化
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
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        TextButton(onClick = viewModel::initScreen) {
                            Text(
                                text = "重试",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            !uiState.hasToken -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "请先登录，以获取数据",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        Button(
                            onClick = onNavigate2Login,
                            modifier = Modifier.size(width = 90.dp, height = 35.dp),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "去登录",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            else -> {
                ProfileContent(
                    uiState = uiState,
                    onClickProduct = onProductClick,
                    loadUserInfo = viewModel::loadUserInfo,
                    onLogout = viewModel::logout
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onClickProduct: (String) -> Unit = {},
    loadUserInfo: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val tabs = listOf("订单", "足迹", "收藏", "个人信息")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(
                start = dimensionResource(R.dimen.padding_small),
                end = dimensionResource(R.dimen.padding_small),
                top = dimensionResource(R.dimen.padding_small),
                bottom = 0.dp
            )
    ) {
        // 头部
        ProfileHeader(
            profile = uiState.profile,
            onRefresh = loadUserInfo,
            onLogout = onLogout
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        // 中部
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray) },
            modifier = Modifier.height(35.dp) // 固定高度
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
                    text = {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    modifier = Modifier.height(35.dp) // 让 Tab 撑满
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
            userScrollEnabled = false, // 禁用左右滑动切换
            // 即使为 0，但若允许滑动切换，还是可能预加载和组装目的 tab，
            // 请查看：https://chat.deepseek.com/share/os7g9b6litccyixnja
            beyondViewportPageCount = 0
        ) { page ->
            when (page) {
                0 -> OrderContent(onClickProductPicture = onClickProduct)
                1 -> HistoryContent(onClickProduct = onClickProduct)
                2 -> FavorContent(onClickProductPicture = onClickProduct)
                3 -> UserInfoContent(userInfo = uiState.profile)
            }
        }
    }
}

@Composable
fun ProfileHeader(
    profile: UserProfile?,
    onRefresh: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var pillVision by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .background(
                Brush.horizontalGradient(
                    0.0f to Color(0xFF885F84),
                    0.2f to Color(0xFF8D6E8A),
                    1.0f to Color(0xFFF8F9FA)
                )
            )
            .padding(dimensionResource(R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = profile?.avatar,
            contentDescription = "avatar",
            modifier = Modifier
                .size(60.dp)
                // 注意 modifier 书写顺序，必须把 shadow 和 clip 放在一起
                .shadow( // 阴影
                    elevation = 4.dp,
                    shape = CircleShape,
                    clip = false // false，防止阴影被自身裁剪
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.default_image),
            placeholder = painterResource(R.drawable.default_image)
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
        Text(
            text = profile?.nickname ?: "--",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "more",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { pillVision = true }
            )
            // DropdownMenu 虽然以 Box 为父容器，但实际上会在屏幕的最上层（Overlay）单独开辟一个窗口来渲染
            // Android 系统的 Popup 默认行为：以父容器的左下角为基准点，将弹出菜单的左上角与之对齐，并且向下偏移一点点（通常是 8dp，避免和点击区域重叠）。
            // 当前父容器 Box 的尺寸就是 Icon 的 size
            // 对齐方式，最终根据 DropdownMenu 与屏幕边缘计算，向左还是向右延申
            DropdownMenu(
                expanded = pillVision,
                onDismissRequest = { pillVision = false }, // 点击外部或返回键关闭
                modifier = Modifier
                    .background(Color.White)
                    .width(100.dp)
            ) {
                // 刷新
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pillVision = false
                            onRefresh()
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "刷新个人信息",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                // 退出登录
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pillVision = false
                            onLogout()
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "退出登录",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}