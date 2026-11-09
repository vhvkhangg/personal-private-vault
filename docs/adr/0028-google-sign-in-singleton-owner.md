# ADR-0028: Google Sign-In and Singleton Owner Identity Linking

- **Status:** Accepted (2026-11-08)
- **Deciders:** Owner, Codex, Antigravity
- **Date:** 2026-11-08
- **Context:** [H1-AUTH Executive Handoff](../implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/executable-handoff-h1-auth-2026-11-08.md)

## Context

Personal Private Vault (PPV) is a strictly single-user modular monolith governed by ADR-0006 (`app_users.id = 1`) and ADR-0008 (HS256 access JWTs, opaque rotated refresh tokens, and private PIN). The Owner desires an optional Google Sign-In mechanism for web browser sessions without:
1. Weakening single-user invariants or creating multi-user registration paths.
2. Allowing third-party Google ID tokens to directly authorize PPV business APIs.
3. Exposing raw OAuth credentials or codes to untrusted browser JavaScript.
4. Compromising the legacy emergency password, refresh token, or PIN access channels.

## Decision

We implement a Spring-owned Google OIDC authorization code exchange flow with PKCE (S256) and singleton Owner identity linking:

1. **Singleton Identity Binding:**
   - Relation `public.ppv_owner_google_identities` binds `owner_id SMALLINT PRIMARY KEY REFERENCES app_users(id)` with check constraint `owner_id = 1`.
   - Immutable mapping: stores `issuer`, `google_sub`, and `linked_at`. No updates, unlinking, rebinds, or multi-user accounts are permitted.
   - Account linking (`POST /api/v1/auth/google/link/start`) requires both an authenticated Owner bearer token and fresh password verification checked synchronously under transactional lock.

2. **Durable Attempt State & Transactional Crypto:**
   - Relation `public.ppv_google_login_attempts` records flow state with fixed 300s TTL.
   - The CSPRNG PKCE code verifier is encrypted at rest using AES-256-GCM with a dedicated 32-byte key (`ppv.security.google-signin.txn-key-file`).
   - Active rows store exactly a 12-byte nonce and 59-byte ciphertext (43-byte verifier + 16-byte GCM tag). Versioned AAD binds `attempt_id`, `purpose`, `owner_id`, `handoff_challenge_hash`, and `client_id`. Terminal rows clear PKCE bytes immediately.

3. **Ephemeral Handoff Tickets:**
   - Relation `public.ppv_google_handoff_tickets` holds one-time tickets with fixed 60s TTL.
   - Redemptions (`POST /api/v1/auth/google/handoff`) require matching proof-of-possession `challengeVerifier` validated in constant time.
   - Redemption atomically consumes the ticket and delegates to `SessionService.issueForVerifiedOwner` within a single database transaction, ensuring atomic rollback on failure.

4. **Default Containment & Zero Regressions:**
   - Feature is disabled by default (`ppv.security.google-signin.enabled=false`). When disabled, no Google endpoints, clients, crypto, or public security matchers are activated.
   - Standard HS256 JWT, refresh token rotation, private PIN, and legacy login mechanisms remain completely unchanged.
   - All three relations are excluded from Portability JSONL snapshots. Full PostgreSQL backups retain encrypted security tables.

## Consequences

- **Positive:** Secure, browser-friendly Google Sign-In for the singleton Owner with strict PKCE, AAD binding, and no secret leakage.
- **Positive:** Unlinked logins fail safely without creating accounts; linking is password-gated.
- **Negative:** Requires external network connectivity to Google's token and JWKS endpoints during authentication.
- **Mitigation:** Emergency access via existing local password, refresh token, and PIN remains fully operational even if Google is offline or unlinked.
