# Guía de secretaría — plan y primera entrega

## Objetivo y alcance

Consultar trámites de TECSUP Arequipa sin conexión, con requisitos, orientación por pasos, costos de referencia, plazos y canal oficial. La app no presenta solicitudes, valida documentos, cobra ni certifica aprobación.

## Fuente y revisión editorial

- Documento aportado por el usuario y contraste con el Manual de Procesos Académicos TECSUP N.º 2: https://www.tecsup.edu.pe/wp-content/uploads/2024/07/mpa-tecsup-2.pdf
- Referencia documental: 2024. Contraste del contenido: 2026-10-18. No equivale a confirmación de vigencia institucional de tarifas/contactos.
- 35 fichas en 7 categorías. Cada una incluye código del proceso y página del PDF (numeración del visor, contando la portada).
- Los pasos son orientación editorial resumida; los anexos contienen los flujos oficiales. No se extraen reglas nuevas de los pasos genéricos.
- Reserva y licencia tienen IDs distintos aunque compartan PE-004. Las constancias y modalidades de titulación también distinguen variantes.
- Separación explícita de tiempo de respuesta, ventana de solicitud y duración de programas. No se calculan fechas a partir de reglas antiguas.
- Rectificaciones: los gastos de envío no se presentan como una tarifa de rectificación. Confirmar otros importes.
- Convalidación de bachiller: el plazo de la tabla se mezcla con texto de traslados; se muestra «Consultar» para evitar asegurar un plazo ambiguo.
- Sílabo: la fuente menciona correo y canal Intranet. Se conserva Intranet y se pide confirmar el destinatario, sin inventar un correo específico.
- Prácticas y refinanciamiento: el contacto general sirve para orientación; no sustituye al sistema de prácticas ni al asesor de Cobranzas.

## Plan de acción

1. Base de contenido y trazabilidad: modelos compartidos, catálogo JSON local editable y reglas de búsqueda y validación.
2. Consulta Android: acceso desde Inicio, búsqueda/categorías, estado vacío, ficha completa y regreso conservando búsqueda y posición.
3. Canales: abrir web en navegador y preparar correo en aplicación externa; gestionar falta de aplicaciones y permitir copiar la dirección.
4. Verificación: pruebas de carga, búsqueda, integridad del catálogo y recuperación de errores; compilación y lint; revisión visual en teléfono con el usuario.
5. Siguiente fase: guía de matrícula para ingresantes/ratificación, reutilizando los procesos relacionados. Resolver acceso informativo previo a Clerk y cronograma sin fechas inventadas.
6. Posteriormente: sincronización con Google Calendar, independiente de esta guía.

## Arquitectura

- sharedLogic/domain/procedures: modelos puros, interfaz ProcedureRepository y búsqueda por palabras sin distinguir tildes/mayúsculas.
- sharedLogic/data/repository: LocalJsonProcedureRepository, DTOs privados y caché de cargas válidas. Devuelve Result y propaga cancelación.
- androidApp/assets/procedures.json: contenido versionado, sin datos personales ni credenciales.
- PaquitoBotApp inyecta lectura del asset en IO; ProcedureViewModel expone Loading/Ready/Error por StateFlow.
- ui/procedures: catálogo y detalle con tokens existentes (DM Sans, PaquitoColors/Shapes y paquitoSurface), botón compartido y flecha existente. No hay catálogo equivalente previo; no se duplica la pantalla de eventos.
- MainActivity mantiene rutas de catálogo/detalle y SaveableStateHolder para búsqueda/filtro/lista al volver. No se añade otra barra de navegación.
- No hay dependencias nuevas ni cambios en sesión, datos académicos o notificaciones.

## Pendientes de validación humana

- Tarifario vigente, vigencia de formularios y destinatarios; cronogramas del semestre.
- Artículos citados del Reglamento Institucional: no se expande su contenido sin fuente vigente.
- Validación visual, teclado, fuente grande, TalkBack, rotación y apertura/cancelación de canales en el dispositivo.
- La primera entrega conserva el acceso tras Clerk existente. La consulta pública para ingresantes se abordará junto con matrícula.

## Validación técnica de la primera entrega

- `:androidApp:assembleDebug`: aprobado.
- `:androidApp:testDebugUnitTest`: 27 pruebas aprobadas.
- `:sharedLogic:testAndroidHostTest`: 23 pruebas aprobadas (9 nuevas para el catálogo).
- `:androidApp:lintDebug`: 0 errores, 50 advertencias y 1 sugerencia. Dos advertencias de la guía recomiendan la extensión KTX `toUri`; no impiden abrir los canales.
- Recursos y referencias verificados; todos los textos nuevos usan DM Sans. Sin nuevas dependencias o permisos.
- Sin instalación o validación visual automática en dispositivo.


## Contraste público del 18/10/2026

Se consultó el [Portal de Transparencia](https://www.tecsup.edu.pe/portal-de-transparencia/). Su apartado 2 enlaza el [Tarifario de procesos y trámites académicos 2026](https://www.tecsup.edu.pe/wp-content/uploads/2026/09/TARIFARIO-DE-PROCESOS-Y-TRAMITES-ACADEMICOS-2026.pdf), publicado en la ruta de septiembre de 2026. El apartado 6.3 sigue enlazando el documento de Proceso de Matrícula de 2024.

Se contrastaron en la columna Arequipa: constancia de estudios S/ 31.50, evaluación extraordinaria S/ 300 y títulos técnico/profesional técnico S/ 400. Las cuatro fichas correspondientes incluyen una nota de contraste; no se cambiaron sus importes porque coincidían. El resto conserva explícitamente el costo del manual de referencia y requiere confirmar equivalencia/aplicación con Tecsup; todas las fichas permiten abrir el tarifario actual.

No se confirma un cronograma de matrícula 2026-II/2027-I para Arequipa en estos documentos. No se inventan fechas: la guía remite al canal oficial. Los requisitos continúan atribuidos a sus documentos originales, incluidas las discrepancias de reserva/licencia. Esta revisión de fuentes públicas no equivale a una aprobación institucional ni a una consulta directa con Secretaría.

Acceso actual: **Perfil → Trámites de secretaría**.
