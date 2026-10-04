# Phase 12 Codex final re-review

- Date: 2026-10-04
- Scope: `phase-12-global-search`, submitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Baseline: `44fdaa9137472b44217c159e87e5bc2b89ca54d4`; uncommitted implementation/remediation.
- Outcome: **CHANGES_REQUESTED**. FR12-1/FR12-2 closed; FR12-3–FR12-6 remain open with narrowed requirements below.
- Preparation P12-1/P12-2 remain closed. Phase 12 is not accepted/frozen.

## Independent verification

Codex ran `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, **813 tests**, zero failures/errors/skips,
01:22 min, finished **2026-10-04T17:59:22+07:00**. Surefire contains 813 testcase elements: the retained 787-test
baseline, four added application architecture tests, and 22 Search integration tests. V1 then V2 applied on PostgreSQL
18.6; Hibernate validation and the existing schema/Modulith tests passed. `git diff --check` passed.

Environment: Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith 2.1.1, Hibernate 7.4.5.Final,
Testcontainers 2.0.5, `postgres:18.6-alpine`. This independent run is distinct from Antigravity's recorded 17:31:51 run.

An ignored diagnostic, `backend/target/final-review-diagnostics/Phase12SnippetRereviewProbe.java`, invoked the compiled
Note snippet helper with the Surefire runtime classpath. Exit 0: `nearEndLength=243`, `literalUnderscore=null`,
`unpairedSurrogate=true`. The first attempt lacked dependency classes; only the successful runtime-classpath invocation
is evidence. The probe uses synthetic content, changes no production/test source, and disappears on the next clean build.

## Closed findings / accepted remediation

- **FR12-1 closed:** global sources now supply bounded K+1 lookahead (at most 601); `hasMore` uses the deduplicated
  candidate count greater than K. The new terminal, overlap and nonterminal tests pass. Source/lookup validation
  permits the single extra candidate while global offset/limit remain unchanged. The maximum-bound test executes
  offset 500/limit 100 against eleven results; it does not prove selection of a real 601st candidate (remaining FR12-5).
- **FR12-2 closed for implementation:** owner query records, nested primitive searches, document lookups and Vault
  qualification now perform the requested structural checks before DB work/early returns. Empty/null batch behavior
  remains deliberate. The added test exercises global and People validation plus some Vault cases; the outstanding
  boundary-proof omissions are recorded under FR12-5, not a new assertion that production validation is absent.
- The original unrelated-first-field snippet fallback is corrected. The indexable `%` prefilter and explicit
  `similarity >= 0.30` are present in inspected owner/Vault sources; production `SET LOCAL` is under read-only Spring
  transaction boundaries. The original scalar-only Music predicate defect is corrected in code, but FR12-4 proof is
  incomplete. New Media/tag ALBUM-before-IMAGE selection and early required-tag rejection tests are useful and pass.

## Remaining findings

Java production paths use prefix `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

### FR12-3 — Medium — Snippet bounds and Unicode are still incorrect

Location: `knowledge/note/internal/application/search/NoteSearchService.java:243–300`; the same window/cleaner is
copied into the other body-search owners.

The helper budgets ellipses before recomputing the window, then recomputes prefix/suffix flags without adjusting the
budget. For `"A".repeat(377) + "needle" + "B".repeat(117)`, query `needle`, cap 240, it returns **243** characters:
the original end touches the content boundary, but the recomputed end needs an additional suffix. UTF-16 substring
boundaries can split an emoji surrogate pair; replacing positions 261–262 with an emoji in that fixture reproduces
an unpaired surrogate. Vietnamese BMP text in the current test does not detect this. The blanket removal of `_`
also turns ordinary `invoice_2026` text into `invoice2026`, suppressing the matching excerpt.

Required: budget the actual final ellipses/window together, avoid splitting Unicode code points, and preserve literal
text while removing actual Markdown/HTML presentation syntax. Apply consistently to the affected helpers within the
approved read-only extension. Add near-end/two-ellipsis, supplementary Unicode and literal-punctuation regressions,
including a real owner result; assert original stored Markdown remains unchanged. Keep the corrected later-field fallback.

### FR12-4 — Medium — Actual query-plan and transaction-state proof is incomplete

Location: `backend/src/test/java/com/vhvkhangg/personalprivatevault/search/GlobalSearchIntegrationTest.java:845–860`,
plus `:946–960`; `docs/implementation/phase-12/test-evidence.md` index-utilization claims.

The new EXPLAIN is a hand-written Music ID-only filter, not the executed source SQL with its rank expressions,
ordering, limit/offset and bound parameters. It runs on the nearly empty fixture without representative seeded data
or ANALYZE. There is no actual Vault tag-source plan or body-scan limitation evidence. The test and its base class
have no transaction annotation: separate pooled JdbcTemplate `SET`, `SET LOCAL`, EXPLAIN and reset calls do not
establish that settings apply to the same connection/transaction. In particular `SET LOCAL` outside a transaction
does not prove the production threshold setup. The submitted claim of guaranteed Bitmap Index Scan is too strong.

Required: retain the corrected production predicates; capture/explain representative actual partial/fuzzy owner and
Vault queries with their parameters on a known transaction-bound connection, realistic synthetic rows and ANALYZE.
Record planner choices and index capability honestly, including body-OR scan limitations. Prove that the candidate
prefilter honors the explicit threshold even when the preexisting session threshold differs and does not leak state
after transaction completion. Test-only planner settings must be restored on that same connection; do not force plans
in production or add speculative body indexes. This is not a request for a latency benchmark or a full planner suite.

### FR12-5 — High — Mandatory adversarial/batch proofs still missing or ineffective

Locations: `GlobalSearchIntegrationTest.java:609–678`, `:727–843`; Phase 12 README Testing and ACTIVE Test/evidence contract.

- No query-count/batch instrumentation or recorded counts exists for Search. The handoff explicitly requires varying
  result/candidate counts and late qualification, with one tag candidate query and bounded owner bulk materialization,
  not per-hit calls/load-all. Source inspection alone is not the requested evidence.
- `verifiesP121MultipleMatchingTagsCollapsedBeforeLimiting` has eight matching tag assignments for five entries and
  global limit ten (source lookahead eleven). Every assignment fits, so global deduplication can conceal missing
  pre-limit collapse. It also never asserts the best tag similarity. Use a source limit small enough that duplicate
  tags would crowd out a later eligible entry; compare exact IDs/similarities from Vault source and global results.
- `verifiesP121MultiTagAndFilteringAndFilmCreditExclusion` passes only `Set.of(tagB)` as required tags. Matching
  `AlphaTag` supplies tag A incidentally, so it does not prove AND behavior with two explicit required tags, especially
  on feature-text hits. Keep its useful Film Credit tag-origin exclusion and add the explicit two-tag success/failure
  and required-tags Film Credit text case.
- The required cross-domain equal-score source/global membership comparison over multiple offsets is still absent.
  Add exact-ID oracles; do not rely solely on types or sorting the already-selected result.
- Boundary proof is People-heavy: no nested primitive API tests, no oversized 602-ID lookup/qualification tests,
  no invalid tags with an empty candidate set, no null-element batch tests, or meaningful valid maximum batch/real
  K=600 lookahead dataset. Add focused tests proving rejection before DB access and the accepted maximum path.
- Index inventory asserts names only, not GIN/lower-expression/column definitions despite the evidence claiming both.
  Assert the required definitions and extension. Complete FR12-3/FR12-4 regression evidence and source-preservation/
  privacy-safe failure checks without re-testing unrelated frozen mutations.

Required: fill these specific proof gaps using PostgreSQL and focused collaborator instrumentation where appropriate,
preserve all baseline coverage, and rerun full clean verify. The useful ten added tests need strengthening, not replacement
with a broad speculative test framework. Manual measured evidence may supplement tests where the handoff permits it.

### FR12-6 — Medium — Evidence/tree/status claims remain inaccurate

Locations: `docs/implementation/phase-12/test-evidence.md:28`, `:49–50`, `:142–155`, `:168–171`;
`docs/repository/repository-package-tree.md:193–203`; ACTIVE implementation/remediation summary.

Evidence still describes nonexistent `LONG_FUZZY` at 300 and `TAG_ONLY` at 200; actual accepted buckets are TAG 300
and BODY 200, with SHORT_FUZZY >=0.30. It lists six nonexistent original integration-test names and four nonexistent
architecture-test names. Blank-query-with-tags behavior is claimed even though the constructor rejects blank queries.
It still uses Person rather than People contract names, claims query/index-definition/AND proof beyond the actual
assertions, and omits required query counts and warnings/limitations. Tree documentation still omits owner/nested/Vault
search capabilities and Feed's `vault::search` dependency. Some current docs retained the earlier 803-test/open-all-six
gate while AGENTS/phase README/ACTIVE claimed complete remediation.

Required: describe actual behavior and exact executed tests, distinguish implementation assertions from proof and
implementer verification from independent review, supply the missing measured evidence, and update the authorized
package extensions/dependencies. Record warnings/limitations; do not claim forced/simplified plans guarantee runtime
plans. Codex has synchronized the current gate to this re-review below. Preserve both historical review reports;
the implementer must not declare Codex findings resolved in a Codex-authored review section before acceptance.

## Other review dimensions / limitations

- Inspected ownership, parent-facade isolation, dependency direction, bind parameters and read-only boundaries remain
  sound. No new Search-owned persistence, feature mutation, reverse Search dependency, V1 change, HTTP, projection,
  async fan-out, or deferred scope found. Narrow facades/direct orchestration fit the task; no speculative patterns
  or generic persistence framework needed. No new locking/snapshot promise is required.
- Repeated snippet business behavior explains the consistency risk above. Correction must remain coherent across
  owners; it does not authorize unrelated frozen-module refactors or a new shared-framework dependency.
- Meaningful leaf packages have package-info; the filled Search placeholder is removed. Package documentation still
  requires FR12-6. No generated diagnostic artifact is proposed for tracking.
- Observed notices: Lombok deprecated Unsafe use; existing CsvImportParser and test PostgreSQLContainer deprecations;
  Mockito/ByteBuddy dynamic-agent/self-attachment and JVM sharing notices; SpringDoc enabled-endpoint warnings.
  No owner IDE warning supplied; no IDE inspection or separate Spotless/Checkstyle/SpotBugs run. No warning-free claim.
- Graphify was targeted navigation only; source/canonical contracts governed. Search, Java/Spring, architecture,
  persistence, testing, pragmatic SOLID, reuse and pattern skills informed this review. No subagents and no tracked
  production/test changes by Codex; only review/status docs and the ignored diagnostic were written.

## Next step

Run Antigravity `/antigravity-implement-handoff` for the narrowed FR12-3–FR12-6 remediation, then return ACTIVE as
`IMPLEMENTED_AWAITING_CODEX_REVIEW` with accurate evidence and invoke `$codex-final-review`. This includes a production
snippet fix, so it is not test-only remediation. No commit message or owner commit/push acceptance yet. After eventual
acceptance/owner commit and phase closeout, Phase 10–12 milestone review is mandatory before Phase 13.
