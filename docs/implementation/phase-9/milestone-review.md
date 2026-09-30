# Phase 7–9 Milestone Review

Status: **CHANGES_REQUESTED**

Formal review: [`reviews/2026-09-30-phase-7-9-milestone-codex-review.md`](reviews/2026-09-30-phase-7-9-milestone-codex-review.md).

Codex reviewed owner-committed implementation `b0aa742` on 2026-09-30. Independent verification passed all 583
tests. Three blockers require an owner-approved maintenance implementation slice: isolate Note frontmatter from
managed mutable state; refresh Vocabulary state under its review lock when already loaded; remove per-entry
snapshot merge existence queries. Phases 7–9 remain frozen, and Phase 10 pre-handoff review remains blocked.

The approved maintenance now has a `READY_FOR_IMPLEMENTATION` [active handoff](../handoffs/ACTIVE.md).
Next action: Antigravity `/antigravity-implement-handoff`. After implementation, final acceptance, and owner
commit/push, rerun `$codex-milestone-review`.


## Approved maintenance remediation

The owner approved the narrow Phase 7–9 maintenance scope on 2026-09-30:

[`../maintenance/milestone-7-9-integrity-and-query-shape/README.md`](../maintenance/milestone-7-9-integrity-and-query-shape/README.md)

It covers exactly the three blocking findings:

1. Note frontmatter managed-state isolation;
2. fresh Vocabulary state under the review row lock;
3. known-new follower-snapshot entry persistence without per-entry merge probes.

The milestone remains `CHANGES_REQUESTED` until this maintenance passes final review, is committed/pushed by the
owner, and `$codex-milestone-review` is rerun. Phase 10 remains blocked.

## Trigger

Backend Phase 9 Collection passed final acceptance re-review and the owner committed/pushed the accepted package.

This is the third three-phase milestone review. Review Phases 7–9 together before Phase 10 enters pre-handoff
review.

## Review window

- Phase 7 — `account`;
- Phase 8 — `knowledge` with nested `study`, `information`, `vocabulary`, and `note`;
- Phase 9 — `collection` with nested `music`, `shopping`, and `software`.

All three phases are frozen. The milestone review is read-only unless it identifies a blocking defect that the owner
later approves as a separate maintenance slice.

## Canonical evidence

### Phase 7

- `docs/implementation/phase-7/README.md`
- `docs/implementation/phase-7/handoff.md`
- `docs/implementation/phase-7/test-evidence.md`
- `docs/implementation/phase-7/reviews/`

### Phase 8

- `docs/implementation/phase-8/README.md`
- `docs/implementation/phase-8/handoff.md`
- `docs/implementation/phase-8/test-evidence.md`
- `docs/implementation/phase-8/reviews/`

### Phase 9

- `docs/implementation/phase-9/README.md`
- `docs/implementation/phase-9/handoff.md`
- `docs/implementation/phase-9/test-evidence.md`
- `docs/implementation/phase-9/reviews/`

Also consult:

- `docs/architecture/`
- `docs/adr/`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `docs/repository/repository-package-tree.md`
- `docs/architecture/module-dependency-matrix.md`

## Mandatory review dimensions

Use `$codex-milestone-review` and review across Phases 7–9 for:

- Vault-backed identity and rollback consistency across External Account, Study, Information, Vocabulary, Note,
  Music, Shopping, and Software;
- top-level/nested Spring Modulith dependency direction, named-interface exposure, and accidental internal or nested
  DTO leakage;
- consistency of the closed parent-facade pattern used by Knowledge and Collection, including thin mapping and
  nested-owned business rules;
- duplicate/uniqueness behavior and race translation for External Account IDs, Study YouTube-channel assignment,
  Note imported hashes, Music credits, and Software platform assignments;
- preservation of privacy-safe constraint logging for expected PostgreSQL conflicts;
- transaction/concurrency correctness for account relationships, follower snapshots, Vocabulary review transitions,
  and Collection set assignments;
- bounded-read correctness, deterministic ordering, limit validation, and absence of load-all/count-all/N+1 patterns;
- immutable historical-state behavior for follower snapshots and Vocabulary review history;
- default/full-replacement semantics, especially Account/Knowledge/Collection updates and Shopping purchase state;
- schema/check precision for SRS numeric state, price/currency, purchase timestamps, and composite assignment keys;
- domain-exception translation without raw persistence details crossing module boundaries;
- security/privacy/logging risks involving external IDs, URLs, hashes, profile metadata, frontmatter, or raw payloads;
- SOLID/cohesion/coupling, package/named-interface consistency, duplicate helpers/rules, and overengineering;
- PostgreSQL contention-test determinism, Flyway/Hibernate fidelity, Spring Modulith verification, and evidence
  accuracy;
- repository/package-tree hygiene, stale `.gitkeep`, duplicate/moved files, generated-state drift, and workflow-status
  drift;
- technical debt that would materially increase the risk/cost of Phase 10 Feed/ImportData or later Search/API work.

Do not request speculative micro-optimizations or reopen frozen scope without a concrete correctness, architecture,
privacy/security, or future-phase blocking reason.

## Phase 10 readiness boundary

Phase 10 remains blocked during this milestone review.

Review only whether the already-frozen public integration boundary is sufficient for the next phase:

- `feed` may depend only on public `vault` + parent `knowledge` contracts;
- `importdata` may depend only on public `vault` + parent `knowledge` contracts;
- neither may reach `knowledge.study`, `knowledge.information`, `knowledge.vocabulary`, or `knowledge.note`
  internals/repositories;
- the Knowledge parent facade is the synchronous integration boundary for conversions/imports.

Do not implement or pre-review Phase 10 as part of this milestone task.

## Outcomes

### `CHANGES_REQUESTED`

- record the formal review under `docs/implementation/phase-9/reviews/`;
- set this document to `CHANGES_REQUESTED`;
- classify each blocker as docs/tooling remediation or an owner-approved maintenance implementation slice;
- keep Phase 10 pre-handoff review blocked.

If frozen production behavior must change, use the canonical milestone-maintenance workflow:

1. owner approves a narrow maintenance scope;
2. ChatGPT prepares the maintenance scope;
3. Codex runs `$codex-create-handoff`;
4. Antigravity runs `/antigravity-implement-handoff`;
5. Codex runs `$codex-final-review`;
6. owner commits/pushes;
7. rerun `$codex-milestone-review`.

### `MILESTONE_READY`

- record the formal review under `docs/implementation/phase-9/reviews/`;
- set this document to `MILESTONE_READY`;
- owner commits/pushes milestone review/status changes;
- owner sends the latest package to ChatGPT for post-milestone synchronization/reset and Phase 10 preparation;
- only then may Phase 10 run `$codex-pre-handoff-review`.

## Invocation

```text
$codex-milestone-review
```
