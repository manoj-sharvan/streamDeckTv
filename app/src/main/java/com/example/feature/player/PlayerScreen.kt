package com.example.feature.player

import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.domain.model.AudioTrackInfo
import com.example.domain.model.SubtitleTrackInfo
import com.example.player.PlayerUiState
import com.example.player.StreamPlayerManager
import com.example.ui.components.TvFocusableButton
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    playerManager: StreamPlayerManager,
    uiState: PlayerUiState,
    onNextChannel: () -> Unit,
    onPreviousChannel: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isOverlayVisible by remember { mutableStateOf(true) }
    var showTrackDialog by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Focus requesters for TV remote D-pad navigation
    val playPauseFocusRequester = remember { FocusRequester() }
    val backFocusRequester = remember { FocusRequester() }
    val audioSubsFocusRequester = remember { FocusRequester() }
    val prevFocusRequester = remember { FocusRequester() }
    val nextFocusRequester = remember { FocusRequester() }

    // Intercept hardware Back button on Android TV remote:
    // 1. If Dialog is open -> close dialog
    // 2. Return to channel catalog immediately
    BackHandler(enabled = true) {
        if (showTrackDialog) {
            showTrackDialog = false
            lastInteractionTime = System.currentTimeMillis()
        } else {
            onBack()
        }
    }

    // Auto-focus the Play/Pause button whenever overlay is brought to front
    LaunchedEffect(isOverlayVisible) {
        if (isOverlayVisible && !showTrackDialog) {
            delay(120)
            try {
                playPauseFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Auto-hide TV player overlay after 6 seconds of user inactivity
    LaunchedEffect(isOverlayVisible, uiState.isPlaying, lastInteractionTime, showTrackDialog) {
        if (isOverlayVisible && uiState.isPlaying && uiState.errorMessage == null && !showTrackDialog) {
            delay(6000)
            isOverlayVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val code = keyEvent.nativeKeyEvent.keyCode
                    when (code) {
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            if (!isOverlayVisible) {
                                isOverlayVisible = true
                                lastInteractionTime = System.currentTimeMillis()
                                true
                            } else {
                                lastInteractionTime = System.currentTimeMillis()
                                false
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (!isOverlayVisible) {
                                isOverlayVisible = true
                                lastInteractionTime = System.currentTimeMillis()
                                true
                            } else {
                                lastInteractionTime = System.currentTimeMillis()
                                false
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (!isOverlayVisible) {
                                onPreviousChannel()
                                isOverlayVisible = true
                                lastInteractionTime = System.currentTimeMillis()
                                true
                            } else {
                                lastInteractionTime = System.currentTimeMillis()
                                false
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (!isOverlayVisible) {
                                onNextChannel()
                                isOverlayVisible = true
                                lastInteractionTime = System.currentTimeMillis()
                                true
                            } else {
                                lastInteractionTime = System.currentTimeMillis()
                                false
                            }
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                        KeyEvent.KEYCODE_HEADSETHOOK -> {
                            playerManager.togglePlayPause()
                            isOverlayVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT,
                        KeyEvent.KEYCODE_CHANNEL_UP -> {
                            onNextChannel()
                            isOverlayVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                        KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            onPreviousChannel()
                            isOverlayVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .testTag("player_screen")
    ) {
        // ExoPlayer Surface View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.getPlayer()
                    useController = false
                    isFocusable = false
                    isFocusableInTouchMode = false
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = playerManager.getPlayer()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Apple TV Inspired Stream Buffering & Loading Overlay
        if (uiState.isLoading) {
            com.example.ui.components.TvPlayerBufferingOverlay(
                channel = uiState.currentChannel,
                statusMessage = if (uiState.currentPositionMs > 0) "Buffering Stream…" else "Connecting to Stream…"
            )
        }

        // Stream Playback Error Banner
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(460.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(TvSurface)
                        .border(1.dp, TvLiveRed, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = TvLiveRed,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Unable to Play Stream",
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = uiState.errorMessage,
                        color = TvTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TvFocusableButton(
                            text = "Retry Stream",
                            icon = Icons.Default.Refresh,
                            onClick = { playerManager.retry() },
                            isPrimary = true,
                            modifier = Modifier.focusRequester(playPauseFocusRequester),
                            tag = "btn_player_retry"
                        )

                        TvFocusableButton(
                            text = "Back to Guide",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = onBack,
                            tag = "btn_player_back_guide"
                        )
                    }
                }
            }
        }

        // On-Screen TV Overlay (controls, metadata, track selectors)
        AnimatedVisibility(
            visible = isOverlayVisible || uiState.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.90f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Metadata Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                    ) {
                        TvFocusableButton(
                            text = "Back",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = onBack,
                            modifier = Modifier.focusRequester(backFocusRequester),
                            tag = "btn_overlay_back"
                        )

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.currentChannel?.name ?: "Stream Playback",
                                    color = TvTextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                if (uiState.isLiveStream) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TvLiveRed)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = uiState.currentChannel?.groupTitle ?: "IPTV Stream",
                                color = TvAccent,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Audio & Subtitle Options Button
                    TvFocusableButton(
                        text = "Audio & Subtitles",
                        icon = Icons.Default.Subtitles,
                        onClick = {
                            lastInteractionTime = System.currentTimeMillis()
                            showTrackDialog = true
                        },
                        modifier = Modifier.focusRequester(audioSubsFocusRequester),
                        tag = "btn_audio_subs"
                    )
                }

                // Bottom Control & Progress Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Seeking progress bar ONLY for seekable non-live media (VOD / Local files)
                    if (uiState.isSeekable && uiState.durationMs > 0L) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDuration(uiState.currentPositionMs),
                                color = TvTextSecondary,
                                fontSize = 12.5.sp
                            )

                            Slider(
                                value = uiState.currentPositionMs.toFloat(),
                                onValueChange = { targetPos ->
                                    lastInteractionTime = System.currentTimeMillis()
                                    playerManager.seekTo(targetPos.toLong())
                                },
                                valueRange = 0f..uiState.durationMs.toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 14.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = TvPrimary,
                                    activeTrackColor = TvPrimary,
                                    inactiveTrackColor = TvSurfaceVariant
                                )
                            )

                            Text(
                                text = formatDuration(uiState.durationMs),
                                color = TvTextSecondary,
                                fontSize = 12.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Professional Circular Media Control Hub
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Channel Button
                        TvCircularMediaButton(
                            icon = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Channel",
                            onClick = {
                                lastInteractionTime = System.currentTimeMillis()
                                onPreviousChannel()
                            },
                            size = 56.dp,
                            iconSize = 28.dp,
                            modifier = Modifier.focusRequester(prevFocusRequester),
                            tag = "btn_prev_channel"
                        )

                        Spacer(modifier = Modifier.width(32.dp))

                        // Large Glowing Play / Pause FAB
                        TvCircularMediaButton(
                            icon = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            onClick = {
                                lastInteractionTime = System.currentTimeMillis()
                                playerManager.togglePlayPause()
                            },
                            isPrimary = true,
                            size = 72.dp,
                            iconSize = 38.dp,
                            modifier = Modifier.focusRequester(playPauseFocusRequester),
                            tag = "btn_play_pause"
                        )

                        Spacer(modifier = Modifier.width(32.dp))

                        // Next Channel Button
                        TvCircularMediaButton(
                            icon = Icons.Default.SkipNext,
                            contentDescription = "Next Channel",
                            onClick = {
                                lastInteractionTime = System.currentTimeMillis()
                                onNextChannel()
                            },
                            size = 56.dp,
                            iconSize = 28.dp,
                            modifier = Modifier.focusRequester(nextFocusRequester),
                            tag = "btn_next_channel"
                        )
                    }
                }
            }
        }

        // Audio & Subtitle Dialog
        if (showTrackDialog) {
            TrackSelectionDialog(
                uiState = uiState,
                onSelectAudio = {
                    playerManager.selectAudioTrack(it)
                    showTrackDialog = false
                    lastInteractionTime = System.currentTimeMillis()
                },
                onSelectSubtitle = {
                    playerManager.selectSubtitleTrack(it)
                    showTrackDialog = false
                    lastInteractionTime = System.currentTimeMillis()
                },
                onDismiss = {
                    showTrackDialog = false
                    lastInteractionTime = System.currentTimeMillis()
                }
            )
        }
    }
}

/**
 * High-end circular media action button with smooth D-pad focus scaling, glowing halo, and TV remote key handling.
 */
@Composable
fun TvCircularMediaButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    size: Dp = 56.dp,
    iconSize: Dp = 28.dp,
    tag: String = "tv_media_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = tween(130),
        label = "media_btn_scale"
    )

    val backgroundBrush = when {
        isFocused && isPrimary -> Brush.radialGradient(
            colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
        )
        isFocused -> Brush.radialGradient(
            colors = listOf(Color(0xFF38BDF8), Color(0xFF0369A1))
        )
        isPrimary -> Brush.radialGradient(
            colors = listOf(Color(0xFF0284C7), Color(0xFF075985))
        )
        else -> Brush.radialGradient(
            colors = listOf(Color(0x40334155), Color(0x281E293B))
        )
    }

    val iconTint = when {
        isFocused -> Color(0xFF0A101D)
        isPrimary -> Color.White
        else -> Color(0xFFF1F5F9)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .shadow(if (isFocused) 18.dp else 4.dp, CircleShape)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(
                width = if (isFocused) 3.dp else 1.5.dp,
                color = if (isFocused) Color.White else Color(0x4494A3B8),
                shape = CircleShape
            )
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP &&
                    (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                     keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                     keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
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
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun TrackSelectionDialog(
    uiState: PlayerUiState,
    onSelectAudio: (AudioTrackInfo) -> Unit,
    onSelectSubtitle: (SubtitleTrackInfo?) -> Unit,
    onDismiss: () -> Unit
) {
    val closeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Audio & Subtitle Tracks",
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TvFocusableButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.focusRequester(closeFocusRequester),
                        tag = "btn_close_track_dialog"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Audio Section
                    item {
                        Text(
                            text = "Audio Track",
                            color = TvAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (uiState.audioTracks.isEmpty()) {
                        item {
                            Text(
                                text = "Default Audio (System Stream)",
                                color = TvTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    } else {
                        items(uiState.audioTracks) { track ->
                            val isSelected = track.id == uiState.selectedAudioTrackId
                            TvFocusableButton(
                                text = "${track.label} ${if (isSelected) "✓" else ""}",
                                icon = Icons.Default.Audiotrack,
                                onClick = { onSelectAudio(track) },
                                isPrimary = isSelected,
                                modifier = Modifier.fillMaxWidth(),
                                tag = "btn_audio_${track.id}"
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Subtitles",
                            color = TvAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    item {
                        val isOff = uiState.selectedSubtitleTrackId == null
                        TvFocusableButton(
                            text = "Subtitles Off ${if (isOff) "✓" else ""}",
                            onClick = { onSelectSubtitle(null) },
                            isPrimary = isOff,
                            modifier = Modifier.fillMaxWidth(),
                            tag = "btn_sub_off"
                        )
                    }

                    items(uiState.subtitleTracks) { track ->
                        val isSelected = track.id == uiState.selectedSubtitleTrackId
                        TvFocusableButton(
                            text = "${track.label} ${if (isSelected) "✓" else ""}",
                            icon = Icons.Default.Subtitles,
                            onClick = { onSelectSubtitle(track) },
                            isPrimary = isSelected,
                            modifier = Modifier.fillMaxWidth(),
                            tag = "btn_sub_${track.id}"
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
