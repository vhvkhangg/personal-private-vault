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
