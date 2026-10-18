# Servicios y funcionalidades académicas

## Preferencias de notificaciones — primer avance

- El onboarding carga las categorías guardadas y las persiste al pulsar Continuar.
- La selección y la variante de presentación sobreviven a la recreación de la pantalla.
- Sin configuración previa se activan laboratorios y plazos; guardar una selección vacía mantiene todos los avisos desactivados.
- Los identificadores de categorías viven en `sharedLogic/domain/notifications`; títulos e iconos permanecen en Android.
- `NotificationPreferencesStore` guarda únicamente preferencias locales de la instalación, separadas de las credenciales. No sincroniza preferencias por cuenta.
- `SessionViewModel` expone la selección en su estado y recibe el evento de guardado desde el flujo existente.
- Las instalaciones que ya completaron el onboarding conservan su flujo y utilizan los valores predeterminados si aún no guardaron categorías.

### Verificación

Pruebas instrumentadas de valores iniciales, recreación del almacenamiento, reemplazo de selección, desactivación total e identificadores desconocidos.

Estado de validación (2026-10-17): `:androidApp:assembleDebug` y `:androidApp:assembleDebugAndroidTest` completados correctamente. La ejecución instrumentada inicial detectó un contexto de pruebas sin Application; se corrigió el almacenamiento para usar el contexto recibido sin retenerlo. La repetición de las cuatro pruebas y la comprobación visual de rotación/reinicio quedan pendientes: el dispositivo se desconectó y el usuario solicitó posponer las instalaciones hasta finalizar el trabajo.

Auditoría del cambio: sin errores de formato (`git diff --check`), referencias Kotlin y dependencias verificadas por compilación, sin cambios en drawables/manifest/permisos. Se conserva la tipografía y los componentes existentes. Los avisos de APIs obsoletas del proyecto y las pruebas iOS no disponibles en Windows son preexistentes. La persistencia Android sigue el patrón local simple de la sesión existente; no se añade un repositorio remoto ni un framework de inyección.

### Siguientes avances

1. Definir frecuencia, anticipación y horarios; gestionar permiso de notificaciones.
2. Programar, actualizar y cancelar avisos sin duplicados.

Este avance no solicita permisos ni envía notificaciones. No incorpora aún frecuencia u horarios.

## Edición después del onboarding

- Acceso: Inicio → campana → Preferencias de notificaciones.
- La edición reutiliza `OnboardingNotificationsScreen`, con Guardar cambios y Cancelar. El onboarding mantiene su botón Continuar.
- Se abre como diálogo de pantalla completa sobre la bandeja para conservar su estado al regresar y bloquear interacciones con el contenido de fondo.
- La selección inicial viene de `SessionUiState.enabledNotifications`; los cambios permanecen en un borrador local restaurable durante la edición.
- Guardar persiste mediante `SessionViewModel.saveNotificationPreferences` y cierra el editor. Cancelar o Atrás descartan el borrador sin escribir preferencias.
- Al volver a abrir se carga la última selección guardada, incluido el conjunto vacío.
- Vista previa en Android Studio: `NotificationPreferencesPreview`, dentro de `OnboardingNotificationsScreen.kt`. El preview permite revisar la selección y el diseño; no reproduce la navegación ni el guardado real de la aplicación.

### Verificación pendiente en dispositivo

1. Guardar una selección y comprobarla al reabrir y reiniciar la app.
2. Modificar y cancelar; comprobar que conserva la selección anterior.
3. Repetir usando Atrás en lugar de Cancelar.
4. Desactivar todas, guardar y reabrir.
5. Girar el dispositivo durante la edición y verificar el borrador.
6. Cambiar la vista de la bandeja y verificar que se conserva al cerrar preferencias.

Las instalaciones y pruebas conectadas permanecen pospuestas por indicación del usuario. No se introducen servicios de notificaciones ni nuevas dependencias para esta edición.
