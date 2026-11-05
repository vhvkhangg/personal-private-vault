# Daily Trends Radar RSS API Extension

- **Status:** READY_FOR_OWNER_COMMIT (2026-11-05) for authorized maintenance slice `DTR26-S2A` per [independent takeover review 1](../implementation/maintenance/2026-11-04-dtr26-s2-preparation/reviews/2026-11-05-dtr26-s2a-independent-takeover-review-1.md); Owner publication pending.
- **Module:** `feed`
- **Specification:** Manual RSS/Atom feed source refresh and combined cached rolling-window Radar view extending the Feed module without schema changes, new dependencies, or phase reopening.

## 1. Route overview

### 1.1 Refresh RSS/Atom feed source

```text
POST /api/v1/feed/sources/{sourceId}/refresh-rss
```

- **Operation ID:** `refreshRssFeedSource`
- **Security:** Requires authenticated Bearer JWT token (`bearerAuth`).
- **Summary:** Manually fetch the configured RSS/Atom XML feed via strictly bounded HTTPS GET, validate through `RssFeedDestinationPolicy`, parse via streaming StAX parser, normalize/deduplicate by external article URL, and ingest newly discovered or updated entries into the feed item repository atomically.

#### Path parameters

| Parameter | Type | Required | Bounds / Validation | Behavior |
| --- | --- | --- | --- | --- |
| `sourceId` | integer (int64) | Required | Positive integer (`> 0`) | ID of the feed source to refresh. Must exist, be `enabled == true`, have `type == RSS`, and have a non-blank `feedUrl`. |

#### Wire response format

Standard PPV `ApiResponse` envelope (ADR-0016):

```json
{
  "data": {
    "sourceId": 2,
    "fetchedAt": "2026-11-04T10:00:00.123456Z",
    "parsedEntries": 25,
    "eligibleEntries": 23,
    "distinctArticleUrls": 20,
    "processedItems": 20
  },
  "error": null,
  "meta": null
}
```

#### Ingestion caps and limits

- Exactly 1 HTTPS GET request to the configured `feedUrl`. Zero redirect-following, zero automatic retries, identity encoding.
- Response body streaming ceiling: 1,048,576 bytes (1 MiB).
- XML StAX streaming limits: nesting depth $\le$ 32, event count $\le$ 20,000, total text code units across document $\le$ 1,048,576, record elements $\le$ 200.
- External article URL grouping: capped at 50 distinct URL groups per refresh in first-seen document order.
- Relative URL policy: relative links are dropped/ineligible (zero resolution, zero base fetching).
- Item identity: `externalId = null` for all RSS feed items; deduplication and identity key is strictly `(source_id, url)`.
- Atomicity: finalizer `@Transactional(REQUIRED, timeout=5)` executes item staging and row update `findByIdForUpdate` with pessimistic locking; on any mismatch (`SOURCE_CHANGED_DURING_REFRESH`), all staged items roll back completely.

### 1.2 Get combined cached Radar view

```text
GET /api/v1/feed/radar/combined
```

- **Operation ID:** `getCombinedFeedRadar`
- **Security:** Requires authenticated Bearer JWT token (`bearerAuth`).
- **Summary:** Retrieve the unified cached rolling-window radar view (past 24 hours) across all enabled Hacker News AND enabled RSS sources. Does not trigger external network calls.

#### Query parameters

| Parameter | Type | Required | Default | Bounds / Validation | Behavior |
| --- | --- | --- | --- | --- | --- |
| `limit` | integer | Optional | `50` | `1..100` | Page size limit between 1 and 100, default 50. `0`, negative, or `> 100` returns `400 VALIDATION_ERROR`. Non-numeric returns `400 MALFORMED_REQUEST`. |

#### Wire response format

```json
{
  "data": {
    "items": [
      {
        "feedItemId": 105,
        "sourceId": 2,
        "sourceName": "Engineering Blog",
        "sourceType": "RSS",
        "title": "Scaling Distributed Systems",
        "url": "https://blog.example.com/scaling",
        "publishedAt": "2026-11-04T09:30:00Z",
        "fetchedAt": "2026-11-04T10:00:00Z",
        "signalType": "RSS_SOURCE_CACHE",
        "sourceRank": null,
        "signalCapturedAt": "2026-11-04T10:00:00Z",
        "hnStoryId": null,
        "hnDiscussionUrl": null
      },
      {
        "feedItemId": 102,
        "sourceId": 1,
        "sourceName": "Hacker News Top",
        "sourceType": "HACKER_NEWS",
        "title": "Show HN: Fast Vector DB",
        "url": "https://example.com/vectordb",
        "publishedAt": "2026-11-04T08:15:00Z",
        "fetchedAt": "2026-11-04T09:00:00Z",
        "signalType": "COMMUNITY_SCORE",
        "sourceRank": 3,
        "signalCapturedAt": "2026-11-04T09:00:00Z",
        "hnStoryId": 41234567,
        "hnDiscussionUrl": "https://news.ycombinator.com/item?id=41234567"
      }
    ],
    "windowStart": "2026-11-03T10:00:00Z",
    "windowEnd": "2026-11-04T10:00:00Z",
    "retrievedAt": "2026-11-04T10:00:00Z"
  },
  "error": null,
  "meta": null
}
```

## 2. Categorical HTTP Status Code Mapping

| Scenario | HTTP Status | Error Code | Error Message |
| --- | --- | --- | --- |
| Missing / invalid Bearer token | `401 Unauthorized` | `AUTHENTICATION_REQUIRED` | Standard authentication error |
| Limit out of bounds (`0`, `> 100`, negative) | `400 Bad Request` | `VALIDATION_ERROR` | Parameter validation failure |
| Non-numeric path or query param | `400 Bad Request` | `MALFORMED_REQUEST` | Type mismatch |
| Source ID does not exist | `404 Not Found` | `FEED_SOURCE_NOT_FOUND` | `Feed source not found` |
| Source exists but is disabled (`enabled == false`) | `422 Unprocessable Entity` | `SOURCE_DISABLED` | `Feed source is disabled` |
| Source type is not RSS (`type != RSS`) | `422 Unprocessable Entity` | `UNSUPPORTED_SOURCE_TYPE` | `Feed source type is not supported for manual refresh` |
| Missing `feedUrl` / destination policy violation (private IP, non-HTTPS, unsafe host) | `502 Bad Gateway` | `UPSTREAM_DESTINATION_REJECTED` | `Upstream destination was rejected by network policy` |
| Non-200 upstream response, redirect, invalid media/encoding, XML syntax/root error, DTD/XInclude | `502 Bad Gateway` | `UPSTREAM_INVALID_RESPONSE` | `Upstream provider returned an invalid response` |
| Response body, records, events, depth, or text code unit limit exceeded | `502 Bad Gateway` | `FETCH_LIMIT_EXCEEDED` | `Fetch limit exceeded` |
| Refresh already running for this source | `409 Conflict` | `REFRESH_ALREADY_RUNNING` | `Refresh is already running for this source` |
| Ingestion uniqueness conflict | `409 Conflict` | `FEED_ITEM_CONFLICT` | `Feed item conflict` |
| Source modified or deleted during refresh | `409 Conflict` | `SOURCE_CHANGED_DURING_REFRESH` | `Feed source configuration changed during refresh` |
| Upstream timeout / network failure / DNS error / upstream 5xx | `503 Service Unavailable` | `UPSTREAM_UNAVAILABLE` | `Upstream provider is unavailable` |
| Upstream 429 rate limit | `503 Service Unavailable` | `UPSTREAM_RATE_LIMITED` | `Upstream provider rate limited the request` |
| Global refresh capacity reached (2 concurrent active) | `503 Service Unavailable` | `REFRESH_CAPACITY_REACHED` | `Global refresh capacity reached` |
| Finalizer pessimistic lock timeout or transaction failure | `503 Service Unavailable` | `REFRESH_FINALIZATION_UNAVAILABLE` | `Refresh finalization timed out or unavailable` |

## 3. Scope and Non-goals

- Strictly manual refresh: no background scheduler, tick loops, polling threads, or cooldown queues (deferred to S2B).
- Zero schema changes: uses existing `feed_sources` and `feed_items` tables and constraints without new Flyway migrations.
- Zero relative link resolution: only absolute HTTP/HTTPS links are eligible as article URLs.
- Zero article content fetching: only metadata and URLs discovered in the feed XML are stored.
