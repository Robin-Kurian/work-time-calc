package com.example.ui.screens.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.NavigationTab
import com.example.ui.components.TimeInputCard
import com.example.ui.dialogs.ComposeTimePickerDialog
import com.example.ui.sheets.SettingsSheet
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.ui.viewmodel.WorkViewModel
import com.example.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    viewModel: WorkViewModel,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrTime by viewModel.arrivalTime.collectAsState()
    val loTime by viewModel.lunchOutTime.collectAsState()
    val liTime by viewModel.lunchInTime.collectAsState()
    val calcState by viewModel.manualCalculationState.collectAsState()
    val targetMinutes by viewModel.targetWorkMinutes.collectAsState()
    val workSsid by viewModel.workSsid.collectAsState()
    val currentSsidValue by viewModel.currentSsid.collectAsState()
    val lastCheckedTime by viewModel.lastCheckedTime.collectAsState()
    val isAutoTrackingEnabled by viewModel.isAutoTrackingEnabled.collectAsState()
    val isAlarmEnabled by viewModel.isAlarmEnabled.collectAsState()

    var showArrivalPicker by remember { mutableStateOf(false) }
    var showLunchOutPicker by remember { mutableStateOf(false) }
    var showLunchInPicker by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    if (showArrivalPicker) {
        ComposeTimePickerDialog(
            currentTime = arrTime,
            title = "Arrival Time",
            onDismiss = { showArrivalPicker = false },
            onTimeSelected = { viewModel.updateArrivalTime(it) }
        )
    }
    if (showLunchOutPicker) {
        ComposeTimePickerDialog(
            currentTime = loTime,
            title = "Lunch Out",
            onDismiss = { showLunchOutPicker = false },
            onTimeSelected = { viewModel.updateLunchOutTime(it) }
        )
    }
    if (showLunchInPicker) {
        ComposeTimePickerDialog(
            currentTime = liTime,
            title = "Lunch In",
            onDismiss = { showLunchInPicker = false },
            onTimeSelected = { viewModel.updateLunchInTime(it) }
        )
    }

    if (showSettings) {
        SettingsSheet(
            viewModel = viewModel,
            targetMinutes = targetMinutes,
            isAutoTrackingEnabled = isAutoTrackingEnabled,
            isSoundMuted = !isAlarmEnabled,
            workSsid = workSsid,
            currentSsidValue = currentSsidValue,
            lastCheckedTime = lastCheckedTime,
            onRequestPermissions = onRequestPermissions,
            onDismiss = { showSettings = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = NavigationTab.Plan.label,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Schedule & leave time",
                fontSize = 13.sp,
                color = AppTheme.colors.mutedText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(shape = RoundedCornerShape(24.dp), fillAlpha = 0.68f, elevation = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.redBg),
            border = BorderStroke(1.dp, AppTheme.colors.redBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Leave at",
                        tint = AppTheme.colors.lightRed,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Leave at",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.lightRed
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = TimeUtils.toTime(calcState.leaveMin),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.lightRed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = AppTheme.colors.hintText,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "SCHEDULE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.hintText,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        TimeInputCard(
            label = "Arrival",
            time = arrTime,
            icon = Icons.Outlined.WbSunny,
            onClick = { showArrivalPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            testTag = "arrival_card"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TimeInputCard(
                label = "Lunch Out",
                time = loTime,
                icon = Icons.Outlined.Restaurant,
                onClick = { showLunchOutPicker = true },
                modifier = Modifier.weight(1f),
                testTag = "lunch_out_card"
            )
            TimeInputCard(
                label = "Lunch In",
                time = liTime,
                icon = Icons.Outlined.LunchDining,
                onClick = { showLunchInPicker = true },
                modifier = Modifier.weight(1f),
                testTag = "lunch_in_card"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DayTimelineBar(calcState = calcState)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSettings = true }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Required work",
                fontSize = 14.sp,
                color = AppTheme.colors.mutedText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = TimeUtils.fmtDur(targetMinutes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )
        }
    }
}
