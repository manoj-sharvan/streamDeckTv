package com.example.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.ui.components.TvChannelCard
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFavoriteGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun FavoritesScreen(
    favorites: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    onChannelOptions: ((Channel) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(top = 20.dp, start = 24.dp, end = 24.dp)
            .testTag("favorites_screen")
    ) {
        Text(
            text = "Favorite Channels",
            color = TvTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${favorites.size} saved favorites",
            color = TvAccent,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = TvFavoriteGold.copy(alpha = 0.6f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Favorite Channels Yet",
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Press the Star icon on any channel in the Live TV guide to quickly access it here.",
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
                items(favorites, key = { it.id }) { channel ->
                    TvChannelCard(
                        channel = channel,
                        onClick = { onChannelClick(channel) },
                        onToggleFavorite = { onToggleFavorite(channel) },
                        onOptionsClick = onChannelOptions?.let { { it(channel) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
