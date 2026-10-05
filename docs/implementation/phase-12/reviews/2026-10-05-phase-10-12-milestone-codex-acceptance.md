# Phase 10–12 Codex Milestone Acceptance

- Date: 2026-10-05.
- Outcome: **MILESTONE_READY**. No blocking findings remain.
- Canonical scope/status: [`../milestone-review.md`](../milestone-review.md).
- Reviewed baseline: `a88154072afe26035accce58e1acb127ea253028`, equal to local `origin/main`; worktree initially clean.
- Review window: frozen Phase 10 Feed/ImportData, Phase 11 Finance/Journal/Personal and Phase 12 Search, including
  the owner-approved, accepted and owner-committed Search normalization maintenance.

## Finding closure

**M10-12-1 — CLOSED.** Commit `a881540` contains the accepted maintenance, its owner approval/handoff, all-15-owner
SQL audit and regressions. PostgreSQL now folds both stored fields and raw/escaped query binds; Java no longer
supplies an independently lowercased SQL parameter. The 13 BODY snippet helpers locate original-text regions
exact-first, then locale-independent/index-preserving matching, with original extents and bounded windows.

ROOT/tr-TR ASCII `id`/`ID`, ROOT identical two-character `İD`, representative owner/global/Vault tag and BODY
regressions pass. The long expanding U+0130-before-`TARGET` regression catches the old 301-versus-601 shifted-window
path; FRM10-12-1 was closed in the
[maintenance acceptance](../../maintenance/milestone-10-12-search-case-normalization/reviews/2026-10-05-final-codex-acceptance.md).
Ranks, raw literal wildcard escaping, lower-expression index alignment, fuzzy threshold/connection isolation,
qualification, ordering, source bounds and query/batch proofs remain intact. This is not a new accent/collation feature.

Historical [2026-10-04 milestone review](2026-10-04-phase-10-12-milestone-codex-review.md) and all phase/maintenance
reviews remain unchanged. The maintenance changed no ImportData/Finance/Journal/Personal/authentication source,
public Search contracts, dependencies, V1/V2 migrations, DBML or repository-tree baseline.

## Independent verification

`mvn -f backend/pom.xml -ntp clean verify`: exit 0, **BUILD SUCCESS**, **817 tests, zero failures/errors/skips**,
01:47 min, finished **2026-10-05T07:26:35+07:00**. Surefire XML independently totals 817 test cases;
`GlobalSearchIntegrationTest` has 26, zero failures/errors/skips. This is a fresh committed-baseline milestone run,
separate from the 815-test original milestone and the 817-test maintenance acceptance.

Real PostgreSQL 18.6 Testcontainers, Flyway V1/V2, Hibernate validation/schema manifest, Modulith/public/nested
boundaries, privacy, deterministic contention, rollback and caller-transaction composition tests pass.
Environment remains Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith 2.1.1, Hibernate 7.4.5.Final,
Testcontainers 2.0.5 and `postgres:18.6-alpine`. `git diff --check` passes before and after status edits.

Antigravity evidence is retained in the
[maintenance test evidence](../../maintenance/milestone-10-12-search-case-normalization/test-evidence.md): focused
26 tests (28.06 s; finished 07:11:12+07:00), full 817 tests (01:51 min; finished 07:13:35+07:00), 2026-10-05.
Independent final acceptance then passed 817 in 01:53 min at 07:18:21+07:00. No duplicate test run is implied.

## Cross-phase review disposition

- **Business/integrity:** prior Phase 10–12 assessment remains valid after the narrowly scoped committed delta.
  Feed owns Vault-backed SavedResource identity, stable hash conflict rollback and atomic parent-Knowledge conversions.
  ImportData's explicit decisions and authoritative guarded lifecycle call parent Knowledge; target validation and
  whole-job rollback remain canonical. Finance derives posted/non-deleted ledger balances, validates exact decimals
  and retains wallet/category/aggregate guards. Journal/Personal remain non-Vault and outside Search's result universe.
- **Dependencies/API/cohesion:** frozen matrix and fresh Modulith verification agree. Search remains a persistence-free
  leaf using public owner APIs and parent Knowledge/Collection facades. Feed/ImportData do not leak nested Knowledge
  repositories/entities. Public records/capability interfaces remain suitable for later explicit HTTP mapping, not
  authorization to add it now. No new service hierarchy, competing canonical rule or speculative pattern/framework.
- **Reuse/maintainability:** owned validation, JSON isolation, conflict handling and persistence stay within their
  canonical modules. Repeated owner-local snippet helpers follow one audited index-preserving policy without adding
  a cross-module dependency. No refactor is demanded merely for syntactic similarity.
- **Performance:** Finance recent-transaction children are batch-loaded; wallet listing maps scalar data, not a balance
  query per wallet. Search qualification and materialization remain page/batch/top-K bounded, including accepted
  offset prefix and K+1 lookahead. Tag collapse/qualification precedes LIMIT. Updated actual Music/Vault plans,
  600/602-row lookahead, [50,5] late qualification, query counts and pinned same-connection fuzzy threshold pass.
  Whole-job import serialization is deliberate mutation atomicity, not an unbounded read-side search path.
- **Transactions/concurrency:** ImportData acquires its job guard and refreshes before transition checks, holds it
  through target writes and final state. Finance refreshes guarded aggregates and locks multiple wallets in ascending
  order; caller-transaction composition and database uniqueness tests remain green. No broader locks or transaction
  scope introduced by maintenance.
- **Security/privacy:** inspected phase logging uses safe summaries, not raw payload/URL/hash/finance/search content
  or vendor detail. Existing JWT configuration validates external signing material and authenticates application
  requests by default; no auth weakening, network adapter, scheduler or new HTTP surface. Phase 13 still needs its
  own HTTP authorization/error/privacy review. No real secrets/private fixtures introduced.
- **Tests/reliability:** real PostgreSQL rather than H2, retained waiting/latch-based race tests, privacy/rollback
  and composition coverage. The missing locale/Unicode/window cases now have regressions and restore locale in finally;
  repository test configuration does not enable parallel execution.
- **Docs/tooling/package tree:** committed scope stayed within the approved read-only Search exception. No schema,
  dependency or package-tree baseline changes; no tracked Graphify/build cache or obsolete source copy. Search's real
  packages have descriptors. Status docs now reflect committed maintenance and this milestone acceptance. The active
  maintenance handoff is retained unchanged for ChatGPT's post-milestone archive/reset; no next handoff created or modified.
- **Warning/debt:** inherited Lombok/Unsafe, CSV/PostgreSQLContainer compiler deprecations, Mockito/ByteBuddy agent
  notices, JVM sharing and default SpringDoc notices remain nonblocking. No owner IDE warnings supplied and no
  IDE/Spotless/Checkstyle/SpotBugs inspection executed; no warning-free claim. Two filled placeholders remain at
  `backend/src/main/resources/db/migration/.gitkeep` and
  `backend/src/test/java/com/vhvkhangg/personalprivatevault/.gitkeep`: **Low**, inherited, nonblocking cleanup debt.

Accepted limitations remain unindexed BODY scans, forced index-capability checks rather than normal cost-planner/
latency guarantees, and no cross-source snapshot promise. None materially blocks the approved next-phase workflow.
No speculative optimization, new maintenance slice or frozen production change is requested.

Graphify was navigation only; canonical source, committed delta and fresh PostgreSQL evidence governed. Domain,
Search, Java/Spring, architecture, persistence/testing, security, SOLID/reuse/pattern skills informed the assessment.
No subagents, production/test-source edits, commits, pushes, tags or PR actions.

## Next step

Owner commits/pushes this milestone review/status package, then gives the latest package to ChatGPT for post-milestone
synchronization/reset and Phase 13 preparation. Only after that preparation run `$codex-pre-handoff-review`.
`MILESTONE_READY` alone does not authorize a Phase 13 handoff or implementation.
