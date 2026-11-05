# DTR26-S2B — Scheduled Refresh | Preparation

**Prepared:** 2026-11-05  
**Owner approval in this conversation:** Begin S2B **preparation/design and independent pre-handoff review only**. This is not implementation or authorization to execute unattended network traffic.  
**Verified source baseline:** [remote `main` `47d4d6d3db49c2fd37211147502e88453a536458`](https://github.com/vhvkhangg/personal-private-vault/commit/47d4d6d3db49c2fd37211147502e88453a536458), `docs(feed): close published DTR26-S2A RSS radar slice`.  
**Disposition:** `READY_FOR_HANDOFF` — [independent pre-handoff re-review 1, 2026-11-05](reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-re-review-1.md).  
**Execution:** [ACTIVE](../../handoffs/ACTIVE.md) = `NO_ACTIVE_HANDOFF`; **S2B implementation NOT_STARTED, 0/3, takeover NOT_AUTHORIZED**.

**Current preparation review:** `DTR26-S2B-PH-01` and `DTR26-S2B-PH-02` **CLOSED for preparation design** by independent re-review 1. Stable-ID selection traces and pinned Spring 7.0.9 timer API availability were checked; **all S2B-01–12 application/runtime cases remain NOT_RUN**. The [initial CHANGES_REQUESTED report](reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-review.md) remains immutable history. **Owner-confirmed topology A: exactly one active backend JVM; actual deployment conformance remains NOT_VERIFIED** and is required before unattended activation. No implementation or timer activation authority follows.

## Outcome to prepare — one small Feed-owned feature slice

Under the **Owner-confirmed A: one active backend JVM deployment constraint** (operational enforcement not yet verified), when the Owner has explicitly opted in both globally and per FeedSource, the backend checks for due HN/RSS sources and runs **the accepted existing provider-specific refresh flows**. It must not poll when globally off, dispatch unsupported providers, hold database transactions across HTTP, duplicate manual jobs, hammer a failing source, starve other due sources, or defeat source changes made while a refresh is running. Existing manual endpoints and both Radar reads remain unchanged.

S2A is already independently accepted and Owner-published: [S2A closeout](../2026-11-04-dtr26-s2-preparation/owner-publication-closeout-2026-11-05.md), implementation `17f8a359eb9ebe1a962066bc5b2a83513eb564cb`. Its review evidence includes **178 fresh RSS tests plus 361 source-applicable reused tests**, with S2A FR-01–06 CLOSED. Do **not** represent these as S2B tests. Historical S2A Antigravity **3/3 exhausted**, one Codex takeover **1/1 independently accepted**; its counter is not transferred to S2B.

The earlier [prospective S2B design](../2026-11-04-dtr26-s2-preparation/scheduled-refresh-design.md) is an input, **not an executable handoff**. This new directory is the canonical S2B preparation scope; do not rewrite the S2A review, handback, contract or published closeout.

## Read in order

1. [Owner scope and gates](owner-scope-and-decisions.md) — stated approval versus proposals and assumptions.
2. [Reuse-first scheduling and authority design](scheduled-refresh-technical-design.md) — corrected stable-ID fairness algorithm, programmatic Feed-only timer registration and unchanged S1/S2A authority.
3. [Operator controls and failure behavior](operations-and-security.md) — no-network default, resource/host limits, rollback/backoff.
4. [Acceptance plan and reviewer instructions](acceptance-and-review-plan.md) — prospective `S2B-01`–`S2B-12`, all `NOT_RUN`.
5. [Decision and traceability register](decision-and-risk-ledger.md) — true blockers for reviewer, KEEP/SIMPLIFY/DEFER/REJECT.

Ground truth for existing functions: [FeedSourceRepository](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/persistence/FeedSourceRepository.java), [FeedSourceService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedSourceService.java), [FeedItemService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java), [HN orchestrator](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshService.java), [HN finalizer](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshFinalizer.java), [RSS orchestrator](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/RssFeedRefreshService.java), [RSS finalizer](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/RssFeedRefreshFinalizer.java), [shared admission](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshAdmission.java), [published S2A API](../../../architecture/daily-trends-radar-rss-api-extension.md), [application configuration](../../../../backend/src/main/resources/application.yml).

## Non-goals

No new module, DB table, Flyway migration, durable job queue, cluster leader election, multi-JVM coordination, generic provider registry, dependency upgrade, new HTTP endpoint, feed content/enclosure scraping, RSS/Atom feature extension, external provider discovery, frontend, Drive, Ideaverse or RAG/S19-2 actions. Prior frozen source/schema/security contracts and existing provider safety limits are preserved.

## Delivery gate

**Reviewed publication package:** these six preparation Markdown files, the preserved initial review and the new independent re-review. Owner publishes this preparation package first; a separate **`$codex-create-handoff`** invocation may then create the S2B-only executable contract. Current review/status edits do not implement, alter `ACTIVE.md`, start a scheduler or authorize another feature. No unresolved material preparation choice remains; actual runtime/deployment proof belongs to the future gates.

Only after independent `READY_FOR_HANDOFF` and Owner commit/push of reviewed preparation may Owner separately invoke **`$codex-create-handoff`** for exactly S2B. Then Antigravity implementation/self-review, independent `$codex-final-review`, Owner-only publication and publication closeout. Implementation corrections start at their own `0/3`; S1 and S2A counters remain frozen. Never open the handoff from this draft directly.

**Docs Sync Check:** `UPDATED` independent re-review and current preparation-status/next-action annotations only; `IMMUTABLE` the initial review and S2A historical evidence; `ALREADY_CONSISTENT` current `ACTIVE=NO_ACTIVE_HANDOFF`, substantive corrected design and prospective acceptance negatives; `DEFERRED` unrelated historical/LOW backlog; `OWNER_DECISION_RECORDED` topology A; `NOT_VERIFIED` S2B application/PostgreSQL/Spring runtime, actual deployment conformance and future acceptance/publication.

**Suggested commit:** `docs(feed): prepare DTR26-S2B scheduled refresh`.

**Next step:** Owner | stage/commit/push reviewed preparation, then separately invoke Codex `$codex-create-handoff` | S2B six preparation files plus initial review and re-review 1 | READY_FOR_HANDOFF; published preparation required; ACTIVE stays NO_ACTIVE_HANDOFF | implementation NOT_STARTED, S2B 0/3; takeover NOT_AUTHORIZED.
