# Independent re-review 1 — DTR26-S2B

- **Date / reviewer / mode:** 2026-11-05 / Codex / independent implementation re-review after Antigravity remediation 1. No submitted production or repository test changes authored by this reviewer.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered IMPLEMENTED_AWAITING_CODEX_REVIEW; [executable contract](../executable-handoff-2026-11-05.md), [initial review](2026-11-05-dtr26-s2b-independent-final-review.md), [current implementer handback](../implementation-handback-2026-11-05.md). HEAD remains published preparation `e296d55db227988b55e86cfb81e83f45e507703a`.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. FR-01–03 independently CLOSED. FR-04 MEDIUM remains OPEN for the reduced evidence/history docket below; this is not a finding that the credited production paths fail.
- **Counter:** S2B **1/3 correction rounds consumed; next correction/re-review 2/3**. No reset, exhausted allowance or takeover authorization. S1/S2A historical counters remain separate.
- **Activation:** default OFF. Owner already confirmed topology A; actual one-JVM/no-overlap deployment conformance remains NOT_VERIFIED before unattended activation. That operational gate alone does not block code acceptance.

## Finding disposition and credited corrections

| Stable ID | Independent disposition / evidence |
| --- | --- |
| DTR26-S2B-FR-01 | **MEDIUM CLOSED.** Scheduled HN uses 30; manual max=50 remains. Both providers now share a narrow private execution flow within their existing orchestrator. Fresh 60-story PostgreSQL fixture and an independent current-class fake-client probe observe exactly 30 scheduled selections/inputs; manual regression observes 50. No provider framework added. |
| DTR26-S2B-FR-02 | **MEDIUM CLOSED.** Due-query cutoff stays fixed; FAILED/BUSY cooldown uses each outcome's current clock. Both advancing-clock unit cases pass. The independent 70-second logical failure probe now observes a full **300 seconds** after completion, retry at 12:06:10Z. Existing inclusive expiry, eviction/cap and no-op rules remain. |
| DTR26-S2B-FR-03 | **MEDIUM CLOSED.** Default timer remains uninitialized until matching readiness; synchronized start/stop serializes ownership. Stop calls destroy once and returns the actual termination result. Real cooperative/nonterminating executor tests pass. Independent controlled active-callback probe measures **1,002ms** for one 1s budget, returns clean=false with worker still active, then confirms termination after release. This replaces the initial doubled-wait defect. Whole-provider/Spring acceptance observations remain under FR-04. |
| DTR26-S2B-FR-04 | **MEDIUM OPEN, narrowed.** Fairness, physical-transaction negative controls, several contention/rollback observations, counts and the guide's RAG baseline statement are corrected. Mandatory Spring/privacy/late-authority observations and immutable-handback requirements remain incomplete. |

The two actual PostgreSQL traces now advance the clock and record real spied provider-entry calls. Trace 1 covers all 30 supported IDs by tick 15 despite success-driven due updates; empty/reset and next-cycle calls are asserted. Trace 2 observes the exact supported order, five FAILED, two BUSY induced by actual shared admission, 23 successes, zero unsupported dispatches, cooldown presence and expiry, and persisted failure/success times. The 20-cooling-row cursor case remains green. These fairness portions of FR-04 are **CLOSED**.

Both provider controls now run the **same physical PID/XID assertion** against REQUIRES_NEW controls and assert rejection. Positive ingest/source/beforeCommit identity evidence remains credited. New two-way manual/scheduled same-source contention passes, as do the existing two-slot/third-BUSY case, fresh-not-due entrypoint state fixture, HN/RSS schedule-OFF during fetch, fresh-interval success, and retained-source-lock NOWAIT observation.

Fresh manual HN/RSS PostgreSQL lock-timeout regressions are applicable to scheduled finalization: the shared execution helper now owns identical exception translation and calls the same REQUIRED timeout=5 finalizer overload; scheduledMode adds only the scheduling-authority predicate. Existing enabled/type/exact RSS URL checks, secure clients/parsers and generic ingestion remain source-applicable. No redundant copy of every accepted manual test is required.

## FR-04 — remaining bounded corrections

All items continue the initial finding and executable S2B-01/02/07/09/10/12 obligations. No new feature, topology choice, production refactor, test-count target or RAG work is requested.

| Remaining area | Concrete observation / smallest correction |
| --- | --- |
| Real Spring callback / restart / active-provider stop | [FeedSchedulerLifecycleIntegrationTest](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedSchedulerLifecycleIntegrationTest.java), **lines 96–103**, checks a future and directly calls refreshService.executeTick(). It captures no registered callback and records no fake provider invocation. Its stop test, **lines 132–139**, stops an idle registration. The new unit cooperative test executes a mocked executeTick; it proves real timer cancellation, not actual provider admission cleanup or suppression of a second candidate. The resetMethods unit case is not destroy/recreate evidence. Finish the already-required controlled Spring OFF/ON recording-timer/callback proof with safe fake providers and due/excluded rows; stop during a blocked actual provider, release it in finally, assert no next dispatch, admission release and owned-thread termination; recreate and assert fresh cursor/cooldown/one delayed registration. Reuse the now-good short executor controls rather than duplicate them. |
| Marker-bearing privacy observation | The same integration test, **lines 106–129**, executes a normal/empty tick and loops over appender.list. It injects neither marker-bearing failure nor marker-bearing source data, and never requires any event. The loop can pass with **zero log events**. Trigger an existing new scheduler logging path using a synthetic failure containing URL/XML/title/credential markers, require the expected safe event to exist, and inspect message/arguments/throwable output for those markers. No live call or new logger is required. |
| Late scheduling authority after staging | [FeedScheduledRefreshConcurrencyIntegrationTest](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedScheduledRefreshConcurrencyIntegrationTest.java), **lines 427–479**, seeds an existing item and asserts update/insert rollback, which is credited. Its barrier is inside fetchTopStories, however; scheduling is disabled at **line 461 before fetch resumes**, not after item staging as its name/handback claim. Add the finite barrier at the real ingestion stage, after item UPDATE/INSERT work and before source authority is locked/refreshed, commit schedule-OFF independently, then assert both item changes and both fetch timestamps roll back. Preserve item-then-source lock order and the same physical transaction. Existing HN/RSS flag-during-fetch and transaction controls remain credited; explain shared-path applicability rather than multiplying equivalent tests. |
| Evidence history and accurate current claims | The initial handback was rewritten rather than preserved with a separate dated correction. Initial captured SHA-256 is `6aa20ce6e50b754f8c9f25e3c74d9cabf807bba0d9ea4ea7d8b49a43cffbaf0c`; current is `014e9e4f37b08801bf3f991a3a5be574f96462a886080c22e96761fa12d1b39a`. No prefix matches even allowing LF/CRLF normalization. The current handback calls all cases PASS and claims captured-callback, marker-bearing logging and interruption-flag-preservation tests that the submitted selectors do not demonstrate. Restore the original from retained history/copy and put round-1 correction in a separate dated artifact, with demonstrated versus NOT_VERIFIED cases mapped honestly. If unavailable, explicitly record NOT_RECOVERED and seek the Owner's narrow history disposition; do not manufacture an original or claim preservation. Preserve the submitted rewritten version as a dated round-1 artifact before restoring the original path; keep both review reports and all receipts intact. Correct current guide §3.2's unconditional clean-termination claim to distinguish timeout/failed cleanup. |

The guide now correctly describes **zero Feed-induced scheduling delta**, rather than claiming RAG reconciliation is never scheduled. Preserve that correction. Spring test display names/comments and handback case rows still claim no global/RAG scheduling, although the test explicitly asserts the existing processor and registry containing Moments.everyMidnight, Moments.everyHour and RagRecoveryService.reconcile. The absence of a global TaskScheduler bean does not establish inactivity. Align those current claims with the observed baseline; do not invoke, disable or modify RAG. The source diff adds no global scheduling configuration and this review asserts no new RAG activation or QA/inference.

## Independent verification and reuse

Fresh ignored receipts, copied XML, entry hashes, summaries and probe sources are in `logs/daily-trends-radar/codex-s2b-rereview1-20261105-c7b98ba3fbd34829a5c72d23c3c94666/`.

| Fresh independent group | Tests | Exit | Seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- |
| Scheduler unit | 25 | 0 | 8.531 | 0 / 0 / 0 |
| Scheduler PostgreSQL | 14 | 0 | 33.094 | 0 / 0 / 0 |
| Scheduler Spring lifecycle | 5 | 0 | 31.687 | 0 / 0 / 0 |
| Manual/shared refresh | 49 | 0 | 44.609 | 0 / 0 / 0 |
| HTTP/architecture | 79 | 0 | 68.719 | 0 / 0 / 0 |
| **Fresh total** | **172** | **all 0** | **186.640** | **0 / 0 / 0** |

Each uses the exact handoff selection through scripts/run-bounded-command.py, normal 300s, offline mvn.cmd, native-access flag and the previously installed PostgreSQL image `sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c`. Surefire XML and final stdout totals agree. A first launch used mvn rather than Windows mvn.cmd and failed before any test started (LAUNCH_ERROR/127); that receipt remains, with the successful unit command in a new v2 receipt.

**Reused independent evidence:** the initial review's **78** Feed/Inbox/read tests, receipt in `logs/daily-trends-radar/codex-s2b-final-review1-20261105-266b33280225412da654e75770c9d025/feed-inbox-read-compatibility.json`. All six selectors match HEAD, call no changed provider refresh entrypoint, and their CRUD/generic-ingest/Radar/Inbox owners remain unchanged. The repository query/finalizer/config inputs also match initial-review hashes. Current provider helper compatibility was separately rerun above. The reuse-78-attestation.json records hashes/applicability; these 78 are **not fresh**.

**Separate implementer evidence:** all six round-1 groups reconcile to **250 tests / 262.891s**, zero failures/errors/skips, all exit 0 in `logs/daily-trends-radar/dtr26-s2b-rem1-final/`. These are not additional reviewer tests. Green counts do not establish the missing observations above.

The additive S2bReviewProbeV3 uses current compiled classes, fake clients/repository/clock/finalizer and a controlled real owned timer, under a separate 60s receipt. It reproduces the three corrected measurements above and explicitly releases/terminates its worker. It is a diagnostic, not Spring/provider end-to-end acceptance evidence. No application rerun follows review/status-only writes.

## Scope and shared review coverage

Round-1 production changes are limited to the two existing provider orchestrators and new tick/lifecycle classes; test changes stay in the authorized Feed selectors. The actual recursive tree remains **118 production Java files / 27 packages / 29 test Java files**; all production packages have descriptors, no .gitkeep, and exactly one root ApiResponse owner consistent with ADR-0016. No POM/schema/migration/module graph/root HTTP/security/RAG source changes. Existing domain/public command/view/exception placement and source/item lock ownership remain intact. Initial-review architecture evidence is source-applicable; fresh architecture and exact 235-operation manifest regressions pass.

| Shared dimensions | Scoped disposition / evidence |
| --- | --- |
| 1 intent; 2 traceability | FINDING FR-04 — finite mandatory observations/current PASS claims still diverge. |
| 3 architecture; 4 ownership; 5 tree | PASS — inspected recursive inventory, descriptors, permitted diff and unchanged frozen ownership. |
| 6 shared contracts; 11 API | PASS — ADR-0016 root envelope, module-local adapters, unchanged 235-operation manifest and fresh HTTP tests. |
| 7 SOLID; 8 principles; 9 patterns | PASS — provider duplication removed within owners; cohesive bounded tick/private timer, no speculative framework. |
| 10 domain; 12 validation; 17 database; 18 resources | PASS demonstrated — 30/manual50, completion cooldown, actual PG fairness, caps/query filters/admission and unchanged accepted provider validation. |
| 13 privacy; 14 leakage/egress; 21 security tests | PASS unchanged auth/secure-client boundaries and reused adversarial fixtures; NOT_VERIFIED new marker-observation subset, FR-04. No live request. |
| 15 atomicity/concurrency | PASS positive joined transactions, rejecting controls, contention/retained lock/timeout reuse; NOT_VERIFIED late post-staging scheduling mutation, FR-04. |
| 16 lifecycle/recovery | PASS corrected initialization/single bounded wait/actual termination result; NOT_VERIFIED whole-provider Spring stop/restart subset, FR-04. |
| 19 retrieval/ML quality; 20 document/OCR fidelity; 23 benchmarks; 24 evaluation science | N/A — no retrieval, document ingestion, ML or throughput project. RSS format/security behavior is unchanged and covered under validation/reuse. |
| 22 test quality; 27 observability; 28 docs/governance | FINDING FR-04 — vacuous/misnamed acceptance evidence and rewritten historical handback. |
| 25 dependencies; 26 tooling/static diagnostics | PASS scoped — no dependency changes, fresh bounded/offline groups; no compiler [WARNING] lines. Existing JVM/Mockito notices remain. IDE/static inspection NOT_VERIFIED, no warning-free claim. |
| 29 hygiene/delivery | PASS source/package/index boundary and no publishing; FINDING FR-04 historical-handback preservation. |

## Docs Sync Check and next gate

- **UPDATED:** this additive review, ACTIVE/executable disposition/counter and operator-guide status header only.
- **ALREADY_CONSISTENT:** published preparation, default OFF/topology choice, fixed HN/cooldown/lifecycle behavior, package inventory/API/frozen ownership and accepted S1/S2A evidence.
- **FINDING / Antigravity:** only FR-04's remaining observations/current claims/history repair above; mandatory self-review and separate re-review. No further FR-01–03 correction requested.
- **NOT_VERIFIED:** listed implementation observations; original-handback recovery; actual deployment conformance and future publication. Deployment remains a separate activation gate.
- **DEFERRED_OUT_OF_SCOPE:** dated older notices, prior LOW/INFO/AUTH-CLOCK backlog, any baseline RAG audit and root ZIP. No unrelated cleanup or closure.

Submitted files match entry hashes before reviewer status writes. Final boundary checks preserve current source/tests/handback/initial review, index and HEAD; only this review and three current status documents may change. Relative file links, UTF-8, final newlines and whitespace are checked. Original initial handback preservation failed in the submitted remediation, not during this review; no recovery is inferred.

No production/test implementation, full suite, actual provider/private-endpoint request, dependency/runtime/image download, direct RAG/QA/model invocation, worker activation, native-hook/trust change, ZIP access, staging or Git publishing occurred. Existing Spring contexts were exercised as declared; no blanket runtime/deployment certification follows.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review` and separate Codex `$codex-final-review` | DTR26-S2B FR-04 in this review and [executable contract](../executable-handoff-2026-11-05.md) | complete the bounded evidence/history corrections; default OFF, preserve credited fixes and reports | 1/3 consumed; next correction/re-review 2/3; takeover NOT_AUTHORIZED.
