package es.guardianos.senior.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import es.guardianos.senior.service.CompanionSpeaker
import es.guardianos.senior.service.ReminderCoordinator
import es.guardianos.senior.ui.theme.GuardianOSSeniorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReminderPromptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableOverlayMode()

        val reminderId = intent.getStringExtra("extra_reminder_id").orEmpty()
        val title = intent.getStringExtra("extra_reminder_title").orEmpty().ifBlank { "RECORDATORIO" }
        val message = intent.getStringExtra("extra_reminder_message").orEmpty().ifBlank { "Tienes una tarea pendiente" }

        CompanionSpeaker.initialize(this)
        CompanionSpeaker.speak(message)

        setContent {
            GuardianOSSeniorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ReminderPromptContent(
                        title = title,
                        message = message,
                        onConfirm = {
                            lifecycleScope.launch {
                                ReminderCoordinator.confirmReminder(applicationContext, reminderId)
                                finish()
                            }
                        },
                        onSnooze = {
                            lifecycleScope.launch {
                                ReminderCoordinator.snoozeReminder(applicationContext, reminderId, 15)
                                finish()
                            }
                        },
                        onTimeout = {
                            lifecycleScope.launch {
                                ReminderCoordinator.markMissedAndEscalate(applicationContext, reminderId)
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun enableOverlayMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}

@Composable
private fun ReminderPromptContent(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit,
    onTimeout: () -> Unit,
) {
    var countdownSec by remember { mutableStateOf(180) }

    LaunchedEffect(Unit) {
        while (countdownSec > 0) {
            delay(1000)
            countdownSec -= 1
        }
        onTimeout()
    }

    val mins = countdownSec / 60
    val secs = countdownSec % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Tiempo para confirmar: ${mins}:${secs.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            Text("SI, YA LO HE HECHO", fontSize = 26.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onSnooze,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text("RECORDARME EN 15 MIN", fontSize = 22.sp)
        }
    }
}
