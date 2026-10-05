# Phase 10–12 Codex milestone review

- Date: 2026-10-04
- Outcome: **CHANGES_REQUESTED** — one Medium blocking correctness finding, M10-12-1.
- Canonical scope/status: [`../milestone-review.md`](../milestone-review.md).
- Baseline: `449eaf686f5c869e2ec49d59051d2e63b82e5f83`, equal to local `origin/main`.
- Phases 10–12 are owner committed/pushed and frozen. Worktree initially contained closeout/status docs only.
- No active implementation handoff; no Phase 13 preparation or implementation authorized by this review.

## Independent evidence

`mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, **815 tests, zero failures/errors/skips**,
01:27 min, finished **2026-10-04T20:30:19+07:00**. Surefire contains 815 testcase elements. Real PostgreSQL 18.6
Testcontainers, V1/V2 migrations, Hibernate validation, schema manifest, Spring Modulith, privacy, deterministic
contention and prior composition/query-count regressions pass. This new milestone run is separate from phase acceptance.

Environment: Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith 2.1.1, Hibernate 7.4.5.Final,
Testcontainers 2.0.5, `postgres:18.6-alpine`. `git diff --check` passes. V1 migration, frozen DBML and frozen dependency
matrix have no diff from the pre-Phase-10 maintenance commit `785dd7d`; V2 contains pg_trgm plus 23 expression GIN
indexes, not new logical tables/columns.

A separate review-only diagnostic uses migrated disposable PostgreSQL, synthetic Vault-backed SavedResource/Music
rows and a tag, the **real** Feed/Music/Vault search implementations, and Spring transaction advice. It rolls back
fixtures, closes the container and restores the original JVM locale in finally. Source is local ignored build output
at `backend/target/milestone-review-diagnostics/SearchLocaleProbe.java`, not a tracked test or production modification.
The source-file Java launcher uses the runtime classpath from Search's Surefire XML. Initial sandbox classpath access
failed; the approved outside-sandbox run completed successfully. No production database or private user data used.

## Blocking finding

### M10-12-1 — Medium — Java/PostgreSQL case normalization disagrees across owner search contracts

Classification: **owner-approved maintenance implementation slice required**, not a docs-only correction.

Locations include:

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/search/FeedSearchService.java:42`;
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/collection/music/internal/application/search/MusicSearchService.java:51`;
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/search/VaultSearchService.java:106`;
- Feed snippet matching at `FeedSearchService.java:239` and the equivalent owner snippet helpers.

All 14 feature-owner SQL search implementations plus Vault tag search use `rawQuery.toLowerCase()` with the JVM
default locale, then compare/bind that value against PostgreSQL `lower(column)`. These are two different casing
implementations, not a guaranteed canonical normalization. Snippet matching repeats default-locale lowercasing.

Measured diagnostic results:

| Fixture / query | JVM locale | PostgreSQL lower result | Actual result |
| --- | --- | --- | --- |
| SavedResource title ID, Music title ID, tag ID; query ID | ROOT | id | one hit from each real service |
| Same ASCII fixtures/query | tr-TR | id | zero Feed, Music and tag hits; Java folds I to dotless U+0131 |
| Music title U+0130 + D; identical query U+0130 + D | ROOT | code points [105, 100] | zero hits; Java query lower is [105, 775, 100] |
| Feed snippet content ID marker; query id | ROOT / tr-TR | not applicable | ID marker / null |

The identical Unicode query is only two UTF-16 characters, so fuzzy search is suppressed. This is an exact literal
self-match failure on the existing PostgreSQL baseline, **even without a Turkish JVM**. It is not a request for
accent folding, broader language ranking or a new collation. Simply replacing calls with `Locale.ROOT` fixes the
ASCII/default-locale case but does **not** fix the measured Java/PostgreSQL Unicode disagreement.

Consequence: existing supported results disappear or receive the wrong match kind/rank, and body matches can lose
their snippets. Phase 13 would expose this inconsistent frozen application behavior through the new API. Current
815-test coverage lacks these casing cases; green architecture and ASCII search tests do not establish correctness.

Required maintenance boundary:

1. Make query-side and stored-field SQL folding deliberately consistent under the existing PostgreSQL semantics
   across all affected owner sources and Vault tag search. Preserve parameter binding, literal wildcard escaping,
   lower-expression index alignment, rank buckets, fuzzy >=0.30 transaction isolation, ordering and source bounds.
2. Make snippet matching locale-independent and ensure case-fold expansions cannot corrupt offsets into the
   original cleaned text. Keep the accepted plain-text, fallback, <=240-character and surrogate-safety behavior.
3. Add real PostgreSQL owner/global text and tag regressions for ASCII I under ROOT/tr-TR and exact identical
   U+0130 title/query under ROOT, including appropriate rank/kind assertions. Add body/snippet coverage and preserve
   percent/underscore/backslash escaping. Restore JVM locale in finally and avoid parallel global-locale contamination.
4. Audit the affected owner implementations for consistency, retain parent Knowledge/Collection contracts, and
   rerun focused and full verification. Keep the 815-test baseline and existing actual query-plan/count proofs.

No schema/collation rewrite, unaccent promise, broad frozen mutation refactor, common cross-module persistence
utility or speculative search framework is required. Owner approval must precede any frozen production change.
Original Phase 12 FR12-1–FR12-6 closures remain historical; this is a new milestone finding, not a status rewrite
of the final acceptance.

## Cross-phase review disposition

- **Business/integrity:** Feed dual-key ambiguity and atomic fetch timestamps, SavedResource Vault identity/conflict
  rollback, parent Knowledge conversions/provenance, ImportData explicit decisions/whole-job rollback and authoritative
  guarded transitions are consistent with Phase 10. Parser structural canonicalization and target-owned business
  validation remain distinct; decimals, JSON nulls/deep snapshots and Markdown preservation retain coverage.
- **Finance/Journal/Personal:** derived posted/non-deleted ledger balances, exact decimal validation, category/wallet
  reference guards and ascending multi-wallet locks remain coherent. Parent/child replacement, fresh-state recurring
  lifecycle, subscription uniqueness and public-service caller-transaction composition retain PostgreSQL regressions.
  Journal permits multiple entries/date; Personal active-self partial uniqueness remains database-arbitrated.
  Neither acquires Vault identity or joins Phase 12's result universe.
- **Architecture/API:** dependency direction matches the frozen matrix; Search is a no-persistence leaf, Feed/ImportData
  call parent Knowledge, and external Knowledge/Collection search callers use parent facades. Narrow capability records
  and interfaces do not leak repositories/entities. Modulith and public/nested isolation checks pass. No Phase 13
  handoff or new domain HTTP surface was introduced.
- **Reuse/SOLID/patterns:** services and adapters remain cohesive and proportionate. Separate module-local JSON
  snapshots preserve ownership; ImportData's precision-preserving JSON mapping is local, not a competing global
  mapper. Finance entry-shape validators are similar but currently consistent; no speculative abstraction requested.
  The repeated Search casing policy is a real cross-owner consistency defect addressed by M10-12-1, not justification
  for a generic shared service hierarchy.
- **Performance:** Finance list children are batched; Search qualification is page/batch-oriented, tags collapse and
  qualify before LIMIT, fan-out/lookahead is bounded at 601 per source, and materialization is bulk top-K. Offset
  materialization includes the bounded prefix by contract, not load-all/N+1. Import execution intentionally processes
  the scoped whole job under its row guard; ingestion's per-item key resolution is mutation work, not a new read-side
  N+1. No extra index/cache/global lock requested without measured need. Forced trigram plans prove capability only;
  unindexed body scans and no cross-source snapshot promise remain disclosed limitations.
- **Security/privacy:** added phases do not expand authentication or introduce network adapters/schedulers. Existing
  stateless JWT filter, external signing-secret validation and authenticated-by-default policy remain; Phase 13 must
  perform its own HTTP security review. Search adds no raw-query/result logging; parser/conflict paths use safe summaries,
  and existing constraint-logging privacy regressions pass. No polymorphic JSON/YAML construction or sensitive exception
  logging was found in the inspected paths. No real secrets or personal fixtures were added.
- **Tests/reliability:** contention tests use latches and observed PostgreSQL waiting rather than timing-only overlap;
  existing stale-context, unique-index, rollback, bounded-query and same-connection fuzzy-threshold proofs remain green.
  Locale/Unicode normalization is the specific missing regression identified above.
- **Docs/tooling/tree:** frozen phase/module instructions still require scoped handoff authority; rules/hooks do not
  authorize reopening a frozen phase on a milestone finding. Search package descriptors/tree remain coherent. Review
  status is synchronized without creating maintenance/Phase 13 scope. Root README's stale owner-commit gate was corrected.
  Two old tracked filled placeholders remain in `backend/src/main/resources/db/migration/.gitkeep` and
  `backend/src/test/java/com/vhvkhangg/personalprivatevault/.gitkeep`; Low, nonblocking inherited hygiene debt, not a
  reason to reopen implementation. No tracked Graphify/build cache or obsolete root VaultJsonFormatMapper found.
- **Warning debt:** observed Lombok Unsafe, existing Commons CSV/PostgreSQLContainer deprecations, Mockito/ByteBuddy
  attachment, JVM sharing and default-enabled SpringDoc notices remain nonblocking. Diagnostic source also reports
  deprecated TransactionInterceptor construction. No owner IDE warnings supplied, no IDE/Spotless/Checkstyle/SpotBugs
  inspection run, and no warning-free claim.

Graphify was navigation only; canonical source and PostgreSQL evidence governed. Milestone, Feed/ImportData,
Finance/Journal/Personal, Search, Java/Spring, architecture, persistence, testing, security, SOLID, reuse and pattern
skills informed this review. No subagents, production/test-source changes, commits, pushes, tags or PR actions.

## Next step

Owner approves the narrow Search normalization/snippet maintenance scope for M10-12-1, then gives the latest package
and this finding to ChatGPT to prepare that maintenance. Follow maintenance handoff → Antigravity implementation →
Codex final review → owner commit/push → milestone re-review. Phase 13 remains blocked. No commit message or
implementation handoff is provided while approval and remediation are outstanding.
