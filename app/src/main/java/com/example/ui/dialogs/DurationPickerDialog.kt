package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.utils.TimeUtils

@Composable
fun DurationPickerDialog(
    currentMinutes: Int,
    title: String = "Work Target",
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(currentMinutes / 60) }
    var minutes by remember { mutableIntStateOf(currentMinutes % 60) }

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
                color = AppTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DurationStepper(
                    label = "Hours",
                    value = hours,
                    onDecrement = { if (hours > 0) hours-- },
                    onIncrement = { if (hours < 12) hours++ }
                )
                DurationStepper(
                    label = "Minutes",
                    value = minutes,
                    onDecrement = { minutes = if (minutes >= 15) minutes - 15 else if (hours > 0) { hours--; 45 } else 0 },
                    onIncrement = { minutes = if (minutes < 45) minutes + 15 else if (hours < 12) { hours++; 0 } else 45 }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = TimeUtils.fmtDur(hours * 60 + minutes),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.accentGreen
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = AppTheme.colors.mutedText)
                }
                Button(
                    onClick = {
                        val total = hours * 60 + minutes
                        if (total > 0) {
                            onDurationSelected(total)
                        }
                        onDismiss()
                    },
                    enabled = hours * 60 + minutes > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentGreen)
                ) {
                    Text("OK", color = AppTheme.colors.textOnAccent)
                }
            }
        }
    }
}

@Composable
private fun DurationStepper(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = AppTheme.colors.mutedText, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(40.dp)
                    .glassSurface(shape = CircleShape, fillAlpha = 0.6f, elevation = 2.dp)
            ) {
                Text("-", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
            }
            Text(
                text = String.format("%02d", value),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(40.dp)
                    .glassSurface(shape = CircleShape, fillAlpha = 0.6f, elevation = 2.dp)
            ) {
                Text("+", color = AppTheme.colors.accentGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}
