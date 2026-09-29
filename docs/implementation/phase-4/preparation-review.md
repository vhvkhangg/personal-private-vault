# Phase 4 Pre-Handoff Preparation Review

Status: **BLOCKED — PRE-PHASE-4 HYGIENE MAINTENANCE**

## Gate

Do not run `$codex-pre-handoff-review` until:

```text
docs/implementation/phase-3/milestone-review.md
```

has status:

```text
MILESTONE_READY
```

If the milestone review returns `CHANGES_REQUESTED`, resolve that milestone first.

The Phase 1–3 milestone is `MILESTONE_READY`. Before this pre-handoff review may run, the owner-approved
`docs/implementation/maintenance/pre-phase4-code-hygiene/` slice must also be implemented, final-reviewed, and
committed/pushed.

## Scope once unblocked

Review only Phase 4 Fiction preparation. Do not create an implementation handoff during the preparation review.

## Prepared artifacts

- `docs/implementation/phase-4/README.md`
- `docs/implementation/phase-4/preparation-review.md`
- `.agents/skills/fiction-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-4-fiction.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/AGENTS.md`
- roadmap/status/workflow changes created by the Phase 3 closeout

No Phase 4 production Java implementation, Flyway change, or implementation handoff is included.

## Required preparation checks once milestone-ready

- scope matches frozen Schema v1 and module dependency matrix;
- Fiction remains Vault Entry-backed and does not duplicate Vault metadata behavior;
- author source is exactly one Person XOR Creator Group and uses public People contracts;
- Fiction genre stays Fiction-owned; shared story archetypes/world settings stay Reference-owned;
- dependency narrowing uses only verified named interfaces and is deferred to implementation;
- set-like classification assignments have explicit duplicate/race semantics;
- Fiction links do not gain unsupported uniqueness restrictions;
- reads remain bounded;
- tests require PostgreSQL/Testcontainers, transaction rollback, constraints, concurrency where applicable, and
  Modulith verification;
- no new custom agent/hook is introduced without a concrete need.

## Next command

Do **not** run the Phase 4 pre-handoff review yet. Run:

```text
$codex-create-handoff
```

for the approved `pre-phase4-code-hygiene` maintenance scope. After that maintenance is committed/pushed, return
this status to `AWAITING CODEX PRE-HANDOFF REVIEW` and run `$codex-pre-handoff-review`.
