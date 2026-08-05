# UI Integration — PaquitoBot

Estado vivo de la integración entre el archivo Figma "Paquito (copia)" y el código nativo por plataforma.

> **Fuente de verdad del diseño**: canvas `109:97 - Main` del archivo, NO `112:146 - assets?` (esa página está desactualizada). Si hay conflicto, gana Main.

## Identificadores

| Campo | Valor |
|---|---|
| Archivo Figma | "Paquito (copia)" — cuenta TECSUP, Antony Andrew Alca Peralta, Full seat |
| URL | `https://www.figma.com/design/Piy1K37xHS9jB1qXaaVtuQ/Paquito--copia-` |
| fileKey | `Piy1K37xHS9jB1qXaaVtuQ` |
| Canvas principal | `109:97 - Main` |
| Modo edición | DevMode activo |

## Decisiones arquitectónicas

- **UI no compartida**: Compose Multiplatform (Android) y SwiftUI (iOS) son **nativos e independientes**. Solo `commonMain` (lógica, modelos, repositorios) se comparte.
- Cada plataforma consume los mismos tokens del design system pero **implementados con APIs nativas**: Compose Color/Font/Shape vs SwiftUI Color/Font/Frame.
- Sin compartir composables entre plataformas (no existe `sharedUI` en este proyecto, solo `sharedLogic`).
- Toda extracción valida contra `Main` (no `assets?`). Ver `references/ui-integration-map.md` en la skill para catálogo completo de frames.

## Alcance del proyecto (actualizado 2026-08-05)

- **Backend vive en un repositorio aparte (FastAPI)**, no en este repo KMP.
- **En este repo solo se trabaja la parte UI/visual por ahora**. La capa de datos, red, ViewModels con lógica real y repositorios se introducen **cuando se conecte el backend al móvil**.
- Mientras no haya backend:
  - Las pantallas pueden usar **datos hardcodeados** como placeholders directos en Composable/View, sin repository ni ViewModel. Cuando llegue el back, se refactoriza esa pantalla para recibir datos del repository.
  - No se construye capa de red, serialización, ni manejo de errores HTTP todavía.
- **Cuándo se conecte el backend KMP**: el plan cambia. Decisiones a tomar en ese momento: cliente HTTP (Ktor / Retrofit / URLSession nativo), serialización (kotlinx.serialization / Jackson / Codable), forma de los contratos, manejo de loading y errores. Esa conversación es para cuando llegue el momento, no ahora.

## Estado de extracción

### Design system (tokens, colores, tipografía, spacing)

| Token | Valor Figma | Estado | Plataforma |
|---|---|---|---|
| `brand/primary` | _pendiente_ | Vacío (marcador `0xFF______`) | Compose: `PaquitoColors.BrandPrimary` / SwiftUI: `PaquitoColor.brandPrimary` |
| `brand/secondary` | _pendiente_ | Vacío | ambas |
| `brand/accent` | _pendiente_ | Vacío | ambas |
| `state/success` | _pendiente_ | Vacío | ambas (verde del semáforo) |
| `state/warning` | _pendiente_ | Vacío | ambas (amarillo) |
| `state/danger` | _pendiente_ | Vacío | ambas (rojo) |
| `surface/background` | _pendiente_ | Vacío | ambas |
| `surface/elevated` | _pendiente_ | Vacío | ambas |
| `surface/overlay` | _pendiente_ | Vacío | ambas |
| `text/primary` | _pendiente_ | Vacío | ambas |
| `text/secondary` | _pendiente_ | Vacío | ambas |
| `text/disabled` | _pendiente_ | Vacío | ambas |
| `text/on-primary` | _pendiente_ | Vacío | ambas |
| `border/default` | _pendiente_ | Vacío | ambas |
| `border/subtle` | _pendiente_ | Vacío | ambas |
| Tipografía | _pendiente_ | Material 3 default como placeholder | ambas |
| Espaciado | 4 / 8 / 16 / 24 / 32 / 48 | Provisional sin extracción | ambas |
| Radios | 8 / 12 / 20 / pill | Provisional sin extracción | ambas |

> **Acción pendiente**: pedir al usuario que tenga abierto el archivo Figma "Paquito (copia)" y el canvas Main seleccionado, luego invocar `get_variable_defs` con `nodeId=109:97` y `fileKey=Piy1K37xHS9jB1qXaaVtuQ`. La respuesta cruda se pega en la tabla superior reemplazando `_pendiente_` y luego se propaga a las plantillas de theme.

### Íconos (set completo)

| Ícono | Frame en Figma | Estado Compose | Estado iOS |
|---|---|---|---|
| `home` | `242:51` (Default) + `242:52` (Fill) | _pendiente_ | _pendiente_ |
| `book` | `242:70` + `242:71` | _pendiente_ | _pendiente_ |
| `calendar_month` | `146:387` + `146:388` | _pendiente_ | _pendiente_ |
| `lab_profile` | `146:343` + `146:344` | _pendiente_ | _pendiente_ |
| `frame_person` | `146:391` + `146:392` | _pendiente_ | _pendiente_ |
| `robot_2` (paquito mini) | `242:89` + `242:90` | _pendiente_ | _pendiente_ |
| `forum` | `365:808` + `365:809` | _pendiente_ | _pendiente_ |
| `docs` | `365:815` + `365:816` | _pendiente_ | _pendiente_ |
| `stylus_note` | `365:822` + `365:823` | _pendiente_ | _pendiente_ |

> **Acción pendiente**: exportar todos los íconos desde Figma en batch (una sola operación). No dibujarlos a mano desde `get_design_context`.

### Componentes reutilizables

| Componente | Símbolo Figma | Estado Compose | Estado SwiftUI |
|---|---|---|---|
| `Navbar` (bottom bar 5 ítems) | `333:224`, `242:198` Default, `242:200` Activate | _pendiente_ | _pendiente_ |
| `PaquitoBot icon` (ícono del bot) | `351:433` | _pendiente_ | _pendiente_ |
| `PaquitoBot` (mascota) | `333:210` | _pendiente_ | _pendiente_ |
| `TaskInfo` (item de lista de tarea) | `365:894` | _pendiente_ | _pendiente_ |
| `TaskList` (lista vertical) | `365:973` | _pendiente_ | _pendiente_ |
| `Day` (día del calendario) | `365:1070` | _pendiente_ | _pendiente_ |

### Pantallas

| Pantalla | Frame Figma (nodeId) | Estado Compose | Estado SwiftUI |
|---|---|---|---|
| Onboarding: welcome | `112:3` | _pendiente_ | _pendiente_ |
| Onboarding: notificaciones (3 cards vertical) | `112:135` | _pendiente_ | _pendiente_ |
| Onboarding: notificaciones (cards horizontales) | `156:124` | _pendiente_ | _pendiente_ |
| Home: versión A (saludo + semana + tareas) | `351:644` | _pendiente_ | _pendiente_ |
| Home: versión B (extendido, header + calendario + alertas) | `364:219` | _pendiente_ | _pendiente_ |
| Chat (placeholder) | `351:667` | _pendiente_ | _pendiente_ |
| Cursos (placeholder) | `351:697` | _pendiente_ | _pendiente_ |
| Horarios (placeholder) | `351:719` | _pendiente_ | _pendiente_ |

## Decisión pendiente: home canónica

Hay 2 versiones de `/home` en Figma:
- `351:644` (versión A): más simple, "¡Bienvenido, {user}!" + semana + tareas.
- `364:219` (versión B): más completa, "Hola, Andrea" + calendario semanal + "Paquito te avisó" + alerta "VENCE EN 6 H" para Cálculo II Lab 4.

La versión B cubre más JTBD del MVP (Dolor 3: pendientes dispersos con countdown real). **Recomendación**: implementar B primero, A queda como referencia / fallback.

## Registro de cambios

- 2026-08-05 — Creación inicial. Skill `figma-extract-paquitobot` creada. Plantillas de theme Compose + SwiftUI con marcadores `0xFF______`. Sin tokens reales todavía.
- 2026-08-05 — Actualización de alcance: backend FastAPI vive en repo aparte; este repo KMP trabaja solo UI por ahora. Capa de datos/red/ViewModels se introduce cuando se conecte el backend al móvil.
