package com.example.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    activePlaylistId: Long?,
    refreshingPlaylistId: Long?,
    onSelectPlaylist: (Long) -> Unit,
    onRefreshPlaylist: (Long) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onOpenAddPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    contentFocusRequester: androidx.compose.ui.focus.FocusRequester? = null,
    onNavigateToRail: (() -> Unit)? = null
) {
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(top = 20.dp, start = 24.dp, end = 24.dp)
            .testTag("playlists_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Playlists",
                    color = TvTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${playlists.size} playlists stored • Tamil TV is your built-in default",
                    color = TvAccent,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            TvFocusableButton(
                text = "Add Custom Playlist",
                icon = Icons.Default.Add,
                onClick = onOpenAddPlaylist,
                isPrimary = true,
                tag = "btn_add_playlist_screen",
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

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(playlists, key = { it.id }) { playlist ->
                val isActive = playlist.id == activePlaylistId
                val isRefreshing = playlist.id == refreshingPlaylistId

                PlaylistItemCard(
                    playlist = playlist,
                    isActive = isActive,
                    isRefreshing = isRefreshing,
                    onSelect = { onSelectPlaylist(playlist.id) },
                    onRefresh = { onRefreshPlaylist(playlist.id) },
                    onRename = { playlistToRename = playlist },
                    onDelete = { playlistToDelete = playlist },
                    onNavigateToRail = onNavigateToRail
                )
            }
        }
    }

    // Rename Dialog
    playlistToRename?.let { pl ->
        RenamePlaylistDialog(
            playlist = pl,
            onConfirm = { newName ->
                onRenamePlaylist(pl.id, newName)
                playlistToRename = null
            },
            onDismiss = { playlistToRename = null }
        )
    }

    // Delete Confirmation Dialog
    playlistToDelete?.let { pl ->
        DeletePlaylistDialog(
            playlist = pl,
            onConfirm = {
                onDeletePlaylist(pl.id)
                playlistToDelete = null
            },
            onDismiss = { playlistToDelete = null }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun PlaylistItemCard(
    playlist: Playlist,
    isActive: Boolean,
    isRefreshing: Boolean,
    onSelect: () -> Unit,
    onRefresh: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToRail: (() -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val refreshedDate = remember(playlist.lastRefreshed) {
        if (playlist.lastRefreshed > 0) dateFormat.format(Date(playlist.lastRefreshed)) else "Just now"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TvSurface)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) TvPrimary else TvSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .testTag("playlist_card_${playlist.id}")
    ) {
        // Top Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (playlist.isDefault) TvPrimary.copy(alpha = 0.2f) else TvSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = if (playlist.isDefault) TvPrimary else TvTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = playlist.name,
                        color = TvTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TvSuccessGreen.copy(alpha = 0.2f))
                                .border(1.dp, TvSuccessGreen, RoundedCornerShape(4.dp))
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

                    if (isRefreshing) {
                        Spacer(modifier = Modifier.width(6.dp))
                        com.example.ui.components.TvSyncPill(message = "Refreshing…")
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${playlist.channelCount} channels • Refreshed: $refreshedDate",
                    color = TvTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (playlist.sourceUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = playlist.sourceUri,
                        color = TvAccent.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Action Bar: Dedicated FlowRow so buttons never squish or overflow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isActive) {
                TvFocusableButton(
                    text = "Select",
                    icon = Icons.Default.Check,
                    onClick = onSelect,
                    isPrimary = true,
                    tag = "btn_select_${playlist.id}",
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

            TvFocusableButton(
                text = if (isRefreshing) "Refreshing…" else "Refresh",
                icon = Icons.Default.Refresh,
                onClick = onRefresh,
                tag = "btn_refresh_${playlist.id}",
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

            if (!playlist.isDefault) {
                TvFocusableButton(
                    text = "Rename",
                    icon = Icons.Default.Edit,
                    onClick = onRename,
                    tag = "btn_rename_${playlist.id}"
                )

                TvFocusableButton(
                    text = "Delete",
                    icon = Icons.Default.Delete,
                    onClick = onDelete,
                    tag = "btn_delete_${playlist.id}"
                )
            }
        }
    }
}

@Composable
private fun RenamePlaylistDialog(
    playlist: Playlist,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(playlist.name) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 460.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Rename Playlist",
                    color = TvTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("input_rename_playlist"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TvTextPrimary,
                        unfocusedTextColor = TvTextPrimary,
                        focusedBorderColor = TvPrimary,
                        unfocusedBorderColor = TvSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TvFocusableButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        tag = "btn_rename_cancel"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    TvFocusableButton(
                        text = "Save",
                        isPrimary = true,
                        onClick = {
                            if (newName.isNotBlank()) {
                                onConfirm(newName)
                            }
                        },
                        tag = "btn_rename_save"
                    )
                }
            }
        }
    }
}

@Composable
private fun DeletePlaylistDialog(
    playlist: Playlist,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cancelFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        try {
            cancelFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 440.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, TvLiveRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Delete Custom Playlist?",
                    color = TvTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Are you sure you want to remove \"${playlist.name}\" and its ${playlist.channelCount} cached channels from your TV?",
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TvFocusableButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.focusRequester(cancelFocusRequester),
                        tag = "btn_delete_cancel"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    TvFocusableButton(
                        text = "Delete",
                        onClick = onConfirm,
                        tag = "btn_delete_confirm"
                    )
                }
            }
        }
    }
}
