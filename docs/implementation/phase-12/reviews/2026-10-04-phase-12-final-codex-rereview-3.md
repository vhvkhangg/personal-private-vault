# Phase 12 Codex final re-review 3

- Date: 2026-10-04
- Scope: active `phase-12-global-search`, submitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Baseline: `44fdaa9137472b44217c159e87e5bc2b89ca54d4`; uncommitted implementation/remediation.
- Outcome: **CHANGES_REQUESTED** — FR12-1–FR12-3 and FR12-5 closed; FR12-4/FR12-6 remain open as narrowed below.
- Test/evidence-only remediation; no additional production change requested. Preparation P12-1/P12-2 remain closed.

## Independent verification

Codex ran `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, **815 tests**, zero failures/errors/skips,
01:39 min, finished **2026-10-04T19:42:05+07:00**. Surefire contains 815 testcase elements: 787 retained baseline,
four added application architecture tests and 24 Search integration tests. V1/V2 migration, Hibernate validation,
schema and Modulith verification passed. `git diff --check` passed. Antigravity's 19:37:16 run is separate.

Environment: Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith 2.1.1, Hibernate 7.4.5.Final,
Testcontainers 2.0.5, PostgreSQL 18.6 (`postgres:18.6-alpine`). No new diagnostic source or tracked implementation
written by Codex in this pass.

## Accepted remediation

- FR12-1–FR12-3 remain closed. Actual 600-row terminal and 602-row nonterminal maximum-bound tests now return the
  expected 100-item slice with truthful `hasMore`.
- **FR12-5 closed:** counting NamedParameterJdbcTemplate now executes unchanged real People/Vault source behavior,
  rather than removing required tags and post-filtering a fixed prefix. It measures actual candidate/tag/qualification/
  materialization SQL and batches. One versus twenty tag hits use one bulk query with batches 1 versus 10; twenty
  text hits need no extra materialization; 55 candidates with the qualifier beyond the first 50 cause two candidate
  and two qualification queries with batches `[50, 5]`. Exact late qualifier is asserted. Maximum lookahead and
  table/column/lower-expression/opclass mappings for all 23 trigram indexes pass. Earlier AND/collapse/offset tests retained.
- The query-plan portion of FR12-4 is substantially addressed: actual Music and required-tag Vault SQL/parameters
  are captured from production service instances and explained on a dedicated connection. This is no longer a
  hand-rewritten source facsimile. The remaining issue is the real-service threshold-isolation regression below.
- FR12-6 ranking, null-set semantics, test names, implementer attribution, measured counts, package docs and most
  warnings are corrected. Remaining factual evidence corrections relate to the same threshold and plan checks.

## Remaining findings

### FR12-4 — Medium — Threshold regression changes a different connection than the proxied search

Location: `backend/src/test/java/com/vhvkhangg/personalprivatevault/search/GlobalSearchIntegrationTest.java:1320–1344`.

The test checks out `conn = dataSource.getConnection()`, sets its session threshold to 0.8, then keeps it checked out
while calling `collectionSearchOperations.search(...)`. Neither this test nor its base has an enclosing transaction,
and `conn` is not bound to Spring's transaction resource. The proxied service therefore acquires another pooled
connection. Its fuzzy hit proves eligibility on that other connection, not eligibility under the modified prior
0.8 setting. The final SHOW on the held connection merely proves an unused connection stayed at 0.8. Removing the
production SET LOCAL can still leave this test green if the service connection has the default 0.3 threshold.

Required: pin/use the **same physical connection** for prior session setup, real proxied service transaction and
post-completion observation through test transaction-aware wiring/binding. Assert connection identity (for example
`pg_backend_pid()` observed in the actual service SQL path) so a second checkout cannot silently mask the regression.
Prove the chosen fixture is fuzzy-only, has measured similarity in `[0.30, 0.80)`, is excluded at prior 0.8 without
the service override, is returned by the real service override, and the prior threshold is restored after completion.
Restore the original setting in `finally`, including assertion failure. Keep the accepted captured-plan/count tests;
no production matching/transaction change or new framework is needed.

### FR12-6 — Medium — Evidence still overstates threshold isolation and normal planner choice

Locations: `docs/implementation/phase-12/test-evidence.md:64`, `:148`, `:164–170`, `:181–185`;
ACTIVE implementation summary.

The threshold statements claim the same prior-0.8 connection was used/restored by the transaction, which the test
above does not establish. Evidence gives similarity 0.45 without recording the measured fixture value. Music/Vault
EXPLAIN occurs after `SET enable_seqscan = off`; the evidence's captured-plan section does not disclose that for those
plans, while labeling only the separate Person check as forced. The direct tag GIN check is a handwritten tags-only
predicate, not the captured full Vault candidate query; distinguish those explicitly. Node-name summaries are not
the full actual plans requested by the previous review.

Required: after correcting FR12-4, record the measured similarity and connection-identity/inside/after threshold
results. Preserve actual full Music/Vault plan text from the synthetic fixture and clearly label the test's planner
settings, forced index capability versus normal cost-based choices, and separate tags-only control/body limitation.
Keep corrected ranks/null semantics/counts/warnings; add the observed existing CsvImportParser compiler deprecation
notice. Synchronize current status to this review without rewriting historical acceptance/remediation reports.

## Other review dimensions / limitations

Inspected production ownership, parent facades, dependency direction, source ordering/qualification, SQL binding,
read-only boundaries, snippets and approved schema scope remain as previously accepted. No new Search-owned
persistence, reverse dependency, feature write, V1 rewrite or deferred scope found. No new production defect identified.
Narrow facades/direct orchestration remain appropriate; no generic framework, speculative pattern, global lock or
snapshot promise is warranted. Package-info/placeholder hygiene and package documentation improvements retained.

Observed notices: Lombok deprecated Unsafe use; existing CsvImportParser and test PostgreSQLContainer deprecations;
Mockito/ByteBuddy dynamic-agent/self-attachment and JVM sharing notices; SpringDoc enabled-endpoint warnings. No IDE
warning supplied; no IDE/Spotless/Checkstyle/SpotBugs inspection run or warning-free claim. Graphify was targeted
navigation only; canonical source governed. Relevant Search/Java/Spring/architecture/persistence/testing/SOLID/reuse/
pattern skills guided review. No subagents or production/test source edits by Codex; only review/status docs changed.

## Next step

Run Antigravity `/antigravity-test-slice` for only the narrowed FR12-4/FR12-6 test/evidence corrections, rerun focused
and full verification, then return ACTIVE as `IMPLEMENTED_AWAITING_CODEX_REVIEW` and invoke `$codex-final-review`.
No owner commit/push acceptance or commit message yet. After eventual acceptance/owner commit and phase closeout,
Phase 10–12 milestone review remains mandatory before Phase 13.
