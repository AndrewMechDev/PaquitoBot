# Convenciones UI nativas por plataforma (KMP)

El proyecto PaquitoBot es Kotlin Multiplatform. Por especificación del producto (ver `requerimientos/PROJECT_CONTEXT.md`), **la UI NO se comparte**: Android usa Compose Multiplatform y iOS usa SwiftUI nativo. Solo `commonMain` contiene lógica.

Este archivo documenta convenciones de nomenclatura, ubicación y anti-patrones para cada plataforma al integrar assets de Figma.

## Convención de nombres (Figma → código)

| Figma | Kotlin | SwiftUI |
|---|---|---|
| `Component Name` | `ComponentName.kt` | `ComponentName.swift` |
| `token/button/primary` | `PaquitoColors.Brand.Primary` | `PaquitoColor.brandPrimary` |
| `#FFFFFF` (hex) | `Color(0xFFFFFFFF)` | `Color(.hex: "FFFFFF")` |
| `text/title` | `PaquitoTypography.Title` | `PaquitoFont.title` |

Reglas:
- **PascalCase** en Kotlin para clases/funciones.
- **PascalCase** en Swift para tipos; **lowerCamelCase** para propiedades.
- Componentes sin sufijo `Screen` (`HomeScreen.kt` → exponer `HomeContent()` reusable).
- Composables que aceptan `modifier: Modifier` lo reciben **primero**, antes de cualquier otro parámetro.

## Anti-patrones (rechazados en code review)

- ❌ Hex hardcodeado dentro de una pantalla (`Color(0xFFFFAA33)` en `HomeScreen`).
- ❌ Acceso directo al sistema de tipografía del sistema (`MaterialTheme.typography.body1` en una pantalla sin definir el theme).
- ❌ Reimplementar un componente que ya existe en `components/`.
- ❌ `Row { Text("Hola") }` directo en una pantalla cuando debería ir en un `Subtitle(text: ...)` reutilizable.
- ❌ `Color.blue`, `Color.red` literales — todo desde `PaquitoColors`.
- ❌ En iOS: usar `Color(.systemBlue)` desde SwiftUI estándar cuando hay un token equivalente.

## Estructura de carpetas esperada

Aún no existe `composeApp/src/`. Cuando se cree, seguir:

```
composeApp/
├── src/
│   ├── commonMain/kotlin/dev/paquitobot/
│   │   ├── theme/
│   │   │   ├── PaquitoColors.kt
│   │   │   ├── PaquitoTypography.kt
│   │   │   ├── PaquitoShapes.kt
│   │   │   └── Theme.kt
│   │   ├── components/
│   │   │   ├── Navbar.kt
│   │   │   ├── TaskList.kt
│   │   │   └── Day.kt
│   │   └── screens/
│   │       ├── onboarding/
│   │       └── home/
│   ├── androidMain/kotlin/dev/paquitobot/
│   │   └── platform/         // si fuera necesario, NO para UI
│   └── iosMain/kotlin/dev/paquitobot/
│       └── platform/         // solo si alguna API nativa es necesaria
iosApp/
└── iosApp/
    ├── Theme/
    │   ├── Theme.swift
    │   ├── Colors.swift
    │   └── Typography.swift
    ├── Components/
    │   ├── Navbar.swift
    │   ├── TaskList.swift
    │   └── Day.swift
    ├── Screens/
    │   ├── Onboarding/
    │   └── Home/
    └── Assets.xcassets/
        └── icons/            // imagesets por ícono exportado
```

## Theme centralizado

Ambos archivos `Theme` exponen:

- `PaquitoColors` (Compose) / `PaquitoColor` (SwiftUI): paleta semántica (`primary`, `secondary`, `success`, `warning`, `danger`, `surface`, `background`, `onPrimary`, `textPrimary`, `textSecondary`).
- `PaquitoTypography` (Compose) / `PaquitoFont` (SwiftUI): escala tipográfica (`displayLarge`, `headline`, `titleLarge`, `titleMedium`, `bodyLarge`, `bodyMedium`, `caption`, `labelSmall`).
- Espaciados: `spacing.xs`, `spacing.sm`, `spacing.md`, `spacing.lg`, `spacing.xl`.

Cualquier nuevo token va primero al theme; **nunca** se introduce en una pantalla y se "promueve" después.

## Validación visual post-integración

Para cada pantalla implementada:

1. Capturar screenshot de la pantalla nativa (`adb shell screencap` para Android, `xcrun simctl io booted screenshot` para iOS).
2. Llamar `get_screenshot` del frame Figma equivalente.
3. Comparar manualmente o con diff visual — pixel-perfect no es el objetivo, sí lo es la coherencia de tokens.
4. Si hay un componente con tokens diferentes del design system, **bloquear** el commit hasta corregir.
