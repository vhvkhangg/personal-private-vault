# SBA-001–007 maintenance — Codex final acceptance, 2026-10-13

**Status: READY FOR OWNER COMMIT.** No blocking findings remain. All seven SBA findings and
FR-SBA-1 through FR-SBA-4 are **CLOSED**. Independent clean verification passed
**1,152 tests / 0 failures / 0 errors / 0 skips**. The same checked-in regression sources fail
for defect-specific reasons on the approved baseline and pass on the implementation.

## Scope and working-tree context

Reviewed [ACTIVE](../../../handoffs/ACTIVE.md), submitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`,
against the [approved scope](../README.md), [owner approval](../owner-approval.md),
[first review](2026-10-13-sba-001-007-final-codex-review.md),
[re-review](2026-10-13-sba-001-007-final-codex-rereview.md), and corrected [test evidence](../test-evidence.md).
Branch `main`; HEAD and origin/main remain `a39d97241c98930a80fd5482be29326d7ed66e5c`.
The maintenance changes comprise **12 production files and 14 test files**, current handoff/evidence,
and maintenance review reports. Tool-generated `.codex/config.toml` approvals are separate from this slice.

Applied `codex-final-review`, the shared 29-dimension rubric, and the previously applied Java/Spring,
SOLID, reuse, pattern, modular architecture, persistence, backend testing, authentication, Feed/import and
People rules. Graphify navigation returned a truncated 700-token result from 7,217 nodes; important facts
were verified in source. Codex changed review/status/evidence documentation only. No production or checked-in
test implementation, commit, push, tag or PR was performed.

Both earlier reports are preserved byte-for-byte. Their SHA-256 values are respectively
`41b5f92c445c70d29cb6dd6991a24df10d879c15d1c366c12720063f5379614c` and
`4371e3e4e59725867cbe266b4f91bd9f1f3057b5e97a350b6809923edb30184e`.

## Previous review finding closure

| Finding | Disposition and verified correction |
| --- | --- |
| **FR-SBA-1 / Medium** | **CLOSED.** Person validation checks raw integer digits with widened arithmetic before normalization and converts normalization overflow to InvalidPersonException. Height/weight × create/update unit, PostgreSQL and HTTP matrices include `1E+2147483647`, `100E+2147483647` and `1000E+2147483647`. Rejection precedes writes; ordinary numeric controls pass. |
| **FR-SBA-2 / Medium** | **CLOSED.** Every Vault service constructor requires the canonical guard; Settings/PIN require EntityManager. No unlocked/null fallback remains. The Vault locked lookup has a real caller and fixtures use mandatory collaborators. |
| **FR-SBA-3 / Medium** | **CLOSED.** All four concurrency observers bind intended waiter PID, holder PID, owning-table statement and PostgreSQL blocking relation before release. Feed asserts exact normalized successful fetch/schedule times before retained item values. Holder timeouts/interruption fail the transaction; finally blocks release latches and await bounded executor termination. PIN clears worker and main-thread security contexts. Import web tests capture output and reject synthetic payload markers in error bodies and application logs. |
| **FR-SBA-4 / Medium** | **CLOSED by direct independent verification and documentation correction.** Codex ran byte-identical checked-in tests on an isolated Git baseline; all seven have actual failing-before/green-after proof. Corrected unsupported exception text, nested-test selectors, counts, Feed rollback descriptions and command quoting in current evidence. Submitted before-run narratives were not treated as executed receipts. |

Relevant current test locations under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`:

- `vault/VaultMutationConcurrencyTest.java:68`: waiter/statement observer; lifecycle/all-six metadata,
  returned views, stale contexts, composition and outer-transaction commit lifetime.
- `settings/AppSettingsConcurrencyTest.java:75`: intended waiter and both disjoint/backup/auto-lock orders,
  six-column fresh assertions, defaults and invalid merge.
- `feed/FeedIngestionConcurrencyTest.java:83`: final-gate observer; `:360` exact ingestion-first fetch/schedule
  retention followed by retained item values. Disable-first uses FOR NO KEY UPDATE to avoid mistaking an item FK wait.
- `authentication/PrivatePinConcurrencyTest.java:68`: before/after security cleanup; `:102` waiter observer,
  exact credential snapshots and both winner orders.
- `importdata/ImportDataWebIntegrationTest.java:99` and `:198`: synthetic trailing-object/garbage markers,
  422/safe response, log capture, blocked transitions and no target writes.

## All seven SBA findings

Production paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

| Finding | Implemented behavior and acceptance proof | Disposition |
| --- | --- | --- |
| **SBA-001 / Medium** | Vault-owned `VaultEntryMutationGuard` locks the existing physical `vault_entries.id` row and refreshes managed state. Lifecycle and six metadata mutations use it before decisions/child writes. `VaultMutationConcurrencyTest` passes 13 cases: cached-state counterexamples, lifecycle and metadata contention in both orders, fresh rows/views, timestamps/idempotency, composition and lock held beyond method return until outer commit. | **CLOSED — first priority.** |
| **SBA-002 / Medium** | `settings/internal/application/configuration/AppSettingsService.initializeOrUpdate(UpdateSettingsCommand)` locks/refreshes the existing singleton before omission merging. Missing-row defaults remain separate. Seven concurrency/validation cases plus existing Settings tests prove all six persisted fields and both orders. | **CLOSED.** |
| **SBA-003 / Medium** | `feed/internal/application/FeedItemService.ingestFetch` rechecks enabled immediately after the existing final locked refresh. Five new cases prove cached committed disable, final-gate contention, update+insert rollback with retained prior timestamps, and exact ingestion-first bookkeeping/item retention. | **CLOSED.** |
| **SBA-004 / Medium** | `importdata/internal/parsing/MarkdownImportParser` recognizes complete delimiter lines and adjacent empty blocks without rewriting raw text. Seventeen unit cases plus persisted Note tests cover both audit fixtures, LF/CRLF/EOF, body thematic breaks and nested/unknown/decimal metadata. | **CLOSED.** |
| **SBA-005 / Medium** | `authentication/internal/application/privatepin/PrivatePinService.changePin` locks/refreshes the owner row before current-PIN comparison. Twelve cases prove stale rejection, exactly one winner in both orders, exact unrelated credentials and auth/format/bootstrap denials. Ordinary verify reads stay unchanged. | **CLOSED.** |
| **SBA-006 / Medium** | `importdata/internal/parsing/JsonImportParser` requires EOF after one array root. Twenty-seven unit cases and domain/HTTP tests prove trailing-document rejection, safe 422 errors/logs, blocked transitions/no writes, and preserved whitespace/decimal controls. | **CLOSED.** |
| **SBA-007 / Low** | `people/internal/application/PersonService.validatePositiveDecimal` performs bounded long digit arithmetic before trailing-zero normalization. Seventy validation cases, 18 PostgreSQL integration cases and six HTTP cases cover field/operation matrices, unchanged numeric(6,2), legitimate scientific notation, no writes and 422/PERSON_INVALID. | **CLOSED.** |

## Independent before/after receipts

Created `logs/sba-codex-baseline-a39d972.zip` using `git archive` of the approved commit's `backend/`.
Unpacked into ignored `logs/sba-codex-baseline-a39d972/`; copied exactly seven current regression source files,
without adaptation. All **1,304 baseline main-source/resource files** match their Git blob hashes; all seven
test copies match their current source SHA-256. The owner's working tree was never reset or switched.

Commands, environment, per-finding assertion failures and source hashes are recorded in
[test evidence §2](../test-evidence.md#2-independent-failing-before--fixed-after-receipts).
Local ignored artifacts include `logs/sba-codex-baseline-manifest.json`,
`logs/sba-codex-baseline-all-receipts.json`, and the two red logs.

| Finding | Actual baseline assertion | Fixed result |
| --- | --- | --- |
| SBA-001 | `cachedActiveCommittedTrashRefusesMetadata`: expected throwable; stale attachTag succeeded. | Same case passes; trashed row and zero tags verified fresh. |
| SBA-002 | `cachedSettingsCommittedUpdatePreservesDisjointFields`: expected pagination 50, actual 20. | Same case passes; omitted committed fields preserved. |
| SBA-003 | `cachedEnabledIndependentCommittedDisableRollsBack`: expected throwable; ingestion succeeded after committed disable. | Same case passes; disabled source, zero batch items and unchanged initial bookkeeping. |
| SBA-004 | `auditFixturePrefixLookingYamlKeyRetainsSummary`: expected summary entry absent; frontmatter truncated to title. | Same case passes with summary/unknown key/raw text retained. |
| SBA-005 | `cachedUserCompetingCommittedChangeRejectsStaleOldPin`: expected throwable; old PIN accepted. | Same case passes; winner PIN and exact credentials retained. |
| SBA-006 | `rejectsArrayFollowedByGarbage`: expected throwable; trailing garbage ignored. | Same case passes with safe domain rejection. |
| SBA-007 | `PersonValidationTest`: 12 extreme-value assertions fail across both fields/create/update. Overflowing width reaches mocked collaborators; trailing-zero normalization throws ArithmeticException. No pre-fix extreme value is sent to JDBC. | All 70 unit cases pass; checked-in PostgreSQL/HTTP no-write matrices also pass. |

First red run: exit **1**, **4 tests / 4 failures / 0 errors / 0 skips**, 49.607 s,
finished 21:21:39 +07. Outer-class method selectors did not execute the three nested unit classes.
Second red run therefore selects those whole classes: exit **1**, **114 tests / 29 failures / 0 errors / 0 skips**,
5.809 s, finished 21:23:57 +07. Markdown contributes 2/17 failures, JSON 15/27, Person 12/70;
the table identifies the defect-specific assertions, rather than attributing every failed control to its SBA ID.
Both are expected regression failures, not compilation/container/setup failures.

The fixed-side independent clean full command, from `backend/`, was:

```powershell
mvn -ntp -l ../logs/sba-final-acceptance-2026-10-13-verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report
```

Exit **0**, BUILD SUCCESS, **1,152/0/0/0**, **04:25**, finished **2026-10-13T21:19:04+07:00**;
JaCoCo analyzed **811 classes**. Actual XML testcase enumeration corroborates 1,152 with no failures/errors/skips.
Nested XML root-suite `tests` attributes alone undercount, so they were not used as the total.
All seven regression sources and 12 changed production files retained their hashes throughout these checks.
The full green run preceded the isolated red runs; no implementation changes occurred between them.

Antigravity supplied focused **417/0/0/0** (01:32, 20:59:43 +07) and clean full **1,152/0/0/0**
(04:33, 21:04:25 +07) results. These are identified as submitted evidence; the independent full run
covers the focused classes, so a duplicate focused run was unnecessary.

Environment: Maven **3.9.15**, Oracle OpenJDK **25.0.2**, Windows 11 amd64, UTF-8;
runtime `C:\Users\VU KHANG\.jdks\openjdk-25.0.2`; Testcontainers **2.0.5**,
PostgreSQL **18.6-alpine**, MinIO **RELEASE.2024-11-07T00-52-28Z**.

## Phase 17, architecture and design assessment

SBA-001 is compatible with [accepted P17-3 §1A](../../../phase-17/revocation-and-derived-data.md):
the common authority is the existing physical Vault row, not an unrelated lock token; fresh reads occur after
locking in the caller's writable REQUIRED transaction; the lock is held through COMMIT and acquired before
child metadata writes. The guard centralizes actual reuse inside Vault and preserves Repository/Facade/Adapter
roles. Mandatory collaborators prevent alternate constructors from bypassing the invariant.

Settings and PIN keep their own singleton policies; Feed retains its final gate; parsers and numeric validation
remain local. No generic lock framework, speculative interfaces/hierarchy, unrelated refactor or module edge
was introduced. Capability APIs, root ApiResponse/error/OpenAPI types and module-local DTO/mapper/advice remain
consistent with [ADR-0016](../../../../adr/0016-root-http-contract-module-local-adapters.md).

This maintenance does not implement or certify Fiction/Film participation, RAG revisions/grants/epochs, Java/Python
worker permissions, staging/purge fences, NTFS handles or final-response delivery. Phase 17's R2/R7 and other
runtime acceptance tests remain required under a separate handoff after maintenance owner commit/push.

## Repository, diagnostics and limits

Actual recursive inspection across Vault, Settings, Authentication, Feed, ImportData and People found
**336 production Java files / 107 packages / 37 test Java files**. Package declarations match paths;
production packages have descriptors; no stale .gitkeep or cross-owner internal import was found.
Commands/views/enums/exceptions and internal adapters fit the canonical package tree; the new helper is in an
existing owning package. Full local inventory: `logs/sba-final-acceptance-package-inventory.json`.

Fresh IntelliJ inspection requested all 12 changed production files: **0 errors, 10 warnings and 2 weak warnings**,
with no reported timeout/incomplete flag. Remaining diagnostics concern unused imports/variable/methods,
duplicated tag argument checks, validation-only ZoneId parsing, functional/style suggestions and a redundant cast.
They do not establish a new scoped defect. No blanket suppression or unrelated warning cleanup is required.

Build warnings include Lombok's deprecated Unsafe use, an existing test-only deprecated Spring converter and
dynamic Byte Buddy agent loading. No comparative SpotBugs/PMD/Checkstyle run or warning-free claim is made.
Representative performance/p50/p95 and comprehensive current CVE/license/SBOM assessment remain NOT_VERIFIED.
Schema, migrations, POM, ADRs, architecture/package-tree, Phase 15 history and Phase 17 preparation diffs are empty.
Raw logs, baseline archive/output and manifests remain ignored and excluded from the maintenance commit.

## Complete 29-dimension ledger

PASS means evidence within this maintenance scope, not a universal certification.

| # | Dimension | Status | Evidence / limit |
| ---: | --- | --- | --- |
| 1 | Owner intent / permitted behavior / non-goals | PASS | Exactly the approved seven-defect slice; no Frontend/RAG/deployment expansion. |
| 2 | Requirement traceability | PASS | All SBA and FR IDs closed; unchanged checked-in regressions have direct baseline red/current green receipts. |
| 3 | ADR / schema / diagrams / workflow consistency | PASS | Frozen diffs empty; approved maintenance and separately gated Phase 17 preserved. |
| 4 | Ownership / NamedInterfaces / transaction data flow | PASS | Recursive descriptors/imports plus full Modulith verification; owner repositories remain internal. |
| 5 | Actual repository/package/folder structure | PASS | 336/107/37 recursive inspection; existing capability grouping, descriptors, no stale placeholders. |
| 6 | Shared HTTP / exception placement | PASS | ADR-0016 root envelope and module-local DTO/mapper/advice unchanged. |
| 7 | SOLID / cohesion / substitutability / dependency direction | PASS | Narrow canonical Vault guard, mandatory collaborators, owner-local policies. |
| 8 | DRY / KISS / YAGNI / invariant consistency | PASS | Real shared invariant centralized; no bypass/null fallback or generic framework. |
| 9 | Patterns / anti-patterns | PASS | Existing Repository/Facade/Adapter roles; minimal concrete helper with actual callers. |
| 10 | Domain correctness / states / idempotency | PASS | Stale states, both serial orders, returned views, repeated transitions and composition pass. |
| 11 | REST / wire / OpenAPI compatibility | PASS | Full HTTP/OpenAPI/route inventory tests; existing generic error codes/messages retained. |
| 12 | Input / bounds / hostile format / overflow | PASS | Complete-document parsing, whole delimiters and bounded numeric normalization with no-write controls. |
| 13 | Authentication / authorization / privacy | PASS | Stale PIN/both winner orders, exact credentials and auth/bootstrap/format denials pass. |
| 14 | Secrets / logs / retention / egress | PASS | Source review and synthetic import error/log capture; no new retention/egress path. |
| 15 | Atomicity / lock identity/order/lifetime / concurrency | PASS | Owning physical rows, fresh state, intended waiter/blocker/statement and outer-commit lifetime proved. |
| 16 | Failure / recovery / rollback | PASS | Feed insert/update rollback and exact success bookkeeping; import no-write failures; bounded worker cleanup. |
| 17 | PostgreSQL / Flyway / JPA / physical schema | PASS | Real migrated PostgreSQL; bigint/Long and smallint/Short identities; unchanged schema/mappings. |
| 18 | Complexity / resources / contention scope | PASS | Indexed row guards, existing Feed final gate, linear delimiter scan and bounded numeric arithmetic; no throughput guarantee. |
| 19 | Retrieval / index / recommendation quality | N/A | No retrieval, recommendation or RAG corpus implementation. |
| 20 | Document / multilingual fidelity | PASS | Exact raw Markdown and nested/unknown/decimal metadata; external-file/OCR formats remain outside this slice. |
| 21 | Adversarial / privacy / authorization tests | PASS | Synthetic stale credentials, hostile numeric/parser inputs, safe body/log markers and no writes. |
| 22 | Test sufficiency / determinism / risk coverage | PASS | Both orders, stale contexts, explicit PostgreSQL waiters, checked timeouts/interruption, context cleanup; 1,152 green tests. |
| 23 | Performance / p50/p95 / memory/workload measurements | NOT_VERIFIED | Representative benchmarks outside this approved repair. |
| 24 | Model / held-out evaluation science | N/A | No model/LLM/retrieval evaluation change. |
| 25 | Dependencies / licenses / vulnerabilities / SBOM | NOT_VERIFIED | No dependency changes; fresh comprehensive advisory/license/SBOM scan not performed. |
| 26 | Build / architecture / static / tooling | PASS | Independent clean verify/coverage and fresh IDE diagnostics with explicit warning triage. |
| 27 | Logging / observability | PASS | Generic errors and existing logging retained; synthetic import payload markers absent from captured logs/bodies. |
| 28 | Documentation / governance / evidence | PASS | Current evidence corrected to actual executions, source semantics and counts; previous reviews/history preserved. |
| 29 | Hygiene / delivery | PASS | No production/test edit by reviewer; diff/link/status/hash checks; generated outputs/config approvals excluded. |

Post-recording verification passed: 27 local documentation links resolve; both ledgers contain exactly dimensions
1–29; current ACTIVE/evidence status is READY_FOR_OWNER_COMMIT; no trailing whitespace; git diff --check exits 0.
All 12 production hashes and both previous review hashes are unchanged; disposable baseline/log outputs are ignored.

## Delivery and next gate

No blocking finding or owner-accepted debt remains in this slice. Record `READY_FOR_OWNER_COMMIT` in ACTIVE;
do not archive/reset the handoff or reopen historical Phase 15 in this review.

Suggested owner commit message:

```text
fix(backend): remediate supplemental audit findings
```

**Next step:** owner reviews the scoped diff and commits/pushes the maintenance, excluding local tool approvals
and ignored verification outputs, then gives the latest package to ChatGPT for maintenance closeout.
Only afterward proceed through the separate Phase 17 implementation-handoff workflow. No agent publishes.
