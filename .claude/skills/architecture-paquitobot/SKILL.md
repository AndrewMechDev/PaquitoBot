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

Contexto PaquitoBot (alcance actual, 2026-08-06):
- Producto: asistente académico para TECSUP (3 dolores, 3 perfiles de estudiante).
- Stack de este repo: Android con Jetpack Compose + Material 3, **y ahora también iOS con SwiftUI** (un colaborador se suma a partir de 2026-08-06 — ver decisión abajo).
- **Android e iOS conviven en este mono-repo**. Un colaborador dedicado a iOS trabaja en su propia rama (`feature/ios-<algo>` desde `develop`, vía `gitflow-merge-directo`), consumiendo `sharedLogic` como framework — NO en un fork aparte. Antes de que empiece, hay que descomentar los targets `iosArm64`/`iosSimulatorArm64` en `sharedLogic/build.gradle.kts` (ver Hard Rules).
- UI **no se comparte** entre plataformas: Android usa Compose nativo, iOS usa SwiftUI nativo. `sharedUI/` fue eliminado del repo por contener Composables que iban contra esa regla — no reintroducir un módulo de UI compartida.
- Backend: FastAPI en repo aparte. Mientras el usuario no indique conexión, la capa de datos usa mocks locales.
- Servicios externos planeados: OneSignal (push), Twilio (aún sin caso confirmado), RAG (no confirmado), Supabase (no confirmado).
- Estado actual: rama `feature/design-figma-extract` con skills de Figma + svg-to-vector-drawable + architecture, ya mergeada a `develop`. El módulo `androidApp` ya existe; **la estructura MVVM `presentation/domain/data` documentada en este skill es aspiracional** — el código real de Home/Navbar vive hoy en `androidApp/src/main/kotlin/pe/tecsup/paquitobot/ui/{home,components,theme}/` sin ViewModel (fase "solo UI", datos hardcoded), no en `presentation/screens/`. Al introducir ViewModel + backend real, ahí sí migrar a la estructura de carpetas de abajo.

## Hard Rules

- **Android e iOS conviven en este repo** (mono-repo KMP). Cualquier trabajo de arquitectura debe considerar que `sharedLogic` se consume desde ambas plataformas — no asumir "solo Android" al diseñar interfaces nuevas en `domain/`.
- Patrón obligatorio por feature: **MVVM con separación de capas (Clean Architecture "light")**. Tres capas: `presentation/` → `domain/` → `data/`.
- ViewModels exponen `StateFlow` o `LiveData` con un único modelo inmutable por pantalla (`HomeUiState`, etc.). Composable recibe estado + eventos y renderiza.
- `data/` vive en `androidMain` (no en `commonMain`) porque por ahora solo hay Android. Si en el futuro se vuelve a sumar `commonMain`, las clases puras de Kotlin van ahí.
- Repository devuelve `kotlinx.coroutines.flow.Flow` o `Result` con errores tipados. NUNCA expone `HttpException` ni tipos del backend.
- Mientras no haya backend FastAPI conectado: las implementaciones de repository son `Mock*Repository` con datos hardcoded. Cuando se conecte el back, se agrega `Remote*Repository` y se elige factory (manual o DI).
- Toda pantalla nueva arranca **sin ViewModel** mientras solo se hace UI: el Composable recibe datos hardcoded como parámetros. ViewModel y repository se introducen **cuando se conecte el backend**.
- Servicios externos (OneSignal, Twilio, etc.) se abstraen detrás de interfaces (`NotificationService`) en `data/` o `domain/` y se implementan en `androidMain`. El ViewModel solo conoce la interfaz.
- **No se introduce MVI** puro hasta tener 5+ pantallas con estado complejo (ej. /chat). Evaluar caso por caso.
- **No se introduce Clean Architecture estricta** (agregado root, dispatcher dedicado, use cases para todo). Solo las tres carpetas + interfaces. Crecer el rigor solo cuando la lógica lo pida.

## Decision Gates

| Necesidad | Acción |
|---|---|
| Crear una pantalla nueva | Definir UiState + (eventualmente) ViewModel con `StateFlow`. Mientras solo UI, los datos van hardcoded en el Composable. |
| Consumir datos del LMS / API | Repository interface (`domain/repository/`) + impl mock (`data/repository/MockLmsRepository.kt`). Cuando llegue el back: agregar impl remota. |
| Notificación push | `interface NotificationService` en `domain/` + impl Android en `data/` (Firebase/OneSignal) + impl iOS equivalente cuando el colaborador de iOS la necesite. ViewModel/lado Swift solo conoce la interfaz. |
| Cliente HTTP | Ktor Client con kotlinx.serialization. Configurar dentro de `data/remote/` con engine OkHttp para Android (o CIO multiplataforma si se vuelve a sumar iOS). |
| Manejo de errores | `Result<T, AppError>` o jerarquía sellada `sealed class AppError { Network, Timeout, Unauthorized, Unknown }`. Mapear errores del backend en repository. |
| Loading state | Parte del UiState (`Loading | Success(data) | Error(msg)`). NO manejar loading con flags sueltos en el ViewModel. |
| Persistencia local | Para MVP: no. Si hace falta después: Room/SQLDelight en commonMain. No adelantarse. |
| Inyección de dependencias | Manual (factory simple) para MVP. Koin/Hilt/Kodein solo si la complejidad lo pide (>10 ViewModels con dependencias cruzadas). |
| Gráficos / charts | Para MVP no. Si el simulador de escenarios pide gráficos, evaluar vico/compose-charts en Android, Charts en SwiftUI. |
| Componente UI compartido entre pantallas | Crear Composable reutilizable en `presentation/components/`. NO en shared (no existe). |

## Stack y Servicios Externos

| Servicio | Estado real en PaquitoBot (a 2026-08-13) | Acción |
|---|---|---|
| Backend FastAPI (`paquitobot-rag`) | **Conectado**. Cliente (2026-08-14): un `HttpClient` compartido, `HEAD /healthz` para despertar Render, timeouts por operación (login/sync 90s, query 120s), `RequestTimeout` distinto de red. Ver `requerimientos/BACKEND_INTEGRATION.md`. | Verificar en dispositivo el login de primer intento. |
| Auth del backend (JWT propio, login con Google + Canvas manual) | **Resuelto**. Gates a nivel app. El nombre de Home/Chat sale del `givenName` de Google. Pestañas Cursos/Horarios son UI mock (2026-08-17). | REST: `GET /me`, cursos, notas, asistencia, inbox de entregas. Ver `BACKEND_INTEGRATION.md`. |
| OneSignal | "Implementación real después, UI placeholder por ahora" | Definir `interface NotificationService` desde el inicio pero no impl. Cuando se implemente, decidir OneSignal vs FCM directo. |
| Twilio | Caso de uso aún no definido | NO implementar nada hasta que producto confirme (OTP, alertas de faltas, etc.). |
| RAG / IA generativa | Implementado del lado del backend (`paquitobot-rag`, LangChain + MiniMax). El cliente KMP solo consume `/query`, no implementa nada de RAG. | Sin acción del lado KMP. |
| Supabase | Confirmado del lado del backend (Postgres + PGVector). El cliente KMP no se conecta a Supabase directamente, solo al backend FastAPI. | Sin acción del lado KMP. |

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

### Estructura de carpetas esperada (en `androidApp/`)

```
androidApp/
└── src/main/kotlin/pe/tecsup/paquitobot/
    ├── presentation/
    │   ├── screens/              # (aspiracional - ver nota abajo)
    │   │   ├── onboarding/      # WelcomeScreen, NotificationsScreen
    │   │   └── home/            # HomeScreen, TaskList, etc.
    │   ├── components/          # Navbar, Day, TaskInfo (Composables reutilizables)
    │   └── theme/               # Theme.kt + PaquitoColors/Typography/Shapes/Spacing
    └── MainActivity.kt          # entry point, llama a AppRoot()

# Nota real (2026-08-11): las pantallas hoy viven en ui/{home,chat,onboarding}/
# y ui/components/, NO en presentation/screens/ como sugiere el diagrama de
# arriba - esa estructura es aspiracional para cuando haya mas ViewModels.
# ChatViewModel.kt (primer ViewModel real del proyecto) vive en ui/chat/,
# junto a la screen que consume, no en una carpeta presentation/ separada.

sharedLogic/                     # modulo compartido Android+iOS
└── src/commonMain/kotlin/pe/tecsup/paquitobot/
    ├── domain/chat/              # ChatRepository (interfaz), ChatAnswer (modelo de dominio)
    ├── domain/auth/              # AuthRepository (interfaz), AuthSession (modelo de dominio)
    ├── domain/canvas/            # CanvasRepository (interfaz)
    ├── data/remote/              # PaquitoBotApi (Ktor), TokenProvider, DTOs, errores tipados
    ├── data/repository/          # RemoteChatRepository/RemoteAuthRepository/RemoteCanvasRepository (impl real) + factories de wiring
    └── Platform.kt, Greeting.kt  # utilidades KMP originales del template

androidApp/.../
├── auth/                        # GoogleAuthClient (Credential Manager) + SecureTokenStore (impl real de TokenProvider)
├── session/                     # SessionViewModel - dueño de los gates de app (isAuthenticated / isCanvasConnected)
└── ui/
    ├── auth/AuthGateScreen.kt   # gate 1: login con Google
    └── canvas/                  # gate 2: CanvasConnectViewModel + CanvasConnectScreen

iosApp/                          # proyecto Xcode nativo, consume sharedLogic como framework
└── iosApp/
    ├── Theme/                   # Theme.swift, Colors.swift, Typography.swift
    ├── Components/              # SwiftUI equivalentes a androidApp/ui/components/
    └── Screens/                 # SwiftUI equivalentes a androidApp/ui/{home,onboarding}/
```

### Nota sobre rutas Compose

- Resources (drawables, raw assets) van en `androidApp/src/main/res/` (no `commonMain/composeResources/` porque no hay `commonMain` — UI es Android nativo).
- SVGs convertidos a Vector Drawable XML van en `androidApp/src/main/res/drawable/` (ver skill `svg-to-vector-drawable`).
- Ícono de launcher va en `androidApp/src/main/res/mipmap-*` (ya existe `ic_launcher`).

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
