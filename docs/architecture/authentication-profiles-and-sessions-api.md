# Authentication Profiles and Sessions API Specification (BF26-DP-A)

- **Status:** Proposed / Implemented Awaiting Codex Review (2026-11-11)
- **Authority:** [BF26-DP-A Executable Handoff](../implementation/maintenance/2026-11-11-dual-login-profiles-preparation/executable-handoff-dp-a-2026-11-11.md), [D1–D6 Ratification](../implementation/maintenance/2026-11-11-dual-login-profiles-preparation/owner-ratification-d1-d6-2026-11-11.md), [ADR-0029](../adr/0029-fixed-profiles-and-persisted-session-authority.md)

## Overview

BF26-DP-A introduces two fixed access profiles for the singleton vault Owner (`app_users.id = 1`):
- `PRIVATE`: Unrestricted access to all business modules, settings, PIN operations, and data export.
- `PUBLIC`: Auth-only access. Strictly forbidden from all business modules, data APIs, search, portability, media, and settings (returns 403 `ACCESS_DENIED`).

## 1. Authentication Matcher Table

| Method | Path | Required Profile | Notes |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Anonymous / Any | Master credentials login (`identifier`, `password`); always returns `PRIVATE` access token. |
| `POST` | `/api/v1/auth/profiles/login` | Anonymous / Any | Identifier-free public login (`profile="PUBLIC"`, `password`); returns `PUBLIC` access token. |
| `POST` | `/api/v1/auth/refresh` | Anonymous / Any | Rotates refresh token; preserves existing profile and lineage. |
| `POST` | `/api/v1/auth/revoke` | Anonymous / Any | Revokes refresh token and lineage. |
| `GET` | `/api/v1/auth/bootstrap/status` | Anonymous / Any | Reports bootstrap status without authentication. |
| `POST` | `/api/v1/auth/bootstrap` | Anonymous / Any | One-time initial setup of Owner and unconfigured public credential metadata. |
| `POST` | `/api/v1/auth/profiles/public-credential` | `PROFILE_PRIVATE` | Sets or resets public password; requires fresh private password proof. |
| `POST` | `/api/v1/auth/sessions/elevate-private` | `PROFILE_PUBLIC` | Elevates active session from PUBLIC to PRIVATE using fresh private password. |
| `POST` | `/api/v1/auth/sessions/downgrade-public` | `PROFILE_PRIVATE` | Downgrades active session from PRIVATE to PUBLIC; requires configured public credential. |
| `POST` | `/api/v1/auth/private-pin/verify` | `PROFILE_PRIVATE` | Verifies 6-digit PIN. |
| `PUT` | `/api/v1/auth/private-pin` | `PROFILE_PRIVATE` | Updates 6-digit PIN. |
| `*` | Any other application route | `PROFILE_PRIVATE` | Strict default DENY. Any request with `PROFILE_PUBLIC` receives 403 `ACCESS_DENIED`. |

## 2. DTO Wire Contracts

### `POST /api/v1/auth/profiles/login`
- **Request Body:** Strict JSON (no extra fields permitted):
  ```json
  {
    "profile": "PUBLIC",
    "password": "user-configured-public-password"
  }
  ```
- **Responses:**
  - `200 OK`: `ApiResponse<AuthTokensResponse>` with claims `profile: "PUBLIC"`.
  - `400 BAD_REQUEST`: Validation error (missing fields, wrong profile name, or extra identifier).
  - `401 UNAUTHORIZED`: Invalid credentials (`AUTH_INVALID_CREDENTIALS`). Unconfigured public login returns the same 401 code.

### `POST /api/v1/auth/profiles/public-credential`
- **Headers:** `Authorization: Bearer <private-access-token>`
- **Request Body:** Strict JSON:
  ```json
  {
    "privatePassword": "current-private-password",
    "publicPassword": "new-public-password-12-to-128-chars"
  }
  ```
- **Responses:**
  - `200 OK`: `ApiResponse<Void>` with `data: null`.
  - `401 UNAUTHORIZED`: Private password verification failed (`AUTH_INVALID_CREDENTIALS`).
  - `422 UNPROCESSABLE_ENTITY`: Public password identical to private password (`AUTH_PASSWORD_NOT_DISTINCT`).
  - `403 FORBIDDEN`: Called with PUBLIC profile token (`ACCESS_DENIED`).

### `POST /api/v1/auth/sessions/elevate-private`
- **Headers:** `Authorization: Bearer <public-access-token>`
- **Request Body:** Strict JSON:
  ```json
  {
    "privatePassword": "current-private-password"
  }
  ```
- **Responses:**
  - `200 OK`: Replacement `ApiResponse<AuthTokensResponse>` with `profile: "PRIVATE"`, incremented generation.
  - `401 UNAUTHORIZED`: Private password incorrect.
  - `403 FORBIDDEN`: Called with PRIVATE profile token.

### `POST /api/v1/auth/sessions/downgrade-public`
- **Headers:** `Authorization: Bearer <private-access-token>`
- **Request Body:** None.
- **Responses:**
  - `200 OK`: Replacement `ApiResponse<AuthTokensResponse>` with `profile: "PUBLIC"`, incremented generation.
  - `409 CONFLICT`: Public credential not configured (`AUTH_PUBLIC_NOT_CONFIGURED`).
  - `403 FORBIDDEN`: Called with PUBLIC profile token.

## 3. JWT Claims Structure
- `iss`: Configured issuer (e.g. `personal-private-vault`).
- `sub`: `"1"` (singleton Owner ID).
- `profile`: `"PUBLIC"` or `"PRIVATE"`.
- `sid`: Random UUID representing `ppv_login_sessions.id`.
- `generation`: Monotonic `long` representing session authority generation.
- `iat`: Epoch seconds of issuance.
- `exp`: Epoch seconds of expiry (rolling 15m TTL).
- `jti`: Random UUID.

## 4. Persisted Admission Check (D3)
On every incoming HTTP dispatch:
1. Parse and verify JWT signature and claims (`sub = "1"`, valid profile, `sid`, `generation`).
2. Execute single-SQL fresh snapshot query joining `ppv_login_sessions` and `ppv_public_login_credential`:
   - Checks `active_profile` equals claim profile.
   - Checks `generation` equals claim generation.
   - Checks `expires_at > now()`.
   - Checks `revoked_at IS NULL`.
   - If session was ever PUBLIC (`public_credential_epoch IS NOT NULL`), checks `public_credential_epoch == ppv_public_login_credential.credential_epoch`.
3. If check fails, request short-circuits immediately with 401 `AUTHENTICATION_REQUIRED`.
