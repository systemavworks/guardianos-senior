package es.guardianos.senior.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import es.guardianos.senior.data.CaregiverViewModel
import es.guardianos.senior.data.PreferencesManager
import es.guardianos.senior.data.ReminderType
import es.guardianos.senior.ui.theme.GuardianOSSeniorTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions

class CaregiverSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuardianOSSeniorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CaregiverSettingsRoute()
                }
            }
        }
    }
}

@Composable
private fun CaregiverSettingsRoute(vm: CaregiverViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val pin by PreferencesManager.caregiverPin(context).collectAsStateWithLifecycle(initialValue = "1234")
    var inputPin by remember { mutableStateOf("") }
    var unlocked by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }

    if (!unlocked) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("MODO CUIDADOR", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = inputPin,
                onValueChange = { inputPin = it.take(6) },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            if (pinError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("PIN incorrecto", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = {
                if (inputPin == pin) {
                    unlocked = true
                    pinError = false
                } else {
                    pinError = true
                }
            }) {
                Text("ENTRAR")
            }
        }
        return
    }

    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val caregivers by vm.caregivers.collectAsStateWithLifecycle()
    val shopping by vm.shoppingItems.collectAsStateWithLifecycle()

    var reminderText by remember { mutableStateOf("") }
    var reminderHour by remember { mutableStateOf("9") }
    var reminderMinute by remember { mutableStateOf("0") }
    var reminderType by remember { mutableStateOf(ReminderType.MEDICATION) }
    var requiresConfirmation by remember { mutableStateOf(true) }
    val selectedDays = remember { mutableStateListOf(true, true, true, true, true, true, true) }
    var editingReminderId by remember { mutableStateOf<String?>(null) }
    var editingPreviousMissedCount by remember { mutableStateOf(0) }

    var caregiverName by remember { mutableStateOf("") }
    var caregiverPhone by remember { mutableStateOf("") }
    var caregiverPrimary by remember { mutableStateOf(false) }

    var shoppingName by remember { mutableStateOf("") }
    var shoppingQty by remember { mutableStateOf("") }
    var guidedMode by remember { mutableStateOf(true) }

    val requestSmsPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }
    val requestNotificationsPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val smsGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val pendingShoppingCount = shopping.count { !it.bought }
    val secureReadinessScore = listOf(smsGranted, notificationsGranted).count { it } / 2f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Panel de cuidador", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Resumen rapido del estado diario",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsCard(
                    title = "Recordatorios",
                    value = reminders.size.toString(),
                    subtitle = "Activos",
                    color = Color(0xFF355C7D),
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Cuidadores",
                    value = caregivers.size.toString(),
                    subtitle = "Configurados",
                    color = Color(0xFF2A9D8F),
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Compra",
                    value = pendingShoppingCount.toString(),
                    subtitle = "Pendientes",
                    color = Color(0xFFE76F51),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F3B57))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Estado de proteccion",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { secureReadinessScore },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF7BD389),
                        trackColor = Color(0xFF2F5D8A)
                    )
                    Text(
                        if (secureReadinessScore >= 1f) "Sistema preparado para alertas" else "Faltan permisos para alertas completas",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF4FF))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Vista")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SeniorActionButton(
                            text = if (guidedMode) "Modo guiado activo" else "Activar modo guiado",
                            onClick = { guidedMode = true },
                            modifier = Modifier.weight(1f)
                        )
                        SeniorActionButton(
                            text = if (!guidedMode) "Modo completo activo" else "Modo completo",
                            onClick = { guidedMode = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = if (guidedMode) {
                            "Modo guiado: menos opciones y botones grandes. Ideal para uso rapido."
                        } else {
                            "Modo completo: muestra todos los bloques de configuracion."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Acciones rapidas")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SeniorActionButton(
                            text = "Plantilla medicacion",
                            onClick = {
                                reminderType = ReminderType.MEDICATION
                                reminderText = "Tomar la medicacion ahora"
                                requiresConfirmation = true
                                reminderHour = "9"
                                reminderMinute = "0"
                                (0..6).forEach { selectedDays[it] = true }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SeniorActionButton(
                            text = "Plantilla comida",
                            onClick = {
                                reminderType = ReminderType.MEAL
                                reminderText = "Es hora de comer"
                                requiresConfirmation = true
                                reminderHour = "14"
                                reminderMinute = "0"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SeniorActionButton(
                            text = "Plantilla cita medica",
                            onClick = {
                                reminderType = ReminderType.APPOINTMENT
                                reminderText = "Tienes cita medica hoy"
                                requiresConfirmation = true
                                reminderHour = "12"
                                reminderMinute = "0"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SeniorActionButton(
                            text = "Plantilla orientacion",
                            onClick = {
                                reminderType = ReminderType.REORIENTATION
                                reminderText = "Reorientacion diaria"
                                requiresConfirmation = false
                                reminderHour = "10"
                                reminderMinute = "0"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Permisos importantes")
                    Text("SMS: ${if (smsGranted) "Concedido" else "Pendiente"}")
                    SeniorActionButton(onClick = {
                        if (!smsGranted) {
                            requestSmsPermission.launch(Manifest.permission.SEND_SMS)
                        }
                    }) {
                        Text(if (smsGranted) "SMS listo" else "Conceder SMS")
                    }

                    Text("Notificaciones: ${if (notificationsGranted) "Concedido" else "Pendiente"}")
                    SeniorActionButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsGranted) {
                            requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }) {
                        Text(if (notificationsGranted) "Notificaciones listas" else "Conceder notificaciones")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0FF))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Cambiar PIN")
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it.take(6) },
                        label = { Text("Nuevo PIN (4-6 digitos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    SeniorActionButton(onClick = {
                        if (newPin.length in 4..6) {
                            scope.launch {
                                PreferencesManager.setCaregiverPin(context, newPin)
                                newPin = ""
                            }
                        }
                    }) {
                        Text("Guardar PIN")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(if (editingReminderId == null) "Nuevo recordatorio" else "Editar recordatorio")
                    OutlinedTextField(
                        value = reminderText,
                        onValueChange = { reminderText = it },
                        label = { Text("Texto del recordatorio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = reminderHour,
                            onValueChange = { reminderHour = it.filter { ch -> ch.isDigit() }.take(2) },
                            label = { Text("Hora") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reminderMinute,
                            onValueChange = { reminderMinute = it.filter { ch -> ch.isDigit() }.take(2) },
                            label = { Text("Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Tipo")
                    if (guidedMode) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SeniorActionButton(
                                text = "Medicacion",
                                onClick = { reminderType = ReminderType.MEDICATION },
                                modifier = Modifier.weight(1f)
                            )
                            SeniorActionButton(
                                text = "Comida",
                                onClick = { reminderType = ReminderType.MEAL },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SeniorActionButton(
                                text = "Cita",
                                onClick = { reminderType = ReminderType.APPOINTMENT },
                                modifier = Modifier.weight(1f)
                            )
                            SeniorActionButton(
                                text = "Orientacion",
                                onClick = { reminderType = ReminderType.REORIENTATION },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { reminderType = ReminderType.MEDICATION }) { Text("Med") }
                            Button(onClick = { reminderType = ReminderType.MEAL }) { Text("Comida") }
                            Button(onClick = { reminderType = ReminderType.APPOINTMENT }) { Text("Cita") }
                            Button(onClick = { reminderType = ReminderType.REORIENTATION }) { Text("Orient") }
                        }
                    }

                    Text("Dias activos")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        DayToggle("L", selectedDays, 0)
                        DayToggle("M", selectedDays, 1)
                        DayToggle("X", selectedDays, 2)
                        DayToggle("J", selectedDays, 3)
                        DayToggle("V", selectedDays, 4)
                        DayToggle("S", selectedDays, 5)
                        DayToggle("D", selectedDays, 6)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = requiresConfirmation,
                            onCheckedChange = { requiresConfirmation = it }
                        )
                        Text("Requiere confirmacion")
                    }

                    SeniorActionButton(onClick = {
                        val days = selectedDays.joinToString(separator = "") { if (it) "1" else "0" }

                        val editId = editingReminderId
                        if (editId == null) {
                            vm.addReminder(
                                content = reminderText,
                                type = reminderType,
                                hour = reminderHour.toIntOrNull() ?: 9,
                                minute = reminderMinute.toIntOrNull() ?: 0,
                                daysOfWeek = days,
                                requiresConfirmation = requiresConfirmation,
                            )
                        } else {
                            vm.updateReminder(
                                id = editId,
                                content = reminderText,
                                type = reminderType,
                                hour = reminderHour.toIntOrNull() ?: 9,
                                minute = reminderMinute.toIntOrNull() ?: 0,
                                daysOfWeek = days,
                                requiresConfirmation = requiresConfirmation,
                                previousMissedCount = editingPreviousMissedCount,
                            )
                        }

                        reminderText = ""
                        reminderHour = "9"
                        reminderMinute = "0"
                        reminderType = ReminderType.MEDICATION
                        requiresConfirmation = true
                        (0..6).forEach { selectedDays[it] = true }
                        editingReminderId = null
                        editingPreviousMissedCount = 0
                    }) {
                        Text(if (editingReminderId == null) "Anadir recordatorio" else "Guardar cambios")
                    }

                    if (editingReminderId != null) {
                        SeniorActionButton(onClick = {
                            reminderText = ""
                            reminderHour = "9"
                            reminderMinute = "0"
                            reminderType = ReminderType.MEDICATION
                            requiresConfirmation = true
                            (0..6).forEach { selectedDays[it] = true }
                            editingReminderId = null
                            editingPreviousMissedCount = 0
                        }) {
                            Text("Cancelar edicion")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recordatorios activos")
                    reminders.forEach { reminder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')} - ${reminder.content}",
                                modifier = Modifier.weight(1f),
                                fontSize = if (guidedMode) 20.sp else 16.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SeniorActionButton(onClick = {
                                    editingReminderId = reminder.id
                                    editingPreviousMissedCount = reminder.missedCount
                                    reminderText = reminder.content
                                    reminderHour = reminder.hour.toString()
                                    reminderMinute = reminder.minute.toString()
                                    reminderType = reminder.type
                                    requiresConfirmation = reminder.requiresConfirmation
                                    reminder.daysOfWeek.forEachIndexed { index, c ->
                                        if (index in 0..6) selectedDays[index] = c == '1'
                                    }
                                }) {
                                    Text("Editar")
                                }
                                SeniorActionButton(onClick = { vm.deleteReminder(reminder.id) }) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Contactos cuidadores")
                    OutlinedTextField(
                        value = caregiverName,
                        onValueChange = { caregiverName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = caregiverPhone,
                        onValueChange = { caregiverPhone = it },
                        label = { Text("Telefono") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = caregiverPrimary,
                            onCheckedChange = { caregiverPrimary = it }
                        )
                        Text("Principal")
                    }
                    SeniorActionButton(onClick = {
                        vm.addCaregiver(caregiverName, caregiverPhone, caregiverPrimary)
                        caregiverName = ""
                        caregiverPhone = ""
                        caregiverPrimary = false
                    }) {
                        Text("Anadir cuidador")
                    }

                    caregivers.forEach { caregiver ->
                        Text(
                            "${if (caregiver.isPrimary) "[Principal] " else ""}${caregiver.name} - ${caregiver.phone}",
                            fontSize = if (guidedMode) 20.sp else 16.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Lista de compra")
                    OutlinedTextField(
                        value = shoppingName,
                        onValueChange = { shoppingName = it },
                        label = { Text("Producto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shoppingQty,
                        onValueChange = { shoppingQty = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SeniorActionButton(onClick = {
                        vm.addShoppingItem(shoppingName, shoppingQty)
                        shoppingName = ""
                        shoppingQty = ""
                    }) {
                        Text("Anadir producto")
                    }

                    shopping.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${if (item.bought) "[Comprado] " else ""}${item.name} ${item.quantity ?: ""}")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SeniorActionButton(onClick = { vm.setBought(item.id, !item.bought) }) {
                                    Text(if (item.bought) "Pendiente" else "Comprado")
                                }
                                SeniorActionButton(onClick = { vm.removeShoppingItem(item.id) }) {
                                    Text("Borrar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1F2937)
    )
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, color = Color.White, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SeniorActionButton(
    text: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text.orEmpty(),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayToggle(label: String, selectedDays: MutableList<Boolean>, index: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = selectedDays[index],
            onCheckedChange = { checked -> selectedDays[index] = checked }
        )
        Text(label)
    }
}
