package com.example.ui.dialogs

import com.example.ui.theme.AppTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ClearDayConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Clear Daily Data?", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Are you sure you want to clear all logged sessions for today?",
                color = AppTheme.colors.mutedText
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentGreen)
            ) {
                Text("Clear", color = AppTheme.colors.textOnAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppTheme.colors.mutedText)
            }
        },
        containerColor = AppTheme.colors.glassFillStrong
    )
}

@Composable
fun DeleteSessionConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete Session?", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Are you sure you want to remove this session entry?",
                color = AppTheme.colors.mutedText
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.lightRed)
            ) {
                Text("Delete", color = AppTheme.colors.textOnAccent, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppTheme.colors.mutedText)
            }
        },
        containerColor = AppTheme.colors.glassFillStrong
    )
}
