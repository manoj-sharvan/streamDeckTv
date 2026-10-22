package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            isFocused && isSelected -> 1.05f
            isFocused -> 1.04f
            isSelected -> 1.01f
            else -> 1.0f
        },
        animationSpec = tween(90),
        label = "tv_card_scale"
    )

    val currentBorderColor by animateColorAsState(
        targetValue = when {
            isFocused && isSelected -> Color.White
            isFocused -> focusedBorderColor
            isSelected -> selectedBorderColor
            else -> unfocusedBorderColor
        },
        animationSpec = tween(90),
        label = "tv_card_border_color"
    )

    val currentBorderWidth = when {
        isFocused && isSelected -> borderWidth + 1.dp
        isFocused -> borderWidth
        isSelected -> 2.dp
        else -> 0.dp
    }

    val backgroundColor = when {
        isFocused && isSelected -> Color(0xFF2B3346)
        isFocused -> Color(0xFF242C3D)
        isSelected -> Color(0xFF1E2638)
        else -> Color(0xFF181A20)
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

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (currentBorderWidth > 0.dp) {
                    Modifier.border(currentBorderWidth, currentBorderColor, shape)
                } else {
                    Modifier.border(1.dp, Color(0x18FFFFFF), shape)
                }
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
                        false
                    } else if (native.action == android.view.KeyEvent.ACTION_UP) {
                        val duration = if (keyDownTime > 0L) System.currentTimeMillis() - keyDownTime else 0L
                        keyDownTime = 0L
                        if (duration >= 450L && onLongClick != null) {
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

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun TvChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onOptionsClick: (() -> Unit)? = null,
    onCategoryClick: ((String) -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    var cardHasFocus by remember { mutableStateOf(false) }

    val titleFocusRequester = focusRequester ?: remember { FocusRequester() }
    val favFocusRequester = remember { FocusRequester() }
    val moreFocusRequester = remember { FocusRequester() }
    val catFocusRequester = remember { FocusRequester() }

    val titleInteractionSource = remember { MutableInteractionSource() }
    val isTitleFocused by titleInteractionSource.collectIsFocusedAsState()

    val catInteractionSource = remember { MutableInteractionSource() }
    val isCatFocused by catInteractionSource.collectIsFocusedAsState()

    val cardScale by animateFloatAsState(
        targetValue = when {
            cardHasFocus && isSelected -> 1.05f
            cardHasFocus -> 1.03f
            isSelected -> 1.01f
            else -> 1.0f
        },
        animationSpec = tween(90),
        label = "channel_card_scale"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isTitleFocused -> TvFocusBorder
            cardHasFocus -> Color(0x604A90E2)
            isSelected -> TvPrimary
            else -> Color(0x18FFFFFF)
        },
        animationSpec = tween(90),
        label = "channel_card_border"
    )

    val cardBgColor = when {
        cardHasFocus && isSelected -> Color(0xFF2B3346)
        cardHasFocus -> Color(0xFF242C3D)
        isSelected -> Color(0xFF1E2638)
        else -> Color(0xFF181A20)
    }

    Box(
        modifier = modifier
            .focusGroup()
            .focusProperties {
                enter = { titleFocusRequester }
            }
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .height(152.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBgColor)
            .border(
                width = if (isTitleFocused) 2.5.dp else if (cardHasFocus || isSelected) 2.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .onFocusChanged { cardHasFocus = it.hasFocus }
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_UP &&
                    (native.keyCode == android.view.KeyEvent.KEYCODE_PROG_YELLOW ||
                     native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_Y ||
                     native.keyCode == android.view.KeyEvent.KEYCODE_STAR)
                ) {
                    onToggleFavorite()
                    true
                } else false
            }
            .testTag("channel_card_${channel.id}")
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row 1: Logo + LIVE/PLAYING badge + Favorite Button + More Vert Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Logo + Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    cardHasFocus && isSelected -> TvPrimary.copy(alpha = 0.35f)
                                    isSelected -> TvPrimary.copy(alpha = 0.2f)
                                    cardHasFocus -> Color(0xFF2E384D)
                                    else -> Color(0xFF1E222B)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) TvPrimary.copy(alpha = 0.8f) else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TvChannelLogo(
                            logoUrl = channel.tvgLogo,
                            channelName = channel.name,
                            iconSize = 24.dp,
                            fallbackTint = when {
                                cardHasFocus || isSelected -> TvPrimary
                                else -> TvTextSecondary
                            }
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TvPrimary)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Text(
                                    text = "PLAYING",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TvLiveRed.copy(alpha = 0.2f))
                                .border(1.dp, TvLiveRed.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(TvLiveRed)
                                )
                                Text(
                                    text = "LIVE",
                                    color = TvLiveRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Right: Action Buttons (Favorite Star & More Vert Options)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TvFocusableIconButton(
                        icon = if (channel.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (channel.isFavorite) "Remove favorite" else "Add favorite",
                        onClick = onToggleFavorite,
                        iconTint = if (channel.isFavorite) TvFavoriteGold else TvTextSecondary,
                        focusedBorderColor = TvFavoriteGold,
                        focusedBackgroundColor = TvFavoriteGold.copy(alpha = 0.3f),
                        size = 32.dp,
                        iconSize = 18.dp,
                        focusRequester = favFocusRequester,
                        modifier = Modifier.focusProperties {
                            left = titleFocusRequester
                            right = if (onOptionsClick != null) moreFocusRequester else catFocusRequester
                        }
                    )

                    if (onOptionsClick != null) {
                        TvFocusableIconButton(
                            icon = Icons.Default.MoreVert,
                            contentDescription = "Channel options",
                            onClick = onOptionsClick,
                            iconTint = if (cardHasFocus || isSelected) TvPrimary else TvTextSecondary,
                            focusedBorderColor = TvPrimary,
                            focusedBackgroundColor = TvPrimary.copy(alpha = 0.35f),
                            size = 32.dp,
                            iconSize = 17.dp,
                            focusRequester = moreFocusRequester,
                            modifier = Modifier.focusProperties {
                                left = favFocusRequester
                                right = catFocusRequester
                            }
                        )
                    }
                }
            }

            // Middle: Channel Title Area (Focusable & Clickable to Play!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isTitleFocused) {
                            Modifier
                                .background(TvPrimary.copy(alpha = 0.22f))
                                .border(1.5.dp, TvFocusBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        } else {
                            Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        }
                    )
                    .focusRequester(titleFocusRequester)
                    .focusProperties {
                        right = favFocusRequester
                    }
                    .onPreviewKeyEvent { keyEvent ->
                        val native = keyEvent.nativeKeyEvent
                        val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                                        native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                                        native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                                        native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                        val isMenu = native.keyCode == android.view.KeyEvent.KEYCODE_MENU
                        if (isMenu && native.action == android.view.KeyEvent.ACTION_UP) {
                            onOptionsClick?.invoke()
                            true
                        } else if (isConfirm && native.action == android.view.KeyEvent.ACTION_DOWN) {
                            onClick()
                            true
                        } else if (isConfirm && native.action == android.view.KeyEvent.ACTION_UP) {
                            true
                        } else false
                    }
                    .clickable(
                        interactionSource = titleInteractionSource,
                        indication = androidx.compose.material3.ripple(),
                        onClick = onClick
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = channel.name,
                        color = when {
                            isTitleFocused -> Color.White
                            isSelected -> TvPrimary
                            cardHasFocus -> Color.White
                            else -> TvTextPrimary
                        },
                        fontWeight = if (isTitleFocused || isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.5.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isTitleFocused) {
                        Text(
                            text = "▶",
                            color = TvPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Bottom Row: Language Pill + Category Badge (Focusable & Clickable to Filter!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Language pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isSelected -> TvPrimary.copy(alpha = 0.25f)
                                cardHasFocus -> TvPrimary.copy(alpha = 0.25f)
                                else -> TvSurfaceVariant
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = channel.effectiveLanguage.uppercase(),
                        color = if (cardHasFocus || isSelected) TvPrimary else TvAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Category pill: Focusable and Clickable!
                val categoryName = if (channel.contentCategory.isNotBlank()) channel.contentCategory else "General"
                Box(
                    modifier = Modifier
                        .focusRequester(catFocusRequester)
                        .focusProperties {
                            left = if (onOptionsClick != null) moreFocusRequester else favFocusRequester
                        }
                        .graphicsLayer {
                            scaleX = if (isCatFocused) 1.08f else 1.0f
                            scaleY = if (isCatFocused) 1.08f else 1.0f
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isCatFocused -> TvAccent.copy(alpha = 0.35f)
                                else -> Color(0x252E384D)
                            }
                        )
                        .border(
                            width = if (isCatFocused) 2.dp else 1.dp,
                            color = if (isCatFocused) TvAccent else Color(0x25FFFFFF),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .onPreviewKeyEvent { keyEvent ->
                            val native = keyEvent.nativeKeyEvent
                            val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                                            native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                                            native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                                            native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                            if (isConfirm && native.action == android.view.KeyEvent.ACTION_DOWN) {
                                onCategoryClick?.invoke(categoryName)
                                true
                            } else if (isConfirm && native.action == android.view.KeyEvent.ACTION_UP) {
                                true
                            } else false
                        }
                        .clickable(
                            interactionSource = catInteractionSource,
                            indication = androidx.compose.material3.ripple(),
                            onClick = { onCategoryClick?.invoke(categoryName) }
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = categoryName,
                        color = if (isCatFocused) Color.White else TvAccent,
                        fontSize = 11.sp,
                        fontWeight = if (isCatFocused) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
