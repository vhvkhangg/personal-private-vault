---
name: frontend-testing
description: Plan or execute risk-based PPV frontend type, component, BFF, browser, accessibility and visual verification with truthful bounded evidence. Use for frontend test planning even without an app; backend-only tests retain backend-testing.
---

# PPV frontend testing and evidence

Read [ACTIVE](../../../docs/implementation/handoffs/ACTIVE.md), its exact acceptance plan and [frontend workflow](../../../docs/workflow/frontend-development.md). No app/toolchain exists in H0: frontend build/type/lint/format/component/BFF/Playwright/axe/visual execution is **NOT_RUN / N/A to H0 runtime**, never PASS. H0 verifies guidance routing, finite stop controls, provenance, saved docs and actual native safety controls only.

## Future H1 evidence, after its own gates

Select compatible versions/tools during accepted H1 preparation. The implementer owns focused behavior tests, then the agreed affected/final checks; independent Codex verifies source-applicable evidence. Do not repeat unchanged suites or add tests that merely mirror prose.

| Layer | Minimum relevant proof when implemented |
| --- | --- |
| Static/toolchain | Reproducible pinned lockfile/toolchain; strict TypeScript, ESLint and formatting checks, then authorized Next build. Record real warnings/errors; no warning-free claim from an unrun IDE inspection |
| Unit/component (Vitest/RTL direction) | Real user interactions: keyboard/forms/drawers/focus, loading/empty/error/unknown, long Vietnamese names, slot change/late result and expired session; assert visible outcomes rather than implementation structure |
| BFF/auth integration | Direct unauthorized calls to routes/actions; secret serialization/public cache negatives; CSRF/origin rejection; cookie flags/scope/lifetime, fixation, rotation concurrency/replay, logout invalidation and failure; unchanged Spring authority |
| OAuth browser flow | Same browser/host/path binding delivered to exact backend callback; absent/wrong/expired/replaced cookie/state fails. Test actual Set-Cookie routing with approved local fixtures; fixed text callback remains text. Mocked server response alone does not prove browser cookie delivery |
| Upload integration | [S2 key syntax/time/bounds](../frontend-bff-security/SKILL.md), missing/invalid/stale key; lost reply and unknown outcome recover with identical key/bytes/name/MIME; changed replay conflict, live claimant, zero byte, exact cap and over-cap/request cap, unsafe name/MIME/Workspace rejection; no dispatch-implies-success or new-key automatic retry |
| Transfer integration | Slot/root/sub/epoch/cursor isolation and pending/reconciliation/error; download expected length, short/excess body, abort/timeout/cancel/cleanup, no post-commit JSON or false completion, no Range promise; bounded memory/backpressure under realistic transfer |
| Browser (Playwright direction) | Approved local fixture desktop and small-screen login/slots/list/ensure/upload/download/navigation/error paths. Separate fake-provider results from live Google. Never enable provider/Drive flags on the user's runtime merely to make tests pass |
| Accessibility (axe + manual) | [Design skill](../frontend-design-accessibility/SKILL.md) state matrix, semantic names, keyboard and focus return, actual-surface contrast, reduced motion/high contrast, 200% text resize and **320 CSS-pixel reflow** (e.g. 1280 at 400%), justified table exception. PH-I01 remains for H1; axe alone cannot prove WCAG 2.2 AA |
| Visual | Screenshots/diffs at recorded viewports, state/locale, artifact version/hash and actual Owner-approved baseline. Agent screenshot success is not Owner design approval; changed material visuals require changed-version approval |

Resolve future browser/BFF download observability honestly: if the browser owns saving, the UI cannot claim a completed save it did not observe. Prove transport failure at the layer that can observe it and expose supported status only. Do not invent test passes, providers or absent frontend suites.

## Bounded execution and handback

Use [bounded-command-execution](../bounded-command-execution/SKILL.md) and existing `scripts/run-bounded-command.py`: **normal profile, <=300s**, fresh ignored `logs/` run/receipt/stdout/stderr, owned-process-tree cleanup. Node/npm/Playwright/install commands are not eligible for the guarded 2,100s Maven/Python full-suite profile. Installation needs its own authorized package scope; no opaque external skill installer. Close only owned test servers/browser processes and retain failures/logs.

For each executed gate record baseline and source hashes, client/tool versions, exact safe command, fixture/environment, count/exit/duration, receipt and raw streams, limitations and PASS/FAIL/NOT_VERIFIED/N/A. Source-qualify reused evidence. Native hook tests require actual trusted-root client events: classifier output or model narration alone is insufficient, and missing Antigravity proof cannot be substituted by Codex. Do not repair trust/configuration to obtain a PASS.

Run mandatory implementer self-review, resolve self-found in-scope defects, persist/reopen ACTIVE/executable handoff/handback with consistent ID/status/counter and scoped Docs Sync Check, then stop for separate independent `$codex-final-review`. No self-acceptance or Git staging/publishing.
