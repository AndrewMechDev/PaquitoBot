# UI Integration Map — catálogo de frames Figma

Snapshot vivo del archivo Figma actual para guiar extracciones. nodeIds verificados con `get_metadata`/`get_design_context` el 2026-08-06.

## Identificadores globales

| Concepto | Valor |
|---|---|
| Archivo | **"Paquito-v2"** (duplicado más reciente del usuario; supersede a "Paquito (copia)") |
| URL base | `https://www.figma.com/design/KCxxCAY076SALBFB5UB4zf/Paquito-v2` |
| fileKey | `KCxxCAY076SALBFB5UB4zf` |
| Página root (única) | `112:146 - assets?` (desactualizado, no usar como fuente de tokens) |
| Canvas principal (fuente de verdad) | `109:97 - Main` |
| Modo edición | DevMode activo en cliente |

> **Historial de archivos**: el proyecto pasó por "Paquito (copia)" (`Piy1K37xHS9jB1qXaaVtuQ`) → "Paquito-v2" (`KCxxCAY076SALBFB5UB4zf`, actual). Si en algún momento aparece un archivo más nuevo, actualizar esta tabla ANTES de extraer nada — los nodeIds no son estables entre duplicados del archivo.

## Frames clave en `109:97 - Main`

### Onboarding (sección `351:538`)

| Frame | nodeId | Estado implementación |
|---|---|---|
| `/` welcome inicial (Paquito bot icono, "Configuremos tu asistente", botón Continuar) | `112:3` | **Implementado** (`WelcomeScreen.kt`) — mascota sola, sin icono 100px. Base visual de `AuthGateScreen`. |
| `/` notificaciones (3 cards verticales: Calificaciones, Plazos de Entrega, Asistencias) | `112:135` | **Implementado** (`OnboardingNotificationsScreen.kt`, variante Compact) |
| `/` notificaciones alternativas (3 cards horizontales opciones) | `156:124` | Pendiente |

### Inicio (sección `351:537`)

| Frame | nodeId | Estado implementación |
|---|---|---|
| `/home` saludo + semana + tareas (versión A, navbar fija, "¡Bienvenido, {user}!") | `351:644` | **Implementado** (`HomeScreen.kt`) — ver notas abajo, tiene desviaciones deliberadas del Figma actual |
| `/home` extendido (header "Hola, Andrea", calendario semanal L-D, "Paquito te avisó" lista, alerta "VENCE EN 6 H" para Cálculo II Lab 4) | `364:219` | Descartada (ver `HomeBScreen.kt`, borrado — pantalla huérfana nunca cableada a la navegación) |

### Chat (`/chat`)

| Frame | nodeId | Estado implementación |
|---|---|---|
| `/chat` welcome + burbujas + input (flecha atrás, "Hola, {nombre}", sin header glassmorphism) | `438:662` | **Implementado** (`ChatScreen.kt`) — reemplaza el diseño anterior (`285:324`, header glassmorphism con avatar+chip LMS). Ver desviaciones abajo. |

#### Desviaciones deliberadas de `438:662` vs código

- **Chip de sincronización**: no está en este frame tal cual, pero se mantiene (fusionado con la zona del saludo) porque reemplaza al divisor "HOY" y al indicador "sincronizado hace 2 min" del header viejo — decisión explícita del usuario, no sacar en una futura re-extracción.
- **Chips de sugerencia** (`SuggestionChip`, fila scrolleable arriba del input): no están en este frame, pero se mantienen restyleados a plano — el usuario los encontró útiles y pidió conservarlos.

#### Desviaciones deliberadas de `351:644` vs código (registrar aquí, no perderlas en la próxima re-extracción)

- **Fondo del frame**: Figma = `bg-white`. Código = `PaquitoColors.SurfaceHomeCanvas = #FFFFFF` (ya alineado).
- **Días de la semana**: Figma muestra 6 días (Lunes-Sábado, grid 2x3). El código muestra **7 días** (agrega Domingo) en **una sola fila** con labels abreviados a 2 letras (`Lu Ma Mi Ju Vi Sa Do`), decisión de producto explícita del usuario — NO revertir a 6 ni al grid 3x3 en una futura re-extracción sin confirmar con el usuario primero.
- **Ícono "Cursos" del Navbar**: usa variante `fill` (`ic_paquito_book_fill`), fiel a Figma.
- **Radio de esquina** de las cards (semana, tareas): 35dp, fiel a Figma.
- **Ícono de foro** al costado de "Tareas Pendientes": ELIMINADO — el Figma actual ya no lo tiene (existía en una iteración vieja).
- **Navbar**: pill de tabs en estilo **glass claro** (sombra + `Brush.verticalGradient` translúcido + borde `Brush.linearGradient` oscuro sutil) — mejora de estilo pedida por el usuario (referencia Samsung Health), **no viene de Figma**.
- **FAB de Paquito**: estilo **glass oscuro** (mismo patrón que la pill, paleta invertida: fondo oscuro translúcido + borde claro sutil). Esto es una decisión técnica, no de Figma: el ícono `ic_paquito_bot.xml` tiene espacio negativo propio en el cuerpo que solo se disimula sobre fondo oscuro/de color (confirmado con `get_screenshot` del nodo `351:441` aislado sobre el fondo cyan del artboard). Un FAB en glass CLARO deja ver ese espacio negativo como mancha blanca — no repetir ese intento sin resolver antes el espacio negativo del ícono.
- **Ícono `ic_paquito_bot.xml`**: `fillColor=#FFFFFF` (blanco), porque el FAB tiene fondo oscuro. Si el fondo del FAB cambia a claro en el futuro, el fill debe volver a `#000000` — y hay que resolver el espacio negativo del cuerpo primero (agrandar ~20% el ícono dentro del círculo ayuda pero no es la causa raíz).

### Pantallas placeholder (en canvas pero vacías/inservibles)

| Sección | nodeId del frame | Notas |
|---|---|---|
| `/chat` | `351:667` | Solo navbar; implementar después |
| `/courses` lista de cursos del ciclo | `351:697` | **Implementado UI mock** (`CoursesScreen.kt`). Figma solo trae navbar; cuerpo de producto (promedio + faltas). |
| `/home` horarios | `351:719` | **Implementado UI mock** (`ScheduleScreen.kt`). Figma solo trae navbar; timeline clases/entregas/faltas. |

### Personaje Paquito (assets gráficos)

| Variante | nodeId | Notas |
|---|---|---|
| PAQUITO 1 (vector autoral) | `374:209` | Muchos sub-vectores; preservar para ícono principal |
| PAQUITO 2 (Gemini removebg) | `375:531` | Versión rápida para avatares |
| PAQUITO 3 (ChatGPT removebg) | `375:535` | Variante alternativa |
| `paquito-bot icon` (el que usa el FAB del Navbar) | `351:441` | 50x40. Fuente real: `svg/paquito-bot icon.svg` en la raíz del repo (gitignoreado). Ver desviación de color arriba. |

## Componentes reutilizables (en `112:146 - assets?` / `242:103 - components`)

| Componente | Símbolo | Notas de variantes |
|---|---|---|
| `paquito-bot icon` | `351:433` | Ícono del bot 50x40 |
| `navbar` | `333:224` | Bottom navbar, íconos: home, book, calendar, lab_profile, frame_person |
| `navbar` estados | `242:198` Default, `242:200` Activate | Reutilizar con parámetro `isActive` |
| `paquito_bot` | `333:210` | Mascota grande 70x64 |
| `task info` | `365:894` | Item de lista de tarea (Home B, `364:219`). **Componente `TaskInfo.kt` borrado 2026-08-06** (huérfano tras descartar Home B) — si se re-extrae Home B en el futuro, recrear desde cero con datos frescos, no asumir que sigue existiendo. |
| `task_list` | `365:973` | Lista vertical de `task info` (Home B). **`TaskList.kt` borrado 2026-08-06**, mismo motivo. |
| `day` | `365:1070` | Día del calendario (Home B). **`Day.kt` borrado 2026-08-06**, mismo motivo. Home A (`351:644`) usa su propio `WeekDayCell` privado dentro de `HomeScreen.kt`, no este componente. |

## Íconos del set (en `112:150 - icons`)

Para exportar todos en una sola operación. Vincular a `home`, `book`, `calendar_month`, `lab_profile`, `frame_person`, `robot_2`, además de los sueltos `forum` (exportado pero SIN USO actualmente, ver desviación arriba), `docs`, `stylus_note`.

Cada ícono tiene 2 estados: `state=Default` (línea) y `state=fill` (relleno). Implementar como `enum class IconState { Default, Fill }` en Compose y como `enum IconStyle` en SwiftUI.

## Marcadores de progreso

- [x] Tokens extraídos de Main (colores del Home A: `SurfaceHomeCanvas`, `SurfaceHomeWeekBg`, `SurfaceHomeTaskListBg`, etc.)
- [x] Theme.kt Compose generado
- [ ] Colors.swift + Typography.swift generados (pendiente, iOS aún no arrancó — ver plan de colaboración en `requerimientos/PROJECT_CONTEXT.md` / conversación con el equipo)
- [x] Íconos exportados → Compose drawable (13 XML en `androidApp/src/main/res/drawable/`)
- [ ] Íconos exportados → iOS imageset (pendiente)
- [ ] Componentes reutilizables implementados como Composables compartidos (hoy están inline en `HomeScreen.kt`/`Navbar.kt`, no extraídos a archivos propios)
- [x] Pantallas Onboarding implementadas (Welcome `112:3` + notificaciones `112:135`/`156:124` + tour 3 dolores; cableadas en `MainActivity` antes de Google)
- [x] Pantalla Inicio implementada (`351:644`, con desviaciones documentadas arriba) — `364:219` descartada
- [x] Pestañas Cursos / Horarios + detalle de curso (UI mock, 2026-08-17; sin body en Figma)
