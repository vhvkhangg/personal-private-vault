# Search Case-Normalization Maintenance — Codex Final Review

Date: 2026-10-04. Outcome: **CHANGES_REQUESTED** — test/evidence-only remediation.

Scope: active handoff `maintenance-milestone-10-12-search-case-normalization`, submitted as
`IMPLEMENTED_AWAITING_CODEX_REVIEW`, against committed Phase 12 baseline `449eaf686f5c869e2ec49d59051d2e63b82e5f83`.
Owner approval and the narrow frozen-module exception remain valid. No production correction is requested.

## Blocking finding

### FRM10-12-1 — Medium: required casing-expansion/window regression is not exercised

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/search/GlobalSearchIntegrationTest.java:1824`
and its assertions at line 1871; corresponding claims in active handoff and maintenance `test-evidence.md`.

The purported length-changing fixture uses U+1E9E (`ẞ`). On the actual Java 25 runtime its ROOT lowercase is `ß`:
one UTF-16 code unit becomes one, not two. Its short body is also below 240 characters, so `extractSnippet` returns
the entire cleaned body before constructing a window. Checking that this result contains `ID` and `ẞ` cannot
detect applying a folded-copy offset to an original-text window. The separate short `İD` self-match tests prove
anchoring but do not exercise expansion before a later windowed match either.

Consequence: the handoff's explicit length-changing-before-match regression is absent, despite evidence claiming it
is proved. Reintroducing the original shifted-window algorithm would not be caught by this particular fixture.

Required correction, **tests/evidence only**:

- Add/replace a real BODY-only fixture under ROOT with genuinely expanding U+0130 before a later case-insensitive
  match in a long body. For example `"\u0130".repeat(300) + " TARGET " + "x".repeat(400)`, query `"target"`,
  and a primary name that does not match. Original match offset is 301; Java ROOT-folded offset is 601.
- Through the real PostgreSQL owner/global stack assert BODY, a non-null snippet containing original `TARGET`,
  <=240 characters and the expected original-text window/ellipsis behavior. Keep locale restoration and existing
  locale, Unicode, boundary, cleanup, fallback and surrogate regressions. The test must fail for the old offset path.
- Correct implementation/evidence wording: all 15 services have SQL folding changes; 13 have snippet helpers.
  Music and Vault have no BODY snippet helper. Actual bind names are `rawQuery`, `prefixPattern`, `substringPattern`;
  do not claim nonexistent `fuzzyPattern` / `rawTagQuery` binds. Do not call U+1E9E lowercasing length-changing.
- Run focused Search tests, full `mvn -f backend/pom.xml -ntp clean verify`, and `git diff --check`; update evidence
  with actual counts/results, then submit as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

Do not change the accepted production approach, schema, dependencies, public contracts or phase scope for this finding.

## Independent verification

`mvn -f backend/pom.xml -ntp clean verify`: **BUILD SUCCESS**, 817 tests, zero failures/errors/skips;
01:27 min, finished `2026-10-04T23:00:11+07:00`. Surefire XML independently totals 817 test cases, including all
26 Search tests. Modulith architecture, V1/V2 Flyway migration, PostgreSQL persistence and Hibernate validation pass.
`git diff --check`: exit 0.

A review-only Java source-file probe invoked the actual private People snippet helper using the Surefire runtime
classpath. Synthetic output: U+1E9E length `1 -> 1`, U+0130 `1 -> 2`; original/folded indices `301 / 601`;
current helper contains `TARGET`, length 240. The baseline's shifted end-window branch selects a 240-character tail
without `TARGET`. This confirms the current implementation handles the proposed regression; it does not substitute
for a committed test. Probe lives only under ignored `backend/target/final-review-diagnostics/` and is clean-build disposable.

## Other mandatory review dimensions

- Correctness: all 15 SQL text blocks compared with HEAD differ only by approved PostgreSQL-side parameter folding
  and formatting. No Java `toLowerCase` remains in these queries/helpers; every query/pattern reference is folded in SQL.
  Raw literal escaping, ranks, similarity, ordering and bounds remain intact. The ROOT/tr-TR ASCII and ROOT identical
  two-character Unicode tests pass through Music, Feed, Note, People, Vault tags and global Search.
- Maintainability/SOLID/patterns: small owner-local original-text helpers, no speculative abstraction or new shared
  dependency. All 13 helpers use exact-first/index-preserving `regionMatches`; original query extent budgets the window.
- Performance: existing actual-query Music/Vault plan, forced-index/control disclosures, pinned-connection fuzzy
  threshold, 600/602 lookahead, [50,5] late qualification and query/batch-count tests pass unchanged against updated SQL.
  No new persistence loops, N+1 or unbounded reads. Unindexed BODY scans remain the accepted limitation; no latency guarantee.
- Persistence/transactions/concurrency: read-only boundaries, transaction-local fuzzy threshold and owner persistence
  stay unchanged. Locale is restored in `finally`; the repository's test configuration does not enable JUnit parallel execution.
- Security/privacy: no new logging, sensitive diagnostics, HTTP surface or mutations; diagnostics use synthetic content only.
- Scope/architecture/tree: only the 15 allowed production Search services and one test source changed. No POM, migration,
  DBML, architecture/dependency or repository-tree changes; no new production packages/public DTOs. Existing unrelated
  placeholder hygiene remains out of this maintenance scope.
- Diagnostics: inherited Lombok/Unsafe, compiler deprecation (CSV and test support), Mockito/ByteBuddy agent notices
  and SpringDoc INFO are not a new production defect. No IDE inspection was run; no warning-free claim is made.
- Docs/evidence: build results are credible, but the false expansion/snippet-count/bind-name claims need the corrections above.

## Next gate

Antigravity `/antigravity-test-slice`, limited to FRM10-12-1 tests and directly related evidence/status documentation;
then `$codex-final-review`. No commit message or owner commit approval yet. M10-12-1 milestone closure remains pending;
after maintenance acceptance and owner commit/push, rerun `$codex-milestone-review`. Phase 13 remains blocked.
