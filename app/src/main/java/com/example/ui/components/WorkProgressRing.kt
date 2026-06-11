package com.example.ui.components

import com.example.ui.theme.AppTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.TimeUtils

@Composable
fun WorkProgressRing(
    percent: Int,
    workedMs: Long,
    targetMinutes: Int,
    isDone: Boolean,
    modifier: Modifier = Modifier,
    ringSize: Dp = 140.dp,
    strokeWidth: Dp = 10.dp,
    showStats: Boolean = true
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    color = if (isDone) colors.lightAmber else colors.accentGreen,
                    startAngle = -90f,
                    sweepAngle = (percent.toFloat() / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$percent%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "done",
                    fontSize = 11.sp,
                    color = colors.mutedText
                )
            }
        }
        if (showStats) {
            Text(
                text = TimeUtils.fmtDurSeconds(workedMs / 1000),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "of ${TimeUtils.fmtDur(targetMinutes)} target",
                fontSize = 13.sp,
                color = colors.mutedText
            )
        }
    }
}
