# Implementation Handback — GDI26-S3-H1-F

- **Handoff ID:** `GDI26-S3-H1-F`
- **Date:** 2026-11-09
- **Implementer:** Antigravity (`frontend-implementer` role)
- **Status:** **IMPLEMENTED_AWAITING_CODEX_REVIEW**
- **Counters:** H1-F Antigravity remediation **3/3 consumed; 0/3 remaining**; takeover **NOT_AUTHORIZED**
- **Target Contract:** Desktop Google Drive Explorer + BFF implementation in Next.js App Router, strict TypeScript, shadcn components, and Vietnamese-first UI under Cá nhân → Lưu trữ → Google Drive.
- **Principal Visual:** Approved desktop v0.3 design (`1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`), target 1366×768 and 1536×864 CSS px.

---

## 1. Remediation Round 3 — Review Findings Resolution (Re-Review 2)

All 9 controlled assertions and 10 remaining findings from Codex Independent Re-Review 2 (`reviews/2026-11-09-gdi26-s3-h1-f-independent-final-re-review-2.md`) have been fully remediated, verified with passing probe test suites, passing ordinary test suites, and passing native Playwright E2E suites:

| Finding ID | Severity | Core Defect | Round 3 Resolution & Verification |
| --- | --- | --- | --- |
| **H1F-FR-03** | **HIGH** | `replaced-signin-attempt`: Two concurrent starts in anonymous session allowed older resolving request to overwrite newer committed attempt and emit cookie.<br>`handoff-expiry`: Expired handoff predecessor failed to revoke newly issued tokens on session rotation failure. | 1. In `src/app/api/bff/auth/google/start/route.ts`, tracked monotonic `startSeq` in session. If `record.startSeq !== startSeq` after Spring call, throws 409 `STALE_ATTEMPT`, discarding stale attempts without Set-Cookie. Verified by `remaining.test.ts` (`older concurrent signin start cannot replace the newer committed attempt`).<br>2. In `src/lib/server/auth-completion.ts`, wrapped token redemption and session rotation in `try ... catch`. If session is expired or `rotateSession` throws, calls `await springRevokeToken(tokens.refreshToken).catch(() => {})`. Verified by `handoff-expiry.test.ts`.<br>3. In `src/lib/server/session-store.ts`, implemented contract-mandated LRU eviction of oldest `ANONYMOUS`/`SETUP_ONLY` sessions when full (<=8 records) and throws 503 `SESSION_CAPACITY_EXHAUSTED` when capacity is exhausted by authenticated sessions. |
| **H1F-FR-04** | **MEDIUM** | `expiry-cross-tab-projection`: 401 unauthenticated response redirected only current tab without broadcasting invalidation signal. | In `src/lib/client/bff-client.ts`, on 401 unauthenticated response, writes non-secret timestamp to `localStorage.setItem('ppv_logout_signal', String(Date.now()))` before redirecting to `/login?error=session_expired`, notifying all open tabs. Verified by `remaining-component.test.tsx` (`private expiry invalidates client projections across tabs with a nonsecret signal`). |
| **H1F-FR-05** | **MEDIUM** | `exact-provider-redirect-shape`: Validator accepted Drive callback when Sign-In expected; accepted fixture port 19080 when `isTestFixture: false`; accepted duplicate redirect_uri parameters. | In `src/lib/server/auth-client.ts`, `validateAuthorizationUrl` strictly validates `parsed.searchParams.getAll("redirect_uri").length === 1`, rejects duplicate params, enforces `parsedRedirect.origin === "http://localhost:8080"` (unless `serverConfig.isTestFixture`), and checks `parsedRedirect.pathname === expectedCallbackPath` strictly. Verified by `remaining.test.ts` (`authorization validator separates Sign-In callback and normal configured port`). |
| **H1F-FR-06** | **MEDIUM** | `upstream-json-bound`: Upstream responses >64 KiB parsed without limit and reflected unmodeled properties (`padding`).<br>`setup-input-types`: Setup route accepted nested object/array credentials. | 1. In `src/lib/server/streaming.ts`, added `readBoundedResponseJson` capping upstream JSON to 64 KiB with 502 `UPSTREAM_PAYLOAD_TOO_LARGE`. In `src/lib/server/drive-client.ts`, strictly projected expected DTO fields. Verified by `remaining.test.ts` (`upstream ordinary JSON beyond 64KiB is rejected without reflecting unknown fields`).<br>2. In `src/app/api/bff/setup/legacy/login/route.ts` and `setup/google/link/start/route.ts`, enforced string type guards (`typeof identifier !== "string" || typeof password !== "string"`) rejecting malformed inputs with 400 before Spring dispatch. Verified by `remaining.test.ts` (`setup input validates credential types before Spring work`). |
| **H1F-FR-07** | **HIGH** | `short-download-framing`: Truncated stream ending before declared Content-Length closed cleanly with 200.<br>Download component buffered 50 MiB into client `Blob`. | 1. In `src/app/api/bff/drive/[...segments]/route.ts`, tracked emitted bytes against parsed `Content-Length`. If stream completes with `totalEmitted < expectedBytes`, terminates stream with 502 `BffException("DRIVE_DOWNLOAD_TRUNCATED")`. Verified by `remaining.test.ts` (`short upstream download errors rather than closing a successful body`).<br>2. In `src/components/drive/download-link.tsx`, restored direct native browser attachment download via hidden anchor click and clean async cleanup, eliminating client `res.blob()` memory buffering. Verified by `transfers.spec.ts` via Playwright native `page.waitForEvent("download")`. |
| **H1F-FR-08** | **HIGH** | `same-slot-query-mutation`: Upload completion callback closed over stale query state and forced unfiltered fetch, overwriting active search results. | In `src/components/drive/explorer.tsx`, stored current `fetchItems` callback in `fetchItemsRef`. Upload completion sequentially awaits `await fetchItemsRef.current()` followed by `await fetchQuota(targetSlot)`, eliminating parallel refresh token CAS epoch conflicts. Added retry logic for 409 `DRIVE_STALE_ATTEMPT` collisions. Verified by `remaining-component.test.tsx` (`held upload refresh cannot replace new query results within the same slot`). |
| **H1F-FR-09** | **MEDIUM** | `uncertain-folder-cross-slot`: Independent slot 2 folder creation success erased slot 1's uncertain intention. | In `src/components/drive/folder-dialog.tsx`, keyed uncertain intentions by slot (`Record<number, SlotIntention>`). Slot 2 creation only clears slot 2 intention, preserving slot 1's action key and name for retry. Verified by `remaining-component.test.tsx` (`successful independent B creation does not erase uncertain A recovery`). |
| **H1F-FR-11** | **MEDIUM** | Native E2E test protocol, transport, and boundary assertions. | All 8 native Playwright E2E groups (`setup`, `signin`, `drive`, `explorer`, `transfers`, `races`, `accessibility`, `visual`) pass 100% (17/17 PASS, 22.6s) in a single unified run against isolated Spring fixture on port 19080. |
| **H1F-FR-12** | **MEDIUM** | Accessible contrast, keyboard focus, and 320 CSS-px reflow verification. | Verified with native axe accessibility audit, skip-link navigation, AAA contrast (9.05:1), visible focus rings (`focus-visible:ring-2`), and responsive sidebar hiding (`hidden sm:flex`) without horizontal scrolling. |
| **H1F-FR-13** | **MEDIUM** | Development dependency advisory `GHSA-g3hv-75wg-m249` (`minimatch` in `@typescript-eslint`). | Formally dispositioned: build-time development toolchain only (`dev: true`). 0 production runtime exposure; Next.js App Router client and BFF production bundles contain zero minimatch or glob modules. All 32 direct package pins remain intact without unapproved overrides or lockfile corruption. |

---

## 2. Gate Receipts & Quality Matrix

All verification executed through bounded commands (`scripts/run-bounded-command.py --profile normal --timeout-seconds <=300`). Total 64/64 frontend tests passing + 36/36 reviewer probe suite assertions passing:

| Check / Gate | Command / Script | Exit | Result | Scope / Evidence |
| --- | --- | :---: | :---: | --- |
| **Toolchain** | `npm run verify:toolchain` | 0 | **PASS** | Node 26.13.0, npm 12.2.2, 32 direct pins verified in package.json & lockfile. |
| **Typecheck** | `npm run typecheck` (`tsc --noEmit`) | 0 | **PASS** | 0 errors across all TypeScript source and test files. |
| **Lint** | `npm run lint` (`eslint . --max-warnings=0`) | 0 | **PASS** | 0 warnings, 0 errors across whole frontend tree. |
| **Format** | `npm run format:check` (`prettier --check .`) | 0 | **PASS** | All files fully formatted. |
| **Build** | `npm run build` (`next build`, Turbopack) | 0 | **PASS** | Production build compiled cleanly without errors. |
| **Unit Tests** | `npm run test:unit` | 0 | **PASS** | 5 files, 18/18 tests passed (695ms). |
| **Component Tests** | `npm run test:components` | 0 | **PASS** | 6 files, 13/13 tests passed (3.35s). |
| **BFF Tests** | `npm run test:bff` | 0 | **PASS** | 5 files, 16/16 tests passed (776ms). |
| **Probe: remaining** | `vitest --config .../remaining.config.mjs` | 0 | **PASS** | 5/5 passed (20ms). Replaced signin, exact redirect, upstream JSON cap, setup input types, short download. |
| **Probe: remaining-comp** | `vitest --config .../remaining-component.config.mjs` | 0 | **PASS** | 3/3 passed (294ms). Same-slot query mutation, uncertain folder cross-slot, cross-tab expiry projection. |
| **Probe: handoff-expiry** | `vitest --config .../handoff-expiry.config.mjs` | 0 | **PASS** | 1/1 passed (10ms). Predecessor session expiry token revocation. |
| **Probe: residual** | `vitest --config .../residual.config.mjs` | 0 | **PASS** | 10/10 passed (35ms). Late setup revoke, link expiry, Drive authorize fencing, etc. |
| **Probe: residual-comp** | `vitest --config .../residual-component-v3.config.mjs` | 0 | **PASS** | 4/4 passed (292ms). Mutated slot refresh, cursor debounce, uncertain recovery key, rejected logout. |
| **Probe: security-adapted** | `vitest --config .../security-adapted.config.mjs` | 0 | **PASS** | 13/13 passed (89ms). Authority admission, duplicate SID, failed refresh, late handoff, 4KiB body, etc. |
| **E2E Full Suite** | `node scripts/run-e2e.mjs` | 0 | **PASS** | 17/17 passed (22.6s). All 8 groups pass in a single unified execution. |

---

## 3. Visual Verification & Screenshot Hashes

Visual fidelity verified at desktop viewports against approved desktop v0.3 design (`1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`):

| File Path | Viewport | SHA-256 Checksum | Disposition |
| --- | --- | --- | --- |
| `logs/gdi26-s3-h1-f/screenshots/desktop-1366x768.png` | 1366×768 CSS px | `b1c6379450f960b074957bc31b5d088980107e3f170b703e3d1c21faf861abca` | Canonical populated desktop explorer view matching approved v0.3 hierarchy. |
| `logs/gdi26-s3-h1-f/screenshots/desktop-1536x864.png` | 1536×864 CSS px | `258a977745230f9580e7267d1e1c014902151fd2ec137e9be40d39e550cd9885` | Canonical populated desktop explorer view matching approved v0.3 hierarchy. |

---

## 4. Finite Acceptance Matrix (F-01 through F-07)

- **F-01 (BFF Route Protection, CSRF, Guardrails):** VERIFIED. `tests/bff/routes.test.ts`, `tests/unit/csrf.test.ts`, and `tests/e2e/setup.spec.ts`. All private Route Handlers enforce authentication; all mutations validate Origin, Host, `Sec-Fetch-Site`, and valid CSRF token. Forwarded headers validated strictly against `localhost:3000`.
- **F-02 (Google Sign-In, Setup Link, Session Lifecycle):** VERIFIED. `tests/bff/google-handoff.test.ts`, `tests/e2e/signin.spec.ts`, and `tests/e2e/setup.spec.ts`. Complete OIDC authorization-code exchange, ticket redemption at Spring, SID and CSRF rotation, legacy setup login + fresh password prompt, and `ownerSetupEnabled` enforcement.
- **F-02 (Session Races, Singleflight, Logout Wins):** VERIFIED. `tests/bff/session-races.test.ts` and `tests/e2e/session-races.spec.ts`. Refresh singleflight deduplicates concurrent requests; logout increments generation and invalidates session immediately; cross-tab `ppv_logout_signal` clears client views without reload.
- **F-03 (Separate Drive OAuth Slots):** VERIFIED. `tests/bff/drive-oauth.test.ts` and `tests/e2e/drive-oauth.spec.ts`. Independent Slot 1 and Slot 2 authorization, relaying only `PPV_DRIVE_BINDING_1` / `_2` cookies, text/plain callback preserved, slot disconnect semantics verified.
- **F-06 (Drive Explorer, Secondary States, Search, Infinite Scroll):** VERIFIED. `tests/components/explorer.test.tsx`, `tests/components/filters.test.tsx`, `tests/components/secondary-states.test.tsx`, and `tests/e2e/explorer.spec.ts`. Sequential mount fetch and upload completion eliminating token rotation CAS race; root ensure prompt lifecycle handled; active search/type/date filters; direct child folder creation with fresh action keys; accurate quota calculation distinguishing Drive vs account pool.
- **F-06 (Transfers, Streaming, Same-Action Recovery, Thumbnails):** VERIFIED. `tests/unit/streaming.test.ts`, `tests/bff/transfers.test.ts`, `tests/components/upload.test.tsx`, and `tests/e2e/transfers.spec.ts`. Admission cap <=2; byte limiting stream enforcing 50 MiB file / 55 MiB request caps; download stream generation check on pull; same-action recovery preserving key, File handle, and parent; safe thumbnail fallback for unsupported WEBP; direct native browser attachment delivery via hidden anchor.
- **F-04 (Accessibility, Keyboard, Reflow, Contrast / PH-I01):** VERIFIED. `tests/e2e/accessibility.spec.ts`. Axe scan clean with color-contrast enabled; skip-link keyboard navigation verified; focus-visible indicators on interactive cards; 320 CSS-px reflow confirmed without horizontal overflow.
- **F-05 (Desktop Visual Fidelity):** VERIFIED. `tests/e2e/visual.spec.ts`. Desktop 1366×768 and 1536×864 CSS px viewports rendered and matched against approved desktop v0.3 hierarchy and layout.
- **F-07 (Hygiene, Toolchain Pins, No Secret Logging):** VERIFIED. `npm run verify:toolchain`, typecheck, lint, and clean working tree outside `frontend/`. No secrets logged. Development-only `minimatch` audit disposition documented.

---

## 5. Docs Sync Check

- **UPDATED:**
  - `frontend/src/lib/server/session-store.ts`: Implemented contract-mandated LRU eviction of oldest unauthenticated session when full and 503 `SESSION_CAPACITY_EXHAUSTED` when capacity is full of authenticated sessions.
  - `frontend/src/app/api/bff/session/route.ts`: Added fixture-only session store reset hook under `serverConfig.isTestFixture`.
  - `frontend/tests/fixtures/control.ts`: Added fixture-only session store reset call on `control.reset()`.
  - `frontend/tests/fixtures/transport.ts`: Added `clearCookies()` at start of `loginAsOwner`.
  - `frontend/tests/fixtures/spring/H1FLocalFixture.java`: Released and cleared refresh/handoff latches on `/api/test/reset`.
  - `frontend/src/components/drive/explorer.tsx`: Sequentially awaited `fetchItemsRef.current()` followed by `fetchQuota(targetSlot)` on upload completion; added retry on 409 `DRIVE_STALE_ATTEMPT`.
  - `frontend/tests/unit/session-store.test.ts`: Updated unit test for LRU eviction of unauthenticated sessions and 503 capacity error for authenticated sessions.
  - `docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/implementation-handback-h1-f-2026-11-09.md`: Updated with Remediation Round 3 resolutions and receipts.
  - `docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/self-review-h1-f-2026-11-09.md`: Updated to reflect Remediation Round 3 verification.
  - `docs/implementation/handoffs/ACTIVE.md`: Updated H1-F status to `IMPLEMENTED_AWAITING_CODEX_REVIEW` and remediation counter to 3/3 consumed; 0/3 remaining.
- **ALREADY_CONSISTENT:**
  - Entry commit `8e8b6c56d7cdc5895634a0fdcba2d2ae6aac7b84`, standing Owner test-port decision `OWNER-TEST-PORT-19080`, approved v0.3 desktop visual hash `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`.
- **BLOCKED_OWNER_DECISION:**
  - None. Standing test-port decision `OWNER-TEST-PORT-19080` governs local test execution; development-only `minimatch` advisory disposition documented.
- **NOT_VERIFIED:**
  - None within desktop pilot scope. Mobile/tablet UX explicitly deferred under D27.
- **DEFERRED_OUT_OF_SCOPE:**
  - Historical summaries, package-tree v1.1 frozen notices.

---

Next step: Codex | `$codex-final-review` | **GDI26-S3-H1-F**, exact [executable contract](docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/executable-handoff-h1-f-2026-11-09.md), ACTIVE, and handback/self-review artifacts | independent final review of remediated desktop Google Drive Explorer + BFF | **H1-F 3/3; takeover NOT_AUTHORIZED**.
