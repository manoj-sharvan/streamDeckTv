package com.example.feature.live

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.ui.components.TvCategoryChip
import com.example.ui.components.TvChannelCard
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFavoriteGold
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.SearchFilter

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
    isOnline: Boolean = true
) {
    // Unicode-aware case-insensitive multi-field search and category filtering
    val filteredChannels = remember(channels, selectedLanguage, selectedContentCategory, searchQuery) {
        SearchFilter.filter(
            channels = channels,
            query = searchQuery,
            selectedLanguage = selectedLanguage,
            selectedCategory = selectedContentCategory
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(top = 18.dp, start = 24.dp, end = 24.dp)
            .testTag("live_tv_screen")
    ) {
        // Top Header & Search bar
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Live Television",
                        color = TvTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                Text(
                    text = "${filteredChannels.size} channels found",
                    color = TvAccent,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // TV search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search name, language, category…", fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TvTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TvTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .widthIn(min = 220.dp, max = 360.dp)
                    .height(50.dp)
                    .testTag("live_tv_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TvTextPrimary,
                    unfocusedTextColor = TvTextPrimary,
                    focusedBorderColor = TvPrimary,
                    unfocusedBorderColor = TvSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 1: Languages Horizontal Bar
        if (languages.size > 1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Language:",
                    color = TvTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(languages) { lang ->
                        val isSelected = (selectedLanguage ?: "All").equals(lang, ignoreCase = true)
                        TvCategoryChip(
                            category = lang,
                            isSelected = isSelected,
                            onClick = { onLanguageSelect(lang) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Content Categories Horizontal Bar
        if (contentCategories.size > 1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Category:",
                    color = TvTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(contentCategories) { category ->
                        val isSelected = (selectedContentCategory ?: "All").equals(category, ignoreCase = true)
                        TvCategoryChip(
                            category = category,
                            isSelected = isSelected,
                            onClick = { onContentCategorySelect(category) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Channels Found",
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "No channels match \"$searchQuery\" in selected filters"
                        } else {
                            "No channels found for the selected language and category"
                        },
                        color = TvTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 210.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredChannels, key = { it.id }) { channel ->
                    TvChannelCard(
                        channel = channel,
                        onClick = { onChannelClick(channel) },
                        onToggleFavorite = { onToggleFavorite(channel) },
                        onOptionsClick = { onChannelOptions(channel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
