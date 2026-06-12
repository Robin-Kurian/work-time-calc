package com.example.navigation

import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.utils.PermissionUtils
import com.example.ui.components.AppBottomBar
import com.example.ui.dialogs.AlarmDialog
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.plan.PlanScreen
import com.example.ui.screens.today.TodayScreen
import com.example.ui.theme.AppBackground
import com.example.ui.viewmodel.WorkViewModel

@Composable
fun MainContainer(viewModel: WorkViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(NavigationTab.Today) }
    val activeAlarm by viewModel.activeAlarm.collectAsState()
    val context = LocalContext.current
    var hasPromptedForRuntimePermissions by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onLocationPermissionChanged()
    }

    val requestMissingPermissions = {
        val missingPermissions = PermissionUtils.missingRuntimePermissions(context)
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            viewModel.onLocationPermissionChanged()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLocationPermissionChanged()
    }

    LaunchedEffect(hasPromptedForRuntimePermissions) {
        if (hasPromptedForRuntimePermissions) return@LaunchedEffect
        hasPromptedForRuntimePermissions = true
        requestMissingPermissions()
    }

    LaunchedEffect(activeAlarm) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        if (activeAlarm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity.setShowWhenLocked(true)
                activity.setTurnScreenOn(true)
            }
            activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
            viewModel.playAlarmSoundAndVibration()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity.setShowWhenLocked(false)
                activity.setTurnScreenOn(false)
            }
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
    }

    AppBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                AppBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        NavigationTab.Today -> TodayScreen(
                            viewModel = viewModel,
                            onNavigateToPlan = { selectedTab = NavigationTab.Plan },
                            onRequestPermissions = requestMissingPermissions
                        )
                        NavigationTab.Plan -> PlanScreen(
                            viewModel = viewModel,
                            onRequestPermissions = requestMissingPermissions
                        )
                        NavigationTab.Focus -> FocusScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    activeAlarm?.let { alarm ->
        AlarmDialog(
            alarm = alarm,
            onStop = { viewModel.stopAlarm() }
        )
    }
}
