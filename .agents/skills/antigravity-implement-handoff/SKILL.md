---
name: antigravity-implement-handoff
description: Implement the active Codex handoff, write tests, iterate to green, preserve evidence, and return the slice to Codex without committing.
---

# Antigravity Implement Handoff

**MANDATORY CURRENT LOOP:** Read [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md) and the current approved slice. Implement **only** that slice; focused tests first; always invoke [`antigravity-self-review`](../antigravity-self-review/SKILL.md) after implementation and before marking `IMPLEMENTED_AWAITING_CODEX_REVIEW`. The self-review does **not** count as Codex review or a remediation round. Independent Codex **slice review** follows each slice. All CRITICAL/HIGH/MEDIUM findings block; LOW/INFO are deferred to milestone/audit backlog. Permit up to **3** Antigravity corrective + independent re-review rounds **per active slice/handoff** (initial implementation/review do not count). No unapproved R7/GPU/OCR/LLM judge or Phase 19. Maintenance B1 watchdog published (normal 300s / full-suite 2,100s / inactivity diagnosis); B2 tooling enforcement implemented.


Use `/antigravity-implement-handoff`.

For substantial backend work select `backend-implementer` from `/agents`.

## Preconditions

Read root/scoped `AGENTS.md` and `docs/implementation/handoffs/ACTIVE.md`.

Proceed only with `READY_FOR_IMPLEMENTATION` or `CHANGES_REQUESTED`.

If `ACTIVE.md` says `NO_ACTIVE_HANDOFF`, references frozen/completed work as executable scope, or does not cover
the requested production change, **STOP**. Never infer work from frozen phase docs or historical TODOs.

## Engineering skills

Apply relevant skills progressively:

- `java-spring-coding-standards` for Java/Spring changes
- `python-rag-coding` for approved Python extraction, OCR, chunking, embeddings and worker code
- `local-rag-engineering` for RAG source authority, job/worker isolation and Phase 17 frozen contracts
- `bounded-command-execution` for potentially long external commands; actual watchdog invocation required
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`
- `authentication-security` when authentication/JWT/PIN/refresh-token work is in scope
- `graphify-context` when broad navigation is needed

## Loop

1. Read exact handoff references.
2. Use Graphify before broad exploration when available; verify source facts.
3. Implement only approved targets. Preserve all accepted source/permission and publication invariants for changed code. Phase 18 is frozen under D26 CPU-only; do not demand unapproved future R7/GPU/OCR mechanisms or mutate frozen behavior silently.
4. Add/update tests.
5. Iterate focused tests.
6. Do not rerun an unchanged failing full suite for a lucky PASS; use focused evidence and diagnose the failure. Three-remediation-round policy is independent of raw test retry count.
7. Run justified final verification after convergence through `scripts/run-bounded-command.py` (Maintenance B1 published: default 300s / full-suite 2,100s with inactivity diagnosis; B2 tooling enforcement implemented).
8. Record exact command/result/test counts.
9. Run mandatory `/antigravity-self-review` and resolve self-found issues before Codex; do not count it as an independent review or remediation round.
10. Refresh Graphify with `scripts/refresh-graphify.ps1` after meaningful source changes when installed.
11. Mark `IMPLEMENTED_AWAITING_CODEX_REVIEW` and summarize changes/evidence/risks.

Do not commit, push, tag, or create/merge a PR.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement.

- implementation/tests complete and handoff marked `IMPLEMENTED_AWAITING_CODEX_REVIEW` → tell the owner to run
  `$codex-final-review`;
- blocked implementation → state the exact blocker and whether the owner/Codex must clarify or amend the handoff;
- remediation round 1 complete → tell the owner to rerun `$codex-final-review`;
- remediation round 2 complete → independent `$codex-final-review`; if still blocked, Antigravity may do the **third** authorized remediation;
- remediation round 3 complete → independent `$codex-final-review`; if still blocked, stop Antigravity and request bounded `$codex-implementation-takeover` with Owner visibility. No fourth automatic attempt.

Never leave the owner to infer the next workflow action.
