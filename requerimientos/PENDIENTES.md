# Pendientes y deuda conocida

> Lista viva de lo que hay que resolver antes de repartir la app o cuando cambie el contexto. Actualizar al cerrar cada ítem. Última revisión: 2026-10-20 (rama `develop`).

## Seguridad

- [ ] **Revisar el historial de git en busca de secretos.** El repo es público (`AndrewMechDev/PaquitoBot`), así que todo lo que alguna vez se commiteó es visible. Buscar en todas las ramas y commits: `BACKEND_SECRET`, client secret de Google, API keys (MiniMax, Clerk `sk_`), tokens y contraseñas. Si aparece algo real, hay que rotarlo: borrar el commit no basta. `local.properties` está fuera de git, pero el historial anterior no se ha revisado.
  - Ya verificado: el Web client ID de Google (`google_web_client_id`) estuvo en `strings.xml` de `ui-auth-figma`, `canvas-mock-data` e `ios-mock-data`. Es un identificador público, no un secreto; no requiere acción. `develop` y `main` ya no lo tienen.
- [ ] **La clave de MiniMax queda dentro del APK debug** y se puede extraer. La variante release la compila vacía (`MINIMAX_API_KEY = ""`), así que el chat no funciona en release. Antes de repartir el chat hace falta un backend que guarde la clave y llame a MiniMax tras validar la sesión de Clerk (ver `MINIMAX_CHAT.md`).

## Antes de repartir la app

- [ ] **Firma de release.** `androidApp/build.gradle.kts` no define `signingConfig`, así que `assembleRelease` genera `androidApp-release-unsigned.apk`, que no se instala en un teléfono. Crear un keystore (guardado fuera de git) y configurarlo; para pruebas internas se podría firmar con la clave de debug.
- [ ] **Decidir cómo se distribuye a los ~10 usuarios:** APK debug (el chat funciona, pero la clave de MiniMax queda expuesta) o release firmado (el chat no funciona sin backend).
- [ ] **Revisar los límites del plan de Clerk** (usuarios activos e inicios de sesión). No se puede ver desde el repo.
- [ ] **Verificar el login en la variante debug.** El sufijo `.debug` cambia el package a `pe.tecsup.paquitobot.debug`. Hipótesis sin verificar: Clerk necesita ese package registrado para el flujo de SSO. Relacionado con el cierre de sesión que se vio al probar.
- [ ] **Borrar `androidApp/src/debug/AndroidManifest.xml`.** Solo habilita HTTP plano (`usesCleartextTraffic`) en debug para canvas-mock, que ya no existe.

## Calidad y pruebas

- [ ] **Avisos de lint** (0 errores, 50 avisos el 2026-10-20): 17 `NewerVersionAvailable`, 6 `UseKtx`, 5 `IconLauncherShape`, 4 `UnusedResources`, 3 `GradleDependency`, 2 `OldTargetApi`. Los dos más concretos:
  - `EmptySuperCall` en `CalendarViewModel.kt:178`: quitar `super.onCleared()`.
  - `DataExtractionRules` en `AndroidManifest.xml`: `allowBackup="false"` está obsoleto desde Android 12; agregar `android:dataExtractionRules`.
- [ ] **Cerrar sesión devuelve al inicio del onboarding.** `SessionViewModel` pone `isOnboardingComplete = false` al cerrar sesión. Confirmar si es lo deseado o si debe ir directo al login de Clerk.
- [ ] **Las pruebas instrumentadas necesitan preparación manual.** `AcademicNotificationsDeviceTest` exige una sesión de Clerk abierta, el onboarding terminado con una categoría activa y el permiso de notificaciones. Gradle desinstala la app al terminar, así que la sesión se pierde en cada corrida.

## Funcionalidad

- [ ] **Completar validación y cobertura de notificaciones.** Programador WorkManager implementado para entregas con fecha y resumen diario, con cancelación y registro antirrepetición. Pendientes: reglas y datos de laboratorios y asistencia, e historial real en la bandeja. Ver `SERVICIOS_ACADEMICOS.md` y `ACADEMIC_NOTIFICATIONS.md`.
- [ ] **Los datos académicos son de demostración.** Salen de los JSON de `androidApp/src/main/assets/academic/` (ver `ESCENARIOS_MOCK.md`). Cambiar a datos reales cuando exista la fuente.

## Verificado el 2026-10-20 (referencia)

- `assembleRelease` compila en `develop` con AGP 9.4.1 (5m31s).
- 76 pruebas unitarias en verde (38 de `androidApp`, 38 de `sharedLogic`) y lint sin errores, corridos en la rama `servicios-y-funcionalidades-academicas` antes de mergearla a `develop` (el merge no tuvo conflictos; no se repitieron sobre `develop`).
- 11 pruebas instrumentadas en verde en un Galaxy S10+ (las 2 de sesión, con la sesión preparada a mano), también sobre esa rama.
