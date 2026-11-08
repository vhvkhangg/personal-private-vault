# Google Drive direct binary files

**Status:** GDI26-S2 READY_FOR_OWNER_COMMIT under the [independent remaining-verification review](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md). Drive/files default OFF; Owner publication and live activation are separate. [ADR-0027](../adr/0027-google-drive-root-file-authority.md), [executable contract](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/executable-handoff-s2-2026-11-06.md).

## Ownership and HTTP

The existing Drive leaf owns files/application, http, persistence, web and config packages; no business-module edge is added. S1 owns OAuth, credential encryption/current epoch and the shared project/subject fence. The two [V26 relations](../database/google-drive-files.dbml) contain integration metadata only and are excluded from Portability alongside S1 credentials.

The file filter remains in `drive.internal.files.web`; the new S2 controller/DTO use `drive.internal.web.controller` / `drive.internal.web.dto`, preserving the existing controller package rule (handoff SC-01). S1 adapters are unchanged.

| Private operation | Result |
| --- | --- |
| POST /api/v1/drive/connections/{slot}/root-folder | 200 ApiResponse {slot,status:READY} only after full proof; no body/caller parent |
| GET /api/v1/drive/connections/{slot}/files | One 1–50-entry provider page; safe five-field metadata, opaque nextCursor and truthful hasMore |
| POST /api/v1/drive/connections/{slot}/files | Exactly one multipart file and stable Idempotency-Key; 201 newly verified, 200 reconciled same action |
| GET /api/v1/drive/connections/{slot}/files/{fileId}/download | Original application/octet-stream attachment; Range unsupported; no JSON wrapping |
| GET /api/v1/drive/connections/{slot}/storage-quota | 200 ApiResponse {usageBytes,usageInDriveBytes,limitBytes,limitKnown,observedAt} |
| POST /api/v1/drive/connections/{slot}/folders | Exactly one JSON body {name} and Idempotency-Key; 201 newly verified first-level child, 200 reconciled |
| GET /api/v1/drive/connections/{slot}/items | Bounded subtree browse/search across root and child folders; 8-call budget; opaque 43-char cursor |
| POST /api/v1/drive/connections/{slot}/folders/{childId}/files | Multipart file upload under verified app-owned child folder |
| GET /api/v1/drive/connections/{slot}/files/{fileId}/thumbnail | Proxied verified image bytes; exact origin https://lh3.googleusercontent.com:443; 1 MiB limit; verified image signature and dimension bounds (<=2048x2048) with standard runtime decoder validation for PNG/JPEG (unsupported formats like WEBP fall back); 8s total transfer deadline & 5s idle checks; sanitized 404 fallback |

Existing Owner JWT is mandatory. Callback permit and S1 responses stay unchanged. Route inventories measure 235 OFF / 239 S1-only / 248 with files enabled (13 Drive operations total; 243 S2-only is historical). Errors are sanitized/no-store; binary after commitment aborts without JSON. Runtime evidence belongs in the implementation handback, not this design description.

Generated OpenAPI JSON success responses declare `application/json`: ensure/list use 200, upload uses 201 for a new file and 200 for a verified retry. Each schema composes the existing root `ApiResponse` envelope with its module-local `Root`, `Page` or `File` data projection; async task internals are excluded. The attachment retains its binary schema.

## Authority and durable state

Lease acquisition validates current S1 subject/epoch/scopes/fence and uses one refresh. Returned ID tokens follow a refresh-specific signed-subject path, preserving callback nonce rules. Token rotation changes credential epoch through the accepted repository CAS. Dispatch enqueues under the shared guard; waiting/streaming occurs outside it. Recheck current lease/root generation before each dispatch and successful state/response publication.

Root identity is per Google subject, not mutable slot. First ensure works with no READY row. About v2 supplies actual account root; create uses root alias but proof compares the returned opaque parent. One live claimant, durable pre-generated ID/UUID and PENDING prevent duplicate roots. Any retained root ID is reconciled by GET only; 404 does not authorize a replacement. READY reread does not rotate its epoch; state changes do. Disconnect retains root metadata but deletes credential authority.

Content requires fresh READY/root proof and exact file ID/root UUID/direct parent/private markers. Native editors, folders, shortcuts, moved/copied/external or malformed entries are denied/omitted. Listing performs one provider request and no per-item fetch loop. Cursor handles are 32 random bytes, max 128, 300s TTL, bound to Owner/slot/subject/credential/root generations/query/limit; no raw Google continuation token reaches the caller.

An upload action binds subject/root/key, normalized name/MIME/observed length, content SHA-256 fingerprint and MD5 to one provider ID. Global key uniqueness rejects cross-account/root reuse without disclosing another action. Root-row locking bounds admission/pruning; no lock spans Google. Only a recovered never-dispatched PREPARING/RESERVED claim may perform its first mutation. DISPATCHED/reconciliation/completed retries verify the exact original ID; absent/ambiguous remote results never allocate G after F.

## Bounded I/O

Two operation permits, two Spring MVC async task workers and bounded servlet read/write bridges cover admission through cleanup. MVC owns security-context propagation and servlet redispatch; the servlet filter chain stays on the container thread. Each response synchronously streams its body within that bounded task. Upload parsing is local to S2 and bypasses eager container multipart spooling; the same 52,428,800-byte file and 57,671,680-byte request limits apply. Copy/hash buffers are <=64KiB. Protected owned spool reserves two 50MiB slots and counts crash orphans; no automatic reupload/orphan deletion.

Each operation has a monotonic 180s budget. Metadata complete bodies cap at 10s, transfers at 120s and 15s idle; connect caps at 2s. About/generated IDs cap at 16KiB, metadata at 64KiB, list at 1MiB, headers at 16KiB. No redirects or automatic retries. Small multipart and large resumable transport both carry fixed root/ID/action markers; validated Location remains ephemeral. Every positive partial download read refreshes idle progress while accumulating a block of at most 64KiB; the total transfer/operation deadlines remain fixed. Download checks expected and actual length, exact EOF before its final chunk and current local authority throughout copying. Provider-side changes between requests and emitted bytes remain explicit limitations.

See the [operations guide](google-drive-file-operations.md) for retention, stable-key recovery and activation limits.
