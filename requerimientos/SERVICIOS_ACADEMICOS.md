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

### Diseño de controles de horario

Las anticipaciones se eligen mediante chips visibles que se distribuyen en varias filas si es necesario. El resumen y la hora usan las superficies y radios de PaquitoBot; la selección se marca con celeste y texto oscuro. El selector de hora ahora es Compose, con DM Sans y paleta local, reloj y entrada por teclado, Confirmar hora y Cancelar. El valor solo se cambia al confirmar; cerrar el selector mantiene la hora anterior. El horario completo sigue siendo un borrador hasta Guardar cambios/Continuar. El contenido del diálogo permite desplazamiento en pantallas de menor altura.

Validación del rediseño: compilación Android y revisión de referencias/tokens; sin nuevas dependencias ni instalación. Pendiente de revisión visual en Android Studio y pruebas de interacción en dispositivo.

### Flujo progresivo de configuración

Para evitar reunir todas las decisiones en una sola pantalla, el selector ahora tiene tres pasos:

1. Categorías de avisos, manteniendo la elección cuadros/lista.
2. Anticipación de los recordatorios de entrega.
3. Resumen diario, selector de hora y permiso de Android.

El avance requiere pulsar Continuar: elegir una categoría u horario no cambia de etapa automáticamente. Se muestra el paso actual y una barra de progreso celeste. La transición combina un desplazamiento corto y desvanecimiento; al usar Atrás se invierte la dirección. Etapa, categorías, variante y horario se mantienen como borrador restaurable fuera del contenido animado.

Solo Finalizar (onboarding) o Guardar cambios (edición) persisten la configuración. Atrás dentro del flujo conserva el borrador; Cancelar descarta toda la edición. El botón de sistema Atrás navega entre etapas durante el onboarding; en el diálogo de edición conserva el comportamiento de cancelar la edición completa. El permiso de Android sigue siendo independiente del guardado.

Validación: compilación Android correcta y revisión de rutas de guardado; pendientes en dispositivo/preview interactivo: recorrer los tres pasos, volver y comprobar elecciones, cancelar sin guardar, reiniciar y girar durante la edición, y revisar espaciado con fuentes grandes. Se mantienen pospuestas las instalaciones por indicación del usuario.

## Reglas y permiso de notificaciones

Reglas iniciales aceptadas por el usuario: recordar entregas 24 horas antes y resumen diario a las 08:00, según la zona horaria del dispositivo.

- Configuración en onboarding y edición: anticipación de 1, 6, 12, 24 o 48 horas; activar/desactivar el resumen y elegir su hora/minuto.
- `NotificationSchedule` vive en dominio compartido. Categorías y horario se guardan juntos mediante `NotificationPreferencesStore`; el borrador se descarta al cancelar o pulsar Atrás.
- Instalaciones anteriores sin horario guardado reciben los valores 24 horas / 08:00. Valores numéricos fuera de rango se sustituyen por los predeterminados.
- `NotificationPermissionSection` informa si Android permite notificaciones. Solicita permiso en Android 13+ únicamente al pulsar el botón, permite abrir ajustes y actualiza el estado al regresar.
- La autorización de Android es independiente del borrador: cancelar no revoca un permiso que ya se concedió. Rechazarlo no impide guardar las preferencias.
- Todas las categorías desactivadas deben impedir tanto recordatorios como resumen en el futuro programador.
- No se garantiza ejecución al minuto: la estrategia de trabajo en segundo plano debe respetar las restricciones del sistema.

### Dependencias de datos y siguiente avance

La API ofrece fechas de entrega, notas, sesiones de clase y asistencias. No expone un tipo de asignación que identifique laboratorios ni un estado explícito de entrega del estudiante. No se deben clasificar laboratorios por palabras del título ni asumir que no tener nota equivale a no haber entregado. Las reglas específicas de laboratorio y asistencia requieren un contrato de datos/regla de negocio antes de implementarse.

El siguiente avance es implementar el programador de recordatorios y resumen, con reprogramación al cambiar ajustes/datos y prevención de duplicados. Este commit todavía no publica notificaciones.

### Verificación

Compilación de la app y del APK de pruebas. Pruebas instrumentadas adicionales preparadas para migración de preferencias anteriores, persistencia de horario y recuperación de valores fuera de rango. Su ejecución, el permiso concedido/rechazado y el retorno desde ajustes quedan pendientes de prueba final en dispositivo por indicación del usuario.
