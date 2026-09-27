---
name: codex-create-handoff
description: Create a concise implementation handoff for Antigravity from the active phase docs and repository state without writing production code.
---

# Codex Create Handoff

Invoke in Codex CLI with `$codex-create-handoff`.

## Purpose

Translate the current approved phase scope into one executable Antigravity handoff.

Codex does **not** implement production code in this step.

## Procedure

1. Read root `AGENTS.md`.
2. Read the active phase document, currently `docs/implementation/backend-phase-1.md`.
3. Read `docs/implementation/handoffs/README.md` and the current `ACTIVE.md`.
4. Inspect `git status --short` and `git diff --stat`.
5. If Graphify is available and a graph exists, use `$graphify-context` or targeted `graphify query` calls before broad source reads.
6. Read only the canonical files required to resolve exact implementation requirements.
7. Write `docs/implementation/handoffs/ACTIVE.md` using the template in `assets/handoff-template.md`.
8. Set `Status: READY_FOR_IMPLEMENTATION`.
9. Include exact implementation targets, acceptance criteria, test expectations, frozen references, non-goals, and known risks.
10. Keep the handoff compact; link to DBML/phase docs rather than copying them.

## Backend Phase 1

The handoff must cover the existing Phase 1 skeleton targets under `reference` and `vault`, Flyway V1 validation,
Lombok-safe implementation conventions, PostgreSQL named enums, capability behavior, and Testcontainers evidence.

Do not add Phase 2 scope.

Do not commit, push, or modify production implementation.
