package es.guardianos.senior.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import es.guardianos.senior.data.SeniorDatabase

object CaregiverNotifier {
    suspend fun notifyMissedReminder(context: Context, reminderText: String, missedCount: Int) {
        val caregiver = SeniorDatabase.getInstance(context).caregiverDao().getPrimary() ?: return
        if (!caregiver.notifyMissedReminders || !caregiver.prefersSms) return

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasSmsPermission) return

        val sms = SmsManager.getDefault()
        val body = "GuardianOS Senior: ${caregiver.name}, aviso importante. No se ha confirmado: '$reminderText' (intentos perdidos: $missedCount)."
        sms.sendTextMessage(caregiver.phone, null, body, null, null)
    }
}
