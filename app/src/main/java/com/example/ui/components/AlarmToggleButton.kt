package com.example.ui.components

import com.example.ui.theme.AppTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircleFilled
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlarmToggleButton(
    isAlarmEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = if (isAlarmEnabled) Icons.Outlined.PauseCircleFilled
            else Icons.Outlined.PlayCircleFilled,
            contentDescription = if (isAlarmEnabled) "Mute alarms" else "Enable alarms",
            tint = if (isAlarmEnabled) AppTheme.colors.accentGreen else AppTheme.colors.mutedText,
            modifier = Modifier.size(28.dp)
        )
    }
}
