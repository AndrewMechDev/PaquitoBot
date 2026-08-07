# Paquito Bot — Asistente Académico IA para TECSUP

> Este documento es la fuente de verdad del producto para cualquier sesión de Claude Code en este repositorio. Antes de generar pantallas, features o copys, lee esto completo. Si vas a sugerir una función nueva, revisa primero la sección "Cómo evaluar nuevas ideas" al final.

## 1. Qué es esto y para quién

Paquito Bot es un asistente de IA personalizado, integrado dentro de la app de estudiantes de TECSUP, que ayuda a cada estudiante a entender **en tiempo real** dónde está parado en el ciclo académico y qué necesita hacer para no perderlo.

No es un chatbot de preguntas generales. No es un dashboard más de notas. Paquito Bot es una herramienta de **detección y prevención temprana de riesgo académico**, construida sobre tres dolores reales, medibles y de alta frecuencia.

**Institución:** TECSUP
**Plataforma:** App móvil, Kotlin Multiplatform (Compose Multiplatform en Android, SwiftUI en iOS — sin UI compartida, cada plataforma nativa)
**Etapa actual:** MVP para presentación a fin de mes ante el equipo de producto de TECSUP. Ya existe: notificaciones básicas, vista de horario, cursos y laboratorios (con datos mock).

## 2. Los 3 dolores que atacamos ahora (prioridad de este MVP)

Cada dolor está formulado como Job-to-be-Done: cuándo se activa, qué quiere lograr el estudiante, y qué pasa si no se resuelve.

### Dolor 1 — Incertidumbre de aprobación
**Contexto del sistema de evaluación:** las notas se dividen en dos tipos:
- **Notas de práctica:** 4 exámenes de práctica por ciclo, cada uno con su propia nota directa.
- **Notas de laboratorio:** 8 laboratorios por ciclo. Cada laboratorio se compone de 2 notas, y el promedio de esas 2 da la nota final de ese laboratorio.

El promedio general del curso combina ambos tipos de nota (práctica + laboratorio), por lo que el simulador debe considerar los dos, no solo los laboratorios.

**JTBD:** Cuando entrego un laboratorio o rindo un examen, quiero saber al instante cómo cambió mi situación real en el curso, para decidir si necesito reforzar algo antes del siguiente, porque si espero hasta fin de ciclo para enterarme, ya no hay tiempo de corregir.

**Qué debe hacer el producto (no solo mostrar el número):**
- Semáforo de estado por curso (aprobado / en riesgo / desaprobado) basado en notas reales ingresadas.
- **Simulador de escenarios**: "si sacas X en el laboratorio que falta e Y en la próxima práctica, tu promedio sería Z". Debe contemplar ambos tipos de nota (práctica y laboratorio) por separado, ya que tienen distinta composición (la práctica es una nota directa; el laboratorio es el promedio de 2 sub-notas). Esto es el corazón del dolor — no es informativo, es una herramienta de decisión.
- Debe verse apenas el estudiante abre la app, sin necesidad de navegar a buscarlo.

### Dolor 2 — Desaprobación por inasistencias
**Contexto de la regla:** límite de 5 faltas. Superarlo significa desaprobación automática del curso, sin importar el rendimiento académico.

**JTBD:** Cuando falto a una clase, quiero saber inmediatamente cuántas faltas más puedo permitirme antes del límite, para decidir si puedo darme el lujo de faltar de nuevo, porque superar el límite es una consecuencia binaria e irreversible.

**Qué debe hacer el producto:**
- Contador visible por curso: "te quedan X faltas antes del límite en [curso]".
- Notificación push en el momento en que se acerca al límite (ej. tras la 3ra o 4ta falta), no un resumen mensual.
- El tono de esta alerta debe sentirse como advertencia útil, no como castigo (ver sección de personalización).

### Dolor 3 — Fechas de vencimiento dispersas
**Contexto:** foros, laboratorios, prácticas — múltiples entregas activas a la vez, repartidas entre aula virtual, avisos verbales y grupos de WhatsApp de sección.

**JTBD:** Cuando tengo varias entregas activas a la vez, quiero ver en un solo lugar qué falta y cuánto tiempo real me queda, para priorizar sin revisar 4 plataformas distintas, porque la carga cognitiva de recordar todo es en sí misma la razón por la que algo se pasa.

**Qué debe hacer el producto:**
- Vista única consolidada de pendientes con countdown real (no solo fecha), ordenada por urgencia.
- Debe funcionar como fuente única de verdad — si el estudiante deja de revisar otras plataformas porque confía en esta vista, el producto ganó.

## 3. Personalización por perfil de estudiante

El mismo dato, comunicado igual a todos los perfiles, no funciona. La personalización no es solo mostrar información distinta — es **calibrar el tono**.

| Perfil | Cómo debe sentirse la comunicación |
|---|---|
| Alto rendimiento | Informativa y directa. Alertas como datos útiles, no como advertencias urgentes. Puede incluir reconocimiento de buen desempeño. |
| Promedio | Balance entre información y motivación. Foco en mostrar el camino claro hacia la aprobación (el simulador de escenarios es clave aquí). |
| En riesgo | Tono empático, nunca punitivo ni alarmista. Foco en acción concreta inmediata ("esto es lo que puedes hacer hoy"), evitar lenguaje que aumente ansiedad. Nunca usar frases que suenen a juicio de valor sobre el desempeño del estudiante. |

El perfil se calibra en el onboarding (ver siguiente sección) y se puede re-calibrar con el tiempo según el comportamiento real (notas, asistencia), no solo la respuesta inicial.

## 4. Onboarding

Objetivo doble: (1) calibrar el tono de Paquito Bot según el perfil real del estudiante, y (2) demostrar valor inmediato en los primeros 60 segundos — no solo pedir datos.

**Flujo sugerido:**
1. Pregunta inicial breve y no evaluativa para calibrar tono. Evitar preguntas que suenen a examen (ej. NO "¿qué tan buen estudiante eres?"; SÍ algo como "¿cómo vas sintiendo el ciclo hasta ahora?").
2. Conexión con datos reales del estudiante (cursos, notas, asistencia) apenas esté disponible la integración — el primer valor debe ser inmediato: mostrar su semáforo de riesgo real, no una pantalla de bienvenida vacía.
3. Cierre del onboarding con una acción concreta ya resuelta: por ejemplo, "esto es lo que te falta entregar esta semana" en vez de un mensaje genérico de bienvenida.

## 5. Lo que ya existe (base actual del MVP)

- Notificaciones (básicas)
- Vista de horario
- Vista de cursos
- Vista de laboratorios
- Diseño visual y sistema de colores ya definidos en Figma (ver `FIGMA_WORKFLOW.md` para el proceso de extracción)

## 6. Roadmap futuro (no prioritario para este MVP, pero documentado para no perderlo)

Estos dolores fueron identificados pero quedan fuera del alcance de la presentación de fin de mes. No implementar todavía, pero tenerlos presentes al diseñar la arquitectura para no cerrar puertas:

- **Información dispersa en múltiples plataformas**: fatiga de triangular aula virtual, correo, WhatsApp de sección.
- **Miedo a pedir ayuda**: detección proactiva de riesgo + conexión privada con tutorías o el propio docente, bajando la barrera social de pedir apoyo.
- **Estudiantes que trabajan y estudian a la vez**: priorización agresiva de "lo urgente hoy" en vez de vistas generales.

## 7. Cómo evaluar nuevas ideas (para Claude Code)

Antes de sugerir o construir una función nueva, evalúala contra estos tres filtros — si no cumple al menos dos, probablemente no es prioridad ahora:

1. **¿Es de alta frecuencia?** ¿El estudiante se topa con este problema semanalmente, no solo una vez al ciclo?
2. **¿Tiene consecuencia dura y medible?** (jalar el curso, perder una entrega) — no solo "se sentiría mejor".
3. **¿Resuelve incertidumbre con una acción concreta?** No basta con mostrar información; debe ayudar a decidir qué hacer.

Filosofía del equipo: **"no competimos por atención, competimos por ansiedad real y medible — esa ansiedad ya existe hoy, con o sin nosotros."** Enamorarse del problema, no de la solución: cualquier feature nueva debe nacer de un dolor real y verificable, no de una funcionalidad que "estaría bonita".

Al sugerir mejoras, Claude Code debe actuar con criterio de: experto en innovación de producto, psicología educativa, pedagogía y diseño UX/UI senior — pensando tanto en lo que el estudiante necesita como en cómo se le vende esto al equipo de producto de TECSUP (ellos aprueban el proyecto; el argumento institucional es retención y prevención temprana de deserción, no solo "features bonitas").
