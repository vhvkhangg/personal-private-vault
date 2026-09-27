---
name: codex-create-handoff
description: Create a concise implementation handoff for Antigravity from an owner-approved current scope without writing production code.
---

# Codex Create Handoff

Invoke with `$codex-create-handoff`. Codex plans the slice; it does not implement production code here.

## Scope gate

A new handoff requires an **owner-approved current scope**, supplied by the owner's current request or a canonical
phase/slice document explicitly marked active/approved.

If no owner-approved current scope exists, **STOP and ask the owner to define/approve the next phase or slice**.

Never infer next work from frozen phase documents, historical TODOs, archived handoffs, review history, Graphify,
or repository structure. `COMPLETE — FROZEN` phases are historical inputs only.

## Procedure

1. Read root `AGENTS.md`.
2. Establish owner-approved scope.
3. Read `docs/implementation/handoffs/README.md` and `ACTIVE.md`.
4. If another live implementation handoff exists, stop and resolve it first.
5. Inspect `git status --short` and `git diff --stat`.
6. Use Graphify for targeted navigation when available; verify facts in canonical files.
7. Read only files needed for the approved slice.
8. Write `docs/implementation/handoffs/ACTIVE.md` using `assets/handoff-template.md`.
9. Set `Status: READY_FOR_IMPLEMENTATION`.
10. Include targets, invariants, non-goals, acceptance criteria, test/evidence contract, frozen references,
    relevant engineering skills, and known risks.
11. Keep the handoff concise.

## Engineering skills

Reference only relevant skills:
`java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`,
`design-pattern-selection`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`.

Do not commit, push, tag, or modify production implementation.
