package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.navigation.MainContainer
import com.example.receivers.AlarmStopReceiver
import com.example.ui.theme.WorkTimeCalcTheme
import com.example.ui.viewmodel.WorkViewModel

class MainActivity : ComponentActivity() {

    companion object {
        var isForeground = false
    }

    private val viewModel: WorkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAlarmIntent(intent)
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            WorkTimeCalcTheme(darkTheme = isDarkTheme) {
                MainContainer(viewModel)
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
}
