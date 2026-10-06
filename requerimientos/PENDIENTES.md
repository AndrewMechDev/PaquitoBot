# Pendientes y deuda conocida

> Lista viva de lo que hay que resolver antes de liberar o cuando cambie el contexto. Actualizar al cerrar cada ítem. Última revisión: 2026-10-06 (rama `feature/canvas-mock-data`).

## Antes de un build release

- [ ] **canvas-mock no funciona en release (a propósito).** El permiso `usesCleartextTraffic` vive solo en `androidApp/src/debug/AndroidManifest.xml`, porque canvas-mock se consume por HTTP plano (`http://<IP-LAN>:8811`). Un build release bloquea ese tráfico, así que Cursos, Horarios y Home no cargarían datos de canvas-mock. Opciones cuando llegue el momento:
  - Desplegar canvas-mock con HTTPS (o usar la API real de Canvas) y cambiar `CANVAS_MOCK_BASE_URL`.
  - O retirar por completo el gate y los ViewModels de canvas-mock (`Academic*ViewModel`, `CanvasMockKeyStore`, `CanvasMockApi`) cuando haya Canvas real.
- [ ] **Registrar el package debug en Google.** El build debug usa `applicationIdSuffix = ".debug"`, es decir `pe.tecsup.paquitobot.debug`. Google Sign-In necesita un cliente OAuth tipo Android con ese package y el SHA-1 del keystore debug en Google Cloud Console. Sin eso, el login en debug puede fallar con `DEVELOPER_ERROR` o "No credentials available". Pendiente de verificar en dispositivo.
- [ ] **Sacar la IP LAN hardcodeada** de `CanvasMockHttpClient.kt` (`CANVAS_MOCK_BASE_URL`). Debería venir de `BuildConfig` o `local.properties`, o de un campo editable en el gate de conexión.
- [ ] **Retirar código muerto del gate de Canvas real.** `CanvasConnectScreen` y `CanvasConnectViewModel` quedaron sin uso en el flujo; se conservan solo para cuando haya integración real.

## Hallazgos de revisión de código sin arreglar (2026-10-06)

Revisión de `develop..HEAD`. Los tres hallazgos críticos ya están corregidos; quedan estos:

- [ ] **Datos vacíos que parecen reales.** `AcademicViewModel`, `AcademicCourseDetailViewModel`, `AcademicHomeViewModel` y `AcademicScheduleViewModel` usan `getOrDefault(emptyList())`. Si una llamada falla, la UI muestra "0 faltas" o "sin notas" sin avisar.
- [ ] **Un refresh fallido borra lo ya cargado** en Home y Horarios. `AcademicViewModel` sí conserva el estado previo; hay que unificar el comportamiento.
- [ ] **`isToday` incorrecto en Home.** `AcademicHomeViewModel` marca como "hoy" el primer día con eventos, no la fecha real del dispositivo.
- [ ] **"Pendientes" incluye entregas ya calificadas o vencidas** (`AcademicViewModel` y `AcademicHomeViewModel`): contadores, "próximo vencimiento" y urgencia. Filtrar por no calificadas y fecha >= hoy.
- [ ] **Eficiencia de red.** 3 llamadas en serie por curso y los mismos datos pedidos por 3 ViewModels. Usar `async`/`awaitAll` y un repositorio compartido con caché.
- [ ] **Copy de UI con voseo** ("Probá", "Intentá", "Tenés", "Conectá"). La app es para Perú: pasar a español neutro.

## Seguridad y almacenamiento

- [ ] **`SecureTokenStore` (JWT de Google) no tiene recuperación** si el archivo cifrado no se puede abrir. `allowBackup="false"` evita el caso del restore, pero no cubre corrupción ni borrado de credenciales de pantalla. `CanvasMockKeyStore` ya tiene esa recuperación; replicarla.
- [ ] `CanvasMockKeyStore.clear()` también borra el flag `skipped`. Hoy no se usa; revisar si se llega a exponer un "desconectar".

## Tooling

- [ ] **Revisión de Gentle AI abierta** (`review-7463355cc0835741`). Los revisores no pueden correr porque la organización (Tecsup) bloqueó el acceso por suscripción al CLI `claude`. Se hizo una revisión manual con `code-review` en su lugar. Cerrar con `gentle-ai review mode disable --scope clone` o resolver el acceso al CLI.
- [ ] El build release completo (`assembleRelease`) no se ha compilado tras el bump de AGP a 9.4.1; solo `assembleDebug` y la fusión del manifest release.
