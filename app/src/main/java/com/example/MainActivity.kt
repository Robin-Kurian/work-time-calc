package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PauseCircleFilled
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.viewmodel.PomodoroMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.Session
import com.example.data.Task
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AmberBg
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBg
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.HintText
import com.example.ui.theme.LightAmber
import com.example.ui.theme.LightRed
import com.example.ui.theme.MutedText
import com.example.ui.theme.RedBg
import com.example.ui.theme.RedBorder
import com.example.ui.theme.TealGreen
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WorkTimeCalcTheme
import com.example.ui.viewmodel.WorkViewModel
import com.example.utils.TimeUtils
import com.example.utils.TimeUtils.BUFFER_IN
import com.example.utils.TimeUtils.BUFFER_OUT
import com.example.utils.TimeUtils.REQUIRED_MINUTES
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: WorkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkTimeCalcTheme {
                MainContainer(viewModel)
            }
        }
    }
}

enum class NavigationTab {
    AutoTracking,
    ManualTracking,
    Focus
}

@Composable
fun MainContainer(viewModel: WorkViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(NavigationTab.AutoTracking) }
    val context = LocalContext.current
    val activeAlarm by viewModel.activeAlarm.collectAsState()

    // Permissions check
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.checkWifiConnectionInstant()
    }

    LaunchedEffect(Unit) {
        val permissionList = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val needsLaunch = permissionList.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needsLaunch) {
            permissionLauncher.launch(permissionList.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgDark,
        bottomBar = {
            CustomBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(250),
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    NavigationTab.AutoTracking -> SessionScreen(viewModel)
                    NavigationTab.ManualTracking -> ManualScreen(viewModel)
                    NavigationTab.Focus -> FocusScreen(viewModel)
                }
            }
        }
    }

    activeAlarm?.let { alarm ->
        Dialog(onDismissRequest = { /* Force stop button usage */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        1.5.dp,
                        if (alarm.type == com.example.ui.viewmodel.AlarmType.WORK_TARGET) AccentGreen.copy(alpha = 0.5f) else LightAmber.copy(alpha = 0.5f),
                        RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1512))
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "alarm_pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                            .background(
                                color = if (alarm.type == com.example.ui.viewmodel.AlarmType.WORK_TARGET) AccentGreen.copy(alpha = 0.15f) else LightAmber.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "Alarm Icon",
                            tint = if (alarm.type == com.example.ui.viewmodel.AlarmType.WORK_TARGET) AccentGreen else LightAmber,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = alarm.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = alarm.message,
                        fontSize = 14.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.stopAlarm() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (alarm.type == com.example.ui.viewmodel.AlarmType.WORK_TARGET) AccentGreen else LightAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Text(
                            text = "STOP ALARM",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

// Custom Bottom Navigation Bar
@Composable
fun CustomBottomNavigation(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        color = if (isLandscape) Color.Transparent else Color(0xFF0A0F0D),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(if (isLandscape) 48.dp else 64.dp)
            .let {
                if (isLandscape) it else it.border(width = 1.dp, color = Color(0x1AFFFFFF))
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = if (isLandscape) Alignment.Bottom else Alignment.CenterVertically
        ) {
            // Auto Tracking Navigation Button
            val isAutoSelected = selectedTab == NavigationTab.AutoTracking
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(NavigationTab.AutoTracking) }
                    .padding(vertical = if (isLandscape) 2.dp else 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isLandscape) Arrangement.Bottom else Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = "Auto tracking tab icon",
                    tint = if (isAutoSelected) AccentGreen else MutedText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(if (isLandscape) 1.dp else 3.dp))
                Text(
                    text = "Auto Tracking",
                    color = if (isAutoSelected) AccentGreen else MutedText,
                    fontSize = 10.sp,
                    fontWeight = if (isAutoSelected) FontWeight.Bold else FontWeight.Medium
                )
            }

            // Manual Tracking Navigation Button
            val isManualSelected = selectedTab == NavigationTab.ManualTracking
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(NavigationTab.ManualTracking) }
                    .padding(vertical = if (isLandscape) 2.dp else 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isLandscape) Arrangement.Bottom else Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Keyboard,
                    contentDescription = "Manual tracking tab icon",
                    tint = if (isManualSelected) AccentGreen else MutedText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(if (isLandscape) 1.dp else 3.dp))
                Text(
                    text = "Manual Tracking",
                    color = if (isManualSelected) AccentGreen else MutedText,
                    fontSize = 10.sp,
                    fontWeight = if (isManualSelected) FontWeight.Bold else FontWeight.Medium
                )
            }

            // Focus Navigation Button
            val isFocusSelected = selectedTab == NavigationTab.Focus
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(NavigationTab.Focus) }
                    .padding(vertical = if (isLandscape) 2.dp else 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isLandscape) Arrangement.Bottom else Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = "Focus tab icon",
                    tint = if (isFocusSelected) AccentGreen else MutedText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(if (isLandscape) 1.dp else 3.dp))
                Text(
                    text = "Focus",
                    color = if (isFocusSelected) AccentGreen else MutedText,
                    fontSize = 10.sp,
                    fontWeight = if (isFocusSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ---------------- MANUAL SCREEN ----------------
@Composable
fun ManualScreen(viewModel: WorkViewModel) {
    val arrTime by viewModel.arrivalTime.collectAsState()
    val loTime by viewModel.lunchOutTime.collectAsState()
    val liTime by viewModel.lunchInTime.collectAsState()
    val calcState by viewModel.manualCalculationState.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // --- HEADER SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "W.T Calc (Manual)",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Log your entries manually",
                    fontSize = 13.sp,
                    color = MutedText
                )
            }
        }

        // 4px Green Line Progress Bar
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(AccentGreen, CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- TIME INPUT CARDS ---
        Text(
            text = "🕒 SET RECENT TIMES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = HintText,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Arrival (Full-width Input Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("arrival_card")
                .clickable {
                    showTimePicker(context, arrTime) { selected ->
                        viewModel.updateArrivalTime(selected)
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(text = "🌅 Arrival", fontSize = 12.sp, color = MutedText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TimeUtils.toTime(TimeUtils.toMin(arrTime)),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }

        // Lunch Out and Lunch In Side-by-Side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("lunch_out_card")
                    .clickable {
                        showTimePicker(context, loTime) { selected ->
                            viewModel.updateLunchOutTime(selected)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(text = "🥪 Lunch Out", fontSize = 12.sp, color = MutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TimeUtils.toTime(TimeUtils.toMin(loTime)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("lunch_in_card")
                    .clickable {
                        showTimePicker(context, liTime) { selected ->
                            viewModel.updateLunchInTime(selected)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(text = "🍱 Lunch In", fontSize = 12.sp, color = MutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TimeUtils.toTime(TimeUtils.toMin(liTime)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- VISUAL SUMMARY BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Arrival: ${TimeUtils.toTime(calcState.arrivalMin)}",
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Leave: ${TimeUtils.toTime(calcState.leaveMin)}",
                color = AccentGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Segmented Visual Bar
        val minSegmentMin = 1f
        val wBufferIn = Math.max(minSegmentMin, BUFFER_IN.toFloat())
        val wAmWork = Math.max(minSegmentMin, calcState.amWork.toFloat())
        val wLunch = Math.max(minSegmentMin, calcState.lunchDur.toFloat())
        val wPmWork = Math.max(minSegmentMin, calcState.pmWork.toFloat())
        val wBufferOut = Math.max(minSegmentMin, BUFFER_OUT.toFloat())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Transparent)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Buffer In
                Box(
                    modifier = Modifier
                        .weight(wBufferIn)
                        .fillMaxHeight()
                        .background(Color(0x23FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (calcState.amWork > 20) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BUF", fontSize = 8.sp, color = HintText, fontWeight = FontWeight.Bold)
                            Text("5m", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // AM Work
                Box(
                    modifier = Modifier
                        .weight(wAmWork)
                        .fillMaxHeight()
                        .background(DarkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    if (calcState.amWork > 30) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AM", fontSize = 8.sp, color = Color(0xB2FFFFFF), fontWeight = FontWeight.Bold)
                            Text(TimeUtils.fmtDur(calcState.amWork), fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Lunch
                Box(
                    modifier = Modifier
                        .weight(wLunch)
                        .fillMaxHeight()
                        .background(Color(0x60000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍔", fontSize = 14.sp)
                        if (calcState.lunchDur > 25) {
                            Text(
                                text = TimeUtils.fmtDur(calcState.lunchDur),
                                fontSize = 9.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // PM Work
                Box(
                    modifier = Modifier
                        .weight(wPmWork)
                        .fillMaxHeight()
                        .background(TealGreen),
                    contentAlignment = Alignment.Center
                ) {
                    if (calcState.pmWork > 30) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PM", fontSize = 8.sp, color = Color(0xB2FFFFFF), fontWeight = FontWeight.Bold)
                            Text(TimeUtils.fmtDur(calcState.pmWork), fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Buffer Out
                Box(
                    modifier = Modifier
                        .weight(wBufferOut)
                        .fillMaxHeight()
                        .background(Color(0x23FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (calcState.pmWork > 20) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BUF", fontSize = 8.sp, color = HintText, fontWeight = FontWeight.Bold)
                            Text("5m", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- STATS ROW (Required vs Leave) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Required work card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Required work",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "7h 50m",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            // Leave at card (red-tinted)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RedBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Leave at",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TimeUtils.toTime(calcState.leaveMin),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- FOOTER LINE ---
        val formattedArrLocal = TimeUtils.toTime(calcState.arrivalMin)
        val formattedLeaveLocal = TimeUtils.toTime(calcState.leaveMin)
        Text(
            text = "Arrive $formattedArrLocal • Leave $formattedLeaveLocal",
            color = HintText,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}


// ---------------- SESSION SCREEN (Auto Tracking) ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(viewModel: WorkViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val workSsid by viewModel.workSsid.collectAsState()
    val currentSsidValue by viewModel.currentSsid.collectAsState()
    val lastCheckedTime by viewModel.lastCheckedTime.collectAsState()
    val liveActiveSeconds by viewModel.liveActiveElapsedSeconds.collectAsState()
    val isAutoTrackingEnabled by viewModel.isAutoTrackingEnabled.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()

    val context = LocalContext.current

    var showEditDialogForSession by remember { mutableStateOf<Session?>(null) }

    val workedMin = viewModel.calculateWorkedMinutes(sessions)
    val pct = Math.min(100, Math.round((workedMin.toFloat() / REQUIRED_MINUTES) * 100))
    val remaining = Math.max(0, REQUIRED_MINUTES - workedMin)
    val isDone = workedMin >= REQUIRED_MINUTES
    val isIn = sessions.isNotEmpty() && sessions.first().outTime == null
    val isWorkWifiConnected = currentSsidValue != null && currentSsidValue == workSsid && workSsid.isNotEmpty()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // --- HEADER SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "W.T Tracker (Auto)",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Auto-tracks via WiFi · Manual override",
                    fontSize = 13.sp,
                    color = MutedText
                )
            }
            
            IconButton(
                onClick = { viewModel.toggleAutoTracking() }
            ) {
                Icon(
                    imageVector = if (isAutoTrackingEnabled) Icons.Outlined.PauseCircleFilled else Icons.Outlined.PlayCircleFilled,
                    contentDescription = if (isAutoTrackingEnabled) "Stop auto-tracking" else "Start auto-tracking",
                    tint = if (isAutoTrackingEnabled) Color.Red else AccentGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Thin green progress bar filling proportionally to pct
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0x20FFFFFF), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct.toFloat() / 100f)
                    .fillMaxHeight()
                    .background(AccentGreen, CircleShape)
            )
        }

        if (!isWorkWifiConnected) {
            Spacer(modifier = Modifier.height(16.dp))
            WifiConnectionCard(
                workSsid = workSsid,
                currentSsidValue = currentSsidValue,
                isAutoTrackingEnabled = isAutoTrackingEnabled,
                lastCheckedTime = lastCheckedTime,
                viewModel = viewModel
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PROGRESS RING & SIDE STATS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Ring canvas
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0x10FFFFFF),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = if (isDone) LightAmber else AccentGreen,
                            startAngle = -90f,
                            sweepAngle = (pct.toFloat() / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$pct%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "done",
                            fontSize = 10.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Work side stats details
                Column {
                    Text(
                        text = "TODAY'S WORK TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText
                    )
                    Text(
                        text = TimeUtils.fmtDur(workedMin),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDone) "✓ Target reached!" else "Remaining: ${TimeUtils.fmtDur(remaining.toInt())}",
                        fontSize = 12.sp,
                        color = if (isDone) LightAmber else AccentGreen,
                        fontWeight = FontWeight.Bold
                    )

                    // Projected leave time calculation
                    val completedMs = if (isIn) {
                        sessions.drop(1).sumOf { it.outTime?.let { out -> out - it.inTime } ?: 0L }
                    } else {
                        sessions.sumOf { it.outTime?.let { out -> out - it.inTime } ?: 0L }
                    }
                    val targetOutTimeMs = if (isIn) {
                        sessions.first().inTime + (REQUIRED_MINUTES * 60 * 1000 - completedMs)
                    } else {
                        System.currentTimeMillis() + (REQUIRED_MINUTES * 60 * 1000 - completedMs)
                    }
                    val calTarget = Calendar.getInstance().apply { timeInMillis = targetOutTimeMs }
                    val calToday = Calendar.getInstance()
                    val isTomorrow = calTarget.get(Calendar.DAY_OF_YEAR) != calToday.get(Calendar.DAY_OF_YEAR) ||
                                     calTarget.get(Calendar.YEAR) != calToday.get(Calendar.YEAR)
                    val leaveStr = TimeUtils.fmtTimestamp(targetOutTimeMs) + (if (isTomorrow) " (Tomorrow)" else "")
                    val leaveLabel = if (isDone) "Target reached at" else "Leave by"
                    Text(
                        text = "$leaveLabel ~$leaveStr",
                        color = LightRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- TARGET REACHED BANNER ---
        if (isDone) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AmberBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FBBF24))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎉 You can leave now!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightAmber
                    )
                    Text(
                        text = "You've worked ${TimeUtils.fmtDur(workedMin)} today",
                        fontSize = 12.sp,
                        color = LightAmber.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MAIN PUNCH BUTTON & LOCK TOGGLE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val punchButtonColor = if (isIn) RedBg else AccentGreen
            val punchButtonBorderColor = if (isIn) RedBorder else Color.Transparent

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp) // Match height of lock button exactly
                    .clip(RoundedCornerShape(20.dp))
                    .background(punchButtonColor)
                    .border(2.dp, punchButtonBorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.punch() }
                    .testTag("punch_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isIn) LightRed else TextWhite, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isIn) "Punch Out" else "Punch In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIn) LightRed else TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Lock Toggle
            IconButton(
                onClick = { viewModel.toggleLock() },
                modifier = Modifier
                    .size(56.dp)
                    .background(if (isLocked) Color(0x33FBBF24) else Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                    .border(1.dp, if (isLocked) Color(0x66FBBF24) else Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                    contentDescription = "Toggle session lock",
                    tint = if (isLocked) LightAmber else TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- SUB PUNCH STATUS PILL ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isIn) AccentGreen else Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            if (isIn) {
                val startLocalTime = TimeUtils.fmtTimestamp(sessions.first().inTime)
                val lockStatus = if (isLocked) " · 🔒 Locked" else ""
                Text(
                    text = "Clocked in since $startLocalTime · ⏱ ${TimeUtils.fmtElapsed(liveActiveSeconds)}$lockStatus",
                    color = AccentGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                if (sessions.isNotEmpty()) {
                    val lastOutVal = sessions.first().outTime
                    val lastOutStr = if (lastOutVal != null) TimeUtils.fmtTimestamp(lastOutVal) else ""
                    Text(
                        text = "Clocked out at $lastOutStr",
                        color = MutedText,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "Not clocked in",
                        color = MutedText,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (isWorkWifiConnected) {
            Spacer(modifier = Modifier.height(16.dp))
            WifiConnectionCard(
                workSsid = workSsid,
                currentSsidValue = currentSsidValue,
                isAutoTrackingEnabled = isAutoTrackingEnabled,
                lastCheckedTime = lastCheckedTime,
                viewModel = viewModel
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SESSION LOG LIST ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 SESSION LOG",
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Clear Day",
                color = AccentGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    showClearConfirmation(context) {
                        viewModel.clearAllSessions()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sessions.isEmpty()) {
            Text(
                text = "No sessions yet. Punch in to start.",
                color = HintText,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sessions.forEachIndexed { index, session ->
                    val sessionNumber = sessions.size - index
                    val isOpen = session.outTime == null
                    val itemBg = if (isOpen) Color(0x1F10B981) else CardBg
                    val itemBorder = if (isOpen) Color(0x3310B981) else CardBorder

                    val durationMin = if (isOpen) {
                        (liveActiveSeconds / 60).toInt()
                    } else {
                        ((session.outTime!! - session.inTime) / 60000).toInt()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = itemBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, itemBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Left accent bar
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(40.dp)
                                            .background(if (isOpen) AccentGreen else Color(0x40FFFFFF), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Session $sessionNumber",
                                            fontSize = 12.sp,
                                            color = MutedText,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = TimeUtils.fmtDur(durationMin),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOpen) AccentGreen else TextWhite
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isOpen) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0x3310B981), RoundedCornerShape(100.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("ONGOING", fontSize = 9.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        showEditDialogForSession = session
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Edit session",
                                            tint = AccentGreen.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = {
                                        showDeleteConfirmation(context) {
                                            viewModel.deleteSession(session)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete session",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // In/Out Pills
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // IN Pill
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x1A10B981), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0x3310B981), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(AccentGreen, CircleShape))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "IN: ${TimeUtils.fmtTimestamp(session.inTime)}",
                                            fontSize = 11.sp,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // OUT Pill
                                Box(
                                    modifier = Modifier
                                        .background(if (isOpen) Color(0x1AFFFFFF) else Color(0x1AF87171), RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isOpen) Color(0x33FFFFFF) else Color(0x33F87171), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(if (isOpen) MutedText else Color.Red, CircleShape))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isOpen) "OUT: --" else "OUT: ${TimeUtils.fmtTimestamp(session.outTime!!)}",
                                            fontSize = 11.sp,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                        }
                    }

                    if (index < sessions.size - 1) {
                        val nextSession = sessions[index + 1]
                        if (nextSession.outTime != null) {
                            val outsideMin = ((session.inTime - nextSession.outTime) / 60000).toInt()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(1.dp)
                                            .background(Color(0x14FFFFFF))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "☕ Outside for ${TimeUtils.fmtDur(outsideMin)}",
                                            fontSize = 11.sp,
                                            color = MutedText,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(1.dp)
                                            .background(Color(0x14FFFFFF))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- FOOTER INFO ---
        val footerText = if (isIn) {
            val durStr = TimeUtils.fmtDur((liveActiveSeconds / 60).toInt())
            "Current session: $durStr · ${sessions.size} session(s) today"
        } else {
            "Punch in to start tracking."
        }
        Text(
            text = footerText,
            color = HintText,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        showEditDialogForSession?.let { session ->
            EditSessionDialog(
                session = session,
                onDismiss = { showEditDialogForSession = null },
                onSave = { updatedSession ->
                    viewModel.updateSession(updatedSession)
                    showEditDialogForSession = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiConnectionCard(
    workSsid: String,
    currentSsidValue: String?,
    isAutoTrackingEnabled: Boolean,
    lastCheckedTime: String,
    viewModel: WorkViewModel,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isEditingSsid by rememberSaveable { mutableStateOf(false) }
    var localSsidInput by remember(workSsid) { mutableStateOf(workSsid) }

    // Pulsing animation for dots
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_wifi")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_wifi"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        if (isEditingSsid) {
            // Editing view: Show a compact, clean, borderless text field with Save and Close buttons inline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = localSsidInput,
                    onValueChange = { localSsidInput = it },
                    textStyle = TextStyle(
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(AccentGreen),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.updateWorkSsid(localSsidInput)
                            isEditingSsid = false
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(Color(0x0FFFFFFF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (localSsidInput.isEmpty()) {
                                Text(
                                    text = "e.g. Office_WiFi",
                                    color = HintText,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.updateWorkSsid(localSsidInput)
                        isEditingSsid = false
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .background(AccentGreen, RoundedCornerShape(8.dp))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = "Save work SSID settings",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        localSsidInput = workSsid
                        isEditingSsid = false
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cancel editing",
                        tint = TextWhite.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Normal view: Minimal-height status line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isWorkWifiConnected = currentSsidValue != null && currentSsidValue == workSsid && workSsid.isNotEmpty()

                Column(modifier = Modifier.weight(1f)) {
                    val wifiText = if (!isAutoTrackingEnabled) {
                        "Auto-tracking Disabled"
                    } else if (isWorkWifiConnected) {
                        "Connected - $currentSsidValue"
                    } else if (currentSsidValue != null) {
                        "Not connected ($currentSsidValue)"
                    } else {
                        "Not connected"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing Dot aligned with first line text
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .alpha(if (isWorkWifiConnected && isAutoTrackingEnabled) pulseAlpha else 1.0f)
                                .background(
                                    color = if (!isAutoTrackingEnabled) Color.Gray else if (isWorkWifiConnected) AccentGreen else Color.Red,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = wifiText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isAutoTrackingEnabled) MutedText else if (isWorkWifiConnected) AccentGreen else Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last checked: ${lastCheckedTime.ifEmpty { "Never" }}",
                        fontSize = 10.sp,
                        color = MutedText,
                        modifier = Modifier.padding(start = 22.dp) // 10 dot + 12 spacer
                    )
                }

                // Action Buttons Row: Edit (pencil) to the left of Refresh/Connect
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pencil (Edit) Icon Button
                    IconButton(
                        onClick = { isEditingSsid = true },
                        modifier = Modifier
                            .background(Color(0x0DFFFFFF), CircleShape)
                            .border(1.dp, CardBorder, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit target SSID",
                            tint = AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Refresh / Connect Button
                    val canConnect = currentSsidValue != null && currentSsidValue != workSsid && !currentSsidValue.isNullOrEmpty()
                    Box(
                        modifier = Modifier
                            .background(if (canConnect) AccentGreen.copy(alpha = 0.1f) else Color(0x1BFFFFFF), RoundedCornerShape(100.dp))
                            .border(1.dp, if (canConnect) AccentGreen.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(100.dp))
                            .clickable { if (canConnect) viewModel.setConnectedAsWork() else viewModel.checkWifiConnectionInstant() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (canConnect) Icons.Outlined.Wifi else Icons.Outlined.Refresh,
                                contentDescription = if (canConnect) "Connect current WiFi" else "Refresh WiFi status",
                                tint = if (canConnect) AccentGreen else TextWhite,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (canConnect) "CONNECT" else "REFRESH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canConnect) AccentGreen else TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog helper for individual time selection
fun showTimePicker(context: Context, currentTime: String, onTimeSelected: (String) -> Unit) {
    val currentParts = currentTime.split(":")
    val calendar = Calendar.getInstance()
    val hour = currentParts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
    val minute = currentParts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute))
        },
        hour,
        minute,
        true
    ).show()
}

// Dialog helper for clearance confirmation
fun showClearConfirmation(context: Context, onConfirmed: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle("Clear Daily Data?")
        .setMessage("Are you sure you want to clear all logged sessions for today?")
        .setPositiveButton("Clear") { dialog, _ ->
            onConfirmed()
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

// Dialog helper for individual session deletion
fun showDeleteConfirmation(context: Context, onConfirmed: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle("Delete Session?")
        .setMessage("Are you sure you want to remove this session entry?")
        .setPositiveButton("Delete") { dialog, _ ->
            onConfirmed()
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun showTimePickerForTimestamp(
    context: Context,
    timestamp: Long?,
    baseDateTimestamp: Long,
    onTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp ?: System.currentTimeMillis()
    }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val base = timestamp ?: baseDateTimestamp
            val updated = TimeUtils.updateTimeOfTimestamp(base, selectedHour, selectedMinute)
            onTimeSelected(updated)
        },
        hour,
        minute,
        true
    ).show()
}

@Composable
fun EditSessionDialog(
    session: Session,
    onDismiss: () -> Unit,
    onSave: (Session) -> Unit
) {
    val context = LocalContext.current
    var tempInTime by remember { mutableStateOf(session.inTime) }
    var tempOutTime by remember { mutableStateOf(session.outTime) }

    val hasError = tempOutTime != null && tempOutTime!! < tempInTime

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111815)),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
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
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- IN TIME SECTION ---
                Text(
                    text = "IN TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            showTimePickerForTimestamp(context, tempInTime, session.inTime) { updated ->
                                tempInTime = updated
                            }
                        }
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
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit In Time",
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- OUT TIME SECTION ---
                Text(
                    text = "OUT TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tempOutTime == null) {
                        // Ongoing indicator
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(AccentGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Ongoing",
                                fontSize = 14.sp,
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Button to Set Out Time
                        Button(
                            onClick = {
                                showTimePickerForTimestamp(context, null, tempInTime) { updated ->
                                    tempOutTime = updated
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Set Out", fontSize = 12.sp, color = TextWhite)
                        }
                    } else {
                        // Clickable Out Time Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    showTimePickerForTimestamp(context, tempOutTime, tempInTime) { updated ->
                                        tempOutTime = updated
                                    }
                                }
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
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Out Time",
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Button to clear Out Time back to null (ongoing)
                        Button(
                            onClick = { tempOutTime = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LightRed.copy(alpha = 0.5f)),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Ongoing", fontSize = 12.sp, color = LightRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- LIVE DURATION / ERROR INFO ---
                val durationMin = if (tempOutTime == null) {
                    ((System.currentTimeMillis() - tempInTime) / 60000).toInt()
                } else {
                    ((tempOutTime!! - tempInTime) / 60000).toInt()
                }

                if (hasError) {
                    Text(
                        text = "⚠️ Out time cannot be before In time",
                        color = LightRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    val durationText = if (tempOutTime == null) {
                        "Duration: Ongoing (${TimeUtils.fmtDur(durationMin)})"
                    } else {
                        "Duration: ${TimeUtils.fmtDur(durationMin)}"
                    }
                    Text(
                        text = durationText,
                        color = if (tempOutTime == null) AccentGreen else TextWhite.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- ACTION BUTTONS ---
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
                        Text(text = "Cancel", color = MutedText)
                    }

                    Button(
                        onClick = {
                            if (!hasError) {
                                onSave(session.copy(inTime = tempInTime, outTime = tempOutTime))
                            }
                        },
                        enabled = !hasError,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            disabledContainerColor = AccentGreen.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Save", color = if (hasError) TextWhite.copy(alpha = 0.5f) else TextWhite)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(viewModel: WorkViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    val pomodoroMode by viewModel.pomodoroMode.collectAsState()
    val remainingSeconds by viewModel.pomodoroRemainingSeconds.collectAsState()
    val totalSeconds by viewModel.pomodoroTotalSeconds.collectAsState()
    val isRunning by viewModel.pomodoroIsRunning.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Formatted minutes and seconds
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeStr = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    val displayMinutes = if (seconds > 0) minutes + 1 else minutes
    val dynamicLabel = if (pomodoroMode == PomodoroMode.WORK) {
        if (displayMinutes > 0) "Work (${displayMinutes}m)" else "Work"
    } else {
        "Break (${displayMinutes}m)"
    }

    if (isLandscape) {
        // Landscape Mode: Timer display stretched big, side-by-side, no tasks.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular timer on the left
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0x10FFFFFF),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = AccentGreen,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dynamicLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = timeStr,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            letterSpacing = (-1).sp
                        )
                    }
                }
            }

            // Controls on the right
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Play / Pause and Reset Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.adjustPomodoroTime(-300) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x0DFFFFFF), CircleShape)
                            .border(1.dp, CardBorder, CircleShape)
                    ) {
                        Text("- 5M", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(if (isRunning) RedBg else AccentGreen.copy(alpha = 0.1f), CircleShape)
                            .border(1.5.dp, if (isRunning) RedBorder else AccentGreen.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Outlined.PauseCircleFilled else Icons.Outlined.PlayCircleFilled,
                            contentDescription = if (isRunning) "Pause timer" else "Start timer",
                            tint = if (isRunning) LightRed else AccentGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.resetPomodoro() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0x1AFFFFFF), CircleShape)
                            .border(1.dp, CardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Reset timer",
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.adjustPomodoroTime(300) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x0DFFFFFF), CircleShape)
                            .border(1.dp, CardBorder, CircleShape)
                    ) {
                        Text("+ 5M", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mode select grid / row
                Text(
                    text = "SELECT MODE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HintText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val firstRowModes = listOf(PomodoroMode.WORK, PomodoroMode.BREAK_5)
                    val secondRowModes = listOf(PomodoroMode.BREAK_10, PomodoroMode.BREAK_15)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        firstRowModes.forEach { mode ->
                            val isSelected = pomodoroMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) AccentGreen else CardBg)
                                    .border(1.dp, if (isSelected) Color.Transparent else CardBorder, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.setPomodoroMode(mode) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) TextWhite else MutedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        secondRowModes.forEach { mode ->
                            val isSelected = pomodoroMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) AccentGreen else CardBg)
                                    .border(1.dp, if (isSelected) Color.Transparent else CardBorder, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.setPomodoroMode(mode) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) TextWhite else MutedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Portrait Mode: Pomodoro Setup + Task Tracker
        var newTaskText by rememberSaveable { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // --- HEADER SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Focus Session",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Boost your productivity with Pomodoro",
                        fontSize = 13.sp,
                        color = MutedText
                    )
                }
            }

            // Progress bar line
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0x20FFFFFF), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(AccentGreen, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- TIMER SECTION ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular progress ring
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0x10FFFFFF),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = AccentGreen,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dynamicLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeStr,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Controls row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.adjustPomodoroTime(-300) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x0DFFFFFF), CircleShape)
                                .border(1.dp, CardBorder, CircleShape)
                        ) {
                            Text("- 5M", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(if (isRunning) RedBg else AccentGreen.copy(alpha = 0.1f), CircleShape)
                                .border(1.dp, if (isRunning) RedBorder else AccentGreen.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Outlined.PauseCircleFilled else Icons.Outlined.PlayCircleFilled,
                                contentDescription = if (isRunning) "Pause timer" else "Start timer",
                                tint = if (isRunning) LightRed else AccentGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.resetPomodoro() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0x1AFFFFFF), CircleShape)
                                .border(1.dp, CardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Reset timer",
                                tint = TextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.adjustPomodoroTime(300) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x0DFFFFFF), CircleShape)
                                .border(1.dp, CardBorder, CircleShape)
                        ) {
                            Text("+ 5M", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Select mode header
                    Text(
                        text = "POMODORO SETUP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HintText,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode select row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PomodoroMode.values().forEach { mode ->
                            val isSelected = pomodoroMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) AccentGreen else Color(0x0DFFFFFF))
                                    .border(1.dp, if (isSelected) Color.Transparent else CardBorder, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.setPomodoroMode(mode) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = when (mode) {
                                        PomodoroMode.WORK -> "25m"
                                        PomodoroMode.BREAK_5 -> "5m"
                                        PomodoroMode.BREAK_10 -> "10m"
                                        PomodoroMode.BREAK_15 -> "15m"
                                    },
                                    color = if (isSelected) TextWhite else MutedText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- TASK TRACKER SECTION ---
            Text(
                text = "📋 TODO LIST",
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Add Task input box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("New task...", color = HintText) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            unfocusedBorderColor = CardBorder,
                            focusedBorderColor = AccentGreen,
                            cursorColor = AccentGreen
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskText.isNotBlank()) {
                                    viewModel.addTask(newTaskText)
                                    newTaskText = ""
                                }
                                keyboardController?.hide()
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                viewModel.addTask(newTaskText)
                                newTaskText = ""
                            }
                            keyboardController?.hide()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("Add", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tasks List
            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks yet. Stay focused!",
                    color = HintText,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        val cardBg = if (task.isCompleted) Color(0x08FFFFFF) else CardBg
                        val borderCol = if (task.isCompleted) Color(0x08FFFFFF) else CardBorder

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Custom Circular Checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .border(
                                                width = 2.dp,
                                                color = if (task.isCompleted) AccentGreen else MutedText,
                                                shape = CircleShape
                                            )
                                            .background(
                                                color = if (task.isCompleted) AccentGreen else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.toggleTaskCompletion(task) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (task.isCompleted) {
                                            Text(
                                                text = "✓",
                                                color = TextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = task.text,
                                        fontSize = 14.sp,
                                        color = if (task.isCompleted) MutedText else TextWhite,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTask(task) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete task",
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
