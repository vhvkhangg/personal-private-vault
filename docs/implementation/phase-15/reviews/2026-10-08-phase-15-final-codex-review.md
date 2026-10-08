# Phase 15 — Closure-Remnant Final Codex Review

Date: 2026-10-08

Verdict: **CHANGES_REQUESTED**

Scope: the 2026-10-08 submission of the single `phase-15-backend-audit-remediation` handoff.

Blocking findings: **three Medium — FR15-10, FR15-11, FR15-12**.

## Gate and authority

The [active handoff](../../handoffs/ACTIVE.md) entered this review as
`IMPLEMENTED_AWAITING_CODEX_REVIEW`. This is a handoff-specific `$codex-final-review`, **not** a new
repository-wide `$codex-backend-audit`. The last [closure audit](2026-10-07-phase-15-closure-backend-audit.md)
closed 13 original findings and FR15-9 and retained BA15-2, BA15-9, BA15-14 and BA15-15. Its dispositions,
captured evidence and earlier final reviews remain historical and unchanged.

The [owner decisions](../owner-decisions.md) and
[ADR-0018](../../../adr/0018-phase-15-bounded-backend-remediation.md) remain the only remediation authority.
The corrections below fit those bounds: existing owning validators remain authoritative; no schema, new module
edge, named interface, generic validation/locking framework or unrelated behavior change is requested.
Any expansion still stops for `OWNER_DECISION_REQUIRED`, including BA15-13 architecture expansion and BA15-14
new total ingestion limits. There is no second handoff and no permission to commit/push.

HEAD/local origin/main remain `6a89a998c512dda27c3e494a3525bf6118ee981e`; review is against the cumulative,
uncommitted authorized remediation worktree. Codex changed only this review, the existing handoff and current
governance/status documentation, plus ignored diagnostic artifacts. Production code, tests and POM were not repaired.

## Independently verified and accepted

- `BusinessHoursService.getSchedule` refreshes the managed parent **after** `findByIdForShare` acquires the
  existing lock. The two new deterministic PostgreSQL reader tests exercise prior managed state, actual competing
  lock waits, both unknown→known and known→unknown directions, consumed Futures and coherent final reads. This
  satisfies the submitted BA15-9 reader repair without a new lock framework or dependency.
- `ImportJobService.findJobItems` computes a long offset and returns a bounded empty list above
  `Integer.MAX_VALUE` before unsupported JPA pagination. Tests cover `Integer.MAX_VALUE` page and offset
  threshold/threshold+1 with limits 50 and 100, no writes, plus the preserved ordered 100/101-item inspection and
  complete atomic decision/execution cases. The HTTP page remains an `int`; no total ingestion cap was introduced.
- Recurring-rule create/update/entry descriptions now explain the existing cardinality, sign, distinct-wallet
  and category rules, without inventing Transfer sum-zero. Both Software schemas mark `type` required without
  introducing runtime `@NotNull` solely for metadata. These BA15-15 source corrections are accepted; the remaining
  explicit enum/no-default regression requirement is FR15-11.
- Useful BA15-2 cases are retained: Location 500/64 boundaries and PUTs; snapshot 500 bounds; nullable follower
  status, defaulted follow status/source and uncapped note; optional Feed URL; free-form Personal email max320
  and blank nationality; ASCII-padded identities/currency; generated corrected lengths/null/default contracts.
  FR15-10 concerns the remaining **owner-exact** normalization, not reversal of those accepted corrections.
- FR15-1–FR15-9 retain their specific closures. Previously accepted startup/password/binary/FK/lifecycle,
  Account/Study contention, monetary width/scale/reload, full replacement and Finance description cases remain
  green. This review does not require their reimplementation.

These are final-review acceptances of named repairs, not new repository-wide BA15 closure dispositions.
A successful later final review must still be followed by `$codex-backend-audit` before owner commit.

## Findings

Java paths in this section are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.
Audit test paths are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/`.

### FR15-10 — Medium — DTO normalization still bypasses or changes owning rules

Affected compact constructors:

- Account `internal/web/dto/CreateFollowerSnapshotRequest` (line20),
  `CreateFollowerSnapshotEntryRequest` (line14) and `SetExternalAccountRelationshipRequest` (line19).
- Collection `internal/web/dto/CreateShoppingItemRequest`, `UpdateShoppingItemRequest`,
  `CreateSoftwareItemRequest`, `UpdateSoftwareItemRequest`, and Feed `internal/web/dto/ConvertToStudyRequest`
  (`currencyCode`); the Collection DTOs are **not** under nested shopping/software packages.
- Feed `internal/web/dto/CreateFeedSourceRequest` and `UpdateFeedSourceRequest` (`feedUrl`, line22).
- Location `internal/web/dto/CreateLocationRequest` and `UpdateLocationRequest` (`phone`).

Concrete behavior: `String.isBlank()` and `trim()` followed by `isEmpty()` are not equivalent. U+2003 EM SPACE
is blank to the former but is retained by `trim()`. The existing owners also differ intentionally in the order
of length checks and blank normalization:

| Input/path | Existing owning contract | Submitted HTTP DTO behavior / consequence |
| --- | --- | --- |
| Snapshot filename/display and relationship note = U+2003 | `FollowerSnapshotService.trimOrNull` (209–215) and `ExternalAccountRelationshipService.trimOrNull` (95+) retain it. | The DTO erases it to null. Snapshot copies for the same target with display U+2003 versus null become indistinguishable, bypassing the owner's conflicting-historical-copy rejection. |
| Shopping/Software/Feed→Study currency = three U+2003 characters, without price | Owning trim/empty normalization retains the code and currency-reference validation rejects the unknown value. | The DTO erases it to null, allowing an input the owner would reject. |
| Feed URL = 2,049 U+2003 characters | `FeedSourceService.validateCommon` (115–132) checks trimmed length **before** `trimUrl` blank-to-null normalization and rejects it. | The DTO first erases the URL to null, so its `@Size` and the owner no longer see the excessive length. |
| Location phone = 65 U+2003 characters | `LocationService.trimIfPresent` (251+) treats blank as null **before** the length check; valid null phone. | DTO trim-only retains 65 characters and `@Size(max=64)` rejects a valid owning input. |

An ignored, source-launch Java probe exercised the **actual compiled constructors**, owning normalization
helpers and validators (reflection), and stock Bean Validation. It confirmed:

```text
snapshot_filename / snapshot_display / relationship_note: owner length1; DTO null
shopping_currency / software_currency / feed_study_currency: owner length3; DTO null
canonical_historical_copies_distinct=true; dto_historical_copies_collapsed=true
raw_owner_currency=rejected:InvalidShoppingItemException; dto_owner_currency=accepted
location_owner_phone_null=true; dto_validation_errors=1
feed_dto_url_null=true; dto_validation_errors=0
raw_owner_feed_url=rejected:InvalidFeedSourceException
```

This is a pure diagnostic using a minimal Reference catalog stub, not a captured end-to-end HTTP or pre-fix
database trace. HTTP/persistence consequences above follow the verified controller→DTO→owning-service paths.
The probe is not included in the 1,021 JUnit test count.

Required correction: make only these boundary transformations match the existing owning policy **and check
order**. Do not change domain validators or indiscriminately replace `isBlank()` across the repository; Feed,
Location and the trim/empty owners have different accepted policies. Keep ASCII padding/null behavior and all
true bounds, currency-required-with-price rules, reference rejection and exact owner-retained historical values.

Required regressions: send raw JSON values (not already-normalized client DTO instances); cover affected
create/update routes, unknown Unicode currency without price with canonical rejection/no write, Feed oversized
Unicode blank URL rejection, Location Unicode blank phone accepted as null, and exact snapshot filename/display
and relationship-note reads. Prove that conflicting duplicate historical display copies remain rejected atomically.
Retain ordinary ASCII blank/null/padded maximum/max+1 controls. No new Unicode policy is authorized.

### FR15-11 — Medium — Explicit remnant acceptance controls are still incomplete

`WebDtoValidationAuditIntegrationTest.shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`
(2420–2520) exercises the new Shopping/Software blank/space/padded currency and identity behavior only through
**POST**. `shopId` and `softId` are extracted but no PUT uses them in that test. Existing plain 500/501 PUT tests
are useful but do not prove the separately changed Update DTO currency/normalization paths. The new positive
Feed conversion cases assert a numeric target ID, not the normalized target title/currency after an owning read.
The generated Software create/update schema checks assert required `type` and prose, but omit the handoff's
explicit **actual enum and no default** assertions.

Consequence: the specifically required update, normalized persistence and structural contract controls are not
established, and the normalization bypass in FR15-10 survives the green suite.

Required correction, within the existing test scope:

- Add Shopping and Software PUT controls for null/blank/one-space currency without price, padded valid currency,
  and currency-required-with-price rejection/no write; assert normalized persistence through existing owning
  public/HTTP reads and preserve full-replacement/null-clearing semantics.
- Test raw ASCII-padded normalized name maximum/max+1 on both create/update routes rather than only short
  padded identities; keep the already accepted plain 500/501 cases.
- Read the converted Study through an existing public/HTTP capability and assert the accepted normalized title
  and currency. Read snapshot entries to assert the historical display value, including the FR15-10 controls.
- Assert the generated `type` enum is exactly `APPLICATION`, `EXTENSION`, and has no default in both Software
  request schemas; keep `schema.required` checks and unchanged runtime status/validation semantics.

No cross-module JPA access, generic test framework, unrelated matrix expansion or production schema change is requested.

### FR15-12 — Medium — Current submission evidence describes behavior/checks that are not present

The 2026-10-08 submission in [ACTIVE](../../handoffs/ACTIVE.md) and current
[test evidence](../test-evidence.md) must be corrected against source and executed tests:

- Test evidence lines70/75/115 and the handoff claim both nullable relationship statuses default to UNKNOWN
  in the mapper. The mapper passes values through; the owner defaults **followStatus** to UNKNOWN and source
  to MANUAL, while **followerStatus remains nullable**. The actual regression correctly asserts null follower
  status. Its display name (Web DTO test line2237) also incorrectly claims plural statuses default to UNKNOWN.
- Test evidence line370 and the handoff claim `Long.MAX_VALUE` was tested as HTTP200/empty. The pagination test
  (line428+) tests `Integer.MAX_VALUE` and offset thresholds only; the controller binds an `int` page. Describe
  those actual successful checks. Do not widen the API or invent a passing Long.MAX_VALUE check to match prose.
- Test evidence lines99–102 use nonexistent `collection.shopping.internal.web.dto` /
  `collection.software.internal.web.dto` paths; both DTO pairs live in `collection.internal.web.dto`.
  The normalization regression name at line118 also differs from the actual `...CurrencyAndPaddedIdentity` method,
  and claims create **and update** coverage missing in the submitted test.
- The reader repair is under `findByIdForShare`, not `findForShareById` (test evidence line248 and handoff).
  Location phone's submitted constructor trims only; claims that it already normalizes blank to null must describe
  the actual corrected boundary/owner path after FR15-10, not the submitted code.

Consequence: current evidence cannot serve as a truthful acceptance record even though the test totals are correct.
Correct only current submission/evidence descriptions, changed symbol/test names and affected display names.
Reconcile each claim with the actual result after FR15-10/11 and record the new verification counts. Preserve
initial audit, previous final/closure reports, original captured probes/SQL and the honest distinction between
captured runtime evidence and source-derived historical hypotheses. Do not rewrite history to remove FR15-9.
This is not a request to make followers default UNKNOWN or to change pagination to `long`.

## Verification and diagnostic record

Independent command, from `backend/`:

```text
mvn -ntp -l ../phase-15-final-closure-remnants-review-2026-10-08-verify.log clean verify
```

Result: **BUILD SUCCESS**, exit0, **1,021 tests, 0 failures, 0 errors, 0 skipped**, 03:47 min;
finished 2026-10-08T08:36:34+07:00. Actual test-case nodes in 95 Surefire XML reports confirm those totals;
101 audit cases across 14 audit suites. Java25.0.2 / Maven3.9.15, Spring Boot4.1.1 / Modulith2.1.1;
PostgreSQL18.6/Testcontainers and storage/binary/HTTP/architecture checks ran in the build; architecture suites
passed 35 + 3 + 3 cases. The probe additionally
used Hibernate Validator9.1.3.Final. The 920 baseline and prior 1,012 verification records remain preserved.

The ignored diagnostic is `backend/target/final-review-probes/ContractNormalizationProbe.java`, launched with
Java25 and the current Web DTO Surefire classpath; exit0. It creates no application/database state and prints
only synthetic normalization/validation outcomes. Repository hygiene checks completed:
1,282 production Java files / 365 production packages / 99 test Java files; no missing package descriptors,
stale gitkeep files, generated/secret path candidates or missing Markdown file targets. After synchronization,
342 Markdown files / 572 file-target links were checked with zero missing targets; `git diff --check` passed.
The source/POM and all 11 historical review digests below were unchanged; HEAD/local origin/main still matched.

Graphify was navigation only: expanded tokens `relationship schedule import schema validation`, BFS depth2,
44 nodes found / 21 shown, truncated at the configured approximately700-token retrieval budget. Its graph
predates Phase15; material facts were verified in current canonical source, tests and documentation, not inferred
as fresh facts from the graph. Navigation feedback preserves that limitation.

No tracked SQL/resource/schema/diagram/package-tree/agent tooling change is part of this review; the existing
authorized stale migration `.gitkeep` removal remains the only diff in the checked frozen/resource path set.
Before review-document edits, the 1,405 source/POM path digest was
`37B82C71BD2AC596C76220390D7674171C71B03A0DD4595AADB35BCD6171C7BC`.
The 11 existing Phase15 review documents' digest was
`A435E545A2CE57B62E5D359D5DAA33B31C47DCC63CA190D0139C2B617A9F9C3A`;
the initial audit SHA256 was `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.
The new report is excluded when comparing the existing historical set.

Compiler/build diagnostics were inspected, not called warning-free: root exception-handler deprecation,
OpenAPI unchecked operations, the media wire test's Jackson2 converter deprecation-for-removal and
Lombok/ByteBuddy JVM notices and Tomcat module-opening/leak-detection warnings remain distinguishable from
these findings. No newly demonstrated warning defect
or blanket suppression is requested. SpotBugs/PMD/CPD/coverage were **not rerun in this handoff review**;
the Oct7 closure audit's 239/57/70 triage is historical, not a fresh zero-warning claim. The subsequent dedicated
closure audit remains responsible for its repository-wide verification/static gate.

## Mandatory dimension disposition

| Dimension | Review disposition |
| --- | --- |
| Business/logical correctness | FR15-10: exact normalization/check ordering; accepted reader/offset fixes retained. |
| Clean code/maintainability; reuse | Small owner-local refresh/offset repairs; no unnecessary framework. Normalization must not compete with owners. |
| Extensibility; SOLID/patterns/overengineering | Existing public dependencies and capability ownership preserved; no speculative abstraction or new edge. |
| Performance/efficiency | Impossible offsets short-circuit; bounded item windows preserved; one parent refresh under existing lock, no new unbounded path. |
| Persistence/transactions/concurrency | Real PostgreSQL reader contention passes; no schema/version rewrite or persistence leakage. Historical value/atomicity regressions required by FR15-10/11. |
| Security/privacy | Existing authentication/error/binary regressions pass; synthetic diagnostics only, no sensitive payload logging or new exposure requested. |
| Tests/evidence | Independent full build green; FR15-11 missing explicit controls and FR15-12 inaccurate claims block acceptance. |
| Tree/package hygiene | Package descriptors, placeholders, generated/secret candidates and file-target links checked; no new source tree required. |
| Static diagnostics/warnings | Executed build warnings reviewed and scope/limits disclosed above; no warning-free assertion. |
| Scope/architecture/docs | Bounded original authority retained; same handoff CHANGES_REQUESTED, current statuses synchronized, historical reports preserved. |

## Outcome

**CHANGES_REQUESTED**. Resolve only FR15-10/11/12 in the existing handoff, preserving accepted repairs/tests.
The backend audit gate remains **REMEDIATION_REQUIRED**; Phase15 is not complete/frozen. No commit message or
owner commit/push permission is issued while these blockers remain. Even after final-review acceptance, only
`BACKEND_AUDIT_READY` from the subsequent repository-wide closure audit permits owner commit.

Next step: Antigravity `/antigravity-implement-handoff` for these bounded production-boundary, regression and
current-evidence corrections, then `$codex-final-review`.
