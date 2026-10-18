# Google Calendar — sincronización manual de tareas

## Estado y alcance

Implementación Android con autorización Google Identity Services y Calendar REST API. La validación real contra Google requiere completar la configuración del proyecto del equipo y usar una cuenta de prueba en un dispositivo con Google Play Services. Las pruebas locales no equivalen a una sincronización real.

- Acceso: Horarios → Google Calendar. Clerk sigue protegiendo el contenido académico; autorizar Google no inicia una sesión de PaquitoBot.
- Conectar obtiene permiso e identifica la cuenta elegida, sin escribir eventos. El correo se muestra antes de sincronizar.
- Sincroniza solamente las tareas seleccionadas sin nota, con fecha válida. Futuras seleccionadas por defecto; vencidas ocultas y sin seleccionar, disponibles con su fecha original al activarlas expresamente.
- El origen actual es `academic.json`, datos de demostración de julio/agosto de 2026. No se desplazan fechas al presente para aparentar tareas vigentes.
- Datetimes sin offset se interpretan en America/Lima (TECSUP Arequipa); offsets explícitos se respetan. Se crea un bloque de 15 minutos desde el vencimiento. Fechas sin hora generan evento de día completo, con fin exclusivo al día siguiente.
- Una vía, manual al pulsar sincronizar. No se importan cambios de Google hacia PaquitoBot, no hay trabajo periódico ni refresh tokens, no se borran eventos por quitar selección, por calificación o por desconectar. Esas acciones no forman parte de esta entrega.

## Configuración de Google Cloud requerida

1. Usar un proyecto del equipo y habilitar **Google Calendar API** en APIs y servicios.
2. Configurar Google Auth Platform (marca, público y acceso a datos) con información real de PaquitoBot. Si está en Testing, añadir las cuentas autorizadas como usuarios de prueba; una organización puede exigir autorización de su administrador.
3. Crear un cliente OAuth de **tipo Android** con estos valores de esta compilación local:

   - applicationId debug: `pe.tecsup.paquitobot.debug` (no confundir con el namespace sin `.debug`).
   - SHA-1 debug: `D6:34:F1:27:40:B0:8C:2B:DE:20:1B:05:9C:4E:9E:EF:1A:6D:B0:E4`.
   - La huella es pública y pertenece al certificado de desarrollo de esta laptop. Cada compañero debe registrar su propia combinación si usa otro certificado. Obtenerla con `./gradlew :androidApp:signingReport`.
4. Registrar los scopes solicitados: `https://www.googleapis.com/auth/calendar.events.owned`, `openid`, `https://www.googleapis.com/auth/userinfo.email`.
5. Esta integración usa autorización Android y token de corta duración; no requiere pegar una clave secreta, ID web o archivo de cuenta de servicio en el APK. No solicita acceso offline. La configuración de Clerk es independiente.
6. Para publicación se necesita cliente Android con applicationId y certificado de la distribución definitiva, además de los requisitos de publicación/verificación OAuth de Google. La variante release todavía no tiene firma configurada en el repositorio.

## Permisos, seguridad y recuperación

- El scope permite gestionar eventos en calendarios propios de la cuenta Google. Esta implementación se limita a `primary` y a IDs/marcadores generados por PaquitoBot; no consulta listas de otros eventos ni modifica eventos ajenos.
- Identificadores deterministas a partir de usuario de Clerk, sujeto Google y IDs de perfil/curso/tarea, mediante SHA-256. No se incluyen IDs de usuario en claro; un cambio de fecha/título conserva el ID del evento.
- GET antes de insertar/actualizar; creación con ID propio, manejo de 409 tras respuesta perdida y validación de marcador privado. GET + PUT con ETag preserva los demás campos y evita sobrescribir un cambio simultáneo. Sin asistentes/invitaciones añadidos.
- Token solo en memoria. No se guarda en SharedPreferences, SavedStateHandle, archivos o logs. La sesión se comprueba antes de cada petición; al perder Clerk se cancela el trabajo y se descartan credenciales.
- Error 401 descarta credenciales e intenta limpiar el token de la caché oficial; el usuario vuelve a conectar. Se distinguen permiso/admin, configuración/API deshabilitada, límite de solicitudes, red, conflicto y evento eliminado.
- El resultado muestra creados, actualizados, sin cambios, fallidos y pendientes. Un fallo de red/permisos/límite detiene el lote; reintentar reutiliza los IDs para evitar duplicados.
- Revocar acceso usa la API de Google y conserva los eventos. Google revoca los scopes previamente concedidos para esa app/proyecto, no solo el scope pasado a esa petición; usar un proyecto coordinado con el equipo. Si falla la revocación, se informa y se permite reintentar. También puede administrarse desde https://myaccount.google.com/permissions .

## Arquitectura

- sharedLogic/domain/calendar: modelos, CalendarGateway y motor de lotes con resultados parciales y guardia de sesión.
- androidApp/domain/calendar: reglas de fecha/identidad con java.time y desugaring existente, testeables en JVM.
- androidApp/data/calendar: autorización oficial Google y cliente REST con HttpURLConnection/JSONObject, siguiendo el transporte ya utilizado por MiniMax para evitar añadir otra pila HTTP. HTTPS fijo, sin redirects con bearer, timeouts y errores tipados.
- ui/calendar: ViewModel/StateFlow con Loading/Ready/Authorizing/Connecting/Syncing/Revoking, resolución por Activity Result y selección de tareas. El componente existente de acceso se reutiliza en Horarios.
- Dependencias nuevas: play-services-auth 22.0.0 (sin Google Sign-In legado) y kotlinx-coroutines-play-services 1.9.0 (alineada con coroutines del proyecto). No hay permisos nuevos en Manifest.

## Prueba real pendiente con el usuario

1. Registrar el cliente Android, habilitar Calendar API y usuario de prueba.
2. Abrir Horarios → Google Calendar; cancelar una vez el consentimiento y verificar que no se envía nada.
3. Conectar y revisar el correo antes de enviar. Mostrar vencidas si se quiere probar el dataset actual; elegir una tarea conscientemente.
4. Sincronizar y comprobar fecha/hora original en Google Calendar. Repetir: debe indicar sin cambios y mantener un solo evento.
5. Cambiar fecha/título en la fuente de desarrollo, actualizar tareas y repetir: debe actualizar el mismo ID.
6. Probar sin internet y reintentar; revocar permiso en Google, reconectar; cerrar sesión en PaquitoBot durante un lote y verificar que no se inician nuevas escrituras.
7. Probar otra cuenta, un conflicto simultáneo y un evento enviado a papelera. Confirmar resultado parcial y que nunca se modifica un evento ajeno.

## Documentación oficial consultada

- https://developer.android.com/identity/authorization
- https://developers.google.com/android/guides/releases
- https://developers.google.com/workspace/calendar/api/auth
- https://developers.google.com/workspace/calendar/api/v3/reference/events/insert
- https://developers.google.com/workspace/calendar/api/v3/reference/events/update

## Validación local (2026-10-18)

- `:androidApp:assembleDebug`: aprobado.
- `:androidApp:testDebugUnitTest`: 34 pruebas aprobadas.
- `:sharedLogic:testAndroidHostTest`: 35 pruebas aprobadas. Total: 69, de las cuales 14 son nuevas para calendario.
- `:androidApp:lintDebug`: 0 errores, 51 advertencias y 1 sugerencia.
- `:androidApp:signingReport` confirmó paquete/firma de desarrollo documentados arriba.
- Recursos, catálogo Gradle y ausencia de almacenamiento/logs de tokens revisados. No se instaló el APK ni se usó una cuenta real de Google para crear eventos.
- Las pruebas cubren reglas de fecha/identidad y motor mediante gateway simulado. No validan OAuth, respuestas REST o actualizaciones ETag en producción: requieren las pruebas reales indicadas arriba.
