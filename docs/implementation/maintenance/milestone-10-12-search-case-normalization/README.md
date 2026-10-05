# Phase 10–12 Milestone Search Case-Normalization Maintenance

Status: **COMPLETE / FROZEN** — owner committed/pushed `a881540` on 2026-10-05; FRM10-12-1/M10-12-1 closed.

Owner approval: **2026-10-04**

Execution: Codex accepted `docs/implementation/handoffs/ACTIVE.md` on 2026-10-05,
handoff ID `maintenance-milestone-10-12-search-case-normalization`.
The owner approval above is retained. Independent clean verify passed 817 tests (zero failures/errors/skips);
the genuine long-body casing-expansion regression and evidence corrections close FRM10-12-1.
See [formal final acceptance](reviews/2026-10-05-final-codex-acceptance.md).
The owner committed/pushed the accepted maintenance as `a88154072afe26035accce58e1acb127ea253028`.
[Milestone acceptance](../../phase-12/reviews/2026-10-05-phase-10-12-milestone-codex-acceptance.md) is `MILESTONE_READY`.
Next step: owner commits/pushes milestone review/status docs, then ChatGPT post-milestone synchronization/reset.

Trigger:
[`../../phase-12/reviews/2026-10-04-phase-10-12-milestone-codex-review.md`](../../phase-12/reviews/2026-10-04-phase-10-12-milestone-codex-review.md)

Trigger finding: **M10-12-1 — Search Java/PostgreSQL case-normalization mismatch**.
Maintenance implementation accepted/committed and milestone finding closed; no reopened production scope remains.

This was one narrow maintenance implementation slice that temporarily reopened only the frozen Phase 12 read-only
Search implementation needed to close M10-12-1. It is now frozen again. Phases 10 and 11 remained untouched.

Phase 13 remains blocked. This maintenance uses the milestone-maintenance exception and does **not** require a
numbered-phase pre-handoff review.

## Goal

Make Search query matching and snippet extraction deterministic across JVM default locales and consistent with the
existing PostgreSQL `lower(...)` semantics, without changing Search ranking, bounds, ownership, schema, or public
feature behavior.

The maintenance must close all of these measured failures:

- ASCII `I` search changing behavior under JVM `tr-TR`;
- identical Unicode `İD` title/query failing under `Locale.ROOT` because Java and PostgreSQL lowercasing differ;
- locale-dependent BODY/snippet extraction returning `null` for content that the Search query matched.

## Frozen behavior to preserve

Keep all accepted Phase 12 behavior unchanged except for the normalization/snippet defect:

- 17 supported Vault-backed result types;
- exact/prefix/substring/fuzzy/tag/body rank buckets;
- fuzzy threshold `>= 0.30`, transaction-local threshold isolation, and short-query fuzzy suppression;
- literal `%`, `_`, and `\` escaping;
- textual `type_name` / `VaultEntryType.name()` ordering;
- required-tag AND qualification and tag/text deduplication;
- bounded fan-out/lookahead, truthful `hasMore`, batch qualification/materialization, and no N+1/load-all;
- plain-text snippets, fallback between body fields, `<= 240` budget, Markdown/HTML cleanup, and surrogate safety;
- read-only Search behavior and privacy-safe logging;
- parent Knowledge/Collection search contracts and existing Modulith boundaries;
- Flyway V1/V2 and all schema/index definitions.

## Finding A — SQL/query case folding must have one authority

### Current defect

All affected owner SQL searches compare PostgreSQL `lower(column)` against query values already lowercased by Java
using the JVM default locale.

That creates two independent case-folding authorities:

```text
stored text -> PostgreSQL lower(...)
query text  -> Java String.toLowerCase(...)
```

The two transformations are not equivalent for all Unicode text and Java's default-locale transformation also changes
with JVM locale.

`Locale.ROOT` alone is insufficient: Java lowercasing `İ` can expand to `i` + combining dot while PostgreSQL's current
baseline folds the same database text/query differently.

### Required query-side policy

PostgreSQL is the sole case-folding authority for SQL Search comparison.

For every affected text/tag owner query:

1. trim/validate the raw query in Java as before;
2. **do not lowercase the SQL comparison parameter in Java**;
3. escape LIKE metacharacters on the raw trimmed query before binding;
4. bind raw/escaped query text;
5. apply PostgreSQL `lower(...)` to the bound query expression in SQL wherever the stored side uses
   `lower(column)`.

Conceptual forms:

```sql
lower(column) = lower(CAST(:rawQuery AS text))

lower(column) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\'

lower(column) % lower(CAST(:rawQuery AS text))

similarity(lower(column), lower(CAST(:rawQuery AS text)))
```

Equivalent parameter naming/layout is acceptable. The essential rule is that both stored text and bound query are
folded by PostgreSQL under the same database semantics.

The LIKE escape contract remains literal. Adding `%` around an already-escaped raw query is still allowed; PostgreSQL
lowering must not turn user wildcard characters back into pattern syntax.

Do not replace this with Java `Locale.ROOT`, `ILIKE`, `citext`, `unaccent`, a new collation, ICU configuration, a
schema rewrite, or a second normalized shadow column.

### Index/query-plan requirement

Keep existing `lower(column)` pg_trgm/index alignment.

The maintenance must not rewrite indexed stored expressions away from the V2 index shape. Existing actual
query-plan/index proofs remain valid or must be updated with equivalent real PostgreSQL evidence.

## Finding B — Snippet matching must be locale-independent and offset-safe

### Current defect

Owner snippet helpers create lowercased copies of cleaned content/query using default-locale Java casing and use the
folded-string match index against the original cleaned string.

This is locale-dependent and can be incorrect when case transformation changes UTF-16/code-point length.

### Required snippet policy

Do not derive original-text offsets from an independently lowercased/case-folded copy.

Use an index-safe, locale-independent search over the **original cleaned text**, for example:

1. exact raw `indexOf` first;
2. otherwise a deterministic case-insensitive region/code-point scan that compares original regions without creating
   a length-changing folded surrogate string;
3. use the matched original-text offset to build the existing bounded snippet.

`String.regionMatches(true, ...)`-style logic is acceptable if wrapped/tested carefully; another owner-local
index-preserving implementation is also acceptable.

Required properties:

- JVM default locale does not affect snippet location;
- identical Unicode query/text always locates itself;
- ordinary ASCII upper/lower matching remains case-insensitive under ROOT and `tr-TR`;
- no offset computed in a transformed string is applied to the original string;
- code-point/case expansions cannot produce out-of-range or split-surrogate offsets;
- existing Markdown/HTML stripping, body-field fallback, ellipsis behavior, `<= 240` budget and stored-content
  immutability remain unchanged.

If a SQL BODY hit cannot be anchored by the local index-safe matcher, preserve the existing safe fallback behavior;
do not fabricate an offset.

## Affected production scope

Audit and modify only the Search implementation needed for this defect.

Expected owner search services:

```text
account/internal/application/search/AccountSearchService.java
collection/music/internal/application/search/MusicSearchService.java
collection/shopping/internal/application/search/ShoppingSearchService.java
collection/software/internal/application/search/SoftwareSearchService.java
feed/internal/application/search/FeedSearchService.java
fiction/internal/application/search/FictionSearchService.java
film/internal/application/search/FilmSearchService.java
knowledge/information/internal/application/search/InformationSearchService.java
knowledge/note/internal/application/search/NoteSearchService.java
knowledge/study/internal/application/search/StudySearchService.java
knowledge/vocabulary/internal/application/search/VocabularySearchService.java
location/internal/application/search/LocationSearchService.java
media/internal/application/search/MediaSearchService.java
people/internal/application/search/PeopleSearchService.java
vault/internal/application/search/VaultSearchService.java
```

The implementer must inspect all of these for Java-side query lowercasing and snippet lowercasing, even if a particular
service needs no final diff.

A tiny owner-local helper is allowed when it reduces duplicated snippet logic inside an owning module. Do not create a
new cross-module persistence/search framework, generic service hierarchy, or dependency solely to share casing code.

## Public/API/schema scope

No public Search command/view/result shape change is required.

No change is authorized to:

- `VaultEntryType`;
- Search rank/type-order contract;
- domain/type/tag filters;
- source bounds/pagination;
- Schema v1;
- Flyway V1/V2;
- DBML;
- existing pg_trgm indexes;
- module dependency matrix;
- HTTP/REST/OpenAPI;
- Phase 13 code.

## Mandatory PostgreSQL regressions

Use the real PostgreSQL Testcontainers Search stack and real owner/global contracts.

At minimum cover:

### Owner/global text search

- ASCII title `ID`, query `id`, JVM default `Locale.ROOT` -> hit with existing accepted rank/kind;
- same fixture/query under JVM default `tr-TR` -> identical hit/rank/kind;
- title `İD`, identical query `İD`, JVM `Locale.ROOT` -> exact hit;
- run representative owner services from different modules, not only one service;
- global Search returns the same qualifying entries/ranking under ROOT and `tr-TR`.

### Vault tag search

- tag `ID`, query `id` under ROOT and `tr-TR` -> same tag-origin result;
- tag `İD`, identical query `İD` under ROOT -> hit;
- preserve required-tag filtering, best-tag collapse, ordering and bounds.

### BODY/snippet

- BODY content containing `ID`, query `id`, ROOT and `tr-TR` -> same non-null snippet;
- identical `İD` content/query under ROOT -> non-null snippet containing the matched original text;
- exercise a match near snippet boundaries;
- preserve `<= 240`, plaintext cleanup, fallback, ellipsis and surrogate-pair safety.

### Escaping and fuzzy behavior

Retain regression coverage proving `%`, `_`, and `\` remain literal.

Retain fuzzy-only fixture coverage and transaction-local pg_trgm threshold restoration. Query-side PostgreSQL lowering
must not change the accepted `>= 0.30` semantics or leak connection/session state.

### Locale test hygiene

Tests that temporarily mutate JVM default locale must:

- save the previous default locale;
- restore it in `finally`;
- not run in parallel with tests that rely on global default locale;
- leave no process-global locale contamination for later suites.

## Query-plan / instrumentation evidence

Retain or update the existing real-production-query instrumentation:

- capture representative owner and Vault SQL after the fix;
- prove the query folds bound parameters inside PostgreSQL;
- retain real `EXPLAIN` evidence that high-value indexed short-field search still uses the intended lower-expression
  trigram index strategy where previously proven;
- retain query-count/batch-size evidence from Phase 12;
- do not weaken tests by replacing production queries with test-only SQL.

## Required verification

Focused maintenance tests first, then:

```text
mvn -f backend/pom.xml -ntp clean verify
git diff --check
```

Required final evidence:

- Java/Maven/PostgreSQL/Testcontainers versions;
- total tests/failures/errors/skips;
- focused ROOT/tr-TR/Unicode owner/global/tag/snippet test names and results;
- wildcard/fuzzy/index-plan preservation evidence;
- Spring Modulith/Flyway/Hibernate validation;
- warnings/limitations;
- confirmation that no V1/V2/schema/dependency/public-contract change occurred.

Record implementation evidence in:

```text
docs/implementation/maintenance/milestone-10-12-search-case-normalization/test-evidence.md
```

## Non-goals

- Phase 13 preparation or implementation;
- accent-insensitive search;
- Unicode normalization (NFC/NFD) feature work;
- locale-aware linguistic collation/ranking;
- `unaccent`, `citext`, ICU/collation/schema migration;
- new search projections/tables/columns;
- changing rank buckets or result ordering;
- broad Search refactor;
- generic cross-module string-normalization utility;
- changing Feed/ImportData/Finance/Journal/Personal business behavior;
- changing authentication/security/deployment/frontend/RAG.

## Workflow

This scope is owner-approved and intentionally uses the maintenance exception.

Maintenance is owner committed/pushed as `a881540`; the milestone is `MILESTONE_READY`.
The active handoff retains its historical acceptance marker until ChatGPT archives/resets it; it is not executable.

The maintenance exception was used without a numbered-phase `$codex-pre-handoff-review` gate.

Owner commits/pushes the milestone review/status package next, then gives the latest package to ChatGPT for
post-milestone synchronization/reset and Phase 13 preparation before `$codex-pre-handoff-review`.

Phase 13 remains blocked until the milestone returns `MILESTONE_READY`, the milestone review/status package is
owner committed/pushed, and ChatGPT completes post-milestone synchronization/reset.
