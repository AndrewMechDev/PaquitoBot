# local-json-data

Branch: `feature/local-json-data` (from `feature/integration-minimax`)

## Objective
Bring the mobile academic functionality (Home, Courses, Schedule, Course detail) from `feature/canvas-mock-data` onto the Clerk + MiniMax line, with data read from a bundled custom JSON instead of the canvas-mock HTTP backend.

## Why
The project no longer uses a backend; all data is custom JSON. canvas-mock (HTTP, API key, LAN IP, cleartext) is obsolete. Auth is Clerk only, so Google Sign-In code must not come along.

## Scope
- Bring: `AcademicRepository` port + domain models, `AcademicDateUtils`, `Academic*ViewModel`s, screen wiring for Home/Courses/CourseDetail/Schedule.
- New: `LocalJsonAcademicRepository` + bundled JSON asset (default; hosting the JSON remotely is out of scope until decided).
- Do NOT bring: `CanvasMock*` (Api, HttpClient, KeyStore, ErrorBanner), `AcademicConnect*` gate, debug manifest cleartext, `GoogleAuthClient` changes, `.debug` applicationId suffix, canvas Google logging, chat files (owned by minimax).

## Constraints
- Files touched by both branches need manual merge: `HomeScreen.kt`, `MainActivity.kt`, `build.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml`. Chat files (`ChatScreen`, `ChatInput`, `Message`) stay as in minimax.
- Conventional commits in Spanish, no AI attribution (user rule).
- Merge to `develop` only with explicit user confirmation (gitflow-merge-directo).

## Tasks
- [x] T1 Port domain: `AcademicRepository` + models, `AcademicDateUtils`. Route: inline (2 mechanical files, comments de-canvas-mocked). Commit: see git log `feat(sharedLogic): portar dominio academico`.
- [ ] T2 `LocalJsonAcademicRepository` + JSON asset + unit test. Route: delegated writer.
- [ ] T3 Port Academic ViewModels and wire Home/Courses/CourseDetail/Schedule, resolving overlaps. Route: delegated writer.
- [ ] T4 Verify: `assembleDebug`, pre-commit audit, senior review (no duplicated screens/dead code).
- [ ] T5 Ask user to confirm merge to `develop`.

## Known open items carried from canvas-mock (still valid in the port)
- Empty-list defaults hide failures; `isToday` wrong in Home; "pendientes" includes graded/overdue; voseo copy (see `requerimientos/PENDIENTES.md` on canvas-mock-data).

## Progress
- Backup: `canvas-mock-data` pushed to origin (cee199e).
- Branch created. Uncommitted `.atl/skill-registry.md` change kept in stash on canvas-mock-data.

## Next step
T1.
