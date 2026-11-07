# GDI26-S3-H0 — Scope, sequence and protected workflow

## Problem and simplest solution

The project currently has backend/RAG-oriented instructions, Codex/Antigravity skills, safety hooks, Windows bounded commands and independent review. It has no `frontend/`, frontend package toolchain, frontend test harness or frontend-specific implementation contract. Initializing Next.js before defining and reviewing agent execution boundaries would weaken the user's explicit requirement to establish the harness first.

**Proposed solution:** one small, separately accepted **H0 harness tooling slice** using and extending existing `.agents` conventions. New tools are **not** justified merely because an external repository advertises a skill. Defer the application, design freeze, BFF implementation and Playwright execution to subsequent gates.

## KEEP / REUSE / SIMPLIFY / REMOVE / DEFER matrix

| Need | Decision | Reason / boundary |
|---|---|---|
| Owner policy, handoff/ACTIVE status, 3-round review workflow | **REUSE** | Already enforced/documented; H0 must not create a parallel policy or self-authorize. |
| Root `AGENTS.md` and `.agents` routing | **REUSE / EXTEND NARROWLY** | Add only frontend entry routing after H0 implementation is authorized. Preserve dated existing history. |
| Existing Windows repository safety hooks and `.codex` owner-Git restrictions | **KEEP / VERIFY** | Test actual trusted client/root hooks before claiming enforceable; avoid replacing global hooks for frontend. |
| Frontend implementer role and scoped instructions | **KEEP / ADD MINIMAL** | One frontend agent for H1; existing backend agent is not an appropriate owner of UI or BFF. |
| React/Next.js, UI design, BFF security and frontend tests | **KEEP as scoped guidance** | Three or four small targeted rules/skills, not duplicated catalogs. See [inventory](harness-inventory-and-targets.md). |
| UI/UX Pro Max, Vercel and Taste external skills | **SELECTIVE REFERENCES** | Audit license/version/compatibility and conflicts; no automatic installation or remote execution. |
| New generic UI framework / monorepo manager / design AI platform | **REMOVE** | No demonstrated need for S3-only frontend. |
| Extensive new hook framework or generic policy DSL | **REMOVE unless a measured gap** | Reuse existing hook tests/guard boundaries; avoid touching unrelated trust settings. |
| `frontend/`, Next.js app, package manager lockfile, components, runtime UI and browser tests | **DEFER H1** | Forbidden during H0 implementation, not merely during preparation. |
| Visual design variants, exact tokens, screenshots | **DEFER design review** | Owner feedback and approval are required before H1. |
| Live Google/Cloud/key files, frontend production deployment | **DEFER separate operator gate** | Default flags OFF; local/fake tests may be approved later. |

## Explicit edit boundaries

**Current preparation package:** new Markdown only under this dated folder, plus one line in `docs/implementation/maintenance/README.md`. No edits to `ACTIVE`, source, executable skill/rule/agent/hook, `AGENTS.md`, backend, tests, diagrams, package manifests or frozen architecture.

**Future H0 implementation proposal (only after approved executable handoff):** within `.agents/{agents,rules,skills}/`, possible narrow root `AGENTS.md` routing, docs/workflow front-end usage note, and **only if proof requires** tests of existing hook selectors or additional scoped guard. No UI runtime dependency, no application/test package, no `frontend/`, no changes to `.codex/config.toml` or trust/permissions by default. Codex must verify final exact allowlist before authorizing changes. Preserve existing history and backend scope.

**Future H1 after all gates:** add a Next.js app under `frontend/` with package manifest + pinned lockfile and its own `AGENTS.md`, frontend test scripts/config, BFF routes, UI/components and E2E harness, **strictly within the approved design version and S3 scope**. Existing backend S1/S2 functions remain the authority; new backend behavior requires separate explicit change approval if uncovered by review.

## Workflow and counters

- H0 is an explicit **maintenance preparation** (not numbered Phase 16). It may be pre-handoff reviewed with `ACTIVE=NO_ACTIVE_HANDOFF` under [`codex-pre-handoff-review`](../../../../.agents/skills/codex-pre-handoff-review/SKILL.md) and [Owner policy](../../../workflow/current-owner-delivery-policy.md).
- If H0 documentation gets `READY_FOR_HANDOFF`, **Owner** commits/pushes preparation; only then Codex `$codex-create-handoff` opens an executable H0 contract. Antigravity must perform mandatory self-review and leave an independent Codex final review. Only Owner publishes accepted H0.
- Keep H0 and H1 distinct in ACTIVE, remediation counts and acceptance. At preparation H0 **0/3 Antigravity rounds**; no takeover authorized. Previously accepted S2 exceptional Codex passes are historic S2 only.
- After H0 acceptance/publish, the Owner participates in explicit visual design revision rounds. Even signed-off H0 + visual design does not automatically open H1; independently accepted/published H1 preparation and distinct executable handoff are required.
- Do not quietly reclassify a frontend harness as broad Phase 16 activation or RAG work.

## Non-goals

No Figma connection mutation or generated screenshot in H0, no UI implementation, no real account OAuth, no new Google scopes, no Google Picker, no read/write of Media S3, no data import/indexing, no new top-level Spring module or new Flyway migration. Avoid working on unrelated `.codex/config.toml` as directed by Owner; do not infer approval for disabling safeguards.

**Next step:** independent Codex H0 preparation review; record findings before any executable handoff.
