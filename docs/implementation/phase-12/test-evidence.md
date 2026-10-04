# Backend Phase 12 — Test Verification Evidence

- Date: 2026-10-04
- Handoff ID: `phase-12-global-search`
- Implementer: Antigravity
- Status: `READY FOR OWNER COMMIT` after [Codex final acceptance](reviews/2026-10-04-phase-12-final-codex-acceptance.md); FR12-1–FR12-6 closed. Owner commit/push and phase closeout remain pending.
- Implementer verification: Clean verify passed with 815 tests (0 failures, 0 errors, 0 skips), execution time 01:44 min, finished 2026-10-04T19:57:40+07:00.
- Codex independent verification (final acceptance): Clean verify passed with 815 tests (0 failures, 0 errors, 0 skips), execution time 01:28 min, finished 2026-10-04T20:04:55+07:00. Surefire contains 815 testcase elements; Search integration suite passed all 24 tests.
- Previous Codex independent verification (re-review 3): Clean verify passed with 815 tests (0 failures, 0 errors, 0 skips), execution time 01:39 min, finished 2026-10-04T19:42:05+07:00.

## Implementation Overview

Phase 12 delivers the PostgreSQL-first cross-module global search engine across all 9 approved feature domains and 17 Vault-backed entry types without table ownership in the `search` orchestration module.

### Core Deliverables

1. **Database Migration (`V2__add_search_support.sql`):**
   - Enabled PostgreSQL `pg_trgm` extension via `CREATE EXTENSION IF NOT EXISTS pg_trgm;`.
   - Created 23 GIN trigram expression indexes on `lower(<column>)` using `gin_trgm_ops` across all searchable columns in all 9 feature domains (persons, fictions, films, film credits, albums, images, brands, locations, study items, information items, vocabulary items, notes, music tracks, shopping items, software items, external accounts, and saved resources).
   - Append-only migration; Schema v1 tables, columns, and foreign keys remain completely untouched.

2. **Domain Read-Only Search Contracts & Implementation:**
   - `vault`: Introduced `VaultSearchOperations`, `VaultTagSearchQuery`, `VaultTagCandidateHit`, and `VaultSearchService` exposed under `@NamedInterface("search")`.
   - 9 Feature Modules: Each module exposes `@NamedInterface("search")` with its search operations interface, document DTO, hit DTO, and application service:
     - `people`: `PeopleSearchOperations`, `PeopleSearchDocument`, `PeopleSearchHit`, `PeopleSearchService`.
     - `fiction`: `FictionSearchOperations`, `FictionSearchDocument`, `FictionSearchHit`, `FictionSearchService`.
     - `film`: `FilmSearchOperations`, `FilmSearchDocument`, `FilmSearchHit`, `FilmSearchService`.
     - `media`: `MediaSearchOperations`, `MediaSearchDocument`, `MediaSearchHit`, `MediaSearchService`.
     - `location`: `LocationSearchOperations`, `LocationSearchDocument`, `LocationSearchHit`, `LocationSearchService`.
     - `knowledge`: `KnowledgeSearchOperations`, `KnowledgeSearchDocument`, `KnowledgeSearchHit`, `KnowledgeSearchService` + nested module services (`StudySearchService`, `InformationSearchService`, `VocabularySearchService`, `NoteSearchService`).
     - `collection`: `CollectionSearchOperations`, `CollectionSearchDocument`, `CollectionSearchHit`, `CollectionSearchService` + nested module services (`MusicSearchService`, `ShoppingSearchService`, `SoftwareSearchService`).
     - `account`: `AccountSearchOperations`, `AccountSearchDocument`, `AccountSearchHit`, `AccountSearchService`.
     - `feed`: `FeedSearchOperations`, `FeedSearchDocument`, `FeedSearchHit`, `FeedSearchService`.

3. **Leaf Orchestration Module (`search`):**
   - Public Named Interfaces:
     - `search.enums`: `SearchDomain`, `SearchMatchKind`.
     - `search.view`: `GlobalSearchResult`, `GlobalSearchPage`.
     - `search.query`: `GlobalSearchQuery`, `GlobalSearchOperations`.
   - Internal Application Service: `GlobalSearchService`.
   - Package Descriptor (`package-info.java`): `@ApplicationModule` declaring explicit narrow dependencies solely on `search` named interfaces of `vault` and the 9 feature modules.
   - Module Invariant: `search` owns zero database tables, zero JPA entities, and zero Spring Data repositories.

4. **Deterministic Ranking & Rubric Adherence:**
   - 8-tier ranking rubric:
     - `600`: `PRIMARY_EXACT`
     - `550`: `PRIMARY_PREFIX`
     - `500`: `PRIMARY_SUBSTRING`
     - `450`: `SECONDARY_EXACT` / `SECONDARY_PREFIX`
     - `400`: `SECONDARY_SUBSTRING`
     - `350`: `SHORT_FUZZY` (pg_trgm similarity >= 0.30 on primary or secondary text)
     - `300`: `TAG` (matches tag name)
     - `200`: `BODY` (matches content/notes/body text)
   - Deterministic tie-breaking comparator: `rankBucket DESC`, then `similarity DESC`, then `typeName ASC` (`CAST(type AS text) COLLATE "C"` / `VaultEntryType.name()`), then `vaultEntryId ASC`.
   - Pre-LIMIT SQL ordering: All domain queries enforce `ORDER BY rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC LIMIT :limit` to prevent native-enum order leakage.
   - Short query fallback: Trigram fuzzy matching branches are conditionally omitted when query length < 3 characters.
   - Safe wildcard escaping: SQL wildcards (`%`, `_`, `\`) are escaped as literals using `ESCAPE '\'`.
   - Soft-delete exclusion: All entries with `deleted_at IS NOT NULL` are strictly excluded from text and tag search results.
   - Privacy-safe logging: No raw search queries, snippets, or tags are logged.

5. **Remediation Details (FR12-1 through FR12-6):**
   - **FR12-1 (Truthful `hasMore` via bounded lookahead):** `GlobalSearchService` requests `targetK = Math.min(K + 1, 601)` from all queried domain sources and Vault tag search. Dedupes candidates across sources, sorts globally, and accurately evaluates `hasMore = sortedCandidates.size() > K`. Terminal pages with 1 or exactly K results truthfully return `hasMore = false`.
   - **FR12-2 (Structural input validation):** Validated query length (1..200, non-blank required; blank queries throw `IllegalArgumentException`), positive tag IDs (at most 10), limit bounds (1..601 for domain sources, 1..100 for global), and non-negative offsets (0..500). Domain batch contracts (`lookupDocuments`, `filterQualifyingActiveEntries`) safely short-circuit on null or empty sets by returning empty collections (`Map.of()` / `Set.of()`) without database execution; batches > 601, sets containing null elements, or non-positive IDs throw `IllegalArgumentException`.
   - **FR12-3 (Plaintext snippets and budget cap):** `extractSnippet` returns `null` when a candidate field does not contain the query, enabling fallback to subsequent body fields. Strips Markdown formatting (using lookaround to avoid stripping literal underscores from identifiers like `invoice_2026`) and HTML tags. Enforces strict `<= 240` character window budgeting with near-start and near-end safety, avoiding double-ellipses or truncation violations, and guards against splitting Unicode supplementary surrogate pairs. Stored database Markdown remains untouched.
   - **FR12-4 (Transaction-bound query plans and threshold isolation):** Short-field fuzzy search queries execute with transaction-scoped `SET LOCAL pg_trgm.similarity_threshold = 0.3;` inside `@Transactional(readOnly = true)` to avoid leaking connection state. Captured actual production Music and Vault SQL/parameters emitted by services. Dedicated connection with differing prior session threshold (`0.8`) is pinned via `SingleConnectionDataSource(conn, true)` and instrumented `NamedParameterJdbcTemplate` with connection identity assertion (`serviceObservedPid == pinnedConnPid`). Proved candidate fixture "Symphony" vs "symphoni" is fuzzy-only (`startsWith` false, `contains` false) with exact measured similarity `0.6363636` in `[0.30, 0.80)`. Direct query on `conn` proves fixture is excluded at session threshold `0.8` without override (`lower(title) % 'symphoni'` = false). Real proxied `MusicSearchService` transaction on the pinned connection executes `SET LOCAL` override, returning the fixture with matchKind `SHORT_FUZZY`. Upon transaction completion, session threshold on the pinned connection is verified restored to `0.8`. Original setting `0.3` is restored in `finally`. Full plan texts recorded under `SET enable_seqscan = off;` and `SET LOCAL pg_trgm.similarity_threshold = 0.3;` for captured queries, verifying `Bitmap Index Scan` on `idx_music_tracks_title_trgm` and `idx_tags_name_trgm`. Evaluated unindexed body search performing `Seq Scan on notes` as expected under `SET enable_seqscan = on;`.
   - **FR12-5 (Adversarial test suite and transparent query instrumentation):** 24 test methods in `GlobalSearchIntegrationTest` covering adversarial cases. Transparently instrumented `CountingNamedParameterJdbcTemplate` on production services: measured tag query count, domain candidate query count, Vault qualification batch sizes, and bulk lookup materialization batch sizes without altering business logic or arguments. Proved: 1 tag query + 1 lookup with batch size 1 for single tag hit; 1 tag query + 1 lookup strictly bounded to selected page (batch size 10, not all 20 DB candidates); 1 candidate query + 0 materialization calls for domain text search (primary text already projected); 2 candidate queries (page size 50) + 2 batch qualifications to Vault (sizes 50, 5) + 0 materialization calls for late-qualifying dataset; and mixed text-plus-tag candidate materialization. Maximum lookahead dataset with 600 rows proves terminal exhaustion (`offset=500, limit=100 -> hasMore=false`), and 602 rows proves nonterminal lookahead capped at `targetK=601` (`hasMore=true`). Mapped all 23 Flyway V2 indexes to exact table, column expression, and `gin_trgm_ops` opclass in `pg_indexes`.
   - **FR12-6 (Documentation synchronization):** Synchronized package tree (all owner, nested, and Vault search capabilities, plus Feed's `vault::search` dependency), test evidence (exact executed test names, corrected ranking rubric, actual query plans, observed warnings/limitations, clear distinction between implementer and Codex independent runs). Antigravity submitted `IMPLEMENTED_AWAITING_CODEX_REVIEW`; Codex final acceptance subsequently synchronized current status to `READY FOR OWNER COMMIT`.

## Final Verification Commands

```powershell
mvn -f backend/pom.xml -ntp clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Implementer build time:** 01:44 min
- **Finished at:** 2026-10-04T19:57:40+07:00
- **Total tests run:** 815
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

```powershell
git diff --check
```

- **Exit status:** `0` (clean, no whitespace warnings or errors)

```powershell
powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1
```

- **Exit status:** `0`

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Modulith:** 2.1.1
- **Hibernate ORM:** 7.4.5.Final
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Test Counts and Summary

- **Total tests run:** 815
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Test Count Composition

- **Approved pre-Phase-12 baseline:** 787 tests
- **Phase 12 architecture tests:** 4 tests added to `ApplicationArchitectureTests` (from 23 to 27 tests)
- **Phase 12 global search integration tests:** 24 tests in `GlobalSearchIntegrationTest`
- **Total tests:** 787 + 4 + 24 = 815 tests

### Breakdown by New / Remediated Test Suite

| Test Class | Test Count | Failures | Errors | Result |
| :--- | :---: | :---: | :---: | :---: |
| `com.vhvkhangg.personalprivatevault.search.GlobalSearchIntegrationTest` | 24 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 27 (+4) | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionArchitectureTests` | 3 | 0 | 0 | PASS |

### Complete Test Inventory in `GlobalSearchIntegrationTest`

1. `searchesAll17VaultBackedTypes`: Verifies that all 17 Vault-backed entry types across all 9 domains are searchable and return correctly populated `GlobalSearchResult` objects.
2. `verifiesRankingRubricOrder`: Validates the complete ranking hierarchy: 600 > 550 > 500 > 450 > 400 > 350 > 300 > 200.
3. `verifiesFuzzyMatchingThresholdAndShortQuerySuppression`: Proves that short queries (< 3 chars) suppress trigram matching, and fuzzy matching respects the >= 0.30 similarity threshold.
4. `verifiesLiteralWildcardEscaping`: Confirms that `%`, `_`, and `\` characters are treated as literal text and do not trigger wildcard matching.
5. `verifiesP121EqualScoreTagCandidatesOrdering`: Proves that candidates with equal rank buckets and similarities break ties deterministically by type name and vault entry ID.
6. `verifiesP121LateQualifyingRequiredTagCandidates`: Validates late tag qualification against candidates matching text searches.
7. `verifiesP121DeduplicationAndOriginPreference`: Confirms that when an entry matches both text and tags, it deduplicates to the highest-scoring candidate hit.
8. `verifiesP122NativeEnumVsAlphabeticalOrder`: Confirms tie-breaking uses alphabetical type name (`typeName ASC`) rather than native PostgreSQL enum ordinal order.
9. `verifiesP122AlbumVsImagePreLimitSqlSelection`: Proves that `CAST(type AS text) COLLATE "C"` ensures `ALBUM` precedes `IMAGE` in pre-LIMIT SQL candidate selection when >K candidates exist, preventing post-LIMIT sorting masks.
10. `verifiesP122TagOnlyAlbumVsImagePreLimitSqlLimiting`: Proves that tag-only candidate limiting in Media search orders by alphabetical type name before limiting.
11. `verifiesP121MultipleMatchingTagsCollapsedBeforeLimiting`: Confirms that multiple matching tags for an entry are collapsed to the highest similarity candidate before SQL top-K limiting in Vault tag search.
12. `verifiesP121EarlyTagSourceRequiredTagRejection`: Verifies that candidate rejection during Vault tag qualification continues paging across candidate batches until K qualifiers or source exhaustion.
13. `verifiesP121MultiTagAndFilteringAndFilmCreditExclusion`: Validates strict multi-tag AND qualification across distinct tags and exclusion of `FILM_CREDIT` entries.
14. `verifiesCrossDomainEqualScoreExactIdMembershipAcrossOffsets`: Evaluates 5 cross-domain items with identical score 600, asserting exact-ID membership across 4 consecutive pagination pages.
15. `verifiesFr121TruthfulHasMoreAndExhaustion`: Tests exact terminal page with 1 item (`hasMore=false`), exact K=10 terminal page (`hasMore=false`), offset terminal page (`hasMore=false`), multi-domain overlap exhaustion (`hasMore=false`), genuinely more results (`hasMore=true`), and maximum lookahead bounds (`limit=100, offset=500` with 600 seeded rows returning `hasMore=false` and 602 seeded rows returning `hasMore=true` with targetK=601 cap).
16. `verifiesFr122StructuralValidationRejection`: Verifies fast validation failure (throwing `IllegalArgumentException`) for global search query, domain search queries, domain bulk lookups, and Vault tag qualifications when limits, lengths, or IDs are invalid or negative.
17. `verifiesFr123SnippetPlaintextAndEllipsesCap`: Verifies fallback to subsequent body fields when earlier fields lack query match, Markdown and HTML tag removal with `invoice_2026` preservation, ellipsis budgeting strictly `<= 240` characters, Unicode supplementary surrogate preservation, and database source preservation.
18. `verifiesFr124V2MigrationIndexInventory`: Queries `pg_indexes` to map all 23 Flyway V2 GIN trigram indexes to their exact table, column expression (`lower(<col>)`), and `gin_trgm_ops` opclass in PostgreSQL.
19. `verifiesFr124ActualQueryPlansAndTransactionThresholdIsolation`: Executes actual captured Music fuzzy query and Vault tag candidate query through `NamedParameterJdbcTemplate` under dedicated connection, proves session threshold rollback restoration, verifies `Bitmap Index Scan` on trigram indexes with realistic synthetic data, and records unindexed body `Seq Scan` limitation.
20. `verifiesFr125QueryCountInstrumentationAndBoundedBatches`: Transparently instruments collaborator calls via `CountingNamedParameterJdbcTemplate`, proving: bounded batch sizes for tag-origin hits (`[1]`, `[10]`), 0 redundant materialization calls for domain text hits, 2 candidate queries + 2 Vault qualification queries (`[50, 5]`) for late-qualifying datasets, and 0 per-hit N+1 calls.
21. `verifiesRecycleBinExclusion`: Proves that soft-deleted entries (`deleted_at IS NOT NULL`) are completely excluded from text and tag searches.
22. `verifiesPagination`: Verifies offset, limit, and deterministic `hasMore` pagination without cross-page overlap.
23. `verifiesFilterIntersection`: Confirms that disjoint domain and entry type filters return an empty page cleanly.
24. `verifiesGinTrigramIndexPlan`: Uses `EXPLAIN` with `SET enable_seqscan = off` to prove that PostgreSQL's query planner utilizes the Flyway V2 GIN trigram expression index (`idx_persons_name_trgm`).

### Architecture Test Inventory in `ApplicationArchitectureTests`

1. `verifiesSearchModuleConfiguration`: Verifies `search` module configuration and named interfaces (`enums`, `query`, `view`).
2. `verifiesSearchNamedInterfacesExposureAndEncapsulation`: Verifies `search` exposes only public DTOs and operations, hiding internal implementation packages.
3. `verifiesSearchModuleHasNoOwnedEntitiesOrRepositories`: Verifies `search` is strictly a leaf orchestration module owning zero database entities and zero repositories.
4. `verifiesSearchModuleDependenciesAndLeafProperty`: Verifies `search` module dependency constraints and leaf property (no application module depends on `search`).

## Query Plans, Measured Counts, Warnings, and Limitations

1. **Captured Production Query Plans (Index Capability Checks):**
   - In PostgreSQL 18.6 with realistic data distribution (150 music tracks, 50 tagged entries) and `ANALYZE`, under `SET enable_seqscan = off;` and `SET LOCAL pg_trgm.similarity_threshold = 0.3;`:
     - **Captured Production Music Candidate Query Plan:**
       ```text
       Limit
         ->  Result
               ->  Sort
                     Sort Key: (CASE WHEN (lower((title)::text) = 'symphony'::text) THEN 600 WHEN (lower((title)::text) ~~ 'symphony%'::text) THEN 550 WHEN (lower((title)::text) ~~ '%symphony%'::text) THEN 500 WHEN ((lower((title)::text) % 'symphony'::text) AND (similarity(lower((title)::text), 'symphony'::text) >= '0.3'::double precision)) THEN 350 ELSE 0 END) DESC, (CASE WHEN (lower((title)::text) = 'symphony'::text) THEN '0'::real WHEN (lower((title)::text) ~~ 'symphony%'::text) THEN '0'::real WHEN (lower((title)::text) ~~ '%symphony%'::text) THEN '0'::real WHEN ((lower((title)::text) % 'symphony'::text) AND (similarity(lower((title)::text), 'symphony'::text) >= '0.3'::double precision)) THEN similarity(lower((title)::text), 'symphony'::text) ELSE '0'::real END) DESC, id
                     ->  Bitmap Heap Scan on music_tracks m
                           Recheck Cond: ((lower((title)::text) ~~ '%symphony%'::text) OR (lower((title)::text) % 'symphony'::text))
                           Filter: ((lower((title)::text) ~~ '%symphony%'::text) OR ((lower((title)::text) % 'symphony'::text) AND (similarity(lower((title)::text), 'symphony'::text) >= '0.3'::double precision)))
                           ->  BitmapOr
                                 ->  Bitmap Index Scan on idx_music_tracks_title_trgm
                                       Index Cond: (lower((title)::text) ~~ '%symphony%'::text)
                                 ->  Bitmap Index Scan on idx_music_tracks_title_trgm
                                       Index Cond: (lower((title)::text) % 'symphony'::text)
       ```
     - **Captured Production Vault Tag Candidate Query Plan (with required tags):**
       ```text
       Limit
         ->  Sort
               Sort Key: (max(similarity(lower((t.name)::text), 'symphonictag'::text))) DESC, (((ve.entry_type)::text)::text) COLLATE "C", ve.id
               ->  GroupAggregate
                     Group Key: ve.id, (((ve.entry_type)::text)::text)
                     ->  Incremental Sort
                           Sort Key: ve.id, (((ve.entry_type)::text)::text) COLLATE "C"
                           Presorted Key: ve.id
                           ->  Nested Loop
                                 ->  Nested Loop
                                       ->  Nested Loop
                                             ->  GroupAggregate
                                                   Group Key: vet2.vault_entry_id
                                                   Filter: (count(DISTINCT vet2.tag_id) = 1)
                                                   ->  Index Only Scan using pk_vault_entry_tags on vault_entry_tags vet2
                                                         Index Cond: (tag_id = '1'::bigint)
                                             ->  Index Only Scan using pk_vault_entry_tags on vault_entry_tags vet
                                                   Index Cond: (vault_entry_id = vet2.vault_entry_id)
                                       ->  Index Scan using tags_pkey on tags t
                                             Index Cond: (id = vet.tag_id)
                                             Filter: ((lower((name)::text) ~~ '%symphonictag%'::text) OR ((lower((name)::text) % 'symphonictag'::text) AND (similarity(lower((name)::text), 'symphonictag'::text) >= '0.3'::double precision)))
                                 ->  Index Scan using vault_entries_pkey on vault_entries ve
                                       Index Cond: (id = vet.vault_entry_id)
                                       Filter: ((deleted_at IS NULL) AND ((entry_type)::text <> 'FILM_CREDIT'::text) AND ((entry_type)::text = 'PERSON'::text))
       ```
     - **Direct Tags-Only Control Query Plan (Handwritten Control Query):**
       ```text
       Bitmap Heap Scan on tags t
         Recheck Cond: ((lower((name)::text) ~~ '%symphonictag%'::text) OR (lower((name)::text) % 'symphonictag'::text))
         Filter: ((lower((name)::text) ~~ '%symphonictag%'::text) OR ((lower((name)::text) % 'symphonictag'::text) AND (similarity(lower((name)::text), 'symphonictag'::text) >= '0.3'::double precision)))
         ->  BitmapOr
               ->  Bitmap Index Scan on idx_tags_name_trgm
                     Index Cond: (lower((name)::text) ~~ '%symphonictag%'::text)
               ->  Bitmap Index Scan on idx_tags_name_trgm
                     Index Cond: (lower((name)::text) % 'symphonictag'::text)
       ```
     - **Planner Mode Disclosure:** Index capability checks use `SET enable_seqscan = off;`, not normal cost-based selection. The Music query and handwritten tags-only control show trigram index access; the full required-tag Vault plan above uses join/primary-key access instead. The separate Person control also verifies index capability. Small-table normal planning may favor sequential scans; no normal-planner choice or production latency guarantee follows from these forced checks.

2. **Unindexed Body Text Scanning Limitation:**
   - Evaluated under `SET enable_seqscan = on;`:
     ```text
     Seq Scan on notes n
       Filter: ((lower(content_markdown) ~~ '%symphonic%'::text) OR (lower(summary) ~~ '%symphonic%'::text))
     ```
   - The displayed handwritten Note body control performs `Seq Scan on notes`. Other unindexed owner body fields may scan their own tables, not the notes table. Migration V2 adds no body trigram indexes; forced short-field index capability is not a body-search performance guarantee.

3. **Measured Query & Batch Counts:**
   - Single tagged hit: 1 tag query, 1 materialization query (batch size 1).
   - 20 tagged hits, limit 10: 1 tag query, 1 materialization query strictly bounded to the 10 selected hits (batch size 10), 0 per-hit queries.
   - 20 text hits, limit 10: 1 candidate query, 0 materialization queries (primary/secondary text directly returned by candidate projection).
   - Late-qualifying 55 items, limit 1: 2 candidate queries (pages of 50), 2 Vault qualification queries with batch sizes `[50, 5]`, 0 materialization queries.
   - Mixed text + tag hits: materialization query executed strictly for tag-origin hits lacking primary text.

4. **Connection Threshold Isolation & Verification:**
   - Physical connection checked out from `dataSource` and pinned via `SingleConnectionDataSource(conn, true)`:
     - Baseline prior session threshold set to `0.8` on `conn`.
     - Connection identity captured: `pinnedConnPid = SELECT pg_backend_pid()`.
     - Fixture "Symphony" vs query "symphoni" proven fuzzy-only (`startsWith` false, `contains` false).
     - Measured similarity in PostgreSQL: `0.6363636` (satisfying $0.30 \le 0.6363636 < 0.80$).
     - Direct query on `conn` confirms exclusion at threshold `0.8` without override: `SELECT lower(title) % 'symphoni'` returns `false`.
     - Real proxied service transaction executes on the pinned connection via `DataSourceTransactionManager` and Spring `TransactionInterceptor`.
     - Service query executes on `conn` with observed PID matching: `serviceObservedPid == pinnedConnPid`.
     - Fixture is returned with matchKind `SHORT_FUZZY` via `SET LOCAL pg_trgm.similarity_threshold = 0.3;`.
     - Upon transaction completion, `SHOW pg_trgm.similarity_threshold` on `conn` returns `0.8`, proving transaction-scoped override rollback.
     - `finally` block restores connection session threshold back to baseline `0.3`.

5. **Observed Compiler and Runtime Warnings:**
   - `CsvImportParser` (Compiler Deprecation): compilation reports that `com.vhvkhangg.personalprivatevault.importdata.internal.parsing.CsvImportParser` uses or overrides a deprecated API and requests `-Xlint:deprecation` for details; no member-level attribution is claimed from the normal build.
   - `Lombok`: Deprecated Unsafe use warning during compilation.
   - `SpringDocAppInitializer`: "SpringDoc /v3/api-docs endpoint is enabled by default. To disable it in production, set the property 'springdoc.api-docs.enabled=false'" and "/swagger-ui.html endpoint is enabled by default".
   - `Mockito / ByteBuddy`: "Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. A Java agent has been loaded dynamically (byte-buddy-agent-1.18.11.jar). If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning".
   - `JVM`: "OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended".
   - `Testcontainers`: Existing `PostgreSQLContainer` deprecation notices.
   - Zero fatal errors, zero broken invariants, zero test failures.
