package com.example.ui.components

import com.example.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Session
import com.example.utils.TimeUtils

@Composable
fun StatusPill(
    sessions: List<Session>,
    isIn: Boolean,
    liveActiveSeconds: Long,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (isIn) AppTheme.colors.accentGreen else AppTheme.colors.inactiveGray, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        if (isIn) {
            val startLocalTime = TimeUtils.fmtTimestamp(sessions.first().inTime)
            val lockStatus = if (isLocked) " · Locked" else ""
            Text(
                text = "Clocked in since $startLocalTime · ${TimeUtils.fmtElapsed(liveActiveSeconds)}$lockStatus",
                color = AppTheme.colors.accentGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        } else if (sessions.isNotEmpty()) {
            val lastOutVal = sessions.first().outTime
            val lastOutStr = if (lastOutVal != null) TimeUtils.fmtTimestamp(lastOutVal) else ""
            Text(
                text = "Clocked out at $lastOutStr",
                color = AppTheme.colors.mutedText,
                fontSize = 12.sp
            )
        } else {
            Text(
                text = "Not clocked in",
                color = AppTheme.colors.mutedText,
                fontSize = 12.sp
            )
        }
    }
}
