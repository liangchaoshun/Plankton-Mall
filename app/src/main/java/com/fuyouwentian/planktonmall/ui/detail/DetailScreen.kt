package com.fuyouwentian.planktonmall.ui.detail

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.common.Constants
import com.fuyouwentian.planktonmall.domain.model.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    id: String = "",
    onBackClick: () -> Unit = {}
) {
    val isQId = Constants.OBJECT_ID_REGEX.matches(id)
    if (!isQId) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "参数错误")
        }
    } else {
        DetailContent(
            modifier = modifier,
            id = id,
            onBackClick = onBackClick,
        )
    }
}

@Composable
fun DetailContent(
    modifier: Modifier = Modifier,
    id: String = "",
    onBackClick: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.fetchProductDetail(id.trim())
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 根布局使用 Box，实现悬浮效果
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // 底部留出空间，防止内容被底部悬浮栏遮挡
            contentPadding = PaddingValues(bottom = 42.dp)
        ) {
            // 轮播图
            item {
                CarouselSection(imageUrls = uiState.product?.banner_url ?: emptyList())
            }

            // 商品标题和描述
            item {
                ProductInfoSection(uiState = uiState)
            }

            // 商品详情描述 (多张图片)
            // 使用 items 直接渲染图片列表，避免嵌套滚动冲突
            if (!uiState.loading && uiState.error.isNullOrBlank()) {
                items(uiState.product?.desc_url ?: emptyList()) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "product desc",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            // 加载中和错误状态的处理
            if (uiState.loading || !uiState.error.isNullOrBlank()) {
                item {
                    ProductDescException(
                        uiState = uiState,
                        onClickRetry = { viewModel.fetchProductDetail(id) }
                    )
                }
            }
        }

        // 返回按钮
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp)
                .align(Alignment.TopStart)
                .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
            )
        }

        // 底部操作栏
        BottomActionBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onFavorClick = { },
            onCartClick = { },
            onAddToCartClick = { },
            onBuyNowClick = { }
        )
    }
}


@Composable
fun CarouselSection(
    imageUrls: List<String>
) {
    // 计算轮播图高度：设为屏幕宽度 (正方形)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val carouselHeight = screenWidth

    // 创建 PagerState
    val pagerState = rememberPagerState(pageCount = {
        if (imageUrls.isNotEmpty()) imageUrls.size else 1
    })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(carouselHeight)
            .background(Color.White.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrls.isEmpty()) {
            Text(text = "暂无图片", color = Color.White)
        } else {
            // HorizontalPager：每页铺满整个 Box
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { i ->
                AsyncImage(
                    model = imageUrls[i],
                    contentDescription = "轮播图_$i",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 轮播图指示器 (小圆点)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(imageUrls.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ProductInfoSection(uiState: DetailUiState) {
    val product = uiState.product

    Column(
        modifier = Modifier
            .heightIn(80.dp, 120.dp)
            .fillMaxWidth()
            .background(Color(0xFFF5FFFA))
            .padding(16.dp)
    ) {
        if (product != null) {
            // 商品标题
            Text(
                text = product.name_zh,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            // 价格显示
            Text(
                text = "¥${product.price}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336)
            )
        } else {
            Text(text = "加载中...", fontSize = 18.sp, color = Color.Red)
        }
    }
}

@Composable
fun ProductDescException(
    uiState: DetailUiState,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(400.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.loading -> {
                CircularProgressIndicator()
            }

            !uiState.error.isNullOrBlank() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "加载失败: ${uiState.error}")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onClickRetry) { Text("重试") }
                }
            }

            else -> {
                Text(text = "详情加载中...")
            }
        }
    }
}

@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    onFavorClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onBuyNowClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF0F8FF),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .clickable { onFavorClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "收藏",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "收藏",
                    fontSize = 10.sp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .clickable { onCartClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "购物车",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "购物车",
                    fontSize = 10.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onAddToCartClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Text(text = "加入购物车", color = Color.White, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onBuyNowClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Text(text = "立即购买", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
