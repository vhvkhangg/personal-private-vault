# Daily Trends Radar API Extension

- **Status:** DELIVERED (2026-11-02) under authorized maintenance slice `DTR26-S1`.
- **Module:** `feed`
- **Specification:** Manual Hacker News source refresh and cached rolling-window Radar view extending the Feed module without schema changes, new dependencies, or phase reopening.

## 1. Route overview

### 1.1 Refresh Hacker News feed source

```text
POST /api/v1/feed/sources/{sourceId}/refresh
```

- **Operation ID:** `refreshFeedSource`
- **Security:** Requires authenticated Bearer JWT token (`bearerAuth`).
- **Summary:** Manually fetch the current Hacker News top stories, deduplicate by external article URL, and ingest newly discovered stories into the feed item repository.

#### Path parameters

| Parameter | Type | Required | Bounds / Validation | Behavior |
| --- | --- | --- | --- | --- |
| `sourceId` | integer (int64) | Required | Positive integer (`> 0`) | ID of the feed source to refresh. Must exist, be `enabled == true`, and have `type == HACKER_NEWS`. |

#### Query parameters

| Parameter | Type | Required | Default | Bounds / Validation | Behavior |
| --- | --- | --- | --- | --- | --- |
| `maxItems` | integer | Optional | `30` | `1..50` | Maximum number of items to select from top stories (1-50, default 30). |

#### Wire response format

Standard PPV `ApiResponse` envelope (ADR-0016):

```json
{
  "data": {
    "sourceId": 1,
    "fetchedAt": "2026-11-02T09:00:00.123456Z",
    "selectedTopIds": 30,
    "eligibleStories": 28,
    "distinctArticleUrls": 25,
    "processedItems": 25
  },
  "error": null,
  "meta": null
}
```

### 1.2 Get cached Radar view

```text
GET /api/v1/feed/radar
```

- **Operation ID:** `getFeedRadar`
- **Security:** Requires authenticated Bearer JWT token (`bearerAuth`).
- **Summary:** Retrieve the cached rolling-window radar view (past 24 hours) across all enabled Hacker News sources. Does not trigger external network calls.

#### Query parameters

| Parameter | Type | Required | Default | Bounds / Validation | Behavior |
| --- | --- | --- | --- | --- | --- |
| `sourceId` | integer | Optional | `null` | Positive integer (`> 0`) | Optional feed source ID to filter radar items. Must be an enabled Hacker News source if specified (returns 422 if not HN). |
| `limit` | integer | Optional | `50` | `1..100` | Page size limit between 1 and 100, default 50. `0`, negative, or `> 100` returns `400 VALIDATION_ERROR`. Non-numeric returns `400 MALFORMED_REQUEST`. |

#### Wire response format

```json
{
  "data": {
    "items": [
      {
        "feedItemId": 42,
        "sourceId": 1,
        "sourceName": "Hacker News Top",
        "sourceType": "HACKER_NEWS",
        "title": "Show HN: My New Project",
        "url": "https://example.com/project",
        "publishedAt": "2026-11-02T08:30:00Z",
        "fetchedAt": "2026-11-02T09:00:00Z",
        "signalType": "HN_TOP_STORIES_AT_CAPTURE",
        "sourceRank": 1,
        "signalCapturedAt": "2026-11-02T09:00:00Z",
        "hnStoryId": 41234567,
        "hnDiscussionUrl": "https://news.ycombinator.com/item?id=41234567"
      }
    ],
    "windowStart": "2026-11-01T09:00:00Z",
    "windowEnd": "2026-11-02T09:00:00Z",
    "retrievedAt": "2026-11-02T09:00:00Z"
  },
  "error": null,
  "meta": null
}
```

When no items match the 24h window or no enabled HN sources exist:
- `data.items` is `[]`.

---

## 2. Error behavior

All error responses use the standard `ApiResponse` envelope:

| Status | Code | Cause |
| --- | --- | --- |
| `400 BAD_REQUEST` | `VALIDATION_ERROR` | Non-positive parameter or value out of bounds. |
| `400 BAD_REQUEST` | `MALFORMED_REQUEST` | Non-numeric parameter binding failures. |
| `401 UNAUTHORIZED` | `UNAUTHORIZED` | Missing, expired, or invalid Bearer JWT token. |
| `404 NOT_FOUND` | `NOT_FOUND` | Feed source with `sourceId` does not exist. |
| `409 CONFLICT` | `SOURCE_DISABLED` | Feed source exists but is disabled (`enabled == false`). |
| `409 CONFLICT` | `REFRESH_ALREADY_RUNNING` | Another refresh operation is currently in-flight for this `sourceId`. |
| `409 CONFLICT` | `SOURCE_CHANGED_DURING_REFRESH` | Feed source was disabled or its type was changed while refresh was in progress. |
| `422 UNPROCESSABLE_ENTITY` | `UNSUPPORTED_SOURCE_TYPE` | Feed source exists and is enabled, but its type is not `HACKER_NEWS`. |
| `502 BAD_GATEWAY` | `UPSTREAM_INVALID_RESPONSE` | Non-200 status, redirect, malformed JSON, missing required fields, or trailing content. |
| `502 BAD_GATEWAY` | `UPSTREAM_DESTINATION_REJECTED` | Unsafe IP/scheme/port/host rejected by destination policy. |
| `502 BAD_GATEWAY` | `FETCH_LIMIT_EXCEEDED` | Top-list > 64 KiB, item > 256 KiB, aggregate > 2 MiB, or metadata > 2 KiB. |
| `503 SERVICE_UNAVAILABLE` | `UPSTREAM_UNAVAILABLE` | Transport failure, DNS timeout/failure, or environment proxy configured. |
| `503 SERVICE_UNAVAILABLE` | `UPSTREAM_RATE_LIMITED` | Upstream provider returned HTTP 429. |
| `503 SERVICE_UNAVAILABLE` | `REFRESH_CAPACITY_REACHED` | Global admission capacity (max 2 concurrent refreshes) is exhausted. |
| `503 SERVICE_UNAVAILABLE` | `REFRESH_FINALIZATION_UNAVAILABLE` | Database finalization lock or transaction timed out. |

---

## 3. Fixed transport and resource budgets

To guarantee safe, predictable resource consumption:

- **Egress destination policy:**
  - Allowed host: `hacker-news.firebaseio.com` (fixed outbound authority)
  - Allowed scheme: HTTPS only
  - Allowed port: 443 only
  - Disallowed: Loopback, unspecified, private (RFC 1918), link-local, CGNAT, multicast, documentation, benchmarking, reserved, and transition forms (IPv4-compatible loopback `::127.0.0.1`, 6to4 `2002:7f00:1::`, Teredo, ISATAP).
  - Direct route only: Configured system or environment proxies (`HTTP_PROXY`, `HTTPS_PROXY`, `ALL_PROXY`, `http_proxy`, `https_proxy`, `all_proxy`, `socksProxyHost`) are rejected with `503 UPSTREAM_UNAVAILABLE`.
- **Payload limits:**
  - `topstories.json`: capped at 64 KiB, at most 1,000 distinct positive integer IDs; selection bounded `1..50` (default 30).
  - Per-item endpoint (`/v0/item/{id}.json`): capped at 256 KiB.
  - Total cumulative response payload: capped at 2 MiB per refresh.
  - Streaming body bytes counted to EOF; unexpected trailing tokens after single JSON root fail.
  - Metadata: capped <= 2 KiB per representative item.
- **Timeouts and monotonic deadlines:**
  - Overall monotonic refresh budget: 70 seconds.
  - Fetch/normalization sub-budget: 60 seconds.
  - Individual exchange deadline: 5,000 ms (including resolution, TLS handshake, connect, headers, and body streaming).
  - DNS resolution caller bound: 2,000 ms (executed via owned bounded 2-worker executor).
  - Connection request timeout: 1,000 ms.
  - Connect timeout: 2,000 ms.
  - Socket inactivity timeout: 2,000 ms.
  - Database finalizer transaction timeout: 5 seconds (`@Transactional(timeout = 5)`).
- **Concurrency bounds:**
  - Single-flight per `sourceId`: Concurrent refresh for the same source is rejected with `409 REFRESH_ALREADY_RUNNING`.
  - Global pool capacity: Maximum 2 concurrent refreshes system-wide. Excess requests are rejected immediately with `503 REFRESH_CAPACITY_REACHED`.

---

## 4. Article-Centric Deduplication (`ARTICLE_URL`)

Per Owner Decision `owner-scope-and-decisions.md`:

1. **Identity invariants:**
   - `externalId` is set to `null` for all Hacker News items.
   - `url` is the exact trimmed external article URL from the story.
   - Stories without external URLs (Ask HN, text-only posts) or invalid URLs are safely skipped.
2. **Within-batch grouping and tie-breaking:**
   - Multiple stories linking to the same trimmed article URL are grouped together.
   - The winner is selected deterministically: latest submission time (`time`), breaking ties with highest Hacker News ID (`id`).
3. **Representative HN provenance:**
   - Preserved in `rawMetadata` as bounded JSON map (capped <= 2 KiB):
     - `hnStoryId` (Long, > 0)
     - `hnDiscussionUrl` ("https://news.ycombinator.com/item?id=" + id)
     - `hnRank` (Integer, 1..50)
     - `hnScore` (Integer, non-negative, nullable)
     - `hnCommentCount` (Integer, non-negative, nullable)
     - `hnCapturedAt` (ISO-8601 UTC string captured strictly after fetch completion)
4. **Generic feed item ingestion:**
   - Ingested via unchanged generic `FeedItemOperations.ingestFetch(sourceId, fetchedAt, candidates)`.
   - Database-level unique constraint on `(feed_source_id, url_hash)` transparently prevents duplicate URL entries across consecutive refresh runs.

---

## 5. Transaction, Locking and Authority Invariants

1. **External I/O outside transactions:**
   - `HackerNewsRefreshService.refreshSource` is explicitly annotated with `@Transactional(propagation = Propagation.NEVER)`.
   - HTTP transport to Hacker News occurs entirely outside database transactions, holding zero database connections or table locks during network operations.
2. **Authoritative transactional finalization:**
   - Ingestion runs in `HackerNewsRefreshFinalizer.finalizeRefresh` with `@Transactional(propagation = Propagation.REQUIRED, timeout = 5)`.
   - Acquires a row-level lock via `FeedSourceRepository.findByIdForUpdate(sourceId)` and refreshes the entity with `entityManager.refresh(source)`.
   - Re-validates that the source remains `enabled == true` and `type == HACKER_NEWS`. If the source was disabled or modified concurrently during the external fetch, the finalizer rolls back and aborts without persisting items.

---

## 6. Cached Radar Semantics

1. **Purely cached preview:**
   - `GET /api/v1/feed/radar` queries existing persisted rows in `feed_items` joined with `feed_sources`.
   - Zero external HTTP calls or background task launches are performed.
2. **Rolling 24-hour window:**
   - Queries items where `published_at >= now - 24 hours` and `published_at <= now`.
   - Filters to sources where `enabled = true` and `type = 'HACKER_NEWS'`.
3. **Data policy and provenance validation:**
   - Rows are validated against `HackerNewsNormalizer.isValidArticleUrl(url)`; malformed legacy URLs are excluded.
   - Complete provenance is verified (`hnStoryId > 0`, `hnRank in 1..50`, canonical discussion URL matching story ID, parseable capture time). If invalid or malformed, the item falls back to `signalType = "SOURCE_CACHE"` with null provenance fields.
4. **Deterministic ordering:**
   - Sorted by `published_at DESC, id DESC`.
   - Database-side limit (`LIMIT :limit`) prevents excessive memory allocation.
5. **Untrusted content security:**
   - External URLs and titles are treated as untrusted strings. Listing never fetches, evaluates, or executes remote content.

---

## 7. Exclusions

This extension explicitly excludes:
- Background schedulers, cron jobs, or automated periodic polling.
- RSS/Atom feed parsing, Reddit, GitHub, or social media providers.
- Web scraping of article bodies, reader views, or Hacker News comment threads.
- Personalization, AI scoring, embedding generation, or RAG indexing.
- New database tables, columns, indexes, Flyway migrations, or Maven dependencies.
