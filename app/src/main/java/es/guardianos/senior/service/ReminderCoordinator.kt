package es.guardianos.senior.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import es.guardianos.senior.data.SeniorDatabase
import es.guardianos.senior.ui.ReminderPromptActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

object ReminderCoordinator {
    private const val ACTION_REMINDER_TRIGGER = "es.guardianos.senior.REMINDER_TRIGGER"
    private const val EXTRA_REMINDER_ID = "extra_reminder_id"
    private const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
    private const val EXTRA_REMINDER_MESSAGE = "extra_reminder_message"

    fun todayIsoDate(): String = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

    fun dayIndexMondayFirst(): Int {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    fun reminderPromptIntent(context: Context, reminderId: String, title: String, message: String): Intent {
        return Intent(context, ReminderPromptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
            putExtra(EXTRA_REMINDER_MESSAGE, message)
        }
    }

    fun reminderAlarmIntent(context: Context, reminderId: String, title: String, message: String): Intent {
        return Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER_TRIGGER
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
            putExtra(EXTRA_REMINDER_MESSAGE, message)
        }
    }

    fun parseReminderIntent(intent: Intent): Triple<String, String, String> {
        val id = intent.getStringExtra(EXTRA_REMINDER_ID).orEmpty()
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE).orEmpty()
        val message = intent.getStringExtra(EXTRA_REMINDER_MESSAGE).orEmpty()
        return Triple(id, title, message)
    }

    suspend fun confirmReminder(context: Context, reminderId: String) {
        if (reminderId.isBlank()) return
        val dao = SeniorDatabase.getInstance(context).reminderDao()
        dao.markConfirmed(reminderId, System.currentTimeMillis())
    }

    suspend fun markMissedAndEscalate(context: Context, reminderId: String) {
        if (reminderId.isBlank()) return
        val dao = SeniorDatabase.getInstance(context).reminderDao()
        val current = dao.getById(reminderId) ?: return
        if (!current.requiresConfirmation) return

        dao.incrementMissed(reminderId)
        val updated = dao.getById(reminderId) ?: return
        if (updated.missedCount >= 2) {
            CaregiverNotifier.notifyMissedReminder(context, updated.content, updated.missedCount)
        }
    }

    suspend fun snoozeReminder(context: Context, reminderId: String, minutes: Int = 15) {
        if (reminderId.isBlank()) return
        val dao = SeniorDatabase.getInstance(context).reminderDao()
        val reminder = dao.getById(reminderId) ?: return

        val title = "RECUERDO: ${reminder.type.label}"
        val message = reminder.content
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            reminderAlarmIntent(context, reminder.id, title, message),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }
}
