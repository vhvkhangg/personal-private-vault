# DTR26 source-backed reuse and prospective API design

**Status:** prospective implementation design with Owner-approved `ARTICLE_URL` item identity (2026-11-02); independent PH-01/PH-02 preparation re-review remains required. **NO** production endpoint/provider described below exists at baseline `6da26d4a457f3465e8b43489b95202d886787036` unless marked **CURRENT**. Revalidate source with the actual execution HEAD.

## 1. Verified current implementation

| Current path (relative to repository root) | Current role / finding |
| --- | --- |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/enums/FeedSourceType.java` | Types `GITHUB_TRENDING`, `HACKER_NEWS`, `REDDIT`, `RSS`, `WEBSITE` already declared; types are **not** provider implementations |
| `.../feed/internal/domain/FeedSource.java` | `type`, `sourceUrl`, `feedUrl`, `enabled`, `scheduledRefreshEnabled`, `refreshIntervalMinutes`, `config`, `lastFetchedAt`, `nextFetchAt` |
| `.../feed/source/FeedSourceOperations.java` | create/update, read one source, bounded due-source lookup |
| `.../feed/internal/application/FeedSourceService.java` | validates schedule, enabled state and refresh interval; stores schedule configuration; does not fetch URLs |
| `.../feed/item/FeedItemOperations.java` | `ingestFetch(sourceId, fetchedAt, List<NormalizedFeedItemInput>)`, by-ID, recent per source |
| `.../feed/internal/application/FeedItemService.java` | validates batch, deduplicates exact normalized candidates, upserts by source external-id/URL hash and updates source time in one transaction; does not perform network I/O |
| `.../feed/internal/infrastructure/persistence/FeedSourceRepository.java` | due-source query + locked source read; no fetch-claim/lease mechanism |
| `.../feed/internal/infrastructure/persistence/FeedItemRepository.java` | recent-by-source order `published_at DESC NULLS LAST, id DESC`; no cross-source rolling-24h read |
| `.../feed/internal/web/controller/FeedController.java` | current `POST/GET/PUT /api/v1/feed/sources`, `GET /sources/due`, item by ID, recent source items; **no refresh endpoint** |
| `.../feed/internal/web/controller/SavedResourceController.java` | current `POST /api/v1/saved-resources/feed-item` and `GET /api/v1/saved-resources/inbox` |
| `docs/architecture/module-dependency-matrix.md` | `feed` may use public `vault` and `knowledge` contracts; no new module dependency justified |

Path ellipses above abbreviate the identical Java package prefix only; the actual ownership and code must be read, not guessed from this index.

## 2. Proposed S1 architecture

```text
Authenticated Owner
   | POST /api/v1/feed/sources/{id}/refresh   (PROPOSED)
   v
Feed-owned refresh application orchestration
   | preflight source enabled == true and type == HACKER_NEWS
   | bounded per-source single-flight; no DB transaction during HTTP
   | NB: preflight is NOT the final commit authority
   v
HN HTTPS API adapter (official topstories + bounded individual story IDs)
   | bounded bytes, response count, timeout, JSON depth and safe metadata
   v
NormalizedFeedItemInput[] (complete validated candidate list)
   | Owner-local HN finalizer: @Transactional(REQUIRED)
   |   1. invoke existing FeedItemOperations.ingestFetch(...)
   |      within SAME transaction (existing item -> source row-lock order)
   |   2. under the retained locked source row, refresh/check
   |      enabled && type == HACKER_NEWS before COMMIT
   |   3. on mismatch, rollback all items + fetch timestamps
   v
Existing feed_items + atomic source fetch state

Authenticated Owner
   | GET /api/v1/feed/radar?...              (PROPOSED)
   v
Bounded recent Feed item projection (read-only; no external HTTP)
   | pick an item id
   v
Existing POST /api/v1/saved-resources/feed-item
   v
Published Inbox UI26 (no duplicate bookmark table)
```

### Provider mapping recommendation

- Fetch the HN `topstories` ID list; use **only the first bounded N IDs** (default proposal `N=30`, max `N=50`; to be validated by Codex), then fetch only those corresponding JSON story records.
- Retain only `type=story`, non-deleted, non-dead records with valid ID/title, valid Unix submission time, and a nonblank **HTTP(S) external article URL**. Skip jobs, deleted/dead/unavailable stories, Ask HN/text-only stories and those without safe displayable external URLs under an explicit, tested skip policy. A story missing a *required* well-formed field must follow the documented malformed-record policy; **do not treat server-error/timeout or invalid top-list JSON as a valid empty feed**. Provider `time` is HN submission creation time, **not** the linked publisher's publication time.
- **PH-01 identity (Owner-approved `ARTICLE_URL`, 2026-11-02; implementation pending review):** FeedItem's stored `url` is the exact **trimmed external article URL**, with `externalId = null` for all S1 HN normalized candidates. Do **not** use `hn:<id>` as FeedItem external ID or fall back to the HN discussion URL. Two HN stories with the same exact trimmed article URL form **one** selected article. Choose the representative deterministically: greatest valid HN submission `time` (seconds), then greatest HN item `id` on ties. This is independent of the upstream top-list order. Retain the representative's `hnStoryId`, `hnDiscussionUrl`, `hnRank`, `hnScore`, `hnCommentCount`, `hnCapturedAt` in bounded provenance metadata, with `hnRank` being that story's **1-based position in the fetched top-list**. Do not claim that other same-URL discussions were archived or represented. If another submission wins in a later refresh, update the existing FeedItem **by URL hash**, retaining FeedItem ID. A previous stored HN `externalId` for the same URL must be tested against the current by-hash branch; replacing its old story ID with null is an intentional article-centric re-projection, not a second row. If the same HN ID later links to a **different URL**, the article-centric design treats it as a distinct article; old rows persist subject to normal retention, without auto-delete or cross-URL rewrite.
- `title`: normalize whitespace and safely interpret the HN HTML title strictly as **untrusted text**; never execute it or retain body/text/comment HTML; max 1000. `publishedAt`: HN submission time, not the linked article publish date. `author`: bounded HN username. HN discussion URL is a **metadata link only**, not the SavedResource target.
- `rawMetadata`: minimal immutable bounded provenance with explicit keys `hnStoryId`, `hnDiscussionUrl`, `hnRank`, `hnScore`, `hnCommentCount`, `hnCapturedAt`; these describe the **selected representative submission at that particular fetch**, not the linked article's publication, a global popularity score or all matching discussions. Validate numeric ranges, JSON shape and total storage bounds.
- Before ingestion, group eligible submissions by **the exact trimmed article URL**, using that string as a grouping key (not a second SHA algorithm); select one representative as above, then pass at most one normalized item per URL to unchanged `ingestFetch`. Preserve its trim/SHA-256 database identity contract. For the same article URL, a newer selected HN story is a metadata update, not a second row. For a revised HN story URL, two distinct article URLs are two distinct records; no cross-URL merge. **Saving a FeedItem to Inbox saves the article URL**, and a preexisting SavedResource with that URL still gives the accepted global-hash conflict. SavedResource metadata is a save-time copy, not automatically rewritten on refresh.
- **Do not** create new FeedSource automatically, silently alter enabled/schedule settings, or fetch arbitrary story URLs. HN's user-provided article URLs are stored and displayed, **not fetched**.

## 3. Proposed S1 HTTP contract — to be finalized by Codex

All are **proposed**, authenticated and use PPV's root `ApiResponse` / module-local error advice and OpenAPI pattern.

| Route | Behavior | Remarks |
| --- | --- | --- |
| `POST /api/v1/feed/sources/{sourceId}/refresh` | perform **one** bounded manual HN provider refresh; response counters only with provable meaning, e.g. `selectedTopIds`, `eligibleStories`, `distinctArticleUrls`, `processedItems` (count of result views, **not** inserted rows), and `fetchedAt` | non-GET action; no made-up inserted/updated counts; safe errors, no raw story HTML |
| `GET /api/v1/feed/radar?limit=50&sourceId=...` | return a **cached HN-only** bounded rolling-24h preview (fixed window, **no `hours` parameter** in S1), sorted by stored HN submission `publishedAt DESC, id DESC` | default `limit=50`, max 100 proposed, source filter optional and must point to enabled HN source; no external HTTP |

Proposed response fields: `items` containing `feedItemId`, `sourceId`, `sourceName`, `sourceType`, `title`, `url`, `publishedAt`, `fetchedAt`, `signalType`, optional `sourceRank` only when backed by stored HN representative-capture provenance (not rank at GET time). `windowStart`, `windowEnd`, `retrievedAt` are server-computed UTC `Instant`s. Do not invent an engagement score for RSS/GitHub, and do not claim a cross-platform "hot score".

### Read semantics

- **Fixed rolling 24h:** `windowStart = clock.now(UTC) - 24h`, `windowEnd = clock.now(UTC)`; no adjustable `hours` query parameter in S1. Compare stored HN **submission creation time** and include only `windowStart <= publishedAt <= windowEnd`. **Null, older and future** times are excluded; do not assert the linked article's publication date. All UTC bounds returned by the server for a fixed clock and testing.
- **S1 only enabled HN source rows are included** in Radar GET, irrespective of a source filter. If `sourceId` is supplied, require a source that exists, `enabled=true` and `type=HACKER_NEWS`; absent/disabled/wrong type receive stable safe module-local errors as defined by the handoff. Without a filter, select only enabled HN sources in the owner-local SQL query, even if stale cached items exist under disabled or retyped sources. Do not leak other source types via an unconstrained global query. The legacy `GET /sources/{sourceId}/items` behavior stays unchanged.
- Sort by `publishedAt DESC, feedItemId DESC` or another clearly documented deterministic key with stable source attribution; do not pretend this sort preserves HN's topstories rank. If an HN-rank view is needed, make the label and ordering explicit rather than combining incomparable provider rankings.
- Query with a database-side `LIMIT`, not an unbounded Java stream; a page-size cap 100 is consistent with existing routes. For a single aggregate list, cursor pagination is optional S1; ensure no unbounded scan and document whether the result is a bounded *preview* vs a complete 24-hour feed.
- The API must not execute remote requests on read. Save to Inbox is an explicit separate action via the accepted `feedItemId` API, so no duplicate save controller is necessary.

### Failure and concurrency semantics

- Fetch/normalize outside a DB transaction; provider timeout, unexpected status, body limit, invalid JSON, incomplete selected batch or failed identity validation must **not** mutate `feed_items` or move `last_fetched_at` / `next_fetch_at`. Explicitly allow item **null/dead/deleted/non-story/missing-external-URL** skips; failures to fetch any selected item, malformed JSON and invalid top-list shape must fail the whole request rather than silently publish a partial snapshot. An explicitly valid empty selection is a successful empty fetch.
- **PH-02 commit authority:** perform initial source-type/enabled check only as a preflight. After all HTTP work, call a distinct owner-local `@Transactional` finalizer (Spring proxy boundary; no ambient transaction during I/O). It calls the existing `FeedItemOperations.ingestFetch` under default `REQUIRED` propagation in the **same transaction**; that existing method obtains `findByIdForUpdate` and `entityManager.refresh` after item staging. Before the outer finalizer returns/commits, it reuses this locked source row and re-reads its authoritative state, checking both `enabled` and `type == HACKER_NEWS`. Any mismatch throws a stable Feed-owned `SOURCE_CHANGED_DURING_REFRESH`-equivalent error and rolls back **all** staged item upserts and fetch timestamps. The source row stays locked until the outer commit. **Do not pre-lock before ingest** (would reverse its existing item-then-source lock order and create a deadlock hazard); do not create a second transaction/`REQUIRES_NEW`, release a lock between check and commit, modify generic ingestion behavior or hold any SQL lock during network I/O. The independent reviewer must check the finalizer/proxy/REQUIRED feasibility and exact locked-state refresh rather than accept an outer optimistic precheck.
- Repeated successful fetch of unchanged provider items remains an upsert; existing URL/ID ambiguity detection and rollback remain in force. An empty legitimate HN `topstories` list may be a successful empty fetch **only if structurally valid and explicitly tested**; do not hide errors as emptiness.
- Single JVM/single Owner: use a bounded per-source single-flight/rejection mechanism to avoid overlapping **HN refresh** requests; release admission on all success/failure/interrupt paths, avoid holding a pessimistic DB lock over HTTP, and document single-process scope. Do not prevent existing generic source updates; a concurrent switch HN→RSS during HTTP must yield no committed HN items, nor any new fetch timestamp. Same-source already-running requests fail with stable `REFRESH_ALREADY_RUNNING`-equivalent response; other sources remain available. No distributed job system or automatic retries.
- Respect old source create/update behavior, including unrestricted strings held as metadata. **Network fetch policy is a distinct gate**; do not silently make previously valid manual source records invalid to edit unless that impact is separately approved.

## 4. Deferred S2 composition

RSS/Atom parser adapter with hardened XML/no external entities and outbound SSRF controls; optional scheduler reading existing `findDueSources` and invoking the same bounded refresh path. Schedule refresh should be explicit opt-in, disabled at application config by default until independently accepted. Concurrent claims/due logic and backoff must be designed for real deployment topology before activating. No migration is presumed; if exact failure/backoff/lease state cannot fit accepted schema, obtain Owner approval before adding a table or migration.

## 5. No automatic scope expansion

The design above is an S1 candidate, not a pre-validated executable acceptance contract. Codex must reconcile S1 with current source/ADRs and testing constraints. Do not add X/Reddit/GitHub providers, an AI agent, a web content summarizer, RAG or a frontend simply because they share the word "trends".
