package es.guardianos.senior.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import es.guardianos.senior.service.CompanionSpeaker
import es.guardianos.senior.service.LoudNotificationHelper
import es.guardianos.senior.service.MemoryReminderWorker
import es.guardianos.senior.service.SeniorCareManager
import es.guardianos.senior.ui.theme.GuardianOSSeniorTheme
import es.guardianos.senior.ui.scenes.HomeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoudNotificationHelper.initChannel(this)
        CompanionSpeaker.initialize(this)
        MemoryReminderWorker.scheduleDaily(this)
        lifecycleScope.launch {
            SeniorCareManager.seedDefaultPlan(applicationContext)
        }

        setContent {
            GuardianOSSeniorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    HomeScreen(context = this)
                }
            }
        }
    }
}
