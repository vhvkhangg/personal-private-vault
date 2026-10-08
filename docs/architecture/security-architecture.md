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

## 4. Phase 13 public/protected paths

Public application paths:

```text
GET  /api/v1/auth/bootstrap/status
POST /api/v1/auth/bootstrap
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/revoke
```

Existing public operational/documentation paths remain:

```text
/actuator/health
/v3/api-docs/**
/swagger-ui/**
/swagger-ui.html
```

Everything else is bearer-protected by default, including `/api/v1/auth/private-pin/**` and every business route.

There is no public registration endpoint.

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
