package es.guardianos.senior.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import es.guardianos.senior.R
import es.guardianos.senior.data.PreferencesManager
import es.guardianos.senior.ui.MainActivity
import kotlin.math.sqrt

class FallDetectionService : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var countdownJob: Job? = null
    private var impactThreshold = 15.0f // valor por defecto; se sobreescribe al iniciar

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildServiceNotification())
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        // Leer la sensibilidad guardada por el usuario
        scope.launch {
            PreferencesManager.fallSensitivity(applicationContext).collect { value ->
                impactThreshold = value
            }
        }
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        scope.cancel()
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_LINEAR_ACCELERATION) return
        val magnitude = sqrt(
            event.values[0] * event.values[0] +
            event.values[1] * event.values[1] +
            event.values[2] * event.values[2]
        )
        if (magnitude > impactThreshold) {
            Log.d("FallDetection", "Impacto detectado: $magnitude")
            triggerPanicCountdown()
        }
    }

    private fun triggerPanicCountdown() {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            delay(10_000) // 10 segundos para cancelar
            if (isActive) activateEmergency()
        }
    }

    private fun activateEmergency() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:112")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(callIntent)
        } else {
            // Sin permiso: lanzar marcador en su lugar para que el usuario confirme
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(dialIntent)
        }
        stopSelf()
    }

    fun cancelPanic() {
        countdownJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildServiceNotification() = NotificationCompat.Builder(this, "fall_channel")
        .setContentTitle("🛡️ GuardianOS Senior")
        .setContentText("Protección activa. Pulsa para cancelar alarma.")
        .setSmallIcon(R.drawable.ic_emergency)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fall_channel", "Emergencia", NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
