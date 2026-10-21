package com.example.feature.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.Composable

@Composable
fun ExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmActionDialog(
        title = "Exit StreamDeck TV?",
        message = "Are you sure you want to exit the application?",
        confirmText = "Exit App",
        cancelText = "Stay",
        isDestructive = true,
        icon = Icons.AutoMirrored.Filled.ExitToApp,
        onConfirm = onConfirmExit,
        onDismiss = onDismiss
    )
}
