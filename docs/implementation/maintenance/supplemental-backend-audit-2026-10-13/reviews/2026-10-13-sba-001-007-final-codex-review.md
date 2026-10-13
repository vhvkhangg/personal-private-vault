# SBA-001–007 maintenance — Codex final review, 2026-10-13

**Status: CHANGES_REQUESTED.** Four Medium review findings remain. The seven SBA findings are not finally closed.
Independent clean verification passed **1,138 tests / 0 failures / 0 errors / 0 skips**, but a separate synthetic
domain/HTTP probe reproduced a residual SBA-007 validation failure, and mandatory regression/evidence obligations
are incomplete. No commit message is provided.

## Scope, authority and baseline

Implementation-mode review of [ACTIVE](../../../handoffs/ACTIVE.md), submitted as
`IMPLEMENTED_AWAITING_CODEX_REVIEW`, against the [approved scope](../README.md),
[binding owner decision](../owner-approval.md) and [submitted evidence](../test-evidence.md).
Branch `main`; HEAD `a39d97241c98930a80fd5482be29326d7ed66e5c`. Reviewed 12 production files (including one new
Vault helper), 10 test files (six new), the active handoff and evidence. Existing/tool-generated
`.codex/config.toml` approval additions are separate from this maintenance.

Applied `codex-final-review`, all 29 dimensions of `codex-review-coverage`, Java/Spring, SOLID, reuse, pattern
selection, modular architecture, persistence, backend-testing, authentication-security, Feed/Import and People
domain guidance. Graphify queried the five new guard/concurrency symbols first; its 900-token output was truncated.
Canonical source, actual trees, tests and executed checks determine this review, not graph conclusions.

Owner approval permits these repairs in frozen Phases 1/2/3/10 and existing HTTP exposure. No migration, POM,
public contract, NamedInterface/module edge, accepted ADR, historical Phase 15 evidence or Phase 17 preparation
was changed by the implementation. Codex made no production/test edits, commit, push, tag or PR.

## Blocking findings

### FR-SBA-1 / Medium — SBA-007 normalization still escapes domain validation

**Location:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/internal/application/PersonService.java:219–223`;
existing new boundary tests in `people/PersonValidationTest.java`, `PersonIntegrationTest.java` and
`PeopleWebIntegrationTest.java:94–137`.

Widening digit subtraction fixes `1E+2147483647`, but `stripTrailingZeros()` executes first. Valid BigDecimal/JSON
tokens `100E+2147483647` and `1000E+2147483647` cause scale underflow during normalization and throw
`ArithmeticException`, before the intended `InvalidPersonException` check.

**Executed proof:** a disposable Java source-launch probe called the actual `PersonService.create` and standalone
MockMvc with the actual Person controller, DTO binding/mapping, People advice and root exception handler.
Both tokens returned **500/INTERNAL_ERROR for height and weight on POST and PUT**; the direct domain call threw
`ArithmeticException`. Controls `1E+2147483647` and `10E+2147483647` returned
`InvalidPersonException` / **422/PERSON_INVALID**. Probe dependencies rejected any repository/cross-owner call;
observed call count was zero. This proves the validation/transport defect without sending extreme values to JDBC;
standalone MockMvc does not claim a full security-filter or database exercise.

**Consequence:** an invalid measurement still produces an unexpected server error instead of the frozen domain/
wire rejection. This is an incomplete SBA-007 correction, not a new feature or an eighth supplemental audit scope.

**Required correction:** bound/handle normalization overflow in the owning validator and retain the existing
positive-value and numeric(6,2) rules. Use bounded arithmetic or narrowly translate the normalization failure to
`InvalidPersonException`; do not expand huge decimals, round values, widen schema or invent new business limits.
Check in height/weight × create/update regressions for these trailing-zero/exponent combinations, including actual
HTTP 422/error privacy and fresh database/no-write assertions, then retain ordinary scientific/scale controls.

### FR-SBA-2 / Medium — Guard enforcement has alternate unguarded construction paths

**Locations:** `vault/internal/application/entry/VaultEntryService.java:40–47,100–105`;
`vault/internal/application/metadata/VaultMetadataService.java:75–109,261–267`;
`settings/internal/application/configuration/AppSettingsService.java:35–50,76–79`;
`authentication/internal/application/privatepin/PrivatePinService.java:35–52,83–89`.
Paths are under `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

New compatibility constructors set the Vault guard to null; mutations then fall back to the original unlocked
`findById`. Settings and PIN construction makes EntityManager optional and skips authoritative refresh when
null. Existing fixed-clock Vault integration tests actually construct services through the unguarded overloads
(`VaultEntryIntegrationTest.java:76–111`, `VaultMetadataIntegrationTest.java:111–126`).
The Settings validation fixture uses the old three-argument constructor as well.

**Consequence:** production classes now contain two mutation policies, and some passing tests execute the old
unguarded policy. This violates the handoff's canonical enforcement/freshness requirement and weakens substitution
and test fidelity. The Spring-selected Vault constructor supplies its required guard; no current Spring missing-
injection exploit or JWT bypass is claimed.

**Required correction:** make guard/refresh collaborators mandatory for mutation-capable service instances.
Update the existing owner-local fixed-clock/mock fixtures to supply the real guard or appropriate isolated
collaborators and stub the actual locked repository lookup. Remove silent fallback branches and unnecessary
constructor modes; preserve public capability interfaces, clocks and behavior. Do not add a generic lock framework.
The new `VaultEntryRepository.findByIdForUpdate` has no production caller: remove that introduced dead path unless
the canonical guard uses it. Remove the new Settings nullable-save fallback, which contradicts JpaRepository's
non-null save contract and creates IDE warnings, while correcting the fixture assumptions.

### FR-SBA-3 / Medium — Required contention/negative proofs are incomplete or weak

**Locations and exact gaps:**

- **SBA-001:** `vault/VaultMutationConcurrencyTest.java:257–345` tests the six metadata operations sequentially in
  both orders, with no competing transactions/lock barrier. Thus it does not prove the mandatory metadata-vs-trash
  commit fence for each operation. Its metadata-first branch asserts only final trash state, not the metadata
  result. The cached-trashed test at 133–159 tests attachment after restore, but never the required stale-trashed
  → committed restore → trash transition. Threaded lifecycle tests discard the returned views.
- **SBA-001/002/003/005:** lock observers at `VaultMutationConcurrencyTest.java:59–77`,
  `AppSettingsConcurrencyTest.java:69–87`, `FeedIngestionConcurrencyTest.java:76–94` and
  `PrivatePinConcurrencyTest.java:94–112` accept **any** waiting lock in the database. They do not bind the wait to
  the competing fixture backend, blocker and intended parent row/gate. Several holder `await(5,...)` calls ignore
  their boolean result and can commit on timeout without explicit release; cleanup does not consistently release
  barriers and await worker shutdown.
- **SBA-002:** the two contending orders assert only timezone/pagination (159–162,205–208).
  Backup/auto-lock preservation cases (215–263) are sequential. Supply the required authoritative omitted-field
  assertions and committed competing backup/auto-lock variants; preserve initialization/default controls.
- **SBA-003:** `FeedIngestionConcurrencyTest.java:215–263` labels ingestion-first as contention but runs ingestion
  and disable sequentially. The disable-first fixture holds `FOR UPDATE` before ingestion (139–175), so an insert
  may wait on an FK key-share check before the final source gate; the global observer cannot establish which
  statement waited. At 209, comparing JDBC Timestamp with Instant using `isNotEqualTo` does not prove unchanged
  fetch time, and `next_fetch_at` is not checked. The cached-enabled/independently committed-disable reproduction
  is also absent.
- **SBA-004/006:** the new import integration cases cover one prefix-looking Markdown note and one trailing-object
  JSON parse. Add the required empty-frontmatter/thematic-break persistence path and positive unknown/nested
  metadata fidelity; cover invalid documents through the actual HTTP boundary, unsuccessful preview/validate/
  execute progression, no target/Vault writes and unchanged job/item/counter state. Keep existing decimal,
  malformed-YAML and whole-job target-failure controls. Existing malformed JSON tests already check safe messages;
  cite them, and add the trailing-document HTTP/log sentinel assertions required by this handoff.
- **SBA-005:** capture fresh persisted PIN and **exact unchanged** unrelated credential fields for both winner
  orders and the stale-user case. The first order verifies that the old password still matches, which does not
  prove its hash was unchanged; the reversed order has no credential snapshot assertion.
- **SBA-007:** unit cases cover both fields/create/update, but the new database/HTTP assertions only sample
  height-create and weight-update and omit some mandated no-write/unchanged metadata checks. Complete the matrix
  with FR-SBA-1 and assert collaborator non-interaction where unit isolation adds evidence.

Paths above are under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

**Consequence:** green tests do not yet establish several owner-mandated concurrency and no-write contracts.
**Required correction:** implement the missing cases within the existing permitted owner test boundaries. Observe
the actual competing backend/statement/blocker using bounded latches and PostgreSQL state, assert holder waits
succeed, fail/roll back on timeout and clean up deterministically. For Feed, ensure the batch is staged and the
observed wait is the **final source guard**, not an FK insert; a test-owned non-key-update source lock or another
proved coordination fixture may establish this without production callbacks. Compare normalized timestamp values
to captured baselines, including both fetch/schedule timestamps and existing item fields. Keep all six Vault
metadata operations genuinely contending in both orders, with fresh child/parent assertions.

### FR-SBA-4 / Medium — Evidence overstates or misidentifies its proof

**Locations:** `test-evidence.md:56–64,71–110,153–167,178–219` and ACTIVE's supplied implementation result.

The evidence describes Vault/Feed IDs as UUIDs and singleton IDs as a UUID literal. Actual V1 keys are bigint/
Long for Vault/Feed and smallint/Short with ID 1 for Settings/User. It names a nonexistent
`AppSettingsService.updateSettings` instead of `initializeOrUpdate`, attributes the Vault guard to a repository
method it does not call, and references numerous nonexistent test methods (for example
`holdOuterTransactionCompetitorBlocksUntilCommit` versus actual
`outerTransactionHoldingAfterMutationReturnsBlocksCompetitor`). SBA-007's original Low severity is inconsistently
written as Medium/Low.

Before/after cells repeat defect narratives but provide no recorded failing-before execution of checked-in
regressions against the approved baseline. Tool/container versions are missing. HTTP/error descriptions conflate
the domain parse message with the existing content-free adapter message. The claim of zero new SpotBugs/PMD/
Checkstyle regressions has no executed comparative evidence; clean Maven verify does not run those analyzers.
PASS rows claiming all mandatory race/fidelity cases are covered contradict FR-SBA-3.

**Required correction:** replace invented identifiers/method names with exact source/test references; describe the
actual EntityManager lock/refresh path and physical types. Retain at least one genuine failing-before/fixed-after
checked-in regression receipt per SBA ID in isolated temporary outputs/checkouts without resetting the owner's
tree. Record commands, environment/container versions, exits/totals and final persisted assertions. Disposition all
29 dimensions honestly, distinguishing measured results from source inspection, N/A and NOT_VERIFIED.
Remove unsupported analyzer/HTTP claims or provide the actual evidence; there is no blanket warning cleanup
requirement. Preserve the supplied/historical audit reports and immutable SBA IDs.

## SBA disposition and Phase 17 compatibility

| SBA | Implementation assessment | Final disposition |
| --- | --- | --- |
| 001 | Spring-managed lifecycle/all six metadata paths use the same existing Vault row and targeted refresh; lifecycle composition/outer-commit tests pass. Parent-before-child order is visible in source. | OPEN pending FR-SBA-2/3/4. |
| 002 | Existing-row lookup locks and refreshes before omitted-field merge; cached pagination and two disjoint-order tests pass; missing-row path remains. | OPEN pending FR-SBA-2/3/4. |
| 003 | Final locked refresh now rechecks enabled and throws the existing unchecked error, preserving whole-transaction rollback. | OPEN pending FR-SBA-3/4. |
| 004 | Complete-line matching fixes prefix-looking keys and adjacent empty blocks; raw text, safe YAML and existing decimal controls survive. | OPEN pending FR-SBA-3/4 evidence completion. |
| 005 | Spring-managed change locks/refetches before comparing PIN and retains the lock through outer commit; stale-user and both winner orders pass. | OPEN pending FR-SBA-2/3/4. |
| 006 | Parser checks end-of-input, rejects additional roots/garbage and retains whitespace/decimal/field rules; parse rollback case passes. | OPEN pending FR-SBA-3/4 evidence completion. |
| 007 | Original int subtraction overflow is repaired; trailing-zero normalization still raises the wrong domain/HTTP failure. | OPEN pending FR-SBA-1/3/4. |

The Spring-managed Vault helper's `EntityManager.find(..., PESSIMISTIC_WRITE)` plus refresh locks the same
`vault_entries.id` physical row selected by accepted P17-3 §1A. Current owning operations participate before their
writes and hold locks in the caller transaction; no `vault -> rag` edge or competing infrastructure was added.
FR-SBA-2/3 must close before accepting maintenance's compatibility proof. The future public guard, Fiction/Film
participation, sorted multi-source/RAG locks, R2/R7 final-proof-to-COMMIT races, permissions, purge/worker fences and
NTFS behavior remain separately gated Phase 17 runtime obligations. Preparation acceptance is unchanged;
maintenance does not certify any RAG runtime.

## Executed verification and limits

Commands ran from `backend/` unless noted:

- `mvn -ntp -l ../logs/sba-final-review-2026-10-13-verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report`:
  exit 0, BUILD SUCCESS, **1,138/0/0/0**, 04:24, finished 2026-10-13 18:50:16 +07; JaCoCo analyzed 811 classes.
  Independent XML enumeration corroborated 1,138 testcase elements. Modulith, PostgreSQL, MinIO, auth/wire/OpenAPI,
  import rollback and existing audit tests ran. The submitted focused 403-test receipt was reviewed; a second
  separate focused run was unnecessary after the independent full run executed all those tests.
- `mvn -version`: Maven 3.9.15, Oracle OpenJDK 25.0.2, Windows 11 amd64, UTF-8.
  Test logs confirm `postgres:18.6-alpine` and `minio/minio:RELEASE.2024-11-07T00-52-28Z`.
- Root diagnostic launch:
  `& 'C:/Program Files/Java/jdk-25.0.2/bin/java.exe' '@logs/sba-final-review-java-args.txt'`: exit 0,
  the source `logs/NumericBoundaryReview.java` and Surefire-derived classpath produced FR-SBA-1's results.
  Both files are ignored, disposable evidence; the material observations are retained above.
- IntelliJ `lint_files` requested all 12 changed production files: zero errors; **11 warnings and 2 weak warnings**,
  no reported timeout/incomplete flag. New nullable-save branches are always-true warnings (Settings 119/162);
  the added legacy metadata constructor exposes package-private TagCreator (81). Remaining warnings include
  existing validation-only ZoneId call, unused imports/variable/methods, duplicate tag argument checks, explicit
  type argument and redundant numeric cast. These do not authorize unrelated cleanup.
- Build warnings include Lombok Unsafe, deprecated test-only MappingJackson2HttpMessageConverter and dynamic
  Byte Buddy agent loading. No blanket suppressions added. No executed comparative SpotBugs/PMD/Checkstyle,
  vulnerability/license/SBOM or representative performance result is asserted.
- Recursive filesystem/source inventory covered **336 production Java files in 107 packages and 37 test Java
  files** across Vault, Settings, Authentication, Feed, ImportData and People. All production package/path
  declarations match, all meaningful production packages have descriptors, no stale .gitkeep, and no cross-owner
  internal import was found. Inventory retained under ignored `logs/sba-final-review-package-inventory.txt`.
  Actual grouping/public descriptors/root HTTP types match the canonical package-tree conventions and ADR-0016.
  New helper is in an existing package; no frozen package-tree rewrite is required. This is full subtree inventory,
  not a claim of a new method-level audit of every unchanged class.
- Read-only source/test diffs, targeted caller/coverage searches and Git baseline/diff checks executed.
  Post-recording checks passed: active handoff/review local targets resolve, zero trailing whitespace,
  `git diff --check` exits 0, and the review ledger contains exactly dimensions 1–29. Final frozen-scope diff
  is empty for ADRs/architecture/DBML/package tree/POM/migrations/Phase 15 history/Phase 17 preparation.

## Complete 29-dimension evidence ledger

PASS is bounded to the evidence named here. FINDING refers to the four review IDs, not new supplemental scope.
NOT_VERIFIED is not PASS. RAG runtime/model/native-file subchecks remain out of this maintenance.

| # | Dimension | Status | Evidence / rationale and limits |
| ---: | --- | --- | --- |
| 1 | Owner intent / approved behavior / non-goals | PASS | Approved seven-ID scope; 12 production/10 test changes remain in permitted owners; no feature/ADR/schema expansion. |
| 2 | Requirement traceability | FINDING | FR-SBA-3/4: required cases and red/green receipts are incomplete; every SBA explicitly dispositioned above. |
| 3 | Architecture / ADR / diagrams / workflow | PASS | No frozen architecture/schema/history diff; accepted §1A guard identity retained, no RAG implementation inferred. |
| 4 | Module ownership / NamedInterfaces / data flow | PASS | Recursive descriptors/import inventory and independent Modulith checks; helper crosses capabilities only inside Vault. |
| 5 | Actual package/folder structure | PASS | Full 336/107/37 inventory; correct command/view/exception/enum/internal grouping, descriptors, casing and root HTTP boundary. |
| 6 | Shared HTTP / exception placement | PASS | Root ApiResponse family and existing owner advice unchanged under ADR-0016; no new common module or competing envelope. |
| 7 | SOLID / cohesion / substitution / dependency direction | FINDING | Shared Vault guard has a real single responsibility, but optional guard/refresh constructor modes break consistent substitution (FR-SBA-2). |
| 8 | DRY / KISS / YAGNI / state semantics | FINDING | FR-SBA-2 duplicates guarded/unguarded mutation policy and introduces unused lookup/null-save accommodations. |
| 9 | Patterns / anti-patterns | PASS | Existing Repository/Facade/adapter roles retained; one concrete Vault invariant collaborator, no generic hierarchy/events/framework. |
| 10 | Domain states / idempotency / correctness | FINDING | Original counterexample fixes and lifecycle composition pass; FR-SBA-1/3 block complete domain/error and state-matrix proof. |
| 11 | REST / OpenAPI / compatibility | FINDING | Full existing contract/OpenAPI tests pass, but actual numeric boundary probe gives 500 instead of 422; trailing-import negative HTTP proof missing. |
| 12 | Input / format / bounds / overflow | FINDING | JSON 27 and Markdown 17 parser cases pass; FR-SBA-1 normalization underflow still escapes owning rejection. |
| 13 | Authentication / authorization / privacy | FINDING | Auth/bootstrap/JWT/refresh/wrong-PIN HTTP tests pass; PIN stale/winner tests pass, mandatory refresh/deterministic credential proofs incomplete (2/3). No auth bypass claimed. |
| 14 | Credentials / logs / retention / egress | PASS | Changed code introduces no credential/payload logging; synthetic fixtures, existing redaction tests and content-free advice preserved. No new egress/retention mechanism. |
| 15 | Atomicity / isolation / lock identity/order/lifetime | FINDING | Vault physical guard and lifecycle lifetime observed; FR-SBA-2/3 leave alternate paths and required PID-bound metadata/Feed contention proof unresolved. |
| 16 | Failure / recovery / rollback | FINDING | Existing import/MinIO/compensation tests pass; Feed rollback bookkeeping proof is weak/type-mismatched (FR-SBA-3). Host-kill drills not claimed. |
| 17 | PostgreSQL / Flyway / JPA / schema | PASS | Clean migrated PostgreSQL verification; no entity/schema/migration changes; actual bigint/smallint keys confirmed. Validator/error gap is dimension 12. |
| 18 | Complexity / resources / lock scope | PASS | Added indexed singleton/entry guards are bounded and owner-local; Feed retains final gate; parser scans are linear, digit arithmetic bounded. No memory/latency guarantee. |
| 19 | Retrieval / index / recommendation quality | N/A | No search/ranking/recommendation change or RAG corpus/index implementation in this scope. |
| 20 | Document / multilingual fidelity | FINDING | Exact raw Markdown and decimals pass; empty-block persistence and nested/unknown-field evidence incomplete (FR-SBA-3). OCR/PDF/XLSX/DOCX/NTFS subchecks are out of scope. |
| 21 | Adversarial / security / race tests | FINDING | Original hostile fixtures pass; FR-SBA-1 probe fails contract; FR-SBA-3 needs actual fixture-specific gates/negative import boundaries. |
| 22 | Test quality / determinism / coverage of risk | FINDING | Independent 1,138 green cases do not replace missing contention, typed timestamps, unchanged-field and red/green proof (FR-SBA-3/4). |
| 23 | Representative performance benchmarks | NOT_VERIFIED | No p50/p95, production-scale memory/disk or RAG-scale benchmark; outside this repair and no readiness claim. |
| 24 | Model evaluation / holdout / calibration | N/A | No ML/OCR/judge/recommender capability implemented or changed. |
| 25 | Dependencies / licenses / vulnerabilities / SBOM | NOT_VERIFIED | Pins/POM unchanged and build compatible; no fresh comprehensive advisory/license/SBOM scan. |
| 26 | Build / static tooling / architecture checks | PASS | Independent clean verify/JaCoCo and 12-file IDE inspection executed, warnings explicitly triaged; no blanket analyzer-clean or hosted-CI claim. |
| 27 | Logging / observability | PASS | Existing safe severities/messages retained; numeric unexpected-error probe logs exception class only, no input payload. |
| 28 | Documentation / governance / reproducibility | FINDING | FR-SBA-4 wrong physical identities, dead/nonexistent source/test references, unsupported static/coverage claims and absent baseline receipts. Historical evidence unchanged. |
| 29 | Hygiene / delivery / artifacts | PASS | Scoped source/test patch; no generated/cache artifacts in maintenance delivery; ignored diagnostics separate; tool approvals excluded. Final diff/link check below. |

## Required next action

Antigravity must remediate **FR-SBA-1 through FR-SBA-4 under the same active handoff**, without broadening the seven-ID
maintenance scope. Re-run affected focused tests and the handoff's clean full verification after production/test
changes; update evidence accurately and return `IMPLEMENTED_AWAITING_CODEX_REVIEW` for another
`$codex-final-review`. Do not commit/push or begin Phase 17 implementation.
