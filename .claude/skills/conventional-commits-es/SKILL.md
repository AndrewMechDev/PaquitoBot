---
name: conventional-commits-es
description: "Trigger: commit, mensaje de commit, conventional commits, git commit en PaquitoBot. Aplica Conventional Commits con mensajes en español para este repo KMP."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Aplica cuando se va a crear un commit en este repo (Kotlin Multiplatform: androidApp, iosApp, sharedLogic, sharedUI). No aplica a mensajes de PR ni a commits de otros repos.

## Hard Rules

- Formato: `tipo(scope opcional): descripción breve en español, imperativo, sin punto final`.
- Tipos válidos: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.
- Scope = módulo afectado cuando el cambio es específico de uno: `androidApp`, `iosApp`, `sharedLogic`, `sharedUI`, `gradle`. Omitir el scope si el cambio cruza varios módulos.
- Cuerpo (opcional, línea en blanco después del título): explicar el POR QUÉ del cambio, no el qué (el diff ya lo muestra).
- Nunca agregar atribución de IA ni "Co-Authored-By" generado por IA.
- Un commit = un cambio lógico coherente. Si el diff mezcla features de distintos módulos sin relación, sugerir dividir en commits separados.

## Execution Steps

1. Revisar `git diff --staged` (o el diff relevante) para identificar módulo(s) tocado(s) y la naturaleza del cambio.
2. Elegir el tipo Conventional Commit correcto según el Decision Gate.
3. Redactar el título en español, imperativo, ≤72 caracteres.
4. Si el cambio no es obvio por el diff, agregar cuerpo con el motivo.
5. Confirmar con el usuario antes de ejecutar `git commit` si no fue pedido explícitamente.

## Decision Gates

| Cambio | Tipo |
|---|---|
| Nueva funcionalidad visible para el usuario | `feat` |
| Corrección de bug | `fix` |
| Solo documentación (README, comentarios) | `docs` |
| Refactor sin cambio de comportamiento | `refactor` |
| Cambios en gradle, dependencias, build | `build` |
| Tests nuevos o corregidos | `test` |
| Config de CI | `ci` |

## Output Contract

Devolver el mensaje de commit propuesto (título + cuerpo opcional) listo para usar en `git commit -m`, sin ejecutar el commit salvo confirmación explícita del usuario.

## References

Ninguna referencia externa; convención basada en Conventional Commits (https://www.conventionalcommits.org/es/).
