# 🛡️ GuardianOS Senior
> Launcher asistencial integral para personas mayores. Accesibilidad adaptativa, seguridad proactiva y 100% privacidad local.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.01-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Privacy-Local%20First-00C853.svg)](#)

---

## 📖 Descripción
**GuardianOS Senior** no es solo un launcher con iconos grandes. Es un **sistema de vida asistida integral** diseñado desde cero para personas con dificultad visual, auditiva, motriz o cognitiva. 

Mientras el mercado ofrece soluciones estáticas y sobrecargadas, GuardianOS Senior implementa una **filosofía de "cero confusión"**: interfaz de una sola capa, complejidad progresiva (niveles 0-3), detección automática de emergencias, recordatorios contextuales y un ecosistema familiar opcional, **todo ejecutándose localmente sin rastro en la nube**.

### 🎯 Filosofía de Diseño
- ✅ **Privacidad por defecto**: Sin analytics, sin tracking, sin cuentas obligatorias
- ✅ **Accesibilidad real**: Adaptación dinámica a visión, audición y motricidad
- ✅ **Seguridad proactiva**: Detección de caídas, botón de pánico, notificaciones bypass-silencio
- ✅ **Soporte cognitivo**: Recordatorios inteligentes, reorientación temporal, modo Alzheimer
- ✅ **Ética abierta**: Código libre (Apache 2.0), modelo freemium justo, sin vendor lock-in

---

## ✨ Características Principales

| Módulo | Descripción |
|--------|-------------|
| 📱 **Launcher Progresivo** | Niveles 0-3: desde "solo llamar/descolgar" hasta funciones completas configurables vía QR |
| 🚨 **Detección de Caídas** | Acelerómetro lineal + countdown de 10s. Si no se cancela, activa emergencia automática |
| 🔔 **Notificaciones Ruidosas** | Ignoran modo silencio, patrones hápticos distintivos y volumen forzado para alertas críticas |
| 💬 **WhatsApp Seguro** | Modo solo-lectura: recibe mensajes/audios, sin teclado ni envío accidental. Bandeja accesible |
| 🖼️ **Galería Familiar** | Visualización gigante, zoom táctil 5x, slideshow automático, etiquetas de voz opcionales |
| 🧠 **Recuerdos & Alzheimer** | Recordatorios contextuales, reorientación horaria, rutinas guiadas, validación de cumplimiento |
| 🔐 **Auditoría Local** | Exportación cifrada de logs de actividad para cuidadores (CSV). Sin subida automática a cloud |
| 🌐 **Configuración QR** | Escaneo inicial por cuidador: define contactos, apps permitidas, umbrales y nivel de complejidad |

---

## 🏗️ Arquitectura & Stack

```
guardianos-senior/
├── app/
│   ├── src/main/java/es/guardianos/senior/
│   │   ├── ui/                 # Compose UI + Pantallas accesibles
│   │   ├── service/            # FallDetection, LoudNotifications, WorkManager
│   │   ├── data/               # Room DB + DataStore Preferences
│   │   └── integration/        # WhatsApp Monitor + Audit Export
│   └── build.gradle.kts        # Kotlin 2.0.21, Compose BOM, Room, KSP
├── fastlane/                   # Metadata para F-Droid/Play Store
├── scripts/                    # build.sh, install.sh, backup.sh (terminal-friendly)
└── docs/                       # Guías accesibles (pictogramas, audio, subtítulos)
```

**Tecnologías Clave:**
- `Kotlin 2.0.21` + `Jetpack Compose` (Material 3, alto contraste)
- `Room 2.6.1` + `DataStore` (persistencia local cifrada)
- `WorkManager` + `AlarmManagerCompat` (recordatorios precisos)
- `SensorManager` (detección de caídas sin ML externo)
- `NotificationListenerService` (monitorización segura de WhatsApp)
- `Android SDK 35` / `Min SDK 26` (compatible con ~98% dispositivos)

---

## 🚀 Instalación & Desarrollo (Terminal)

### 📦 Requisitos
- JDK 17+
- Android SDK 35 (compile/target), 26 (min)
- `git`, `curl`, `adb`
- Editor: VS Code + Extensiones Kotlin/Compose, o Android Studio

### 🛠️ Setup Rápido
```bash
# 1. Clonar/Inicializar (si no usaste el setup script)
git clone https://github.com/tu-user/guardianos-senior.git
cd guardianos-senior

# 2. Dar permisos a scripts
chmod +x scripts/*.sh

# 3. Build & Install en dispositivo conectado
./scripts/build.sh
./scripts/install.sh

# 4. Ver logs en tiempo real
adb logcat | grep -E "guardianos.senior|FallDetection|LoudNotification"
```

> 💡 **Testing sin Android Studio**: Usa `scrcpy` para espejar y controlar el dispositivo desde ZorinOS:
> ```bash
> sudo apt install scrcpy
> scrcpy --turn-screen-off --stay-awake
> ```

---

## ⚙️ Configuración & Uso

### 🔑 Permisos Requeridos
| Permiso | Motivo |
|---------|--------|
| `CALL_PHONE` | Llamada directa a contactos/emergencias |
| `SEND_SMS` | Envío de ubicación a contactos de confianza |
| `ACCESS_FINE_LOCATION` | Geolocalización en alertas de emergencia |
| `ACTIVITY_RECOGNITION` | Detección de caídas/inmovilidad |
| `POST_NOTIFICATIONS` | Alertas ruidosas en Android 13+ |
| `READ_MEDIA_IMAGES` | Acceso a fotos familiares (sin edición) |

### 📲 Flujo de Configuración
1. Instalar app en dispositivo del mayor
2. Generar QR desde `guardianos.es/senior-config` (o app cuidador)
3. Escanear QR → aplicación automática de nivel, contactos y apps permitidas
4. Activar manualmente: `Ajustes > Accesibilidad > Acceso a notificaciones` (para WhatsApp)
5. Verificar detección de caídas con `Ajustes > GuardianOS > Test Sensores`

---

## 🔒 Privacidad & Seguridad

- 🚫 **Sin Analytics**: Ni Firebase, ni Crashlytics, ni telemetry
- 🗄️ **Almacenamiento Local**: Room + DataStore en dispositivo. Sin cloud por defecto
- 🔐 **Cifrado Opcional**: SQLCipher disponible para datos médicos/sensibles
- 📤 **Consentimiento Explícito**: Los logs de actividad solo se comparten vía export manual o aprobación del usuario
- 🛡️ `android:allowBackup="false"` + `dataExtractionRules` estrictos
- ✅ Compatible con `guardianos-audit` para verificación de cumplimiento de privacidad

---

## 🔗 Integración con Ecosistema GuardianOS

| Proyecto | Sinergia |
|----------|----------|
| `guardianos-shield` | Reutiliza capa de seguridad, DNS filtering para bloqueo de estafas, DevicePolicyManager |
| `guardianos-audit` | SaaS-Plataforma de Auditoria en ciberseguridad. Exportación de logs de actividad, generación de reportes PDF/CSV para cuidadores/instituciones |
| `guardianos-web` | Dashboard opcional para gestión multi-dispositivo (residencias, ayuntamientos) |

> 🔄 Todos los componentes son **independientes**. Senior funciona perfectamente de forma aislada.

---

## 📅 Roadmap

| Fase | Estado | Entregables |
|------|--------|-------------|
| ✅ MVP | Completado | Launcher básico, botón pánico, detección caídas, QR config |
| 🟡 Fase 2 | En desarrollo | WhatsApp seguro, galería, recordatorios, notificaciones ruidosas |
| ⚪ Fase 3 | Planificado | Voice commands locales, dashboard cuidador web, validación clínica |
| ⚪ Fase 4 | Futuro | Integración con wearables, IA de patrones de uso (on-device), F-Droid |

---

## 🤝 Contribuir

Este proyecto sigue la licencia **Apache 2.0**. Aceptamos PRs, reportes de bugs y sugerencias de accesibilidad.

```bash
1. Fork el repositorio
2. Crea tu rama (`git checkout -b feature/accesibilidad-mejorada`)
3. Commit tus cambios (`git commit -m '♿️ Mejora contraste en modo baja visión'`)
4. Push a la rama (`git push origin feature/accesibilidad-mejorada`)
5. Abre un Pull Request
```

> 📜 **Normas éticas**: 
> - Ningún PR que añada tracking, analytics o cloud obligatorio
> - Toda función de accesibilidad debe pasar pruebas con usuarios reales
> - Documentación obligatoria en formato accesible (texto plano + pictogramas)

---

## 📄 Licencia

```
Copyright 2026 Av Works System [@systemavworks]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

📩 **Contacto & Soporte**: `info@guardianos.es` | 🌐 `https://guardianos.es`  
🛡️ *Por una tecnología que protege, no que explota.*
```
