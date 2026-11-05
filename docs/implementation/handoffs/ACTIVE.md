# Active Implementation Handoff

- **Handoff ID / created:** `DTR26-S2B` / Codex, 2026-11-05.
- **Status:** `READY_FOR_OWNER_COMMIT`.
- **Executable contract:** [DTR26-S2B opt-in scheduled HN/RSS refresh](../maintenance/2026-11-05-dtr26-s2b-preparation/executable-handoff-2026-11-05.md). Read the whole contract for targets, exact behavior, limits, tests and exclusions.
- **Authority:** current Owner `$codex-create-handoff` invocation after [reviewed preparation](../maintenance/2026-11-05-dtr26-s2b-preparation/README.md) publication at `e296d55db227988b55e86cfb81e83f45e507703a`; [Owner scope/topology A](../maintenance/2026-11-05-dtr26-s2b-preparation/owner-scope-and-decisions.md), [independent READY_FOR_HANDOFF re-review 1](../maintenance/2026-11-05-dtr26-s2b-preparation/reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-re-review-1.md).
- **Entry baseline:** local HEAD and remote main verified equal to e296d55; clean entry tree; eight preparation/review Markdown changes only from source baseline `47d4d6d3db49c2fd37211147502e88453a536458`. This handoff is a subsequent docs-only change.
- **Preparation findings:** PH-01/PH-02 remain CLOSED; actual fairness/lifecycle/transaction observations and recovered history are independently accepted for bounded S2B implementation.
- **Implementer:** Antigravity, existing backend-implementer, `/antigravity-implement-handoff`, then mandatory `/antigravity-self-review`.
- **Final reviewer:** separate independent Codex `$codex-final-review`.
- **Implementation / acceptance:** `READY_FOR_OWNER_COMMIT / ACCEPTED` after [independent re-review 3](../maintenance/2026-11-05-dtr26-s2b-preparation/reviews/2026-11-05-dtr26-s2b-independent-re-review-3.md). FR-01–04 independently CLOSED. Corrected late-staging case observes SOURCE_CHANGED_DURING_REFRESH, both timestamp/item rollbacks and rejection of the independent guard-bypass control. Reviewer **14 fresh PostgreSQL tests** pass plus **236** source-applicable reused tests; separate implementer all-six receipts total **250** passing tests. Accepted delivery remains uncommitted; actual deployment conformance remains NOT_VERIFIED before unattended activation.
- **Counter:** S2B remediation `3/3 consumed; independently ACCEPTED / STOPPED`; no further correction round, takeover NOT_REQUIRED / NOT_AUTHORIZED. Initial review consumed 0/3; rounds 1/2/3 consumed 1/3, 2/3, 3/3 respectively. S1/S2A exhausted 3/3 and accepted takeovers remain historical, not transferable.
- **Operational gate:** Owner already confirmed exactly one active backend JVM. Actual deployment process/rollout conformance NOT_VERIFIED before unattended activation. Leave default OFF; no production polling/deployment/enable action authorized here.

## Goal and required invariants

Implement one conditional private Feed timer after readiness, default `ppv.feed.scheduler.enabled=false`; 60s initial/fixed delay. Scheduler-only supported/due query orders by stable source ID, one page of at most 20; cursor last **examined** row; at most two serial provider-entry invocations including busy/no-op, wrap on next tick after empty page. Five-minute local cooldown, maximum 512 entries, no false DB success timestamps.

Reuse existing HN/RSS refresh paths and shared same-source/global max-two admission, acquired once inside providers. Scheduled eligibility is reread after acquisition before I/O. Scheduled finalizers add scheduling-enabled authority to existing enabled/type/RSS exact-URL checks in the same physical transaction/retained source lock as ingestion and timestamps. Interval-only edit uses the fresh interval; no post-ingest due check. Manual source/refresh/Radar/Inbox contracts and exact 235-operation API inventory remain unchanged.

One private ThreadPoolTaskScheduler/future; no general timer bean, EnableScheduling/Scheduled/global annotation registrar or provider worker queue. Feed ON must not activate the existing RAG periodic method. Stop/cancel/dispose owned work with bounded termination; no later dispatch. Actual fairness, source-race/physical-transaction and Spring OFF/ON/isolation/cleanup proof is mandatory.

## Targets, tests and documentation

The executable contract permits narrowly scoped Feed tick/lifecycle/internal scheduled methods/finalizers, two repository query branches, default-OFF YAML, relevant tests and implementation-adjacent docs. Its six-group focused gate uses scripts/run-bounded-command.py, normal **300s** per command with unique ignored receipts. No full suite/extended profile, live provider smoke, downloads, RAG QA/model/worker or native trust/hook bypass.

**Docs Sync Check — independent re-review 3:** UPDATED — additive acceptance review, this register/executable disposition/counter and operator-guide status only. ALREADY_CONSISTENT — default OFF in the uncommitted working tree, accepted FR-01–04 behavior/proof, cleanup guide, package inventory/API/frozen ownership and byte-verified handback history. OWNER_DECISION_RECORDED — topology A; no repeat question. NOT_VERIFIED — actual deployment conformance/unattended activation and future Owner publication. DEFERRED_OUT_OF_SCOPE — older notices/backlog and baseline RAG audit; no unrelated closure.

Antigravity may write the new operator guide, one root README navigation link and additive actual Feed package inventory exactly as authorized; it records a dated handback/self-review/activation ledger and status. ChatGPT owns separately requested routine governance/publication closeout; Owner alone publishes. Preparation and prior reviews/handbacks/contracts/closeouts stay dated and immutable.

## Preserved historical states and limits

- S2A accepted/Owner-published at [`17f8a359eb9ebe1a962066bc5b2a83513eb564cb`](https://github.com/vhvkhangg/personal-private-vault/commit/17f8a359eb9ebe1a962066bc5b2a83513eb564cb); [closeout](../maintenance/2026-11-04-dtr26-s2-preparation/owner-publication-closeout-2026-11-05.md) published at `47d4d6d`. FR-01 HIGH/FR-02–06 MEDIUM CLOSED. [Independent takeover review 1](../maintenance/2026-11-04-dtr26-s2-preparation/reviews/2026-11-05-dtr26-s2a-independent-takeover-review-1.md): 178 fresh RSS tests plus 361 source-applicable reused; separate implementer 254. Not fresh S2B tests. S2A Antigravity 3/3 exhausted; one Owner-authorized Codex takeover 1/1 independently accepted/STOPPED.
- Accepted S2A manual RSS2/Atom1/combined Radar, exact article-link identity, RSS IPv6 IANA 2003::/18 destination boundary and source-bounded fake-connector/native-DNS limits stay preserved. No live provider/full-backend/deployment certification inferred.
- [S1 closeout](../maintenance/2026-11-02-daily-trends-radar-preparation/owner-publication-closeout-2026-11-04.md): published `7438e7f19e4b2ace0c3c0583bd9cd7f988e5b5ca`, FR-01–07 CLOSED, DTR26-FR-L01 LOW/OPEN; 3/3 and one accepted bounded Codex takeover historical.
- [Universal Inbox](../maintenance/2026-11-01-universal-inbox-preparation/owner-publication-closeout-2026-11-02.md) published `50aaf7d5`, UI26-FR-L02 LOW/OPEN. [CSA26](../maintenance/2026-11-01-current-system-readonly-audit/owner-publication-closeout-2026-11-01.md) published `4cf6204`, CSA26-FR-L01 LOW/OPEN. [Spring-only Archify](../maintenance/2026-11-01-spring-only-archify/owner-publication-closeout-2026-11-01.md) published `84f1185`, SAD-FR-L01 LOW/OPEN. DTR26-S2-PH-L01, AUTH-CLOCK/other LOW/INFO remain in their registers; future comprehensive audit must adjudicate all actionable severities.
- Root personal-private-vault.zip remains outside accepted scope; do not open/modify/remove/certify it. No unrelated cleanup/new provider/schema/POM/API/framework/Drive/frontend deployment scope follows.
- P17 FROZEN, P18 CLOSED, S19-1 ACCEPTED/OWNER_PUBLISHED. S19-2 SUSPENDED_BY_OWNER / NOT_ACCEPTED / ARCHIVAL_ONLY under [suspension](../phase-19/owner-decision-s19-2-suspension-2026-10-30.md) and [archival authority](../phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md). C1 REJECTED, C2 NOT_QUALIFIED, M10 FAIL/cause NOT_VERIFIED; F01–F05/F08/F09 OPEN. External indexing INACTIVE, live originals DISABLED/DEFERRED; no RAG/QA/inference/download authority. Historical counters/receipts immutable.
- Only Owner stages/commits/pushes/tags/releases/merges PRs. No agent publication or supported native-hook/root/trust/client-limit bypass.

## Implementation result

Remediation 3 repaired the late-authority concurrency test false positive:
- Hooked `findByIdForUpdate` inside `FeedItemService.ingestFetch` via `ProxyFactory(originalIngestRepository)` and called `itemRepo.flush()`, flushing SQL `UPDATE` (item 991) and `INSERT` (item 992) to PostgreSQL before the `feed_sources` row lock is acquired.
- Concurrently executed `UPDATE feed_sources SET scheduled_refresh_enabled = false WHERE id = ?` in < 5ms without blocking.
- Resumed provider transaction to acquire the source lock, triggering `FeedRefreshException(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH)` in `HackerNewsRefreshFinalizer.finalizeRefresh`.
- Asserted `outcome == ScheduledRefreshOutcome.FAILED`, `finalizerException` is `SOURCE_CHANGED_DURING_REFRESH`, existing item title remains `"Original Item Title"`, new item 2 is not inserted (1 item in DB), and both `lastFetchedAt` and `nextFetchAt` remain null.
- Verified all six test groups across 250 tests with 0 failures, 0 errors, 0 skips in `logs/daily-trends-radar/dtr26-s2b-rem3-final/`.
- Default property `ppv.feed.scheduler.enabled: false` accurately described as present in uncommitted working tree `backend/src/main/resources/application.yml` (default OFF).
- Preserved original handback, round-1 handback, and round-2 handback with matching SHA-256 hashes.

## Final review

**READY FOR OWNER COMMIT / ACCEPTED** — [independent re-review 3](../maintenance/2026-11-05-dtr26-s2b-preparation/reviews/2026-11-05-dtr26-s2b-independent-re-review-3.md), 2026-11-05. FR-01–04 CLOSED. Fresh normal test and independent mutation control verify actual rejection versus guard-bypassed REFRESHED; prior evidence remains source-applicable. Remediation STOPPED at 3/3, no takeover. Actual deployment conformance/unattended activation remains separately held; Owner publication and subsequent closeout are not yet performed.

**Next step:** Owner | commit/push accepted delivery, then ChatGPT records verified publication closeout | DTR26-S2B, re-review 3 and executable contract | READY_FOR_OWNER_COMMIT; retain default OFF until actual deployment conformance | 3/3 consumed; ACCEPTED / STOPPED; no takeover.
