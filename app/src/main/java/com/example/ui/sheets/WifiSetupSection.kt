package com.example.ui.sheets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface
import com.example.ui.theme.sheetCardSurface
import com.example.ui.viewmodel.WorkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiSetupSection(
    workSsid: String,
    currentSsidValue: String?,
    isAutoTrackingEnabled: Boolean,
    lastCheckedTime: String,
    viewModel: WorkViewModel,
    modifier: Modifier = Modifier,
    requestPermissionsOnMount: Boolean = true
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isEditingSsid by rememberSaveable { mutableStateOf(false) }
    var localSsidInput by remember(workSsid) { mutableStateOf(workSsid) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.checkWifiConnectionInstant()
    }

    LaunchedEffect(requestPermissionsOnMount) {
        if (!requestPermissionsOnMount) return@LaunchedEffect
        val permissionList = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needsLaunch = permissionList.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needsLaunch) {
            permissionLauncher.launch(permissionList.toTypedArray())
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_wifi")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_wifi"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .sheetCardSurface(shape = RoundedCornerShape(16.dp), elevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
    ) {
        if (isEditingSsid) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = localSsidInput,
                    onValueChange = { localSsidInput = it },
                    textStyle = TextStyle(
                        color = AppTheme.colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(AppTheme.colors.accentGreen),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.updateWorkSsid(localSsidInput)
                            isEditingSsid = false
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(AppTheme.colors.surfaceOverlay, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (localSsidInput.isEmpty()) {
                                Text(text = "e.g. Office_WiFi", color = AppTheme.colors.hintText, fontSize = 13.sp)
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.updateWorkSsid(localSsidInput)
                        isEditingSsid = false
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .background(AppTheme.colors.accentGreen, RoundedCornerShape(8.dp))
                        .size(36.dp)
                ) {
                    Icon(Icons.Outlined.Save, "Save", tint = AppTheme.colors.textOnAccent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        localSsidInput = workSsid
                        isEditingSsid = false
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .background(AppTheme.colors.surfaceOverlay, RoundedCornerShape(8.dp))
                        .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                        .size(36.dp)
                ) {
                    Icon(Icons.Outlined.Close, "Cancel", tint = AppTheme.colors.mutedText, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isWorkWifiConnected = currentSsidValue != null &&
                    currentSsidValue == workSsid && workSsid.isNotEmpty()

                Column(modifier = Modifier.weight(1f)) {
                    val wifiText = when {
                        !isAutoTrackingEnabled -> "Auto-tracking Disabled"
                        isWorkWifiConnected -> "Connected - $currentSsidValue"
                        currentSsidValue != null -> "Not connected ($currentSsidValue)"
                        else -> "Not connected"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .alpha(if (isWorkWifiConnected && isAutoTrackingEnabled) pulseAlpha else 1.0f)
                                .background(
                                    color = when {
                                        !isAutoTrackingEnabled -> AppTheme.colors.inactiveGray
                                        isWorkWifiConnected -> AppTheme.colors.accentGreen
                                        else -> AppTheme.colors.lightRed
                                    },
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = wifiText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                !isAutoTrackingEnabled -> AppTheme.colors.mutedText
                                isWorkWifiConnected -> AppTheme.colors.accentGreen
                                else -> AppTheme.colors.lightRed
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SSID: ${workSsid.ifEmpty { "Not set" }} · Last checked: ${lastCheckedTime.ifEmpty { "Never" }}",
                        fontSize = 10.sp,
                        color = AppTheme.colors.mutedText,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { isEditingSsid = true },
                        modifier = Modifier
                            .background(AppTheme.colors.surfaceOverlay, CircleShape)
                            .border(1.dp, AppTheme.colors.cardBorder, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, "Edit SSID", tint = AppTheme.colors.accentGreen, modifier = Modifier.size(16.dp))
                    }

                    val canConnect = currentSsidValue != null &&
                        currentSsidValue != workSsid && !currentSsidValue.isNullOrEmpty()
                    Box(
                        modifier = Modifier
                            .background(
                                if (canConnect) AppTheme.colors.accentGreen.copy(alpha = 0.1f) else AppTheme.colors.surfaceOverlayMedium,
                                RoundedCornerShape(100.dp)
                            )
                            .border(
                                1.dp,
                                if (canConnect) AppTheme.colors.accentGreen.copy(alpha = 0.3f) else Color.Transparent,
                                RoundedCornerShape(100.dp)
                            )
                            .clickable {
                                if (canConnect) viewModel.setConnectedAsWork()
                                else viewModel.checkWifiConnectionInstant()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (canConnect) Icons.Outlined.Wifi else Icons.Outlined.Refresh,
                                contentDescription = if (canConnect) "Connect" else "Refresh",
                                tint = if (canConnect) AppTheme.colors.accentGreen else AppTheme.colors.textPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (canConnect) "CONNECT" else "REFRESH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canConnect) AppTheme.colors.accentGreen else AppTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WifiDisconnectedBanner(
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(12.dp), fillAlpha = 0.6f, elevation = 3.dp)
            .background(AppTheme.colors.redBg, RoundedCornerShape(12.dp))
            .border(1.dp, AppTheme.colors.redBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Wifi,
            contentDescription = "WiFi disconnected",
            tint = AppTheme.colors.lightRed,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Disconnected",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.lightRed,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WifiConnectedSlimBanner(
    ssid: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(12.dp), fillAlpha = 0.58f, elevation = 2.dp)
            .background(AppTheme.colors.accentGreenBg, RoundedCornerShape(12.dp))
            .border(1.dp, AppTheme.colors.accentGreenBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(AppTheme.colors.tealGreen, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Connected",
            fontSize = 12.sp,
            color = AppTheme.colors.tealGreen,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
