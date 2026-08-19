---
name: pre-commit-audit-android
description: "Trigger: antes de commit, pre-commit, validar cambios, revisar código, evitar errores de compilación, pre-commit hook, lint Kotlin, pre-flight check en PaquitoBot. Ejecuta auditoría completa sobre los archivos modificados antes de un commit en PaquitoBot (Android-only KMP) para evitar errores recurrentes."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activar cuando:
- El usuario pide "revisar antes de commit", "auditar", "no quiero los mismos errores otra vez", "validar compilación".
- Se va a hacer `git commit` y el diff toca archivos Kotlin en `androidApp/src/main/kotlin/`.
- El usuario reporta un error de build (después de resolverlo, usar esta skill para verificar que no quedan otros latentes).
- Después de una sesión larga de extracción Figma → código (alto riesgo de imports sin usar, referencias sin resolver, drawables mal formateados).

Contexto PaquitoBot (2026-08-06):
- Solo se trabaja Android en este repo. Compose + Material 3.
- Errores recurrentes documentados en `requerimientos/UI_INTEGRATION.md` sección "Lecciones aprendidas".
- Esta skill NO reemplaza a `./gradlew :androidApp:assembleDebug`; es un **chequeo previo** más rápido que detecta las clases de errores que ya nos han mordido.

## Hard Rules

Esta skill es **read-only**: NO modifica archivos. Solo lee, reporta y propone fixes. El usuario decide si aplica los fixes.

Categorías de chequeo (todas son OBLIGATORIAS):

### 1. Drawables (carpeta `androidApp/src/main/res/drawable/`)

| Verificar | Cómo |
|---|---|
| Ningún archivo con extensión distinta a `.xml` o `.png` | `Glob` con patrón `*` (excluyendo `mipmap-anydpi-v26`) y filtrar extensiones |
| Cada `.xml` tiene root válido | Leer cada uno y verificar que empieza con `<vector>`, `<bitmap>`, `<shape>`, `<layer-list>`, `<inset>`, `<selector>`, `<animated-rotate>`, `<animated-vector>` o `<animation-list>` |
| No hay atributos SVG-only | `Grep` en drawables por: `stroke-opacity`, `fill-opacity`, `stop-opacity`, `xlink:href` mal usado, `clip-path` SVG. **Recordar**: Android usa `android:strokeAlpha` / `android:fillAlpha` (no `strokeOpacity` / `fillOpacity`) |
| `fill="none"` no aparece | Usar siempre `android:fillColor="#00000000"` |
| Atributos en camelCase | `strokeLineCap` NO `stroke-linecap`; `strokeLineJoin` NO `stroke-linejoin` |
| `pathData` bien escapado | Sin comas sueltas como separadores (usar espacios) |
| `.gitignore` excluye SVGs en res/ | Verificar que `**/res/drawable/*.svg` está en `.gitignore` |

### 2. Kotlin (carpeta `androidApp/src/main/kotlin/`)

Por cada archivo `.kt` en el diff:

| Verificar | Cómo |
|---|---|
| Todos los `R.drawable.*` resuelven | Cross-ref contra `Glob` de `res/drawable/*.xml` y `*.png` |
| Todos los `R.string.*` resuelven | Cross-ref contra `res/values/strings.xml` |
| Todos los `PaquitoColors.*` resuelven | Cross-ref contra `ui/theme/PaquitoTheme.kt` |
| Todos los `PaquitoFont.*` resuelven | Idem |
| Todos los `PaquitoTypography.*` resuelven | Idem |
| Todos los `PaquitoShapes.*` resuelven | Idem |
| Todo `Text(...)` tiene fuente explícita | O `fontFamily = PaquitoFont.DMSans` (nunca omitirlo — cae en Roboto del sistema, bug real 2026-08-19), o mejor `style = PaquitoTypography.*` |
| Ningún `Text(...)` nuevo usa `PaquitoFont.DMMono`/`InstrumentSans`/`BricolageGrotesque` | Decisión de UX 2026-08-19: **una sola fuente en todo el proyecto** (DM Sans). Esas 3 familias quedan declaradas sin uso — no reintroducirlas |
| Todas las funciones `@Composable` están anotadas | Buscar funciones que llaman a otras `@Composable` (Text, Row, Column, Box, Image, etc.) sin `@Composable` |
| `Modifier.padding(...)` con firma válida | Las firmas válidas son: `padding(all)`, `padding(horizontal, vertical)`, `padding(start, top, end, bottom)`, `padding(paddingValues)`. NO mezclar |
| `Modifier.border(...)` con orden correcto | Esperado: `width: Dp, color: Color, shape: Shape`. Detectar `color=Dp` o `width=Color` |
| Sin imports no usados obvios | Si un import no aparece ni como referencia directa ni vía FQN en el cuerpo, marcarlo como warning |
| Sin imports faltantes | Si una clase se usa pero no está importada ni como FQN, es error |

### 3. Recursos (`androidApp/src/main/res/`)

| Verificar | Cómo |
|---|---|
| `app_name` existe en `strings.xml` | El manifest lo requiere |
| `colors.xml` y `themes.xml` no están duplicados | `Glob` para `values*/colors.xml` y `values*/themes.xml`; debe haber a lo sumo uno de cada |
| `AndroidManifest.xml` tiene launcher activity con `android:exported="true"` | Es requerido en API 31+ |
| `AndroidManifest.xml` referencia solo drawables/strings que existen | `Grep` en manifest por `@drawable/` y `@string/`, cross-ref |
| No hay permisos peligrosos innecesarios | `INTERNET`, `READ_EXTERNAL_STORAGE`, etc., solo si hay código que los justifique |
| `versionCode` y `versionName` coherentes | Si van a release, deben ser > 0 y no quedar en `1.0` por error |

### 4. Gradle

| Verificar | Cómo |
|---|---|
| Aliases en `androidApp/build.gradle.kts` existen en `gradle/libs.versions.toml` | Cross-ref `libs.X.Y` contra `[libraries]` y `[plugins]` del catálogo |
| No hay duplicados en `[libraries]` | `Grep` por coordenadas Maven repetidas |
| Versiones siguen formato válido | `major.minor.patch` o `major.minor.patch-suffix` |
| `compileSdk` >= `targetSdk` | Leer ambos y comparar |
| Módulos en `settings.gradle.kts` tienen `build.gradle.kts` | Verificar cada `include(":X")` apunte a un módulo real |
| Targets iOS en `sharedLogic/build.gradle.kts` coherentes con el estado del proyecto | Desde 2026-08-06 el repo es Android+iOS (colaborador dedicado a iOS). `iosArm64()`/`iosSimulatorArm64()` activos son ESPERADOS, no legacy. Solo marcar como problema si el bloque está comentado pero hay código Swift en `iosApp/` que depende de `sharedLogic` (framework no se generaría) |

### 5. Documentación viva

| Verificar | Cómo |
|---|---|
| `requerimientos/UI_INTEGRATION.md` refleja el estado actual | Si se agregaron tokens/componentes/pantallas nuevas, documentarlos |
| `requerimientos/FIGMA_WORKFLOW.md` está vigente | Si cambió el proceso de extracción, actualizarlo |
| `AGENTS.md` lista las skills existentes | Si se creó una skill nueva, agregarla |

## Execution Steps

1. **Identificar archivos modificados** del diff (`git status`, `git diff --stat`, o lo que el usuario indique).
2. **Ejecutar cada categoría** solo sobre los archivos en el diff (no el repo completo, a menos que el usuario pida auditoría global).
3. **Para cada categoría**, seguir las verificaciones de la tabla. Si una fila es N/A (ej. el diff no toca drawables), saltarla explícitamente.
4. **Compilar el reporte** en formato:
   - ✅ Categorías verificadas limpias
   - ❌ Errores bloqueantes (con archivo, línea, fix sugerido)
   - ⚠️ Warnings (no bloqueantes, ej. imports no usados)
   - 💡 Recomendaciones
5. **Esperar decisión del usuario** sobre si aplicar los fixes o proceder con el commit igual.

## Decision Gates

| Severidad | Acción |
|---|---|
| ❌ Error bloqueante (archivo .svg en res/, build.gradle con alias inexistente, manifest sin app_name) | NO commitear hasta arreglar |
| ⚠️ Warning (import no usado, token sin usar, target iOS comentado) | Commitear está bien, pero limpiar pronto |
| 💡 Recomendación (documentación desactualizada, posible duplicación) | Opcional |

## Output Contract

Devolver un reporte markdown con:

```markdown
# Auditoría pre-commit — [resumen de archivos modificados]

## ✅ Limpio
- [categorías verificadas sin problemas]

## ❌ Errores bloqueantes (N)
1. **[categoría]**: [descripción]
   - Archivo: [ruta]:[línea]
   - Fix sugerido: [snippet o instrucción]

## ⚠️ Warnings (N)
1. **[categoría]**: [descripción]
   - Archivo: [ruta]:[línea]
   - Recomendación: [texto]

## 💡 Mejoras opcionales
- [ítems]

## Veredicto
- 🟢 Listo para commit
- 🟠 Listo con warnings aceptados
- 🔴 Bloqueado, requiere fixes
```

## Lecciones aprendidas (errores reales que motivaron esta skill)

| Fecha | Error | Categoría | Lección |
|---|---|---|---|
| 2026-08-05 | `padding(horizontal=X, top=Y)` no compila | Kotlin | Solo usar firmas válidas de Modifier.padding |
| 2026-08-05 | `border(width=Color, color=Dp)` argumentos invertidos | Kotlin | Orden: width, color, shape |
| 2026-08-05 | `android:strokeOpacity` en XML drawable | Drawables | Usar `android:strokeAlpha` |
| 2026-08-05 | SVGs placeholder con bounding-boxes vacíos | Drawables | Filtrar SVGs con paths vacíos antes de convertir |
| 2026-08-06 | `*.svg` en `res/drawable/` rompe el build con "The file name must end with .xml or .png" | Drawables | **Nunca commitear SVGs en res/drawable/** — `.gitignore` reforzado |
| 2026-08-19 | Toda la pantalla de Chat (burbujas, input, chips), el Navbar y las cards de onboarding sin `fontFamily` en sus `Text()` — caían en Roboto del sistema, invisible hasta que el usuario lo notó | Kotlin | Chequear SIEMPRE que `Text()` tenga fuente explícita (fila agregada arriba) |
| 2026-08-19 | Indicador de tab seleccionado en `Navbar` (`NavTabItem`) solo cambiaba un fondo translúcido sutil — icono y texto NUNCA cambiaban de color con `selected`. Sobre la píldora de vidrio (Haze) el fondo se volvía casi imperceptible: "Inicio" y "Cursos" se veían idénticos sin importar cuál estaba activo | Kotlin/UX | Un estado `selected` necesita señal por **color** (icono + texto tintados), no solo un fondo sutil — más robusto contra fondos translúcidos/blur |
| 2026-08-19 | `SwipeToDismissBox` en un loop sin `key()` estable por item — al eliminar uno, el siguiente heredaba el estado "ya swipeado" (card visualmente "pegada" con el fondo rojo) | Kotlin | Todo `forEach` que renderiza `SwipeToDismissBox` (o cualquier estado con memoria propia) necesita `key(item.id)` — nunca depender de la posición en la lista |
| 2026-08-13 | Píldora del Navbar con Haze (`hazeSource`) flotando sobre un padding vacío de 110dp — blur de "nada" (fondo blanco vacío) sigue siendo blanco sólido, el glassmorphism no tenía contenido real que difuminar | Kotlin/UX | Un elemento con blur de fondo necesita contenido REAL detrás para que el efecto se note — no reservar espacio vacío bajo un elemento con `hazeEffect` |
| 2026-08-19 | `CoursesScreen`/`ScheduleScreen` envolvían su lista en `Column(weight(1f).verticalScroll(...))` propio, adentro de `AppTabScaffold` — que desde el fix de Navbar (2026-08-13) YA scrollea toda la pantalla como una sola superficie. El doble scroll (uno anidado sin altura acotada dentro de otro) dejaba el título pegado arriba, como un header fijo | Kotlin | Con `AppTabScaffold`, las pantallas NO deben envolver su contenido en otro `verticalScroll` — ya scrollea todo junto. Un `verticalScroll` interno solo es correcto si tiene altura ACOTADA (`heightIn(max=...)`, ver `HomeTasksSection`) o si la pantalla vive fuera de `AppTabScaffold` (ej. `CourseDetailScreen`, con su propio `fillMaxSize()` raíz) |

## References

- `../svg-to-vector-drawable/SKILL.md` — conversión SVG → XML para Compose.
- `../architecture-paquitobot/SKILL.md` — convenciones del proyecto.
- `../../requerimientos/UI_INTEGRATION.md` — documentación viva del estado de la UI.
- `../../requerimientos/FIGMA_WORKFLOW.md` — workflow de extracción Figma.