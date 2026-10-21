package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    content: @Composable (isFocused: Boolean, isSelected: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isFocused && isSelected -> 1.07f
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

    val shadowElevation by animateFloatAsState(
        targetValue = when {
            isFocused && isSelected -> 22f
            isFocused -> 16f
            isSelected -> 6f
            else -> 2f
        },
        label = "tv_card_shadow"
    )

    val backgroundColor = when {
        isFocused && isSelected -> TvSurfaceFocused
        isFocused -> TvSurfaceFocused
        isSelected -> Color(0xFF132A3E)
        else -> TvSurfaceCard
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .shadow(shadowElevation.dp, shape)
            .then(
                if (currentBorderWidth > 0.dp) {
                    Modifier.border(currentBorderWidth, currentBorderColor, shape)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .focusable(interactionSource = interactionSource),
        shape = shape,
        color = backgroundColor,
        interactionSource = interactionSource
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
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = tween(120),
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
            .scale(scale)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color.White else TvSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
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
fun TvChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onOptionsClick: (() -> Unit)? = null
) {
    TvFocusableCard(
        onClick = onClick,
        isSelected = isSelected,
        modifier = modifier
            .height(146.dp)
            .testTag("channel_card_${channel.id}")
    ) { isFocused, cardIsSelected ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Channel Logo or Placeholder Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isFocused && cardIsSelected -> TvPrimary.copy(alpha = 0.35f)
                                cardIsSelected -> TvPrimary.copy(alpha = 0.2f)
                                else -> TvSurfaceVariant
                            }
                        )
                        .border(
                            width = if (cardIsSelected) 1.dp else 0.dp,
                            color = if (cardIsSelected) TvPrimary.copy(alpha = 0.7f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!channel.tvgLogo.isNullOrBlank()) {
                        AsyncImage(
                            model = channel.tvgLogo,
                            contentDescription = channel.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = when {
                                isFocused || cardIsSelected -> TvPrimary
                                else -> TvTextSecondary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge: PLAYING if selected, otherwise LIVE badge
                    if (cardIsSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TvPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0A101D))
                                )
                                Text(
                                    text = "PLAYING",
                                    color = Color(0xFF0A101D),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TvLiveRed.copy(alpha = 0.2f))
                                .border(1.dp, TvLiveRed.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = TvLiveRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Favorite button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(bounded = false, radius = 20.dp),
                                onClick = onToggleFavorite
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (channel.isFavorite) "Remove favorite" else "Add favorite",
                            tint = if (channel.isFavorite) TvFavoriteGold else TvTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Options button (for TV channel actions: Play, Favorite, Change Language)
                    if (onOptionsClick != null) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = androidx.compose.material3.ripple(bounded = false, radius = 20.dp),
                                    onClick = onOptionsClick
                                )
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Channel options",
                                tint = if (isFocused || cardIsSelected) TvPrimary else TvTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = channel.name,
                    color = if (cardIsSelected) TvPrimary else TvTextPrimary,
                    fontWeight = if (isFocused || cardIsSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    lineHeight = 17.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    cardIsSelected -> TvPrimary.copy(alpha = 0.25f)
                                    isFocused -> TvPrimary.copy(alpha = 0.25f)
                                    else -> TvSurfaceVariant
                                }
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = channel.effectiveLanguage.uppercase(),
                            color = if (isFocused || cardIsSelected) TvPrimary else TvAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (channel.contentCategory.isNotBlank()) channel.contentCategory else "General",
                        color = when {
                            cardIsSelected -> TvAccent
                            isFocused -> TvAccent
                            else -> TvTextSecondary
                        },
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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

                // Logo or TV Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TvSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!channel.tvgLogo.isNullOrBlank()) {
                        AsyncImage(
                            model = channel.tvgLogo,
                            contentDescription = channel.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = if (isFocused || cardIsSelected) TvPrimary else TvTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple(bounded = false, radius = 18.dp),
                            onClick = onToggleFavorite
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (channel.isFavorite) "Remove favorite" else "Add favorite",
                        tint = if (channel.isFavorite) TvFavoriteGold else TvTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (onOptionsClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(bounded = false, radius = 18.dp),
                                onClick = onOptionsClick
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = if (isFocused || cardIsSelected) TvPrimary else TvTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
            isFocused && isSelected -> 1.08f
            isFocused -> 1.06f
            isSelected -> 1.02f
            else -> 1.0f
        },
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
            .scale(scale)
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
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            color = textColor,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
