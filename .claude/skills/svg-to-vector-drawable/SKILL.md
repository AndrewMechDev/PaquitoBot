---
name: svg-to-vector-drawable
description: "Trigger: SVG icon, vector drawable, Android icon conversion, Compose painterResource falla, xml drawable. Convierte SVGs exportados desde Figma (canvas Main 109:97) a Android Vector Drawable XML para Compose Multiplatform en PaquitoBot."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "2.0"
---

## Activation Contract

Activar cuando:
- Se exporta un SVG desde Figma (canvas Main `109:97` del archivo "Paquito (copia)") y se necesita en `composeResources/drawable/`.
- Android crashea con "Android platform doesn't support SVG format" en `painterResource()`.
- Se pide convertir SVG a vector drawable XML para Compose Multiplatform.

Contexto PaquitoBot:
- UI Compose Multiplatform: `composeApp/src/commonMain/composeResources/drawable/`.
- UI SwiftUI: imágenes vía PDF en `iosApp/iosApp/Assets.xcassets/` (esta skill NO cubre iOS — solo Android/Compose).
- Frontend separado del backend FastAPI; los íconos son assets visuales sin estado, son del frontend.
- Fuente de los íconos: exportar en batch desde Figma, nodos del canvas Main (`109:97`).

## Hard Rules

- Android NO soporta SVG en `painterResource()`. SIEMPRE usar `.xml` vector drawables.
- Extensión de salida DEBE ser `.xml`, nunca `.svg`.
- Elemento raíz DEBE ser `<vector>` con `xmlns:android="http://schemas.android.com/apk/res/android"`.
- `android:width` y `android:height` en `dp`, igualando el tamaño lógico del SVG.
- `android:viewportWidth` y `android:viewportHeight` mapear desde el `viewBox` del SVG.
- Fill transparente: usar `android:fillColor="#00000000"`, nunca `fill="none"`.
- Las referencias Kotlin a recursos (`Res.drawable.ic_paquito_xxx`) funcionan con `.svg` y `.xml` por igual — no requiere cambios en el código Composable.
- Naming convention PaquitoBot: `ic_paquito_<recurso>.xml` (lowercase, underscores). Ej: `ic_paquito_home.xml`, `ic_paquito_book.xml`.
- Si el mismo ícono existe en Figma con dos estados (`state=Default` y `state=fill`), exportarlos como dos archivos separados: `ic_paquito_home_outline.xml` y `ic_paquito_home_fill.xml`. NO usar un único ícono con dos colores.

## Decision Gates

| SVG Element | Vector Drawable Equivalent |
|---|---|
| `viewBox="0 0 W H"` | `android:viewportWidth="W"` + `android:viewportHeight="H"` |
| `<path d="...">` | `<path android:pathData="...">` |
| `stroke="#color"` | `android:strokeColor="#color"` |
| `stroke-width="N"` | `android:strokeWidth="N"` |
| `fill="#color"` | `android:fillColor="#color"` |
| `fill="none"` | `android:fillColor="#00000000"` |
| `stroke-linecap` | `android:strokeLineCap` (camelCase) |
| `stroke-linejoin` | `android:strokeLineJoin` (camelCase) |
| `<circle cx="X" cy="Y" r="R">` | `<path>` con arc: `M X-R,Y a R,R 0 1,0 2R,0 a R,R 0 1,0 -2R,0` |
| `<line x1 y1 x2 y2>` | `<path android:pathData="M x1,y1 L x2,y2">` |
| `<rect x y w h>` | `<path android:pathData="M x,y h w v h h -w Z">` |
| `<g>` con transform | Aplanar transforms en path data o usar `<group>` |

## Execution Steps

1. Leer el archivo SVG. Extraer `viewBox` width/height y todos los elementos visuales.
2. Si viene del Figma: confirmar que la fuente es el canvas Main `109:97` (NO `assets?`). Ver `references/ui-integration-map.md` de la skill `figma-extract-paquitobot`.
3. Crear el XML vector drawable siguiendo el template en `references/conversion-guide.md`.
4. Mapear cada elemento del SVG a su equivalente Android con la tabla Decision Gates.
5. Guardar como `.xml` en `composeApp/src/commonMain/composeResources/drawable/` con nombre `ic_paquito_<nombre>.xml`.
6. Borrar el `.svg` original (no se commitea).
7. Verificar que compila: `./gradlew :composeApp:assembleDebug` (cuando el módulo `composeApp` exista; ahora mismo el proyecto no lo tiene).

## Output Contract

Devolver:
- Archivos `.xml` creados y `.svg` borrados.
- Cualquier elemento que requirió conversión manual (gradientes complejos, filtros, máscaras).
- Resultado de la verificación de build.

## References

- `references/conversion-guide.md` — template, ejemplos y edge cases (fórmulas de circle/line/rect, features no soportadas).
- `../figma-extract-paquitobot/references/ui-integration-map.md` — nodeIds de los íconos en Figma.
- `../figma-extract-paquitobot/SKILL.md` — workflow padre de extracción.
