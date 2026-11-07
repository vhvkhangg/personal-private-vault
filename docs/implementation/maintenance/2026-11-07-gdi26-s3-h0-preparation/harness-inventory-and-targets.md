# GDI26-S3-H0 — Baseline harness inventory and smallest proposed target

## Source checked

At verified main [`d8da1677f670`](https://github.com/vhvkhangg/personal-private-vault/commit/d8da1677f6700c308aec750dcc714fc8c3983185), repository tree is 3,748 entries; no `frontend/`, root `package.json` or S3-H0 harness files. Baseline observations below are **repository facts**; H0 target paths are **proposals**, not files delivered by this preparation.

| Existing asset | Status / H0 action |
|---|---|
| `AGENTS.md`, `.agents/README.md` | Existing universal instructions, many historical banners. Retain; future minimal frontend entry point only if needed after reviewing order of precedence. |
| `.agents/agents/backend-implementer/agent.md` and `architecture-auditor` | Backend-focused; reuse review role, do not repurpose backend implementer as visual author. |
| `.agents/skills/codex-pre-handoff-review`, `codex-create-handoff`, `antigravity-implement-handoff`, `antigravity-self-review`, `codex-final-review` | Existing canonical cross-slice gates. Keep and only add frontend-specific applicability links where absolutely necessary. |
| `.agents/rules/agent-handoff-workflow.md`, `security.md`, `tests.md`, `documentation.md`, `context-efficiency.md`, `code-reuse-and-patterns.md` | Existing generic rules; preserve and avoid duplicates. |
| `.agents/hooks.json`, `.agents/hooks/repository_safety.py`, `.agents/hooks/test_repository_safety.py`, `.codex/rules/owner-git.rules` | Existing safety policy. Native enforcement depends on trusted actual Windows client/root execution; tests must not confuse unit passes with native enforcement. No new global hook by default. |
| `.codex/config.toml` | Existing locally relevant configuration; **out of H0 scope**. Do not alter trust, IDE endpoint, hook registration, permissions or owner-Git policy without a separate explicit authorization. |
| `scripts/run-bounded-command.py`, `scripts/test_run_bounded_command.py` | Reuse bounded command runner for later Node/browser work; measure correct timeout/process-tree cleanup behavior instead of inventing a second runner. |
| `docs/workflow/current-owner-delivery-policy.md`, `docs/workflow/documentation-synchronization.md`, `docs/templates/` | Reuse source/review/docs-sync contracts. Do not rewrite dated prior reviews. |

## Proposed H0 implementation inventory (Codex may KEEP/SIMPLIFY/REJECT)

| Candidate path | Intent | Proposed classification |
|---|---|---|
| `.agents/agents/frontend-implementer/agent.md` | Single scoped agent for Next.js/TypeScript UI, BFF, testing, design implementation; obey ACTIVE, Owner visual gate and no Git publishing. | **KEEP one** |
| `.agents/skills/frontend-nextjs-engineering/SKILL.md` | Next.js App Router/RSC/client-server boundary, typed API, composable components, performance and package/toolchain conventions. Reference official Next.js/Vercel. | **KEEP** |
| `.agents/skills/frontend-design-accessibility/SKILL.md` | Owner-approved design directions, tokenization, usability, accessibility, responsive, review states; no independent design approval. | **KEEP** |
| `.agents/skills/frontend-bff-security/SKILL.md` | JWT/cookie/session/CSRF, callback binding, no browser token leak, safe upload/download proxy and no auth bypass. | **KEEP** |
| `.agents/skills/frontend-testing/SKILL.md` | Unit/component/integration/Playwright/axe/visual regression, bounded commands and evidence contract. | **KEEP** |
| `.agents/rules/frontend-nextjs.md` | Narrow always-applicable TS/React structure and typed runtime boundaries. | **KEEP** |
| `.agents/rules/frontend-ux-quality.md` | Tokens, a11y, glass contrast and required state review; link to design skill not duplicate content. | **KEEP** |
| `.agents/rules/frontend-security.md` | Small, mandatory auth and BFF boundary reminders; link to security skill. | **KEEP** |
| Root `AGENTS.md` (minimum additive routing), `.agents/README.md` index | Allow future agents to find and select frontend skill/agent; no active authorization by simple listing. | **OPTIONAL, minimal** |
| New frontend-only native guard/hook | Only if actual B1/B2 hook coverage proves a specific gap that cannot be addressed by an existing selector/rule. | **DEFER unless evidence** |
| Future `frontend/AGENTS.md` | Scoped runtime instructions only when H1 creates the app. | **DEFER H1** |

Four small task-bound skills vs a single monolithic frontend skill is proposed so agents need not load design guidance for a pure auth fix; reviewer may consolidate if overlap or instruction overhead is demonstrated. Avoid duplicating whole Vercel/UI UX Pro Max repositories. No general frontend test harness **execution** is possible until H1 app exists; H0 can fully review definitions and agent/hook dry-runs only.

## H0 invariant checks

1. A future frontend agent cannot run based solely on `frontend/` in a roadmap; it must read `ACTIVE.md` and a valid H1 executable handoff.
2. H0 agent scope must not grant `git add/commit/push/tag`, PR/merge, activation or hidden script/remote-install privileges.
3. Design review references D11–D22 and requires a version-bound Owner visual approval, not a Codex/Antigravity self-approval.
4. Existing hook/test security guard remains effective under trusted Windows CLI; record tool-specific verification and limitations. A mocked hook test is not proof of real Codex/Antigravity interception.
5. Documentation/skill additions must not add a new dependency edge into frozen Spring backend or redefine Personal/Drive ownership.
6. H0 stays independent of installer choice (npm/pnpm), design tool and exact UI token values until H1 review selects and pins them.

**Next step:** Codex decide exact minimal target files and failure tests, not implement them in preparation review.
