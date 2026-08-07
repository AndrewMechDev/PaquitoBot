# Herramientas del MCP de Figma — referencia operativa

Catálogo de las herramientas que se usan en este proyecto, sus quirks reales (validados en sesión 2026-08-05 contra "Paquito (copia)") y el rol que cumplen dentro del workflow.

## Índice

- `whoami` — diagnóstico de cuenta.
- `get_metadata` — mapa de estructura (XML).
- `get_variable_defs` — extracción de variables/tokens.
- `get_design_context` — extracción de pantalla para implementar.
- `get_screenshot` — captura visual de referencia.
- `get_libraries` / `search_design_system` — descubrimiento de assets reutilizables.
- `create_new_file` / `use_figma` — para code-to-design (no aplica al PaquitoBot actual).

## `whoami`

- **Input**: ninguno.
- **Output**: handle, email, planes con seats y tier (student/team/pro).
- **Cuándo usar**: al inicio de sesión para confirmar la cuenta activa y el plan (estudiantil Full = sin límite de 6 llamadas/mes). También útil si `get_design_context` empieza a fallar por rate-limit.

## `get_metadata`

- **Input**: `nodeId` opcional + `fileKey`.
- **Output**: árbol XML del nodo (capas, nombres, posiciones, tamaños). Liviano comparado con `get_design_context`.
- **Cuándo usar**: para orientarse en un archivo antes de pedir el detalle completo. **Sí funciona sin selección activa del usuario** — es la única herramienta que da mapa estructural sin selección.
- **Quirk**: si no se pasa `nodeId`, lista las páginas top-level del documento (las primeras páginas del archivo, no todos los frames).

## `get_variable_defs`

- **Input**: `nodeId` + `fileKey`.
- **Output**: pares `nombreVariable → valor` (colores como `#RRGGBB`, números, strings).
- **Cuándo usar**: extraer tokens del design system (paleta, tipografía, spacing).
- **Quirk crítico**: **requiere selección activa en la app Figma**. Pasar solo `nodeId` no es suficiente; si no hay selección, devuelve `You currently have nothing selected`.
- **Workaround**: pedir al usuario que haga clic en la página/frame correspondiente en Figma antes de invocar.

## `get_design_context`

- **Input**: `nodeId` + `fileKey` + opcional `skillNames`.
- **Output**: código referencia (React + Tailwind) + screenshot del nodo + contextual hints sobre assets, fuentes y componentes.
- **Cuándo usar**: para implementar una pantalla. Es la herramienta principal del flujo design-to-code.
- **Mandatorio**: cargar la skill `figma-design-to-code` (via `get_figma_skill` o de la lista del plugin) y pasarla en `skillNames` antes de llamar.
- **Quirk**: la salida es React+Tailwind aunque el target sea Compose o SwiftUI. Tratar siempre como **referencia**, no como código a pegar verbatim.

## `get_screenshot`

- **Input**: `nodeId` + `fileKey` + opcional `maxDimension`, `enableBase64Response`.
- **Output**: URL corta de un PNG hosteado por Figma + (opcional) base64 inline si `enableBase64Response=true`.
- **Cuándo usar**: validar visualmente que la implementación generada coincide con el diseño original.
- **Quirks importantes**:
  - La URL **expira en segundos** (~7 días según la skill `figma-design-to-code`, pero en la práctica mucho menos en sesiones MCP activas). No funciona descargar después, ni pasar a `WebFetch` (timeout confirmado).
  - Si necesitas la imagen persistente, hay que pedir `enableBase64Response=true` o capturar en el mismo turno.
  - `maxDimension` default 1024; subir a 1600 para ver detalle de tipografía.
  - Devuelve además `width`, `height`, `original_width`, `original_height` para decido si re-pedir a mayor resolución.

## `get_libraries` / `search_design_system`

- **Input** (libraries): `fileKey`. **Input** (search): texto.
- **Output**: bibliotecas suscritas al archivo / assets de bibliotecas que matchean el query.
- **Cuándo usar**: para descubrir si el archivo tiene una biblioteca de componentes propios publicada antes de extraer variantes manualmente.

## `create_new_file` / `use_figma`

- **Cuándo NO usar en este proyecto**: la fase actual es **read-from-Figma** (extraer para implementar código), no **write-to-Figma** (crear archivos nuevos de diseño).

## Reglas operativas

1. **Una llamada pesada por turno** cuando sea posible: agrupar capturas con `get_screenshot` y metadata con `get_metadata` en paralelo solo cuando no dependan entre sí.
2. **Persistir respuesta cruda**: si `get_variable_defs` o `get_design_context` devuelve un JSON largo, pegarlo a `requerimientos/UI_INTEGRATION.md` antes de transformarlo. Las sesiones MCP no garantizan re-extracción.
3. **Selección del usuario = prerrequisito** para `get_variable_defs`, `get_design_context` y `get_screenshot`. Avisar antes de invocar.
