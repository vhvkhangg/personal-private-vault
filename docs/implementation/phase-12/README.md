# Backend Phase 12 — PostgreSQL-First Global Search

Status: **READY FOR OWNER COMMIT** (implementation final acceptance)

Codex [final acceptance](reviews/2026-10-04-phase-12-final-codex-acceptance.md) closed FR12-1–FR12-6.
Independent clean verify passed 815 tests (0 failures, 0 errors, 0 skips). Owner commit/push and ChatGPT closeout
are next, followed by mandatory Phase 10–12 milestone review before Phase 13. Phase 12 is not yet frozen.
Preparation was owner committed/pushed as `44fdaa9`;
P12-1/P12-2 remain closed. The active handoff is `docs/implementation/handoffs/ACTIVE.md`.

Phase 12 implements the dedicated top-level `search` orchestration module and narrow read-only search contracts inside
the already-frozen searchable feature modules.

No Phase 12 production implementation is authorized until:

1. this preparation passes `$codex-pre-handoff-review`;
2. the approved Phase 12 preparation slice is committed/pushed;
3. `$codex-create-handoff` creates an active Phase 12 implementation handoff.

Phase 12 is a **milestone phase**. After accepted implementation is owner committed/pushed, Phases 10–12 must pass
`$codex-milestone-review` before Phase 13 preparation may begin.

## Architecture authority

Canonical sources remain:

- `docs/architecture/search-architecture.md`;
- `docs/adr/0010-postgresql-first-global-search.md`;
- `docs/architecture/module-dependency-matrix.md`;
- `docs/architecture/module-boundaries.md`;
- `docs/architecture/data-architecture.md`;
- frozen Schema v1 DBML plus append-only Flyway migrations.

Phase 12 does not move source-data ownership into `search`.

## Search module ownership/dependencies

`search` owns **no business tables**.

Frozen top-level dependency direction remains:

```text
search
  -> vault
  -> people
  -> fiction
  -> film
  -> media
  -> location
  -> knowledge
  -> collection
  -> account
  -> feed
```

More precisely, Phase 12 should narrow the `search` descriptor to the exact new search named interfaces plus Vault
types needed by its public result model.

No business module may depend back on `search`.

`finance`, `journal`, `personal`, `authentication`, `settings`, `reference`, and `importdata` are **not** added as
Search dependencies in Phase 12. Their data is outside this frozen global-search dependency matrix.

## Explicit frozen-module feature extension

Phase 12 is an accepted architectural reason to make a **narrow read-only extension** to frozen modules.

Allowed changes in these frozen modules are limited to:

- one semantic public `search` named interface per searchable top-level module;
- owner-local internal query/application implementation needed by that search interface;
- owner-local repository/native query methods required for search;
- package descriptors/architecture tests necessary to expose only that interface;
- Search-specific tests;
- repository/package-tree synchronization for the implemented search packages;
- Vault search qualification/tag support described below.

Do **not** change existing mutation behavior, validation semantics, entity ownership, CRUD signatures, lifecycle rules,
or unrelated queries.

Searchable top-level owners:

- `vault`
- `people`
- `fiction`
- `film`
- `media`
- `location`
- `knowledge`
- `collection`
- `account`
- `feed`

For `knowledge` and `collection`, the top-level parent owns the external search contract. Nested-module persistence
and matching remain nested-owner responsibilities; top-level `search` never imports nested Knowledge/Collection
packages.

## Searchable result universe

Phase 12 global results are Vault-backed content only.

Supported `VaultEntryType` values:

| Search domain | Vault entry types |
| --- | --- |
| People | `PERSON` |
| Fiction | `FICTION` |
| Film | `FILM`, `FILM_CREDIT` |
| Media | `ALBUM`, `IMAGE` |
| Location | `BRAND`, `LOCATION` |
| Knowledge | `STUDY`, `INFORMATION`, `VOCABULARY`, `NOTE` |
| Collection | `MUSIC`, `SHOPPING`, `SOFTWARE` |
| Account | `EXTERNAL_ACCOUNT` |
| Feed | `SAVED_RESOURCE` |

Non-Vault entities are not standalone Phase 12 results. This excludes creator groups, genres/taxonomies, addresses,
location categories, feed sources/items, import jobs, Finance, Journal, and Personal profiles.

`FILM_CREDIT` remains non-taggable. When a required-tag filter is present, Film Credit results are excluded.

## Search public API direction

Preferred `search` package shape:

```text
search/
├── query/                         # @NamedInterface("query")
│   ├── GlobalSearchOperations
│   └── GlobalSearchQuery
├── view/                          # @NamedInterface("view")
│   ├── GlobalSearchPage
│   └── GlobalSearchResult
├── enums/                         # @NamedInterface("enums")
│   ├── SearchDomain
│   └── SearchMatchKind
└── internal/
    └── application/
```

`search` does not need a JPA entity/repository package because it owns no table/projection in Phase 12.

### Query contract

Global query:

- `query`: required after trimming, length `1..200`;
- `domains`: optional set; empty means all nine search domains;
- `entryTypes`: optional set; empty means all supported Vault entry types;
- `requiredTagIds`: optional set of positive IDs, maximum 10; semantics are **match all supplied tags**;
- `offset`: `0..500`;
- `limit`: `1..100`, default may be 20 at the calling layer.

Domain and entry-type filters are intersected. A valid combination with no mapped result types returns an empty page,
not an error.

Query matching is literal. `%`, `_`, and escape characters supplied by the user must not become SQL wildcards.
Bind parameters; never concatenate raw query text into SQL.

The normalized search term is the trimmed user text. Do not remove Vietnamese diacritics and do not use `unaccent`.
Case-insensitive comparison is required; internal whitespace is otherwise preserved.

### Result contract

A normalized `GlobalSearchResult` should contain only search-navigation data:

- `vaultEntryId`;
- `VaultEntryType`;
- `SearchDomain`;
- `primaryText`;
- optional `secondaryText`;
- optional plain-text `snippet`;
- `SearchMatchKind`;
- normalized relevance metadata needed for deterministic ordering.

`primaryText` remains required display/navigation data but is **not** a global ordering key. This is deliberate:
Vault-owned tag hits must be rankable before feature-owned document materialization.

Do not return JPA entities, raw JSON/frontmatter, object keys, secrets, or HTML-highlight markup.

Snippet length should be bounded (for example <= 240 characters) and derived from the matched text without mutating
the source record.

`GlobalSearchPage` should contain:

- requested `offset`;
- requested `limit`;
- ordered result list;
- `hasMore` when determinable from the bounded fan-out result set.

Phase 13 may map this to the shared HTTP pagination contract; Phase 12 does not add controllers.

## Feature-module search contract

Each searchable feature module exposes one parent/top-level `search` named interface.

The owner-local public contract should provide two bounded capabilities:

1. **text search**
   - trimmed query;
   - supported type subset;
   - required tag IDs;
   - requested candidate limit;
   - returns ranked module hits;
2. **bulk document lookup by Vault IDs**
   - bounded set of Vault IDs;
   - returns display/search documents for tag-origin matches without per-ID calls.

Module search DTOs remain module-owned and may use primitives plus `VaultEntryType`; they must not depend on the
top-level `search` module.

A module hit must expose enough normalized information for the orchestrator to compare hits consistently:

- Vault ID/type;
- primary/secondary display text;
- bounded snippet;
- integer rank bucket;
- trigram similarity value in `[0,1]` where applicable;
- match field/kind as a module-owned stable value or equivalent primitive.

### Canonical type tie key

Every source and the global Java comparator use the **enum label/name text**, not PostgreSQL native-enum order and not
Java enum ordinal/natural order.

Canonical key:

```text
type_name = VaultEntryType.name()
```

SQL/native-query representation:

```sql
CAST(ve.entry_type AS text) COLLATE "C" AS type_name
```

When a feature table owns the native enum column under another alias, use the equivalent cast on that owner-local
entry-type expression and expose/order by the textual alias `type_name`.

Because all `VaultEntryType` names are ASCII enum labels, PostgreSQL `COLLATE "C"` bytewise text order agrees with
Java `Enum.name()` / `String.compareTo` ordering.

Do **not** write bare `entry_type ASC` / `vault_entry_type ASC` for a source LIMIT: PostgreSQL native enums sort by
their declaration order, which is not the canonical alphabetical type-name order.

Each feature module must select/order its own text-origin candidates with the same source-order tuple used globally:

```text
rank_bucket DESC
similarity DESC
type_name ASC
vault_entry_id ASC
```

`primaryText` is materialized/displayed but does not participate in source/global ranking.

No feature module imports `search.*`.

## Vault search qualification/tag contract

Add a narrow public `vault::search` named interface used by feature search implementations and the global orchestrator.

It must support bounded batch operations, not one call per result:

### Active/tag qualification

Given a bounded candidate Vault-ID set and optional required tag IDs:

- exclude `vault_entries.deleted_at IS NOT NULL`;
- when tags are supplied, keep only entries containing **all** required tags;
- return only qualifying IDs/types;
- reject oversized candidate/tag batches through stable validation.

This is the authoritative active/trash + tag-filter boundary. Feature modules do not query Vault repositories/tables.

### Tag-name search

Vault resolves tag-origin search completely enough to select the exact tag-source top-K **before** feature document
materialization.

For the normalized user query, Vault:

1. matches tag names using the approved case-insensitive literal partial + short-text fuzzy rules;
2. joins tag assignments to Vault entries;
3. applies active/trash qualification;
4. applies optional domain/type filters;
5. applies `requiredTagIds` with **AND** semantics before source limiting;
6. excludes `FILM_CREDIT`;
7. collapses multiple matching tags for one Vault entry to that entry's **best tag similarity**;
8. orders the deduplicated entry candidates by:

```text
rank_bucket = 300
best_tag_similarity DESC
type_name ASC
vault_entry_id ASC
```

9. returns at most the requested positive candidate limit, optionally with one-extra/`hasMore` metadata.

Because the final/global comparator uses the same rank/similarity/`type_name`/ID tuple, Vault has every key needed
to choose the exact tag-source top-K. Feature-owned `primaryText` is display data only and is not needed to decide whether an
entry belongs in that top-K.

The orchestrator then groups only those selected tag-origin IDs by owning feature module and performs bounded bulk
document lookup. It must never call feature owners one ID at a time.

If a selected active supported Vault entry cannot be materialized by its owning module, treat that as an integrity
failure rather than silently dropping it and substituting a lower-ranked unseen candidate.

This preserves ownership: Vault never reads feature tables, and feature modules never read Vault repositories/tables.

## Correct bounded qualification and source limiting

### Text-origin module search with required tags

Feature-module text search with required tags must return the **top requested module hits after tag qualification**.

A correct implementation may:

1. fetch owner-local text candidates in bounded pages using the canonical source-order tuple;
2. batch-qualify candidate IDs through `vault::search`;
3. continue until the requested qualifying module hit count is filled or the owner-local result source is exhausted.

Do not post-filter one fixed unqualified prefix and stop, because rejected early candidates could hide qualifying
later candidates.

### Tag-origin source

Tag-origin required-tag qualification, active/trash filtering, domain/type filtering, `FILM_CREDIT` exclusion, and
multiple-tag collapse all occur **inside Vault before the tag-source top-K limit**.

A fixed oversampling factor followed by post-filtering is not correct.

Vault's tag-source limit is safe because its source order exactly matches the global comparator keys available before
feature materialization:

```text
300 DESC
best_tag_similarity DESC
type_name ASC
vault_entry_id ASC
```

No feature-owned title/name is part of that ordering.

All transport/API calls remain positively bounded. No implementation may load an entire module/table into memory or
issue one Vault/feature call per result.

## Searchable fields

The first Phase 12 contract is intentionally explicit.

| Entry type | Primary/short searchable fields | Body/secondary searchable fields |
| --- | --- | --- |
| `PERSON` | `persons.name` | `persons.notes` |
| `FICTION` | `title`, `original_title` | `description`, `review` |
| `FILM` | `title`, `original_title` | `description`, `review` |
| `FILM_CREDIT` | `character_name` | `note` |
| `ALBUM` | `title` | `description` |
| `IMAGE` | `title`, `location_text`, `image_type` | — |
| `BRAND` | `name` | `description`, `review` |
| `LOCATION` | `name`; Location-owned address text may participate | `description`, `review` |
| `STUDY` | `title`, `site_domain` | `description`, `review`, `current_progress_text` |
| `INFORMATION` | `title`, `source_name` | `description`, `content_markdown`, `example` |
| `VOCABULARY` | `word`, `pronunciation`, `ipa`, `part_of_speech` | `meaning`, `example`, `source_name` |
| `NOTE` | `title`, `source_name`, `imported_file_name` | `summary`, `content_markdown` |
| `MUSIC` | `title` | — |
| `SHOPPING` | `name` | `description` |
| `SOFTWARE` | `name` | `description`, `review` |
| `EXTERNAL_ACCOUNT` | `display_name`, `username`, `owner_name`, `external_id` | `profile_description`, `notes` |
| `SAVED_RESOURCE` | `title`, `source_name`, `author`, `external_id` | `summary` |

Notes:

- raw Note `frontmatter` JSON is not searched in Phase 12;
- URLs, hashes, checksums, object keys, and raw Feed metadata are not general search text;
- Film Credit search does not join People internals merely to obtain Person names;
- Music search does not join People internals merely to obtain artist names;
- non-Vault related records do not become standalone results.

## Matching semantics

### Case-insensitive literal partial matching

All searchable text supports case-insensitive literal substring matching.

The user query is treated as text, not a LIKE pattern. SQL wildcard characters must be escaped.

### Fuzzy matching

Fuzzy matching is required for **short identity fields** (titles, names, words, usernames, character names and similar
short fields), not for long Markdown/body text.

Use PostgreSQL `pg_trgm` similarity with an explicit application/query threshold:

```text
similarity(lower(field), lower(query)) >= 0.30
```

Only enable fuzzy matching when the trimmed query length is at least 3 characters.

Do not rely on the session-global `%` operator threshold as the sole semantic definition.

### No accent folding

Vietnamese accent-insensitive matching is not required.

Do not use `unaccent`, accent stripping, transliteration, or a second accent-folded shadow field.

A fuzzy algorithm may naturally produce some similarity between accented/unaccented text; Phase 12 does not promise
accent-insensitive equivalence.

## Cross-module relevance ordering

Every module must return comparable rank metadata using this common rubric:

| Rank bucket | Meaning |
| ---: | --- |
| 600 | primary field case-insensitive exact match |
| 550 | primary field prefix match |
| 500 | primary field substring match |
| 450 | secondary short field exact/prefix match |
| 400 | secondary short field substring match |
| 350 | short-field trigram fuzzy match |
| 300 | tag-name match |
| 200 | long/body-text substring match |

For the best match on one entry:

- keep the highest rank bucket;
- within a fuzzy/tag bucket, use similarity descending;
- non-fuzzy buckets use similarity `0` unless the module has a deterministic equivalent.

Global ordering:

1. rank bucket descending;
2. similarity descending;
3. `VaultEntryType.name()` ascending using Java `String.compareTo`;
4. `vaultEntryId` ascending.

The SQL/source equivalent of step 3 is the canonical textual alias:

```sql
CAST(<native_vault_entry_type_expression> AS text) COLLATE "C" AS type_name
```

and source queries order by `type_name ASC`, never by the native PostgreSQL enum value.

`primaryText` is intentionally **not** a tie-break key. It remains display/navigation content only.

Every source must use this same comparable tuple — including textual `type_name`, never native enum order — before
applying its own top-K limit. This is what makes independent
bounded source selection composable across module text search and Vault-owned tag search.

If the same Vault entry is produced by text and tag search, deduplicate by `vaultEntryId` and keep the stronger ranked
representation. If rank + similarity are identical, either representation has the same global ordering keys; prefer
the text-origin representation only as a stable presentation rule, not as a ranking advantage.

## Bounded fan-out / pagination behavior

For a global request:

```text
K = offset + limit
```

with `K <= 600` from the Phase 12 bounds.

For each selected domain, request at most its exact top `K` **qualifying** text-origin hits using the canonical
source/global comparator. Separately request at most the exact top `K` **already-qualified and deduplicated**
tag-origin Vault hits using the same comparator tuple.

Merge, deduplicate, sort globally, then return `[offset, offset + limit)`.

Correctness proof:

- each source applies all filters/qualification that can remove candidates **before** its final source limit;
- each source orders candidates with the same global keys: rank, similarity, textual `type_name`, ID;
- therefore a candidate ranked below position `K` within one source cannot enter the global top `K`;
- tag-origin title/name materialization occurs only after Vault has selected its exact top `K`, and cannot alter rank
  because `primaryText` is not a global ordering key.

This proof does not rely on fixed oversampling.

No parallel executor/thread pool is introduced in Phase 12. Sequential in-process fan-out is sufficient for the
single-user system and easier to keep deterministic.

## Read consistency / side effects

Global search is read-only.

- no search call mutates business state;
- no search history is stored;
- no cross-module locking or global transaction snapshot is required;
- individual module/Vault reads use normal transaction semantics;
- a search concurrent with owner edits may observe different committed snapshots across module calls, which is
  acceptable for this non-transactional discovery feature.

## PostgreSQL search migration

Phase 12 may add one append-only Flyway migration, expected name:

```text
V2__add_search_support.sql
```

Allowed contents:

- `CREATE EXTENSION IF NOT EXISTS pg_trgm`;
- Search-supporting indexes only.

No new table, column, foreign key, constraint, trigger, generated column, search projection, or DBML logical-schema
change is authorized.

### Required trigram index coverage

Provide trigram support for the high-value short fields used for partial/fuzzy matching:

- `tags.name`;
- `persons.name`;
- `fictions.title`, `fictions.original_title`;
- `films.title`, `films.original_title`;
- `film_credits.character_name`;
- `albums.title`;
- `images.title`, `images.location_text`;
- `brands.name`;
- `locations.name`;
- `study_items.title`;
- `information_items.title`;
- `vocabulary_items.word`;
- `notes.title`;
- `music_tracks.title`;
- `shopping_items.name`;
- `software_items.name`;
- `external_accounts.display_name`, `external_accounts.username`, `external_accounts.owner_name`;
- `saved_resources.title`.

Prefer `lower(column)` trigram expression indexes where the search predicate also uses `lower(column)`.

Long body fields must remain searchable correctly. Do not add a large set of speculative body indexes merely because
the fields exist; add additional body/secondary indexes only when Phase 12 query-plan evidence demonstrates a concrete
need.

Full-text vectors/generated columns and Elasticsearch/OpenSearch are out of Phase 12.

## Performance/query-shape rules

- no per-result repository/API calls;
- no `findAll()`/load-all search implementation;
- no feature module reads another module's repository/table directly;
- text-origin required-tag qualification is batch-oriented and continues across bounded owner pages until the
  requested qualifying source count is filled or exhausted;
- tag-origin active/type/domain/required-tag qualification + duplicate-tag collapse occurs before Vault source
  limiting;
- bulk document materialization for the already-selected tag-origin top-K is batch-oriented;
- query count may scale with selected modules and bounded candidate pages, but must not scale linearly with returned
  hit count;
- all public/module candidate limits are validated and bounded.

## Security/privacy

Search queries and result text are personal data.

Do not log:

- raw search query strings;
- result snippets;
- tags searched/filtered;
- external IDs/usernames;
- Note/Information Markdown;
- account/profile descriptions;
- raw SQL/vendor details containing values.

Stable validation errors may report only safe structural facts such as invalid length, limit, offset, or unsupported
filter combination.

Use bind parameters for all query values.

## Testing contract

Use real PostgreSQL Testcontainers/Flyway; no H2.

### Search behavior

Cover:

- all 17 supported Vault entry types route to the correct owner module;
- exact > prefix > substring > fuzzy > tag > body rank ordering across multiple modules;
- deterministic global tie ordering;
- case-insensitive exact/partial matching;
- `%`, `_`, and escape characters are treated literally;
- fuzzy short-name/title matching at the explicit threshold;
- fuzzy disabled for queries shorter than 3 characters;
- no `unaccent`/accent-stripping contract is introduced;
- body/Markdown partial matching for Information and Note;
- Film Credit character-name search;
- Account username/display-name search;
- tag-name query produces active taggable entry results;
- explicit required-tag filters use AND semantics;
- `FILM_CREDIT` disappears when required tags are present;
- domain/type filters and their intersection;
- recycle-bin (`vault_entries.deleted_at`) content is excluded;
- duplicate text+tag hits collapse by Vault ID;
- **more than K equal-rank/equal-similarity tag matches** where Vault ID ordering is the reverse of feature title
  ordering: source/global order still follows type + ID and no title-based post-materialization reordering changes the
  selected set;
- offset pages across equal-rank tag candidates and cross-domain ties preserve the canonical
  rank/similarity/`type_name`/ID order;
- real PostgreSQL tag-only LIMIT/OFFSET cases where equal-score `ALBUM` and `IMAGE` candidates prove SQL
  `type_name ASC` matches Java `VaultEntryType.name()` order (`ALBUM` before `IMAGE`) despite frozen native-enum
  declaration order placing `IMAGE` before `ALBUM`;
- the same native-enum-vs-name-order regression for a multi-type feature text source (Media), with more than `K`
  equal-score candidates so post-LIMIT Java resorting cannot conceal an incorrect SQL source selection;
- cross-domain equal-score type ties compare actual SQL/source output with the final Java/global comparator across
  multiple offset pages;
- multiple matching tags assigned to one entry collapse to one tag-origin candidate using its best tag similarity
  before source limiting;
- required-tag AND filtering rejects early candidates **before** the Vault source limit so qualifying later candidates
  can enter the top-K;
- text/tag overlap keeps one Vault result with the stronger rank/similarity representation;
- bounded batch evidence shows one tag-origin candidate query plus bounded owner bulk materialization, not per-hit
  lookups or fixed-factor oversampling;
- offset/limit bounds and correct cross-module slicing.

### Architecture

Verify:

- `search` has no owned persistence table/entity/repository;
- `search` imports only public named search interfaces + explicitly needed Vault public types;
- no searchable feature imports `search.*`;
- frozen modules receive only the narrow Phase 12 read-only search extension;
- Knowledge/Collection top-level search contracts do not expose nested module types;
- no module imports another module's internal package/repository for search;
- Spring Modulith dependencies remain acyclic.

### PostgreSQL migration/query shape

Verify:

- Flyway V2 applies after frozen V1;
- `pg_trgm` extension exists;
- required short-field trigram indexes exist;
- frozen V1 tables/columns/constraints remain unchanged;
- representative short-field partial/fuzzy queries are compatible with the created trigram expression/index shape;
- SQL source ordering uses an explicit textual `type_name` alias based on `CAST(... AS text) COLLATE "C"` and
  matches Java `VaultEntryType.name()` ordering; no source LIMIT orders by the native enum directly;
- Vault tag-origin SQL applies active/type/domain/required-tag qualification and per-entry best-tag collapse before
  `ORDER BY best_similarity DESC, type_name ASC, vault_entry_id ASC` + limit, where `type_name` is
  `CAST(ve.entry_type AS text) COLLATE "C"` (or equivalent owner-local native-enum expression);
- no N+1/per-hit query pattern;
- unfiltered result count changes do not cause linear query-count growth;
- tag qualification/materialization remains batch-oriented.

### Regression/final verification

Preserve all **787** Phase 11/baseline tests.

Run:

```text
mvn -f backend/pom.xml -ntp clean verify
git diff --check
```

Record exact commands, Java/Maven/PostgreSQL/Testcontainers versions, total tests/failures/errors/skips, focused search
tests, Flyway V2/`pg_trgm` evidence, query-count evidence, Spring Modulith results, and warnings/limitations in:

```text
docs/implementation/phase-12/test-evidence.md
```

## Out of scope

- Finance/Journal/Personal search;
- Search HTTP controllers/OpenAPI (Phase 13);
- frontend search UI;
- search history/recent-query storage;
- recommendation/personalized ranking;
- embeddings/vector search/RAG;
- Elasticsearch/OpenSearch;
- central search table/materialized projection;
- background indexing/event-driven projection;
- `unaccent`/accent folding;
- semantic search;
- search analytics;
- generic cross-module repository access;
- mutation changes in frozen feature modules;
- Phase 13+ implementation.

## Preparation tooling

Added for Phase 12:

- `.agents/skills/global-search-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-12-global-search.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/search/AGENTS.md`

Existing searchable-module AGENTS files receive only a Phase 12 read-only search-extension clause so the implementation
handoff can touch their search contracts without reopening frozen business behavior.

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook
