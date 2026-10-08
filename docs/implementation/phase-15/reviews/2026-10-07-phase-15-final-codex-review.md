# Phase 15 Remediation — Codex Final Review

- Date: 2026-10-07
- Verdict: **CHANGES_REQUESTED**
- Handoff: `phase-15-backend-audit-remediation`; received as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Baseline: `6a89a998c512dda27c3e494a3525bf6118ee981e`; reviewed the submitted, uncommitted working tree,
  including untracked production contracts/package descriptors and audit tests.
- Authority: [owner decisions](../owner-decisions.md), [ADR-0018](../../../adr/0018-phase-15-bounded-backend-remediation.md)
  and the [same active handoff](../../handoffs/ACTIVE.md). This is handoff-specific final review, **not** the
  comprehensive closure audit.
- Eight blocking findings: **one High, seven Medium**. The 17 original BA15 findings remain open pending acceptance
  and closure verification; no finding is accepted as debt.

Production paths below use `J/` for `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.
Test paths use `T/` for `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

## Independent verification

From `backend/`, Codex ran:

```text
mvn -ntp -l ../phase-15-final-review-verify.log clean verify
```

**BUILD SUCCESS — 962 tests, 0 failures, 0 errors, 0 skipped; 03:11 min**, finished
2026-10-07T10:01:04+07:00. This confirms the submitted suite passes, not that the required regressions exist.
The run includes `ApplicationArchitectureTests` (35), `CollectionArchitectureTests` (3), and
`KnowledgeArchitectureTests` (3). No migration SQL, DBML, module descriptor/dependency matrix or diagram change
was present in the submitted diff. Only the two authorized stale `.gitkeep` files were removed.
Review-document file-target checks passed (133 targets; anchors/external URLs not checked), `git diff --check`
passed, and the repository safety hook's 13 tests passed. Existing production/test changes remain the implementer's;
Codex changed only review/current governance documentation and ignored diagnostic artifacts.

Actual environment: Java 25.0.2; Maven 3.9.15; Spring Boot 4.1.1; Spring Modulith 2.1.1; Hibernate 7.4.5.Final;
Testcontainers 2.0.5; PostgreSQL 18.6; Docker Desktop engine 29.8.1. Boot/Modulith versions are also explicit in
`backend/pom.xml`. Compiler output reports deprecated APIs and unchecked OpenAPI operations; runtime output includes
Lombok Unsafe/Mockito dynamic-agent warnings and the failing 406 exception-handler negotiation below. No claim of
warning-free code or a new SpotBugs/PMD/CPD/coverage closure run is made.

Codex additionally used isolated, ignored probes, not production/test-suite changes:

- `backend/target/phase15-final-review/FinalReviewProbe.java`: real disposable PostgreSQL, ordinary Boot-managed
  startup and Hibernate validation, latch-controlled overlapping transactions with prior managed reads, and an
  embedded Tomcat HTTP server. Synthetic storage was confined to this probe; this is not a new S3/MinIO test.
- `backend/target/phase15-final-review/ZeroLengthProbe.java`: controller header/body boundary check; **not** a wire test.
- Classpath preparation: `mvn -ntp org.apache.maven.plugins:maven-dependency-plugin:build-classpath
  "-Dmdep.outputFile=target/phase15-final-classpath.txt"`; each probe was compiled with JDK 25 `javac -cp` and
  run with `java -cp` using this classpath and `target/classes;target/test-classes`.
- Logs retained at repository root: `phase-15-final-review-verify.log`, `phase-15-final-review-probe.log`,
  `phase-15-final-review-probe-complete.log`, `phase-15-final-review-zero-length.log` (ignored artifacts).
  The first probe stopped before its transport checks because its disposable fixture omitted USD; that partial log
  is preserved. The completed probe corrected the fixture and forced a scalar change in its stale-mutation scenario;
  both completed probes exited 0. Diagnostic output contains only synthetic data, not tokens or credentials.
- Initial audit report SHA-256 remains
  `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.

Graphify was used only for navigation, with vocabulary expansion `[media, image, controller, exception, handler]`,
BFS depth 2, budget 800. Its older graph located ImageController/root error handling; all conclusions below come
from current source, submitted tests and executable evidence, not inferred graph edges.

## Blocking findings

### FR15-1 — High — Account locking does not refresh authoritative state (BA15-13)

**Files:** `J/account/internal/application/ExternalAccountService.java:108–113,181–185`,
`J/account/internal/infrastructure/persistence/ExternalAccountRepository.java:27–29`,
`J/knowledge/study/internal/application/StudyItemService.java:309–311`.

`findByIdForUpdate` locks the database row but can return an already-managed stale Account. Neither `update` nor
`findAndLock` refreshes it. Mutation guards compare proposed values against that stale object; Study validates the
same stale view. The fallback from `findAndLock` to unlocked `findById` also weakens the synchronization contract.

Two real-PostgreSQL counterexamples were reproduced with overlapping transactions and latch-controlled prior reads:

1. T1 loads an OTHER account. T2 changes it to a YouTube channel and commits a referencing Study. T1 then changes
   username while submitting its old OTHER/platform values. The guard sees no type/platform change; Hibernate's
   whole-row update writes the stale type/platform. Result: **account type OTHER, one referencing YouTube Study**.
2. T1 loads a valid YouTube account. T2 changes the unreferenced account to OTHER and commits. T1 creates a Study;
   `findAndLock` returns the stale valid view. Result: **Study creation succeeds against an actual OTHER account**.

**Correction:** make both Account-owned synchronization paths read fresh authoritative state under the held lock
before deciding or returning a view. Remove an unlocked fallback that can bypass the lock contract; update mocks
to model the real contract. Keep the existing Account-owned SPI/Knowledge implementation direction, transactions,
schema and named interfaces. Add deterministic PostgreSQL regressions for both counterexamples and the handoff's
assignment-versus-type/platform mutation orders, including Study update. Document transaction/lock order and preflight.
If a solution needs broader architecture, obey the existing BA15-13 OWNER_DECISION_REQUIRED stop; no such expansion
is required or authorized by this review.

### FR15-2 — Medium — Feed timestamp repair still uses stale configuration and managed state (BA15-17)

**Files:** `J/feed/internal/application/FeedItemService.java:47,130–135`,
`J/feed/internal/infrastructure/persistence/FeedSourceRepository.java:29–31`,
`J/feed/internal/domain/FeedSource.java:143–145`.

The selective update protects name/configuration columns, but `nextFetch` is calculated from the earlier, unlocked
FeedSource snapshot. A concurrent configuration update can commit a new interval/disable scheduling, after which
fetch writes a next-fetch timestamp based on the old settings. The bulk JPQL update also leaves the managed source
stale and bypasses its existing `@PreUpdate` updatedAt behavior.

The PostgreSQL probe loaded a 60-minute source in T1; T2 committed interval 120; T1 ingested a fetch. Committed
result: **interval 120, next fetch delay 60 minutes**. A same-transaction public read after ingestion returned
**lastFetchedAt null**, despite the executed database update. The submitted concurrency test checks only name and
non-null lastFetchedAt, so neither defect fails it.

**Correction:** preserve scheduling coherence using authoritative current configuration at the serialization/write
boundary, preserve existing updatedAt behavior, and synchronize affected managed state without detaching unrelated
entities or adding a generic framework. Keep concurrent ingestion/resource behavior safe. Test both commit orders,
interval/enable/scheduled changes, all configuration fields, exact timestamps and read-after-write in an outer
transaction; prove PostgreSQL contention where serialization is claimed.

### FR15-3 — Medium — Unsupported Accept produces an empty 406, and OpenAPI omits 406/415 (BA15-5)

**Files:** `J/ApiExceptionHandler.java:144–149`, `J/OpenApiConfiguration.java`,
`T/audit/HttpMediaTypeNegotiationIntegrationTest.java:32–39`.

The new 406 handler returns an envelope without selecting a writable JSON representation. With `Accept:
application/xml`, its own response fails negotiation again. Both the full verification log and embedded-wire probe
show `Failure in @ExceptionHandler ...handleMediaTypeNotAcceptable`; the actual response is **406 with empty body**.
The test checks only status. Generated responses also omit 415 on `/api/v1/auth/login` POST and 406 on
`/api/v1/reference/countries` GET; the probe reports both absent.

**Correction:** return a complete canonical JSON error through a narrow negotiation-error path before commitment;
retain committed-stream abort behavior. Document applicable 415/406 error envelopes in generated OpenAPI. Assert
JSON content type, complete data/error/meta shape and safe error code, not status alone, for both MockMvc and actual
HTTP negotiation. Preserve supported JSON behavior.

### FR15-4 — Medium — Newly added OpenAPI descriptions invent domain behavior (BA15-15)

**Files:** `J/finance/internal/web/dto/CreateFinancialTransactionRequest.java:18–33`, its update counterpart,
`FinancialTransactionEntryRequest.java:14–16`; `J/collection/internal/web/dto/UpdateMusicRequest.java:12`,
`UpdateSoftwareItemRequest.java:11–15`, `UpdateShoppingItemRequest.java:23`.

New descriptions claim:

- transaction status defaults to **COMPLETED**; actual default is **POSTED**, and COMPLETED is not a status;
- INCOME/EXPENSE require a category; the owning validator permits null and checks compatibility when supplied;
- TRANSFER deltas must sum to zero; the accepted contract requires two distinct wallets with opposite signs,
  not numerical equality across potentially different currencies;
- Software type defaults to APPLICATION and includes OPERATING_SYSTEM/GAME/TOOL; type is required and its enum
  is APPLICATION/EXTENSION;
- purchasedAt defaults to the current time; PURCHASED permits null and no timestamp is invented;
- Music versions include REMIX/LIVE/INSTRUMENTAL; actual enum is ORIGINAL/COVER/PARODY/MIX.

**Correction:** change documentation only to the existing domain rules/enums. State Shopping's WISHLIST default,
nullable purchase timestamp and explicit clearing requirements accurately. Do not change validators to match these
descriptions. Add generated-schema semantic regression assertions, including defaults and full-replacement behavior.

### FR15-5 — Medium — Feed conversion DTOs still reject accepted Knowledge contracts (BA15-2)

**Files:** `J/feed/internal/web/dto/ConvertToStudyRequest.java:15,30`,
`ConvertToInformationRequest.java:9,14`, `ConvertToNoteRequest.java:9–14`.

These unchanged parallel HTTP paths still cap titles/source names/progress/imported filename at 255 while their
canonical Knowledge create operations allow the affected fields up to 500. `ConvertToNoteRequest` still requires
nonblank Markdown while the owner permits non-null blank Markdown. SavedResourceConversionService delegates the
commands directly to the public Knowledge facade, so a valid owning-domain command remains inaccessible through
conversion HTTP even though the direct Knowledge DTO was corrected. This is related validation drift in the
authorized Feed/Knowledge adapter families, not a new domain feature.

**Correction:** align these conversion DTOs and generated contracts to the same accepted owner rules, keeping
validation in Knowledge and conversion/provenance atomic. Review the affected families for equivalent alternate
adapter drift; add HTTP/schema boundaries and exact blank-content tests. Do not weaken or duplicate domain rules.

### FR15-6 — Medium — Known zero provider length falls back to incorrect metadata (BA15-8)

**File:** `J/media/internal/web/controller/ImageController.java:157–162`.

The condition `providerLength > 0` treats an actual known zero-byte object as unknown. With provider length 0 and
editable metadata size 75, the isolated header/body probe returns **Content-Length 75, actual body 0 bytes**.
Zero is a known nonnegative length, not missing metadata. The repair still permits malformed/truncated framing at
this boundary; no new zero-byte upload policy is needed to fix download framing.

**Correction:** give every known valid provider length precedence, including zero; retain metadata fallback only
for genuinely unknown length. Add the zero boundary plus actual-wire downloads after smaller/larger/null metadata
edits, exact byte equality, fallback and cleanup. Do not introduce mismatch rejection or modify metadata business rules.

### FR15-7 — Medium — Mandatory regression evidence is absent or non-deterministic (multiple BA15 IDs)

**Files:** `T/audit/`, especially `MediaBinaryFramingWireIntegrationTest`, `LocationConcurrencyIntegrationTest`,
`LifecycleWritersConcurrencyIntegrationTest`, `StudyAccountInvariantIntegrationTest`,
`FeedImportKnowledgeExceptionIntegrationTest`, `NullCollectionMemberValidationTest`, and
`docs/implementation/phase-15/test-evidence.md`.

The 42 added tests are not the handoff-required coverage. Specific gaps:

- BA15-1 has one startup test; no restart is performed. BA15-2 has no claimed WebDtoValidationAuditIntegrationTest
  or new HTTP/generated-schema family boundary suite.
- BA15-3 tests encoder matching, not bootstrap/login at long-password boundaries, outside-range rejection or
  legacy bcrypt login. Existing shorter-password tests do not prove the newly repaired HTTP credential contract.
- BA15-4 validates constructed DTOs, not HTTP 400/no-write behavior; both update Finance list variants and supported
  null/empty unknown schedules need coverage.
- BA15-6 constructs advice directly; it never performs a conversion/import, duplicate Note hash, missing reference
  or all-or-nothing target/provenance/job rollback.
- BA15-7/8's purported wire integration test starts no server, reads no HTTP response and compares no downloaded
  bytes. It directly invokes advice/controller methods. Its committed-response test merely accepts another
  ResponseEntity; that is not proof of abort/no JSON append, disconnect, timeout or resource cleanup.
- BA15-9/10/17 use barriers **before** entering services. There is no forced conflicting read/write order or
  observable PostgreSQL wait. Location lacks unknown schedule/prior managed-read cases; Journal/Personal lack
  restore and both ordered lifecycle races; Feed omits configuration/timestamp coherence. Rating lacks creation
  timestamp and forced first-insert contention assertions. Baseline failing/race evidence was not supplied.
- BA15-11 tests sequential missing/duplicate references, not removal races/concurrent assignment/privacy assertions.
- BA15-12 covers selected create-command failures but no HTTP/update boundary matrix or committed reload equality;
  `MAX_VALID` is declared but never used. BA15-13 has only sequential tests, despite the reproduced failures above.
- BA15-14 covers a 105-item job whose decisions are **all SKIP**. It proves page access, not complete real target
  execution/atomic failure above 100, 100/101 boundaries, rejected page/limit inputs or generated page contract.

**Correction:** supply the focused behavioral tests required by each handoff row, including deterministic
PostgreSQL ordering/contention and real embedded wire tests where specified. Use existing test infrastructure;
do not add generic test/locking frameworks. Preserve initial evidence before clean. Retain exact focused commands,
assertions and red/green evidence for source-derived races; if a baseline allegation cannot be reproduced, return
it to Codex for disposition instead of manufacturing locks. Then rerun `mvn -ntp clean verify`.

### FR15-8 — Medium — Submitted evidence and current docs are not trustworthy/current

**Files:** `docs/implementation/phase-15/test-evidence.md`, ACTIVE's Implementation result, Phase 15 current-status
documents, and affected canonical runtime/API/security/import documentation.

The evidence reports Maven 3.9.9, Boot 3.4.3, Modulith 1.3.3 and PostgreSQL 17, unlike the actual build. It lists
nonexistent suites and 54 audit tests, while the submitted tree contains these **13 suites/42 tests**:

| Actual audit suite | Tests |
| --- | ---: |
| FeedImportKnowledgeExceptionIntegrationTest | 6 |
| FlywayStartupIntegrationTest | 1 |
| HttpMediaTypeNegotiationIntegrationTest | 3 |
| ImportJobPaginationIntegrationTest | 1 |
| LifecycleWritersConcurrencyIntegrationTest | 3 |
| LocationCategoryAssignmentIntegrationTest | 3 |
| LocationConcurrencyIntegrationTest | 1 |
| MediaBinaryFramingWireIntegrationTest | 4 |
| NullCollectionMemberValidationTest | 5 |
| PasswordRangeIntegrationTest | 5 |
| PriceNumericBoundsIntegrationTest | 5 |
| StudyAccountInvariantIntegrationTest | 4 |
| VaultRatingFirstSetConcurrencyIntegrationTest | 1 |
| **Total** | **42** |

It describes nonexistent `KnowledgeValidationException`/`KnowledgeResourceNotFoundException` and
`NumericBoundsValidator`, incorrect `vault_ratings`/`vault_item_id`/`saveUpsert` identifiers, account deletion guards
not implemented by this SPI, wire tests/rollback/restart tests not present, and full BA15 closure unsupported by
the findings above. The actual native upsert is on `ratings(vault_entry_id)` and exception types are public
`InvalidKnowledgeItemException`, `KnowledgeConflictException`, `KnowledgeNotFoundException`.
Current summaries still said READY_FOR_IMPLEMENTATION/no implementation started despite the submitted changes;
this review synchronizes those status summaries. That does not substitute for implementer evidence correction.

**Correction:** replace unsupported implementation-evidence claims with actual symbols, suite counts, commands,
versions and results; distinguish unit/header probes from HTTP/wire/concurrency tests. Record BA15-13 narrow
coordination, transaction/lock order and preflight accurately, without inventing earlier approval. Synchronize
the directly affected canonical runtime/security/API/import contracts with actual repaired behavior. Preserve
historical reviews/initial audit evidence. Do not call all findings repaired/closed until their gates pass.

## Review disposition and remaining risks

Business correctness, transactions, alternate validation paths, HTTP framing, generated contracts and evidence have
blocking findings above. The domain-owned price checks, narrow rating upsert, module-local lifecycle guards and
Account-owned synchronous SPI are proportionate mechanisms; no generic framework/refactor or new module dependency
was found or requested. Scope, package ownership and unchanged schema were inspected; architecture suites passed.
Password encoding uses a standard full-input Spring Security primitive with bcrypt verification support; no custom
crypto, payload logging or secret-bearing DTO addition was found. Performance review considered row-lock scope,
fetch concurrency, bounded import pages, lack of new global lists and provider-based streaming. It does not imply
load-test/penetration/CVE/IDE or full static closure assurance.

BA15-1/3/4/6/9/10/11/12/14/16 contain plausible scoped repairs but are not finally closed by this review.
BA15-2/5/8/13/15/17 have concrete residual defects. BA15-7's small/large first-read failure was independently checked
on embedded HTTP: both returned complete 117-byte canonical JSON with no stale Content-Length and no-store caching;
post-commit/disconnect/timeout and byte-integrity acceptance coverage still must be demonstrated as specified.

No owner expansion is requested: FR15-1–FR15-8 stay within the already approved remediation and evidence/docs
obligations. Existing BA15-13 architectural and BA15-14 total-limit stops remain in force.

**Next step:** Antigravity runs `/antigravity-implement-handoff` against the **same** CHANGES_REQUESTED handoff,
repairs FR15-1–FR15-8, supplies accurate evidence and returns for `$codex-final-review`. Do not commit/push.
Only after final acceptance may the separate closure `$codex-backend-audit` run; owner implementation commit
requires `BACKEND_AUDIT_READY`. Phase 15 is not complete/frozen; Phase 16/17 preparation stays deferred.
