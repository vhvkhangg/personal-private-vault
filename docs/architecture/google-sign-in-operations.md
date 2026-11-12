# Google Sign-In and Singleton Owner Operations

- **Authority:** GDI26-S3-H1-AUTH
- **Status:** Operational Guide
- **Related ADR:** [ADR-0028](../adr/0028-google-sign-in-singleton-owner.md)
- **Database Schema:** [google-signin-authentication.dbml](../database/google-signin-authentication.dbml)

## Overview

Personal Private Vault (PPV) provides optional Google Sign-In for the singleton Owner (`app_users.id = 1`). This document describes operational administration, configuration, key custody, rate limiting, and disaster recovery.

## Configuration & Activation

Google Sign-In is disabled by default (`ppv.security.google-signin.enabled=false`). To enable it, provide the following properties or environment variables:

| Property | Environment Variable | Default | Description |
| --- | --- | --- | --- |
| `ppv.security.google-signin.enabled` | `PPV_GOOGLE_SIGNIN_ENABLED` | `false` | Master feature toggle |
| `ppv.security.google-signin.client-id` | `PPV_GOOGLE_SIGNIN_CLIENT_ID` | `""` | Google OAuth2 Client ID |
| `ppv.security.google-signin.client-secret` | `PPV_GOOGLE_SIGNIN_CLIENT_SECRET` | `""` | Google OAuth2 Client Secret |
| `ppv.security.google-signin.txn-key-file` | `PPV_GOOGLE_SIGNIN_TXN_KEY_FILE` | `""` | Path to 32-byte AES-256 key file |

### Generating the Transaction Key File

Before enabling the feature, generate a 32-byte cryptographically secure random key file:

```powershell
# Windows PowerShell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[System.IO.File]::WriteAllBytes("C:\path\to\vault\keys\google-txn.key", $bytes)
```

Ensure strict OS-level file permissions (read-only for the application service user).

## Lifecycle and Security Controls

### 1. Identity Linking

The singleton Owner must first link their Google account by presenting both an authenticated Owner bearer token and fresh master vault password:
- Endpoint: `POST /api/v1/auth/google/link/start`
- The password is authenticated synchronously under a pessimistic row lock on `app_users(id=1)`.
- Failed password attempts are committed as `LINK_PASSWORD_FAILURE` rate records.
- 5 failed attempts within 15 minutes trigger a hard rate-limit lockout.
- Once linked in `ppv_owner_google_identities`, the binding is immutable. Unlinking or re-binding to a different Google subject is forbidden.

### 2. OIDC Code Exchange & PKCE

- All flows generate a 43-character base64url CSPRNG code verifier with S256 PKCE.
- The verifier is encrypted using AES-256-GCM (12-byte nonce, 59-byte ciphertext) with AAD binding to the attempt ID, purpose, owner ID, challenge hash, and client ID.
- Attempts expire in 300 seconds. Upon terminal status (completion or failure), PKCE nonces and ciphertexts are zeroed in the database.

### 3. Ephemeral Handoff Tickets

- Upon successful code exchange, a 43-character base64url ticket is generated with a strict 60-second TTL.
- The ticket is stored as a SHA-256 hash in `ppv_google_handoff_tickets`.
- Redemption requires the exact client-held `handoffVerifier` validated in constant time.
- Redemption atomically consumes the ticket and delegates to `SessionService.issueForVerifiedOwner` within a single database transaction.

## Emergency Access & Disaster Recovery

If Google services are unreachable, Google credentials are lost, or authentication fails:
1. **Local Master Password:** The Owner can authenticate directly via `POST /api/v1/auth/login` using the master vault password.
2. **Refresh Token:** Existing desktop/BFF sessions holding valid PPV refresh tokens can refresh access JWTs via `POST /api/v1/auth/refresh`.
3. **Private PIN:** Private-mode operations remain accessible via `POST /api/v1/auth/private-pin/verify` (which requires an existing authenticated PPV bearer token). Private PIN does not substitute for master authentication.
4. **Lost Password & Google Account Limitation:** If both the master vault password and access to the linked Google account are lost, the vault is **not recoverable by H1**. An offline database backup does not itself restore lost credentials or provide a valid Google identity; restoration still requires the master vault password. No automated email reset, backdoor, PIN-only rebind, or secondary recovery service exists.
5. **No Unsafe Edits:** Never attempt manual database modification or direct SQL edits of `ppv_owner_google_identities`. The application design intentionally omits unlinking/rebind mechanisms to preserve strict singleton ownership.
6. **Production Activation Gates:** Enabling `ppv.security.google-signin.enabled=true` is strictly Owner-gated and requires explicit Owner authorization beyond possession of credentials. Production Google Cloud OAuth2 Client ID/Secret provisioning and transaction key file creation under strict filesystem ACLs are mandatory prerequisites. Testing credentials or synthetic test fixtures must never be configured in production.
7. **Dual Login Profiles Preparation (D4 Adaptation):** Under BF26-DP-A, Google Sign-In defaults remain OFF. When enabled in local testing environments:
   - `POST /api/v1/auth/google/start` returns 503 `GOOGLE_SIGNIN_UNAVAILABLE` before attempt creation.
   - Pre-cutover LOGIN callbacks redirect 303 to BFF completion with no handoff ticket and cookies cleared.
   - LINK start (`POST /api/v1/auth/google/link/start`) requires an authenticated `PROFILE_PRIVATE` bearer token + master password proof. Valid LINK callbacks preserve immutable Owner binding and issue a one-time handoff ticket.
   - Handoff ticket redemption (`POST /api/v1/auth/google/handoff`) rejects fail-closed with 503 before ticket consumption or token issuance.
