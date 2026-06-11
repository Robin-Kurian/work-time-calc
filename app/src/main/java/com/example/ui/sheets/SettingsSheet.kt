package com.example.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PauseCircleFilled
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dialogs.DurationPickerDialog
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.WorkViewModel
import com.example.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: WorkViewModel,
    targetMinutes: Int,
    isAutoTrackingEnabled: Boolean,
    isSoundMuted: Boolean,
    workSsid: String,
    currentSsidValue: String?,
    lastCheckedTime: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showDurationPicker by remember { mutableStateOf(false) }
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    if (showDurationPicker) {
        DurationPickerDialog(
            currentMinutes = targetMinutes,
            title = "Work Target",
            onDismiss = { showDurationPicker = false },
            onDurationSelected = { viewModel.updateTargetWorkMinutes(it) }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.sheetSurface,
        scrimColor = AppTheme.colors.sheetScrim
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SCHEDULE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.hintText,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDurationPicker = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Work target", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                    Text(
                        text = TimeUtils.fmtDur(targetMinutes),
                        fontSize = 12.sp,
                        color = AppTheme.colors.mutedText
                    )
                }
                Icon(Icons.Outlined.Edit, "Edit target", tint = AppTheme.colors.accentGreen)
            }

            HorizontalDivider(color = AppTheme.colors.dividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AUTOMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.hintText,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAutoTrackingEnabled) Icons.Outlined.PauseCircleFilled
                        else Icons.Outlined.PlayCircleFilled,
                        contentDescription = null,
                        tint = if (isAutoTrackingEnabled) AppTheme.colors.lightRed else AppTheme.colors.accentGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Auto-tracking", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                        Text(
                            text = if (isAutoTrackingEnabled) "WiFi-based punch enabled" else "Manual punch only",
                            fontSize = 12.sp,
                            color = AppTheme.colors.mutedText
                        )
                    }
                }
                Switch(
                    checked = isAutoTrackingEnabled,
                    onCheckedChange = { viewModel.toggleAutoTracking() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.accentGreen,
                        checkedTrackColor = AppTheme.colors.accentGreen.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            WifiSetupSection(
                workSsid = workSsid,
                currentSsidValue = currentSsidValue,
                isAutoTrackingEnabled = isAutoTrackingEnabled,
                lastCheckedTime = lastCheckedTime,
                viewModel = viewModel,
                requestPermissionsOnMount = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = AppTheme.colors.dividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "APPEARANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.hintText,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Outlined.DarkMode
                        else Icons.Outlined.LightMode,
                        contentDescription = null,
                        tint = AppTheme.colors.accentGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Dark mode", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                        Text(
                            text = if (isDarkTheme) {
                                "Frosted glass on deep slate"
                            } else {
                                "Light glassmorphism theme"
                            },
                            fontSize = 12.sp,
                            color = AppTheme.colors.mutedText
                        )
                    }
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.accentGreen,
                        checkedTrackColor = AppTheme.colors.accentGreen.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = AppTheme.colors.dividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SOUND",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.hintText,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSoundMuted) Icons.Outlined.VolumeOff
                        else Icons.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = if (isSoundMuted) AppTheme.colors.mutedText else AppTheme.colors.accentGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Mute sound", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                        Text(
                            text = if (isSoundMuted) {
                                "Work target and timer alarms are silenced"
                            } else {
                                "Work target and timer alarms will ring"
                            },
                            fontSize = 12.sp,
                            color = AppTheme.colors.mutedText
                        )
                    }
                }
                Switch(
                    checked = isSoundMuted,
                    onCheckedChange = { viewModel.setSoundMuted(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.mutedText,
                        checkedTrackColor = AppTheme.colors.mutedText.copy(alpha = 0.4f),
                        uncheckedThumbColor = AppTheme.colors.accentGreen,
                        uncheckedTrackColor = AppTheme.colors.accentGreen.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}
