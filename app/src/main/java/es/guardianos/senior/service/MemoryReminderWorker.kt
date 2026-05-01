package es.guardianos.senior.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.work.*
import es.guardianos.senior.data.*
import es.guardianos.senior.data.SeniorDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MemoryReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = SeniorDatabase.getInstance(applicationContext)
        val dao = db.reminderDao()
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMin = now.get(Calendar.MINUTE)
        val today = ReminderCoordinator.todayIsoDate()
        val dayIndex = ReminderCoordinator.dayIndexMondayFirst()

        val dueReminders = dao.getPendingReminders(currentHour, currentMin, dayIndex, today)
        dueReminders.forEach { reminder ->
            val title = "RECUERDO: ${reminder.type.label}"
            val message = reminder.content

            LoudNotificationHelper.sendLoudAlert(
                context = applicationContext,
                title = title,
                message = message,
                type = "reminder_${reminder.id}"
            )
            CompanionSpeaker.initialize(applicationContext)
            CompanionSpeaker.speak(message)
            dao.markTriggeredToday(reminder.id, today)

            applicationContext.startActivity(
                ReminderCoordinator.reminderPromptIntent(
                    context = applicationContext,
                    reminderId = reminder.id,
                    title = title,
                    message = message
                )
            )
            
            // Reorientación temporal si es Alzheimer
            if (reminder.type == ReminderType.REORIENTATION) {
                val orientation = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("EEEE d MMMM, HH:mm", Locale("es", "ES"))
                )
                LoudNotificationHelper.sendLoudAlert(
                    context = applicationContext,
                    title = "HORA ACTUAL",
                    message = "Ahora mismo es $orientation",
                    type = "time_check"
                )
                CompanionSpeaker.speak("Orientacion temporal. Ahora mismo es $orientation")
            }
        }

        // Aviso diario de lista de compra (18:00) si hay pendientes.
        if (currentHour == 18 && currentMin in 0..14) {
            val digestAlreadySent = db.auditDao().countByTypeAndDetails("SHOPPING_DIGEST", today) > 0
            if (!digestAlreadySent) {
                val pendingShopping = db.shoppingDao().getPendingNow(3)
                if (pendingShopping.isNotEmpty()) {
                    val itemsText = pendingShopping.joinToString(", ") { it.name }
                    val shoppingMessage = "Recuerda comprar: $itemsText"
                    LoudNotificationHelper.sendLoudAlert(
                        context = applicationContext,
                        title = "LISTA DE COMPRA",
                        message = shoppingMessage,
                        type = "shopping_digest"
                    )
                    CompanionSpeaker.speak(shoppingMessage)
                    db.auditDao().insert(
                        AuditLogEntity(
                            type = "SHOPPING_DIGEST",
                            details = today,
                            location = null,
                            confirmed = true
                        )
                    )
                }
            }
        }

        Result.success()
    }

    companion object {
        fun scheduleDaily(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<MemoryReminderWorker>(
                15, TimeUnit.MINUTES // Chequea cada 15min
            )
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_reminders",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun scheduleExact(context: Context, reminder: ReminderEntity) {
            val title = "RECUERDO: ${reminder.type.label}"
            val message = reminder.content
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                ReminderCoordinator.reminderAlarmIntent(context, reminder.id, title, message),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
            }.timeInMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }
}
