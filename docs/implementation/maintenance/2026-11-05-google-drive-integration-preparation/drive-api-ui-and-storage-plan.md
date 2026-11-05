# GDI26 — Drive API, database, folder and future Explorer plan

**Owner decisions D00–D07 resolved; this is a proposed contract, not code.** `ACTIVE=NO_ACTIVE_HANDOFF`. Existing S3 Media remains separate; this Drive store is app-managed and does not implement MediaStoragePort.

## 1. Why selected file scope works and where it stops

Google's `drive.file` authorizes files the app creates or the user explicitly opens/shares with it, not arbitrary Drive-wide contents. PPV **itself creates** one Google Drive folder for each connected account in S2, persists the returned file ID and creates children using `parents=[folderId]`. It separately validates account, root folder and current parentage when listing/downloading/updating; **OAuth alone does not enforce folder-only access**. The user moving an app-authorized file outside that folder must cause subsequent PPV file operations to refuse it. Existing external files manually added through Google Drive are **not guaranteed accessible**; no claim of full-folder watch/import. See [Google scopes](https://developers.google.com/workspace/drive/api/guides/api-specific-auth) and [create folders/files](https://developers.google.com/workspace/drive/api/guides/folder).

Do not request restricted `drive`/`drive.readonly`/`drive.metadata.readonly` to compensate silently. `appdata` is not a visible user folder. No Google Picker is needed for PPV-created content; selecting existing arbitrary files with Picker is separately gated by future UI/scope approval.

## 2. S1 — precise minimal HTTP boundary

| Method / route | Auth | Semantics |
| --- | --- | --- |
| `POST /api/v1/drive/connections/{slot}/authorize` | PPV JWT (1 or 2 only) | Create one-use 5-minute state+PKCE+browser cookie; return consent URL (no tokens). Reconnect reuses this route. Reject new starts for a slot with local revoke in flight; pre-revoke attempts may never later commit for the same subject. |
| `GET /api/v1/drive/oauth/callback` | Public route **only with valid pending state+cookie** | Consume attempt; exchange code; verify `sub` and scopes; reject if subject/project has an outstanding or uncertain old revoke **even when callback targets the other slot**; require a fresh post-revoke attempt. Never claim `CONNECTED` or revoke the rejected fresh token; fixed sanitized browser result. |
| `GET /api/v1/drive/connections` | PPV JWT | Return at most two slot statuses and permitted display email; distinguish local disconnection from transient `PENDING`/`UNCERTAIN` remote revoke status while known in this process; after restart never fabricate remote-success evidence. Never ciphertext/refresh token. |
| `DELETE /api/v1/drive/connections/{slot}` | PPV JWT | Register subject/project revoke fence **before** local delete + exactly one bounded best-effort remote call. Local disconnect succeeds independently; return `REQUEST_ACCEPTED`/`FAILED`/`UNCERTAIN` without claiming fully propagated revocation. Never delete files or retry old revoke after new grant. |

All new owner-local web adapters use root `ApiResponse`, validation and OpenAPI conventions, except the navigation callback's deliberately minimal browser response. Do not introduce a whole-Drive GET, file metadata GET, picker token relay or upload endpoint in S1. The existing 235-operation API inventory is historical; the reviewed S1 adds exactly the operations above if implemented.

## 3. S1 — proposed minimal Flyway table and module ownership

**Add a new Drive-owned `drive_connections` relation only, behind a new, separately reviewed migration** (do not change frozen Schema v1 or existing tables). Concrete proposal, for independent validation:

| Column | Type / constraint | Purpose |
| --- | --- | --- |
| `slot` | `SMALLINT PRIMARY KEY CHECK (slot IN (1,2))` | Exactly two connection slots, selected by authenticated PPV Owner. |
| `google_sub` | `VARCHAR(255) NOT NULL UNIQUE` | Stable, verified Google identity; cannot connect same subject to both slots. |
| `display_email` | `VARCHAR(320)` nullable | Display-only, never credential or identity authority. |
| `granted_scopes` | `TEXT NOT NULL` | Canonical normalized granted scope set, never an implicit broad grant. |
| `status` | `VARCHAR(32) NOT NULL CHECK (status IN ('CONNECTED','RECONNECT_REQUIRED'))` | Only durable states; disconnected row is **deleted locally**. |
| `encrypted_refresh_token` | `BYTEA` nullable with conditional CHECK | AEAD payload; `NOT NULL` when CONNECTED, `NULL` when RECONNECT_REQUIRED. |
| `key_id` | `VARCHAR(64)` nullable | Encryption key identifier, matching token presence; not a secret. |
| `credential_epoch` | `UUID NOT NULL UNIQUE` | Per-connection randomized generation for stale-write rejection. |
| `created_at`, `updated_at` | `TIMESTAMPTZ NOT NULL` | Minimal operation chronology without secret payload. |

**No `drive_files` table in S1**, no pending OAuth SQL table, no encrypted token columns in Account, Authentication or Settings. Ephemeral pending state is bounded in memory and fails closed on process restart. If final reviewer identifies a necessity for durable pending state or a different constraint, make a finite review finding, not an undocumented schema extension. S2 may add `root_folder_id` as an approved additive column or separate Drive-owned record **only** under its own handoff; no guessed migration now.

**No SQL transaction across external Google HTTP.** Atomic slot/subject uniqueness and token updates use row-level CAS by credential epoch; disconnect reserves a *subject/project* in-process fence, removes credential locally, then performs one bounded external revoke. A fresh grant from **either slot** cannot commit while the prior same-subject revoke is outstanding/uncertain; any authorization attempt begun before/during it is stale even if the callback finishes later. Subject/project fencing supplements, not replaces, SQL epoch CAS. Remote HTTP 200 is request acceptance, not guaranteed complete grant invalidation. On timeout/error leave an in-memory fail-closed quarantine and require Owner-controlled recovery; no automatic retry, no new table/status/endpoint or cross-HTTP SQL lock. See [PH-03 lifecycle and restart limits](oauth-security-and-data-governance.md#31-ph-03--same-subjectproject-revoke--reconnect-ordering-s1-mandatory). SQL errors return sanitized API codes, not unique-subject values. No DB writes in disabled mode. S1 addition requires a bounded Spring Modulith descriptor/package tree and module matrix update; no other module edges.

## 4. S2 and S3 — separately approved next slices

**S2:** PPV creates or reconciles its own folder per account, stores opaque folder ID (not name match), checks actual Drive parent chain/account before app-owned file listing, bounded upload/download and paginated metadata. Upload needs explicit maximum size/streaming/retry/ambiguous completion acceptance, and Google-native document export must not be incorrectly treated as binary download. No deletion, rename, move, ACL/share changes, cross-account copy or automated sync. Google API limits and account storage quotas are checked at runtime; capacity is not proven by prior user reports.

**S3:** frontend account picker (1/2), scoped file explorer and upload/download, after separate Owner frontend authorization. This is **not** an implicit Phase 16 launch. Google Picker only if owner separately opts into opening existing files; no browser refresh-token exposure. Do not promise arbitrary Drive browsing. No RAG indexing even when reading a file for upload/download.

## 5. Cloud and deployment limitations

Local first: exact registered `http://localhost:8080/api/v1/drive/oauth/callback`. Google OAuth web clients need one registered client ID and a secure secret; consent test users, enabled API, redirect URI and project publication state are provisioned by the operator and independently tested. External/Testing refresh tokens can have a seven-day lifetime for non-basic scopes; **durable connection continuity cannot be certified on that mode**. Separate HTTPS reverse proxy, stable domain and real browser evidence before remote rollout. Do not turn ON unattended Feed refresh or dormant RAG at Drive setup. No live provider requests or production configuration changes happen in preparation.

**Next step:** Owner → Codex | Owner publishes reviewed preparation, then Codex `$codex-create-handoff` | connection-only GDI26-S1 accepted in [re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md) | READY_FOR_HANDOFF; S2/S3 retain separate execution gates | implementation NOT_STARTED; remediation 0/3 unchanged; no takeover.
