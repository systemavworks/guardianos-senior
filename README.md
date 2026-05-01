# GuardianOS Senior

Launcher Android asistencial para personas mayores (+65), con enfoque local-first, accesibilidad y flujos simplificados de uso diario.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.01-4285F4.svg)](https://developer.android.com/jetpack/compose)

## Estado actual

El proyecto compila correctamente con wrapper local:

```bash
./gradlew :app:assembleDebug
```

Build validada en `main` el 1 de mayo de 2026.

## UX actual del launcher

Pantalla principal senior con accesos grandes:

- LLAMAR
- FOTOS
- WHATSAPP
- AYUDANTE
- EMERGENCIA

Acceso de mantenimiento (cuidador/configuración): pulsación larga sobre el título de la Home.

## Funcionalidades implementadas

### Senior (uso diario)

- Launcher simplificado con botones de gran tamaño.
- Llamada rápida con marcador del sistema.
- Galería interna (`PhotoGalleryActivity`) con permisos runtime y visualización Compose.
- Bandeja segura de mensajes (`SafeInboxActivity` + `SafeInboxScreen`).
- Ayudante de recordatorios con voz (TTS) y pantalla de confirmación (`ReminderPromptActivity`).
- Emergencia/detección de caídas (`FallDetectionService`).

### Asistencia cognitiva

- Recordatorios recurrentes por hora y día (`ReminderEntity.daysOfWeek`).
- Confirmación de tarea y opción de posponer (snooze).
- Escalado por recordatorio no confirmado (SMS al cuidador) si corresponde.
- Reorientación temporal y recordatorio diario de compra.

### Mantenimiento / cuidador

- Panel interno protegido por PIN (`CaregiverSettingsActivity`).
- Gestión de recordatorios (alta, edición, eliminación).
- Gestión de cuidadores (incluyendo cuidador principal).
- Lista de compra (pendiente/comprado).
- Solicitud guiada de permisos críticos (SMS y notificaciones).

## Arquitectura del proyecto

```text
app/src/main/java/es/guardianos/senior/
    data/         Room + DataStore + ViewModels
    service/      WorkManager, Alarm/Reminder, TTS, emergencia, notificaciones
    integration/  Monitor de notificaciones WhatsApp + export de auditoría
    ui/           Activities Compose
    ui/scenes/    Home principal
    ui/screens/   Pantallas de inbox y galería
    ui/theme/     Tema Compose
```

Persistencia local:

- Room (`SeniorDatabase`) para recordatorios, mensajes, cuidadores, auditoría y compra.
- DataStore (`PreferencesManager`) para configuración rápida y PIN.

## Stack técnico

- Android Gradle Plugin `8.7.3`
- Gradle Wrapper `8.9` (incluido en el repo)
- Kotlin `2.0.21`
- Jetpack Compose + Material3
- Room `2.6.1`
- DataStore Preferences `1.1.1`
- WorkManager `2.9.0`
- Min SDK `26`, Target/Compile SDK `35`

## Build y desarrollo

### Requisitos

- JDK 17
- Android SDK instalado y configurado

### Comandos principales

```bash
# Limpiar
./gradlew clean

# Compilar debug
./gradlew :app:assembleDebug

# Compilar release (sin firma final)
./gradlew :app:assembleRelease
```

## Permisos usados por la app

Declarados en [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml):

- `CALL_PHONE`
- `SEND_SMS`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_HEALTH`
- `RECEIVE_BOOT_COMPLETED`
- `WAKE_LOCK`
- `VIBRATE`
- `POST_NOTIFICATIONS`
- `READ_EXTERNAL_STORAGE` (maxSdk 32)
- `READ_MEDIA_IMAGES`

## Notas importantes

- El repositorio incluye wrapper local para evitar afectar otros proyectos del sistema.
- `docs/` y `scripts/` existen como estructura, actualmente sin contenido funcional.
- El flujo WhatsApp seguro está orientado a bandeja local simplificada; no hay envío de mensajes desde esa pantalla.

## Roadmap (realista)

- Estabilización técnica base: completada.
- Cierre de flujos internos del launcher (sin saltos externos innecesarios): completado en esta iteración.
- Mejora de robustez del ayudante (intents de voz cerrados y FSM): pendiente.
- Endurecimiento de fallback de permisos y UX de denegación persistente: pendiente.

## Licencia

Apache 2.0. Ver [LICENSE](LICENSE).
