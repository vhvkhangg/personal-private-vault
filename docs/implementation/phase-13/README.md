# Backend Phase 13 — Shared REST/API Contract + Module HTTP Exposure

Status: **READY FOR OWNER COMMIT** (2026-10-06)

Preparation gate: **READY FOR HANDOFF**; accepted preparation owner committed/pushed as `b3d91a5`.
Active handoff: [`../handoffs/ACTIVE.md`](../handoffs/ACTIVE.md), ID `phase-13-rest-api`.
Codex [final acceptance](reviews/2026-10-06-phase-13-final-codex-acceptance.md) closes all FR13-1–FR13-7 findings.
Independent full clean verification passed **867 tests**, 0 failures/errors/skips. The earlier intermittent
baseline boundary-test failure is preserved in historical evidence and did not recur in this run.
Next: owner commits/pushes using the acceptance report's commit message, then gives the latest package to ChatGPT
for Phase 13 closeout/freeze and Phase 14 preparation. Agents must not commit/push; Phase 13 is not yet frozen.

The owner approved the prepared Phase 13 concept and ADR-0016's narrow root HTTP exception for preparation review
on 2026-10-05. Codex accepted the preparation in
[`reviews/2026-10-05-phase-13-pre-handoff-codex-acceptance.md`](reviews/2026-10-05-phase-13-pre-handoff-codex-acceptance.md).
The owner committed/pushed the accepted preparation and Codex created the active implementation handoff.

The Phase 10–12 milestone is `MILESTONE_READY`, its review/status package is owner committed/pushed, and
post-milestone synchronization/reset is complete.

The Phase 13 production implementation prerequisites are satisfied:

1. this preparation passed `$codex-pre-handoff-review`;
2. the accepted Phase 13 preparation slice is owner committed/pushed;
3. `$codex-create-handoff` created the active Phase 13 implementation handoff.

Implementation authority is limited to that handoff.

Phase 13 is not a milestone phase. The next milestone is after Phase 15.

## Goal

Expose the already-implemented backend capabilities through one versioned REST/JSON API while preserving every
frozen domain/module rule.

Phase 13 owns the exact shared HTTP response/error/page contract, `/api/v1` versioning, module-local controllers/
request/response DTOs/mappers/advice, authentication HTTP exposure, OpenAPI consistency, boundary validation,
truthful pagination metadata and HTTP integration/contract tests.

Phase 13 adds no new domain use case.

## Architecture authority

Follow:

- `docs/architecture/api-architecture.md`
- `docs/architecture/security-architecture.md`
- `docs/adr/0003-rest-json-openapi-api.md`
- `docs/adr/0016-root-http-contract-module-local-adapters.md`

No new application module or module-dependency edge is added.

## Shared root HTTP contract

Implementation may add a narrow root-package allowlist such as:

```text
ApiResponse
ApiError
ApiFieldError
ApiMeta
ApiPageMeta
ApiResponses
ApiExceptionHandler
OpenApiConfiguration
```

Do not create a new `common`, `shared`, `api`, `web`, `controller` or `dto` top-level package.

Update root `package-info.java` during implementation to document this narrow exception.

## Module-local HTTP adapters

Each exposed module uses real-code-only packages under:

```text
<module>/internal/web/
├── controller/
├── dto/
├── mapper/
└── advice/
```

Controllers inject the module's existing `*Operations`/facade contracts. They never access another module's
repository/entity/internal package.

Knowledge and Collection external HTTP adapters use the top-level parent facades, not nested-module APIs.

## Shared response/status rules

Use the exact envelope in `docs/architecture/api-architecture.md`.

- create -> 201;
- read/update/action -> 200;
- no `/api/v1/**` 204;
- malformed/structural request -> 400;
- auth -> 401;
- forbidden -> 403;
- not found -> 404;
- conflict/current state/duplicate -> 409;
- business rule -> 422;
- unexpected -> generic 500.

Every known exception reachable from an exposed operation must have an explicit semantic mapping or intentionally
fall through to generic 500. Never classify by exception-message text.

## HTTP DTO rule

Create explicit web request/response records. Do not serialize JPA entities, application command records, or
application view records directly as the wire contract.

Sensitive auth DTOs must not render credentials/tokens in logs or assertion diagnostics.

## Security

Public application paths only:

```text
GET  /api/v1/auth/bootstrap/status
POST /api/v1/auth/bootstrap
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/revoke
```

Health/OpenAPI/Swagger keep their existing public access.

All other `/api/v1/**` endpoints require Bearer JWT.

Phase 13 may change `SecurityConfiguration` only for these request matchers and JSON authentication-entry-point/
access-denied handling. JWT signing/validation/rotation, password/PIN behavior and stateless sessions stay frozen.

Refresh/revoke use explicit JSON bodies. Cookies/CORS/browser storage remain deferred.

## Canonical HTTP surface

| Owner | Route family | Existing capabilities exposed |
| --- | --- | --- |
| Authentication | `/api/v1/auth/**` | bootstrap status/bootstrap, login, refresh, revoke, private-PIN verify/change |
| Settings | `/api/v1/settings` | read, initialize-or-update |
| Reference | `/api/v1/reference/**` | read-only reference catalogs |
| Vault | `/api/v1/vault/**` | entry read/trash/restore, metadata, favorites/ratings/tags |
| People | `/api/v1/people/**` | persons, roles, creator groups/members |
| Fiction | `/api/v1/fictions/**`, `/api/v1/fiction-genres/**` | fiction, classifications, genres, links |
| Film | `/api/v1/films/**`, `/api/v1/film-genres/**` | films, classifications, genres, links, credits |
| Media | `/api/v1/albums/**`, `/api/v1/images/**` | albums/images and image listing |
| Location | `/api/v1/addresses/**`, `/api/v1/brands/**`, `/api/v1/location-categories/**`, `/api/v1/locations/**` | accepted location capabilities |
| Knowledge | `/api/v1/knowledge/**` | parent-facade Study/Information/Vocabulary/Note capabilities |
| Collection | `/api/v1/collection/**` | parent-facade Music/Shopping/Software capabilities |
| Account | `/api/v1/accounts/**` | external accounts, relationships, follower snapshots |
| Feed | `/api/v1/feed/**`, `/api/v1/saved-resources/**` | source configuration/reads, item reads, saved resources/conversions |
| ImportData | `/api/v1/imports/jobs/**` | create/parse/validate/execute/cancel/read jobs/items |
| Finance | `/api/v1/finance/**` | wallets/balance, categories, transactions, recurring rules, subscriptions |
| Journal | `/api/v1/journal/diary-entries/**` | diary create/read/update/range/soft-delete/restore |
| Personal | `/api/v1/personal/profiles/**` | profiles/self/read/update/soft-delete/restore |
| Search | `GET /api/v1/search` | `GlobalSearchOperations` only |

### Explicit exclusions

Do not expose:

- standalone `VaultEntryOperations.create`;
- Phase 12 owner-module Search contracts;
- `FeedItemOperations.ingestFetch`;
- feed/scheduler runtime;
- recurring finance auto-post/materialization;
- object-storage file upload/download;
- import object-key I/O;
- export/backup;
- hard/permanent delete;
- internal repositories/entities/services.

## Special route conventions

```text
POST   /api/v1/auth/refresh
POST   /api/v1/auth/revoke
POST   /api/v1/auth/private-pin/verify
PUT    /api/v1/auth/private-pin

DELETE /api/v1/vault/entries/{id}
POST   /api/v1/vault/entries/{id}/restore
PUT    /api/v1/vault/entries/{id}/favorite
DELETE /api/v1/vault/entries/{id}/favorite
PUT    /api/v1/vault/entries/{id}/rating
DELETE /api/v1/vault/entries/{id}/rating
POST   /api/v1/vault/tags
PUT    /api/v1/vault/entries/{id}/tags/{tagId}
DELETE /api/v1/vault/entries/{id}/tags/{tagId}

POST   /api/v1/knowledge/vocabulary/{id}/reviews
GET    /api/v1/knowledge/vocabulary/due

POST   /api/v1/imports/jobs/{id}/parse
POST   /api/v1/imports/jobs/{id}/validate
POST   /api/v1/imports/jobs/{id}/execute
POST   /api/v1/imports/jobs/{id}/cancel

GET    /api/v1/finance/wallets/{id}/balance
DELETE /api/v1/finance/{resource}/{id}
POST   /api/v1/finance/{resource}/{id}/restore

DELETE /api/v1/journal/diary-entries/{id}
POST   /api/v1/journal/diary-entries/{id}/restore
DELETE /api/v1/personal/profiles/{id}
POST   /api/v1/personal/profiles/{id}/restore
```

Associations use nested PUT/DELETE only when the underlying application capability exists. Do not invent removal
behavior absent from the domain API.

## ImportData HTTP boundary

No multipart/file storage is introduced.

`parse` accepts a JSON request DTO containing `rawText`. Structural HTTP request limits may protect the boundary
without changing parser/domain rules.

Never log or echo imported raw content in errors.

## Search HTTP boundary

`GET /api/v1/search` query parameters:

```text
q
domain       # repeated
entryType    # repeated
tagId        # repeated
offset
limit
```

Map directly to `GlobalSearchQuery`. The HTTP adapter must not rerank, refilter or re-snippet results.

## Pagination/bounds

- default HTTP `limit = 50`;
- maximum HTTP `limit = 100`, unless the underlying accepted operation is stricter;
- expose non-negative `offset` only where the underlying application operation already supports it;
- Search keeps its accepted bounds and truthful `hasMore`;
- emit `ApiPageMeta` only when truthful;
- do not fabricate totals/page counts/hasMore for limit-only reads.

## OpenAPI

Every operation needs a stable operation ID, concise domain description, validation schema, success/error responses,
and correct bearer/public security declaration.

Generated docs must not expose internal owner Search/ingestion/scheduler endpoints.

## Testing contract

Use MockMvc/Spring Boot integration plus existing PostgreSQL Testcontainers; no H2.

### Shared contract

Cover exact `ApiResponse` JSON, null behavior, 201/200, no 204, 400 validation/malformed JSON, 404, 409, 422, generic
500 and JSON security failures.

### Security

Cover all five public auth application paths without Bearer, protected private-PIN/business routes with
missing/invalid/expired Bearer, valid Bearer success, and existing health/OpenAPI/Swagger public paths. Prove no extra
anonymous `/api/v1/**` route exists.

### Module HTTP surface

At minimum one successful route per owner family above, plus representative mutations, Optional->404 and module
exceptions.

Knowledge/Collection tests prove parent-facade use.

Feed proves `ingestFetch` has no HTTP route. Search proves only global Search is exposed.

### Pagination

Cover default/max limit, stricter accepted bounds, Search page pass-through, image offset handling, and absence of
fabricated page metadata on limit-only reads.

### OpenAPI

Assert `/v3/api-docs` includes `/api/v1`, bearer scheme, public auth operations, representative response/error
schemas and unique operation IDs, while excluding internal Search/ingestion/scheduler surfaces.

### Architecture

Verify controllers/DTOs live under owner `internal.web`, no controller imports other-module internals/repositories/
entities, no JPA entity appears in controller signatures, root HTTP support stays within ADR-0016 allowlist, no new
top-level application module appears, and nested Knowledge/Collection modules do not own external controllers.

### Final verification

```text
mvn -f backend/pom.xml -ntp clean verify
git diff --check
```

Preserve the **817-test** post-milestone baseline and record exact results in
`docs/implementation/phase-13/test-evidence.md`.

## Documentation synchronization during implementation

Update `docs/repository/repository-package-tree.md` to the actual Phase 13 package inventory after code exists.
Synchronize status docs without rewriting historical Phase 10–12 reviews/evidence.

## Out of scope

- new domain use cases;
- schema/Flyway/DBML changes;
- new Spring Modulith application module/dependency edge;
- hard delete;
- multipart/object storage;
- export/backup;
- schedulers/background fetch/posting;
- CORS/browser cookie/token-storage choice;
- rate limiting/2FA/passkeys/security headers/deployment hardening;
- frontend/RAG/deployment;
- Phase 14+ implementation.

## Preparation tooling

Added:

- `.agents/skills/rest-api-http-contracts/SKILL.md`
- `.agents/rules/backend-phase-13-rest-api.md`
- Phase 13 HTTP-adapter clauses in top-level module `AGENTS.md` files

Reused:

- `backend-implementer`
- `architecture-auditor`
- authentication/security skill
- Java/Spring, modular-monolith, backend-testing, SOLID and reuse skills
- repository safety hook

No new custom agent or hook.
