---
name: canvas-mock-backend
description: "Trigger: canvas-mock, backend mock, X-Api-Key, stu_001, adm_001, datos mock de cursos/notas/asistencia, probar canvas-mock, JWT admin canvas-mock. Integracion TEMPORAL de PaquitoBot con el backend `canvas-mock` (datos ficticios de cursos/notas/asistencia) para demostrar funcionalidad mientras no hay acceso a la API real de Canvas."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activar cuando:
- Se toca cualquier archivo relacionado a `AcademicRepository`/`CanvasMockApi` en `sharedLogic` o `androidApp`.
- Se necesita levantar, probar o depurar el backend `canvas-mock` localmente.
- Se pide generar un JWT para probar los endpoints `/admin/*` (escritura).
- Se discute si Home/Cursos/Horarios deberían mostrar datos reales o mock.

Contexto (2026-08-19): `canvas-mock` (`C:\Users\dark_\Documents\KMP\canvas-mock`, repo separado, del compañero de backend) es un **mock de la API de Canvas LMS** — mismo shape de datos que Canvas real (cursos, asignaciones, notas, asistencia), pero 100% ficticio. Existe porque **todavía no hay acceso a la API real de Canvas** (ni credenciales institucionales) — sirve para demostrar la funcionalidad completa del producto (Home/Cursos/Horarios con datos reales de verdad, no hardcodeados) mientras se consigue ese acceso. **Es una integración intencionalmente temporal**: el día que haya API real de Canvas, esta capa se reemplaza sin tocar la UI (mismo patrón `AcademicRepository` → cambia la implementación, no el contrato).

La rama `feature/canvas-mock-data` que consume esto es **opcional** - una demo funcional, no necesariamente el camino final de producción. Prioridad: que funcione 100% end-to-end en dispositivo, no perfección arquitectónica.

## Hard Rules

- **Nunca pegar secretos reales de `canvas-mock` en el chat.** `DATABASE_URL` (con password), `JWT_SECRET`, `WEBHOOK_FERNET_KEY` son secretos de ese backend - si aparecen en una conversación, avisar y recomendar rotarlos (mismo criterio que `BACKEND_SECRET` de `paquitobot-rag`).
- El `.env` de `canvas-mock` vive en `C:\Users\dark_\Documents\KMP\canvas-mock\.env` (gitignored, ya armado a partir de los valores que dio el usuario) - no se commitea, no se toca el `.env.example` con valores reales.
- **La app (PaquitoBot) solo necesita `X-Api-Key`** para todo lo que consume (perfil, cursos, notas, asistencia, asignaciones - todo bajo `/api/v1/users/self/*`). El JWT Bearer (HS256, `JWT_SECRET`) es **solo para los endpoints `/admin/*`** (crear/editar cursos, usuarios, etc.) - la app de un estudiante NUNCA llama esos endpoints, así que **no hace falta implementar generación/manejo de JWT en el cliente KMP**.
- La API key mock (`stu_001`, `stu_002`, ...) se guarda con el mismo patrón que el JWT de Google: `EncryptedSharedPreferences` vía un `TokenProvider`, nunca en texto plano en logs.
- No tocar la base de datos compartida de Supabase con `alembic upgrade/downgrade` sin confirmar con el usuario primero - **ya está migrada y sembrada** por el compañero (confirmado 2026-08-19: el historial de Alembic en la DB va más adelante que el checkout local). Correr migraciones a ciegas puede desincronizar el schema.

## Modelo de auth de `canvas-mock` (confirmado en vivo, 2026-08-19)

```
Header               Requerido para          Formato
X-Api-Key             TODAS las requests       Texto plano: "stu_001".."stu_005" (estudiante), "adm_001" (admin)
Authorization: Bearer  Solo POST/PUT/PATCH/DELETE   JWT HS256 firmado con JWT_SECRET (cualquier payload sirve, no hay cross-check contra el X-Api-Key)
```

`X-Api-Key` se compara con bcrypt contra `users.api_key_hash` en la tabla `users` (prefijo `adm_`/`stu_` determina el rol). El backend NO es Canvas real - no reusar el flujo de `paquitobot-rag` (`/auth/canvas/connect`, que prueba el token contra la API real de Canvas) para esto: son dos backends y dos modelos de auth totalmente distintos.

## Endpoints relevantes para la app (todos GET, prefijo `/api/v1/users/self`)

| Endpoint | Devuelve |
|---|---|
| `/profile` | `{id, name, email, role, created_at}` |
| `/courses` | Lista de cursos del estudiante (enrollment activo) o todos (admin) |
| `/favorites/courses` | Igual a `/courses` (compat Canvas) |
| `/grades` | Notas del caller en TODOS sus cursos |
| `/attendance?days=N` | Asistencia del caller, últimos N días (default 14) |
| `/courses/{id}/assignments` | Asignaciones de un curso (requiere estar enrolado) |
| `/courses/{id}/class_sessions` | Sesiones de clase de un curso |
| `/courses/{id}/grades` | Notas del caller en ese curso |
| `/courses/{id}/attendance` | Asistencia del caller en ese curso |
| `/assignments/{id}` | Detalle de una asignación |

`/admin/*` (courses, users, enrollments, assignments, attendance, grades, webhooks) son CRUD completo pero **solo rol admin** - no consumidos por la app de estudiante.

## Usuarios seed disponibles (`alembic/versions/0002_seed.py`)

| API Key | Rol | Nombre | Cursos |
|---|---|---|---|
| `adm_001` | admin | Dr. Ada Lovelace | ve todos los cursos del tenant |
| `stu_001` | student | Mateo Rivera | CS101, MATH201 |
| `stu_002` | student | Hana Park | MATH201, PHIL110 |
| `stu_003` | student | Diego Silva | PHIL110, CS101 |
| `stu_004` | student | Priya Patel | CS101, MATH201 |
| `stu_005` | student | Sam Chen | MATH201, PHIL110 |

## Bugs reales encontrados en `canvas-mock` (2026-08-19, parcheados solo LOCAL, avisar al compañero)

| Bug | Archivo | Fix aplicado localmente |
|---|---|---|
| `alembic upgrade` rompe con `ValueError: invalid interpolation syntax` si el password de `DATABASE_URL` tiene `%` codificado (ej. `%23`, típico de Supabase) | `alembic/env.py` | `config.set_main_option("sqlalchemy.url", settings.database_url.replace("%", "%%"))` — escapar `%` antes de pasarlo a `configparser` |
| `ModuleNotFoundError: No module named 'psycopg2'` al migrar/correr — el proyecto declara `psycopg[binary]` (v3), pero `DATABASE_URL` sin esquema explícito hace que SQLAlchemy default a `psycopg2` | `.env` (`DATABASE_URL`) | Usar el esquema `postgresql+psycopg://` en vez de `postgresql://` |

Estos NO están commiteados en el repo `canvas-mock` (no es este repo) - son fixes locales al `.env`/`env.py` de esa carpeta. Si el compañero corre `alembic` en su máquina, le va a pasar lo mismo.

## Levantar el backend localmente (para probar)

```bash
cd "C:\Users\dark_\Documents\KMP\canvas-mock"
python -m venv .venv
.venv\Scripts\python.exe -m pip install -e ".[dev]"
# .env ya armado (gitignored) con DATABASE_URL (+psycopg), JWT_SECRET, WEBHOOK_FERNET_KEY
.venv\Scripts\python.exe -m uvicorn app.main:app --port 8811
```

Probar auth (PowerShell/bash con curl):
```bash
curl http://127.0.0.1:8811/healthz
curl http://127.0.0.1:8811/api/v1/users/self/profile -H "X-Api-Key: stu_001"
```

## Generar un JWT para probar `/admin/*` (Postman, no lo usa la app)

```bash
cd "C:\Users\dark_\Documents\KMP\canvas-mock"
.venv\Scripts\python.exe -c "
from app.core.security import sign_jwt
from app.core.config import Settings
print(sign_jwt({'sub': 'adm_001'}, Settings(), ttl_seconds=86400))
"
```
Devuelve un JWT válido 24h. Usar en Postman como `Authorization: Bearer <token>` junto con `X-Api-Key: adm_001`.

## Conectar desde el emulador/dispositivo Android

- **Emulador Android**: `http://10.0.2.2:8811` (alias fijo de Android para el `localhost` de la máquina host).
- **Dispositivo físico**: IP LAN de la PC donde corre `uvicorn` (ej. `http://192.168.x.x:8811`), y el firewall de Windows tiene que permitir esa conexión entrante. El backend no está deployado en ningún lado público todavía.

## Arquitectura del lado del cliente KMP

Mismo patrón que `paquitobot-rag` (`architecture-paquitobot` skill) - interfaz en `domain/`, implementación real en `data/`, factory de wiring, sin exponer Ktor fuera de `sharedLogic`:

```
sharedLogic/commonMain/kotlin/pe/tecsup/paquitobot/
├── data/remote/
│   ├── CanvasMockHttpClient.kt   # HttpClient Ktor propio (base URL distinta a paquitobot-rag)
│   ├── CanvasMockApi.kt          # header X-Api-Key, GET /users/self/*
│   └── dto/AcademicDto.kt        # UserProfileDto, CourseDto, GradeDto, AttendanceDto, AssignmentDto
├── domain/academic/
│   └── AcademicRepository.kt     # interfaz de dominio (profile, courses, grades, attendance)
└── data/repository/
    └── RemoteAcademicRepository.kt + factory

androidApp/.../auth/
└── CanvasMockKeyStore.kt         # implementa TokenProvider, guarda la api key mock (EncryptedSharedPreferences)
```

## References

- `../architecture-paquitobot/SKILL.md` — patrón general de capas del proyecto.
- `../../requerimientos/BACKEND_INTEGRATION.md` — integración con `paquitobot-rag` (Chat, login, Canvas real) - NO confundir los dos backends.
- Repo del backend: `C:\Users\dark_\Documents\KMP\canvas-mock`.
