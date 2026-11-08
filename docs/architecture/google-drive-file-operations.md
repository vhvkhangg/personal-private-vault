# Google Drive file operations

**Status:** S2 READY_FOR_OWNER_COMMIT under the [independent remaining-verification review](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md). Default OFF. No live Google/Cloud/browser/spool ACL/deployment operation has been certified.

## Configuration and supported flow

| Setting | Default / requirement |
| --- | --- |
| PPV_DRIVE_ENABLED | false, preserves S1 default |
| PPV_DRIVE_FILES_ENABLED | false; true requires Drive enabled |
| PPV_DRIVE_FILES_SPOOL_DIR | Empty; when files enabled, existing protected writable directory outside Git, no symlink ancestor |
| PPV_FEED_SCHEDULER_ENABLED | false, unchanged |

Existing [S1 client/key/browser setup](google-drive-connection-operations.md) remains a separately authorized operational gate. Provision owner-only key and spool ACLs, restricted backups, one JVM without overlapping restart, valid OAuth/Cloud configuration and actual localhost browser proof before separately approved activation. Code/tests do not provision or authorize any of these. There is no website Explorer or Picker in S2.

After connecting a verified account, explicit ensure creates/revalidates its single PPV-owned My Drive folder. List/upload/download work only with READY and fresh root/file provenance. Files must be direct regular binary children created by PPV, not manually placed/imported files or Google-native editors. Disconnect never deletes remote content; root/action metadata remains locally retained and excluded from Portability, while S1 credentials are removed. A full database backup can still contain sensitive integration metadata.

## Upload actions and recovery

Supply a new canonical yyyyMMddTHHmmssZ.lowercase-UUIDv4 Idempotency-Key for each intentional upload. Reuse that exact key plus identical original bytes/name/MIME for every retry, including a lost HTTP reply. An absent key must be within server UTC [-600s,+120s]; a recorded action can be retried after that window. Name is normalized NFC/trimmed, with path/control/CRLF names rejected; invalid MIME syntax defaults to application/octet-stream, and native Workspace MIME is denied. Zero-byte regular files are supported.

Same name with a different fresh key is a new intentional file. The same key with a different fingerprint returns UPLOAD_KEY_CONFLICT. PREPARING/RESERVED without a live claimant can recover its first dispatch; a live loser gets UPLOAD_IN_PROGRESS. Once DISPATCHED, every retry is exact-ID GET reconciliation: 200 only after matching root/ID/action/size/checksum proof, otherwise UPLOAD_OUTCOME_UNKNOWN. No new file ID, session or PUT is sent after dispatch. A provider 404 does not prove noncreation; keep the original key/body for a later explicit retry/investigation.

Do not delete/reset ledger rows to force retries. This can create a duplicate remote file. Retain uncertain/nonterminal records indefinitely; capacity rejects new actions (8 nonterminal/1024 total per root) while preserving existing-key recovery. Only terminal records older than 30 days are eligible for bounded prune; a missing old key then expires rather than becoming fresh. A permanently unavailable root requires separately reviewed replacement, not automatic recreation/name adoption. No background reconciler or session-resume UX exists.

## Bytes, cancellation and cleanup

File maximum is existing Spring 50MB = 52,428,800 bytes; request maximum is 55MB = 57,671,680 bytes. S2 admits at most two operations before multipart parsing, bypasses eager servlet multipart spooling locally and reserves at most two owned 50MiB spool files. Count crash-orphan owned files against quota; unsafe/full spool fails closed. Do not clean historical logs or unrelated paths. Operator cleanup requires a stopped/quiesced deployment, verifying exact owned spool names/paths and that no upload remains active; deleting spool data never authorizes another remote mutation.

Buffers are <=64KiB; metadata/body/idle/operation caps are 10/120/15/180 seconds, connect 2s. Request/provider streams, timer tasks, spool and admission are cleaned on supported success/failure/cancellation paths; any retained deletion-failure orphan consumes later quota. Real host ACLs/process topology remain NOT_VERIFIED.

Small files use streamed multipart/related; >5,000,000 bytes use one ephemeral resumable initiation plus one whole PUT. This is transport selection, not persisted resume. Provider Location must match the exact trusted HTTPS origin/path/query. No broad scopes, redirects, exports or automatic retries are allowed.

Downloads return no-store/nosniff attachments with safe UTF-8 filename and verified expected length. The approved localhost HTTP/1.1 response uses Connection:close so a truncated transfer ends promptly. Before commitment errors are ApiResponse JSON. Short/excess/deadline/disconnect failures after commitment truncate the expected transfer; JSON is never appended. Already emitted bytes cannot be undone, and metadata/media calls are not an atomic Google snapshot. Range is unsupported. Actual Google quota, permissions, propagation and end-to-end deployment remain separate unknowns.

## H1-B Explorer operations (Child Folders, Search, Quota, Thumbnails)

- **Child Folders:** Created via `POST /api/v1/drive/connections/{slot}/folders` with JSON `{"name":"..."}` and `Idempotency-Key`. Capped at 256 retained and 8 nonterminal rows per account root. Replays return 200 with the same ID; lost responses return 409 `FOLDER_OUTCOME_UNKNOWN` without re-creating. App-owned first level only (`ppvKind=child`).
- **Files under Child Folders:** Uploaded via `POST /api/v1/drive/connections/{slot}/folders/{childId}/files`. Verifies fresh remote child proof before upload, then validates returned file parent affinity and local current-authority guards upon publication (noting the non-atomic provider boundary). Downloads (`GET /files/{fileId}/download`) verify either root or child parent affinity automatically without caller specifying parent.
- **Subtree Search & Pagination:** `GET /items?scope=all|parent&parentId=...&limit=...&cursor=...&name=...&type=...&from=...&to=...&timeZone=...`. Hard budget of at most 8 provider HTTP dispatches per operation; provider page size `min(limit, 5)`. Cursors are opaque 43-character base64 strings bound to exact query parameters; atomically consumed on first use. Incomplete searches fail honestly with 502 `DRIVE_SEARCH_INCOMPLETE`.
- **Storage Quota:** `GET /storage-quota` returns projected account storage quota from Google Drive API v3 `/about`. Does not fabricate values on failure.
- **Thumbnail Proxy:** `GET /files/{fileId}/thumbnail` proxies small images (<= 1 MiB) strictly from the approved exact origin `https://lh3.googleusercontent.com:443`, sending Authorization only after destination origin validation, enforcing verified complete image signatures/bounds (`verifyImageSignature`, dimension limits <= 2048x2048, and standard runtime decodability for PNG/JPEG; unsupported formats like WEBP fall back), an 8s total transfer deadline, and a 5s body idle watchdog. Returns sanitized 404 `THUMBNAIL_UNAVAILABLE` on fallback.
