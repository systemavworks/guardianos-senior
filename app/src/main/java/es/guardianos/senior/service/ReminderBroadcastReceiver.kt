package es.guardianos.senior.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val (reminderId, title, message) = ReminderCoordinator.parseReminderIntent(intent)

        LoudNotificationHelper.sendLoudAlert(
            context = context,
            title = if (title.isBlank()) "RECORDATORIO" else title,
            message = if (message.isBlank()) "Tienes una tarea pendiente" else message,
            type = "exact_reminder_$reminderId"
        )
        CompanionSpeaker.initialize(context)
        CompanionSpeaker.speak(message.ifBlank { "Tienes un recordatorio pendiente" })

        context.startActivity(
            ReminderCoordinator.reminderPromptIntent(
                context = context,
                reminderId = reminderId,
                title = title.ifBlank { "RECORDATORIO" },
                message = message.ifBlank { "Tienes una tarea pendiente" }
            )
        )
    }
}
