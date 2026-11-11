# Owner Decision — OWNER-TEST-PORT-19080: standing local fixture port

- **Date / status:** 2026-11-09 / `OWNER_AUTHORIZED / STANDING / REUSABLE`.
- **Source:** the Owner's explicit message in the continuing `GDI26-S3-H1-F` `$codex-create-handoff` conversation, headed **Owner Decision — Permanent Test Fixture Port Authorization**.
- **Scope:** H1-F and future **separately authorized** Personal Private Vault local testing slices. This record grants a test-port choice, not a new slice, implementation, provider activation or production change.
- **Current effect:** the H1-F port-authority blocker is `OWNER_DECISION_RESOLVED`. Runtime fixture/security evidence is still required.

## Approved decision

The Owner explicitly approves **`http://localhost:19080` as an alternative fixed Spring test-fixture origin**, with **`http://localhost:3000` for the Next.js BFF where required**. The authorization is standing and reusable. Codex and Antigravity **must not request Owner approval again solely to use port 19080** in a later authorized local fixture.

The Owner's controlling conditions are:

1. **Test-only.** Production/pilot callback, host and security contracts stay unchanged. In H1, shipping Spring callbacks remain `http://localhost:8080/api/v1/auth/google/callback` and `http://localhost:8080/api/v1/drive/oauth/callback`; the fixed BFF completion remains `http://localhost:3000/api/bff/auth/google/complete`.
2. Verify availability before every fixture run. Use one explicit fixed port, never a hidden proxy, host rewrite, random fallback or assumed-free port. Verify the relevant IPv4/IPv6 listener and actual browser resolution.
3. **Never use port 18080. Do not stop unrelated services occupying 8080**, alter their reservations, or change firewall/trust/hooks to manufacture a pass.
4. Preserve strict Host, cookie, CSRF, OIDC and Drive OAuth checks. Use the same literal `localhost` hostname where the contract requires it. Cookies are host/path scoped and are **not isolated by port**.
5. Test-only callback/port configuration must be **explicit, isolated and independently verified**. The H1 fixture may use the exact corresponding callback paths at `localhost:19080`; this does not authorize an alternate production callback. Keep the Next BFF origin at `localhost:3000` where required.
6. The port authorization does **not** waive native-browser evidence, change a production validator, admit wildcard Host/origins, or permit live Google/Cloud credentials, provider consent or deployment.

The earlier [D31 H1-AUTH decision](../implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/owner-decision-auth-fr11-alternate-native-test-port-2026-11-09.md) remains immutable history with its original scope. This new standing decision supplies the authority for H1-F and future authorized tests; do not retroactively rewrite D31's evidence, counters or acceptance.

## Execution and evidence boundary

- Read [Owner policy](current-owner-delivery-policy.md), [ACTIVE](../implementation/handoffs/ACTIVE.md), the current slice's exact executable contract, and this decision. A future slice still needs its own scope, fixture/source allowlist and acceptance gate.
- Prefer existing test-only dependency injection/constructor/setter seams. A fixture-specific expected port remains **exactly 19080**, not an accept-any-port branch. Do not edit shipping backend source/configuration to bypass a strict default. If existing seams cannot support the approved test origin, report the concrete call sites and required additional implementation authority; **do not ask again about the already-approved port alone**.
- Use the existing normal-profile watchdog (at most 300 seconds), fresh ignored receipts/source hashes and owned-process cleanup. Recheck ports at launch; a successful earlier preflight is not a reservation.
- Prove real Spring and real Next listeners, actual same-browser cookie delivery, correct callback paths/Host and the slice's positive and negative cases. A mock response, changed test expectation, metadata or a different client is not native-browser proof.
- Keep normal Google Sign-In, Drive, Drive Files and Feed flags OFF/unset. Any enabled fake-provider configuration must belong only to an isolated deterministic fixture with synthetic keys/data and disposable PostgreSQL. Preserve unrelated processes, data, historical logs and receipts.
- Only the Owner stages/commits/pushes/tags/releases or creates/merges PRs. This recorded approval is **not a claim of Owner Git publication** of these documentation files.

## Initial H1-F preflight, not implementation acceptance

On 2026-11-09, the existing runner completed a read-only environment preflight in **1.031 seconds**, exit 0, capture/cleanup complete. Real bind/listen/close checks passed for `127.0.0.1` and `::1` on **3000 and 19080**. Edge metadata was **157.0.4341.42**, Docker Server **29.9.2**; ambient Node/npm were **22.19.0 / 10.9.3**. No Next/Spring app or browser acceptance suite was started.

Receipt: `logs/gdi26-s3-h1-f/handoff-entry-017134a11a1a4555aca13555d282f98b/preflight.json`, with sibling `preflight.stdout.log` and `preflight.stderr.log`. This is availability/version evidence only; implementation must prepare the selected toolchain and repeat launch preflight.

**Docs Sync Check:** UPDATED — this standing decision and its discoverable link in Owner policy. ALREADY_CONSISTENT — test-only authority, unchanged production defaults, no native-proof waiver and existing Owner-only publishing rules. HISTORICAL — D31 and prior blocked H1-F entry. NOT_VERIFIED — future fixture/native-browser results. No new remediation round or takeover follows.
