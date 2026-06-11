package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface

@Composable
fun PunchActionBar(
    isIn: Boolean,
    isLocked: Boolean,
    onPunch: () -> Unit,
    onToggleLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onPunch,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("punch_button"),
            shape = RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isIn) AppTheme.colors.redBg else AppTheme.colors.accentGreenBg,
                contentColor = if (isIn) AppTheme.colors.lightRed else AppTheme.colors.accentGreen
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (isIn) AppTheme.colors.lightRed else AppTheme.colors.accentGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isIn) "Punch Out" else "Punch In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onToggleLock,
            modifier = Modifier
                .size(56.dp)
                .glassSurface(
                    shape = RoundedCornerShape(16.dp),
                    fillAlpha = if (isLocked) 0.55f else 0.65f,
                    elevation = 4.dp
                )
                .then(
                    if (isLocked) {
                        Modifier
                            .background(AppTheme.colors.amberSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, AppTheme.colors.amberBorder, RoundedCornerShape(16.dp))
                    } else {
                        Modifier.border(1.dp, AppTheme.colors.glassBorder, RoundedCornerShape(16.dp))
                    }
                )
        ) {
            Icon(
                imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                contentDescription = "Toggle session lock",
                tint = if (isLocked) AppTheme.colors.lightAmber else AppTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
