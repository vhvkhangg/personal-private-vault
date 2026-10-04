---
name: global-search-domain-modeling
description: Guide Phase 12 PostgreSQL-first cross-module search, module-owned search contracts, Vault tag qualification, deterministic ranking, and pg_trgm query/index design.
---

# Global Search Domain Modeling

Use for Phase 12 preparation, implementation, testing, or review.

## Boundary

`search` is a leaf/orchestrator and owns no source-data table.

It may depend only on public search contracts of:

- Vault
- People
- Fiction
- Film
- Media
- Location
- Knowledge parent
- Collection parent
- Account
- Feed

Business modules never depend on `search`.

## Frozen-module extension

Phase 12 may add read-only search contracts/queries to frozen searchable modules. Do not change their mutation,
validation, lifecycle, identity, or ownership behavior.

Knowledge/Collection external search contracts remain parent-owned; do not expose nested types.

## Result universe

Only Vault-backed Phase 12 types are standalone results. Do not add Finance/Journal/Personal or non-Vault entities
without an explicit architecture change.

## Matching

- trim query; 1..200 chars;
- case-insensitive literal exact/prefix/substring;
- escape LIKE wildcard characters;
- fuzzy only on short identity fields, query length >= 3;
- explicit pg_trgm similarity threshold >= 0.30;
- no unaccent/accent-folding promise;
- body/Markdown uses partial matching, not fuzzy-by-default.

## Ranking

Use the canonical rank buckets in `docs/implementation/phase-12/README.md`, then similarity, textual type name,
ID. `primaryText` is display data only and is never a source/global tie key.

Persistence source ordering must expose `CAST(<entry_type> AS text) COLLATE "C" AS type_name` and order by
`type_name ASC`; never rely on PostgreSQL native-enum declaration order. Java/global comparison uses
`VaultEntryType.name()` / `String.compareTo`, never enum ordinal/natural order.

Deduplicate text/tag matches by Vault ID and keep the stronger hit.

## Tags/trash

Vault owns:

- active-vs-trash qualification;
- required-tag AND filtering;
- tag-name search.

For tag-origin search, Vault performs active/type/domain/required-tag qualification and collapses multiple matching
tags to each entry's best similarity **before** applying its source top-K. Order by similarity DESC, textual `type_name` ASC, ID ASC.

Use bounded batches. Never query Vault tables/repositories from feature modules.

FILM_CREDIT is not taggable.

## Fan-out

For global `offset` + `limit`, each selected source contributes at most `K = offset + limit` exact top qualifying
candidates under the shared rank/similarity/textual-`type_name`/ID comparator. Feature text sources may page + batch-qualify tags
until K qualifiers are filled; Vault tag search qualifies/deduplicates before its final K limit.
Merge/dedupe/sort/slice centrally.

No load-all search and no per-result calls.

## PostgreSQL

Phase 12 may add append-only Flyway V2 with `pg_trgm` + search indexes only. No logical schema/table/column/projection
change.

Prefer lower(column) trigram expression indexes for high-value short fields. Add body indexes only from actual
query-plan evidence.

## Privacy

Do not log raw search terms, snippets, tag filters, private account identifiers, Markdown, or vendor SQL details.

Use bind parameters.

## Testing

Use PostgreSQL Testcontainers. Verify all supported types, ranking, tags, trash exclusion, filters, pagination,
literal wildcard behavior, pg_trgm migration/indexes, no N+1, and exact Modulith dependency direction.
