package com.example.feature.live

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.domain.model.PlaybackHistory
import com.example.ui.components.TvCategoryChip
import com.example.ui.components.TvChannelCard
import com.example.ui.components.TvChannelCardSkeleton
import com.example.ui.components.TvChannelInfoPanel
import com.example.ui.components.TvEmptyState
import com.example.ui.components.TvFocusableIconButton
import com.example.ui.components.tvVerticalScrollbar
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFavoriteGold
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.SearchFilter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Premium Live TV Screen matching StreamDeck TV Wireframe (Panels 2, 3, 4, 7).
 *
 * TV Navigation & UX Features:
 * - Direct Channel Selection: Clicking/Pressing OK on any channel card immediately starts playback.
 * - Unified 2D Grid: Uses LazyVerticalGrid for smooth, native D-pad 4-directional navigation & continuous scrolling.
 * - Remote Navigation Flow:
 *     Rail -> DPAD_RIGHT -> Category Chips -> DPAD_DOWN -> First Channel Card
 *     Grid -> DPAD_UP at top row -> Category Chips
 *     Grid -> DPAD_LEFT at index 0 / col 0 -> Navigation Rail
 * - Category Chips Bar: "All", "Tamil", "Sports", "News", "Entertainment", etc.
 * - Header: Live TV title, live channel counter badge, digital clock (hh:mm a), and expandable Search.
 * - Long Press / Options: Opens Channel Details Info Panel (Wireframe Panel 4) with preview, metadata, and actions.
 * - Remote Back Key: Closes Info Panel when open before navigating away.
 */
@Composable
fun LiveTvScreen(
    channels: List<Channel>,
    languages: List<String>,
    selectedLanguage: String?,
    contentCategories: List<String>,
    selectedContentCategory: String?,
    searchQuery: String,
    onLanguageSelect: (String?) -> Unit,
    onContentCategorySelect: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onChannelOptions: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true,
    isChannelsLoading: Boolean = false,
    gridState: LazyGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() },
    contentFocusRequester: FocusRequester? = null,
    onNavigateToRail: (() -> Unit)? = null,
    recentChannels: List<PlaybackHistory> = emptyList(),
    playingChannelId: Long? = null
) {
    var currentTimeString by remember { mutableStateOf("") }
    var selectedChannelForDetails by remember { mutableStateOf<Channel?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val searchFocusRequester = remember { FocusRequester() }
    val firstCategoryFocusRequester = contentFocusRequester ?: remember { FocusRequester() }
    val firstChannelFocusRequester = remember { FocusRequester() }

    // Remote Back Handler: Closes Channel Details Overlay if active
    if (selectedChannelForDetails != null) {
        BackHandler {
            selectedChannelForDetails = null
        }
    }

    // Ensure initial or restored focus lands on the first channel card or category chip
    LaunchedEffect(Unit) {
        for (attemptDelay in listOf(80L, 180L, 300L)) {
            delay(attemptDelay)
            val success = try {
                firstChannelFocusRequester.requestFocus()
                true
            } catch (_: Exception) {
                try {
                    firstCategoryFocusRequester.requestFocus()
                    true
                } catch (_: Exception) {
                    false
                }
            }
            if (success) break
        }
    }

    // Live Clock updater (HH:mm a)
    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        while (true) {
            currentTimeString = formatter.format(Date())
            delay(10_000)
        }
    }

    // Filter channels based on search and selected categories
    val filteredChannels = remember(channels, selectedLanguage, selectedContentCategory, searchQuery) {
        SearchFilter.filter(
            channels = channels,
            query = searchQuery,
            selectedLanguage = selectedLanguage,
            selectedCategory = selectedContentCategory
        )
    }

    // Combine Languages and Categories into unified category chips list (Wireframe Panel 2)
    val unifiedChips = remember(languages, contentCategories) {
        val list = mutableListOf("All")
        // Add content categories first (e.g. Entertainment, Sports, News, Movies)
        contentCategories.filter { it != "All" && it.isNotBlank() }.forEach {
            if (!list.contains(it)) list.add(it)
        }
        // Add languages (e.g. Tamil, English, Hindi)
        languages.filter { it != "All" && it.isNotBlank() }.forEach {
            if (!list.contains(it)) list.add(it)
        }
        list
    }

    // Group channels into sections for clear visual categorization in the grid
    val channelSections = remember(filteredChannels, recentChannels, channels, selectedContentCategory, selectedLanguage, searchQuery) {
        val sections = mutableListOf<Pair<String, List<Channel>>>()

        // 1. Recently Watched section if no active search or subcategory filter
        if (searchQuery.isBlank() && selectedContentCategory == null && selectedLanguage == null && recentChannels.isNotEmpty()) {
            val channelsMap = channels.associateBy { it.id }
            val recentList = recentChannels.mapNotNull { history ->
                channelsMap[history.channelId] ?: channels.firstOrNull { it.streamUrl == history.streamUrl }
            }.distinctBy { it.id }
            if (recentList.isNotEmpty()) {
                sections.add("Recently Watched" to recentList)
            }
        }

        if (searchQuery.isNotBlank()) {
            sections.add("Search Results (\"$searchQuery\")" to filteredChannels)
        } else if (selectedContentCategory != null && selectedContentCategory != "All") {
            sections.add(selectedContentCategory to filteredChannels)
        } else if (selectedLanguage != null && selectedLanguage != "All") {
            val grouped = filteredChannels.groupBy {
                if (it.contentCategory.isNotBlank()) it.contentCategory else "General"
            }
            grouped.forEach { (category, list) ->
                if (list.isNotEmpty()) sections.add(category to list)
            }
        } else {
            // "All" selected: Group by content category or language
            val grouped = filteredChannels.groupBy {
                when {
                    it.contentCategory.isNotBlank() -> it.contentCategory
                    it.effectiveLanguage.isNotBlank() -> it.effectiveLanguage.uppercase()
                    else -> "Entertainment"
                }
            }
            val sortedKeys = grouped.keys.sortedWith(
                compareByDescending<String> { it.equals("Entertainment", ignoreCase = true) }
                    .thenByDescending { it.equals("Tamil", ignoreCase = true) }
                    .thenByDescending { it.equals("Sports", ignoreCase = true) }
                    .thenByDescending { it.equals("News", ignoreCase = true) }
                    .thenByDescending { it.equals("Movies", ignoreCase = true) }
            )
            sortedKeys.forEach { key ->
                grouped[key]?.let { list ->
                    if (list.isNotEmpty()) {
                        sections.add(key to list)
                    }
                }
            }
        }
        sections
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .testTag("live_tv_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp, start = 24.dp, end = 24.dp)
        ) {
            // Top Header: "Live TV" + Channel Count Badge on left, Search & Clock on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = TvPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Live TV",
                        color = TvTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TvPrimary.copy(alpha = 0.18f))
                            .border(1.dp, TvPrimary.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${filteredChannels.size} Channels",
                            color = TvPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isOnline) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TvFavoriteGold.copy(alpha = 0.2f))
                                .border(1.dp, TvFavoriteGold, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OFFLINE CACHE",
                                color = TvFavoriteGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Right controls: Search box + Clock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isSearchExpanded || searchQuery.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search channels…", fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TvTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    onSearchQueryChange("")
                                    isSearchExpanded = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Close Search",
                                        tint = TvTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .width(240.dp)
                                .height(46.dp)
                                .focusRequester(searchFocusRequester)
                                .onPreviewKeyEvent { keyEvent ->
                                    val native = keyEvent.nativeKeyEvent
                                    if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                                        when (native.keyCode) {
                                            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                try {
                                                    firstCategoryFocusRequester.requestFocus()
                                                } catch (_: Exception) {}
                                                true
                                            }
                                            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                                onNavigateToRail?.invoke()
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TvTextPrimary,
                                unfocusedTextColor = TvTextPrimary,
                                focusedBorderColor = TvPrimary,
                                unfocusedBorderColor = TvSurfaceVariant
                            )
                        )
                    } else {
                        TvFocusableIconButton(
                            icon = Icons.Default.Search,
                            contentDescription = "Search",
                            onClick = {
                                isSearchExpanded = true
                                searchFocusRequester.requestFocus()
                            },
                            modifier = Modifier.onPreviewKeyEvent { keyEvent ->
                                val native = keyEvent.nativeKeyEvent
                                if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                                    when (native.keyCode) {
                                        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                            try {
                                                firstCategoryFocusRequester.requestFocus()
                                            } catch (_: Exception) {}
                                            true
                                        }
                                        android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                            onNavigateToRail?.invoke()
                                            true
                                        }
                                        else -> false
                                    }
                                } else false
                            }
                        )
                    }

                    // Digital Clock (Wireframe Panel 2)
                    if (currentTimeString.isNotBlank()) {
                        Text(
                            text = currentTimeString,
                            color = TvTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Category Chips Bar (Wireframe Panel 2)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(unifiedChips) { index, chipName ->
                    val isSelected = when {
                        chipName == "All" -> (selectedContentCategory == null || selectedContentCategory == "All") && (selectedLanguage == null || selectedLanguage == "All")
                        contentCategories.contains(chipName) -> selectedContentCategory.equals(chipName, ignoreCase = true)
                        languages.contains(chipName) -> selectedLanguage.equals(chipName, ignoreCase = true)
                        else -> false
                    }

                    TvCategoryChip(
                        category = chipName,
                        isSelected = isSelected,
                        onClick = {
                            when {
                                chipName == "All" -> {
                                    onContentCategorySelect(null)
                                    onLanguageSelect(null)
                                }
                                contentCategories.contains(chipName) -> {
                                    onContentCategorySelect(chipName)
                                }
                                languages.contains(chipName) -> {
                                    onLanguageSelect(chipName)
                                }
                            }
                        },
                        modifier = Modifier
                            .then(if (index == 0) Modifier.focusRequester(firstCategoryFocusRequester) else Modifier)
                            .onPreviewKeyEvent { keyEvent ->
                                val native = keyEvent.nativeKeyEvent
                                if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                                    when (native.keyCode) {
                                        android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                            if (index == 0 && onNavigateToRail != null) {
                                                onNavigateToRail()
                                                true
                                            } else false
                                        }
                                        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                            // Move directly from Category Chips down to the Channel Grid
                                            try {
                                                firstChannelFocusRequester.requestFocus()
                                            } catch (_: Exception) {}
                                            true
                                        }
                                        android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                            try {
                                                searchFocusRequester.requestFocus()
                                            } catch (_: Exception) {}
                                            true
                                        }
                                        else -> false
                                    }
                                } else false
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content Area: Unified 2D Channel Grid with smooth D-pad scrolling
            if (isChannelsLoading) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 190.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(12) {
                        TvChannelCardSkeleton(modifier = Modifier.fillMaxWidth())
                    }
                }
            } else if (filteredChannels.isEmpty()) {
                TvEmptyState(
                    title = if (searchQuery.isNotBlank()) "No channels match \"$searchQuery\"" else "No channels found",
                    subtitle = "Try selecting \"All\" or another category",
                    actionLabel = "Show All Channels",
                    onAction = {
                        onSearchQueryChange("")
                        onContentCategorySelect(null)
                        onLanguageSelect(null)
                    }
                )
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 190.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 36.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .tvVerticalScrollbar(gridState)
                ) {
                    var globalCardIndex = 0

                    channelSections.forEachIndexed { sectionIndex, (sectionTitle, sectionChannels) ->
                        // Full-width section header if multiple categories exist
                        if (channelSections.size > 1) {
                            item(
                                span = { GridItemSpan(maxLineSpan) },
                                key = "section_header_$sectionTitle"
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (sectionIndex > 0) 14.dp else 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sectionTitle,
                                        color = TvTextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${sectionChannels.size} channels",
                                        color = TvTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        itemsIndexed(
                            items = sectionChannels,
                            key = { _, channel -> "${sectionTitle}_${channel.id}" }
                        ) { indexInSection, channel ->
                            val currentCardIndex = globalCardIndex++
                            val isFirstCard = currentCardIndex == 0
                            val isTopRow = sectionIndex == 0 && indexInSection < 5

                            TvChannelCard(
                                channel = channel,
                                onClick = {
                                    // DIRECT PLAYBACK: Immediately play the channel on TV selection
                                    onChannelClick(channel)
                                },
                                onToggleFavorite = { onToggleFavorite(channel) },
                                onOptionsClick = {
                                    // LONG PRESS / OPTIONS: Open Channel Details Info Panel
                                    selectedChannelForDetails = channel
                                },
                                isSelected = playingChannelId == channel.id,
                                isPlaying = playingChannelId == channel.id,
                                focusRequester = if (isFirstCard) firstChannelFocusRequester else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onPreviewKeyEvent { keyEvent ->
                                        val native = keyEvent.nativeKeyEvent
                                        if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                                            when (native.keyCode) {
                                                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                                    // Move back to navigation rail from leftmost items or first card
                                                    if (isFirstCard && onNavigateToRail != null) {
                                                        onNavigateToRail()
                                                        true
                                                    } else false
                                                }
                                                android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                                    // Move back up to category chips from top row
                                                    if (isTopRow) {
                                                        try {
                                                            firstCategoryFocusRequester.requestFocus()
                                                        } catch (_: Exception) {}
                                                        true
                                                    } else false
                                                }
                                                else -> false
                                            }
                                        } else false
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Channel Details / Info Panel Overlay (Wireframe Panel 4) - Triggered via Long Press or Options
        selectedChannelForDetails?.let { detailChannel ->
            TvChannelInfoPanel(
                channel = detailChannel,
                onWatch = {
                    val toPlay = detailChannel
                    selectedChannelForDetails = null
                    onChannelClick(toPlay)
                },
                onToggleFavorite = { onToggleFavorite(detailChannel) },
                onMoreInfo = { onChannelOptions(detailChannel) },
                onClose = { selectedChannelForDetails = null }
            )
        }
    }
}
