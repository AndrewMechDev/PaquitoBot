# UI Integration Map — catálogo de frames Figma

Snapshot vivo del archivo "Paquito (copia)" para guiar extracciones. nodeIds verificados con `get_metadata` el 2026-08-05.

## Identificadores globales

| Concepto | Valor |
|---|---|
| Archivo | "Paquito (copia)" (cuenta TECSUP, Antony Andrew Alca Peralta, Full seat) |
| URL base | `https://www.figma.com/design/Piy1K37xHS9jB1qXaaVtuQ/Paquito--copia-` |
| fileKey | `Piy1K37xHS9jB1qXaaVtuQ` |
| Página root (única) | `112:146 - assets?` (desactualizado, no usar como fuente de tokens) |
| Canvas principal (fuente de verdad) | `109:97 - Main` |
| Modo edición | DevMode activo en cliente |

## Frames clave en `109:97 - Main`

### Onboarding (sección `351:538`)

| Frame | nodeId | Estado implementación |
|---|---|---|
| `/` welcome inicial (Paquito bot icono, "Configuremos tu asistente", botón Continuar) | `112:3` | Pendiente |
| `/` notificaciones (3 cards verticales: Calificaciones, Plazos de Entrega, Asistencias) | `112:135` | Pendiente |
| `/` notificaciones alternativas (3 cards horizontales opciones) | `156:124` | Pendiente |

### Inicio (sección `351:537`)

| Frame | nodeId | Estado implementación |
|---|---|---|
| `/home` saludo + semana + tareas (versión A, navbar fija, "¡Bienvenido, {user}!") | `351:644` | Pendiente |
| `/home` extendido (header "Hola, Andrea", calendario semanal L-D, "Paquito te avisó" lista, alerta "VENCE EN 6 H" para Cálculo II Lab 4) | `364:219` | Pendiente (la más completa, candidata a versión final) |

### Pantallas placeholder (en canvas pero vacías/inservibles)

| Sección | nodeId del frame | Notas |
|---|---|---|
| `/chat` | `351:667` | Solo navbar; implementar después |
| `/courses` | `351:697` | Solo navbar |
| `/home` horarios | `351:719` | Solo navbar |

### Personaje Paquito (assets gráficos)

| Variante | nodeId | Notas |
|---|---|---|
| PAQUITO 1 (vector autoral) | `374:209` | Muchos sub-vectores; preservar para ícono principal |
| PAQUITO 2 (Gemini removebg) | `375:531` | Versión rápida para avatares |
| PAQUITO 3 (ChatGPT removebg) | `375:535` | Variante alternativa |

## Componentes reutilizables (en `112:146 - assets?` / `242:103 - components`)

| Componente | Símbolo | Notas de variantes |
|---|---|---|
| `paquito-bot icon` | `351:433` | Ícono del bot 50x40 |
| `navbar` | `333:224` | Bottom navbar, íconos: home, book, calendar, lab_profile, frame_person |
| `navbar` estados | `242:198` Default, `242:200` Activate | Reutilizar con parámetro `isActive` |
| `paquito_bot` | `333:210` | Mascota grande 70x64 |
| `task info` | `365:894` | Item de lista de tarea con badge izquierda, título, curso, hora |
| `task_list` | `365:973` | Lista vertical de `task info` |
| `day` | `365:1070` | Día del calendario 73x63 (variantes para día seleccionado) |

## Íconos del set (en `112:150 - icons`)

Para exportar todos en una sola operación. Vincular a `home`, `book`, `calendar_month`, `lab_profile`, `frame_person`, `robot_2`, además de los sueltos `forum`, `docs`, `stylus_note`.

Cada ícono tiene 2 estados: `state=Default` (línea) y `state=fill` (relleno). Implementar como `enum class IconState { Default, Fill }` en Compose y como `enum IconStyle` en SwiftUI.

## Marcadores de progreso

- [ ] Tokens extraídos de Main → pendiente selección activa del usuario
- [ ] Theme.kt Compose generado
- [ ] Colors.swift + Typography.swift generados
- [ ] Íconos exportados → Compose drawable + iOS imageset
- [ ] Componentes reutilizables implementados
- [ ] Pantallas Onboarding implementadas (3 frames)
- [ ] Pantallas Inicio implementadas (2 frames, decidir cuál es la canónica)
