# PH-02 — H1-B prospective Drive authority/schema/API contract v0.4

**Status: SELECTED PROPOSAL FOR OWNER AUTHORITY APPROVAL / NOT_IMPLEMENTED.** D25=A already authorizes the *product behavior* of first-level app-created child folders. This contract seeks approval for its **particular schema/API/security extension**, not a second D25 vote. It is not an amendment that has already landed. It needs an independently reviewed preparation and an **exact H1-B executable handoff** after publication; never change S1/S2/frozen contracts now.

## Ownership, separation, finite authority

- One existing Spring `drive` module owns all provider operations, credential/sub/epoch checks and application provenance. Next BFF is transport/auth session only. Root folder authority (`drive_folder_roots`, V26), current root-only `GET/POST /files`, `POST /root-folder`, existing `GET /files/{fileId}/download`, Spring login/OAuth callback and Media S3 remain unchanged in behavior for root-only use.
- A child folder is **direct child** `parent == current verified PPV root ID`, MIME `application/vnd.google-apps.folder`, not trashed, exact app-private child markers, registered in one durable **Drive child-folder-intent ledger**. A child ID is never itself proof. Grandchildren, foreign/adopted/moved folders, shortcut and native Workspace docs are excluded.
- One app-created file can be direct child of root, or of **one verified first-level PPV child**; after a move outside that parent, fail closed. No direct arbitrary Drive browse.
- Retain `PPV_DRIVE_ENABLED=false` and `PPV_DRIVE_FILES_ENABLED=false`; H1-B needs both enabled **only in independently authorized isolated tests**, not the normal owner environment. No OAuth/scope changes, automatic remote mutation retries, no key/token/browser disclosure, no general Phase 16 authority.

## Exactly selected schema delta (prospective V27; no V26 edit)

**One new relation `public.drive_child_folder_intents`** — completed rows are also the durable registry of known child folders. Columns:

| Column | SQL/proof contract |
| --- | --- |
| `google_sub varchar(255)`, `root_binding_id uuid`, `idempotency_key varchar(53)` | Composite PK and FK `(google_sub,root_binding_id)` → `drive_folder_roots`; key also unique globally in this relation; all immutable |
| `root_binding_epoch uuid`, `intent_epoch uuid` | Immutable current-root/fencing attempt ID; intent epoch UNIQUE; current lease must verify epoch before dispatch/finalization |
| `safe_name varchar(255)`, `request_fingerprint char(64)` | Validated normalized UTF-8 folder name (existing input conventions), immutable SHA-256 canonical intent fingerprint containing sub/root binding/ROOT parent/name/action; `char(64)` lowercase hex check |
| `provider_folder_id varchar(255) NULL UNIQUE` | Exactly one pre-generated provider ID per action, persisted **before dispatch**; same Google ID across retries |
| `status varchar(32)` | `PREPARING`, `RESERVED`, `DISPATCHED`, `RECONCILIATION_REQUIRED`, `COMPLETED`, `FAILED_DEFINITIVE`; status/ID/terminal timestamp consistency CHECK |
| `created_at`, `updated_at`, `completed_at` | TIMESTAMPTZ; `completed_at` nonnull iff terminal; no silent deletion of unknown or completed child authority |

**One additive nullable column** `drive_upload_intents.target_parent_id varchar(255) NULL` with provider ID-format CHECK. **NULL means legacy root-only upload**, and historic root-only fingerprint/proof/retry semantics remain byte-for-byte compatible. New child uploads use nonnull immutable `target_parent_id`; new fingerprint is versioned/typed and contains parent ID, actual observed bytes/hash, normalized safe name and MIME. Never backfill/recompute historical S2 fingerprints, reinterpret old key as child upload, or allow mutation of parent after key admission. Both new folder and new child-file actions use the existing timestamped UUIDv4 key syntax; root file uploads keep S2 exact format and behavior.

### Illustrative V27 DDL contract (source code **NOT** created)

The executable H1-B handoff must refine this specific additive shape, rather than offer an unnamed alternative:

```sql
CREATE TABLE public.drive_child_folder_intents (
  google_sub VARCHAR(255) NOT NULL,
  root_binding_id UUID NOT NULL,
  idempotency_key VARCHAR(53) NOT NULL,
  root_binding_epoch UUID NOT NULL,
  intent_epoch UUID NOT NULL UNIQUE,
  safe_name VARCHAR(255) NOT NULL,
  request_fingerprint CHAR(64) NOT NULL,
  provider_folder_id VARCHAR(255) UNIQUE,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ,
  PRIMARY KEY (google_sub, root_binding_id, idempotency_key),
  UNIQUE (idempotency_key),
  FOREIGN KEY (google_sub,root_binding_id)
    REFERENCES public.drive_folder_roots(google_sub,root_binding_id),
  CHECK (status IN ('PREPARING','RESERVED','DISPATCHED',
    'RECONCILIATION_REQUIRED','COMPLETED','FAILED_DEFINITIVE')),
  CHECK (request_fingerprint ~ '^[0-9a-f]{64}$'),
  CHECK (safe_name <> '' AND octet_length(safe_name) BETWEEN 1 AND 255),
  CHECK (provider_folder_id IS NULL OR
    provider_folder_id ~ '^[A-Za-z0-9_-]{1,112}$'),
  CHECK ( (status='PREPARING' AND provider_folder_id IS NULL)
      OR (status IN ('RESERVED','DISPATCHED','RECONCILIATION_REQUIRED','COMPLETED')
          AND provider_folder_id IS NOT NULL)
      OR status='FAILED_DEFINITIVE'),
  CHECK ((status IN ('COMPLETED','FAILED_DEFINITIVE'))
      = (completed_at IS NOT NULL))
);
ALTER TABLE public.drive_upload_intents
  ADD COLUMN target_parent_id VARCHAR(255)
  CHECK (target_parent_id IS NULL OR
    target_parent_id ~ '^[A-Za-z0-9_-]{1,112}$');
```

Identity fields remain immutable in application persistence code after insert; all status changes use version/epoch/status CAS and never permit re-targeting a child/upload action. The future V27 includes bounded uniqueness/foreign-key/immutability tests. A SQL-level trigger is **not automatically required** unless independent review finds the guarded writes inadequate; any additional DDL must be scoped before the executable handoff.

**Portability:** add `drive_child_folder_intents` to `PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES` and its public-schema inventory regression. Preserve full DB backup security caveat; no new public export of Drive subjects, folder IDs, tokens or action keys. Document the additive migration in `docs/database/google-drive-files.dbml` and a **dated additive ADR-0027 applicability addendum**; do not rewrite accepted ADR-0027's historical root-only assertion as though it already approved children. Schema v1 frozen; V26 immutable.

**Bounds selected for H1-B:** max **256 durable child-folder records per root** (count completed and uncertain; no automatic row deletion, so full capacity blocks new children but does not invalidate old authority), max **8 nonterminal folder intents per root**; existing file-upload **8 nonterminal / 1024 retained** and bounded operation/admission/spool limits remain. Only explicit Owner-authorized future cleanup/migration may remove child authority; never prune uncertainty/identity to free capacity. These are engineering capacity proposals for Owner approval; do not silently implement a smaller cap.

## Durable create state machine and safe replay

1. Authenticate Owner JWT → resolve slot → current verified subject/credential epoch and root binding/epoch; validate child name, action key freshness and fingerprint. Key never grants authority.
2. Unique PostgreSQL insert admits one winner per subject/root/action. Same key+same fingerprint may read/reconcile existing state; differing name/parent/subject fails 409 with no new provider mutation. Race losers never allocate a second provider folder ID.
3. In `PREPARING`, reserve exactly **one** ID with Drive `files.generateIds` and persist `RESERVED`. Before any provider `files.create`, persist `DISPATCHED` under CAS **in a short SQL transaction**. No SQL lock spans network I/O.
4. Create folder with ID, `parents=[verifiedRootId]`, MIME folder, appProperties `ppvKind=child`, `ppvRootBinding=<UUID>`, `ppvCreatedId=<providerId>`, `ppvFolderAction=<SHA256(action key)>` and `ppvParentId=<verifiedRootId>`; verify exact parent, MIME, trash, markers and returned ID before `COMPLETED` CAS. Do not infer success from a 409 provider response alone.
5. After `DISPATCHED`, timeout/connection loss/HTTP ambiguity/crash → `RECONCILIATION_REQUIRED`; replay **never** sends create again. Only bounded `files.get(providerFolderId)` can confirm exact identity/proof; 404, propagation delay, missing markers or provider failure remain `FOLDER_OUTCOME_UNKNOWN`, never safe second create. Recovery after restart uses existing ledger. `FAILED_DEFINITIVE` only for conclusively pre-dispatch failures; never after uncertain dispatch.
6. Fresh S1 authority is mandatory before any create/read/confirmation. Disconnect/revoke/credential epoch invalidates old lease; unknown intents survive without authority and are not borrowed into another Google subject.
7. Completed ledger row is required for opening child, uploading under child, global-scoped search parent proof, download and preview; lookup alone or a matching name is insufficient. Root and child metadata are checked against Google under the current slot before sensitive content I/O.

**App-property length proof:** existing provider validation caps property name+value to 124 UTF-8 bytes. New H1-B must validate every marker before mutation; generated IDs must fit S2 bounds. `ppvFolderAction` is a fixed-length digest; `ppvParentId` is a verified provider ID and needs explicit combined-length tests.

## Selected REST contract (prospective, JWT-private)

Prefix: `/api/v1/drive/connections/{slot}`. Keep S1/S2 routes as-is unless the explicitly described extension below is required; JSON in root `ApiResponse`, download/thumbnail image bytes exceptions. Safe DTOs only; no provider URL, token or private appProperties in response.

| Method + path | Request / response | Semantics |
| --- | --- | --- |
| `GET /storage-quota` | `StorageQuota{usageBytes?,usageInDriveBytes?,limitBytes?,limitKnown,observedAt}` | one current-slot provider `about.get(fields=storageQuota...)`; label total all-service usage vs usageInDrive correctly; optional/pooled limit is not “PPV folder capacity”; no fake percentage when unknown |
| `POST /folders` | JSON `{name}` + `Idempotency-Key`; `Folder{id,name,parentId,kind:"folder",modifiedAt?}`; `201` created / `200` same verified intent; `409 FOLDER_OUTCOME_UNKNOWN` on uncertainty | app-owned first-level child only; durable ledger, pre-generated ID, no duplicate create |
| `GET /items` | query `scope=parent|all`, `parentId=root|<childId>` only with `scope=parent`, `limit=1..50`, opaque `cursor`, optional `name`, `type`, `from`, `to`, `timeZone`; `Page{items,nextCursor,hasMore}` | one bounded metadata **search/list** surface; folders (root direct children) and binary files; folder vs account-subtree scope explicit; cursor criteria isolation; see [search contract](h1-search-and-cursor-contract-v0.4.md) |
| `POST /folders/{childId}/files` | same bounded S2 multipart `file` and required `Idempotency-Key`; new child-specific route, not a reinterpretation of root `POST /files` | immutable target child ID in fingerprint, ledger and provider markers. Preserve existing root upload route unchanged |
| `GET /files/{fileId}/download` | existing binary | add verified root **or** verified child parent proof; original root behavior unchanged; no caller-provided parent as authority |
| `GET /files/{fileId}/thumbnail` | small supported image bytes or sanitized 404 fallback | current verified provenance; strict host/credential policy; see [thumbnail contract](h1-thumbnail-security-contract-v0.4.md) |

**Files in child:** new `ppvParentId=<childProviderId>` marker on provider files (in addition to existing root-binding, created-ID and action markers); root files **remain without** this required child marker. A file claiming child parent but missing a matching durable child record, with moved parent or wrong root binding is denied. The S2 `DriveFileProof` root path must not silently widen; extend with explicit root/child verification branches and tests.

**Route inventory:** S1 has four operations and S2 four, **eight historical Drive routes**. H1-B proposes exactly **five additive routes** (`storage-quota`, `folders`, `items`, child upload, thumbnail) plus one behavioral extension to the existing download; expected Drive count is **13** only when both flags are ON and all H1-B routes are active. Codex must independently check actual Spring route inventory, including OFF profiles, rather than assume any global application count.

## Exact change boundary for *future* H1-B handoff, not permission today

- **Allowed when separately authorized:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive/internal/files/**` new folder intent entity/repository/service/proof, extended provider client and cursor; `drive/internal/web/{controller,dto}/**` Drive-only new routes/DTO; `backend/src/main/resources/db/migration/V27__drive_child_folders_and_parent_affinity.sql` (one additive migration); `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java` **ONLY** table-security classification; `docs/database/google-drive-files.dbml`, an additive dated ADR-0027 addendum and Drive operations docs; focused Drive/Portability test paths. Actual source tree and minimal file allowlist must be enumerated and approved in executable handoff.
- **Forbidden:** root `authentication` business rules; scope expansion; S1 OAuth/callback; Account/Personal/Media/Feed/RAG modules; original V26/frozen Schema v1; changed `.agents`/hooks/config; browser/frontend; Google activation; automatic repair of unrelated legacy code. Additive backend changes require focused review and Owner commit before H1-F.

## H1-B mandatory independent future evidence (NOT_RUN now)

**Use existing backend implementer + fake Drive provider + PostgreSQL Testcontainers.** Report actual counts/exits; no invented pass. Focused suites: (a) V27 schema/CHECK/unique/FK and application-start Flyway compatibility; (b) root/child authority/marker/move/foreign-parent/slot/epoch/logout; (c) same-key concurrent create and crash between RESERVED/DISPATCHED/response-lost/404; (d) parent-binding upload fingerprint/retry/slot-swap while old root actions remain compatible; (e) GET `/items` complete multi-page search/filters/cursors; (f) thumbnail host/size/MIME/redirection failures; (g) quota optional/pool labels; (h) Portability public-table inventory exclusion; (i) Spring Modulith module boundary and HTTP route inventory + default OFF. No full unrelated RAG/Media suite by default.

**Owner authority still required:** approve this particular H1-B schema/API/integrity addendum, including 256/8 folder limits, prior to `READY_FOR_HANDOFF`. The review's former PH-02 cannot be independently CLOSED by ChatGPT alone. Do not commit/publish/implement until subsequent independent review/Owner action.
