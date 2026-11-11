# PPV frontend development workflow

**Current execution — 2026-11-11:** `BACKEND_FIRST / FRONTEND_PAUSED_BY_OWNER / NO_ACTIVE_HANDOFF` under [BF26](../implementation/maintenance/2026-11-11-backend-first-assessment/owner-decision-backend-first-2026-11-11.md). H1-F is indefinitely paused despite Owner publication; `CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED` and FR-13 `UPSTREAM_DEPENDENCY_BLOCKED` persist. Preserve source, tests/harness, dependencies/lock, evidence, counters and approved v0.3. A redesign, including any future Gemini candidate, requires a separate Owner visual decision. No new design or dependency work is authorized. The H0/H1 sequencing below is retained workflow history/guidance; it does not resume execution.

**Authority:** [Owner policy](current-owner-delivery-policy.md), [ACTIVE](../implementation/handoffs/ACTIVE.md) and the active executable contract govern execution. This workflow is H0 guidance, not H0 acceptance, visual approval or authority to create an app. [D11–D22](../implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/owner-decisions-and-visual-gate.md) preserve the Owner's directions and nondelegable visual gate.

## Scope and sequence

1. H0 implements only the named frontend harness; implementer tests and self-reviews, separate independent Codex reviews, then Owner publishes and the brief closeout records the actual commit. H0 contains no `frontend/`, package/runtime/BFF/UI/mockup/assets. The [H0 exception](../implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/owner-authorization-exceptional-codex-h0-2026-11-07.md) authorizes Codex as this initial implementer only; it does not waive native evidence or independent review.
2. Separately authorized visual work creates concrete versioned desktop/small-screen designs and state coverage for Owner feedback. Directions and provisional placement are not approval. Revisions have no automatic cap. Use the single reusable record below in later H1 design/preparation; H0 creates no actual H1 record/folder.
3. **H1 remains BLOCKED** until H0 is independently accepted and Owner-published, H1 preparation is independently accepted and Owner-published, its distinct executable handoff is READY, and the Owner explicitly approves the concrete inspected visual version. All are required; published H0 alone cannot start H1. Material visual changes need approval of the changed version.
4. H1 may then implement only its approved S3 scope and exact design; risk-based tests, mandatory implementer self-review and separate independent `$codex-final-review` precede Owner publication. The standard three correction/re-review rounds follow an initial blocking slice review; neither design iterations nor initial self-review consume them. No automatic takeover or reused S1/S2 exception.

Spring keeps Authentication/Drive business and policy authority. Personal → Storage → Google Drive is provisional UI grouping only. Drive/Drive Files/Feed stay false/unset; actual Google/Cloud/consent, HTTPS/deployment, RAG and unrelated Phase 16 features remain separately gated.

## Client entry, selection and precedence

Start the actual client at the trusted repository root with its existing policy/hooks. Do not add permission overrides or repair trust/configuration to make a check pass. Read AGENTS, ACTIVE and the active contract before using the [frontend role](../../.agents/agents/frontend-implementer/agent.md). The role inherits model and existing execution policy, grants only available tools and no new privilege.

| Task, including pre-app planning | Read only the applicable guidance |
| --- | --- |
| Next/App Router, typed components, caching/performance | [Engineering skill](../../.agents/skills/frontend-nextjs-engineering/SKILL.md), [Next rule](../../.agents/rules/frontend-nextjs.md); engineering links testing |
| UI layout/states/Vietnamese/accessibility | [Design skill](../../.agents/skills/frontend-design-accessibility/SKILL.md), [UX rule](../../.agents/rules/frontend-ux-quality.md) and this visual record |
| BFF/auth/cookie/OAuth/upload/download | [Security skill](../../.agents/skills/frontend-bff-security/SKILL.md), [security rule](../../.agents/rules/frontend-security.md) and applicable testing cases |
| Frontend verification planning/execution | [Testing skill](../../.agents/skills/frontend-testing/SKILL.md); no new fourth routing rule |
| Backend-only Java/Python/domain work | Existing [backend role](../../.agents/agents/backend-implementer/agent.md) and applicable backend skills/rules; no frontend rule requirement |

**Codex:** repo `.agents/skills/<name>/SKILL.md` uses name/description discovery and explicit `$frontend-nextjs-engineering`, `$frontend-design-accessibility`, `$frontend-bff-security` or `$frontend-testing` invocation. Confirm actual loaded paths on the installed client; restart/reopen only when ordinary discovery requires it, without changing trust. `.agents/agents/*/agent.md` is not Codex-native agent registration, and `.agents/rules/*.md` is not Codex execpolicy. Explicitly read the selected role/rule and this workflow; do not claim automatic loading of those files. Root AGENTS supplies the discovery link. [Official skill documentation](https://learn.chatgpt.com/docs/build-skills) describes explicit and implicit skill selection; local native evidence must still establish the tested path.

**Antigravity:** use the existing `.agents` role/frontmatter convention and installed client's help/agent list to verify `frontend-implementer` selection (where supported, `agy --agent frontend-implementer`). Rules use existing `trigger: model_decision` with task descriptions so planning works without a `frontend/` glob. Verify actual selection/loading; if automatic discovery is not observed, explicitly read the exact role, relevant rule and SKILL.md paths. A listing or file existence alone is not an executed workflow. Native unavailability is NOT_VERIFIED, not Codex-substituted evidence.

Precedence for this guidance: explicit Owner scope/approvals, current policy + ACTIVE/executable contract, applicable root/scoped instructions and local rules/skills, then optional external references. System/client restrictions still bind. External guidance never authorizes an installer, publish, token exposure or contract change. For a material unresolved policy/product conflict, stop and identify the decision; no invented authority.

## Commands and evidence

Reuse [bounded-command-execution](../../.agents/skills/bounded-command-execution/SKILL.md) and `scripts/run-bounded-command.py`. Potentially long hooks/native/Node/browser commands use normal profile **<=300s**, fresh unique ignored `logs/` receipts/stdout/stderr and owned-tree cleanup. The guarded 2,100s profile is only for eligible explicitly approved Maven/Python full suites; no Node/browser/install exemption or parallel runner. Do not delete or overwrite historical logs. Test server/browser cleanup must target only owned processes.

Reuse `.agents/hooks.json`, the repository safety evaluator/tests and `.codex/rules/owner-git.rules`; do not modify registration, trust, `.codex/config.toml` or Owner-Git rules. Only Owner stages/commits/pushes/tags/releases or opens/merges PRs. Supported native-root proof needs real pre-dispatch tool denials and benign completions with exact versions, selected paths and ambient-policy limits; parser output/model narration alone is insufficient. Never probe real staging/publishing. Use the active contract's harmless help-form controls and watchdog.

H0 runs the contract's focused hook suite and static/manual routing, security, provenance, visual-gate and saved-file checks. Frontend build/type/lint/format/unit/component/BFF/browser/axe/visual tests are NOT_RUN because there is no app. H1's own accepted plan owns those executions and approved local fixtures; no live provider implication. Report actual counts/exits/receipts, failures and mandatory NOT_VERIFIED. Implementer self-review is neither Antigravity-authored when Codex implements nor independent acceptance. Reopen saved ACTIVE, executable handoff and handback, reconcile ID/status/counters/results and Docs Sync, then stop for a **separate independent Codex `$codex-final-review` session**. Mandatory missing evidence blocks READY_FOR_OWNER_COMMIT.

## Owner visual approval record template

Copy this checklist into the separately authorized H1 design/preparation record, not into H0 as an approval. Keep evidence fields empty until real evidence exists. The default is **NOT_DESIGN_APPROVED**. Only the Owner's explicit acceptance can support a later recorded DESIGN_APPROVED; an agent may transcribe and an independent reviewer may verify it, neither may supply it.

| Record field | Value to fill from actual evidence |
| --- | --- |
| Scope / status | GDI26-S3-H1 initial S3 visuals only / **NOT_DESIGN_APPROVED** |
| Design artifact(s) / immutable version or hash / date | — |
| Inspected desktop and small-screen artifact refs / viewport sizes | — |
| Approved token/state sheet versions linked to those artifacts | — |
| Login/session; slot/root; loading/empty/list/cursor; upload progress/success/conflict/unknown/size; download/callback/error; responsive navigation coverage | — |
| Accessibility evidence: keyboard/focus, actual contrast, reflow/text resize, reduced motion; unresolved limitations | — |
| Explicit dated Owner approval wording and source identifying this exact version | — |
| Exclusions, rejected portions and requested revisions / resolution evidence | — |
| Independent reviewer / date / applicability verification against exact version and Owner source | — |
| Accepted/published H0 + H1 preparation references and distinct H1 executable handoff | — |

Before H1 implementation verify every applicable field and the four sequence gates above. No Owner message, silence, palette-only approval, rejected layout, wrong-version consent, unfinished revisions or agent opinion each leaves H1 **BLOCKED**. Screenshots/test passes/H0 completion cannot fill the Owner field. A hypothetical fully evidenced record permits only that later approved scope and grants nothing now. Later material changes invalidate applicability until the Owner approves their concrete changed version. Do not impose a fixed visual revision budget.

External source audit and pinning: [H0 provenance manifest](../implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/frontend-guidance-provenance-h0-2026-11-07.md). Refresh compatibility against actual H1 package versions when authorized; no external installer is required by this workflow.
