package es.guardianos.senior.integration

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import es.guardianos.senior.data.*
import es.guardianos.senior.service.LoudNotificationHelper
import es.guardianos.senior.ui.screens.SafeInboxScreen
import kotlinx.coroutines.*
import android.provider.Settings
import android.content.pm.PackageManager

class WhatsAppSafeMonitor : NotificationListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var db: SeniorDatabase

    override fun onCreate() {
        super.onCreate()
        db = SeniorDatabase.getInstance(this)
        LoudNotificationHelper.initChannel(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName != "com.whatsapp") return
        val extras = sbn.notification.extras ?: return
        
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: "Desconocido"
        val message = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val timestamp = sbn.postTime

        serviceScope.launch {
            val messageEntity = MessageEntity(
                id = System.currentTimeMillis().toString(),
                sender = sender,
                content = message,
                timestamp = timestamp,
                isRead = false
            )
            db.messageDao().insert(messageEntity)
            
            // Notificación ruidosa automática
            LoudNotificationHelper.sendLoudAlert(
                context = this@WhatsAppSafeMonitor,
                title = "💬 MENSAJE DE $sender",
                message = message,
                type = "whatsapp_$sender"
            )
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        fun isPermissionGranted(context: Context): Boolean {
            val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            val componentName = ComponentName(context, WhatsAppSafeMonitor::class.java).flattenToShortString()
            return enabled?.contains(componentName) == true
        }

        fun openSettings(context: Context) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }
}
