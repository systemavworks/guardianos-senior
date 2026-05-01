package es.guardianos.senior.data
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "senior_config")

object PreferencesManager {
    private object Keys {
        val AUTO_ANSWER = booleanPreferencesKey("auto_answer")
        val AUTO_ANSWER_RINGS = intPreferencesKey("auto_answer_rings")
        val EMERGENCY_CONTACTS = stringSetPreferencesKey("emergency_contacts")
        val FALL_SENSITIVITY = floatPreferencesKey("fall_sensitivity")
        val MODE_LEVEL = intPreferencesKey("mode_level") // 0-3
        val CAREGIVER_PIN = stringPreferencesKey("caregiver_pin")
    }

    fun autoAnswer(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[Keys.AUTO_ANSWER] ?: false }

    fun emergencyContacts(context: Context): Flow<Set<String>> =
        context.dataStore.data.map { it[Keys.EMERGENCY_CONTACTS] ?: emptySet() }

    fun modeLevel(context: Context): Flow<Int> =
        context.dataStore.data.map { it[Keys.MODE_LEVEL] ?: 0 }

    fun fallSensitivity(context: Context): Flow<Float> =
        context.dataStore.data.map { it[Keys.FALL_SENSITIVITY] ?: 15.0f }

    fun caregiverPin(context: Context): Flow<String> =
        context.dataStore.data.map { it[Keys.CAREGIVER_PIN] ?: "1234" }

    suspend fun setCaregiverPin(context: Context, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CAREGIVER_PIN] = pin
        }
    }

    suspend fun update(context: Context, key: Preferences.Key<*>, value: Any) {
        context.dataStore.edit { prefs ->
            when (key) {
                Keys.AUTO_ANSWER -> prefs[Keys.AUTO_ANSWER] = value as Boolean
                Keys.AUTO_ANSWER_RINGS -> prefs[Keys.AUTO_ANSWER_RINGS] = value as Int
                Keys.EMERGENCY_CONTACTS -> prefs[Keys.EMERGENCY_CONTACTS] = value as Set<String>
                Keys.FALL_SENSITIVITY -> prefs[Keys.FALL_SENSITIVITY] = value as Float
                Keys.MODE_LEVEL -> prefs[Keys.MODE_LEVEL] = value as Int
                Keys.CAREGIVER_PIN -> prefs[Keys.CAREGIVER_PIN] = value as String
            }
        }
    }
}
