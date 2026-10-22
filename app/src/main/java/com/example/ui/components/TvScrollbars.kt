package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TvPrimary

/**
 * TV-Friendly Scrollbars for Jetpack Compose on Android TV.
 * Renders an accessible, high-contrast, smoothly tracking scrollbar thumb
 * with rounded capsule styling along the edge of scrollable containers.
 */

fun Modifier.tvVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    thumbColor: Color = TvPrimary.copy(alpha = 0.85f),
    trackColor: Color = Color(0x20FFFFFF),
    minThumbHeight: Dp = 32.dp,
    paddingEnd: Dp = 4.dp
): Modifier = composed {
    val isScrollInProgress = state.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (isScrollInProgress) 1f else 0.5f,
        animationSpec = tween(200),
        label = "scrollbar_alpha"
    )

    drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo.size

        if (totalItems > visibleItems && visibleItems > 0) {
            val totalHeight = size.height
            val trackWidth = width.toPx()
            val minThumbPx = minThumbHeight.toPx()

            val thumbHeight = (totalHeight * visibleItems / totalItems).coerceIn(minThumbPx, totalHeight)
            val firstVisible = state.firstVisibleItemIndex.toFloat()
            val scrollFraction = (firstVisible / (totalItems - visibleItems)).coerceIn(0f, 1f)
            val thumbOffset = scrollFraction * (totalHeight - thumbHeight)

            val x = size.width - trackWidth - paddingEnd.toPx()

            // Draw track
            drawRoundRect(
                color = trackColor.copy(alpha = trackColor.alpha * alpha),
                topLeft = Offset(x, 0f),
                size = Size(trackWidth, totalHeight),
                cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
            )

            // Draw thumb
            drawRoundRect(
                color = thumbColor.copy(alpha = thumbColor.alpha * alpha),
                topLeft = Offset(x, thumbOffset),
                size = Size(trackWidth, thumbHeight),
                cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
            )
        }
    }
}

fun Modifier.tvVerticalScrollbar(
    state: LazyGridState,
    width: Dp = 4.dp,
    thumbColor: Color = TvPrimary.copy(alpha = 0.85f),
    trackColor: Color = Color(0x20FFFFFF),
    minThumbHeight: Dp = 32.dp,
    paddingEnd: Dp = 4.dp
): Modifier = composed {
    val isScrollInProgress = state.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (isScrollInProgress) 1f else 0.5f,
        animationSpec = tween(200),
        label = "grid_scrollbar_alpha"
    )

    drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo.size

        if (totalItems > visibleItems && visibleItems > 0) {
            val totalHeight = size.height
            val trackWidth = width.toPx()
            val minThumbPx = minThumbHeight.toPx()

            val thumbHeight = (totalHeight * visibleItems / totalItems).coerceIn(minThumbPx, totalHeight)
            val firstVisible = state.firstVisibleItemIndex.toFloat()
            val scrollFraction = (firstVisible / (totalItems - visibleItems)).coerceIn(0f, 1f)
            val thumbOffset = scrollFraction * (totalHeight - thumbHeight)

            val x = size.width - trackWidth - paddingEnd.toPx()

            // Draw track
            drawRoundRect(
                color = trackColor.copy(alpha = trackColor.alpha * alpha),
                topLeft = Offset(x, 0f),
                size = Size(trackWidth, totalHeight),
                cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
            )

            // Draw thumb
            drawRoundRect(
                color = thumbColor.copy(alpha = thumbColor.alpha * alpha),
                topLeft = Offset(x, thumbOffset),
                size = Size(trackWidth, thumbHeight),
                cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
            )
        }
    }
}

fun Modifier.tvVerticalScrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    thumbColor: Color = TvPrimary.copy(alpha = 0.85f),
    trackColor: Color = Color(0x20FFFFFF),
    minThumbHeight: Dp = 32.dp,
    paddingEnd: Dp = 4.dp
): Modifier = composed {
    val isScrollInProgress = state.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (isScrollInProgress) 1f else 0.5f,
        animationSpec = tween(200),
        label = "scroll_state_alpha"
    )

    drawWithContent {
        drawContent()

        if (state.maxValue > 0) {
            val totalHeight = size.height
            val trackWidth = width.toPx()
            val minThumbPx = minThumbHeight.toPx()

            val thumbHeight = (totalHeight * totalHeight / (totalHeight + state.maxValue)).coerceIn(minThumbPx, totalHeight)
            val scrollFraction = (state.value.toFloat() / state.maxValue.toFloat()).coerceIn(0f, 1f)
            val thumbOffset = scrollFraction * (totalHeight - thumbHeight)

            val x = size.width - trackWidth - paddingEnd.toPx()

            // Draw track
            drawRoundRect(
                color = trackColor.copy(alpha = trackColor.alpha * alpha),
                topLeft = Offset(x, 0f),
                size = Size(trackWidth, totalHeight),
                cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
            )

            // Draw thumb
            drawRoundRect(
                color = thumbColor.copy(alpha = thumbColor.alpha * alpha),
                topLeft = Offset(x, thumbOffset),
                size = Size(trackWidth, thumbHeight),
                cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
            )
        }
    }
}

fun Modifier.tvHorizontalScrollbar(
    state: LazyListState,
    height: Dp = 3.dp,
    thumbColor: Color = TvPrimary.copy(alpha = 0.85f),
    trackColor: Color = Color(0x20FFFFFF),
    minThumbWidth: Dp = 28.dp,
    paddingBottom: Dp = 2.dp
): Modifier = composed {
    val isScrollInProgress = state.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (isScrollInProgress) 1f else 0.4f,
        animationSpec = tween(200),
        label = "horiz_scrollbar_alpha"
    )

    drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo.size

        if (totalItems > visibleItems && visibleItems > 0) {
            val totalWidth = size.width
            val trackHeight = height.toPx()
            val minThumbPx = minThumbWidth.toPx()

            val thumbWidth = (totalWidth * visibleItems / totalItems).coerceIn(minThumbPx, totalWidth)
            val firstVisible = state.firstVisibleItemIndex.toFloat()
            val scrollFraction = (firstVisible / (totalItems - visibleItems)).coerceIn(0f, 1f)
            val thumbOffset = scrollFraction * (totalWidth - thumbWidth)

            val y = size.height - trackHeight - paddingBottom.toPx()

            // Draw track
            drawRoundRect(
                color = trackColor.copy(alpha = trackColor.alpha * alpha),
                topLeft = Offset(0f, y),
                size = Size(totalWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2, trackHeight / 2)
            )

            // Draw thumb
            drawRoundRect(
                color = thumbColor.copy(alpha = thumbColor.alpha * alpha),
                topLeft = Offset(thumbOffset, y),
                size = Size(thumbWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2, trackHeight / 2)
            )
        }
    }
}
