---
name: senior-review-paquitobot
description: "Trigger: nuevo componente, nueva pantalla, antes de crear un Composable, revisar duplicacion, auditoria de codigo, dead code, se repite el componente en PaquitoBot. Verifica que no se dupliquen componentes/pantallas y que no quede codigo huerfano tras un refactor."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activar SIEMPRE que:
- Se va a crear un Composable nuevo que "suena" a algo que ya podria existir (fila de tarea, celda de dia, chip, card, header, badge, input).
- Se va a crear una pantalla nueva (`*Screen.kt`).
- Se termino un refactor o se borro un archivo (ej. una pantalla no usada) — verificar si quedaron dependencias huerfanas.
- El usuario pide explicitamente "revisa duplicados", "auditoria", "no quiero repetir componentes".

Motivacion (incidente real, 2026-08-06): `HomeScreen.kt` reimplemento `WeekDayCell`/`TaskInfoRow` privados en vez de reusar `Day.kt`/`TaskInfo.kt` que ya existian. Ademas `HomeBScreen.kt` quedo como pantalla completa sin usar (no llamada desde `MainActivity`) durante mas de una sesion sin que nadie lo notara, hasta que arrastro 4 componentes (`Day.kt`, `TaskInfo.kt`, `TaskList.kt`, `AlertCard.kt`) a quedar huerfanos cuando se borro.

## Hard Rules

- **Buscar antes de crear.** Antes de escribir un Composable nuevo, `Grep` en `ui/components/` por nombres conceptualmente similares (no solo el nombre exacto — "fila de tarea" puede ya existir como `TaskInfo`, `TaskRow`, `TaskItem`, etc.). Si existe algo equivalente, reusarlo o extenderlo con parametros, NO crear una copia privada dentro de la pantalla.
- **Menos codigo > codigo nuevo.** Si un componente existente cubre el 80% del caso, se extiende con un parametro opcional. No se duplica por el 20% restante.
- **Una pantalla, un dueno.** Si dos archivos implementan la misma pantalla conceptual (ej. dos "Home"), uno de los dos es el real (el que esta cableado en `MainActivity`/navegacion) y el otro es candidato a borrar o a fusionar — nunca dejar ambos indefinidamente sin una decision explicita del usuario.
- **Verificar consumidores antes de dar por reusable un componente.** Un componente en `ui/components/` que solo se referencia desde su propio `@Preview` esta huerfano, no "disponible para el futuro". Marcarlo como candidato a borrar.
- **Tras borrar una pantalla, correr el chequeo de huerfanos** (ver Execution Steps) — borrar una pantalla casi siempre deja componentes que solo ella usaba.
- Esta skill es de **analisis y recomendacion**, no borra nada por si sola: toda eliminacion de codigo requiere confirmacion explicita del usuario (mismo principio que `gitflow-merge-directo`: nunca automatico).

## Decision Gates

| Situacion | Accion |
|---|---|
| Voy a crear un Composable para "fila de X con icono + texto + timestamp" | `Grep -r "fun.*Row\|fun Task\|fun.*Item"` en `ui/components/` antes de escribir una linea |
| Voy a crear una pantalla nueva para un frame de Figma | Confirmar si ya existe una pantalla para ese mismo concepto (aunque sea de otro frame/iteracion) antes de crear un archivo nuevo |
| Encontre 2 componentes que hacen lo mismo con params distintos | Proponer al usuario cual queda como el reusable y cual se elimina — no dejar los dos "por si acaso" |
| Termine de borrar una pantalla/componente | `Grep` el nombre de cada Composable que esa pantalla importaba, confirmar si sigue teniendo consumidores reales (no solo su propio Preview) |
| El usuario pide una pantalla que se parece a una ya construida pero para un Figma distinto | Preguntar si el objetivo es reemplazar la pantalla vieja o mantener ambas por una razon de producto — no asumir |

## Execution Steps

### Chequeo de huerfanos (correr tras cualquier borrado de pantalla/componente)

1. Por cada Composable publico (`fun NombreComposable(`) en el/los archivo(s) borrado(s) o tocado(s), `Grep` su nombre en todo `androidApp/src/main/kotlin/`.
2. Si las UNICAS coincidencias son la propia definicion y su propio `@Preview`, es huerfano.
3. Listar todos los huerfanos encontrados y presentarlos al usuario en un solo bloque — no borrarlos sin confirmacion.

### Antes de crear un componente/pantalla nueva

1. `Grep` por el concepto (no el nombre literal) en `ui/components/` y `ui/{home,chat,onboarding}/`.
2. Si aparece algo equivalente: leerlo completo, decidir si se reusa/extiende. Si la respuesta es "no, porque esta pantalla necesita un estilo distinto" — documentarlo en un comentario corto en el nuevo Composable (igual que ya se hace en `HomeScreen.kt` con las desviaciones de Figma), para que la proxima sesion entienda por que coexisten dos.
3. Si NO aparece nada equivalente: crear el componente nuevo en `ui/components/` (no inline en la pantalla) si es razonable que se reuse en mas de un lugar; inline dentro de la pantalla si es genuinamente especifico de esa pantalla.

## Output Contract

Devolver:
- Lista de componentes/pantallas duplicados o huerfanos encontrados, con ruta de archivo.
- Veredicto por cada uno: mantener / fusionar / eliminar (con la razon).
- Ninguna eliminacion ejecutada sin confirmacion explicita del usuario en el mismo turno.

## References

- `../architecture-paquitobot/SKILL.md` — estructura de carpetas y patron MVVM del proyecto.
- `../pre-commit-audit-android/SKILL.md` — auditoria tecnica (compilacion, imports) complementaria a esta (duplicacion/dead code).
- `requerimientos/UI_INTEGRATION.md` — historial de decisiones sobre que version de una pantalla es la canonica (ej. Home A vs Home B).
