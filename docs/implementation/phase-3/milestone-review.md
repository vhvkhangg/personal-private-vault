# Phase 1–3 Milestone Review

Status: **CHANGES_REQUESTED**

The [2026-09-28 Codex milestone review](reviews/2026-09-28-phase-1-3-milestone-codex-review.md)
found two concurrency defects in frozen Phase 1/2 behavior. They require a separate owner-approved,
scoped maintenance implementation slice and PostgreSQL regression coverage before this milestone can be
re-reviewed. Phase 4 pre-handoff review remains blocked.


## Approved maintenance remediation

The owner approved the narrow maintenance implementation scope on 2026-09-28:

[`../maintenance/milestone-1-3-concurrency/README.md`](../maintenance/milestone-1-3-concurrency/README.md)

The milestone remains `CHANGES_REQUESTED` until that maintenance handoff is implemented, final-reviewed,
committed/pushed, and this milestone review is rerun. Phase 4 remains blocked.

## Trigger

Phase 3 has completed final review and the owner has committed/pushed it.

This is the first three-phase milestone review. Phase 0 is intentionally excluded from the cadence.

## Review window

Review together:

- Phase 1 — PostgreSQL/Flyway Schema v1 + `reference` + `vault`;
- Phase 2 — `authentication` + `settings`;
- Phase 3 — `people`.

All three phases are frozen. The milestone review is read-only unless it identifies a blocking defect that the owner
later approves as a separate maintenance slice.

## Canonical evidence

### Phase 1

- `docs/implementation/phase-1/README.md`
- `docs/implementation/phase-1/handoff.md`
- `docs/implementation/phase-1/test-evidence.md`
- `docs/implementation/phase-1/reviews/`

### Phase 2

- `docs/implementation/phase-2/README.md`
- `docs/implementation/phase-2/handoff.md`
- `docs/implementation/phase-2/test-evidence.md`
- `docs/implementation/phase-2/reviews/`

### Phase 3

- `docs/implementation/phase-3/README.md`
- `docs/implementation/phase-3/handoff.md`
- `docs/implementation/phase-3/test-evidence.md`
- `docs/implementation/phase-3/reviews/`

Also consult:

- `docs/architecture/`
- `docs/adr/`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `docs/repository/repository-package-tree.md`
- `docs/architecture/module-dependency-matrix.md`

## Mandatory review dimensions

Use `$codex-milestone-review` and check, across Phases 1–3:

- business/domain invariant consistency;
- duplicate/competing business-rule implementations;
- Spring Modulith dependency direction and public API leakage;
- SOLID/cohesion/coupling and API responsibility drift;
- design-pattern misuse or overengineering;
- JPA/PostgreSQL query shape, transaction boundaries, race handling, and concrete performance hazards;
- authentication/security/privacy/logging boundaries;
- test quality, architecture verification, PostgreSQL evidence, and regression gaps;
- stale docs/status/tooling;
- technical debt that would materially increase Phase 4–6 risk.

Do not request speculative micro-optimizations.

## Outcomes

### `CHANGES_REQUESTED`

Record the milestone review under `docs/implementation/phase-3/reviews/`, set this document to
`CHANGES_REQUESTED`, and identify whether each blocker requires docs/tooling remediation or a separate
owner-approved maintenance implementation slice.

Phase 4 pre-handoff review remains blocked.

### `MILESTONE_READY`

Record the review under `docs/implementation/phase-3/reviews/` and set this document to `MILESTONE_READY`.

Only then may the owner proceed to Phase 4 `$codex-pre-handoff-review`.

## Invocation

```text
$codex-milestone-review
```
