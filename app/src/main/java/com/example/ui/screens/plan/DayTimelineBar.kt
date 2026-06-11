package com.example.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.ui.viewmodel.ManualCalcState
import com.example.utils.TimeUtils
import com.example.utils.TimeUtils.BUFFER_IN
import com.example.utils.TimeUtils.BUFFER_OUT
import kotlin.math.max

@Composable
fun DayTimelineBar(
    calcState: ManualCalcState,
    modifier: Modifier = Modifier
) {
    val minSegmentMin = 1f
    val wBufferIn = max(minSegmentMin, BUFFER_IN.toFloat())
    val wAmWork = max(minSegmentMin, calcState.amWork.toFloat())
    val wLunch = max(minSegmentMin, calcState.lunchDur.toFloat())
    val wPmWork = max(minSegmentMin, calcState.pmWork.toFloat())
    val wBufferOut = max(minSegmentMin, BUFFER_OUT.toFloat())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .glassSurface(shape = RoundedCornerShape(14.dp), fillAlpha = 0.55f, elevation = 4.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(wBufferIn)
                    .fillMaxHeight()
                    .background(AppTheme.colors.surfaceOverlayStrong),
                contentAlignment = Alignment.Center
            ) {
                if (calcState.amWork > 20) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BUF", fontSize = 8.sp, color = AppTheme.colors.hintText, fontWeight = FontWeight.Bold)
                        Text("5m", fontSize = 10.sp, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(wAmWork)
                    .fillMaxHeight()
                    .background(AppTheme.colors.darkGreen),
                contentAlignment = Alignment.Center
            ) {
                if (calcState.amWork > 30) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AM", fontSize = 8.sp, color = AppTheme.colors.timelineLabel, fontWeight = FontWeight.Bold)
                        Text(TimeUtils.fmtDur(calcState.amWork), fontSize = 10.sp, color = AppTheme.colors.textOnAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(wLunch)
                    .fillMaxHeight()
                    .background(AppTheme.colors.glassFillSubtle),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lunch", fontSize = 9.sp, color = AppTheme.colors.lightAmber, fontWeight = FontWeight.Bold)
                    if (calcState.lunchDur > 25) {
                        Text(
                            text = TimeUtils.fmtDur(calcState.lunchDur),
                            fontSize = 9.sp,
                            color = AppTheme.colors.hintText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(wPmWork)
                    .fillMaxHeight()
                    .background(AppTheme.colors.tealGreen),
                contentAlignment = Alignment.Center
            ) {
                if (calcState.pmWork > 30) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PM", fontSize = 8.sp, color = AppTheme.colors.timelineLabel, fontWeight = FontWeight.Bold)
                        Text(TimeUtils.fmtDur(calcState.pmWork), fontSize = 10.sp, color = AppTheme.colors.textOnAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(wBufferOut)
                    .fillMaxHeight()
                    .background(AppTheme.colors.surfaceOverlayStrong),
                contentAlignment = Alignment.Center
            ) {
                if (calcState.pmWork > 20) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BUF", fontSize = 8.sp, color = AppTheme.colors.hintText, fontWeight = FontWeight.Bold)
                        Text("5m", fontSize = 10.sp, color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
