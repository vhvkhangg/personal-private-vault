# Security Architecture

## 1. Current scope

Security v1 is deliberately scoped to the current development phase:

- permanent single user;
- one-time account bootstrap;
- login with username or email + password;
- JWT access token + refresh token lifecycle;
- refresh token rotation/revocation support;
- 6-digit private-mode PIN;
- private mode automatically locks after 5 minutes of inactivity by default.

Internet-deployment hardening is **not frozen yet**.

## 2. Passwords and PIN

Passwords and private-mode PINs must be stored only as strong password hashes. Plaintext password/PIN values must
never be persisted or logged.

The PIN is a UI/application privacy gate for someone physically using an already authenticated device. It is **not
encryption of personal database fields** and must not be described as protection against database theft.

## 3. JWT model

Use short-lived access tokens plus refresh tokens rather than one long-lived JWT.

Refresh-token requirements:

- persist only a token hash or equivalent non-replayable server representation;
- support expiration;
- support explicit revocation;
- support rotation/replacement tracking;
- do not log raw token material.

Protected Phase 13 HTTP requests use:

```text
Authorization: Bearer <access-token>
```

Refresh/revoke operations carry the raw refresh token only in an explicit HTTPS JSON request body at the API boundary.
Browser cookie/storage policy remains deferred to frontend/deployment work.

## 4. Authentication and authorization

Application routes that accept requests without a bearer token:

```text
GET  /api/v1/auth/bootstrap/status
POST /api/v1/auth/bootstrap
POST /api/v1/auth/login
POST /api/v1/auth/profiles/login
POST /api/v1/auth/refresh
POST /api/v1/auth/revoke
```

Each authentication endpoint still applies its own credential, proof or state checks.

When Google Drive integration is enabled (`ppv.drive.enabled=true`):
```text
GET  /api/v1/drive/oauth/callback
```
Only the exact HTTP GET callback accepts anonymous browser redirects. The callback still requires OAuth state and browser proof. Other Drive requests require PRIVATE authorization and the applicable route and feature flags.

When Google Sign-In is enabled, POST `/api/v1/auth/google/start`, GET
`/api/v1/auth/google/callback` and POST `/api/v1/auth/google/handoff` have their own
proof and state requirements. POST `/api/v1/auth/google/link/start` requires PRIVATE.

Existing public operational/documentation paths remain:

```text
/actuator/health
/actuator/health/**
/v3/api-docs/**
/swagger-ui/**
/swagger-ui.html
```

The [BF26-DP-A publication of 2026-11-12](../implementation/maintenance/2026-11-11-dual-login-profiles-preparation/owner-publication-closeout-dp-a-2026-11-12.md)
introduced persisted profile and session authority. PUBLIC was auth-only at that checkpoint.

- Access tokens carry `profile` (`PUBLIC` or `PRIVATE`), `sid` (session UUID) and `generation`.
- `SessionAuthorityFilter` validates persisted lineage on every request dispatch via a single-SQL admission join against `ppv_login_sessions` and `ppv_public_login_credential`.
- Profile elevation requires PUBLIC and fresh private password proof. Downgrade, public credential management and PIN operations require PRIVATE under the [authentication matcher table](authentication-profiles-and-sessions-api.md#1-authentication-matcher-table).

There is no public registration endpoint.

### Current business authorization matrix

The eight GET rows allowed for authenticated PUBLIC are the complete business allowlist.
PRIVATE remains subject to the matching route, domain checks, request validation and applicable feature flags.
Missing, invalid or expired authentication receives 401 on these business routes.

| Method and path | Anonymous | Authenticated PUBLIC | Authenticated PRIVATE |
| --- | --- | --- | --- |
| GET `/api/v1/fictions/{id}` | 401 | Eligible detail | Original detail contract |
| GET `/api/v1/films/{id}` | 401 | Eligible detail | Original detail contract |
| GET `/api/v1/reference/countries` | 401 | Allowed | Allowed |
| GET `/api/v1/reference/countries/{code}` | 401 | Allowed | Allowed |
| GET `/api/v1/reference/languages` | 401 | Allowed | Allowed |
| GET `/api/v1/reference/languages/{code}` | 401 | Allowed | Allowed |
| GET `/api/v1/reference/currencies` | 401 | Allowed | Allowed |
| GET `/api/v1/reference/currencies/{code}` | 401 | Allowed | Allowed |
| POST `/api/v1/films` | 401 | 403 `ACCESS_DENIED` | Existing Film creation with validation |
| GET `/api/v1/vault/trash` | 401 | 403 `ACCESS_DENIED` | Deleted-entry identity page |
| All other business methods and paths, including nested reads and mutations | 401 | 403 `ACCESS_DENIED` | Where a matching controller and its conditions permit |

Unknown paths under the PRIVATE fallback return the standard 404. The authentication and operational
exceptions above retain their own admission rules.

**Exact `ApiResponse.data` keys**

| Successful response | Keys |
| --- | --- |
| Fiction detail, PRIVATE GET/POST/PUT | `id`, `title`, `originalTitle`, `nationalityCode`, `posterUrl`, `format`, `isNsfw`, `genreId`, `authorPersonId`, `authorGroupId`, `description`, `totalChapters`, `progressStatus`, `consumptionStatus`, `currentProgressText`, `review`, `storyArchetypeIds`, `worldSettingIds` |
| Fiction detail, PUBLIC GET | The 18 Fiction keys above plus `authorName` |
| Film detail, PRIVATE GET/POST/PUT | `id`, `title`, `originalTitle`, `nationalityCode`, `posterUrl`, `format`, `productionStyle`, `isNsfw`, `directorPersonId`, `description`, `totalEpisodes`, `progressStatus`, `consumptionStatus`, `currentProgressText`, `review`, `genreIds`, `storyArchetypeIds`, `worldSettingIds` |
| Film detail, PUBLIC GET | The 18 Film keys above plus `directorName` |
| Country or Language list item or detail | `code`, `nameEn`, `nameVi` |
| Currency list item or detail | `code`, `name`, `symbol`, `decimalPlaces` |
| Vault trash page, PRIVATE GET | `items`, `nextCursor`. Each item contains only `id`, `entryType`, `deletedAt` |

Each PUBLIC work detail has 19 keys. Its added name key is always present and can be null.
Approved relationship IDs remain in the work payload even when the name is null.
A Person name requires the linked Person and a matching active `PERSON` Vault identity.
For Fiction with a group author, the CreatorGroup is resolved directly by its ID. The Person Vault check applies only to the Person branch.
An unresolved or blank display name yields null.

PUBLIC work eligibility requires a non-NSFW work with a matching-ID, correct-type, active Vault identity.
Missing, hidden, trashed and wrong-type works return the same safe 404 before name resolution.
Successful PUBLIC work responses and these 404s use `Cache-Control: no-store`. A genuine
lookup or transaction failure yields a safe 500 with `data: null` and no-store.

Vault trash uses descending ID keyset order, a default `limit` of 25 and an inclusive range of
1–100. The opaque `nextCursor` is null at exhaustion. Success and applicable errors use no-store,
including a safe 500 with `data: null` on a genuine read or transaction failure.

Published contracts are recorded in the [B1 allowlist closeout](../implementation/maintenance/2026-11-12-bf26-dp-b1-preparation/owner-publication-closeout-dp-b1-2026-11-12.md),
[RO-A Film POST revocation](../implementation/maintenance/2026-11-13-bf26-dp-public-read-only-preparation/owner-publication-closeout-dp-ro-a-2026-11-14.md),
[RO-B work detail closeout](../implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/owner-publication-closeout-dp-ro-b-2026-11-15.md)
and [V1 trash closeout](../implementation/maintenance/2026-11-15-bf26-v1-recycle-browse-preparation/owner-publication-closeout-bf26-v1-2026-11-15.md).

## 5. Bootstrap

The first account is created by the one-time bootstrap API flow. Secrets come from request/runtime input and must not
be committed as seed SQL, migration constants, example real credentials, or repository configuration.

The public bootstrap status endpoint returns only whether bootstrap is complete.

## 6. Security error contract

Missing/invalid/expired bearer authentication and access-denied responses use the shared Phase 13 `ApiResponse`
error envelope rather than HTML/default framework bodies.

Authentication failures remain generic where needed to avoid username/email enumeration.

Error responses never include credentials, raw JWT/refresh token, authorization header, signing material, internal
exception or SQL detail.

## 7. Secret management

Never commit passwords, PINs, JWT signing keys, database/cloud credentials, private API keys, production backups,
imported personal data or media.

Use environment/runtime secret injection.

## 8. Logging

Use SLF4J/Logback. Do not log credentials/tokens/auth headers, imported content, private Markdown, financial/personal
payloads or raw Search query/snippet/tag content.

Request correlation IDs remain optional and are not introduced by Phase 13.

## 9. CSRF/CORS/session policy

The backend remains stateless bearer-token based:

- no server HTTP session;
- CSRF remains disabled for this non-cookie bearer model;
- Phase 13 does not add a CORS policy;
- Phase 13 does not choose browser token storage/cookies.

Revisit these if a later frontend/deployment phase adopts cookie-based authentication.

## 10. Deferred hardening

Before broad Internet exposure, revisit TLS termination, cookies/token transport, CSRF, rate limiting/login
throttling, security headers, 2FA/passkeys, session/device management, audit logging, reverse proxy exposure, backup
encryption and secret rotation.

## 11. Phase 15 implementation notes

For password length bounds (12–128 characters) and delegating legacy BCrypt verification details, see:
[`phase-15-implementation-notes.md`](phase-15-implementation-notes.md)

## 12. Google Drive connection security and token custody

Submitted for separate independent review under Maintenance GDI26-S1 ([ADR-0026](../adr/0026-google-drive-connection-authority.md), [`google-drive-connection-authority.md`](google-drive-connection-authority.md)):

1. **Token Encryption at Rest:** Refresh tokens are encrypted using AES-256-GCM with a version 0x01 envelope, random 12-byte IV and 128-bit authentication tag. The Associated Authenticated Data (AAD) is bound to `slot:googleSub:credentialEpoch`. The 32-byte encryption key is read from an external file (`ppv.drive.token-key-file`) outside source control.
2. **Browser Cookie Binding:** OAuth authorization sets a short-lived (300s TTL) `HttpOnly`, `SameSite=Lax` cookie containing a CSPRNG proof value. The callback validates the SHA-256 digest of this cookie in constant time before invoking provider endpoints.
3. **OpenID Connect & Scope Whitelist:** OpenID Connect ID token signature is validated against bounded fixed-endpoint Google JWKS. Audience is mandatory and azp is checked separately. Allowed scopes are strictly verified to contain `https://www.googleapis.com/auth/drive.file`, `openid`, and `email`; Google's userinfo.email equivalent is accepted and every unselected scope is rejected.
4. **PH-03 Grant-wide Revocation Fence:** Disconnect immediately invalidates and deletes local credentials before sending a single bounded revocation request to Google. In-flight authorization attempts initiated before or during revocation are blocked from committing. Failed or timed-out remote revocations transition the subject into `REVOKE_UNCERTAIN` quarantine against reconnect.
5. **Export Classification:** `drive_connections` is included in `PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES`, preventing credential leakage into portability export archives.
