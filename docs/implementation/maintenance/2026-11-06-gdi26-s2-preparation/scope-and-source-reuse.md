# GDI26-S2 — bounded requirements, source inventory and minimum delta

**Verified repository HEAD:** `1386ef07d659a69183cc651b98fba5ac6366e938`. Source descriptions here refer to inspected published S1 and are **FACT**. The new behavior is **PROPOSAL** until independently reviewed/implemented. No source changes supplied in this preparation.

## Product requirements and dispositions

| Need / mechanism | KEEP / REUSE / SIMPLIFY / REMOVE / DEFER | Evidence and proportional outcome |
| --- | --- | --- |
| Two connected Google accounts/subject identity | **REUSE** | S1 `DriveConnectionEntity` / `DriveConnectionService`, verified `google_sub`, `slot`, `credential_epoch`, no cross-slot token reuse. |
| S1 OAuth/PKCE/JWT/revoke/callback | **REUSE, preserve** | S1 FR-01–09 accepted; no callback/auth matcher or provider revocation contract relaxation. |
| One folder per subject | **KEEP S2** | Google `files.create` folder in owner's My Drive; store opaque ID, not just name; use `google_sub` binding independent of mutable slot. |
| List direct app-owned binary files | **KEEP S2** | Bounded `files.list` direct parent query, explicit fields/page token; verify caller's current subject/root and each result, never broad full-Drive. |
| Upload bounded original file | **KEEP S2** | 50MB inbound multipart max, binary `parents=[rootId]`, no native-doc conversion; Google resumable **transport** for >5MB if appropriate, without persisted resume/restart UX; bounded streaming/spooling and durable required Idempotency-Key + reserved provider-ID outcome plan. |
| Download original binary file | **KEEP S2** | Metadata authorization before `files.get?alt=media`, bounded server stream with safe attachment metadata, no browser token. |
| Source changes in authentication/account/media | **REMOVE** | S1 already owns token authority; `account` only external profile catalog, `media` owns S3 `MediaStoragePort`; no new intermodule dependency. |
| Background sync, multi-JVM service, full folder traversal, **full file catalog**, generic connector framework | **REMOVE** | Not a D00–D10 need; one process, limited direct folder listing. A *small durable upload-intent ledger* is justified solely by S2-11/lost-response duplicate prevention; it is **not** a file index. |
| Resumable session/history, Google Workspace native export, nested folders | **DEFER** | D08–D10 explicitly exclude. Failure ambiguity still needs safe handling (not feature creep). |
| Explorer UI / Google Picker, Media S3 replacement, RAG ingestion | **DEFER / forbidden here** | S3 and other gate; must not be implied by S2. |

## Verified implementation reuse

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive/internal/application/DriveConnectionService.java`: validates Owner identifier `1`, two slots, stores S1 `google_sub`/epoch, decrypts via `DriveCredentialCrypto`, uses `SubjectLifecycleManager` to fence revocation and token commitment. **Do not call its browser callback for file operations.** Add a Drive-private access-token/dispatch adapter that captures connection subject + credential epoch and **rechecks** authority; no new public token API.
- `drive/internal/oauth/DriveOAuthClient.java`: existing bounded OAuth HTTP client, validation, 2-second connect/5-second read for small responses and six-request semaphore. File/media HTTP has different streaming and 50MB requirements; **do not reuse its 64KiB response-buffer code** for file bytes. Reuse common trusted endpoint and error-redaction conventions, not unsafe body assumptions.
- `drive/internal/persistence/DriveConnectionRepository.java`: slot/epoch CAS and `findByGoogleSub`. New Drive-only root and upload-intent metadata tables can foreign-key neither a deleted connection row nor a transient slot if root survives disconnect; relation by verified Google subject plus own integrity constraints. Keep existing V25 unchanged.
- `drive/internal/config/DriveConfiguration.java`: entire S1 Drive module currently conditional on `ppv.drive.enabled=true` and lacks any file client. **Derived proposal:** file/folder handlers additionally require explicit `ppv.drive.files.enabled=true`; feature flag defaults false, validation refuses contradictory state.
- `backend/src/main/resources/application.yml`: currently multipart 50MB file/55MB request, `PPV_DRIVE_ENABLED=false` and separate `PPV_FEED_SCHEDULER_ENABLED=false`. No enlargement of global multipart limits required.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/security/SecurityConfiguration.java`: existing JWT resource server; new S2 routes are **JWT required**, no public callback exception added.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/application/storage/MediaStoragePort.java`: existing Media S3 port; no Drive adapter/substitution.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java`: existing S1 credentials excluded. Assess exclusion of new root binding as Drive integration metadata, avoid accidentally exporting subject/root identifier; no broad export format change.
- `docs/adr/0026-google-drive-connection-authority.md`, `docs/database/google-drive-connections.dbml`, `docs/architecture/module-dependency-matrix.md`: S1 additive acceptance. Future handoff should write a dated **S2 ADR/addendum**, add canonical schema documentation and relevant API/operations/package notes, and keep module graph with no new business edge. Do not edit accepted historical S1 review/ADR to pretend it approved S2.

## Proposed edit boundary for a future S2 handoff — not allowed yet

**Expected:** new Drive-internal file/folder service, provider HTTP/streaming adapter, **two minimal Drive-owned metadata relations** (subject-root binding plus bounded upload-intent ledger) with repositories/entities + **one additive** Flyway migration, Drive web endpoints/DTO/advice and focused tests; conditional config, root status-only metadata; relevant current ADR/schema/API/architecture docs. May require a narrow existing Drive service method for fresh credential epoch and safe dispatch. **Forbidden:** Account/Media/RAG/Feed changes, external OAuth scopes, new root auth matcher, renaming legacy migration, arbitrary table access, broad package refactor or root shared abstractions.

**PH-01/02/03 contract:** ensure starts with current S1 authority even when root ABSENT; `READY` is content-only prerequisite, and every root/file has mandatory ID/root-bound app-private provenance. `POST upload` requires a stable timestamped UUID Idempotency-Key and one durable provider file ID per action. No second create on repeated/unknown action; one bounded upload-intent ledger, **not** a general catalog.

**Architecture contract:** same Drive module/ownership; no new top-level module, no direct access to `authentication.internal` or `media.internal`; `portability` only its approved narrowly scoped exclusion. All inbound S2 operations authenticate PPV JWT and verify **slot -> current Google `sub` -> bound root ID -> allowed file** before remote content I/O. No SQL transaction spans Google HTTP.

**Baseline gates:** S1 224 independent focused tests PASS and 235 OFF/239 S1-ON HTTP operations are **historical source-backed facts**, not S2 acceptance. If four proposed S2 endpoints are added, the projected fully enabled total is **243**; Codex must check the actual route inventory in implementation. Preserve Drive default OFF and S1/Feed/RAG dormant boundaries.

**Next step:** Codex `$codex-pre-handoff-review` | source reuse / root and file contracts | verify KISS and no silent frozen amendment; S2 remains preparation-only.
