# P18-S1-IMPORT-SNAPSHOT — independent Codex final review

**Decision: CHANGES_REQUESTED — Slice 1 only.**  
**Date:** 2026-10-19. **Reviewer:** independent Codex, review-only.  
**Inspected HEAD:** `edac5f466935fdd29698acbe89d4f129efce1701`, branch `main`, plus the submitted uncommitted diff and untracked implementation/tests/migration/handback. No production code, test or migration was edited by this review. No commit/push/tag/PR was performed.

## Authority and limits

Reviewed against [ACTIVE](../../handoffs/ACTIVE.md), the [Slice 1 handoff](../slice-1-import-snapshot-handoff.md), [D12/D13](../owner-decisions.md), and [ADR-0023](../../../adr/0023-phase-18-application-level-snapshot-worker-trust.md). The entry status was `IMPLEMENTED_AWAITING_CODEX_REVIEW`, satisfying the implementation review gate. D12/D13 supersede conflicting older stopped/native-security instructions for this bounded slice.

ADR-0023's accepted same-Windows-user risk applies. This review does **not** demand Restricted Token, LPAC, private DACL, hostile writer/handle isolation, native root-to-stream ABA, or universal power-loss durability proofs. Ordinary application concurrency, stale consent, file/DB failure and detectable integrity errors remain in scope. All findings below concern those retained requirements.

FR18-3 remains **OPEN**. External indexing remains **INACTIVE**, live-original mode **DISABLED / DEFERRED**, Phase 17 **COMPLETE — FROZEN**, and P18-FINAL **NOT VERIFIED / NOT ACCEPTED**. No historical Phase 18 finding or G1–G4 gate is closed. This is not Phase 18 acceptance or Slice 2 authorization.

## Independent verification

Executed from repository root on Windows 11 amd64, OpenJDK 25.0.2, Maven 3.9.15, Python 3.11.7 for the watchdog, Docker Desktop 29.8.1, Testcontainers 2.0.5 and disposable PostgreSQL 18.6 image `ppv-rag-postgres:18.6-pgvector0.8.6`:

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 1200 --receipt logs/phase18/s1-codex-review-focused-2026-10-19.json -- mvn.cmd -ntp -f backend/pom.xml '-Dtest=RagOriginalBytesStoreTest,RagWindowsAcquisitionTest,RagSnapshotAcquisitionServiceTest,RagSlice1SnapshotIntegrationTest,RagSourceReservationsIntegrationTest,RagLifecycleIntegrationTest,RagSourceAuthorityIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApplicationArchitectureTests' test
```

**PASS: 130 tests, 0 failures, 0 errors, 0 skips; BUILD SUCCESS; wrapper exit 0, 109.031 seconds, no timeout or process cleanup required.** Receipt: `logs/phase18/s1-codex-review-focused-2026-10-19.json` (gitignored). UTC interval: `15:38:36.287`–`15:40:25.306`. Maven validated/migrated through V20 against real PostgreSQL; Hibernate initialization succeeded.

| Test class | Passed |
| --- | ---: |
| RagOriginalBytesStoreTest | 14 |
| RagWindowsAcquisitionTest | 14 |
| RagSnapshotAcquisitionServiceTest | 9 |
| RagSlice1SnapshotIntegrationTest | 11 |
| RagSourceReservationsIntegrationTest | 16 |
| RagLifecycleIntegrationTest | 19 |
| RagSourceAuthorityIntegrationTest | 9 |
| OpenApiRouteInventoryIntegrationTest | 1 (228 operations) |
| ApplicationArchitectureTests | 37 |

Surefire evidence is under `backend/target/surefire-reports/` using these exact class names. Do not confuse older suffixed historical reports in that directory with this invocation. `git diff --check` passed; Git reported CRLF-to-LF notices for two Java files. Maven reused compiled classes; no clean compiler-warning census, IDE inspection, full Java suite, Python suite, benchmark or P18-FINAL was executed. Observed warnings include Mockito dynamic-agent attachment/CDS and SpringDoc development endpoints; these do not prove a new slice defect or a warning-free repository.

All findings below are established by inspecting reachable source paths and their transaction/data dependencies. The failing interleavings and failure-injection cases described below were **not** added as tests or dynamically reproduced in this review. Passing submitted tests does not exercise them.

## Credited implementation

- Reuses `RagOriginalBytesStore` for streaming SHA-256, byte limits, temp verification, manifest validation, native Windows no-overwrite sealing, integrity reads and deletion reports. `WindowsNativeFileGuard` is unchanged; its old adversarial security scope is not an acceptance requirement here.
- Reuses RAG-owned external UUID authority/table, `TransactionTemplate`, source graph/guards, module facade, DTO mapping, root `ApiResponse` and module-local exception advice. No second worker, queue, module or universal transaction coordinator was introduced.
- V20 is forward-only and RAG-owned; V1–V19 and unrelated frozen schema/code are unchanged in the submitted diff. It adds selected/pending revision metadata and sequence usage for `rag_runtime`, without granting worker publication authority.
- Successful new/changed captures set pending metadata. No caller of `setSelectedRevision` or `promotePendingToSelected` exists in production. Inspected Slice 1 paths do not advance external sourceVersion, select R or publish G. The unused promotion method itself is not proof of an early-promotion defect.
- `RagSourceAuthority.prepareSources` still rejects external files before worker/index admission. No parser/OCR/model/Python/worker-publication change appears in this diff. Domain allowlists, source projections and v2 contracts are preserved in the inspected code and focused compatibility tests.
- No public finalized `NO_CHANGE` outcome was added. However, the internal equal-byte candidate handling has the integrity defect described in S1-04.

## Blocking findings

Paths abbreviated below use the base `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/`: **application/** = `internal/application/`, **storage/** = `internal/infrastructure/storage/`, **domain/** = `internal/domain/`. Line numbers refer to the submitted worktree.

### P18-S1-01 — High — pending revision is not bound to the owner's expected consent/CAS

**Evidence:** `application/RagSourceService.java:179–253, 324–363`; `rag/api/RagConsentCommand.java:9–10`; `internal/web/dto/RegisterFileSourceRequest.java:5–8`; `domain/RagExternalFileSource.java:111–129`.

The capture command accepts only source ID and path. It does not accept/compare the expected selected R, source/reset/authorization/policy state or pending intake identity captured by the owner's intent. `expectedSelectedRevisionId` and `expectedSourceVersion` are filled from the latest server state after copying; their presence is metadata, not an enforced request CAS. Pending metadata contains no binding to the relevant consent/reset/authorization context.

There is a deterministic stale-preview path: register pending A and retain its approval command; capture changed pending B; approve using the old command. Capture changes none of the four versions in that command. Approval verifies whichever pending revision exists when approval starts and accepts the old command for B. The caller cannot specify the revision/digest it approved. A second race exists after verification: pending A can be replaced by B between lines 336 and 340; the transaction never compares current pending identity/hash/length with the verified tuple before approving with A's hash.

**Consequence:** stale owner intent can approve different bytes, or approval metadata can describe a revision different from the pending revision. This is a present consent defect even though external indexing is correctly disabled.

**Smallest correction:** bind the finite capture/approval intent to an explicit pending revision/intake identity and expected source/selection/reset/consent context; verify that same tuple under the existing ordered guards at final metadata commit. Preserve sourceVersion and selected-R/G until future C_P. Do not replace stale owner expectations with server-current values.

**Required proof:** A-preview → B-capture → stale-A-approval rejection; deterministic replacement between integrity verification and guarded approval; stale selected/reset/authorization expectations during capture. Include the authenticated API contract, not only a service command with `999` as sourceVersion.

**Contract impact:** implements Slice 1 MUST 1/3/4/5 and ADR-0023 retained source/CAS rules; no new owner architecture decision or frozen Phase 17 change is needed.

### P18-S1-02 — High — re-registration bypasses the guarded pending-update path

**Evidence:** `application/RagSourceService.java:79–175`, compared with `202–253` and `340–364`; `V18__rag_document_indexing.sql:26–80`; `domain/RagExternalFileSource.java` has no optimistic `@Version` field.

Registering a path already present in `external_file_sources` silently takes an update branch. That branch does not acquire the dependency graph/source guards, call `externalFileAuthority.requireEditable`, refresh/lock the RAG policy row, or compare any captured request authority. It seals a replacement and deletes the previous pending files before the eventual SQL update. Approval/revoke and capture therefore have no common exclusion discipline across all entry points. Hibernate's eventual row update/SQL reservation trigger is too late to protect those prior filesystem deletions.

For example, concurrent registrations can both read pending A, independently seal B/C, delete A, and then overwrite the pending metadata without detecting competing intent. A registration that races a reserved operation can delete its old pending files before the SQL trigger rejects the update. Concurrent first registration can also create multiple rows with the same path digest: V18 has only a nonunique digest index, while subsequent lookup assumes one result. Path lookup must not ambiguously substitute for explicit logical S/update identity.

**Consequence:** ambiguous pending authorization, lost updates, possible reapplication of stale entity state, and filesystem mutation despite later reservation denial. This is application concurrency, not a malicious same-user NTFS attack.

**Smallest correction:** centralize existing-S pending mutation in one source-first guarded/CAS path used by every API. Make first-import versus update/idempotent retry explicit and race-safe; preserve separate authorized source identities where the contract permits distinct imports. Keep capture/hash work outside SQL row locks; final guarded metadata changes must validate the captured intent.

**Required proof:** barrier-controlled register/register, register/capture and register/revoke/approve races; reserved-source re-registration must deny before deleting/replacing registered bytes. Verify both DB metadata and surviving files, including rollback.

**Contract impact:** Slice 1 MUST 4/5/6 and reuse-first requirements; no new reservation subsystem or Phase 17 rewrite.

### P18-S1-03 — High — exposed file lifecycle has no production reconciliation or durable cleanup retry

**Evidence:** `application/RagSourceService.java:89–112, 142–171, 224–250, 401–423`; `storage/RagOriginalBytesStore.java:638–703, 711–845`; `application/RagRecoveryService.java:34–179`; `application/RagExternalFileAuthority.java:82–86`.

Sealing happens inside a SQL transaction, but a failure/unknown outcome of DB save/commit has no compensation or reconciliation hook. Replacements delete the previously registered pending R before metadata commits: rollback leaves the old DB pointer referencing deleted files and the new sealed R unregistered. Reservation/lookup/seal failures after acquisition can also leave temp data without a caller-level `finally` cleanup.

`deleteRevision`/`cleanupTemp` return `PENDING_INACCESSIBLE`; callers discard those results. Revoke clears pending metadata and rewrites `RagSource.sourceId` to `disabled:<id>` via the frozen revoke behavior, without preserving a retry disposition for failed physical deletion. A successful REVOKED response therefore does not establish managed-byte cleanup. The broad ignored exception at line 418 also hides incomplete external cleanup.

`findAllLegitimateRevisionIds`, `detectOrphans` and `cleanupOrphans` are called by tests, not by any production recovery path. The unchanged `RagRecoveryService` reconciles jobs/generations, not Slice 1 captures/revisions. Its existence cannot support the handback's crash/restart signoff. Current inventory helpers also read/scan the full inventory, rather than implementing a bounded reconciliation batch.

**Consequence:** controlled crash/SQL failure/delete failure can strand private bytes indefinitely, leave registered snapshots missing, and lose cleanup retry knowledge. External indexing denial prevents publication but does not fulfill Slice 1 recovery/privacy obligations.

**Smallest correction:** connect the existing storage helpers to a bounded, idempotent production recovery path; retain enough authoritative pending/cleanup information to reconcile known/unknown SQL outcomes without adopting orphans or rereading originals. Retire old files only after their reference change is known committed, and retain/retry noncompleted deletion reports. No distributed transaction or general incident framework is required.

**Required proof:** DB failure after seal and after replacement, unknown commit, controlled restart with orphan/temp/partial manifest, inaccessible deletion followed by retry, and revoke during copy. Demonstrate denial, no orphan adoption, and eventual bounded managed-storage cleanup through the real lifecycle, not direct test calls to helpers.

**Contract impact:** Slice 1 MUST 3/5 and ADR-0023 G3.4; retained baseline behavior need not be rewritten.

### P18-S1-04 — Medium — equal-byte reuse trusts metadata without verifying the registered pending R

**Evidence:** `application/RagSourceService.java:123–158, 210–238`.

When no selected R exists, equality uses only `pendingRevisionHash`/`pendingByteLength`. Delete or modify that pending `.orig`/manifest, then repeat an import/capture of unchanged original bytes: this path deletes the good newly acquired temp and returns success while preserving the missing/corrupt registered pending R. It never calls the store's integrity verifier. For selected R, every exception from verification is swallowed and treated as changed input, so integrity failure is indistinguishable from an ordinary valid changed-byte comparison.

**Consequence:** successful candidate reuse can retain an invalid R and hide a detectable integrity incident. A later approval's verification is useful, but does not make this successful reuse classification correct. This is not a claim that the API currently emits the finalized Slice 4 `NO_CHANGE` enum.

**Smallest correction:** independently validate the registered revision against its own authoritative descriptor before comparing it with acquired SHA-256/length; distinguish byte inequality from missing/corrupt authority. Deny detectable integrity uncertainty and retain recovery state. Do not repair/rebind by equality or expose full NO_CHANGE.

**Required proof:** missing `.orig`, missing/partial manifest and same-length tampering on repeated identical import/capture; all must deny candidate reuse. Keep the valid same-byte case without extra R allocation and verify changed input against a valid prior R.

**Contract impact:** Slice 1 MUST 3/4/5; ADR-0023 G3.3. No OS isolation proof or full Slice 4 predicates required.

### P18-S1-05 — Medium — production acquisition has no effective cancellation/deadline path

**Evidence:** `application/RagSourceService.java:77, 194` always supplies `null`; `application/RagSnapshotAcquisitionService.java:78–83, 133–143, 217–242`; `storage/RagOriginalBytesStore.java:204–220`.

The new acquisition helper has an optional cancellation supplier, but both exposed production operations disable it. There is no elapsed-time bound, interruption check or final cancellation check before metadata/seal success. The 10-second `TransactionTemplate` timeout starts after acquisition and cannot bound that earlier read. JUnit `@Timeout` and the command watchdog bound test processes, not application capture requests.

**Consequence:** a canceled/stalled capture can continue copying/sealing/committing; byte limits alone do not enforce the required finite acquisition lifetime. The submitted cancellation test only starts with the supplier already true.

**Smallest correction:** use a finite request/capture cancellation and deadline context, checked during copy and at the final guarded transition, with cleanup on cancellation. Validate supported local filesystem/path cases before opening; the current path checks have no filesystem/locality policy. Do not add another worker/job engine or native adversarial proof machinery.

**Required proof:** deterministic mid-copy cancellation and observable mutation, cancellation after copy before seal/commit, and a bounded slow/failed read. Assert no new selected R/version/G and safe pending/temp cleanup. Add a practicable symlink/reparse/unsupported-path test for this new ordinary acquisition path; legacy native-guard tests do not exercise it.

**Contract impact:** Slice 1 MUST 2/5, ADR-0023 G1 finite file/time/disk bounds.

### P18-S1-06 — Medium — newly added atomic-move fallback can overwrite an existing target

**Evidence:** `storage/RagOriginalBytesStore.java:301–308, 348–390`, especially new line 378.

The native Windows branch uses `MoveFileExW(..., 0)` and retains its no-overwrite behavior. If native binding is unavailable, the new fallback uses `Files.move(..., ATOMIC_MOVE)` and assumes a target collision necessarily throws. This assumption is false for the installed JDK: inspected local `JAVA_HOME/lib/src.zip`, entry `java.base/sun/nio/fs/WindowsFileCopy.java`, whose `if (atomicMove)` branch invokes `MoveFileEx(sourcePath, targetPath, MOVEFILE_REPLACE_EXISTING)`. The store checks for an existing manifest, not an existing `.orig`, before this fallback. An existing writable partial `.orig` without its manifest can be replaced by the application.

**Consequence:** the fallback violates the retained application no-overwrite rule. This is a source-confirmed fallback defect; the reviewed successful tests use the available native Windows binding and do not force this branch. No dynamic fallback collision probe was executed.

**Smallest correction:** use a feasible no-clobber fallback or fail closed when its semantics cannot be guaranteed. Do not treat `ATOMIC_MOVE` as a substitute for no-clobber, or demand forensic filesystem durability.

**Required proof:** force native-binding-unavailable behavior, precreate target `.orig` without manifest, and verify seal denial preserves its exact bytes. Retain normal seal and manifest-collision coverage.

**Contract impact:** Slice 1 MUST 3 and ADR-0023 G3.2. No new security contract needed.

### P18-S1-07 — Medium — new successful-acquisition log exposes the original private path

**Evidence:** `application/RagSnapshotAcquisitionService.java:180–184`; [API/privacy inventory](../../../architecture/rag-phase-18-api-extension.md), external reporting row.

The debug log emits `canonicalPath` and its stable digest for every successful acquisition. Raising the RAG logger to DEBUG writes the owner's original directory/file name into ordinary logs. The handback's statement that raw source paths do not leak is therefore false regardless of whether this test run enabled DEBUG. HTTP advice uses safe generic errors; that does not sanitize this log.

**Smallest correction:** log only safe operation/capture identifiers and non-sensitive status/size where useful; avoid original paths and sensitive stable source digests. Review new error/report paths for the same policy.

**Required proof:** capture logs at DEBUG for a synthetic sensitive filename and confirm neither raw locator nor its sensitive digest appears.

**Contract impact:** retained privacy/logging policy, ADR-0022 WF-8 as qualified by ADR-0023; no OS-risk expansion.

## Handback and canonical evidence corrections required

The [Antigravity handback](../slice-1-import-snapshot-handback-2026-10-19.md) correctly reports the 92 focused tests and the independent run reproduced those plus its 38 architecture/OpenAPI tests. Its broader acceptance signoff is not supported:

- The nine acquisition tests contain **pre-cancellation only** and no altered-during-copy, midstream cancellation, new-path symlink/reparse, unwritable store or read-failure test.
- The eleven Slice 1 integration tests contain no competing update or cancel/revoke race and no injected metadata-write/commit failure or controlled restart. `testApproveSource_staleCasRejected` passes an invented `999L`; it does not exercise stale pending consent. `testStorageOrphanDetectionAndCleanup` directly invokes helpers and does not prove runtime reconciliation.
- The source service duplicated pending-update logic across registration and capture; those paths already differ in their guard enforcement. Consolidation is required to correct S1-02, not as a speculative general refactor.
- `docs/database/phase-18-document-indexing.dbml` still inventories V18/V19 and omits V20's columns; the API inventory does not distinguish the now-exposed Slice 1 routes/actual pending consent contract from target-only shapes; the additive package inventory omits `RagSnapshotAcquisitionService`. Synchronize the minimal implemented delta after remediation, without rewriting historical evidence or claiming future targets work.

Preserve this original handback as attributed evidence. A corrective handback must explicitly replace its unsupported signoffs with actual commands/results and remaining limitations. Do not relabel old native-security obligations PASS or require new OS-level proofs.

## Recursive package and architecture inspection

Enumerated the entire actual `rag` production subtree and RAG test folder, plus affected migrations/web inventory. Packages with Java content: `api` (16 files), `source` (5), `internal/application` (22), `internal/domain` (15), infrastructure `client` (3), `persistence` (13), `storage` (5), `windows` (2), and web `advice` (2), `controller` (3), `dto` (16), `mapper` (2). Every content-bearing package has `package-info.java`; empty layer parent folders contain no Java and do not need manufactured descriptors. No `.gitkeep`, `.class` or `.pyc` artifact was found in this production subtree. RAG tests remain in the existing RAG test package; only two new test classes were added.

API commands/results/exceptions remain in the established `rag/api` named interface and source views/enums in `rag/source`; persistence and storage exceptions remain internal. No new root transport package or duplicate response envelope exists. ADR-0016's root `ApiResponse`/`ApiResponses` and module-local advice/controller/DTO ownership are respected. No taste-only grouping/module refactor is requested. Cohesion concerns are the duplicated, divergent lifecycle in S1-02 and file I/O inside guarded SQL transactions in S1-03; fix those proportionately. New public setters/promotion helpers have no active promotion caller and should not be expanded into a speculative Slice 3 workflow.

Graphify was used for navigation (existing 8,894-node graph; bounded query explicitly truncated); all material findings were verified in current source. No graph rebuild or graph-derived proof was claimed.

## 29-dimension evidence ledger

Statuses concern **this slice**; N/A/deferred does not certify later slices.

| # | Dimension | Status | Evidence / limit |
| ---: | --- | --- | --- |
| 1 | Owner intent / scope | FINDING | D12/D13 honored for risk boundary, but pending intent/cancel/recovery requirements fail S1-01–05. No later slice enabled. |
| 2 | Requirement traceability | FINDING | MUST 1–6 and focused checklist mapped above; handback overstates failure/concurrency evidence. |
| 3 | Architecture / canonical consistency | FINDING | RAG ownership/dependency graph preserved; V20/API/package documentation drift noted above. ADR-0023 controls; frozen diagrams need no unrelated rewrite. |
| 4 | Module contracts / encapsulation | PASS | Same rag→vault/fiction/film public edges; no foreign internals; 37 architecture tests passed. |
| 5 | Recursive folder/package structure | PASS | Full subtree enumeration above; existing semantic grouping/descriptors, no new module or stale placeholder. |
| 6 | Shared transport / exceptions | PASS | Root ApiResponse family and module-local RagExceptionAdvice match ADR-0016; no common module requested. |
| 7 | SOLID / cohesion | FINDING | Acquisition/store boundary is reasonable; duplicate update orchestration diverges in S1-02. |
| 8 | DRY/KISS/YAGNI / invariants | FINDING | No provider framework; duplicate mutation and missing expected-intent invariants S1-01/02/04. |
| 9 | Patterns / anti-patterns | PASS | Existing facade/repository/storage adapter reused, no generic bases/events/second queue. Lifecycle fixes do not need a new pattern framework. |
| 10 | Business transitions | FINDING | No early R/version/G promotion; stale approval and invalid repeated capture S1-01/04. |
| 11 | API compatibility / validation | FINDING | OpenAPI 228 operations passes; new command lacks pending/expected authority binding S1-01. Domain API unchanged. |
| 12 | Input validation / bounds | FINDING | Regular-file/extension/size/before-after checks present; finite cancellation/time and supported-path evidence incomplete S1-05. |
| 13 | Authorization / consent / privacy | FINDING | Existing auth `.anyRequest().authenticated()` remains; exact pending grant and re-registration guards fail S1-01/02. New authenticated endpoint behavior lacks focused API tests. |
| 14 | Secrets / retention / copies / egress | FINDING | No new downloads/dependencies/worker egress; controlled-byte cleanup and path logging fail S1-03/07. Complete backup/WAL/owner-copy erasure is not asserted. |
| 15 | Transactions / concurrency | FINDING | Source-first mechanisms reused on approval, incompletely on capture/registration; S1-01–03. File hashing/sealing/deletion also occurs inside update transactions; move heavy I/O out while preserving final CAS. |
| 16 | Cancellation / crash / recovery | FINDING | Existing job recovery untouched; new snapshot lifecycle not wired to it, S1-03/05. |
| 17 | PostgreSQL / Flyway / permissions | FINDING | V20 migration + JPA init passed on PG18.6; no worker grant expansion. Missing runtime exclusion/intent consistency S1-01/02; pending/selected metadata is documented incompletely. |
| 18 | Time / memory / I/O / lock cost | FINDING | Streaming 64 KiB copy and source cap present; no capture deadline, full scans and repeated hash/seal work under update TX S1-03/05. No speculative micro-optimization requested. |
| 19 | Retrieval/index/recommendation quality | N/A | No retrieval/recommendation or external G publication in Slice 1; do not close FR18-3. |
| 20 | Parser/OCR/multilingual fidelity | N/A | Slice 2 deferred, parser/Python untouched. Binary fixture here proves exact custody, not document quality. |
| 21 | Adversarial / concurrency security tests | FINDING | Existing store/native tests pass; new path lacks practical concurrent/mid-copy/consent/failure cases specified above. Superseded malicious same-user/native tests not required. |
| 22 | Test quality / assertion adequacy | FINDING | All 130 selected tests pass; their scopes do not establish the handback's stronger lifecycle signoffs. Required focused regressions are attached to each finding. |
| 23 | Performance benchmarks | N/A | Slice 4/full resource profiling deferred; no latency/GPU benchmark claimed. Current finite acquisition remains mandatory S1-05. |
| 24 | Held-out evaluation science | N/A | No OCR/model/retrieval quality enabled or changed; later phase gates preserved. |
| 25 | Dependencies / versions / lifecycle | PASS | No pom/lock/model/dependency delta; installed JDK source confirms S1-06's fallback behavior. No vulnerability/license audit or newest-version claim. |
| 26 | Build / tooling / warnings | PASS | Bounded Maven 130/0/0/0, PG migration, architecture/OpenAPI and diff check; warning limits recorded above. Full clean/IDE checks NOT RUN. |
| 27 | Logging / operations | FINDING | Raw private path at DEBUG S1-07; deletion status and retry knowledge discarded S1-03. HTTP error advice remains generic. |
| 28 | Docs / governance | FINDING | Entry gate valid, historical reviews preserved; handback and V20/API/package evidence need accurate synchronization. No blanket Phase 18 readiness. |
| 29 | Hygiene / delivery | PASS | Submitted scope limited to RAG/new V20/two tests/web inventory/status/handback; diff check passes, no publishing action. Review adds only review/status documentation. |

## Disposition and next gate

**Seven blocking implementation findings remain: S1-01/02/03 High; S1-04/05/06/07 Medium.** They are defects or missing mandatory Slice 1 evidence, not a request to change D12/D13 or resurrect the old threat model. No separate architecture/security decision is needed to understand the required behavior; remediation execution still requires the owner's workflow authorization.

Set current Slice 1 status to **CHANGES_REQUESTED**. Preserve credited reuse, pending-only semantics, and all historical findings/round records. No commit message is supplied while blockers remain.

**Next step:** the historical Antigravity remediation/re-review allowance is already **2/2 exhausted**. D13 authorized this separate **initial** Slice 1 implementation/review, not another remediation allowance. The owner should authorize a finite, separate `$codex-implementation-takeover` session for these approved-scope defects, followed by independent re-review; any different Antigravity remediation exception needs an explicit new owner policy decision. This review launches neither path automatically. Do not commit/push, start Slice 2, close FR18-3 or accept Phase 18.
