# Checklist de extracción de componente Figma → código

Usar este checklist **cada vez** que extraigas un componente. Mantener copia del resultado pegada en `requerimientos/UI_INTEGRATION.md` para auditoría.

## Antes de llamar al MCP

- [ ] Identificado el `nodeId` del frame/sección del componente.
- [ ] Confirmado que los nodeIds de variantes están en `references/ui-integration-map.md`.
- [ ] Decidido a qué plataforma(s) aplica (Compose, SwiftUI, ambas).
- [ ] Tokens ya extraídos (sin esto, la implementación usará `PaquitoColors`/ `PaquitoColor` con valores vacíos).

## Llamadas al MCP

- [ ] `get_metadata` para refrescar el árbol del componente si han pasado > 1 semana desde la última consulta.
- [ ] `get_variable_defs` (con selección activa del usuario en Figma) → guarda el JSON crudo.
- [ ] `get_design_context` (cargar skill `figma-design-to-code` antes, pasarla en `skillNames`).
- [ ] `get_screenshot` con `enableBase64Response=true` para validación visual inline.

## Implementación por plataforma

### Compose Multiplatform

- [ ] Archivo en `composeApp/src/commonMain/kotlin/dev/paquitobot/components/<NombreComponente>.kt`.
- [ ] Función Composable pública con `modifier: Modifier = Modifier` como primer parámetro.
- [ ] Slots nombrados para variantes detectadas (no `if-else` encadenados).
- [ ] Usa `PaquitoColors.*`, `PaquitoTypography.*`, `PaquitoSpacing.*` — sin literales.
- [ ] `Preview` con datos mock (no previews vacíos).
- [ ] Sin acceso a `android.*` o `ios.*` en `commonMain`.

### SwiftUI

- [ ] Archivo en `iosApp/iosApp/Components/<NombreComponente>.swift`.
- [ ] `struct <NombreComponente>: View { var body: some View { ... } }`.
- [ ] Propiedades por variante (`var isActive: Bool = false`, no enums gigantes).
- [ ] Usa `PaquitoColor.*`, `PaquitoFont.*`, `PaquitoSpacing.*`, `PaquitoRadius.*`.
- [ ] `#Preview` con datos mock al final del archivo.
- [ ] Sin dependencias de frameworks no estándar.

## Validación visual

- [ ] Screenshot de la pantalla nativa generada.
- [ ] Comparada con `get_screenshot` del frame Figma equivalente.
- [ ] Inconsistencias resueltas **modificando el código para alinearse con Figma** (el design system es la fuente de verdad, salvo bug evidente).
- [ ] Tokens usados en el componente coinciden 1:1 con `requerimientos/UI_INTEGRATION.md`.

## Documentación

- [ ] Estado del componente actualizado en `references/ui-integration-map.md`.
- [ ] Si se introdujeron nuevos tokens, agregados a las plantillas de theme (`assets/theme-*.template.*`).
- [ ] Commit siguiendo conventional-commits-es con scope `androidApp`, `iosApp` o `sharedUI`.
