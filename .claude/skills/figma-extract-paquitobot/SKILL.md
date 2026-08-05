---
name: figma-extract-paquitobot
description: "Trigger: figma, design system, tokens, theme, extraer pantallas, get_variable_defs, get_design_context, get_metadata, get_screenshot, Main canvas, assets. Extrae design system y pantallas del archivo Figma Paquito (copia) hacia código nativo por plataforma (Compose Android + SwiftUI iOS), reutilizando el theme compartido."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activar cuando el trabajo implique extraer assets, colores, tipografía, componentes o pantallas del archivo Figma "Paquito (copia)" para integrarlos al proyecto KMP. Cubre tres responsabilidades acopladas (design system, Onboarding, Inicio) en una sola skill porque el skill-creator recomienda explícitamente este patrón cuando las áreas comparten contexto casi siempre.

Estructura real del archivo (validada contra `get_metadata` 2026-08-05):
- Página raíz única: `112:146 - assets?` (contiene íconos y componentes, pero **DESACTUALIZADA**).
- Canvas principal: `109:97 - Main` (es la **fuente de verdad actual** de colores, tipografía, íconos en uso y pantallas).
- Remoto: `https://github.com/AndrewMechDev/PaquitoBot.git`
- fileKey: `Piy1K37xHS9jB1qXaaVtuQ`

## Hard Rules

- UI NO se comparte entre plataformas: Compose Multiplatform Android y SwiftUI iOS son **nativos e independientes**. Solo `commonMain` (lógica, modelos, casos de uso) es compartido. Cada plataforma tiene su propio theme que consume los mismos tokens.
- **Alcance actual del repo**: solo UI/visual. El backend vive en un repositorio aparte (FastAPI). No construir capa de red, repositorios ni ViewModels con datos reales hasta que el usuario indique que se va a conectar el backend al móvil.
- **Main canvas (`109:97`) es la fuente de verdad**, no `assets?` (`112:146`). Si hay conflicto entre ambos, gana Main.
- Antes de `get_variable_defs` o `get_screenshot`, el usuario **debe tener el nodo seleccionado en la app de Figma**; estas herramientas devuelven error "nothing selected" si se llaman sin selección activa, aunque se les pase `nodeId`.
- Las URLs de assets devueltas por `get_screenshot` expiran en segundos: no se pueden descargar después ni pasar a WebFetch. Si se necesita referencia visual, pedir inline con `enableBase64Response=true` o capturar en el mismo turno.
- Nunca hardcodear hex (`#FFAA33` o `0xFFFF0000`) dentro de una pantalla. Todo color/tipografía/espaciado debe pasar por `Theme.kt` (Compose) o `Color/Typography` (SwiftUI).
- Nomenclatura: Figma snake_case `/Component Name` → Kotlin `PascalCase` → SwiftUI `PascalCase`. Documentar el mapeo en `references/ui-integration-map.md`.
- Un commit por cambio lógico. Mensajes en español siguiendo la skill `conventional-commits-es`.

## Decision Gates

| Necesidad | Acción |
|---|---|
| Extraer tokens/variables de un frame | `get_variable_defs` (requiere selección en app Figma) sobre el nodo seleccionado |
| Capturar referencia visual | `get_screenshot` con `enableBase64Response=true` (URL expira rápido) |
| Generar código de una pantalla | `get_design_context` (cargar `figma-design-to-code` antes), adaptar a Compose/SwiftUI, no pegar verbatim |
| Mapeo de frames y nodeIds | Ver `references/ui-integration-map.md` y `requerimientos/UI_INTEGRATION.md` |
| Convenciones UI por plataforma | `references/kmp-ui-conventions.md` |
| Plantillas de theme | `assets/theme-compose.template.kt` y `assets/theme-swiftui.template.swift` |

## Execution Steps

### Paso 0 — Selección activa
Pedir al usuario que tenga abierto el archivo Figma "Paquito (copia)" y el nodo relevante **seleccionado** antes de invocar `get_variable_defs`, `get_screenshot` o `get_design_context`. Sin selección, esas herramientas fallan.

### Paso 1 — Identificar el frame (read-only)
- `get_metadata` sobre `109:97` para refrescar mapa de frames si han pasado semanas desde la última consulta.
- Cruzar IDs contra `references/ui-integration-map.md` y `requerimientos/UI_INTEGRATION.md` antes de cualquier llamada al MCP.

### Paso 2 — Tokens del design system (requiere selección)
1. Usuario selecciona canvas Main (`109:97`) y frames clave (navbar, task_list, day) en la app Figma.
2. `get_variable_defs` → devuelve pares `nombre → valor`. **Guardar la respuesta cruda** en `requerimientos/UI_INTEGRATION.md` antes de transformarla.
3. Transformar a:
   - `composeApp/src/commonMain/kotlin/.../theme/Theme.kt` con paleta + tipografía + spacing usando la API de Compose (`Color(0xFFXXXXXX)`, `Typography(...)`, `Shapes(...)`).
   - `iosApp/iosApp/Theme/Theme.swift` + `Colors.swift` + `Typography.swift` con APIs nativas SwiftUI (`Color(.hex)`, `Font.system(...)`).
4. Ambos archivos consumen los **mismos hex del step 2**: la fuente de verdad es única.

### Paso 3 — Íconos (batch)
- Exportar el set completo de íconos en una sola operación (no uno por uno) desde Figma.
- Guardar:
  - Compose: `composeApp/src/commonMain/composeResources/drawable/<nombre>.xml` (vectoriales) o PNG según corresponda.
  - iOS: `iosApp/iosApp/Assets.xcassets/<icon>.imageset/` con PDF vectorial.
- **No dibujar íconos a mano** desde `get_design_context`. Solo descargar el asset exportado y referenciarlo.

### Paso 4 — Componentes reutilizables
- Extraer componentes (navbar, task_list, day, paquito_bot) **una vez** vía `get_design_context` + `get_screenshot` (validación).
- Implementar como componentes compartidos por plataforma:
  - Compose: `composeApp/src/commonMain/kotlin/.../components/<Nombre><Componente>.kt` (parámetros para variantes detectadas en Figma).
  - SwiftUI: `iosApp/iosApp/Components/<NombreComponente>.swift`.
- Reutilizar en Onboarding e Inicio. No regenerar variantes por pantalla.

### Paso 5 — Pantallas (Onboarding, Inicio)
1. Por cada frame objetivo (ver `ui-integration-map.md`), llamar `get_design_context` con su `nodeId`.
2. Adaptar la salida (React+Tailwind) a Compose o SwiftUI nativo. **No pegar verbatim**.
3. Reutilizar componentes del Paso 4. Reutilizar tokens del Paso 2.
4. Validar cada pantalla con `get_screenshot` antes de marcarla cerrada.

### Paso 6 — Documentar y commitear
- Actualizar `requerimientos/UI_INTEGRATION.md` con cualquier nuevo nodeId o cambio.
- Commit en `feature/<nombre>` siguiendo `gitflow-merge-directo` con mensaje conventional commit en español.

## Output Contract

Devolver:
- Lista de archivos creados/modificados por plataforma (`composeApp/src/...`, `iosApp/iosApp/...`, `.claude/skills/...`, `requerimientos/...`).
- nodeIds y nombres de frames Figma usados o descubiertos.
- Cualquier supuesto sobre tokens si la extracción quedó pendiente (hueco a llenar).
- Mensaje de commit propuesto (sin ejecutar commit salvo confirmación).

## References

- `references/ui-integration-map.md` — catálogo de nodeIds y frames actualizado.
- `references/kmp-ui-conventions.md` — convenciones de UI por plataforma.
- `references/figma-mcp-tools.md` — herramientas del MCP disponibles y sus quirks.
- `assets/theme-compose.template.kt` — plantilla `Theme.kt` para Compose Multiplatform.
- `assets/theme-swiftui.template.swift` — plantilla de theme para SwiftUI.
- `assets/component-extraction.template.md` — checklist al extraer un componente.
- `requerimientos/UI_INTEGRATION.md` — estado vivo de la integración.
- `requerimientos/FIGMA_WORKFLOW.md` — guía original del workflow MCP (desactualizada — single Main canvas, no 3 páginas separadas).
