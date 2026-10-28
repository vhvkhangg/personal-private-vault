# Independent slice review — P19-S19-1 Codex corrective attempt 4

- **Date / reviewer / mode:** 2026-10-28 / Codex, separate from the Codex implementation session / `SLICE`.
- **Disposition: READY_FOR_OWNER_COMMIT.** **F13 MEDIUM is independently CLOSED. F01–F13 and L01 are CLOSED** for their adjudicated scope. No new blocking finding or mandatory NOT_VERIFIED remains. **L02 LOW remains OPEN/nonblocking**, deferred to milestone/audit.
- **Authority:** [exact attempt-4 Owner decision](../owner-authorization-s19-1-codex-corrective-attempt-4-2026-10-28.md), [executable contract](../s19-1-executable-handoff-2026-10-27.md), [attempt-3 independent review](2026-10-28-s19-1-independent-codex-corrective-attempt-3-final-review.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md), [review skill](../../../../.agents/skills/codex-final-review/SKILL.md). Codex authored the candidate; its self-check is implementer evidence, not this independent acceptance.
- **Entry:** main/HEAD `ead5291bdffee09e53d0b41957ea751231015db2`; **80 unstaged/untracked candidate paths**, empty index. All **59** final runtime/test manifest hashes match. Independently comparing attempt-4 entry and prior final hashes confirms **six changed paths**: the sender, its existing integration test and four current implementation/status documents. **57/59** prior runtime/test hashes remain unchanged.
- **Counters:** Antigravity **3/3 EXHAUSTED**; Codex attempts **1–3 independently blocked**; separately authorized **attempt 4 independently accepted**. Prior decisions/reviews/failures/counters remain preserved; no automatic further implementation.
- **Boundary:** reviewed S19-1 delivery only, not Phase 19 final acceptance. P17 FROZEN; P18 CLOSED; S19-2+ unapproved; indexing INACTIVE; live originals DISABLED/DEFERRED. No source/test edits, staging, publication, activation or configuration/trust repair by this reviewer.

## User requirement and simplest-mechanism check

The existing API already requires a root JSON 503 error for a healthy deadline before emission. The correction distinguishes logical `onTimeout` from actual `onError`, retaining the same bounded error-only output owner, explicit DTO adapter and [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md) root `ApiResponse`/`ApiResponses` family.

The existing public Servlet WriteListener is initialized before computation, without writing headers or evidence. One bounded payload is selected later; success still follows C_E. Concurrent container/producer/timeout signals are coalesced into one cursor owner. Ready error output can finish during timeout handling; unready, failed or committed evidence output aborts without appending/replaying JSON. Readiness is checked before each bounded write/flush. No SQL lock or context monitor spans output I/O. Callback/container/native/transport quiescence and buffer disposal still precede guarded release.

No new type, package, framework, timer, queue, dependency, model, privilege, migration or contract amendment is introduced. The requested behavior is implemented proportionately within the existing sender.

## Finding register

Java paths in this table are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/`.

| ID | Severity / blocking / status | Independent evidence and disposition |
| --- | --- | --- |
| **F13** | MEDIUM / no / **CLOSED** | `internal/application/retrieval/RagRetrievalService.java:239` distinguishes logical deadline from transport failure; `finishDeadlineOutput:270`, `startOutput:398` and `initializeOutput:415` reuse the sole nonblocking cursor and root error. Fresh healthy pre-C_E HTTP test returns nonempty 503 `RAG_QUERY_UNAVAILABLE`, no-store and no evidence while retaining the paused reader. The original independent reproduction now returns **503 / 121 bytes / expected root code present**, with nonterminal/reserved reader until computation joins. |
| F05 | MEDIUM / no / CLOSED retained | Fresh `cancelBeforeEmissionReturnsRootErrorWhileComputationStillRetainsReader` preserves real authenticated retrieval/cancel HTTP: structured 409, no evidence, retention until join, then terminal CANCELLED. All 20 sender cases pass. |
| F01–F04/F06–F12/L01 | CLOSED retained | Exact authority/currentness/inspection/incident/role/provenance/input/native/recovery fixes retain applicable source hashes. Fresh request/native/incident/wire-loss gates cover the changed integration; prior independently passing source/guard/role/architecture/actual controller-crash and protocol proof is explicitly reused below. No demonstrated regression reopens a finding. |
| L02 | LOW / no / OPEN, deferred | Existing meaningful `internal/web` root lacks `package-info.java`. Recursive inventory confirms the same sole descriptor gap. Track at milestone/audit; no LOW-only correction is required for this slice. |

Fresh `deadlineAfterCommittedPrefixCannotAppendErrorOrReleaseActiveWriter` flushes a real first success byte, blocks the active writer, observes deadline stop and retained reservation, then proves terminal ABORTED after writer exit. Captured wire remains HTTP 200 with its existing prefix and **no appended unavailable/cancel JSON**. Existing not-ready, late-callback, disconnect, blocked-output/incident, failed native-stop and unknown-commit controls also pass. Timeout notification or `complete()` alone is never treated as release proof.

## Acceptance matrix

| Requirement | Result / evidence and limit |
| --- | --- |
| S19-1-01–10, 12, 14 | PASS, retained accepted scope. Fresh selection, successful retrieval, native producer/query/HTTP, exact incident denial and connection-loss cases supplement unchanged source/currentness/SQL/role proof. |
| S19-1-11, S19-1-13 | PASS. Fresh healthy pre-C_E deadline/root 503, canonical cancellation 409, committed-response abort, DTO/root ownership, ingress/error and positive quiescence tests. F13's original independent failing case now passes. |
| R06 | PASS. Actual HTTP timeout/cancel/disconnect/committed/not-ready/late-callback/active-writer controls and native-stop retention. Controlled vectors isolate lifecycle timing; actual native gates separately verify the pinned process. |
| R01–R05, R07–R10 | PASS through fresh applicable incident/admission/wire-loss/native gates and explicitly reused unchanged authority/SQL/physical currentness-order/actual controller-recovery proof. No fresh controller-crash or protocol execution is claimed. |
| Later generation/recommendation/live-original/other-format capabilities | N/A: unapproved for this retrieval-only slice. No future quality/activation gate is inferred. |

## Verification

Fresh evidence root: `logs/phase-19/s19-1/codex-independent-attempt-4-review-1793168874689/`. Source manifests, exact delta, recursive inventory, run-scoped selected XML, independent probe source/compile/receipts, reuse applicability and final boundary checks are retained there. All launched reviewer build/probe commands exited **0**, capture/cleanup complete, cleanup not_needed.

The fresh Maven command was:

```powershell
& 'rag-engine/.venv-d17/Scripts/python.exe' scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt 'logs/phase-19/s19-1/codex-independent-attempt-4-review-1793168874689/retrieval-sender-native.json' -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest,RagRetrievalTakeoverIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagRetrievalEmbeddingNativeIntegrationTest' test
```

| Fresh receipt | Actual independent result |
| --- | --- |
| `retrieval-sender-native.json` | **84 distinct Java cases**, zero failures/errors/skips, **154.223s**: Selection 13, Integration 4, Sender 20, Takeover 10, Incident 17, ConnectionLoss 12, EmbeddingNative 8. Exact XML copied to a new run-scoped directory. |
| `healthy-deadline-compile.json` | Watchdog 30s → `javac.exe @<root>/probe-compile.args`; PASS, **2.528s**. |
| `healthy-deadline-probe.json` | Watchdog 120s → `java.exe @<root>/probe-classpath.args com.vhvkhangg.personalprivatevault.rag.ReviewAttemptFourHealthyDeadlineProbe`; PASS, **24.584s**. Same prior healthy control, strengthened with exact JSON code/no-store and retained-reader assertions. Test-only shorter timeout, `notReady=false`, open write barrier, client connection held open, computation paused before C_E. After join, real production finish reaches TERMINAL. |
| `graph-navigation.json` | Baseline navigation only; graph predates untracked S19-1. Canonical files/source hashes control the review. |

Observed independent control: `pre_C_E_deadline emission_started=false retrieval_http=503 body_bytes=121 expected_root_error_present=true finish=DEADLINE`.

A native hook rejected inline helper setup before launch for ambiguous quoting. The helper was written as a plain review file under the unique evidence root and invoked with an explicit path; no hook/configuration override or repair. No launched reviewer test failure or timeout is omitted.

**Explicit reuse: 134 Java cases**, independently checked against original receipts/XML and unchanged owner/test hashes:

- **121** from prior independent attempt 3 `codex-independent-attempt-3-review-1793163920909/`: SourceAuthority 13 + SnapshotCorrective 42 (`core-authority.json`), existing reservations/lifecycle/reindex/roles 28 + architecture 37 + OpenAPI 1 (`native-guards-architecture.json`). No module edge, public type, route, privilege, schema or source authority changed in attempt 4.
- **2** actual HTTP-controller crash/recovery cases from independent attempt 2 `codex-independent-attempt-2-review-1793160328113/http-controller-recovery.json`, plus **11** native protocol cases from previously independently inspected `codex-takeover-ec4aeab6925548ef98e03b3cf963efe3/native-stop-race-4.json` and its selected ProtocolNative XML. Recovery/inspection/SQL/process/protocol owners are unchanged. Fresh actual HTTP/native/incident cases cover their changed sender integration.

Thus **218 applicable Java cases = 84 fresh + 134 reused**, not 218 fresh or a full application suite. **14 Python tests plus Ruff/format/Mypy** are explicitly reused from independent `codex-independent-takeover-review-1793142642248/python-query.json`, `python-lint.json`, `python-format.json`, `python-types.json`: exits 0, source/test/pipeline/lock/pins unchanged. Existing Java 25.0.2, native Python 3.14.4, disposable PostgreSQL/pgvector, installed pinned artifacts and sealed synthetic Markdown only. No operational DB, Owner corpus, download or install.

Fresh VI/EN/Han actual producer → publication → CPU query → PostgreSQL → HTTP startup **2.844–2.921s**, native total **2.925–3.058s**, peak process **967,663,616–1,146,298,368 bytes**, within the existing 2 GiB query Job cap. The separate 8,500ns idempotent stop measurement occurs after compute positively stopped the child/transport; it is not cancellation latency. No production throughput/recall/whole-request percentile claim.

Implementer's two failed progress runs remain failed/immutable; its later final passes are not substituted for independent verification. Expected connection-loss and unproven-stop warnings correspond to passing negative controls. Existing JDK/Mockito/CDS/SpringDoc diagnostics remain visible; no blanket suppression or IDE/warning-free claim.

## Shared coverage and scope-bounded Docs Sync Check

| Dimensions | Result / rationale |
| --- | --- |
| 1–2, 10–12, 16, 21–22 | PASS: approved deadline/root contract, finite acceptance/race mapping, real HTTP positive/negative controls and retained quiescence. |
| 3–4, 6–9 | PASS: existing RAG ownership/public boundary, root factories and controller DTO adapter; one cursor rather than another sender framework, no speculative abstraction or dependency change. |
| 5 | FINDING LOW L02 only. Actual recursive **141 RAG Java files / 23 Python source files** inventoried with packages/types/descriptor gaps. Same capability grouping, exceptions/interfaces/enums/commands/views and package-tree inventory; no new type/package or stale tracked generated copy. |
| 13–15, 17–20, 23, 25–27 | PASS for scoped privacy/authority/transaction/SQL/recovery/provenance/bounds/offline/resource/dependency/logging/tooling risks, using fresh changed integration plus hash-applicable independent evidence. Wider OCR/GPU/live-file and production benchmark guarantees are N/A/unclaimed. |
| 24 | N/A: answer/judge/catalog evaluation is not approved retrieval scope. |
| 28 | PASS within current review/status boundaries; implementation notes describe actual timeout/transport distinction. Submission-time NOT_ACCEPTED statements remain historical implementer provenance, superseded by this review and current status. |
| 29 | PASS: unique ignored evidence, source/test/history preservation, empty index and no publishing. Final boundary validation checks permitted edits, links/fences/whitespace and immutable decisions/reviews. |

- **UPDATED:** this independent report, [ACTIVE](../../handoffs/ACTIVE.md) and executable handoff current review/status/next action. F13 independently CLOSED; S19-1 READY_FOR_OWNER_COMMIT. Approved acceptance and historical evidence unchanged.
- **ALREADY_CONSISTENT:** exact six-path corrective delta, 59 final runtime/test hashes, disabled defaults, unchanged schema/ADR/module/package/API inventory and protected prior closures. Handback/notes retain the implementer's dated candidate provenance. Owner authorization is not publication.
- **DEFERRED_OUT_OF_SCOPE:** ChatGPT-owned root/backend AGENTS, preparation/governance navigation and post-publication brief closeout; existing pre-issuance notices do not override ACTIVE. L02 and B1/B2/MA/DOCB LOW backlog retain milestone/audit ownership. No repository-wide cleanup.
- **BLOCKED_OWNER_DECISION:** none for accepting/publishing this bounded S19-1 delivery. S19-2+ and activation still require their own future authorization.
- **NOT_VERIFIED:** Owner staging/commit/push and resulting implementation commit SHA; no publication is claimed. No mandatory S19-1 verification remains missing.

## Owner next action

Commit/push the reviewed **complete S19-1 candidate**, including this review/status result, using normal Owner staging. No separate staging-review session. ChatGPT then makes a brief S19-1 closeout grounded in the actual published commit; Phase 19 remains open for separately approved remaining slices.

Suggested commit: `feat(rag): add authorized Markdown evidence retrieval`.

**Next step: Owner | stage, commit and push the accepted S19-1 delivery, then ChatGPT records its brief commit-grounded closeout | P19-S19-1 ACTIVE + this independent attempt-4 review | READY_FOR_OWNER_COMMIT; publication pending, S19-2+/activation unapproved | Antigravity 3/3 exhausted; Codex attempts 1–3 independently blocked, attempt 4 independently accepted.**

