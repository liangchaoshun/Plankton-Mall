package com.fuyouwentian.planktonmall.ui.detail

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.common.Constants
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    id: String = "",
    onBackClick: () -> Unit = {},
    onCartIconClick: () -> Unit = {},
    onBuyNowClick: () -> Unit = {}
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
            onCartIconClick = onCartIconClick,
            onBuyNowClick = onBuyNowClick
        )
    }
}

@Composable
fun DetailContent(
    modifier: Modifier = Modifier,
    id: String = "",
    onBackClick: () -> Unit = {},
    onCartIconClick: () -> Unit = {},
    onBuyNowClick: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Log.d("Detail", "isMadeOrder: ${uiState.isMadeOrder}")

    LaunchedEffect(id) {
        viewModel.fetchProductDetail(id.trim())
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 加载中和错误状态的处理
        if (uiState.loading || !uiState.error.isNullOrBlank()) {
            ProductDescException(
                uiState = uiState,
                onClickRetry = { viewModel.fetchProductDetail(id) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // 底部留出空间，防止内容被底部悬浮栏遮挡
                contentPadding = PaddingValues(bottom = 42.dp)
            ) {
                // 轮播图
                item { CarouselSection(imageUrls = uiState.product?.bannerUrl ?: emptyList()) }
                // 商品标题和描述
                item { ProductInfoSection(product = uiState.product) }
                // 商品详情描述 (多张图片)
                // 使用 items 直接渲染图片列表，避免嵌套滚动冲突
                items(uiState.product?.descUrl ?: emptyList()) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "product desc",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            // 底部操作栏
            BottomActionBar(
                product = uiState.product,
                modifier = Modifier.align(Alignment.BottomCenter),
                onFavorIconClick = {
                    uiState.product?.let {
                        viewModel.toggleFavorites()
                    }
                },
                onCartIconClick = onCartIconClick, // 跳转到购物车页面
                onClickAdd2Cart = {
                    uiState.product?.let {
                        viewModel.add2Cart(uiState.product!!)
                    }
                },
                onClickBuyNow = {
                    uiState.product?.let {
                        // 下单后再跳路由
                        viewModel.immediatelyMadeAnOrder(uiState.product!!.id)
                        if (uiState.isMadeOrder) onBuyNowClick() // 路由跳转
                    }
                }
            )

            // 蒙版：商品被删除或下架了，售罄不用出现蒙版
            uiState.product?.let { product ->
                if (product.isInvalid) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(0.7f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // 去除波纹
                            ) { Log.d("Detail", "click cover") },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (product.isDeleted) "商品无效" else "商品已下架",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        // 返回按钮
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                //.statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp)
                .align(Alignment.TopStart)
                .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .size(width = 30.dp, height = 28.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }

}


@Composable
fun CarouselSection(
    imageUrls: List<String>
) {
    // 计算轮播图高度：设为屏幕宽度 (正方形)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // 创建 PagerState
    val pagerState = rememberPagerState(pageCount = {
        if (imageUrls.isNotEmpty()) imageUrls.size else 1
    })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenWidth)
            .background(Color.White.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrls.isEmpty()) {
            Text(
                text = "暂无图片",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
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
fun ProductInfoSection(product: Product?) {
    Column(
        modifier = Modifier
            .heightIn(80.dp, 120.dp)
            .fillMaxWidth()
            .background(Color(0xFFF5FFFA))
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        product?.let {
            // 商品标题
            Text(
                text = product.nameZh,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 价格显示
                Text(
                    text = "¥${product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
                if (product.stock < 1) {
                    Text(
                        text = "售罄",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        } ?: Text(
            text = "加载中...",
            style = MaterialTheme.typography.bodyMedium,
        )

    }
}

@Composable
fun ProductDescException(
    uiState: DetailUiState,
    onClickRetry: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small)),
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
                    Text(text = uiState.error)
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                    TextButton(onClick = onClickRetry) { Text("重试") }
                }
            }
        }
    }
}

@Composable
fun BottomActionBar(
    product: Product?,
    modifier: Modifier = Modifier,
    onFavorIconClick: () -> Unit,
    onCartIconClick: () -> Unit,
    onClickAdd2Cart: () -> Unit,
    onClickBuyNow: () -> Unit
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
                    .clickable { onFavorIconClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (product?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "收藏",
                    tint = if (product?.isFavorite == true) Color(0xFFFF9800) else LocalContentColor.current,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "收藏",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .clickable { onCartIconClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "购物车",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "购物车",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                enabled = product?.isInvalid == false && product.stock != 0,
                onClick = onClickAdd2Cart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = "加入购物车",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                enabled = product?.isInvalid == false && product.stock != 0,
                onClick = onClickBuyNow,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = "立即购买",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
