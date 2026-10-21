package com.example.feature.playlist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.parser.PlaylistValidator
import com.example.data.parser.ValidationResult
import com.example.data.repository.ImportStatus
import com.example.ui.components.TvFocusableButton
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSuccessGreen
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddPlaylistDialog(
    importStatus: ImportStatus,
    onImportUrl: (url: String, name: String?) -> Unit,
    onImportUri: (uri: Uri, name: String?) -> Unit,
    onLoadDemo: () -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf("") }
    var playlistNameText by remember { mutableStateOf("") }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    // SAF Document Picker for local M3U / M3U8 files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportUri(uri, playlistNameText.takeIf { it.isNotBlank() })
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 620.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, TvPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("add_playlist_dialog"),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add Custom M3U / M3U8 Playlist",
                    color = TvTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Provide an HTTP/HTTPS playlist URL, pick a local M3U file, or load public demo channels.",
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Status banners
                when (importStatus) {
                    is ImportStatus.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TvSurfaceCard)
                                .border(1.dp, TvPrimary, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = TvPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = importStatus.message,
                                color = TvTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    is ImportStatus.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TvLiveRed.copy(alpha = 0.15f))
                                .border(1.dp, TvLiveRed, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = TvLiveRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = importStatus.error,
                                color = TvTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    is ImportStatus.Success -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TvSuccessGreen.copy(alpha = 0.2f))
                                .border(1.dp, TvSuccessGreen, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (importStatus.isExisting) {
                                    "Switched to '${importStatus.playlist.name}' (${importStatus.count} channels updated)!"
                                } else {
                                    "Successfully imported '${importStatus.playlist.name}' with ${importStatus.count} channels!"
                                },
                                color = TvSuccessGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            TvFocusableButton(
                                text = "Done",
                                onClick = onDismiss,
                                isPrimary = true,
                                tag = "btn_import_done"
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    else -> Unit
                }

                if (localValidationError != null && importStatus !is ImportStatus.Error) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TvLiveRed.copy(alpha = 0.15f))
                            .border(1.dp, TvLiveRed, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = TvLiveRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localValidationError!!,
                            color = TvTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Playlist Name (optional)
                OutlinedTextField(
                    value = playlistNameText,
                    onValueChange = { playlistNameText = it },
                    label = { Text("Playlist Name (Optional)") },
                    placeholder = { Text("e.g. Tamil Sports, World News") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_playlist_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TvTextPrimary,
                        unfocusedTextColor = TvTextPrimary,
                        focusedBorderColor = TvPrimary,
                        unfocusedBorderColor = TvSurfaceVariant,
                        focusedLabelColor = TvPrimary,
                        unfocusedLabelColor = TvTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Playlist URL input
                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        localValidationError = null
                    },
                    label = { Text("M3U / M3U8 Playlist URL (HTTP or HTTPS)") },
                    placeholder = { Text("https://example.com/stream/playlist.m3u") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_playlist_url"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TvTextPrimary,
                        unfocusedTextColor = TvTextPrimary,
                        focusedBorderColor = TvPrimary,
                        unfocusedBorderColor = TvSurfaceVariant,
                        focusedLabelColor = TvPrimary,
                        unfocusedLabelColor = TvTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions row with responsive wrapping
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TvFocusableButton(
                        text = "Save & Import",
                        icon = Icons.Default.CloudDownload,
                        onClick = {
                            val validation = PlaylistValidator.validateUrl(urlText)
                            if (validation is ValidationResult.Invalid) {
                                localValidationError = validation.reason
                            } else {
                                localValidationError = null
                                onImportUrl(urlText, playlistNameText.takeIf { it.isNotBlank() })
                            }
                        },
                        isPrimary = true,
                        tag = "btn_import_url"
                    )

                    TvFocusableButton(
                        text = "Open Local File",
                        icon = Icons.Default.FolderOpen,
                        onClick = {
                            filePickerLauncher.launch(arrayOf("*/*", "application/x-mpegURL", "audio/x-mpegurl"))
                        },
                        tag = "btn_open_file"
                    )

                    TvFocusableButton(
                        text = "Load Demo",
                        icon = Icons.Default.PlayCircleOutline,
                        onClick = onLoadDemo,
                        tag = "btn_load_demo"
                    )

                    TvFocusableButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        tag = "btn_cancel_dialog"
                    )
                }
            }
        }
    }
}
