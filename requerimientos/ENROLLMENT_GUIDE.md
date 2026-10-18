# Guía de matrícula — primera entrega

## Experiencia

- Acceso desde Inicio y enlace informativo «Consultar guía de matrícula» desde Bienvenida, antes de configurar la app o iniciar sesión. Esta entrada no completa onboarding ni habilita cursos, sesión o notificaciones.
- Dos recorridos: ingresantes (7 pasos) y ratificación (4 pasos). Pago, validación y confirmación comparten contenido canónico.
- Preparación inicial, selección de perfil, pasos con transición y progreso de lectura. Se puede volver, cambiar perfil y consultar trámites relacionados manteniendo la posición del recorrido.
- La confirmación ocurre exclusivamente en el portal oficial. La app no verifica pagos, documentos, elegibilidad ni estado real de matrícula. No pide datos personales ni guarda avance como expediente.
- Fechas de matrícula regular, extraordinaria e inicio de clases pendientes. Campos editables en el contenido JSON; si se añade una fecha debe indicarse el período académico. No se infieren del reloj ni de la fecha de demostración del Inicio.

## Fuentes y decisiones

1. Documento aportado por el usuario y MPA TECSUP N.º 2: https://www.tecsup.edu.pe/wp-content/uploads/2024/07/mpa-tecsup-2.pdf (páginas 2–4 del visor para el flujo principal y reserva/licencia).
2. Portal oficial consultado: https://www.tecsup.edu.pe/portal-de-transparencia/ . Ofrece acceso a «Procesos de matrícula»; no se obtuvo de esa página un cronograma semestral con fechas concretas para esta entrega.
3. Documento enlazado por el portal: https://www.tecsup.edu.pe/wp-content/uploads/2024/07/proceso-de-matricula.pdf . Confirma que el cronograma debe indicar matrícula regular, extraordinaria e inicio de clases. Contiene diferencias con el MPA sobre reserva y licencia.

Contraste realizado el 2026-10-18; no equivale a validación institucional de vigencia. Tarifas y tiempos se presentan como referencia 2024.

- Reserva: MPA menciona hasta 4 períodos; «Proceso de matrícula» menciona un año. La ficha relacionada muestra la discrepancia y solicita confirmar la duración aplicable.
- Licencia: MPA indica sin pago y ventana entre períodos; «Proceso de matrícula» menciona pago y solicitud durante matrícula. No se presenta ninguna de las dos condiciones como vigente sin confirmación.
- Requisitos técnicos: se orienta a navegador actualizado y consulta del portal; no se recomienda Windows 7 como sistema actual.
- El cambio de cursos es opcional. El plazo de respuesta se distingue de un calendario de matrícula.

## Implementación

- Modelos puros EnrollmentGuide/Track/Step/Calendar en sharedLogic/domain/procedures.
- Se amplía el catálogo JSON y el repositorio existente; validación de referencias a pasos/trámites, orden pago → validación → confirmación y fechas con período.
- El mismo ProcedureViewModel carga información pública sin dependencias de Clerk.
- EnrollmentGuideRoute encapsula carga/error, consulta de trámites relacionados y regreso. La pantalla recibe datos y callbacks; usa estado guardable para el recorrido.
- Reutiliza tarjeta de acceso, cabecera, chips, secciones, datos y aviso de fuente de Secretaría. GuideChannelAction comparte manejo de web/correo y errores entre ambas guías.
- No hay nuevas dependencias o permisos. No se conecta la web de matrícula como API ni se almacenan credenciales.

## Revisión manual pendiente

- Ambos perfiles, navegación anterior/siguiente, cambio de perfil y regreso desde reserva/licencia.
- Entrada desde Bienvenida y regreso al onboarding sin autorizar acceso a contenidos personales.
- Texto grande, teclado, rotación, enlaces externos y aplicación de correo ausente.
- Confirmación institucional de tarifas, documentos contradictorios y calendario vigente.

## Validación técnica

- APK debug generado. 27 pruebas Android y 28 pruebas sharedLogic aprobadas (55 en total, 5 nuevas).
- Lint: 0 errores, 48 advertencias y 1 sugerencia. Recursos y referencias verificados.
- Pruebas nuevas cubren ambos perfiles, reutilización del pago, pasos enlazados, orden de validación/confirmación, fechas sin período y visibilidad de condiciones oficiales contradictorias.
- No se instaló el APK ni se automatizó el navegador, pagos o inicio de sesión. La revisión visual y funcional en teléfono queda pendiente.
