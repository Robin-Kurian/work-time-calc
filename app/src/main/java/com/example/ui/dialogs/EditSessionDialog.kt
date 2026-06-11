package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Session
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.utils.TimeUtils

@Composable
fun EditSessionDialog(
    session: Session,
    onDismiss: () -> Unit,
    onSave: (Session) -> Unit
) {
    var tempInTime by remember { mutableLongStateOf(session.inTime) }
    var tempOutTime by remember { mutableStateOf(session.outTime) }
    var showInPicker by remember { mutableStateOf(false) }
    var showOutPicker by remember { mutableStateOf(false) }

    val hasError = tempOutTime != null && tempOutTime!! < tempInTime

    if (showInPicker) {
        ComposeTimestampPickerDialog(
            timestamp = tempInTime,
            baseDateTimestamp = session.inTime,
            title = "In Time",
            onDismiss = { showInPicker = false },
            onTimeSelected = { tempInTime = it }
        )
    }

    if (showOutPicker) {
        ComposeTimestampPickerDialog(
            timestamp = tempOutTime,
            baseDateTimestamp = tempInTime,
            title = "Out Time",
            onDismiss = { showOutPicker = false },
            onTimeSelected = { tempOutTime = it }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .glassSurface(shape = RoundedCornerShape(24.dp), fillAlpha = 0.82f, elevation = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Edit Session",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "IN TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.mutedText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceOverlay, RoundedCornerShape(12.dp))
                        .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                        .clickable { showInPicker = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TimeUtils.fmtTimestamp(tempInTime),
                            fontSize = 16.sp,
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit In Time",
                            tint = AppTheme.colors.accentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OUT TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.mutedText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tempOutTime == null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(AppTheme.colors.accentGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, AppTheme.colors.accentGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Ongoing",
                                fontSize = 14.sp,
                                color = AppTheme.colors.accentGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Button(
                            onClick = { showOutPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Set Out", fontSize = 12.sp, color = AppTheme.colors.textOnAccent)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(AppTheme.colors.surfaceOverlay, RoundedCornerShape(12.dp))
                                .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable { showOutPicker = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = TimeUtils.fmtTimestamp(tempOutTime!!),
                                    fontSize = 16.sp,
                                    color = AppTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Out Time",
                                    tint = AppTheme.colors.accentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { tempOutTime = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.lightRed.copy(alpha = 0.5f)),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Ongoing", fontSize = 12.sp, color = AppTheme.colors.lightRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val durationSec = if (tempOutTime == null) {
                    (System.currentTimeMillis() - tempInTime) / 1000
                } else {
                    (tempOutTime!! - tempInTime) / 1000
                }

                if (hasError) {
                    Text(
                        text = "Out time cannot be before In time",
                        color = AppTheme.colors.lightRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    val durationText = if (tempOutTime == null) {
                        "Duration: Ongoing (${TimeUtils.fmtDurSeconds(durationSec)})"
                    } else {
                        "Duration: ${TimeUtils.fmtDurSeconds(durationSec)}"
                    }
                    Text(
                        text = durationText,
                        color = if (tempOutTime == null) AppTheme.colors.accentGreen else AppTheme.colors.mutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(text = "Cancel", color = AppTheme.colors.mutedText)
                    }

                    Button(
                        onClick = {
                            if (!hasError) {
                                onSave(session.copy(inTime = tempInTime, outTime = tempOutTime))
                            }
                        },
                        enabled = !hasError,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.accentGreen,
                            disabledContainerColor = AppTheme.colors.accentGreen.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = if (hasError) AppTheme.colors.textOnAccent.copy(alpha = 0.5f) else AppTheme.colors.textOnAccent
                        )
                    }
                }
            }
        }
    }
}
