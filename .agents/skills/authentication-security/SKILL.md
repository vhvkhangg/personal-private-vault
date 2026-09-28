---
name: authentication-security
description: Design and review the single-user authentication, JWT access-token, refresh-token rotation/revocation, private PIN, bootstrap, and secret-handling implementation safely.
---

# Authentication Security

Use for Phase 2 authentication/security implementation and review.

## Credentials

- Use Spring Security password-hashing abstractions; never write custom password/PIN crypto.
- Persist only password/PIN hashes.
- Validate the private-mode PIN as exactly six decimal digits before hashing.
- Never log raw credentials or hashes.

## Single-user bootstrap

- No public registration endpoint.
- Bootstrap must create only the singleton user allowed by Schema v1 and behave safely under repeated/concurrent
  attempts.
- Bootstrap credentials/secrets come from runtime input/configuration, never migration constants or repository files.

## Access tokens

- Access JWTs are short-lived and contain only necessary claims.
- Signing material is external runtime secret/configuration.
- Never log bearer tokens or authorization headers.

## Refresh tokens

- Store only a secure server-side hash/non-replayable representation of the presented token.
- Enforce expiration, revocation, rotation, and `replaced_by_token_id` semantics transactionally.
- Rotation must fail closed on invalid/revoked/expired/replayed material.
- Do not weaken rotation/revocation behavior to simplify tests.

## Private mode

- Treat the PIN as an application/UI privacy gate for an authenticated session, not encrypted-at-rest protection.
- Default auto-lock remains 5 minutes unless settings explicitly changes it within schema constraints.

## Logging and tests

- Log only meaningful security lifecycle events with minimal non-sensitive metadata.
- Include negative/replay/rotation/bootstrap-race tests and HTTP authorization tests where applicable.
