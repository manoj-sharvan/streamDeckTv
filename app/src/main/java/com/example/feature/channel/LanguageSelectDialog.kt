package com.example.feature.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.parser.LanguageClassifier
import com.example.domain.model.Channel
import com.example.ui.components.TvFocusableButton
import com.example.ui.theme.TvAccent
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceCard
import com.example.ui.theme.TvSurfaceFocused
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageSelectDialog(
    channel: Channel,
    onSelectLanguage: (String?) -> Unit, // null means reset to auto-detected
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, TvPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .testTag("language_select_dialog"),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Edit Channel Language",
                    color = TvTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Channel: ${channel.name}",
                    color = TvAccent,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (channel.userLanguageOverride != null) {
                        "Currently manually set to: ${channel.userLanguageOverride}"
                    } else {
                        "Auto-detected as: ${channel.language} (${if (channel.isLanguageVerified) "Verified" else "Default/Inferred"})"
                    },
                    color = TvTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Language Grid / Flow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageClassifier.KNOWN_LANGUAGES.forEach { lang ->
                        val isSelected = channel.effectiveLanguage.equals(lang, ignoreCase = true)
                        LanguageOptionChip(
                            language = lang,
                            isSelected = isSelected,
                            onClick = {
                                onSelectLanguage(lang)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Buttons (Reset to Auto & Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableButton(
                        text = "Reset to Auto",
                        icon = Icons.Default.Refresh,
                        onClick = {
                            onSelectLanguage(null)
                        },
                        tag = "btn_reset_language"
                    )

                    TvFocusableButton(
                        text = "Cancel",
                        icon = Icons.Default.Close,
                        onClick = onDismiss,
                        tag = "btn_cancel_language"
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionChip(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val backgroundColor = when {
        isSelected -> TvPrimary
        isFocused -> TvSurfaceFocused
        else -> TvSurfaceCard
    }

    val contentColor = when {
        isSelected -> TvBackground
        isFocused -> TvPrimary
        else -> TvTextPrimary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) TvPrimary else TvSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("lang_chip_$language"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.height(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = language,
                color = contentColor,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
