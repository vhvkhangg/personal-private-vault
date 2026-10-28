# Independent slice review — P19-S19-1 Codex corrective attempt 2

- **Date / reviewer / mode:** 2026-10-28 / Codex, separate from the implementing Codex session / `SLICE`.
- **Disposition: CHANGES_REQUESTED / STOPPED / OWNER_DECISION_REQUIRED.** **F04 HIGH and F11 MEDIUM now CLOSED. F06 HIGH and F05 MEDIUM remain OPEN/blocking**, narrowed to the two independently reproduced failures below. No production or implementation-test source was corrected by this review.
- **Authority:** [attempt-2 Owner decision](../owner-authorization-s19-1-codex-corrective-attempt-2-2026-10-28.md), [executable handoff](../s19-1-executable-handoff-2026-10-27.md), [attempt-1 independent review](2026-10-28-s19-1-independent-codex-takeover-final-review.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md), [final-review skill](../../../../.agents/skills/codex-final-review/SKILL.md).
- **Entry:** main/HEAD `ead5291bdffee09e53d0b41957ea751231015db2`; 75 unstaged candidate paths; empty index. All **58** files in the implementer's final runtime/source manifest match their SHA-256. The review hashes and inventories the actual candidate; it does not treat the old preparation's clean-entry statement as current Git status.
- **Counters:** Antigravity **3/3 EXHAUSTED**; Codex attempt 1 independently blocked; **separately authorized corrective attempt 2 implemented and independently blocked**. No automatic attempt 3, Antigravity round 4, acceptance, activation or publication. The attempt-2 decision expressly requires another Owner decision if blocked again.
- **Preserved boundaries:** all earlier reviews/Owner decisions/raw receipts remain unchanged. F01/F02/F03/F07/F08/F09/F10/F12/L01 remain CLOSED. L02 LOW remains deferred/nonblocking. P17 FROZEN, P18 CLOSED, S19-2+ unapproved, indexing INACTIVE, live originals DISABLED/DEFERRED.

## User requirement and simplest-mechanism check

The four authorized corrections use the existing RAG owners and finite reader/inspection mechanism. The new snapshot-inspection owner records immutable recovery proof and reconciles the original UUID; actual source/SQL backstops, contributor-scoped generation denial and a Servlet nonblocking sender are present. No new model, dependency, business privilege, queue, scheduler or generic framework is required or introduced.

The original collateral invalidation and missing production recovery proof are independently resolved. Two boundary failures remain: inspection fencing is checked before, rather than inside, the final existing currentness transaction; cancellation can return an empty HTTP error before emission. Correct these within existing owners and transport contracts. No new feature or broader benchmark/audit is requested.

## Finding register

Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/`. V24 is `backend/src/main/resources/db/migration/V24__rag_markdown_retrieval_readers.sql`.

| ID | Severity / status | Independent adjudication |
| --- | --- | --- |
| **F04** | HIGH / **CLOSED** | V24:531–543 and :750–755 revoke only generations whose own contributing manifest contains the exact incident file/revision. Fresh `incidentRevokesOnlyAffectedRootAndTrueDependentWhileIndependentRootRemainsCurrent` proves A/true parent revoked, unrelated B CURRENT/integrity false, and actual HTTP evidence from the unchanged B generation. Exact token, no-gap physical commit, affected-row and wire-loss tests remain green. |
| **F06** | HIGH / **OPEN, narrowed** | Standalone predicates, ordinary reserve/lock/release/promotion/purge backstops, immutable proof, original-operation reuse and production recovery are corrected. The remaining final-guard race still returns CURRENT after an unresolved inspection wins before that guarded transaction. Exact reproduction and minimal correction below. |
| **F05** | MEDIUM / **OPEN, narrowed** | `RagRetrievalService:346–390` now uses WriteListener/readiness and callback/buffer quiescence; the actual controller supplies `RagRetrievalResponseDto::fromResponse`, used at :252–253. These original mechanism/adapter defects are fixed. A distinct remaining error-path defect at :175–185 and :268–281 returns HTTP 409 with zero body when canceled before C_E. No new early-release/native-stop defect is asserted. |
| **F11** | MEDIUM / **CLOSED** | Fresh two-case real HTTP-owning Spring controller crash/new-owner recovery and the two production reader-free SQL rollback/ACK-loss cases now discover/classify the original committed inspection UUID. Proof is unchanged; original obligations fence reserve until safe disposition, and the new owner serves HTTP evidence. No test-only manual classification substitutes for these production recovery cases. Closure of this evidence finding does not waive F05/F06. |
| Protected closed findings | **CLOSED retained** | F01/F02/F03/F07/F08/F09/F10/F12/L01: source identity, full retrieval admission/emission authority, lock-order repair, least privilege, exact locators, limits/envelope/Unicode, native protocol and redaction remain source-applicable and covered by focused regression/native evidence. No demonstrated regression reopens them. |

**L02 LOW / OPEN / nonblocking retained:** `internal/web` root with the ingress filter still lacks `package-info.java`. Milestone/audit backlog only; no LOW-only correction or additional attempt is authorized. Other prior LOW backlog remains unchanged.

### F06 HIGH — recheck the inspection predicate under the final currentness guard

**Locations:** `RagGenerationState.java:80` checks the inspection before its final transaction at :115–116. `RagSourceAuthority.java:115–116,157–158` checks it during preparation. Its final `guardPrepared` path (:288 onward, current-read external locks at :329–335) refreshes/locks source metadata but does not recheck the inspection predicate. Inspection registration now correctly uses the same graph ordering slot (V24:347), so both commit orders can be tested deterministically.

**Fresh real PostgreSQL/Spring reproduction:**

1. Create a valid selected snapshot and CURRENT generation with the existing sealed synthetic fixture.
2. An independent runtime connection holds the graph advisory transaction lock. Start actual production `RagGenerationState.state(generation)` on another thread. Observe its final guard blocked on that lock using PostgreSQL lock activity, after its preparation/preflight checks.
3. The first connection preregisters an exact file/revision UNRESOLVED inspection, with the actual controller identity, and **commits** before releasing the graph guard.
4. Production currentness resumes and returns **CURRENT**, while the committed inspection remains **UNRESOLVED**.

Observed receipt output: `inspection_commits_while_currentness_waits_at_guard currentness=CURRENT inspection=UNRESOLVED`. There is no mocked authority/currentness or sleep establishing the ordering. Running production reconciliation afterward classifies the **same** operation CLEAN, which is credited as fixed recovery rather than evidence that the preceding CURRENT result was valid.

**Impact:** the existing canonical status can certify an unresolved source as CURRENT despite the inspection winning the final authority ordering. This violates the explicit shared admission/currentness fence and attempt-2 F06 requirement. This probe does **not** demonstrate retrieval body leakage: retrieval's additional SQL C_A/C_E predicates remain in place, and F02 stays closed.

**Minimal correction:** re-evaluate the exact pending/unresolved predicate within the canonical current-read guard while graph/external/source guards are held, before returning a fresh currentness proof. Preserve source/revision specificity and unrelated-sibling behavior. Extend an existing authority/currentness test with this physical two-connection ordering and its reversed control. No architecture, privilege, cap or frozen-contract change is required.

### F05 MEDIUM — canceled pre-emission HTTP response loses the root error envelope

**Locations:** `RagRetrievalService.java:175–185` resets an uncommitted stopped response, sets a failure status and `Content-Length: 0`, then completes it. In `runSender`, :268–275 skips the existing error-envelope output pump when `ctx.stopRequested` is true. The finalizer then completes the empty response. The enabled DTO/nonblocking success path itself is corrected.

**Strong fresh HTTP reproduction:** a valid explicit selected source is admitted; a controlled query-vector computation barrier pauses it **before C_E**. The real HTTP response has emitted no evidence. Start the actual cancellation operation, receive the original retrieval response, release/join the computation, and observe successful terminal cancellation. Result:

`before_C_E_cancel emission_started=false retrieval_http=409 body_bytes=0 root_error_present=false terminal_cancel=CANCELLED`

The query-vector override isolates HTTP lifecycle; it is not claimed as native quality/stop proof. The HTTP server, bearer authentication, durable reader, stop/finish transactions, response and container completion are real. A separate not-ready/pre-first-byte case also returns an empty 409, but the pre-C_E result alone establishes the defect without relying on interpretation of already-authorized emission.

**Impact:** callers receiving a normal completed HTTP 409 cannot read the mandatory `ApiResponse.error.code=RAG_RETRIEVAL_CANCELLED`. The executable API table requires the structured cancellation result before body commitment; [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md) and the shared HTTP contract require the root response family. `s19-1-implementation-notes.md:33` explicitly permits empty uncommitted failure responses, but the Owner approved **no** sender/wire contract amendment or waiver. This is an actual runtime error-path defect, not a request for a new sender abstraction.

**Minimal correction:** preserve the bounded canonical non-evidence cancellation/error envelope on the pre-emission path using the existing adapter/output owner, without reviving evidence, releasing before quiescence or appending JSON to a committed body. Distinguish a positively aborted/unwritable transport from a normally completed structured HTTP error. Add the pre-C_E authenticated HTTP assertion (status **and** root code/body) to an existing sender test; retain not-ready/late-callback, disconnect/deadline and failed-stop safety. Synchronize the implementation notes to the unchanged approved contract. No blocking-writer fallback or new policy is authorized.

## Acceptance and race applicability

| Original scope | Result / evidence |
| --- | --- |
| S19-1-01–04, 06–08, 12 | PASS within the existing finite retrieval slice: generic/explicit/empty, multilingual native expected passages, consent/roster/contributors and exact locators; no generated-answer claim. |
| S19-1-05 | **FAIL, F06:** stationary pending-inspection denial and recovery pass, but inspection-first final-guard ordering still returns CURRENT. |
| S19-1-09–10, 14 | Supplied reader/mutation/incident/SQL backstop cases PASS, including corrected A/B/dependent scope and physical C_D/C_RL boundary. This does not waive the F06 canonical currentness race. |
| S19-1-11 | **FAIL, F05:** positive nonblocking/native/context/callback lifetime and actual crash recovery pass; pre-emission cancellation response is not the required structured error. |
| S19-1-13 / public error contract | **FAIL for the F05 cancellation branch**; normal ingress/Unicode/roster/bounds/busy/error-envelope cases pass. |
| R01–R05, R08–R10 | Existing exact-scope, mutation, native-consumption, physical HTTP/incident, no-gap and single-reader controls remain passing in applicable tests. Add the exposed F06 ordering to canonical currentness coverage; do not infer its PASS from C_A/C_E. |
| R06 | Callback/transport/native-stop and not-ready cancellation safety PASS; public pre-emission error behavior FAIL under F05. |
| R07 | Original missing production reconciliation proof is now PASS: reader-free rollback/ACK loss plus actual HTTP controller prepare/reader crash and new production owner, original UUID/proof retained. This closes F11 only. |

Future answer generation, answer/abstention benchmarks, catalog recommendations, OCR/other formats/GPU/live originals and broader audits remain N/A/unapproved. No new requirement or full-suite gate is imposed.

## Verification and preserved evidence

Fresh root: `logs/phase-19/s19-1/codex-independent-attempt-2-review-1793160328113/`. Contains entry/source hashes, changed-path and recursive-tree inventory, exact selected Surefire XML, probe sources/classpath/compile receipts, evidence applicability and final boundary checks. No old receipt/log was overwritten.

Every fresh Maven command used native `.venv-d17/Scripts/python.exe scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <unique.json> -- mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<exact classes below> test`.

| Fresh receipt | Exact classes / actual result |
| --- | --- |
| `core-authority.json` | RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalTakeoverIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagSourceAuthorityIntegrationTest,RagSnapshotCorrectiveIntegrationTest — **124**, zero failures/errors/skips, exit 0, **130.228s**. |
| `http-controller-recovery.json` | RagRetrievalHttpRecoveryIntegrationTest — **2**, zero failures/errors/skips, exit 0, **141.355s**. Both actual controller prepare/reader crash cases pass. |
| `native-guards-architecture.json` | RagRetrievalEmbeddingNativeIntegrationTest,RagSourceReservationsIntegrationTest,RagSlice4LifecycleIntegrationTest,RagSlice4ReindexIntegrationTest,RagRuntimeRoleIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest — **74**, zero failures/errors/skips, exit 0, **187.765s**. |
| `boundary-probe.json` | Watchdog 180s → `java.exe @<root>/probe-classpath.args com.vhvkhangg.personalprivatevault.rag.ReviewAttemptTwoProbe` — exit 0, **23.571s**. Actual currentness race, original-UUID production reconciliation and not-ready HTTP cancellation observations. Harness success reports candidate defects. |
| `pre-emission-probe.json` | Watchdog 180s → same Java args, `com.vhvkhangg.personalprivatevault.rag.ReviewPreEmissionCancelProbe` — exit 0, **47.965s**. Confirms empty completed 409 with `emission_started=false`, then terminal cancellation. |

**Fresh independent total: 200 distinct Java cases.** All listed command captures/cleanup are complete. Both probe compilations pass on the first attempt; no failed harness run is omitted. Probes use disposable PostgreSQL 18.6/pgvector with the actual isolated runtime, production Spring owners, real authenticated HTTP and sealed synthetic fixtures. No operational data/database was used. Native/JDK prerequisites remain the existing Java 25.0.2 and Python 3.14.4 environment; no repair/install/activation occurred.

**Explicit reuse, not freshly rerun:** 14 Python tests and Ruff/format/Mypy from the previous independent review root `codex-independent-takeover-review-1793142642248/` (query receipt exit 0, 23.774s; static exits 0, 0.557/0.536/1.046s). Query Python/test hashes are identical, other Python/pipeline/config/lock sources are unchanged. Also reused: 11 native protocol cases from implementer attempt 1 `codex-takeover-ec4aeab6925548ef98e03b3cf963efe3/native-stop-race-4.json`/selected ProtocolNative XML, whose setup/limits were independently inspected in the prior review; query supervisor/native implementation/protocol sources are unchanged. Fresh native/sender/authority runs cover changed integration. The implementer's final 58-file manifest has zero drift; reuse does not turn old evidence into fresh reviewer tests.

Fresh real VI/EN/Han producer-to-HTTP native startup **2.841–2.852s**, native total **2.927–2.965s**, peak process **968,077,312–1,148,207,104 bytes**, below the 2 GiB Job cap. These are synthetic native request measurements, not production latency/recall or whole-servlet certification. No full application suite, IDE inspection or later-phase evaluation ran.

## Shared coverage and Docs Sync Check

The [shared rubric](../../../../.agents/skills/codex-review-coverage/SKILL.md) is applied proportionately, reusing unchanged architecture/source evidence from the prior review rather than creating additional gates.

| Dimensions | Result / scoped rationale |
| --- | --- |
| 1–2, 10, 13, 15–16 | FINDING F06 and F05 as mapped above; no demonstrated new retrieval text/credential leak or premature release. Exact incident scope and production recovery now PASS. |
| 3–4, 6–9 | PASS for actual module ownership, root ApiResponse placement, explicit DTO adapter/nonblocking mechanism, concrete reuse/SOLID/pattern fit. No new module/interface hierarchy or framework; F05's remaining wire **behavior** is separate. |
| 5 | FINDING LOW only, L02. Recursive actual RAG tree **141 files**, Python source **23 files**; inspected new owner, retrieval API/exception/command/view grouping, callback/controller, SQL/model/repository/tests against additive package inventory. No required package reshuffle. |
| 11–12 | FINDING F05; normal validation, envelopes and finite code-point/ingress/scope/excerpt/response bounds PASS. |
| 14, 17–20, 23, 25, 27 | PASS within current capability: offline/native/role boundaries, immutable proof/DBML/additive V24, finite reads/output/resources, exact provenance/ranking and safe logs. V1–V23, dependency/model pins, native implementation and module edges unchanged. Non-Markdown and wider performance guarantees unclaimed. |
| 21–22, 26 | PASS for executed meaningful focused/actual PostgreSQL/native/HTTP/crash evidence; independent boundary probes additionally expose the two residual defects. Existing guard/role 28, architecture 37 and OpenAPI 1 pass. Mockito self-attach/JDK agent/CDS and SpringDoc/migration notices are triaged, not blanket-suppressed. No warning-free claim. |
| 24 | N/A: later answer/judge/catalog evaluation is not this retrieval-only slice. |
| 28 | FINDING: notes' empty-error permission is not Owner-approved; final-guard currentness claim is incomplete. Current review/status/routing synchronized; historical records immutable. |
| 29 | PASS: review/status-only edits; unique ignored evidence/probes, empty index, unchanged protected source/history and no publishing. Final hash/link/whitespace checks retained. |

- **UPDATED:** this independent report, [ACTIVE](../../handoffs/ACTIVE.md), executable handoff current disposition/counter/next action only. In particular replace the stale ACTIVE bottom NOT_STARTED launcher with the actual review result. No production/test or approved requirement edits.
- **ALREADY_CONSISTENT:** current Owner scope, no F05 contract waiver, disabled defaults, actual HEAD, unchanged 58-file final source manifest, additive schema/model/package inventory and existing role/pin/frozen boundaries. Prior review/Owner-decision hashes remain unchanged; no test implies Owner publication.
- **BLOCKED_OWNER_DECISION:** further bounded correction/disposition for F06 final-guard predicate and F05 pre-emission cancellation response after attempt 2 is independently blocked. No automatic third attempt.
- **NOT_VERIFIED:** any future correction/publication; current candidate is not accepted despite passing named suites. Earlier mandatory F11 production recovery evidence is now verified, not left blanket NOT_VERIFIED.
- **DEFERRED_OUT_OF_SCOPE:** root/backend AGENTS and preparation/governance routing assigned to ChatGPT by the handoff; L02 and other LOW backlog at milestone/audit. No broad documentation cleanup or frozen-phase reopening.

## Owner next action

No commit message/readiness is supplied. The finite residual scope is **F06: check the exact inspection predicate inside the canonical final current-read guard and prove both orderings; F05: canonical pre-emission cancellation/error response with real HTTP assertions, preserving nonblocking/DTO/quiescence safety and correcting its notes**. F04/F11 and every previously closed fix must remain closed unless a demonstrated regression exists. Any correction requires another explicit bounded Owner decision and later independent review.

**Next step: Owner | explicitly decide/authorize the two remaining bounded corrections or another disposition | P19-S19-1 ACTIVE + this attempt-2 independent review | no further implementation/publication without that decision; independent review after correction | Antigravity 3/3 exhausted; Codex attempts 1 and 2 independently blocked; no automatic attempt 3.**
