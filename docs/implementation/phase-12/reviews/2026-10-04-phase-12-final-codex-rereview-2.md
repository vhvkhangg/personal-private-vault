# Phase 12 Codex final re-review 2

- Date: 2026-10-04
- Scope: implemented `phase-12-global-search`, submitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Baseline: `44fdaa9137472b44217c159e87e5bc2b89ca54d4`; uncommitted implementation/remediation.
- Outcome: **CHANGES_REQUESTED** — FR12-1–FR12-3 closed; FR12-4–FR12-6 remain open for targeted test/evidence remediation.
- No additional production change requested. Preparation P12-1/P12-2 remain closed; Phase 12 is not accepted/frozen.

## Independent verification

Codex ran `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, **815 tests**, zero failures/errors/skips,
01:34 min, finished **2026-10-04T18:57:05+07:00**. Surefire contains 815 testcase elements: retained baseline 787,
four added architecture tests, 24 Search integration tests. V1/V2 migration, Hibernate validation and existing
schema/Modulith tests passed. `git diff --check` passed. Antigravity's 18:47:44 run is implementer verification,
not this independent Codex run.

Environment unchanged: Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith 2.1.1, Hibernate 7.4.5.Final,
Testcontainers 2.0.5, PostgreSQL 18.6 (`postgres:18.6-alpine`).

Ignored diagnostic `backend/target/final-review-diagnostics/Phase12SnippetClosureProbe.java` invoked compiled owner
snippet helpers with the Surefire runtime classpath. Exit 0: **13 owners, 65 fixtures passed**, checking cap/query
preservation, near-end and two-sided windows, supplementary Unicode boundaries and `invoice_2026`. The source helper
blocks also have one identical SHA-256 across all 13 owners. Diagnostic uses synthetic content and disappears on the
next clean build; no tracked production/test source changed by Codex.

## Accepted remediation

- FR12-1/FR12-2 remain closed for implementation; bounded K+1 lookahead and early validation retained.
- **FR12-3 closed:** cap, surrogate-boundary and identifier-underscore regressions are fixed consistently. New real
  Note result tests cover later-field fallback, markup, near-end/two-ellipsis, supplementary/BMP Unicode and source
  preservation. Independent probe confirms the previous failing fixtures across all affected owners.
- Strengthened pre-limit tag collapse/best similarity, two explicit required-tag AND and Film Credit exclusion tests
  now exercise the previously omitted behavior. Cross-domain exact-ID global pages are useful. Oversized/null-element
  batches, invalid tags on empty candidates, nested limit bounds and valid 601-ID transport paths are now covered.
- Package documentation now lists owner/nested/Vault public search capabilities and Feed's `vault::search` dependency.
  Original nonexistent test names/API names and blank-query claims have been removed. Remaining evidence errors below
  prevent FR12-6 closure.

## Remaining findings

Test locations below refer to
`backend/src/test/java/com/vhvkhangg/personalprivatevault/search/GlobalSearchIntegrationTest.java`.

### FR12-4 — Medium — Plan/threshold tests still do not exercise executed production SQL

Locations: test `:1132`, `:1182`, `:1215`, `:1245`, `:1286`;
actual `collection/music/internal/application/search/MusicSearchService.java` and
`vault/internal/application/search/VaultSearchService.java` under the production package root.

The dedicated connection fixes the earlier connection ambiguity, but Music/Vault SQL is still hand-rewritten:
Music assigns 400 in its fallback, omits the actual prefix/rank/similarity cases, type-name key, match kind, escape,
enableFuzzy parameter and offset. Vault omits the actual type/Film Credit/required-tag filters, textual type-name
ordering and selected fields. A separate tags-only query proves the tag index exists, not the full executed Vault
candidate plan. The test never captures SQL/parameters from either production service. It manually sets local 0.3
on a default-0.3 connection, without establishing a different prior threshold, executing production searches or
checking eligible fuzzy results. Removing the production SET LOCAL would not make this test fail.

Required: capture and EXPLAIN the SQL/parameters actually emitted by the owner/Vault services on a known connection
with seeded data and ANALYZE (no production query rewrite needed). Cover representative partial/fuzzy source queries
including required-tag Vault qualification, record full plan text and distinguish forced index-capability checks
from normal planner choices/body-scan limitations. Through the real transaction/proxy path, set a differing prior
session threshold (for example 0.8), prove a >=0.30 but <0.8 fuzzy-only fixture remains eligible, and prove restoration
after transaction completion. Reset test settings on the same connection. Do not force plans in production or add
speculative indexes. Reuse the existing test connection/infrastructure rather than adding a generic framework.

### FR12-5 — High — Instrumentation substitutes business logic instead of measuring it

Locations: test `:1302–1416`, especially `:1327–1332`; maximum lookahead `:883–888`; index definitions `:1122–1127`.

The People wrapper calls the real source with `Set.of()` instead of the requested required tags, truncates to the
source limit, then post-filters that fixed prefix itself. This is the exact qualification pattern the handoff forbids;
it is not transparent instrumentation of production. Its `vaultQualifyCalls` count measures only this added wrapper
call, not the real owner's internal batch qualifications. `peopleSearchCalls == 1` counts an API invocation, not SQL
candidate pages; one bulk API invocation could still hide N per-ID SQL queries. The purported late fixture has two
candidates, with the qualifying Alpha first; it never crosses a candidate-page boundary. Thus the stated query-count
and late qualification proof is not established even though the test passes.

Required: transparently instrument the real proxied services/JDBC/DataSource path without changing arguments or
qualification behavior. Record measured candidate, Vault-qualification, tag-query and owner-materialization query/
batch counts for small versus larger result sets and a late-qualifying dataset exceeding the real candidate page
size. Assert bounded batches and exact selected IDs, continued paging until K+1/exhaustion and no per-ID lookups.
Verify that inserting per-hit SQL or fixed-prefix post-filtering would fail the proof. Keep the useful existing
global bulk-call test only if relabeled and made semantically transparent.

Also complete the still-requested real maximum lookahead proof: current offset 500/limit 100 test queries eleven
entries and returns empty; 601 positive IDs in an empty lookup do not prove selecting an actual 601st result. Use
at least 601 qualifying rows, assert the exact terminal/nonterminal slice and `hasMore`. The index-definition loop
currently checks only `USING gin` and `lower(`; map each expected index to its table/column/expression and
`gin_trgm_ops`, so a same-named index on the wrong lower(column) cannot pass. Preserve the newly completed AND,
collapse, boundary and cross-domain tests; do not reopen unrelated frozen-module mutations.

### FR12-6 — Medium — Evidence still misstates ranks, null semantics and measurements

Locations: `docs/implementation/phase-12/test-evidence.md:7`, `:47–49`, `:61`, `:63–65`, `:146–148`, `:161–176`;
ACTIVE implementation result.

Evidence now assigns SECONDARY_PREFIX to 400 and SECONDARY_SUBSTRING to 350. Canonical and production buckets are
SECONDARY_EXACT/PREFIX 450, SECONDARY_SUBSTRING 400, SHORT_FUZZY 350. It claims null ID sets are rejected, while the
code and its own test deliberately return empty results for them. Hand-written plans/API counters are described as
actual production query plans/counts, and the implementer's run is labeled independent. The section titled Warnings
does not list the observed build/runtime warnings. Full actual plans/measured counts are still not recorded.

Required: correct these exact facts, distinguish API calls from SQL counts and forced capability from actual planner
choice, record results of FR12-4/FR12-5, and include observed warnings/limitations plus precise implementer versus
Codex verification attribution. Retain current valid package docs and exact test inventory. Synchronize current gates;
preserve historical reviews and do not mark the phase complete/frozen before owner commit/push.

## Other review dimensions / limitations

- Inspected business ownership, narrow facades/parent isolation, dependency direction, SQL binding, read-only
  transactions and approved schema scope remain sound. No new production defect beyond the closed snippet finding
  identified in this pass. No Search-owned persistence, reverse dependency, feature write, V1 rewrite or deferred
  HTTP/RAG/frontend scope found. No additional pattern, framework, locking or global snapshot promise is warranted.
- Repeated helpers are now consistent; narrow domain APIs remain preferable to a generic cross-module base service.
  Leaf package-info/placeholder hygiene is retained, and the package documentation improvement is accepted.
- Observed notices remain Lombok deprecated Unsafe use; existing CsvImportParser and test PostgreSQLContainer
  deprecations; Mockito/ByteBuddy dynamic-agent/self-attachment and JVM sharing notices; SpringDoc enabled-endpoint
  warnings. No owner IDE warning supplied and no IDE/Spotless/Checkstyle/SpotBugs inspection run; no warning-free claim.
- Graphify was targeted navigation only. Search, Java/Spring, architecture, persistence, testing, pragmatic SOLID,
  reuse and pattern skills guided review, especially preserving the real qualification path in measurement tests.
  No subagents; only review/status docs and ignored diagnostic source written by Codex.

## Next step

This is explicitly **test/evidence-only remediation**. Run Antigravity `/antigravity-test-slice` for FR12-4–FR12-6,
preserve production behavior and all baseline coverage, retain focused/full verification evidence and return ACTIVE
to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, then rerun `$codex-final-review`. If those tests reveal a production defect,
report it and route through the implementation workflow rather than expanding this test-only slice silently.
No commit message or owner commit/push acceptance yet. After eventual acceptance/owner commit and phase closeout,
the Phase 10–12 milestone review remains mandatory before Phase 13.
