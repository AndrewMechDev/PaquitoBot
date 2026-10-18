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

1. Permitir modificar las preferencias después del onboarding.
2. Definir frecuencia, anticipación y horarios; gestionar permiso de notificaciones.
3. Programar, actualizar y cancelar avisos sin duplicados.

Este avance no solicita permisos ni envía notificaciones. No incorpora aún frecuencia u horarios.
