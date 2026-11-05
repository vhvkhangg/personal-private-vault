# GDI26 — Owner decision record (2026-11-05)

**Authority:** explicit Owner selections in the current PPV conversation on 2026-11-05. Preparation/design authorization only. This dated record supersedes the unresolved proposal statuses in the *historical initial submission*, but **does not** rewrite the [independent CHANGES_REQUESTED review](reviews/2026-11-05-gdi26-independent-pre-handoff-review.md), modify source, open `ACTIVE.md`, authorize Google API access or certify implementation. Codex must independently re-review the corrected preparation.

| ID | Explicit Owner disposition | Binding implementation boundary |
| --- | --- | --- |
| D00 | **A — BOTH** | PPV has a Drive-backed file store and an eventual in-website Explorer, **not** a replacement for existing Media S3. |
| D01 | **A — NARROW** | Two Google accounts; OAuth `drive.file` plus OIDC identity; backend limits itself to each account's PPV app-controlled folder and descendants, never whole-Drive browsing. |
| D02 | **A — THREE SLICES** | S1 connect/status/reconnect/disconnect only; S2 app-owned folder + list/upload/download; S3 website Explorer. |
| D03 | **A — BACKEND FIRST** | No Next.js/Picker frontend in S1/S2, absent separate Owner frontend authorization. Backend endpoints are not represented as completed web UI. |
| D04 | **A — APPROVE CONCRETE SECURITY DESIGN** | Server-side authorization-code OAuth with PKCE, one-use state and browser binding, exact callback; refresh token AES-256-GCM stored in a **new Drive-owned table** with external protected key file; stable Google `sub`; bounded module-matrix, Spring Security and Flyway amendments, subject to Codex review. |
| D05 | **A — DEFER** | No delete, move, rename, share/ACL, cross-account copy or background synchronization; no automated RAG ingestion. Disconnect does **not** delete third-party files. |
| D06 | **A — APP CREATES FOLDER** | PPV creates and records an opaque Drive folder ID per account **in S2**, not S1. A named pre-existing folder or manually-added external files are not silently discovered. |
| D07 | **A — LOCAL-FIRST** | Validate OAuth with an exact localhost redirect URI in controlled development; remote browser/HTTPS deployment follows its own readiness/security gate. |

**Derived technical choices, not new Owner product decisions:** for the approved D04, the corrected preparation proposes a dedicated `drive` module with no business-module dependencies, a two-slot `drive_connections` table, ephemeral one-use authorization state, fixed backend callback `GET /api/v1/drive/oauth/callback`, authenticated initiation/status/disconnect API, a config-provided key file, and fail-closed token lifecycle. These are *concrete reviewable implementations* of D04, not proof that code exists. Codex may reject a technical choice with evidence; do not infer a new Owner scope expansion.

**Items not requiring another product decision before the S1 handoff:** Google Cloud project/client provisioning, testing account registration, callback registration, exact environment variables, file permissions, live verification, OAuth publishing status, and browser/network validation are operator/implementation gates. An entirely different domain, broad Drive scopes, new frontend authority, or Drive-as-Media-S3-replacement **would** require new approval. Those remain excluded.

**Current gate:** `CHANGES_REQUESTED / CORRECTIONS_SUBMITTED_FOR_INDEPENDENT_RE_REVIEW`; PH-01/PH-02 are **ADDRESSED IN DESIGN, NOT CLOSED**. `ACTIVE=NO_ACTIVE_HANDOFF`; S1 `NOT_STARTED`; Antigravity 0/3; Codex takeover NOT_AUTHORIZED.

**Next step:** Owner | apply docs-only correction package, then Codex `$codex-pre-handoff-review` | GDI26 preparation and the original dated independent review | require new `READY_FOR_HANDOFF`, Owner publish reviewed docs before `$codex-create-handoff` | implementation 0/3, no takeover.
