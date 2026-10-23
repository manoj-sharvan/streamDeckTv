package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.onPreviewKeyEvent
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.Channel
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvFavoriteGold
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceFocused
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    shape: Shape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = TvFocusBorder,
    selectedBorderColor: Color = TvPrimary,
    unfocusedBorderColor: Color = Color.Transparent,
    borderWidth: Dp = 3.dp,
    onLongClick: (() -> Unit)? = null,
    content: @Composable (isFocused: Boolean, isSelected: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isFocused && isSelected -> 1.06f
            isFocused -> 1.05f
            isSelected -> 1.01f
            else -> 1.0f
        },
        animationSpec = tween(150),
        label = "tv_card_scale"
    )

    val currentBorderColor by animateColorAsState(
        targetValue = when {
            isFocused && isSelected -> Color.White
            isFocused -> focusedBorderColor
            isSelected -> selectedBorderColor
            else -> unfocusedBorderColor
        },
        animationSpec = tween(150),
        label = "tv_card_border_color"
    )

    val currentBorderWidth = when {
        isFocused && isSelected -> borderWidth + 1.dp
        isFocused -> borderWidth
        isSelected -> 2.dp
        else -> 0.dp
    }

    val backgroundColor = when {
        isFocused && isSelected -> Color(0xFF1A2540)
        isFocused -> Color(0xFF162035)
        isSelected -> Color(0xFF14203A)
        else -> Color(0xFF101420)
    }

    val clickModifier = if (onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            onClick = onClick,
            onLongClick = onLongClick
        )
    } else {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            onClick = onClick
        )
    }

    var keyDownTime by remember { mutableStateOf(0L) }
    LaunchedEffect(isFocused) {
        if (!isFocused) {
            keyDownTime = 0L
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = if (currentBorderWidth > 0.dp) currentBorderWidth else 1.dp,
                color = if (currentBorderWidth > 0.dp) currentBorderColor else Color(0x18FFFFFF),
                shape = shape
            )
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                val isMenu = native.keyCode == android.view.KeyEvent.KEYCODE_MENU

                if (isMenu && native.action == android.view.KeyEvent.ACTION_UP) {
                    if (onLongClick != null) {
                        onLongClick()
                        true
                    } else false
                } else if (isConfirm) {
                    if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                        if (keyDownTime == 0L) {
                            keyDownTime = System.currentTimeMillis()
                        }
                        true // Consume ACTION_DOWN to prevent clickable from handling it
                    } else if (native.action == android.view.KeyEvent.ACTION_UP) {
                        val duration = if (keyDownTime > 0L) System.currentTimeMillis() - keyDownTime else 0L
                        keyDownTime = 0L
                        if (duration in 450L..2500L && onLongClick != null) {
                            onLongClick()
                            true
                        } else {
                            onClick()
                            true
                        }
                    } else false
                } else {
                    false
                }
            }
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        content(isFocused, isSelected)
    }
}

@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = TvFocusBorder,
    unfocusedBorderColor: Color = Color.Transparent,
    borderWidth: Dp = 3.dp,
    onLongClick: (() -> Unit)? = null,
    content: @Composable (isFocused: Boolean) -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        modifier = modifier,
        isSelected = false,
        shape = shape,
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        borderWidth = borderWidth,
        onLongClick = onLongClick,
        content = { isFocused, _ -> content(isFocused) }
    )
}

@Composable
fun TvFocusableButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    tag: String = "tv_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = tween(90),
        label = "button_scale"
    )

    val backgroundColor = when {
        isFocused -> TvPrimary
        isPrimary -> TvSurfaceVariant
        else -> TvSurfaceCard
    }

    val contentColor = when {
        isFocused -> Color(0xFF0A101D)
        isPrimary -> TvPrimary
        else -> TvTextPrimary
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color.White else TvSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                if (isConfirm && native.action == android.view.KeyEvent.ACTION_UP) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .testTag(tag)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.5.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TvFocusableIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = TvTextSecondary,
    focusedBorderColor: Color = TvPrimary,
    focusedBackgroundColor: Color = TvPrimary.copy(alpha = 0.25f),
    size: Dp = 34.dp,
    iconSize: Dp = 19.dp,
    focusRequester: FocusRequester? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.18f else 1.0f,
        animationSpec = tween(90),
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(size)
            .clip(CircleShape)
            .background(if (isFocused) focusedBackgroundColor else Color(0x351E222B))
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) focusedBorderColor else Color(0x22FFFFFF),
                shape = CircleShape
            )
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                if (isConfirm && native.action == android.view.KeyEvent.ACTION_DOWN) {
                    onClick()
                    true
                } else if (isConfirm && native.action == android.view.KeyEvent.ACTION_UP) {
                    true
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isFocused) Color.White else iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Animated Audio Equalizer Wave Bars (Wireframe Panel 3).
 * Rendered when channel is in Playing state.
 */
@Composable
fun TvAudioEqualizerBars(
    modifier: Modifier = Modifier,
    barColor: Color = TvPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 540, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar1Height.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar2Height.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar3Height.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
    }
}

/**
 * Premium single-focus-target channel card (Wireframe Panel 3).
 * Visual states:
 * 1. Normal: clean dark glass surface, logo, channel name, Ch number, subtle favorite star.
 * 2. Focused: bright blue glow border (TvFocusBorder), 1.05x subtle scale, star visible.
 * 3. Selected (OK Press): checkmark badge confirmation indicator.
 * 4. Playing: LIVE badge + audio equalizer wave animation.
 */
@Composable
fun TvChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isPlaying: Boolean = false,
    onOptionsClick: (() -> Unit)? = null,
    onCategoryClick: ((String) -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    TvFocusableCard(
        onClick = onClick,
        isSelected = isSelected,
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .height(152.dp)
            .testTag("channel_card_${channel.id}")
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_UP) {
                    when (native.keyCode) {
                        android.view.KeyEvent.KEYCODE_PROG_YELLOW,
                        android.view.KeyEvent.KEYCODE_BUTTON_Y,
                        android.view.KeyEvent.KEYCODE_STAR -> {
                            onToggleFavorite()
                            true
                        }
                        else -> false
                    }
                } else false
            },
        onLongClick = onOptionsClick,
        shape = RoundedCornerShape(14.dp),
        focusedBorderColor = TvFocusBorder,
        borderWidth = 2.5.dp
    ) { isFocused, cardIsSelected ->
        val channelNumber = remember(channel.id) { (channel.id % 900 + 100).toInt() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Status badge / Number on left, Indicators on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Playing/LIVE badge or Channel Number
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvLiveRed)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Ch $channelNumber",
                        color = if (isFocused) TvAccent else TvTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Right Indicators: Selected Checkmark / Playing Equalizer / Favorite Star
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPlaying) {
                        TvAudioEqualizerBars(barColor = if (isFocused) Color.White else TvAccent)
                    } else if (cardIsSelected) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(TvPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Favorite Star (Gold if favorite, outline if focused and not favorite)
                    if (channel.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = TvFavoriteGold,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isFocused) {
                        Icon(
                            imageVector = Icons.Outlined.StarBorder,
                            contentDescription = "Toggle Favorite",
                            tint = Color(0x90FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Center: Channel Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isPlaying -> TvLiveRed.copy(alpha = 0.2f)
                                isFocused && cardIsSelected -> TvPrimary.copy(alpha = 0.35f)
                                cardIsSelected -> TvPrimary.copy(alpha = 0.2f)
                                isFocused -> Color(0xFF1E283D)
                                else -> Color(0xFF131722)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                isPlaying -> TvLiveRed.copy(alpha = 0.5f)
                                isFocused -> Color(0x35FFFFFF)
                                else -> Color(0x14FFFFFF)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TvChannelLogo(
                        logoUrl = channel.tvgLogo,
                        channelName = channel.name,
                        iconSize = 30.dp,
                        fallbackTint = when {
                            isFocused || cardIsSelected -> TvPrimary
                            else -> TvTextSecondary
                        }
                    )
                }
            }

            // Bottom: Channel Name + Category / Subtitle
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = channel.name,
                    color = when {
                        isFocused -> Color.White
                        cardIsSelected -> TvPrimary
                        else -> TvTextPrimary
                    },
                    fontWeight = if (isFocused || cardIsSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    lineHeight = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val categoryText = when {
                    channel.contentCategory.isNotBlank() -> channel.contentCategory
                    channel.effectiveLanguage.isNotBlank() -> channel.effectiveLanguage.uppercase()
                    else -> "Live"
                }

                Text(
                    text = categoryText,
                    color = if (isFocused) TvAccent else TvTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Horizontal Channel Row with smooth auto-scroll and left/right indicators (Wireframe Panel 5).
 * LEFT key at index 0 navigates to the navigation rail.
 */
@Composable
fun TvChannelRow(
    title: String,
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onOptionsClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    selectedChannelId: Long? = null,
    playingChannelId: Long? = null,
    onNavigateToRail: (() -> Unit)? = null,
    isFirstRow: Boolean = false,
    firstItemFocusRequester: FocusRequester? = null
) {
    if (channels.isEmpty()) return

    val listState = rememberLazyListState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TvTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${channels.size} channels",
                color = TvTextSecondary,
                fontSize = 11.5.sp
            )
        }

        // Horizontal Carousel with Left/Right chevrons and auto-scroll
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(channels, key = { _, channel -> channel.id }) { index, channel ->
                    val isFirstItem = index == 0
                    TvChannelCard(
                        channel = channel,
                        onClick = { onChannelClick(channel) },
                        onToggleFavorite = { onToggleFavorite(channel) },
                        onOptionsClick = { onOptionsClick(channel) },
                        isSelected = selectedChannelId == channel.id,
                        isPlaying = playingChannelId == channel.id,
                        focusRequester = if (isFirstRow && isFirstItem) firstItemFocusRequester else null,
                        modifier = Modifier
                            .width(136.dp)
                            .then(
                                if (isFirstItem && onNavigateToRail != null) {
                                    Modifier.onPreviewKeyEvent { keyEvent ->
                                        val native = keyEvent.nativeKeyEvent
                                        if (native.action == android.view.KeyEvent.ACTION_DOWN &&
                                            native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT
                                        ) {
                                            onNavigateToRail()
                                            true
                                        } else false
                                    }
                                } else Modifier
                            )
                    )
                }
            }
        }
    }
}

/**
 * Channel Details / Info Panel (Wireframe Panel 4).
 * Appears when a channel is selected, showing video preview, metadata, Watch, Favorite, and Up Next.
 */
@Composable
fun TvChannelInfoPanel(
    channel: Channel,
    onWatch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoreInfo: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val watchButtonFocusRequester = remember { FocusRequester() }
    val channelNumber = remember(channel.id) { (channel.id % 900 + 100).toInt() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        try {
            watchButtonFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6080A12))
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN &&
                    native.keyCode == android.view.KeyEvent.KEYCODE_BACK
                ) {
                    onClose()
                    true
                } else false
            }
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Back action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvFocusableIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close Info",
                    onClick = onClose
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvLiveRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content: Left Preview Video/Thumbnail + Right Metadata
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Left: Preview Card (16:9)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121622))
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    TvChannelLogo(
                        logoUrl = channel.tvgLogo,
                        channelName = channel.name,
                        iconSize = 64.dp
                    )

                    // LIVE badge on bottom left of preview
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvLiveRed)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LIVE BROADCAST",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right: Channel Metadata + Actions + Up Next
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Channel Logo + Name + Ch 103
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1A2234))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TvChannelLogo(
                                    logoUrl = channel.tvgLogo,
                                    channelName = channel.name,
                                    iconSize = 26.dp
                                )
                            }

                            Column {
                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ch $channelNumber · ${channel.effectiveLanguage} · ${if (channel.contentCategory.isNotBlank()) channel.contentCategory else "Live TV"}",
                                    color = TvAccent,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Current Program Info
                        Text(
                            text = "${channel.name} Live Program",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Live Stream · High Definition Quality",
                            color = TvTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Stream digital broadcast in high definition directly to your Android TV with full remote playback controls.",
                            color = TvTextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: Watch, Add to Favorites, More Info
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TvFocusableButton(
                                text = "Watch",
                                icon = Icons.Filled.PlayArrow,
                                onClick = onWatch,
                                isPrimary = true,
                                modifier = Modifier.focusRequester(watchButtonFocusRequester)
                            )

                            TvFocusableButton(
                                text = if (channel.isFavorite) "Favorited" else "Add to Favorites",
                                icon = Icons.Default.Star,
                                onClick = onToggleFavorite,
                                isPrimary = false
                            )

                            TvFocusableButton(
                                text = "More Info",
                                icon = Icons.Default.MoreVert,
                                onClick = onMoreInfo,
                                isPrimary = false
                            )
                        }
                    }

                    // Up Next Section (Wireframe Panel 4)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x18FFFFFF))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "UP NEXT",
                            color = TvAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Evening Highlights & World News",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Next scheduled program segment",
                            color = TvTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TvActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        modifier = modifier.height(148.dp),
        shape = RoundedCornerShape(12.dp)
    ) { isFocused ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isFocused) TvPrimary else TvSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFocused) Color(0xFF071224) else TvPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TvTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = if (isFocused) TvPrimary else TvTextSecondary,
                fontSize = 11.5.sp,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TvChannelListItem(
    channel: Channel,
    channelNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    onOptionsClick: (() -> Unit)? = null
) {
    TvFocusableCard(
        onClick = onClick,
        isSelected = isSelected,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("channel_item_${channel.id}")
    ) { isFocused, cardIsSelected ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Channel number
                Text(
                    text = String.format("%02d", channelNumber),
                    color = if (cardIsSelected || isFocused) TvPrimary else TvTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp)
                )

                // Logo or TV Icon with shimmer placeholder and fallback
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TvSurfaceVariant)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TvChannelLogo(
                        logoUrl = channel.tvgLogo,
                        channelName = channel.name,
                        iconSize = 18.dp,
                        fallbackTint = if (isFocused || cardIsSelected) TvPrimary else TvTextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        color = if (cardIsSelected) TvPrimary else TvTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (isFocused || cardIsSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = channel.effectiveLanguage.uppercase(),
                            color = TvAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "·",
                            color = TvTextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = if (channel.contentCategory.isNotBlank()) channel.contentCategory else "General",
                            color = TvTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (cardIsSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PLAYING",
                            color = Color(0xFF0A101D),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                TvFocusableIconButton(
                    icon = if (channel.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (channel.isFavorite) "Remove favorite" else "Add favorite",
                    onClick = onToggleFavorite,
                    iconTint = if (channel.isFavorite) TvFavoriteGold else TvTextSecondary,
                    focusedBorderColor = TvFavoriteGold,
                    focusedBackgroundColor = TvFavoriteGold.copy(alpha = 0.3f),
                    size = 32.dp,
                    iconSize = 18.dp
                )

                if (onOptionsClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    TvFocusableIconButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        onClick = onOptionsClick,
                        iconTint = if (isFocused || cardIsSelected) TvPrimary else TvTextSecondary,
                        focusedBorderColor = TvPrimary,
                        focusedBackgroundColor = TvPrimary.copy(alpha = 0.35f),
                        size = 32.dp,
                        iconSize = 16.dp
                    )
                }
            }
        }
    }
}

@Composable
fun TvCategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isFocused && isSelected -> 1.05f
            isFocused -> 1.04f
            isSelected -> 1.01f
            else -> 1.0f
        },
        animationSpec = tween(90),
        label = "chip_scale"
    )

    val backgroundColor = when {
        isFocused && isSelected -> TvPrimary
        isSelected -> TvPrimary.copy(alpha = 0.9f)
        isFocused -> TvSurfaceFocused
        else -> TvSurfaceVariant
    }

    val textColor = when {
        isSelected -> Color(0xFF070D18)
        isFocused -> TvPrimary
        else -> TvTextSecondary
    }

    val borderColor = when {
        isFocused && isSelected -> Color.White
        isFocused -> TvFocusBorder
        isSelected -> TvPrimary
        else -> Color.Transparent
    }

    val borderWidth = when {
        isFocused && isSelected -> 2.5.dp
        isFocused -> 2.dp
        isSelected -> 1.dp
        else -> 0.dp
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                if (isConfirm && native.action == android.view.KeyEvent.ACTION_DOWN) {
                    onClick()
                    true
                } else if (isConfirm && native.action == android.view.KeyEvent.ACTION_UP) {
                    true
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            color = textColor,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
