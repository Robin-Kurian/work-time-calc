package com.example.ui.screens.focus

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.NavigationTab
import com.example.ui.components.AlarmToggleButton
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.ui.viewmodel.PomodoroMode
import com.example.ui.viewmodel.WorkViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: WorkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pomodoroMode by viewModel.pomodoroMode.collectAsState()
    val remainingSeconds by viewModel.pomodoroRemainingSeconds.collectAsState()
    val totalSeconds by viewModel.pomodoroTotalSeconds.collectAsState()
    val isRunning by viewModel.pomodoroIsRunning.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val isAlarmEnabled by viewModel.isAlarmEnabled.collectAsState()

    DisposableEffect(isRunning) {
        val activity = context as? Activity
        val window = activity?.window
        if (isRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        Box(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AlarmToggleButton(
                isAlarmEnabled = isAlarmEnabled,
                onToggle = { viewModel.toggleAlarmEnabled() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                PomodoroRing(
                    progress = progress,
                    label = dynamicLabel,
                    timeStr = timeStr,
                    ringSize = 260.dp,
                    strokeWidth = 14.dp,
                    timeFontSize = 56.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PomodoroControls(
                    isRunning = isRunning,
                    viewModel = viewModel,
                    buttonScale = ControlScale.Landscape
                )
                Spacer(modifier = Modifier.height(20.dp))
                PomodoroModeSelector(
                    selectedMode = pomodoroMode,
                    viewModel = viewModel,
                    layout = ModeLayout.LandscapeGrid
                )
            }
            }
        }
    } else {
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
                        text = NavigationTab.Focus.label,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Boost your productivity with Pomodoro",
                        fontSize = 13.sp,
                        color = AppTheme.colors.mutedText
                    )
                }
                AlarmToggleButton(
                    isAlarmEnabled = isAlarmEnabled,
                    onToggle = { viewModel.toggleAlarmEnabled() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(AppTheme.colors.surfaceOverlayLight, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(AppTheme.colors.accentGreen, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(shape = RoundedCornerShape(24.dp), fillAlpha = 0.72f, elevation = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PomodoroRing(
                        progress = progress,
                        label = dynamicLabel,
                        timeStr = timeStr,
                        ringSize = 160.dp,
                        strokeWidth = 10.dp,
                        timeFontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PomodoroControls(
                        isRunning = isRunning,
                        viewModel = viewModel,
                        buttonScale = ControlScale.Portrait
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PomodoroModeSelector(
                        selectedMode = pomodoroMode,
                        viewModel = viewModel,
                        layout = ModeLayout.PortraitRow
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            TaskListSection(tasks = tasks, viewModel = viewModel)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PomodoroRing(
    progress: Float,
    label: String,
    timeStr: String,
    ringSize: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    timeFontSize: TextUnit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = colors.ringTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = colors.accentGreen,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = if (timeFontSize.value > 40) 16.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.mutedText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeStr,
                fontSize = timeFontSize,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                letterSpacing = if (timeFontSize.value > 40) (-1).sp else 0.sp
            )
        }
    }
}
