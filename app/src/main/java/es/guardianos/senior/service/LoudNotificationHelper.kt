package es.guardianos.senior.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import es.guardianos.senior.R
import es.guardianos.senior.ui.MainActivity

object LoudNotificationHelper {
    private const val CHANNEL_ID = "loud_alerts"

    fun sendLoudAlert(context: Context, title: String, message: String, type: String) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager.isWiredHeadsetOn) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_emergency)
            .setContentTitle(title)
            .setContentText(message)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(type.hashCode(), notification)
    }

    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alertas Ruidosas", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }
}
