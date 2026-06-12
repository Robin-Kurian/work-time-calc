package com.example.ui.screens.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.NavigationTab
import com.example.ui.components.AlarmToggleButton
import com.example.ui.components.LeaveAtChip
import com.example.ui.components.PunchActionBar
import com.example.ui.components.SessionBreakDivider
import com.example.ui.components.SessionLogItem
import com.example.ui.components.StatusPill
import com.example.ui.components.WorkProgressRing
import com.example.ui.sheets.SessionLogSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.sheets.WifiConnectedSlimBanner
import com.example.ui.sheets.WifiDisconnectedBanner
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.ui.viewmodel.WorkViewModel
import com.example.utils.PermissionUtils
import com.example.utils.TimeUtils
import com.example.utils.WifiConnectionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: WorkViewModel,
    onNavigateToPlan: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessions by viewModel.sessions.collectAsState()
    val workSsid by viewModel.workSsid.collectAsState()
    val currentSsidValue by viewModel.currentSsid.collectAsState()
    val lastCheckedTime by viewModel.lastCheckedTime.collectAsState()
    val liveActiveSeconds by viewModel.liveActiveElapsedSeconds.collectAsState()
    val isAutoTrackingEnabled by viewModel.isAutoTrackingEnabled.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isAlarmEnabled by viewModel.isAlarmEnabled.collectAsState()
    val targetMinutes by viewModel.targetWorkMinutes.collectAsState()
    val clockTick by viewModel.clockTick.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showSessionLog by remember { mutableStateOf(false) }

    val progress = viewModel.getWorkProgress(sessions, targetMinutes, clockTick)
    val dynamicLeaveMin = viewModel.getDynamicLeaveMin(sessions, targetMinutes, clockTick)
    val workedMs = progress.workedMs
    val pct = progress.percent
    val isDone = progress.isDone
    val isIn = sessions.any { it.outTime == null }
    val hasLocationPermission = PermissionUtils.hasLocationPermission(context)
    val isWorkWifiConnected =
        workSsid.isNotEmpty() && WifiConnectionHelper.isWorkNetworkMatch(currentSsidValue, workSsid)

    val dateSubtitle = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
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

    if (showSessionLog) {
        SessionLogSheet(
            viewModel = viewModel,
            sessions = sessions,
            targetMinutes = targetMinutes,
            liveActiveSeconds = liveActiveSeconds,
            onDismiss = { showSessionLog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = NavigationTab.Today.label,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = dateSubtitle,
                    fontSize = 13.sp,
                    color = AppTheme.colors.mutedText
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlarmToggleButton(
                    isAlarmEnabled = isAlarmEnabled,
                    onToggle = { viewModel.toggleAlarmEnabled() }
                )
                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = AppTheme.colors.mutedText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSettings = true }
                .glassSurface(shape = RoundedCornerShape(24.dp), fillAlpha = 0.72f, elevation = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            border = BorderStroke(0.dp, androidx.compose.ui.graphics.Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WorkProgressRing(
                    percent = pct,
                    workedMs = workedMs,
                    targetMinutes = targetMinutes,
                    isDone = isDone,
                    ringSize = 140.dp
                )
                if (!isDone) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Remaining: ${TimeUtils.fmtDurSeconds(progress.remainingMs / 1000)}",
                        fontSize = 12.sp,
                        color = com.example.ui.theme.AppTheme.colors.accentGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isDone) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(shape = RoundedCornerShape(16.dp), fillAlpha = 0.6f, elevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.amberBg),
                border = BorderStroke(1.dp, AppTheme.colors.amberBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "You can leave now!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.lightAmber
                    )
                    Text(
                        text = "You've worked ${TimeUtils.fmtDurSeconds(workedMs / 1000)} today",
                        fontSize = 12.sp,
                        color = AppTheme.colors.lightAmber.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PunchActionBar(
            isIn = isIn,
            isLocked = isLocked,
            onPunch = { viewModel.punch() },
            onToggleLock = { viewModel.toggleLock() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        StatusPill(
            sessions = sessions,
            isIn = isIn,
            liveActiveSeconds = liveActiveSeconds,
            isLocked = isLocked
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAutoTrackingEnabled && hasLocationPermission) {
                if (!isWorkWifiConnected) {
                    WifiDisconnectedBanner(
                        onTap = { showSettings = true },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else if (currentSsidValue != null) {
                    WifiConnectedSlimBanner(
                        ssid = currentSsidValue!!,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
            LeaveAtChip(
                leaveMin = dynamicLeaveMin,
                onClick = onNavigateToPlan,
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SESSION LOG",
                color = AppTheme.colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            if (sessions.isNotEmpty()) {
                Text(
                    text = "View all (${sessions.size})",
                    color = com.example.ui.theme.AppTheme.colors.accentGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showSessionLog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sessions.isEmpty()) {
            Text(
                text = "No sessions yet. Punch in to start.",
                color = AppTheme.colors.hintText,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            val orderedSessions = sessions.sortedByDescending { it.inTime }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val previewSessions = if (orderedSessions.size == 1) {
                    listOf(orderedSessions.first() to orderedSessions.size)
                } else {
                    val currentSession = orderedSessions.firstOrNull { it.outTime == null } ?: orderedSessions.first()
                    val currentSessionNumber = orderedSessions.size - orderedSessions.indexOf(currentSession)
                    val firstSession = orderedSessions.last()

                    listOf(
                        currentSession to currentSessionNumber,
                        firstSession to 1
                    ).distinctBy { it.first.id }
                }

                previewSessions.forEachIndexed { index, (session, sessionNumber) ->
                    val isOpen = session.outTime == null
                    val durationSec = if (isOpen) liveActiveSeconds
                    else (session.outTime!! - session.inTime) / 1000

                    SessionLogItem(
                        session = session,
                        sessionNumber = sessionNumber,
                        durationSec = durationSec,
                        isOpen = isOpen,
                        isFirstSession = sessionNumber == 1,
                        onEdit = { showSessionLog = true },
                        onDelete = { showSessionLog = true }
                    )

                    if (index == 0 && previewSessions.size > 1) {
                        val oldest = orderedSessions.last()
                        val totalOutsideSec = TimeUtils.totalOutsideSeconds(orderedSessions)
                        if (totalOutsideSec > 0 && oldest.outTime != null) {
                            SessionBreakDivider(
                                outsideSec = totalOutsideSec,
                                outTime = oldest.outTime!!,
                                aggregated = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
