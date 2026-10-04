# Phase 12 Codex final acceptance

- Date: 2026-10-04
- Scope: active `phase-12-global-search`, submitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Approved preparation baseline: `44fdaa9137472b44217c159e87e5bc2b89ca54d4`.
- Outcome: **READY FOR OWNER COMMIT**. FR12-1 through FR12-6 closed; P12-1/P12-2 remain closed.
- Phase 12 is accepted, not yet owner committed/pushed or frozen. Historical reviews remain unchanged.

## Independent verification

Codex ran `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, **815 tests**, zero failures/errors/skips,
01:28 min, finished **2026-10-04T20:04:55+07:00**. Surefire reports contain 815 testcase elements: 787 retained
baseline tests, four added application architecture tests and 24 Search integration tests. PostgreSQL V1/V2
migration, Hibernate validation, schema checks and Modulith verification passed. `git diff --check` passed.
Antigravity's 19:57:40 run and earlier Codex runs are separate evidence, not substitutes for this verification.

Environment: Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith 2.1.1, Hibernate 7.4.5.Final,
Testcontainers 2.0.5, PostgreSQL 18.6 (`postgres:18.6-alpine`).

## Final finding closure

- **FR12-1:** truthful bounded K+1 lookahead, including actual 600-row terminal and 602-row nonterminal maximum-offset
  datasets. Sources remain bounded at 601; no load-all pagination.
- **FR12-2:** deliberate query/tag/limit/batch validation at public owner contracts, including nested capabilities.
- **FR12-3:** field fallback, plain-text Markdown/HTML snippets, literal identifier preservation, <=240-character
  budget, supplementary Unicode safety and stored-source preservation.
- **FR12-4:** `verifiesFr124ActualQueryPlansAndTransactionThresholdIsolation` now pins the checked-out physical
  connection using SingleConnectionDataSource and invokes the real MusicSearchService through Spring transaction
  advice with its production annotation. Actual service-query PID equals the setup connection PID. PostgreSQL
  measures Symphony/symphoni similarity at approximately 0.6363636; the fuzzy-only fixture is excluded at prior
  0.8, returned as SHORT_FUZZY through the service's local 0.3 override, and the same connection returns to 0.8
  after transaction completion. Finally cleanup restores the test baseline 0.3. This detects omission of SET LOCAL,
  unlike the earlier second-connection test. Captured actual Music/Vault SQL and index inventory checks retained.
- **FR12-5:** transparent unchanged production SQL instrumentation proves bounded bulk materialization, no per-hit
  lookup, and late qualification across candidate pages with Vault batches [50, 5]. Exact IDs, AND/collapse,
  textual-type pre-LIMIT ordering, global offsets and maximum lookahead proofs retained.
- **FR12-6:** full captured Music/required-tag Vault plans, forced planner settings, separate handwritten tags-only
  control, measured threshold proof, correct ranks/null semantics/counts and observed warnings now documented.
  Codex corrected minor evidence wording: the CsvImportParser package/deprecation attribution, small-table planner
  choice is not guaranteed, and the Note scan does not claim other owners query the notes table. Status synchronized.

## Review dimensions and limits

Business rules, deterministic ranking/deduplication, qualification before source limits, validation and snippets are
accepted. Search remains a no-table leaf using narrow owner contracts; Knowledge/Collection remain parent facades.
No reverse dependency, cross-module repository/entity leakage, feature mutation, V1 rewrite or deferred scope found.
Approved V2 adds pg_trgm and 23 expression GIN indexes only; frozen logical schema and module ownership are preserved.
Read-only sequential transactions do not promise a cross-source snapshot. SQL remains parameterized; no raw search
terms, snippets or private identifiers are logged by the added search path.

Cohesive owner implementations and direct orchestration are proportionate. Public capability interfaces/facades
protect real ownership boundaries without a speculative strategy framework or generic base service. Package-info,
placeholder removal and synchronized package-tree documentation are accepted. Prior broader production review
remains applicable; the final submission was the narrowed test/evidence slice, not a new production redesign.

Forced enable_seqscan=off proves index capability, not normal cost-based selection or production latency. The full
required-tag Vault plan uses join/primary-key access in this fixture, not the tags GIN index; the separate tags-only
control proves GIN capability. Unindexed body matching can scan owner tables. No speculative body index requested.

Observed notices: Lombok deprecated Unsafe use; existing CsvImportParser/test PostgreSQLContainer deprecations;
Mockito/ByteBuddy self-attachment/dynamic-agent and JVM sharing notices; SpringDoc enabled-endpoint warnings.
No IDE warning supplied. No IDE/Spotless/Checkstyle/SpotBugs inspection executed and no warning-free claim made.
Relevant Search, Java/Spring, architecture, persistence, testing, SOLID, reuse and pattern skills guided review.
Codex changed only review/status/evidence documentation; no production/test source edits, subagents or publishing.

## Owner commit and next gate

Commit message: `feat(search): add PostgreSQL-first global search`

Next step: owner commits/pushes the accepted slice, then gives the latest package to ChatGPT for Phase 12 closeout.
Run the mandatory Phase 10–12 `$codex-milestone-review` before Phase 13 preparation. Agents do not commit/push.
