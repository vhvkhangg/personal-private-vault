# Phase 12 Pre-Handoff Preparation Review

Status: **READY FOR HANDOFF**

Latest review: [2026-10-04 Codex pre-handoff acceptance](reviews/2026-10-04-phase-12-pre-handoff-codex-acceptance.md).
P12-1/P12-2 closed; no blocking preparation findings remain. Owner commits/pushes preparation, then invokes
`$codex-create-handoff`. No handoff is active and no production implementation is authorized by this review alone.

## Preconditions

- Phase 11 Finance + Journal + Personal is complete/frozen after final acceptance and owner commit/push.
- Phase 11 independent final verification recorded 787 passing tests.
- Phase 11 itself does not require a milestone review.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.

## Scope

Review Phase 12 PostgreSQL-first global Search preparation only. Do not create an implementation handoff during this
review.

## Prepared artifacts

- `docs/implementation/phase-12/README.md`
- `docs/implementation/phase-12/preparation-review.md`
- `docs/implementation/phase-12/reviews/README.md`
- `.agents/skills/global-search-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-12-global-search.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/search/AGENTS.md`
- narrow Phase 12 search-extension clauses in searchable-module AGENTS files
- existing implementer/auditor skill routing updated for the Phase 12 skill

No Phase 12 production Java/test implementation or Flyway V2 migration is included in this preparation patch.

## P12-1 remediation

The canonical contract now requires:

- global/source order = rank DESC, similarity DESC, textual type name ASC, Vault ID ASC;
- `primaryText` is display-only and cannot change top-K membership;
- Vault applies active/type/domain/required-tag AND qualification before tag-source limiting;
- multiple matching tags collapse to one entry using best tag similarity before limiting;
- text-origin required-tag filtering continues across bounded module pages rather than post-filtering a fixed prefix;
- feature bulk lookup materializes only the already-selected tag top-K;
- adversarial PostgreSQL regressions cover reverse ID/title order, offset/cross-domain ties, duplicate matching tags,
  required-tag late qualifiers, text/tag overlap, and bounded batch query shape.

No projection table, cross-module Vault->feature dependency, fixed oversampling factor, load-all, or production code is
introduced by this preparation remediation.

## P12-2 remediation

The canonical type tie key is now explicit and identical across persistence and Java:

- SQL/native source queries expose `CAST(<entry_type> AS text) COLLATE "C" AS type_name`;
- source `ORDER BY` uses `type_name ASC`, never native PostgreSQL enum order;
- Java/global ordering uses `VaultEntryType.name()` ascending, never enum ordinal/natural order;
- ASCII enum labels make PostgreSQL `C` collation and Java string ordering equivalent;
- PostgreSQL regressions must cover equal-score `ALBUM`/`IMAGE` candidates, cross-domain ties, multi-type Media text
  search, LIMIT/OFFSET, and more-than-K sets so post-limit resorting cannot mask an incorrect source order.

P12-1's ownership-preserving tag top-K design remains unchanged.

## Required Codex checks

Verify at minimum:

- Search remains a no-table leaf/orchestration module;
- the frozen dependency matrix is unchanged and excludes Finance/Journal/Personal;
- supported results map exactly to the 17 Vault entry types listed in the canonical README;
- non-Vault entities do not silently become standalone global results;
- adding read-only `search` named interfaces to frozen modules is explicitly authorized without reopening mutations;
- Knowledge/Collection expose only parent-owned search contracts to top-level Search;
- Vault owns active/trash and tag qualification, with bounded batch APIs rather than per-hit lookups;
- tag-name matching plus required-tag AND filtering are coherent and exclude Film Credit from tagged results;
- text/tag duplicate hits deduplicate by Vault ID;
- query normalization, literal `%`/`_` handling, case-insensitive partial semantics, fuzzy >= 0.30 for query length >= 3,
  and no accent folding are explicit;
- searchable field inventory is compatible with Schema v1 and module ownership;
- rank buckets are comparable across modules and every source/global comparator is exactly rank, similarity,
  textual `type_name`, ID; `primaryText` is display-only;
- SQL `type_name = CAST(... AS text) COLLATE "C"` and Java `VaultEntryType.name()` ordering are explicitly
  equivalent; native PostgreSQL enum/Java ordinal ordering is forbidden for source limiting;
- PostgreSQL ALBUM/IMAGE + multi-type/cross-domain LIMIT/OFFSET regressions prove source/global ordering agreement;
- Vault can select exact tag-origin top-K before feature materialization because all final ordering keys are
  Vault-owned at that stage;
- tag-origin active/type/domain/required-tag filtering and multiple-tag collapse happen before source limiting;
- adversarial P12-1 regressions cover reverse ID/title ordering, offset/cross-domain ties, multiple matching tags,
  late required-tag qualifiers, text/tag overlap, and bounded batch materialization;
- `K = offset + limit <= 600` makes global fan-out bounded/correct;
- no module search requires another module's repository/table/internal package;
- Search reads have no side effects or stored query history;
- the proposed append-only Flyway V2 is limited to `pg_trgm` + indexes and requires no logical DBML change;
- high-value trigram index coverage is proportionate and body indexes are evidence-driven rather than speculative;
- query-shape requirements prohibit N+1/load-all behavior;
- privacy guidance prohibits raw query/result logging;
- Phase 12 completion is followed by the mandatory Phase 10–12 milestone review before Phase 13;
- existing agents/hooks remain sufficient.

## Invoke

```text
$codex-pre-handoff-review
```

Successful result:

```text
READY FOR HANDOFF
```

If Codex returns `CHANGES_REQUESTED`, give the findings/latest package to ChatGPT for narrow preparation remediation.
