# Alertas tempranas académicas

## Propósito

PaquitoBot identifica señales de riesgo académico antes de que el estudiante
llegue al final del ciclo. Es una demostración para hackathon: los datos son
locales y las reglas quedan explícitas para poder cambiarlas cuando Tecsup o
el backend entreguen la política vigente.

## Reglas de la demostración

| Señal | Regla | Aviso |
| --- | --- | --- |
| Proyección de nota | Menor de 12 sobre 20 | Riesgo de desaprobar |
| Recuperación | Desde 12 y menor de 13 | Elevar promedio / contactar tutora |
| Nota por vigilar | Desde 13 y menor de 14 | Mantener próximas evaluaciones sobre 13 |
| Asistencia sin laboratorio | 4 faltas permitidas; alerta desde 3 | Una falta disponible |
| Asistencia con teoría y laboratorio | 8 faltas permitidas; alerta desde 7 | Una falta disponible |
| Límite de asistencia | Exactamente el límite | Una falta más puede poner en riesgo |
| Límite excedido | Más faltas que el límite | Contactar tutora hoy |

La proyección mantiene el promedio actual en las ocho evaluaciones previstas.
Muestra cuánto se necesita promediar en las evaluaciones restantes para llegar
a 13; no es una nota oficial ni sustituye una decisión académica.

## Experiencia del estudiante

1. El trabajador de avisos evalúa las notas y asistencias del escenario activo.
2. Si existe un riesgo y la categoría **Riesgo académico** está habilitada,
   Android muestra la notificación y la guarda en la bandeja.
   Se priorizan los riesgos sobre los avisos de calificaciones históricas.
   Un mismo riesgo persistente se recuerda como máximo una vez cada 24 horas;
   WorkManager revisa periódicamente y Android decide el momento de ejecución.
3. El aviso de riesgo del sistema abre el seguimiento del curso y la pestaña
   correspondiente. Las entregas, notas de laboratorio e inasistencias abren
   el detalle de su curso. La bandeja ofrece los mismos destinos.
4. El seguimiento explica el motivo del aviso, muestra el valor registrado y
   permite revisar las evaluaciones o las fechas de inasistencia del curso.
   También está disponible desde Perfil → Notas y asistencias.

Los contactos de tutoría son ficticios (`example.invalid`) y solo sirven para
demostrar el flujo. Un backend deberá entregar tutoras reales autorizadas.

## Demo para jurado

En Preferencias de notificaciones, usar **Iniciar demo de todos los avisos**.
Dejar la app en segundo plano: el primer aviso llega aproximadamente a los
15 segundos desde que arranca el trabajo, y los siguientes cada 5 segundos
(15, 20, 25, 30… segundos). Primero muestra una muestra de cada tipo existente:
faltas, notas, entregas por vencer o programadas, laboratorio calificado y
resumen. Continúa con los restantes hasta un máximo de doce, sin duplicados.
**Detener demo** cancela la secuencia y permite volver a iniciarla.
Android puede retrasar el comienzo según la batería o la cuota de trabajos.
Los avisos se calculan con los mismos datos del estudiante que muestra la aplicación.
Los tipos sin datos se omiten. Las entregas y calificaciones se abren en su
curso; los riesgos abren el seguimiento; el resumen abre la bandeja.

Inicio calcula el promedio del ciclo a partir de las medias de los cursos
con notas (igual peso entre cursos). No muestra puestos ficticios de ranking.
El horario usa las mismas sesiones y entregas sin calificar que el repositorio,
filtradas por la semana seleccionada; el catálogo incluye clases de noviembre.

## Fuente institucional consultada

Reglamento institucional publicado por Tecsup:
https://www.tecsup.edu.pe/wp-content/uploads/2024/07/reglamento-institucional.pdf

- Art. 41: nota mínima aprobatoria de 13 para unidades didácticas;
  redondeo de fracción mayor o igual a 0.5 a favor del estudiante.
- Art. 43: inasistencias hasta el 30% de las horas programadas, considerando
  teoría y práctica; tres tardanzas equivalen a una inasistencia.
- Art. 44: opción de recuperación con promedio final de 11 o 12.

La alerta durante el ciclo no declara una recuperación ni una desaprobación
definitivas. Los límites 4/8 y las ocho evaluaciones de igual peso siguen siendo
la configuración acordada para los datos locales. No se deducen del documento:
para calcular la regla institucional por horas se necesita el sílabo completo,
la duración total programada y los pesos de evaluación. El catálogo actual
contiene sesiones parciales y no debe usarse como total del ciclo.

## Datos y siguientes pasos

- El catálogo demo contiene cuatro cursos para mantener las pantallas legibles.
- `AcademicRiskEngine` está en `sharedLogic`, por lo que la misma política
  puede consumirse desde la implementación SwiftUI de iOS.
- Al integrar datos reales: reemplazar los escenarios por el repositorio remoto,
  confirmar los límites institucionales y suministrar tutorías desde el backend.
