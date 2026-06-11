package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.utils.TimeUtils

@Composable
fun LeaveAtChip(
    leaveMin: Int?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val label = if (leaveMin == null) "Leave now" else "Leave at ${TimeUtils.toTime(leaveMin)}"
    Row(
        modifier = modifier
            .wrapContentWidth()
            .glassSurface(shape = RoundedCornerShape(12.dp), fillAlpha = 0.6f, elevation = 4.dp)
            .background(AppTheme.colors.amberBg, RoundedCornerShape(12.dp))
            .border(1.dp, AppTheme.colors.amberBorder, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = "Leave time",
            tint = AppTheme.colors.lightAmber,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.lightAmber
        )
    }
}
