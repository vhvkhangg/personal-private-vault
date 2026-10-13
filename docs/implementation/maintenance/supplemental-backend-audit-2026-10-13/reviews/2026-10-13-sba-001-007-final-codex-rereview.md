# SBA-001–007 maintenance — Codex final re-review, 2026-10-13

**Status: CHANGES_REQUESTED — test/evidence-only closeout.** FR-SBA-1 and FR-SBA-2 are CLOSED.
FR-SBA-3 is substantially repaired but still needs the bounded proof/fixture corrections below; FR-SBA-4 remains
OPEN. No further production change is requested. Independent clean verification passed **1,152 tests / 0 failures /
0 errors / 0 skips**, and the previously failing numeric domain/HTTP probe now passes.

## Scope and baseline

Reviewed [ACTIVE](../../../handoffs/ACTIVE.md), resubmitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`, against the
[approved seven-finding scope](../README.md), [owner approval](../owner-approval.md),
[first final review](2026-10-13-sba-001-007-final-codex-review.md) and [current evidence](../test-evidence.md).
Branch `main`; HEAD remains `a39d97241c98930a80fd5482be29326d7ed66e5c`.
Actual implementation inventory is **12 production files and 14 test files**, including one new production helper
and six new test files. Tool-approval changes in `.codex/config.toml` remain separate.

Applied the same final-review/shared 29-dimension and relevant Java/Spring, SOLID, reuse, pattern, architecture,
persistence, testing, authentication and domain rules as the first review. Graphify navigation ran first for
guard/concurrency symbols: 7,217 nodes; the 700-token result was truncated and verified in canonical source.
No production/test implementation, commit, push, tag or PR was performed by Codex. The previous review is preserved;
its SHA-256 at re-review was `41b5f92c445c70d29cb6dd6991a24df10d879c15d1c366c12720063f5379614c`.

## Review finding dispositions

### FR-SBA-1 / Medium — CLOSED

`people/internal/application/PersonService.java:219–234` now bounds raw integer digits with widened arithmetic
before normalization, and converts any normalization ArithmeticException into the existing InvalidPersonException.
No large decimal-string/power expansion, rounding, schema widening or invented measurement limit was added.

Checked-in Person validation, PostgreSQL integration and HTTP tests now exercise height/weight × create/update for
`1E+2147483647`, `100E+2147483647` and `1000E+2147483647`, with non-interaction/no-write assertions and unchanged
measurements/name. Existing ordinary width/scale/scientific-notation controls pass. Repeating the exact disposable
probe from the first review yields InvalidPersonException and **422/PERSON_INVALID** for all 16 HTTP combinations
(four extreme tokens including `10E+2147483647`, both fields and both methods); zero persistence/cross-owner calls.
The probe is standalone adapter/domain proof, complemented by the full suite's actual PostgreSQL/web tests.

### FR-SBA-2 / Medium — CLOSED

Vault services now require VaultEntryMutationGuard in every constructor and have no unlocked fallback.
Settings and PIN services require EntityManager and refresh unconditionally after obtaining an existing-row lock.
Clock defaults and exposed capability interfaces remain unchanged. Fixed-clock Vault fixtures supply the guard,
and Settings mock fixtures supply EntityManager and stub the actual locked lookup.

The Vault helper now calls `VaultEntryRepository.findByIdForUpdate` and refreshes under PESSIMISTIC_WRITE;
the formerly unused lookup therefore has a concrete canonical caller. The nullable-save fallback is removed.
The helper centralizes a real shared invariant inside Vault without a generic hierarchy or cross-owner abstraction.

### FR-SBA-3 / Medium — PARTIALLY CLOSED; remaining test-only requirements

The substantial original gaps are repaired:

- Vault covers all six metadata operations with actual two-thread contention in both orders, seeded removal
  cases and fresh child assertions. Cached trashed→committed restore→trash is covered; lifecycle contender views
  are retained and asserted. Repeated transitions, same-transaction composition and outer-commit lock lifetime pass.
- Settings asserts all six fields after both disjoint orders and both backup/auto-lock orders.
- Feed now uses FOR NO KEY UPDATE in its disable-first fixture, allowing item FK key-share checks while blocking
  the final owning source guard. It includes cached enabled→committed disable, true ingestion-first contention,
  and exact Timestamp.toInstant comparisons for rollback of both fetch/schedule timestamps.
- PIN captures exact username/email/password-hash/created-at baselines, fresh winning PIN hashes and both winner
  orders; negative format/current-PIN/bootstrap/anonymous controls pass.
- Import adds persisted empty-block/thematic-break and positive nested/unknown metadata cases; negative trailing
  JSON object/garbage HTTP cases assert 422, safe messages, blocked transitions and no target writes.
- Person includes the repaired numeric cases across both fields and operations with database and transport proof.

**Remaining concrete gaps:**

1. `feed/FeedIngestionConcurrencyTest.java:335–348` checks ingestion-first fetch/schedule timestamps only for
   non-null values. This cannot prove that the later disable preserved the successful bookkeeping. Assert exact
   normalized fetch time and scheduled next time (or compare a captured committed baseline), plus the retained item
   values, in that order. The production code has no observed timestamp defect; this is the required proof gap.
2. Lock observers in the four owner concurrency files now bind to a holder PID, improving the old global test,
   but still accept any ungranted lock blocked by that holder. Complete the original fixture-specific obligation:
   capture the intended waiter's PID and verify its expected owning-row/statement wait and blocker before release.
   Preserve Feed's final-gate isolation; no production callbacks, mocks as lock proof or timing-based ordering.
3. Holder timeout awaits now assert success and finally blocks release latches, but Vault/Settings/PIN interrupted
   holder waits often only re-interrupt and return; executors are shutdown without awaited termination. Fail/roll
   back interrupted holders and confirm bounded termination in cleanup. In `PrivatePinConcurrencyTest.java:347–413`,
   several negative tests install main-thread authentication without clearing it afterward. Add reliable fixture
   cleanup for that thread as well as worker threads so authentication cannot survive into another test.
4. The handoff's trailing-document **log** sentinel obligation is not present in the checked-in import tests.
   `ImportDataWebIntegrationTest.java:95–230` checks the safe HTTP message, but does not capture/assert log output.
   Add narrowly scoped synthetic sentinels and assert they do not appear in application logs or the error body.
   Preserve the existing generic job-ID-only logging; no actual payload leak was observed in source or the build.

Paths here are under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.
These are test/fixture/evidence changes only; do not alter successful production semantics to satisfy them.

### FR-SBA-4 / Medium — OPEN; before/after receipts and semantic references remain inaccurate

Physical bigint/smallint identities and original SBA-007 Low severity are improved, and unsupported comparative
SpotBugs/PMD/Checkstyle claims are removed. However, `test-evidence.md:57–65` still labels historical defect
narratives as “Failing Before / Fixed After Receipt” without a baseline commit, executed before command, expected
failure result/exit or named-test output per SBA ID. No such receipt is linked elsewhere in the file. Inspection
of available ignored logs found only the old supplemental baseline file inventory, not checked-in regression
before/after receipts. This is **not proof those tests were never run**; the required evidence is not supplied.

Material source/reference contradictions also remain:

- Row SBA-001 says restore throws “Cannot restore an active entry”; actual restore is idempotent and successfully
  restores after committed trash. It also gives an invented metadata exception message and nonexistent methods
  such as `trashAndMetadataContentionBothOrders` / `cachedActiveEntryCommittedTrashBlocksRestore`.
  Actual methods include `parameterizeAllSixMetadataOperationsAgainstTrash`,
  `cachedActiveCommittedTrashRestoreReturnsActive` and `realTrashVsRestoreContentionBothOrders`.
- Settings §1 names nonexistent `UpdateAppSettingsCommand`; the actual contract is `UpdateSettingsCommand`.
  Several Settings/PIN/Feed/parser method references are still nonexistent; for example JSON
  `auditFixtureTrailingObjectRejected` should refer to actual `rejectsArrayFollowedBySecondObject`.
- Feed §1/§3 claims a refresh overload with PESSIMISTIC_WRITE; the actual call is
  `entityManager.refresh(sourceToUpdate)` after the repository has acquired the lock. Its rollback example
  retains prior non-null fetch/schedule times, rather than always keeping them null.
- Counts are inconsistent: inventory headings say 11 production/13 tests but enumerate 12/14.
  Independent XML counts are Vault **13**, Settings **7**, Feed **5**, PIN **12**, Markdown **17**, JSON **27**.
  Fix both the evidence and ACTIVE's current implementation summary where these claims are presented as current.
- Commands wrap `$env:JAVA_HOME` inside a double-quoted child PowerShell invocation, which expands in a PowerShell
  parent; the environment/container versions and actual before/after invocation provenance remain unspecified.

**Required closure:** record at least one actual checked-in regression failing on approved baseline
`a39d97241c98930a80fd5482be29326d7ed66e5c` and passing on this implementation **for each SBA-001–007**.
Use isolated temporary baseline outputs/checkouts and unchanged regression sources, never reset the owner's tree.
Do not send the pre-fix extreme Person value to JDBC: use the existing isolated validator regression for its red
receipt. Record exact selectors, environment, exit/result and relevant final assertion; expected baseline test
failure is evidence, not a green build. Source/test names and public behavior must be mechanically checked against
current files/results. Do not rename working tests or change public behavior to match erroneous documentation.

These are substantive evidence/contract inaccuracies; minor count/wording corrections alone would not block,
but the missing baseline proof and false lifecycle semantics prevent closing FR-SBA-4.

## SBA-001–007 and Phase 17 assessment

| Finding | Current functional proof | Final closure |
| --- | --- | --- |
| SBA-001 | Canonical physical Vault row lock/refresh, stale contexts, all-six metadata contention, lifecycle orders/views/composition and outer-commit lifetime pass. | Pending the remaining test/evidence closeout above. |
| SBA-002 | Locked authoritative partial merge, all-field assertions, both disjoint and backup/auto-lock orders, defaults and invalid merge pass. | Pending closeout. |
| SBA-003 | Recheck after final refresh; cached disable and update+insert rollback pass; true contention in both orders. | Exact ingestion-first bookkeeping proof still required. |
| SBA-004 | Both delimiter counterexamples, LF/CRLF/EOF, exact raw text, persisted empty/nested/unknown metadata and decimal controls pass. | Baseline/reference evidence still required. |
| SBA-005 | Mandatory locked refresh before current-PIN check, stale and both winner orders, exact credential preservation and auth denials pass. | Fixture cleanup/lock/evidence closeout required. |
| SBA-006 | Complete-document parser and negative HTTP/transition/no-write cases pass; whitespace/decimal/field controls survive. | Log sentinel and baseline/reference evidence required. |
| SBA-007 | Widened digit arithmetic and bounded normalization reject all reviewed extremes before writes; original HTTP 500 probe now returns 422. | Baseline/reference evidence still required. |

No new production correctness defect was established in this re-review. The existing `vault_entries.id` lock,
same caller transaction, targeted freshness and parent-before-child ownership remain compatible with accepted
P17-3 §1A. No source-guard public API, Fiction/Film participation, RAG grant/epoch/worker/role/NTFS implementation
or new module edge exists here. Phase 17 preparation remains accepted; its runtime tests and R2/R7 source-commit
fences remain independently gated. No maintenance commit or Phase 17 handoff is approved yet.

## Independent checks

- From `backend/`:
  `mvn -ntp -l ../logs/sba-final-rereview-2026-10-13-verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report`.
  Exit 0, BUILD SUCCESS, **1,152/0/0/0**, 05:48, finished 2026-10-13 19:59:22 +07; JaCoCo 811 classes.
  XML testcase enumeration independently corroborated 1,152. The full suite includes the requested focused
  classes, Modulith, Flyway/PostgreSQL, MinIO, authentication/HTTP/OpenAPI and existing audit/rollback tests.
  Submitted focused 417-test result is reported as Antigravity evidence; no duplicate focused run was needed.
- `mvn -version`: Maven 3.9.15, Oracle OpenJDK 25.0.2, Windows 11 amd64, UTF-8.
  Existing fixtures use PostgreSQL `18.6-alpine` and MinIO `RELEASE.2024-11-07T00-52-28Z`.
- Root command `& 'C:/Program Files/Java/jdk-25.0.2/bin/java.exe' '@logs/sba-final-review-java-args.txt'`
  repeated the first review's unchanged disposable `logs/NumericBoundaryReview.java`: exit 0, four domain
  rejections, 16 HTTP 422 results, zero persistence/cross-owner calls. It does not replace checked-in tests.
- IntelliJ `lint_files` requested all 12 changed production files: zero errors, **10 warnings / 2 weak warnings**,
  no reported timeout/incomplete flag. Nullable-save/visibility warnings identified in the first review are gone.
  Remaining diagnostics are unused import/variable/methods, duplicate argument checks, validation-only ZoneId,
  optional functional expression, explicit type argument and redundant cast. No blanket suppression or unrelated
  warning cleanup requested.
- Build warnings remain Lombok Unsafe, deprecated test-only MappingJackson2HttpMessageConverter and dynamic
  Byte Buddy agent loading. No executed comparative SpotBugs/PMD/Checkstyle or advisory/license/SBOM result is claimed.
- Actual recursive inventory: **336 production Java files / 107 packages / 37 test Java files** across the six
  affected owners; no package/path mismatch, missing production package descriptor, stale .gitkeep or cross-owner
  internal import. Capability commands/views/enums/exceptions/internal web grouping and the canonical root HTTP
  family fit the package-tree rules and ADR-0016. Full inventory is ignored at
  `logs/sba-final-rereview-package-inventory.txt`; unchanged methods were not re-audited wholesale.
- Diff checks show no ADR/architecture/DBML/package-tree/POM/migration/Phase 15 historical/Phase 17 preparation
  changes. Production/test source remained untouched by this review. Post-recording checks passed: all 20 active/
  review local targets resolve, zero trailing whitespace, exactly ledger dimensions 1–29, previous review hash
  unchanged and git diff --check exits 0. Raw logs and diagnostic artifacts remain ignored.

## Complete 29-dimension ledger

PASS is scoped evidence, not a universal certification. No runtime risk is waived by missing measurements.

| # | Dimension | Status | Evidence / limit |
| ---: | --- | --- | --- |
| 1 | Owner intent / permitted behavior / non-goals | PASS | Same approved seven-ID maintenance, 12 production/14 test targets; no feature expansion. |
| 2 | Requirement traceability | FINDING | FR-SBA-3/4: remaining exact proof and baseline receipts; all seven IDs expressly assessed above. |
| 3 | ADR / schema / diagrams / workflow consistency | PASS | Frozen scope diffs empty; no RAG implementation or historical reopening inferred. |
| 4 | Ownership / NamedInterfaces / transaction data flow | PASS | Full descriptors/import inventory and independent Modulith verification; owning repositories remain internal. |
| 5 | Actual repository/package/folder structure | PASS | Recursive 336/107/37 inspection, correct capability grouping/descriptors and no stale placeholders. |
| 6 | Shared HTTP / exception placement | PASS | One unchanged root envelope and owner-local DTO/mapper/advice under ADR-0016. |
| 7 | SOLID / cohesion / substitutability / dependency direction | PASS | Mandatory guard/refresh collaborators now enforce one mutation policy in all constructors; owner-local responsibilities. |
| 8 | DRY / KISS / YAGNI / invariant consistency | PASS | Canonical Vault helper has actual reuse; dead fallback/null-save policy removed; no speculative framework. |
| 9 | Patterns / anti-patterns | PASS | Existing Repository/Facade/Adapter roles retained, minimal concrete invariant collaborator. |
| 10 | Domain correctness / states / idempotency | PASS | Stale-state fixes, transitions/returned views/composition and boundaries pass; no new production defect observed. |
| 11 | REST / wire / OpenAPI compatibility | PASS | Full web/contract tests and repaired domain/HTTP probe; DTO/advice/public capability contracts unchanged. |
| 12 | Input / bounds / hostile format / overflow | PASS | Numeric normalization repaired; full parser/numeric controls and no-write rejection pass. |
| 13 | Authentication / authorization / privacy | PASS | PIN stale/winner/auth-denial and exact credential tests pass; production security unchanged. Fixture cleanup is dimension 22. |
| 14 | Secrets / logs / retention / egress | PASS | Changed-source review and generic errors/logs establish no observed payload/credential leak; no new retention/egress path. |
| 15 | Atomicity / lock identity/order/lifetime / concurrency | FINDING | Correct physical guards and actual contention pass; remaining fixture-specific waiter/statement and interruption proof in FR-SBA-3. |
| 16 | Failure / recovery / rollback | FINDING | Rollback tests pass; exact ingestion-first bookkeeping preservation and bounded worker cleanup remain required. |
| 17 | PostgreSQL / Flyway / JPA / physical schema | PASS | Migrated PostgreSQL full verification, unchanged mappings/schema, bigint/smallint keys confirmed. |
| 18 | Complexity / resources / contention scope | PASS | Constant indexed guards, Feed final gate retained, linear delimiter scan and bounded numeric arithmetic; no latency/memory guarantee. |
| 19 | Retrieval / index / recommendation quality | N/A | No search/retrieval/recommender or RAG corpus changes. |
| 20 | Document / multilingual fidelity | PASS | Exact Markdown, empty/thematic/nested/unknown metadata, provenance and decimals covered; OCR/NTFS/other extraction outside this slice. |
| 21 | Adversarial / security tests | FINDING | Hostile numeric/import/credential cases pass; mandated checked-in import log sentinel assertions still missing. |
| 22 | Test quality / determinism / risk coverage | FINDING | 1,152 green cases; FR-SBA-3 cleanup/bookkeeping/observability and FR-SBA-4 baseline proof remain. |
| 23 | Representative performance measurements | NOT_VERIFIED | No p50/p95 or production/RAG-scale resource benchmark; outside maintenance. |
| 24 | Model evaluation / calibration / holdout | N/A | No ML/OCR/judge implementation changed or introduced. |
| 25 | Dependencies / licenses / vulnerabilities / SBOM | NOT_VERIFIED | Pins/POM preserved and build compatible; no fresh comprehensive security/license/SBOM assessment. |
| 26 | Build / static tooling / architecture verification | PASS | Independent clean verify/coverage and 12-file IDE inspection; warnings explicitly triaged, no analyzer-clean claim. |
| 27 | Logging / observability | PASS | Generic source-free messages/logs preserved; no observed leak. Required sentinel test coverage is dimension 21. |
| 28 | Documentation / governance / reproducibility | FINDING | FR-SBA-4 nonexistent references, false restore semantics and missing executed baseline receipts. |
| 29 | Hygiene / delivery / frozen evidence | PASS | Scoped patch, ignored diagnostics, previous review preserved, no unapproved frozen artifact changes; final link/diff check below. |

## Required next action — test/evidence only

Run Antigravity `/antigravity-test-slice` against this same active handoff for the remaining **FR-SBA-3/4**
requirements. Change only the permitted owner tests, current evidence and ACTIVE's current result/status;
do not change production behavior, ADRs, dependencies, schema, historical reviews or Phase 17 preparation.
The test-only skill explicitly allows adding missing coverage named by this review.

Use focused selectors while producing the per-ID baseline red/fixed green receipts, then run the handoff's
focused matrix and clean full verification after the test changes. If a production defect is exposed, report it
and return to the implementation workflow under authorized scope rather than silently broadening this test slice.
Return `IMPLEMENTED_AWAITING_CODEX_REVIEW`, then rerun `$codex-final-review`.
No commit message; do not commit/push or start Phase 17 implementation.
