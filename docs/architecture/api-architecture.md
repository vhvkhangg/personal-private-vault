# API Architecture

## 1. Style and versioning

The backend exposes a **REST/JSON** API documented with OpenAPI/Swagger.

The first public API version is:

```text
/api/v1
```

All application REST controllers introduced in Backend Phase 13 use this prefix. Do not add a second versioning
scheme, unversioned business endpoint, GraphQL surface, or gRPC surface.

The existing operational/documentation endpoints remain outside the envelope/version prefix:

- `/actuator/health`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

## 2. Controller ownership

REST controllers live inside the module that owns the use case, under module-internal web adapter packages such as:

```text
<module>/internal/web/
├── controller/
├── dto/
├── mapper/
└── advice/
```

A controller calls its module's existing public/application capability interface. It never injects another module's
repository/entity/internal service.

For `knowledge` and `collection`, top-level parent HTTP adapters use the parent facade. Do not expose nested-module
Java contracts directly to the HTTP layer.

HTTP adapters are not Spring Modulith named interfaces.

## 3. Shared HTTP contract placement

Phase 13 adds **no new top-level Spring Modulith application module**.

The small application-wide HTTP contract/configuration types live directly in the application root package:

```text
com.vhvkhangg.personalprivatevault
```

Expected shared types are narrowly limited to:

- `ApiResponse`
- `ApiError`
- `ApiFieldError`
- `ApiMeta`
- `ApiPageMeta`
- a small response factory/helper
- framework-level API exception advice
- OpenAPI configuration

This is the accepted Phase 13 exception to the root package's earlier bootstrap-only convention. It avoids a
`common`/`shared`/`web` application module and avoids adding dependency edges to the frozen business-module matrix.

No business logic, repository, entity, domain service, or module-specific DTO belongs in the root package.

See ADR-0016.

## 4. Exact `ApiResponse` wire contract

All JSON `/api/v1/**` success and error responses use one envelope. Do not invent module-specific envelopes.

Phase 14 adds two explicit successful binary-transfer exceptions:

- `GET /api/v1/images/{id}/content` -> streamed media bytes;
- `POST /api/v1/portability/exports` -> streamed `application/zip`.

GDI26-S3-H1 adds one explicit successful redirect exception:
- `GET /api/v1/auth/google/callback` -> HTTP 303 See Other redirect to `http://localhost:3000/api/bff/auth/google/complete` carrying HttpOnly cookies with no JSON response body.

For those binary routes, failures **before the servlet response is committed** use the normal JSON `ApiResponse`
envelope. After binary bytes/headers are committed, a stream/read/write/timeout/client-disconnect failure aborts the
transfer; the server must not append JSON to the binary body or reset a committed response. A portability ZIP counts
as complete only after ZIP finalization and normal response-body completion; truncated/non-finalized ZIPs are failed
exports. Image downloads use `Content-Length` when known so truncation is detectable. Multipart image upload itself
returns the normal JSON envelope.

Conceptual Java shape:

```text
ApiResponse<T>(
  T data,
  ApiError error,
  ApiMeta meta
)

ApiError(
  String code,
  String message,
  List<ApiFieldError> fieldErrors
)

ApiFieldError(
  String field,
  String message
)

ApiMeta(
  ApiPageMeta page
)

ApiPageMeta(
  Integer limit,
  Integer offset,
  Boolean hasMore
)
```

JSON uses camelCase.

### Success

```json
{
  "data": {},
  "error": null,
  "meta": null
}
```

Paged/windowed success when truthful page metadata exists:

```json
{
  "data": [],
  "error": null,
  "meta": {
    "page": {
      "limit": 20,
      "offset": 0,
      "hasMore": true
    }
  }
}
```

### Error

```json
{
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "fieldErrors": [
      {
        "field": "name",
        "message": "must not be blank"
      }
    ]
  },
  "meta": null
}
```

Rules:

- success means `error == null`;
- error means `data == null`;
- `fieldErrors` is an empty list when not applicable, never a map of rejected values;
- `meta` is null unless meaningful metadata exists;
- never include exception class names, SQL/vendor detail, stack traces, tokens, secrets, rejected secret values, or
  private payload excerpts;
- `/api/v1/**` does not use empty-body `204` responses; void/idempotent JSON actions return `200` with `data: null`;
- Phase 14 binary-success responses are the only approved non-envelope success bodies and still return JSON envelopes
  for errors;
- create operations normally return `201` with the envelope and may include `Location`;
- JSON null behavior is part of the contract above; do not globally hide null envelope fields.

## 5. HTTP DTO boundary

Do not bind or serialize JPA entities.

Do not use application command/view records directly as the HTTP wire model by default.

Each module creates explicit request/response DTOs under its `internal/web` adapter and maps them to/from the existing
public/application contracts. This keeps HTTP field naming, validation and future compatibility separate from the
domain/application API.

Sensitive authentication request DTOs must provide redacted `toString()` behavior or otherwise guarantee that
credentials/tokens cannot be rendered by logs/assertion diagnostics.

## 6. Validation

Validation occurs at both boundaries:

- HTTP request DTOs use Jakarta Bean Validation for structural requirements and length/range constraints;
- application/domain operations remain authoritative for business invariants;
- database constraints remain the final integrity layer.

Boundary validation must not duplicate or weaken domain invariants.

Unknown JSON properties are rejected for Phase 13 request DTOs rather than silently ignored, so client contract
mistakes fail visibly.

## 7. Error mapping

Use one shared framework-level `@RestControllerAdvice` for transport/framework failures and one module-local advice
where module exception classes need semantic mapping.

Canonical HTTP classes:

| HTTP | Meaning | Typical stable code family |
| ---: | --- | --- |
| 400 | malformed JSON, missing/invalid request shape, query/path validation | `MALFORMED_REQUEST`, `VALIDATION_ERROR` |
| 401 | missing/invalid bearer authentication, invalid credentials/refresh token | `AUTHENTICATION_REQUIRED`, `AUTH_INVALID_*` |
| 403 | authenticated but forbidden | `ACCESS_DENIED` |
| 404 | requested application resource absent | `<MODULE>_NOT_FOUND` |
| 409 | duplicate/conflict/illegal current state/concurrency conflict | `<MODULE>_CONFLICT` |
| 422 | structurally valid request violates a business rule | `<MODULE>_BUSINESS_RULE` |
| 500 | unexpected server failure | `INTERNAL_ERROR` |

Rules:

- stable codes use uppercase snake case;
- module handlers explicitly map known exposed exceptions; do not derive status/code from Java class names;
- an `Optional.empty()` at an HTTP resource lookup maps to 404;
- unexpected exceptions are logged without private payloads and return a generic 500 envelope;
- authentication entry-point/access-denied responses use the same envelope even though they occur in the Security
  filter chain.

## 8. Pagination and bounded lists

HTTP list query names are standardized:

```text
limit
offset
```

Phase 13 does not invent offset semantics where the frozen application operation only supports a bounded top-N/recent
read.

Rules:

- HTTP boundary default `limit`: 50;
- HTTP boundary maximum `limit`: 100 unless an existing operation has a stricter bound;
- `offset` is exposed only where the underlying accepted application contract already supports offset;
- Search retains its accepted `offset <= 500`, `limit <= 100`, bounded lookahead and truthful `hasMore`;
- `ApiPageMeta` is emitted only when the adapter can state its fields truthfully;
- no fabricated total count, page count or `hasMore`;
- limit-only recent/top-N endpoints return the bounded list without fake page metadata.

Phase 14 may add additional read-model hardening where justified; Phase 13 does not reopen domain query contracts just
to simulate pagination.

## 9. Authentication and security exposure

Bearer access tokens use:

```text
Authorization: Bearer <access-token>
```

Browser cookie/storage policy remains deferred.

Public application paths are limited to:

```text
GET  /api/v1/auth/bootstrap/status
POST /api/v1/auth/bootstrap
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/revoke
```

The refresh/revoke token is carried in an explicit JSON request body in Phase 13, not a cookie.

When `ppv.security.google-signin.enabled=true`, exactly three public method matchers are added:

```text
POST /api/v1/auth/google/start
GET  /api/v1/auth/google/callback
POST /api/v1/auth/google/handoff
```

`POST /api/v1/auth/google/link/start` remains bearer-protected and requires singleton Owner authentication plus fresh master password verification.

Private-PIN endpoints and all non-auth business endpoints are bearer-protected.

The existing public health/OpenAPI/Swagger paths remain public. Phase 13 does not add CORS policy, browser token
storage, server sessions, rate limiting, 2FA, passkeys, or deployment hardening.

## 10. OpenAPI

Springdoc remains the generator.

Phase 13 adds one shared OpenAPI configuration with:

- title and v1 description;
- bearer JWT security scheme;
- explicit public auth operations without bearer requirement;
- protected business operations documented with bearer security;
- meaningful summaries/descriptions and response/error semantics.

OpenAPI must reflect real request/response DTOs and validation.

Do not publish internal owner-search contracts, repositories, entities, Feed ingestion adapters, scheduler runtime,
or implementation-only endpoints.

## 11. Resource/action conventions

General mapping:

- `POST /resources` — create;
- `GET /resources/{id}` — find;
- `PUT /resources/{id}` — full accepted update command;
- `DELETE /resources/{id}` — existing soft-delete/trash operation only;
- `POST /resources/{id}/restore` — existing restore operation;
- association assignment — `PUT` on the association resource;
- association removal — `DELETE` on the association resource;
- domain transitions/actions that are not CRUD — `POST /.../{action}`.

Phase 13 must map existing use cases; it must not expose repositories or invent new business transitions.

## 12. Global search

The external Search endpoint is:

```text
GET /api/v1/search
```

Canonical query parameters:

- `q`
- repeated `domain`
- repeated `entryType`
- repeated `tagId`
- `offset`
- `limit`

It delegates only to `GlobalSearchOperations`.

Owner-module Phase 12 search interfaces are internal integration capabilities and receive no HTTP endpoints.

## 13. Privacy/logging

Do not log request/response bodies by default.

Never log or expose:

- passwords/PINs;
- access/refresh tokens or authorization headers;
- imported raw text/files;
- Note/Information Markdown;
- finance/personal payloads;
- private search queries/snippets/tags;
- raw vendor/SQL details.

Stable structural diagnostics may include endpoint name, safe error code, field name and numeric bounds.

## 14. Deferred

Phase 13 does not include frontend, object-storage I/O, export/backup, scheduler runtime, CORS/browser cookie storage,
rate limiting/security deployment hardening, RAG/vector search, or production deployment.

## 15. Phase 15 implementation notes

For standardized HTTP error status codes (415, 406, 400 with indexed paths, 422/409 domain translations) and streaming transfer error boundaries, see:
[`phase-15-implementation-notes.md`](phase-15-implementation-notes.md)


## 16. Phase 17 Alternative B contract amendment

Owner-approved 2026-10-15, [ADR-0020](../adr/0020-phase-17-single-job-reservations-e1.md).
Required future contract, not current wire support or execution enablement;
ADR-0016 root envelopes/auth/module-local adapters remain unchanged.

| Existing foundation surface | Approved bounded change |
| --- | --- |
| POST /api/v1/rag/sources/register-domain, /register-file; GET /sources and /sources/{id} | Registration never implies approval. Return current sourceVersion/consentResetVersion/authorizationEpoch/fieldPolicyVersion and selected fields/confirmed description hash only under authorized safe preview. Live file execution remains disabled. |
| POST /api/v1/rag/sources/{id}/approve | approvedFields plus required expectedSourceVersion, expectedConsentResetVersion, expectedAuthorizationEpoch, expectedFieldPolicyVersion; optional exact description confirmation only for opted-in field. Guarded CAS mismatch 409 SOURCE_CHANGED/no renewal. No automatic Private->NON_PRIVATE or implicit latest values. |
| POST /api/v1/rag/sources/{id}/revoke and every maintained affected owner/classification/field/dependency mutation | Reserved set: 409 RAG_SOURCE_BUSY/no mutation C; cancel-first, no queued edit. Unreserved revoke C denies related derivatives and starts verified purge, preserving unrelated item configuration. |
| POST /api/v1/rag/jobs/admit | Explicit owner Index/Reindex; no automatic launch/resume. Fixed approved closure, required current version/epoch scope and exact current changed-description hash confirmations. Persist confirmation/admission/reservations atomically; stale/missing required confirmation rejects, never silently drops fields. Second active job 409 RAG_JOB_BUSY. |
| GET /api/v1/rag/jobs/{id} | Report historical outcome separately from generation effective CURRENT/OUTDATED/REVOKED, cleanup status and pending recovery. No promise job SUCCEEDED implies current permission. Safe bounded status without denied content/count leaks. |
| POST /api/v1/rag/jobs/{id}/cancel | Stop request does not acknowledge safe editing release until verified tasks/process/tree termination plus SQL seal and release C. Pending/unknown stays nonterminal/reserved. Already finished reports actual completion/conflict; never participation revoke or deleting previous CURRENT index. |
| POST /api/v1/rag/jobs/{id}/finalize | Java-only trusted verification path, exact receipt/manifest/current authority; source/attempt seal/terminal/P/release atomic. E1 gate-time expiry; current late P can outlive completed work lease. Duplicate returns resolved identity, never new publication or replay of unknown C. |

These are amendments to the existing candidate actions, not new query/preview/
classification endpoint authorization. Use existing owning commands for config;
do not invent a new REST subsystem. Any final URI/DTO compatibility mapping must
be synchronized with OpenAPI/tests during separately authorized implementation.

Internal Java public operations are specified by P17-1; worker typed versioned
loopback scope binds actual job-owned process/endpoint, one-use grant, attempt,
input/field/dependency digests and deadlines. Claim before body consumption;
strict output/stage/kind/producer validation and SQL attempt fencing. No owner
JWT/PIN to Python, arbitrary path/SQL, existing-daemon fallback or worker P.
NSFW/Private and restrictive quoted metadata always deny. Unknown returns safe
typed conflict/unverified/pending; no raw source/path/credential diagnostics.

## Phase 18 proposed RAG extension (not implemented)

The [owner-approved D18-A additive RAG API/worker inventory](rag-phase-18-api-extension.md) adds **proposed typed batch admission and per-file report**, preserving the existing single-root API, `/api/v1` and shared `ApiResponse` envelope, and the module-local RAG HTTP adapter. No new shared envelope or cross-module dependency is authorized by this proposal; see [ADR-0021](../adr/0021-phase-18-bounded-multi-file-verified-indexing.md).

## 17. Universal Inbox API extension

Delivered 2026-11-01 under authorized maintenance slice `UI26-INBOX-BROWSE`.
Adds `GET /api/v1/saved-resources/inbox` for keyset-paged Universal Inbox browsing, preserving ADR-0016 envelopes, authentication, module-local adapter ownership, and existing save/read/conversion endpoints.
See [`universal-inbox-api-extension.md`](universal-inbox-api-extension.md).

## 18. Daily Trends Radar API extension

Delivered 2026-11-02 under authorized maintenance slice `DTR26-S1`.
Adds manual Hacker News source refresh (`POST /api/v1/feed/sources/{sourceId}/refresh`) and cached rolling-window Radar view (`GET /api/v1/feed/radar`), preserving ADR-0016 envelopes, single-user JWT authentication, Feed module-local adapter ownership, and article-centric deduplication against existing generic Feed storage.
See [`daily-trends-radar-api-extension.md`](daily-trends-radar-api-extension.md).

## 19. Daily Trends Radar RSS API extension

Implemented 2026-11-04 under authorized maintenance slice `DTR26-S2A`.
Adds manual RSS/Atom source refresh (`POST /api/v1/feed/sources/{sourceId}/refresh-rss`) and combined rolling-window Radar view (`GET /api/v1/feed/radar/combined`), preserving ADR-0016 envelopes, single-user JWT authentication, Feed module-local adapter ownership, and article-centric deduplication against existing generic Feed storage.
See [`daily-trends-radar-rss-api-extension.md`](daily-trends-radar-rss-api-extension.md).

## 20. Google Drive Connection Authority API extension

GDI26-S1 is submitted for separate independent review, default OFF; deterministic implementer evidence is linked from the [handback](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/implementation-handback-s1-2026-11-06.md). Public callback success/failure are fixed plain text (including its declared error responses); stored state selects the proof cookie, and duplicate security parameters/code-plus-error fail before exchange.

Implemented 2026-11-06 under authorized maintenance slice `GDI26-S1`.
Introduces 4 REST operations in the leaf module `drive`, disabled by default (`ppv.drive.enabled=false`):
1. `POST /api/v1/drive/connections/{slot}/authorize`: bearer-authenticated, returns consent URL and sets browser proof cookie.
2. `GET /api/v1/drive/oauth/callback`: public browser redirect callback, returns `text/plain` status.
3. `GET /api/v1/drive/connections`: bearer-authenticated, lists slot statuses (CONNECTED, RECONNECT_REQUIRED, DISCONNECTED) and display emails without exposing secrets.
4. `DELETE /api/v1/drive/connections/{slot}`: bearer-authenticated, removes local credentials immediately and executes one bounded remote token revocation.

Preserves the `/api/v1` prefix, root `ApiResponse` envelope for JSON operations, module-local adapter ownership in `com.vhvkhangg.personalprivatevault.drive.internal.web.controller`, and the exact 235-route OpenAPI baseline when disabled (239 when enabled).
See [`google-drive-connection-authority.md`](google-drive-connection-authority.md) and [ADR-0026](../adr/0026-google-drive-connection-authority.md).

## 21. GDI26-S2 direct binary file API extension

D08–D10 and [ADR-0027](../adr/0027-google-drive-root-file-authority.md) authorize four additional JWT-private operations only when both Drive and files flags are explicitly enabled. The [independent remaining-verification review](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md) records READY_FOR_OWNER_COMMIT; defaults remain OFF.

| Method | Path after `/api/v1/drive/connections/{slot}` | Result |
| --- | --- | --- |
| POST | `/root-folder` | 200 JSON root status (no request body) |
| GET | `/files?limit=25&cursor=…` | One direct-child page, limit 1–50, opaque bound cursor |
| POST | `/files` | Exactly one multipart `file`, required stable `Idempotency-Key`; 201 new / 200 verified repeat |
| GET | `/files/{fileId}/download` | Original binary attachment; no Range support |

JSON uses the root ApiResponse envelope and explicit module-local wire projections; binary uses application/octet-stream, safe UTF-8 attachment name, no-store and nosniff. A committed failure truncates the verified Content-Length and appends no JSON. Counts are 235 OFF, 239 S1-only, 243 with files enabled. See [file contracts](google-drive-files.md) and [operations](google-drive-file-operations.md) for bounds, action identity, errors and unverified live setup.
