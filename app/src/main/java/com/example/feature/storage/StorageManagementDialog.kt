package com.example.feature.storage

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.feature.common.ConfirmActionDialog
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvFavoriteGold
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSuccessGreen
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.AppCacheInfo
import com.example.util.StorageInfo
import com.example.util.StorageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Storage & Cache Popup with Inputs Menu Dropdown UI Behavior (Wireframe matching docs/inputs-menu.jpg).
 * - Anchored top-right directly beneath the top bar controls.
 * - Frosted glass dropdown with rounded corners and high-contrast pill item focus.
 * - Displays device storage progress, app cache management, and Android Settings deep link.
 */
@Composable
fun StorageManagementDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var storageInfo by remember { mutableStateOf(StorageManager.getStorageInfo(context)) }
    var cacheInfo by remember { mutableStateOf<AppCacheInfo?>(null) }
    var isClearingCache by remember { mutableStateOf(false) }
    var showConfirmClearCache by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    val firstItemFocusRequester = remember { FocusRequester() }

    fun refreshStats() {
        coroutineScope.launch {
            storageInfo = StorageManager.getStorageInfo(context)
            cacheInfo = StorageManager.getAppCacheInfo(context)
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
        delay(100)
        try {
            firstItemFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Refresh when returning from System Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Back key dismisses dropdown popup
    BackHandler(enabled = true) {
        if (showConfirmClearCache) {
            showConfirmClearCache = false
        } else {
            onDismiss()
        }
    }

    // Fullscreen scrim container: clicking outside closes popup
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("storage_dropdown_overlay")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { -16 },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(140)) { -16 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 24.dp)
        ) {
            // Frosted Glass Dropdown Panel (matching docs/inputs-menu.jpg)
            Box(
                modifier = modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF0131722)) // Deep frosted glass
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0x40FFFFFF),
                                Color(0x18FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click inside panel
                    )
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Optional feedback banner inside dropdown
                    if (feedbackMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isFeedbackError) TvLiveRed.copy(alpha = 0.2f)
                                    else TvSuccessGreen.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFeedbackError) Icons.Default.Warning else Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = if (isFeedbackError) TvLiveRed else TvSuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedbackMessage ?: "",
                                color = TvTextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 1. Device Storage Row (with embedded progress bar)
                    StorageMenuDeviceRow(
                        storageInfo = storageInfo,
                        focusRequester = firstItemFocusRequester,
                        onClick = {
                            val opened = StorageManager.openSystemStorageSettings(context)
                            if (opened) {
                                onDismiss()
                            } else {
                                feedbackMessage = "Could not open Android Settings."
                                isFeedbackError = true
                            }
                        }
                    )

                    DropdownMenuSeparator()

                    // 2. Clear App Cache Row
                    StorageMenuItemRow(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear App Cache",
                        subtitle = "Temp buffers & thumbnails (${cacheInfo?.cacheFormatted ?: "Calculating…"})",
                        statusText = if (isClearingCache) "Clearing…" else "Clear",
                        statusDotColor = if (isClearingCache) TvFavoriteGold else TvAccent,
                        onClick = {
                            showConfirmClearCache = true
                        }
                    )

                    DropdownMenuSeparator()

                    // 3. Manage System Storage Row
                    StorageMenuItemRow(
                        icon = Icons.Default.Settings,
                        title = "System Storage",
                        subtitle = "Manage all TV apps & flash memory",
                        statusText = "Open ↗",
                        statusDotColor = null,
                        onClick = {
                            val opened = StorageManager.openSystemStorageSettings(context)
                            if (opened) {
                                onDismiss()
                            } else {
                                feedbackMessage = "Could not open Android Settings."
                                isFeedbackError = true
                            }
                        }
                    )

                    DropdownMenuSeparator()

                    // 4. Refresh Row
                    StorageMenuItemRow(
                        icon = Icons.Default.Refresh,
                        title = "Refresh Storage Info",
                        subtitle = "Query latest device stats",
                        statusText = null,
                        statusDotColor = null,
                        onClick = {
                            feedbackMessage = null
                            refreshStats()
                        }
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Clearing Cache
    if (showConfirmClearCache) {
        val cacheSizeText = cacheInfo?.cacheFormatted ?: "0 B"
        ConfirmActionDialog(
            title = "Clear Application Cache?",
            message = "This will delete temporary image thumbnails and stream buffers ($cacheSizeText). " +
                    "Your playlists, saved favorites, watch history, and user preferences are strictly preserved.",
            confirmText = "Clear Cache",
            cancelText = "Cancel",
            isDestructive = true,
            icon = Icons.Default.Delete,
            onConfirm = {
                showConfirmClearCache = false
                coroutineScope.launch {
                    isClearingCache = true
                    val result = StorageManager.clearAppCache(context)
                    isClearingCache = false
                    if (result.success) {
                        feedbackMessage = if (result.bytesDeleted > 0L) {
                            "Freed ${result.bytesDeletedFormatted} of cache"
                        } else {
                            "App cache is empty"
                        }
                        isFeedbackError = false
                    } else {
                        feedbackMessage = "Cleaned ${result.bytesDeletedFormatted}"
                        isFeedbackError = false
                    }
                    refreshStats()
                }
            },
            onDismiss = {
                showConfirmClearCache = false
            }
        )
    }
}

/**
 * Device Storage Row with Integrated Progress Bar.
 * When focused, becomes a bright white rounded pill just like HDMI 1 in inputs-menu.jpg.
 */
@Composable
private fun StorageMenuDeviceRow(
    storageInfo: StorageInfo,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedProgress by animateFloatAsState(
        targetValue = (storageInfo.usedPercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "dropdown_progress_anim"
    )

    val itemShape = RoundedCornerShape(12.dp)
    val itemBg = if (isFocused) Color.White else Color.Transparent
    val titleColor = if (isFocused) Color(0xFF0C101B) else TvTextPrimary
    val subtitleColor = if (isFocused) Color(0xFF4A5568) else TvTextSecondary
    val iconTint = if (isFocused) Color(0xFF0C101B) else TvPrimary

    val progressGradient = when {
        isFocused -> Brush.horizontalGradient(listOf(Color(0xFF0A84FF), Color(0xFF0056B3)))
        storageInfo.isCriticallyLow || storageInfo.usedPercentage >= 90 -> Brush.horizontalGradient(
            listOf(TvLiveRed, Color(0xFFDC2626))
        )
        storageInfo.isLowStorage || storageInfo.usedPercentage >= 75 -> Brush.horizontalGradient(
            listOf(TvFavoriteGold, Color(0xFFF59E0B))
        )
        else -> Brush.horizontalGradient(
            listOf(TvPrimary, TvAccent)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .clip(itemShape)
            .background(itemBg)
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                if (isConfirm && native.action == android.view.KeyEvent.ACTION_UP) {
                    onClick()
                    true
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Device Storage",
                        color = titleColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status info & Dot (like "Connected" in inputs popup)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    storageInfo.isCriticallyLow -> TvLiveRed
                                    storageInfo.isLowStorage -> TvFavoriteGold
                                    else -> TvSuccessGreen
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${storageInfo.availableFormatted} free",
                        color = if (isFocused) Color(0xFF0C101B) else if (storageInfo.isLowStorage) TvFavoriteGold else TvSuccessGreen,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // Proportional Horizontal Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isFocused) Color(0x30000000) else Color(0x28FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressGradient)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Subtitle metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${storageInfo.usedFormatted} / ${storageInfo.totalFormatted} (${storageInfo.usedPercentage}%)",
                    color = subtitleColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isFocused) "Settings ↗" else "Settings",
                    color = subtitleColor,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

/**
 * Standard Item Row in the Inputs-Style Dropdown Menu.
 * Focus transforms it into a bright white pill matching docs/inputs-menu.jpg.
 */
@Composable
private fun StorageMenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    statusText: String? = null,
    statusDotColor: Color? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val itemShape = RoundedCornerShape(12.dp)
    val itemBg = if (isFocused) Color.White else Color.Transparent
    val titleColor = if (isFocused) Color(0xFF0C101B) else TvTextPrimary
    val subtitleColor = if (isFocused) Color(0xFF4A5568) else TvTextSecondary
    val iconTint = if (isFocused) Color(0xFF0C101B) else TvTextSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(itemShape)
            .background(itemBg)
            .onPreviewKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                val isConfirm = native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_BUTTON_A
                if (isConfirm && native.action == android.view.KeyEvent.ACTION_UP) {
                    onClick()
                    true
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = titleColor,
                        fontSize = 13.5.sp,
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            color = subtitleColor,
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            if (statusText != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    if (statusDotColor != null) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = statusText,
                        color = if (isFocused) Color(0xFF0C101B) else TvTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Subtle Separator matching docs/inputs-menu.jpg.
 */
@Composable
private fun DropdownMenuSeparator() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        thickness = 0.8.dp,
        color = Color(0x18FFFFFF)
    )
}
