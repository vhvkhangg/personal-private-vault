# GDI26 — Owner scope, binding choices and implementation gates

**Authority:** Owner selected D00–D03 earlier and **D04–D07=A** on 2026-11-05. The [dated decision](owner-decision-record-2026-11-05.md) is controlling for this proposed maintenance preparation. `ACTIVE=NO_ACTIVE_HANDOFF`; production implementation NOT_STARTED; preparation corrections require independent review.

## Binding selections

| ID | Owner selection | Concrete scope |
| --- | --- | --- |
| D00 | Both | App-managed Google Drive file storage plus eventual in-PPV Explorer; existing Media S3 unchanged. |
| D01 | A | `openid`, `email`, `https://www.googleapis.com/auth/drive.file`; **not** whole-Drive OAuth or permission at folder scope. Server additionally restricts to the app-created PPV folder in S2. |
| D02 | A | S1 connection authority; S2 folder/list/upload/download; S3 Explorer. |
| D03 | A | Backend-first; no frontend implementation in S1/S2 without a new exception. |
| D04 | A | Fixed secure server OAuth callback, one-use state + browser binding + PKCE, Drive-owned token encryption/persistence, minimal schema and security/module amendments, subject to Codex verification. |
| D05 | A | No delete/move/rename/share/copy, background sync or Drive→RAG; disconnect never deletes Google files. |
| D06 | A | Each Drive folder created by PPV in S2; record verified immutable folder ID, not name-based access. |
| D07 | A | Exact localhost OAuth callback first, HTTPS remote integration gated later. |

## S1: deliverables and explicit non-goals

**S1 only:** one OAuth web client for up to two Google accounts; owner-authenticated authorize-initiation for slot 1 or 2, exact browser callback guarded by server-side one-use state+PKCE+cookie, verified Google `sub` and scopes, encrypted offline refresh token, status without secrets, same-subject reconnect, disconnect and Google revoke attempt. No S1 Drive files/folders provider APIs; no Google Picker; no URL-based token passing; no modification to existing PPV JWT semantics except an **exactly matched callback permit** with its own security guard. Keep `drive.enabled=false` default.

S2 owns app-created folders and safe list/upload/download; S3 owns Explorer UI. Storage means a **Drive-owned file area**, **not MediaStoragePort or existing image storage**. Both later slices remain independently gated even when preparation describes them.

**Hard constraints:** single PPV owner ≠ OAuth client multi-tenancy; two slots and unique stable Google subjects are sufficient. An external account profile is metadata only. Media S3 object keys and backend contracts remain frozen. No scanning two entire Drives, no 5-TB content crawl, no service account impersonation, no new generic connector platform, no scheduled jobs, no RAG, no root API contract rewrite or silent frozen database changes.

## Approved narrow amendment proposal and verification burden

- New Spring Modulith `drive` top-level module, owning only its `drive_connections` relation and private OAuth/credential adapters; **no cross-module business dependency edges**. Update module matrix, package tree and the relevant architecture note **only within a future approved handoff**, not here. Verify Modulith and package-info descriptors. No dependency to `account.internal` or `authentication.internal`.
- Additive Flyway migration creates its **new table only** (exact schema in [Drive API plan](drive-api-ui-and-storage-plan.md)); leave frozen v1 DBML unchanged, record the additive delta in architecture docs. No migration is created by preparation.
- Add exact `/api/v1/drive/oauth/callback` GET permit matcher with state/cookie verification in Drive handler; all other Drive API calls require existing PPV JWT. No broad security permit or wholesale change of existing auth behavior. HTTP operation inventory grows by the deliberately reviewed new operations.
- Reuse Spring HTTP/security/validation and the existing centralized `ApiResponse` envelope; choose **one** compatible OAuth client mechanism at implementation without adding competing SDK abstractions. Resource-server dependency **does not itself implement** OAuth client callback/refresh.

## Not Owner decisions / not grounds to fake readiness

Google Cloud OAuth project setup, client ID/secret provisioning, registering an exact redirect, testing accounts/consent screen status, external token/key file permissions, app/browser cookie behavior, live network verification, migration and actual module tests are implementation/operation proof obligations; not yet performed. S2B Feed schedule activation remains independent. A design inconsistency found by Codex can require a new targeted Owner decision, but **there are no unresolved known product selections for S1**.

**Current disposition:** **READY_FOR_HANDOFF for connection-only S1**; PH-01/02/03 **CLOSED FOR PREPARATION** after [independent re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md). D00–D07 remain unchanged; runtime evidence remains NOT_RUN. GDI26-PH-L01 is LOW/OPEN and nonblocking. Implementation NOT_STARTED; remediation 0/3; no takeover.

**Next step:** Owner → Codex | Owner publishes reviewed preparation, then Codex `$codex-create-handoff` | GDI26 connection-only S1 and re-review 2 | READY_FOR_HANDOFF; verify publication before executable handoff | ACTIVE=NO_ACTIVE_HANDOFF; implementation NOT_STARTED; remediation 0/3 unchanged; no takeover.
