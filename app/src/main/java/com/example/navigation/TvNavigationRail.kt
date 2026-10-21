package com.example.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NavTab
import com.example.R
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceFocused
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

data class NavItem(
    val tab: NavTab,
    val titleRes: Int,
    val icon: ImageVector
)

@Composable
fun TvNavigationRail(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem(NavTab.HOME, R.string.nav_home, Icons.Default.Home),
        NavItem(NavTab.LIVE_TV, R.string.nav_live_tv, Icons.Default.LiveTv),
        NavItem(NavTab.FAVORITES, R.string.nav_favorites, Icons.Default.Star),
        NavItem(NavTab.PLAYLISTS, R.string.nav_playlists, Icons.AutoMirrored.Filled.PlaylistPlay),
        NavItem(NavTab.LOCAL_MEDIA, R.string.nav_local_media, Icons.Default.Folder),
        NavItem(NavTab.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(200.dp)
            .background(TvSurface)
            .border(
                width = 1.dp,
                color = TvSurfaceVariant,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // App Branding Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TvPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "StreamDeck",
                        color = TvTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "TV EDITION",
                        color = TvAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Items
            navItems.forEach { item ->
                val isSelected = currentTab == item.tab
                TvNavRailItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onTabSelected(item.tab) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // TV Remote Navigation hint badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TvSurfaceCard)
                .padding(8.dp)
        ) {
            Text(
                text = "Use D-Pad remote to browse channels",
                color = TvTextSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun TvNavRailItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        label = "nav_item_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> TvSurfaceFocused
            isSelected -> TvSurfaceVariant
            else -> Color.Transparent
        },
        label = "nav_item_bg"
    )

    val contentColor = when {
        isFocused -> TvFocusBorder
        isSelected -> TvPrimary
        else -> TvTextSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) TvPrimary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .testTag("nav_item_${item.tab.name.lowercase()}")
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = item.titleRes),
                color = if (isFocused || isSelected) TvTextPrimary else TvTextSecondary,
                fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
