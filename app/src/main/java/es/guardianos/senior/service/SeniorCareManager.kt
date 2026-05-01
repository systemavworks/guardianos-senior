package es.guardianos.senior.service

import android.content.Context
import es.guardianos.senior.data.CaregiverEntity
import es.guardianos.senior.data.ReminderEntity
import es.guardianos.senior.data.ReminderType
import es.guardianos.senior.data.SeniorDatabase
import es.guardianos.senior.data.ShoppingItemEntity

object SeniorCareManager {
    suspend fun seedDefaultPlan(context: Context) {
        val db = SeniorDatabase.getInstance(context)
        val reminderDao = db.reminderDao()
        val caregiverDao = db.caregiverDao()
        val shoppingDao = db.shoppingDao()

        caregiverDao.upsert(
            CaregiverEntity(
                id = "primary-caregiver",
                name = "Cuidador principal",
                phone = "600000000",
                isPrimary = true
            )
        )

        reminderDao.insert(
            ReminderEntity(
                id = "reminder-medication-0900",
                type = ReminderType.MEDICATION,
                content = "Tomar la medicacion de la manana con un vaso de agua",
                hour = 9,
                minute = 0,
                daysOfWeek = "1111111",
                recurring = true,
                requiresConfirmation = true
            )
        )

        reminderDao.insert(
            ReminderEntity(
                id = "reminder-appointment-1200",
                type = ReminderType.APPOINTMENT,
                content = "Revisar si hoy hay cita medica programada",
                hour = 12,
                minute = 0,
                daysOfWeek = "1111100",
                recurring = true,
                requiresConfirmation = true
            )
        )

        reminderDao.insert(
            ReminderEntity(
                id = "reminder-reorientation-1000",
                type = ReminderType.REORIENTATION,
                content = "Orientacion diaria",
                hour = 10,
                minute = 0,
                daysOfWeek = "1111111",
                recurring = true,
                requiresConfirmation = false
            )
        )

        shoppingDao.upsert(
            ShoppingItemEntity(
                id = "shopping-milk",
                name = "Leche",
                quantity = "2 litros"
            )
        )
        shoppingDao.upsert(
            ShoppingItemEntity(
                id = "shopping-bread",
                name = "Pan integral",
                quantity = "1 unidad"
            )
        )
    }
}
