# Google Drive connection authority

**Status:** GDI26-S1 implementation under exceptional Codex takeover; separate independent review required. Default OFF. This document describes the submitted behavior, not live acceptance. [ADR-0026](../adr/0026-google-drive-connection-authority.md), [contract and authorization ledger](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/executable-handoff-s1-2026-11-06.md).

## Ownership and HTTP boundary

Drive owns [drive_connections](../database/google-drive-connections.dbml), crypto, ephemeral attempts and project/subject lifecycle. It has no business-module dependencies. Media S3, Account, Feed and RAG retain their contracts.

| Operation | Security / response |
| --- | --- |
| POST /api/v1/drive/connections/{slot}/authorize | Existing Owner bearer JWT; ApiResponse with slot/authorizationUrl/expiresAt and slot proof cookie |
| GET /api/v1/drive/oauth/callback | Only exact enabled GET is public; fixed text/plain success/failure, no-store and no-referrer |
| GET /api/v1/drive/connections | Owner JWT; two ordered slots with status/displayEmail/transient remoteRevocation only |
| DELETE /api/v1/drive/connections/{slot} | Owner JWT; local DISCONNECTED plus bounded best-effort remote result |

OFF has 235 existing operations, absent Drive handlers and no key/provider work; authenticated Drive paths are 404 and anonymous paths including callback remain 401. ON adds four operations (239). Callback rejects duplicate security parameters and code-plus-error before dispatch. Stored state selects the correct slot proof even when both cookies are present; terminal cleanup cannot clear an unknown/replayed/replaced attempt's newer cookie.

## Authority, custody and finalization

State stores Owner ID, fixed redirect, slot/attempt generation and monotonic initiation order, expiry, proof digest, nonce and verifier. One current attempt per slot; four pending plus processing maximum. New starts/disconnect cancel old authority; processing still occupies capacity until finish, even after TTL. Finalization rechecks current attempt, exact context, expiry, captured row epoch and subject/fence under one guard through a short transaction. Empty slots use insert-only conflict handling, never assigned-ID merge.

A fresh random UUID epoch changes with credential mutation. CONNECTED requires one usable validation refresh; RECONNECT_REQUIRED retains verified metadata without token/key. Both use the same final gate and subject reservation. ID-token audience is mandatory; matching azp cannot replace it. Refresh access-token/type/expiry and any returned identity/scopes are validated before rotation. Only selected scopes and Google's equivalent email alias are accepted.

AES-256-GCM envelope is 0x01 || random 12-byte IV || ciphertext || 16-byte tag. Separate key_id is v1. AAD is UTF-8 slot:sub:UUID (fixed one-digit slot and 36-character UUID delimit the variable subject). Key custody is external. Status/export/diagnostic paths do not expose secrets; authenticated initiation deliberately returns the consent URL.

## Project-grant revocation and bounds

Disconnect reserves a subject/project fence and deletes the captured epoch before one remote POST. Persistence failure restores the fence and sends no revoke. No guard/SQL transaction spans provider work, allowing unrelated V to proceed while U is paused.

Revoke completion records a monotonic initiation-order cutoff covering the whole interval, including equal clock timestamps. Pre/during starts stay stale after 200 and fresh reconnect. Successful cutoff records retire only when old pending/processing work has drained or expired; uncertain records never evict/expire automatically. Two lifecycle records cover durable connections/recovery rows and retained fences.

One callback budget is 30s: at most one code exchange, one refresh and two JWKS fetches. Each complete response, including a trickling body, is capped at 5s or remaining budget, with cancellation and byte caps (64 KiB token/revoke, 256 KiB keys); connect is 2s. Revoke total is at most 8s, one request, no automatic retry. HTTP 200 reports REQUEST_ACCEPTED; FAILED/UNCERTAIN retains quarantine.

Single-JVM/restart limits, key recovery and separately gated real-provider operation are detailed in the [operations guide](google-drive-connection-operations.md). [Handback](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/implementation-handback-s1-2026-11-06.md) maps implementer receipts and review limits; independent findings remain OPEN until separately adjudicated.
