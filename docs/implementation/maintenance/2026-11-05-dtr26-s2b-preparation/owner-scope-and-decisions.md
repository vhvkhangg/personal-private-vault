# DTR26-S2B — Owner scope, approvals and bounded decisions

**Owner's explicit 2026-11-05 request:** Start **preparation** of `DTR26-S2B — Scheduled Refresh` after S2A publication closeout. This authorizes documentation and independent review, **not production edits, network requests, or an executable handoff**.

## User-visible purpose and exact scope

- Honor source scheduling fields already accepted in `feed_sources`: `enabled`, `scheduled_refresh_enabled`, `refresh_interval_minutes`, `last_fetched_at`, `next_fetch_at`.
- On a separate, explicit **application-global opt-in** (proposed `ppv.feed.scheduler.enabled=false`), inspect due sources and refresh only `HACKER_NEWS` and `RSS` through **their existing provider implementations**. Keep valid positive source intervals, including 1 minute, with best-effort timing rather than exact execution promises.
- Prefer no extra web API: the current source CRUD already provides the per-source scheduling switch. Existing HN/RSS manual POST routes and HN-only/combined Radar GET routes remain accepted, unchanged in contract; Save to Inbox remains manual and unchanged.
- **Owner-confirmed A**: S2B supports exactly one concurrently active backend JVM (2026-11-05); no persistent task queue or cross-process scheduling guarantee. Global scheduler OFF never starts automatic provider work.

## Owner authorization versus suggested defaults

| Matter | Recorded authority/disposition | Reviewer gate |
| --- | --- | --- |
| S2B preparation | **OWNER_APPROVED** by current request, docs/review only | `READY_FOR_HANDOFF` is required before implementation handoff. |
| Active JVM topology | **OWNER_CONFIRMED_A (2026-11-05)**: S2B supports **exactly one concurrently active backend JVM**. Multiple simultaneously active JVMs sharing the Feed database are excluded. | Verify the design and operator/deployment instructions enforce the one-active-JVM condition; actual runtime topology remains `NOT_VERIFIED`, not a pending Owner choice. |
| Global scheduler OFF by default | **RECOMMENDED**; required safety invariant for first implementation | Verify existing application config has no equivalent active scheduler; negative startup test. |
| Per-source flag | **REUSE** current `scheduled_refresh_enabled`; `enabled=true` also mandatory | Manual refresh must ignore scheduler opt-in and continue normally. |
| Trigger cadence / registration | **PH-02 preparation design independently reviewed; NOT implemented:** Feed-private single-thread `TaskScheduler` direct fixed-delay registration 60 seconds after app readiness; no `@EnableScheduling`/`@Scheduled` activation | Prove OFF has no Feed timer; ON adds Feed task without registering the existing RAG `@Scheduled` method or other unrelated scheduled tasks. |
| Due-source fairness / tick limits | **PH-01 preparation design independently reviewed; NOT implemented:** scheduler-only **stable source-ID cursor**, max 20 due candidates *examined* and max 2 provider-entry invocations per tick (busy counts), serial dispatch; stop cursor at last **examined** row | Prove PostgreSQL progress for >=30 eligible sources, both all-success and failure/busy cases; no mutable `(nextFetchAt,id)` keyset or public due-query change. |
| Failure cooldown | **PROPOSED** five-minute in-memory per-source cooldown, bounded 512 entries; manual calls never held behind cooldown | Prove bounded memory, restart reset and no false `lastFetchedAt/nextFetchAt` mutation on failure. |
| Disabling schedule mid-fetch | **DESIGN RULE from prospective S2B note:** fail closed before scheduled commit; never affect manual refresh | Must add narrow scheduled-invocation authority guard inside source-locked finalizer, preserving production transaction identity and lock order. |
| Global toggle while running | **PROPOSED** startup property (immutable until restart), not a live hot-reload switch | During graceful shutdown, no new admissions; bounded active jobs terminate/complete according to existing deadlines. |
| Provider allowlist | HN + RSS **only** | `GITHUB_TRENDING` / `REDDIT` / `WEBSITE` remain metadata-only and do not monopolize scanning. |
| Schema/REST/public API | **REUSE, NO MIGRATION/NEW ROUTES by default** | Any unavoidable breaking contract/schema change escalates to Owner before handoff. |

Review may simplify these **proposals** with evidence. Do not describe them as separately confirmed Owner decisions. If an irreversible choice materially changes what the Owner can expect, return a finite `OWNER_DECISION_REQUIRED` with alternatives; do not ask the Owner again for permission already granted to **prepare** S2B.

## Source-of-truth caveats

1. [Current `FeedSourceRepository`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/persistence/FeedSourceRepository.java) selects *all* enabled scheduled due types, including unsupported enum members. A scheduler-specific HN/RSS filter/query is required; **do not narrow the existing public `GET /api/v1/feed/sources/due` contract**.
2. [Current `FeedSourceService`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedSourceService.java) sets `nextFetchAt=null` for new scheduled sources without a successful fetch and recalculates it when schedule is changed. [Generic `ingestFetch`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java) advances fetch timestamps only in a successful transaction. A failed scheduled attempt must not fabricate success.
3. [Current HN finalizer](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshFinalizer.java) checks enabled/type; [current RSS finalizer](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/RssFeedRefreshFinalizer.java) checks enabled/type/exact fetched feedUrl. Neither currently checks the **scheduler-specific** source flag through commit. This is future code, not present functionality.
4. [Current HN/RSS refresh services](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshService.java) already acquire/release the **same shared admission** and prohibit ambient transactions during HTTP. Scheduler must not separately acquire it then invoke a refresh that acquires again.
5. The inspected bootstrap has no `@EnableScheduling`, but [RagRecoveryService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java) has an existing 30-second `@Scheduled` reconcile method. **PH-02:** do **not** enable application-wide annotation scheduling even inside a Feed-conditional configuration. Use a **privately owned, programmatically registered Feed `TaskScheduler`** and test RAG/non-Feed negative activation.

## Owner topology decision register — A explicitly confirmed 2026-11-05

**Owner decision (2026-11-05):** The Owner explicitly selected **A: DTR26-S2B supports only one active backend JVM**. This confirms the supported deployment topology, **not** that the deployed environment has already been inspected or validated.

- **A — OWNER_APPROVED / IN SCOPE:** one active process owns the Feed timer; existing per-process admission and cooldown are sufficient for the bounded best-effort contract **within that constraint**. No cross-process exactly-once claim. Document startup, shutdown, and rolling-restart overlap avoidance, and verify lifecycle/concurrency behavior.
- **B — OUT OF SCOPE / NOT OWNER-APPROVED:** two or more active JVMs require separate Owner approval and coordination/lease/idempotency design. Do not implement distributed scheduling under S2B; stop if deployment has concurrent active replicas.
- **Recorded Owner answer:** **A / CONFIRMED on 2026-11-05**, from the Owner’s direct statement: “Tôi chọn A: DTR26-S2B chỉ hỗ trợ một backend JVM hoạt động.” Preserve the historical reviewer’s topology question as a fact about the **earlier review**, not an unresolved Owner response. **Deployment runtime verification remains pending**; it belongs to the future implementation/operational gate.

## Historical boundaries and future order

P17 FROZEN, P18 CLOSED, S19-1 accepted/published; S19-2 SUSPENDED/NOT_ACCEPTED/ARCHIVAL_ONLY, indexing INACTIVE, live originals DISABLED/DEFERRED. S1 and S2A accepted history and all residual LOW/INFO remain unchanged. After S2B (if later accepted): two-account Google Drive → Dashboard/API readiness → final comprehensive non-RAG audit (adjudicate all actionable severities) → separate Phase 16 Frontend authorization. No RAG or new provider scope inferred.

**Next step:** Owner | publish reviewed preparation, then separately invoke Codex `$codex-create-handoff` | DTR26-S2B scope and [independent re-review 1](reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-re-review-1.md) | READY_FOR_HANDOFF; topology A recorded, runtime/deployment gates remain; ACTIVE stays NO_ACTIVE_HANDOFF | implementation NOT_STARTED, 0/3; takeover NOT_AUTHORIZED.
