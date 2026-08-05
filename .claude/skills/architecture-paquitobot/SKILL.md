---
name: architecture-paquitobot
description: "Trigger: arquitectura, MVVM, Clean Architecture, ViewModel, Repository, capa de datos, Android, iOS, Compose, SwiftUI, OneSignal, Twilio, RAG, Supabase, backend FastAPI, estructura de carpetas en PaquitoBot. Define el patron arquitectonico del proyecto KMP y como se integran los servicios externos."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activar cuando:
- Se crea una nueva pantalla, feature o componente en PaquitoBot.
- Se discute dónde va un archivo (ViewModel, repository, model, etc.).
- Se necesita decidir cliente HTTP, capa de red, manejo de errores o loading.
- Se agrega un servicio externo nuevo (OneSignal, Twilio, RAG, Supabase, etc.).
- Se debate MVVM / MVI / Clean Architecture / otra opción.

Contexto PaquitoBot:
- Producto: asistente académico para TECSUP (ProjectContext: 3 dolores, 3 perfiles de estudiante).
- Stack: Kotlin Multiplatform. UI NO compartida (Compose Android + SwiftUI iOS, cada una nativa).
- Backend: FastAPI en repo aparte. Mientras el usuario no indique conexión, la capa de datos usa mocks locales.
- Servicios externos planeados: OneSignal (push), Twilio (aún sin caso confirmado), RAG (no confirmado), Supabase (no confirmado).
- Estado actual: rama `feature/design-figma-extract` con skill de Figma + svg-to-vector-drawable. Falta crear el módulo `composeApp/`.

## Hard Rules

- UI nativa por plataforma. Compose Multiplatform Android y SwiftUI iOS son **independientes**. No compartir composables entre plataformas.
- `commonMain` solo contiene: lógica de ViewModel, modelos de dominio, interfaces de repository. NUNCA composables, NUNCA código SwiftUI, NUNCA acceso a `android.*` o `ios.*`.
- Patrón obligatorio por feature: **MVVM con separación de capas (Clean Architecture "light")**. Tres capas: `presentation/` → `domain/` → `data/`.
- ViewModels exponen `StateFlow` o `LiveData` con un único modelo inmutable por pantalla (`HomeUiState`, etc.). Composable recibe estado + eventos y renderiza.
- Repository devuelve `kotlinx.coroutines.flow.Flow` o `Result` con errores tipados. NUNCA expone `HttpException` ni tipos del backend.
- Mientras no haya backend FastAPI conectado: las implementaciones de repository son `Mock*Repository` con datos hardcoded. Cuando se conecte el back, se agrega `Remote*Repository` y se elige factory (manual o DI).
- Toda pantalla nueva arranca **sin ViewModel** mientras solo se hace UI: el Composable recibe datos hardcoded como parámetros. ViewModel y repository se introducen **cuando se conecte el backend**.
- Servicios externos (OneSignal, Twilio, etc.) se abstraen detrás de interfaces (`NotificationService`, `SmsService`) en `commonMain` y se implementan en `androidMain` / `iosMain`. El ViewModel solo conoce la interfaz.
- **No se introduce M** puro hasta tener 5+ pantallas con estado complejo (ej. /chat). Cuando llegue ese momento, evaluar caso por caso; no por defecto.
- **No se introduce Clean Architecture estricta** (agregado root, dispatcher dedicado, use cases para todo). Solo las tres carpetas + interfaces. Crecer el rigor solo cuando la lógica lo pida.

## Decision Gates

| Necesidad | Acción |
|---|---|
| Crear una pantalla nueva | Definir UiState + (eventualmente) ViewModel con `StateFlow`. Mientras solo UI, los datos van hardcoded en el Composable. |
| Consumir datos del LMS / API | Repository interface (`domain/repository/`) + impl mock (`data/repository/MockLmsRepository.kt`). Cuando llegue el back: agregar impl remota. |
| Notificación push | `interface NotificationService` (commonMain) + impl en androidMain (Firebase/OneSignal) + impl en iosMain (APNs/OneSignal). ViewModel no conoce la impl. |
| Cliente HTTP | Ktor Client con kotlinx.serialization. Configurar en `commonMain` con engines distintos por plataforma (OkHttp en Android, Darwin en iOS). |
| Manejo de errores | `Result<T, AppError>` o jerarquía sellada `sealed class AppError { Network, Timeout, Unauthorized, Unknown }`. Mapear errores del backend en repository. |
| Loading state | Parte del UiState (`Loading | Success(data) | Error(msg)`). NO manejar loading con flags sueltos en el ViewModel. |
| Persistencia local | Para MVP: no. Si hace falta después: Room/SQLDelight en commonMain. No adelantarse. |
| Inyección de dependencias | Manual (factory simple) para MVP. Koin/Hilt/Kodein solo si la complejidad lo pide (>10 ViewModels con dependencias cruzadas). |
| Gráficos / charts | Para MVP no. Si el simulador de escenarios pide gráficos, evaluar vico/compose-charts en Android, Charts en SwiftUI. |
| Componente UI compartido entre pantallas | Crear Composable reutilizable en `presentation/components/`. NO en shared (no existe). |

## Stack y Servicios Externos

| Servicio | Estado real en PaquitoBot (a 2026-08-05) | Acción |
|---|---|---|
| Backend FastAPI | Confirmado, vive en repo aparte. | Mientras no conectado: nada. Al conectar: client Ktor en commonMain. |
| OneSignal | "Implementación real después, UI placeholder por ahora" | Definir `interface NotificationService` desde el inicio pero no impl. Cuando se implemente, decidir OneSignal vs FCM directo. |
| Twilio | Caso de uso aún no definido | NO implementar nada hasta que producto confirme (OTP, alertas de faltas, etc.). |
| RAG / IA generativa | No confirmado | NO implementar nada. Si llega: proveedor externo vía FastAPI, no en KMP. |
| Supabase | No confirmado | NO implementar nada. La auth probablemente viva en FastAPI, no en KMP directo. |

Cuando el usuario confirme cualquiera de estos servicios: actualizar esta tabla, agregar la interfaz correspondiente en `commonMain`, y dejar la implementación real para la sesión que se implemente.

## Execution Steps

### Agregar una pantalla nueva

1. Confirmar que el frame destino está mapeado en `figma-extract-paquitobot/references/ui-integration-map.md` (saber qué nodeId de Figma corresponde).
2. Si se está en fase "solo UI" (sin backend): crear `presentation/screens/<feature>/<Feature>Screen.kt` con datos hardcoded en el Composable. Sin ViewModel.
3. Si ya hay backend conectado:
   a. Crear `presentation/screens/<feature>/<Feature>ViewModel.kt` con `StateFlow<<Feature>UiState>`.
   b. Crear `presentation/screens/<feature>/<Feature>Screen.kt` que recibe estado + callbacks.
   c. Crear `domain/repository/<Feature>Repository.kt` (interface) y `data/repository/Mock<Feature>Repository.kt` (impl).
   d. ViewModel recibe el repository por constructor.
4. Reutilizar componentes de `presentation/components/` y tokens de `theme/`.

### Estructura de carpetas esperada

```
composeApp/
├── src/
│   ├── commonMain/kotlin/dev/paquitobot/
│   │   ├── presentation/
│   │   │   ├── screens/
│   │   │   │   ├── onboarding/
│   │   │   │   └── home/
│   │   │   ├── components/      # Composables reutilizables (Navbar, Day, TaskList)
│   │   │   └── theme/           # Theme.kt + PaquitoColors/Typography/Shapes
│   │   ├── domain/
│   │   │   ├── models/          # data classes puras (Task, Course, LabNote, StudentProfile)
│   │   │   └── repository/      # interfaces (TaskRepository, NotificationService)
│   │   └── data/
│   │       └── repository/      # Mock*Repository hasta conectar backend
│   ├── androidMain/kotlin/dev/paquitobot/
│   │   ├── MainActivity.kt
│   │   └── platform/            # solo si hace falta (NO UI aqui)
│   └── iosMain/kotlin/dev/paquitobot/
│       └── MainViewController.kt

iosApp/iosApp/
├── Screens/         # SwiftUI Views
├── Components/      # SwiftUI views reutilizables
├── Theme/           # Theme.swift + Colors/Typography
├── ViewModels/      # mismos UiState que en Compose
├── Models/          # structs espejo de domain/models/
├── Repository/      # protocolos + impl mock
└── Resources/       # Bundle iOS de assets propios
```

### Conectar backend FastAPI (cuando se decida)

1. Confirmar con el usuario que el backend está listo para ser consumido.
2. Agregar dependencias en `gradle/libs.versions.toml`:
   - `io.ktor:ktor-client-core`
   - `io.ktor:ktor-client-cio` (multiplatform, sirve para Android+iOS)
   - `io.ktor:ktor-client-content-negotiation`
   - `io.ktor:ktor-serialization-kotlinx-json`
   - `org.jetbrains.kotlinx:kotlinx-serialization-json`
3. Configurar HttpClient en commonMain con engines específicos por plataforma si se necesita mayor control.
4. Crear `data/remote/Remote<Feature>Repository.kt` que envuelve la API.
5. Elegir factory de repository (manual para empezar).
6. NO exponer tipos del backend al ViewModel. Mapear a modelos de domain en repository.

## Output Contract

Devolver, en cualquier trabajo arquitectural:
- Estructura de carpetas afectada (`presentation/`, `domain/`, `data/`).
- Interfaces nuevas en `domain/` y sus implementaciones nuevas en `data/`.
- UiState y eventos definidos si se agregó una pantalla.
- Cualquier desviación de las Hard Rules (debe estar justificada).

## References

- `requerimientos/PROJECT_CONTEXT.md` — los 3 dolores y perfiles que justifican las decisiones arquitectónicas.
- `requerimientos/UI_INTEGRATION.md` — estado vivo de la integración Figma↔código.
- `../figma-extract-paquitobot/SKILL.md` — sister skill para extracción de Figma.
- `../svg-to-vector-drawable/SKILL.md` — sister skill para íconos.
