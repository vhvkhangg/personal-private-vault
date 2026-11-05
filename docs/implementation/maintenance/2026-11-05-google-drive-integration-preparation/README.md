# GDI26 — Google Drive Integration | Owner-decided preparation

**Date / scope:** 2026-11-05; **Owner-approved PREPARATION ONLY**, not implementation or live Google consent. **State: `READY_FOR_HANDOFF` for connection-only GDI26-S1** after [independent pre-handoff re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md). **PH-01/PH-02/PH-03 CLOSED for preparation**; no blocking preparation finding remains. One historical navigation issue, GDI26-PH-L01, is LOW/OPEN and nonblocking. The [initial review](reviews/2026-11-05-gdi26-independent-pre-handoff-review.md) and [re-review 1](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-1.md) remain immutable. `ACTIVE=NO_ACTIVE_HANDOFF`; S1 not started; remediation **0/3**, takeover not authorized.

**Source baseline:** GitHub `main` [`f4144e89ebc27b59225fd21f59dcb3dc6f1ebbf4`](https://github.com/vhvkhangg/personal-private-vault/commit/f4144e89ebc27b59225fd21f59dcb3dc6f1ebbf4), independently accepted DTR26-S2B docs publication. Feed automatic scheduler remains OFF pending its separate deployment gate.

## Decisions and bounded outcome

The Owner resolved **D00–D07** on 2026-11-05. See [dated Owner decision record](owner-decision-record-2026-11-05.md) and [canonical scope](owner-scope-and-decisions.md). Goal: connect **exactly two independently verified Google accounts**, with `drive.file` least privilege; in S2, PPV **creates and owns one folder ID per account** and restricts file operations to those folders; in S3, a browser Explorer presents those permitted files. Keep Media S3 unchanged. No arbitrary whole-Drive discovery, shared-media replacement, RAG indexing, destructive file operations or background sync.

**Three separately authorized slices:**

1. **S1 — Connection authority only:** backend OAuth authorization-code + PKCE, browser-bound single-use callback, two fixed connection slots, encrypted refresh token, connection status, reconnect, local disconnect and best-effort remote revoke. No actual file list, folder creation, upload, download, browser file management, new PPV login or RAG activity.
2. **S2 — Drive app-owned folder + file operations:** create one folder for each connected Google account, store and validate folder ID; bounded list, upload and download **of app-authorized files within that subtree**. No auto-discovery of files inserted manually via Google Drive. Must receive its own execution approval and acceptance.
3. **S3 — Website Explorer:** switch between two account folders, browse eligible items, upload/download from web. Separate Owner frontend authorization; do **not** treat a backend API or Google Picker as an already-delivered frontend.

## Read in order

1. [Owner decision record](owner-decision-record-2026-11-05.md) — explicit binding D00–D07 and scope.
2. [Owner scope and decisions](owner-scope-and-decisions.md) — product capability boundaries and exclusions.
3. [PPV source reuse / module design](source-reuse-and-design.md) — frozen-module separation and narrow amendments.
4. [OAuth and credential security design](oauth-security-and-data-governance.md) — fixed callback, key handling, state/session/revocation.
5. [Drive API, folder and persistence design](drive-api-ui-and-storage-plan.md) — concrete two-slot table and S2/S3 boundaries.
6. [Acceptance and review plan](acceptance-and-review-plan.md) — GDI26-01–16 with **S1 / future** allocation; all NOT_RUN.
7. [Decision and risk ledger](decision-and-risk-ledger.md) — owner-approved scope and outstanding runtime gates.
8. [Initial independent review (immutable)](reviews/2026-11-05-gdi26-independent-pre-handoff-review.md) — historical PH-01/02.
9. [Independent pre-handoff re-review 1 (immutable)](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-1.md) — PH-01/02 CLOSED; PH-03 MEDIUM/OPEN as of the review, before this correction.

## Preparation-only bounds and evidence

Existing backend is Spring Boot 4.1.1, Java 25, Spring Modulith 2.1.1 with PPV bearer-JWT authentication, `account` external-profile catalog, and `media` S3/disabled storage adapters. None is an OAuth token store. A new Drive-owned table/module, narrow auth callback matcher, and additive Flyway/module-matrix/package documentation are **prospective bounded amendments authorized for design review**, not implemented or certified by this package.

No application code, migration, test, provider call, Google Cloud config, OAuth token, runtime download, network permission or frontend feature is supplied. There are no Owner-product choices remaining *for this exact proposed S1*. The PH-03 remote grant revocation/reconnect ordering is a bounded **technical** correction only: subject/project-aware in-process fence, one non-retried revoke request, conservative timeout handling and a controlled provider-side revocation regression. The [OAuth contract](oauth-security-and-data-governance.md), [API plan](drive-api-ui-and-storage-plan.md), [acceptance plan](acceptance-and-review-plan.md) and [risk ledger](decision-and-risk-ledger.md) define it. No extra schema/module/framework or production permission follows. The external client/redirect configuration, secure key file and Cloud consent status are still NOT_VERIFIED **operational prerequisites**; real app/Google end-to-end and deployment proof belong to later gates.

**Docs Sync Check:** `UPDATED` — these seven preparation docs and one dated decision record; `IMMUTABLE` — prior independent review, S2B implementation reviews/closeout; `ALREADY_CONSISTENT` — `ACTIVE=NO_ACTIVE_HANDOFF`, accepted frozen Account/Media/API/RAG ownership; `NOT_VERIFIED` — all S1 runtime/cloud/security tests; `DEFERRED_OUT_OF_SCOPE` — S2/S3 implementation, old LOW/INFO, Drive live access, Feed activation.

**Next step:** Owner → Codex | Owner stages/commits/pushes the reviewed GDI26 preparation, then Codex `$codex-create-handoff` for connection-only S1 | this scope and [independent re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md) | READY_FOR_HANDOFF; verify Owner publication before handoff creation; keep ACTIVE=NO_ACTIVE_HANDOFF until that separate step | implementation NOT_STARTED; remediation 0/3 unchanged; takeover NOT_AUTHORIZED.
