---
name: gitflow-merge-directo
description: "Trigger: rama, branch, gitflow, merge, feature branch, release, hotfix en PaquitoBot. Estrategia de ramas Gitflow con integración por merge directo, sin pull requests."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Aplica al crear, nombrar o integrar ramas en este repo. Este proyecto NO usa pull requests: toda integración se hace con `git merge` local seguido de push directo a las ramas remotas correspondientes.

## Hard Rules

- Ramas permanentes: `main` (producción/release estable) y `develop` (integración).
- Ramas de trabajo con prefijo obligatorio: `feature/<nombre-corto>`, `release/<version>`, `hotfix/<nombre-corto>`.
- `feature/*` nace de `develop` y se mergea de vuelta a `develop`.
- `hotfix/*` nace de `main` y se mergea a `main` **y** a `develop`.
- `release/*` nace de `develop`, se mergea a `main` (con tag de versión) y de vuelta a `develop`.
- Nunca hacer `git push --force` sobre `main` o `develop`.
- Sin PRs: el propio autor revisa el diff (`git diff develop...feature/x`) antes de mergear.
- Merge sin fast-forward (`git merge --no-ff`) para conservar el historial de la rama en `main`/`develop`.
- **El merge NUNCA es automático, sobre NINGUNA rama** (ni siquiera `develop`, no solo `main`). Siempre preguntar y esperar confirmación explícita del usuario antes de ejecutar `git merge`, sin excepción.
- **Borrar una rama tampoco es automático.** Después de un merge exitoso, preguntar si se borra la rama de trabajo (local y remota) o si se deja — nunca asumir ninguna de las dos opciones por defecto.

## Execution Steps

1. Confirmar rama base correcta (`develop` para feature/release, `main` para hotfix) y traerla actualizada (`git pull`).
2. Crear la rama de trabajo: `git checkout -b feature/<nombre> develop`.
3. Al terminar, revisar el diff completo contra la rama destino antes de integrar.
4. **Preguntar al usuario y esperar confirmación explícita** antes de integrar. Recién con el OK: `git checkout <destino> && git merge --no-ff feature/<nombre>`.
5. Si es `release/*`, además taguear: `git tag -a vX.Y.Z -m "..."` sobre `main`.
6. Push de la(s) rama(s) actualizada(s): `git push origin <destino>` — confirmar antes de pushear si no fue pedido explícitamente en el mismo turno.
7. **Preguntar** si se borra la rama de trabajo ya integrada (local y remota, si existía en remoto). Solo borrar con confirmación explícita.

## Output Contract

Devolver la secuencia exacta de comandos git a ejecutar (checkout, merge, tag, push, delete). Pedir confirmación explícita antes de: (a) cualquier `git merge` sobre cualquier rama, (b) cualquier `git push`, (c) borrar cualquier rama. Nunca asumir "sí" por defecto en ninguno de los tres.

## References

Ninguna referencia externa; adaptación de Gitflow (Vincent Driessen) sin flujo de pull requests.
