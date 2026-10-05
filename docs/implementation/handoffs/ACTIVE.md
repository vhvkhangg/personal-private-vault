# Active Implementation Handoff

- Handoff ID: `maintenance-milestone-10-12-search-case-normalization`
- Created by: Codex, 2026-10-04
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: owner-approved M10-12-1 maintenance of frozen Phase 12 read-only Search extensions only
- Owner approval: 2026-10-04, recorded in the canonical maintenance scope (`APPROVED FOR HANDOFF`)
- Committed implementation baseline: `449eaf686f5c869e2ec49d59051d2e63b82e5f83`

## Goal

Close M10-12-1 by making SQL Search comparisons PostgreSQL-folded and snippets locale-independent with original-text
offsets. This temporarily reopens only approved query/snippet paths, not frozen owner mutation behavior. Phase 13
remains blocked; milestone status remains `CHANGES_REQUESTED` until accepted maintenance and milestone re-review.

## Sources of truth

- `docs/implementation/maintenance/milestone-10-12-search-case-normalization/README.md` — approved targets,
  PostgreSQL-only folding/snippet policy, non-goals and mandatory regressions; maintenance preparation exception applies.
- `docs/implementation/phase-12/reviews/2026-10-04-phase-10-12-milestone-codex-review.md` — measured M10-12-1 failures.
- `docs/implementation/phase-12/milestone-review.md` — canonical milestone gate, not implementation authority.
- `docs/implementation/phase-12/README.md` and archived `handoff.md` — retained Search behavior only.
- Root/backend/module/test `AGENTS.md`, including approved M10-12-1 module exceptions.
- Frozen `docs/architecture/module-dependency-matrix.md`, `module-boundaries.md`, `search-architecture.md`;
  `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`, Flyway V1/V2 and
  `docs/repository/repository-package-tree.md` — no baseline change authorized.

## Implementation targets

- Audit all **15 owner/Vault SearchService files** enumerated in the scope's “Affected production scope” section:
  Account; Collection Music/Shopping/Software; Feed; Fiction; Film; Knowledge Information/Note/Study/Vocabulary;
  Location; Media; People; Vault. Modify only SQL query-parameter folding and affected snippet helpers.
- Tiny owner-local helpers are permitted only as the scope allows; no shared cross-module framework/dependency.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/search/GlobalSearchIntegrationTest.java` — real owner/global/
  Vault regression and actual-query evidence updates. Focused owner-local snippet-offset tests may be added as needed.
- Canonical maintenance `test-evidence.md` and this handoff's implementation-result section — exact audit/change/test
  results. Update only directly affected docs if necessary.

## Required behavior / invariants

- Trim/validate the raw query as before. Bind **raw** query and raw-query LIKE-escaped patterns; do not lowercase SQL
  parameters in Java. PostgreSQL must fold both sides consistently in all WHERE/rank/similarity/tag expressions.
  Example: `lower(column) = lower(CAST(:rawQuery AS text))`; equivalent bind naming/layout is acceptable.
- Retain indexed stored-side `lower(column)`. Escape literal percent, underscore and backslash **before** binding;
  prefix/substring wildcards remain deliberate wrappers. Parameterize values; never interpolate them into SQL.
- Snippets locate matches in **original cleaned text**, exact-first with an index-preserving, locale-independent
  fallback or equivalent approved implementation. Never apply folded-copy offsets to original text; budget with
  original matched extent. If unanchorable, retain safe fallback rather than inventing an offset.
- Preserve plaintext Markdown/HTML cleanup, body-field fallback, ellipsis behavior, <=240-character budget,
  surrogate safety and unchanged stored Markdown. Casing expansion before/within a match must not shift the window.
- Preserve 17 result types; ranks 600/550/500/450/400/350/300/200; textual type-name then ID ordering; required-tag
  AND/trash/type qualification, tag collapse/deduplication and parent Knowledge/Collection boundaries.
- Retain fuzzy >=0.30, query-length >=3 suppression and transaction-local threshold restoration; sources remain
  bounded at 601 with accepted K+1 lookahead, truthful hasMore, paged qualification and bulk materialization.
- Search remains read-only; no raw query, snippet, tag/private identifier or vendor diagnostics in application logs/errors.
  Evidence uses synthetic fixtures only; never include real private payloads or secrets.

## Non-goals

- No Phase 13 preparation/code, REST/OpenAPI, public Search DTO/contract shape changes or feature mutations.
- No Phase 10 Feed/ImportData or Phase 11 Finance/Journal/Personal business changes; Feed's Phase 12 search path only.
- No schema/DBML/V1/V2/index/dependency/collation rewrite, ILIKE/citext/unaccent/ICU replacement, NFC/NFD feature,
  accent-folding or linguistic ranking promise, shadow column/projection/history or new search framework.
- No unrelated refactor, placeholder/warning cleanup, auth/deployment/frontend/RAG or agent/hook changes.

## Test/evidence contract

- Real PostgreSQL Testcontainers and real owner/global/Vault contracts; no H2 or rewritten test-only source SQL.
- ROOT and tr-TR: title/tag `ID`, query `id` as specified, **also uppercase query `ID`** to expose the actual Java
  default-locale defect. Compare exact IDs/ranks/kinds through representative owners in different modules and global Search.
- ROOT: title/tag `\u0130D` (U+0130 followed by D), identical query -> exact text hit / tag hit. This two-character
  fixture cannot rely on fuzzy rescue. Merely adding Locale.ROOT is not an acceptable SQL fix.
- BODY-only fixtures: `ID` with query `id` under both locales and identical U+0130 content/query under ROOT -> anchored,
  non-null snippets containing original matched text. Include long/boundary text and length-changing casing before a
  later match; assert original offsets, <=240 budget, cleanup/fallback/ellipsis and supplementary Unicode preservation.
- Save/restore prior JVM locale in finally; ensure locale-mutating tests cannot overlap other locale-sensitive tests.
- Retain all 815 baseline tests, wildcard escaping, actual 600/602-row lookahead, late qualification [50,5], query/batch
  counts, index inventory, captured production Music/Vault plans and pinned same-connection fuzzy-threshold proof.
  Update affected captured binds to raw text and explain real updated queries; disclose forced settings/control queries/
  body-scan limitations, not a normal-planner or latency guarantee.
- Focused: `mvn -f backend/pom.xml -ntp -Dtest=GlobalSearchIntegrationTest test`, plus any added owner-local suite.
  Final: `mvn -f backend/pom.xml -ntp clean verify` and `git diff --check`.
- Evidence: `docs/implementation/maintenance/milestone-10-12-search-case-normalization/test-evidence.md` — commands,
  versions, counts/failures/errors/skips, named ROOT/tr-TR/Unicode owner/global/tag/snippet tests, all-15-file audit,
  SQL/index/batch/fuzzy preservation, warnings/limitations, Modulith/Flyway/Hibernate results and unchanged
  schema/dependency/public-contract confirmation.

## Constraints / risks

- Implementation is committed; preparation/status changes are uncommitted and belong to the owner. Preserve them.
  No commit/push/tag/PR. Do not modify the archived Phase 12 handoff or historical reviews.
- Java/PostgreSQL lowercasing differs even under ROOT; folded-string offsets are unsafe. A locale-only patch or
  global JVM-locale override does not close M10-12-1. Audit every WHERE/rank/similarity branch, not just eligibility.
- Keep owner-local persistence and parent contracts. Stop/report conflicts or needed scope expansion.
- Engineering skills: `global-search-domain-modeling`, `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`, `jpa-postgresql-persistence`,
  `backend-testing`; `graphify-context` for navigation. Authentication work is not in scope.
- Codex performed planning/source checks only; no production/test implementation or build run during handoff creation.

## Implementation result

Implemented by Antigravity on 2026-10-04; remediated on 2026-10-05. Status transitioned to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

### Summary of Changes

1. **PostgreSQL-Side Query Parameter Folding (All 15 Search Services):**
   - Audited and updated all 15 owner/Vault SearchService classes:
     - `AccountSearchService.java`
     - `MusicSearchService.java`
     - `ShoppingSearchService.java`
     - `SoftwareSearchService.java`
     - `FeedSearchService.java`
     - `FictionSearchService.java`
     - `FilmSearchService.java`
     - `InformationSearchService.java`
     - `NoteSearchService.java`
     - `StudySearchService.java`
     - `VocabularySearchService.java`
     - `LocationSearchService.java`
     - `MediaSearchService.java`
     - `PeopleSearchService.java`
     - `VaultSearchService.java`
   - Bound parameters using raw trimmed query and raw-query LIKE-escaped patterns (`rawQuery`, `prefixPattern`, `substringPattern`) without Java-side `toLowerCase()`.
   - SQL expressions fold both sides PostgreSQL-side via `lower(CAST(:... AS text))` for exact (`=`), prefix (`LIKE ... ESCAPE '\'`), substring (`LIKE ... ESCAPE '\'`), pg_trgm fuzzy (`%`, `similarity`), and Vault tag searches.
   - Retained indexed stored expressions `lower(column)` for trigram index compatibility.

2. **Locale-Independent, Offset-Safe Snippet Matching (13 Search Services with Body Content):**
   - Discontinued applying offsets from lowercased copies to original text across all 13 services searching body content (`MusicSearchService` and `VaultSearchService` do not search body text and have no snippet helpers).
   - Implemented exact `indexOf(query)` first, followed by case-insensitive `findMatchIndex` using `regionMatches(true, ...)` across the original cleaned text.
   - Casing expansions (e.g. U+0130 expanding from 1 to 2 UTF-16 code units under `Locale.ROOT`) cannot desynchronize character offsets or shift snippet windows.
   - Preserved <= 240 character budget, Markdown/HTML stripping, body-field fallback, ellipsis rules, and surrogate pair boundaries.

3. **Mandatory Regressions Added to `GlobalSearchIntegrationTest.java`:**
   - `verifiesSearchCaseNormalizationUnderRootAndTurkishLocales`:
     - Proves queries `"id"` and `"ID"` for title/tag `"ID"` produce identical hits, kinds (`PRIMARY_EXACT`), and rank buckets (`600`) across representative modules (`collection.music`, `feed`, `knowledge.note`, `people`, `vault` tags, and `GlobalSearchOperations`) under both `Locale.ROOT` and `tr-TR`.
     - Proves two-character query `"\u0130D"` for title/tag `"\u0130D"` produces exact hits (`PRIMARY_EXACT`, rank 600) under `Locale.ROOT` without relying on fuzzy similarity rescue.
     - Proves Global Search produces identical qualifying entry ordering under both lowercase and uppercase queries across JVM default locales.
   - `verifiesBodySnippetExtractionCaseNormalizationAndOffsets`:
     - Proves BODY match for ASCII `"ID"` with query `"id"` under both `Locale.ROOT` and `tr-TR` returns non-null snippet containing original `"ID"`.
     - Proves BODY match for Unicode `"\u0130D"` with query `"\u0130D"` under `Locale.ROOT` returns non-null snippet containing `"\u0130D"`.
     - Proves boundary positioning (start, middle, end) adheres to the <= 240 char budget and proper ellipsis formatting.
     - Proves genuine casing expansion in long body (> 240 chars) under `Locale.ROOT` (`"\u0130".repeat(300) + " TARGET " + "x".repeat(400)` with query `"target"`): original offset 301 is preserved and windowed instead of shifted to 601, returning a <= 240 char snippet containing `"TARGET"` with ellipsis padding (`...`).
     - Checks U+1E9E and `"ID"` character preservation in a short body; the separate long U+0130 fixture proves window offsets.
     - Proves surrogate pair safety near boundary (`\uD83D\uDE00`) avoids splitting surrogate code units.
     - Proves Markdown and HTML tags are cleaned from snippet while preserving content.

### Verification Results

- **Focused Test Suite:**
  `mvn -f backend/pom.xml -ntp -Dtest=GlobalSearchIntegrationTest test`
  - Result: `BUILD SUCCESS` (26 tests run, 0 failures, 0 errors, 0 skipped, 28.06 s, total time 46.634 s).
  - Finished at: 2026-10-05T07:11:12+07:00.
- **Full Verification Suite:**
  `mvn -f backend/pom.xml -ntp clean verify`
  - Result: `BUILD SUCCESS` (817 tests run, 0 failures, 0 errors, 0 skipped, 01:51 min).
  - Finished at: 2026-10-05T07:13:35+07:00.
- **Git Diff Check:**
  `git diff --check`
  - Result: Exit code 0 (clean).
- **Canonical Evidence File:**
  `docs/implementation/maintenance/milestone-10-12-search-case-normalization/test-evidence.md`.

## Codex remediation

Codex review, 2026-10-04: **FRM10-12-1 (Medium), tests/evidence only; CLOSED on 2026-10-05**. No production correction requested.
Formal review: `../maintenance/milestone-10-12-search-case-normalization/reviews/2026-10-04-final-codex-review.md`.

- Replace/add the claimed casing-expansion fixture: U+1E9E lowercases from one character to one, and its short body
  bypasses snippet windowing. Use genuinely expanding U+0130 before a later match in >240-character BODY content
  under ROOT; assert the original-text window contains the match and fails with the old shifted-offset algorithm.
  See the formal review's concrete 301-versus-601-offset fixture. Preserve locale restoration and existing regressions.
- Correct evidence/result claims: SQL changes in 15 services; snippet helpers in 13 (not Music/Vault); real binds
  `rawQuery`/`prefixPattern`/`substringPattern`; U+1E9E is not a lowercase expansion fixture.
- Antigravity `/antigravity-test-slice`; focused Search tests, full clean verify and diff check. Record actual results
  and resubmit as `IMPLEMENTED_AWAITING_CODEX_REVIEW`. Keep all production changes and frozen baselines unchanged.

**Remediation Status (2026-10-05):** Addressed via `/antigravity-test-slice`:
- Replaced short U+1E9E casing expansion claim in `GlobalSearchIntegrationTest.java` with genuine expanding U+0130 long-body fixture (`"\u0130".repeat(300) + " TARGET " + "x".repeat(400)`, query `"target"`) under `Locale.ROOT`. Verified match at offset 301 is preserved within <= 240 char budget (`...<sub containing TARGET>...`) while old algorithm shifted offset to 601 (tail branch) and omitted `TARGET`.
- Corrected evidence and summary claims: 15 search services audited with SQL parameter folding; 13 search services have snippet helpers (Music and Vault have no body snippet helpers). Actual bind names: `rawQuery`, `prefixPattern`, `substringPattern`.
- Re-verified focused suite (26 tests, 0 failures, 28.06 s), full clean verify (817 tests, 0 failures, 0 errors, 0 skips, 01:51 min), and `git diff --check` (clean exit 0). Resubmitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

## Final review

**READY FOR OWNER COMMIT**, 2026-10-05. FRM10-12-1 closed; no blocking findings remain.
Formal acceptance: `../maintenance/milestone-10-12-search-case-normalization/reviews/2026-10-05-final-codex-acceptance.md`.
Independent clean verify: 817 tests, zero failures/errors/skips, 01:53 min,
finished `2026-10-05T07:18:21+07:00`; diff check clean.

Suggested commit message: `fix(search): align PostgreSQL casing and snippet offsets`

Next step: owner commits/pushes the accepted maintenance package, then reruns `$codex-milestone-review`.
Phase 13 still requires `MILESTONE_READY`, committed milestone docs and ChatGPT post-milestone synchronization/reset.
