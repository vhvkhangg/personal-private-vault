# BF26 — Owner deferred work: fixed Public/Private login profiles and Recycle Bin

- **Decision date:** 2026-11-11.
- **Owner disposition:** `OWNER_DECIDED / DEFERRED / NOT_AUTHORIZED_FOR_IMPLEMENTATION`.
- **Verified published baseline at drafting:** [`bbdcd3e6d7c1eac1d800f95883a5753070d72e9e`](https://github.com/vhvkhangg/personal-private-vault/commit/bbdcd3e6d7c1eac1d800f95883a5753070d72e9e) on `main`, `docs(backend): record backend-first decision and readiness assessment`.
- **Authority:** Owner's 2026-11-11 message in the PPV conversation confirms the product direction below, but **does not authorize source edits, frozen-contract changes, implementation, review execution or a migration**. Record future work only.
- **Ordering:** Prepare **Step 5 — Dashboard & Frontend API Readiness** first; defer the access-profile/privacy change and Recycle Bin browse as separate Backend work. No automatic implementation sequence is authorized merely by documenting either feature.

## 1. Owner-selected product behavior

The PPV remains a **single-Owner personal application** with **one shared database, one shared logical dataset, one set of application features and domain entities**. The proposed change is **two fixed login profiles/credentials**, not two independent human owners, users with separate data, tenants, workspaces or database schemas:

| Fixed login profile | Permitted data boundary | Intended operation |
| --- | --- | --- |
| `PUBLIC` | Public-permitted resources and actions only | Can view and modify shared Public records subject to existing business rules. No Private data or metadata disclosures. |
| `PRIVATE` | Public and Private resources and actions | Can view and modify both classes, subject to unchanged business rules and existing additional sensitive-operation controls. |

- **Private definition is fixed by resource type/module and existing NSFW flags**, **not** a globally editable Public/Private field on arbitrary records.
- **NSFW** Fiction/Film content is Private. Public-permitted Fiction/Film records remain visible to both profiles.
- **Owner personal-data modules**, expressly including **Media** and **Personal**, belong to the Private protection scope. The phrase “Media, Personal, …” is intentionally **not** treated as a complete approved module list. A concrete endpoint/resource classification table (including Journal, Finance, Notes, collections, Settings, Feed, Drive, exports, shared metadata and generated derivatives) must be reviewed before implementation. **Fail closed for PUBLIC on any ambiguous or unclassified data path in the future design**, rather than inventing Public permission.
- Shared data and functions do **not** imply equal access privileges. Both profiles refer to the same records; only the authorized profile may invoke an operation or see a result involving Private data. Owner membership of the data remains singular.
- **Fast switching:** `PRIVATE -> PUBLIC` may downgrade immediately, but must revoke/replace any active Private server/browser privilege, invalidate stale caches/in-flight requests and prevent old-token/session use. `PUBLIC -> PRIVATE` **requires the Private password**, successfully verified by the authoritative backend; no client-only flag, shared local toggle, remembered Private token or automatic elevation. The precise session/refresh-token swap protocol must be designed separately.
- Changing profile is not a live consent to expose Private data. Avoid showing private previews, cached search results, aggregate counts, export names, thumbnails, filenames or metadata after downgrading.
- The current 6-digit PIN system may ultimately be retired or repurposed **only after equivalent security/privacy requirements and migration/rollback tests are separately accepted**. No PIN API removal or existing data migration is authorized here.

## 2. Verified existing implementation and why this is nontrivial

The current published source has a singleton authentication entity and fixed JWT subject, not two independently authorized scopes:

- [`AppUser.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/domain/AppUser.java) defines `SINGLETON_ID = 1` with a single username/email/password and PIN hash.
- [`JwtTokenService.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/token/JwtTokenService.java) issues tokens using that one subject without a Public/Private profile claim.
- [`PrivatePinService.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/privatepin/PrivatePinService.java) verifies a six-digit PIN. [`AuthController.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/web/controller/AuthController.java) has PIN verify/change endpoints; success is not a persistent privilege-bearing session in the inspected service.
- [`SecurityConfiguration.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/security/SecurityConfiguration.java) uses authenticated-by-default routes. A profile-aware resource authorization mechanism must be demonstrated at service/read-model and transport boundaries, not only via frontend hiding.
- Fiction/Film already expose an `isNsfw` flag; this **does not** establish that every read/export/search/derived-media path is currently profile-filtered. In particular, source-eligibility restrictions for RAG are not a replacement for a cross-API authorization model.
- Accepted H1-AUTH Google singleton identity, BFF sessions, legacy password login/refresh, Drive's two Google account slots, and the encrypted provider credentials must remain distinct from these two new PPV login profiles. **Two Google Drive slots are not the two PPV login profiles.**

## 3. Future architecture/acceptance checklist — planning only

1. Explicitly freeze the **resource/endpoint/module-to-profile matrix**, including public summaries and non-obvious references to Private data. Scope must be fixed by source classification, not arbitrary per-record marking. Determine exact treatment of Shared/Reference, Vault identity/metadata, Search, Inbox/Radar, export/backup, file/media bytes, Drive, RAG, and direct-ID routes.
2. Define profile credentials and bootstrap/migration without creating a second domain Owner or altering ownership of existing records. Preserve the current Owner and immutable Google subject, avoid credential guessing, and handle password recovery securely.
3. Make token/refresh/session identity and effective profile authoritative, with password-verified elevation, atomic downgrade, replay-resistant refresh, logout/revocation and multi-tab/concurrent-request fencing. Explicitly test stale Private token use after downgrade or decide whether any concurrent Private session is allowed.
4. Enforce authorization in server-owned queries/operations **before** sensitive data is serialized; cover 401/403/404 non-disclosure semantics, pagination/count/ordering/ID enumeration, download/thumbnail, activity/search, exports/backup and aggregated Dashboard cards. Check that write/update actions cannot convert a Private item into a leak through an indirect public reference.
5. Verify real PostgreSQL migration and rollback/backward compatibility under frozen schemas, JWT and Google Sign-In contracts; tests must include both profiles, direct unauthorized ID access, cross-module joins, cache/session switches, stale requests and multiple devices.
6. No change to normal feature flags, real Google/Cloud activation, Frontend H1-F acceptance, or FR-13 upstream wait is authorized.

The checklist is **not an executable contract**. The exact internal account tables, full classification matrix, authentication mechanism and migration remain **TO_DESIGN / TO_REVIEW**. A separate Owner-approved pre-handoff and independent review must precede source implementation.

## 4. Recycle Bin browsing — retained as separate deferred Backend work

- Preserve **`BF26-V1-RECYCLE-BROWSE`** from the [Backend readiness assessment](backend-readiness-assessment-2026-11-11.md) as `BACKLOG / NOT_AUTHORIZED`, not a prerequisite for Step 5.
- Existing Vault API supports trash and restore **by known ID**; an authenticated paginated browse endpoint does not exist in the assessed baseline. The assessment's proposed keyset `GET /api/v1/vault/trash?limit=…&cursor=…`, metadata-only result and bounded PostgreSQL query remain a **proposal, not a frozen/approved contract**.
- Implement only after the new fixed-profile authorization contract is independently accepted **if the Owner later selects that ordering**; ensure `PUBLIC` cannot enumerate Private/NSFW trashed entries, counts, existence, titles or metadata, including across pagination. `PRIVATE` can access the permitted shared trash dataset.
- No purge, permanent delete, cascade, broader Vault rewrite, new ownership split or frontend Recycle Bin UI is authorized now.

## 5. Current priority and preservation

**ACTIVE next planning priority:** [Step 5 Dashboard & Frontend API Readiness preparation](../2026-11-11-dashboard-api-readiness-preparation/README.md), **not** implementation of either deferred item.

Preserve `BACKEND_FIRST / NO_ACTIVE_HANDOFF`; H1-F `OWNER_PUBLISHED / NOT_INDEPENDENTLY_ACCEPTED / CHANGES_REQUESTED / FRONTEND_PAUSED_BY_OWNER`; FR-13 `MEDIUM / OPEN / UPSTREAM_DEPENDENCY_BLOCKED`, H1F-L01 LOW/OPEN/DEFERRED, and all historical reviews/counters. S19-2 remains `SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY` and RAG indexing remains inactive. No source, migration, API behavior, feature flag or CI/security setting change is approved by this record.

**Docs Sync / handoff:** New dated deferred Owner decision draft only. Before publication, have the repository agent verify actual HEAD/worktree, local links, consistency with ACTIVE/maintenance index and owner policy; do not rewrite historical records. Owner alone stages, commits and pushes.
