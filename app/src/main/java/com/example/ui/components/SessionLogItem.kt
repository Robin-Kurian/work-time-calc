package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Session
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.utils.OutsideBreakType
import com.example.utils.TimeUtils

@Composable
fun SessionLogItem(
    session: Session,
    sessionNumber: Int,
    durationSec: Long,
    isOpen: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemBorder = if (isOpen) AppTheme.colors.accentGreenBorder else AppTheme.colors.cardBorder

    Card(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(16.dp), fillAlpha = if (isOpen) 0.68f else 0.65f, elevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isOpen) AppTheme.colors.accentGreenBg else androidx.compose.ui.graphics.Color.Transparent),
        border = BorderStroke(1.dp, itemBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .background(if (isOpen) AppTheme.colors.accentGreen else AppTheme.colors.inactiveDot, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Session $sessionNumber",
                            fontSize = 12.sp,
                            color = AppTheme.colors.mutedText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = TimeUtils.fmtDurSeconds(durationSec),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOpen) AppTheme.colors.accentGreen else AppTheme.colors.textPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOpen) {
                        Box(
                            modifier = Modifier
                                .background(AppTheme.colors.accentGreenBorder, RoundedCornerShape(100.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ONGOING", fontSize = 9.sp, color = AppTheme.colors.accentGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit session",
                            tint = AppTheme.colors.darkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete session",
                            tint = AppTheme.colors.lightRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .background(AppTheme.colors.accentGreenBg, RoundedCornerShape(8.dp))
                        .border(1.dp, AppTheme.colors.accentGreenBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(AppTheme.colors.accentGreen, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "IN: ${TimeUtils.fmtTimestamp(session.inTime)}",
                            fontSize = 11.sp,
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            if (isOpen) AppTheme.colors.surfaceOverlayMedium else AppTheme.colors.redBg,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isOpen) AppTheme.colors.cardBorder else AppTheme.colors.redBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isOpen) AppTheme.colors.mutedText else AppTheme.colors.lightRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOpen) "OUT: --" else "OUT: ${TimeUtils.fmtTimestamp(session.outTime!!)}",
                            fontSize = 11.sp,
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionBreakDivider(
    outsideSec: Long,
    outTime: Long,
    modifier: Modifier = Modifier,
    aggregated: Boolean = false
) {
    Box(
        modifier = modifier
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
                    .background(AppTheme.colors.dividerColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .background(AppTheme.colors.surfaceOverlay, RoundedCornerShape(12.dp))
                    .border(1.dp, AppTheme.colors.dividerColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                OutsideBreakLabel(outTime = outTime, outsideSec = outsideSec, aggregated = aggregated)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(AppTheme.colors.dividerColor)
            )
        }
    }
}

@Composable
private fun OutsideBreakLabel(
    outTime: Long,
    outsideSec: Long,
    modifier: Modifier = Modifier,
    aggregated: Boolean = false
) {
    val duration = TimeUtils.fmtDurSeconds(outsideSec)
    if (aggregated) {
        OutsideTimeLabel(duration = duration, modifier = modifier)
        return
    }
    val breakType = TimeUtils.classifyOutsideBreak(outTime, outsideSec)

    when (breakType) {
        OutsideBreakType.WARNING, OutsideBreakType.DEDUCTED -> {
            OutsideTimeLabel(duration = duration, modifier = modifier)
        }
        else -> {
            Text(
                modifier = modifier,
                text = TimeUtils.formatOutsideBreak(outTime, outsideSec),
                fontSize = 11.sp,
                color = AppTheme.colors.mutedText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OutsideTimeLabel(
    duration: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.RemoveCircle,
            contentDescription = "Outside time",
            tint = AppTheme.colors.lightRed,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = duration,
            fontSize = 11.sp,
            color = AppTheme.colors.lightRed,
            fontWeight = FontWeight.Medium
        )
    }
}
