package com.example.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.example.R
import com.example.domain.model.Playlist
import com.example.ui.components.TvFocusableButton
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSuccessGreen
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun SettingsScreen(
    playlists: List<Playlist>,
    activePlaylistId: Long?,
    onSelectPlaylist: (Long) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onClearPlaybackHistoryClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onOpenStorageDialog: () -> Unit = {},
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    contentFocusRequester: FocusRequester? = null,
    onNavigateToRail: (() -> Unit)? = null
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(top = 20.dp, start = 24.dp, end = 24.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Settings",
                color = TvTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Customize your playlists, storage, and remote shortcuts.",
                color = TvAccent,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Playlists Management Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = null,
                            tint = TvPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Your Playlists (${playlists.size})",
                            color = TvTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TvFocusableButton(
                        text = "Add Playlist",
                        onClick = onAddPlaylistClick,
                        isPrimary = true,
                        tag = "settings_btn_add_playlist",
                        modifier = Modifier
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
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No playlists yet. Add a web link or open a file to start watching.",
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        playlists.forEach { playlist ->
                            val isActive = playlist.id == activePlaylistId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TvSurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isActive) TvPrimary else TvSurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = playlist.name,
                                            color = TvTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (playlist.isDefault) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(TvPrimary)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "DEFAULT",
                                                    color = Color(0xFF0A101D),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(TvSuccessGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    color = TvSuccessGreen,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${playlist.channelCount} channels • ${if (playlist.isLocal) "From Device" else "Online Stream"}",
                                        color = TvTextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isActive) {
                                        TvFocusableButton(
                                            text = "Activate",
                                            icon = Icons.Default.Check,
                                            onClick = { onSelectPlaylist(playlist.id) },
                                            tag = "btn_activate_${playlist.id}",
                                            modifier = if (onNavigateToRail != null) {
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
                                    }

                                    if (!playlist.isDefault) {
                                        TvFocusableButton(
                                            text = "Delete",
                                            icon = Icons.Default.Delete,
                                            onClick = { onDeletePlaylist(playlist.id) },
                                            tag = "btn_delete_${playlist.id}",
                                            modifier = if (isActive && onNavigateToRail != null) {
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Data & Cache Management Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = TvPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Storage & Data",
                        color = TvTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Free up temporary cached data. Your playlists and favorite channels will always be preserved.",
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TvFocusableButton(
                        text = "Storage & Cache Monitor",
                        icon = Icons.Default.Storage,
                        onClick = onOpenStorageDialog,
                        isPrimary = true,
                        tag = "btn_settings_storage_dashboard",
                        modifier = if (onNavigateToRail != null) {
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

                    TvFocusableButton(
                        text = "Clear Watch History",
                        icon = Icons.Default.History,
                        onClick = onClearPlaybackHistoryClick,
                        tag = "btn_settings_clear_history"
                    )

                    TvFocusableButton(
                        text = "Free Up Cached Data",
                        icon = Icons.Default.Delete,
                        onClick = onClearCacheClick,
                        tag = "btn_settings_clear_cache"
                    )
                }
            }
        }

        // TV Remote Navigation Guide
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SettingsRemote,
                        contentDescription = null,
                        tint = TvAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Remote Control Shortcuts",
                        color = TvTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• Arrow Buttons: Browse through menus, categories, and channels.\n" +
                            "• Center Button (OK): Watch channel, play/pause video, or choose an item.\n" +
                            "• Back Button: Return to the previous screen or close the video player.\n" +
                            "• Menu Key: Quick options to favorite or edit a channel.\n" +
                            "• Play / Pause: Instantly control playback anytime.",
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // App Information & Version
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TvPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "About StreamDeck",
                        color = TvTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "StreamDeck TV — Version 1.0.0\n" +
                            "A modern TV media player supporting live channels, custom playlists, and offline videos.",
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // Legal & Compliance Notice
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = TvTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "About Content & Sources",
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "StreamDeck TV is an independent media player that lets you enjoy your own content. It does not provide, host, or broadcast any media or channels. Please ensure you have the rights to any links or playlists you add.",
                    color = TvTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Android TV Launcher Banner Preview
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Home Screen Banner Preview",
                    color = TvTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Banner shown on your TV home screen launcher.",
                    color = TvTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Image(
                    painter = painterResource(id = R.drawable.tv_banner),
                    contentDescription = "TV Launcher Banner",
                    modifier = Modifier
                        .width(320.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
