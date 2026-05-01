# GuardianOS Senior Architecture

## 1. Objetivo

GuardianOS Senior es un launcher Android asistencial para personas mayores, con enfoque local-first, accesibilidad y flujos simplificados.

## 2. Capas del sistema

### 2.1 UI Layer

Ubicación principal: app/src/main/java/es/guardianos/senior/ui

Responsabilidades:
- Renderizado Compose de launcher y pantallas asistenciales.
- Actividades de entrada y navegación local.
- Flujo senior principal y acceso oculto de mantenimiento.

Componentes relevantes:
- MainActivity: host principal del launcher.
- HomeScreen (ui/scenes): acciones LLAMAR, FOTOS, WHATSAPP, AYUDANTE, EMERGENCIA.
- PhotoGalleryActivity + PhotoGalleryScreen: galería interna.
- SafeInboxActivity + SafeInboxScreen: bandeja simplificada de mensajes.
- ReminderPromptActivity: confirmación asistida de tareas.
- CaregiverSettingsActivity: panel de mantenimiento/cuidado protegido por PIN.

### 2.2 Domain/Service Layer

Ubicación principal: app/src/main/java/es/guardianos/senior/service

Responsabilidades:
- Orquestación de recordatorios y alertas.
- Detección de caídas y emergencia.
- TTS y notificaciones ruidosas.
- Escalado a cuidador en eventos críticos.

Componentes relevantes:
- MemoryReminderWorker: ciclo periódico de recordatorios y digest de compra.
- ReminderCoordinator: coordinación de confirmación, snooze y escalado.
- FallDetectionService: servicio foreground de detección de impacto.
- LoudNotificationHelper: canal/alertas críticas.
- CompanionSpeaker: voz TTS local.
- CaregiverNotifier: envío SMS de aviso.
- ReminderBroadcastReceiver y BootReceiver: arranque y disparadores.

### 2.3 Data Layer

Ubicación principal: app/src/main/java/es/guardianos/senior/data

Responsabilidades:
- Persistencia local de recordatorios, mensajes, cuidadores, auditoría y compra.
- Configuración ligera mediante DataStore.

Componentes relevantes:
- SeniorDatabase (Room) + DAOs.
- Entidades: ReminderEntity, MessageEntity, CaregiverEntity, ShoppingItemEntity, AuditLogEntity.
- PreferencesManager (DataStore): preferencias y PIN.
- ViewModels: MessageViewModel, CaregiverViewModel.

### 2.4 Integration Layer

Ubicación principal: app/src/main/java/es/guardianos/senior/integration

Responsabilidades:
- Integración con notificaciones de WhatsApp.
- Exportación local de auditoría CSV.

Componentes relevantes:
- WhatsAppSafeMonitor: NotificationListenerService.
- AuditIntegration: exportActivityLog y shareAuditFile.

## 3. Flujo principal de usuario (Senior)

1. MainActivity inicia HomeScreen.
2. HomeScreen ofrece accesos rápidos grandes.
3. Ayudante dispara ReminderPromptActivity y TTS.
4. Recordatorios programados se ejecutan por WorkManager y AlarmManager.
5. Si no hay confirmación, se incrementa missedCount y puede escalar por SMS.

## 4. Navegación y entradas

- Launcher principal: MainActivity con intent MAIN/HOME.
- Acceso de mantenimiento: pulsación larga en el título de Home.
- Navegación interna por Activities (sin dependencia de navegador ni cloud).

## 5. Persistencia y consistencia

- Room centraliza datos asistenciales críticos.
- DataStore guarda configuración operativa ligera.
- Estrategia actual de migración: fallbackToDestructiveMigration.

Nota: fallbackToDestructiveMigration simplifica desarrollo, pero implica pérdida de datos ante cambios de esquema.

## 6. Seguridad y privacidad

- Modelo local-first: sin backend obligatorio para uso base.
- allowBackup desactivado en Manifest.
- Permisos sensibles se solicitan en runtime desde panel de mantenimiento.

## 7. Build y toolchain

- Android Gradle Plugin: 8.7.3
- Gradle Wrapper: 8.9
- Kotlin: 2.0.21
- Compile/Target SDK: 35
- Min SDK: 26

## 8. Decisiones arquitectónicas actuales

- Prioridad a robustez de flujos senior sobre complejidad funcional.
- Cierre de flujos internos (fotos e inbox seguros dentro de la app).
- Separación clara por capas ui/service/data/integration.

## 9. Deuda técnica identificada

- Revisar migraciones Room para evitar borrado en upgrades.
- Reducir APIs deprecadas en notificaciones y SMS.
- Consolidar aún más la navegación hacia un único patrón (si aplica).
