# GDI26-S3-H0 — Acceptance, tests and evidence plan

**Matrix applies to FUTURE H0 implementation acceptance, not to the current docs-only preparation.** A Codex pre-handoff reviewer should first challenge sufficiency and scope, mark issues and return `READY_FOR_HANDOFF` only if this is an executable reviewable plan. `READY_FOR_HANDOFF` is not an implementation or design approval.

| Case | Minimum later H0 acceptance evidence | Failure condition |
|---|---|---|
| H0-01 | Verified main SHA, `ACTIVE=NO_ACTIVE_HANDOFF` at preparation, no `frontend/`/`package.json` created by H0. | H0 creates Next app or modifies backend. |
| H0-02 | Decision record D11–D22 linked correctly; D21 is provisional UI-only, D22 unconditional Owner gate. | Treating design directions as accepted screenshots. |
| H0-03 | Explicit H0/H1/visual gate sequencing in agent and Codex handoff docs with no Phase 16 expansion. | Executable H1 before actual design approval. |
| H0-04 | One small frontend agent with precise applicable tools, `ACTIVE` guard and no Git publishing/activation. | Agent stages/commits or acts without handoff. |
| H0-05 | Skill/rule inventory proven nonduplicative; tests of triggers and precedence; narrow backend scope exclusions. | General copied external prompt overrides PPV controls. |
| H0-06 | Verified external references: exact upstream commit/path, license, scan of executable snippets and installation side effects. | Blind `npx ... init`, unpinned installer/vendor bundle. |
| H0-07 | Next.js skill correctly distinguishes server/client, BFF auth, caching of private pages and streaming. | Private data cached/publicly serialized. |
| H0-08 | BFF security skill explicitly protects tokens/session/CSRF/logout and forbids localStorage secrets/Google refresh leakage. | Browser-accessible refresh token or unauthenticated BFF route. |
| H0-09 | S1 OAuth cookie/callback contract and same-browser, same-host flow recorded with negative design/test controls. | Treating authorize as server-only BFF call that loses browser cookie. |
| H0-10 | S2 four routes, root/file provenance, two slots, stable `Idempotency-Key`, unknown outcome/size/streaming contract present in agent skill review checks. | New key on ambiguous retry, arbitrary file browsing or buffering 50MiB in browser. |
| H0-11 | UI quality skill codifies dark/blue-violet/glass/two rails, Personal secondary Drive, balance, responsive, Vietnamese/i18n, reduced-motion without locking actual tokens. | Declaring dark blue screenshot `DESIGN_APPROVED` automatically. |
| H0-12 | A11y criteria and state matrix cover keyboard, focus, AA contrast on glass, responsive/zoom, loading/error/empty/unknown. | Color-only status, hover-only navigation, unreadable contrast. |
| H0-13 | Frontend test skill describes TypeScript strict/lint/format/unit-component + Playwright/axe/visual-diff E2E **to execute at H1**, with ownership of test evidence. | Claiming frontend tests pass in H0 before any app exists. |
| H0-14 | Existing hook and Owner Git policy verified by unit tests and **native Windows trusted-root CLI negative controls**, with exact actual tools and limitations; if no native access, report NOT_VERIFIED. | Mock-only guard considered enforceable or hook bypass recommended. |
| H0-15 | Bounded-command runner reused; future node/browser command timeout, cleanup/evidence expectations documented and safe. | Unbounded background processes or deleting historic logs. |
| H0-16 | H0 skills/rules are discoverable in both target agent workflows from root/scoped instructions; no stale broken local links. | Skill filenames exist but cannot be selected/traced. |
| H0-17 | No secret, live Cloud, provider consent, frontend runtime/deployment, new OAuth scope, RAG, Phase 16 or module mutation in H0 diff. | Any unrelated production or privileged edit. |
| H0-18 | Independent Codex final-review checks source hashes, H0 negative controls, provenance and exact handback; only Owner publishes. | Implementer self-acceptance or auto Git publication. |
| H0-19 | A persistent Owner visual gate record template/checklist exists for H1, but current status is `NOT_DESIGN_APPROVED`; missing Owner evidence fails H1. | Treat H0 complete / visual direction as enough to start H1. |
| H0-20 | Docs Sync Check and future H0 closeout align README/ACTIVE/handoff; counters are scoped to H0 (initial 0/3). | Borrowing S2 Codex exception or remediations for H0. |

## Review procedure for CURRENT preparation

1. Independently confirm GitHub main entry SHA, current active state, no frontend, and source applicability; do not rely only on the generated package narrative.
2. Read D00–D10 and current S1/S2 contracts, relevant root/hook/rule/agent/skill docs, Owner D11–D22 scope and H0/H1 delineation.
3. Audit candidate frontend artifacts for **KEEP / REUSE / SIMPLIFY / REMOVE / DEFER**; reject unnecessary framework and skill duplication.
4. Check absolute prohibition on creating `frontend/` or product source in H0; no edit to `ACTIVE` in preparation; check the actual allowed future H0 edit paths and whether an Owner decision is still needed.
5. Assess authentication/callback-cookie/BFF/CSRF/file streaming/idempotency applicability; do not expand S1/S2 OAuth.
6. Inspect all local links and status labels, exact negative controls and D22 approval rule; confirm H1 cannot proceed without explicit Owner `DESIGN_APPROVED` evidence.
7. Produce dated review under this folder `reviews/`, with findings severity, required corrections, evidence, reviewer and `Next step`. Keep historical review/Owner decision immutable.

## Implementation-era verification strategy (after separate handoff)

- **Fresh H0 assertions:** static/structural contract checks of new skill frontmatter/triggers, hook rule tests, preservation of old backend guards, precise path/diff inventory, native client invocation under its own trusted local environment, negative control demonstrating Git stage denial. Bounded commands via existing runner with unique receipts.
- **Deferred H1 assertions:** Next app package install/lockfile reproducibility, TS strict, ESLint, format, Vitest/RTL, mocked server-side BFF auth/security integration, Playwright desktop/mobile with axe, upload-stream/idempotency/timeout and OAuth browser-cookie E2E under approved fixture; screenshot regression relative to approved design; no live Google without separate gate.
- **Do not conflate:** H0 test plan vs executed frontend tests; fake provider vs real Google Cloud; local UI screenshot vs Owner approval; hook unit test vs actual client hook enforcement; 372 prior S2 tests vs current fresh H0 tests.

**Current test status:** `NOT_RUN` (docs-only preparation); no frontend app/toolchain exists. All H0 acceptance rows are prospective, **not** 20 PASS claims. If H0 review identifies new HIGH/MEDIUM finding, block readiness; LOW/INFO track under existing Owner policy.

**Next step:** independent `$codex-pre-handoff-review` with no source edits; future H0 initial remediation 0/3, takeover not authorized.
