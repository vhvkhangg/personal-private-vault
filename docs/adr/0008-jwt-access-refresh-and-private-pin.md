# ADR-0008 — Use Access/Refresh JWTs and a Separate Private-Mode PIN

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The owner wants username/email + password login with JWT and a quick private-mode lock for an already authenticated device.

## Decision

Use short-lived JWT access tokens plus refresh-token rotation/revocation. Store refresh-token server representations securely. Store password and the exactly 6-digit private-mode PIN only as password hashes. The PIN is an application/UI privacy gate and auto-locks after 5 minutes of inactivity by default.

## Rationale

Access/refresh separation supports revocation/rotation without requiring one long-lived bearer token. Treating the PIN separately avoids misrepresenting it as database encryption.

## Consequences

- No registration endpoint.
- Token lifetimes/browser transport are finalized during implementation.
- 2FA/passkeys and Internet hardening are deferred.

## Alternatives considered

- **Single long-lived JWT:** rejected because revocation/rotation is weaker.
- **PIN as encryption key:** rejected because the requirement is UI privacy gating, not encrypted-at-rest personal fields.
