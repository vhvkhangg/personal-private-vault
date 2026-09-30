# Backend Phase 8 — Knowledge Foundation

Preparation: **READY FOR HANDOFF** after the [2026-09-30 Codex re-review](reviews/2026-09-30-phase-8-pre-handoff-codex-rereview.md); owner committed/pushed it as `b3f3cd9`.

Implementation: **READY FOR OWNER COMMIT** after the [Codex final re-review](reviews/2026-09-30-phase-8-final-codex-rereview.md); owner commit/push is pending under the [active handoff](../handoffs/ACTIVE.md).

Phase 8 implements the top-level `knowledge` facade and its four nested Spring Modulith modules:

- `knowledge.study`
- `knowledge.information`
- `knowledge.vocabulary`
- `knowledge.note`

Phase 8 follows the frozen Account foundation because Study may reference a stored YouTube-channel External Account.

No Phase 8 production implementation is authorized until:

1. this preparation passes `$codex-pre-handoff-review`;
2. the approved Phase 8 preparation slice is committed/pushed;
3. `$codex-create-handoff` creates an active Phase 8 implementation handoff.

Phase 8 is **not** a milestone phase. The next cross-phase milestone is after Phase 9.

## Owned Schema v1 tables

### Study

- `study_items`

### Information

- `information_items`

### Vocabulary

- `vocabulary_items`
- `vocabulary_reviews`

### Note

- `notes`

No DBML/Flyway/schema change is planned.

## Architecture and nested-module boundary

`knowledge` is the parent facade. External top-level modules must not access nested repositories/internals directly.

Frozen whole-module dependency direction:

```text
knowledge -> vault, people, reference, account
```

Nested responsibilities should remain cohesive:

- `study` owns Study persistence and Study-specific cross-module validation;
- `information` owns Information persistence;
- `vocabulary` owns Vocabulary/SRS state and review history;
- `note` owns Markdown/Obsidian-style Note persistence;
- the parent `knowledge` package owns only stable facade/orchestration contracts needed to prevent future
  `feed`/`importdata`/`search` callers from reaching nested internals.

Do not duplicate nested-domain business rules in the parent facade. A parent facade may delegate to nested public
contracts, but it must not become a second persistence/service layer.

The active Phase 8 handoff must verify the actual Spring Modulith nested-module dependency syntax and narrow
dependencies to the public named interfaces genuinely used. Do not weaken module boundaries merely to make nested
wiring convenient.

## Shared Vault identity

The following rows are Vault Entry-backed with the same ID:

| Knowledge type | Vault entry type |
| --- | --- |
| `study_items` | `STUDY` |
| `information_items` | `INFORMATION` |
| `vocabulary_items` | `VOCABULARY` |
| `notes` | `NOTE` |

Create each Vault Entry and domain row in one transaction. Failure must leave no orphan Vault Entry.

Favorite/rating/tag/recycle behavior remains canonical in Vault. All four Vault types already support those metadata
capabilities; Knowledge must not duplicate the Vault capability matrix.

`vocabulary_reviews` is review history owned by Vocabulary and is **not** a Vault Entry.

## Study contract

Respect frozen Schema v1:

- required nonblank title;
- type: `COURSE`, `BOOK`, `GITHUB_REPOSITORY`, `WEBSITE`, or `YOUTUBE_CHANNEL`;
- optional poster, published date, description, URL, review;
- optional Person author or Creator Group author, but never both;
- optional nonnegative `price_amount`;
- if price is present, `currency_code` is required;
- `learning_status`: `PLANNED`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`;
- optional `progress_percent` in `[0, 100]`;
- optional free-form `current_progress_text`.

If an author ID is present, validate it through public People contracts. Zero authors is valid; do not invent an
"author required" rule.

If a currency code is present, validate it through public `ReferenceCatalog`. Preserve the frozen rule that a
currency may exist without a price, while a price may not exist without a currency.

### WEBSITE

For `WEBSITE`:

- `site_domain` and `url` are required;
- `site_domain` is a hostname value, not a full URL/path;
- normalize it by trimming and lowercasing with locale-independent semantics;
- all non-`WEBSITE` Study types must have `site_domain = null`.

Do not invent a second Website table or fetch/scrape website metadata in Phase 8.

### YOUTUBE_CHANNEL

For `YOUTUBE_CHANNEL`:

- `youtube_channel_account_id` is required;
- all non-`YOUTUBE_CHANNEL` Study types must have it null;
- the referenced External Account must exist through public `ExternalAccountOperations`;
- its `accountType` must be `YOUTUBE_CHANNEL`;
- its platform must resolve through public `ReferenceCatalog` to the canonical YouTube platform. Schema v1 has no
  platform code, so Phase 8 should treat a trimmed, case-insensitive platform name equal to `YouTube` as the
  canonical identity while still using the referenced platform ID for persistence.

Do not copy the channel handle/name/avatar/banner into Study. Those remain Account-owned.

Frozen Schema v1 uniquely constrains `youtube_channel_account_id`. Reusing a non-null channel account for a second
Study item must yield a stable Study-domain conflict; concurrent duplicates have one winner, the losing Study/Vault
transaction rolls back, and raw persistence/vendor-detail output must not leak.

The Phase 4–6 privacy-safe logging policy remains in force for this new unique-conflict path.

## Information contract

`information_items` is Vault-backed `INFORMATION` content.

Required:

- nonblank title;
- type: `FINANCE`, `TECHNOLOGY`, `HEALTH`, or `OTHER`.

Optional fields:

- description;
- Markdown content;
- example;
- source name;
- source URL.

Do not invent category/reference tables, full-text search, feed provenance joins, or URL uniqueness in Phase 8.
Feed/saved-resource conversion is a later workflow and must use the public Knowledge facade when implemented.

## Vocabulary contract

`vocabulary_items` is Vault-backed `VOCABULARY` content.

Required:

- nonblank `word`;
- nonblank `meaning`;
- required `language_code`, validated through public `ReferenceCatalog`.

Optional lexical metadata includes example, pronunciation, IPA, part of speech, source name, and source URL.

Frozen SRS state:

- `learning_status`: `NEW`, `LEARNING`, `REVIEW`, `MASTERED`;
- `next_review_at`: nullable;
- `interval_days >= 0`;
- `ease_factor > 0`;
- `repetition_count >= 0`;
- `lapse_count >= 0`.

Schema v1 defines **no unique constraint** on `(word, language_code)`. Phase 8 must not invent deduplication or
uniqueness for vocabulary items.

### Review transition contract

Schema v1 stores review history but does not freeze an SM-2/Anki scheduling formula. Phase 8 must therefore avoid
inventing a hidden algorithm.

Expose one atomic review-transition capability that:

1. row-locks/serializes one Vocabulary item for the transition;
2. reads its actual committed current SRS state;
3. accepts the review response (`AGAIN`, `HARD`, `GOOD`, `EASY`) plus an explicit desired resulting state:
   - learning status;
   - next review time;
   - interval days;
   - ease factor;
   - repetition count;
   - lapse count;
4. validates all frozen numeric/check constraints;
5. appends one `vocabulary_reviews` row containing:
   - response/reviewed time;
   - previous interval/ease from the locked item;
   - requested new interval/ease;
   - resulting next-review time;
6. updates the Vocabulary item's current SRS state in the same transaction.

No partially applied review is allowed. Concurrent review transitions for the same Vocabulary item must serialize so
that each history row's `previous_*` values describe the actual committed state immediately before that transition.

A future owner-approved feature may add an automatic scheduling algorithm on top of this transition primitive. Do
not freeze an algorithm by accident in Phase 8.

### Due Vocabulary read semantics

Phase 8 may expose a bounded read equivalent to:

```text
findDue(cutoff, limit)
```

with the following fixed query semantics. This defines only **which rows are due and their ordering**; it does not
choose or execute an SRS scheduling algorithm.

A Vocabulary item is due when:

```text
learning_status <> MASTERED
AND (
    (next_review_at IS NOT NULL AND next_review_at <= cutoff)
    OR
    (learning_status = NEW AND next_review_at IS NULL)
)
```

Rules:

- `cutoff` is required and **inclusive** (`next_review_at <= cutoff`);
- `NEW` + `next_review_at = null` is considered unscheduled-new work and is due;
- `LEARNING` or `REVIEW` + `next_review_at = null` is **not** due; the read must not invent a schedule;
- `MASTERED` is never due, even if a stale/non-null `next_review_at` exists;
- `NEW` with a non-null future `next_review_at` is not due until that timestamp reaches the cutoff;
- the read is side-effect free and must not mutate status, intervals, counters, or timestamps.

Deterministic order:

1. explicitly scheduled due rows (`next_review_at` non-null), oldest `next_review_at` first;
2. tie-break scheduled rows by `id` ascending;
3. unscheduled `NEW` rows (`next_review_at` null) after scheduled due rows, ordered by `id` ascending.

The API must require a positive bounded `limit`; do not expose an unbounded due-list method.

Review-history reads must be Vocabulary-scoped and explicitly bounded/ordered.

## Note contract

`notes` is Vault-backed `NOTE` content.

Required:

- nonblank title;
- non-null Markdown content.

Preserve `content_markdown` as supplied; do not normalize or rewrite Obsidian syntax such as wikilinks, tags,
embeds, callouts, or Markdown formatting.

Optional:

- summary;
- source name/URL;
- imported file name;
- imported file hash;
- JSONB frontmatter.

Unknown frontmatter keys must round-trip without silent loss. Use a representation that can preserve arbitrary JSON
object content without exposing a JPA entity.

### Imported-file hash uniqueness and privacy

`imported_file_hash` is nullable but unique when present.

A duplicate non-null imported hash must yield a stable Note-domain conflict rather than silently returning/updating
another Note. This supports the later import workflow, where duplicate detection and user update/skip/cancel choices
are explicit.

Concurrent duplicate hashes must have one winner and domain-conflict losers with losing Vault transactions rolled
back.

Because a file hash can be private metadata, add a PostgreSQL-backed privacy regression using a distinctive valid
hash/marker and captured logs. Assert neither the private value nor raw PostgreSQL `Detail: Key
(imported_file_hash)=(` output appears while the stable Note-domain conflict remains observable.

Do not implement the Phase 10 import pipeline in Phase 8.

## Bounded public reads

Keep reads ID- or parent/domain-scoped and bounded:

- Study by ID;
- Information by ID;
- Vocabulary by ID;
- bounded due Vocabulary items using the explicit inclusive-cutoff/null/status predicate and deterministic ordering
  defined above;
- bounded review history for one Vocabulary item;
- Note by ID;
- optional single Note lookup by imported file hash for later duplicate detection.

Do not add global unbounded lists or global search. Global search belongs to Phase 12.

## Parent Knowledge facade

Phase 8 should establish a small stable public parent facade so future top-level `feed`, `importdata`, and `search`
modules do not import nested packages or nested internals.

### Externally consumable type boundary

Every facade method intended for a **top-level external module** must declare its parameters, return values, generic
element types, and public exceptions using types owned/exposed by the parent `knowledge` module itself.

Preferred direction:

```text
knowledge/
├── package-info.java
├── api/                            # parent-owned @NamedInterface("api")
│   ├── package-info.java
│   ├── <small facade capability interfaces>
│   └── <parent-owned commands/views/enums used by external callers>
├── internal/
│   └── application/                # mapping/delegation only
└── study|information|vocabulary|note/
    └── <nested public contracts + internals>
```

The exact names may be refined by the handoff, but these boundary rules are mandatory:

- a top-level caller must never need to import `knowledge.study.*`, `knowledge.information.*`,
  `knowledge.vocabulary.*`, or `knowledge.note.*` merely to call the parent facade;
- nested public command/view types remain usable by the parent and allowed nested/sibling interactions, but are not
  leaked through externally consumable parent method signatures;
- the parent may perform small, explicit mapping between parent-owned API DTOs and nested DTOs;
- mapping/delegation must not duplicate nested validation, persistence, transaction, or scheduling rules;
- parent API types are immutable and never JPA entities/repositories.

Requirements:

- delegate to nested module public capabilities;
- expose parent-owned immutable API types to external top-level callers;
- do not reproduce validation/persistence logic already owned by a nested module;
- do not expose repositories;
- do not make `knowledge` open merely to bypass nested-module access rules;
- do not create import/feed-specific adapters or conversion workflows before their phases.

The handoff must add an architecture/compile-time regression with a representative external top-level consumer
(preferably a test fixture in an existing deferred caller such as `feed`) that:

1. consumes the parent Knowledge facade using only `knowledge` / `knowledge.api` imports;
2. does not import any `knowledge.study`, `knowledge.information`, `knowledge.vocabulary`, or `knowledge.note` type;
3. passes Spring Modulith architecture verification.

The handoff may refine the exact facade interface grouping if Spring Modulith verification shows a cleaner shape, but
the external boundary must remain a closed parent `knowledge` API.

## Proposed package direction

```text
knowledge/
├── package-info.java
├── api/
│   ├── package-info.java            # parent-owned named interface for top-level consumers
│   └── <small facade contracts + parent-owned immutable API types>
├── internal/
│   └── application/                 # mapping/delegation/orchestration only
├── study/
│   ├── package-info.java            # nested @ApplicationModule
│   ├── <public Study capability/view/enum packages>
│   └── internal/
├── information/
│   ├── package-info.java            # nested @ApplicationModule
│   ├── <public Information capability/view/enum packages>
│   └── internal/
├── vocabulary/
│   ├── package-info.java            # nested @ApplicationModule
│   ├── <public Vocabulary capability/view/enum packages>
│   └── internal/
└── note/
    ├── package-info.java            # nested @ApplicationModule
    ├── <public Note capability/view packages>
    └── internal/
```

Remove the existing `.gitkeep` files as real implementation content is added.

Do not create generic CRUD bases, a cross-nested "common" package, `Service` / `ServiceImpl` pairs, placeholder
events, or speculative strategy/factory hierarchies.

## Expected cross-module contracts

The handoff must derive exact Spring Modulith named-interface dependencies from real imports. Expected needs are:

### Study

- Vault entry/enums/view;
- People person/group/view;
- Reference catalog/view;
- Account account/enums/view.

### Information

- Vault entry/enums/view.

### Vocabulary

- Vault entry/enums/view;
- Reference catalog/view.

### Note

- Vault entry/enums/view.

The parent Knowledge facade should depend on nested public contracts, not nested internals/repositories. Codex must
verify the exact nested-module dependency declaration supported by the current Spring Modulith version rather than
guessing syntax.

## Required invariants for the future handoff

- all four Knowledge content types preserve transactional Vault identity;
- Study author XOR and type-specific Website/YouTube fields match frozen Schema v1;
- YouTube Study validation checks both External Account type and canonical YouTube platform through public contracts;
- duplicate YouTube-channel assignment is a race-safe stable Study conflict;
- Information remains simple content ownership without invented uniqueness;
- Vocabulary language and numeric SRS state are validated;
- Vocabulary review transitions are atomic and serialize per Vocabulary item;
- Vocabulary duplicates are allowed because Schema v1 defines no natural-key uniqueness;
- Note Markdown/frontmatter round-trips without silent normalization/loss;
- Note imported-hash duplicates are stable/race-safe conflicts;
- all new expected constraint conflicts preserve privacy-safe logging;
- externally consumable parent facade signatures contain only parent-owned Knowledge API types, never nested-module
  command/view types;
- parent facade contains no duplicate persistence/business rules;
- no cross-module or cross-nested-module internal/repository access;
- public views are immutable.

## Testing contract for the future handoff

Require PostgreSQL Testcontainers coverage for:

- all five Knowledge-owned tables against unchanged Flyway V1/Hibernate validation;
- Vault rollback/identity for Study, Information, Vocabulary, and Note;
- Study author validation through public People contracts;
- Study price/currency and progress constraints;
- Website type-specific fields;
- YouTube Study account type + canonical platform validation through Account/Reference public contracts;
- sequential/concurrent duplicate `youtube_channel_account_id` conflict, Vault rollback, deterministic PostgreSQL
  contention, and privacy-safe captured logs with no raw vendor detail;
- Information create/update/find behavior;
- Vocabulary language validation and all frozen numeric constraints;
- allowed duplicate vocabulary words/languages;
- atomic Vocabulary review transition/history and rollback;
- deterministic concurrent review transitions using observable row-lock contention, not timing-only sleeps;
- bounded due-item read with inclusive cutoff, explicit null/status handling, scheduled-first deterministic ordering,
  and no read-side state mutation;
- bounded/deterministically ordered review-history reads;
- Note Markdown/Obsidian syntax preservation and arbitrary JSONB frontmatter round-trip;
- Note imported-file-hash sequential/concurrent conflict, Vault rollback, deterministic PostgreSQL contention, and
  privacy-safe captured logs;
- exact nested/top-level Spring Modulith boundaries and named-interface dependencies;
- representative external top-level consumer compiles/architecturally verifies using only parent `knowledge` API
  types and no nested Knowledge package imports;
- no stale `.gitkeep` in implemented Knowledge packages;
- final `mvn -f backend/pom.xml -ntp clean verify` on Java 25;
- `git diff --check`.

Do not use H2. Race tests must prove the competing operation reached PostgreSQL contention/serialization rather than
using timing-only sleeps as the sole proof.

## Out of scope

- Phase 9 Collection;
- Phase 10 Feed/ImportData workflows;
- automatic web/GitHub/YouTube metadata fetching;
- OAuth, scraping, browser automation, schedulers;
- automatic SRS/SM-2/Anki algorithm selection;
- global search;
- REST/controllers/OpenAPI;
- frontend;
- RAG/embeddings/vector search;
- migrations/schema redesign;
- delete/permanent-delete behavior unless separately approved;
- frozen Vault/People/Reference/Account production changes.

## Preparation tooling

Added for Phase 8:

- `.agents/skills/knowledge-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-8-knowledge.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for Phase 8.
