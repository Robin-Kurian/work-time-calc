package com.example.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircleFilled
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.ui.viewmodel.WorkViewModel

@Composable
fun PomodoroControls(
    isRunning: Boolean,
    viewModel: WorkViewModel,
    modifier: Modifier = Modifier,
    buttonScale: ControlScale = ControlScale.Portrait
) {
    val minusSize = when (buttonScale) {
        ControlScale.Landscape -> 40.dp
        ControlScale.Portrait -> 36.dp
    }
    val playSize = when (buttonScale) {
        ControlScale.Landscape -> 64.dp
        ControlScale.Portrait -> 56.dp
    }
    val resetSize = when (buttonScale) {
        ControlScale.Landscape -> 48.dp
        ControlScale.Portrait -> 44.dp
    }
    val playIconSize = when (buttonScale) {
        ControlScale.Landscape -> 36.dp
        ControlScale.Portrait -> 32.dp
    }
    val resetIconSize = when (buttonScale) {
        ControlScale.Landscape -> 24.dp
        ControlScale.Portrait -> 20.dp
    }
    val spacing = when (buttonScale) {
        ControlScale.Landscape -> 16.dp
        ControlScale.Portrait -> 12.dp
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { viewModel.adjustPomodoroTime(-300) },
            modifier = Modifier
                .size(minusSize)
                .glassSurface(shape = CircleShape, fillAlpha = 0.6f, elevation = 3.dp)
        ) {
            Text("- 5M", color = AppTheme.colors.mutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        IconButton(
            onClick = { if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro() },
            modifier = Modifier
                .size(playSize)
                .background(if (isRunning) AppTheme.colors.redBg else AppTheme.colors.accentGreen.copy(alpha = 0.1f), CircleShape)
                .border(
                    if (buttonScale == ControlScale.Landscape) 1.5.dp else 1.dp,
                    if (isRunning) AppTheme.colors.redBorder else AppTheme.colors.accentGreen.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Outlined.PauseCircleFilled else Icons.Outlined.PlayCircleFilled,
                contentDescription = if (isRunning) "Pause timer" else "Start timer",
                tint = if (isRunning) AppTheme.colors.lightRed else AppTheme.colors.accentGreen,
                modifier = Modifier.size(playIconSize)
            )
        }

        IconButton(
            onClick = { viewModel.resetPomodoro() },
            modifier = Modifier
                .size(resetSize)
                .glassSurface(shape = CircleShape, fillAlpha = 0.65f, elevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Reset timer",
                tint = AppTheme.colors.textPrimary,
                modifier = Modifier.size(resetIconSize)
            )
        }

        IconButton(
            onClick = { viewModel.adjustPomodoroTime(300) },
            modifier = Modifier
                .size(minusSize)
                .glassSurface(shape = CircleShape, fillAlpha = 0.6f, elevation = 3.dp)
        ) {
            Text("+ 5M", color = AppTheme.colors.accentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

enum class ControlScale { Portrait, Landscape }
