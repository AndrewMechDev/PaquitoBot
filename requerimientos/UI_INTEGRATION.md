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

## Registro de cambios

- 2026-08-05 — Creación inicial. Skill `figma-extract-paquitobot` creada. Plantillas de theme Compose + SwiftUI con marcadores `0xFF______`. Sin tokens reales todavía.
- 2026-08-05 — Actualización de alcance: backend FastAPI vive en repo aparte; este repo KMP trabaja solo UI por ahora. Capa de datos/red/ViewModels se introduce cuando se conecte el backend al móvil.
- 2026-08-05 — Refactor arquitectural: solo se trabaja Android en este repo. iOS queda fuera de scope (otra persona). `sharedUI/` eliminado del repo. Columna "Estado SwiftUI" removida de las tablas; la cobertura iOS se coordinará aparte.
