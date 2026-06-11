package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.utils.TimeUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeTimePickerDialog(
    currentTime: String,
    title: String = "Select Time",
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val parts = currentTime.split(":")
    val calendar = Calendar.getInstance()
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(shape = RoundedCornerShape(24.dp), fillAlpha = 0.82f, elevation = 12.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            TimePicker(state = timePickerState)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = AppTheme.colors.mutedText)
                }
                Button(
                    onClick = {
                        val selected = String.format(
                            Locale.US,
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onTimeSelected(selected)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentGreen)
                ) {
                    Text("OK", color = AppTheme.colors.textOnAccent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeTimestampPickerDialog(
    timestamp: Long?,
    baseDateTimestamp: Long,
    title: String = "Select Time",
    onDismiss: () -> Unit,
    onTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp ?: System.currentTimeMillis()
    }

    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(shape = RoundedCornerShape(24.dp), fillAlpha = 0.82f, elevation = 12.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (timestamp != null) {
                Text(
                    text = TimeUtils.fmtTimestamp(timestamp),
                    fontSize = 12.sp,
                    color = AppTheme.colors.mutedText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            TimePicker(state = timePickerState)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = AppTheme.colors.mutedText)
                }
                Button(
                    onClick = {
                        val base = timestamp ?: baseDateTimestamp
                        val updated = TimeUtils.updateTimeOfTimestamp(
                            base,
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onTimeSelected(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentGreen)
                ) {
                    Text("OK", color = AppTheme.colors.textOnAccent)
                }
            }
        }
    }
}
