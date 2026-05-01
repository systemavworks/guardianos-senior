package es.guardianos.senior.ui.scenes
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.guardianos.senior.service.CompanionSpeaker
import es.guardianos.senior.service.FallDetectionService
import es.guardianos.senior.service.ReminderCoordinator
import es.guardianos.senior.ui.CaregiverSettingsActivity
import es.guardianos.senior.ui.PhotoGalleryActivity
import es.guardianos.senior.ui.SafeInboxActivity

@Composable
fun HomeScreen(context: Context) {
    val emergencyIntent = Intent(context, FallDetectionService::class.java)
    var isPanicActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "GUARDIANOS SENIOR",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // Acceso oculto para configuración de mantenimiento.
                        context.startActivity(Intent(context, CaregiverSettingsActivity::class.java))
                    }
                )
            }
        )

        Text(
            "Inicio sencillo",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SeniorGridButton(
                label = "LLAMAR",
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_DIAL).apply { `package` = null })
                }
            )
            SeniorGridButton(
                label = "FOTOS",
                color = Color(0xFF006C9C),
                modifier = Modifier.weight(1f),
                onClick = {
                    context.startActivity(Intent(context, PhotoGalleryActivity::class.java))
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SeniorGridButton(
                label = "WHATSAPP",
                color = Color(0xFF1FAA59),
                modifier = Modifier.weight(1f),
                onClick = {
                    context.startActivity(Intent(context, SafeInboxActivity::class.java))
                }
            )
            SeniorGridButton(
                label = "AYUDANTE",
                color = Color(0xFF5E35B1),
                modifier = Modifier.weight(1f),
                onClick = {
                    CompanionSpeaker.initialize(context)
                    CompanionSpeaker.speak("Te ayudo con tu rutina diaria")
                    context.startActivity(
                        ReminderCoordinator.reminderPromptIntent(
                            context = context,
                            reminderId = "reminder-medication-0900",
                            title = "RECORDATORIO",
                            message = "Es momento de revisar tu medicacion"
                        )
                    )
                }
            )
        }

        SeniorMainButton(
            label = "EMERGENCIA",
            color = Color(0xFFC62828),
            onClick = {
                if (!isPanicActive) {
                    context.startForegroundService(emergencyIntent)
                    isPanicActive = true
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f, fill = true))

        Text(
            "Mantener pulsado el titulo para ajustes de mantenimiento",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
    }
}

@Composable
fun SeniorMainButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun SeniorGridButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(116.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
