package com.example.feature.home

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.domain.model.PlaybackHistory
import com.example.domain.model.Playlist
import com.example.ui.components.TvCategoryChip
import com.example.ui.components.TvChannelCard
import com.example.ui.components.TvFocusableButton
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFavoriteGold
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    playlists: List<Playlist>,
    activePlaylist: Playlist?,
    channels: List<Channel>,
    recentChannels: List<PlaybackHistory>,
    favorites: List<Channel>,
    languages: List<String>,
    contentCategories: List<String>,
    isOnline: Boolean,
    isRefreshing: Boolean,
    onSelectPlaylist: (Long) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onChannelOptions: (Channel) -> Unit,
    onRefreshActivePlaylist: () -> Unit,
    onOpenAddPlaylist: () -> Unit,
    onLoadDemoChannels: () -> Unit,
    onNavigateToLiveTv: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToLocalMedia: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectLanguageAndNavigate: (String) -> Unit,
    onSelectCategoryAndNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("HH:mm · EEE, MMM d", Locale.getDefault())
        while (true) {
            currentTimeString = formatter.format(Date())
            delay(10_000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(top = 20.dp, start = 24.dp, end = 24.dp)
            .testTag("home_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = "StreamDeck TV",
                    color = TvTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (activePlaylist != null) {
                        "Active Playlist: ${activePlaylist.name} (${activePlaylist.channelCount} channels)"
                    } else {
                        "No active playlist loaded"
                    },
                    color = TvAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = currentTimeString,
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.padding(end = 6.dp)
                )

                if (activePlaylist != null) {
                    TvFocusableButton(
                        text = if (isRefreshing) "Refreshing…" else "Refresh",
                        icon = Icons.Default.Refresh,
                        onClick = onRefreshActivePlaylist,
                        tag = "home_btn_refresh_playlist"
                    )
                }

                TvFocusableButton(
                    text = "Add Playlist",
                    icon = Icons.Default.Add,
                    onClick = onOpenAddPlaylist,
                    isPrimary = true,
                    tag = "home_btn_add_playlist"
                )
            }
        }

        // Offline Notification Banner
        if (!isOnline) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TvFavoriteGold.copy(alpha = 0.15f))
                    .border(1.dp, TvFavoriteGold, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = TvFavoriteGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Offline Mode: Showing locally cached channels. Internet connection required to refresh or stream.",
                    color = TvTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (playlists.isEmpty()) {
            EmptyHomeState(
                onOpenAddPlaylist = onOpenAddPlaylist,
                onLoadDemoChannels = onLoadDemoChannels
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Playlists Switcher Row (if more than 1)
                if (playlists.size > 1) {
                    item {
                        Column {
                            Text(
                                text = "Your Playlists",
                                color = TvTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(playlists, key = { it.id }) { playlist ->
                                    val isSelected = activePlaylist?.id == playlist.id
                                    val label = if (playlist.isDefault) {
                                        "${playlist.name} ★ (${playlist.channelCount})"
                                    } else {
                                        "${playlist.name} (${playlist.channelCount})"
                                    }
                                    TvCategoryChip(
                                        category = label,
                                        isSelected = isSelected,
                                        onClick = { onSelectPlaylist(playlist.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Hero Quick Watch Banner
                item {
                    HeroQuickWatchBanner(
                        activePlaylist = activePlaylist,
                        channelCount = channels.size,
                        onNavigateToLiveTv = onNavigateToLiveTv
                    )
                }

                // Quick Navigation Hub
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeNavCard(
                            title = "Favorites",
                            count = "${favorites.size} saved",
                            icon = Icons.Default.Star,
                            iconTint = TvFavoriteGold,
                            onClick = onNavigateToFavorites,
                            modifier = Modifier.weight(1f)
                        )
                        HomeNavCard(
                            title = "My Playlists",
                            count = "${playlists.size} active",
                            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                            iconTint = TvPrimary,
                            onClick = onNavigateToPlaylists,
                            modifier = Modifier.weight(1f)
                        )
                        HomeNavCard(
                            title = "Local Media",
                            count = "Storage",
                            icon = Icons.Default.Folder,
                            iconTint = TvAccent,
                            onClick = onNavigateToLocalMedia,
                            modifier = Modifier.weight(1f)
                        )
                        HomeNavCard(
                            title = "Settings",
                            count = "Options",
                            icon = Icons.Default.Settings,
                            iconTint = TvTextSecondary,
                            onClick = onNavigateToSettings,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recently Watched Section (Last 5 channels played for quick access)
                item {
                    val last5Recent = recentChannels.take(5)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recently_watched_section")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = TvAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recently Watched",
                                    color = TvTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (last5Recent.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TvPrimary.copy(alpha = 0.2f))
                                            .border(1.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${last5Recent.size}/5 RECENT",
                                            color = TvPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (last5Recent.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(last5Recent, key = { it.channelId }) { history ->
                                    val matchedChannel = channels.firstOrNull { it.id == history.channelId || it.streamUrl == history.streamUrl }
                                        ?: Channel(
                                            id = history.channelId,
                                            playlistId = activePlaylist?.id ?: 0,
                                            name = history.channelName,
                                            streamUrl = history.streamUrl,
                                            groupTitle = history.groupTitle,
                                            tvgLogo = history.tvgLogo
                                        )
                                    TvChannelCard(
                                        channel = matchedChannel,
                                        onClick = { onChannelClick(matchedChannel) },
                                        onToggleFavorite = { onToggleFavorite(matchedChannel) },
                                        onOptionsClick = { onChannelOptions(matchedChannel) },
                                        modifier = Modifier.width(220.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TvSurfaceCard)
                                    .border(1.dp, TvSurfaceVariant, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "No recently watched channels yet. Tune into any channel from the guide to populate your quick-access list.",
                                    color = TvTextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Dynamic Language Categories Row
                val nonAllLanguages = languages.filter { it != "All" }
                if (nonAllLanguages.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = TvPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Languages (${nonAllLanguages.size})",
                                    color = TvTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(nonAllLanguages) { lang ->
                                    TvCategoryChip(
                                        category = lang,
                                        isSelected = false,
                                        onClick = { onSelectLanguageAndNavigate(lang) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Dynamic Content Categories Row
                val nonAllContentCategories = contentCategories.filter { it != "All" }
                if (nonAllContentCategories.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = TvAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Content Categories (${nonAllContentCategories.size})",
                                    color = TvTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(nonAllContentCategories) { cat ->
                                    TvCategoryChip(
                                        category = cat,
                                        isSelected = false,
                                        onClick = { onSelectCategoryAndNavigate(cat) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Favorites Row (if any exist)
                if (favorites.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = TvFavoriteGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Your Favorites (${favorites.size})",
                                        color = TvTextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TvFocusableButton(
                                    text = "View All",
                                    onClick = onNavigateToFavorites,
                                    tag = "btn_view_all_favorites"
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(favorites.take(15), key = { it.id }) { channel ->
                                    TvChannelCard(
                                        channel = channel,
                                        onClick = { onChannelClick(channel) },
                                        onToggleFavorite = { onToggleFavorite(channel) },
                                        onOptionsClick = { onChannelOptions(channel) },
                                        modifier = Modifier.width(220.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // All Channels Guide Row
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activePlaylist?.name ?: "Channels"} Guide",
                                color = TvTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = 12.dp)
                            )
                            TvFocusableButton(
                                text = "View All Channels (${channels.size})",
                                icon = Icons.Default.LiveTv,
                                onClick = onNavigateToLiveTv,
                                tag = "btn_view_all_channels"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(channels.take(20), key = { it.id }) { channel ->
                                TvChannelCard(
                                    channel = channel,
                                    onClick = { onChannelClick(channel) },
                                    onToggleFavorite = { onToggleFavorite(channel) },
                                    onOptionsClick = { onChannelOptions(channel) },
                                    modifier = Modifier.width(220.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeNavCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        modifier = modifier.height(68.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TvTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = count,
                    color = if (isFocused) TvPrimary else TvTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HeroQuickWatchBanner(
    activePlaylist: Playlist?,
    channelCount: Int,
    onNavigateToLiveTv: () -> Unit
) {
    TvFocusableCard(
        onClick = onNavigateToLiveTv,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .testTag("hero_quick_watch_banner")
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TvPrimary.copy(alpha = 0.2f))
                            .border(1.dp, TvPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (activePlaylist?.isDefault == true) "BUILT-IN TAMIL TV" else "LIVE TV GUIDE",
                            color = TvPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$channelCount Channels Available",
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = activePlaylist?.name ?: "StreamDeck Live TV",
                    color = TvTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Press Select or Enter to open the full TV Guide with category filters and search.",
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isFocused) TvPrimary else TvSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Watch",
                    tint = if (isFocused) Color(0xFF0F172A) else TvTextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHomeState(
    onOpenAddPlaylist: () -> Unit,
    onLoadDemoChannels: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TvSurface)
                .border(1.dp, TvSurfaceVariant, RoundedCornerShape(16.dp))
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TvPrimary.copy(alpha = 0.15f))
                    .border(1.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = null,
                    tint = TvPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading Tamil TV Playlist…",
                color = TvTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "StreamDeck TV automatically loads the built-in Tamil TV playlist on first start. If network is unavailable, you can also add custom playlists or demo channels.",
                color = TvTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvFocusableButton(
                    text = "Add Custom Playlist",
                    icon = Icons.Default.Add,
                    onClick = onOpenAddPlaylist,
                    isPrimary = true,
                    tag = "empty_btn_add_playlist"
                )

                TvFocusableButton(
                    text = "Load Demo Streams",
                    icon = Icons.Default.PlayArrow,
                    onClick = onLoadDemoChannels,
                    tag = "empty_btn_load_demo"
                )
            }
        }
    }
}
