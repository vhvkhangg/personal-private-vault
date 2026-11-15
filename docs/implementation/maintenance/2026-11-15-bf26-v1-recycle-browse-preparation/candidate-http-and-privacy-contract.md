# BF26-V1 — proposed HTTP, pagination and privacy contract

**Status: CANDIDATE / NOT_YET_OWNER_RATIFIED / NOT_IMPLEMENTED.** Derives from [2026-11-11 Backend-first assessment](../2026-11-11-backend-first-assessment/backend-readiness-assessment-2026-11-11.md) and current `main` implementation. This is a future change specification, **not** evidence of current behavior or permission to change frozen code.

## 1. Single new operation and admission boundary

`GET /api/v1/vault/trash?limit=25&cursor=...` under the existing `VaultController`, uniquely named `listVaultTrash` (candidate operation ID; review for manifest collisions). `limit` and `cursor` are query parameters. No new endpoint for restore, purge, permanent delete, search, count or metadata enrichment.

| Caller | Proposed result | Security invariant |
| --- | --- | --- |
| Valid persisted PRIVATE session | 200 with canonical `ApiResponse`; item metadata only | Default existing PRIVATE fallback, no change to `SecurityConfiguration` |
| Valid persisted PUBLIC session (direct login or PRIVATE→PUBLIC downgrade) | **403 `ACCESS_DENIED`**, before handler | Not in the eight existing PUBLIC business GET pairs; never expose even ID/type/deleted timestamp |
| Anonymous / expired / invalid token | **401** canonical authentication error | No handler, database read or result disclosure |
| Valid PRIVATE session, invalid query | **400** canonical safe transport/argument error | No repository read or cursor echo that reveals internal state |

The eight existing PUBLIC business GET pairs (Fiction/Film direct detail + six Reference endpoints) remain exactly eight; PRIVATE still has its existing authorized routes. Status and content of existing Vault ID-based trash/restore endpoints remain unchanged. No new security matcher, broad GET wildcard, profile handling redesign or PIN model.

## 2. Exact suggested success wire

```json
{
  "data": {
    "items": [
      {"id": 142, "entryType": "FILM", "deletedAt": "2026-11-14T08:12:01Z"}
    ],
    "nextCursor": "<opaque-versioned-cursor-or-null>"
  },
  "error": null,
  "meta": null
}
```

The JSON above is an **illustrative future specimen, not observed data**. Proposed item has **only three** keys: `id` (positive int64), `entryType` (existing Vault enum name), `deletedAt` (non-null UTC ISO-8601 instant). No created/updated timestamps in new list item; existing `VaultEntryView` may still carry them internally. `items` always present as array; `nextCursor` always present as a string or explicit `null`, not omitted. `data` never includes work title, author/director, review, NSFW, source filename, tags, rating, favorite, relationship, credential, content or feature-owned DTOs.

**Wire choice under review:** use `ApiResponse.data = {items,nextCursor}` and leave `ApiResponse.meta = null`, matching the existing Feed cursor-page concept. Do **not** misuse root `ApiPageMeta.offset` for keyset; avoid duplicating a contradictory page model. Exact generated OpenAPI must show this shape and the PRIVATE-only permission contract. `Cache-Control: no-store` is proposed for successful and application-level error responses containing/referencing trash metadata; it must be tested against existing security-layer response headers without rewriting global filters.

## 3. Keyset contract and bounded parsing

- `limit` absent → **25**. Accepted integer range **1–100**, inclusive. Reject zero, negative, overflow, non-integer and out-of-range with canonical 400; note framework type mismatch may use existing `MALFORMED_REQUEST` while explicit range/codec errors may use `INVALID_ARGUMENT`. The reviewer should verify exact status/codes through HTTP specimens instead of inventing one code for both branches.
- `cursor` absent → first page. If supplied, must be a strict **unpadded Base64url UTF-8 encoding** of a versioned, single positive ID marker such as `v1|142`; this is **a candidate format**, not a published client contract. Limit encoded length (proposal: **128 characters**) and reject blank, padding if canonical encoding forbids it, invalid UTF-8, extra delimiters/fields, unknown version, nonpositive, nondecimal, overflow or malformed IDs. Re-encode/compare or equivalent canonical verification to reject ambiguous encodings. No cursor tokens, secrets or raw parser diagnostics in API errors or logs.
- Seek predicate: `deleted_at IS NOT NULL AND (cursor absent OR id < cursorId)`; **ORDER BY id DESC**, fetch no more than `limit + 1` qualifying rows using a parameterized bounded database query. Do not page in memory, use offset, or issue a count query. Build `nextCursor` only if an extra row exists, encoding the **last emitted** ID. Return at most `limit` items. If no next page, `nextCursor: null`.
- `id DESC` reflects identity creation order **not deletion chronology**; deleted-at-only order would require a separately approved alternative cursor/index strategy. Cursor encoding is not a security or authentication mechanism and must not be trusted to grant privileges.
- When membership is stable, pagination must cover all deleted entries across all enum types without duplicates/skips or order drift; test at least three pages, with interspersed active entries. Read-only query must work on empty, one-item, exactly-limit, limit+1, and exhausted pages.
- **Weak cross-request consistency by design:** a restore after page 1 removes the entry from subsequent eligible pages; re-trash may re-enter later pages according to its unchanged ID. A newly trashed entry with higher ID than the current cursor may be missed in an ongoing traversal. No cross-request snapshot or exactly-once scan claim; concurrency must not cause an unbounded loop or write.

## 4. Existing lifecycle compatibility

Keep `moveToTrash` and `restore` idempotence, timestamps, guards, locks and existing source reservation behavior unchanged; new GET is genuinely read-only and never holds mutation locks. Retain active-only search semantics, restore-by-ID and old API response schemas. No permanent purge, cascade, per-feature deletion policy, external provider, scheduled job, DTO hydration or owner-data migration. No schema/Flyway/DBML/index/dependency change is presently justified by source; check a real PostgreSQL plan and escalate if evidence demands index/ordering modifications.

## 5. Explicit Owner contract choices before source grant

1. **Privacy:** accept identity-only `{id,entryType,deletedAt}` without a title, despite reduced UI usefulness.
2. **Ordering:** accept `id DESC` rather than most recently deleted first, with documented weak pagination consistency.
3. **Wire:** accept canonical envelope with `{items,nextCursor}`, `meta:null`, opaque versioned cursor and no global pagination framework.
4. **Frozen-boundary exception:** after independent review, authorize exact Vault Phase 1 and REST/API Phase 13 source/test files **separately**. Source authority does not follow automatically from these proposals.
5. **Branch/worktree:** if adopting a non-`main` path, select and authorize it explicitly; no worktree or branch creation is included in preparation.
