package com.example.feature.localmedia

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.example.ui.components.TvFocusableButton
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun LocalMediaScreen(
    onPlayLocalFile: (Uri, String) -> Unit,
    modifier: Modifier = Modifier,
    contentFocusRequester: FocusRequester? = null,
    onNavigateToRail: (() -> Unit)? = null
) {
    // SAF Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Local Video"
            onPlayLocalFile(uri, fileName)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(top = 20.dp, start = 24.dp, end = 24.dp)
            .testTag("local_media_screen")
    ) {
        Text(
            text = "My Videos",
            color = TvTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Play videos saved on your TV or USB drive without an internet connection.",
            color = TvAccent,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Pick File Card
            TvFocusableCard(
                onClick = {
                    videoPickerLauncher.launch(
                        arrayOf("video/*", "video/mp4", "video/mkv", "video/webm", "video/mp2t")
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(240.dp)
                    .then(if (contentFocusRequester != null) Modifier.focusRequester(contentFocusRequester) else Modifier)
                    .then(
                        if (onNavigateToRail != null) {
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
                    .testTag("card_pick_local_video")
            ) { isFocused ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isFocused) TvPrimary else TvSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = null,
                            tint = if (isFocused) Color.White else TvPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Choose Video File",
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Browse videos stored on your TV or connected USB drive",
                        color = TvTextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Offline Capabilities & Supported Formats Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TvAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Supported Formats",
                            color = TvTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Videos: MP4, MKV, WebM, MPEG-TS, HLS (.m3u8)\n" +
                               "• Codecs: H.264, H.265 (HEVC), VP9, AV1\n" +
                               "• Audio: AAC, MP3, AC3, Dolby Digital, Opus\n" +
                               "• Subtitles: SRT, WebVTT, and embedded tracks",
                        color = TvTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TvSurfaceCard)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Tip: Offline playback works without internet. Live channels require a network connection.",
                            color = TvTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
