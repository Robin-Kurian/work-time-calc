package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
    Manual,
    Session
}

@Composable
fun MainContainer(viewModel: WorkViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.Session) }
    val context = LocalContext.current

    // Permissions check
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
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
                    NavigationTab.Manual -> ManualScreen(viewModel)
                    NavigationTab.Session -> SessionScreen(viewModel)
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
    Surface(
        color = Color(0xFF0A0F0D),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(64.dp)
            .border(width = 1.dp, color = Color(0x1AFFFFFF))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Manual Navigation Button
            val isManualSelected = selectedTab == NavigationTab.Manual
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(NavigationTab.Manual) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Keyboard,
                    contentDescription = "Manual tab icon",
                    tint = if (isManualSelected) AccentGreen else MutedText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Manual",
                    color = if (isManualSelected) AccentGreen else MutedText,
                    fontSize = 10.sp,
                    fontWeight = if (isManualSelected) FontWeight.Bold else FontWeight.Medium
                )
            }

            // Session Navigation Button
            val isSessionSelected = selectedTab == NavigationTab.Session
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(NavigationTab.Session) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = "Session tab icon",
                    tint = if (isSessionSelected) AccentGreen else MutedText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Session",
                    color = if (isSessionSelected) AccentGreen else MutedText,
                    fontSize = 10.sp,
                    fontWeight = if (isSessionSelected) FontWeight.Bold else FontWeight.Medium
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
                    text = "Work Time Calc.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "\"You never know 'when!'\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MutedText
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0x1A10B981), RoundedCornerShape(100.dp))
                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SDK 52 • 2026",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    letterSpacing = 1.sp
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
            horizontalArrangement = Arrangement.spacedKey(10.dp)
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
                    Text("🍔", fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
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

// Reusable arrangement helper
fun Arrangement.spacedKey(space: androidx.compose.ui.unit.Dp): Arrangement.HorizontalOrVertical {
    return Arrangement.spacedBy(space)
}

// Reusable Native TimePicker helper
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


// ---------------- SESSION SCREEN ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(viewModel: WorkViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val workSsid by viewModel.workSsid.collectAsState()
    val currentSsid by viewModel.currentSsid.collectAsState()
    val lastCheckedTime by viewModel.lastCheckedTime.collectAsState()
    val liveActiveSeconds by viewModel.liveActiveElapsedSeconds.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val workedMin = viewModel.calculateWorkedMinutes(sessions)
    val pct = Math.min(100, Math.round((workedMin.toFloat() / REQUIRED_MINUTES) * 100))
    val remaining = Math.max(0, REQUIRED_MINUTES - workedMin)
    val isDone = workedMin >= REQUIRED_MINUTES
    val isIn = sessions.isNotEmpty() && sessions.first().outTime == null

    // Pulsing animation for dots
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

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
                    text = "Session Tracker",
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

        Spacer(modifier = Modifier.height(16.dp))

        // --- WIFi STATUS PILL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isWorkWifiConnected = currentSsid != null && currentSsid == workSsid && workSsid.isNotEmpty()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsing Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .alpha(if (isWorkWifiConnected) pulseAlpha else 1.0f)
                            .background(
                                color = if (isWorkWifiConnected) AccentGreen else Color.Red,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val wifiText = if (isWorkWifiConnected) {
                            "✓ Work WiFi — $currentSsid"
                        } else if (currentSsid != null) {
                            "✗ Not on work WiFi ($currentSsid)"
                        } else {
                            "✗ Not on work WiFi"
                        }
                        Text(
                            text = wifiText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWorkWifiConnected) AccentGreen else Color.Red
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Last checked: ${lastCheckedTime.ifEmpty { "Never" }}",
                            fontSize = 10.sp,
                            color = MutedText
                        )
                    }
                }

                // Action Column (Refresh & Set as Work)
                Column(horizontalAlignment = Alignment.End) {
                    // Refresh small button
                    Box(
                        modifier = Modifier
                            .background(Color(0x1BFFFFFF), RoundedCornerShape(100.dp))
                            .clickable { viewModel.checkWifiConnectionInstant() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh WiFi status",
                                tint = TextWhite,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REFRESH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                    
                    if (currentSsid != null && currentSsid != workSsid) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use current WiFi",
                            color = AccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.setConnectedAsWork() }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SSID SETTING Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔧 Work WiFi Name (SSID)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))

                var localSsidInput by remember(workSsid) { mutableStateOf(workSsid) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = localSsidInput,
                        onValueChange = { localSsidInput = it },
                        placeholder = { Text("e.g. Office_WiFi", color = HintText) },
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
                                viewModel.updateWorkSsid(localSsidInput)
                                keyboardController?.hide()
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateWorkSsid(localSsidInput)
                            keyboardController?.hide()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = "Save work SSID settings",
                            tint = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "App auto-punches when it detects this WiFi name",
                    fontSize = 11.sp,
                    color = MutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MAIN PUNCH BUTTON ---
        val punchButtonColor = if (isIn) RedBg else AccentGreen
        val punchButtonBorderColor = if (isIn) RedBorder else Color.Transparent

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(0.98f)
                .height(72.dp)
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
                    text = if (isIn) "🔴 Punch Out" else "🟢 Punch In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIn) LightRed else TextWhite
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
                Text(
                    text = "Clocked in since $startLocalTime · ⏱ ${TimeUtils.fmtElapsed(liveActiveSeconds)}",
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

        Spacer(modifier = Modifier.height(24.dp))

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
                    val nowMin = TimeUtils.nowMinutes()
                    val leaveMin = Math.floor(nowMin.toDouble() + Math.ceil(remaining.toDouble())).toInt()
                    val nextDay = leaveMin >= 1440
                    val leaveStr = TimeUtils.toTime(leaveMin) + (if (nextDay) " (Tomorrow)" else "")
                    Text(
                        text = "Leave by ~$leaveStr",
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                    val inStr = TimeUtils.fmtTimestamp(session.inTime)
                                    val outStr = if (isOpen) "Now" else TimeUtils.fmtTimestamp(session.outTime!!)
                                    Text(
                                        text = "$inStr — $outStr",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isOpen) {
                                            "Session $sessionNumber • Active"
                                        } else {
                                            "Session $sessionNumber"
                                        },
                                        fontSize = 10.sp,
                                        color = MutedText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = TimeUtils.fmtDur(durationMin),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOpen) AccentGreen else TextWhite
                                )
                                Text(
                                    text = if (isOpen) "ONGOING" else "COMPLETED",
                                    fontSize = 9.sp,
                                    color = if (isOpen) AccentGreen else HintText,
                                    fontWeight = FontWeight.Bold
                                )
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
    }
}

// Dialog helper for clearance confirmation to avoid crash
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
