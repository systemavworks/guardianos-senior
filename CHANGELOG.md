# Changelog

Todas las mejoras relevantes del proyecto se documentan aquí.

## [Unreleased]

### Planned
- Endurecimiento de migraciones Room sin pérdida de datos.
- Revisión de APIs deprecadas (SMS y audio routing).
- Mejora de resiliencia en flujos de permisos denegados.

## [1.0.0] - 2026-05-01

### Added
- Launcher senior con acciones principales: LLAMAR, FOTOS, WHATSAPP, AYUDANTE, EMERGENCIA.
- Acceso oculto de mantenimiento por pulsación larga en título.
- Panel de mantenimiento/cuidado con PIN.
- Gestión de recordatorios con alta, edición y eliminación.
- Gestión de cuidadores con cuidador principal.
- Gestión de lista de compra (pendiente/comprado).
- Integración de voz TTS para el ayudante virtual.
- Pantalla de confirmación de recordatorio con timeout y snooze.
- Escalado por SMS a cuidador ante no confirmación repetida.
- Bandeja segura de mensajes y actividad dedicada.
- Galería interna con actividad dedicada y permisos runtime.
- Worker periódico para recordatorios y digest de compra.
- Receiver de boot y receiver de recordatorios exactos.
- Exportación de actividad local en CSV.
- Wrapper de Gradle propio del repositorio (8.9).

### Changed
- Estabilización de dependencias AndroidX con versiones explícitas.
- Corrección de imports y paquetes para coherencia de arquitectura.
- Cierre de flujos internos del launcher para evitar saltos externos innecesarios.
- Actualización de README al estado real del proyecto.

### Fixed
- Errores de compilación por KSP/dependencias sin versión.
- Errores de recursos de icono en Manifest.
- Errores de compilación Kotlin en PreferencesManager e imports de tema.
- Incoherencias de navegación entre Home y actividades internas.

### Build
- Build verificada con:
  - ./gradlew clean
  - ./gradlew :app:assembleDebug

[Unreleased]: https://github.com/systemavworks/guardianos-senior/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/systemavworks/guardianos-senior/releases/tag/v1.0.0
