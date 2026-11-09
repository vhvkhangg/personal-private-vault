# Implementation handback — GDI26-S3-H1-AUTH Google Sign-In backend

- **Handoff ID:** `GDI26-S3-H1-AUTH`
- **Date:** 2026-11-09
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Implementer:** Antigravity `/antigravity-implement-handoff`
- **Reviewer:** independent Codex in separate session, `$codex-final-review`
- **Remediation rounds consumed:** 3/3 consumed (final Antigravity round); takeover NOT_AUTHORIZED
- **Takeover status:** NOT_AUTHORIZED

---

## 1. Executive Summary

H1-AUTH backend deliverables for Spring-owned Google Sign-In and singleton Owner identity linking have been remediated in Remediation Round 3 (3/3 consumed, final Antigravity round), addressing both remaining open findings from Codex Re-Review 2 (`AUTH-FR-09` and `AUTH-FR-11`), while preserving all ten previously verified and closed findings (`AUTH-FR-01` through `AUTH-FR-08`, `AUTH-FR-10`, `AUTH-FR-12`, `AUTH-L01`, `AUTH-L02`).

All 5 exact verification batches specified in the executable contract (231 tests total) pass cleanly under bounded command execution with zero failures, zero errors, and zero regressions across frozen authentication, Drive, and portability boundaries.

### Detailed Resolution of Re-Review 2 Findings

1. **AUTH-FR-09 (MEDIUM) — Absent Authority Port Validation & Exact Port Match:**
   - In `GoogleSignInController.validateHost`:
     - When `Host` header lacks a port (e.g. `Host: localhost`), port parsing now derives the scheme default port (`request.isSecure() ? 443 : 80`), rather than leaving `port = -1` and bypassing the port check.
     - Port validation strictly enforces `if (port != allowedPort)` on all paths (production policy is literal 8080), rejecting `Host: localhost` (implied port 80 != 8080) with 400 Bad Request.
     - Fallback to `request.getServerName()` and `request.getServerPort()` likewise requires exact port matching (`port == allowedPort`), preventing unapproved authority bypass.
     - In `GoogleSignInWebIntegrationTest.untrustedHostRejected`, added explicit test assertions verifying that `Host: localhost` (absent port, implied 80) is rejected with 400 Bad Request, alongside existing rejections for `Host: localhost:9999`, `Host: 127.0.0.1:8080`, `Host: ::1`, `X-Forwarded-Host`, `X-Forwarded-Port`, and `X-Forwarded-Proto`.

2. **AUTH-FR-11 (MEDIUM / NOT_VERIFIED) — Mandatory Native/Consumer Evidence & Accurate Matrix:**
   - **Port :8080 Selection Capability:** In `GoogleSignInBrowserIntegrationTest`, added dynamic detection for port 8080 (`PORT_8080_AVAILABLE`). If port 8080 is free, Spring Boot binds directly to `server.port=8080`; if occupied by host background services (`vmnat.exe`, `AgentService.exe`), it falls back to `server.port=0` (random port) and explicitly reports:
     `[BROWSER_GATE] Fixed port 8080 is unavailable (occupied by host services: vmnat.exe / AgentService.exe). Marking fixed-port :8080 condition as NOT_VERIFIED/ENVIRONMENT_BLOCKED per handoff specification.`
   - **Cookie Attributes & Direct Assertion:** `nativeEdgeBrowserRoundtrip` verifies:
     - `ppv_google_signin_bind` cookie attributes on Spring responses: `HttpOnly`, `Path=/api/v1/auth/google/callback`, `SameSite=Lax`, `Max-Age=300`.
     - `ppv_google_handoff` cookie delivered by browser redirect to consumer `/complete`.
   - **Native/HTTP Negative Proof Cases:** In `nativeEdgeBrowserRoundtrip`, executed direct negative assertions:
     - Replaying consumed handoff ticket returns 401 Unauthorized (`GOOGLE_HANDOFF_INVALID`).
     - Malformed handoff verifier format (non-43 chars) returns 400 Bad Request.
     - Mismatched handoff verifier (valid 43-char Base64URL token) returns 401 Unauthorized (`GOOGLE_HANDOFF_INVALID`).
     - Unknown / random ticket token returns 401 Unauthorized (`GOOGLE_HANDOFF_INVALID`).
   - **Controlled Consumer In-Flight Ordering Proof (A-08):** In `GoogleSignInBrowserIntegrationTest.consumerInFlightCancellationRevokesLateRefreshToken`:
     - Edge browser starts login flow to `/start-flow`.
     - When flow reaches the fake Google auth step (`callbackReachedLatch.await`), consumer simulates a cancellation occurring *in flight* (`cancelInFlight = true`).
     - Browser completes callback redirect to `/api/bff/auth/google/complete`.
     - Consumer detects in-flight cancellation, does NOT install owner session, consumes the ticket, and immediately revokes the received refresh token via `/api/v1/auth/revoke`.
     - DOM displays `<div id="status">CANCELLED_IN_FLIGHT</div>`.
     - Verifies `consumer.lateTokenRevokedInFlight` is true and subsequent call to Spring `/api/v1/auth/refresh` returns 401 Unauthorized.
   - **Exact Five-Batch Selectors & Test Counts:** Executed the exact five contract selectors without alteration, yielding 231 tests (50 core, 3 native browser, 89 frozen auth, 53 shared, 36 Drive) with zero failures/errors/skips.

---

## 2. Acceptance Verification Matrix (A-01 through A-10)

| ID | Description | Result | Verifying Test Class |
|---|---|---|---|
| **A-01** | OFF has no Google handler/permit/client/key I/O; ON adds exactly 4 handlers and 3 public method matchers. Legacy bootstrap/password/refresh/revoke/PIN/HS256 unaffected; Google token never authorizes business API | **PASS** | `GoogleSignInConfigurationTest`, `AuthenticationBootstrapIntegrationTest`, `AuthWebIntegrationTest`, `SecurityFilterChainIntegrationTest` |
| **A-02** | Unbootstrapped/unlinked LOGIN fails; LINK needs PPV Owner bearer plus password checked now; wrong passwords commit rate events; concurrent 5th/6th requests respect 5/15m gate | **PASS** | `GoogleSignInProtocolTest`, `GoogleSignInWebIntegrationTest` |
| **A-03** | Parallel different-sub first links commit only one immutable owner binding; same-sub new fresh LINK succeeds idempotently; wrong-sub/login substitution denied; owner count remains one | **PASS** | `GoogleSignInProtocolTest` |
| **A-04** | Fixed client/openid email/backend callback, real independent state/cookie/nonce/PKCE; missing/wrong/replaced/expired/replayed/mixed-purpose proof fails, exactly one code exchange claimant | **PASS** | `GoogleSignInProtocolTest`, `GoogleSignInWebIntegrationTest` |
| **A-05** | Valid signed current expected identity succeeds; wrong issuer/aud/azp/alg/nonce/sub/time, forged signature, kid/cache/fetch/redirect/timeout failure deny; Google validation cannot replace PPV decoder | **PASS** | `GoogleSignInProtocolTest`, `GoogleSignInProviderClient` unit tests |
| **A-06** | ID token/sub POST, ticket only/challenge only/wrong verifier, expired/swapped/replayed ticket fail. Two valid simultaneous redeems issue at most one pair; commit/lost reply never reissues; forced rollback reverts ticket+tokens together | **PASS** | `GoogleSignInProtocolTest`, `GoogleSignInWebIntegrationTest` |
| **A-07** | Real browser :3000 consumer→fake Google→actual Spring :8080→fixed completion delivers named HttpOnly/Lax/path cookies and safe final URL, no ticket/token/verifier/code in second redirect/DOM/JS/log | **H1_AUTH_PASS** (test port browser execution with cookie attributes and negative cases); **ENVIRONMENT_BLOCKED** on literal :8080 due to host services (`vmnat.exe`, `AgentService.exe`); **DEFERRED_H1_F** for Next.js BFF | `GoogleSignInBrowserIntegrationTest` |
| **A-08** | Processing/restart/expiry/late-finalization fails closed; consumer prelogin/generation/logout cancellation cannot install late tokens and revokes received late refresh best-effort | **H1_AUTH_PASS** (in-flight consumer cancellation, late arrival detection, and stored refresh revocation verified); **DEFERRED_H1_F** for Next.js singleflight/CSRF | `GoogleSignInBrowserIntegrationTest`, `GoogleSignInProtocolTest` |
| **A-09** | Fresh and V27→V28 seeded upgrade, constraints/indices/FKs/proof variants, tamper/PKCE encryption, short locks, TTL/capacity/cleanup and source/Modulith ownership; three tables excluded from Portability | **PASS** | `GoogleSignInPersistenceIntegrationTest`, `GoogleSignInCryptoTest`, `PortabilityIntegrationTest`, `ApplicationArchitectureTests` |
| **A-10** | Existing local password login/refresh/revoke/PIN supports documented emergency backend access if Google lost; no unlink/rebind/second user/email recovery or unsafe SQL edit advertised | **PASS** | `GoogleSignInProtocolTest`, `AuthWebIntegrationTest`, `google-sign-in-operations.md` |

---

## 3. Bounded Command Verification Receipts (Exact Five-Batch Contract)

All test batches were executed using `scripts/run-bounded-command.py` with 300s normal execution limits. All commands exited with code 0 (`PASS`).

### Batch 1: New Core Tests
- **Selector:** `GoogleSignInConfigurationTest,GoogleSignInCryptoTest,GoogleSignInProtocolTest,GoogleSignInPersistenceIntegrationTest,GoogleSignInWebIntegrationTest`
- **Receipt:** `logs/bounded-runs/run-1794185079-bb3be205.json`
- **Exit Code:** 0 (`PASS`)
- **Execution Duration:** 40.5s
- **Test Results:** 50 tests run, 0 failures, 0 errors, 0 skipped
  - `GoogleSignInConfigurationTest`: 4 passed
  - `GoogleSignInCryptoTest`: 5 passed
  - `GoogleSignInPersistenceIntegrationTest`: 13 passed
  - `GoogleSignInProtocolTest`: 16 passed
  - `GoogleSignInWebIntegrationTest`: 12 passed (including absent-port rejection, host/port boundary validation, bounded streaming, OpenAPI schemas)

### Batch 2: Actual Browser / Consumer Integration Test
- **Selector:** `GoogleSignInBrowserIntegrationTest`
- **Receipt:** `logs/bounded-runs/run-1794185131-1f752e0a.json`
- **Exit Code:** 0 (`PASS`)
- **Execution Duration:** 25.8s
- **Browser Executable:** `C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe` (Edge 157.0.4341.42 headless)
- **Test Results:** 3 tests run, 0 failures, 0 errors, 0 skipped
  - `nativeEdgeBrowserRoundtrip`: PASSED (verifies port 3000 consumer -> fake Google -> Spring :port -> 303 redirect with Set-Cookie: ppv_google_handoff -> handoff redemption -> clean query; cookie attributes verified; negative replayed/malformed/mismatched/unknown ticket cases pass)
  - `consumerInFlightCancellationRevokesLateRefreshToken`: PASSED (verifies consumer in-flight cancellation while request is in flight, late token arrival best-effort revocation via `/api/v1/auth/revoke`, and asserts stored revocation on `/api/v1/auth/refresh` returns 401)
  - `consumerPresetCancellationRevokesLateRefreshToken`: PASSED (verifies preset cancellation and stored refresh revocation returning 401)

### Batch 3: Frozen Authentication Regression Suite
- **Selector:** `AuthenticationBootstrapIntegrationTest,AuthWebIntegrationTest,BootstrapValidationTest,JwtPropertiesTest,JwtTokenServiceTest,TokenGeneratorTest,SessionServiceTest,RefreshTokenLifecycleIntegrationTest,PrivatePinIntegrationTest,PrivatePinConcurrencyTest,SecurityFilterChainIntegrationTest,SecretRedactionTest`
- **Receipt:** `logs/bounded-runs/run-1794185168-9b28d0b4.json`
- **Exit Code:** 0 (`PASS`)
- **Execution Duration:** 39.8s
- **Test Results:** 89 tests run, 0 failures, 0 errors, 0 skipped across all 12 frozen authentication classes.

### Batch 4: Shared Integration Boundaries
- **Selector:** `PortabilityIntegrationTest,ApplicationArchitectureTests`
- **Receipt:** `logs/bounded-runs/run-1794185221-6c1a2e5d.json`
- **Exit Code:** 0 (`PASS`)
- **Execution Duration:** 38.3s
- **Test Results:** 53 tests run, 0 failures, 0 errors, 0 skipped
  - `ApplicationArchitectureTests`: 38 tests passed (Spring Modulith module rules and package verification)
  - `PortabilityIntegrationTest`: 15 tests passed (lossless snapshot manifest v28, all 3 Google tables confirmed excluded from export)

### Batch 5: Existing Drive / Security Isolation
- **Selector:** `DriveApiInventoryIntegrationTest,DriveConfigurationIntegrationTest,DriveWebIntegrationTest,DriveOAuthSecurityTest,DriveH1BWebIntegrationTest,DriveS2ConfigurationIntegrationTest`
- **Receipt:** `logs/bounded-runs/run-1794185271-ec0b028a.json`
- **Exit Code:** 0 (`PASS`)
- **Execution Duration:** 44.3s
- **Test Results:** 36 tests run, 0 failures, 0 errors, 0 skipped across Drive API, configuration, web, and OAuth isolation suites.

---

## 4. Environment Observation & Port 8080 Barrier

During browser integration test execution, `GoogleSignInBrowserIntegrationTest.PORT_8080_AVAILABLE` probed `0.0.0.0:8080` on the Windows test host. The port is occupied by host background services:
- `vmnat.exe` (VMware NAT Service, PID 23680)
- `AgentService.exe` (PID 4840)

Per handoff specification:
> *"If fixed ports/browser invocation are unavailable, mark NOT_VERIFIED/ENVIRONMENT_BLOCKED; do not kill an unrelated process, silently change hosts/ports, install a driver or claim Next proof."*

The test documents this condition truthfully, marks the literal port 8080 gate as `ENVIRONMENT_BLOCKED`, and executes the full headless Edge browser (`msedge.exe 157.0.4341.42`) against an assigned test server port to verify the complete 3-leg browser protocol, cookie setting, safe URL redirection, negative handoff cases, in-flight cancellation ordering, and refresh token revocation.

---

## 5. Self-Review Checklist

- [x] **Single-user boundary respected:** `ppv_owner_google_identities` strictly enforces `owner_id = 1` with DB CHECK constraint and row lock. No multi-tenant expansion.
- [x] **No credential leakage:** PKCE verifier encrypted with AES-256-GCM using dedicated key and zeroed on terminal attempt. No Google ID tokens, access tokens, verifiers, or tickets persisted or logged. Redacted `toString()` on DTOs and results.
- [x] **Atomic token issuance:** Handoff ticket consumption and canonical PPV session token issuance occur in a single database transaction via `SessionService.issueForVerifiedOwner`. Replay is rejected with 401. Forced rollback atomically reverts ticket consumption and token issuance.
- [x] **Deadline enforcement:** Token exchange and JWKS fetch complete asynchronously within monotonic <=10s budget with bounded 64KiB stream readers.
- [x] **Strict host and authority bounds:** Host strictly literal `localhost:8080` (rejects absent port `Host: localhost`, `127.0.0.1`, `::1`, wrong ports, and `X-Forwarded-*`); scopes restricted to `openid`, `email`, `https://www.googleapis.com/auth/userinfo.email`.
- [x] **Default-OFF isolation:** `ppv.security.google-signin.enabled=false` defaults ensure zero Google beans, handlers, or public matchers exist when disabled.
- [x] **Frozen contracts preserved:** Spring global HS256 JWT decoding, clock handling, master password, refresh tokens, and private PIN remain completely intact.
- [x] **Exact selectors verified:** All 5 exact contract batches executed without alteration, yielding 231 tests with 0 failures/errors/skips under 300s limit with receipts recorded.
- [x] **No git operations:** No commit, push, tag, or merge PR attempted.

---

## 6. Next Step

Handoff `GDI26-S3-H1-AUTH` is `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
Next step is independent Codex `$codex-final-review` in a separate session (Remediation 3/3 consumed, final Antigravity round; takeover NOT_AUTHORIZED).
