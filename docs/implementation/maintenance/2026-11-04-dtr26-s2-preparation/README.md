# DTR26-S2 — RSS/Atom and opt-in Scheduled Refresh preparation

**Recorded:** 2026-11-04  
**Owner authorization:** Start **preparation/design and independent pre-handoff review** of Daily Trends Radar S2 (RSS/Atom + bounded scheduling). This is **NOT** an implementation handoff or approval to run third-party network calls.  
**Source baseline:** remote [`main` at `8502a30b38c48fb812397b3a5de216be804fd958`](https://github.com/vhvkhangg/personal-private-vault/commit/8502a30b38c48fb812397b3a5de216be804fd958), `docs(feed): close published DTR26-S1 radar slice`.  
**Disposition:** **`READY_FOR_HANDOFF — DTR26-S2A ONLY`** after the [independent preparation review](reviews/2026-11-04-dtr26-s2a-independent-pre-handoff-review.md). Its explicit resolutions govern the package's marked proposals. S2B remains prospective; runtime evidence is NOT_RUN and implementation is not authorized. No tests or application changes performed in preparation.  
**Execution register:** [`ACTIVE.md`](../../handoffs/ACTIVE.md) = `NO_ACTIVE_HANDOFF` at this baseline. Neither S2 implementation slice has started; each would start at 0/3 only after its own reviewed handoff.

## What already works (source-verified on baseline)

- DTR26-S1 was independently accepted and Owner-published at [`7438e7f19e4b`](https://github.com/vhvkhangg/personal-private-vault/commit/7438e7f19e4b2ace0c3c0583bd9cd7f988e5b5ca). Its separate [publication closeout](../2026-11-02-daily-trends-radar-preparation/owner-publication-closeout-2026-11-04.md) is included in current `main`.
- `feed` owns `feed_sources`, `feed_items`, `saved_resources` and conversions, with accepted public module dependencies only on `vault` and `knowledge` ([Feed rules](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md), [dependency matrix](../../../architecture/module-dependency-matrix.md)). `FeedSourceType.RSS` **exists**, but it does not implement an RSS/Atom adapter.
- Current HN-only `POST /api/v1/feed/sources/{sourceId}/refresh` uses `HackerNewsRefreshService` with HTTP outside the transaction, existing in-memory admission, and authoritative finalization. `GET /api/v1/feed/radar` reads **HN-only** cached rows and must retain its accepted behavior. `FeedItemOperations.ingestFetch` provides the generic transactional normalized upsert; successful ingest updates `lastFetchedAt` and `nextFetchAt`.
- `feed_sources.enabled`, `scheduled_refresh_enabled`, `refresh_interval_minutes`, `last_fetched_at`, `next_fetch_at` and `GET /api/v1/feed/sources/due` already exist. **No scheduler actually runs** yet. A configured source/schedule is metadata, not evidence of active polling.
- The accepted `UI26` Save-to-Inbox path works for FeedItems; no new saved-resource or bookmark API is necessary.

## Owner-intended S2 deliverable, split for bounded implementation

| Slice | Goal | Readiness gate | Avoided coupling |
| --- | --- | --- | --- |
| **DTR26-S2A** | RSS 2.0 / Atom 1.0 **manual** refresh for a configured RSS source, normalized article URLs, cached RSS content visible in an additive combined Radar preview; reuse generic `ingestFetch` and Save to Inbox | First independent `$codex-pre-handoff-review` may return `READY_FOR_HANDOFF` for S2A **only**; Owner then publishes preparation and requests an S2A handoff | No scheduler; keep HN refresh and HN-only Radar responses unchanged |
| **DTR26-S2B** | Single-instance, explicitly opt-in, bounded scheduler for enabled/due HN and RSS sources; reuse the same manual provider refresh/finalizer paths | **Separate** approved sub-scope check after S2A is independently accepted and Owner-published; fresh implementation review and handoff | Do not build an unreviewed queue, lease table, distributed coordinator, or background service in S2A |

This two-slice sequence is a **recommended delivery decomposition** of the Owner's S2 request. No S2B behavior is implied by S2A acceptance; the second slice requires current owner confirmation/authority and another preparation applicability check. If Codex finds one combined implementation truly simpler and equally reviewable, escalate for explicit Owner approval rather than combining it silently.

## Read before reviewing

1. [Owner scope and decisions](owner-scope-and-decisions.md) — binding preparation intent, candidate choices and exclusions.
2. [Source-backed API / normalization design](source-reuse-and-api-design.md) — S2A contract and compatibility policy.
3. [RSS/Atom fetch and parser safety](rss-atom-fetch-security.md) — arbitrary-host SSRF and untrusted XML requirements.
4. [Scheduled refresh contract](scheduled-refresh-design.md) — S2B only, source and cadence authority.
5. [Acceptance and review plan](acceptance-and-review-plan.md) — deterministic mandatory tests and decision gates.
6. S1 [API extension](../../../architecture/daily-trends-radar-api-extension.md), S1 [independent takeover acceptance](../2026-11-02-daily-trends-radar-preparation/reviews/2026-11-04-dtr26-s1-independent-takeover-review-1.md), [Phase 10 frozen rules](../../phase-10/README.md), current [Owner policy](../../../workflow/current-owner-delivery-policy.md).

## Scope constraints

No new top-level module, migration/DBML/ADR rewrite, new generic provider framework, RAG, Google Drive, frontend, web scraping, article-body downloading, social-site crawling, media/enclosure fetching, third-party content copying, user account model, autonomous AI, or unapproved network testing. S1 source/output contracts and historical reviews are immutable evidence. Root `personal-private-vault.zip` is preexisting and out of scope; the Owner has already said it was an accidental retained archive; **do not touch it in S2 preparation**.

## Workflow

1. Owner applies only these **six new Markdown files** on the verified base; check existing uncommitted changes and source drift before any review. Do not stage or publish from Codex/Antigravity.
2. Run **`$codex-pre-handoff-review`** in explicit, Owner-approved maintenance/feature-preparation mode. Review S2A for a finite, executable handoff and S2B as a future bounded design. Reviewer must reconcile material decisions and write a dated review under `reviews/`; **no production code, active handoff or network fetch** in this gate.
3. `READY_FOR_HANDOFF` for S2A means **Owner reviews/commits/pushes the reviewed preparation**, then explicitly invokes `$codex-create-handoff` for **S2A only**; Antigravity implements/self-reviews, separate Codex final-reviews, Owner publishes and records closeout.
4. S2B is considered only after S2A publication; owner confirms activation policy and scope, Codex independently re-checks the S2B plan and creates a separate handoff. Antigravity and reviewer do not inherit S1's exhausted `3/3` or Codex takeover.
5. Later: Google Drive (two accounts) -> Dashboard/API readiness -> final comprehensive non-RAG backend + docs audit (every actionable severity, including LOW/INFO) -> separate Phase 16 Frontend authorization. Ideaverse and RAG stay deferred/suspended.

**Docs Sync Check:** `UPDATED` — prospective S2 design package only; `ALREADY_CONSISTENT` — published S1, UI26 and current ACTIVE; `NOT_VERIFIED` — new RSS/provider/scheduler/runtime execution (all tests NOT_RUN); `DEFERRED` — existing `DTR26-FR-L01`, `UI26-FR-L02`, `SAD-FR-L01`, `CSA26-FR-L01`, other LOW/INFO, S19-2 and unrelated historic notices.

**Next step:** Owner -> Codex | Owner commits/pushes the reviewed preparation, then invokes `$codex-create-handoff` for S2A only | DTR26-S2A preparation and [independent review](reviews/2026-11-04-dtr26-s2a-independent-pre-handoff-review.md) | reviewed package published, current baseline checked, `ACTIVE=NO_ACTIVE_HANDOFF` | S2A implementation NOT_STARTED/future 0-of-3; takeover NOT_AUTHORIZED; S1 counters preserved.
