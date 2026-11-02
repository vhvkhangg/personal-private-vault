# Owner scope and decisions — Daily Trends Radar

**Owner request:** 2026-11-02 — begin Daily Trends Radar after published Universal Inbox closeout.  
**Baseline:** `main / 6da26d4a457f3465e8b43489b95202d886787036`.  
**Authority:** preparation/design and independent review only. The Owner explicitly **APPROVED `ARTICLE_URL`** as the DTR26-S1 FeedItem identity on 2026-11-02, after the independent review raised `DTR26-PH-01`. All other choices remain prospective implementation design requiring independent pre-handoff acceptance; this does **not** authorize production/network code, scheduler execution, deployment or an `ACTIVE` handoff.

## Owner-visible purpose

- Show the Owner a **recent, source-attributed list of technology/news links** in one backend feature that will later be used by the frontend.
- Refresh selected sources on demand; report success/failure honestly; never mistake "fetch attempted" for a successful ingest.
- Select a feed item and save it through the **existing**, accepted `/api/v1/saved-resources/feed-item` API, retaining existing duplicate/conflict semantics.
- Permit future scheduled ingestion and more approved providers without changing `FeedItemOperations.ingestFetch` or introducing a parallel database.

## Binding Owner identity choice and other design decisions to validate

| Topic | Owner disposition or proposed decision | Basis / outstanding check |
| --- | --- | --- |
| Module owner | `feed` only; reuse `feed_sources` + `feed_items` + public contracts | Source-backed; frozen module matrix |
| First source | Official **Hacker News API**, `HACKER_NEWS` type | Stable, permitted read interface; inspect official API and current availability at execution |
| Read window | Rolling `24h` based on **published timestamp**, not device-local calendar date | Reproducible across time zones; null/old timestamps not misrepresented as today |
| Ranking/label | HN API's `topstories` list position may be called *HN Top Stories*. Other feeds are *latest*, not objectively hot | Different popularity signals cannot be compared without a justified model |
| First refresh | Authenticated **manual** `POST` per source, bounded and idempotent at stored-item identity level | Avoid a global scheduler and unpredictable network traffic in S1 |
| Read API | One bounded, authenticated Radar read representation; reuse existing per-source reads where possible | Minimal additive HTTP/read model; no new global search engine |
| DB/schema | **No migration** in S1 by default | Existing tables/columns/unique keys are sufficient for bounded snapshot metadata |
| Source management | Reuse existing create/update and enabled/source type; avoid changing accepted API contract | Existing `scheduledRefreshEnabled` is configuration only, not an active scheduler |
| Story identity (`DTR26-PH-01`) | **OWNER-APPROVED: `ARTICLE_URL` (2026-11-02)** — exactly one FeedItem per **exact trimmed external article URL**, not one per HN story. `externalId=null`; chosen representative HN story ID/rank/time in bounded metadata. | Binding S1 design decision: Save for Later stores the original article URL; reviewer must still verify collision/upsert handling before `READY_FOR_HANDOFF`. |
| Missing or invalid article URL | **Skip from article-only S1**, including HN discussions without an external article link; do not silently substitute HN discussion URLs. | HN discussion can be supported in a separately approved future enhancement. |
| Timeline | S1 for HN; S2 RSS/Atom and optional scheduling only by later Owner approval | Lower regression and network-policy risk |
| GitHub | Later optional official repository search only, clearly described as search/approximate popularity; **not** GitHub Trending API | GitHub has search endpoints, not an official Trending list endpoint |
| Reddit | Deferred pending explicit lawful/authorized API access and retention/deletion obligations | `REDDIT` enum is not an access grant |

## Explicit non-goals

No X/Facebook/TikTok scraping, Reddit workarounds, social media downloading, bulk image/video copying, fiction copies/backup, LLM/agent summarization, Hermes/OpenClaw, RAG data, Google Drive, Ideaverse, browser UI, external article body fetch, user-profile inference, ad/tracking data, content personalization, global trend scoring, notifications, unrestricted webhook URL fetching, new tenants or credentials. Do not activate archived S19-2 QA, indexing or native workers.

## Boundary and compatibility rules

1. **Frozen Phases 10/13 remain historical.** New future source changes are permitted only under a newly reviewed and Owner-published maintenance/feature handoff. Existing create/update/list/ingest/save/conversion semantics remain valid.
2. **External network I/O is not authorized by `feed_sources` alone.** A new provider adapter must validate its target URL at each hop and its selected source type; old records may contain HTTP, non-HTTPS or private addresses because earlier acceptance did not define fetch-time URL policy. Do not silently rewrite existing rows or reject previously accepted `POST /sources` values unless separately approved.
3. **No DB lock while waiting for remote HTTP.** Keep outbound fetch/parse outside any transaction. After fetching, an owner-local transactional HN finalizer invokes the **unchanged** generic `FeedItemOperations.ingestFetch` in its **same REQUIRED transaction**, then under its existing `findByIdForUpdate` source-row lock re-reads the authoritative source and enforces `enabled=true && type=HACKER_NEWS` **before commit**; any mismatch rolls the entire ingest and source timestamps back. Do not check only before the network, release the source lock before commit or reverse the existing item-then-source lock order by pre-locking the source. Explicitly prove propagation and lock retention with PostgreSQL tests. If code inspection shows the shared REQUIRED transaction cannot be guaranteed, STOP rather than writing a check-then-ingest workaround.
4. Preserve per-source unique keys `(feed_source_id, external_id)` and `(feed_source_id, url_hash)`, the trimmed-URL SHA-256 rule, immutable JSON snapshot isolation and privacy-safe errors. **For S1 article identity, normalized `externalId` is null** (do not send `hn:<id>` as the uniqueness key); select exactly one representative per exact trimmed article URL **before** `ingestFetch`. Put its HN story ID/discussion URL/rank/score/capture time only in bounded `rawMetadata`. No URL normalization beyond existing trim, uniqueness relaxation or silently mixed article/discussion identity. Existing FeedItem rows with a prior HN `externalId` must be reconciled safely by the existing URL-hash upsert, with full real-DB tests; do not assume no historical data.
5. New route(s) use the existing authenticated `ApiResponse`, module-local DTOs/advice, explicit validation and OpenAPI inventory update. HTTP fetch is **not** performed by GET/read endpoints.
6. Never turn scheduled refresh on merely because a source has `scheduledRefreshEnabled=true`: that field predates the scheduler and is currently not an executing guarantee.

## Review decisions that must remain visible

The independent preparation reviewer must verify: (a) HN API transport/metadata rules; (b) whether manual-refresh + list completes one useful vertical slice; (c) existing `ingestFetch` safety and transaction semantics on empty/partial response; (d) actual API/error shape and limits; (e) whether an additive schema change is genuinely required; and (f) risk of a new `@Scheduled` path. It must not unilaterally promote S2, GitHub, Reddit, or new credentials into S1.

The initial `DTR26-PH-01` reviewer found a real collision: two HN submissions may share the same exact article URL while their HN IDs differ. **Owner decision, 2026-11-02: `ARTICLE_URL` confirmed explicitly.** The selected S1 model is article-centric, not HN-discussion-centric: deduplicate exact trimmed article URLs before generic ingest, normalize `externalId=null`, retain selected HN story details as provenance, and save the article URL via the existing Inbox endpoint. `HN_DISCUSSION_URL` was the reviewed alternative but **is not selected for S1**. Do not mix the two identities, invent URL canonicalization or silently relax existing unique keys. This resolves the Owner choice; the reviewer alone decides whether the documented collision handling sufficiently closes PH-01.

The `DTR26-PH-02` response is a proposal for an HN-specific shared-transaction finalizer as described above. The independent reviewer must confirm it protects type/enabled checks through commit and retains the generic ingest lock order.

If a material requirement cannot fit S1 without a schema/frozen contract change, record `OWNER_DECISION_REQUIRED` with the minimal alternative. Do not approve an arbitrary extension or label an unimplemented provider as ready.

**Next step:** Owner | apply the Owner-confirmed `ARTICLE_URL` PH-01/02 documents and rerun Codex `$codex-pre-handoff-review` | DTR26-S1 and original `reviews/2026-11-02-independent-pre-handoff-review.md` | Owner choice recorded; Codex must independently close PH-01/02 before `READY_FOR_HANDOFF`; `NO_ACTIVE_HANDOFF`, implementation `0/3`, takeover not authorized.
