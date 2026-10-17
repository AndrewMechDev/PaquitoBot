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
- [x] T2 `LocalJsonAcademicRepository` + JSON asset + unit test. Route: delegated writer (4 new files + build file). RED: test failed to compile before the class existed; GREEN: `./gradlew --configure-on-demand :sharedLogic:allTests` passes (12 new tests), re-run by parent. Gradle needs `--configure-on-demand` because `clerk.publishableKey` is missing in `local.properties`. Build file declares serialization plugin/libs directly (catalog has no entry): swap to catalog aliases if the catalog gains them. iOS compile not checked (needs macOS).
- [x] T3 Port Academic ViewModels and wire Home/Courses/CourseDetail/Schedule, resolving overlaps. Route: delegated writer (8 new files, 7 changed). Decisions: injectable `today` with `DEMO_TODAY = "2026-08-03"` in `MainActivity`; failed load keeps previous data and shows `AcademicErrorBanner`; pending = not graded and due >= today; Home calendar = Monday-Sunday week of `today`; Courses `pendingCount` is now the total of pending assignments. Parent spot checks: Clerk/session/chat wiring in `MainActivity` intact, no `CanvasMock`/Google leftovers, two `Text()` without font in the evaluation dialog fixed (pre-commit audit). Evidence: 16 tests green and `assembleDebug` built in a scratchpad copy (dummy Clerk key + test deps only there); in-repo build NOT run (needs `clerk.publishableKey` in `local.properties`).
- [~] T4 Tests adopted: 16 tests in `androidApp/src/test/.../ui/academic/AcademicLogicTest.kt` + `testImplementation` junit and coroutines-test in `androidApp/build.gradle.kts` (same setup that passed in the scratchpad copy). Still pending, user runs it with their Clerk key: verify in-repo: `assembleDebug` + `testDebugUnitTest` once `clerk.publishableKey` exists in `local.properties`; senior review (no duplicated screens/dead code). Pre-commit audit of T3 done (fonts); the rest is covered by the scratchpad compile.
- [ ] T5 Ask user to confirm merge to `develop`.

## Known open items carried from canvas-mock (still valid in the port)
- Empty-list defaults hide failures; `isToday` wrong in Home; "pendientes" includes graded/overdue; voseo copy (see `requerimientos/PENDIENTES.md` on canvas-mock-data).

## Progress
- Backup: `canvas-mock-data` pushed to origin (cee199e).
- Branch created. Uncommitted `.atl/skill-registry.md` change kept in stash on canvas-mock-data.

## Next step
T3: wire Android (`readJson` from `assets/academic.json`) and port Academic ViewModels + screens. The JSON demo "today" is the week of 2026-08-03, so `isToday` must come from the device date, not from the JSON.
