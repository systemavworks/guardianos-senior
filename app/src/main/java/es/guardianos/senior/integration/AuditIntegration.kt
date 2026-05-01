package es.guardianos.senior.integration

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import es.guardianos.senior.data.*
import es.guardianos.senior.data.SeniorDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object AuditIntegration {
    private const val EXPORT_DIR = "audit_exports"
    private const val AUTHORITY = "es.guardianos.senior.fileprovider"

    suspend fun exportActivityLog(context: Context, days: Int = 30): String = withContext(Dispatchers.IO) {
        val db = SeniorDatabase.getInstance(context)
        val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        val logs = db.auditDao().getRecentLogs(since)
        
        val dir = File(context.filesDir, EXPORT_DIR).apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "guardian_senior_audit_$timestamp.csv")
        
        FileWriter(file).use { writer ->
            writer.appendLine("Timestamp,Tipo,Detalle,Ubicación,Confirmado")
            logs.forEach { log ->
                writer.appendLine("${log.timestamp},${log.type},${log.details?.replace(",", ";")},${log.location ?: "N/A"},${log.confirmed}")
            }
        }
        file.absolutePath
    }

    fun shareAuditFile(context: Context, filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte GuardianOS Senior")
            putExtra(Intent.EXTRA_TEXT, "Adjunto registro de actividad de los últimos días.")
        }
        context.startActivity(Intent.createChooser(intent, "Enviar a Cuidador"))
    }

    // Stub para integración futura con guardianos-audit
    suspend fun syncWithAuditServer(context: Context, encryptedPayload: ByteArray) {
        // TODO: Implementar subida cifrada vía Retrofit/OkHttp a tu backend de Audit
        // Mantener local-first hasta que el usuario autorice explícitamente la sincronización
    }
}
