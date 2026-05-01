package es.guardianos.senior.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CaregiverViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SeniorDatabase.getInstance(application)
    private val reminderDao = db.reminderDao()
    private val caregiverDao = db.caregiverDao()
    private val shoppingDao = db.shoppingDao()

    val reminders: StateFlow<List<ReminderEntity>> = reminderDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val caregivers: StateFlow<List<CaregiverEntity>> = caregiverDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = shoppingDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addReminder(
        content: String,
        type: ReminderType,
        hour: Int,
        minute: Int,
        daysOfWeek: String,
        requiresConfirmation: Boolean,
    ) {
        if (content.isBlank()) return
        viewModelScope.launch {
            reminderDao.insert(
                ReminderEntity(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    content = content.trim(),
                    hour = hour.coerceIn(0, 23),
                    minute = minute.coerceIn(0, 59),
                    daysOfWeek = daysOfWeek,
                    recurring = true,
                    requiresConfirmation = requiresConfirmation,
                )
            )
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            reminderDao.deleteById(id)
        }
    }

    fun updateReminder(
        id: String,
        content: String,
        type: ReminderType,
        hour: Int,
        minute: Int,
        daysOfWeek: String,
        requiresConfirmation: Boolean,
        previousMissedCount: Int = 0,
    ) {
        if (id.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            reminderDao.insert(
                ReminderEntity(
                    id = id,
                    type = type,
                    content = content.trim(),
                    hour = hour.coerceIn(0, 23),
                    minute = minute.coerceIn(0, 59),
                    daysOfWeek = daysOfWeek,
                    recurring = true,
                    requiresConfirmation = requiresConfirmation,
                    missedCount = previousMissedCount,
                )
            )
        }
    }

    fun addCaregiver(name: String, phone: String, isPrimary: Boolean) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            if (isPrimary) {
                caregivers.value.forEach { caregiver ->
                    if (caregiver.isPrimary) {
                        caregiverDao.upsert(caregiver.copy(isPrimary = false))
                    }
                }
            }

            caregiverDao.upsert(
                CaregiverEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    phone = phone.trim(),
                    isPrimary = isPrimary
                )
            )
        }
    }

    fun addShoppingItem(name: String, quantity: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            shoppingDao.upsert(
                ShoppingItemEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    quantity = quantity.trim().ifBlank { null }
                )
            )
        }
    }

    fun setBought(id: String, bought: Boolean) {
        viewModelScope.launch {
            shoppingDao.setBought(id, bought)
        }
    }

    fun removeShoppingItem(id: String) {
        viewModelScope.launch {
            shoppingDao.deleteById(id)
        }
    }
}
