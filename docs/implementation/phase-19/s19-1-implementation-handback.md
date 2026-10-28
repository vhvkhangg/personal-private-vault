<!-- CODEX_CORRECTIVE_ATTEMPT_4_BEGIN -->
# S19-1 Codex corrective attempt 4 — current implementer handback

**2026-10-28 / IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED / NOT_ACCEPTED.** [Exact Owner decision](owner-authorization-s19-1-codex-corrective-attempt-4-2026-10-28.md), [ACTIVE](../handoffs/ACTIVE.md), [executable contract](s19-1-executable-handoff-2026-10-27.md) and [independent attempt-3 review](reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-3-final-review.md) authorize only **F13 MEDIUM**, healthy writable pre-C_E deadline root 503. Codex authored the correction and completed its own implementation self-check. **F13 remains OPEN for separate independent adjudication**; F01–F12/L01 remain independently CLOSED, L02 LOW deferred. Antigravity **3/3 exhausted**; Codex attempts **1–3 independently blocked**; separately Owner-authorized **attempt 4 implemented, independent review pending**. No automatic attempt 5, self-acceptance or READY claim.

Entry HEAD/main `ead5291bdffee09e53d0b41957ea751231015db2`, 80 unstaged/untracked candidate paths, empty index; all 59 prior final runtime/test hashes match. Fresh evidence root: `logs/phase-19/s19-1/codex-corrective-attempt-4-baba16d5905f4a62b48f4ecf43e732d0/`. Earlier handbacks/reviews/Owner decisions/logs remain immutable. No schema/native/dependency/source-authority change, staging/publication, indexing activation, phase reopening or hook/config/trust repair.

## Scope and pending gate

- **COMPLETED:** parent/source/receipt verification, bounded F13 sender correction, healthy and committed deadline proof, focused-to-affected regression/native verification, own implementation self-check and authorized notes/status synchronization. COMMON and Docs Sync were read. Each actual Maven exit/capture/cleanup was checked before the next run; no command was blindly restarted.
- **INVALIDATED BY DRIFT:** old sender/HTTP/native success evidence alone cannot prove the new deadline/readiness lifetime. Fresh reports below cover those changed paths. Final Sender XML comes from the later run with the committed-prefix negative control; no production change followed the final retrieval/native run. The earlier two failed runs remain FAILED.
- **PENDING:** Owner opens a **separate** Codex `$codex-final-review` to verify the candidate and adjudicate F13, retaining every closed finding. Publication has not occurred.
- **BLOCKED:** no known remaining implementation/environment blocker from the focused checks; independent acceptance is unavailable in this implementing session.

Antigravity corrective [re-review 1](reviews/2026-10-27-s19-1-independent-final-re-review-1.md), [re-review 2](reviews/2026-10-27-s19-1-independent-final-re-review-2.md), [re-review 3](reviews/2026-10-27-s19-1-independent-final-re-review-3.md), all dated 2026-10-27, remain blocking history. Codex [attempt 1](reviews/2026-10-28-s19-1-independent-codex-takeover-final-review.md), [attempt 2](reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-2-final-review.md) and [attempt 3](reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-3-final-review.md), dated 2026-10-28, were each independently blocked. Only the new explicit F13 decision grants attempt 4. Internal iterations do not advance/reset a counter or authorize attempt 5.

## F13 candidate correction and own self-check

Only `RagRetrievalService`, its existing sender integration test and four authorized current documents changed: **six paths**, recorded in `final-attempt4-change-inventory.json`. No new package/type, model, privilege, schema, timer, dependency or queue.

`onTimeout` now records **logical deadline stop** and reuses the existing bounded error-only owner/root `RAG_QUERY_UNAVAILABLE` envelope. `onError` marks actual transport failure. The same public Servlet `WriteListener` is registered **before computation**, with no headers/payload written then. A single bounded payload is selected later; success still requires C_E first. This matters because the installed Tomcat 11.0.24 source confirms that initial readiness remains pending until its container callback, and timeout handling must be handled/completed before the timeout listener returns (`container-readiness-diagnostic.json`). First registering a listener during timeout and manually invoking it cannot clear that pending state. No container internals are called or changed.

Ready error bytes are drained through the **same listener** before returning from timeout handling. Concurrent producer/container/timeout signals are coalesced into one cursor owner, with readiness checked before each bounded write/flush; no monitor or SQL lock spans writes. Unready, failed or already-committed evidence output follows bounded abort without appending/replaying an error. Existing preflight failures initialize the same owner, and cancellation arriving before registration waits for its producer to select the error after setup. Callback/buffer/container/native/transport quiescence and guarded read-back remain necessary for C_RL; timeout notification and `complete()` do not release a reader.

**Real authenticated healthy deadline proof:** `RagRetrievalSenderIntegrationTest.healthyWritableDeadlineBeforeEmissionReturnsRootErrorAndRetainsReaderUntilJoin` pauses query work before C_E and uses the existing shortened test-only AsyncContext timeout. The real connection stays open; `notReady=false`, write barrier count is zero, and the actual ready callback writes through that stream. The result is **HTTP 503, nonempty JSON root `error.code=RAG_QUERY_UNAVAILABLE`, no-store, no evidence sentinel**, while `emission_started=false`, durable stop intent is set, and the reader remains nonterminal/reserved. Only after computation joins does production reach TERMINAL/ABORTED, positive typed quiescence evidence and released reservation. The vector override isolates timing; it does not substitute for native quality/stop proof.

**Actual committed-output control:** `deadlineAfterCommittedPrefixCannotAppendErrorOrReleaseActiveWriter` flushes the first success byte before blocking the active write, so the 200 response is genuinely committed. After the real deadline, the reader remains reserved/nonterminal until that writer leaves. Captured connection-close wire retains the 200 success prefix and contains **no appended `RAG_QUERY_UNAVAILABLE`/`RAG_RETRIEVAL_CANCELLED` JSON**; production then proves terminal ABORTED release. The other 18 sender cases preserve cancellation 409, not-ready/late callback, actual disconnect/deadline, blocked write/incident and false native stop coverage. Backpressure in the fixture begins only after payload headers are selected, keeping the new empty initial readiness callback separate from evidence-writing barriers; production deadlines are unchanged.

Own self-check inspected initial readiness/payload separation, C_E-before-evidence, one cursor/no replay, concurrent callback signals, early cancellation/preflight errors, healthy deadline versus actual transport failure, committed/unready abort, active callback/buffer retention, native/context stop, token/read-back and unchanged DTO/root factory ownership. All source/inspection/SQL/native supervisor owners remain unchanged; F01–F12/L01 are protected. No closed finding is reopened or self-adjudicated.

## Focused verification — actual results

Every Java run used native `rag-engine/.venv-d17/Scripts/python.exe scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh.json> -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<classes>' test` from the repository root. Final receipts are **PASS/exit 0, zero failures/errors/skips**, `capture_complete=true`, `cleanup_complete=true`, cleanup `not_needed`. Exact selected XML is preserved in run-scoped `-xml/` directories.

| Receipt / exact Dtest classes | Actual result |
| --- | --- |
| `final-retrieval-native-4.json`: RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalTakeoverIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagRetrievalEmbeddingNativeIntegrationTest | **83 cases, 201.432s**. Final selection uses the six non-Sender reports/64 cases here and the stronger final Sender report below. |
| `final-sender-5.json`: RagRetrievalSenderIntegrationTest | **20 cases, 63.117s**, including healthy pre-C_E 503 and committed-prefix deadline control, plus all 18 prior sender cases. |

**84 distinct fresh Java cases pass** (seven final class reports; repeats counted once) in `final-verification-summary.json`. **134 additional Java cases** are explicitly reused with exact receipts/XML in `reused-evidence-applicability.json`: SourceAuthority 13, Snapshot 42, existing guards/roles 28, architecture 37, OpenAPI 1, actual controller crash/recovery 2 and native protocol 11. Their owners/contracts are unchanged; **57 of the prior 59 runtime/test source hashes remain unchanged**, with only the sender and its test modified. Fresh actual HTTP/native/incident/wire tests cover changed integration. Architecture reuse is limited to unchanged public types/interfaces/import directions/package/module edges, schema and wire inventory; no new type/edge exists. **218 applicable Java cases in total**, not 218 fresh executions or a full application suite. Fourteen Python tests plus Ruff/format/Mypy checks remain explicitly reused with unchanged Python/model/pin sources and preserved receipts.

**Failed progress evidence remains failed:** `focused-sender-1.json` exit 1 (19 cases: 2 failures/3 errors), including container fallback HTTP 500 and subsequent no-row/missed-computation cases; the new test's cleanup now joins production sender completion before the next fixture. `focused-sender-2.json` exit 1 (19 cases: one empty-503 body failure; the other 18 pass) showed that a newly registered readiness callback cannot be made ready by direct invocation. Both have complete capture/cleanup and preserved selected XML. `focused-sender-3.json` passed all 19 before the final preflight/early-cancellation self-check and committed control; it is progress evidence, not the final selection. No failure/iteration consumed another takeover attempt.

Final synthetic VI/EN/Han producer → publication → pinned CPU query → PostgreSQL → HTTP startup was **2.844–3.124s**, native total **2.925–3.237s**, peak process **1,041,969,152–1,144,991,744 bytes**, below the unchanged 2 GiB query Job limit. The **18,500ns** idempotent stop was after compute already stopped child/transport, not cancellation latency. Existing JDK/Mockito/SpringDoc diagnostics remain visible. Only disposable PostgreSQL, sealed synthetic Markdown and installed pinned artifacts were used; no operational database/deployment, artifact download, indexing activation or production latency/quality certification.

## Docs Sync Check — final attempt-4 implementer boundary

Recovered checkpoint: the attempt-3 independent review/attempt-4 Owner authorization overlay, checked only for changed/invalidated scope. COMMON and Docs Sync were read; actual source and receipts control over narration.

- **UPDATED:** ACTIVE/executable handoff implementation and reviewer routing; this attempt-4 prefix; [implementation notes](s19-1-implementation-notes.md) for logical deadline versus transport failure, early readiness/no premature bytes, same error-only root owner and bounded committed/unready abort. Original API table, nonblocking/DTO/ADR-0016 and positive quiescence contracts are unamended. F13 is not self-closed.
- **ALREADY_CONSISTENT:** exact Owner scope/counters, unchanged HEAD/main/empty index, independently closed fixes, disabled defaults and 11 immutable review/Owner artifacts (`immutable-history-check.json`, zero mismatch). Source/inspection/SQL/recovery/native/pins, schema/ADR/package/API inventory have no new scope or edit. Final runtime/test manifest has 59 paths; 57 prior paths are unchanged for explicit evidence reuse. Final boundary checks cover current source hashes, scoped local links/whitespace, protected paths and preserved historical handback body.
- **DEFERRED_OUT_OF_SCOPE:** root/backend AGENTS and handoffs/P19/preparation/governance/skill routing remain ChatGPT-owned under the executable contract; actual ACTIVE plus the explicit attempt-4 decision control execution. L02 and other B1/B2/MA/DOCB LOW backlog remain with milestone/audit. No cleanup B or publication closeout is authorized.
- **NOT_VERIFIED:** separate independent attempt-4 acceptance and any Owner publication. The mandatory scoped deadline branch now has implementer proof; independent review must adjudicate it. No full-suite/operational/later-phase acceptance claim.
- **BLOCKED_OWNER_DECISION:** none for this completed bounded candidate. If the separate review blocks it again, no automatic attempt 5 or Antigravity round 4 follows.

**Next step: Owner opens Codex in a SEPARATE session | `$codex-final-review` | P19-S19-1 [ACTIVE](../handoffs/ACTIVE.md) + this attempt-4 handback + [exact Owner decision](owner-authorization-s19-1-codex-corrective-attempt-4-2026-10-28.md) + [attempt-3 residual review](reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-3-final-review.md) | disclose Codex implementer authorship; independently verify/adjudicate F13 and applicable closed-finding regressions; no staging/publication | Antigravity 3/3 exhausted; Codex attempts 1–3 independently blocked; attempt 4 implemented, independent review pending.**
<!-- CODEX_CORRECTIVE_ATTEMPT_4_END -->

<!-- CODEX_CORRECTIVE_ATTEMPT_3_BEGIN -->
# S19-1 Codex corrective attempt 3 — current implementer handback

**2026-10-28 / IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED / NOT_ACCEPTED.** [Exact Owner decision](owner-authorization-s19-1-codex-corrective-attempt-3-2026-10-28.md), [ACTIVE](../handoffs/ACTIVE.md), [executable contract](s19-1-executable-handoff-2026-10-27.md) and [independent attempt-2 review](reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-2-final-review.md) authorize only **F06 HIGH and F05 MEDIUM**. Codex authored the correction and completed its own implementation self-check; both findings remain **OPEN for separate independent adjudication**. F01/F02/F03/F04/F07/F08/F09/F10/F11/F12/L01 remain independently CLOSED; L02 LOW stays deferred. Antigravity **3/3 exhausted**; Codex attempts 1–2 independently blocked; **separately Owner-authorized attempt 3 implemented, independent review pending**. No automatic attempt 4 or self-acceptance.

Entry HEAD/main `ead5291bdffee09e53d0b41957ea751231015db2`, 77 unstaged/untracked candidate paths, index empty. All 58 previous final runtime/source hashes match; the two added paths since independent review are its new report and attempt-3 Owner decision. Fresh evidence root: `logs/phase-19/s19-1/codex-corrective-attempt-3-686e4018d2d24ed388f6f704eebf23bf/`. Earlier handbacks, independent decisions/reviews and logs are preserved.

## Scope, completion and counters

- **COMPLETED:** actual parent/source/receipt inspection, the two authorized corrections, focused-to-affected verification, own implementation self-check and in-scope notes/status synchronization. COMMON and Docs Sync were read. All commands exited normally with actual receipts before any subsequent Maven run; no interrupted command was blindly rerun.
- **INVALIDATED BY DRIFT:** pre-correction Java evidence does not prove the changed currentness/sender branches. Fresh gates below cover those branches. The final SourceAuthority report comes from a targeted rerun after isolating the race fixture's periodic reconciliation, rather than from the earlier broader run. No production change followed the final core/native runs.
- **PENDING:** Owner opens a **separate** Codex `$codex-final-review` session to verify the candidate and adjudicate F06/F05. No Owner publication has occurred.
- **BLOCKED:** no known remaining implementation/environment blocker from the focused checks. This implementing session cannot supply independent acceptance.

The three Antigravity corrective re-reviews remain immutable: [round 1](reviews/2026-10-27-s19-1-independent-final-re-review-1.md), [round 2](reviews/2026-10-27-s19-1-independent-final-re-review-2.md), [round 3](reviews/2026-10-27-s19-1-independent-final-re-review-3.md), all dated 2026-10-27 and independently blocking. [Codex attempt 1 review](reviews/2026-10-28-s19-1-independent-codex-takeover-final-review.md) and [attempt 2 review](reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-2-final-review.md), dated 2026-10-28, remain independently blocking history. The exact attempt-3 Owner decision grants this attempt only. Internal test iterations, interruptions and fixture refinement do not advance the counter or authorize another attempt.

## Candidate changes and precise proof

| Finding / affected original scope | Correction and concrete test evidence |
| --- | --- |
| **F06 HIGH / S19-1-05, currentness ordering** | `RagSourceAuthority.guardPrepared` now checks the existing exact committed inspection predicate for each external member of the prepared dependency closure **after graph/external/source guards are held**, before returning a current-read proof. Existing `RagGenerationState` handles an unproven proof as UNAVAILABLE; no schema/privilege change or global denial. `RagSourceAuthorityIntegrationTest.committedInspectionIsRecheckedInsideFinalCurrentnessGuard` has three real PostgreSQL parameters: affected current R is UNAVAILABLE; obsolete R and independent sibling controls remain CURRENT. An independent connection holds the graph guard, the actual production currentness call is observed waiting in PostgreSQL lock activity, and the inspection commits before that guard is released. The row stays UNRESOLVED and valid source integrity is unchanged. `currentnessGuardFirstCompletesBeforeInspectionAndNextReadIsFenced` uses a real lease-row barrier to observe the actual currentness transaction holding graph/source guards, then proves the exact inspection connection waits on that graph owner. Guard-first returns CURRENT; after inspection commits the next read is UNAVAILABLE. No mocked authority or sleep establishes either order. Periodic reconciliation is delayed only in this test fixture, excluding unrelated graph waiters. |
| **F05 MEDIUM / S19-1-11/13, R06 error branch** | `RagRetrievalService` distinguishes logical stop, an error-only output cursor and aborted transport. A healthy unused cursor sends the existing bounded root cancellation envelope through readiness/WriteListener even though evidence is stopped. Cursor registration is serialized; callback writes run outside the monitor and SQL. Cancellation intent is published before its stop flag. No existing evidence cursor is restarted, no JSON is appended to committed output, and callback/buffer/container/native/transport quiescence still gates terminal finish. `RagRetrievalSenderIntegrationTest.cancelBeforeEmissionReturnsRootErrorWhileComputationStillRetainsReader` uses real authenticated retrieval **and cancellation HTTP**, with a controlled query-computation barrier before C_E. The original response is **409, nonempty root `error.code=RAG_RETRIEVAL_CANCELLED`, no-store and no evidence sentinel**, while the reader is still nonterminal/reserved. Only after computation is released does cancellation return 200 and the row become TERMINAL/CANCELLED, `emission_started=false`, emission disposition ABORTED. This controlled vector is lifecycle proof, not native quality proof. All prior 17 sender cases, including blocked writes, not-ready/late callbacks, disconnect/deadline and failed native stop, also pass. |

`attempt3-change-inventory.json` identifies exactly **eight changed paths**: two production Java owners, two existing test classes, ACTIVE, executable handoff, this current handback prefix and implementation notes. The SourceAuthority test was unchanged at entry and is the one newly changed tracked path absent from that entry inventory. No schema, migration, recovery owner, supervisor, dependency/model pin, API DTO/controller or module edge changed in attempt 3. No P17/P18 contract reopening, new slice, indexing/live-original activation, hook/config/trust repair, LOW-only work, staging or publication.

Own self-check reviewed final guarded predicate placement/closure specificity, physical commit order, sender/error cursor ownership, volatile cancellation ordering, duplicate registration/buffer disposal, monitor/SQL boundaries, failed/unknown stop and final read-back, unchanged DTO/root factory ownership and scope/history preservation. Existing readers/inspections/reconciliation and response adapters are reused; no new framework/queue/worker/migration. The 42 frozen snapshot regression cases remain green.

## Focused verification — actual results

Fresh root is the exact attempt-3 directory above. Every Java run used native `rag-engine/.venv-d17/Scripts/python.exe scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh.json> -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<classes>' test`. All receipts below are **PASS/exit 0**, with **zero failures/errors/skips**, `capture_complete=true`, `cleanup_complete=true`, cleanup `not_needed`. Selected XML is preserved in each run's `-xml/` directory.

| Receipt / exact Dtest classes | Actual result |
| --- | --- |
| `final-core-authority-2.json`: RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalTakeoverIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagSourceAuthorityIntegrationTest,RagSnapshotCorrectiveIntegrationTest | **129 cases, 164.333s**. Final selection uses seven reports/116 cases here and the later SourceAuthority report below. |
| `final-native-guards-3.json`: RagRetrievalEmbeddingNativeIntegrationTest,RagSourceReservationsIntegrationTest,RagSlice4LifecycleIntegrationTest,RagSlice4ReindexIntegrationTest,RagRuntimeRoleIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest | **74 cases, 203.794s**: native 8, existing guards/roles 28, architecture 37, OpenAPI 1. |
| `final-currentness-order-4.json`: RagSourceAuthorityIntegrationTest | **13 cases, 56.618s**, including the four final physically ordered currentness cases after test-only reconciliation isolation. |

**203 distinct fresh Java cases pass**, selected in `final-verification-summary.json` (16 class reports; repeats counted once). The initial `focused-authority-sender-1.json` also passed 31 cases; it is preserved as progress evidence before the cancellation publication-order adjustment, not counted again or substituted for the final runs. No attempt-3 failed/timeout test receipt is hidden; all four actual test commands passed.

**Explicit reuse:** two independently credited real HTTP-controller crash/recovery cases from attempt 2 `http-controller-recovery-final-2.json`, plus 11 native protocol cases from attempt 1 `native-stop-race-4.json`/selected ProtocolNative XML. Recovery/inspection/SQL/supervisor/protocol owners are unchanged; final core/native tests exercise the stronger currentness and changed sender integration. **216 distinct applicable Java cases in total**. Fourteen Python tests and Ruff/format/Mypy checks are reused from the preserved attempt-1 receipts with unchanged Python source/test hashes. `reused-evidence-applicability.json` records exact paths, 11 matching relevant source/test hashes, actual reused receipt/XML results and boundary-specific rationale. These are not fresh crash/protocol/Python executions.

The final native VI/EN/Han producer → publication → pinned CPU query → PostgreSQL → real HTTP cases measured startup **2.844–2.859s**, native total **2.927–2.956s**, peak process **1,103,454,208–1,145,610,240 bytes**, below the 2 GiB query Job cap. The separate **6,600ns** idempotent stop was after compute positively stopped child/transport, not cancellation latency. These synthetic local fixtures do not certify production quality/latency or a full application suite. Existing compiler/JDK/SpringDoc diagnostics remain visible. Only disposable migration/SQL fixtures were used; no operational migration, new artifacts/downloads or deployment.

## Docs Sync Check — final attempt-3 implementer boundary

Recovered checkpoint: the attempt-2 independent review and attempt-3 authorization overlay, rechecked only for changed/invalidated current scope. Final local evidence includes unchanged HEAD/main/empty index, final source hashes, exact receipts and the eight-path delta.

- **UPDATED:** ACTIVE and executable handoff implementation/reviewer routing; this attempt-3 prefix; [implementation notes](s19-1-implementation-notes.md) for the final guarded inspection predicate and mandatory healthy pre-C_E cancellation envelope, removing the unsupported empty-normal-error allowance. Original acceptance, ADR-0016 and nonblocking/DTO contracts are unamended; F06/F05 are not self-closed.
- **ALREADY_CONSISTENT:** existing API DTO/controller/OpenAPI structure, package/schema/ADR-0025 inventory and migration facts remain applicable without edits; no new public interface/schema/component exists. Unchanged closed fixes, disabled defaults, exact scope/counters, nine immutable review/Owner artifacts, V1–V23/native process/pins and historical handback body are preserved. Final manifest `final-runtime-source-manifest-2.json` includes 59 runtime/test paths; it supersedes only the final test fixture hash from the earlier manifest. Boundary verification checks hashes, scoped local links, whitespace, empty index and protected paths.
- **DEFERRED_OUT_OF_SCOPE:** root/backend AGENTS, handoffs/P19 navigation and preparation-era governance/skill status pointers remain ChatGPT-owned under the executable contract; actual ACTIVE plus the explicit attempt-3 Owner decision control this execution. L02 and B1/B2/MA/DOCB LOW backlog stay with milestone/audit. No repository-wide cleanup B or publication closeout.
- **NOT_VERIFIED:** separate independent attempt-3 acceptance and any Owner publication. Both remaining runtime branches now have implementer evidence above; that does not adjudicate the findings. No full application suite/operational or later-phase acceptance is claimed.
- **BLOCKED_OWNER_DECISION:** none for the completed bounded candidate. If independent review blocks it again, no fourth Codex/Antigravity attempt follows automatically.

**Next step: Owner opens Codex in a SEPARATE session | `$codex-final-review` | P19-S19-1 [ACTIVE](../handoffs/ACTIVE.md) + this attempt-3 handback + [exact Owner decision](owner-authorization-s19-1-codex-corrective-attempt-3-2026-10-28.md) + [attempt-2 residual review](reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-2-final-review.md) | disclose Codex implementer authorship, verify final evidence and independently adjudicate F06/F05; no staging/publication | Antigravity 3/3 exhausted; Codex attempts 1–2 independently blocked; attempt 3 implemented, independent review pending.**
<!-- CODEX_CORRECTIVE_ATTEMPT_3_END -->

<!-- CODEX_CORRECTIVE_ATTEMPT_2_BEGIN -->
# S19-1 Codex corrective attempt 2 — current implementer handback

**2026-10-28 / IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED / NOT_ACCEPTED.** Authority: [attempt 2 Owner decision](owner-authorization-s19-1-codex-corrective-attempt-2-2026-10-28.md), [ACTIVE](../handoffs/ACTIVE.md), [executable handoff](s19-1-executable-handoff-2026-10-27.md) and [independent attempt 1 review](reviews/2026-10-28-s19-1-independent-codex-takeover-final-review.md). Codex authored attempt 2 and completed its own implementation self-check. **F04/F06 HIGH and F05/F11 MEDIUM remain OPEN for separate independent adjudication.** F01/F02/F03/F07/F08/F09/F10/F12/L01 stay independently CLOSED; L02 stays deferred LOW. Antigravity **3/3 exhausted**; attempt 1 independently blocked; **separately Owner-authorized attempt 2 implemented, review pending**. No automatic attempt 3, counter reset, self-acceptance or READY claim.

Entry HEAD `ead5291bdffee09e53d0b41957ea751231015db2`, branch main, empty index, all 55 previous final-source hashes match. Fresh evidence root: `logs/phase-19/s19-1/codex-corrective-attempt-2-99592596bfe34cbeaebca949663fd61d/`, including entry hashes and applicability. Old reviews/Owner decisions/logs are immutable. No staging/publication, activation, P17/P18 reopening, later slice, hook/config/trust repair or cleanup B.

## Resume classification and bounded corrections

- **COMPLETED:** source/receipt recovery, the four authorized corrections, focused regressions and required scoped gates, own implementation self-check and authorized implementation-adjacent Docs Sync. COMMON and the Docs Sync contract were read. Actual parent/source files and receipts were inspected; no command was blindly restarted. `final-core-2` exited 1 with capture/cleanup complete before its corrective rerun; no CLI interruption was counted as another attempt.
- **INVALIDATED BY DRIFT:** previous Java evidence cannot establish the changed sender/inspection/guard paths. Final receipts below replace that coverage. The first real HTTP recovery pass was repeated after the runtime-registration guard changed. Final Takeover XML is selected from the last run after adding reader-free root/generation purge assertions. Unchanged protocol/Python evidence is explicitly reused by SHA-256, rather than rerun or overwritten.
- **PENDING:** Owner opens a separate Codex `$codex-final-review` session to adjudicate F04/F06/F05/F11 and the candidate. Any Owner publication follows independent acceptance; it has not occurred.
- **BLOCKED:** no known remaining implementation/environment blocker in the focused checks. Independent acceptance is unavailable in this implementing session; missing review is not acceptance.

| Residual finding / original affected coverage | Candidate change and exact focused evidence |
| --- | --- |
| **F04 HIGH / R02, R09; S19-1-09/10** | V24's private `retrieval_revoke_inspected_generations` matches the affected file/R against each generation's own canonical contributing manifest, including true dependent roots. The sole finisher retains exact observed tuple/token/affected-row checks and one physical C_D + C_RL transaction. `RagRetrievalTakeoverIntegrationTest.incidentRevokesOnlyAffectedRootAndTrueDependentWhileIndependentRootRemainsCurrent` proves A and its true parent revoked, independent B integrity false/CURRENT, and real HTTP evidence from the **same unchanged B generation**, with a controlled query vector. `newAdmissionAtPhysicalDenialReleaseCommitCannotObserveGap`, wrong-token/unrelated-incident tests and wire-loss cases preserve atomicity/retention proof. |
| **F06 HIGH / R07, R09; S19-1-05/10/11** | Immutable `inspection_proof` captures original R/hash/length and controller instance/PID/FILETIME before I/O. Exact outstanding predicates now fence source/currentness, reserve/lock/release, promotion and generation/root/chunk/retired-artifact cleanup. Unknown R is conservative; unrelated old R and B are controls. `RagSnapshotInspections` owns per-file byte checks, active-operation accounting and bounded original-UUID reconciliation in the existing production owner. Registration/runtime adoption share the graph guard, with the proven inventory rechecked inside the adoption transaction; native identity proofs happen outside SQL. `exactOutstandingInspectionFencesIndexOwnersAndReusesOriginalIdentityWithoutCollateralDenial` proves standalone fencing, safe B/old-R controls, denied raw root/generation deletion and reuse across recognized producers. `readerInspectionRetryReusesOriginalUnresolvedOperation` proves same/new requested UUID retries return the original attached UUID and block finish until classified. Unknown producer/process/descriptor remains fenced; no TTL or new CLEAN UUID substitute. |
| **F05 MEDIUM / R06; S19-1-11** | The real controller supplies `RagRetrievalResponseDto::fromResponse` and the shared root envelope. An owned producer computes/serializes; Servlet `WriteListener` callbacks check readiness before bounded writes/flush and return when not ready. Release requires callback/buffer/container/native/transport quiescence. Late callbacks cannot replay. The 17-case `RagRetrievalSenderIntegrationTest` covers `notReadySenderCancelsWithoutReleasingAWriteBarrierAndLateCallbackCannotReplay`, real loopback DTO evidence, actual deadline/disconnect, blocked active writer/incident retention, cancellation and false native stop. The normal response test now checks state/disposition on the same fresh terminal database read, removing its stale pre-wait entity assertion without relaxing the contract. No sender/DTO/ADR-0016 waiver or amendment. |
| **F11 MEDIUM / original R07; S19-1-11/14** | `RagRetrievalConnectionLossIntegrationTest.productionRecoveryReconcilesOriginalReaderFreeInspectionAfterRollbackOrLostAck` (2 parameters) drops the actual SQL wire around preregistration commit and invokes production reconciliation, preserving the original committed UUID and denial until its safe classification. `RagRetrievalHttpRecoveryIntegrationTest.actualHttpControllerCrashReconcilesOriginalInspectionThroughNewProductionOwner` (prepare/reader) sends real authenticated HTTP to a separate real Spring controller, pauses only after the production original UNRESOLVED operation is committed, verifies original PID/FILETIME (and attached reader/bound worker), kills that owned JVM, proves reserve denied, then starts a new production owner. Startup discovers/classifies the **original** UUID with immutable proof unchanged, terminalizes the old reader where applicable, and serves fresh HTTP EVIDENCE. No test-only `recordInspection` or administrative reader repair substitutes for this recovery. |

The exact attempt-2 inventory is `final-attempt2-change-inventory.json`: **25 paths**, including 20 changed entry-listed paths and five newly changed/added paths absent from that entry inventory. These include the shared existing source/currentness owners, new `RagSnapshotInspections`, additive V24/model, sender/controller, focused tests/real HTTP fixture and eight authorized current/derived documents. The frozen snapshot test change only supplies the new recovery constructor dependency. Previous candidate changes remain present; the inventory does not attribute all HEAD differences to attempt 2.

Own self-check inspected exact incident scope, operation identity/monotonicity, currentness and raw SQL backstops, file/graph/reader lock order, runtime takeover races, unknown commit/stop retention, callback ownership and buffer disposal, DTO boundaries, migration privileges and documentation. No SQL locks span new inspection/model/HTTP waits. V1–V23, NativeWorkerProcess, query supervisor, Python implementation, dependency/model pins and module edges remain unchanged by attempt 2. The safe frozen unapproved/unselected pending-replacement behavior remains covered by all 42 snapshot cases; its old unresolved revision is retained for classification/cleanup fencing. No new framework, scheduler, independent acceptance, activation or LOW-only cleanup.

## Final focused verification — actual results

All Java commands used the existing native venv and approved watchdog: `rag-engine/.venv-d17/Scripts/python.exe scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh.json> -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<exact classes>' test`. Each receipt below is **PASS, exit 0, capture_complete/cleanup_complete true, cleanup not_needed**. Exact selected XML is preserved beside it; no historical logs were overwritten.

| Receipt / exact Dtest classes | Actual cases / duration |
| --- | --- |
| `final-core-authority-4.json`: RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalTakeoverIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagSourceAuthorityIntegrationTest,RagSnapshotCorrectiveIntegrationTest | **124**, 0 failures/errors/skips; **138.245s**. Final selection uses seven reports (114 cases), with the later stronger Takeover report below. |
| `http-controller-recovery-final-2.json`: RagRetrievalHttpRecoveryIntegrationTest | **2**, 0 failures/errors/skips; **106.232s**. Genuine HTTP-controller prepare/reader interruption and new production-owner recovery. |
| `final-native-guards-5.json`: RagRetrievalEmbeddingNativeIntegrationTest,RagRetrievalTakeoverIntegrationTest,RagSourceReservationsIntegrationTest,RagSlice4LifecycleIntegrationTest,RagSlice4ReindexIntegrationTest,RagRuntimeRoleIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest | **84**, 0 failures/errors/skips; **243.880s**. Native 8, final Takeover 10, existing guard/role 28, architecture 37, OpenAPI 1. |

**200 distinct fresh Java cases pass.** `final-verification-summary.json` selects 17 exact fresh class reports, counting repeated Takeover cases once. **11 additional native protocol cases** are reused from attempt 1 `native-stop-race-4.json`/selected ProtocolNative XML, with unchanged supervisor/controller-identity/Python/protocol-test hashes and boundary rationale in `native-protocol-evidence-applicability.json`. Changed SQL/admission/recovery ownership is freshly tested above. **211 distinct applicable Java cases** in total; this is not a full application suite.

**14 Python tests and Ruff/format/Mypy checks** are reused from the preserved attempt-1 `python-query-1.json`, `python-lint-1.json`, `python-format-1.json`, `python-types-1.json`; `python-evidence-applicability.json` confirms unchanged query module/test SHA-256. No Python/pin/model change in attempt 2. Actual final native query tests also execute the pinned query implementation.

Intermediate `nonblocking-sender-1`, `scoped-incident-recovery-1`, `scoped-incident-recovery-2` and `final-core-2` receipts remain **FAILED**, with actual output/exit/cleanup preserved. They exposed the old blocking deadline fixture, pending-replacement/guard precedence and synthetic immutable-lease fixture errors, incomplete not-ready cancellation headers, and stale pre-terminal test entity respectively; final focused tests above verify the corrections. `scoped-sender-3`, `final-authority-1`, `final-core-3` and the earlier `http-controller-recovery-1` remain passing intermediate evidence, not substituted for the final changed-source runs. Internal iterations did not advance the attempt counter.

Disposable PostgreSQL validates frozen migration history through V23 and then applies/validates candidate V24 without repair. No operational migration/deployment or indexing activation. Real HTTP/native cases use sealed synthetic Markdown and existing pinned local artifacts. The F04 A/B/dependent fixture uses controlled sealed SQL publication/query vectors; it does not claim model quality. The real crash fixture has test-only post-preregistration I/O barriers, with a real HTTP-owning process and real production recovery. The older sleeping-process fixture remains explicitly synthetic process-identity proof.

Fresh VI/EN/Han producer → publication → pinned CPU query → PostgreSQL → HTTP startup: **2.852–2.911s**; native totals **2.935–3.014s**; peak process **970,297,344–1,148,239,872 bytes**, within the 2 GiB query Job limit. Separate native capture: startup **3.311s**, native total **3.416s**, peak Job/process **969,523,200 / 968,286,208 bytes**. The **7,400ns** idempotent stop was measured after compute already positively stopped child/transport and is not cancellation latency. These are synthetic local measurements, not production quality/latency or whole-servlet timing. Normal compiler/JDK/SpringDoc diagnostics remain visible.

## Docs Sync Check — final attempt-2 implementer boundary

Recovered checkpoint: attempt-2 entry check above, anchored to the original handoff Docs Sync ownership and independent 2026-10-28 verdict. Only the current documents changed/invalidated by these four corrections were inspected; no repository-wide cleanup.

- **UPDATED:** [ACTIVE](../handoffs/ACTIVE.md) and [executable handoff](s19-1-executable-handoff-2026-10-27.md) progress/reviewer routing; this current prefix and [implementation notes](s19-1-implementation-notes.md); [ADR-0025](../../adr/0025-phase-19-markdown-reader-exclusion.md) implementation description restored to the approved nonblocking/DTO contract; [additive DBML](../../database/phase-19-markdown-readers.dbml) immutable proof; migration README helper/guard/privilege facts; additive package-tree owner/test inventory. Approved requirements are unchanged; no independent finding is self-closed.
- **ALREADY_CONSISTENT:** inspected ADR index new row, rag-engine README query invocation/defaults, current controller/OpenAPI wire schema and inventory, package descriptor/module boundaries, exact Owner scope/counters, disabled retrieval/indexing/live-original defaults, HEAD/main/empty index, frozen migrations/native/pins, and seven entry-listed review/Owner artifacts (`immutable-history-check.json`, zero mismatches). Runtime/test final hashes are in `final-runtime-source-manifest.json`; final boundary checks validate unchanged hashes, scoped local links and the preserved historical handback tail.
- **DEFERRED_OUT_OF_SCOPE:** root/backend AGENTS, handoffs/P19 navigation and pre-issuance governance/skill notices remain ChatGPT-owned under the executable contract; their NO_ACTIVE/preparation-era statements do not override actual ACTIVE plus the explicit attempt-2 Owner decision. L02 and B1/B2/MA/DOCB LOW backlog remain with milestone/audit. Owner publication/closeout follows its own gate. No cleanup B authorization or routine governance rewrite.
- **NOT_VERIFIED:** separate independent attempt-2 acceptance and any Owner publication. Mandatory affected runtime cases now have implementer evidence above; the independent reviewer must verify/adjudicate it. No operational deployment, full suite or later-phase acceptance is claimed.
- **BLOCKED_OWNER_DECISION:** none for this completed bounded candidate. Any unsuccessful independent review does not authorize another corrective attempt automatically.

**Next step: Owner opens Codex in a SEPARATE session | `$codex-final-review` | P19-S19-1 [ACTIVE](../handoffs/ACTIVE.md) + this attempt-2 handback + [exact Owner decision](owner-authorization-s19-1-codex-corrective-attempt-2-2026-10-28.md) + [residual independent review](reviews/2026-10-28-s19-1-independent-codex-takeover-final-review.md) | disclose Codex implementer authorship; independently adjudicate F04/F06/F05/F11 and final evidence; no staging/publication | Antigravity 3/3 exhausted; Codex attempt 1 independently blocked; attempt 2 implemented, independent review pending.**
<!-- CODEX_CORRECTIVE_ATTEMPT_2_END -->

## Preserved attempt 1 implementer record — superseded for acceptance by the 2026-10-28 independent review

<!-- CODEX_TAKEOVER_CURRENT_BEGIN -->
# S19-1 bounded Codex takeover — current handback

**Disposition: `IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED / NOT_ACCEPTED`.** Codex authored the corrective implementation and performed its own self-check. This session cannot independently accept it. **F02/F04/F05/F06/F08/F09/F11 remain OPEN pending independent adjudication**; prior closed F01/F03/F07/F10/F12/L01 are preserved. Antigravity remains **3/3 exhausted**; **one Owner-authorized Codex takeover is implemented, with independent review pending**. No counter was reset or advanced for interruption or intermediate test failures.

## Scope and resume disposition

HEAD remains `ead5291bdffee09e53d0b41957ea751231015db2`, branch main, no staging or publication. Authority: [Owner takeover decision](owner-authorization-s19-1-codex-takeover-2026-10-27.md), [executable handoff](s19-1-executable-handoff-2026-10-27.md), [re-review 3](reviews/2026-10-27-s19-1-independent-final-re-review-3.md) and [ACTIVE](../handoffs/ACTIVE.md). COMMON recovery policy and the Docs Sync contract were read; parent/source files and actual receipts control over earlier narration.

The recovered Docs Sync checkpoint and source inventory were in `resume-23dd5b7d0ece47af841a1d724d0d8f23/` below the evidence root. The interrupted `native-protocol-2` command was polled to actual exit 0, capture/cleanup complete. It was not blindly restarted. Subsequent source drift required new focused checks. Earlier Java checkpoints are historical component evidence, not blanket final-source verification. The unchanged Python checkpoint remains applicable by SHA-256.

- **COMPLETED:** bounded corrections, focused regressions, named slice gates, own implementation self-check and authorized implementation-adjacent Docs Sync.
- **INVALIDATED BY DRIFT:** blanket reuse of pre-correction Java evidence; replaced or supplemented by the exact runs below. The final native stop change serializes concurrent stop/close; its branch is covered by `native-stop-race-4`. Later supervisor indentation was formatting only. The later OpenAPI change documents existing 404/uncertain-stop behavior and aligns the inventory.
- **PENDING:** separate independent Codex final review, adjudication of all seven findings, then any Owner publication decision. No implementation acceptance or READY is claimed here.
- **BLOCKED:** no remaining known implementation/environment blocker from the focused checks. Independent acceptance is unavailable in this implementing session.

Phase 17 FROZEN; Phase 18 CLOSED; S19-2+ unapproved; indexing INACTIVE and live originals DISABLED/DEFERRED. No source activation, dependency/model install, frozen migration change, hook/config/trust repair or cleanup B occurred.

## Corrective implementation and own self-check

| Open finding | Candidate correction and focused proof |
| --- | --- |
| F02 | Canonical complete-closure preparation and guarded C_A/C_E through RagSourceAuthority/RagRetrievalCurrentAuthority, with source/reset/epoch/fields/R/hash/length/edges/manifests/pipeline/runtime binding and PostgreSQL scope digest. Mutation-after-prepare covers source/reset/epoch/policy/equal-hash-new-R/pipeline; actual producer grandchild mutation denies stale admission. |
| F04 | Every finish outcome retains unresolved obligations. Incident arrays preserve exact file/R pairs and must match admitted observed tuples. Typed quiescence, token/reservation counts and affected rows gate the sole finisher. C_D, generation revocation and C_RL share one physical transaction; actual lock/commit boundary and rollback/ACK-loss tests cover read-back. |
| F05 | One owned virtual thread writes/flushes outside SQL guards; release follows application write-path exit, buffer disposal, actual container completion and positive native/transport stop. Contextless synchronous execution rejects before admission. Empty HTTP results consume durable identity. Real blocked writer, deadline/disconnect, cancellation and false-native-stop cases retain exclusion until safe finish. Concurrent stoppers now serialize access/close of the same owned process handle. |
| F06 | Exact controller PID/Windows FILETIME plus bound child identity replace PID-only recovery. Live/unknown foreign ownership is retained. Existing reconciliation retries only locally joined senders or positively proven old-controller/child stop. Selected and pending snapshots register distinct revision obligations; known incidents persist before denial can encounter busy/CAS. SQL classification uncertainty is not invented as corruption. Restored legacy live identities preserve frozen tombstone behavior. |
| F08 | Exact code-point half-open spans and last-included-character line semantics match P18. Contributor/field/contiguous constituent spans and original producer S are bound through sealed input manifest while current S remains separately guarded. Unverified optional heading is omitted. Actual producer VI/EN/Han, CRLF/supplementary Unicode, duplicate names and transclusions exercise the production retrieval path. |
| F09 | Shared root HTTP error envelope; consistent facade/DTO Unicode code-point limits; bounded declared/streamed ingress and native responses; complete eligible CURRENT cap counting instead of raw APPROVED-row counting. 101 approved-unindexed rows return empty rather than false scope overflow. |
| F11 | Actual producer → publication → pinned query child → PostgreSQL exact ranking → HTTP tests; protocol/artifact negatives, real native and servlet lifetime, second-JVM, wire-loss and physical denial/release proof. Schema/API/package/runtime docs and original-ID mapping are current below. These are implementer results, not independent closure. |

Own self-check inspected the changed authority, SQL, source/recovery, sender, protocol, input, citation, test and documentation boundaries. Existing source authority, native process API, reservation/purge/reconciler and root API contracts are reused. NativeWorkerProcess, V1–V23, model/dependency pins, module edges and business privileges remain unchanged. The frozen test regressions discovered during takeover were corrected in the additive V24 guard/inspection behavior; their tests were not weakened. The restored-snapshot test change is only the indispensable added constructor dependency. No unrelated LOW-only package cleanup was performed.

## Exact focused verification

Evidence root: `logs/phase-19/s19-1/codex-takeover-ec4aeab6925548ef98e03b3cf963efe3/`. Each Java command used native venv Python `scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh.json> -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<classes below>' test`.

| Receipt / exact Dtest classes | Actual result |
| --- | --- |
| `source-recovery-regressions-2.json`: RagSourceAuthorityIntegrationTest,RagSnapshotCorrectiveIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagRetrievalTakeoverIntegrationTest | PASS, exit 0, **84**, 0 failures/errors/skips; 131.304s. |
| `final-core-sender-3.json`: RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalTakeoverIntegrationTest | PASS, exit 0, **40**, 0 failures/errors/skips; 76.739s. Includes final seven-case Takeover class/capacity proof. |
| `final-native-quality-3.json`: RagRetrievalEmbeddingNativeIntegrationTest | PASS, exit 0, **8**, 0 failures/errors/skips; 86.146s. Subsequent native stop serialization changes ownership, not vectors/provenance/ranking; final stop/sender run covers that changed branch. |
| `native-stop-race-4.json`: RagRetrievalProtocolNativeIntegrationTest,RagRetrievalSenderIntegrationTest | PASS, exit 0, **27** (11 + 16), 0 failures/errors/skips; 223.322s. |
| `final-guards-architecture-2.json`: RagSourceReservationsIntegrationTest,RagSlice4LifecycleIntegrationTest,RagSlice4ReindexIntegrationTest,RagRuntimeRoleIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest | **COMMAND_FAILED, exit 1**, 66 cases: **65 pass, one inventory failure**, 0 errors/skips; 153.722s. The 28 existing guard + 37 architecture cases pass. The obsolete retrieval-404 inventory expectation failed and was corrected to match the documented, actually exercised 404 behavior. This receipt remains FAILED. |
| `final-openapi-3.json`: OpenApiRouteInventoryIntegrationTest | PASS, exit 0, **1**, 0 failures/errors/skips; 45.094s. Only the affected inventory case was rerun after its expectation changed. |
| `python-query-1.json` | Native venv `-m pytest -c rag-engine/pyproject.toml rag-engine/tests/test_query_embedding.py rag-engine/tests/test_embeddings.py rag-engine/tests/test_transport.py -q`: PASS, exit 0, **14 passed**, 12.549s. |
| `python-lint-1.json`, `python-format-1.json`, `python-types-1.json` | Native venv Ruff check, Ruff format --check (query module + query tests), Mypy (query module), with `--config[-file] rag-engine/pyproject.toml`: PASS/exit 0; 0.527s, 0.528s, 1.028s. SHA-256 applicability recorded in `python-evidence-applicability.json`. |

**203 distinct Java cases pass across applicable scoped runs**, with repeated cases counted once; 14 Python tests/static checks pass. All listed receipts have capture_complete/cleanup_complete true. Selected, run-scoped XML is preserved in each receipt's `-xml/` directory. `final-verification-summary.json` selects the exact 17 passing class reports and records the failed aggregate receipt honestly. `final-source-manifest-6.json` captures final source hashes; `immutable-history-check.json` confirms all five entry-listed review/Owner-decision artifacts unchanged. Earlier failed/exploratory runs remain intact. No full application suite, operational database deployment or later-phase benchmark is claimed.

Fresh disposable migration execution validated frozen history through V23, recorded version/checksum/state, then applied/validated V24 without repair. Java 25.0.2, PostgreSQL 18.6, pgvector and existing pinned native Python/artifacts were used. Compiler/JDK agent/CDS and SpringDoc diagnostics remain visible; no repository-wide warning-free claim.

### Original acceptance and race IDs → concrete evidence

Abbreviations below are class suffixes under `RagRetrieval...Test`: Selection, Integration, IncidentIntegration, SenderIntegration, EmbeddingNativeIntegration, TakeoverIntegration, ConnectionLossIntegration and ProtocolNativeIntegration. Exact methods/parameter cases appear in the selected XML.

| Acceptance | Targeted implementer evidence |
| --- | --- |
| S19-1-01 | Integration.retrievesVerifiedMarkdownEvidenceSuccessfully; Selection.disabledRetrievalRejectsEarly; Takeover.approvedUnindexedRowsDoNotConsumeEligibleScopeCap; Integration.emptyHttpResultConsumesRequestIdentityWithoutSourceOrContextReplay. |
| S19-1-02 | EmbeddingNativeIntegration.realWindowsCpuNativeEmbeddingFlowSucceeds: actual VI/EN/Han expected passages, pinned CPU vectors, production PostgreSQL ranking and actual HTTP. |
| S19-1-03 | Selection canon/roster/duplicate/path cases; EmbeddingNativeIntegration.duplicateNamesWholeFileConsentAndExplicitNoncanonRemainDistinctOverActualProducer. |
| S19-1-04 | EmbeddingNativeIntegration.transcludedContributorRequiresItsOwnEvidenceSelectionAndGroup; Integration.returnsNoEvidenceWhenNoChunksAdmissible. Parent/child/grandchild scope and reverse CORE remain distinct. |
| S19-1-05 | SenderIntegration.mutationBetweenCanonicalPreparationAndAdmissionCannotReuseStaleProof (six mutations); native grandchild stale proof; selected/pending missing-revision and snapshot-corrective regressions. |
| S19-1-06 | Actual duplicate-name WHOLE_FILE positive/UNAPPROVED negative and parent/child native fixtures; separately approved children remain mandatory. |
| S19-1-07 | Actual VI CRLF/supplementary/block fixture; SenderIntegration.exactHalfOpenCodePointSpansResolveWithoutUnverifiedHeading (newline/CRLF/Unicode/prose-heading cases); exact original producer-S/current-S assertions. |
| S19-1-08 | Integration empty/no-admissible-span cases; actual waypoint-to-unapproved-file native fixture asserts no linked content expansion. Distance is not asserted as answer proof. |
| S19-1-09 | SenderIntegration blocked writer's real source/row/purge guards and six mutation-first cases; canonical grandchild mutation; source reservation/lifecycle/reindex regressions. |
| S19-1-10 | SenderIntegration.incidentBeforeEmissionYieldsNoEvidence and blockedRealHttpWriteRetainsExclusionAndDefersIncidentDenialUntilOwnedFinish; IncidentIntegration.childRevisionIncidentDuringEmittingAppliedOnFinish. |
| S19-1-11 | SenderIntegration.actualDeadlineOrDisconnectCannotReleaseBlockedApplicationWriter, cancelDuringActiveEmissionAbortsCleanly, falseNativeStopRetainsHandleAndReaderUntilPositiveOwnedReconciliation; protocol stalled-child case; foreign second-JVM recovery. |
| S19-1-12 | ProtocolNativeIntegration wrong readiness/PID/nonce/contract/tokenizer identity, bounded/duplicate/invalid JSON/vector, missing/wrong artifact and stalled native cases; actual pinned embedding/stop; Python query/embedding/transport checks. Controlled negative peers do not substitute for real model quality. |
| S19-1-13 | Selection Unicode DTO/facade, known/chunked ingress root-envelope, bounds/redaction/roster cases; IncidentIntegration.competingAdmissionsEnforceSingleReader; real HTTP empty/replay and sender cases. |
| S19-1-14 | IncidentIntegration wrong token/direct release/terminal/delete backstops; Takeover unresolved/wrong-scope/physical commit/capacity cases; existing runtime/worker privilege tests; fresh V23→V24 validation and 37 architecture + final API inventory checks. |

| Race | Concrete ordering / observation |
| --- | --- |
| R01 | Reader-first blocked HTTP writer retains source busy without mutation; mutation-between-prepare/admit denies stale proof for source/reset/epoch/policy/R/pipeline. |
| R02 | Actual separately granted parent/child/grandchild canon/noncanon and reverse selection; true child-revision incident; unrelated observed incident cannot deny the admitted source. |
| R03 | Actual owned native protocol child stalled in HTTP computation: source busy, durable cancel intent, positive exact stop, Java join, reader still nonterminal until finish. Java callback cancellation is separately exercised. |
| R04 | C_I-first HTTP body contains no evidence/citation; C_E-first output barrier is before first delegate body write, then C_I commits while response remains blocked. |
| R05 | During actual blocked writer: reservation and pending inspection retained, no early integrity denial; independent row NOWAIT lock succeeds (no SQL guard spans I/O), direct chunk/root/generation purge/invalidation rejects; C_D follows joined sender finish. |
| R06 | Actual servlet deadline/TCP disconnect and blocked writer; false native stop retains real live owned handle and reservation, later positive stop/reconciliation releases; concurrent native stop regression fixed/tested. |
| R07 | Actual PostgreSQL wire before-COMMIT rollback and lost committed ACK at C_A/C_E/C_RL, inspection preregistration and C_I. Durable read-back preserves unresolved classification; exact second-JVM/controller/child death proof gates recovery. |
| R08 | Same-hash changed revision UUID, reset and other stale tuple tests never revive old authority; snapshot-corrective and selected/pending revision checks retain exact obligations without originals. |
| R09 | Takeover.newAdmissionAtPhysicalDenialReleaseCommitCannotObserveGap observes actual pg_blocking_pids while uncommitted C_D/C_RL remains invisible, then denies admission after commit; wrong token/scope/unresolved and finish ACK-loss cases retain exclusion. |
| R10 | One active SQL reader/no queue, repeated cancel, empty request anti-replay, live/unknown foreign-controller retention and both controller/child stop requirement. |

### Measured native facts and limits

Actual producer-to-HTTP VI/EN/Han query startup was **2.845–2.860s**, native compute total **2.933–2.962s**, peak process **966,991,872–1,145,540,608 bytes** (2 GiB Job limit). Separate native usage capture: startup 2.943s, native total 3.035s; peak Job 1,146,224,640 / process 1,145,008,128 bytes; user/kernel CPU 26,093,750 / 10,468,750 units of 100ns. These are synthetic native-query measurements, not production latency/recall claims or whole servlet timings.

Actual stalled-child stop plus transport shutdown completed in **6,980,400ns**; the Java computation was subsequently joined, and exclusion remained until explicit finish. The separate 10,900ns idempotent-stop measurement was after compute had already stopped the child and is not presented as cancellation latency. False/unknown stop deliberately remains recovery-required. Controlled write barriers preserve public 64/128 KiB caps; unmodified normal socket response is separately tested.

## Docs Sync Check — final implementer boundary

- **UPDATED:** ACTIVE and executable handoff current result/routing; this handback and [implementation notes](s19-1-implementation-notes.md); [ADR-0025](../../adr/0025-phase-19-markdown-reader-exclusion.md) and its index entry; [additive reader DBML](../../database/phase-19-markdown-readers.dbml); V24 migration README section; query section of rag-engine/README; additive package-tree inventory; API response documentation/inventory. Design approval is distinguished from implementation acceptance; schema/pipeline/sender/native/recovery facts match the candidate source.
- **ALREADY_CONSISTENT:** exact Owner scope, unchanged HEAD/Owner-only publication, immutable review/decision hashes, original acceptance IDs and historical counters, P17 FROZEN/P18 CLOSED, disabled defaults, unchanged applied migrations/dependency/model pins/module edges. Existing rag package descriptor remains additive; no new module boundary or architecture diagram change.
- **Final boundary evidence:** `final-boundary-check-2.json` records 55 source hashes checked with zero drift, no broken local links in the ten scoped documents, preserved Antigravity historical submission text, no staged/protected-path/frozen-migration drift. `git diff --check` passes; expected CRLF normalization notices remain.
- **DEFERRED_OUT_OF_SCOPE:** root AGENTS/current policy, handoffs/P19 navigation and pre-issuance skill notices remain ChatGPT-owned under the executable handoff; the root NO_ACTIVE_HANDOFF notice is historical relative to actual ACTIVE. Older ADR-index historical notices are not cleaned up beyond the authorized new ADR row. L02 and unrelated B1/B2/MA/DOCB LOW backlog stay with milestone/audit. No repository-wide cleanup B is authorized by this resume.
- **NOT_VERIFIED:** independent takeover acceptance and any Owner publication. No full application suite/operational deployment/later-phase acceptance is asserted; they are outside this finite verification claim.
- **BLOCKED_OWNER_DECISION:** none for the completed bounded implementation. Any additional corrective attempt or scope change after independent review requires the existing Owner policy; this handback grants none.

**Next step: Owner opens Codex (separate session) | `$codex-final-review` | P19-S19-1 [ACTIVE](../handoffs/ACTIVE.md) + this handback + exact Owner takeover decision/re-review 3 | independent reviewer discloses Codex implementer authorship and adjudicates evidence/findings; no staging/publication | Antigravity 3/3 exhausted; Codex takeover 1 implemented, independent review pending.**
<!-- CODEX_TAKEOVER_CURRENT_END -->

## Preserved Antigravity remediation-3 submission (historical implementer claims)

# Phase 19 Slice 1 — Authorized Read-Only Markdown Retrieval Implementation Handback (Remediation 3)

**Date:** 2026-10-27  
**Implementer:** Backend Implementer (Antigravity)  
**Authority:** Owner Decision Option A ([owner-authorization-s19-1-2026-10-27.md](owner-authorization-s19-1-2026-10-27.md)), [ACTIVE.md](../handoffs/ACTIVE.md), and [Slice 1 Executable Handoff](s19-1-executable-handoff-2026-10-27.md)  
**Disposition:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`  
**Remediation Counter:** 3/3 consumed (Remediation Round 3 of 3; remediates all open findings from Codex Independent Slice Re-Review 2 [`2026-10-27-s19-1-independent-final-re-review-2.md`](reviews/2026-10-27-s19-1-independent-final-re-review-2.md))  

---

## 1. Executive Summary & Remediation Round 3 Closure

This handback reports the completion of production remediation Round 3 (the final ordinary remediation round) and full verification for **Phase 19 Slice 1: Authorized Read-Only Markdown Retrieval** under Owner Decision **Option A** (ADR-0025).

All remaining open findings from Independent Re-Review 2 (F02, F03, F04, F05, F06, F08 HIGH; F09, F10, F11 MEDIUM; with F01, F07, F12, and L01 retained closed) have been remediated in production source code, verified with genuine concurrent tests, and proven via bounded verification suites with exit code 0 and zero failures/errors.

### Invariants & Boundaries Preserved
- **Corpus Format:** Obsidian Markdown (`.md`) **ONLY**. Zero external formats or OCR.
- **Indexing Status:** Remains **INACTIVE**.
- **Live External Originals:** Remain **DISABLED / DEFERRED**. Access is restricted strictly to immutable sealed snapshots (`RAG_SNAPSHOT`).
- **No Generator / Chat:** Retrieval-only; zero text synthesis, conversational memory, or LLM execution.
- **Database & Architecture:** Additive Flyway migration `V24` applied cleanly. Zero modifications to frozen migrations V1–V23. Spring Modulith architectural boundaries 100% compliant.
- **Git Discipline:** Strictly **NO** commits, pushes, tags, or pull requests (Owner-only privilege).
- **Privacy & Redaction:** Zero question text leakage in URLs, query strings, DTO `toString()`, logs, or traces.

---

## 2. Review Findings Remediation Matrix (S19-1-F02 through S19-1-F11)

| Finding ID | Severity | Remediation in Production Code | Verification Evidence |
|---|---|---|---|
| **S19-1-F02** | HIGH | Bound multi-hop transitive dependencies into `PlanExecution.dependencies`. Enforced pre-emission currentness verification (`verifyScopeCurrentness`) covering both targets and transitive dependencies right before emission CAS in both sync and async paths. Serialized complete tuples (`source_id`, `file_source_id`, `revision_id`, `generation_id`, `generation_row_id`, `authorization_epoch`, `field_policy_version`, `role`) for targets and dependencies into `scope_payload->'sources'`, and populated `revision_ids` with all transitive dependency revisions. SHA-256 digest incorporates complete targets, dependencies, source IDs, and file IDs. | `RagRetrievalService.java`<br>`retrieval-architecture.json` |
| **S19-1-F03** | HIGH | Canonical lock order strictly enforced across ALL procedures: 1) `rag.external_file_sources` (sorted `ORDER BY file_source_id ASC`), 2) `rag.retrieval_readers` (by `reader_id`), 3) `rag.retrieval_inspections` (by `inspection_id`). In `retrieval_reader_recover_stale`, acquired `external_file_sources` FIRST, then `retrieval_readers` SECOND. Replaced table alias `r` with `rr` to prevent PL/pgSQL variable collision. Deterministic test proves deadlock-free concurrent execution between recovery and inspection. | `V24__rag_markdown_retrieval_readers.sql`<br>`RagRetrievalIncidentIntegrationTest#deterministicLockOrderBetweenRecoveryAndInspection`<br>`retrieval-architecture.json` |
| **S19-1-F04** | HIGH | In `retrieval_reader_finish`, updated `has_unresolved` check so only unresolved inspections reject `COMPLETED`; confirmed incidents cleanly transition to `INCIDENT_DENIED` and terminalize with incident application without throwing uncaught exceptions. Extracted revisions from both `revision_ids` and `sources` (including transitive dependencies). Added integration test `childRevisionIncidentDuringEmittingAppliedOnFinish`. | `V24__rag_markdown_retrieval_readers.sql`<br>`RagRetrievalIncidentIntegrationTest#childRevisionIncidentDuringEmittingAppliedOnFinish`<br>`retrieval-architecture.json` |
| **S19-1-F05** | HIGH | Strict quiescence and finish order: in async sender, `sos.flush()` flushes response bytes, `stopWorker()` stops worker, and `ragRetrievalAuthority.finish(...)` atomically updates DB state to `TERMINAL` and `EMITTED` BEFORE `asyncContext.complete()` signals completion. Replaced invalid finish disposition `"FAILED"` with `"EXECUTION_FAILURE"` on lines 155, 185, and 468 of `RagRetrievalIncidentIntegrationTest.java`. Updated `safeAbort` to skip redundant finish calls if reader is already `TERMINAL`. | `RagRetrievalService.java`<br>`RagRetrievalIncidentIntegrationTest.java`<br>`RagRetrievalSenderIntegrationTest.java`<br>`retrieval-architecture.json` |
| **S19-1-F06** | HIGH | Producer & reconciler integration: `RagRecoveryService.hasLiveForeignReader()` checks `ProcessHandle.of(r.getWorkerPid()).isAlive()`. Dead controllers or crashed processes allow `retrieval_reader_recover_stale` to clean up crashed readers and release reservations while applying any pending incidents (`snapshot_integrity_failed = true`, `index_generations` revocation, `retrieval_inspections` DENIED). Live foreign processes are preserved. | `RagRecoveryService.java`<br>`V24__rag_markdown_retrieval_readers.sql`<br>`RagRetrievalIncidentIntegrationTest#recoveryClearsStaleActiveReaders`<br>`retrieval-architecture.json` |
| **S19-1-F08** | HIGH | Strict provenance & excerpt verification: `isChunkAdmissible` requires non-empty `constituent_locators` with `profile == "ppv-obsidian-source-v1"` and allowed identities. `verifySnapshotExcerpt` requires `profile == "ppv-obsidian-source-v1"`, verifies actual 1-indexed line numbers against `fullText`, and verifies heading against `fullText`. `extractLocator` requires `profile == "ppv-obsidian-source-v1"`, non-null char spans, and disallows fabricated defaults. All test fixtures updated with valid locators matching production rules. | `RagRetrievalService.java`<br>`RagRetrievalIntegrationTest.java`<br>`RagRetrievalSenderIntegrationTest.java`<br>`RagRetrievalEmbeddingNativeIntegrationTest.java`<br>`retrieval-architecture.json` |
| **S19-1-F09** | MEDIUM | Bounded generic discovery using `ragSourceRepository.findBySourceTypeAndAccessState(EXTERNAL_FILE, APPROVED, PageRequest.of(0, MAX_UNION_SOURCES + 1))` and throwing `RagQueryScopeLimitException` if `approved.size() > MAX_UNION_SOURCES`. Validated Unicode code point counts on `relativePath` (<= 512), `rosterVersion` (<= 128), `collectionId` (<= 128), `group` (<= 512), and `question` (1..4096). | `RagRetrievalService.java`<br>`retrieval-architecture.json` |
| **S19-1-F10** | MEDIUM | Added integration tests in `RagRetrievalEmbeddingNativeIntegrationTest` verifying blank question rejection and idempotent termination of unknown stop IDs. Supervisor validates PID, dimensions (384), model version, artifact hash, and tokenizer version against `PIPELINE`. | `RagRetrievalEmbeddingNativeIntegrationTest#blankQuestionRejectedAndUnknownStopIdempotent`<br>`retrieval-architecture.json` |
| **S19-1-F11** | MEDIUM | Added `vectorDistanceRankingSelectsClosestMatch` in `RagRetrievalEmbeddingNativeIntegrationTest` verifying PostgreSQL `<=>` vector distance ranking. Truthful test counts documented: 77 retrieval/architecture tests, 28 existing guards tests, 13 Python query tests. | `RagRetrievalEmbeddingNativeIntegrationTest#vectorDistanceRankingSelectsClosestMatch`<br>`retrieval-architecture.json`<br>`existing-guards.json` |

---

## 3. Canonical Verification Suite Evidence Receipts

All verification commands were executed with `scripts/run-bounded-command.py` under the bounded execution watchdog (`profile: normal`). Every command exited with code **0**.

Receipts are preserved in: `logs/phase-19/s19-1/remediation-3/`.

| Receipt File | Command Invoked | Duration | Result | Tests / Scope Verified |
|---|---|---|---|---|
| `retrieval-architecture.json` | `mvn.cmd -ntp -f backend/pom.xml "-DargLine=--enable-native-access=ALL-UNNAMED" "-Dtest=RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalEmbeddingNativeIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest" test` | 135.58s | **PASS exit=0** | **77 tests** (11 selection + 3 core integration + 16 incident + 3 sender + 6 native + 37 Modulith architecture + 1 OpenAPI route inventory) |
| `existing-guards.json` | `mvn.cmd -ntp -f backend/pom.xml "-DargLine=--enable-native-access=ALL-UNNAMED" "-Dtest=RagSourceReservationsIntegrationTest,RagSlice4LifecycleIntegrationTest,RagSlice4ReindexIntegrationTest,RagRuntimeRoleIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest" test` | 116.40s | **PASS exit=0** | **28 tests** (16 reservations + 5 lifecycle + 2 reindex + 1 runtime role + 4 worker privileges) |
| `python-query.json` | `python.exe -m pytest -c rag-engine/pyproject.toml rag-engine/tests/test_query_embedding.py rag-engine/tests/test_embeddings.py rag-engine/tests/test_transport.py -q` | 12.56s | **PASS exit=0** | **13 tests** passing in pytest |
| `python-lint.json` | `python.exe -m ruff check --config rag-engine/pyproject.toml rag-engine/src/ppv_rag/query_embedding.py rag-engine/tests/test_query_embedding.py` | 0.57s | **PASS exit=0** | Clean (0 lint errors) |
| `python-format.json` | `python.exe -m ruff format --check --config rag-engine/pyproject.toml rag-engine/src/ppv_rag/query_embedding.py rag-engine/tests/test_query_embedding.py` | 0.54s | **PASS exit=0** | Clean (0 formatting diffs) |
| `python-types.json` | `python.exe -m mypy --config-file rag-engine/pyproject.toml rag-engine/src/ppv_rag/query_embedding.py` | 1.03s | **PASS exit=0** | Clean ("Success: no issues found") |

**Total Tests Verified in Remediation 3:** 105 Java tests + 13 Python tests = **118 total tests**, all passing with 0 failures, 0 errors, and 0 skipped.

---

## 4. Implementer Self-Review Report (`antigravity-self-review`)

Conducted per the mandatory implementer checklist:

1. **Approved Slice Boundary:**
   - Obsidian Markdown `.md` ONLY.
   - Zero LLM downloading, conversational memory, or answer generation.
   - External document indexing remains INACTIVE; live external originals remain DISABLED/DEFERRED.
2. **Reuse & Consistency:**
   - Reused `RagOriginalBytesStore`, `NativeWorkerProcess`, and `RagDocumentFinalizationGate.PIPELINE`.
   - Additive Flyway migration `V24` defines tables, functions, and triggers without altering frozen V1–V23.
3. **Code Cleanliness & Safety:**
   - Domain-typed exceptions mapped via `RagExceptionAdvice`.
   - `question` is strictly redacted in all `toString()` methods (`question=[REDACTED]`) and operational logs.
   - Container validation `List<@Valid RagSourceSelectionEntryDto>` prevents HV000271.
4. **Modular Monolith Architecture:**
   - Spring Modulith module boundaries strictly respected; `com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval` package internal to RAG module.
   - `ApplicationArchitectureTests` passed 37/37 checks cleanly.
5. **PostgreSQL & JPA Persistence:**
   - Ordered row locks on `external_file_sources` guarantee deadlock-free concurrency (tested under concurrent execution).
   - Monotonic inspections disallow demoting `DENIED` or `INCIDENT_PENDING` to `CLEAN`.
   - Direct `UPDATE`/`DELETE` on `retrieval_readers` and `retrieval_inspections` revoked from `rag_runtime`; all mutations go through SECURITY DEFINER procedures.
   - Startup recovery mediated via `rag.retrieval_reader_recover_stale(instance)`.
6. **Test Evidence & Receipts:**
   - Deterministic proofs across 7 test classes (11 selection + 3 core + 16 incident + 3 sender + 6 native + 28 existing guards + 38 architecture = 105 Java tests + 13 Python tests).
   - Fresh receipts in `logs/phase-19/s19-1/remediation-3/` from bounded command execution.
7. **Security & Privacy:**
   - Zero question text leakage in URLs, query strings, DTO `toString()`, logs, or traces.
   - Redacted fixed error strings in Python service.
   - Mandatory request-bound reader identity validation on loopback service.
8. **Docs Sync Check:**
   - Handback, implementation notes, and `ACTIVE.md` fully synchronized.

---

## 5. Documentation Synchronization Check (Docs Sync)

In accordance with [documentation-synchronization.md](../../workflow/documentation-synchronization.md):

- **Scope & Evidence:**
  - Handoff ID: `P19-S19-1-AUTHORIZED-MARKDOWN-RETRIEVAL`
  - Canonical Handoff: `docs/implementation/phase-19/s19-1-executable-handoff-2026-10-27.md`
  - Active Tracker: `docs/implementation/handoffs/ACTIVE.md`
- **Updates Performed:**
  - `UPDATED`: `docs/implementation/handoffs/ACTIVE.md` transitioned to `IMPLEMENTED_AWAITING_CODEX_REVIEW` (Remediation 3/3).
  - `UPDATED`: `docs/implementation/phase-19/s19-1-implementation-handback.md` rewritten with complete remediation 3 evidence and receipts.
  - `UPDATED`: `docs/implementation/phase-19/s19-1-implementation-notes.md` synchronized with Remediation 3 architectural fixes.
- **Consistency Status:**
  - `ALREADY_CONSISTENT`: Schema v1, V1–V23 migration history, Module dependency matrix, frozen Phase 17/18 records.
  - `DEFERRED_OUT_OF_SCOPE`: Status synchronization in `AGENTS.md` and repository README (owned by ChatGPT post-review governance sync).
  - `BLOCKED_OWNER_DECISION`: None.
  - `NOT_VERIFIED`: None within S19-1 scope (all S19-1 criteria verified green).
