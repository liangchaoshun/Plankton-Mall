package com.fuyouwentian.planktonmall.ui.cart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    onSettleClick: () -> Unit = {},
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
        color = Color(0xFFF5F6FA) // background
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

            uiState.cartData.isEmpty() -> {
                // 请求成功但没有数据
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "空空如也")
                }
            }

            else -> {
                val cartData = uiState.cartData
                val allSelected = cartData.all { it.checked }
                val someSelected = cartData.any { it.checked } && !allSelected
                val grandTotal = cartData
                    .filter { it.checked }
                    .fold(BigDecimal.ZERO) { acc, product ->
                        val price = product.price.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        acc + price * product.quantity.toBigDecimal()
                    }
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        modifier = modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                    ) {
                        items(cartData, key = { it.id }) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min) // 自动撑满最高子项
                                    .clickable { onProductClick(product.id) }
                                    .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small)))
                                    .background(Color.White)
                                    .padding(vertical = dimensionResource(R.dimen.padding_medium))
                                    .padding(
                                        start = 0.dp,
                                        end = dimensionResource(R.dimen.padding_medium)
                                    ),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Checkbox(
                                    checked = product.checked,
                                    onCheckedChange = { viewModel.toggleSelect(product.id, it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.White
                                    )
                                )
                                AsyncImage(
                                    model = product.goodsIcon,
                                    contentDescription = product.goodsNameZh,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_small))),
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
                                            onChangeQuantity = { value ->
                                                viewModel.changeQuantity(
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
                                onClick = viewModel::toggleSelectAll
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
                                onClick = onSettleClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF40E21)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = someSelected
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
            }
        }
    }
}

@Composable
fun NumberStepper(
    value: Int,
    onChangeQuantity: (Int) -> Unit,
    modifier: Modifier = Modifier
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
                    enabled = value > 1,
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
                    enabled = value < 1000,
                    onClick = { onChangeQuantity(value + 1) }
                )
        )
    }
}
