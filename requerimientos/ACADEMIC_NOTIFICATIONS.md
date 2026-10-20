# Avisos académicos · versión de demostración

## Reglas implementadas

- WorkManager revisa periódicamente (intervalo mínimo 15 minutos) cuando existe sesión institucional autorizada, onboarding completo, alguna categoría seleccionada y notificaciones habilitadas. Android puede aplazar la ejecución por batería/Doze: la hora elegida no es una alarma exacta.
- Entregas: tareas sin calificación y fecha futura dentro del anticipo elegido (24 horas por defecto).
- Laboratorios: se identifican por `AcademicAssignmentKind.Laboratory`, leído del campo explícito `kind: laboratory` del JSON. No se deduce el tipo por el nombre al ejecutar. Reciben recordatorios de vencimiento y avisos de calificaciones disponibles; cada versión de una nota tiene una clave estable.
- Asistencias: solo registros explícitos de falta vinculados a una sesión que terminó en las últimas 24 horas. Una lista vacía o una clase futura no implica falta. No se calculan alertas de expulsión ni límites sin una regla institucional confirmada.
- Resumen diario desde la hora elegida (08:00 por defecto): pendientes del día entre las categorías seleccionadas y faltas recientes. Solo se publica si hay contenido. Se omite cuando está desactivado.
- Exclusión mutua, comprobación de sesión/preferencias antes de cada envío y registro de entrega separado por usuario evitan duplicados y envíos tras cambios de sesión. El registro se conserva 90 días. Máximo 10 avisos por ejecución; los restantes se reconsideran en la siguiente.
- Las notas de laboratorio disponibles pueden generar su primer aviso al activar la categoría aunque se calificaran antes: la fuente demo no ofrece fecha de publicación. El texto dice «calificación disponible» y no afirma que sea una nota nueva.
- Todos los avisos muestran **Datos de demostración** mientras se usa el archivo académico de ejemplo. No se activa ninguna API académica real.

## Validación

Pruebas JVM: selección por categoría, límite del anticipo, vencidas, zona horaria, hora del resumen, cambios de horario de verano, claves estables, duplicados, sesiones futuras/antiguas, resúmenes de asistencia y versiones de notas. Prueba compartida: tipo explícito de actividad, compatibilidad de JSON anterior y rechazo de tipos desconocidos.

Se ejecutaron 11 comprobaciones instrumentadas en un celular Android 16: preferencias, presentación nativa privada, historial por cuenta, envío con la app en segundo plano y dos ejecuciones del worker académico sin repetir entregas registradas. La revisión de batería/Doze prolongado y del último diseño de la bandeja queda a cargo del usuario.

## Revisión manual adicional

1. Instalar la versión debug y conceder notificaciones; completar onboarding e iniciar sesión institucional.
2. Elegir cada categoría por separado; comprobar que el trabajo `academic-notifications` queda programado aun sin Entregas.
3. Enviar la app a segundo plano, esperar el intervalo y comprobar las notas demo de laboratorios; repetir la ejecución y confirmar que no aparecen avisos duplicados.
4. Para entregas/asistencias, usar datos demo con fechas dentro de la ventana actual. Confirmar que las sesiones futuras, registros presentes y faltas antiguas no avisan.
5. Cambiar categorías/horarios, desactivar resumen, revocar el permiso del sistema y cerrar sesión; verificar cancelación del trabajo y ausencia de avisos posteriores.
6. Perfil: verificar identidad, apertura/regreso de Secretaría y matrícula, confirmación/cancelación del cierre, rotación y texto ampliado.
