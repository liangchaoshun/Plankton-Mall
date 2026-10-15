package com.fuyouwentian.planktonmall.ui.components.widgets

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun BouncingDotsLoading(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    jumpHeight: Dp = 12.dp,
    animationDuration: Int = 600 // 1200ms 完整周期，包含等待时间
) {
    val density = LocalDensity.current
    // 1. 提前计算出跳起高度的像素值(px)
    val jumpHeightPx = with(density) { jumpHeight.toPx() }

    val infiniteTransition = rememberInfiniteTransition(label = "bouncing")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            // 2. 将跳起高度转为 Dp，用于 Compose 的 animateValue
            val jumpHeightInDp = jumpHeight / density.density

            // 核心逻辑：
            // 每个周期总长设为 1200ms。前 600ms 起跳落下，后 600ms 原地不动(等待)。
            // 后面的点通过 index * 400 的延迟，刚好能接上前一个点完全落地后的动作
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -jumpHeightPx, // 直接传入计算好的 Float px 值
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        easing = EaseInOutCubic,
                        delayMillis = index * 400 // 这里的 400 决定了接力的节奏
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset$index"
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset { IntOffset(0, offsetY.toInt()) }
                    .background(dotColor, shape = CircleShape)
            )
        }
    }
}
