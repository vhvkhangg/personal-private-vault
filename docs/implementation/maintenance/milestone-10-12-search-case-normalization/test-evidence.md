# Phase 10–12 Milestone Maintenance: Search Case-Normalization — Test Verification Evidence

- Date: 2026-10-04 (remediated 2026-10-05)
- Handoff ID: `maintenance-milestone-10-12-search-case-normalization`
- Trigger Finding: M10-12-1 — Search Java/PostgreSQL case-normalization mismatch
- Status: `COMPLETE / FROZEN` — Codex acceptance 2026-10-05; owner committed/pushed `a881540`; FRM10-12-1 closed.
- Implementer: Antigravity
- Final reviewer: Codex
- Pre-maintenance baseline: 815 tests (clean verify, 0 failures, 0 errors, 0 skips)
- Post-maintenance verification: Clean verify passed with **817 tests** (0 failures, 0 errors, 0 skips), execution time 01:51 min, finished 2026-10-05T07:13:35+07:00.
- Focused verification: `GlobalSearchIntegrationTest` passed all **26 tests** (24 baseline + 2 new regression suites), execution time 28.06 s (total time 46.634 s), finished 2026-10-05T07:11:12+07:00.

---

## Codex independent final acceptance — 2026-10-05

`mvn -f backend/pom.xml -ntp clean verify`: **BUILD SUCCESS**, 817 tests, zero failures/errors/skips,
01:53 min, finished `2026-10-05T07:18:21+07:00`. Surefire XML independently confirms 817 cases, including all
26 Search tests. `git diff --check` exits 0. Architecture/Modulith, Flyway V1/V2 and Hibernate validation pass.
All 15 SQL query text blocks were rechecked against HEAD: only approved PostgreSQL-side folding/formatting changes.
The long U+0130 BODY regression closes FRM10-12-1. See [formal acceptance](reviews/2026-10-05-final-codex-acceptance.md).
No separate Codex focused command was run; the independent full run executes all focused tests.
Inherited Lombok/Unsafe and test-support compiler deprecations also remain; no IDE inspection/warning-free claim.
Owner subsequently committed/pushed `a881540`. Fresh milestone verification passed 817 tests, zero failures/errors/skips,
01:47 min, finished `2026-10-05T07:26:35+07:00`; [milestone acceptance](../../phase-12/reviews/2026-10-05-phase-10-12-milestone-codex-acceptance.md)
is `MILESTONE_READY`. Next: owner commits/pushes milestone docs, then ChatGPT post-milestone synchronization/reset.

## Codex independent review & Antigravity remediation — 2026-10-04 / 2026-10-05

- **Codex Final Review (2026-10-04):** Review returned `CHANGES_REQUESTED` on FRM10-12-1 (Medium, tests/evidence only).
  The original casing-expansion test fixture used U+1E9E, which in Java 25 lowercases 1-to-1 (`ß`) and had a short body (<240 chars)
  bypassing window calculation. Required a genuine expanding fixture (U+0130, expanding 1-to-2 under `Locale.ROOT`) before a later
  match in a >240-char body, along with corrections to evidence claims (15 SQL services, 13 snippet helpers, actual bind names).
  No production code changes requested.
- **Antigravity Remediation (2026-10-05):** Addressed via `/antigravity-test-slice`:
  1. Updated `GlobalSearchIntegrationTest.java`: Added a genuine long-body (>240 chars) casing-expansion fixture
     `"\u0130".repeat(300) + " TARGET " + "x".repeat(400)` with query `"target"` under `Locale.ROOT`. In Java under `Locale.ROOT`,
     `\u0130.toLowerCase(Locale.ROOT)` expands from 1 to 2 UTF-16 code units (`i\u0307`), which would produce index 601 in a
     lowercased copy. In the original text (length 708), index 601 fell into the tail window branch (starting at 471), missing
     `TARGET` entirely. The new original-text windowing matches at offset 301, properly producing `"...<sub containing TARGET>..."`
     within the <=240-character budget.
  2. Corrected evidence claims: SQL parameter folding implemented in all 15 search services; 13 search services contain body
     snippet helpers (`MusicSearchService` and `VaultSearchService` do not search body text and have no snippet helpers).
  3. Corrected bind names: parameters bound are `rawQuery`, `prefixPattern`, and `substringPattern`.
  4. Executed focused tests (`GlobalSearchIntegrationTest`: 26 tests, 0 failures, 28.06 s) and full clean verification
     (`mvn clean verify`: 817 tests, 0 failures, 0 errors, 0 skips, 01:51 min). `git diff --check` exited clean.

## 1. Overview of Maintenance Implementation

This maintenance closes Finding M10-12-1 by establishing PostgreSQL as the sole authority for query case-folding in SQL search comparisons, and making snippet extraction locale-independent and offset-safe over original cleaned text.

### Key Changes

1. **PostgreSQL-Side Query Parameter Folding (All 15 Search Services):**
   - Java code trims and validates queries, escaping LIKE metacharacters (`%`, `_`, `\`) on the raw trimmed query before binding.
   - Parameters are bound as raw strings without Java `toLowerCase()`.
   - Parameter names bound: `rawQuery`, `prefixPattern`, and `substringPattern` (along with pagination and control parameters).
   - SQL queries apply `lower(CAST(:rawQuery AS text))` (and equivalent `lower(CAST(:prefixPattern AS text))` / `lower(CAST(:substringPattern AS text))`) to match against `lower(column)` on both sides.
   - Applies across all SQL comparison clauses: exact equality (`=`), prefix (`LIKE ... ESCAPE '\'`), substring (`LIKE ... ESCAPE '\'`), pg_trgm fuzzy (`%`, `similarity`), and Vault tag searches.
   - Preserves indexed `lower(column)` expressions on the table side, aligning with Flyway V2 GIN trigram indexes.

2. **Locale-Independent, Offset-Safe Snippet Matching (13 Search Services with Body Content):**
   - Snippet extraction locates match offsets in the **original cleaned text**, never applying offsets derived from a lowercased or case-folded copy.
   - Audited across all 13 services searching body content (`MusicSearchService` and `VaultSearchService` do not search body text and have no snippet helpers).
   - Strategy: Exact `indexOf(query)` first; fallback to deterministic case-insensitive `findMatchIndex` scanning regions using `regionMatches(true, ...)`.
   - Length-changing casing expansions (e.g. U+0130 expanding from 1 to 2 UTF-16 code units under `Locale.ROOT`) cannot desynchronize character offsets or shift snippet windows.
   - Preserves <= 240 character budget, Markdown/HTML cleanup, body-field fallback, ellipsis rules, and surrogate pair boundaries.

3. **Architecture & Scope Invariants Preserved:**
   - Zero changes to Database Schema v1, Flyway migrations (V1, V2), or DBML.
   - Zero changes to module boundaries or package trees; no shared cross-module search framework or service hierarchy.
   - All 17 Vault result types, ranking rubric (600/550/500/450/400/350/300/200), deterministic tie-breaking, and pagination contracts preserved.
   - Read-only search behavior and privacy-safe logging preserved (no raw query/snippet leakage).

---

## 2. Audit of All 15 Affected Search Services

All 15 owner and Vault search services were systematically audited and updated:

| # | Service | Path | SQL Parameter Folding | Snippet Offset Safety |
|---|---------|------|-----------------------|-----------------------|
| 1 | `AccountSearchService` | `backend/.../account/internal/application/search/AccountSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 2 | `MusicSearchService` | `backend/.../collection/music/internal/application/search/MusicSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | (Music search operates on title/artist; no body snippet helper) |
| 3 | `ShoppingSearchService` | `backend/.../collection/shopping/internal/application/search/ShoppingSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 4 | `SoftwareSearchService` | `backend/.../collection/software/internal/application/search/SoftwareSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 5 | `FeedSearchService` | `backend/.../feed/internal/application/search/FeedSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 6 | `FictionSearchService` | `backend/.../fiction/internal/application/search/FictionSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 7 | `FilmSearchService` | `backend/.../film/internal/application/search/FilmSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 8 | `InformationSearchService` | `backend/.../knowledge/information/internal/application/search/InformationSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 9 | `NoteSearchService` | `backend/.../knowledge/note/internal/application/search/NoteSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 10 | `StudySearchService` | `backend/.../knowledge/study/internal/application/search/StudySearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 11 | `VocabularySearchService` | `backend/.../knowledge/vocabulary/internal/application/search/VocabularySearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 12 | `LocationSearchService` | `backend/.../location/internal/application/search/LocationSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 13 | `MediaSearchService` | `backend/.../media/internal/application/search/MediaSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 14 | `PeopleSearchService` | `backend/.../people/internal/application/search/PeopleSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | `findMatchIndex` via `regionMatches(true, ...)` on original cleaned text |
| 15 | `VaultSearchService` | `backend/.../vault/internal/application/search/VaultSearchService.java` | Bound raw parameters; folded with `lower(CAST(:... AS text))`; `ESCAPE '\\'` | (Vault tag search returns matched tag name directly, no body snippet) |

---

## 3. Verification Commands and Results

### Focused Test Run

```powershell
mvn -f backend/pom.xml -ntp -Dtest=GlobalSearchIntegrationTest test
```

- **Exit code:** `0` (`BUILD SUCCESS`)
- **Total tests run:** 26
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Time elapsed:** 28.06 s (Total time: 46.634 s)
- **Finished at:** 2026-10-05T07:11:12+07:00

### Full Test Verification

```powershell
mvn -f backend/pom.xml -ntp clean verify
```

- **Exit code:** `0` (`BUILD SUCCESS`)
- **Total tests run:** 817
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Build time:** 01:51 min
- **Finished at:** 2026-10-05T07:13:35+07:00

### Git Diff Check

```powershell
git diff --check
```

- **Exit code:** `0` (clean, no whitespace warnings or trailing whitespace errors)

---

## 4. Mandatory Regressions in `GlobalSearchIntegrationTest`

Two new comprehensive integration test methods were added to `GlobalSearchIntegrationTest.java`:

### 1. `verifiesSearchCaseNormalizationUnderRootAndTurkishLocales`

- **ASCII `ID` / `id` queries:**
  - Tested with title/name `"ID"` under both `Locale.ROOT` and Turkish locale `Locale.forLanguageTag("tr-TR")`.
  - Both lowercase `"id"` and uppercase `"ID"` queries return identical hits, match kinds (`PRIMARY_EXACT`), and rank buckets (`600`) across representative modules:
    - `collection.music` (`MusicSearchService`)
    - `feed` (`FeedSearchService`)
    - `knowledge.note` (`NoteSearchService`)
    - `people` (`PeopleSearchService`)
  - Vault tag search: tag `"ID"` matches queries `"id"` and `"ID"` under both `Locale.ROOT` and `tr-TR`.
  - Global Search ranking check: queries `"id"` and `"ID"` produce identical qualifying entry ordering.
- **Unicode `\u0130D` query under `Locale.ROOT`:**
  - Tested with title/name `"\u0130D"` (U+0130 Latin Capital Letter I with Dot Above + D) and tag `"\u0130D"`.
  - Query `"\u0130D"` matches with `PRIMARY_EXACT` (rank 600) across Music, Feed, Note, People, Vault tags, and Global Search.
  - Verifies that two-character query matching succeeds in PostgreSQL without relying on fuzzy similarity rescue.
- **Locale hygiene:**
  - Original default locale is captured before testing and unconditionally restored in a `finally` block.

### 2. `verifiesBodySnippetExtractionCaseNormalizationAndOffsets`

- **ASCII BODY-only fixture:**
  - Primary text does not match `"id"`, body notes contain `"ID"`.
  - Query `"id"` matches as `BODY` (rank 200) under both `Locale.ROOT` and `tr-TR`.
  - Returned snippet is non-null, bounded <= 240 chars, and contains original `"ID"`.
- **Unicode `\u0130D` BODY fixture:**
  - Primary text does not match `"\u0130D"`, body notes contain `"\u0130D"`.
  - Query `"\u0130D"` under `Locale.ROOT` returns non-null snippet containing `"\u0130D"`, bounded <= 240 chars.
- **Boundary match positions:**
  - Start boundary: begins with `"ID"`, ends with `"..."`, length <= 240.
  - End boundary: starts with `"..."`, ends with `"ID"`, length <= 240.
  - Middle boundary (surrounded by 300+ chars on each side): starts with `"..."`, contains `"ID"`, ends with `"..."`, length <= 240.
- **Genuine casing expansion in long body (> 240 chars) under `Locale.ROOT`:**
  - Content: `"\u0130".repeat(300) + " TARGET " + "x".repeat(400)` with query `"target"` and unrelated primary name.
  - In Java under `Locale.ROOT`, U+0130 lowercases to two code units (`i\u0307`), which in the old shifted-window algorithm found index 601 in the lowercased copy instead of original index 301, taking the tail branch and missing `TARGET`.
  - Verifies match kind `BODY`, non-null snippet containing original `"TARGET"`, <= 240 char budget, and proper ellipsis prefix/suffix (`...`).
- **Non-ASCII character preservation before later match:**
  - German capital sharp S `\u1E9E` (`ẞ`) precedes target `"ID"` in body content.
  - Checks original U+1E9E and `ID` character preservation in the short body, not window offsets;
    the separate long U+0130 fixture proves the expansion/window regression.
- **Surrogate pair safety:**
  - UTF-16 supplementary character (U+1F600 Grinning Face `\uD83D\uDE00`) placed near window boundary.
  - Verified snippet starts and ends with valid non-surrogate code points without splitting pairs.
- **Plaintext cleanup:**
  - Markdown formatting (`##`, `**`, `[link](...)`) and HTML tags (`<span>`) are stripped while keeping original content intact.
- **Locale hygiene:**
  - Original default locale is captured before testing and unconditionally restored in a `finally` block.

---

## 5. Complete Test Inventory

- **Baseline pre-maintenance tests:** 815 tests
- **New maintenance integration tests:** 2 tests
- **Total active test suite:** 817 tests (0 failures, 0 errors, 0 skipped)

| Test Class | Baseline | Added | Total | Status |
|:---|:---:|:---:|:---:|:---:|
| `com.vhvkhangg.personalprivatevault.search.GlobalSearchIntegrationTest` | 24 | +2 | 26 | PASS |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 27 | 0 | 27 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeArchitectureTests` | 3 | 0 | 3 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionArchitectureTests` | 3 | 0 | 3 | PASS |
| All other domain integration, unit, and architecture test suites | 758 | 0 | 758 | PASS |
| **Total** | **815** | **+2** | **817** | **PASS** |

---

## 6. Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Modulith:** 2.1.1
- **Hibernate ORM:** 7.4.5.Final
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

---

## 7. Warnings and Limitations Disclosed

- **Normal Build Warnings:** The standard compiler deprecation notice for `CsvImportParser`, ByteBuddy / Mockito self-attaching agent notices on Java 25, and SpringDoc `/v3/api-docs` info messages remain present and harmless as documented in Phase 12.
- **Unindexed Body Text Scanning:** Body searches in owner tables continue to perform sequential scans on unindexed text columns (`notes`, `summary`, `content_markdown`) as designed. Migration V2 provides GIN trigram indexes on indexed short fields only.
- **Forced Planner Checks:** Trigram index capability tests utilize `SET enable_seqscan = off;` to evaluate index access on small test fixtures; this demonstrates index availability and is not a cost-planner guarantee under all data distributions.
- **Zero Schema Alterations:** Schema v1, Flyway migrations (V1, V2), DBML, and database constraints remain completely unchanged.
