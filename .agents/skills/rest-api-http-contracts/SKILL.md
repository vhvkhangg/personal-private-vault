---
name: rest-api-http-contracts
description: Guide Phase 13 REST/JSON adapters, ApiResponse/error consistency, security exposure, pagination truthfulness and OpenAPI contracts.
---

# REST API + HTTP Contract Modeling

Use for Phase 13 preparation, implementation, testing and review.

## Ownership

- controllers stay in the owning module under `internal.web`;
- controllers call existing owner operations/facades;
- no controller accesses another module internal/repository/entity;
- Knowledge/Collection external controllers use parent facades.

## Shared contract

Use the exact `ApiResponse` family from `docs/architecture/api-architecture.md`.

- success: `error = null`;
- error: `data = null`;
- no `/api/v1/**` empty-body 204;
- field errors never include rejected secret/private values;
- module-specific envelopes are forbidden.

Shared transport types live directly in the application root package per ADR-0016. Do not create a
`common`, `shared`, `api` or `web` application module.

## DTOs

Use explicit HTTP request/response DTOs. Never bind/serialize JPA entities directly and do not expose application
command/view records as the wire contract by default.

## Status mapping

- 400 malformed/structural validation;
- 401 authentication;
- 403 access denied;
- 404 absent resource;
- 409 conflict/current-state/duplicate;
- 422 business rule;
- 500 generic unexpected.

Map known domain exceptions explicitly. Never classify by exception-message text.

## Security

Public application routes are only bootstrap status/bootstrap/login/refresh/revoke. All other `/api/v1/**` routes
require Bearer JWT. Private PIN is protected.

Keep refresh tokens in explicit JSON requests for Phase 13; do not invent cookies/CORS/browser storage.

Security filter errors use the same JSON envelope.

## Pagination

Default HTTP `limit=50`, max 100 unless underlying contract is stricter.

Expose `offset` only when the accepted application operation supports it. Never fabricate `hasMore`, totals or page
counts.

Search pagination is passed through unchanged.

## OpenAPI

Document meaningful semantics, stable operation IDs, request validation, relevant errors and bearer/public security
requirements. Do not expose internal owner Search or ingestion/scheduler adapters.

## Privacy

No request/response body logging by default. Never log credentials/tokens/imported content/Markdown/finance/personal
payloads/raw Search content.

## Testing

Use MockMvc + existing PostgreSQL Testcontainers. Test envelope/status/security/OpenAPI/representative module routes
and architecture boundaries. Preserve all 817 post-milestone tests.
