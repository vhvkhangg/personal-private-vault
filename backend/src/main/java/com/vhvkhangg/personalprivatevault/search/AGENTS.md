# Search Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.search`.

## Ownership

Search owns no v1/Phase-12 business table.

It orchestrates read-only global search over public module search contracts.

## Allowed dependencies

Only public named interfaces of:

- `vault`
- `people`
- `fiction`
- `film`
- `media`
- `location`
- parent `knowledge`
- parent `collection`
- `account`
- `feed`

Never import another module's `internal` package/repository.

Do not add Finance/Journal/Personal dependencies in Phase 12.

## Invariants

- standalone results are the 17 approved Vault entry types only;
- trash entries are excluded through Vault-owned qualification;
- required tag filters use AND semantics;
- Film Credit is never returned from tag-origin/tag-filtered search;
- query is literal, case-insensitive, bounded, and not logged;
- fuzzy matching uses explicit pg_trgm similarity semantics only on short fields;
- no accent folding;
- rank buckets/global ordering are deterministic and shared by contract: rank DESC, similarity DESC, textual
  `type_name` ASC, ID ASC; `primaryText` never changes ranking;
- SQL/source `type_name` is `CAST(... AS text) COLLATE "C"`; Java/global comparison uses
  `VaultEntryType.name()` string order, never PostgreSQL native-enum or Java ordinal order;
- text/tag duplicate hits deduplicate by Vault ID;
- Vault tag-origin filtering + best-tag collapse occurs before exact top-K limiting;
- feature text sources may page + batch-qualify required tags until K qualifying hits are filled;
- module/tag/document fan-out is bounded and batch-oriented;
- no load-all or per-hit cross-module calls;
- Search writes no history/business state;
- no central projection/table in Phase 12.

## Phase gate

Production changes require:

1. Phase 12 preparation review = `READY FOR HANDOFF`;
2. an active approved Phase 12 handoff.

After Phase 12 owner commit/push, the Phase 10–12 milestone review is mandatory before Phase 13.

## M10-12-1 maintenance exception

A Codex handoff created from
`docs/implementation/maintenance/milestone-10-12-search-case-normalization/README.md` may modify this frozen module
only for the Search SQL/query-folding and snippet-offset defect described there.

Do not use the maintenance handoff to change mutation behavior, ownership, public Search contracts, ranking, source
bounds, schema/index definitions, or unrelated code. The maintenance handoff is the temporary authority; otherwise
the frozen phase rules remain in force.
