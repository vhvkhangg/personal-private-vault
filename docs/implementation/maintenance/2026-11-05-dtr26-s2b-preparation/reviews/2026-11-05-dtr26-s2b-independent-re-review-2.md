# Independent re-review 2 — DTR26-S2B

- **Date / reviewer / mode:** 2026-11-05 / Codex / independent implementation re-review after Antigravity remediation 2. Review-only; no submitted production or repository test changes authored here.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered IMPLEMENTED_AWAITING_CODEX_REVIEW; [executable contract](../executable-handoff-2026-11-05.md), [re-review 1](2026-11-05-dtr26-s2b-independent-re-review-1.md), [round-2 handback](../implementation-handback-remediation-2-2026-11-05.md). HEAD remains `e296d55db227988b55e86cfb81e83f45e507703a`.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. FR-01–03 remain CLOSED. FR-04 MEDIUM remains OPEN for one late-authority test false positive and its current evidence claims. The lifecycle, privacy and history corrections are independently credited below.
- **Counter:** S2B **2/3 correction rounds consumed; next correction/re-review 3/3**. Not exhausted; takeover NOT_AUTHORIZED. No historical S1/S2A allowance transfers.
- **Activation:** default OFF. Owner-confirmed topology A remains recorded; actual one-JVM/no-overlap deployment conformance is NOT_VERIFIED before unattended activation. That separate operational gate does not itself block code acceptance.

## FR-04 — late-authority test passes without the scheduling guard

**MEDIUM OPEN**, continuing S2B-07 and the exact late-staging remainder from re-review 1.

[FeedScheduledRefreshConcurrencyIntegrationTest](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedScheduledRefreshConcurrencyIntegrationTest.java), **lines 429–486**, places its barrier **after** inv.callRealMethod() returns from ingestFetch at line 456. [FeedItemService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java) already obtains the source FOR UPDATE lock at **line 145**, updates/flushes source timestamps and flushes items at **lines 158–159** before returning. The independent schedule-OFF UPDATE therefore waits on that retained source lock. The barrier's five-second await at test line 459 ignores its boolean result and coincides with the finalizer's five-second transaction timeout.

The case asserts only categorical FAILED plus rollback. A transaction timeout produces those observations even when scheduling authority is never checked.

An independent diagnostic executes the **existing submitted test**, unchanged on disk, using its real Spring/PostgreSQL context and fake provider. It records the actual finalizer exception and independent UPDATE duration, then repeats with scheduledMode forwarded as false only at the finalizer boundary, bypassing the scheduling predicate while preserving the transaction and other checks:

| Diagnostic execution | Independent schedule-OFF UPDATE | Actual finalizer exception | Submitted case |
| --- | --- | --- | --- |
| Original scheduling mode | **5,012ms** | TransactionTimedOutException | **passes** |
| Scheduling guard bypassed | **5,002ms** | TransactionTimedOutException | **still passes** |

The regular fresh Surefire case also takes **5.145s**. The diagnostic confirms causality rather than inferring it from duration: the new test accepts the same timeout when the scheduling guard is bypassed. It does **not** prove rejection with SOURCE_CHANGED_DURING_REFRESH after a committed late edit. The production guard remains source-credible; this mandatory late-authority observation is **NOT_VERIFIED**.

**Smallest correction, test/evidence scope:**

1. Place the controlled barrier after real item UPDATE/INSERT work has been staged/flushed **and before the first source FOR UPDATE acquisition inside ingestion**. Retain the existing item-then-source lock order, physical transaction and five-second finalizer timeout.
2. Require that the independent scheduling edit actually commits while the provider transaction is paused; release through a checked latch and finally cleanup. A timed-out await must fail the fixture. Keep all waits short enough to exercise authority rather than transaction expiry.
3. Observe the actual SOURCE_CHANGED_DURING_REFRESH rejection, not only aggregate FAILED, and verify the original item title, absence of the new item, and unchanged **both lastFetchedAt and nextFetchAt**. A guard-bypassed execution must no longer satisfy the case. Reuse the existing observing/proxy seams and finite fixture; no new framework or production redesign is required.
4. Record the corrected observation in a **new dated handback**. Preserve the submitted round-2 claim as history. Current S2B-07/all-cases-VERIFIED claims remain unaccepted until this proof is repaired. Also describe the local default property as present in the uncommitted working tree: round-2 activation ledger's wording “committed in application.yml” is inaccurate while HEAD is the preparation commit.

No additional FR-01–03 correction, RAG change, new feature, schema/dependency change, full suite or new Owner topology decision is requested. If the next independent correction round remains blocked, the existing three-round policy then requires an Owner disposition; this review does not authorize takeover.

## Credited corrections and preserved acceptance

| Area | Independent disposition / evidence |
| --- | --- |
| FR-01 / FR-02 / FR-03 | **CLOSED, unchanged.** HN scheduled30/manual50/shared provider flow, completion-based cooldown, readiness initialization and single bounded termination wait/result retain re-review-1 evidence. Tick/providers/finalizers/config and unit selectors match those reviewed inputs. |
| FR-04 lifecycle | **CLOSED for the prior remainder.** The new controlled ON-context exercise runs the registered owned real-timer callback through real Spring provider/admission/repository services with safe fake transport and due/excluded rows. It observes active admission, interrupts an actually blocked provider, terminates its owned pool, verifies admission count returns to zero and only one dispatch occurred, then recreates fresh service/lifecycle state and stops the new delayed registration. Existing exact 60s registration/unit controls remain applicable. This is controlled lifecycle recreation with Spring dependencies; no production restart is certified. |
| FR-04 privacy | **CLOSED.** Synthetic URL/XML/title/credential markers now enter a failing query collaborator; a real WARN event is required. Captured new scheduler message/arguments/throwable observations and constant no-cause logging establish the scoped safe logging path. It is no longer an empty-event-loop assertion. |
| FR-04 history / cleanup guide | **CLOSED for recovery/preservation.** Original handback SHA-256 exactly matches `6aa20ce6e50b754f8c9f25e3c74d9cabf807bba0d9ea4ea7d8b49a43cffbaf0c`; preserved [round-1 handback](../implementation-handback-remediation-1-2026-11-05.md) exactly matches `014e9e4f37b08801bf3f991a3a5be574f96462a886080c22e96761fa12d1b39a`. The round-2 handback is separate; prior reviews are unchanged. Guide §3.2 now distinguishes clean versus timed-out cleanup. No history exception is needed for the recovered original. |
| Previously credited FR-04 evidence | **CLOSED portions retained.** Actual PostgreSQL fairness/cooldown traces, joined PID/XID positives and rejecting REQUIRES_NEW controls, HN/RSS flag edits during fetch, shared admission, fresh interval, retained lock and source-qualified manual timeout regressions remain credited. Only the late-authority case above remains open. |

The changed production file is the internal lifecycle class, whose explicit constructor permits the controlled test seam; initialization/registration/stop behavior remains as reviewed. No global TaskScheduler bean or annotation scheduler is added. Current ON-context registry still contains the existing Moments/RagRecoveryService baseline; the guide's zero-Feed-delta explanation is retained. Absence of a global timer bean is not a blanket inactivity claim, and no RAG method was directly invoked for evidence.

## Independent verification and limits

Evidence directory: `logs/daily-trends-radar/codex-s2b-rereview2-20261105-5ce91de5556a4d7ea95de686987bf4ba/`.

| Fresh independent focused group | Tests | Exit | Seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- |
| Scheduler unit | 25 | 0 | 8.547 | 0 / 0 / 0 |
| Scheduler PostgreSQL | 14 | 0 | 40.078 | 0 / 0 / 0 |
| Scheduler Spring lifecycle | 5 | 0 | 39.641 | 0 / 0 / 0 |
| **Fresh total** | **44** | **all 0** | **88.266** | **0 / 0 / 0** |

All three exact selectors use scripts/run-bounded-command.py, normal 300s, offline mvn.cmd, native-access flag and the previously installed image `sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c`. Copied Surefire XML and final stdout totals agree. Green counts include the false-positive case and are not acceptance of its scenario.

**Reused independent evidence, 206 tests:** re-review 1's manual/shared refresh **49** and HTTP/architecture **79**, plus initial review's Feed/Inbox/read **78**. The former receipts reside in `logs/daily-trends-radar/codex-s2b-rereview1-20261105-c7b98ba3fbd34829a5c72d23c3c94666/`; the latter in `logs/daily-trends-radar/codex-s2b-final-review1-20261105-266b33280225412da654e75770c9d025/`. Provider/finalizer/transport/parser/CRUD/ingest/read/API/module-dependency inputs are unchanged; the lifecycle visibility edit is separately exercised above. tree-and-reuse-inspection.json records input hashes and applicability. These tests are **not fresh**.

**Separate implementer evidence:** all six `dtr26-s2b-rem2-final/` receipts reconcile to **250 tests / 263.641s**, zero failures/errors/skips, all exit 0. They are not additional reviewer executions.

S2bLateAuthorityProbe.java, arguments, receipt and parsed observations are additive ignored diagnostics. Under a normal **120s** cap it completes in **34.109s**, executing the existing case twice through TestContextManager with runtime observers and the explicit guard-bypass control; no repository test/source file is changed. Receipt exit 0 means the diagnostic completed, **not acceptance PASS**. The context and owned PostgreSQL container are explicitly closed. A read/summary command was rejected by the native hook for ambiguous quoting; the safely simplified read succeeded, with no hook/trust change or approval bypass.

## Scope and shared review coverage

Graphify navigation was queried first for relationships; its older graph does not establish current scheduler behavior. Exact handoff/source inspection is authoritative. Recursive Feed inventory remains **118 production Java files / 27 packages / 29 test Java files**, all production descriptors present, no .gitkeep and one root ApiResponse. Root envelope/module-local adapters still follow ADR-0016. No POM, migration/schema, NamedInterface/module edge, root HTTP/security or RAG source change. Existing command/view/exception/outcome placement remains cohesive; no generic provider abstraction was added.

| Shared dimensions | Scoped disposition / rationale |
| --- | --- |
| 1 intent; 2 traceability; 22 test quality | FINDING FR-04 — late-authority case accepts timeout and guard bypass; current full-verification claim is unsupported. |
| 3 architecture; 4 ownership; 5 tree; 6 shared contracts; 11 API | PASS scoped — recursive inventory, ADR-0016/root envelope and unchanged public/module/API contracts; applicable prior 235-operation/architecture checks retained. |
| 7 SOLID; 8 principles; 9 patterns | PASS — existing cohesive private timer/tick and shared owner-local provider flow; bounded internal test seams, no speculative framework. |
| 10 domain; 12 validation; 17 persistence; 18 resources | PASS demonstrated/reused — HN30/manual50, completion cooldown, stable-ID bounds/progress/admission and accepted provider validation. No schema change. |
| 13 privacy; 14 leakage/egress; 21 security tests; 27 logging | PASS scoped — non-vacuous marker observation plus unchanged auth/secure clients and applicable adversarial evidence. No live request or containment certification. |
| 15 transactions/concurrency | PASS existing positives/negative controls and retained-lock/manual timeout reuse; NOT_VERIFIED late scheduling-authority observation, FR-04. |
| 16 lifecycle/recovery | PASS scoped — fresh real owned active-provider cancellation/admission cleanup, no next dispatch and controlled recreation; existing single-wait timeout controls retained. Actual deployment N/V remains an operational gate. |
| 19 retrieval/ML; 20 document/OCR; 23 benchmarks; 24 evaluation science | N/A — no changed retrieval/document/ML/throughput capability. Unchanged RSS/XML safety reused under validation. |
| 25 dependencies; 26 tooling/static diagnostics | PASS scoped — existing dependencies and bounded/offline gates; no compiler [WARNING] lines. JVM/Mockito notices retained; IDE/static inspection NOT_VERIFIED, no warning-free assertion. |
| 28 docs/governance | PASS recovered immutable handbacks/previous reports and corrected cleanup guide; FINDING FR-04 current late-authority/VERIFIED/publication wording. |
| 29 hygiene/delivery | PASS — permitted diff, descriptor/index/source/history boundaries; no staging/publishing or archive access. |

## Docs Sync Check and next gate

- **UPDATED:** this additive review, ACTIVE/executable disposition/remediation counter and operator-guide status header only.
- **ALREADY_CONSISTENT:** default OFF, topology choice, FR-01–03, credited lifecycle/privacy/history/fairness/transaction observations, actual package inventory and accepted S1/S2A/frozen ownership.
- **FINDING / Antigravity:** only the late-authority test and associated accurate dated evidence/current claims above. Preserve the three credited areas and earlier reports/handbacks/receipts.
- **NOT_VERIFIED:** late source-authority rejection after a committed staged edit; actual deployment conformance and future publication. Deployment remains separate from code acceptance.
- **DEFERRED_OUT_OF_SCOPE:** older dated routing notices, existing LOW/INFO/AUTH-CLOCK backlog, baseline RAG audit and root ZIP. No unrelated cleanup or closure.

Entry hashes match before reviewer writes. Final boundary checks constrain writes to this review and three current status documents, preserve source/tests/all handbacks/prior reviews, and verify unchanged HEAD/index, UTF-8, links and whitespace. No application tests are rerun for status-only edits.

No production/test implementation, full suite, actual provider/private-endpoint request, dependency/runtime/image download, direct RAG/QA/model invocation, worker activation, native-hook/trust change, ZIP access, staging or publishing occurred. Controlled test fixtures use their isolated PostgreSQL database and fake clients; future activation is not authorized here.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review` and separate Codex `$codex-final-review` | DTR26-S2B FR-04 late-authority proof in this review and [executable contract](../executable-handoff-2026-11-05.md) | repair the finite test/evidence false positive; retain credited fixes/history and default OFF | 2/3 consumed; next correction/re-review 3/3; takeover NOT_AUTHORIZED.
