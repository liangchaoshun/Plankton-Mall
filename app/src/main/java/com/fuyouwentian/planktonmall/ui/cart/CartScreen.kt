package com.fuyouwentian.planktonmall.ui.cart

import android.widget.Toast
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

// 定义滑动状态
enum class DragValue {
    Settled, // 初始状态，未露出按钮
    Revealed // 露出按钮状态
}

// 购物车数据没分页，一次性全部查回来
@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    afterClickSettleBtn: () -> Unit = {},
    onNavigate2Login: () -> Unit = {},
    onNavigate2Pickup: () -> Unit = {},
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartRecords by viewModel.cartRecords.collectAsStateWithLifecycle()
    var revealedProductId by remember { mutableStateOf<String?>(null) } // 记录当前滑出的商品 ID
    val lazyColumnState = rememberLazyListState()

    // cartRecords 是缓存值，当 key1，即 cartRecords 发生引用变化（即数据更新）时，会重新排序
    val sortedCartRecords = remember(cartRecords) {
        // 即使 cartRecords 为 emptyList()，也会调用 sortedWith 方法，返回结果也为 emptyList()
        cartRecords.sortedWith(
            compareByDescending<CartProduct> { it.createTime } // 第一排序字段（倒序）
                .thenBy { it.id } // 若第一排序字段相同，则使用第二排序字段（正序），一次类推 .thenBy
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchCartData()
    }

    // 监听 LazyColumn 的滚动状态，如果在滑动，就收起已滑出的 Item
    LaunchedEffect(lazyColumnState.isScrollInProgress) {
        if (lazyColumnState.isScrollInProgress) {
            revealedProductId = null
        }
    }

    LaunchedEffect(uiState.isMakeOrderDone) {
        if (uiState.isMakeOrderDone) afterClickSettleBtn()
    }

    Surface(modifier = modifier) {
        // 根据状态展示不同 UI
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
                    TextButton(onClick = viewModel::fetchCartData) {
                        Text(
                            text = "重试",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
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

            cartRecords.isEmpty() -> {
                // 请求成功但没有数据
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty_cart),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "购物车空空如也",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        Button(
                            onClick = { onNavigate2Pickup() },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                        ) {
                            Text(
                                text = "去挑选",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F6FA))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            // 监听点击事件，清除滑出状态
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    revealedProductId = null
                                }
                            }
                    ) {
                        LazyColumn(
                            state = lazyColumnState,
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                            modifier = modifier.fillMaxSize()
                        ) {
                            items(sortedCartRecords, key = { it.id }) { product ->
                                SwipeableCartItem(
                                    product = product,
                                    isRevealed = revealedProductId == product.id,
                                    onRevealedChange = { isRevealed ->
                                        revealedProductId = if (isRevealed) product.id else null
                                    },
                                    onProductClick = onProductClick,
                                    onToggleSelect = viewModel::toggleSelect,
                                    onQuantityChange = viewModel::changeQuantity,
                                    onCollect = viewModel::add2Favorites,
                                    onDelete = viewModel::deleteCartRecords,
                                    onSyncClick = viewModel::syncCartData2Server
                                )
                            }
                        }
                    }

                    // 底部结算栏
                    SettleColumn(
                        cartRecords = sortedCartRecords,
                        committing = uiState.committingOrder,
                        makeOrder = viewModel::makeOrder,
                        onToggleSelectAll = viewModel::toggleSelectAll
                    )
                }
            }
        }
    }
}

@Composable
fun NumberStepper(
    value: Int,
    onChangeQuantity: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isValid: Boolean = true,
) {
    var text by remember { mutableStateOf(value.toString()) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // 外部 value 变化（点 +/-）时同步输入框
    LaunchedEffect(value) {
        if (text.toIntOrNull() != value) {
            text = value.toString()
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        // 减号
        Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "minus",
            tint = if (value > 1) Color(0xFF333333) else Color(0xFFCCCCCC), // 手动变灰
            modifier = Modifier
                .size(dimensionResource(R.dimen.padding_medium))
                .clickable(enabled = isValid && value > 1) { onChangeQuantity(value - 1) }
        )

        // 数字显示区
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFF2F2F7),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small))
                )
                .padding(
                    horizontal = dimensionResource(R.dimen.padding_medium),
                    vertical = dimensionResource(R.dimen.padding_small)
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = text,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    text = digits
                    digits.toIntOrNull()?.let {
                        if (it in 1 until 1000) {
                            onChangeQuantity(it)
                        } else {
                            text = value.toString()
                            Toast.makeText(context, "限制数量：1~999", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                enabled = isValid,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                ),
                modifier = Modifier
                    .width(25.dp)
                    .onFocusChanged { state ->
                        if (!state.isFocused && text.isEmpty()) {
                            text = value.toString()
                        }
                    }
            )
        }

        // 加号
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "plus",
            tint = if (value < 999) Color(0xFF333333) else Color(0xFFCCCCCC), // 手动变灰
            modifier = Modifier
                .size(dimensionResource(R.dimen.padding_medium))
                .clickable(enabled = value < 1000) { onChangeQuantity(value + 1) }
        )
    }
}

@Composable
fun SwipeableCartItem(
    product: CartProduct,
    isRevealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    onProductClick: (String) -> Unit,
    onToggleSelect: (String, Boolean) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onCollect: (CartProduct) -> Unit,
    onDelete: (List<String>) -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSyncFiled: Boolean = false,
) {
    val density = LocalDensity.current
    val actionWidth = 100.dp // 按钮区域的总宽度（例如两个按钮，每个 50dp）
    val actionWidthPx = with(density) { actionWidth.toPx() }

    // 创建 AnchoredDraggableState
    val dragState = remember(product.id) {
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

    // // 内部滑动结束后，通知外部
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

    Box(modifier = modifier.fillMaxWidth()) {
        // 背景层（收藏和删除按钮）
        Row(
            modifier = Modifier
                .matchParentSize() // 不参与 Box 的高度测量，而是等前景层测量完，直接强行和前景层一样大
                .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small))),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 收藏按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .background(Color(0xFFFFB800))
                    .clickable(enabled = isRevealed) { onCollect(product) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = "收藏", tint = Color.White)
                    Text(
                        text = "收藏",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            // 删除按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .background(Color(0xFFF40E21))
                    .clickable(enabled = isRevealed) { onDelete(listOf(product.id)) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White)
                    Text(
                        text = "删除",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        // 前景层（商品内容），应用 anchoredDraggable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { // 根据偏移量移动
                    // 判断锚点是否已初始化，避免在 LaunchedEffect 执行前读取 offset 导致崩溃
                    val offset = if (dragState.anchors.size > 0) dragState.requireOffset() else 0f
                    IntOffset(if (offset.isNaN()) 0 else offset.roundToInt(), 0)
                }
                .anchoredDraggable(
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
                .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small)))
                .background(Color.White) // 确保放 .offset 之后，否则背景覆盖背景层
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Checkbox(
                            checked = product.isValid && product.checked,
                            enabled = product.isValid && !isCurrentlyRevealed && product.goodsStock > 0,
                            onCheckedChange = {
                                onToggleSelect(
                                    product.id,
                                    it
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        // 若该商品（checked、quantity）同步失败，则显示一个刷新的 icon
                        // 点击该 icon，会同步所有的待同步项！
                        if (isSyncFiled) {
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = onSyncClick) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "同步",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(dimensionResource(R.dimen.padding_medium))
                                )
                            }
                        }
                    }
                    AsyncImage(
                        model = product.goodsIcon,
                        contentDescription = null,
                        modifier = Modifier
                            // 该大小可撑开父级（Row）高度：110dp + 上下 padding
                            .size(
                                width = 100.dp,
                                height = 110.dp
                            )
                            .padding(
                                horizontal = 0.dp,
                                vertical = dimensionResource(R.dimen.padding_small)
                            )
                            .dropShadow(
                                shape = RectangleShape, // RoundedCornerShape 已验证不可用
                                shadow = Shadow(
                                    radius = 8.dp,
                                    offset = DpOffset(x = 0.dp, y = 3.dp),
                                    color = Color.Black.copy(alpha = 0.3f)
                                )
                            )
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small)))
                            .clickable(enabled = product.isValid && !isCurrentlyRevealed) {
                                onProductClick(product.goodsId)
                            },
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .heightIn(min = 110.dp) // 设置最小高度，否则撑不开高度
                            .padding(dimensionResource(R.dimen.padding_small))
                    ) {
                        Text(
                            text = product.goodsNameZh,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¥${product.goodsPrice}",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (product.goodsStock < 1) {
                                Text(
                                    text = "售罄",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            } else {
                                NumberStepper(
                                    value = product.quantity,
                                    isValid = product.isValid && !isCurrentlyRevealed,
                                    onChangeQuantity = { value ->
                                        onRevealedChange(false)
                                        onQuantityChange(
                                            product.id,
                                            value
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 蒙版和水印：无效或下架的商品
                if (!product.isValid || product.goodsOnSale == 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.6f)) // 半透明底色
                            .pointerInput(Unit) { detectTapGestures(onTap = {}) }
                        /*// 为啥不直接设置 enabled = false ？ >> https://chat.deepseek.com/share/1qs26af7zg4rtnx899
                        clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // 去除波纹
                        ) {}*/,
                        contentAlignment = Alignment.Center
                    ) {
                        // 水印文字
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (!product.isValid) "无效" else "已下架",
                                color = Color.Gray.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.rotate(-30f) // 文字旋转
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettleColumn(
    cartRecords: List<CartProduct>,
    committing: Boolean = false,
    makeOrder: () -> Unit = {},
    onToggleSelectAll: () -> Unit = {}
) {
    val context = LocalContext.current
    val validRecords = cartRecords.filter { it.isValid && it.goodsStock > 0 && it.goodsOnSale == 1 }
    val allSelected = validRecords.all { it.checked }
    val anySelected = validRecords.any { it.checked }
    val someSelected = !allSelected && anySelected

    val grandTotal = validRecords
        .filter { it.checked }
        .fold(BigDecimal.ZERO) { acc, product ->
            val price = product.goodsPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
            acc + price * product.quantity.toBigDecimal()
        }
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(30.dp)
            .background(Color(0xFFDDE3E9))
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 0.dp,
                    end = dimensionResource(R.dimen.padding_small),
                    top = 2.dp,
                    bottom = 2.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TriStateCheckbox(
                state = when {
                    allSelected -> ToggleableState.On
                    someSelected -> ToggleableState.Indeterminate
                    else -> ToggleableState.Off
                },
                onClick = onToggleSelectAll
            )
            Text(
                text = "全选",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "￥$grandTotal",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Button(
                modifier = Modifier.width(100.dp),
                enabled = !committing,
                onClick = {
                    if (anySelected) {
                        makeOrder()
                    } else {
                        Toast.makeText(
                            context,
                            "至少选择一件商品",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF40E21),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (committing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "去结算",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
