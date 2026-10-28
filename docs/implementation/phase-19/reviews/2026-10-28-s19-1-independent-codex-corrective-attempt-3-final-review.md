# Independent slice review — P19-S19-1 Codex corrective attempt 3

- **Date / reviewer / mode:** 2026-10-28 / Codex, separate from the implementing Codex session / `SLICE`.
- **Disposition: CHANGES_REQUESTED / STOPPED / OWNER_DECISION_REQUIRED.** **F06 HIGH and F05 MEDIUM are now CLOSED.** One newly reproduced **F13 MEDIUM / OPEN / blocking** remains: a healthy pre-emission deadline completes an empty HTTP 503 instead of the approved root error envelope. No regression of the corrected currentness or cancellation behavior is claimed.
- **Authority:** [attempt-3 Owner decision](../owner-authorization-s19-1-codex-corrective-attempt-3-2026-10-28.md), [executable contract](../s19-1-executable-handoff-2026-10-27.md), [attempt-2 independent review](2026-10-28-s19-1-independent-codex-corrective-attempt-2-final-review.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md), [review skill](../../../../.agents/skills/codex-final-review/SKILL.md).
- **Entry:** main/HEAD `ead5291bdffee09e53d0b41957ea751231015db2`; **78 unstaged candidate paths**, empty index. All **59** final runtime/test manifest hashes match. Only two production owners and two existing test classes changed since the preceding candidate; other differences are authorized current docs/new Owner decision and the previous review report.
- **Counters:** Antigravity **3/3 EXHAUSTED**; Codex attempts 1–2 independently blocked; **separately authorized attempt 3 implemented and independently blocked on F13**. No automatic attempt 4, Antigravity round 4 or authorization to implement the newly reported correction. All historical counters/evidence remain intact.
- **Protected state:** F01–F12 and L01 are now CLOSED for their adjudicated scope; **L02 LOW remains OPEN/nonblocking/deferred**. P17 FROZEN; P18 CLOSED; S19-2+ unapproved; indexing INACTIVE; live originals DISABLED/DEFERRED. No publication, staging, production/test edits or activation by this reviewer.

## User requirement and simplest-mechanism check

The two requested fixes are implemented within the existing source guard and nonblocking sender. Currentness now checks committed inspections while the final source guards are held. Healthy pre-C_E cancellation uses an error-only output cursor and the shared root envelope while retaining exclusion until actual computation/sender quiescence. No schema, privilege, model, framework, scheduler or contract change is introduced.

The newly confirmed deadline defect is an existing approved API requirement, not a new feature or pattern request. The executable contract's **Before body commitment** table already requires deadline failure to return 503 `RAG_QUERY_UNAVAILABLE` through the shared response family. The cancellation fix does not cover the separate timeout-listener branch. This review credits the completed authorized fixes and records that distinct remaining defect under a new ID rather than reopening them.

## Finding register

Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/`.

| ID | Severity / blocking / status | Independent evidence / disposition |
| --- | --- | --- |
| **F06** | HIGH / no / **CLOSED** | `RagSourceAuthority.java:357–363` checks the exact inspection predicate inside `guardPrepared` after graph/external/source guards are held, covering every external member of the closure. Fresh three-parameter inspection-first tests prove affected R → UNAVAILABLE, obsolete R/sibling → CURRENT; fresh guard-first test proves CURRENT first, inspection waits on its actual owner, then UNAVAILABLE. The earlier independent reproduction now returns UNAVAILABLE with the original inspection still UNRESOLVED; production reconciliation subsequently classifies that same UUID CLEAN. |
| **F05** | MEDIUM / no / **CLOSED** | Cancellation intent is published before stop; `RagRetrievalService.java:195–212` and its error-only output path retain the approved WriteListener/DTO/root factories. Fresh real authenticated retrieval **and cancellation HTTP** test returns nonempty 409 `RAG_RETRIEVAL_CANCELLED`, no-store/no evidence, while computation still retains the reader. After join, cancellation returns 200 and the reader is TERMINAL/CANCELLED with `emission_started=false`. Independent reproduction also returns the expected 142-byte root error. No early release, callback replay or native-stop regression is reproduced by the 18 sender cases. |
| **F13** | **MEDIUM / yes / OPEN — new** | `RagRetrievalService.java:235–249` sends both timeout and transport-error notifications through `stop`, which unconditionally marks `transportAborted=true`, closes output and completes it. `completeContext:178–185` resets an uncommitted response to 503 with Content-Length 0. A healthy, writable pre-C_E request therefore receives a normally completed empty 503 with no `RAG_QUERY_UNAVAILABLE` envelope. Exact reproduction/minimal correction below. |
| F01/F02/F03/F04/F07/F08/F09/F10/F11/F12/L01 | **CLOSED retained** | Earlier exact-identity/full-authority/lock/incident-scope/role/locator/validation/native/redaction and production-recovery fixes are preserved and covered by the fresh applicable gates or explicitly unchanged independent evidence. No demonstrated regression reopens them. |

**L02 LOW / OPEN / nonblocking retained:** meaningful `internal/web` root containing the ingress filter lacks `package-info.java`. Deferred to milestone/audit. No LOW-only work or attempt extension is authorized; other existing LOW backlog is unchanged.

### F13 — timeout notification is treated as a broken transport

**Contract and location:** [executable API contract](../s19-1-executable-handoff-2026-10-27.md), **Before body commitment**, deadline-failure row; [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md) root wire ownership. The production listener's `onTimeout` calls the same `stop` method as `onError`. That method sets `ctx.transportAborted=true` without an I/O failure or positive transport-abort observation, bypassing the new error-only response path and completing the empty status.

**Independent actual HTTP reproduction (`healthy-deadline-probe.json`):**

1. Use the real authenticated endpoint, a valid selected sealed synthetic snapshot/generation and actual PostgreSQL reader/stop/finish owners. A query-vector override provides a controlled computation pause; it is not a native-quality substitute.
2. Pause before C_E and confirm `emission_started=false`. Use the existing test-only shorter AsyncContext timeout to exercise the unchanged production timeout listener without waiting the public 120-second budget.
3. Keep the client connection open and the output path writable: the existing test filter has **`notReady=false` and its write barrier already released** (`CountDownLatch(0)`). There is no disconnect or imposed backpressure. An initial normal HTTP request warms/verifies the same server path.
4. The real request deadline fires. HttpClient receives a normal completed **503 with zero body bytes**, with no root `error.code=RAG_QUERY_UNAVAILABLE`. Release/join computation; production reaches terminal cleanup.

Observed output: `pre_C_E_deadline emission_started=false retrieval_http=503 body_bytes=0 expected_root_error_present=false finish=DEADLINE`.

The initial broader boundary probe also included a deliberately not-ready deadline case. **That subcase alone is not used as proof of a healthy-transport defect.** The separate control above removes both not-ready and blocked-write conditions. Its source, compile receipt, actual logs and successful cleanup are preserved. No model, authority, Servlet callback or HTTP status result is mocked; only request work timing and the test timeout are controlled.

**Impact/severity:** a client cannot parse the required structured failure/code after an otherwise normal pre-body HTTP deadline. This is a confirmed incorrect approved wire behavior and therefore MEDIUM under Owner policy. No evidence disclosure, premature exclusion release or new HIGH privacy defect is asserted.

**Smallest correction/proof:** distinguish logical deadline expiry on a healthy uncommitted response from an actual failed/unwritable transport. Reuse the existing bounded error-only output owner/root `RAG_QUERY_UNAVAILABLE` envelope, while preserving stop intent and callback/container/native quiescence. Genuine disconnect, committed-body or positively aborted/unwritable paths must still avoid appending/replaying JSON. Extend an existing sender deadline test with the **pre-C_E, output-ready, nonempty root 503** assertion and retain its broken/blocked transport controls. Update only the directly affected notes if needed. No blocking writer, new timer framework, larger timeout, dependency, schema or architecture is requested. This report authorizes no correction automatically.

## Original acceptance and race applicability

| Scope | Result / limit |
| --- | --- |
| S19-1-01–10, 12, 14 | PASS within the bounded reviewed retrieval contract and applicable evidence: exact source/currentness/incident ordering, generic/explicit/consent/provenance/native/offline/role cases. F06's two commit orders are now independently verified. |
| S19-1-11 and public error contract under S19-1-13 | **FAIL for F13's healthy pre-emission deadline response.** Cancellation's previously failed response is now PASS; ordinary ingress/validation/busy/error-envelope and lifecycle safety checks pass. |
| R01–R05, R07–R10 | Earlier accepted/unchanged proof plus fresh source/sender/incident/SQL gates remain applicable. Fresh actual graph/lease lock observations strengthen currentness ordering; actual controller-crash recovery remains explicitly reused, not newly rerun. |
| R06 | Positive stop/retention/nonblocking/late-callback/cancel/unknown-stop portions PASS; deadline's normal structured-error result FAIL under F13. |

No new answer-generation, recommendation, OCR/other-format/GPU/live-source, large benchmark or whole-backend gate is imposed. Those capabilities remain N/A/unapproved for this slice.

## Verification and applicability

Fresh evidence root: `logs/phase-19/s19-1/codex-independent-attempt-3-review-1793163920909/`. Entry/final source hashes, changed paths, selected run-scoped XML, recursive inventory, probe source/compile/command receipts, applicability and final boundary checks are retained there. All listed commands have actual exit 0 and capture/cleanup complete; no historical log is overwritten.

Every fresh Maven command used native `.venv-d17/Scripts/python.exe scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <unique.json> -- mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<exact classes below> test`.

| Fresh receipt | Exact selection / actual result |
| --- | --- |
| `core-authority.json` | RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalTakeoverIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagSourceAuthorityIntegrationTest,RagSnapshotCorrectiveIntegrationTest — **129**, zero failures/errors/skips, **180.666s**. Includes final 13 authority and 18 sender cases; no rerun or relaxed fixture needed by the reviewer. |
| `native-guards-architecture.json` | RagRetrievalEmbeddingNativeIntegrationTest,RagSourceReservationsIntegrationTest,RagSlice4LifecycleIntegrationTest,RagSlice4ReindexIntegrationTest,RagRuntimeRoleIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest — **74**, zero failures/errors/skips, **212.880s**. |
| `boundary-probe.json` | Watchdog 180s → `java.exe @<root>/probe-classpath.args com.vhvkhangg.personalprivatevault.rag.ReviewAttemptThreeProbe` — **33.675s**. Independently confirms both original fixes; includes the separately labeled not-ready deadline observation. |
| `healthy-deadline-probe.json` | Same Java wrapper/args with `ReviewAttemptThreeHealthyDeadlineProbe` — **34.435s**. Actual healthy output-ready pre-C_E deadline F13 reproduction. Its successful harness result reports a candidate failure, not acceptance. |

**Fresh independent total: 203 distinct Java cases.** Both probe compilations pass; no failed/timeout reviewer harness is omitted. Actual JDK 25.0.2, existing native Python 3.14.4, disposable PostgreSQL 18.6/pgvector, sealed synthetic Markdown and pinned local artifacts only. No operational migration/database, Owner corpus, install, startup/config/trust repair or indexing activation.

**Explicit reuse:** two independently passing real HTTP-controller crash/recovery cases from prior independent attempt 2 `codex-independent-attempt-2-review-1793160328113/http-controller-recovery.json` (exit 0, 141.355s and preserved XML); 11 native protocol cases from `codex-takeover-ec4aeab6925548ef98e03b3cf963efe3/native-stop-race-4.json`/selected XML, inspected in earlier independent reviews. Recovery/inspection/SQL/supervisor/protocol/test sources are unchanged by hash. Fresh stronger guard/sender/native integration tests cover their changed entry paths. **216 applicable Java cases** is the fresh-plus-reused total, not a fresh execution count.

Fourteen Python tests and Ruff/format/Mypy are reused from the prior independent root `codex-independent-takeover-review-1793142642248/`: exits 0; 23.774s query receipt, 0.557/0.536/1.046s static receipts. Python/query test/pipeline/config/lock sources remain unchanged. Nine directly relevant reused source/test hashes match; all 59 implementer final runtime/test hashes match. No full application suite or IDE inspection ran.

Fresh actual VI/EN/Han producer → publication → pinned CPU query → PostgreSQL → real HTTP startup **2.939–5.028s**, native total **3.101–5.184s**, peak process **967,139,328–1,321,082,880 bytes**, within the 2 GiB Job cap. These are synthetic request measurements, not production recall/latency certification. Mockito/JDK agent/CDS, SpringDoc and migration diagnostics are visible and triaged; no warning-free claim.

## Shared coverage and scope-bounded Docs Sync Check

Use the [shared coverage rubric](../../../../.agents/skills/codex-review-coverage/SKILL.md) proportionately, retaining unchanged independently inspected architecture/source evidence.

| Dimensions | Result / rationale |
| --- | --- |
| 1–2, 10–12 | FINDING F13 against existing deadline/root HTTP requirement. F05/F06 and other finite business/validation/traceability cases now PASS. No new product rule or pattern. |
| 3–4, 6–9 | PASS within changed scope: canonical source owner/current-read guard, concrete callback/error cursor ownership, root factories and explicit controller DTO adapter; existing named module interface/dependency direction unchanged. No speculative abstraction. |
| 5 | FINDING LOW L02 only. Actual recursive inventory: **141 RAG files, 23 Python source files**; changed package/type grouping and existing additive package-tree inventory checked. No new package/type or `.gitkeep` cleanup required. |
| 13–20, 23, 25–27 | PASS for scoped authority/role/persistence/recovery/source/privacy/offline/provenance/bounds/resource/dependency/logging/tooling risks. New final-guard predicate is checked after source locks; no SQL/monitor spans added output I/O. Frozen migrations/native/model/dependency pins unchanged. Broader guarantees are unclaimed. |
| 21–22 | FINDING F13's missing healthy deadline wire case; actual independent HTTP control exposes it despite green suites. New original-finding tests are physically ordered, meaningful and passing. |
| 24 | N/A: later answer/judge/catalog evaluation is not approved retrieval-only scope. |
| 28 | FINDING F13 vs the unchanged before-body deadline table/general healthy-error notes; current verdict/routing updated. Historical status/evidence not rewritten. |
| 29 | PASS: review/status-only edits, unique ignored evidence, empty index, source/history/Owner decisions preserved and no publishing. |

- **UPDATED:** this new independent report, [ACTIVE](../../handoffs/ACTIVE.md) and executable handoff current result/counter/next action only. F05/F06 are independently CLOSED; F13 is newly OPEN, with finite evidence/correction. No change to approved acceptance or implementer-owned notes/source/tests.
- **ALREADY_CONSISTENT:** actual HEAD/index, 59-file manifest, original bounded Owner scope, unchanged schema/ADR/module/package inventory and disabled defaults. Previous reviews/Owner decisions remain immutable. No test result implies Owner publication.
- **BLOCKED_OWNER_DECISION:** further correction/disposition for the newly confirmed deadline branch after attempt 3. The attempt-3 approval grants no automatic fourth attempt or unrelated implementation.
- **NOT_VERIFIED:** any future F13 correction or Owner publication; no blanket missing proof remains on F05/F06. The current candidate remains unaccepted because of the concrete MEDIUM defect.
- **DEFERRED_OUT_OF_SCOPE:** ChatGPT-owned root/backend AGENTS/preparation/governance routing, L02 and other LOW milestone/audit backlog. No repository-wide cleanup or P17/P18 reopening.

## Owner next action

No commit message/readiness is supplied. The remaining scope is **only F13: healthy pre-emission deadline must return the existing bounded root 503 error, with one focused real HTTP deadline regression and directly affected notes**. Preserve all closed findings, nonblocking/DTO contracts, positive stop/finish proof, caps and historical evidence. Another explicit Owner decision is required before additional implementation; a separate independent review follows any authorized correction.

**Next step: Owner | explicitly authorize bounded F13 correction or choose another disposition | P19-S19-1 ACTIVE + this attempt-3 independent review | no further implementation/publication without that decision; independent review after correction | Antigravity 3/3 exhausted; Codex attempts 1–3 independently blocked; no automatic attempt 4.**
