package com.fuyouwentian.planktonmall.ui.cart

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

// 定义滑动状态
enum class DragValue {
    Settled, // 初始状态，未露出按钮
    Revealed // 露出按钮状态
}

@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    onSettleClick: () -> Unit = {},
    onNavigate2Login: () -> Unit = {},
    onNavigate2Pickup: () -> Unit = {},
    viewModel: CartViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchCartData()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    Surface(
        modifier = modifier,
        color = if (uiState.cartData.isEmpty()) Color.White else Color(0xFFF5F6FA) // background
    ) {
        // 根据状态展示不同 UI
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
                        Text(text = "${uiState.error}")
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        TextButton(onClick = viewModel::fetchCartData) { Text("重试") }
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
                        Text(text = "请先登录，以获取数据")
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        Button(
                            onClick = { onNavigate2Login() },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                        ) { Text("去登录") }
                    }
                }
            }

            uiState.cartData.isEmpty() -> {
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
                        Text(text = "购物车空空如也")
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                        Button(
                            onClick = { onNavigate2Pickup() },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                        ) { Text("去挑选") }
                    }
                }
            }

            else -> {
                val cartData = uiState.cartData
                // 记录当前滑出的商品 ID
                var revealedProductId by remember { mutableStateOf<String?>(null) }

                Column(modifier = Modifier.fillMaxSize()) {
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
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                            modifier = modifier.fillMaxSize()
                        ) {
                            items(cartData, key = { it.id }) { product ->
                                SwipeableCartItem(
                                    product = product,
                                    isRevealed = revealedProductId == product.id,
                                    isSyncFiled = product.id in uiState.syncFailedIds,
                                    onRevealedChange = { isRevealed ->
                                        // Log.d("Cart", "$isRevealed")
                                        revealedProductId = if (isRevealed) product.id else null
                                    },
                                    onProductClick = onProductClick,
                                    onToggleSelect = viewModel::toggleSelect,
                                    onQuantityChange = viewModel::changeQuantity,
                                    onCollect = { value -> Log.d("Cart", "收藏: ${product.id}") },
                                    onDelete = { value -> Log.d("Cart", "删除: ${product.id}") },
                                    onSyncClick = viewModel::syncData2Server
                                )
                            }
                        }
                    }

                    // 底部结算栏
                    SettleColumn(
                        cartData = cartData,
                        onSettleBtnClick = onSettleClick,
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
                .clickable(
                    enabled = isValid && value > 1,
                    onClick = { onChangeQuantity(value - 1) }
                )
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
                    fontSize = 14.sp,
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
                .clickable(
                    enabled = isValid && value < 1000,
                    onClick = { onChangeQuantity(value + 1) }
                )
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
    onCollect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSyncClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSyncFiled: Boolean = false,
) {
    val density = LocalDensity.current
    val actionWidth = 120.dp // 按钮区域的总宽度（例如两个按钮，每个 60dp）
    val actionWidthPx = with(density) { actionWidth.toPx() }

    // 创建 AnchoredDraggableState
    val state = remember { AnchoredDraggableState(initialValue = DragValue.Settled) }

    // 设置锚点（Anchors），为何使用监听的方式：https://chat.deepseek.com/share/hv5sfio9udh1lzjygi
    // Settled 对应 offset = 0，Revealed 对应 offset = -actionWidthPx
    LaunchedEffect(actionWidthPx) {
        // 设置两个槽点
        state.updateAnchors(
            DraggableAnchors {
                DragValue.Settled at 0f
                DragValue.Revealed at -actionWidthPx
            }
        )
    }

    // 监听外部传入的 isRevealed，如果为 false 且当前是 Revealed，则收起
    LaunchedEffect(isRevealed) {
        if (!isRevealed && state.currentValue == DragValue.Revealed) {
            state.animateTo(DragValue.Settled)
        }
    }

    // 监听内部滑动状态，通知外部
    LaunchedEffect(state.currentValue) {
        onRevealedChange(state.currentValue == DragValue.Revealed)
    }

    // 计算当前是否真的处于滑出状态（用于控制点击）
    // 注意：这里用 targetValue 而不是 currentValue，因为动画过程中也要禁止点击
    val isCurrentlyRevealed = state.targetValue == DragValue.Revealed

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // 背景层（收藏和删除按钮）
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F6FA)),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 收藏按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .background(Color(0xFFFFB800))
                    .clickable(enabled = isRevealed) { onCollect(product.id) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.StarBorder, contentDescription = "收藏", tint = Color.White)
                    Text("收藏", color = Color.White, fontSize = 12.sp)
                }
            }
            // 删除按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .background(Color(0xFFF40E21))
                    .clickable(enabled = isRevealed) { onDelete(product.id) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White)
                    Text("删除", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // 前景层（商品内容），应用 anchoredDraggable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .offset { // 根据偏移量移动
                    // 判断锚点是否已初始化，避免在 LaunchedEffect 执行前读取 offset 导致崩溃
                    val offset = if (state.anchors.size > 0) state.requireOffset() else 0f
                    IntOffset(offset.roundToInt(), 0)
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                        state = state,
                        positionalThreshold = { distance -> distance * 0.5f },
                        animationSpec = spring()
                    )
                )
                .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small)))
                .background(Color.White)
                // 如果已滑出，则收起
                .clickable { onRevealedChange(false) }
                .padding(vertical = dimensionResource(R.dimen.padding_medium))
                .padding(start = 0.dp, end = dimensionResource(R.dimen.padding_medium))
                // 无效商品蒙版和水印
                .then(
                    if (!product.isValid) {
                        Modifier.drawWithContent {
                            // 先绘制原本的内容
                            drawContent()
                            // 绘制半透明蒙版
                            drawRect(color = Color.White.copy(alpha = 0.6f))
                            // 绘制水印文字
                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 40.dp.toPx()
                                    isAntiAlias = true
                                    alpha = 150 // 半透明
                                    textAlign =
                                        Paint.Align.CENTER
                                    typeface =
                                        Typeface.DEFAULT_BOLD
                                }
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                // 旋转画布绘制倾斜水印
                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.rotate(
                                    -30f,
                                    centerX,
                                    centerY
                                )
                                canvas.nativeCanvas.drawText(
                                    "无效",
                                    centerX,
                                    centerY,
                                    paint
                                )
                                canvas.nativeCanvas.restore()
                            }
                        }
                    } else Modifier
                ),
        ) {

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Checkbox(
                    checked = product.isValid && product.checked,
                    enabled = product.isValid && !isCurrentlyRevealed,
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
                if (isSyncFiled) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onSyncClick(product.id) }) {
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
                contentDescription = product.goodsNameZh,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small)))
                    .clickable(enabled = product.isValid && !isCurrentlyRevealed) {
                        onProductClick(product.id)
                    },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.goodsNameZh,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¥${product.price}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
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
}

@Composable
fun SettleColumn(
    cartData: List<CartProduct>,
    modifier: Modifier = Modifier,
    onSettleBtnClick: () -> Unit = {},
    onToggleSelectAll: () -> Unit = {}
) {
    val context = LocalContext.current
    val allSelected = cartData.filter { it.isValid }.all { it.checked }
    val anySelected = cartData.filter { it.isValid }.any { it.checked }
    val someSelected = cartData.filter { it.isValid }.any { it.checked } && !allSelected
    val grandTotal = cartData
        .filter { it.isValid && it.checked }
        .fold(BigDecimal.ZERO) { acc, product ->
            val price = product.price.toBigDecimalOrNull() ?: BigDecimal.ZERO
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
                    end = dimensionResource(R.dimen.padding_medium)
                )
                .padding(vertical = dimensionResource(R.dimen.padding_medium)),
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
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "￥$grandTotal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
            Button(
                onClick = {
                    if (anySelected) {
                        onSettleBtnClick()
                    } else {
                        Toast.makeText(
                            context,
                            "至少选择一件商品",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF40E21)
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "去结算",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
