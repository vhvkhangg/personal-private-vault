# Executable slice handoff — GDI26-S2 app-owned folder and binary files

- **Handoff ID:** GDI26-S2.
- **Status:** READY_FOR_OWNER_COMMIT.
- **Current gate:** Owner publication after the [independent remaining-verification review](reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md). GDI26-S2-FR-01 and GDI26-S2-FR-02 remain VERIFIED/CLOSED; S2-19 Media compatibility is now PASS/VERIFIED for the submitted test fixture. No blocking finding or mandatory unverified local acceptance remains. Default OFF and separate live-operation gates remain; no publication claim.
- **Classification:** Owner-approved implementation-maintenance slice; no numbered-phase, frontend or RAG authority.
- **Owner authority:** [D08–D10](owner-decision-record-s2-2026-11-06.md), inherited [D00–D07](../2026-11-05-google-drive-integration-preparation/owner-decision-record-2026-11-05.md), original handoff/initial exception and the separately recorded 2026-11-07 corrective authorization below.
- **Preparation gate:** [independent re-review 1](reviews/2026-11-06-gdi26-s2-independent-pre-handoff-re-review-1.md) = READY_FOR_HANDOFF. PH-01–03 CLOSED for preparation; PH-04 source-documented/resolved. PH-L01 was LOW/OPEN at implementation entry and is now narrowly VERIFIED/CLOSED in the independent final review.
- **Handoff-creation baseline:** [fb6994a0e83d34cb442ca4a1f1e694f17c0db1f6](https://github.com/vhvkhangg/personal-private-vault/commit/fb6994a0e83d34cb442ca4a1f1e694f17c0db1f6), then matching local HEAD/queried remote main and a clean creation entry. At handoff creation the backend/POM diff from accepted S1 b277e301 was empty. Exceptional implementation entry contained ACTIVE and this draft only; current changes are in the handback inventory.
- **Implementer:** Codex initial exceptional implementation and separate Owner-authorized corrective pass, the latter limited to FR-01/02; historical submissions/self-checks remain preserved. Current submitted Media change is test-fixture-only and was not edited by this reviewer.
- **Independent reviewer:** separate Codex reviews verify FR-01/02 and now pass the remaining 39-case Account/Media/Feed gate, including 33 Media cases, under the 2026-11-07 remaining-verification review. READY_FOR_OWNER_COMMIT; earlier blocked reviews remain preserved, with no self-acceptance.
- **Counter:** S2 Antigravity remediation **0/3 consumed**; exceptional initial Codex implementation **1 COMPLETE**; separately Owner-authorized exceptional corrective Codex pass **1 COMPLETE / independently accepted after remaining verification**. Earlier CHANGES_REQUESTED dispositions remain historical. No Antigravity round, exhaustion, new takeover or transfer from S1 is claimed.

## Exceptional Owner-authorized initial Codex takeover — 2026-11-06

The Owner explicitly authorizes Codex to implement the **complete initial GDI26-S2 executable scope, S2-01–20 and PH-01–03/04 refinements**, because Antigravity is unavailable due to repeated model-capacity/high-traffic failures. This is one bounded S2-only exception, not correction after a failed S2 review. Preserve D00–D10, PH-L01 LOW/OPEN, accepted S1/history and all OFF defaults; no live Google/Cloud/deployment, extra feature or agent Git publishing.

[Owner policy](../../../workflow/current-owner-delivery-policy.md#remediation-ownership-and-git-safety) permits an expressly authorized exceptional bounded takeover. The current Owner instruction specifically supersedes the [takeover skill](../../../../.agents/skills/codex-implementation-takeover/SKILL.md) entry condition 2 (three completed correction/re-review rounds) and correction-only/remaining-blocker wording for this **initial implementation**. Conditions 1/4 remain satisfied by this maintenance handoff and explicit authorization; condition 3's approved-scope/frozen-contract boundary remains mandatory. No nonexistent S2 findings or corrective reviews are invented. Global skill/policy and dated reviews are unchanged.

| Event | Role / disposition | Counter |
| --- | --- | --- |
| Published preparation and executable handoff | READY_FOR_IMPLEMENTATION; initial implementation NOT_STARTED at entry | Antigravity 0/3 |
| Current explicit Owner authorization, 2026-11-06 | Codex exceptional initial implementer AUTHORIZED / IN_PROGRESS, S2 only | Antigravity remains **0/3**; exceptional S2 takeover 1 |
| Required handback | Codex-authored implementation, focused evidence/docs and non-independent self-check | IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED; no self-acceptance |
| Implementation completed, 2026-11-06 | Codex exceptional initial implementation complete; 369 distinct passing cases, actual duplicate-bypass failure/restored pass and scoped self-review/docs | Antigravity **0/3**; exceptional S2 initial takeover **1**, awaiting separate review |
| Initial independent review, 2026-11-06, resumed same invocation | CHANGES_REQUESTED: 369 named cases pass; two additional real HTTP/OpenAPI assertions fail, FR-01/02 MEDIUM / OPEN. PH-L01 narrowly VERIFIED/CLOSED | Antigravity **0/3**; exceptional initial Codex implementation **1**, independently CHANGES_REQUESTED; no new takeover |

The supplied $codex-final-review skill is the **future separate-session gate**, not authority for independent review in this implementation session. Owner will open a separate Codex session after handback. Historical NOT_AUTHORIZED/Antigravity-next-route statements below retain their creation/preparation meaning and are superseded only by this explicit exception.

## Exceptional Owner-authorized corrective Codex pass — 2026-11-07

The Owner explicitly authorized **one additional bounded corrective implementation pass for GDI26-S2-FR-01 and GDI26-S2-FR-02 only**, because Antigravity remains unavailable and the repository moved to a new Windows laptop. This is a new exception to takeover skill entry condition 2; it is not automatic continuation of the initial exception and does not assert three consumed corrective rounds. Codex is the implementer and cannot independently accept these fixes in this session. The supplied `$codex-final-review` is the future **separate-session** gate.

Preserve all existing uncommitted S2 source/tests/V26/docs, D00–D10, reviewed history and OFF defaults. No startup/trust/hook repair, broader feature, real Google OAuth/Drive/Feed activation, deployment, RAG or agent Git staging/publication is authorized. Source changes are limited to partial-download progress accounting and JSON success OpenAPI annotations, their focused regressions and affected current documentation.

| Event | Role / disposition | Counter |
| --- | --- | --- |
| Explicit corrective authorization, 2026-11-07 | Codex exceptional corrective implementer AUTHORIZED / IN_PROGRESS for FR-01/02 only; same HEAD `fb6994a0e83d34cb442ca4a1f1e694f17c0db1f6` | Antigravity **0/3**; initial exceptional implementation **1**, corrective exceptional pass **1** |
| Machine recovery / fresh verification | Prior session history is not used; initial and reviewer raw evidence roots are absent on this laptop. Historical reviewed results remain recorded results, not fresh receipts. New evidence: `logs/gdi26/s2-codex-corrective-20261107T103655Z-539778d6d4904fe4824bff6e084e0557/` | No interruption or machine migration increments a counter |
| Required handback | Implementation self-review, exact new evidence and Docs Sync Check; then IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED | No self-closure of FR-01/02; separate Codex review required |
| Corrective pass completed, 2026-11-07 | FR-01/02 implementer-addressed; failed-then-passing real HTTP/OpenAPI regressions, fresh bounded verification and actual duplicate control/restoration. 339 distinct cases pass; Media suite fails initialization because the exact MinIO image is unavailable. Self-review and scoped docs complete; full verification gate remains blocked by that environment case | Antigravity **0/3**; initial exception **1** independently CHANGES_REQUESTED; separate corrective exception **1 COMPLETE**, independent review required |
| Separate independent corrective review, 2026-11-07 | FR-01/02 independently VERIFIED/CLOSED; 150 fresh independent cases pass, supplied corrective receipts/XML and all 1,842 backend inputs checked. Mandatory Media remains NOT_VERIFIED / ENVIRONMENT_BLOCKED; no READY_FOR_OWNER_COMMIT | Antigravity **0/3**; initial exception **1**, separate corrective exception **1 COMPLETE / CORRECTIONS_INDEPENDENTLY_VERIFIED**; no new round/takeover |
| Owner-invoked remaining-verification review, 2026-11-07 | Submitted existing Media fixture reviewed under SC-02; all assertions unchanged. Fresh selected Account/Media/Feed gate passes 39/39, including 33 Media cases; source-qualified complete named gate 372 positives. READY_FOR_OWNER_COMMIT; no live activation/publication | Antigravity **0/3**; initial exception **1**, corrective exception **1**, unchanged; no new implementation round/takeover |

## Goal, authority and frozen references

Deliver four private backend operations: ensure one PPV-created My Drive folder per verified Google subject, list its permitted direct binary children, upload original bytes up to the existing 50MB limit, and stream an authorized attachment download. Preserve the accepted S1 identity/token/revocation boundary and both flags OFF by default. Implementation uses deterministic fixtures/local PostgreSQL; real Google accounts, Cloud/client provisioning, consent, browser verification and operational activation remain separately gated.

Read [scope/reuse](scope-and-source-reuse.md), [folder/intent lifecycle](folder-lifecycle-and-persistence.md), [file API/I/O](file-api-and-io.md), [security/concurrency](security-and-concurrency.md), [20-case acceptance](acceptance-and-review-plan.md), [risk/operations](risk-and-operations-ledger.md), and the accepted re-review above. This handoff fixes its numeric/HTTP/schema/protocol refinements; preparation-time NO_ACTIVE_HANDOFF/NOT_AUTHORIZED labels describe their submission, while [ACTIVE](../../handoffs/ACTIVE.md) now controls execution.

Apply [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [accepted S1 ADR](../../../adr/0026-google-drive-connection-authority.md), [S1 closeout](../2026-11-05-google-drive-integration-preparation/owner-publication-closeout-2026-11-06.md), [module matrix](../../../architecture/module-dependency-matrix.md), [module boundaries](../../../architecture/module-boundaries.md), [package tree](../../../repository/repository-package-tree.md) and [frozen v1 DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml). S1 V25/ADR/history and unaffected frozen contracts stay intact. No material Owner choice is open.

## User-visible acceptance — MUST

U/V are distinct verified Google subjects; F/G are provider file IDs. Each row retains the approved S2 case ID. All evidence is future implementation proof.

| ID | Scenario/input | Required observable result | Minimal test / reusable boundary |
| --- | --- | --- | --- |
| S2-01 | Drive OFF; S1-only; both flags ON; contradictory flags | Exactly 235 / 239 / 243 operations respectively; no active file beans/egress with files OFF. Files ON + Drive OFF fails startup before provider work. Migration still applies normally. | G3 real contexts/inventory, existing conditional S1 config. |
| S2-02 | Anonymous/bad/expired JWT, forged Google bearer, wrong PPV Owner, slot 0/3/noninteger | JWT 401; invalid slot/Owner rejected; no Google dispatch. Four new routes private; S1 exact public callback unchanged. | G3 filter/controller tests; existing JWT/root envelope. |
| S2-03 | First ensure U/V with no binding | Common S1 authority admits ABSENT; one durable root identity/UUID per subject, PPV-created folder under actual My Drive, mandatory markers. No READY prerequisite/name adoption. | G1 PG + G2 fake About/generate/create/get. |
| S2-04 | Concurrent/restarted ensure; disconnect/epoch change during create | ABSENT→PENDING→READY only after full provider proof/current S1 + root CAS. One retained root/provider ID; stale response cannot confer authority. | G1 barriers/unique constraints + fake create counter. |
| S2-05 | Root create timeout/5xx/late success; missing root metadata | Same-ID read-only reconciliation; 404 never releases/reallocates ID. Content routes denied until verified READY. | G1/G2 delayed commit/repeated ensure. |
| S2-06 | Denied files.get(root) but valid v2 About; malformed/denied About; moved/trashed/wrong root | Canonical About lookup works without File-root access; wrong ID/parent/type/markers/authority fails closed, no name fallback. | G2 PH-04 fixtures and post-I/O authority barrier. |
| S2-07 | Limit 1–50, filtered/empty page, cross-slot/expired/restart-lost cursor | One bounded direct-child page, opaque subject/epoch/root/query/limit-bound cursor; no recursion/total fabrication. Empty page can have next cursor. | G2/G3 cursor/query tests. |
| S2-08 | Binary vs external/copied/native/shortcut/folder/missing-marker entries | Only jointly authorized direct binary files projected; no private markers/subject/provider URLs in response. | G2 metadata field/negative matrix. |
| S2-09 | PDF/ZIP/image/video/.md/DOCX/XLSX, including distinct actions with same name | Original bytes preserved; server filename/MIME/size validation, fixed parent/markers; no conversion/overwrite by name. | G2 real multipart/provider bytes/hash checks. |
| S2-10 | Exact 50MB edge, one byte over, 55MB multipart limit, concurrent bodies | Edge succeeds with selected Google transport; excess rejected before file mutation; bounded admission/buffers/temp bytes/cleanup. | G2 embedded HTTP and spool/stream counters. |
| S2-11 | Stable key/body retry, parallel loser, lost Google/PPV reply/restart, stale key after prune | One durable action/fingerprint/provider identity; after dispatch GET-only recovery, no G after F. Changed body/name/MIME conflicts; capacity retains uncertainty. Pre-dispatch no-live-owner recovery follows matrix below. | G1/G2 PostgreSQL + actual failed duplicate-bypass control/restored test. |
| S2-12 | Owned direct binary download | Exact attachment bytes, safe encoded filename, no-store/nosniff; no token/Google URL/JSON file bytes. | G2 real HTTP stream. |
| S2-13 | Google-native editor, shortcut/folder/manual/copied/moved-out/ID-mismatch | List omits; download/reconcile denies. No files.export, abuse bypass or external-file discovery. | G2 MIME/parent/provenance negatives. |
| S2-14 | Oversize/short/chunked/trickling/headers-only body; provider/client interruption | Byte/deadline bounds close streams/spool/admission; precommit error is JSON, committed stream abort is truncated transfer without appended JSON. No full-file buffer. | G2 real wire/deadline/resource observations. |
| S2-15 | U file ID in V slot, U file moved under V root, plausible forged provider metadata | No content exposure; root-specific UUID/ID/parent/current credential required together. Cursor/key is not authority. | G2/G3 both-subject adversarial fake. |
| S2-16 | Disconnect/regrant/revoke fence during ensure/list/upload/download | No new dispatch after local authority loss; no stale JSON success/READY/COMPLETED commit. In-flight remote mutation can remain unknown; streamed bytes cannot be undone. | G1/G3 deterministic PG/provider barriers. |
| S2-17 | OAuth invalid_grant/401, Google 403/404/429/5xx, quota, redirect, malformed response | Stable sanitized codes; same-epoch downgrade where applicable, no cross-slot fallback/blind retries or secret/body leakage. | G2/G3 controlled faults/redaction. |
| S2-18 | Apply additive schema and export | One V26 for exactly root+upload-intent metadata relations; DBML/JPA/checks/CAS agree; V25/v1 unchanged, no module edge, both excluded from Portability. | G1/G4 Flyway/Modulith/export tests. |
| S2-19 | Existing S1/Account/Media S3/Feed/RAG/frontend | S1 callback/scopes/credentials intact; no other business implementation or activation. | G4 selected S1/adjacent regression and exact diff inventory. |
| S2-20 | Read runbook/config/handback | Flags OFF, root/intent retention, recovery/body limits/partial output and actual operational unknowns accurately stated; no live certification. | G5 docs/current source and self-review. |

## Non-goals — MUST NOT

No S3 Explorer/Picker/Phase 16 UI, nested folders, shared-drive support, whole-Drive/name scan, externally inserted file discovery, Google-native export/conversion, parsing/OCR, delete/move/rename/share/copy, cross-account server transfer, Media S3 replacement, background sync/reconciler, persistent Google resumable session or resume-after-restart UX. No new module/login/public matcher/scope/token API/SDK/framework/worker/agent/hook/benchmark. No V25/v1 rewrite, broad package refactor, token/session/file-body storage in new tables or automatic compensation deletion. No RAG/QA/model/runtime download, Drive/Feed activation, real account/Cloud configuration or deployment changes. Only Owner stages/publishes.

## Simplicity decision and burden

| Decision | Selected smallest adequate implementation / effort |
| --- | --- |
| REUSE | S1 DriveConnectionService, DriveCredentialCrypto, SubjectLifecycleManager lock/fence, DriveOAuthClient bounded token transport, current repository CAS, existing JWT/ApiResponse/HTTP tests/watchdog. No second authority hierarchy. |
| KEEP | One subject-root row and one bounded upload-action ledger: root survives disconnect; same key survives loss/restart. A nullable S1 connection column would lose the binding, and an in-memory upload key cannot prove post-restart identity. These are the two approved tables, not a file catalog. |
| SIMPLIFY | Single process; one provider list page/request; short local claims/CAS, synchronous attachment streaming, limited protected spool. Explicit read-only unknown recovery, zero automatic provider retries. |
| KEEP | Direct binary operations/current source+root+marker checks and size/deadline/security proof. Upload intent/recovery is the largest work area; folder/list medium, download medium. Estimates are relative, not delivery-time promises. |
| REMOVE / DEFER | Extra SDKs, generic connector/caching/job infrastructure, durable session continuation, native export, nested/UI/RAG work. No new dependency is expected. |

Implement within one vertical slice, in authority/root → list → upload → download increments, then the finite gate/self-review/independent review. No extra active handoff or implicit feature-final acceptance.

## Four precise HTTP operations

All use existing PPV bearer JWT and require the singleton Owner. JSON uses root ApiResponse/ApiResponses with module-local DTO/advice, meta null unless genuinely used. Existing S1 routes/status bodies stay unchanged.

| Method/path / operationId | Success and changed-boundary errors |
| --- | --- |
| POST /api/v1/drive/connections/{slot}/root-folder / ensureDriveRootFolder | No body or caller parent/subject. 200 data={slot,status:"READY"} after verified create/reuse/reconcile. 409 DRIVE_ROOT_PENDING for live winner, DRIVE_ROOT_OUTCOME_UNKNOWN for unresolved creation, DRIVE_ROOT_UNAVAILABLE for invalid/inaccessible retained root. Never 2xx PENDING as usable root. |
| GET /api/v1/drive/connections/{slot}/files?limit=25&cursor=... / listDriveRootFiles | limit integer 1–50, default 25. 200 data={files:[{id,name,mimeType,sizeBytes,modifiedAt}],nextCursor,hasMore}; hasMore means provider continuation, not count/visibility guarantee. No totals/offset fiction. 400 DRIVE_INVALID_LIMIT/DRIVE_CURSOR_INVALID. |
| POST /api/v1/drive/connections/{slot}/files / uploadDriveRootFile | Exactly one multipart part named file and required Idempotency-Key. 201 data={id,name,mimeType,sizeBytes,modifiedAt} for newly verified creation; 200 same shape for verified completed/reconciled retry. Errors/matrix below. Reject caller parent/URL/account/token selection; no overwrite/update route. |
| GET /api/v1/drive/connections/{slot}/files/{fileId}/download / downloadDriveRootFile | 200 application/octet-stream attachment, safe ASCII fallback + RFC 5987 filename*, Cache-Control:no-store, X-Content-Type-Options:nosniff. No ApiResponse wrapping of bytes. Reject Range in S2 with 400 DRIVE_RANGE_NOT_SUPPORTED rather than partial-transfer promises. 404 DRIVE_FILE_UNAVAILABLE for absent/inaccessible/cross-root/marker mismatch; owned native type 415 DRIVE_FILE_NOT_BINARY; invalid ID 400 DRIVE_INVALID_FILE_ID. |

Common errors: existing JWT 401; wrong authenticated PPV Owner 403; slot invalid 400 DRIVE_INVALID_SLOT; no connection 409 DRIVE_CONNECTION_REQUIRED; RECONNECT_REQUIRED 409 DRIVE_RECONNECT_REQUIRED; existing revocation errors retained; changed epoch 409 DRIVE_STALE_AUTHORITY; non-READY content root 409 DRIVE_ROOT_NOT_READY. Admission/spool/cursor capacity 503 DRIVE_FILE_CAPACITY_EXHAUSTED. Upload bytes/request overflow 413 PAYLOAD_TOO_LARGE; unsafe filename/MIME/parts 400 DRIVE_INVALID_FILE_INPUT (Google-native requested type 415); sanitized persistence failure 500 DRIVE_FILE_PERSISTENCE_FAILURE. Non-mutating provider failure maps to 502 DRIVE_PROVIDER_FAILURE, 503 DRIVE_PROVIDER_RATE_LIMITED/DRIVE_PROVIDER_QUOTA, or 504 DRIVE_PROVIDER_TIMEOUT. Do not relay provider JSON/details/headers/Retry-After verbatim.

Google missing/inaccessible file is the same public 404; foreign key/unique conflicts do not expose subjects/markers/SQL. Errors after an ambiguous mutation take ROOT_OUTCOME_UNKNOWN or UPLOAD_OUTCOME_UNKNOWN precedence. All JSON is no-store. Binary before commitment may return existing JSON error; after commitment close/abort, never append JSON or promise new status/rollback.

S1 callback remains its accepted exact browser exception. OFF 235, S1-only 239, fully enabled 243 operations must be measured with both configuration contexts; four new operations require bearerAuth and download declares binary OpenAPI response.

## Shared authority and provider boundary

Capture Owner, slot, verified google_sub, current S1 credential_epoch/scopes and root binding identity/version. Use **the existing SubjectLifecycleManager guard** for fresh local checks/short transactions; no guard/SQL lock/transaction through Google I/O, multipart body or streamed output. Recheck before each new provider dispatch and before successful JSON/READY/COMPLETED publication. No new dispatch can linearize after disconnect/fence; a dispatch admitted before it may finish and must not resurrect authority. Tests must distinguish those orderings.

Add one Drive-private access-token lease/dispatch adapter. Decrypt accepted S1 refresh token internally, use bounded S1 refresh HTTP, retain no durable access token and expose none to callers/other modules. Verify usable bearer, expiry, exact inherited scopes and any returned signed identity's issuer/audience/time/stable subject. File-operation refresh has no new authorization nonce: provide a narrowly scoped refresh validation path; do not pass null into a helper that dereferences nonce or weaken S1 callback nonce verification. If refresh rotates the refresh token, use existing AEAD plus matching epoch CAS to publish it and return the resulting current epoch; without rotation retain epoch. invalid_grant clears only the matching current credential to RECONNECT_REQUIRED. Stale refresh never inserts/revives a deleted row. No token cache framework or cross-slot fallback.

Select only these fixed HTTPS provider operations, with trusted test replacements injected in tests, never user/env endpoint overrides:

- GET https://www.googleapis.com/drive/v2/about?fields=rootFolderId — PH-04 canonical account root lookup, under the same current subject credential. No fallback files.get(root) dependency, name scan or broader scope. Nonblank bounded opaque actual ID required; denied/malformed/oversize response is unresolved authority.
- GET https://www.googleapis.com/drive/v3/files/generateIds?count=1&space=drive&type=files — one intended provider ID, root/fingerprint binding persisted before file mutation. Unused generated IDs are not created files.
- POST https://www.googleapis.com/drive/v3/files for root metadata; GET that base with fixed query for direct list; GET /{validatedId} with fields projection for root/file proof, or alt=media for download.
- POST https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart or uploadType=resumable for binary creation; a single PUT to the validated returned resumable Location for that same operation. No files.update/export/delete, broad changes/about projection, shared-drive or abuse override.

No raw provider URL, access header, resumable URL, filename/content, key, subject, private marker or provider body in logs/receipts/JSON errors. Location is ephemeral authority: exact HTTPS origin www.googleapis.com:443, exact /upload/drive/v3/files path, no userinfo/fragment, bounded query, uploadType=resumable and nonempty upload_id. Validate parsed/normalized URI before sending Authorization; no redirect following, host suffix/encoded-path tricks or trust based on string prefix. Fake host injection is test-only. HTTP 308 is incomplete upload/unknown, not permission to redirect/retry.

### Root and file proof — all conjunctive

Resolve actual My Drive parent with About on every operation; create uses parents=["root"] but subsequent parent comparison uses the returned opaque actual ID, never literal root. Root metadata fields exactly id,mimeType,parents,trashed,appProperties. Require exact reserved root ID, folder MIME, exactly that account root parent, trashed=false, ppvKind=root, ppvRootBinding=<durable random root UUID>, ppvCreatedId=<reserved ID>. Name PPV is initial display only; renaming alone does not change identity. Existing same-name folder/copy never adopted.

File metadata/list projection includes id,name,mimeType,size,modifiedTime,parents,trashed,appProperties; reconciliation additionally md5Checksum. Require current root UUID, exact file ID, one current-root parent, not trashed, permitted binary type, sane size/name/time, ppvKind=file, ppvRootBinding matching, ppvCreatedId=id, ppvUploadAction a lowercase 64-hex action digest. Reconciliation must compare that digest with SHA-256 of canonical ASCII Idempotency-Key, plus stored size/MD5. Ordinary list/download require a well-formed action marker but not an eternally retained ledger row; terminal intent pruning must not hide old valid files.

appProperties are app-private provenance, not cryptographic attestation/Google subject fields. Assume only trusted PPV writes these keys; still enforce credential/root/ID/parent together. U's file moved into V's root fails its U binding even when provider returns plausible metadata. Validate Google's 124-byte UTF-8 key+value property limit before creation; ppvCreatedId is 12 bytes, so a generated ID over 112 ASCII bytes is incompatible and must fail before file mutation. Do not silently omit/truncate a marker.

## Root lifecycle — PH-01/04 refinement

| Durable U-root state | Explicit ensure behavior | Content behavior |
| --- | --- | --- |
| ABSENT | Common S1 gate, single live local claimant, About + generate ID, unique-insert PENDING with root UUID/epoch before one create; verify response and final S1/root CAS to READY | Deny ROOT_NOT_READY; no auto-create |
| PENDING | Existing active winner returns pending; otherwise same-ID GET-only reconciliation. Never new ID/create after persisted intent | Deny |
| RECONCILIATION_REQUIRED | Same-ID read-only proof, then CAS READY if fully authorized; otherwise unknown/unavailable | Deny |
| READY | Fresh About/root proof and current S1 gate; same binding, no recreation | Require proof plus file-specific checks |
| Any state with stale/disconnected/fenced S1 | No new mutation/successful authority; retain root metadata | Deny; retained mapping is not credential |

Root creation uses one short live claimant per subject, removed on completion; no HTTP under its mutex/SQL lock. Persist one intended ID before POST. Ambiguous response or stale final check retains PENDING/RECONCILIATION_REQUIRED; a late result cannot overwrite a newer epoch. Successful read-only reconciliation may restore READY only with matching row/current authority. Any durable state change rotates binding_epoch; routine successful re-read does not. No S2 automatic root-ID release/replacement, including after 404/403/5xx; definitive pre-allocation failure without a row is retryable because no create was permitted. A permanently unresolved/deleted root requires explicit operator investigation and separately reviewed replacement if necessary, not a silent new PPV folder.

Retain root and action metadata across S1 local disconnect without tokens; U can reconnect into either slot and reverify the same root, V cannot attach it. No FK to a deleted connection/slot.

## Upload identity, recovery and bounded retention — PH-03 / PH-L01

Key format is canonical ASCII yyyyMMddTHHmmssZ.<lowercase UUIDv4>, exactly 53 characters, real strict UTC timestamp/UUID variant. Example 20261106T090000Z.550e8400-e29b-41d4-a716-446655440000. An **absent** key must be within inclusive server-time [-600s,+120s]; missing/malformed key 400 UPLOAD_KEY_REQUIRED/UPLOAD_KEY_INVALID, old/future absent key 409 UPLOAD_KEY_EXPIRED. Look up only the current subject/root tuple; a recorded key can be retried regardless of age. New intentional upload always uses a new key.

Server normalizes filename NFC, trims outer whitespace, rejects empty/dot/path separators/control/CRLF and UTF-8 length >255 bytes. MIME is a syntactically valid lowercase base type <=127 ASCII bytes; missing/untrusted syntax falls back to application/octet-stream, Google-native MIME is refused. No content sniffing/conversion/AV claim. Zero-byte regular files are permitted via the small multipart path. Compute raw SHA-256, MD5 and actual length while boundedly spooling; never trust client digest/size.

Fingerprint = SHA-256 of: fixed ASCII GDI26-S2-UPLOAD-V1 plus NUL, u32 big-endian UTF-8 safe-name length+name, u32 big-endian normalized-MIME length+MIME, u64 big-endian actual size, then raw 32-byte content SHA-256. Persist lowercase hex fingerprint and MD5. Immutable tuple (subject,root_binding_id,key), normalized metadata/fingerprint and provider ID cannot be rewritten for another payload. Add a unique action-key constraint as well as the tuple PK so reusing the same action key for another root/subject conflicts without querying or returning another account's action.

| Existing action | Same-key/same-fingerprint Owner retry | Provider create permitted |
| --- | --- | --- |
| Absent + fresh key/capacity | After current S1/READY/root proof, unique PREPARING insert; one claimant generates ID, CAS RESERVED, then durable DISPATCHED before mutation | One first dispatch |
| PREPARING/RESERVED with live claimant | 409 UPLOAD_IN_PROGRESS, no stealing/reset/allocation | None by loser |
| PREPARING with no live owner after recovery | Claim through fresh intent_epoch CAS, reverify current S1/same root; generate/store ID only if still null | First dispatch only after RESERVED→DISPATCHED |
| RESERVED with no live owner after recovery | Claim same row/ID via epoch CAS; reverify, retain fingerprint/provider ID | First dispatch only |
| DISPATCHED / RECONCILIATION_REQUIRED, including restart/lost reply | Exact-ID GET only; full current root/file/action/size/MD5 proof gives 200 original F and CAS COMPLETED; otherwise 409 UPLOAD_OUTCOME_UNKNOWN | Never another create/session/PUT |
| COMPLETED | Reverify current S1/root/file and original metadata/checksum; 200 F, or sanitized unavailable/unknown; no file recreated | None |
| FAILED_DEFINITIVE | 409 UPLOAD_FAILED; retain original terminal record; new intentional action needs new key | None |
| Any fingerprint mismatch | 409 UPLOAD_KEY_CONFLICT before ID allocation/file mutation | None |
| Absent old key after eligible prune | 409 UPLOAD_KEY_EXPIRED | None |

This **clarifies PH-L01**: a recovered, provably never-dispatched PREPARING/RESERVED action may perform its first dispatch; a concurrent loser or any ever-DISPATCHED/ambiguous action may not. Single-process live-claim registry is bounded by active admissions; durable CAS and DISPATCHED-before-I/O establish the crash boundary. No arbitrary time-based claim stealing while a live caller exists. Initial review's LOW remains historical until independent disposition.

After any upload request could have reached Google, retain the ID. Provider 409, timeout/5xx/lost Google response/lost PPV response, crash or invalidated S1/root final gate is not success or proof of noncreation. Reconcile with GET only; 404/missing checksum/wrong proof cannot free it. Mark COMPLETED only with current credential/fence/root identity+epoch/intent CAS; an explicit retry after same-root revalidation may capture the new current binding epoch under CAS, invalidating an older in-flight claimant without changing root UUID/fingerprint/file ID. No mutation replay after dispatch even if Google documents some same-ID retries as safe; this slice selects the reviewed stricter policy.

Use at most 8 nonterminal and 1024 total retained rows per root. Under a short root-row lock, atomically check existing key before capacity/freshness, optionally prune <=32 eligible terminal rows and admit one new action. Only COMPLETED/FAILED_DEFINITIVE older than 30 days eligible; unresolved/preparing/reserved/dispatched rows never TTL-evicted. Existing-key recovery and S1 disconnect remain available at ledger capacity. Failed definitive is verifiably no upload dispatch, never inferred from GET absence. No background prune/reconciler or full file catalog.

## Numeric provider, stream, cursor and spool budgets

These are caps, not measured performance or a production-host guarantee. Test resource/deadline behavior using shorter injected budgets as well as asserting production defaults.

| Boundary | Fixed budget / behavior |
| --- | --- |
| Flags/config | ppv.drive.files.enabled / PPV_DRIVE_FILES_ENABLED=false additionally to accepted S1 OFF. Files ON without S1 fails early. Both ON require a protected external PPV_DRIVE_FILES_SPOOL_DIR; no eager Google calls. |
| File bytes | Existing Spring 50MB = 52,428,800 bytes/file; 55MB = 57,671,680 bytes/request, unchanged globally. Download metadata and actual count <=52,428,800. Reject declared excess before remote content dispatch and actual excess without forwarding it. |
| Admission | Max 2 S2 operations process-wide, fail-fast 503; upload admission before multipart parsing/spooling. All four private routes only; S1 disconnect/status/callback never waits on this permit. Retain permit through full synchronous body/cleanup, not just controller return. |
| Heap/temp | Copy/hash buffer <=64 KiB/stream, no file readAllBytes/whole-file body aggregation. Own spool max 2 files/100MiB quota including orphan owned bytes; account before parsing. Existing servlet multipart storage max 2×55MiB, own raw spool max 2×50MiB: <=210MiB temporary data plus bounded metadata. Count crash orphans/refuse unsafe/full admission, no automatic reupload; operator cleanup only verified owned paths. Delete own request temp data on success/failure/cancellation; never touch historical logs/unrelated temp. |
| Deadlines | Connect 2s; complete small-metadata body <=10s; transfer body <=120s with <=15s no-progress; each S2 operation <=180s including input/output/remaining token work. Fixed clients, zero redirects/retries. Enforce full bodies, not headers-only HttpRequest.timeout/InputStream availability; bounded owned cancellation/deadline tasks close request/provider streams and release admission on terminal cleanup. No periodic provider work/unbounded helper executor/global timeout rewrite. |
| Response/input bounds | About/generateIds <=16KiB each, individual metadata <=64KiB, list <=1MiB/50 entries; response headers <=16KiB, resumable URI <=4096 characters, incoming ID <=255 ASCII allowlisted characters, generated IDs also satisfy marker capacity. Bounded fields/string counts; malformed account-root metadata denies authority. |
| Provider calls | Per operation <=8 Drive HTTP requests total, including About/generate/list/session/PUT/reconcile/optional poststream check; one metadata generation and one create sequence at most. At most one bounded OAuth refresh via S1 (its existing 30s complete budget), no automatic retry on 401/429/5xx. Use remaining outer deadline; token rotation CAS becomes the operation's new current epoch. |
| List/cursor | One provider page per request, pageSize=requested limit <=50, fixed direct parent query and fields; no fill-page loop. Process <=50 candidates. Max 128 opaque 32-byte-random cursor handles/300s process TTL, stored Google token <=2048 bytes, bound to Owner/sub/credential epoch/root UUID+epoch/query/limit. Expired/other context/restart-lost fails 400; no raw token acceptance. Current valid read cursor can repeat after full authority recheck; no count guarantee. Expired cleanup only; full store fails 503, never silently drops continuation. |

If the selected servlet/body mechanism cannot enforce admission/deadlines without unrelated shared changes, report the concrete limitation before expanding scope. A source assertion/thread interrupt alone is not runtime timeout evidence. Actual key/temp ACL/deployment conformance remains an operational gate; tests prove the supported configured path without claiming the real host was provisioned.

### Selected upload/download wire behavior

Small files <=5,000,000 bytes: streaming Google multipart/related with fixed metadata part and spooled binary part, including empty files. Larger files: one resumable initiation POST carrying pre-generated id/parent/markers and X-Upload-Content-Length/Type, then one streamed whole-body PUT to validated session URI; no persisted URI/session, progress UI, resume probe/retry or chunk engine. Persist DISPATCHED before initiation/direct POST; a session start is already the potentially mutating boundary. Completion/create response or exact GET must yield full identity/provenance/length/MD5 proof. Invalid Location or partial PUT becomes retained unknown. Original raw bytes, no Workspace conversion.

List always queries escaped fixed validated root ID in parents AND trashed=false; projects internal proof to public five-field DTO and continuation only. Invalid/unapproved candidates are omitted; malformed whole response fails. No per-item N+1 content/proof fetch loop.

Download is synchronous bounded copy to HttpServletResponse, following fresh root/file metadata. Reject unsupported Google types before media GET. Validate media headers before committing output where possible; count actual bytes against metadata and cap, treat premature EOF/excess/mismatch as failure. Prefer bounded streaming without advertising a fabricated client length; committed failure aborts the response rather than emitting a successful terminator/JSON. Check local authority while copying/before each next dispatch, close on disconnect/deadline. Parent/content can change between Google metadata/media calls; do not promise provider-atomic snapshots or rollback of already emitted bytes.

## One additive S2 migration — precise metadata schema

Create backend/src/main/resources/db/migration/V26__drive_folder_roots_and_upload_intents.sql; V25 is latest at entry. No V1–V25/connection schema edits. If numbering/baseline drifts, inspect/report rather than overwrite.

| Relation | Required columns, keys and checks |
| --- | --- |
| public.drive_folder_roots | google_sub VARCHAR(255) PK; root_file_id VARCHAR(255) NOT NULL UNIQUE; root_binding_id UUID NOT NULL UNIQUE; status VARCHAR(32) NOT NULL CHECK PENDING/READY/RECONCILIATION_REQUIRED; binding_epoch UUID NOT NULL UNIQUE; created_at/updated_at TIMESTAMPTZ NOT NULL. Add UNIQUE(google_sub,root_binding_id) for the upload FK. Only valid nonblank bounded identities; one U row. |
| public.drive_upload_intents | google_sub VARCHAR(255), root_binding_id UUID, idempotency_key VARCHAR(53), all NOT NULL, composite PK and unique idempotency_key; request_fingerprint CHAR(64), content_md5 CHAR(32), size_bytes BIGINT, safe_name VARCHAR(255), mime_type VARCHAR(127), all NOT NULL; provider_file_id VARCHAR(255) nullable UNIQUE; status VARCHAR(32) NOT NULL; intent_epoch UUID NOT NULL UNIQUE; root_binding_epoch UUID NOT NULL; created_at/updated_at TIMESTAMPTZ NOT NULL; completed_at TIMESTAMPTZ nullable. FK (google_sub,root_binding_id) to root pair with no cascade deletion; no connection/slot FK. |

Intent CHECKs: six statuses from matrix; lowercase exact hex digests; size 0..52,428,800; safe-name UTF-8 length 1..255 and valid MIME bound; PREPARING has null provider ID, RESERVED/DISPATCHED/RECONCILIATION_REQUIRED/COMPLETED require nonblank provider ID, FAILED_DEFINITIVE may retain ID or null. Terminal statuses require completed_at (terminal-resolution timestamp); nonterminal statuses require it null. No identity/fingerprint/ID mutation once bound. Capacity/prune across rows is enforced transactionally under root-row lock, not an invalid aggregate CHECK.

JPA/Flyway/additive DBML must agree on nullability/types/statuses/CAS. No new PostgreSQL enum, full file catalog, file bodies/token/key/Google session URL columns, OAuth-table FK or worker access. Add only both table names to Portability EXCLUDED_SECURITY_TABLES, keep export allowlist/format and business dependencies unchanged. Adjust frozen-v1 physical-manifest comparisons to exclude these additive private tables while preserving 69 v1 / 67 exported application-table baselines.

## Permitted change map, source ownership and skills

| Target relative to repo | Scope |
| --- | --- |
| backend/src/main/java/com/vhvkhangg/personalprivatevault/drive/internal/files/** (new) | Cohesive application/persistence/http/web capability packages; root/intent entities/repositories, bounded cursor/spool/admission, provider stream adapter, DTO/controller/advice. Keep packages role-consistent, no speculative generic layer. |
| Existing drive/internal/config/DriveConfiguration.java, DriveProperties.java; drive/internal/application/DriveConnectionService.java; drive/internal/oauth/SubjectLifecycleManager.java, DriveOAuthClient.java; drive/internal/persistence/DriveConnectionRepository.java | Only narrow private file authority/lease/refresh/epoch/fence/config support described above. Reuse existing lock/CAS/custody, preserve S1 code/refresh/nonce/revoke API behavior. No S1 callback/controller rewrite or duplicate lifecycle map. |
| backend/src/main/resources/application.yml; new V26 migration | File subflag/spool property defaults, no global multipart increase/change to unrelated flags; two new relations only. No new POM dependency expected. |
| portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java under Java root | Only security exclusions for two integration-metadata tables; no export allowlist/format/query/framework change or Drive-internal import. |
| backend/src/test/java/com/vhvkhangg/personalprivatevault/drive/** and related fixtures; relevant architecture/schema/Portability/API tests | New focused S2 tests; retain S1 tests/negative control and extend changed-boundary assertions. Narrow fixture/support changes only. |
| Implementation-adjacent docs below; this contract/ACTIVE result and handback fields | Actual technical delta/proof/counters, no historical acceptance rewrite. |

No production change in authentication, Account, Media, Feed, RAG, root ApiResponse/ApiResponses/ApiExceptionHandler, package/module edges, unrelated shared MVC/servlet settings, scripts/skills/agents/hooks or root ZIP. Any demonstrated necessary extension outside map requires a concrete scope finding before change. No new HTTP public permit.

**Implementation scope finding SC-01 — 2026-11-06:** The first required architecture gate rejected the new `drive.internal.files.web` controller: the existing invariant requires controllers in `drive.internal.web.controller`. Preserve that invariant. Under the Owner's bounded minimum-production/docs authorization, place only the new S2 `DriveFileController` and `DriveFileDtos` in existing `drive.internal.web.controller` / `drive.internal.web.dto`; retain the filter and all file application/persistence/transport/config internals under `files`. This is a necessary placement clarification to the target map, not a new module edge, S1 callback/controller rewrite, architecture relaxation, additional feature or remediation round. Evidence: `logs/gdi26/s2-codex-takeover-1793962734749/g4-schema-export-01.json`; separate review must assess this correction with the complete diff.

**SC-02 — submitted test-fixture clarification, 2026-11-07:** The current Owner-invoked remaining-verification review includes the submitted existing `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java` container fixture/import change. It uses local `ricardbejarano/minio:RELEASE.2024-11-07T00-52-20Z` as a MinIOContainer-compatible test substitute and sets container-local HOME=/config. All test assertions/application code remain unchanged. [Independent review](reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md) verifies 33 actual Media cases plus Account/Feed, records the digest and DEVELOPMENT.GOGET identity limitation, and supersedes the earlier review's exact-old-image prerequisite for this concrete fixture only. No production Media behavior, general substitution authority, new dependency/architecture or remediation/takeover round is added.

Use java-spring-coding-standards, pragmatic-solid-design, reuse-and-consistency, modular-monolith-architecture, jpa-postgresql-persistence, backend-testing, authentication-security, rest-api-http-contracts, database-migration-review, architecture-change, bounded-command-execution; backend-integration-portability-storage only for existing export/stream conventions. design-pattern-selection only for real adapter responsibilities. Python/RAG/model/evaluation skills and additional agents are N/A.

## Five-group verification and named final gate

All S2 runtime requirements must be demonstrated, not merely described. Real PostgreSQL/Testcontainers, actual Spring security/config/HTTP multipart+binary handling, controlled signed OAuth/Drive HTTP fake with cross-account/moved-metadata faults and deterministic barriers. Do not reuse S1's 64KiB buffered body reader for file media. No H2/live credentials/Google network/model inference.

| Group | Required classes/responsibilities |
| --- | --- |
| G1 folder + PostgreSQL | DriveS2FolderIntegrationTest, DriveS2UploadIntentIntegrationTest: V26/checks/root-first state, subject uniqueness, ledger key/fingerprint/provider ID/capacity/prune, pre-dispatch recovery, stale epoch/CAS/restart. |
| G2 provider/file I/O | DriveS2ProviderTest, DriveS2StreamingIntegrationTest, DriveS2UploadRecoveryIntegrationTest: About root denial/success matrix; markers/two-account negative; both upload wire formats/original bytes/50MB boundary; Location validation; partial/trickle/timeout/header/size/spool/admission cleanup; key/response-loss duplicate control. |
| G3 auth/config | DriveS2WebIntegrationTest, DriveS2AuthorityIntegrationTest, DriveS2ConfigurationIntegrationTest: four JWT/private routes, exact flags/inventory, invalid Owner/slot/input, cursor and paused disconnect/regrant/fence no-dispatch/final-publication tests. |
| G4 architecture/export/S1/adjacent | Existing nine S1 Drive classes, SecurityFilterChainIntegrationTest, AuthWebIntegrationTest, OpenApiRouteInventoryIntegrationTest, ApplicationArchitectureTests, FlywayV1SchemaManifestIntegrationTest, PortabilityIntegrationTest; selected AccountWebIntegrationTest, MediaStorageIntegrationTest, FeedSchedulerLifecycleIntegrationTest. Current root/file module/package/permission inventory. Fresh affected S1 boundary tests; qualified unchanged evidence separately identified. |
| G5 docs/config/runbook | Dated S2 ADR, additive DBML, module/API/package/current diagrams/operations, exact config defaults, handback and /antigravity-self-review. Links, known unknowns and counters truthful. No IDE/static/live PASS without execution. |

**Actual S2-11 failed control:** same DriveS2UploadRecoveryIntegrationTest#lostResponseAndRestartKeepsOneProviderIdentity scenario/assertion, with a test-only duplicate-identity bypass (e.g. -Dgdi26.s2.test.reallocateOnRetry=true). Fake accepts F, loses Google or PPV response, restart/retry identical key/body; faulty path must actually create/attempt G and fail the assertion requiring one identity/mutation. It must not fail merely from compilation/setup/quoting or different weakened assertions. Restore guarded behavior and run the identical selected test green; normal gate also includes it. Keep both unique receipts/XML/source hashes, report control separately from pass counts. No production bypass flag, no overwrite/deletion of old failed evidence.

### GDI26-S2-focused-final

From repository root, sequential normal-profile invocations. These DriveS2-prefixed responsibility names are required selectors unless handback supplies exact equivalent cohesive class mapping. No empty selectors or omitted mandatory case. No new runner script.

~~~powershell
$gdiS2Python = (Get-Command python -ErrorAction Stop).Source
$gdiS2Run = Join-Path (Get-Location) ('logs/gdi26/s2-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $gdiS2Run -ErrorAction Stop | Out-Null
$gdiS2Groups = [ordered]@{
    'g1-pg' = 'DriveS2FolderIntegrationTest,DriveS2UploadIntentIntegrationTest'
    'g2-provider' = 'DriveS2ProviderTest'
    'g2-io-recovery' = 'DriveS2StreamingIntegrationTest,DriveS2UploadRecoveryIntegrationTest'
    'g3-auth' = 'DriveS2WebIntegrationTest,DriveS2AuthorityIntegrationTest,DriveS2ConfigurationIntegrationTest'
    'g4-s1' = 'DriveApiInventoryIntegrationTest,DriveConfigurationIntegrationTest,DriveCredentialCryptoTest,DriveGoogleOAuthClientTest,DriveOAuthSecurityTest,DriveGrantRevocationIntegrationTest,DriveProviderRegressionTest,DriveWebIntegrationTest,DrivePersistenceIntegrationTest'
    'g4-security-api' = 'SecurityFilterChainIntegrationTest,AuthWebIntegrationTest,OpenApiRouteInventoryIntegrationTest'
    'g4-schema-export' = 'ApplicationArchitectureTests,FlywayV1SchemaManifestIntegrationTest,PortabilityIntegrationTest'
    'g4-adjacent' = 'AccountWebIntegrationTest,MediaStorageIntegrationTest,FeedSchedulerLifecycleIntegrationTest'
}
foreach ($gdiS2Group in $gdiS2Groups.GetEnumerator()) {
    $gdiS2Started = [DateTime]::UtcNow
    & $gdiS2Python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $gdiS2Run ($gdiS2Group.Key + '.json')) -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' "-Dtest=$($gdiS2Group.Value)" test
    $gdiS2Exit = $LASTEXITCODE
    $gdiS2XmlDir = Join-Path $gdiS2Run ($gdiS2Group.Key + '-surefire')
    New-Item -ItemType Directory -Path $gdiS2XmlDir -ErrorAction Stop | Out-Null
    $gdiS2Expected = $gdiS2Group.Value.Split(',')
    $gdiS2Evidence = @()
    $gdiS2Stale = @()
    foreach ($gdiS2Report in Get-ChildItem -LiteralPath 'backend/target/surefire-reports' -Filter 'TEST-*.xml' -File -ErrorAction SilentlyContinue) {
        $gdiS2Class = $gdiS2Report.BaseName.Split('$')[0].Split('.')[-1]
        if ($gdiS2Class -notin $gdiS2Expected) { continue }
        if ($gdiS2Report.LastWriteTimeUtc -lt $gdiS2Started) {
            $gdiS2Stale += $gdiS2Class
            continue
        }
        Copy-Item -LiteralPath $gdiS2Report.FullName -Destination $gdiS2XmlDir -ErrorAction Stop
        $gdiS2Xml = [xml](Get-Content -LiteralPath $gdiS2Report.FullName -Raw)
        $gdiS2Evidence += [pscustomobject]@{
            class = $gdiS2Class
            suite = [string]$gdiS2Xml.testsuite.name
            tests = [int]$gdiS2Xml.testsuite.tests
            failures = [int]$gdiS2Xml.testsuite.failures
            errors = [int]$gdiS2Xml.testsuite.errors
            skipped = [int]$gdiS2Xml.testsuite.skipped
            xmlSha256 = (Get-FileHash -LiteralPath $gdiS2Report.FullName -Algorithm SHA256).Hash
        }
    }
    [pscustomobject]@{ exit = $gdiS2Exit; startedUtc = $gdiS2Started; selected = $gdiS2Evidence; stale = $gdiS2Stale } |
        ConvertTo-Json -Depth 5 |
        Set-Content -LiteralPath (Join-Path $gdiS2XmlDir 'selected-suites.json') -Encoding UTF8
    if ($gdiS2Exit -ne 0) { throw ('GDI26-S2 command failed: ' + $gdiS2Group.Key) }
    $gdiS2Missing = @($gdiS2Expected | Where-Object { $_ -notin $gdiS2Evidence.class })
    if ($gdiS2Missing.Count -gt 0 -or $gdiS2Stale.Count -gt 0) { throw ('Missing/stale selected XML: ' + $gdiS2Group.Key) }
    if (@($gdiS2Evidence | Where-Object { $_.tests -lt 1 -or $_.failures -gt 0 -or $_.errors -gt 0 -or $_.skipped -gt 0 }).Count -gt 0) {
        throw ('Nonpassing selected suite: ' + $gdiS2Group.Key)
    }
}
~~~

The gate snapshots **selected freshly written** XML before another Maven call and records timestamps/counts/class inventory/hashes in selected-suites.json; it rejects missing/stale/skipped evidence rather than assuming a reportsDirectory override worked. Run the failed/restored control separately with the same selection/snapshot discipline. A reviewer/implementer may execute these invocations individually or split a long selector, preserving exact case mapping. Report a nonzero promptly and preserve that failed run.

No full-backend suite is authorized/required by this gate. Existing accepted B1/B2: normal cap 300s, at most 2100s only for a separately approved full-suite-shaped gate; inactivity diagnostics/owned-tree cleanup and supported native Windows repository-root/trust limitations retained. Split expensive groups within 300s rather than bypass/extend. S1 accepted evidence root logs/gdi26/codex-s1-independent-takeover-review-1793950183141/ (224 tests) is historical. Reuse unchanged adjacent evidence only after checking current source/config/dependency/test input hashes and explaining applicability; it is never fresh S2 execution. Changed private credential/fence/root/API/schema paths need fresh relevant tests.

Handback actual commands/exits/counts/source manifests, selected XML and receipt/stdout/stderr paths, resource observations, control failure/restoration, reused vs fresh evidence and every S2-01–20 disposition. A missing mandatory case or skipped required assertion blocks acceptance; no inflated duplicated counts.

## Documentation impact and editor

Antigravity is explicitly authorized for these implementation-adjacent updates only, reflecting actual code. Preserve dated Owner records/initial and re-review/preparation/source evidence and S1 review/ADR/closeout; new statements distinguish planned/submitted/independently reviewed/published.

| Affected current/new artifact | Required sync proof |
| --- | --- |
| docs/adr/0027-google-drive-root-file-authority.md (new), docs/adr/README.md | D00–D10/review authority, two-table amendment/root retention/keys/recovery/flags/limits; no retrospective S1 approval of S2. |
| docs/database/google-drive-files.dbml (new), docs/architecture/data-architecture.md | Exact V26/JPA columns/keys/checks/epochs/retention and security export exclusions, additive alongside unchanged v1/V25. |
| docs/architecture/google-drive-files.md (new), docs/architecture/api-architecture.md, docs/architecture/module-boundaries.md, docs/architecture/module-dependency-matrix.md, docs/repository/repository-package-tree.md | Four private operations/binary exception/235-239-243/current module ownership and recursive actual changed packages; no new edge or public token contract. Do not repair unrelated S1 LOW citations/descriptors by stealth. |
| docs/architecture/google-drive-file-operations.md (new), docs/architecture/README.md, narrow link from google-drive-connection-operations.md | Both flags OFF; protected spool/quota/cleanup, Root/intent metadata retention at disconnect, key reuse vs new action, pre-dispatch recovery vs post-dispatch read-only unknown, partial streams/limitations; actual Cloud/browser/key ACL/one-JVM/HTTPS remains NOT_VERIFIED and separately authorized. |
| docs/architecture/structurizr/workspace.dsl and only directly affected current sources/exports under docs/architecture/diagrams | Extend existing Drive behavior/persistence descriptions only where the current view changes; source before its corresponding export, list exact applicability/paths. No new visual framework or dated Archify/S1 snapshot regeneration/unrelated badge cleanup. |
| This directory's implementation-handback-s2-2026-11-06.md (new), self-review evidence, executable/ACTIVE result fields | Actual changed targets, S2 mapping/receipts/control/resources/limits and next command; no independent acceptance or invented publication SHA. |

Codex creation writes only this executable contract and ACTIVE. ChatGPT owns routine governance/closeout/current-notice cleanup after actual independent acceptance/Owner publication. Preparation NO_ACTIVE labels are historical; PH-L01 is clarified here pending review disposition. S1 FR-L01/02/PH-L01, submitted diagram badges, legacy AGENTS/policy/agent notices and unrelated LOW/INFO stay DEFERRED_OUT_OF_SCOPE.

Primary provider facts rechecked on 2026-11-06: [v2 About scopes](https://developers.google.com/workspace/drive/api/reference/rest/v2/about/get), [rootFolderId](https://developers.google.com/workspace/drive/api/reference/rest/v2/about), [pre-generated IDs](https://developers.google.com/workspace/drive/api/reference/rest/v3/files/generateIds), [upload transports/IDs](https://developers.google.com/workspace/drive/api/guides/manage-uploads), [private-property limits](https://developers.google.com/workspace/drive/api/guides/properties), [binary downloads](https://developers.google.com/workspace/drive/api/guides/manage-downloads). These support feasibility, not a live provider/account result.

## Definition of done, handback and blockers

1. Implement the exact permitted vertical slice/defaults; no broader feature/schema/dependency change.
2. All S2-01–20/five groups pass with actual selected fresh evidence or narrowly qualified unchanged reuse, including real failed duplicate control/restored green case. No mandatory runtime unknown remains within deterministic acceptance.
3. Run /antigravity-self-review; fill the implementation handback/result and Docs Sync Check, actual source/module/API/schema/resource delta, evidence and remaining finding IDs.
4. Set executable and ACTIVE to IMPLEMENTED_AWAITING_CODEX_REVIEW; next independent Codex $codex-final-review. Do not stage/commit/push/tag/open/merge PRs.
5. CRITICAL/HIGH/MEDIUM and unverified mandatory cases block; LOW/INFO recorded without correction loops. At most three Antigravity correction + independent re-review rounds on this handoff; stop on acceptance. No automatic takeover, transfer of S1's exception or counter reset.
6. Only after READY_FOR_OWNER_COMMIT does Owner publish; ChatGPT writes a brief actual-SHA closeout. Later S3/feature-final aggregation/operational activation require their own authorization.

No blocking preparation choice remains. If fixed provider/provenance/size or bounded servlet/transport contract cannot be met within permitted targets, stop with reproducible counterexample/smallest technical decision; do not widen scopes, replay unknown mutations or silently waive proof. Missing real accounts/Cloud/browser/key ACL does not block deterministic S2 work but prevents any live/deployment claim.

Preserve P17 FROZEN, P18 CLOSED, S19-1 accepted/published, S19-2 SUSPENDED/NOT_ACCEPTED/ARCHIVAL_ONLY; external indexing INACTIVE, live originals DISABLED/DEFERRED and Feed OFF. S1 Antigravity 0/3 plus one independently accepted exceptional Codex takeover remain separate historical facts.

## Interrupted-session recovery checkpoint — 2026-11-06

Recovered the last completed scope-specific Docs Sync Check from reviews/2026-11-06-gdi26-s2-independent-pre-handoff-re-review-1.md, published in entry fb6994a0e83d34cb442ca4a1f1e694f17c0db1f6. The partial draft's creation checkpoint was provisional: ACTIVE had not yet been registered. Read COMMON.md, the documentation-synchronization contract, current AGENTS/ACTIVE/Owner scope/review and the partial executable source before continuing; no fresh task or scope was inferred.

| Step | Verified recovery classification / final disposition |
| --- | --- |
| Owner scope, readiness and publication | COMPLETED / REUSED: D00–D10 and READY_FOR_HANDOFF unchanged; fresh remote main and local HEAD both fb6994a0; no conflicting live handoff. |
| Partial executable contract/evidence command | COMPLETED / RETAINED: one untracked S2 draft existed; its finalized selected-XML collection was present. No duplicate handoff/review or lost-file reconstruction. |
| ACTIVE registration and document validation | PENDING at resume, now COMPLETED within these two authorized files. |
| Invalidated by source drift / blocked | NONE / NONE: no HEAD, AGENTS, backend or agent-source drift; no material Owner decision open. |
| Interrupted execution / receipts | No S2 build/test/gate command was launched; scoped S2 receipt directories absent. No owned running command required cleanup or rerun; historical logs untouched. Short documentation checks completed, including the code-fence-aware link validator. |
| Counters and role | Same handoff-creation role; S2 remediation 0/3, takeover NOT_AUTHORIZED. No implementation or independent runtime acceptance. |

**Focused creation verification:** PASS — 31 local Markdown links across executable/ACTIVE resolve; 20 acceptance rows, five proof groups/eight bounded command groups; 18 existing test selectors resolve, eight S2 selectors explicitly prospective; exact gate/evidence-collection PowerShell parses without execution; fences/whitespace/status/counters pass, git diff --check passes, and Git status contains exactly the two authorized documents. These are documentation results, not S2 application-test evidence.

## Historical implementation result — Codex exceptional initial implementer, 2026-11-06

IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED. All S2-01–20 implementer-addressed with 145 passing S2 cases plus 224 S1/security/schema/adjacent cases (369 distinct; repeated inventory/control excluded). The duplicate-bypass control actually created G after accepted F and failed at 2 mutations versus 1; restored same regression passes with one ID/mutation. [Handback](implementation-handback-s2-2026-11-06.md) records exact changed paths, commands, fresh XML/source qualifications, failed development runs, resource cleanup and remaining LOW/NOT_VERIFIED items. V26 only, no module edge/new dependency or unrelated production behavior; defaults OFF. Saved-state verification: logs/gdi26/s2-codex-takeover-1793962734749/final-checks-02.json. No live Google/deployment or independent acceptance is asserted.

## Historical Codex remediation — independent review, 2026-11-06

**CHANGES_REQUESTED.** Antigravity remains **0/3 consumed**; the one exceptional **initial** Codex implementation is complete and independently CHANGES_REQUESTED. No corrective/re-review cycle has completed. PH-L01 is independently VERIFIED/CLOSED for pre-dispatch recovered work versus live losers and ever-dispatched uncertainty; S1 LOW/history remains unchanged.

| Finding | Required bounded correction / focused regression |
| --- | --- |
| GDI26-S2-FR-01 — MEDIUM / OPEN | DriveFileController must observe actual partial-read progress within its bounded accumulation, retaining EOF/final-chunk/authority/deadline/cleanup guarantees. A healthy 100-byte body arriving every 50ms must complete under the 15s transfer budget despite taking longer than the 2s idle budget; retain true-stall/excess-duration/size/disconnect cases. |
| GDI26-S2-FR-02 — MEDIUM / OPEN | Describe ensure/list/upload JSON success responses as the existing ApiResponse envelope and actual Root/Page/File DTOs, with correct application/json and 200/201 status contracts. Assert resolved generated schemas/fields and exclude async task internals; retain inventory/security/binary/callback checks. |

Exact source locations, reproducible evidence, consequences and minimal corrections remain in the preserved [independent review](reviews/2026-11-06-gdi26-s2-independent-final-review.md). The Owner subsequently explicitly assigned the separate corrective Codex exception above; its [handback](implementation-handback-s2-2026-11-06.md#exceptional-corrective-pass--2026-11-07) now submits only these fixes for independent review. No full-backend rerun, unrelated LOW cleanup or production redesign is required.

## Historical final review — independent disposition, 2026-11-06

**CHANGES_REQUESTED**, 2026-11-06. Fresh independent named-gate verification: **369 passed, 0 failures/errors/skips** (145 S2 + 224 compatibility). Actual duplicate-bypass control failed at two mutations versus one, then the restored same regression passed. Two additional reviewer-only real HTTP/OpenAPI assertions failed genuinely, proving FR-01/02; these are separate from the 369 positive cases and expected control. Evidence: logs/gdi26/codex-s2-independent-review-1793968776298/. No READY_FOR_OWNER_COMMIT or commit message.

## Historical corrective implementation submission — 2026-11-07

**IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED.** Codex is the corrective implementer. FR-01/02 remain MEDIUM / OPEN for separate adjudication. Only the S2 controller, streaming/web regressions and four affected current docs changed in this pass. D00–D10, the original independent review, existing uncommitted migration/implementation, OFF defaults and frozen modules are preserved.

Fresh evidence: `logs/gdi26/s2-codex-corrective-20261107T103655Z-539778d6d4904fe4824bff6e084e0557/`; exact selectors/exits/counts/XML/hashes and new-machine qualifications are in the [corrective handback](implementation-handback-s2-2026-11-06.md#exceptional-corrective-pass--2026-11-07). Before the fixes, three real HTTP/OpenAPI assertions fail; afterwards they pass. All 148 S2, 113 S1, 17 security/API and 55 schema/export cases pass, plus Account 1 and Feed 5: **339 distinct passing cases**. The adjacent command exits **1** with a Media initialization error: the exact `minio/minio:RELEASE.2024-11-07T00-52-28Z` image is absent and pulling returns 404/pull access denied. No Media assertion executed; this mandatory compatibility case remains **NOT_VERIFIED / ENVIRONMENT_BLOCKED**, without a waiver or image/source substitution. The actual duplicate-bypass control fails at 2 mutations versus 1; the identical restored case passes, separately counted.

Historical initial/reviewer raw evidence directories are unavailable on this laptop; dated reviewed documents retain their recorded results without being counted as fresh evidence. Java 25, Docker/PostgreSQL Testcontainers and configured project/hook trust were checked before editing. Hook unit tests/read-only execpolicy checks pass; current-client native interception and complete IDE lint remain NOT_VERIFIED, not claimed from old-machine receipts. No startup/config/trust repair or safety bypass.

## Historical blocked independent corrective review result — 2026-11-07

**CHANGES_REQUESTED / ENVIRONMENT_BLOCKED.** The [separate independent review](reviews/2026-11-07-gdi26-s2-independent-corrective-review.md) verifies and closes GDI26-S2-FR-01 and GDI26-S2-FR-02. Fresh HTTP/streaming/authority/config/security/OpenAPI tests pass **95** cases; architecture/schema/export passes **55**: **150/150**, zero failures/errors/skips. Evidence: `logs/gdi26/s2-independent-corrective-review-1794050210348/`. All 24 files-subtree Java files plus controller/DTO were inspected; all 1,842 named corrective-verification backend inputs match. Supplied 339 passing cases, one Media initialization error, genuine pre-fix failures and actual failed/restored duplicate control were audited from their receipts/XML, not counted as additional fresh independent executions.

The exact approved `minio/minio:RELEASE.2024-11-07T00-52-28Z` image remains absent locally, independently checked. Mandatory S2-19 Media assertions remain NOT_VERIFIED; the supplied failed initialization/pull evidence is preserved. No blind repeated pull, substitute/retag, Media/source/registry/configuration repair or waiver. Owner must supply that image before the expressly test-only remaining `AccountWebIntegrationTest,MediaStorageIntegrationTest,FeedSchedulerLifecycleIntegrationTest` gate and final adjudication. No code correction remains for FR-01/02; Antigravity stays 0/3, initial exception 1 and separate corrective exception 1. No READY_FOR_OWNER_COMMIT or commit message.

**Docs Sync Check (independent corrective review, 2026-11-07):** UPDATED — this current review/status ledger, ACTIVE and new independent report. ALREADY_CONSISTENT — checked receipt/XML/source applicability, D00–D10, OFF defaults, approved schema/package/API/operations structure and preserved handback/initial review/history. DEFERRED_OUT_OF_SCOPE — prior LOW/badges, legacy notices, unrelated RAG/config drift and cleanup. BLOCKED_OWNER_DECISION / ENVIRONMENT — exact approved MinIO image availability before mandatory test-only verification; no production correction requested. NOT_VERIFIED — Media, missing historical raw roots, complete IDE/current-client native interception and separately gated provider/host/deployment/diagram-tool proof. Saved-state source/protected-document/link/receipt checks are in the new evidence root. The following implementation and 2026-11-06 checkpoints remain historical.

**Docs Sync Check (corrective implementation, 2026-11-07):** UPDATED — this authorization/result ledger, ACTIVE, handback and current file API/I/O description. ALREADY_CONSISTENT — D00–D10, preserved independent review/PH-L01 closure, V26/DBML/package/module/operations boundaries and OFF/frozen history. DEFERRED_OUT_OF_SCOPE — prior LOW/badges/legacy notices, unrelated RAG/config drift and cleanup. BLOCKED_OWNER_DECISION — none for these fixes; exact approved MinIO image availability is required to rerun the blocked mandatory gate. NOT_VERIFIED — Media compatibility on this machine, absent historical raw artifacts, complete IDE lint/current-client native interception, actual provider/Cloud/browser/key/spool ACL/topology/deployment and formal diagram-tool validation. Saved-state consistency is checked after transition; this is not independent acceptance. The following 2026-11-06 checks are historical.

**Docs Sync Check (resumed independent review, 2026-11-06):** UPDATED — this disposition/remediation ledger, ACTIVE and new independent review. ALREADY_CONSISTENT — same HEAD, 1,841 backend hashes, four protected Owner/review/submission hashes, completed receipts, scoped source/schema/API/package/operations/diagram checkpoint and OFF/frozen boundaries. DEFERRED_OUT_OF_SCOPE — S1 LOW/badges/legacy notices and repository cleanup B. BLOCKED_OWNER_DECISION — none for the two minimal fixes; further exceptional corrective staffing needs separate Owner authorization if Antigravity remains unavailable. NOT_VERIFIED — actual provider/Cloud/browser/key/spool ACL/topology/deployment and formal diagram-tool validation. Historical checkpoints below retain their creation/submission meaning.

**Docs Sync Check (implementation):** UPDATED — in-scope ADR/schema/API/package/runbook/current diagram descriptions and implementation handback/ACTIVE. ALREADY_CONSISTENT — D00–D10, S1/frozen history, no new business edge, OFF flags and source protections. DEFERRED_OUT_OF_SCOPE — historical LOW/badges/legacy notices/S3. BLOCKED_OWNER_DECISION — none known. NOT_VERIFIED — real Google/Cloud/browser/key/spool ACL/one-JVM/HTTPS/deployment and formal Graphviz/Structurizr validation; see handback for exact evidence and SC-01 placement correction. This is not independent acceptance.

**Docs Sync Check (resumed creation):** UPDATED — retained executable/ACTIVE, recovery classification, validated creation evidence, approved future doc ownership and PH-04/PH-L01 refinements. ALREADY_CONSISTENT — published READY_FOR_HANDOFF, clean original entry/no competing live handoff, unchanged S1/backend/AGENTS/Owner scope and same 0/3 counter. DEFERRED_OUT_OF_SCOPE — preparation-era labels, historical S1/LOW/governance/badge cleanup and S3/activation; ChatGPT handles later authorized governance/closeout. BLOCKED_OWNER_DECISION — none. NOT_VERIFIED — all implementation/tests/resources/static/IDE and actual provider/Cloud/browser/key/deployment; future docs remain targets. No production/test/log/historical evidence or publication change.

## Current independent remaining-verification acceptance — 2026-11-07

**READY_FOR_OWNER_COMMIT**, [independent review](reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md). Fresh `AccountWebIntegrationTest,MediaStorageIntegrationTest,FeedSchedulerLifecycleIntegrationTest` passes **39/39**, including **33 Media cases**, exit 0 in 94.703s under the existing normal 300s watchdog. Evidence: `logs/gdi26/s2-media-verification-review-1794054707906/`. SC-02 is the only backend change since the prior independent review; assertions/production are unchanged and source inputs remain stable throughout verification. FR-01/02 stay VERIFIED/CLOSED. Revalidated retained evidence plus the new Media cases establish **372 distinct named-gate positives**; **189 distinct cases** were executed across the two independent review invocations, **39 fresh here**. No full-backend run or duplicate count inflation.

The former mandatory Media blocker is resolved by executed compatibility proof for the recorded local image digest. Its binary reports DEVELOPMENT.GOGET; no official-release or production-provider identity certification follows. Prior failed probes/initialization, blocked reviews, implementation handback and counters remain preserved. Both Drive flags and Feed remain OFF; actual Google/Cloud/browser/host/deployment and formal diagram-tool proof remain separately NOT_VERIFIED. Owner alone publishes.

**Docs Sync Check (remaining-verification acceptance, 2026-11-07):** UPDATED — current READY/result/SC-02 ledger, ACTIVE/new review and directly affected S2 acceptance labels in ADR/index/API/data/file/operations/package docs. ALREADY_CONSISTENT — D00–D10, unchanged assertions/production/schema/module/defaults, source-qualified prior reviews/controls and historical failures. DEFERRED_OUT_OF_SCOPE — prior LOW/badges/legacy notices, unrelated RAG/config drift and cleanup. BLOCKED_OWNER_DECISION — none within accepted local scope. NOT_VERIFIED — image official-release identity and separately gated provider/host/deployment/repository IDE/native-hook/diagram-tool proof. Saved-state checks in the new evidence root verify protected history/source/links/receipt capture/cleanup and no agent publishing.

**Next step:** Owner | normal Git add/commit/push of accepted GDI26-S2, then ChatGPT brief slice closeout | this executable contract and independent remaining-verification review | READY_FOR_OWNER_COMMIT; closeout uses the actual published commit | Antigravity 0/3; exceptional initial implementation 1, corrective pass 1, unchanged.

