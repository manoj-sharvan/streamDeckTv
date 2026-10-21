package com.example.feature.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.TvFocusableButton
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvPrimary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDestructive: Boolean = false,
    icon: ImageVector = Icons.Default.Warning,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = if (isDestructive) TvLiveRed.copy(alpha = 0.8f) else TvPrimary.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
                .testTag("confirm_action_dialog"),
            color = TvSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDestructive) TvLiveRed else TvPrimary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = title,
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = TvTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableButton(
                        text = cancelText,
                        icon = Icons.Default.Close,
                        onClick = onDismiss,
                        tag = "btn_dialog_cancel"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    TvFocusableButton(
                        text = confirmText,
                        icon = Icons.Default.Check,
                        onClick = onConfirm,
                        isPrimary = true,
                        tag = "btn_dialog_confirm"
                    )
                }
            }
        }
    }
}
