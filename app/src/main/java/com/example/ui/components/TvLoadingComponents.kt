package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSecondary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceFocused
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

/**
 * Size variants for the modern TV Spinner.
 */
enum class TvSpinnerSize(val diameter: Dp, val strokeWidth: Dp) {
    Small(22.dp, 2.5.dp),
    Medium(42.dp, 3.5.dp),
    Large(60.dp, 4.5.dp),
    ExtraLarge(76.dp, 5.5.dp)
}

/**
 * Apple TV-inspired lightweight rotating arc spinner with smooth glow & gradient sweep.
 * Lightweight, hardware accelerated, and clearly visible from 10-foot TV viewing distance.
 */
@Composable
fun TvSpinner(
    modifier: Modifier = Modifier,
    size: TvSpinnerSize = TvSpinnerSize.Medium,
    primaryColor: Color = TvPrimary,
    secondaryColor: Color = TvAccent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tv_spinner_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tv_spinner_rotation"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tv_spinner_pulse"
    )

    Box(
        modifier = modifier
            .size(size.diameter)
            .graphicsLayer { alpha = pulseAlpha },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = size.strokeWidth.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokePx) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background subtle track ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = radius,
                center = center,
                style = Stroke(width = strokePx)
            )

            // Active rotating arc
            val sweepGradient = Brush.sweepGradient(
                listOf(
                    primaryColor.copy(alpha = 0.05f),
                    secondaryColor.copy(alpha = 0.6f),
                    primaryColor
                ),
                center = center
            )

            drawArc(
                brush = sweepGradient,
                startAngle = rotation,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

/**
 * Lightweight shimmer brush for TV skeleton loaders.
 * Uses hardware accelerated linear gradient animation.
 */
@Composable
fun rememberTvShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(
            TvSurfaceCard,
            TvSurfaceFocused.copy(alpha = 0.8f),
            TvSurfaceCard
        ),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )
}

/**
 * Apple TV-inspired Fullscreen Startup Loading Screen.
 * Displays centralized app branding, animated TV spinner, and status indicators.
 * Optimized for OLED/LED televisions (1080p and 4K) with deep contrast and smooth transitions.
 */
@Composable
fun TvStartupLoadingScreen(
    statusMessage: String = "Initializing StreamDeck TV…",
    subStatusMessage: String? = "Connecting to channels and organizing your TV guide…",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .testTag("tv_startup_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient background gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TvPrimary.copy(alpha = 0.08f),
                        TvSecondary.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.maxDimension * 0.45f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // App Branding Icon Platter
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                TvPrimary.copy(alpha = 0.25f),
                                TvSurfaceCard
                            )
                        )
                    )
                    .border(1.5.dp, TvPrimary.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = TvPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "StreamDeck TV",
                color = TvTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Sleek Apple TV Style Loading Spinner
            TvSpinner(
                size = TvSpinnerSize.Large,
                primaryColor = TvPrimary,
                secondaryColor = TvAccent
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Status Message
            Text(
                text = statusMessage,
                color = TvTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            if (!subStatusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subStatusMessage,
                    color = TvTextSecondary,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Skeleton placeholder for TvChannelCard while channels or playlists are loading.
 */
@Composable
fun TvChannelCardSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberTvShimmerBrush()

    Box(
        modifier = modifier
            .height(146.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TvSurfaceCard)
            .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Logo placeholder
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerBrush)
                )

                // Badge placeholder
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Channel name placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                // Subtitle/category placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(shimmerBrush)
                )
            }
        }
    }
}

/**
 * Skeleton placeholder for TvChannelListItem while list items are loading.
 */
@Composable
fun TvChannelListItemSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberTvShimmerBrush()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TvSurfaceCard)
            .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Channel number
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Titles
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(shimmerBrush)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(shimmerBrush)
            )
        }
    }
}

/**
 * Modern TV Video Playback Buffering Overlay.
 * Appears seamlessly over ExoPlayer video view when buffering, recovering, or connecting.
 * Disappears smoothly once video frames start flowing.
 */
@Composable
fun TvPlayerBufferingOverlay(
    channel: Channel?,
    statusMessage: String = "Buffering Stream…",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .testTag("player_buffering_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(TvSurface.copy(alpha = 0.92f))
                .border(1.dp, TvPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(horizontal = 28.dp, vertical = 26.dp)
        ) {
            // Channel Mini Logo or TV Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurfaceCard)
                    .border(1.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = TvPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (channel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = channel.name,
                    color = TvTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Apple TV Spinner
            TvSpinner(
                size = TvSpinnerSize.Medium,
                primaryColor = TvPrimary,
                secondaryColor = TvAccent
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusMessage,
                color = TvTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Optimizing audio & video stream for TV playback…",
                color = TvTextSecondary,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Non-intrusive Live Sync Pill for Top Bar.
 * Indicates background IPTV playlist parsing or channel updates without blocking remote control navigation.
 */
@Composable
fun TvSyncPill(
    message: String = "Syncing Playlist…",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(TvSurfaceCard)
            .border(1.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        TvSpinner(
            size = TvSpinnerSize.Small,
            primaryColor = TvPrimary,
            secondaryColor = TvAccent
        )
        Text(
            text = message,
            color = TvAccent,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Inline loading state for screen transitions or category filtering.
 */
@Composable
fun TvInlineLoadingState(
    message: String = "Loading channels…",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TvSpinner(
                size = TvSpinnerSize.Medium,
                primaryColor = TvPrimary,
                secondaryColor = TvAccent
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                color = TvTextSecondary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * TV Channel Logo with smooth shimmer placeholder while loading,
 * smooth crossfade transition, and resilient TV fallback icon on failure.
 */
@Composable
fun TvChannelLogo(
    logoUrl: String?,
    channelName: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    fallbackTint: Color = TvTextSecondary
) {
    val shimmerBrush = rememberTvShimmerBrush()
    if (!logoUrl.isNullOrBlank()) {
        val context = androidx.compose.ui.platform.LocalContext.current
        coil.compose.SubcomposeAsyncImage(
            model = androidx.compose.runtime.remember(logoUrl) {
                coil.request.ImageRequest.Builder(context)
                    .data(logoUrl)
                    .crossfade(250)
                    .build()
            },
            contentDescription = channelName,
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerBrush)
                )
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = fallbackTint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = fallbackTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
