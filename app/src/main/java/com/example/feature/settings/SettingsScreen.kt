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
    modifier: Modifier = Modifier
) {
    LazyColumn(
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
                text = "Settings & Device Preferences",
                color = TvTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage your playlists, storage cache, history, and TV remote shortcuts.",
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
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
                            text = "Managed Playlists (${playlists.size})",
                            color = TvTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TvFocusableButton(
                        text = "Add New",
                        onClick = onAddPlaylistClick,
                        isPrimary = true,
                        tag = "settings_btn_add_playlist"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No playlists stored. Import an M3U file or URL to get started.",
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
                                        text = "${playlist.channelCount} channels • ${if (playlist.isLocal) "Local SAF File" else "Remote URL"}",
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
                                            tag = "btn_activate_${playlist.id}"
                                        )
                                    }

                                    if (!playlist.isDefault) {
                                        TvFocusableButton(
                                            text = "Delete",
                                            icon = Icons.Default.Delete,
                                            onClick = { onDeletePlaylist(playlist.id) },
                                            tag = "btn_delete_${playlist.id}"
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
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
                        text = "Data & Storage Management",
                        color = TvTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Perform safe maintenance on your Room database. Favorites and playlist configurations are always preserved.",
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
                        text = "Clear Playback History",
                        icon = Icons.Default.History,
                        onClick = onClearPlaybackHistoryClick,
                        tag = "btn_settings_clear_history"
                    )

                    TvFocusableButton(
                        text = "Clear Channel Cache",
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
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
                        text = "Android TV Remote Control Guide",
                        color = TvTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• D-Pad Left / Right: Navigate between sidebar, category filters, and channels.\n" +
                            "• D-Pad Up / Down: Traverse channel rows and menu items.\n" +
                            "• D-Pad Center (Select / Enter): Tune channel, toggle play/pause, activate buttons.\n" +
                            "• Menu Key / Options: Open channel actions (Play, Favorite, Manual Language edit).\n" +
                            "• Back Button: Return from video player to live TV guide, or from sub-screens to Home.\n" +
                            "• Media Play/Pause keys: Directly toggle video playback.",
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
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
                        text = "Application Information",
                        color = TvTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "StreamDeck TV — Android TV IPTV Player\n" +
                            "Version: 1.0.0 (Release)\n" +
                            "Features: Tamil TV default feed, Custom M3U playlists, D-Pad Remote Control, Dynamic Language & Content Classification, Offline Room Cache, Media3 ExoPlayer.",
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
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
                        text = "Legal Disclaimer & Policy Compliance",
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "StreamDeck TV is an open media player application. It does not host, provide, broadcast, or bundle any television streams, IPTV subscriptions, or copyrighted media. Users must import their own legally obtained M3U playlists or local media files. Demo streams are public domain and Creative Commons authorized samples.",
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(TvSurface)
                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Android TV Launcher Banner (320 × 180 px)",
                    color = TvTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Official leanback banner displayed in the Android TV and Google TV home screen carousel.",
                    color = TvTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Image(
                    painter = painterResource(id = R.drawable.tv_banner),
                    contentDescription = "Android TV Launcher Banner",
                    modifier = Modifier
                        .width(320.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
