# Active Implementation Handoff

- Handoff ID: `phase-12-global-search`
- Created by: Codex, 2026-10-04
- Status: `COMPLETE_FROZEN`
- Owner commit/push: completed 2026-10-04
- Implementer: Antigravity
- Final reviewer: Codex
- Approved preparation baseline: `44fdaa9137472b44217c159e87e5bc2b89ca54d4`

## Goal

Implement Phase 12 PostgreSQL-first global Search across nine approved domains and 17 Vault-backed types through
narrow read-only owner contracts. Search remains a no-table leaf/orchestrator. The owner's current invocation
authorizes the accepted phase scope, not deferred features.

## Sources of truth

- `docs/implementation/phase-12/README.md`: complete query/result, field, ranking, batching, index and test contract.
- `docs/implementation/phase-12/preparation-review.md`: `READY FOR HANDOFF`.
- `docs/implementation/phase-12/reviews/2026-10-04-phase-12-pre-handoff-codex-acceptance.md`: P12-1/P12-2 closed.
- `docs/architecture/search-architecture.md` and `docs/adr/0010-postgresql-first-global-search.md`.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md` and
  `docs/architecture/data-architecture.md`.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and frozen Flyway V1.
- `docs/repository/repository-package-tree.md`, root/backend and affected module `AGENTS.md`.
- `.agents/rules/backend-phase-12-global-search.md`.

Before creation the working tree was clean and HEAD equaled local `origin/main` at the preparation baseline; no
remote fetch was performed. Phase 11 is frozen with retained independent 787-test verification; its handoff is
archived at `docs/implementation/phase-11/handoff.md`. No milestone gate is due before Phase 12. The Phase 10–12
milestone is mandatory after its accepted owner commit/push. This handoff supersedes preparation-era next-step text.

## Implementation targets

Java package paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

- `search/`: `query`, `view`, `enums` named interfaces and internal application orchestration following the canonical
  API direction. Narrow its descriptor to approved owner search interfaces and explicitly needed Vault public types;
  no owned entity/repository/table.
- `vault/search/` and Vault-owned internal application/query code: bounded active/type/tag qualification and exact
  tag-origin candidates through one `vault::search` named interface.
- `people/`, `fiction/`, `film/`, `media/`, `location/`, `knowledge/`, `collection/`, `account/`, `feed/`: one
  top-level public `search` named interface per owner, bounded text search/bulk document lookup, owner-local
  query/repository additions and necessary descriptors. Knowledge/Collection external contracts are parent-owned;
  matching/persistence remains nested-owned, without leaking nested types to Search.
- `backend/src/main/resources/db/migration/V2__add_search_support.sql`: append-only `pg_trgm` plus the required
  search-index inventory, with predicate/expression alignment; additional body indexes require evidence.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/`: focused Search/owner/Vault, migration, query-shape
  and architecture regressions; preserve existing coverage without weakening assertions.
- `docs/repository/repository-package-tree.md`: synchronize implemented search packages only, not ownership or
  frozen module structure. Add meaningful package-info files; remove only placeholders filled by this slice.
- `docs/implementation/phase-12/test-evidence.md` and this handoff's result section: exact implementation evidence.

Frozen-module permission is limited to these read-only search contracts/queries, descriptors and tests. Do not
change existing mutation, validation, identity, CRUD, lifecycle or unrelated query behavior.

## Required behavior / invariants

The entire accepted Phase 12 README is binding; these highlights do not replace it.

- Support exactly its nine-domain/17-type mapping and field inventory. Only Vault-backed standalone results.
  Domain/type filters intersect; a valid empty intersection yields an empty page.
- Trim query, preserve internal whitespace, require 1..200 characters; positive required-tag IDs, at most 10,
  AND semantics; offset 0..500, limit 1..100. Validate/bound all module/candidate/document transport batches.
- Case-insensitive literal exact/prefix/substring, escaped `%`, `_` and escape characters, bound parameters.
  Short-field fuzzy uses explicit `similarity(lower(field), lower(query)) >= 0.30` for query length >= 3 only;
  body/Markdown uses partial matching. No accent folding or session-threshold-only semantics.
- Shared rank buckets: 600 primary exact, 550 primary prefix, 500 primary substring, 450 secondary exact/prefix,
  400 secondary substring, 350 short fuzzy, 300 tag, 200 body. Select the best representation.
- Every source orders **before limiting** by rank DESC, similarity DESC, textual type name ASC, Vault ID ASC.
  SQL uses `CAST(<owner-local entry-type expression> AS text) COLLATE "C" AS type_name`; Java compares
  `VaultEntryType.name()` strings. Never native enum/ordinal order. `primaryText` is display-only.
- Vault owns trash exclusion and required-tag qualification. Feature modules use its public batch contract, never
  Vault tables/repositories. `FILM_CREDIT` is excluded from tag-origin/required-tag search.
- Vault applies active/type/domain/required-tag qualification and per-entry best-tag-similarity collapse **before**
  exact tag-source top-K selection. Materialize selected IDs through bounded owner bulk contracts; inability to
  materialize a selected active supported entry is an integrity failure, not silent replacement/drop.
- For `K = offset + limit <= 600`, each selected domain supplies exact top K qualifying text hits and Vault exact
  top K qualified/deduplicated tag hits. Text candidates rejected by batch qualification require continued bounded
  paging until K qualifiers or exhaustion. No fixed oversampling/post-filtered fixed prefix, load-all or per-hit
  calls. Merge/deduplicate by Vault ID, keep stronger rank/similarity, sort/slice globally; identical scores prefer
  text-origin presentation. Report `hasMore` only when determinable from bounded results.
- Acyclic dependencies: Search -> Vault/People/Fiction/Film/Media/Location/Knowledge/Collection/Account/Feed, never
  owner -> Search. No foreign internals/repositories/tables or nested contracts in Search. Foundations stay independent.
- Read-only sequential fan-out, normal module transactions; no history/business writes, global lock/snapshot or
  executor. Different committed snapshots during concurrent owner edits are acceptable.
- Navigation DTOs and bounded plain-text snippets only; no entities, raw JSON/frontmatter, secrets, object keys or
  highlight HTML. Never log raw terms/results/tags/private identifiers/Markdown/value-bearing vendor details.
  Validation and diagnostics expose safe structural facts only.

## Non-goals

- Finance/Journal/Personal search, non-Vault standalone results or additional module dependencies.
- HTTP/OpenAPI, frontend, deployment, multi-user/authentication changes or Phase 13+ implementation.
- History/analytics, recommendations, semantic/vector/RAG, Elasticsearch/OpenSearch or unaccent.
- Central projection/table, background/event indexing, full-text vectors/generated columns, speculative frameworks.
- V1 edits or logical schema changes: no new tables, columns, constraints, foreign keys, triggers or DBML redesign.
- Unrelated frozen-module refactors, new agents/hooks, commits, pushes, tags or PRs.

## Test/evidence contract

Use JUnit 5/AssertJ, Mockito where isolation adds value, Spring Modulith and real PostgreSQL Testcontainers/Flyway;
never H2. All canonical README testing requirements are mandatory, including:

- All 17 types/fields, ranking/match kinds, bounds, literal wildcards/escapes, case, explicit fuzzy threshold and
  short-query suppression, body/Markdown, filter intersections, tags, trash, deduplication, pagination, privacy-safe
  failure paths and no side effects.
- P12-1: more-than-K equal-score tag hits with reverse ID/title order, offset/cross-domain ties, duplicate matching
  tags, early rejected/late qualifying required-tag candidates, text/tag overlap and bounded bulk materialization.
- P12-2: PostgreSQL tag-only ALBUM/IMAGE and multi-type Media text LIMIT/OFFSET with more-than-K equal-score
  candidates; cross-domain pages compare source membership/order to the Java comparator. Post-LIMIT resorting
  must not mask native-enum SQL mistakes.
- V2 after frozen V1, extension/index inventory, unchanged V1 tables/columns/constraints, representative partial/fuzzy
  query-plan compatibility with trigram expression indexes; record actual plans and limitations.
- Query-count/batch evidence as candidate/result counts vary: no N+1, per-hit calls or load-all; qualification
  continues across bounded pages. Modulith proves dependency direction, named-interface isolation, parent/nested
  ownership and absence of Search persistence.

Iterate focused tests, preserve all 787 baseline tests, then run from repository root:

```text
mvn -f backend/pom.xml -ntp clean verify
git diff --check
```

Repository Maven wrapper equivalent is acceptable; record the exact command executed. Required evidence file:
`docs/implementation/phase-12/test-evidence.md`. Include Java/Maven/PostgreSQL/Testcontainers versions, exit codes,
total tests/failures/errors/skips, focused tests, migration/index/query-plan/query-count proof, Modulith results,
warnings and limitations. Historical 787-test evidence does not replace a fresh final run.

## Constraints / risks

- Use `global-search-domain-modeling`, `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`,
  `jpa-postgresql-persistence` and `backend-testing` from `.agents/skills/`.
- Exact qualified top-K and SQL/Java enum-name ordering are high-risk correctness boundaries; prove them on
  PostgreSQL, not only mocks. Query/index efficiency is an implementation proof obligation, not assumed.
- Keep APIs capability-oriented and reuse owner rules; no generic base repositories or speculative patterns.
- If accepted scope conflicts with a frozen baseline, stop/report it; do not silently expand permission.
- Graphify was limited navigation; canonical files govern. No production changes or implementation tests were
  performed during handoff creation. Owner commits/pushes only after Codex final acceptance.

## Implementation result

- Remediation slice submitted by Antigravity for FR12-4 and FR12-6 following Codex final re-review 3; Codex closure is recorded below, not inferred from implementer claims.
- Test evidence: [`docs/implementation/phase-12/test-evidence.md`](../phase-12/test-evidence.md).
- Verification results:
  - `mvn -f backend/pom.xml -ntp clean verify`: Exit status 0, BUILD SUCCESS, 815 tests run (0 failures, 0 errors, 0 skipped), 01:44 min, finished 2026-10-04T19:57:40+07:00. All 787 baseline tests preserved + 28 new/remediated tests passing (4 architecture tests + 24 search integration tests).
  - `git diff --check`: Exit status 0 (clean, no whitespace warnings or errors).
  - `powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1`: Exit status 0.
- Implementer-reported remediation details:
  - **FR12-1 (Truthful bounded lookahead & hasMore):** Closed in prior review.
  - **FR12-2 (Structural input validation):** Closed in prior review.
  - **FR12-3 (Plaintext snippets and budget cap):** Closed in prior review.
  - **FR12-4 (Pinned physical connection threshold isolation & query plans):**
    - Remediation implemented strictly in `GlobalSearchIntegrationTest.java`: dedicated physical `Connection` checked out from the test container `DataSource` and wrapped in `SingleConnectionDataSource(pinnedConn, true)`.
    - Real `MusicSearchService` wired via Spring `ProxyFactory` with `DataSourceTransactionManager(pinnedDataSource)` and `AnnotationTransactionAttributeSource`, ensuring service transaction executes on the exact same physical PostgreSQL backend connection.
    - Verified identical PostgreSQL PID (`serviceObservedPid == pinnedConnPid`) between direct connection check and instrumented service execution (`SELECT pg_backend_pid()`).
    - Verified candidate fixture "Symphony" vs query "symphoni" matches fuzzy-only (`startsWith` false, `contains` false) with exact PostgreSQL similarity measured as `0.6363636` (satisfying $0.30 \le 0.6363636 < 0.80$).
    - Direct query on `pinnedConn` under prior session threshold 0.8 proves exclusion (`lower(title) % 'symphoni' == false`) without transaction override.
    - Inside transaction, `SET LOCAL pg_trgm.similarity_threshold = 0.3;` executes on `pinnedConn`, successfully finding "Symphony" as a `SHORT_FUZZY` match.
    - After transaction completion, `SHOW pg_trgm.similarity_threshold` on `pinnedConn` proves threshold restored to `0.8`.
    - `finally` block safely resets session threshold back to baseline `0.3`.
  - **FR12-5 (Comprehensive PostgreSQL test suite & transparent query instrumentation):** Closed in prior review.
  - **FR12-6 (Documentation synchronization & factual evidence):**
    - Updated `docs/implementation/phase-12/test-evidence.md` with:
      - Measured similarity `0.6363636` and pinned PID proof details.
      - Full captured query plans for Music candidate query (`Bitmap Heap Scan` on `music_tracks`, `Bitmap Index Scan` on `idx_music_tracks_title_trgm`) and Vault candidate query (`GroupAggregate`, `Incremental Sort`, `Index Scan` on `tags_pkey` and `vault_entries_pkey`).
      - Planner disclosures explaining `SET enable_seqscan = off;` was used during isolation tests to verify index path capability on small test datasets against PostgreSQL cost-based preference for sequential scans on small tables.
      - Clear distinction between full Vault production query plan and handwritten control query on tags (`Bitmap Index Scan` on `idx_tags_name_trgm`).
      - Full plan and limitations for unindexed note content body search (`Seq Scan` on `notes` with `Filter: ((lower(content_markdown) ~~ '%symphonic%') OR (lower(summary) ~~ '%symphonic%'))`).
      - Added existing pre-Phase-12 compiler deprecation notice for `importdata.internal.parsing.CsvImportParser`; normal compilation does not identify the deprecated member without additional diagnostics.
      - Updated test counts and execution timestamps.

## Codex remediation

2026-10-04 Codex [final acceptance](reviews/2026-10-04-phase-12-final-codex-acceptance.md):
**READY FOR OWNER COMMIT**. Independent clean verify passed 815 tests, zero failures/errors/skips,
01:28 min, finished 20:04:55+07:00. FR12-1–FR12-6 closed; P12-1/P12-2 remain closed.

- FR12-1–FR12-3 and FR12-5 remain closed, including transparent real SQL/batch counting, late qualification
  `[50, 5]`, actual maximum lookahead datasets and all 23 expected index mappings.
- FR12-4 closed: same physical connection is pinned for prior-0.8 setup, the real proxied Music service transaction
  and post-completion observation. Actual service-query PID matches setup PID; measured fuzzy-only similarity
  is approximately 0.6363636, excluded without override, returned as SHORT_FUZZY with local 0.3, restored to 0.8
  after transaction completion and cleaned to test baseline 0.3 in finally.
- FR12-6 closed: full captured Music/Vault plans, forced planner settings, separate tags-only control, measured
  threshold proof, correct ranks/null semantics/counts/package docs and observed warnings are recorded.
  Codex corrected minor evidence wording and synchronized current status. Historical reviews remain unchanged.

No implementation/test remediation remains. Owner commit/push is required before phase closeout/freeze.

## Final review

Codex final acceptance returned `READY FOR OWNER COMMIT`; FR12-1–FR12-6 closed after independent 815-test verification.
Commit message: `feat(search): add PostgreSQL-first global search`.
Next step: owner commits/pushes, then gives the latest package to ChatGPT for Phase 12 closeout.
After owner commit/push and phase closeout, run the mandatory Phase 10–12 `$codex-milestone-review` before Phase 13.
