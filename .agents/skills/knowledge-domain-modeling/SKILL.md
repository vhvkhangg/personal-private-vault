---
name: knowledge-domain-modeling
description: Guide the Knowledge parent facade and nested Study, Information, Vocabulary, and Note modules with Vault-backed identity, cross-module validation, atomic SRS transitions, preserved Markdown/frontmatter, and privacy-safe uniqueness conflicts.
---

# Knowledge Domain Modeling

Use for Phase 8 Knowledge planning, implementation, testing, or review.

## Topology

`knowledge` is the parent facade with nested application modules:

- `study`
- `information`
- `vocabulary`
- `note`

Nested modules own persistence/business rules. The parent facade delegates/orchestrates stable public use cases and
must not duplicate repositories or validation logic.

Externally consumable parent facade signatures must use parent-owned `knowledge` API types. Do not leak
`knowledge.study.*`, `knowledge.information.*`, `knowledge.vocabulary.*`, or `knowledge.note.*` command/view types
through methods intended for top-level callers. Small parent-to-nested DTO mapping is allowed; duplicate validation
or persistence logic is not.

Never access another nested module's `internal` package or another top-level module's internals.

## Vault identity

Study, Information, Vocabulary, and Note are Vault Entry-backed with `STUDY`, `INFORMATION`, `VOCABULARY`, and
`NOTE` respectively. Create Vault identity + owned row transactionally.

Metadata capabilities remain Vault-owned.

## Study

Preserve frozen Study type-specific semantics:

- optional Person XOR Creator Group author;
- price nonnegative; price requires currency;
- progress percentage is null or 0–100;
- Website requires `site_domain` + URL and other types have no `site_domain`;
- YouTube Channel requires a unique External Account reference and other types have none.

Validate People, Currency, External Account, and Platform only through their public contracts.

A YouTube Study account must be Account type `YOUTUBE_CHANNEL` and belong to the canonical YouTube platform resolved
through Reference. Do not copy Account-owned channel metadata into Study.

Treat duplicate YouTube-account assignment as a race-safe Study conflict and preserve privacy-safe constraint
logging.

## Information

Keep Information simple: Vault-backed structured content with its frozen enum and optional Markdown/example/source
metadata. Do not invent uniqueness, categories, feeds, or search.

## Vocabulary

Do not invent `(word, language)` uniqueness.

Validate language through Reference and enforce frozen nonnegative/positive SRS state checks.

Schema v1 does not define an automatic scheduling formula. Phase 8 exposes an atomic explicit review-transition
primitive: lock one Vocabulary item, read its committed current state, validate the requested resulting state,
append one review row with actual previous interval/ease + requested new values, and update the item in the same
transaction.

Concurrent transitions for one item must serialize. Do not silently install an SM-2/Anki formula.

Due reads are scheduling-neutral and fixed: exclude `MASTERED`; include non-mastered rows with non-null
`next_review_at <= cutoff`; additionally include `NEW` with null `next_review_at`; exclude null-time `LEARNING` and
`REVIEW`. Order scheduled due rows by `next_review_at`, then ID, followed by null-time `NEW` rows by ID. Cutoff is
inclusive, limit is positive/bounded, and the read performs no state mutation.

## Note

Preserve Markdown/Obsidian content rather than normalizing it.

Preserve arbitrary frontmatter JSON object data without dropping unknown keys.

Duplicate non-null imported-file hashes are stable Note-domain conflicts, including concurrent races. Losing Vault
creation rolls back. Captured logs must not contain private hash values or raw PostgreSQL vendor detail.

Do not implement the later import workflow here.

## Reads

Keep reads bounded/domain-scoped. Useful specialized reads may include the explicitly defined due Vocabulary query,
per-item review history, and a single Note lookup by imported hash.

Do not add global lists/search.

## Privacy-safe constraint conflicts

Every newly introduced expected unique conflict must retain the repository's privacy-safe constraint logging policy:

- no rejected private value in logs;
- no raw PostgreSQL `Detail: Key` text;
- stable domain result remains observable;
- useful diagnostics, if emitted, contain only non-sensitive metadata.

## Testing

Use PostgreSQL Testcontainers. Cover:

- four Vault-backed rollback/identity paths;
- Study cross-module/type validation and unique YouTube race;
- Information basic persistence;
- Vocabulary state validation, explicit atomic review transition, and row-lock concurrency;
- Note content/frontmatter preservation and imported-hash race;
- captured-log privacy for new expected uniqueness paths;
- exact parent/nested Modulith boundaries, including a representative external top-level consumer that uses only
  parent Knowledge API types and no nested Knowledge package imports;
- unchanged Flyway/Hibernate validation.

Use observable PostgreSQL contention rather than timing-only sleeps for concurrency claims.
