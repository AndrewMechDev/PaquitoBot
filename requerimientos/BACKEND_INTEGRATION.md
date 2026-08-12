# Integración con el backend `paquitobot-rag`

> Estado vivo de la conexión entre la app KMP y el backend FastAPI (`C:\Users\dark_\Documents\KMP\paquitobot-rag`, deployado en `https://paquitobot-rag.onrender.com`). Actualizar este doc cada vez que cambie el contrato o el estado del bloqueante de auth.

## Alcance actual (2026-08-11)

Solo `ChatScreen` consume datos reales, vía `POST /query`. Home sigue con datos hardcoded — decisión explícita del usuario, no ampliar sin confirmar.

## Contrato de endpoints (verificado leyendo el código del backend, no solo su documentación)

| Endpoint | Método | Auth | Request | Response |
|---|---|---|---|---|
| `/auth/canvas/connect` | POST | JWT backend | Header `X-Canvas-Token` (sin body) | `204 No Content` |
| `/sync` | POST | JWT backend + Canvas conectado | sin body | `202 {status, last_successful_at, last_status, last_error_class, correlation_id}` |
| `/query` | POST | JWT backend + Canvas conectado | `{question: string, language?: string}` | `200 {answer, lang, route, correlation_id}` |
| `/healthz` | GET | — | — | estado del servicio (Ollama/DB/scheduler) |

`QueryRequest` usa `extra="forbid"` del lado del backend: cualquier campo fuera de `question`/`language` hace que rechace con 422. El `tenant_id` sale del JWT, nunca del body — por diseño, para que un cliente no pueda leer datos de otro tenant.

## 🔴 Bloqueante: no existe forma de conseguir el JWT del backend

Todos los endpoints exigen `Authorization: Bearer <jwt>` (HS256, firmado con `BACKEND_SECRET`, claim `sub`). El backend **no tiene ningún endpoint que emita ese JWT** — confirmado leyendo `app/controllers/` completo (solo existen `auth.py` para Canvas, `query.py`, `sync.py`, `health.py`). El propio doc del backend (`FLUJO_ENDPOINTS.md`) lo admite: *"OAuth de Canvas... queda fuera de v1"*.

**Decisión (2026-08-11)**: el compañero de backend va a crear el endpoint de login. Del lado de la app queda en **standby** — no se hardcodea ningún JWT ni secreto en el código fuente.

## Cómo está resuelto del lado del cliente (para no bloquear el resto del trabajo)

- `TokenProvider` (interfaz, `sharedLogic/data/remote/TokenProvider.kt`): abstrae de dónde sale el JWT. Hoy la única implementación es `NoOpTokenProvider`, que siempre devuelve `null`.
- Cuando `getToken()` devuelve `null`, `PaquitoBotApi.query()` devuelve `Result.failure(PaquitoBotApiError.NotAuthenticated)` **sin llegar a pegarle a la red** — no tiene sentido mandar una request que el backend va a rechazar con 401.
- `ChatViewModel` traduce ese error a un mensaje de sistema dentro del chat ("Todavía no iniciaste sesión..."), usando el patrón `MessageRole.System` que ya existía en el chat para avisos de conexión.
- **El día que el compañero entregue el login real**: implementar una clase que cumpla `TokenProvider` (ej. lea el JWT de `DataStore`/almacenamiento seguro) y pasarla como parámetro a `createDefaultChatRepository(tokenProvider = ...)` en vez de `NoOpTokenProvider`. Nada más del cliente cambia — ni `PaquitoBotApi`, ni `ChatRepository`, ni `ChatViewModel`.

## Recomendación pendiente de coordinar con el compañero de backend

No hay ningún endpoint que devuelva el nombre del estudiante (ninguno de los 4 endpoints expone perfil). Recomendación: agregar algo tipo `GET /me` que devuelva el nombre ya sincronizado desde Canvas, en vez de que la app hable directo con la API de Canvas. Mantiene el token de Canvas 100% server-side, coherente con cómo está diseñado el resto del backend (el token nunca debería vivir más tiempo del necesario fuera de la DB cifrada).

Mientras tanto, `ChatScreen` sigue mostrando `"{nombre}"` como placeholder (sin regresión — nunca mostró un nombre real).

## Manejo de errores (mapeo código HTTP → UX)

| Código / caso | `PaquitoBotApiError` | Mensaje al usuario |
|---|---|---|
| Sin JWT (`TokenProvider` devuelve `null`) | `NotAuthenticated` | "Todavía no iniciaste sesión..." |
| 401 | `InvalidSession` | "Todavía no iniciaste sesión..." (mismo mensaje, no distinguimos expirado de ausente por ahora) |
| 403 `tenant_credentials_missing` | `CanvasNotConnected` | "Conectá tu cuenta de Canvas..." |
| 429 | `RateLimited` | "Estoy respondiendo muchas preguntas, esperá un momento..." |
| 502 / 503 | `ServiceUnavailable` | "El servicio no está disponible..." |
| Timeout / sin red | `NetworkFailure` | "Sin conexión..." |
| Cualquier otro código | `Unknown` | "Algo salió mal..." |

## Arquitectura del cliente

```
sharedLogic/commonMain/kotlin/pe/tecsup/paquitobot/
├── data/remote/
│   ├── PaquitoBotHttpClient.kt   # HttpClient Ktor (engine CIO), sin loggear headers
│   ├── TokenProvider.kt          # interfaz + NoOpTokenProvider
│   ├── PaquitoBotApiError.kt     # errores tipados (sealed class)
│   ├── PaquitoBotApi.kt          # POST /query + mapeo de códigos HTTP
│   └── dto/QueryDto.kt           # QueryRequestDto / QueryResponseDto (kotlinx.serialization)
├── domain/chat/
│   └── ChatRepository.kt         # interfaz de dominio + ChatAnswer
└── data/repository/
    ├── RemoteChatRepository.kt   # impl real de ChatRepository
    └── ChatRepositoryFactory.kt  # wiring (Ktor queda 100% interno a sharedLogic)

androidApp/.../ui/chat/
├── ChatViewModel.kt   # primer ViewModel real del proyecto - StateFlow<ChatUiState>
└── ChatScreen.kt      # stateless, recibe uiState + onSendMessage (ya no maneja mensajes con remember)
```

**Por qué el `HttpClient` no es visible desde `androidApp`**: las dependencias de Ktor en `sharedLogic/build.gradle.kts` son `implementation`, no `api` — `androidApp` solo ve `ChatRepository` (interfaz de dominio) y la función `createDefaultChatRepository()`. Esto evita que Ktor "se filtre" al módulo Android y mantiene la superficie pública de `sharedLogic` mínima.

**Por qué está en `sharedLogic` y no en `androidApp`**: es la misma razón por la que se reactivaron los targets iOS - el compañero de iOS va a necesitar consumir el mismo backend, y CIO es el engine Ktor multiplataforma. Ver nota de riesgo sobre CIO/iOS en `PaquitoBotHttpClient.kt` (no se pudo verificar la compilación real para iOS en esta sesión, desarrollada en Windows sin Xcode).

## Verificación

`./gradlew :androidApp:assembleDebug` → `BUILD SUCCESSFUL`. Sin verificación end-to-end contra el backend real (no hay JWT válido disponible del lado del cliente todavía — ver bloqueante arriba). Sin verificación de compilación para iOS.
