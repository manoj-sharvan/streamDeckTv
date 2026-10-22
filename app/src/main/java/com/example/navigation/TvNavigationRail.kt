package com.example.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
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
import com.example.ui.components.tvVerticalScrollbar

data class NavItem(
    val tab: NavTab,
    val titleRes: Int,
    val icon: ImageVector
)

@Composable
fun TvNavigationRail(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToContent: (() -> Unit)? = null,
    selectedTabFocusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    val navItems = listOf(
        NavItem(NavTab.HOME, R.string.nav_home, Icons.Default.Home),
        NavItem(NavTab.LIVE_TV, R.string.nav_live_tv, Icons.Default.LiveTv),
        NavItem(NavTab.FAVORITES, R.string.nav_favorites, Icons.Default.Star),
        NavItem(NavTab.PLAYLISTS, R.string.nav_playlists, Icons.AutoMirrored.Filled.PlaylistPlay),
        NavItem(NavTab.LOCAL_MEDIA, R.string.nav_local_media, Icons.Default.Folder),
        NavItem(NavTab.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
    )

    val railScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(208.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF141720), Color(0xFF0C0E14))
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x1AFFFFFF),
                shape = RoundedCornerShape(0.dp)
            )
            .tvVerticalScrollbar(railScrollState)
            .verticalScroll(railScrollState)
            .padding(vertical = 22.dp, horizontal = 12.dp),
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(TvPrimary, Color(0xFF0056B3))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "StreamDeck",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "TV EDITION",
                        color = TvAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.5.sp,
                        letterSpacing = 1.2.sp
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
                    onClick = { onTabSelected(item.tab) },
                    onNavigateToContent = onNavigateToContent,
                    focusRequester = if (isSelected) selectedTabFocusRequester else null
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // TV Remote Navigation hint badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF161A22))
                .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Use remote D-pad to navigate",
                color = TvTextSecondary,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun TvNavRailItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onNavigateToContent: (() -> Unit)? = null,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = tween(90),
        label = "nav_item_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused && isSelected -> Color(0xFF1B365D)
            isFocused -> Color(0xFF232B3C)
            isSelected -> Color(0xFF142136)
            else -> Color.Transparent
        },
        animationSpec = tween(90),
        label = "nav_item_bg"
    )

    val contentColor = when {
        isFocused -> Color.White
        isSelected -> TvPrimary
        else -> TvTextSecondary
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) TvPrimary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                    when (native.keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            var handled = false
                            if (onNavigateToContent != null) {
                                try {
                                    onNavigateToContent()
                                    handled = true
                                } catch (_: Exception) {}
                            }
                            if (!handled) {
                                focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Right)
                            } else {
                                true
                            }
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER,
                        android.view.KeyEvent.KEYCODE_NUMPAD_ENTER,
                        android.view.KeyEvent.KEYCODE_BUTTON_A -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(interactionSource = interactionSource, indication = androidx.compose.material3.ripple(), onClick = onClick)
            .testTag("nav_item_${item.tab.name.lowercase()}")
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TvPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(id = item.titleRes),
                color = if (isFocused || isSelected) Color.White else TvTextSecondary,
                fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.5.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
