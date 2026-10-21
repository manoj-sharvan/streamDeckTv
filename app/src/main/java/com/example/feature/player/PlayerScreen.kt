package com.example.feature.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
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
    val context = LocalContext.current
    var isOverlayVisible by remember { mutableStateOf(true) }
    var showTrackDialog by remember { mutableStateOf(false) }

    // Intercept hardware Back button on Android TV remote to safely exit player
    BackHandler {
        if (showTrackDialog) {
            showTrackDialog = false
        } else {
            onBack()
        }
    }

    // Auto-hide TV player overlay after 5 seconds of inactivity
    LaunchedEffect(isOverlayVisible, uiState.isPlaying) {
        if (isOverlayVisible && uiState.isPlaying && uiState.errorMessage == null) {
            delay(5000)
            isOverlayVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isOverlayVisible = !isOverlayVisible
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

        // Loading Indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = TvPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buffering Stream…",
                        color = TvTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Stream Playback Error Banner
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
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
                            tag = "btn_player_retry"
                        )

                        TvFocusableButton(
                            text = "Back to Guide",
                            icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
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
                                Color.Black.copy(alpha = 0.85f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(24.dp),
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
                            icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = onBack,
                            tag = "btn_overlay_back"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.currentChannel?.name ?: "Stream Playback",
                                    color = TvTextPrimary,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                if (uiState.isLiveStream) {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TvLiveRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
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
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Track & Subtitle Options Button
                    if (uiState.audioTracks.isNotEmpty() || uiState.subtitleTracks.isNotEmpty()) {
                        TvFocusableButton(
                            text = "Audio & Subtitles",
                            icon = Icons.Default.Subtitles,
                            onClick = { showTrackDialog = true },
                            tag = "btn_audio_subs"
                        )
                    }
                }

                // Bottom Control & Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Seeking progress bar ONLY for seekable non-live media (VOD / Local files)
                    if (uiState.isSeekable && uiState.durationMs > 0L) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDuration(uiState.currentPositionMs),
                                color = TvTextSecondary,
                                fontSize = 12.sp
                            )

                            Slider(
                                value = uiState.currentPositionMs.toFloat(),
                                onValueChange = { targetPos ->
                                    playerManager.seekTo(targetPos.toLong())
                                },
                                valueRange = 0f..uiState.durationMs.toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = TvPrimary,
                                    activeTrackColor = TvPrimary,
                                    inactiveTrackColor = TvSurfaceVariant
                                )
                            )

                            Text(
                                text = formatDuration(uiState.durationMs),
                                color = TvTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Media Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvFocusableButton(
                            text = "Previous",
                            icon = Icons.Default.SkipPrevious,
                            onClick = onPreviousChannel,
                            tag = "btn_prev_channel"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        TvFocusableButton(
                            text = if (uiState.isPlaying) "Pause" else "Play",
                            icon = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            onClick = { playerManager.togglePlayPause() },
                            isPrimary = true,
                            tag = "btn_play_pause"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        TvFocusableButton(
                            text = "Next",
                            icon = Icons.Default.SkipNext,
                            onClick = onNextChannel,
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
                onSelectAudio = { playerManager.selectAudioTrack(it) },
                onSelectSubtitle = { playerManager.selectSubtitleTrack(it) },
                onDismiss = { showTrackDialog = false }
            )
        }
    }
}

@Composable
private fun TrackSelectionDialog(
    uiState: PlayerUiState,
    onSelectAudio: (AudioTrackInfo) -> Unit,
    onSelectSubtitle: (SubtitleTrackInfo?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(480.dp)
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
                        tag = "btn_close_track_dialog"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Tracks
                if (uiState.audioTracks.isNotEmpty()) {
                    Text(
                        text = "Audio Track",
                        color = TvAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.audioTracks.forEach { track ->
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Subtitle Tracks
                Text(
                    text = "Subtitles",
                    color = TvAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val isOff = uiState.selectedSubtitleTrackId == null
                    TvFocusableButton(
                        text = "Off ${if (isOff) "✓" else ""}",
                        onClick = { onSelectSubtitle(null) },
                        isPrimary = isOff,
                        modifier = Modifier.fillMaxWidth(),
                        tag = "btn_sub_off"
                    )

                    uiState.subtitleTracks.forEach { track ->
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
