# Integración con el backend `paquitobot-rag`

> Estado vivo de la conexión entre la app KMP y el backend FastAPI (`C:\Users\dark_\Documents\KMP\paquitobot-rag`, deployado en `https://paquitobot-rag.onrender.com`). Actualizar este doc cada vez que cambie el contrato o el estado del bloqueante de auth.

## Alcance actual (2026-08-13)

`ChatScreen` consume datos reales vía `POST /query`. Toda la app (Home incluido) está detrás de dos gates secuenciales: login con Google, y conexión de Canvas (token pegado manualmente — todavía no hay OAuth de Canvas). Home sigue con datos hardcoded más allá de los gates — decisión explícita del usuario, no ampliar sin confirmar.

## Contrato de endpoints (verificado leyendo el código del backend, no solo su documentación)

| Endpoint | Método | Auth | Request | Response |
|---|---|---|---|---|
| `/auth/login` | POST | — | `{id_token: string}` (Google Sign-In) | `200 {access_token, token_type, expires_in, sub, email}` |
| `/auth/canvas/connect` | POST | JWT backend | Header `X-Canvas-Token` (sin body) | `204 No Content` |
| `/sync` | POST | JWT backend + Canvas conectado | sin body | `202 {status, last_successful_at, last_status, last_error_class, correlation_id}` |
| `/query` | POST | JWT backend + Canvas conectado | `{question: string, language?: string}` | `200 {answer, lang, route, correlation_id}` |
| `/healthz` | GET, HEAD | — | — | GET: estado (Ollama/DB/scheduler). HEAD: mismo liveness, sin body (wake-up móvil). |

`QueryRequest` usa `extra="forbid"` del lado del backend: cualquier campo fuera de `question`/`language` hace que rechace con 422. El `tenant_id` sale del JWT, nunca del body — por diseño, para que un cliente no pueda leer datos de otro tenant.

## ✅ Bloqueante de auth resuelto (2026-08-12)

El compañero de backend implementó `POST /auth/login`: recibe un `id_token` de Google Sign-In, lo verifica contra `google_client_id` (audience) con la librería oficial de Google, y devuelve un JWT propio del backend (HS256, firmado con `BACKEND_SECRET`, claim `sub`). Del lado de la app:

- **Google Sign-In**: `androidApp/.../auth/GoogleAuthClient.kt`, usando Credential Manager (API moderna, reemplaza `GoogleSignInClient`). Requiere dos OAuth Client IDs en el mismo proyecto de Google Cloud: uno "Web" (usado como `serverClientId`/audience, ver `R.string.google_web_client_id`) y uno "Android" (package name + SHA-1 del debug keystore).
- **Almacenamiento del JWT**: `androidApp/.../auth/SecureTokenStore.kt`, `EncryptedSharedPreferences` (AES256-GCM, clave en Android Keystore) — implementa `TokenProvider`, reemplaza a `NoOpTokenProvider`.
- **Login**: `AuthRepository`/`RemoteAuthRepository` (`sharedLogic/domain/auth`, `sharedLogic/data/repository`) consumen `POST /auth/login` vía `PaquitoBotApi.login()`.

## ✅ Flujo de gates a nivel app (2026-08-13)

Decisión del usuario: los gates bloquean **toda la app**, no solo el chat, y una vez pasados no se vuelven a pedir durante la sesión.

```
Abrir app
  └─ ¿Sesión Google válida? (SecureTokenStore.hasValidSession())
       NO → AuthGateScreen ("Conectar con Google")
       SÍ ↓
  └─ ¿Canvas ya conectado? (SecureTokenStore.hasCanvasConnected(), flag local)
       NO → CanvasConnectScreen (input de token + botón "Conectar")
             → POST /auth/canvas/connect → AlertDialog "¡Listo!"
       SÍ ↓
  └─ HomeScreen / ChatScreen normal — ningún gate se repite
```

- **`SessionViewModel`** (`androidApp/.../session/SessionViewModel.kt`): dueño de `isAuthenticated`/`isCanvasConnected` a nivel app. `completeSignIn(idToken)` consume `AuthRepository.loginWithGoogle`, persiste la sesión en `SecureTokenStore` y actualiza el estado. `markCanvasConnected()` lo llama `MainActivity` cuando el usuario cierra el `AlertDialog` de éxito de Canvas.
- **`CanvasConnectViewModel`** (`androidApp/.../ui/canvas/`): llama `CanvasRepository.connect(canvasToken)` → `PaquitoBotApi.connectCanvas()` → `POST /auth/canvas/connect` con header `X-Canvas-Token`. Éxito marca `SecureTokenStore.markCanvasConnected()` (flag local — el backend no expone un `GET` que diga si el tenant ya conectó Canvas, ver limitación abajo), y dispara `CanvasRepository.sync()` → `POST /sync` antes de mostrar el popup de éxito (ver sección de sync abajo).
- **`ChatViewModel`/`ChatScreen`** volvieron a ser solo del chat — ya no manejan `isAuthenticated`, eso vive en `SessionViewModel` un nivel arriba.
- **`MainActivity.kt`** arma `SecureTokenStore` + `GoogleAuthClient` una sola vez y evalúa `sessionUiState` en cada recomposición para decidir `AuthGateScreen` / `CanvasConnectScreen` / `Home`+`Chat`.

**⚠️ Seguridad**: el token de Canvas se escribe directo en el campo de `CanvasConnectScreen`, nunca se pega en el chat del asistente ni se comparte con Claude — es una credencial real del estudiante, igual de sensible que `BACKEND_SECRET`.

**Limitación conocida**: `hasCanvasConnected()` es un flag local (`EncryptedSharedPreferences`), no una consulta al backend. Si se reinstala la app o se borra su almacenamiento sin perder la sesión de Google, va a volver a pedir el token de Canvas aunque el backend ya lo tenga guardado cifrado. Aceptado como v1 — mejora futura: `GET /me` o similar que exponga si el tenant ya tiene Canvas conectado (ver recomendación pendiente abajo).

## 🔴→✅ Bug encontrado y resuelto: `/query` sin datos tras conectar Canvas (2026-08-13)

**Síntoma reportado por el usuario**: recién logueado y con Canvas conectado, `POST /query` desde la app respondía "no pude obtener la lista de cursos", mientras que el mismo `/query` desde Postman (con datos ya sincronizados de una sesión de pruebas anterior) traía todo correctamente.

**Causa raíz** (confirmada leyendo `app/controllers/sync.py` y `app/main.py`): el backend sincroniza datos de Canvas hacia su DB vía `POST /sync`, ya sea manual o por un scheduler en background (`app/sync/scheduler.py`, arrancado en `app/main.py::lifespan`). `/query` responde en base a lo que YA está sincronizado en la DB — no llama a Canvas en vivo. Conectar el token (`/auth/canvas/connect`) no dispara ningún sync; si el scheduler todavía no corrió para ese tenant, `/query` no tiene nada que responder.

**Fix**: `CanvasConnectViewModel.connect()` ahora llama `CanvasRepository.sync()` (→ `PaquitoBotApi.sync()` → `POST /sync`) inmediatamente después de un `connect()` exitoso, antes de marcar el gate como pasado. Si el sync falla (429 `sync_throttled`/`sync_locked`, u otro error), el gate igual se marca como pasado — el token ya está guardado del lado del backend — pero el popup de éxito muestra un aviso ("puede tardar unos minutos en aparecer tu información") en vez del mensaje normal.

**Sin acción del lado del backend** — `/sync` ya existía y funciona tal cual está.

**Segunda vuelta (mismo día)**: el usuario probó de nuevo tras el fix de arriba y le seguía apareciendo el mismo error. Causa: `hasCanvasConnected()` es un flag persistido en disco (`EncryptedSharedPreferences`) que sobrevive actualizaciones de la app — como el usuario ya había pasado el gate de Canvas con la versión ANTERIOR (sin el `sync()` automático), el flag ya estaba en `true`, así que la nueva versión salta directo a Home/Chat sin volver a llamar `/sync` nunca. El fix de "sincronizar solo al conectar" no alcanza para sesiones que ya venían conectadas de antes.

**Fix definitivo**: `SessionViewModel` ahora también dispara `CanvasRepository.sync()` en su `init` cada vez que la app arranca con ambos gates ya pasados (`isAuthenticated && isCanvasConnected`), no solo la primera vez que se conecta Canvas. Es best-effort y silencioso — si el backend lo throttlea (429 `sync_throttled`), no pasa nada, ya había datos de una sync anterior.

## 🔴→✅ Bug encontrado: el logging de Ktor nunca funcionó (2026-08-13)

Auditando un logcat real de 30 minutos de uso (compartido por el usuario) para diagnosticar el problema de arriba: **cero líneas de red aparecían en Logcat**, ni siquiera un `println`. Causa: `install(Logging) { level = LogLevel.INFO }` en `PaquitoBotHttpClient.kt` nunca fijó un `logger` explícito, así que usaba `Logger.DEFAULT` — en JVM/Android eso resuelve vía SLF4J, y sin un binding de SLF4J en el classpath (no hay ninguno en este proyecto) se convierte en un logger mudo. Nunca logueó nada desde que existe este archivo.

**Fix**: `logger = Logger.SIMPLE` explícito (multiplataforma, basado en `println`, no depende de SLF4J). Sigue en `LogLevel.INFO` — solo método+URL+status, nunca headers ni body (el `Authorization` con el JWT no debe aparecer en Logcat).

**Cómo ayudó a diagnosticar el problema de arriba**: el mensaje que vio el usuario ("No he podido obtener la lista de tus cursos...") no coincide con ningún string de `PaquitoBotApiError` — es texto conversacional del LLM. Eso confirma que `/query` respondió `200 OK`: el problema nunca fue login ni token, fue exactamente la falta de datos sincronizados diagnosticada arriba.

**Timeouts (2026-08-14, Fase 1 cliente)**: el default de 20s cortaba el login contra Render dormido y `/query` RAG. Ahora cada operación fija su propio timeout: login/healthz/sync 90s, query 120s, canvas connect 30s. Timeout ya no se mapea como "sin conexión" ni como "Google rechazó".

**Wake-up**: al mostrar el gate de Google (y otra vez al tocar el botón) el cliente llama `HEAD /healthz` para despertar Render antes de `POST /auth/login`. Best-effort, sin auth. Cualquier status HTTP cuenta (si el deploy aún no acepta HEAD, un 405 igual indica que el servicio despertó).

**HttpClient único**: Auth/Canvas/Chat comparten `sharedPaquitoBotHttpClient()` — ya no se crea un CIO por factory.

## Recomendación pendiente de coordinar con el compañero de backend

Las pestañas Inicio / Cursos / Horarios (2026-08-17) son **UI mock**. El chat sigue siendo el único consumidor de datos reales (`POST /query`). Para cablear las pantallas hace falta REST (no tools del LLM):

| Dolor | Pantalla | Endpoint propuesto | Por qué no alcanza `/query` |
|---|---|---|---|
| Cómo voy | Cursos + detalle | `GET /courses` (ciclo actual) y `GET /courses/{id}/grades` (prácticas, labs, pesos, notas) | El agente responde texto; la UI necesita filas estructuradas. El SQL ya existe en tools `get_user_courses_current_term` / submissions. |
| Faltas (límite 5, por confirmar) | Horarios + detalle | `GET /courses/{id}/attendance` (`used`, `limit`, sesiones) | Canvas sync hoy no persiste inasistencias. Hay que confirmar si el LMS de TECSUP expone attendance y el límite institucional. |
| Entregas desordenadas | Home lista | `GET /assignments?due=upcoming` con `source` (`lab` / `practice` / `forum`) | Assignments sí se sincronizan; falta clasificar tipo y un inbox, no un párrafo del RAG. |
| Saludo + flag Canvas | Home / gates | `GET /me` (`short_name`, `canvas_connected`, `current_term`) | El nombre de Canvas está en `users` post-sync; el flag Canvas sigue siendo local. |

**Mientras tanto**: Home/Cursos/Horarios usan `*ScreenData.default()`. Chat usa Google `givenName`.

## Primer uso — cómo visualizarlo

En dispositivo: Welcome → Notificaciones → Tour → Google → Canvas → pestañas. Para repetir: Ajustes → Apps → PaquitoBot → Borrar datos.

En Android Studio: `androidApp/.../ui/FirstRunFlowPreviews.kt` (previews 1–10).

## Manejo de errores (mapeo código HTTP → UX)

| Código / caso | `PaquitoBotApiError` | Mensaje al usuario |
|---|---|---|
| Sin JWT (`TokenProvider` devuelve `null`) | `NotAuthenticated` | "Tu sesión expiró..." |
| 401 (JWT) | `InvalidSession` | "Tu sesión expiró..." |
| 401 en `/auth/login` | `GoogleSignInRejected` | "Google rechazó el inicio de sesión..." (solo 401 real, no timeout) |
| Sheet de Google cancelado | `GoogleSignInFailure.Cancelled` | "Cancelaste el inicio de sesión." |
| Timeout (cold start / RAG / sync) | `RequestTimeout` | "El servidor está despertando..." / "Paquito tardó más de lo normal..." |
| 403 `tenant_credentials_missing` | `CanvasNotConnected` | "Conectá tu cuenta de Canvas..." |
| 401 `canvas_token_invalid` en `/auth/canvas/connect` | `InvalidCanvasToken` | "Canvas rechazó ese token. Revisá que lo copiaste completo." |
| 429 | `RateLimited` | "Estoy respondiendo muchas preguntas, esperá un momento..." |
| 502 / 503 | `ServiceUnavailable` | "El servicio no está disponible..." |
| Sin red (DNS/IO) | `NetworkFailure` | "Sin conexión..." |
| Cualquier otro código | `Unknown` | "Algo salió mal..." |

## Arquitectura del cliente

```
sharedLogic/commonMain/kotlin/pe/tecsup/paquitobot/
├── data/remote/
│   ├── PaquitoBotHttpClient.kt   # HttpClient Ktor (engine CIO), sin loggear headers
│   ├── TokenProvider.kt          # interfaz + NoOpTokenProvider
│   ├── PaquitoBotApiError.kt     # errores tipados (sealed class)
│   ├── PaquitoBotApi.kt          # POST /query + /auth/login + /auth/canvas/connect + /sync + mapeo HTTP
│   └── dto/
│       ├── QueryDto.kt           # QueryRequestDto / QueryResponseDto
│       └── AuthDto.kt            # LoginRequestDto / LoginResponseDto
├── domain/
│   ├── chat/ChatRepository.kt    # interfaz de dominio + ChatAnswer
│   ├── auth/AuthRepository.kt    # interfaz de dominio + AuthSession
│   └── canvas/CanvasRepository.kt # interfaz de dominio (connect + sync)
└── data/repository/
    ├── RemoteChatRepository.kt / ChatRepositoryFactory.kt
    ├── RemoteAuthRepository.kt   # impl real de AuthRepository + createDefaultAuthRepository()
    └── RemoteCanvasRepository.kt # impl real de CanvasRepository + createDefaultCanvasRepository()

androidApp/.../
├── auth/
│   ├── GoogleAuthClient.kt   # wrapper de Credential Manager, necesita Context de Activity
│   └── SecureTokenStore.kt   # implementa TokenProvider, EncryptedSharedPreferences
├── session/
│   └── SessionViewModel.kt   # gates de app: isAuthenticated / isCanvasConnected
├── ui/auth/AuthGateScreen.kt      # gate 1: "Conectar con Google"
├── ui/canvas/
│   ├── CanvasConnectViewModel.kt  # gate 2: POST /auth/canvas/connect
│   └── CanvasConnectScreen.kt
└── ui/chat/
    ├── ChatViewModel.kt   # StateFlow<ChatUiState>, solo mensajes/envío
    └── ChatScreen.kt      # stateless, sin gate propio
```

**Por qué el `HttpClient` no es visible desde `androidApp`**: las dependencias de Ktor en `sharedLogic/build.gradle.kts` son `implementation`, no `api` — `androidApp` solo ve `ChatRepository` (interfaz de dominio) y la función `createDefaultChatRepository()`. Esto evita que Ktor "se filtre" al módulo Android y mantiene la superficie pública de `sharedLogic` mínima.

**Por qué está en `sharedLogic` y no en `androidApp`**: es la misma razón por la que se reactivaron los targets iOS - el compañero de iOS va a necesitar consumir el mismo backend, y CIO es el engine Ktor multiplataforma. Ver nota de riesgo sobre CIO/iOS en `PaquitoBotHttpClient.kt` (no se pudo verificar la compilación real para iOS en esta sesión, desarrollada en Windows sin Xcode).

## Verificación

`./gradlew :androidApp:assembleDebug` → `BUILD SUCCESSFUL`. Verificado en dispositivo real (2026-08-13): login con Google + query end-to-end contra el backend real (con `CanvasNotConnected` antes de que existiera el gate de Canvas). El flujo completo de 2 gates (Google → Canvas) todavía no se probó en dispositivo tras esta iteración — pendiente. Sin verificación de compilación para iOS.
