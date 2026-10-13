# Supplemental backend audit maintenance — test evidence

- **Date:** 2026-10-13.
- **Handoff:** `maintenance-supplemental-backend-audit-2026-10-13`.
- **Status:** `READY_FOR_OWNER_COMMIT`; SBA-001–007 and FR-SBA-1–4 CLOSED.
- **Implementer:** Antigravity. **Final reviewer:** Codex.
- **Approved baseline:** `a39d97241c98930a80fd5482be29326d7ed66e5c`, unchanged HEAD/origin/main.
  Historical baseline audit passed 1,021 tests; this review reran the selected baseline regressions, not another full baseline suite.
- **Independent fixed-side verification:** **1,152 tests / 0 failures / 0 errors / 0 skips**, 04:25,
  finished **2026-10-13T21:19:04+07:00**; JaCoCo 811 classes; +131 tests over the historical baseline.
- **Submitted Antigravity results:** focused **417/0/0/0**, 01:32, 20:59:43 +07;
  clean full **1,152/0/0/0**, 04:33, 21:04:25 +07. These are submitted results, distinct from Codex's independent run.
- **Authoritative disposition:** [final Codex acceptance](reviews/2026-10-13-sba-001-007-final-codex-acceptance.md).
  Codex corrected this current evidence file to the executions and source semantics actually verified.
  Both preceding formal review reports remain unchanged.

Environment verified by `mvn -version` and the independent build: Maven **3.9.15**, Oracle OpenJDK **25.0.2**,
Windows 11 amd64, UTF-8; runtime `C:\Users\VU KHANG\.jdks\openjdk-25.0.2`;
Testcontainers **2.0.5**, PostgreSQL **18.6-alpine**, MinIO **RELEASE.2024-11-07T00-52-28Z**.

## 1. Implementation and deterministic acceptance proofs

Production paths are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`;
test paths are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

| Finding | Implemented correction | Checked-in proof and fresh final assertion |
| --- | --- | --- |
| **SBA-001 / Medium** | `vault/internal/application/entry/VaultEntryMutationGuard` uses `VaultEntryRepository.findByIdForUpdate` and `EntityManager.refresh(entry, PESSIMISTIC_WRITE)`. Every lifecycle/metadata service constructor requires the guard. | `vault/VaultMutationConcurrencyTest` (**13**): cached active/trashed contexts, trash/restore both orders and returned views; all six metadata operations with seeded removals and actual contention both orders; no-write denial, timestamps/idempotency, composition and outer-transaction commit lifetime. Fresh JDBC child/lifecycle assertions. |
| **SBA-002 / Medium** | `AppSettingsService.initializeOrUpdate(UpdateSettingsCommand)` locks/refreshes the existing singleton before deriving omitted fields. Missing-row defaults remain separate; EntityManager is mandatory. | `settings/AppSettingsConcurrencyTest` (**7**): stale pagination, disjoint updates both orders, backup/auto-lock both orders, unrelated-field preservation, explicit disable/omission, defaults and invalid merge. Fresh JDBC checks all six columns. |
| **SBA-003 / Medium** | `FeedItemService.ingestFetch` rechecks `isEnabled()` immediately after the existing final locked lookup and `entityManager.refresh(sourceToUpdate)`. Denial rolls back staged items and bookkeeping. | `feed/FeedIngestionConcurrencyTest` (**5**): cached committed disable; disable-first update+insert rollback, retaining prior non-null fetch/schedule timestamps; ingestion-first true contention, exact normalized last/next fetch values followed by retained item values. FOR NO KEY UPDATE isolates the final gate from item FK key-share waits. |
| **SBA-004 / Medium** | `MarkdownImportParser` recognizes complete delimiter lines, adjacent empty blocks, LF/CRLF and EOF without rewriting raw Markdown. | `importdata/internal/parsing/MarkdownImportParserTest` (**17**), plus `ImportDataIntegrationTest.markdownAuditWorkflowEndToEnd`, `markdownEmptyFrontmatterThematicBreakEndToEnd`, `markdownPositiveNestedMetadataFidelityEndToEnd`: both audit fixtures, exact raw text, unknown/nested/decimal metadata and persisted Note/Knowledge assertions. |
| **SBA-005 / Medium** | `PrivatePinService.changePin` locks/refreshes the existing owner before comparing current PIN and retains the guard until commit. Ordinary verify reads remain unchanged. | `authentication/PrivatePinConcurrencyTest` (**12**): stale old-PIN rejection, exactly one winner in both orders, fresh winner hash verification and exact username/email/password-hash/created-at equality; wrong-current/format/bootstrap/anonymous denials. Main/worker authentication contexts cleared. |
| **SBA-006 / Medium** | `JsonImportParser` consumes one root array and requires `parser.nextToken() == null`; rejects trailing documents/garbage with the existing safe domain/wire error policy. | `importdata/internal/parsing/JsonImportParserTest` (**27**); integration/HTTP cases prove whole-job no-write failure, blocked validate/execute, unchanged prior job, valid whitespace/decimal controls, and synthetic markers absent from error bodies/logs. |
| **SBA-007 / Low** | `PersonService.validatePositiveDecimal` checks raw integer digits using long arithmetic before normalization, catches normalization ArithmeticException as InvalidPersonException and preserves numeric(6,2). | `people/PersonValidationTest` (**70**), `PersonIntegrationTest` (**18**), `PeopleWebIntegrationTest` (**6**): height/weight × create/update for extreme positive exponents including trailing zeros; collaborator non-interaction, no new Vault/Person rows, immutable existing values and HTTP 422/PERSON_INVALID. Valid width/scale/scientific-notation controls pass. |

All four remaining fixture obligations from the re-review are now implemented: observers identify intended waiter PID,
holder PID, owning-table statement, wait event and `pg_blocking_pids`; interrupted/timed-out holders fail their
transaction; finally blocks release latches and await executor termination; PIN before/after fixtures clear the
main thread. Import web tests use OutputCaptureExtension with synthetic trailing-object and trailing-garbage markers.

## 2. Independent failing-before / fixed-after receipts

Codex created the isolated snapshot with:

```powershell
git archive --format=zip --output=logs/sba-codex-baseline-a39d972.zip a39d97241c98930a80fd5482be29326d7ed66e5c backend
```

Unpacked under ignored `logs/sba-codex-baseline-a39d972/`. Exactly seven current regression files were copied
byte-for-byte, without adaptation: Vault/Settings/Feed/PIN concurrency, Markdown/JSON parser, and Person validation.
All **1,304 baseline main-source/resource files** match Git blob hashes. All seven copied tests match their current
SHA-256. No owner-tree reset, branch switch, production/test edit or publish occurred.

The exact first command, from `logs/sba-codex-baseline-a39d972/backend/`, was:

```powershell
mvn -ntp -l ../../sba-codex-baseline-red.log "-Dtest=VaultMutationConcurrencyTest#cachedActiveCommittedTrashRefusesMetadata,AppSettingsConcurrencyTest#cachedSettingsCommittedUpdatePreservesDisjointFields,FeedIngestionConcurrencyTest#cachedEnabledIndependentCommittedDisableRollsBack,MarkdownImportParserTest#auditFixturePrefixLookingYamlKeyRetainsSummary,PrivatePinConcurrencyTest#cachedUserCompetingCommittedChangeRejectsStaleOldPin,JsonImportParserTest#rejectsArrayFollowedByGarbage,PersonValidationTest#rejectsHeightExceedingPrecision" test
```

**B1:** exit **1**, BUILD FAILURE, **4 tests / 4 failures / 0 errors / 0 skips**, **49.607 s**,
finished **2026-10-13T21:21:39+07:00**. Only the four top-level concurrency methods executed.
The outer-class method filters did not execute the nested parser/Person methods; B1 therefore provides no receipt
for SBA-004/006/007. Their actual second command selected the complete nested-test classes:

```powershell
mvn -ntp -l ../../sba-codex-baseline-unit-red.log "-Dtest=MarkdownImportParserTest,JsonImportParserTest,PersonValidationTest" test
```

**B2:** exit **1**, BUILD FAILURE, **114 tests / 29 failures / 0 errors / 0 skips**, **5.809 s**,
finished **2026-10-13T21:23:57+07:00**. Markdown: 2/17 failures; JSON: 15/27; Person: 12/70.
The per-finding table identifies the relevant assertion rather than treating every failed control as an SBA defect.
Both red runs compiled and executed normally; failures are test assertions, not setup/container failures.

| Finding | Run / exact executed regression | Observed baseline failure | Fixed-side proof |
| --- | --- | --- | --- |
| **SBA-001** | B1: `VaultMutationConcurrencyTest#cachedActiveCommittedTrashRefusesMetadata` | AssertionError: expected a throwable; stale cached active entry allowed attachTag after committed trash. | Same case passes; fresh trashed row and zero tags. |
| **SBA-002** | B1: `AppSettingsConcurrencyTest#cachedSettingsCommittedUpdatePreservesDisjointFields` | AssertionFailedError: expected pagination 50, actual 20. | Same case passes; fresh six-column settings state preserves independently committed values. |
| **SBA-003** | B1: `FeedIngestionConcurrencyTest#cachedEnabledIndependentCommittedDisableRollsBack` | AssertionError: expected a throwable; ingestFetch admitted committed-disabled source. | Same case passes; source disabled, zero staged items, initial last-fetch value unchanged. Separate contention fixture preserves prior last/next timestamps exactly. |
| **SBA-004** | B2: `MarkdownImportParserTest`, nested `AuditFixturesAndDelimiterTests.auditFixturePrefixLookingYamlKeyRetainsSummary` | AssertionError: payload lacks `summary="High level patterns summary"`; frontmatter contains only title. | Same case passes with summary, `---custom`, source fields and exact raw text retained. |
| **SBA-005** | B1: `PrivatePinConcurrencyTest#cachedUserCompetingCommittedChangeRejectsStaleOldPin` | AssertionError: expected a throwable; superseded current PIN was accepted. | Same case passes; winning PIN and unrelated credentials retained. Actual denial message is `Current PIN is incorrect`. |
| **SBA-006** | B2: `JsonImportParserTest`, nested `AuditTrailingTokensTests.rejectsArrayFollowedByGarbage` | AssertionError: expected a throwable; trailing garbage ignored. | Same case passes with InvalidImportJobException; negative HTTP/log/no-write controls also pass. |
| **SBA-007** | B2: `PersonValidationTest`, extreme-value assertions in nested create/update validation classes | 12 assertions fail across height/weight × create/update: width overflow admits `1E+2147483647` and reaches mocked collaborators; `100E+2147483647`/`1000E+2147483647` produce ArithmeticException during normalization instead of InvalidPersonException. No extreme baseline value reaches JDBC. | All 70 cases pass with safe domain rejection; current PostgreSQL/HTTP no-write matrices pass. |

The green side is the independent full command in §6, with the exact same regression sources.
It ran before the isolated red commands; source hashes remained identical throughout. XML enumeration confirms
the named cases are green, not merely selected in a command.

Local ignored provenance: `logs/sba-codex-baseline-manifest.json` (commit, archive hash, all test hashes, source check
and run results), `logs/sba-codex-baseline-source-tree.txt`, `logs/sba-codex-baseline-all-receipts.json`,
`logs/sba-final-acceptance-selected-green-cases.json`; raw XML remains under the two target directories.

| Raw log | SHA-256 |
| --- | --- |
| `logs/sba-codex-baseline-red.log` | `f8c56e4f71e1dc0305e747443bafba4ac0acd73c9276da65236f098cdd5a92bb` |
| `logs/sba-codex-baseline-unit-red.log` | `b4cac25e321fd4eaf6752732604a2a48f7184664003da2d567e3ffe0de5659d5` |
| `logs/sba-final-acceptance-2026-10-13-verify.log` | `1d5009da9492dd44ca58536b16a6c30afbbe2ecaa04f3434d51721584b80093e` |

## 3. Physical guards, freshness and Phase 17 compatibility

| Finding | Physical key / Java identity | Guard and freshness |
| --- | --- | --- |
| SBA-001 | `vault_entries.id`: bigint / Long | Same existing parent row, repository PESSIMISTIC_WRITE plus explicit locked refresh before decisions; parent before metadata children, same REQUIRED writable caller transaction, held through outer COMMIT. |
| SBA-002 | `app_settings.id`: smallint / Short, `(short) 1` | Existing singleton lock and explicit PESSIMISTIC_WRITE refresh before omitted-field merge. Missing-row initialization stays separate. |
| SBA-005 | `app_users.id`: smallint / Short, `(short) 1` | Existing owner lock and explicit PESSIMISTIC_WRITE refresh before current-PIN comparison/update. |
| SBA-003 | `feed_sources.id`: bigint / Long | Existing final repository lock followed by `refresh(sourceToUpdate)` and enabled recheck. Rollback retains whatever prior bookkeeping existed, including non-null timestamps. |

The implemented Vault invariant is compatible with accepted
[Phase 17 §1A](../../phase-17/revocation-and-derived-data.md): one physical source guard, authoritative state after
lock, parent-first order and commit lifetime. No public RAG guard API, Fiction/Film participation, approval epoch,
worker/lease/purge, Python runtime or NTFS capability is implemented or certified here. Future R2/R7 and other
source-commit tests remain independently required under the separate Phase 17 handoff after owner commit/push.

## 4. HTTP and error compatibility

Root ApiResponse/framework/OpenAPI types and module-local DTO/mapper/advice remain in their ADR-0016 locations.
Public capability/wire contracts are unchanged. Full web and OpenAPI inventory tests pass.

| Error path | Preserved HTTP result |
| --- | --- |
| Vault trash denial | 409 / `VAULT_CONFLICT`. Domain message is `Vault entry is in trash: <id>`; restore remains idempotent. |
| Incorrect current PIN | 401 / `AUTH_INVALID_PIN`. Domain message is `Current PIN is incorrect`. |
| Person invalid measurement | 422 / `PERSON_INVALID`, generic message `Invalid person data`. |
| Invalid JSON import document | 422 / `INVALID_IMPORT_JOB`, generic message `Invalid import job data`; domain message `Invalid JSON syntax in import file`. |

Trailing-document web fixtures prove markers absent from error bodies/application logs, zero target writes and
blocked job transitions. These are synthetic fixture assertions, not a universal claim about every historic log.

## 5. Complete inventory of changed implementation files

### Production Code (12 files)
1. `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java` (New helper: single canonical responsibility for locking & refreshing `VaultEntry`)
2. `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java` (Guarded `moveToTrash`, `restore`; eliminated unguarded constructors and fallback paths)
3. `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/metadata/VaultMetadataService.java` (Guarded 6 metadata mutations; eliminated unguarded constructors and fallback paths)
4. `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java` (Added `findByIdForUpdate` with `PESSIMISTIC_WRITE`)
5. `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/internal/application/configuration/AppSettingsService.java` (Made `EntityManager` mandatory; eliminated unguarded constructors; removed nullable-save fallback)
6. `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/internal/infrastructure/persistence/AppSettingsRepository.java` (Added `findByIdForUpdate` with `PESSIMISTIC_WRITE`)
7. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java` (Added enabled recheck immediately after locked refresh)
8. `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/MarkdownImportParser.java` (Overhauled frontmatter delimiter detection for exact line matching)
9. `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/JsonImportParser.java` (Enforced single root document and rejected trailing tokens)
10. `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/privatepin/PrivatePinService.java` (Made `EntityManager` mandatory; eliminated unguarded constructors)
11. `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/persistence/AppUserRepository.java` (Added `findByIdForUpdate` with `PESSIMISTIC_WRITE`)
12. `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/internal/application/PersonService.java` (Updated `validatePositiveDecimal` to use `long` arithmetic and guarded `stripTrailingZeros()`)

### Test Code (14 files)
1. `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultMutationConcurrencyTest.java` (New concurrency suite, 13 tests)
2. `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultEntryIntegrationTest.java` (Injected guard into constructor fixtures, 5 tests)
3. `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultMetadataIntegrationTest.java` (Injected guard into constructor fixtures, 11 tests)
4. `backend/src/test/java/com/vhvkhangg/personalprivatevault/settings/AppSettingsConcurrencyTest.java` (New concurrency suite, 7 tests)
5. `backend/src/test/java/com/vhvkhangg/personalprivatevault/settings/AppSettingsValidationTest.java` (Supplied mock `EntityManager` and stubbed `findByIdForUpdate`, 22 tests)
6. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedIngestionConcurrencyTest.java` (New concurrency suite, 5 tests)
7. `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/MarkdownImportParserTest.java` (New parser suite, 17 tests)
8. `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/JsonImportParserTest.java` (New parser suite, 27 tests)
9. `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/ImportDataIntegrationTest.java` (Added integration tests for Markdown lookalike/empty/nested frontmatter and trailing JSON tokens, 13 tests)
10. `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/ImportDataWebIntegrationTest.java` (Added MockMvc HTTP 422 tests and log sentinel assertions for trailing JSON tokens, 3 tests)
11. `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/PrivatePinConcurrencyTest.java` (New concurrency suite, 12 tests)
12. `backend/src/test/java/com/vhvkhangg/personalprivatevault/people/PersonValidationTest.java` (Expanded height/weight create/update matrix with `100E+2147483647` and `1000E+2147483647` and non-interaction checks, 70 tests)
13. `backend/src/test/java/com/vhvkhangg/personalprivatevault/people/PersonIntegrationTest.java` (Expanded height/weight create/update matrix with zero DB write and immutability assertions, 18 tests)
14. `backend/src/test/java/com/vhvkhangg/personalprivatevault/people/PeopleWebIntegrationTest.java` (Expanded height/weight create/update matrix with MockMvc HTTP 422 `PERSON_INVALID` and zero DB write assertions, 6 tests)

### Documentation & Governance (2 files)
1. `docs/implementation/maintenance/supplemental-backend-audit-2026-10-13/test-evidence.md` (This evidence file)
2. `docs/implementation/handoffs/ACTIVE.md` (Updated status to `READY_FOR_OWNER_COMMIT`)


Formal Codex reports are in this maintenance scope's `reviews/`; prior reports are preserved.
Tool-generated `.codex/config.toml` approvals remain separate. Baseline snapshots/logs/manifests and generated
build/coverage output remain ignored and excluded from the maintenance commit.

## 6. Exact verification commands and results

From `backend/`, direct PowerShell command used by Codex:

```powershell
mvn -ntp -l ../logs/sba-final-acceptance-2026-10-13-verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report
```

Exit **0**, BUILD SUCCESS, **1,152/0/0/0**, **04:25**, finished **2026-10-13T21:19:04+07:00**;
JaCoCo **811 classes**. Actual XML testcase enumeration independently corroborates 1,152.
Nested suite-root `tests` attributes undercount; the total is not their naive sum.
All affected focused classes, Modulith boundaries, PostgreSQL/Flyway, MinIO, authentication, HTTP/OpenAPI,
import rollback and existing backend regression suites are included.

Submitted Antigravity focused command, expressed directly in PowerShell without interpolating a child shell:

```powershell
mvn -ntp "-Dtest=Vault*Test,AppSettings*Test,SettingsWebIntegrationTest,Feed*Test,ImportData*Test,MarkdownImportParserTest,JsonImportParserTest,PrivatePin*Test,AuthWebIntegrationTest,AuthenticationBootstrapIntegrationTest,BootstrapValidationTest,RefreshTokenLifecycleIntegrationTest,SecurityFilterChainIntegrationTest,SecretRedactionTest,Person*Test,PeopleWebIntegrationTest" test
```

Submitted result: **417/0/0/0**, **01:32**, 20:59:43 +07.
Submitted full command: `mvn -ntp clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report`;
**1,152/0/0/0**, **04:33**, 21:04:25 +07. Codex independently reran the clean full gate rather than duplicating the
focused run. Root `git diff --check` is part of final documentation/hygiene verification.

## 7. Complete 29-dimension ledger

See the [formal acceptance ledger](reviews/2026-10-13-sba-001-007-final-codex-acceptance.md#complete-29-dimension-ledger)
for detailed reviewer disposition. PASS is limited to this approved repair.

| # | Dimension | Status | Evidence / limit |
| ---: | --- | --- | --- |
| 1 | Owner intent / non-goals | PASS | All seven approved findings; no Frontend/RAG/deployment expansion. |
| 2 | Requirement traceability | PASS | §1/2: all seven IDs and actual unchanged-source baseline/current regressions. |
| 3 | Architecture / ADR / diagrams | PASS | Frozen baselines unchanged; separately gated Phase 17 respected. |
| 4 | Ownership / NamedInterfaces / direction | PASS | Owner-local repositories/policies; recursive import/descriptors and Modulith gate. |
| 5 | Repository / package / folder organization | PASS | Recursive 336 Java / 107 packages / 37 tests across affected owners; no mismatch/missing production descriptor/placeholder. |
| 6 | Shared contracts / exception placement | PASS | ADR-0016 root HTTP support, owner-local DTO/mapper/advice unchanged. |
| 7 | SOLID / cohesion / coupling | PASS | One reused Vault invariant helper; mandatory collaborators; owner policies remain cohesive. |
| 8 | DRY / KISS / YAGNI / invariants | PASS | No generic hierarchy, unlocked constructor or null fallback. |
| 9 | Patterns / anti-patterns | PASS | Existing Repository/Facade/Adapter roles; smallest concrete guard. |
| 10 | Domain behavior / states / idempotency | PASS | Stale states, both orders, timestamps/views, repeated transitions and composition. |
| 11 | REST / wire / OpenAPI compatibility | PASS | Full HTTP/OpenAPI regressions and unchanged safe codes/messages. |
| 12 | Input / format / numeric validation | PASS | Whole delimiters, complete JSON root and bounded numeric normalization; no-write controls. |
| 13 | Authentication / authorization / privacy | PASS | PIN stale/winner/auth/format/bootstrap cases and exact credential preservation. |
| 14 | Credentials / logs / retention / egress | PASS | Safe source/errors plus synthetic import markers; no new retention/egress path. |
| 15 | Transactions / locks / concurrent writes | PASS | Physical owner rows, fresh state, waiter/blocker/owning statement and commit lifetime. |
| 16 | Failure / recovery / rollback | PASS | Feed staged insert/update rollback and exact successful bookkeeping; import failure no writes; bounded cleanup. |
| 17 | Flyway / PostgreSQL / JPA / schema | PASS | Real migrated PostgreSQL, correct bigint/smallint identities, unchanged physical schema. |
| 18 | Complexity / resources / contention scope | PASS | Indexed guards, existing final Feed gate, linear delimiter scan, no huge numeric expansion. No load guarantee. |
| 19 | Data / index / recommendation quality | N/A | No retrieval/recommendation/corpus change. |
| 20 | Document / multilingual fidelity | PASS | Exact Markdown and nested/unknown/decimal metadata. OCR/external-file formats outside this slice. |
| 21 | Security / adversarial tests | PASS | Stale credentials, hostile formats/exponents, synthetic log/body markers and no writes. |
| 22 | Test sufficiency / determinism / risk coverage | PASS | Both orders, stale contexts, intended waiters, interruption/termination/context cleanup; 1,152 green cases. |
| 23 | Performance benchmarks | NOT_VERIFIED | Representative workload/p50/p95/memory measurements outside this repair. |
| 24 | Evaluation science / held-out model set | N/A | No model/LLM evaluation change. |
| 25 | Dependencies / licenses / vulnerabilities | NOT_VERIFIED | POM/dependency pins unchanged; fresh comprehensive CVE/license/SBOM assessment not run. |
| 26 | Build / static / tooling / CI | PASS | Independent clean verify/coverage; fresh IDE diagnostics explicitly triaged. |
| 27 | Logging / observability | PASS | Existing generic logging/errors; synthetic import source markers absent from captured output. |
| 28 | Documentation / governance | PASS | Actual receipts, selectors, source semantics/counts; previous reports preserved. |
| 29 | Hygiene / delivery | PASS | Diff/link/status/hash verification; ignored outputs and separate config approvals excluded. |

## 8. Diagnostics and retained limitations

Fresh IntelliJ inspection requested all **12** changed production files: **0 errors / 10 warnings / 2 weak warnings**,
with no reported timeout/incomplete flag. Observations concern unused imports/variable/methods, duplicate tag
argument checks, validation-only ZoneId parsing and optional style/cast simplifications; no new blocking defect.
No broad warning cleanup or suppression is authorized.

Build warnings include Lombok Unsafe use, an existing test-only deprecated Spring JSON converter and dynamic
Byte Buddy agent loading. No comparative SpotBugs/PMD/Checkstyle result, warning-free repository,
vulnerability-free claim or RAG-scale performance claim is made.

Representative performance and current dependency/license/SBOM measurements remain NOT_VERIFIED.
No schema/migration/POM/ADR/module-edge/public-contract change. No frozen Phase 15 review or Phase 17 preparation
was modified. Codex's acceptance report records recursive tree and history/hash checks.

## 9. Next gate

All seven SBA findings and four review findings are CLOSED; ACTIVE is `READY_FOR_OWNER_COMMIT`.
Owner reviews and commits/pushes the scoped maintenance, excluding local approvals and ignored outputs, then
provides the latest package to ChatGPT for maintenance closeout. The separate Phase 17 implementation handoff
may proceed only after that owner commit/push. No agent commits, pushes, tags or creates/merges a PR.
