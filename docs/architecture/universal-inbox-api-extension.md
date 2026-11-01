# Universal Inbox API Extension

- **Status:** DELIVERED (2026-11-01) under authorized maintenance slice `UI26-INBOX-BROWSE`.
- **Module:** `feed`
- **Specification:** Additive keyset-paged Universal Inbox browsing endpoint extending Phase 10/13 contracts without schema changes or phase reopening.

## 1. Route overview

```text
GET /api/v1/saved-resources/inbox
```

- **Operation ID:** `findSavedResourceInbox`
- **Security:** Requires authenticated Bearer JWT token (`bearerAuth`).
- **Summary:** Browse active saved web resources in descending saved-time order with optional kind filtering and truthful continuation.

### Query parameters

| Parameter | Type | Required | Default | Bounds / Validation | Behavior |
| --- | --- | --- | --- | --- | --- |
| `kind` | string | Optional | null (all kinds) | `ARTICLE`, `REPOSITORY`, `SOCIAL_POST`, `WEB_PAGE`, `OTHER` | Restricts results before Vault qualification. Empty normalizes to all kinds. Invalid enum returns `400 MALFORMED_REQUEST`. |
| `limit` | integer | Optional | `50` | `1..100` | Number of items to return. `0`, negative, or `> 100` returns `400 VALIDATION_ERROR`. Non-numeric returns `400 MALFORMED_REQUEST`. |
| `cursor` | string | Optional | null (page 1) | Max 512 chars, Base64url | Opaque seek token. Blank/malformed/out-of-range/filter-mismatch returns `400 INVALID_ARGUMENT`. |

## 2. Wire response format

Standard PPV `ApiResponse` envelope (ADR-0016):

```json
{
  "data": {
    "items": [
      {
        "id": 105,
        "feedItemId": null,
        "kind": "ARTICLE",
        "sourceName": "Spring Blog",
        "sourceUrl": "https://spring.io",
        "externalId": null,
        "title": "Spring Modulith 1.3 Released",
        "resourceUrl": "https://spring.io/blog/2026/01/modulith-release",
        "resourceUrlHash": "a1b2c3d4e5f6...",
        "author": "Oliver Drotbohm",
        "summary": "Deep dive into application module architecture.",
        "publishedAt": "2026-10-30T12:00:00Z",
        "fetchedAt": null,
        "savedAt": "2026-11-01T14:30:00.123456Z",
        "rawMetadata": {
          "tags": ["spring", "java"]
        }
      }
    ],
    "nextCursor": "djF8QVJUSUNMRXwyMDI2LTExLTAxVDE0OjMwOjAwLjEyMzQ1Nlp8MTA1"
  },
  "error": null,
  "meta": null
}
```

When query returns no items or reaches the terminal page:
- Empty result set: `data.items` is `[]`, `data.nextCursor` is `null`.
- Non-empty terminal page: `data.items` contains the final slice of items (`1..limit`), and `data.nextCursor` is `null` (since no further eligible lookahead items exist).

## 3. Error behavior

All error responses use the standard `ApiResponse` format with `error.code` and safe sanitized messages:

- **400 VALIDATION_ERROR**: Parameter validation failures (e.g. `limit` is 0, negative, or > 100).
- **400 MALFORMED_REQUEST**: Binding errors (e.g. `limit=abc`, `kind=UNKNOWN`).
- **400 INVALID_ARGUMENT**: Cursor decoding failures (blank string, non-base64, unsupported version, corrupt layout, out-of-range SQL timestamp, non-positive ID, or filter mismatch such as reusing an `ARTICLE` cursor with `kind=REPOSITORY`).
- **401 UNAUTHORIZED**: Missing, expired, or invalid Bearer token.

## 4. Query, lifecycle and safety invariants

1. **Active-only qualification:**
   - Candidate saved resources are matched against `vault_entries.deleted_at IS NULL` via the public `VaultSearchOperations.filterQualifyingActiveEntries` interface.
   - Trashed entries in the recycle bin never appear in the inbox.
   - Restoring a trashed Vault entry causes it to reappear at its original `(saved_at, id)` position.

2. **Live-list semantics:**
   - The list reflects the committed state of the database at query time. No write locks, cross-request snapshots, or reservation sessions are created.
   - Trash committed before batch qualification immediately excludes candidates from subsequent reads; newer saves above a cursor require restarting at page one.
   - Continuation does not require the anchor row to exist or remain active.

3. **Deterministic keyset ordering:**
   - Strict `(saved_at DESC, id DESC)` ordering guarantees stable pagination without duplicates or skipped items, even across exact timestamp ties.
   - Public cursors are emitted from the last returned item only when an additional eligible row (lookahead) confirms a subsequent page exists.

4. **Persistence context and query bounds:**
   - Feed candidate keyset queries fetch at most 100 rows per batch.
   - Candidate scanning uses non-entity projections (`SavedResourceRepository.CandidateSummary`), ensuring that scanning large contiguous blocks of trashed entries does not accumulate managed entity instances in the JPA persistence context.
   - Full `SavedResource` entities are retrieved only for the at most `limit` items returned in the final page.

5. **Untrusted content security:**
   - External URLs and titles are treated strictly as untrusted data. Listing never fetches, mirrors, parses, or executes remote content.
   - Existing manual URL acceptance rules (including non-HTTP schemes) remain intact.

6. **Backward compatibility:**
   - Preserves `POST /api/v1/saved-resources/manual`, `POST /api/v1/saved-resources/feed-item`, `GET /api/v1/saved-resources/{id}`, `GET /api/v1/saved-resources?urlHash=`, and `POST /convert/**`.
   - Legacy ID and hash reads continue to return trashed resources as originally designed.
   - Vault remains the exclusive owner of favorite, tag, trash, and restore operations.
