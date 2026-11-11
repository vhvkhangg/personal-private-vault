# Personal Private Vault — Frontend (GDI26-S3-H1-F)

Desktop Google Drive Explorer + BFF implementation for the Personal Private Vault.

## Architecture & Scope

- **Framework:** Next.js 16.4.5 (App Router, Node.js Route Handlers, server components).
- **Language & Types:** TypeScript 6.0.3 (`strict`, `noEmit`, `noUncheckedIndexedAccess`, `exactOptionalPropertyTypes`).
- **UI Primitives:** shadcn UI (release 4.23.0 adapted source), Radix UI, Tailwind CSS v4.
- **Language / Locale:** Vietnamese-first interface (`vi-VN`), local/system fonts, system typography.
- **Visual Target:** Approved desktop v0.3 design (`1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`) for 1366×768 and 1536×864 CSS px desktop viewports.
- **Backend Integration:** Connects to PPV Spring Boot modular monolith. Spring retains sole authority for authentication, Google OIDC, Drive provenance, quotas, search, actions, and thumbnails. Zero backend modifications.

## Security & BFF Boundary

1. **Volatile Sessions:** Process-local, maximum eight live sessions. Expired records are reclaimed; live records are never evicted. Exhaustion returns HTTP 429 without a new cookie or mutation of live proofs. Restart loses sessions. Host-only, HttpOnly, SameSite=Lax `ppv_dev_sid`, Path `/`, uses the approved local HTTP pilot exception.
2. **CSRF Protection:** Independent 32-byte CSRF token bound to session generation; verified on all mutations with constant-time equality.
3. **Generation Fence:** Refresh singleflight coordination and logout-wins generation fencing. Logout immediately invalidates local state; late refresh tokens cannot reinstall authority.
4. **Binary Transfers:** Raw multipart streaming, at most two admitted transfers, 50 MiB file / 55 MiB request caps. Header/body/idle/total deadlines, logout/expiry and client abort release admission and cancel upstream work. Download length is exact, identity encoded, private/no-store; a started native download is not a claim that the browser saved it successfully. The page retains at most eight File actions, each with its original bytes/name/MIME/slot/parent/key for explicit uncertain-outcome retry.
5. **Port Isolation:** Normal Spring origin remains `http://localhost:8080`. Standing Owner decision `OWNER-TEST-PORT-19080` permits isolated Spring fixture `localhost:19080`, with actual Next at `localhost:3000`. Strict Host, callback, CSRF and endpoint checks remain enabled; no real Google activation.

Google LOGIN and setup LINK share replacement-attempt ordering. Server-only `PPV_GOOGLE_SIGNIN_CLIENT_ID` and `PPV_DRIVE_CLIENT_ID` must match the separately configured Spring public client IDs; missing IDs fail authorization validation closed. Test mode selects only the controlled fixture client IDs and port, with the same fixed HTTPS Google consent endpoint and callback validation. The server-only test flag must stay off in normal operation. It enables isolated reset/expiry/resource observations for tests; it is not a product management interface. Normal setup is disabled by default.

Filters search the selected slot's eligible root and direct children via Spring `scope=all`; the browser does not filter only loaded cards. View changes fence and abort old work. Explicit listing restart replaces obsolete append admission; a late obsolete completion cannot release a newer append's admission. Cursor errors stop scrolling without automatic 409 replay. An uncertain folder creation retains its original name/key per slot across close/reopen. Success or the canonical definitive 409 outcomes `FOLDER_KEY_EXPIRED` (absent stale intent) / `FOLDER_FAILED` (FAILED_DEFINITIVE) resolve that protection. A new name edit explicitly creates a new intention/key; nothing is automatically dispatched. Unknown, pending and conflicted outcomes remain protected; an arbitrary HTTP 409 is not definitive.

## Toolchain & Verification

- **Node.js:** executable contract pin 26.13.0 (selected portable executable, not an ambient alternative).
- **npm:** 12.2.2.
- **Package Lock:** Lockfile version 3, exact direct dependency pinning across all 32 dependencies.

### Command Inventory

```bash
# Verify toolchain versions and lockfile integrity
npm run verify:toolchain

# Static checks
npm run typecheck
npm run lint
npm run format:check

# Unit, Component & BFF Test Suites
npm run test:unit
npm run test:components
npm run test:bff

# Build
npm run build

# Fixture preparation requires a new output directory (repository root invocation)
# powershell -File frontend/scripts/compile-fixture.ps1 -ArtifactDir logs/gdi26-s3-h1-f/<unique-fixture>

# Native group runner also requires a fresh artifact path and compiled fixture path
# node frontend/scripts/run-e2e.mjs --group=signin --artifact-dir=logs/gdi26-s3-h1-f/<unique-run> --fixture-dir=logs/gdi26-s3-h1-f/<unique-fixture>
# Groups: signin, setup, drive, explorer, transfers, races, accessibility, visual
```

Run potentially long commands through repository `scripts/run-bounded-command.py`, normal profile, at most 300 seconds, with a fresh receipt/stdout/stderr path for every invocation. Use the selected Node/npm in the child PATH. Group runner outputs are non-clobbering; they include actual Edge/toolchain, frontend/build/fixture source hashes, real controlled PostgreSQL/provider metadata and owned cleanup. Do not run groups concurrently on shared ports. Actual headed zoom uses the recorded native UI handshake; a resized/emulated viewport is not actual zoom proof.

Current corrections/evidence and remaining gates are in the [Codex corrective follow-up 1 handback](../docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/codex-follow-up-1-handback-h1-f-2026-11-10.md), under the [separate Owner authorization](../docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/owner-authorization-h1-f-codex-follow-up-1-2026-11-10.md). The [takeover 1 handback](../docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/codex-takeover-handback-h1-f-2026-11-09.md) and its independently blocking review remain history. FR-08/09 corrections await independent review. FR-12/PH-I01 remains mandatory NOT_VERIFIED: the permitted native client timed out before a complete viewport capture, and its documented recovery found no owned window. No fresh actual zoom PASS is claimed. FR-13 remains OPEN / UPSTREAM_DEPENDENCY_BLOCKED under the Owner's wait-for-supported-patch decision; package/lock/installed dependencies are unchanged. No acceptance, publication, blanket accessibility certification or live-provider claim follows from the local tests. A separate independent Codex session must review this implementation.
