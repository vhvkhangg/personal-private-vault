# ADR-0027: Google Drive root and binary-file authority

- **Date:** 2026-11-06.
- **Status:** Owner-approved S2 contract; READY_FOR_OWNER_COMMIT under the [independent remaining-verification review](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md). No live activation or Owner publication is asserted.
- **Authority:** [D00–D07](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/owner-decision-record-2026-11-05.md), [D08–D10](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/owner-decision-record-s2-2026-11-06.md), [executable S2 handoff and exceptional authorization](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/executable-handoff-s2-2026-11-06.md).

## Context and decision

S2 adds four private operations to accepted S1: ensure one PPV-created My Drive root per verified subject, list permitted direct binary children, upload original bytes and download an attachment. D08 excludes nested folders; D09 excludes Workspace conversion/export; D10 fixes the existing 50MB limit. Media S3 and the S1 callback/login/scope contract stay intact.

Use the existing S1 credential repository, AEAD and SubjectLifecycleManager guard. A Drive-private lease performs one bounded refresh, validates bearer/expiry/scopes/signed subject without inventing a new authorization nonce, and CAS-publishes any refresh rotation. invalid_grant/401 downgrade only the captured matching epoch. Provider dispatch admission and final local publication use the same S1 guard; no guard or SQL transaction spans provider/body I/O.

Add [two metadata relations](../database/google-drive-files.dbml) in V26: one durable subject/root identity and one bounded upload-action ledger. They outlive local disconnect but confer no credential authority and are both security exclusions from Portability. A nullable connection column would lose root identity on disconnect; memory-only upload keys would lose duplicate prevention on restart. No additional module/dependency/table/file catalog is justified.

First ensure admits ABSENT through the common S1 gate. Resolve actual My Drive parent with the selected v2 About/rootFolderId read; allocate one provider ID and root UUID, persist PENDING before create, then require all root markers/type/parent/ID and current epoch before READY. A retained pending/unknown identity permits GET-only reconciliation, never name adoption or automatic replacement.

Every file needs the current root UUID/ID, exact direct parent, nontrashed binary type, ppvKind=file, ppvCreatedId=id and a 64-hex upload-action marker. Upload completion/reconciliation additionally matches its stored action digest, normalized metadata, actual size and MD5. Private properties supplement subject/root proof; they are not cryptographic provider attestation.

Uploads require a timestamped UUIDv4 Idempotency-Key and server-observed fingerprint. PREPARING/RESERVED can recover a never-dispatched first attempt through CAS when no live claimant exists. DISPATCHED is durable before any mutation; all later retries, response loss and restart use the same ID with read-only GET proof. Unknown/404 never releases identity. Retain at most eight nonterminal and 1024 total rows per root; prune only terminal records older than 30 days, at most 32 per admission. An absent old key is expired rather than a fresh action.

Use streamed multipart/related at <=5,000,000 bytes; larger files use one validated ephemeral resumable initiation and one whole PUT. No session URL is persisted or exposed. All endpoints are fixed Google HTTPS; session validation checks exact origin/path/query before sending Authorization.

## Bounds and consequences

Default Drive, files and Feed flags remain OFF. Two process-wide S2 admissions precede body parsing and persist through cleanup. A module-local servlet bridge and single-part parser enforce the existing 50MB/55MB limits without changing shared MVC settings or allocating full-file arrays. Owned external spool is bounded to two files/100MiB including crash orphans; container multipart spooling is avoided on these four routes. Operation/transfer/idle/metadata limits are 180/120/15/10 seconds, with 2s connect, bounded response/header sizes and at most eight Drive calls plus one S1 refresh.

MVC owns async task dispatch/security propagation; a fixed pair of S2 task workers performs synchronous bounded copies while the filter retains admission until servlet completion. Preserve the established controller/DTO package placement. Before commitment, errors use existing ApiResponse. Download uses safe attachment headers and verified expected length; its final chunk is held until exact EOF/current authority checks so committed errors are observably incomplete, with no appended JSON. The approved localhost HTTP/1.1 attachment closes its connection at completion to terminate underlength errors promptly. Already emitted bytes and provider changes between metadata/media cannot be rolled back.

Single-process/restart limits, actual key/spool ACLs and real Google/browser/deployment readiness remain separate operational gates. No file deletion, nested management, UI/Picker, sharing, sync or RAG capability follows. See [architecture](../architecture/google-drive-files.md) and [operations](../architecture/google-drive-file-operations.md).
