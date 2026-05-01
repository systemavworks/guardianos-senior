package es.guardianos.senior.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val type: ReminderType,
    val content: String,
    val hour: Int = 0,
    val minute: Int = 0,
    val daysOfWeek: String = "1111111", // lunes..domingo, 1 = activo
    val recurring: Boolean = true,
    val lastTriggeredDate: String? = null, // yyyy-MM-dd
    val requiresConfirmation: Boolean = true,
    val confirmedAt: Long? = null,
    val missedCount: Int = 0
)

@Entity(tableName = "caregivers")
data class CaregiverEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val notifyMissedReminders: Boolean = true,
    val notifyFallDetection: Boolean = true,
    val notifyLowBattery: Boolean = false,
    val prefersSms: Boolean = true,
    val isPrimary: Boolean = false
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: String? = null,
    val addedBy: String = "caregiver",
    val bought: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String,
    val details: String?,
    val location: String?,
    val confirmed: Boolean = false
)

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE hour = :hour
          AND minute = :minute
          AND substr(daysOfWeek, :dayIndex + 1, 1) = '1'
          AND (lastTriggeredDate IS NULL OR lastTriggeredDate != :today)
        """
    )
    suspend fun getPendingReminders(hour: Int, minute: Int, dayIndex: Int, today: String): List<ReminderEntity>

    @Query("UPDATE reminders SET lastTriggeredDate = :today WHERE id = :id")
    suspend fun markTriggeredToday(id: String, today: String)

    @Query("UPDATE reminders SET confirmedAt = :timestamp, missedCount = 0 WHERE id = :id")
    suspend fun markConfirmed(id: String, timestamp: Long)

    @Query("UPDATE reminders SET missedCount = missedCount + 1 WHERE id = :id")
    suspend fun incrementMissed(id: String)

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE missedCount >= :threshold")
    suspend fun getMissed(threshold: Int = 2): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface CaregiverDao {
    @Query("SELECT * FROM caregivers ORDER BY isPrimary DESC, name ASC")
    fun getAll(): Flow<List<CaregiverEntity>>

    @Query("SELECT * FROM caregivers WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimary(): CaregiverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(caregiver: CaregiverEntity)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items WHERE bought = 0 ORDER BY addedAt DESC")
    fun getPending(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items ORDER BY bought ASC, addedAt DESC")
    fun getAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE bought = 0 ORDER BY addedAt DESC LIMIT :limit")
    suspend fun getPendingNow(limit: Int = 5): List<ShoppingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET bought = :bought WHERE id = :id")
    suspend fun setBought(id: String, bought: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getRecentLogs(since: Long): List<AuditLogEntity>

    @Query("SELECT COUNT(*) FROM audit_logs WHERE type = :type AND details = :details")
    suspend fun countByTypeAndDetails(type: String, details: String): Int

    @Insert
    suspend fun insert(log: AuditLogEntity)
}

enum class ReminderType(val label: String) {
    MEDICATION("Medicación"), MEAL("Comida"), APPOINTMENT("Cita"),
    HYGIENE("Higiene"), REORIENTATION("Reorientación Temporal"), SAFETY("Seguridad")
}

@Database(
    entities = [
        ReminderEntity::class,
        MessageEntity::class,
        AuditLogEntity::class,
        CaregiverEntity::class,
        ShoppingItemEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class SeniorDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun messageDao(): MessageDao
    abstract fun auditDao(): AuditDao
    abstract fun caregiverDao(): CaregiverDao
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile private var INSTANCE: SeniorDatabase? = null
        fun getInstance(context: Context): SeniorDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context, SeniorDatabase::class.java, "senior_db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
