---
name: codex-final-review
description: Perform the final review of the active handoff after Antigravity implementation; request remediation or return READY FOR OWNER COMMIT with one commit message.
---

# Codex Final Review

Invoke with `$codex-final-review` or select it from `/skills`.

Codex is review-only in this step.

## Preconditions

Read:

- root/scoped `AGENTS.md`;
- `docs/implementation/handoffs/ACTIVE.md`;
- Antigravity test evidence;
- relevant phase/architecture/database docs.

Expected handoff status: `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

## Context strategy

Use targeted Graphify queries first when available, then inspect the exact changed/canonical files.
Do not reread the entire repository.

## Review

Review the diff against the handoff for:

- correctness/business invariants;
- scope completeness and scope creep;
- module boundaries/ownership;
- persistence/Flyway/JPA correctness;
- transaction behavior;
- security/logging;
- Lombok/JPA misuse;
- test quality and PostgreSQL evidence;
- stale documentation.

Record findings under `docs/reviews/` with severity: Critical / High / Medium / Low.

## Outcome: changes required

If any blocking finding exists:

1. Set active handoff status to `CHANGES_REQUESTED`.
2. Add a precise remediation checklist under `## Codex remediation`.
3. Do not edit production code.
4. Do not provide a commit message.
5. Return control to `/antigravity-implement-handoff`.

## Outcome: ready

If no blocking finding exists:

1. Set active handoff status to `READY_FOR_OWNER_COMMIT`.
2. Record `READY FOR OWNER COMMIT` in the review log.
3. Give exactly one recommended Conventional Commit message, appropriate to the slice.
4. Do not commit or push.

## Phase 1 focus

Additionally verify Flyway V1 fidelity, PostgreSQL named enums, internal repository/entity encapsulation,
vault fail-closed capability enforcement, recycle-bin semantics, Lombok safety, and real PostgreSQL/Testcontainers evidence.
