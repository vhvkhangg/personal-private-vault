# ADR-0026: Google Drive connection authority and credential custody

- **Status:** Owner-approved D04 design; implementation submitted for separate independent review. No implementation acceptance or publication is claimed.
- **Date:** 2026-11-06.
- **Authority:** [Owner D00–D07](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/owner-decision-record-2026-11-05.md), [executable GDI26-S1 contract and takeover ledger](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/executable-handoff-s1-2026-11-06.md).

## Context

D00 selects Drive storage alongside Media S3 and an eventual Explorer. D01 selects two accounts and narrow drive.file plus OIDC. D02 separates connection authority (S1), app-owned folder/files (S2) and Explorer (S3); D03 keeps S1/S2 backend first. D04 approves the concrete OAuth/security/module/table amendments. D05 defers destructive/file-sharing/background/RAG work; D06 assigns folder creation to S2. D07 gates initial operation to the exact localhost callback.

These Owner decisions retain their original meanings. The technical choices below implement D04 and the executable handoff, rather than introducing replacement decision identifiers.

## Decision

Add the Drive-owned leaf module and [additive credential relation](../database/google-drive-connections.dbml), with no business-module edges. Preserve frozen Schema v1 and V1–V24. Portability classifies drive_connections as a security exclusion; its existing 67-table export allowlist and format stay unchanged.

Expose exactly authenticated authorize/status/disconnect and the enabled-only exact public GET callback. The plain browser response is the bounded ADR-0016 exception; existing PPV JWT login remains unchanged. Default OFF creates no active Drive controllers, crypto/key loader or OAuth client; ordinary Flyway migration/repository discovery still applies.

OAuth uses one-use state, stored authenticated Owner/redirect/slot context, PKCE S256, nonce, slot-specific host-only HttpOnly SameSite=Lax proof cookies, and a final attempt-generation/expiry/credential-epoch gate for both CONNECTED and RECONNECT_REQUIRED. New starts and disconnect invalidate older slot authority. Cancelled processing retains its bounded resource admission until completion. ID tokens require RS256 with trusted fixed-endpoint Google keys, Google issuer, required audience, authorized party where applicable, expiry, nonce and stable sub. Only selected effective scopes and the userinfo.email equivalent are allowed.

One external raw 32-byte AES key protects refresh tokens. Envelope: version byte 0x01, random 12-byte IV, ciphertext, 16-byte GCM tag; key_id is v1. AAD is UTF-8 decimal slot + ":" + verified sub + ":" + canonical UUID epoch. Slot is one digit and UUID is fixed length, making the outer boundaries unambiguous. Epochs are fresh random UUIDs tested for equality, not counters. Access tokens and browser/PKCE data remain ephemeral; diagnostic strings redact credentials.

One usable candidate-validation refresh precedes CONNECTED; omitted refresh/scopes preserve only the validated candidate/context. Stored invalid_grant downgrades only the captured matching epoch. Transient/provider-identity failures preserve a different valid generation.

A short in-process guard and PostgreSQL transactions linearize final writes and local disconnect. No guard/SQL transaction spans Google I/O. Reserve a project/subject revoke fence before local CAS deletion; send the captured old token once. A completion-order cutoff covers starts before/during the entire revoke, survives fresh reconnect while old attempts remain, and retires only after those attempts drain/expire. Recovery rows reserve lifecycle capacity too. Failed/uncertain revokes quarantine without automatic expiry/retry.

Provider calls have finite admission, 2s connect/5s complete-response bounds, 64 KiB token/revoke and 256 KiB JWKS caps, redirects disabled and no application mutation retry. One monotonic 30s callback budget covers one code exchange, one validation refresh and at most two JWKS fetches; revoke has an 8s total ceiling.

## Alternatives and consequences

Reuse existing JWT/envelopes, Nimbus verification, JDK HTTP, Flyway/PostgreSQL and watchdogs. A generic connector framework, second login stack, scheduler and durable/distributed coordinator add no S1 value and are excluded.

Attempts, fences and remote status are intentionally ephemeral in one JVM; restart is not proof that an old remote operation ceased. HTTP 200 means REQUEST_ACCEPTED, not completed propagation. Later invalid_grant remains possible. Real Google consent/browser/Cloud/HTTPS operation and activation are separately gated and NOT_VERIFIED. See [operator guide](../architecture/google-drive-connection-operations.md) and [implementation evidence](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/implementation-handback-s1-2026-11-06.md).
