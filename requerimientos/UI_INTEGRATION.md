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

## Flujo de primer uso (2026-08-17)

Orden en `MainActivity`: Welcome (`112:3`) → Notificaciones (`112:135`) → Tour 3 dolores (sin frame Figma) → Google → Canvas → pestañas Inicio / Cursos / Horarios + Chat (FAB) + detalle de curso.

Visualizar sin reinstalar:
- Android Studio → `ui/FirstRunFlowPreviews.kt` (previews 1–10).
- Dispositivo: borrar datos de la app para volver a ver Welcome.

Pestañas (Figma `351:697` Cursos y `351:719` Horarios solo traen navbar; el cuerpo es de producto, mismos tokens que Home):
- **Inicio**: snapshot Cómo vas / Faltas / Entregas + semana + lista unificada.
- **Cursos**: promedio y faltas por materia → detalle prácticas/labs/foros.
- **Horarios**: clases + entregas + faltas en una línea de tiempo.

Auditoría UX 2026-08-14:
- No apilar icono 100px + mascota en Welcome (se superponían sobre el título).
- No copiar `top: 350dp` de Figma: empujaba notificaciones y escondía Continuar.
- Activity `enableEdgeToEdge` exige `systemBarsPadding` o el CTA queda bajo la barra de gestos.
- Nombre de saludo: Google `givenName`, no `{nombre}` (backend aún sin `GET /me`).

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

**Nota sobre el saludo**: Figma dice `¡Bienvenido, {user}!`. En runtime `MainActivity` inyecta el `givenName` de Google. El default de preview es `estudiante`, no las llaves literales.

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
- 2026-08-06 — **Root cause real del icono Paquito + sync con Figma "Paquito-v2"** (fileKey `KCxxCAY076SALBFB5UB4zf`). El usuario proveyó el SVG fuente real (`svg/paquito-bot icon.svg`, gitignoreado): confirma que las orejas agregadas en la entrada anterior NO existen en el diseño real, fueron un hack. `ic_paquito_bot.xml` reescrito: se eliminan los 2 paths de orejas y se cambia `fillColor` de `#000000` a `#FFFFFF` en los 4 paths reales del SVG (pensado para fondo claro, pero usado dentro del FAB oscuro `#1C1B1F`), manteniendo `fillType=evenOdd` en los paths 3 y 4. Badge offset ajustado de `(-6,-4)` a `(-8,-6)`. Reconexión de MCP Figma completada sobre archivo nuevo "Paquito-v2". Re-extracción de `351:644` vía `get_design_context` reveló 3 diffs reales: (a) el card de semana en Figma actual muestra 6 días (sin Domingo); se decide mantener 7 días pero pasar de grid 3x3 a una sola fila de 7 celdas con labels abreviados a 2 letras (`Lu Ma Mi Ju Vi Sa Do`) para evitar el día huérfano de la 3ra fila; (b) ícono "Cursos" corregido de `ic_paquito_book_outline` a `ic_paquito_book_fill`; (c) radio de esquina de las cards ajustado de 28dp a 35dp. Build verificado (`assembleDebug` OK), sin verificación visual en dispositivo/emulador (no disponible en este entorno).
- 2026-08-06 — **Cierre del frame Home: fondo blanco, FAB/badge recalculados, navbar glass**. Continuación de la sync con "Paquito-v2": (a) `PaquitoColors.SurfaceHomeCanvas` cambiado de `#A8B6BC` a `#FFFFFF` — Figma confirma `bg-white` en el frame `351:644` actualizado; (b) FAB del Navbar recalculado de 56dp a 64dp (contenedor externo 72dp→80dp) para respetar la proporción real de Figma; badge recalculado de 20dp/borde 1.5dp/offset(-8,-6) a 22dp/borde 2.5dp/offset(+3,-3) anclado directo al Box del FAB (no al contenedor externo) — en Figma el badge (23x23px, borde 3px) sobresale apenas ~4px arriba y ~2px a la derecha del FAB, prácticamente pegado, no flotando separado; (c) pill de tabs del Navbar con estilo **glass** (pedido explícito del usuario, referencia Samsung Health, no viene de Figma): fondo cambiado de alpha plano a `Brush.verticalGradient` translúcido, borde cambiado de color sólido a `Brush.linearGradient` diagonal ("glass edge highlight"). Nota técnica: sin blur real (`Modifier.blur` no difumina contenido detrás de la capa; un backdrop blur real requeriría la librería Haze, no agregada). Build verificado (`assembleDebug` OK).
- 2026-08-06 — **Segunda pasada de feedback sobre el cierre del frame**. Tres correcciones: (a) el estilo glass del navbar de la entrada anterior usaba fondo y borde **blancos** translúcidos, pensado para un fondo con color detrás (la referencia Samsung Health tiene fondo rosa/violeta) — pero el Home ahora tiene fondo blanco, así que blanco-sobre-blanco era casi invisible. Redisenado con `shadow(elevation=6.dp)` para que la pill se "levante" del fondo, más opacidad en el fondo translúcido (0.55→0.35 en vez de 0.22→0.08), y borde con tinte **negro** sutil en degradé (0.14→0.04 alpha) en vez de blanco — el patrón real de las cards "frosted glass" sobre fondo claro; (b) eliminado el ícono de foro (`ic_paquito_foro`) al costado de "Tareas Pendientes" — confirmado con `get_design_context` que el frame actual de Figma ya no lo tiene; (c) padding-top del body de `HomeScreen` subido de 50dp a 64dp para bajar un poco el saludo, a pedido del usuario. Build verificado (`assembleDebug` OK).
- 2026-08-06 — **Tercera pasada: FAB glass + icono invertido de vuelta a negro, glass suavizado**. (a) El usuario pidió que el FAB de Paquito use el MISMO patrón glass que la pill de tabs (sombra + fondo translúcido claro + borde oscuro sutil en degradé) en vez del fondo sólido oscuro `#1C1B1F` que tenía. Aplicado el patrón idéntico al `Box` del FAB; (b) como consecuencia, `ic_paquito_bot.xml` vuelve a `fillColor=#000000` (estaba en `#FFFFFF` desde la sync con "Paquito-v2", pensado para fondo oscuro) — con el FAB ahora claro, el robot negro es lo correcto, mismo patrón de contraste que antes pero invertido; (c) el glass de la pill de tabs se suaviza a pedido del usuario ("bájale un poco, que se note pero más suave"): sombra 6dp→4dp, opacidad de fondo 0.55/0.35→0.42/0.24, borde 1.2dp@0.14/0.04→1dp@0.10/0.03. El mismo valor suavizado se reutiliza en el FAB para consistencia visual entre ambos elementos del Navbar. Build verificado (`assembleDebug` OK), sin verificación visual en dispositivo/emulador (no disponible en este entorno).
- 2026-08-06 — **Cuarta pasada: mancha blanca bajo el ícono Paquito, diagnosticada con getBBox() real**. El usuario reportó una mancha/trazo blanco visible debajo del ícono dentro del FAB glass. Primer intento (padding asimétrico 9dp/13.6dp para igualar el aspect ratio 50:40 del ícono) NO la resolvió — el usuario confirmó que seguía visible. Diagnóstico real: se renderizó el SVG del ícono de forma aislada en un servidor HTTP local (`python -m http.server`, ya que el Browser pane no renderiza `file://` fuera del proyecto) y se midió `getBBox()` de cada uno de los 4 paths vía `javascript_tool`. Resultado: el hueco NO es un problema de letterboxing/aspect-ratio — es transparencia REAL dentro del propio SVG, pegada a las esquinas del canvas 50x40 (el casco, path 4, no baja más allá de `y=34.65`; el cuerpo, path 1, solo llega a `y=40` en la franja central `x:9.57-39.78`; fuera de esa franja, en las esquinas inferiores, ningún path pinta nada). Esas esquinas transparentes eran invisibles con el FAB negro sólido de antes, pero visibles como mancha blanca con el FAB glass claro actual. Fix: en `Navbar.kt`, en vez de ajustar el ícono EXACTO al espacio disponible, se agranda ~20% más allá del ajuste perfecto (`Modifier.size(55.dp, 44.dp)` en vez de padding + `fillMaxSize`) para que las esquinas transparentes del canvas queden fuera del radio del círculo del FAB (32dp) y las recorte el propio `clip(CircleShape)`, sin recortar el cuerpo del robot (más central). Build verificado (`assembleDebug` OK), sin verificación visual en dispositivo/emulador (no disponible en este entorno) — recomendado confirmar en un build real antes de dar el fix por cerrado.
- 2026-08-06 — **Quinta pasada: mancha blanca CONFIRMADA en build real, decisión final — FAB "glass oscuro"**. El usuario compiló e instaló en dispositivo real (Samsung Galaxy, logcat adjunto) y confirmó con captura marcada que la mancha seguía visible pese al recorte del 20%. Se pidió un screenshot de Figma del nodo `351:441` (el ícono) aislado, sobre el fondo cyan del artboard: confirma que el cuerpo del robot (path 2) es una forma que se afina/redondea hacia abajo, con espacio negativo alrededor **a propósito** — no es un bug de conversión ni de layout. Ese espacio negativo se disimula sobre cualquier fondo de color u oscuro, pero se nota como mancha blanca sobre un fondo claro. Conclusión: el FAB en "glass claro" (de la pasada anterior) es **incompatible** con este ícono específico. Decisión final para mantener la mejor consistencia visual: el FAB pasa a **"glass oscuro"** — mismo patrón que la pill de tabs (sombra `elevation=8.dp` + `Brush.verticalGradient` translúcido + `Brush.linearGradient` de borde) pero con la paleta invertida: fondo `Color(0xFF2A2930)→Color(0xFF1C1B1F)` (alpha 0.92/0.88) en vez de blanco, borde `Color.White` (alpha 0.28/0.08) en vez de negro. `ic_paquito_bot.xml` vuelve a `fillColor=#FFFFFF` (el fondo del FAB es oscuro de nuevo). El agrandado ~20% del ícono (`Modifier.size(55.dp, 44.dp)`) se mantiene por encuadre visual, aunque ya no es indispensable para tapar la mancha. La pill de tabs mantiene su glass claro sin cambios — solo el FAB cambia de paleta. Build verificado (`assembleDebug` OK); pendiente de confirmación visual del usuario en dispositivo real.
- 2026-08-06 — **Confirmado el fix del FAB glass oscuro** (el usuario probó en dispositivo real y la mancha blanca ya no aparece). A partir de esto, dos frentes nuevos en la misma sesión: (a) **Auditoría de duplicación**: `HomeBScreen.kt` estaba huérfano (no cableado en `MainActivity`, solo su propio `@Preview`), y `HomeScreen.kt` reimplementaba `WeekDayCell`/`TaskInfoRow` privados en vez de reusar `Day.kt`/`TaskInfo.kt` que ya existían para Home B. Se borró `HomeBScreen.kt` (confirmado por el usuario) y, tras verificar con `Grep` que quedaron sin consumidores reales, también `Day.kt`, `TaskInfo.kt`, `TaskList.kt` y `AlertCard.kt` (confirmado). Se creó la skill `senior-review-paquitobot` para prevenir este patrón a futuro (buscar antes de crear, chequear huérfanos tras borrar una pantalla). (b) **Nuevo frame de Chat** (`438:662`, más simple que la iteración anterior en `285:324`): `ChatHeader.kt` reescrito (flecha atrás real — asset SVG descargado de Figma y convertido a `ic_paquito_arrow_back.xml`, ya no un ícono reusado a las apuradas — + "Hola, {nombre}" + subtítulo + chip de estado de sincronización `SyncStatus{Synced,Syncing,Offline}`, fusionando el viejo divisor "HOY" con el indicador de sync del header anterior, a pedido del usuario). `Message.kt` (burbujas planas `#00C9FB`/`#F6F6F6`, sin sombra/borde/gradiente), `ChatInput.kt` (campo y botón planos) y `SuggestionChip.kt` (chip plano, se mantiene a pedido del usuario aunque no está en el frame actual) restyleados para el nuevo lenguaje visual plano. `ChatScreen.kt` actualizado: agrega `onBackClick` (default = `onPaquitoClick`, mismo comportamiento de volver a Home), agrega `userName`/`syncStatus` params. Build verificado (`assembleDebug` OK), sin verificación visual en dispositivo (pendiente).
- 2026-08-06 — **Feedback sobre el Chat recién implementado**: seis ajustes. (a) Texto de las burbujas 20sp→15sp (mismo tamaño que el placeholder del input, antes se veían desproporcionadas); saludo del header 36sp→28sp, subtítulo 20sp→16sp. (b) Ícono de enviar reemplazado: `ic_paquito_send_arrow.xml` (un ícono viejo reciclado, se veía como un "^" en vez de una flecha de enviar) por `ic_paquito_send.xml`, bajado real de Figma (asset `I438:665;435:429`) — el viejo quedó huérfano y se borró. (c) Chip de sincronización: labels simplificados a "Conectado"/"Desconectado"/"Sincronizando..." (antes "Sincronizado con LMS", más largo y ambiguo) y movido a la misma fila que la flecha de volver (antes ocupaba una fila propia debajo del subtítulo), para reducir el alto del header. (d) FAB del Navbar bajado de 64dp a 60dp — medido contra la altura real de la pill de tabs (`NavTabItem`: padding 6dp×2 + ícono 22dp + spacedBy 2dp + texto ≈16dp + padding del Row contenedor 4dp×2 ≈ 60dp) para que calcen exacto, antes el FAB sobresalía. (e) Badge de notificación: offset aumentado de `(3,-3)` a `(6,-6)` para que no se funda visualmente con el casco negro del ícono. (f) **Navbar flotante eliminada de `ChatScreen.kt`**: mostrar el navbar completo (Home/Cursos/Horarios/FAB) en una pantalla de chat a pantalla completa duplicaba la navegación (ya existe la flecha de volver) y rompía la composición visual — a pedido explícito del usuario. `ChatScreen.kt` simplificado: se sacan los params `currentTab`/`onTabSelected`/`notificationCount`/`onPaquitoClick` (ya no aplican sin Navbar), queda solo `onBackClick`. `MainActivity.kt` actualizado al nuevo contrato. Build verificado (`assembleDebug` OK), sin verificación visual en dispositivo (pendiente).
- 2026-08-06 — **Teclado tapa el input + chip de conexión eliminado**. (a) El teclado se superponía al campo de texto en vez de desplazar el contenido limpio (comparado con WhatsApp). Fix: `android:windowSoftInputMode="adjustResize"` en `AndroidManifest.xml` + `Modifier.imePadding()` en el Column raíz de `ChatScreen.kt` — con edge-to-edge activado (`enableEdgeToEdge()`), el manifest solo no alcanza, hace falta leer el inset del IME desde Compose. (b) El chip de estado de conexión en el header (agregado hace dos iteraciones, ya simplificado a "Conectado"/"Desconectado") se sacó por completo — el usuario lo consideró ruido visual que rompía el diseño. En su lugar, se agregó `MessageRole.System` a `Message.kt`: un tipo de mensaje centrado y discreto (chip chico, sin burbuja) para avisos como "sin conexión" o "mensaje no entregado", que aparece DENTRO del flujo de la conversación en vez de como chrome fijo — mismo patrón que WhatsApp usa para este tipo de avisos. No hay lógica real de conexión todavía (el usuario indicó que se agrega después); por ahora es solo la capacidad visual, lista para que el ViewModel la dispare cuando exista. `ChatHeader.kt` vuelve a su forma más simple (solo flecha atrás + saludo + subtítulo). Build verificado (`assembleDebug` OK).
- 2026-08-06 — **Gap del teclado + rediseño de hora/confirmación**. (a) El `imePadding()` de la iteración anterior sí evitaba que el teclado tapara el input, pero el `padding(bottom = 24.dp)` fijo del Column raíz SUMABA por encima (imePadding ya agrega la altura exacta del teclado), dejando un hueco grande entre el input y el teclado. Bajado a `8.dp` — respiro chico cuando el teclado está cerrado, sin duplicar espacio cuando está abierto (comparado contra WhatsApp). (b) El footnote (`"Te aviso otra vez a las 20:00"`, `"Fuente: notas del LMS · 2 ago"`) tenía una caja con borde que se veía "rara/fea" — se sacó el borde, queda como texto simple dentro de la burbuja, sin caja. (c) Se agregó `ChatMessage.timestamp` (hora chica) y `ChatMessage.status: MessageStatus{Sent,Delivered,Read}` (solo para mensajes de usuario): se renderizan abajo a la derecha DE CADA BURBUJA (no de la pantalla), estilo WhatsApp — hora sola en burbujas de bot, hora + palomitas (✓ enviado, ✓✓ gris entregado, ✓✓ celeste leído) en burbujas de usuario. Sin ícono nuevo (no hay asset de Figma para esto, es un patrón nuevo no cubierto por ningún frame) — se usan caracteres de texto (✓/✓✓) en vez de fabricar un vector sin fuente real. Build verificado (`assembleDebug` OK), sin verificación visual en dispositivo (pendiente).
- 2026-08-06 — **Auditoría pre-commit final (skill `senior-review-paquitobot`)**: `ic_paquito_divider.xml` y `ic_paquito_foro.xml` eliminados por no tener ninguna referencia en el código (huérfanos reales). `ic_paquito_home_fill.xml`/`ic_paquito_book_outline.xml`/`ic_paquito_calendar_default.xml` se dejan sin uso a propósito — son parte del set de íconos con 2 estados (Default/Fill) exportado en batch, no huérfanos de una pantalla descartada. `WelcomeScreen.kt`/`OnboardingNotificationsScreen.kt` no están cableados a `MainActivity`, pero se dejan: es el flujo de Onboarding pendiente de integrar (frames `112:3`/`112:135`/`156:124`, estado "Pendiente" en `ui-integration-map.md`), no una versión descartada como HomeBScreen. Build verificado (`assembleDebug` OK).
