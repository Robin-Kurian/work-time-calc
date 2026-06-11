package com.example.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.ui.viewmodel.PomodoroMode
import com.example.ui.viewmodel.WorkViewModel

@Composable
fun PomodoroModeSelector(
    selectedMode: PomodoroMode,
    viewModel: WorkViewModel,
    modifier: Modifier = Modifier,
    layout: ModeLayout = ModeLayout.PortraitRow
) {
    when (layout) {
        ModeLayout.PortraitRow -> {
            Column(modifier = modifier) {
                Text(
                    text = "POMODORO SETUP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.hintText,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PomodoroMode.entries.forEach { mode ->
                        ModeChip(
                            mode = mode,
                            isSelected = selectedMode == mode,
                            onClick = { viewModel.setPomodoroMode(mode) }
                        )
                    }
                }
            }
        }
        ModeLayout.LandscapeGrid -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT MODE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.hintText,
                    letterSpacing = 1.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(PomodoroMode.WORK, PomodoroMode.BREAK_5).forEach { mode ->
                        ModeChipLandscape(mode, selectedMode == mode) { viewModel.setPomodoroMode(mode) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(PomodoroMode.BREAK_10, PomodoroMode.BREAK_15).forEach { mode ->
                        ModeChipLandscape(mode, selectedMode == mode) { viewModel.setPomodoroMode(mode) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeChip(mode: PomodoroMode, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isSelected) {
                    Modifier.background(AppTheme.colors.accentGreen, RoundedCornerShape(20.dp))
                } else {
                    Modifier.glassSurface(shape = RoundedCornerShape(20.dp), fillAlpha = 0.58f, elevation = 2.dp)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = when (mode) {
                PomodoroMode.WORK -> "25m"
                PomodoroMode.BREAK_5 -> "5m"
                PomodoroMode.BREAK_10 -> "10m"
                PomodoroMode.BREAK_15 -> "15m"
            },
            color = if (isSelected) AppTheme.colors.textOnAccent else AppTheme.colors.mutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModeChipLandscape(mode: PomodoroMode, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isSelected) {
                    Modifier.background(AppTheme.colors.accentGreen, RoundedCornerShape(20.dp))
                } else {
                    Modifier.glassSurface(shape = RoundedCornerShape(20.dp), fillAlpha = 0.6f, elevation = 2.dp)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = mode.displayName,
            color = if (isSelected) AppTheme.colors.textOnAccent else AppTheme.colors.mutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

enum class ModeLayout { PortraitRow, LandscapeGrid }
