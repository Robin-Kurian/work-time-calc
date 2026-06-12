package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.utils.AccessBlockReason
import com.example.utils.AppAccessDecision
import com.example.utils.formattedAppVersionLabel

@Composable
fun AppAccessLoadingScreen() {
    AppBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .glassSurface(
                    shape = RoundedCornerShape(24.dp),
                    fillAlpha = 0.78f,
                    elevation = 8.dp
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Checking app access...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
fun AppAccessBlockedScreen(
    decision: AppAccessDecision.Block,
    isRetrying: Boolean = false,
    onRetry: () -> Unit,
    onOpenUpdateUrl: (String) -> Unit
) {
    val appVersionLabel = remember { formattedAppVersionLabel() }
    val title = when (decision.reason) {
        AccessBlockReason.APP_DISABLED -> "App disabled"
        AccessBlockReason.MINIMUM_VERSION_REQUIRED -> "Update required"
        AccessBlockReason.VERSION_BLOCKED -> "Version blocked"
        AccessBlockReason.CANNOT_VERIFY -> "Verification failed"
    }
    val statusColor = when (decision.reason) {
        AccessBlockReason.APP_DISABLED -> AppTheme.colors.lightRed
        AccessBlockReason.MINIMUM_VERSION_REQUIRED -> AppTheme.colors.accentGreen
        AccessBlockReason.VERSION_BLOCKED -> AppTheme.colors.lightAmber
        AccessBlockReason.CANNOT_VERIFY -> AppTheme.colors.lightAmber
    }
    val statusIcon: ImageVector = when (decision.reason) {
        AccessBlockReason.APP_DISABLED -> Icons.Outlined.Lock
        AccessBlockReason.MINIMUM_VERSION_REQUIRED -> Icons.Outlined.Download
        AccessBlockReason.VERSION_BLOCKED -> Icons.Outlined.Info
        AccessBlockReason.CANNOT_VERIFY -> Icons.Outlined.Info
    }

    AppBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .glassSurface(
                        shape = RoundedCornerShape(28.dp),
                        fillAlpha = 0.82f,
                        elevation = 10.dp
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(statusColor.copy(alpha = 0.14f), CircleShape)
                            .border(1.dp, statusColor.copy(alpha = 0.32f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )
                    val showPrimaryMessage = decision.reason != AccessBlockReason.APP_DISABLED &&
                        decision.message.isNotBlank()
                    if (showPrimaryMessage) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = decision.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = AppTheme.colors.mutedText
                        )
                    }
                    val updateUrl = decision.updateUrl
                    val showUpdateButton = decision.reason != AccessBlockReason.APP_DISABLED &&
                        !updateUrl.isNullOrBlank()
                    if (showUpdateButton) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onOpenUpdateUrl(updateUrl) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentGreen,
                                contentColor = AppTheme.colors.textWhite
                            )
                        ) {
                            Text("Update app", color = AppTheme.colors.textWhite)
                        }
                    }

                    if (decision.canRetry) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onRetry,
                            enabled = !isRetrying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppTheme.colors.textPrimary
                            )
                        ) {
                            Text(if (isRetrying) "Retrying..." else "Retry")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = appVersionLabel,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.hintText
            )
        }
    }
}
