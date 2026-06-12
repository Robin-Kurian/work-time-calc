package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.navigation.MainContainer
import com.example.receivers.AlarmStopReceiver
import com.example.ui.screens.AppAccessBlockedScreen
import com.example.ui.theme.WorkTimeCalcTheme
import com.example.ui.viewmodel.WorkViewModel
import com.example.utils.AppAccessController
import com.example.utils.AppAccessDecision
import com.example.utils.AccessBlockReason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        var isForeground = false
    }

    private val viewModel: WorkViewModel by viewModels()
    private val appAccessController by lazy { AppAccessController(applicationContext) }
    private var appAccessState: AppAccessUiState by mutableStateOf(AppAccessUiState.Checking)
    private var isInitialAccessCheckComplete by mutableStateOf(false)
    private var isRetryInProgress by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isInitialAccessCheckComplete }

        super.onCreate(savedInstanceState)
        handleAlarmIntent(intent)
        runAccessCheck(isInitialLaunch = true)
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            when (val state = appAccessState) {
                AppAccessUiState.Checking -> Unit
                AppAccessUiState.Allowed -> WorkTimeCalcTheme(darkTheme = isDarkTheme) {
                    MainContainer(viewModel)
                }
                is AppAccessUiState.Blocked -> WorkTimeCalcTheme(darkTheme = false) {
                    AppAccessBlockedScreen(
                        decision = state.decision,
                        isRetrying = isRetryInProgress,
                        onRetry = ::runAccessCheck,
                        onOpenUpdateUrl = ::openUpdateUrl
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        isForeground = true
        viewModel.onLocationPermissionChanged()
    }

    override fun onStop() {
        super.onStop()
        isForeground = false
    }

    private fun handleAlarmIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(AlarmStopReceiver.EXTRA_STOP_ALARM, false) == true) {
            viewModel.stopAlarm()
            intent.removeExtra(AlarmStopReceiver.EXTRA_STOP_ALARM)
        }
    }

    private fun runAccessCheck() {
        runAccessCheck(
            isInitialLaunch = false,
            isUserInitiated = true
        )
    }

    private fun runAccessCheck(
        isInitialLaunch: Boolean,
        isUserInitiated: Boolean = false
    ) {
        if (isUserInitiated && isRetryInProgress) return

        if (isInitialLaunch) {
            appAccessState = AppAccessUiState.Checking
            isInitialAccessCheckComplete = false
        }
        if (isUserInitiated) {
            isRetryInProgress = true
        }

        lifecycleScope.launch {
            val decision = try {
                appAccessController.evaluateAccess(
                    allowCachedFallback = !isUserInitiated,
                    maxFetchAttempts = if (isUserInitiated) 3 else 1
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                AppAccessDecision.Block(
                    reason = AccessBlockReason.CANNOT_VERIFY,
                    message = "Unable to verify access right now. Connect to internet and try again.",
                    detail = error.message,
                    updateUrl = null,
                    canRetry = true
                )
            }

            try {
                appAccessState = when (decision) {
                    AppAccessDecision.Allow -> AppAccessUiState.Allowed
                    is AppAccessDecision.Block -> AppAccessUiState.Blocked(decision)
                }
            } finally {
                if (isUserInitiated) {
                    isRetryInProgress = false
                }
                if (isInitialLaunch) {
                    isInitialAccessCheckComplete = true
                }
            }
        }
    }

    private fun openUpdateUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private sealed interface AppAccessUiState {
        data object Checking : AppAccessUiState
        data object Allowed : AppAccessUiState
        data class Blocked(val decision: AppAccessDecision.Block) : AppAccessUiState
    }
}
