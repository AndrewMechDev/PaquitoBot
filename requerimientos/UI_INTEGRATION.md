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

- **Solo se trabaja Android en este repo**. iOS lo implementa otra persona en su propio repositorio/fork. Este documento no cubre la implementación SwiftUI; los assets exportados de Figma para iOS (PDFs) deben coordinarse con quien haga ese trabajo.
- **UI no se comparte** entre plataformas. Cada plataforma consume los mismos tokens del design system pero **implementados con APIs nativas**: Compose Color/Font/Shape para Android, SwiftUI/UIKit para iOS (cuando se integre).
- El módulo `sharedUI/` fue eliminado del repo porque exportaba Composables que iban contra la regla de UI no compartida. Se mantiene solo `sharedLogic/` para clases puras Kotlin.
- Toda extracción valida contra `Main` (no `assets?`). Ver `references/ui-integration-map.md` en la skill para catálogo completo de frames.

## Alcance del proyecto (actualizado 2026-08-05)

- **Backend vive en un repositorio aparte (FastAPI)**, no en este repo.
- **En este repo solo se trabaja Android y solo la parte UI/visual por ahora**. La capa de datos, red, ViewModels con lógica real y repositorios se introducen **cuando se conecte el backend al móvil**.
- Mientras no haya backend:
  - Las pantallas pueden usar **datos hardcodeados** como placeholders directos en el Composable, sin repository ni ViewModel. Cuando llegue el back, se refactoriza esa pantalla para recibir datos del repository.
  - No se construye capa de red, serialización, ni manejo de errores HTTP todavía.
- **Cuándo se conecte el backend Android**: el plan cambia. Decisiones a tomar en ese momento: cliente HTTP (Ktor), serialización (kotlinx.serialization), forma de los contratos, manejo de loading y errores. Esa conversación es para cuando llegue el momento, no ahora.

## Estado de extracción

### Design system (tokens, colores, tipografía, spacing)

> **Estado del descubrimiento (2026-08-05)**: el archivo "Paquito (copia)" **NO tiene variables de Figma configuradas**. `get_variable_defs` devolvió `{}` sobre Main (`109:97`), sobre Onboarding (`351:538`) y sobre Welcome (`112:3`). Los colores están aplicados como hex literal en cada elemento.
>
> **Estrategia**: extraer colores reales con `get_design_context` (que sí expone los hex aplicados por elemento), observar la repetición de hex para identificar los "tokens de facto" del design system.

#### Tokens observados en Welcome screen (`112:3`) — extraídos 2026-08-05

| Token | Valor Figma observado | Hex Compose | Plataforma |
|---|---|---|---|
| `brand/primary` | `#00C9FB` (fondo del botón "Continuar") | `0xFF00C9FB` | Compose: `PaquitoColors.BrandPrimary` |
| `text/on-primary` | `#FFFFFF` (texto del botón) | `0xFFFFFFFF` | Compose: `PaquitoColors.TextOnPrimary` |
| `surface/background` | `#FFFFFF` (fondo del screen) | `0xFFFFFFFF` | Compose: `PaquitoColors.Background` |
| `text/primary` | `rgba(0,0,0,0.85)` (título "PaquitoBot") | `0xD9000000` | Compose: `PaquitoColors.TextPrimary` |
| `text/secondary` | `rgba(0,0,0,0.7)` (subtítulo "Configuremos tu asistente") | `0xB3000000` | Compose: `PaquitoColors.TextSecondary` |
| Tipografía primaria | `DM Sans` (SemiBold títulos, Regular cuerpo) | `FontFamily(Font(R.font.dm_sans))` o `FontFamily.SansSerif` fallback | Compose: `PaquitoFontFamily` |
| Tamaño display (título) | `48px` en Figma → `34sp` Android (factor 0.71) | `PaquitoTypography.HeadlineLarge` |
| Tamaño headline (subtítulo) | `32px` → `~23sp` | `PaquitoTypography.HeadlineSmall` |
| Tamaño botón | `24px` → `~17sp` | `PaquitoTypography.BodyLarge` |
| Tamaño dot del badge | `5×5` | Vector/Box de 5.dp |
| Radios | `20px` (botón "Continuar", ícono del bot 100×100) | `20.dp` (Shapes.Large) |
| Padding botón | `15px` | `15.dp` |
| Alto botón | `51px` | `51.dp` |

#### Tokens adicionales observados en Navbar (`351:645`) — extraídos 2026-08-05

| Token | Valor Figma | Hex Compose | Plataforma |
|---|---|---|---|
| `surface/elevated` | `#EEEEEE` (fondo del tab activo) | `0xFFEEEEEE` | Compose: `PaquitoColors.SurfaceElevated` |
| `text/on-surface` | `#1C1B1F` (etiquetas de tabs) | `0xFF1C1B1F` | Compose: `PaquitoColors.TextOnSurface` |
| Fondo wrapper Navbar | `rgba(255,255,255,0.1)` (translúcido sobre fondo de pantalla) | `Color.White.copy(alpha = 0.1f)` | en Navbar.kt directamente |
| Texto tabs | DM Sans SemiBold `14px` | `fontSize = 14.sp, FontWeight.SemiBold` | en Navbar.kt directamente |
| Tamaño íconos tab | `36px` (estado fill) / `24px` (default) | `Modifier.size(24.dp)` (default Compose) | en Navbar.kt |

> **Nota de discrepancia**: el archivo Figma usa dos tonos de texto distintos sin razón clara:
> - `rgba(0,0,0,0.85)` → Welcome → `PaquitoColors.TextPrimary`
> - `#1C1B1F` → Navbar → `PaquitoColors.TextOnSurface` (mantengo nombres separados hasta confirmar canónico)
>
#### Tokens adicionales observados en Home B (`364:219`) — extraídos 2026-08-05

| Token | Valor Figma | Hex Compose | Plataforma |
|---|---|---|---|
| `state/info` | `#0393C9` (dot de día con tarea + link "Ver todo") | `0xFF0393C9` | Compose: `PaquitoColors.StateInfo` |
| `state/info-strong` | `#0277A8` (texto día seleccionado + badges NT) | `0xFF0277A8` | Compose: `PaquitoColors.StateInfoStrong` |
| `state/warning` | `#C88C14` (badge "FO" foro por vencer) | `0xFFC88C14` | Compose: `PaquitoColors.StateWarning` |
| `state/danger` | `#FF445A` (badge navbar "3" + dot día crítico) | `0xFFFF445A` | Compose: `PaquitoColors.StateDanger` |
| `state/neutral` | `#4D5866` (badge "EX" parcial) | `0xFF4D5866` | Compose: `PaquitoColors.StateNeutral` |
| `surface/glass-strong` | `rgba(255,255,255,0.82)` + backdrop-blur 12dp (cards superiores) | `0xD6FFFFFF` | Compose: `PaquitoColors.SurfaceGlassStrong` |
| `surface/glass-soft` | `rgba(246,247,249,0.85)` (días normales, base de cards) | `0xD9F6F7F9` | Compose: `PaquitoColors.SurfaceGlassSoft` |
| `border/info` | `rgba(3,147,201,0.45)` (borde del día seleccionado) | `0x730393C9` | Compose: `PaquitoColors.BorderInfo` |
| `border/subtle` | `rgba(13,21,32,0.06)` (días) | `0x0F0D1520` | Compose: `PaquitoColors.BorderSubtle` |
| `border/default` | `rgba(13,21,32,0.07)` (divisores cards) | `0x120D1520` | Compose: `PaquitoColors.BorderDefault` |
| `text/on-card-strong` | `#0D1520` (títulos de tareas, saludo) | `0xFF0D1520` | Compose: `PaquitoColors.TextOnCardStrong` |
| `text/on-card-muted` | `#6E7885` (subtítulos de tareas) | `0xFF6E7885` | Compose: `PaquitoColors.TextOnCardMuted` |
| `text/on-card-muted-alt` | `#7B8694` ("Paquito te avisó") | `0xFF7B8694` | Compose: `PaquitoColors.TextOnCardMutedAlt` |
| `text/on-dark-muted` | `#93A3B4` ("Cálculo II" sobre card oscura) | `0xFF93A3B4` | Compose: `PaquitoColors.TextOnDarkMuted` |
| `text/on-dark-stronger` | `#A3B1C0` ("Hoy 23:59") | `0xFFA3B1C0` | Compose: `PaquitoColors.TextOnDarkStronger` |
| `text/on-primary-dim` | `#04202E` (texto chips y "Entregar" sobre BrandPrimary) | `0xFF04202E` | Compose: `PaquitoColors.TextOnPrimaryDim` |
| `text/link` | `#0393C9` ("Ver todo") | `0xFF0393C9` | Compose: `PaquitoColors.TextLink` |
| `text/timestamp` | `#98A1AC` ("2 d", "hace cuanto") | `0xFF98A1AC` | Compose: `PaquitoColors.TextTimestamp` |
| `text/day-active` | `#0277A8` | `0xFF0277A8` | Compose: `PaquitoColors.TextDayActive` |
| `text/day-inactive` | `#8D96A1` | `0xFF8D96A1` | Compose: `PaquitoColors.TextDayInactive` |

#### Tokens adicionales observados en Chat (`285:324`) — extraídos 2026-08-05

| Token | Valor Figma | Hex Compose | Plataforma |
|---|---|---|---|
| `state/success` | `#15A05A` (dot "sincronizado" del header del chat) | `0xFF15A05A` | Compose: `PaquitoColors.StateSuccess` ✅ nuevo |
| `text/bubble` | `#16202C` (texto de burbuja del bot) | `0xFF16202C` | Compose: `PaquitoColors.TextBubble` |
| `text/bubble-chip` | `#28323E` (chips "¿Qué vence esta semana?") | `0xFF28323E` | Compose: `PaquitoColors.TextBubbleChip` |
| `text/input-placeholder` | `#757575` (placeholder "Preguntale a Paquito…") | `0xFF757575` | Compose: `PaquitoColors.TextInputPlaceholder` |
| `background/lms-chip` | `#F2F4F6` (chip LMS del header) | `0xFFF2F4F6` | (no expuesto en PaquitoColors; usado directo en ChatHeader) |

#### Tipografías descargadas (`res/font/`)

| Familia | Pesos | Origen | Paquete Compose |
|---|---|---|---|
| DM Sans | Regular 400, Medium 500, SemiBold 600, Bold 700 | Google Fonts OFL | `PaquitoFont.DMSans` |
| DM Mono | Regular 400, Medium 500 | Google Fonts OFL | `PaquitoFont.DMMono` |
| Instrument Sans | Regular 400, Medium 500, SemiBold 600, Bold 700 | Google Fonts OFL | `PaquitoFont.InstrumentSans` |
| Bricolage Grotesque | SemiBold 600, Bold 700 | Google Fonts OFL | `PaquitoFont.BricolageGrotesque` |

> **Mapeo aplicado a `PaquitoTypography`**:
> - DM Sans → titulos y cuerpo principal (Welcome, Navbar, Onboarding Notificaciones, NotificationCard).
> - DM Mono → etiquetas de dia, badges de iniciales, "VENCE EN 6 H", timestamps.
> - Instrument Sans → titulos de tarea, subtitulos, cuerpo del chip LMS, footer "Te aviso otra vez...".
> - Bricolage Grotesque → saludo "Hola, Andrea", avatar "C24", titulo del Chat "Paquito", titulo de la AlertCard.
>
> **Tokens aún pendientes**:
> - `brand/secondary`, `brand/accent` → no aparecen en Main.
> - `text/disabled` → no usado todavía.

#### Extracción cruda (referencia)

Respuesta textual de `get_design_context` sobre `112:3` el 2026-08-05 (con la selección activa del frame Welcome en Figma):

```tsx
const imgPaquitoBotIcono = "https://www.figma.com/api/mcp/asset/8e63a4ab-ff2c-4d28-ad26-bf38d33ee16a.png";
const imgGeminiGeneratedImageDacs5Qdacs5QdacsRemovebgPreview1 = "https://www.figma.com/api/mcp/asset/983fe375-8e7b-4d2e-bcfd-d76003db2f7f.png";

export default function Component() {
  return (
    <div className="bg-white relative size-full" data-node-id="112:3" data-name="/">
      <div className="absolute content-stretch flex flex-col gap-[25px] h-[330px] items-start left-[25px] top-[555px] w-[380px]" data-node-id="112:134" data-name="body">
        <div className="relative rounded-[20px] shrink-0 size-[100px]" data-node-id="112:133" data-name="paquito-bot icono">
          <div aria-hidden className="absolute inset-0 pointer-events-none rounded-[20px]">
            <div className="absolute bg-white inset-0 rounded-[20px]" />
            <div className="absolute inset-0 overflow-hidden rounded-[20px]">
              <img alt="" className="absolute left-[-261.53%] max-w-none size-full top-[-95.78%]" src={imgPaquitoBotIcono} />
            </div>
          </div>
        </div>
        <div className="[word-break:break-word] content-stretch flex flex-col gap-[10px] items-start relative shrink-0 w-full" data-node-id="112:132" data-name="content">
          <p className="font-['DM_Sans:SemiBold'] font-semibold leading-[normal] min-w-full relative shrink-0 text-[48px] text-[rgba(0,0,0,0.85)] w-[min-content]" data-node-id="112:34" style={{ fontVariationSettings: '"opsz" 14' }}>
            PaquitoBot
          </p>
          <p className="[text-box-edge:cap_alphabetic] [text-box-trim:trim-both] font-['DM_Sans:Regular'] font-normal leading-[35px] relative shrink-0 text-[32px] text-[rgba(0,0,0,0.7)] w-[209px]" data-node-id="112:57" style={{ fontVariationSettings: '"opsz" 14' }}>
            Configuremos tu asistente
          </p>
        </div>
        <a className="bg-[#00c9fb] content-stretch cursor-pointer flex h-[51px] items-center justify-center p-[15px] relative rounded-[20px] shrink-0 w-full" data-node-id="112:44" data-name="Botón?">
          <p className="[word-break:break-word] font-['DM_Sans:Regular'] font-normal leading-[normal] overflow-hidden relative shrink-0 text-[24px] text-ellipsis text-left text-white whitespace-nowrap" data-node-id="112:36" style={{ fontVariationSettings: '"opsz" 14' }}>
            Continuar
          </p>
        </a>
      </div>
      <div className="absolute aspect-[428/583] left-[2.56%] right-[42.56%] top-[359px]" data-node-id="2005:360" data-name="Gemini_Generated_Image_dacs5qdacs5qdacs-removebg-preview 1">
        <img alt="" className="absolute inset-0 max-w-none object-cover pointer-events-none size-full" src={imgGeminiGeneratedImageDacs5Qdacs5qdacsRemovebgPreview1} />
      </div>
    </div>
  );
}
```

> **Notas de la skill `figma-design-to-code`**: el output es React+Tailwind (referencia). Para Android hay que **adaptar, NO copiar**. Los `data-node-id` se conservan como comentario para auditoría cruzada. Las URLs de assets expiran en ~7 días, por eso ya se descargaron a `androidApp/src/main/res/drawable/`.

#### Assets descargados de Welcome

| Asset original en Figma | nodeId Figma | Nombre en `androidApp/src/main/res/drawable/` | Tamaño |
|---|---|---|---|
| paquito-bot icono | `112:133` | `paquito_bot_icon.png` | 1.36 MB |
| Personaje Paquito | `2005:360` | `paquito_personaje.png` | 261 KB |

> Pendiente optimizarlos con `cwebp` o similar antes de subir a producción. Los mantengo como PNG original de Figma para la primera iteración.

### Íconos (set completo)

| Ícono | Frame en Figma | Estado Android |
|---|---|---|
| `home` | `242:51` (Default) + `242:52` (Fill) | _pendiente_ |
| `book` | `242:70` + `242:71` | _pendiente_ |
| `calendar_month` | `146:387` + `146:388` | _pendiente_ |
| `lab_profile` | `146:343` + `146:344` | _pendiente_ |
| `frame_person` | `146:391` + `146:392` | _pendiente_ |
| `robot_2` (paquito mini) | `242:89` + `242:90` | _pendiente_ |
| `forum` | `365:808` + `365:809` | _pendiente_ |
| `docs` | `365:815` + `365:816` | _pendiente_ |
| `stylus_note` | `365:822` + `365:823` | _pendiente_ |

> **Acción pendiente**: exportar todos los íconos desde Figma en batch (una sola operación). No dibujarlos a mano desde `get_design_context`. Para iOS (otro equipo) se exportan PDFs o PNGs desde la misma fuente.

### Componentes reutilizables

| Componente | Símbolo Figma | Estado Android |
|---|---|---|
| `Navbar` (bottom bar 5 ítems) | `333:224`, `242:198` Default, `242:200` Activate | _pendiente_ |
| `PaquitoBot icon` (ícono del bot) | `351:433` | _pendiente_ |
| `PaquitoBot` (mascota) | `333:210` | _pendiente_ |
| `TaskInfo` (item de lista de tarea) | `365:894` | _pendiente_ |
| `TaskList` (lista vertical) | `365:973` | _pendiente_ |
| `Day` (día del calendario) | `365:1070` | _pendiente_ |

### Pantallas

| Pantalla | Frame Figma (nodeId) | Estado Android |
|---|---|---|
| Onboarding: welcome | `112:3` | _en progreso (extracción hecha, falta Composable)_ |
| Onboarding: notificaciones (3 cards vertical) | `112:135` | _pendiente_ |
| Onboarding: notificaciones (cards horizontales) | `156:124` | _pendiente_ |
| Home: versión A (saludo + semana + tareas) | `351:644` | _pendiente_ |
| Home: versión B (extendido, header + calendario + alertas) | `364:219` | _pendiente_ (recomendada primero) |
| Chat (placeholder) | `351:667` | _pendiente_ |
| Cursos (placeholder) | `351:697` | _pendiente_ |
| Horarios (placeholder) | `351:719` | _pendiente_ |

## Decisión pendiente: home canónica

Hay 2 versiones de `/home` en Figma:
- `351:644` (versión A): más simple, "¡Bienvenido, {user}!" + semana + tareas.
- `364:219` (versión B): más completa, "Hola, Andrea" + calendario semanal + "Paquito te avisó" + alerta "VENCE EN 6 H" para Cálculo II Lab 4.

La versión B cubre más JTBD del MVP (Dolor 3: pendientes dispersos con countdown real). **Recomendación**: implementar B primero, A queda como referencia / fallback.

### Tokens observados en Home A (`351:644`) — re-extraído 2026-08-05 (Figma fileKey `EPz2duUog3AaMXRvPX9JNi`)

Este frame es el que se terminó usando como canónico para el Home definitivo (después de descartar la fusión A+B). El layout es: saludo + fecha → card oscura con grid 3x2 de 6 días → card translúcida con 4 tareas + timestamps grandes.

| Token | Valor Figma | Hex Compose | Plataforma |
|---|---|---|---|
| `surface/home-canvas` | `#A8B6BC` (fondo del frame) | `0xFFA8B6BC` | Compose: `PaquitoColors.SurfaceHomeCanvas` |
| `surface/home-week-bg` | `#10151A` (card oscura calendario) | `0xFF10151A` | Compose: `PaquitoColors.SurfaceHomeWeekBg` |
| `surface/home-task-list-bg` | `rgba(245,245,245,0.2)` (wrapper task_list) | `0x33F5F5F5` | Compose: `PaquitoColors.SurfaceHomeTaskListBg` |
| `text/home-strong` | `#000000` (saludo "¡Bienvenido, {user}!") | `0xFF000000` | Compose: `PaquitoColors.TextHomeStrong` |
| `text/home-muted` | `#4D4D4D` (fecha "Lunes, 5 de enero de 2026") | `0xFF4D4D4D` | Compose: `PaquitoColors.TextHomeMuted` |
| `text/home-day-active-label` | `rgba(255,255,255,0.9)` (label día actual) | `0xE6FFFFFF` | Compose: `PaquitoColors.TextHomeDayActiveLabel` |
| `text/home-day-label` | `rgba(28,27,31,0.9)` (label día normal) | `0xE61C1B1F` | Compose: `PaquitoColors.TextHomeDayLabel` |
| `text/home-day-number` | `rgba(0,201,251,0.7)` (número día normal) | `0xB300C9FB` | Compose: `PaquitoColors.TextHomeDayNumber` |
| `text/home-day-number-critical` | `rgba(255,0,0,0.7)` (número día crítico) | `0xB3FF0000` | Compose: `PaquitoColors.TextHomeDayNumberCritical` |
| `text/timestamp-large` | `#29617B` (timestamp normal) | `0xFF29617B` | Compose: `PaquitoColors.TextTimestampLarge` |
| `text/timestamp-large-accent` | `rgba(34,204,255,0.7)` (timestamp urgente) | `0xB322CCFF` | Compose: `PaquitoColors.TextTimestampLargeAccent` |
| `text/timestamp-large-muted` | `rgba(0,201,251,0.5)` (timestamp futuro lejano) | `0x8000C9FB` | Compose: `PaquitoColors.TextTimestampLargeMuted` |
| `text/home-task-label` | `rgba(28,27,31,0.5)` (label "Curso" sobre task info) | `0x801C1B1F` | Compose: `PaquitoColors.TextHomeTaskLabel` ✅ nuevo |

**Nota sobre el "¡Bienvenido, {user}!"**: el placeholder literal del frame Figma. Cuando se conecte el backend, se reemplaza por el nombre real del estudiante.

### Estructura del calendario semanal

A diferencia del Home B (que tenía 7 días en fila horizontal con semáforo de colores), el Home A tiene **6 días en grid 3x2** dentro de una card oscura:

```
[ Lunes  ] [ Martes    ] [ Mierco.. ]
[ 03     ] [ 04        ] [ 05       ]
[ Jueves ] [ Viernes   ] [ Sabado   ]
[ 06     ] [ 07        ] [ 08       ]   <- 08 rojo translúcido (crítico)
```

- El día actual (Lunes) tiene fondo `rgba(211,211,211,0.28)` y label blanco translúcido.
- Los demás días tienen fondo `rgba(255,255,255,0.9)` y label gris translúcido.
- El número del día siempre es 48sp; cambia el color del número según urgencia:
  - Normal: cyan translúcido `rgba(0,201,251,0.7)`.
  - Crítico: rojo translúcido `rgba(255,0,0,0.7)`.

### Estructura del `task info` (lista de tareas)

Layout horizontal (de Figma 365:896):

```
[ icono 24x24 ] [ col (label "Curso"      ] [ timestamp grande 40sp ]
                [       titulo "Nombre Tarea") ]
```

- Icono: vector `ic_paquito_docs_default` (ícono "Docs" del navbar de Figma 365:814).
- Label "Curso": DM Sans Medium 16sp, `rgba(28,27,31,0.5)`.
- Título "Nombre Tarea": DM Sans SemiBold 20sp, `#1C1B1F`.
- Timestamp grande 40sp DM Sans SemiBold: cambia color según urgencia:
  - Normal: `#29617B`.
  - Urgente: `rgba(34,204,255,0.7)`.
  - Futuro: `rgba(0,201,251,0.5)`.

### Assets nuevos / actualizados en `androidApp/src/main/res/drawable/`

| Asset original en Figma | nodeId Figma | Nombre en `drawable/` |
|---|---|---|
| Paquito Bot icon | `I351:645;351:441` | `ic_paquito_bot.xml` (sin cambios, ya existía) |
| Home fill | `I351:645;333:183;242:55` | `ic_paquito_home_fill.xml` (regenerado, 16x18) |
| Book outline | `242:69` | `ic_paquito_book_outline.xml` (regenerado, 16x20) |
| Book fill | `242:71` | `ic_paquito_book_fill.xml` (nuevo, 16x20) |
| Calendar default | `127:177` | `ic_paquito_calendar_default.xml` (nuevo, 18x20) |
| Calendar fill | `146:388` | `ic_paquito_calendar_fill.xml` (regenerado, 18x20) |
| Docs default | `365:814` | `ic_paquito_docs_default.xml` (nuevo, 14x20) |
| Divider horizontal | `535d4587-...` | `ic_paquito_divider.xml` (nuevo, 320x1) |

> **Nota**: el frame nuevo trae SVGs más completos que los que tenía la versión anterior (los originales eran bounding-boxes vacíos `#D9D9D9`). Esta extracción reemplaza los placeholders por paths reales.

## Registro de cambios

- 2026-08-05 — Creación inicial. Skill `figma-extract-paquitobot` creada. Plantillas de theme Compose + SwiftUI con marcadores `0xFF______`. Sin tokens reales todavía.
- 2026-08-05 — Actualización de alcance: backend FastAPI vive en repo aparte; este repo KMP trabaja solo UI por ahora. Capa de datos/red/ViewModels se introduce cuando se conecte el backend al móvil.
- 2026-08-05 — Refactor arquitectural: solo se trabaja Android en este repo. iOS queda fuera de scope (otra persona). `sharedUI/` eliminado del repo. Columna "Estado SwiftUI" removida de las tablas; la cobertura iOS se coordinará aparte.
- 2026-08-05 — Re-extracción del frame `351:644` (Home A) desde el nuevo archivo Figma (`fileKey EPz2duUog3AaMXRvPX9JNi`, mismo canvas `109:97 - Main`). `HomeScreen.kt` sobrescrito con el layout fiel al frame: saludo + fecha → card oscura con grid 3x2 de 6 días → card translúcida con 4 tareas + timestamps grandes 40sp. Fusión A+B descartada (no satisfacía al usuario). 9 íconos nuevos/regenerados: `ic_paquito_home_fill.xml`, `ic_paquito_book_outline.xml`, `ic_paquito_book_fill.xml`, `ic_paquito_calendar_default.xml`, `ic_paquito_calendar_fill.xml`, `ic_paquito_docs_default.xml`, `ic_paquito_divider.xml`. Nuevo token: `TextHomeTaskLabel = Color(0x801C1B1F)`.
- 2026-08-06 — Compactación iterativa de Home + Navbar. `Navbar.kt` rediseñada: tabs compactos (72dp width, 12sp fontSize) + botón Paquito FAB (56x56dp, `shadow(elevation=8dp, shape=CircleShape)` para efecto Floating Action Button) con badge de notificaciones 20dp encima. `HomeScreen.kt` compactado para que el contenido completo (header + 6 días + 4 tareas + navbar) entre en pantallas estándar 360-411dp sin scrollear: saludo 26sp, fecha 18sp, título semana 20sp, card semana 190dp altura, día 28sp/12sp en cell 72dp, tareas 16sp/12sp con timestamp 28sp, padding del body 20dp. Greeting revierte a "¡Bienvenido, {user}!" (literal del Figma, no más "Andrea"). Snapshot del frame actual guardado en `requerimientos/home_a_screenshot_v2.png` como referencia.
- 2026-08-06 — **Auditoría completa del repo + limpieza de imports huérfanos**. Motivada por errores recurrentes (`*.svg` en res/drawable, `android:strokeOpacity`, parámetros invertidos en `Modifier.padding/border`, imports faltantes). Resultados de la auditoría:
  - **Drawables**: 0 problemas. 13 `.xml` válidos, 0 SVGs en el árbol activo, 0 atributos SVG-only. `ic_paquito_home_fill.xml` tiene aspect ratio ligeramente descentrado (16dp ancho vs viewportHeight 20), warning menor.
  - **Kotlin**: 0 errores de compilación. **Warnings de imports no usados** corregidos en `ChatScreen.kt` (10 imports), `HomeBScreen.kt` (PaquitoShapes), `Message.kt` (TextAlign), `TaskList.kt` (Color, RoundedCornerShape), `TaskInfo.kt` (border, Color), `NotificationCard.kt` (PaddingValues). Cruce completo `R.drawable.*` ↔ drawables, `PaquitoColors.*` ↔ theme, todas las referencias resuelven.
  - **Gradle/Manifest**: configuración coherente. `sharedLogic` mantenía targets iOS activos; comentados con nota explicativa (alcance actual Android-only, listos para descomentar si vuelve a incluirse iOS). `sharedUI/` ya no existe (huérfano, fue borrado en commit previo).
  - **`.gitignore`**: reforzadas las reglas contra SVGs en `res/drawable/` (`**/res/drawable/*.svg`, `**/res/drawable/**/*.svg`, `**/res/raw/*.svg`) tras el incidente del `_paquito_bot_raw.svg` untracked que rompía el build.
  - **Nueva skill** `.claude/skills/pre-commit-audit-android/SKILL.md` — checklist ejecutable antes de commit que cubre las 5 categorías (drawables, kotlin, recursos, gradle, docs) y referencia los errores reales que motivaron su creación.
  - **AGENTS.md**: registrada la nueva skill en la lista.
- 2026-08-06 — **Iteración visual post-feedback del compilado** (imágenes 1/2/3). Cuatro correcciones:
  1. **"Mucho espacio vacío en las cards de los días"**: el `WeekDayCell` tenía `horizontalAlignment = Start` y `padding(horizontal = 10.dp)`, lo que dejaba hueco asimétrico blanco a la derecha del texto en cells con labels cortos ("Lunes", "Jueves"). Cambiado a `horizontalAlignment = CenterHorizontally` y `padding(horizontal = 6.dp)`. El "03" ahora se ve centrado dentro del cell, fiel al Figma.
  2. **"Las tareas tienen un borde/contorno #FDFDFD"**: el card wrapper translúcido (`SurfaceHomeTaskListBg = #33F5F5F5`) generaba un rectángulo blanco tenue sobre el fondo `#E5F5FB` del Home. **Eliminado el wrapper**, las tareas ahora se renderizan directamente sobre el fondo del screen con divisores internos entre items, sin card visible alrededor.
  3. **"El navbar está pegado al borde inferior de la pantalla"**: el `Box` contenedor del Navbar en `HomeScreen.kt`, `ChatScreen.kt` y `HomeBScreen.kt` no tenía padding inferior. Unificado a `padding(bottom = 24.dp)` en los tres. Removido también el padding lateral en `HomeBScreen.kt` para evitar doble padding (ya lo maneja el Navbar internamente via `horizontalPadding`).
  4. **"El icono Paquito tiene como dos ojos blancos y una oreja blanca" + "el Navbar tiene un borde/contorno #FFFFFF"**: dos correcciones en uno. El **icono Paquito** (`ic_paquito_bot.xml`) tenía el path del visor con sub-paths internos (`M26.6886,20.3795`, `M35.4154,28.5102`) que el SVG original definía con `fill-rule="evenodd"` creando huecos. Reconstruido el icono como **silueta sólida** sin sub-paths que generen huecos: ahora es 100% negro sin "ojos" ni "oreja" blancos visibles cuando se renderiza sobre el FAB blanco del Navbar. El **fondo blanco translúcido** (`Color.White.copy(alpha = 0.1f)` ≈ #FFFFFF) del pill wrapper de tabs del Navbar se eliminó; los tabs ahora se renderizan directamente sin fondo detrás.
- 2026-08-06 — **Restauración de los contornos y detalle del icono Paquito** (segunda iteración post-feedback, imágenes 4/5). En la iteración anterior se habían eliminado tres detalles del Figma por interpretación errónea del feedback. Esta entrada RESTAURA lo que el usuario quería mantener:
  1. **Contorno del card de Tareas Pendientes**: el usuario quería el **borde visible**, NO eliminar el card. Restaurado el wrapper `SurfaceHomeTaskListBg = #33F5F5F5` Y agregado un borde sutil `1dp @ rgba(255,255,255,0.4) = #66FFFFFF` para que el contorno se vea definido sin ser agresivo.
  2. **Contorno del Navbar (pill de tabs)**: igual, el usuario quería mantenerlo. Restaurado el fondo `Color.White.copy(alpha = 0.08f)` (reducido del 0.10 anterior para que no sea tan invasivo) + borde `1dp @ #66FFFFFF`. Mantiene el "wrapper" de pill de tabs fiel al Figma.
  3. **Icono Paquito — ojos, orejas y casco bien diferenciados**: la versión anterior era silueta sólida sin detalle. Restaurados los **sub-paths internos** del visor (2 "ojos" grandes rectangulares) y del casco (2 "orejas" rectangulares) con `android:fillType="evenOdd"`. Esos huecos se ven BLANCOS sobre el FAB oscuro. Para que el truco funcione, el **fondo del FAB** se cambió de `PaquitoColors.Background` (blanco) a `Color(0xFF1C1B1F)` (grafito oscuro, mismo color que `TextHomeStrong`). Ahora el robot Paquito se ve con casco, ojos grandes blancos y dos orejitas blancas, fiel al frame Figma `351:441`.
  4. **Badge rojo separado del FAB circular** ("la bolita roja se fusiona con el casco"): el badge estaba pegado al FAB porque el contenedor era del mismo tamaño. Se amplió el `Box` contenedor del FAB a **72×72dp** (con el FAB de 56×56dp centrado) y se reposicionó el badge a `Alignment.TopEnd + offset(x=-6.dp, y=-4.dp)`. Ahora el badge asoma por encima del FAB sin tocarlo, fiel al Figma `364:222`.
- 2026-08-06 — **Orejas rectangulares del icono Paquito**: tras la restauración anterior, el usuario reportó que solo se ve "un espacio blanco que parece una oreja debajo del casco en la izquierda". Diagnóstico técnico: el path SVG del casco tiene sub-paths internos que NO dibujan orejas reales (son detalles de antena/crestas) — los sub-paths actuales son `M28.5347,6.27534...` (zigzag de "crestas") y `M3.52886,24.8597...` (cola trasera). Las orejas del Figma `351:441` son paths separados que se perdieron en la conversión original. Solución: se **agregan 2 paths nuevos explícitos** al final del XML del icono (rectángulos blancos sólidos `fillColor=#FFFFFF`) en coordenadas `x=7..11, y=21..27` (oreja izquierda) y `x=39..43, y=21..27` (oreja derecha). Las coordenadas se eligen para que (a) queden dentro del círculo del FAB (radio ~18dp centrado en 25,20), (b) caigan dentro del área del casco negro (que se renderiza antes), y (c) queden visibles porque se pintan al final del XML (último gana en vector drawable). Ahora el robot se ve con sus 2 orejas rectangulares blancas simétricas, fiel al Figma.
