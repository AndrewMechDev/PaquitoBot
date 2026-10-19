# Escenarios académicos de demostración

La app incluye un catálogo compartido en `androidApp/src/main/assets/academic/catalog.json` y 15 escenarios separados en `academic/scenarios/scenario-XX.json`. Cada escenario define notas, asistencias y, cuando corresponde, fechas de entrega distintas. `scripts/generate_demo_scenarios.py` regenera los archivos de escenario.

| ID | Situación |
| --- | --- |
| 01 | Estudiante regular |
| 02 | Alto rendimiento |
| 03 | Notas bajas |
| 04 | Faltas en laboratorios de Física |
| 05 | Faltas en laboratorios de varios cursos |
| 06 | Asistencia en riesgo en Algoritmos (6 de 8 faltas) |
| 07 | Asistencia al límite en Física (8 de 8 faltas) |
| 08 | Límite excedido en Cálculo (9 de 8 faltas) |
| 09 | Entregas vencidas |
| 10 | Varias entregas próximas |
| 11 | Recuperación académica |
| 12 | Rendimiento desigual entre cursos |
| 13 | Laboratorios pendientes |
| 14 | Inicio de ciclo con pocas notas |
| 15 | Riesgo académico integral |

`AcademicScenarioAssignment` utiliza el ID estable de Clerk para escoger el escenario mediante SHA-256. Guarda el ID asignado en preferencias privadas de la instalación, con una clave derivada del hash, para conservarlo si se actualiza el banco. Con el mismo banco de 15 escenarios, el cálculo da la misma asignación en otra instalación. Puede haber dos cuentas con el mismo escenario; para garantizar exclusividad global se necesitaría guardar la asignación en un backend autenticado.

`PaquitoBotApp.academicRepositoryFor(userId)` construye el repositorio de la cuenta activa desde catálogo + escenario. Inicio, Cursos, Horarios, detalle, Google Calendar y avisos usan ese repositorio. Los ViewModels se identifican por cuenta para descartar datos anteriores al cambiar de usuario.

Antes de cada consulta, `AcademicContextSnapshot` arma un JSON con tareas, notas, asistencia y faltas desde ese mismo repositorio. `MiniMaxChatRepository` comprueba que la sesión de Clerk siga perteneciendo a la cuenta asignada antes de enviarlo a la IA. El JSON y las respuestas son datos de demostración, no registros institucionales. Para datos reales, el backend debe verificar el token de Clerk y obtener el contexto autorizado en el servidor.
