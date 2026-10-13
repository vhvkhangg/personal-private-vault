# Supplemental backend audit maintenance — Archived Implementation Handoff

**Final status:** `COMPLETE — FROZEN` after Codex `READY FOR OWNER COMMIT`, owner commit/push
`d195b6260274` (`fix(backend): remediate supplemental audit findings`) and
[maintenance closeout](closeout.md). All SBA-001–007 and FR-SBA-1–4 are closed.

The original handoff contract is reproduced below as a **historical pre-commit snapshot**. Its
status fields, baseline commit and future-tense instructions reflect the implementation/review
period and do not override the closeout or authorize new work. Local relative Markdown links
have been rebased for this archive path.

---

# Active Implementation Handoff

- Handoff ID: `maintenance-supplemental-backend-audit-2026-10-13`
- Created by: Codex — 2026-10-13
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: one owner-approved maintenance slice, **SBA-001 through SBA-007**
- Baseline: `main`, HEAD `a39d97241c98930a80fd5482be29326d7ed66e5c`; initially clean. Tool-approval additions to `.codex/config.toml` appeared during planning; preserve them, exclude them from maintenance changes.

## Goal

Correct all seven verified supplemental defects in frozen Backend Phases 1/2/3/10 and their existing HTTP exposure,
without reopening historical Phase 15 findings. Implement SBA-001 first, then SBA-002/005, SBA-004/006/003 and SBA-007.
All seven remain OPEN until implementation evidence and Codex final review close them. Phase 17 preparation remains
accepted; its implementation handoff must wait for maintenance final acceptance and owner commit/push.

## Sources of truth

- [Approved maintenance scope and acceptance matrix](README.md)
- [Binding owner approval](owner-approval.md)
- [Verified findings, exact counterexamples and audit limitations](../../phase-15/reviews/2026-10-13-supplemental-backend-audit.md)
- [Handoff workflow](../../handoffs/README.md), root `AGENTS.md`, `docs/AGENTS.md`, `backend/AGENTS.md`,
  `backend/src/test/AGENTS.md` and the affected owners' `AGENTS.md`.
- Frozen [module dependency matrix](../../../architecture/module-dependency-matrix.md),
  [repository/package tree](../../../repository/repository-package-tree.md) and
  [Schema v1 DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml).
- Architecture decisions under `docs/adr/`, especially ADR-0004/0005/0006/0008/0012/0015/0016/0017/0018/0019; preserve each document’s accepted/proposed status.
  [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md) preserves root HTTP contracts.
- Phase 17 compatibility references only:
  [accepted preparation](../../phase-17/preparation-review.md),
  [latest 29-dimension/P17-3 review](../../phase-17/reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md),
  [source guard §1A and races](../../phase-17/revocation-and-derived-data.md).
  These references grant no RAG implementation authority here.

## Implementation targets

Production paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.
Only the listed corrections are authorized; the owner approval temporarily overrides historical phase-only gates
in affected module instructions for these defects.

| Finding | Permitted production targets | Required correction |
| --- | --- | --- |
| **SBA-001 / Medium** | `vault/internal/application/entry/VaultEntryService.java`; `vault/internal/application/metadata/VaultMetadataService.java`; `vault/internal/infrastructure/persistence/VaultEntryRepository.java` | Guard the existing entry row and refresh authoritative lifecycle state before trash/restore decisions and all six protected metadata mutations. If reuse needs a collaborator, permit only `vault/internal/application/entry/VaultEntryMutationGuard.java`, with the single lock/refresh responsibility. |
| **SBA-002 / Medium** | `settings/internal/application/configuration/AppSettingsService.java`; `settings/internal/infrastructure/persistence/AppSettingsRepository.java`; `settings/internal/domain/AppSettings.java` only if essential | Lock/refresh the existing singleton before merging omitted fields; retain the separate missing-row initialization/default path. |
| **SBA-003 / Medium** | `feed/internal/application/FeedItemService.java` | Recheck enabled immediately after the existing final source lock/refresh, before fetch bookkeeping; reject and roll back the staged batch. |
| **SBA-004 / Medium** | `importdata/internal/parsing/MarkdownImportParser.java` | Recognize complete delimiter lines, including adjacent empty blocks, LF/CRLF and closing delimiter at EOF, while preserving exact raw Markdown and complete frontmatter. |
| **SBA-005 / Medium** | `authentication/internal/application/privatepin/PrivatePinService.java`; `authentication/internal/infrastructure/persistence/AppUserRepository.java`; `authentication/internal/domain/AppUser.java` only if essential | Lock/refresh the existing owner row before current-PIN verification and retain the lock through PIN update commit. |
| **SBA-006 / Medium** | `importdata/internal/parsing/JsonImportParser.java` | Require one complete JSON document, rejecting every trailing token/garbage while accepting ordinary trailing whitespace. Keep configuration parser-local. |
| **SBA-007 / Low** | `people/internal/application/PersonService.java` | Make digit-width arithmetic overflow-safe before persistence; retain positive-value, normalized scale and numeric(6,2) limits. |

Test paths are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`:

- SBA-001: add `vault/VaultMutationConcurrencyTest.java`; extend `VaultEntryIntegrationTest.java`,
  `VaultMetadataIntegrationTest.java` and `VaultWebIntegrationTest.java` under `vault/` as needed.
- SBA-002: add `settings/AppSettingsConcurrencyTest.java`; retain/extend `AppSettingsIntegrationTest.java`,
  `AppSettingsValidationTest.java` and `SettingsWebIntegrationTest.java` under `settings/`.
- SBA-003: add `feed/FeedIngestionConcurrencyTest.java`; retain/extend `FeedIntegrationTest.java` and
  `FeedValidationTest.java` under `feed/`.
- SBA-004/006: add `importdata/internal/parsing/MarkdownImportParserTest.java` and `JsonImportParserTest.java`;
  extend `importdata/ImportDataIntegrationTest.java`, `ImportDataValidationTest.java` and
  `ImportDataWebIntegrationTest.java`. Keep `ImportDataConcurrencyTest.java` green.
- SBA-005: add `authentication/PrivatePinConcurrencyTest.java`; extend `PrivatePinIntegrationTest.java`
  and `AuthWebIntegrationTest.java` under `authentication/` as needed.
- SBA-007: extend `people/PersonValidationTest.java`, `PersonIntegrationTest.java` and `PeopleWebIntegrationTest.java`.

Reuse existing test support without modifying production for test hooks. Keep any additional test helper owner-local.
Before adding any other production path, return to Codex to identify and justify the minimal essential path within
the approved owners. New dependency edges, migrations, shared abstractions, ADR/public-contract changes need a new
owner decision; do not implement them under this handoff.

## Required behavior / invariants

### SBA-001 — priority and Phase 17 compatibility

1. Use a PostgreSQL transaction-scoped write lock on **the existing physical `vault_entries.id` row**.
   Lifecycle and metadata paths share this lock identity. Acquire and authoritatively refresh before evaluating
   deleted state, capability or idempotent transition, and before protected writes; a locking query alone does
   not refresh an already-managed entity.
2. Cover `moveToTrash`, `restore`, `favorite`, `unfavorite`, `setRating`, `removeRating`,
   `attachTag` and `detachTag`. Parent entry guard precedes child metadata locks/writes. The guard uses the
   caller's writable REQUIRED transaction, same datasource/physical connection, and survives service return
   until outer COMMIT/ROLLBACK. No JVM/advisory-only guard, separate lock transaction or after-commit event proof.
3. Preserve existing null/missing-entry errors, capability matrix, trash denial (`IllegalStateException`),
   metadata read behavior, first-rating/tag uniqueness and idempotent favorite/rating/tag behavior. Repeated
   trash/restore must preserve timestamps when no transition occurs. Refresh must respect earlier writes in the
   same transaction and must not discard unrelated pending changes through whole-context clearing.
4. Document the concrete SQL/lock identity, freshness mechanism, caller transaction lifetime and parent-before-child
   order in implementation evidence. This must fit P17-3 §1A: future ordered Vault guards before owner/RAG rows,
   fresh owning proofs and locks through final Java commit. **Do not add the future public source-guard API,
   Fiction/Film participation, RAG locks, projections, grants, epochs or worker fences here.**
5. Maintenance proves Vault lock compatibility, not complete RAG source safety. P17-1/2/3 design acceptance remains
   unchanged; its High-risk runtime proofs (last-check-to-commit, field authority, worker/purge/roles and NTFS)
   remain mandatory in the future separately authorized Phase 17 handoff. Its R2/R7 tests must pause after the
   final authoritative proof and before dependent COMMIT, contend with owning writes in both orders, and prove
   no stale publication/output; the maintenance tests cannot substitute for those future runtime cases.

### Remaining corrections

- **SBA-002:** derive all omitted fields from fresh locked singleton state, then validate/merge/write atomically.
  Preserve timezone/currency normalization, pagination, auto-lock, backup enable/interval coherence and initialization
  defaults. Disabling backup retains the existing interval-clearing rule; omission preserves committed values.
- **SBA-003:** keep the existing final locked gate as the smallest correction. Disabled authoritative state throws
  `InvalidFeedSourceException`; staged inserts and updates and source fetch/schedule timestamps all roll back.
  Enabled serial orders retain existing deduplication, dual-key conflicts, schedule calculations and safe errors.
  Do not introduce a scheduler, HTTP ingestion endpoint or broaden the lock over the entire batch.
- **SBA-004:** a closing marker is a whole `---` line terminated by LF/CRLF or EOF, never a prefix such as
  `---custom: retained`. Preserve opening-marker policy, raw string byte-for-byte after UTF-8 encoding,
  title fallback, summary, aliases, nested/unknown metadata, safe YAML and decimal fidelity. Keep ordinary body
  thematic breaks and existing no-frontmatter/unterminated-opening behavior; do not normalize line endings.
- **SBA-005:** current hash comparison and update serialize on the existing `app_users` singleton. Preserve ordinary
  read-only PIN verification, six-digit format, password-encoder/hash compatibility, missing-bootstrap behavior,
  authenticated-context checks, login/JWT/password/refresh semantics. No plaintext/hash logging or new PIN model.
- **SBA-006:** validate end-of-input before accepting/importing the root array. Preserve target/field canonicalization,
  decimal handling and privacy-safe `InvalidImportJobException`. Keep job serialization/rollback semantics;
  parser exceptions must not import the valid first root or persist a successful parse.
- **SBA-007:** widen operands before digit subtraction or use an equally bounded magnitude check. Avoid expansion
  to plain decimal strings or huge powers/allocations. Invalid height/weight raises `InvalidPersonException`
  before Vault/Person writes; no schema widening, rounding or invented human measurement limits.

Existing wire contracts stay fixed: Vault trash denial is HTTP 409/`VAULT_CONFLICT`; wrong current PIN is
401/`AUTH_INVALID_PIN`; invalid Person is 422/`PERSON_INVALID`; invalid import document is
422/`INVALID_IMPORT_JOB`. Preserve anonymous authorization handling, existing envelopes and safe messages.
Business exceptions stay capability-owned; DTOs/mappers/advice stay owner `internal/web`; root `ApiResponse`
and shared HTTP support remain in place under ADR-0016. No other module's entity/repository/internal access.

## Non-goals

No unrelated refactoring, static-warning cleanup, search-snippet consolidation, generic lock/settings/parser
framework, `common` module, package relocation or speculative interfaces/patterns. No Schema v1/Flyway/DBML/POM/
dependency graph changes, ADR revisions, public API expansion, frontend, deployment, RAG production code,
Python runtime, OCR/QA/recommendations, source expansion, Ideaverse integration, ingestion cap or multi-user features.
Preserve frozen Phase 0–15 acceptance, BA15/FR15 dispositions, ADR-0018 and all historical reports, including the
supplemental audit. Do not edit Phase 15 audit status or Phase 17 preparation/status/contracts to imply completion.

## Test/evidence contract

### Deterministic regression matrix

Use JUnit 5/AssertJ and real PostgreSQL Testcontainers via existing support. Concurrency tests require independent
transactions/connections, preloaded first-level caches, bounded latches/barriers/futures and observed PostgreSQL
lock waiting (`pg_locks`/`pg_blocking_pids`, tied to the fixture's competing backend). Release the holder only after
the competitor reaches that lock. Polling may wait for observed state; elapsed sleeps never establish the order.
Always release barriers/stop executors in cleanup; do not add production callbacks or mocked-lock proofs.

| Finding | Mandatory checked-in regression cases and final assertions |
| --- | --- |
| **SBA-001** | Reproduce cached-active → committed trash → metadata denial and cached-active → committed trash → restore returning/persisting active. Also cached-trashed → committed restore → trash and metadata admission. Real trash-vs-restore contention in both orders; final state matches the last serialized operation and returned views reflect each operation. Parameterize **all six** metadata operations against trash in both orders: trash first denies with unchanged child rows; metadata first commits its permitted result before trash. Seed removal targets to prove no accidental deletion; include absent/idempotent targets, capability/null/missing errors, repeated transitions with unchanged timestamps, and trash→restore / restore→trash composition in one transaction. Hold an outer transaction after a public mutation returns and prove a competing direct `FOR UPDATE`/lifecycle call remains blocked until commit. Assert final parent/child state through fresh JDBC, including no mutation/timestamp changes on denial. |
| **SBA-002** | Exact cached pagination-20 → independent pagination-50 commit → timezone-only `Asia/Bangkok` update preserves 50. Two disjoint partial updates contend in both commit orders; preserve both supplied changes and every omitted field. Exercise auto-lock and backup enable/interval vs unrelated updates, explicit backup disable, invalid-merge no-write, existing-row initialize and missing-row defaults/initialization. Fresh JDBC assertions include currency, timezone, pagination, auto-lock, backup fields and timestamps. |
| **SBA-003** | Cached enabled source, committed disable before final refresh: reject without new items, existing-item changes or fetch/schedule timestamp changes. Stage a batch containing an update plus an insert before the final gate; prove whole-batch rollback. Real final-gate-vs-disable contention: disable first causes rollback; ingestion first succeeds from enabled authority, then disable commits with bookkeeping retained. Include empty batch, enabled success and existing schedule/dual-key/unique-conflict controls. Assert source and item rows through fresh JDBC. |
| **SBA-004** | Use both exact audit fixtures (prefix-looking YAML key with retained summary; adjacent empty block followed by a body thematic break). Cover normal/empty LF and CRLF, closing marker at EOF, delimiter-prefix lookalikes, no frontmatter, unterminated opener, body thematic breaks, unknown/nested keys, malformed/non-map YAML and precise decimals. Through createJob→parse→validate preview→execute, assert complete parsed frontmatter/summary, unchanged raw Markdown/file provenance and persisted Note read via parent Knowledge public API plus fresh JDBC. Invalid items cannot create/update targets; preserve whole-job target-failure rollback controls. |
| **SBA-005** | Cached user/current-PIN proof followed by a competing committed PIN change: stale old PIN rejects with no overwrite. Two authenticated transactions using the same old PIN and distinct new PINs contend in both winner orders; exactly one succeeds, loser gets `PinVerificationException`. Each thread installs/clears its own security context. Fresh persisted hash matches winner only; old/loser PINs fail fresh verification; username/email/password hash and unrelated credentials stay unchanged. Wrong-current, invalid format, unbootstrapped and anonymous denials, HTTP error controls, bootstrap/password-range/refresh regressions stay green. |
| **SBA-006** | Exact audit JSON followed by second object or `garbage`; also second array, string/number/boolean/null root, invalid trailing syntax and malformed first root. Valid spaces/tabs/CR/LF and empty arrays remain accepted; decimals and existing target/field rules survive. Parser and createJob→parse/preview/validate/execute boundary tests prove invalid input cannot progress or write Note/other target/Vault rows; failed parse leaves prior job/items/counters/hash/status unchanged after rollback, with no successful prefix preview. HTTP 422 and content-free error/log assertions; valid-input execution and whole-job rollback controls stay green. |
| **SBA-007** | Parameterize height/weight × create/update with `1E+2147483647`, its negative counterpart and extreme negative exponent, plus null, zero/negative, `9999.99`, `10000`, accepted scientific notation (`1E+3`), excessive fractional scale and trailing-zero normalization. Domain rejects before any repository/Vault write; database create counts and existing update row/metadata stay unchanged. Send the extreme numeric token through actual MockMvc JSON binding for POST/PUT; require existing 422/`PERSON_INVALID`, no write and no expanded value in errors/logs. |

For **each** SBA ID retain at least one failing-before/fixed-after regression result on the approved baseline,
then the complete fixed matrix. Use isolated temporary outputs/checkouts if needed; never reset the owner's tree.
Disposable audit probes and the historical 1,021-test result do not replace checked-in regressions.

### Commands and delivery evidence

Run commands from `backend/`, using Java 25, Maven 3.9.x and existing PostgreSQL/MinIO test fixtures:

```powershell
mvn -ntp "-Dtest=Vault*Test,AppSettings*Test,SettingsWebIntegrationTest,Feed*Test,ImportData*Test,MarkdownImportParserTest,JsonImportParserTest,PrivatePin*Test,AuthWebIntegrationTest,AuthenticationBootstrapIntegrationTest,BootstrapValidationTest,RefreshTokenLifecycleIntegrationTest,SecurityFilterChainIntegrationTest,SecretRedactionTest,Person*Test,PeopleWebIntegrationTest" test
mvn -ntp clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report
```

Narrow focused runs by class/method while iterating; the listed focused matrix and clean full verification are final
gates. The full suite must exercise Modulith boundaries, PostgreSQL/Flyway, MinIO, security, imports, affected HTTP
and existing OpenAPI contracts. Do not skip unavailable containers/tests or remove assertions to obtain green.
Record exact commands, exit codes, tool/container versions, totals/failures/errors/skips and coverage of changed
branches; reconcile actual testcase totals and any differences from 1,021. No new coverage percentage threshold.

Required new evidence file (Antigravity creates during implementation):
`docs/implementation/maintenance/supplemental-backend-audit-2026-10-13/test-evidence.md`.
Include SBA→production path/method→test method→before/after result→fresh persisted/no-write assertion;
lock identity/order/lifetime/freshness proof; affected HTTP/error/OpenAPI compatibility; changed-file inventory;
diagnostic triage; exact verification results and remaining limits. Keep raw logs/generated reports in ignored
`logs/` or `backend/target/`; never copy credentials, hashes or raw owner content into evidence.

Apply the [29-dimension rubric](../../../../.agents/skills/codex-review-coverage/SKILL.md) in that evidence and Codex
final review: exactly 1–29, each `PASS / FINDING / N/A / NOT_VERIFIED`, with paths/tests and reasons/limits.
This handoff defines future obligations; no implementation/runtime result is certified here.

- **1–6:** trace all seven IDs, verify approved boundaries and recursively inventory every changed owner package/test
  subtree against the canonical tree; preserve NamedInterfaces, ADRs, root envelope and exception placement.
- **7–9:** inspect concrete service/repository/helper responsibilities and dependency direction. Share Vault lock/
  refresh once where needed; keep settings/PIN policies in their owners. Preserve existing Repository/Facade/Adapter
  roles; justify any helper by current reuse. No generic base service, Strategy/State hierarchy or stylistic rewrite.
- **10–17:** prove lifecycle/idempotency, wire/errors, hostile validation, auth/privacy/credential handling,
  atomicity/lock order and rollback with the matrix; preserve DB constraints/mappings and unrelated pending writes.
- **18:** inspect added query/refresh count and lock scope/contention; avoid unrelated serialization and unbounded
  numeric allocation. **23:** representative performance/p50/p95 remains NOT_VERIFIED/outside this repair;
  do not claim RAG-scale readiness or introduce a benchmark framework.
- **19, 24:** N/A to unchanged retrieval/recommendation and absent model evaluation; no new RAG corpus/model.
  **20:** Markdown/structured-metadata fidelity is mandatory; OCR/PDF/XLSX/DOCX/NTFS proof is N/A here and remains
  separately gated in Phase 17/18.
- **21–22:** adversarial/no-write fixtures, real deterministic contention and risk coverage are required; private
  production fixtures are forbidden. **25:** preserve dependency/POM pins; fresh comprehensive CVE/license/SBOM
  assessment remains NOT_VERIFIED, no vulnerability-free claim.
- **26–29:** clean build/architecture/HTTP/OpenAPI checks, targeted IDE/static diagnostics for changed code with
  warning triage, safe logging/no payload leaks, accurate evidence/local links/status and clean patch hygiene.
  No requirement to clear historical analyzer warnings or mutate tooling. Run `git diff --check` from root and
  verify no unapproved production, frozen-history, generated or cache files enter the maintenance patch.

## Constraints / risks

Relevant engineering skills for implementation/review:
`java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`,
`design-pattern-selection`, `modular-monolith-architecture`, `jpa-postgresql-persistence`,
`backend-testing`, `authentication-security`, `feed-import-workflow-modeling` and `people-domain-modeling`.
Use the smallest correction satisfying the tests: SOLID/cohesion, DRY, KISS and YAGNI are acceptance criteria,
not permission to restructure frozen modules.

Material risks are new lock order/lifetime and stale-context refresh (SBA-001/002/005), rollback of staged Feed
writes (003), delimiter/text/metadata fidelity (004), JSON completeness without field/decimal drift (006), and
bounded numeric validation (007). Preserve initialization, existing unique-conflict handling and caller composition.
Any missing required consistency/security proof or unresolved SBA finding prevents acceptance.
An essential additional path or baseline conflict must be reported for Codex/owner resolution before editing.

Only active handoff result/status and the new maintenance evidence file need documentation changes during this
implementation. Other directly affected documentation needs a specific defect-driven justification; historical
reports, owner approval, frozen baselines and Phase 17 acceptance remain unchanged. No agent commits, pushes,
tags or creates/merges PRs.

## Implementation result

- **Status:** `READY_FOR_OWNER_COMMIT` (Codex final acceptance, 2026-10-13)
- **Implementer:** Antigravity
- **Evidence artifact:** [`docs/implementation/maintenance/supplemental-backend-audit-2026-10-13/test-evidence.md`](test-evidence.md)

### Defect Implementation Summary
1. **SBA-001 (Vault Entry Lifecycle & Metadata Mutation Guard):**
   - Created `VaultEntryMutationGuard` to enforce pessimistic write locking (`LockModeType.PESSIMISTIC_WRITE`) and explicit `entityManager.refresh` on `vault_entries` rows.
   - Updated `VaultEntryService.moveToTrash` and `restore` to use `VaultEntryMutationGuard`.
   - Updated `VaultMetadataService` across all six metadata mutation methods (`favorite`, `unfavorite`, `setRating`, `removeRating`, `attachTag`, `detachTag`) to lock and refresh parent `VaultEntry` prior to child metadata writes.
   - Added `findByIdForUpdate` to `VaultEntryRepository`.
   - Added `VaultMutationConcurrencyTest` (13 tests) covering preloaded cached entries, trash-vs-restore contention in both orders, metadata mutation denial against committed trash, and outer transaction lock hold.
2. **SBA-002 (Settings Partial Update Merging):**
   - Added `findByIdForUpdate` with `PESSIMISTIC_WRITE` to `AppSettingsRepository`.
   - Updated `AppSettingsService.initializeOrUpdate` (`UpdateSettingsCommand`) to lock and refresh the singleton entity before deriving omitted fields, preserving concurrent updates to disjoint fields.
   - Added `AppSettingsConcurrencyTest` (7 tests) proving disjoint partial updates preserve omitted fields and committed changes.
3. **SBA-003 (Feed Ingestion Committed Disable Recheck):**
   - Updated `FeedItemService.ingestFetch` to recheck `sourceToUpdate.isEnabled()` immediately after locking and refreshing the source row at the final gate.
   - If disabled, raises `InvalidFeedSourceException("Feed source is disabled")`, rolling back staged items and fetch timestamps atomically.
   - Added `FeedIngestionConcurrencyTest` (5 tests) proving whole-batch rollback when disable commits prior to final gate, and asserting exact normalized fetch/schedule timestamps followed by retained item values.
4. **SBA-004 (Markdown Import Parser Delimiter Fidelity):**
   - Overhauled `MarkdownImportParser` to match whole delimiter lines terminated by LF, CRLF, or EOF, preventing prefix collision (`---custom: retained`).
   - Correctly handles adjacent empty frontmatter blocks (`---\n---\n`), EOF closing markers, and preserves exact byte-for-byte raw Markdown content.
   - Added `MarkdownImportParserTest` (17 tests) and end-to-end integration test in `ImportDataIntegrationTest`.
5. **SBA-005 (Private PIN Concurrency & Stale Credential Overwrite):**
   - Added `findByIdForUpdate` with `PESSIMISTIC_WRITE` to `AppUserRepository`.
   - Updated `PrivatePinService.changePin` to lock and refresh the user entity before verifying current PIN hash.
   - Added `PrivatePinConcurrencyTest` (12 tests) verifying that when two requests contend with the same old PIN, only one succeeds; the second sees the refreshed hash and fails with `PinVerificationException`. Fixtures reliably clean main-thread and worker security contexts.
6. **SBA-006 (JSON Import Single Document & Trailing Token Rejection):**
   - Updated `JsonImportParser` using lower-level Jackson `JsonParser` to parse the root array and assert `parser.nextToken() == null`.
   - Permits trailing whitespace while rejecting trailing tokens, objects, arrays, and syntax garbage with `InvalidImportJobException("Invalid JSON syntax in import file")`.
   - Added `JsonImportParserTest` (27 tests) and catastrophic failure / whole-job rollback integration test in `ImportDataIntegrationTest`.
   - Verified absence of synthetic log sentinels via `OutputCaptureExtension` in `ImportDataWebIntegrationTest`.
7. **SBA-007 (Person Numeric-Width Arithmetic Overflow Safety):**
   - Updated `PersonService.validatePositiveDecimal` to evaluate integer digits using `long` arithmetic, preventing 32-bit integer overflow on extreme exponents (`1E+2147483647`).
   - Rejects extreme values with `InvalidPersonException` prior to persistence.
   - Extended `PersonValidationTest` (70 tests), `PersonIntegrationTest` (18 tests), and `PeopleWebIntegrationTest` (6 tests with MockMvc HTTP 422 `PERSON_INVALID`).

### Verification Outcomes
- **Focused Regression Suite:** `mvn -ntp "-Dtest=Vault*Test,AppSettings*Test,SettingsWebIntegrationTest,Feed*Test,ImportData*Test,MarkdownImportParserTest,JsonImportParserTest,PrivatePin*Test,AuthWebIntegrationTest,AuthenticationBootstrapIntegrationTest,BootstrapValidationTest,RefreshTokenLifecycleIntegrationTest,SecurityFilterChainIntegrationTest,SecretRedactionTest,Person*Test,PeopleWebIntegrationTest" test`
  - **Result:** `BUILD SUCCESS` (417 tests, 0 failures, 0 errors, 0 skips; 01:32 min).
- **Full Verification & JaCoCo Coverage Suite:** `mvn -ntp clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report`
  - **Result:** `BUILD SUCCESS` (1,152 tests, 0 failures, 0 errors, 0 skips; 04:33 min; 811 classes analyzed; net +131 tests over 1,021 baseline).
- **Git Diff Hygiene:** `git diff --check` exited code 0 clean.
- **Graphify:** Refreshed via `scripts/refresh-graphify.ps1` (7,217 nodes, 27,125 edges, 605 communities).

### Changed File Inventory
- **Production (12 files):**
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/metadata/VaultMetadataService.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/internal/application/configuration/AppSettingsService.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/internal/infrastructure/persistence/AppSettingsRepository.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/MarkdownImportParser.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/JsonImportParser.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/privatepin/PrivatePinService.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/persistence/AppUserRepository.java`
  - `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/internal/application/PersonService.java`
- **Tests (14 files):**
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultMutationConcurrencyTest.java` (13 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultEntryIntegrationTest.java` (5 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultMetadataIntegrationTest.java` (11 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/settings/AppSettingsConcurrencyTest.java` (7 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/settings/AppSettingsValidationTest.java` (22 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedIngestionConcurrencyTest.java` (5 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/MarkdownImportParserTest.java` (17 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/JsonImportParserTest.java` (27 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/ImportDataIntegrationTest.java` (13 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/ImportDataWebIntegrationTest.java` (3 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/PrivatePinConcurrencyTest.java` (12 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/people/PersonValidationTest.java` (70 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/people/PersonIntegrationTest.java` (18 tests)
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/people/PeopleWebIntegrationTest.java` (6 tests)
- **Documentation (2 files):**
  - `docs/implementation/maintenance/supplemental-backend-audit-2026-10-13/test-evidence.md`
  - `docs/implementation/handoffs/ACTIVE.md`

### Limitations & Deferred Scope
- Scope remains strictly bounded to SBA-001 through SBA-007 and review findings FR-SBA-1 through FR-SBA-4.
- Zero database migrations or schema alterations made.
- No public REST contract changes or ADR revisions.
- Phase 16 Frontend and Phase 17 RAG production runtime remain unstarted and gated.

## Codex re-review remediation — test/evidence only (2026-10-13)

All four review findings from the formal Codex re-review (`2026-10-13-sba-001-007-final-codex-rereview.md`) are closed:

1. **FR-SBA-1 / Medium — CLOSED:**
   - In `PersonService.validatePositiveDecimal`, bounded `rawIntegerDigits > 4` with `long` arithmetic and wrapped `value.stripTrailingZeros()` in `try/catch (ArithmeticException ex)` throwing `InvalidPersonException(fieldName + " must fit numeric(6,2)")`.
   - Completed full height/weight × create/update matrix for `1E+2147483647`, `100E+2147483647`, and `1000E+2147483647` across `PersonValidationTest` (70 tests), `PersonIntegrationTest` (18 tests), and `PeopleWebIntegrationTest` (6 tests).

2. **FR-SBA-2 / Medium — CLOSED:**
   - Removed all unguarded compatibility constructors and fallback paths from `VaultEntryService`, `VaultMetadataService`, `AppSettingsService`, and `PrivatePinService`.
   - Mandatory collaborators (`VaultEntryMutationGuard`, `EntityManager`) enforced across production and test fixtures. Removed dead code and nullable-save fallbacks.

3. **FR-SBA-3 / Medium — CLOSED:**
   - `FeedIngestionConcurrencyTest`: asserted exact normalized timestamps (`last_fetched_at`, `next_fetch_at`) and all item fields in order.
   - Lock observers across all 4 concurrency suites (`FeedIngestionConcurrencyTest`, `AppSettingsConcurrencyTest`, `PrivatePinConcurrencyTest`, `VaultMutationConcurrencyTest`) bind to waiter PID, holder PID, expected table, and `pg_blocking_pids`.
   - Interrupted holder waits throw `AssertionError` instead of swallowing; executors cleanly shut down with `assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue()`.
   - `PrivatePinConcurrencyTest`: `@BeforeEach` and `@AfterEach` reliably clear `SecurityContextHolder.clearContext()`.
   - `ImportDataWebIntegrationTest`: added `@ExtendWith(OutputCaptureExtension.class)` and verified absence of synthetic log sentinels in both HTTP response body and application logs.

4. **FR-SBA-4 / Medium — CLOSED:**
   - Codex independently executed failing-before/current-green regressions on isolated baseline `a39d97241c98930a80fd5482be29326d7ed66e5c` with byte-identical checked-in test sources for all SBA-001 through SBA-007; corrected execution provenance is recorded in the current evidence and final acceptance report.
   - Corrected all method/type references (`initializeOrUpdate`, `UpdateSettingsCommand`, `parameterizeAllSixMetadataOperationsAgainstTrash`, etc.).
   - Reconciled exact XML test counts (Vault 13, Settings 7, Feed 5, PIN 12, Markdown 17, JSON 27; total 1,152 tests, 0 failures, 0 errors, 0 skips).
   - Documented full environment and container provenance and completed 29-dimension ledger in `test-evidence.md`.

Final accepted status: `READY_FOR_OWNER_COMMIT`.

## Final review

**2026-10-13 — READY FOR OWNER COMMIT.** All **SBA-001 through SBA-007** and **FR-SBA-1 through FR-SBA-4** are CLOSED. No blocking finding or owner-accepted debt remains in this maintenance slice.

Formal disposition: [final Codex acceptance](reviews/2026-10-13-sba-001-007-final-codex-acceptance.md). The first review and re-review reports are preserved; current [test evidence](test-evidence.md) records verified source semantics, selectors and receipts.

- Independent clean full verification: **1,152 tests / 0 failures / 0 errors / 0 skips**, **04:25**, finished **2026-10-13T21:19:04+07:00**; JaCoCo **811 classes**. Actual XML testcase enumeration corroborates the result. Submitted focused result: **417/0/0/0**.
- Independent baseline proof: all seven unchanged checked-in regression sources have defect-specific failures on approved commit `a39d97241c98930a80fd5482be29326d7ed66e5c` and pass on the fixed implementation. Isolated baseline production/resources match Git; the owner tree was never reset. Nested unit cases were executed using class selectors.
- Remaining test-only remediation is accepted: waiter/holder/statement-bound lock observers, exact Feed success bookkeeping/item retention, failed interrupted holders, bounded executor termination, PIN authentication cleanup and synthetic import log/body assertions. No further production change is required.
- Complete 29-dimension review, recursive 336-Java/107-package/37-test owner-tree inspection and fresh 12-file IDE diagnostics completed. IDE: 0 errors, 10 warnings, 2 weak warnings, triaged in the report. Performance and comprehensive current CVE/license/SBOM assessment remain NOT_VERIFIED.
- SBA-001 shares the existing physical Vault parent guard/freshness/commit lifetime required by accepted Phase 17 §1A. This maintenance implements no RAG feature or future Fiction/Film/NTFS/source-commit integration; separate Phase 17 runtime acceptance tests remain required.

**Next step:** owner reviews and commits/pushes the scoped maintenance, excluding local `.codex/config.toml` approvals and ignored verification outputs, then gives the latest package to ChatGPT for maintenance closeout. Only afterward proceed with the separate Phase 17 implementation handoff. Keep ACTIVE until that closeout; Codex has not committed/pushed or archived historical evidence.
