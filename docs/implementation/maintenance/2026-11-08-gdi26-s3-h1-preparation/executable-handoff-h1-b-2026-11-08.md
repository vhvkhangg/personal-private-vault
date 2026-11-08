# Executable slice handoff — GDI26-S3-H1-B Drive Explorer backend

- **Handoff ID:** `GDI26-S3-H1-B`
- **Created by / date:** Codex / 2026-11-08
- **Status:** `READY_FOR_OWNER_COMMIT`
- **Mode:** explicit Owner-approved maintenance/feature extension of the existing Drive backend, **H1-B only**. Not H1-AUTH, H1-F, Phase 16 or RAG implementation.
- **Implementer:** Antigravity `/antigravity-implement-handoff`, existing `backend-implementer`; mandatory `/antigravity-self-review`.
- **Reviewer:** independent Codex in a separate session, `$codex-final-review` for this slice.
- **Entry commit / published preparation:** [`b0cb5f7a27b8515af7324e6ef8998dad5c059416`](https://github.com/vhvkhangg/personal-private-vault/commit/b0cb5f7a27b8515af7324e6ef8998dad5c059416), `docs(frontend): finalize H1 Explorer and Google sign-in preparation`. Local HEAD, `origin/main` and live GitHub `refs/heads/main` matched; entry worktree clean, `ACTIVE=NO_ACTIVE_HANDOFF`, migration inventory ends at V26, no frontend/root package or Graphify graph.
- **Preparation acceptance:** [independent re-review 2](reviews/2026-11-08-gdi26-s3-h1-independent-pre-handoff-re-review-2.md) = `READY_FOR_HANDOFF`. PH-01–07 and PH-L01 CLOSED **for preparation**; PH-L02 LOW/OPEN. No unresolved material H1-B Owner decision was identified.
- **Authority:** current Owner `$codex-create-handoff` invocation after preparation publication; [D23–D25 and requirements](owner-decisions-and-open-questions.md), [D26–D30 addendum](owner-decision-addendum-v0.5.md), accepted H1-B/schema/search/thumbnail contracts below. H0 and S1/S2 exceptional Codex authority is not transferred.
- **Implementation / tests:** independently ACCEPTED in [final re-review 3](reviews/2026-11-08-gdi26-s3-h1-b-independent-final-re-review-3.md). FR-01–08 and FR-L01 CLOSED; B-01–08 PASS. Evidence: 215 fresh JUnit PASS, 115 qualified reused PASS, 10 standalone checks PASS separately. Exact current source hashes/receipts and bounded operational limits are recorded there.
- **Remediation:** H1-B Antigravity **3/3 consumed, accepted and stopped**. No further correction loop; takeover **NOT_AUTHORIZED / NOT_USED**.

## Outcome and authority precedence

Provide the backend required by the approved Explorer: app-owned first-level child folders, original files within those children, bounded account-subtree filename/type/date search, real account quota and safe optional thumbnails. Preserve legacy root-only behavior and all existing credential, provenance, action-identity and transfer guarantees. Backend delivery comes before the separately reviewed Google Sign-In and Next/BFF slices.

Read [root AGENTS](../../../../AGENTS.md), [backend AGENTS](../../../../backend/AGENTS.md), [test AGENTS](../../../../backend/src/test/AGENTS.md), [docs AGENTS](../../../AGENTS.md), current [ACTIVE](../../handoffs/ACTIVE.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md) and [Docs Sync](../../../workflow/documentation-synchronization.md), then:

- [H1-B authority/schema/API](h1-b-authority-schema-api-contract-v0.4.md), [current search v0.6 at stable v0.4 path](h1-search-and-cursor-contract-v0.4.md), [thumbnail policy](h1-thumbnail-security-contract-v0.4.md), [acceptance B-01–08 / H1-28–35](acceptance-and-verification-plan.md), latest independent review.
- [Main visual approval](owner-visual-approval-record-v0.3.md), [desktop/state delegation](h1-desktop-and-state-delegation-v0.5.md), [scope/sequencing](scope-and-sequencing.md) and [scope README](README.md).
- Existing [ADR-0027](../../../adr/0027-google-drive-root-file-authority.md), [Drive file architecture](../../../architecture/google-drive-files.md), [operations](../../../architecture/google-drive-file-operations.md), [Drive DBML](../../../database/google-drive-files.dbml), [ADR-0016 shared HTTP](../../../adr/0016-root-http-contract-module-local-adapters.md), [module matrix](../../../architecture/module-dependency-matrix.md), [package inventory](../../../repository/repository-package-tree.md), and [S2 closeout](../2026-11-06-gdi26-s2-preparation/owner-publication-closeout-2026-11-07.md).

The accepted re-review and D26–D30 supersede earlier pending-review/approval wording. **Search uses five candidate/child proofs, no buffered candidate rows**, not the obsolete six/buffer text in risk H1-R19 (PH-L02). Do not repair that LOW summary here or use it as a competing implementation contract. Preparation publication-pending/no-active statements remain dated history; this handoff establishes execution.

The exact desktop v0.3 hash was checked as `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`. Owner consent/delegation was checked through canonical records, not a newly fetched external conversation. D28 delegates auxiliary states; D27 defers mobile. No new approval of the old v0.4 derivative sheets is needed. **This backend handoff neither creates UI nor opens the later slices.**

## Necessary behavior and simplest implementation

| Decision | Selected mechanism / practical limit | Relative effort estimate |
| --- | --- | --- |
| KEEP / REUSE | One durable child-intent registry, existing S2 lease/resource/short-transaction pattern, current PostgreSQL; no second root/file catalog | High: identity, races and restart proof |
| KEEP additive | One V27 relation plus one nullable upload-parent column, explicit legacy-root branch; no V26 edit/backfill | Moderate: migration and compatibility |
| SIMPLIFY | One metadata listing surface, <=5 provider entries and <=8 Drive dispatches per request; local exact substring filtering, opaque process-local continuation | Moderate/high: completeness and authority tests |
| KEEP | One account-quota read; absent/pooled fields remain truthful | Localized |
| SIMPLIFY | Exact-origin <=1 MiB thumbnail proxy with honest fallback; no universal renderer or remote URL in DTO | Moderate: transport/credential negatives |
| REUSE | Existing controllers/DTO package, ApiResponse, feature flags, admission/spool, Spring auth and S1 callbacks | Moderate: focused HTTP/root regressions |
| DEFER | Google website login/OIDC/V28/client starter; Next/BFF/session/UI/packages; live Google/Cloud/HTTPS and operational activation | No implementation here |
| REMOVE | Generic job/adapter framework, extra role, dependency upgrade, Redis, whole-file index, new hook/runner or benchmark platform | No demonstrated need |

Effort is a qualitative estimate assuming unchanged S1/S2 seams, not measured duration or a test-count quota. These coupled features form the independently selected H1-B scope; do not silently remove a required behavior to make a smaller passing delivery.

## Exact edit boundary and reuse map

Prefixes below are literal repository paths, not wildcard permissions:

- `D = backend/src/main/java/com/vhvkhangg/personalprivatevault/drive`
- `T = backend/src/test/java/com/vhvkhangg/personalprivatevault/drive`

| Allowed production paths | Required outcome / reusable seam |
| --- | --- |
| `D/internal/files/application/DriveFileService.java`, `DriveFileProof.java`, `DriveFileInputs.java`, `DriveUploadIntentStore.java`, `DriveFileException.java` (each in that application directory) | Explicit child upload/download proof and typed parent-affine action; preserve root methods/wire behavior. Reuse `requireOwner`, `acquire/verify/finalWrite/dispatch`, input normalization/digests and root readiness; do not duplicate credential authority |
| `D/internal/files/persistence/DriveUploadIntent.java`, `DriveUploadIntentRepository.java` | Nullable immutable target parent, guarded insert/lookup/replay, unchanged historical root fingerprint/retention/CAS behavior |
| `D/internal/files/http/DriveFileHttpClient.java`, `DriveFileResources.java` | Fixed provider operations, controlled test seam, scoped thumbnail caps/cancellation using existing admissions/timer cleanup; no raised call/transfer/spool limits |
| `D/internal/files/web/DriveFileFilter.java` | Extend exact route selection for all five new routes; child upload receives the same pre-parse upload admission/spool/request bounds |
| `D/internal/files/config/DriveFilesConfiguration.java` | Wire only the new Drive-owned operations under existing files flag and Drive prerequisite; no new flag/default or root/security configuration change |
| `D/internal/web/controller/DriveFileController.java`, `D/internal/web/dto/DriveFileDtos.java` | Five additive private operations, explicit DTOs/OpenAPI, old operation IDs and JSON/binary semantics retained; reuse bounded async/error/stream handling |
| New `D/internal/files/persistence/DriveChildFolderKey.java`, `DriveChildFolderIntent.java`, `DriveChildFolderIntentRepository.java` | Focused child action identity/entity/repository; no cross-module JPA access or generic persistent job type |
| New `D/internal/files/application/DriveChildFolderIntentStore.java`, `DriveChildFolderService.java`, `DriveItemSearchService.java`, `DriveItemCursors.java`, `DriveThumbnailService.java`, `DriveExplorerService.java` | Small cohesive store/child-proof/search/cursor/thumbnail/orchestration responsibilities. Nested immutable criteria/results are sufficient; omit an unused proposed file if cohesion permits inlining within another listed file. No extra classes/packages/framework solely to match this inventory |
| `backend/src/main/resources/db/migration/V27__drive_child_folders_and_parent_affinity.sql` (new) | Exactly selected additive shape below; V1–V26 unchanged, no V28/auth table |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java` | **Only** add `drive_child_folder_intents` to `EXCLUDED_SECURITY_TABLES`; preserve read-only adapter/business table allowlist |

Existing `DriveFolderService`, `DriveFileAuthority`, `DriveFileCursors` and S1 OAuth/crypto/lifecycle sources are **read/reuse only**. Keep legacy S2 cursors separate from the new one-use query cursor semantics. Reuse existing packages/package-info; no new module, public named interface or dependency edge. API DTOs remain under `drive/internal/web/dto`, not a new `common` owner. No `backend/pom.xml`, Authentication, shared security/HTTP source, global MVC, application defaults, `.agents`/`.codex`/hooks/runner, other business modules or frontend changes.

### Exact test and fixture allowance

New tests may use these paths under `T`: `DriveH1BPersistenceIntegrationTest.java`, `DriveH1BAuthorityIntegrationTest.java`, `DriveH1BSearchIntegrationTest.java`, `DriveH1BThumbnailIntegrationTest.java`, `DriveH1BQuotaIntegrationTest.java`, `DriveH1BWebIntegrationTest.java`. These are organizing choices; coverage of B-01–08 is mandatory, not one class per row.

Reuse/extend only `T/fixture/FakeGoogleDriveServer.java`, `DriveS2TestSupport.java`, `DriveS2HttpTestSupport.java`; if needed add `DriveH1BTestSupport.java`, `DriveH1BHttpTestSupport.java`, `FakeGoogleThumbnailServer.java` in that fixture directory. Existing fixtures need child-ledger cleanup **before** root deletion and source-compatible construction. Fake OAuth is reused unchanged; fake keys/accounts only, all controlled servers/streams/processes owned and closed.

Existing `T/DriveS2WebIntegrationTest.java`, `DriveS2UploadIntentIntegrationTest.java`, `DriveS2ConfigurationIntegrationTest.java` may receive only necessary constructor/fixture/route-inventory adjustments and stronger compatibility assertions. Retain original root, schema, payload, auth and failure assertions; do not weaken expected behavior. `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java` may add the new security exclusion/no-export proof; its 67-business-table inventory does not increase for a security-only relation. `ApplicationArchitectureTests` and other selected S1/S2 tests are run/reused unchanged. No unrelated test/helper or Media fixture repair.

No other files, deletions or moves are authorized. If a concrete needed target is outside this list, report its specific purpose before changing scope.

## Selected API and transport

Prefix `/api/v1/drive/connections/{slot}`; PPV bearer must represent Owner `sub=1`. S1 slots remain 1/2 and independent of future Google website login. Wrong/absent/malformed/expired/Google ID token must not reach provider content or mutation. No SecurityConfig/public-route change is needed.

| Method / suffix | Request and public result | Required status / example |
| --- | --- | --- |
| GET `/storage-quota` | `StorageQuota{usageBytes?,usageInDriveBytes?,limitBytes?,limitKnown,observedAt}` | 200 truthful projection under current slot; failed provider read is an error, not invented zero |
| POST `/folders` | JSON `{name}` and required `Idempotency-Key`; `Folder{id,name,parentId,kind:"folder",modifiedAt?}` | 201 verified new / 200 verified replay; 409 `FOLDER_OUTCOME_UNKNOWN` on uncertain create |
| GET `/items` | `scope,parentId,limit,cursor,name,type,from,to,timeZone`; `ItemPage{items,nextCursor,hasMore}` | 200 bounded fully processed provider page, possibly empty with continuation; explicit errors on incomplete proof |
| POST `/folders/{childId}/files` | Same single `file` multipart and action header as S2, immutable verified child target; existing File DTO | 201 new / 200 same verified action; existing upload errors/limits preserved |
| GET `/files/{fileId}/thumbnail` | No caller URL; verified small supported image bytes | 200 safe image or sanitized 404 `THUMBNAIL_UNAVAILABLE`; auth/stale authority errors retain their proper category |
| Existing GET `/files/{fileId}/download` | Original binary attachment | Explicit verified root **or child** branch; no caller parent as authority; old operation ID/headers/failure semantics retained |

`Item` exposes only `id,name,kind,mimeType,sizeBytes?,modifiedAt,parentId`; folder size is null, not fabricated zero. No Google subject/credential/root epoch, appProperties, action fingerprint, provider token/URL or JPA entity serialization. JSON uses root ApiResponse under ADR-0016; thumbnail/download are intentional bytes exceptions. Stable new operation IDs: `getDriveStorageQuota`, `createDriveChildFolder`, `listDriveItems`, `uploadDriveChildFile`, `downloadDriveFileThumbnail`.

Folder JSON is explicitly bounded (4 KiB), name-only, rejects unknown/duplicate fields and malformed body before provider mutation. Query parameters are allowlisted, duplicate/unknown values rejected; no raw provider query/page token. Folder names use existing NFC/trim/1–255 UTF-8-byte/no slash/control/dot-path rules. Existing key syntax/freshness applies: `yyyyMMddTHHmmssZ.lowercase-UUIDv4`, new absent action within UTC [-600s,+120s], recorded actions recover later. Folder validation maps to documented `FOLDER_KEY_REQUIRED`/`FOLDER_KEY_INVALID` (400), `FOLDER_KEY_EXPIRED`/`FOLDER_KEY_CONFLICT`/`FOLDER_IN_PROGRESS`/`FOLDER_FAILED` (409). Reuse syntax validation, not an independent key format. Existing upload error literals remain unchanged.

Invalid criteria/input 400; missing/wrong bearer 401, authenticated non-Owner 403; unavailable/foreign file/parent 404; stale authority 409; incomplete search/provider failure 502; capacity/quota/rate failure 503; existing provider timeout 504. Reuse safe DriveException handling; do not return raw provider/SQL/URL/input diagnostics. `FOLDER_OUTCOME_UNKNOWN` needs its own safe recovery message. All private JSON uses no-store. Errors before byte commitment are JSON; never append JSON to a committed binary body.

Expected Drive inventory: S1 four + S2 four + H1-B five = **13 operations with both flags ON**. Drive ON/files OFF retains four; both OFF retains none; files ON/Drive OFF fails closed. Current source asserts global 243 with S2 enabled, so additive total is expected 248 absent other changes; independently enumerate actual routes/OpenAPI, do not infer a universal application total. Preserve all eight old operation IDs and S1 callback's public GET/text response.

## V27, durable folder identity and capacity

Implement the **specific selected DDL** in the H1-B contract, with these final refinements; do not invent an alternative schema:

- One `public.drive_child_folder_intents` relation: composite PK `(google_sub varchar(255),root_binding_id uuid,idempotency_key varchar(53))`, global unique key, non-cascading composite FK to `drive_folder_roots`; immutable `root_binding_epoch uuid`, unique `intent_epoch uuid`, `safe_name varchar(255)`, lowercase-hex `request_fingerprint char(64)`, nullable globally unique `provider_folder_id varchar(255)`, `status varchar(32)`, UTC `created_at/updated_at/completed_at timestamptz`.
- Preserve all selected name/ID/fingerprint/status/terminal-time CHECKs. Provider IDs fit `[A-Za-z0-9_-]{1,112}`. PREPARING requires null ID; RESERVED/DISPATCHED/RECONCILIATION_REQUIRED/COMPLETED require ID; completed_at exists iff COMPLETED/FAILED_DEFINITIVE. FK/PK/unique indexes suffice for bounded <=256-row root scans and exact provider-ID lookup; no speculative indexes/trigger.
- Exactly one nullable `drive_upload_intents.target_parent_id varchar(255)` with the selected ID-format CHECK. Null is legacy root mode. No backfill, historical fingerprint recomputation, token/body storage, cascade or old-migration edit.
- Identity/name/fingerprint/parent fields are immutable through application writes. **New folder epochs are immutable action/fence identities**; do not copy the existing upload store's rotating-epoch claim updates into the new relation. Guard state changes by captured identity, expected state and short committed locking/CAS; existing upload epoch semantics remain unchanged. SQL constraint proof and application-write immutability proof are distinct; no claim of an unimplemented DB trigger.
- Folder fingerprint: domain-separated SHA-256 `GDI26-H1B-FOLDER-V1` plus unambiguous length-prefixed canonical sub, root-binding UUID, ROOT discriminator/current root ID, normalized name and action-key digest. Use existing digest/input helpers; no raw key/subject/fingerprint logs.
- **256 total durable folder records/root**, all retained statuses counted; **8 nonterminal folder intents/root**. Completed and uncertain rows are never automatically pruned. Existing-key replay/reconciliation still works at capacity. Root-row lock (`lockBySubject`) plus insert/uniqueness/count in one short transaction protects different-key races; JVM live claims are not a replacement for DB admission/uniqueness. Existing file-action 8 nonterminal/1024 retained/terminal pruning and two operation/two-spool budgets remain unchanged.

Create sequence: authenticate/acquire/ready → validate/admit one action → PREPARING → allocate/persist one provider ID as RESERVED → persist DISPATCHED **before** create → one provider create → validate identity/markers/current authority → COMPLETED. No SQL or S1 lifecycle lock spans network/body I/O. A live loser does not generate another ID or mutate Google. Failed pre-reservation allocation can leave PREPARING without authorizing duplicate remote creation; after an ID is durable it is never replaced.

Child create uses parents=[verified root ID], folder MIME and exact appProperties `ppvKind=child`, `ppvRootBinding=<UUID>`, `ppvCreatedId=<reserved ID>`, `ppvFolderAction=<SHA256 key>`, `ppvParentId=<root ID>`. Validate combined property key+value <=124 UTF-8 bytes **before mutation**. Existing common proof reads `ppvKind` with max length four: support the five-character `child` kind deliberately, without relaxing unrelated root/file proof checks.

After DISPATCHED, crash/lost reply/timeout/ambiguous status becomes or remains RECONCILIATION_REQUIRED (durable DISPATCHED is already uncertain). Replay is bounded exact-ID GET only, never another create/session/PUT; 404 or mismatched markers do not prove safe noncreation. `FAILED_DEFINITIVE` is permitted only for conclusively pre-dispatch failure. Only exact proof plus current slot/sub/credential/root fence publishes success; a provider 409 alone is insufficient. Disconnect/revoke/reassign fences dispatch and completion; records survive without granting credentials. No background recovery or cleanup/reset API.

## Parent-affine files and original root compatibility

Opening/listing/upload/download/thumbnail under a child needs a **COMPLETED durable child row** for current sub/root and fresh Google folder proof: direct parent is current root, nontrashed folder, exact creation/root/action/parent markers and ID. Foreign/adopted/moved/shortcut/grandchild folders fail closed. A name or ID alone never grants access.

Child upload persists nonnull immutable target parent and adds `ppvParentId=<child ID>` to otherwise existing file markers. Derive the child fingerprint from a new domain-separated encoding of target parent and the existing **server-observed S2 spool fingerprint**; that existing digest already binds normalized name/MIME, actual size/content SHA-256. This avoids a second body read/parser/fingerprint implementation. Root mode keeps the exact old digest/metadata and null parent. Same key on another parent/mode conflicts; payload/name/MIME changes conflict. Reuse checksum reconciliation and both upload transports without automatic redispatch.

Root proof remains explicit; it must not accept a moved child file merely because its new parent is root. Reject child-parent markers on root-mode files; child mode requires actual parent==immutable marker==verified child and current root binding. Test child A→root, child A→child B, root→child without marker, moved/trashed child and foreign sub/root. Never infer original parent from the browser. Folder registry stays durable even when existing terminal file intents are legitimately pruned; do not add a file catalog.

Preserve 52,428,800-byte file / 57,671,680-byte request caps, zero-byte behavior, <=64 KiB transfer buffers, validated spool/owned cleanup, original file MIME/name rules and no native Workspace conversion. Preserve metadata/body/idle/operation 10/120/15/180s, connect 2s and <=8 Drive dispatches plus existing separately bounded S1 refresh. Download uses actual expected length/no-store/nosniff/attachment and current authority through final EOF; abort/short/excess/deadline/disconnect is observable failure, no completed response synthesized from partial bytes. No Range support is added.

## Search v0.6 — exact scope, bounded page and honest continuation

- Defaults: scope=parent, parentId=root, limit=50, type=all, timezone=Asia/Ho_Chi_Minh; absent name/date means no such filter. Caller limit 1..50; **effective provider pageSize=min(limit,5)**. Active name/type/date filtering requires scope=all; scope=all forbids parentId. Parent browsing accepts root sentinel or a current completed/proven child only, never arbitrary IDs.
- Trim supplied name, require 1..128 Unicode codepoints, NFC then `Locale.ROOT` lowercase; accent-sensitive **substring**, not fullText or provider `name contains`. Clear UI search omits the parameter; supplied blank is malformed. Quoted names are data, never raw q syntax.
- Types: all/folder/image/pdf/document/text/other. Use explicit MIME classification: folder only proven child folders; image regular image/*; pdf application/pdf; document legacy/OOXML Word, Excel, PowerPoint and OpenDocument text/spreadsheet/presentation MIME allowlist; text remaining text/*; other remaining allowed binary types. No filename-extension guessing or native Workspace documents. Document the exact finite document MIME allowlist and test its members/complement.
- Optional strict YYYY-MM-DD from/to use modifiedTime and the sole allowed zone Asia/Ho_Chi_Minh, inclusive local dates translated to UTC half-open bounds. Reject invalid/reversed/overflowing dates/unsupported zones. Example from=2026-11-01,to=2026-11-02 becomes [2026-10-31T17:00Z,2026-11-02T17:00Z); include lower bound, exclude upper. Folder date qualification needs actual valid modifiedTime.
- Provider query fixes corpora=user, spaces=drive, trashed=false, exact escaped app-private ppvRootBinding predicate and optional verified direct-parent clause; orderBy=`modifiedTime desc,name`. Request bounded metadata including incompleteSearch and nextPageToken; exclude root itself/native/foreign items and independently qualify candidates. No allDrives, prefix substitution, broad text search or local persistent catalog.
- **Actual request accounting:** ready() performs accountRoot + PPV-root GETs (two), then one files.list and at most five distinct child GETs = **eight**. Every returned provider candidate is processed within that request; per-operation proof reuse only. A parent-child browsing proof counts inside the same budget. No hidden extra root/file/proof call, sixth child or ninth dispatch; never raise S2's ceiling. S1 credential refresh remains its existing separate bounded operation, not a hidden Drive-list call.
- Five provider entries can yield zero visible items. Fully processed empty page plus next provider token returns items=[], hasMore=true, nonnull cursor. Final null cursor/hasMore=false requires no provider token and no incompleteSearch/mandatory proof error. Malformed candidate authority is omitted or denied appropriately; unavailable mandatory child verification/network failure cannot become false terminal success. There is **no retained candidate-row buffer** or cached prior-request parent authority.
- H1 cursors: CSPRNG opaque 43-character handles, 300s expiry, <=128 outstanding, bound to Owner/sub/slot/credential epoch/root binding+epoch/scope/verified parent/all normalized criteria/callerLimit/effective pageSize/provider token. Validate then atomically consume once; concurrent replay has at most one winner. Wrong bindings/expired/replayed/restart handles yield DRIVE_CURSOR_INVALID (400); provider token poison/loop/incompleteSearch yields DRIVE_SEARCH_INCOMPLETE (502), capacity existing safe 503, timeout existing 504. Use bounded token-loop/progress metadata; exhausting that guard is explicit incomplete, never complete. No provider token is public.
- Traversal is not a snapshot under provider edits. No total/file count or global stable ordering promise; document reorder/duplicates and restart semantics for later UI. Bounded pages limit per-request work, not total cost for an arbitrarily large corpus.

## Account quota and safe thumbnail

Quota uses current S1 lease/slot/epoch and one `/drive/v3/about?fields=storageQuota(...)` read; it needs no folder creation or fake root capacity. Verify current authority before projection. Parse optional nonnegative int64 fields without substituting zero: usage=all services (possibly pooled organization), usageInDrive=Drive, limit may be absent; limitKnown reflects a valid supplied limit, observedAt is UTC. Failed/malformed reads are errors, not invented metrics. No shared cross-subject cache or computed percentage.

Thumbnail flow: current Owner/slot/root → current root/child file proof and fresh metadata → **untrusted provider URL validation before bearer dispatch** → bounded image verification → bytes. No URL parameter or raw thumbnailLink in DTO, logs or browser redirects. Exactly HTTPS `lh3.googleusercontent.com`, port absent/443; canonical ASCII hostname comparison, no other subdomain/suffix, IP, userinfo, fragment, non-HTTPS/non-443, malformed/oversize URI. Bound URL length to 4096; reject untrusted metadata rather than broaden trust. This host is a selected restrictive policy, **not a Google hostname guarantee**.

Use fixed trusted TLS/hostname validation, GET only, redirects disabled, no cookies/arbitrary headers/proxy override/compression decoding; send bearer only after origin validation. Per admitted operation, one image request at a time, connect <=2s, image-request total <=8s, idle <=5s and <=1,048,576 actual bytes. Reuse operation cancellation/timer/admission; any shorter phase limit must not lengthen legacy phases. Metadata/proof calls retain their existing bounds. Small bounded image buffering for verification is permitted; no shared thumbnail heap cache, transcoding or PDF/DOCX renderer.

Accept only verifiable PNG/JPEG/WEBP with exact safe MIME and bounded signature/decoder sanity checks; reject SVG/HTML/XML/GIF, wildcard MIME, mismatch, empty/oversize/truncated/compressed/suspicious content. Do not allocate unbounded decoded images or add a codec framework; an unsupported verification path falls back. Return safe image Content-Type, `Cache-Control: private, no-store`, nosniff and restrictive CSP. Missing/untrusted/unsupported/quota/timeout image yields sanitized THUMBNAIL_UNAVAILABLE fallback; authority failure remains a denial, no content-success leak. Recheck cancellation/current authority around dispatch/completion. Test-only controlled origin/transport is not a production override or proof of live lh3/TLS/provider coverage.

## Mandatory acceptance and risk evidence — NOT_RUN at creation

| Gate / traceability | Observable positive and negative result | Minimal evidence / existing reuse |
| --- | --- | --- |
| **B-01** schema/integrity | Fresh and V26→V27 seeded upgrade/startup succeed; existing root fingerprints and rows unchanged; nullable parent, FK/unique/hex/name/ID/status/terminal-time constraints reject invalid data; no v1/V26 drift | Actual PostgreSQL/Flyway/JPA validate; repository-write immutability/state-transition checks, source/migration hashes |
| **B-02** H1-29, R13 | Same-key races and response loss produce at most one durable ID/create; RESERVED crash can do first dispatch, DISPATCHED/restart/404 stays GET-only unknown; 256/8 bounds withstand **different-key** concurrent requests; existing-key recovery still works at full capacity | PostgreSQL barriers + fake provider dispatch counters; root-row lock/state store; no sleeps as race proof or deleting uncertainty |
| **B-03** H1-28/30, R14 | Completed child plus current parent/markers/slot/sub/root/epoch required; foreign/adopted/trashed/shortcut/moved/grandchild and disconnect/reassign races deny before content/mutation | Actual service/HTTP with PG + fake metadata; barriers before dispatch/final publication; existing S1 authority guard |
| **B-04** H1-10–14/31, R05/15 | Child original upload/list/download works; changed parent/mode/body conflicts; stale child cannot publish; legacy root bytes/digest/metadata/key/recovery remain compatible; body caps/cancel/short/excess fail honestly | Real HTTP/stream tests plus S2 regression; fixed legacy fingerprint/seeded old intent, actual observed payload/checksum, no new-key automatic retry |
| **B-05** H1-09/22/23/32/33, R16/19 | At least six distinct child IDs across pages, later substring hit and full stable eligible corpus reachable; first five-child page exactly two root+list+five proofs (8), sixth in next operation; empty continuation/terminal no-match/Unicode/type/date/cursor swaps/replay/poison/incompleteSearch/relocation/cap/timeout truthful | Count actual Drive dispatches at fake server AND operation seam; compare stable corpus, no spill buffer. Separately show non-snapshot limitation under mutations |
| **B-06** H1-24/35, R17 | Root/child authorized safe image succeeds when supported; URL variants/redirects cause **zero bearer dispatch to forbidden destinations**; MIME/signature/1 MiB/slow-progress idle/8s deadline/abort/slot switch deny or fallback, never raw URL/token/cache leak | Controlled image transport + actual private HTTP path/current PG authority; default TLS/redirect policy tests, exact production/fixture limits recorded |
| **B-07** H1-21/34, R18 | Slot-specific all-service and Drive usage differ correctly; absent/unlimited/pooled/error values never become fabricated capacity/percentage, late slot/epoch projection denied | Fake about response and actual DTO/error assertions; current lease/final verification |
| **B-08** H1-01/20/42, R12/20/25 | New ledger excluded from public snapshot and manifest security inventory; 13/4/0 Drive route flag matrix, files-without-Drive fail, auth and OpenAPI DTO/bytes schemas exact; module boundary unchanged/defaults OFF | Portability PG inventory/no-export regression, actual route/OpenAPI inventory, ApplicationModules.verify; complete changed-subtree/source review and saved docs evidence |

B-06 URI controls include evil suffix, other lhN, http, IP/localhost, encoded userinfo, fragment, port 444, malformed/oversize and 302/307; content controls include valid images, forged/truncated body, declared-length mismatch, >1 MiB and slow trickle. B-05 includes NFC/NFD Vietnamese accents, quoted names, lower/upper date boundary and no false exhaustive result on provider failure. Mandatory proof is not replaced by mocks where PostgreSQL, network dispatch or actual HTTP body behavior matters. These tests use synthetic accounts/keys and controlled fake providers only.

## Focused verification contract

Use existing Java/JUnit/AssertJ/Testcontainers/HTTP fixtures; no H2, new dependency, model, browser or live Google. Run focused failures while implementing; after convergence execute the selected gate **once per unchanged source set**, sequentially with fresh receipts. Existing relevant passes can be reused by exact source/fixture/command identity; no fabricated aggregate count or duplicate case counting.

| Batch | Exact Maven selector |
| --- | --- |
| New H1-B acceptance | `DriveH1B*Test` (all created allowed H1-B test classes; verify discovery is nonempty) |
| Legacy root resources/action/proof/HTTP | `DriveS2*Test` |
| S1 unaffected contract/security | `DriveApiInventoryIntegrationTest,DriveConfigurationIntegrationTest,DriveWebIntegrationTest,DriveOAuthSecurityTest,DriveGrantRevocationIntegrationTest` |
| Integration boundaries | `PortabilityIntegrationTest,ApplicationArchitectureTests` |

**Final gate command template**, repository root PowerShell, invoked separately for each selector above:

```powershell
$h1bPython = (Get-Command python -ErrorAction Stop).Source
$h1bMaven = (Get-Command mvn.cmd -ErrorAction Stop).Source
$h1bSelector = 'DriveH1B*Test'
$h1bRunDir = Join-Path 'logs/gdi26/h1-b' ('verify-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $h1bRunDir -ErrorAction Stop | Out-Null
& $h1bPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $h1bRunDir 'maven.json') -- $h1bMaven -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' ('-Dtest=' + $h1bSelector) test
```

Record actual selected/discovered tests, command/exit/duration, provider/PG/transport proof, failures/skips and receipt/raw stdout/stderr paths under unique ignored logs. Ordinary cap **300s**; existing published runner supports approved eligible full suites up to 2,100s, but this handoff grants **no extended profile/full-suite waiver**. If an actual selected batch exceeds 300s, report it and split the same approved classes into bounded invocations with source-qualified reuse, never drop mandatory assertions or silently extend time. Preserve inactivity diagnosis/owned-tree cleanup and every prior log.

Check `git diff --check`, actual defaults, schema ordering, changed-package inventory/visibility, DTO/error/OpenAPI and docs links. No unrelated RAG/Media/full backend suite, native hook re-audit, IDE-clean or performance certification is required. Hooks/config/runner are unchanged; accepted H0/B1/B2 evidence remains only within its source/client/trust limits. No live Google, key/spool ACL, real lh3 coverage, HTTPS/deployment/topology or frontend inference is made from fake-provider passes.

## Documentation impact and ownership

Antigravity is expressly authorized to update only these implementation-adjacent docs alongside accepted scope:

- `docs/database/google-drive-files.dbml`: additive child relation/nullable target and exact constraints/retention/Portability; no frozen v1 rewrite.
- `docs/adr/0027-google-drive-root-file-authority.md`: **dated additive H1-B applicability section** citing D25/D29/preparation/this handoff. Preserve historical root-only decision/evidence; explain narrow exception and unchanged S2 branch, not retroactive S2 acceptance of children.
- `docs/architecture/google-drive-files.md`, `docs/architecture/google-drive-file-operations.md`: current H1-B authority/API/state/bounds/error/fallback/search limitations and default-OFF operator hold, labeled implementation pending independent review until accepted. Distinguish legacy root scope from additive child scope; no invented publication/live claims.
- `docs/repository/repository-package-tree.md`: append only actual H1-B Drive files/tests to its existing additive Drive inventory; preserve frozen v1.1 baseline, ownership and unrelated entries. No new diagrams/module-matrix edge is implied; shared HTTP/ADR-0016 conventions remain unchanged.
- This executable handoff and `docs/implementation/handoffs/ACTIVE.md`: result/status/counter/next action only. New `docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/implementation-handback-h1-b-2026-11-08.md`: B-01–08 results, exact source/schema/test paths/hashes, commands/receipts, failures/limits, self-review and Docs Sync Check.

Implement the approved logical addendum/DBML consistently with V27 and final source; do not claim new runtime behavior merely because docs were written. No scoped instruction needs changing: root/backend/test rules already permit only ACTIVE; Drive has no existing scoped AGENTS. Independent Codex owns review/findings; ChatGPT owns separately authorized routine preparation/index/LOW/governance/publication closeout. After independent acceptance only Owner stages/publishes; later closeout cites the real commit. D30 means any requested package contains only new/modified files, no historical/consolidated archive.

**Handoff-creation Docs Sync Check:** UPDATED — ACTIVE and this contract only. ALREADY_CONSISTENT — reviewed preparation published at b0cb5f7; no live handoff/V27/frontend at entry; current D23–D30 and v0.6 five/no-buffer contract; v0.3 hash; defaults/source ownership unchanged. Saved-file verification confirms aligned status/next action, all B-01–08 rows, 34 relative links to 28 existing targets, no trailing whitespace, clean git diff --check and exactly two changed paths with no staged files. HISTORICAL — earlier preparation pending/blocked language and old no-active closeout. DEFERRED_OUT_OF_SCOPE — PH-L02 obsolete risk/summary wording, H0 FR-L01 opaque-refresh wording/PH-I01, unrelated global banners/configuration exception. NOT_VERIFIED — all B-01–08 runtime, live provider/deployment and later slices. This is handoff creation, no implementation test execution or code change.

## Completion, review and explicit exclusions

Deliver the exact scope and meaningful risk-based tests, self-review and truthful handback. Map B-01–08 and applicable H1-28–35 to actual evidence; supply source hashes, migration safety/compatibility, operation counts, concurrency barriers, auth denials and failed/NOT_VERIFIED cases. Mark nonapplicable UI/Google-login/OCR/RAG/model/evaluation dimensions N/A with reasons, never runtime PASS. New High/Critical source/privacy/integrity risks require the concrete evidence above, not a generic best-practices claim.

After `/antigravity-self-review`, the parent implementation skill must save top-level ACTIVE `- **Status:**` as `IMPLEMENTED_AWAITING_CODEX_REVIEW`, align banner/result/counters/next step and this contract/handback, then **reopen all three saved files**. Findings addressed by an implementer remain pending independent adjudication. Docs-only repair consumes no additional round. Separate Codex final review assigns acceptance; CRITICAL/HIGH/MEDIUM or mandatory NOT_VERIFIED blocks READY_FOR_OWNER_COMMIT, LOW/INFO stays backlog. Stop on acceptance; at most three correction+independent re-review rounds after an initial blocking review, then separate Owner takeover authority only.

Stop for unapproved target/identity/root-depth/host/scope/limit/recovery changes, a conflicting live handoff/source drift, required configuration/installation/activation beyond scope or an unresolved material contract conflict. Do not silently weaken selected proof, invent extra Owner gates or borrow H0/S1/S2 exceptions.

**Excluded:** H1-AUTH/Google sign-in/managed client starter/V28/password or JWT policy changes, H1-F/Next/BFF/session/UI/mockups/mobile/package install, `.agents`/`.codex`/trust/hook/runner changes, provider scopes/S1 callback changes, adopted/shared/recursive folders or grandchild operations, directory upload, native export, delete/rename/move/share/copy, sync/background recovery, file catalog, Media replacement, Feed/RAG/indexing/model/deployment and unrelated refactors. No Schema v1 or applied V26 edits. Preserve P17 frozen/P18 closed/S19-1 accepted/S19-2 suspended, historical LOW/INFO/counters/logs. `PPV_DRIVE_ENABLED`, `PPV_DRIVE_FILES_ENABLED`, `PPV_FEED_SCHEDULER_ENABLED` remain false/unset in normal config; true Drive/files values are permitted **only in isolated synthetic test contexts** with controlled endpoints, never owner/live runtime.

**Engineering skills:** relevant existing `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `database-migration-review`, `backend-testing`, `rest-api-http-contracts`, `bounded-command-execution`, `documentation-writing`; `architecture-change` only for the expressly approved additive ADR/DBML/package impact, `authentication-security` for unchanged bearer/secret boundary checks. No new agent/skill or Python-RAG requirement. Independent reviewer uses `codex-final-review` and `codex-review-coverage`.

### Primary-source check (2026-11-08; read-only)

Google's [pre-generated IDs](https://developers.google.com/workspace/drive/api/guides/manage-uploads) support folder identity allocation; this application deliberately uses stricter GET-only reconciliation after dispatch. [files.list](https://developers.google.com/workspace/drive/api/reference/rest/v3/files/list) describes continuation/incomplete search, while [query terms](https://developers.google.com/workspace/drive/api/guides/ref-search-terms) confirm name-contains is prefix matching. [About](https://developers.google.com/workspace/drive/api/reference/rest/v3/about) defines account/pooled usage, and [about.get](https://developers.google.com/workspace/drive/api/reference/rest/v3/about/get) permits drive.file. [Thumbnail metadata](https://developers.google.com/workspace/drive/api/guides/file-metadata) supports credentialed short-lived links, **not** a guaranteed lh3 host. These are API-direction facts, not live account/behavior certification.

## Implementation result

`READY_FOR_OWNER_COMMIT` after [independent final re-review 3](reviews/2026-11-08-gdi26-s3-h1-b-independent-final-re-review-3.md). Decoder warning rejection now passes the previously failed private JPEG 404 regression and valid-image controls. FR-01–08/FR-L01 CLOSED; B-01–08 PASS. Fresh independent JUnit evidence is 55 H1-B + 148 S2 + 12 probes = 215 PASS; 115 prior cases are source-qualified reuse, and 10 standalone checks PASS separately. No blocking finding or mandatory unverified acceptance remains within this scope. The handback is preserved as dated implementer submission evidence; current acceptance is this review/ACTIVE. Owner publication remains pending.

## Codex remediation

H1-B Antigravity **3/3 consumed, accepted and stopped**. No further correction round requested. Takeover **NOT_AUTHORIZED / NOT_USED**; no H0/S1/S2 exception transfers.

Residual JPEG FR-04 is independently CLOSED in [re-review 3](reviews/2026-11-08-gdi26-s3-h1-b-independent-final-re-review-3.md): warning-aware bounded standard decoding rejects incomplete entropy/premature EOI and the real private HTTP negative now returns the required 404. Valid JPEG/PNG and all existing regressions/limits pass. Prior closures remain intact; implementation correction is complete. No scope/activation/later-slice authority beyond the accepted contract is added.

## Final review

**READY_FOR_OWNER_COMMIT** — [2026-11-08 independent final re-review 3](reviews/2026-11-08-gdi26-s3-h1-b-independent-final-re-review-3.md). All H1-B findings and required acceptance gates are closed/passed for this bounded slice. Historical blocked reviews/failed evidence are preserved. Owner alone performs normal staging/commit/push; brief publication closeout records the actual published SHA. No additional staging-review session or further implementation correction is required.

**Docs Sync Check (independent acceptance):** UPDATED — this executable status/result/review/counter/next action, ACTIVE and new dated re-review 3 only. ALREADY_CONSISTENT — accepted adjacent docs/finite evidence, current source provenance, preparation/decisions and OFF defaults. The handback and prior review records retain their dated input/history roles. Prior LOW/INFO/global-summary/configuration items and operational/live/later gates retain their separate disposition. No acceptance is mislabeled as Owner publication.

**Next step:** Owner | normal Git add/commit/push, then brief slice publication closeout | GDI26-S3-H1-B, current ACTIVE, this executable handoff and linked independent acceptance re-review 3 | READY_FOR_OWNER_COMMIT; closeout records actual published SHA; later slices retain separate gates | H1-B Antigravity 3/3 consumed, accepted/stopped; takeover NOT_AUTHORIZED/NOT_USED.
