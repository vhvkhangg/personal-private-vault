# Phase 10–12 Milestone Review

Status: **CHANGES_REQUESTED**

Codex [2026-10-04 milestone review](reviews/2026-10-04-phase-10-12-milestone-codex-review.md) found one Medium
blocker, **M10-12-1**: Java/default-locale and PostgreSQL case normalization disagree across owner SQL/tag search,
including an identical Unicode title/query failure under Locale.ROOT and locale-dependent snippet matching.
Independent clean verify passed 815 tests, zero failures/errors/skips, finished 2026-10-04T20:30:19+07:00;
a separate disposable PostgreSQL diagnostic reproduced the missing cases. Those results describe the pre-maintenance baseline.

The owner approved the narrow Search normalization/snippet maintenance scope and ChatGPT prepared the canonical
[maintenance slice](../maintenance/milestone-10-12-search-case-normalization/README.md). Its
[final acceptance](../maintenance/milestone-10-12-search-case-normalization/reviews/2026-10-05-final-codex-acceptance.md)
and [active maintenance handoff](../handoffs/ACTIVE.md) are `READY FOR OWNER COMMIT`; FRM10-12-1 is closed.
Independent maintenance clean verify passed 817 tests. M10-12-1 implementation is accepted, but milestone closure
still requires the owner commit/push and a separate milestone re-review.
Phase 13 remains blocked; milestone status is unchanged until maintenance acceptance and re-review.

Next step: owner commits/pushes the accepted maintenance package, then reruns `$codex-milestone-review`.

## Trigger

Backend Phase 12 PostgreSQL-first Global Search passed final Codex acceptance and the owner committed/pushed the
accepted implementation.

Phases 10, 11, and 12 are now complete/frozen. This milestone review must pass before Phase 13 preparation begins.

## Review window

Review together:

- Phase 10 — `feed` + `importdata`;
- Phase 11 — `finance` + `journal` + `personal`;
- Phase 12 — PostgreSQL-first global `search`.

The review is read-only unless it identifies a blocking defect that the owner later approves as a separate maintenance
slice.

## Canonical evidence

### Phase 10

- `docs/implementation/phase-10/README.md`
- `docs/implementation/phase-10/handoff.md`
- `docs/implementation/phase-10/test-evidence.md`
- `docs/implementation/phase-10/reviews/`

### Phase 11

- `docs/implementation/phase-11/README.md`
- `docs/implementation/phase-11/handoff.md`
- `docs/implementation/phase-11/test-evidence.md`
- `docs/implementation/phase-11/reviews/`

### Phase 12

- `docs/implementation/phase-12/README.md`
- `docs/implementation/phase-12/handoff.md`
- `docs/implementation/phase-12/test-evidence.md`
- `docs/implementation/phase-12/reviews/`

Also consult:

- `docs/architecture/`
- `docs/adr/`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`
- `backend/src/main/resources/db/migration/V2__add_search_support.sql`
- `docs/repository/repository-package-tree.md`
- `docs/architecture/module-dependency-matrix.md`

## Mandatory review dimensions

Use `$codex-milestone-review` and review across Phases 10–12 for:

- Spring Modulith dependency direction across Feed/ImportData, Finance/Journal/Personal, Search, Vault, Knowledge,
  and all Search owner modules;
- accidental public/internal/nested-module leakage introduced by Feed conversion/import orchestration or the Phase 12
  read-only search extensions;
- consistency of parent-facade boundaries, especially `knowledge`/`collection`, and absence of direct nested-owner
  repository access from top-level modules;
- Vault-backed identity/rollback behavior for SavedResource creation/conversion and Search active/tag qualification;
- ImportData lifecycle serialization, whole-job atomicity, parser/privacy boundaries, payload isolation, and
  owner-target rollback behavior;
- Finance ledger source-of-truth rules, wallet/category/reference locking, transaction/recurring aggregate
  serialization, Subscription uniqueness, Personal active-self uniqueness, and module-owned soft delete/restore;
- Search correctness across 17 Vault entry types: literal matching, fuzzy threshold isolation, rank/type ordering,
  required-tag qualification, tag/text deduplication, bounded fan-out, truthful pagination, snippet privacy, and
  read-only semantics;
- Flyway V2 scope discipline: pg_trgm + search indexes only, no accidental logical Schema v1 drift;
- PostgreSQL query shape/performance hazards: load-all/N+1/per-hit cross-module calls, unbounded reads, incorrect
  source LIMIT ordering, index/predicate mismatch, unnecessary locks, or transaction scope expansion;
- privacy/security/logging across imported payloads, URLs/hashes, finance/personal data, search queries/snippets/tags,
  and raw persistence/vendor diagnostics;
- duplicate helpers/competing business rules or over-generalized frameworks across JSON handling, conflict
  translation, bounded reads, soft deletion, ranking, and batch qualification;
- SOLID/cohesion/coupling, package/named-interface consistency, owner responsibility, and design-pattern misuse;
- deterministic concurrency/race tests, PostgreSQL Testcontainers evidence, Flyway/Hibernate fidelity, Spring Modulith
  verification, and retained regression coverage from the 815-test Phase 12 baseline;
- repository/package-tree hygiene, stale placeholders, generated-state drift, stale workflow/status docs, and
  agent/rule/skill instructions that could authorize frozen-phase changes incorrectly;
- technical debt that would materially raise the risk/cost of Phase 13 shared REST/API exposure.

Do not request speculative micro-optimizations or reopen frozen scope without a concrete correctness, architecture,
privacy/security, performance, or next-phase blocking reason.

## Phase 13 readiness boundary

Phase 13 remains blocked during this milestone review.

Review only whether the frozen backend public contracts are sufficiently coherent for the next phase to add:

- shared REST/API contract conventions;
- module HTTP exposure;
- OpenAPI/error/pagination consistency.

Do not prepare or implement Phase 13 in this milestone task. Do not change frozen domain behavior merely to make HTTP
mapping convenient.

## Outcomes

### `CHANGES_REQUESTED`

- record the formal review under `docs/implementation/phase-12/reviews/`;
- set this document to `CHANGES_REQUESTED`;
- classify each blocker as docs/tooling remediation or an owner-approved maintenance implementation slice;
- keep Phase 13 preparation blocked.

If frozen production behavior must change, use the canonical milestone-maintenance workflow:

1. owner explicitly approves a narrow maintenance scope;
2. ChatGPT prepares that maintenance scope;
3. Codex runs `$codex-create-handoff` for the maintenance slice;
4. Antigravity runs `/antigravity-implement-handoff`;
5. Codex runs `$codex-final-review`;
6. owner commits/pushes the accepted maintenance;
7. rerun `$codex-milestone-review`.

### `MILESTONE_READY`

- record the formal review under `docs/implementation/phase-12/reviews/`;
- set this document to `MILESTONE_READY`;
- owner commits/pushes the milestone review/status changes;
- owner gives the latest package to ChatGPT for post-milestone synchronization/reset and Phase 13 preparation;
- only after that synchronization may Phase 13 run `$codex-pre-handoff-review`.

## Invocation

```text
$codex-milestone-review
```
