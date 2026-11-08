---
name: frontend-bff-security
description: Plan or review PPV frontend BFF sessions, private routes, OAuth cookie binding and Drive upload/download security, including pre-app architecture. Preserves Spring authority; grants no backend auth, provider activation or H1 implementation authority.
---

# PPV BFF security

Read [ACTIVE](../../../docs/implementation/handoffs/ACTIVE.md) and the [frontend workflow](../../../docs/workflow/frontend-development.md). H0 encodes requirements only. For future H1 reuse [Authentication security](../authentication-security/SKILL.md), [S1 authority](../../../docs/architecture/google-drive-connection-authority.md), [S1 operations](../../../docs/architecture/google-drive-connection-operations.md), [S2 authority](../../../docs/architecture/google-drive-files.md) and [S2 operations](../../../docs/architecture/google-drive-file-operations.md). Actual backend contracts win over illustrative external guides; stop for any required backend/authority change.

## PPV session boundary — H1 must resolve and prove

- Spring remains authentication, rotation/revocation and business authority. The BFF adapts private transport/session. Authenticate/authorize each sensitive Route Handler, Server Action and server read; direct calls cannot bypass this via layout/middleware/client checks. No unauthenticated sensitive BFF route or browser-only authorization.
- PPV access/refresh JWTs, Google tokens, provider Authorization headers and key material stay server-only. Never place them in browser JS/state/storage, serialized props/RSC/HTML, URLs, `NEXT_PUBLIC_` config, exception strings, ordinary logs or test artifacts. Use an opaque protected browser session rather than sending raw backend tokens to the browser.
- H1 preparation selects the smallest justified server session persistence/library, cookie scope and CSRF mechanism. Require HttpOnly, Secure under HTTPS, deliberate SameSite/Path/lifetime and expiry, fixation prevention on login/rotation, fail-closed invalid/replayed/expired sessions, and backend-compatible serialized refresh concurrency. Do not invent a session library/store in H0 or weaken backend rotation for concurrent tabs.
- Prove CSRF defense at every browser mutation (login/logout/authorize/disconnect/ensure/upload and any selected Server Action): origin/host and token protections as designed, safe-method semantics and credentialed CORS restrictions. SameSite alone is not a complete CSRF design. No wildcard credentialed proxy, arbitrary upstream URL or forward-all header/cookie behavior.
- Logout must invalidate the server session and appropriate backend refresh authority, clear the exact browser cookie scope, and handle failure/expiry truthfully; no cosmetic client redirect that leaves credentials usable. Reject refresh races/replay and clear private projections after logout/expiry. Preserve backend error authority and sanitize diagnostics.

## S1 browser binding — unchanged backend routes

Two slots use `GET /api/v1/drive/connections`, `POST /api/v1/drive/connections/{slot}/authorize`, `DELETE /api/v1/drive/connections/{slot}` and exact backend `GET /api/v1/drive/oauth/callback`.

`DriveController.authorizeDriveConnection` returns an authorization URL and host-only **HttpOnly, SameSite=Lax, Max-Age=300, Path `/api/v1/drive/oauth/callback`** binding cookie. Secure follows accepted backend configuration; the approved localhost HTTP exception grants no production exception. A server-only authorize request that loses this Set-Cookie cannot complete the browser-bound flow.

H1 must prove safe cookie forwarding or verified same-host routing: the same browser jar/host sends the correct binding at the callback path. Different localhost ports do not waive cookie scope. Missing/wrong/expired/replaced binding fails; state selects slot, not cookie order or display email. `completeDriveConnection` returns fixed **text/plain**, no invented redirect/JSON wrapper. No new callback, OAuth state, public matcher, backend patch or remote HTTPS consent is authorized here.

## S2 file authority and recovery — guidance, not new business logic

Under `/api/v1/drive/connections/{slot}`, reuse `DriveFileController.ensure/list/upload/download`:

| Operation | Exact route suffix | Boundary |
| --- | --- | --- |
| Ensure root | POST `/root-folder` | One app-owned root, actual pending/reconciliation/error semantics |
| List | GET `/files` | Direct-child regular PPV-created binary files, opaque cursor, limit 1–50 |
| Upload | POST `/files` | One intentional action, stable `Idempotency-Key`, exact payload identity |
| Download | GET `/files/{fileId}/download` | Authorized bounded binary attachment with expected length |

Preserve slot/verified Google sub/credential epoch/root/file provenance; display email is not identity. Invalidate old pages and in-flight responses when switching slots. UI placement under Personal creates no backend `personal → drive` edge; Media S3 remains separate. No nested/whole-Drive search, Picker, Workspace export or file mutations.

- `DriveFileInputs.keyTime` requires **`yyyyMMddTHHmmssZ.lowercase-UUIDv4`**; illustrative syntax only: `20261107T100000Z.12345678-1234-4abc-8def-123456789abc`. Generate fresh server-UTC-compatible action values; do not reuse this example. `freshKey` admits an absent action in **[-600s,+120s]**; recorded actions can recover later. Missing/invalid = `UPLOAD_KEY_REQUIRED`/`UPLOAD_KEY_INVALID` (400); stale first action = `UPLOAD_KEY_EXPIRED` (409).
- Create one key before first dispatch for each intentional upload. Lost reply or `UPLOAD_OUTCOME_UNKNOWN`: retain **same key and identical bytes/name/MIME** for explicit same-action recovery. No automatic mutation retry/new key/new provider ID on uncertainty. Same key/different fingerprint = `UPLOAD_KEY_CONFLICT`; active claimant = `UPLOAD_IN_PROGRESS`. Only verified new/reconciled completion is 201/200. Reuse backend normalization/validation, never duplicate Google authority or reset the action ledger.
- Exact cap: file **52,428,800 bytes (50 MiB)**; multipart request **57,671,680 bytes (55 MiB)**. Preserve zero-byte binary support and safe filename/MIME validation; reject Workspace-native files. No browser/React or Next whole-file JS memory buffering, base64/JSON conversion, cached binary or unsupported resumable-UX promise.
- Use bounded streaming/pass-through, backpressure, cancellation, timeout and cleanup for browser/BFF/backend hops. Preserve no-store/nosniff, safe attachment/type and expected download length. Short/excess/aborted/timed-out bodies are failures: no JSON appended after commitment, failed body converted into normal completion, false download-complete UI, provider URL/token leak or Range-support promise.
- Disabled/disconnected/reconnect/root-pending/reconciliation and quota/error states use actual API codes/status. Feature OFF must not trigger Google calls or fabricated connected state. Keep Drive/Drive Files/Feed false/unset; actual Google/Cloud/browser-consent/HTTPS/deployment and RAG activation are separate gates.

Require the [testing skill's H1 negative evidence](../frontend-testing/SKILL.md); an instruction walkthrough proves guidance coverage only. Audited external references: [provenance](../../../docs/implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/frontend-guidance-provenance-h0-2026-11-07.md).
