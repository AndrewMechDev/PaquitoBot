# Módulo docente

## Estado

Especificación funcional de `feature/vista-maestro`. Android incluye una vista docente de demostración para la cuenta acordada, con datos sintéticos de prueba y aviso local. No está conectada a la fuente académica real ni sustituye la experiencia del estudiante. Las reglas y límites aquí descritos corresponden a la vista docente.

## Propósito

Permitir que un docente consulte sus cursos y secciones, reconozca estudiantes que requieren seguimiento por notas o inasistencias y entienda el motivo con datos del período académico activo.

La vista es de consulta. El módulo no permite registrar, editar ni borrar notas, asistencia, alumnos, cursos o secciones.

## Decisiones confirmadas

| Tema | Decisión |
| --- | --- |
| Nota mínima aprobatoria | 13 |
| Promedio | Media aritmética de las notas registradas. Todas tienen el mismo peso. |
| Riesgo por nota parcial | Mostrar riesgo actual si el promedio registrado es menor que 13 y faltan evaluaciones. Marcar el promedio y el riesgo como parciales. |
| Laboratorios compuestos | Promediar sus subnotas para obtener una nota de laboratorio. Esa nota cuenta una vez junto a cada práctica en el promedio del curso. |
| Laboratorio incompleto | Mostrar las subnotas registradas como parciales. Excluir el laboratorio del promedio del curso hasta registrar todas sus subnotas requeridas. |
| Período consultable en el MVP | Solo el período académico activo. No se consultan períodos anteriores en la primera versión. |
| Base del cálculo de asistencia | Clases realizadas. Las clases futuras no entran en el denominador. |
| Estado de la clase | La fuente académica informa si cada clase está realizada, cancelada o reprogramada. |
| Alcance del umbral DI | Regla operativa de la vista docente basada en clases realizadas. No representa por sí sola la determinación institucional por horas. |
| Advertencia por asistencia | Desde el 20 % de inasistencias, incluido. |
| DI por asistencia | Más del 30 % de inasistencias. Exactamente 30 % todavía no activa DI. |
| Ámbito de DI | Calcular independientemente para cada alumno, curso y sección. No combinar cursos diferentes. |
| Resumen del inicio | Mostrar alumnos únicos con al menos una señal, con desglose por riesgo de notas, advertencia de asistencia y DI. |
| Asistencia incompleta | Calcular con las clases realizadas que sí tienen asistencia registrada y marcar el porcentaje y la señal como provisionales. |
| Tardanzas | Contarlas por separado. No sumarlas al numerador de inasistencias. |
| Operaciones del docente | Solo lectura. |
| Mensaje de seguimiento en pruebas | Desde la tarjeta de un alumno con señales, preparar un mensaje según riesgo de notas, advertencia o DI. Un promedio bajo 13 en un curso incompleto se comunica como zona de riesgo; solo un curso completo puede comunicarse como desaprobado. Mostrar confirmación mediante notificación local al docente; no enviar push ni mensaje a las cuentas o dispositivos de los alumnos. |
| Asistente del docente | Mostrar preguntas sugeridas propias para maestros y responder usando el conjunto de notas y asistencias de prueba de la vista docente. Mantener las sugerencias estudiantiles en sus cuentas; no mezclar sus contextos. No mencionar que las respuestas usan datos de prueba salvo que se pregunte. |
| Cuenta docente para este caso | `yordan.sapacayo@tecsup.edu.pe`. La cuenta autenticada debe vincularse con sus asignaciones docentes en la fuente académica real. |
| Alumnos de referencia | `andrew.alca@tecsup.edu.pe` y `joshua.salazar@tecsup.edu.pe` son alumnos previamente registrados. Consultar sus perfiles existentes; no crear registros derivados de estos correos. |
| Carrera de referencia de los alumnos | C-24 Diseño y Desarrollo de Software. Los cursos y secciones concretos deben obtenerse de la matrícula y asignación académica real. |
| Cursos de demostración docente | Teoría: Cálculo II y Física I. Programación: Desarrollo de Aplicaciones Empresariales Avanzado e Integración de Sistemas Empresariales Avanzado. Esta selección limita la demo y no confirma asignaciones reales. |
| Datos de demostración docente | Todos los datos de la demo son datos sintéticos de prueba. Usar un perfil con buen rendimiento y otro con riesgo por notas y DI, con notas y asistencias de las semanas 1 a 11 en los cuatro cursos elegidos. Identificarlos como prueba y mantenerlos aislados de los registros académicos reales. |
| Origen de datos | La vista académica normal usa datos reales del proyecto o su fuente institucional. Los datos sintéticos solo aparecen en un modo de demostración explícito, nunca como reemplazo cuando falla la fuente real. |
| Diseño Android | Reutilizar `PaquitoTheme`, `PaquitoColors`, `PaquitoTypography`, `PaquitoSpacing`, `PaquitoShapes` y los componentes existentes que encajen. |

## Navegación propuesta

1. **Acceso docente**. Una sesión con rol docente abre la experiencia del módulo. La autorización del rol debe proceder de una fuente confiable y proteger también los datos solicitados.
2. **Inicio del docente**. Mostrar su nombre, los cursos y las secciones asignados en el período activo, y cuántos alumnos únicos tienen señales. Desglosar los conteos por riesgo de notas, advertencia de asistencia y DI. Facilitar el acceso a los grupos.
3. **Selección por carrera**. Al seleccionar una carrera, listar los cursos que ese docente enseña en esa carrera.
4. **Selección de curso y sección**. Mostrar las secciones disponibles para el curso elegido y el tamaño de cada grupo.
5. **Lista de alumnos**. En la sección elegida, mostrar por alumno el promedio registrado, el número de notas usado, las faltas sobre las clases realizadas y las señales aplicables.
6. **Detalle del alumno**. Permitir consultar el desglose de las notas registradas y de las clases realizadas, sujeto a definir qué información necesita el docente para entender cada señal.

La ruta principal es **Carrera → Curso → Sección → Alumnos**. El módulo utiliza el período académico activo. Al volver a los niveles anteriores, conserva la carrera, el curso y la sección seleccionados.

## Lectura rápida de una sección

Como propuesta para el MVP, al abrir una sección el docente debería ver primero los alumnos con señales, con DI antes que advertencias y riesgo por notas. El orden exacto queda pendiente de validación con docentes.

Cada fila debe dejar claro qué dato produjo la señal. Por ejemplo:

```text
María Pérez
Promedio 12.5 · 2 notas
Inasistencia 2 de 8 clases · 25 % · Advertencia
```

Los rótulos describen el cálculo disponible. Un promedio menor de 13 entre notas registradas es una señal durante el período, no confirma por sí solo que el alumno desaprobó el curso. La pantalla debe mostrar el promedio y la cantidad de notas para evitar presentarlo como nota final cuando el registro está incompleto.

Si falta la información necesaria, se muestra el estado correspondiente, como **Sin notas registradas**, **Asistencia pendiente de registro** o **Datos insuficientes para calcular DI**. Una ausencia de dato no se convierte en cero ni en inasistencia.

## Reglas académicas

### Promedio y riesgo por notas

El promedio actual se calcula así:

```text
nota de laboratorio = promedio de sus subnotas
promedio del curso = suma de notas finales registradas / cantidad de notas finales registradas
```

- Cada nota registrada tiene el mismo peso.
- Cada práctica aporta una nota final. Cada laboratorio aporta una nota final calculada primero a partir de sus subnotas.
- Si faltan subnotas requeridas, se muestran las que están registradas como parciales, pero el laboratorio no aporta una nota al promedio del curso.
- Un promedio menor que 13 se marca como **Riesgo por notas**. Un promedio igual o superior a 13 no activa esa señal.
- Cuando aún falten notas finales esperadas, marcar el promedio actual y el riesgo como **Parciales**. La señal representa la situación actual de las notas registradas, no un resultado final.
- No se asigna promedio cuando el alumno aún no tiene notas finales registradas. La interfaz muestra **Sin notas finales registradas** y, si corresponde, el detalle de los resultados parciales.
- Se muestra cuántas notas forman el cálculo, por ejemplo, `12.5 · 2 notas`. Esto permite distinguir un promedio parcial de uno con más evaluaciones registradas.
- No se asignan ceros a evaluaciones sin nota registrada.

La cantidad de notas incluidas debe acompañar el promedio. La fuente debe indicar las evaluaciones esperadas o la finalización del curso para distinguir promedios parciales de completos.

### Asistencia, advertencia y DI

```text
porcentaje de inasistencia = inasistencias registradas / clases realizadas con asistencia registrada × 100
```

- Menos del 20 %: sin señal de asistencia.
- Desde 20 % y hasta 30 %, incluido: **Advertencia de asistencia**.
- Más de 30 %: **DI por asistencia**.
- El cálculo es por alumno, curso y sección. No se mezclan clases de cursos diferentes. Un alumno puede tener señales distintas en diferentes cursos.
- La fuente académica provee el estado de cada clase. Solo las clases con estado **Realizada** pueden entrar en el cálculo. Las canceladas y reprogramadas no cuentan como realizadas.
- Solo las faltas confirmadas aumentan el numerador. Una tardanza cuenta como clase con asistencia registrada en el denominador y se muestra por separado.
- La lista debe mostrar los valores utilizados, por ejemplo, `2 faltas · 1 tardanza de 8 clases registradas · 25 %`.
- Las clases realizadas sin asistencia registrada se excluyen temporalmente del numerador y del denominador. La cantidad de registros pendientes debe ser visible.
- Si quedan clases realizadas con asistencia pendiente de registro, el porcentaje y cualquier señal derivada se etiquetan **Provisional**. El resultado puede cambiar cuando se complete la asistencia.
- Si no hay asistencias registradas, el cálculo se presenta como pendiente y no se asigna un porcentaje.

No se debe inferir una inasistencia a partir de un dato ausente.

El cálculo por clases realizadas es la decisión operativa de este módulo. La documentación de reglas estudiantiles de este repositorio describe un límite institucional por porcentaje de horas programadas, considerando teoría y práctica, además de una conversión de tardanzas. Por eso, el porcentaje de esta vista debe comunicarse como indicador docente de seguimiento y no como determinación institucional oficial de DI.

### Señales combinadas

- El alumno aparece una sola vez en la lista de la sección.
- Las señales por promedio y por asistencia se calculan independientemente.
- Si aplica más de una, se muestran ambas. Por ejemplo, **Riesgo por notas** y **Advertencia de asistencia**.
- En resúmenes que agrupan varias secciones, contar alumnos únicos. Si también se muestra cuántas señales curso-sección existen, distinguir ese total del número de alumnos.
- El resumen del inicio cuenta alumnos únicos con al menos una señal en las asignaciones del docente. Desglosa cuántos alumnos tienen riesgo de notas, advertencia de asistencia y DI. Una persona puede aparecer en más de un subtotal, pero solo una vez en el total general.
- El resumen de una sección también cuenta alumnos únicos. Si muestra subtotales por motivo, un mismo alumno puede figurar en más de un subtotal.
- DI debe distinguirse visualmente de una advertencia y acompañarse del porcentaje y los datos usados para calcularlo.

## Información y presentación

- Carrera, curso, sección y período académico actual.
- Perfil del docente con identidad y asignaciones docentes confirmadas por la fuente de datos real.
- Lista de alumnos con nombre y únicamente la información académica necesaria para esta consulta.
- Promedio actual, cantidad de notas, número de inasistencias, clases realizadas y porcentaje de asistencia.
- Señales visibles por motivo, con indicadores de carga, error, lista vacía y ausencia de notas o de datos suficientes para calcular un porcentaje.
- Propuesta de lista para el MVP: priorizar DI, luego advertencia de asistencia y después riesgo por notas; permitir filtrar por tipo de señal. La búsqueda por nombre queda pendiente de priorización.
- Si la fuente real no entrega un dato, mostrarlo como no disponible o pendiente. No completar la pantalla real con alumnos, asignaciones, notas o asistencias de demostración.

La interfaz Android debe seguir los tokens y patrones visuales que ya existen en la aplicación. Antes de añadir una tarjeta, fila, badge o filtro, revisar `androidApp/src/main/kotlin/pe/tecsup/paquitobot/ui/components/` para reutilizar un elemento equivalente. Los estados de riesgo deben mantener texto además del color.

## Acceso, datos y alcance técnico

- La aplicación actual valida el correo institucional de Clerk, pero no distingue todavía el rol docente del rol estudiante.
- Para este caso, `yordan.sapacayo@tecsup.edu.pe` es la cuenta de referencia del docente. Su correo verificado identifica la cuenta de prueba acordada, mientras que el rol y sus carreras, cursos y secciones deben confirmarse mediante la fuente de identidad/asignaciones definida para la aplicación.
- Los alumnos de referencia son `andrew.alca@tecsup.edu.pe` y `joshua.salazar@tecsup.edu.pe`, previamente registrados según el alcance acordado. Su carrera indicada es C-24 Diseño y Desarrollo de Software. Los correos sirven para localizar sus registros existentes; no prueban por sí solos que estén matriculados en un curso o sección asignados al docente.
- Para revisar la experiencia antes de conectar la fuente académica real, se solicitan notas y asistencias de demostración de las semanas 1 a 11 para ambos alumnos en Cálculo II, Física I, Desarrollo de Aplicaciones Empresariales Avanzado e Integración de Sistemas Empresariales Avanzado. Los valores son sintéticos, deben llevar una señal visible de demostración y no escribirse en los registros académicos reales ni presentarse como oficiales. La demostración debe usar los cálculos de promedio, laboratorio incompleto, riesgo parcial y umbrales descritos aquí.
- La notificación de riesgo de esta demostración se publica únicamente en el dispositivo Android con la sesión docente activa y lleva una marca visible de demostración. No contactar a los alumnos ni enviar a sus cuentas. El flujo Android existente de notificaciones es local al dispositivo autenticado y no ofrece entrega remota a otros estudiantes.
- Clerk puede autenticar las cuentas y sus webhooks pueden avisar a un endpoint de la aplicación, pero la cuenta Clerk por sí sola no entrega notificaciones push Android. Aunque los alumnos ya tengan la app instalada, la app debe registrar sus tokens push con sus cuentas y tener permiso de notificaciones. Un endpoint/backend debe validar la sesión Clerk y la asignación docente, resolver el token de cada alumno y enviar mediante OneSignal u otro proveedor push. La guía de arquitectura del repo menciona OneSignal como opción futura; en el Android actual no se encontró SDK/configuración del proveedor, registro de tokens ni envío remoto.
- La documentación de integración y escenarios del repositorio confirma que Android usa datos académicos locales de demostración y no consulta un backend académico propio. No se ha identificado en el proyecto una fuente institucional real para el perfil docente, sus asignaciones, secciones, alumnos, notas y asistencias. Clerk autentica la cuenta, pero no aporta esos datos académicos.
- Por tanto, conectar la vista normal a datos reales es una dependencia previa a su implementación funcional. Primero se debe localizar o integrar la fuente real disponible para el proyecto y documentar cómo se relaciona la identidad autenticada del docente con sus asignaciones. `catalog.json`, `scenario-XX.json`, datos hardcodeados y fixtures solo pueden alimentar el modo de demostración explícito; nunca se usan como respaldo silencioso de la vista normal.
- La fuente académica real debe relacionar docentes, carreras, cursos, período activo, secciones, alumnos, notas, subnotas de laboratorio, asistencias y estado de cada clase. También debe informar las evaluaciones esperadas o la finalización del curso para saber cuándo marcar un promedio y su señal como parciales.
- Cada dato mostrado debe proceder de esa fuente real. Si una consulta falla, la asignación no existe o falta un dato, mostrar el estado correspondiente y permitir reintentar cuando aplique. No mostrar datos de demostración como respaldo ni interpretar datos ausentes como cero.
- La asignación y autorización de grupos debe comprobarse al entregar los datos. Ocultar una pantalla en Android por rol no constituye por sí solo un control de acceso.
- La vista del docente será de solo lectura tanto en la interfaz como en las operaciones disponibles para esa cuenta.
- Los datos y la lógica que eventualmente deban compartir Android e iOS pueden colocarse en `sharedLogic`. La interfaz se mantiene nativa en Compose para Android y SwiftUI para iOS.

### Escenario de demostración solicitado

Los siguientes valores son datos sintéticos de prueba para la demo, no notas ni matrículas oficiales. Se muestran bajo una sección claramente rotulada **Sección de demostración** y no se escriben en los perfiles académicos reales de los alumnos. Cada curso tiene 11 clases realizadas, una por semana, con asistencia registrada. Para los cursos sin laboratorio compuesto, se registra una nota final por semana. Los promedios se muestran como parciales al corte de semana 11 mientras la fuente demo indique que el curso sigue activo.

| Curso | Andrew: notas de semanas 1–11 | Promedio demo | Joshua: notas de semanas 1–11 | Promedio demo |
| --- | --- | ---: | --- | ---: |
| Cálculo II | 15, 16, 14, 17, 16, 15, 18, 14, 17, 16, 15 | 15.7 | 10, 12, 11, 13, 9, 12, 10, 11, 12, 8, 11 | 10.8 |
| Física I | 16, 15, 17, 14, 16, 18, 15, 17, 16, 14, 17 | 15.9 | 11, 10, 12, 9, 13, 11, 10, 12, 9, 11, 10 | 10.7 |
| Integración de Sistemas Empresariales Avanzado | 16, 17, 15, 18, 16, 17, 19, 15, 18, 16, 17 | 16.7 | 11, 13, 10, 12, 9, 11, 12, 10, 13, 11, 9 | 11.0 |
| Desarrollo de Aplicaciones Empresariales Avanzado | Semanas 1–9: prácticas 17, 16, 18, 17, 15, 19, 16, 18, 17. Laboratorio 1: subnotas 17 y 15 en semanas 10–11, promedio 16. Laboratorio 2: subnota 18 en semana 11; falta la segunda. | 16.9* | Semanas 1–9: prácticas 12, 11, 10, 12, 9, 11, 10, 13, 11. Laboratorio 1: subnotas 8 y 10 en semanas 10–11, promedio 9. Laboratorio 2: subnota 9 en semana 11; falta la segunda. | 10.8* |

\* El promedio de Desarrollo de Aplicaciones Empresariales incluye nueve prácticas y el laboratorio 1 como una sola nota. El laboratorio 2 se presenta parcial y no se incluye.

| Alumno demo | Cálculo II | Física I | Desarrollo de Aplicaciones Empresariales Avanzado | Integración de Sistemas Empresariales Avanzado |
| --- | --- | --- | --- | --- |
| Andrew (`andrew.alca@tecsup.edu.pe`) | 1 falta / 11 clases = 9.1 % | 1 / 11 = 9.1 % | 1 / 11 = 9.1 % | 1 / 11 = 9.1 % |
| Joshua (`joshua.salazar@tecsup.edu.pe`) | 3 faltas / 11 = 27.3 % · Advertencia | 4 / 11 = 36.4 % · DI | 2 / 11 = 18.2 % | 2 / 11 = 18.2 % |

Esta combinación deja a Andrew sin señales de riesgo y muestra a Joshua con riesgo por notas, advertencia de asistencia en Cálculo II y DI en Física I. Para que las filas de asistencia sean reproducibles, Andrew falta en la semana 11 de cada curso; Joshua falta en las semanas 3, 7 y 10 de Cálculo II; 2, 5, 8 y 11 de Física I; 4 y 9 de Desarrollo de Aplicaciones Empresariales Avanzado; y 3 y 10 de Integración de Sistemas Empresariales Avanzado. Todos los demás registros representan presencia. Si se incluye una tardanza, asignarla a Joshua en Desarrollo de Aplicaciones Empresariales Avanzado, semana 7; se muestra separada y no cambia el porcentaje.

## Criterios de aceptación del alcance

1. Un docente autorizado llega a su módulo y ve solo sus asignaciones del período académico activo.
2. El inicio muestra el número de alumnos únicos con señales y el desglose por riesgo de notas, advertencia y DI. Una persona que aparece en varios subtotales cuenta una vez en el total general.
3. Puede recorrer carreras, cursos y secciones, y consultar la lista de alumnos de cada sección.
4. Un promedio de 12.99 activa riesgo por notas y un promedio de 13 no lo activa. Si quedan evaluaciones, el promedio y el riesgo se muestran como parciales.
5. Las notas registradas se promedian con igual peso y la pantalla informa cuántas notas entraron al cálculo.
6. Un laboratorio con subnotas 10 y 14 aporta una nota final de 12. Junto a una práctica de 16, el promedio del curso es 14.
7. Si solo una de las dos subnotas del laboratorio está registrada, se muestra como parcial y no cambia el promedio del curso.
8. Clases canceladas y reprogramadas no cuentan como realizadas. Una clase reprogramada entra en el cálculo cuando la fuente la marque como realizada.
9. Con ocho clases realizadas y ocho asistencias registradas, dos inasistencias muestran advertencia al 25 %. Con treinta clases y treinta asistencias registradas, nueve inasistencias equivalen al 30 % y no activan DI. Diez activan DI.
10. Dos inasistencias y una tardanza en ocho clases con registro producen 25 % de inasistencia. La tardanza se informa por separado y no aumenta el porcentaje.
11. La asistencia de un alumno se calcula por curso y sección. Una inasistencia en un curso no se agrega al cálculo de otro curso.
12. Si se cumplen varios criterios, el alumno aparece una vez con todas sus señales.
13. Si una clase realizada no tiene asistencia cargada, el porcentaje usa las clases con registro y muestra **Provisional**, junto con la cantidad pendiente.
14. Si no hay asistencias registradas, la pantalla muestra que el cálculo está pendiente y no presenta un porcentaje.
15. Si faltan notas o datos de asistencia, la pantalla explica la limitación y no presenta un resultado inventado.
16. El docente puede consultar la información, pero no modificarla desde este módulo.
17. Los elementos visuales usan el sistema de diseño existente y presentan el estado con texto además del color.
18. En la vista normal, perfil, asignaciones, alumnos, notas y asistencias proceden de la fuente académica real autorizada. El modo de demostración presenta sus valores sintéticos con una etiqueta visible y sin escribirlos en los registros reales.
19. Si la fuente real aún no está disponible o devuelve un error, la vista normal muestra indisponibilidad o error sin sustituirla por datos de ejemplo.
20. El mensaje de seguimiento genera el texto correspondiente a las señales del alumno y muestra confirmación local solo cuando Android publica el aviso en el dispositivo docente. Este flujo de prueba no entrega mensajes a Andrew, Joshua ni a otra cuenta.

## Decisiones pendientes para seguir puliendo

### Prioridad alta antes de cerrar las reglas

1. **Notas esperadas**. ¿Cómo informa la fuente de datos el total de evaluaciones y cuándo se han publicado todas las notas del curso?

### Prioridad de producto y datos

1. **Perfil docente**. ¿Qué datos deben aparecer además del nombre, como correo institucional, sede o departamento?
2. **Lista del grupo**. Validar con docentes el orden propuesto y los filtros por motivo. ¿La búsqueda por nombre es necesaria en la primera versión?
3. **Detalle del alumno**. ¿Qué desglose necesita consultar para entender un promedio o una inasistencia? Se deben excluir datos personales que no contribuyan a esa tarea.
4. **Cambios durante el período**. ¿Cómo debe mostrarse una nota o asistencia que se actualiza mientras el docente consulta la lista?
5. **Fuente académica real**. ¿Qué origen de datos ya disponible o qué servicio institucional entregará el perfil docente, las asignaciones, listas, notas y asistencias? El estado actual documentado del repositorio solo identifica escenarios de demostración.
6. **Rol y asignaciones**. ¿Qué sistema proporciona el rol docente y la relación actualizada entre la cuenta autenticada, sus cursos y secciones?
7. **Secciones y asignaciones reales**. La demostración se limitará a Cálculo II, Física I, Desarrollo de Aplicaciones Empresariales Avanzado e Integración de Sistemas Empresariales Avanzado. Confirmar las secciones del período activo y en cuáles están matriculados Andrew y Joshua. La selección de cursos para demo no confirma que el docente los tenga asignados realmente.
8. **Distribución de evaluaciones de demostración**. Definir qué semanas contienen prácticas, laboratorios y subnotas de laboratorio antes de asignar calificaciones de ejemplo. Las subnotas deben seguir promediándose como una nota final de laboratorio; un laboratorio incompleto permanece parcial y fuera del promedio del curso.
9. **Política de repetición de la notificación demo**. El envío manual publica un aviso local de prueba desde la tarjeta del alumno. Falta decidir si se debe impedir una segunda publicación repetida. Si más adelante se decide enviarla a los alumnos, Clerk puede validar la identidad, pero hace falta un endpoint/backend integrado con un proveedor push, tokens de dispositivo registrados y autorización por asignación; el correo registrado por sí solo no basta.
10. **Pruebas de aceptación**. Los límites 13, 20 % y más de 30 % se deben validar con casos controlados claramente identificados como pruebas, separados de cualquier dato que se presente en la aplicación como información académica real.

## Relación con reglas existentes

Este documento especifica las reglas de la **vista docente**. Las reglas de alertas y escenarios de la experiencia estudiantil se describen en `ALERTAS_TEMPRANAS_DEMO.md`, `PROJECT_CONTEXT.md` y `ESCENARIOS_MOCK.md`. Antes de compartir cálculos entre ambas experiencias, se debe resolver la diferencia entre el promedio simple acordado aquí y la composición de evaluaciones descrita para estudiantes, además de la unidad de cálculo de asistencia.
