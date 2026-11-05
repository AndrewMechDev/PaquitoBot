# PaquitoBot

### Tu mundo académico, en orden

PaquitoBot es un asistente académico para estudiantes de **TECSUP**. Reúne cursos, notas, asistencias, entregas y orientación sobre trámites en una aplicación móvil, con un objetivo: ayudar al estudiante a reconocer los riesgos y actuar antes de que termine el ciclo.

**Kotlin Multiplatform · Jetpack Compose · SwiftUI · Asistente con IA**

**Hack4Edu Arequipa 2026 · Modalidad Senior · MVP de demostración**

## Funcionalidades principales

| Funcionalidad | Qué aporta al estudiante |
| --- | --- |
| **Alertas por notas bajas** | Identifica promedios por debajo de la meta, explica el motivo y orienta sobre las próximas evaluaciones. |
| **Alertas de inasistencia** | Advierte cuando quedan pocas faltas disponibles, se alcanza el límite o se supera. Permite revisar las fechas registradas por curso. |
| **Cursos y evaluaciones** | Consulta notas, promedios, prácticas, laboratorios y entregas pendientes desde el detalle de cada curso. |
| **Recordatorios de entregas** | Recibe avisos de trabajos y laboratorios próximos a vencer, calificaciones disponibles y un resumen académico diario. |
| **Paquito, asistente con IA** | Pregunta por tus cursos, tareas, horarios, notas y asistencias en lenguaje natural. Responde utilizando el contexto académico de la cuenta activa. |
| **Horario y calendario** | Revisa clases y entregas por semana y sincroniza las tareas seleccionadas con Google Calendar, previa autorización. |
| **Guía de matrícula** | Sigue recorridos para ingresantes y estudiantes que ratifican su matrícula, con pasos, requisitos y acceso al portal correspondiente. |
| **Trámites de secretaría** | Busca guías por categoría y consulta requisitos, pasos, costos de referencia, plazos y canales oficiales desde Perfil. |
| **Perfil del estudiante** | Consulta tu identidad, accede a servicios al estudiante y cierra sesión. |

## Acompañamiento antes de llegar al límite

PaquitoBot conecta la alerta con una acción concreta:

1. Revisa las notas y asistencias disponibles en los cursos de la cuenta.
2. Detecta señales de riesgo y muestra un aviso, incluso con la app en segundo plano cuando Android permite ejecutar el trabajo programado.
3. Al tocarlo, abre el seguimiento del curso y explica **por qué llegó la alerta**.
4. El estudiante puede consultar sus notas o faltas y acceder a la opción de orientación con tutoría.

El seguimiento distingue **Notas** y **Asistencias**, muestra el valor registrado y propone qué revisar. Las entregas y los laboratorios también enlazan con su curso. La bandeja permite filtrar avisos, cambiar su presentación y eliminarlos con un gesto, conservando la opción **Deshacer**.

Los indicadores de Inicio se calculan con las notas disponibles: promedio de los promedios por curso, cursos que alcanzan la meta de 13 y cantidad de cursos. Los cursos sin calificaciones no se cuentan como una nota cero.

## Estado del proyecto

Este repositorio contiene un **MVP de demostración**, con cuatro cursos y un banco de quince escenarios académicos. La sesión identifica al estudiante, pero sus notas, faltas y tareas siguen siendo datos locales de prueba, no registros reales de TECSUP. Inicio, Cursos, Horarios, IA, Calendar y avisos utilizan el mismo repositorio académico por cuenta.

- **Reglas de la demo:** meta de nota 13, proyección orientativa con ocho evaluaciones de igual peso y límites de cuatro u ocho faltas según el tipo de curso. La aplicación alerta sobre el riesgo; no determina una desaprobación oficial. Los pesos y límites institucionales deben ajustarse al sílabo y a las horas programadas.
- **Tutoría:** el acceso está preparado para mostrar el recorrido; los contactos actuales son ficticios.
- **Matrícula y secretaría:** ofrecen orientación documental. No realizan matrículas, pagos ni solicitudes. Las fechas, tarifas y condiciones que requieren confirmación remiten a los canales oficiales.
- **Plataformas:** Android dispone de los recorridos de demostración descritos aquí. iOS tiene una aplicación nativa en SwiftUI y consume lógica y datos compartidos; las nuevas pantallas Android no aparecen automáticamente en iOS.

## Participación en Hack4Edu 2026

PaquitoBot participa en **modalidad Senior** y parte de un proyecto previo del equipo. No se presenta toda la aplicación como trabajo creado durante el evento del **3 al 5 de noviembre de 2026**.

| Origen | Alcance |
| --- | --- |
| **Base anterior al hackathon** | Proyecto Android/iOS, interfaces y autenticación, consulta de cursos, notas, asistencias y horarios, chat académico, avisos existentes, guías de matrícula y secretaría e integración con Calendar. |
| **Avance de esta iteración del hackathon** | Motor de alerta temprana por notas y faltas; seguimiento con explicación y acceso al curso desde los avisos y Perfil; indicadores calculados con las notas; agenda semanal; demo rápida de notificaciones; mejoras del borrado por gesto y pruebas asociadas. |

La [base de integración anterior al evento](https://github.com/AndrewMechDev/PaquitoBot/tree/cf5c12b) está registrada el 20 de octubre. Los [cambios de esta iteración](https://github.com/AndrewMechDev/PaquitoBot/compare/cf5c12b...9289c42) están separados en commits del 5 de noviembre. Este historial permite revisar el alcance añadido sin atribuir al hackathon funcionalidades previas; debe acompañarse de la demostración y las evidencias de avance del equipo.

## Uso de inteligencia artificial y revisión

Usamos Codex como herramienta de apoyo para avanzar en el prototipado, organizar la documentación, explorar el diseño y reducir el tiempo dedicado a tareas repetitivas de código. Siguiendo las pautas del hackathon, declaramos para qué se utilizó y cómo se revisaron los resultados:

| Herramienta | Para qué la usamos | En qué parte del proyecto | Cómo lo revisamos |
| --- | --- | --- | --- |
| **Codex** | Apoyo en la primera etapa de prototipado, redacción de documentación, propuestas de diseño y automatización de tareas repetitivas, además de ayuda para implementar y depurar código. El objetivo fue reducir trabajo manual y tiempo de desarrollo. | Prototipos de flujos y pantallas, README y guías técnicas, ajustes de interfaz y código de seguimiento académico, notificaciones, navegación y pruebas. | Compilación Android y pruebas automatizadas, contraste con capturas y ajustes según las observaciones del equipo. La revisión visual completa y la explicación del código por el equipo deben completarse antes de entregar. |

Como funcionalidad del producto, el chat de Paquito utiliza MiniMax mediante API para responder consultas académicas; este uso es distinto del apoyo de Codex al desarrollo. No se afirma haber entrenado un modelo propio. **El cálculo de riesgo es determinista**, con reglas explícitas: no es un modelo predictivo entrenado ni una garantía de aprobación. La autoría y las decisiones del proyecto corresponden al equipo, que debe comprender y responder por lo que presenta.

### Datos y uso responsable

- **Datos de ejemplo:** las calificaciones, asistencias y entregas provienen del catálogo local de demostración. No se han conectado expedientes académicos reales en este MVP.
- **Qué recibe la IA:** el chat envía la pregunta escrita y el contexto académico demo a MiniMax. Ese contexto no incluye el nombre, correo ni identificador de Clerk. El texto libre del usuario no tiene anonimización automática: no deben introducirse datos personales reales, contraseñas ni claves.
- **Control y revisión:** los avisos explican sus motivos y remiten a los registros del curso. Las recomendaciones de IA no sustituyen al docente, la tutora ni los procedimientos institucionales.
- **Acceso y límites:** los datos y guías locales reducen la dependencia de la red; el chat con IA y Google Calendar requieren conexión. La accesibilidad, el uso en equipos de pocos recursos y los posibles sesgos requieren validaciones adicionales. No se presentan como resueltos ni se atribuyen mejoras de rendimiento o retención que no se hayan medido.

## Licencia y recursos

**Pendiente de acuerdo del equipo:** todavía no se ha seleccionado ni incorporado una licencia libre general para este repositorio. Debe resolverse antes de la entrega conforme a las bases del evento. Este README no concede una licencia ni declara cumplido ese requisito.

El equipo también debe verificar la procedencia y los permisos de uso de imágenes, iconos y otros recursos antes de distribuirlos. No se presume una autorización por haber utilizado una herramienta de IA.

## Probar la demo de notificaciones

En Android, inicia sesión y abre **Notificaciones → Preferencias de notificaciones → Iniciar demo de todos los avisos**. Si hace falta, concede el permiso solicitado por el sistema y deja la app en segundo plano.

La secuencia está configurada para emitir el primer aviso a los **15 segundos de ejecución** y los siguientes cada **5 segundos**, hasta doce avisos de los tipos disponibles: riesgo de notas, faltas, entregas, laboratorios calificados y resumen. El mismo botón permite detenerla.

La demo usa los datos de la cuenta. Android puede aplazar el inicio o agrupar las notificaciones según sus ajustes. Los avisos periódicos habituales tienen una programación independiente de esta secuencia rápida.

## Arquitectura

La aplicación utiliza **Kotlin Multiplatform** para compartir lógica y modelos, manteniendo interfaces nativas para cada plataforma. Las funcionalidades se organizan por responsabilidades de interfaz, dominio y datos, con ViewModels y repositorios.

```text
PaquitoBot/
├── androidApp/       # Android: Jetpack Compose, avisos y servicios nativos
├── iosApp/           # iOS: SwiftUI y configuración de Xcode
├── sharedLogic/      # Modelos, repositorios y lógica compartida de dominio
├── requerimientos/   # Guías funcionales y documentación de integraciones
└── scripts/          # Generación de escenarios de demostración
```

La sesión se gestiona con **Clerk**. El prototipo Android consulta **MiniMax** para el asistente y utiliza **WorkManager** para los avisos en segundo plano. La autorización de **Google Calendar** es independiente de la sesión de PaquitoBot.

## Ejecutar el proyecto

### Android

1. Clona el repositorio y ábrelo en Android Studio.
2. Usa una versión de Android Studio compatible con el Android Gradle Plugin definido en [el catálogo de versiones](gradle/libs.versions.toml).
3. Configura `local.properties` en la raíz del proyecto. Android Studio administra la ruta del SDK; agrega las credenciales de tu entorno:

   ```properties
   clerk.publishableKey=TU_CLAVE_PUBLICA_DE_CLERK
   minimax.apiKey=TU_CLAVE_DE_MINIMAX
   ```

4. Sincroniza Gradle, selecciona un dispositivo o emulador y ejecuta la configuración **androidApp**. Para acceder a los cursos y al chat necesitas una cuenta autorizada.

Compilar el APK debug desde PowerShell:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

El APK se genera en `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. En macOS o Linux utiliza `./gradlew` en lugar de `.\gradlew.bat`.

**Credenciales:** `local.properties` está excluido de Git. En el prototipo Android, la clave de MiniMax se incorpora al APK debug y puede extraerse; no debe utilizarse para una distribución pública. La configuración release actual deja esa clave vacía. Para producción, la credencial debe permanecer en el backend.

Para sincronizar con Google Calendar también debes habilitar su API y registrar el cliente OAuth correspondiente. Consulta la [guía de integración](requerimientos/GOOGLE_CALENDAR.md).

### iOS

Requiere **macOS y Xcode**. Abre el proyecto de `iosApp`, configura la firma y utiliza [la plantilla local de configuración](iosApp/Configuration/Config.local.template.xcconfig) para los valores de tu entorno. El archivo `Config.local.xcconfig` está excluido de Git. Compila la aplicación para un simulador o dispositivo iOS; un APK de Android no se instala en iPhone.

## Pruebas

Pruebas de la lógica compartida y de Android:

```powershell
.\gradlew.bat :sharedLogic:allTests :androidApp:testDebugUnitTest
```

Las pruebas incluyen cálculo de riesgo académico, promedios, fechas, entregas pendientes y planificación de avisos. Las pruebas del simulador iOS requieren macOS y no se ejecutan en Windows.

## Documentación

- [Alertas tempranas y demo de notificaciones](requerimientos/ALERTAS_TEMPRANAS_DEMO.md)
- [Avisos académicos en segundo plano](requerimientos/ACADEMIC_NOTIFICATIONS.md)
- [Guía de matrícula](requerimientos/ENROLLMENT_GUIDE.md)
- [Guía de secretaría](requerimientos/SECRETARIA_GUIDE.md)
- [Integración con Google Calendar](requerimientos/GOOGLE_CALENDAR.md)
- [Asistente MiniMax](requerimientos/MINIMAX_CHAT.md)
- [Escenarios académicos de demostración](requerimientos/ESCENARIOS_MOCK.md)
- [Diseño e integración de interfaces](requerimientos/UI_INTEGRATION.md)
