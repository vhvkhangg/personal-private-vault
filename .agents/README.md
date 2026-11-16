# Agents, skills and rules — current owner workflow

**Current authority:** [Owner policy](../docs/workflow/current-owner-delivery-policy.md)
and [ACTIVE](../docs/implementation/handoffs/ACTIVE.md). V1 is published/CLOSED;
ACTIVE is NO_ACTIVE_HANDOFF, P17 frozen, P18 closed, S19-2 suspended and H1-F paused.
The [2026-11-16 H1 ratification](../docs/implementation/maintenance/2026-11-16-bf26-nr-h1-harness-preparation/h1-owner-ratification-2026-11-16.md)
grants only bounded harness remediation after entry gates. H1 is not yet
independently accepted/published; no D0/B0/X launch follows. Older
S19-1/publication-pending checkpoints are historical, not current permission.


## Roles and gates

- ChatGPT clarifies Owner decisions and prepares docs; Codex pre-handoff review + precise handoff; **Antigravity implements and always self-reviews**; Codex independently reviews **each slice**, conducts **phase final review** after all slices, **RAG milestones after P19 and P22**, then RAG-wide and RAG+Backend audit when authorized.
- Remediation: maximum **three** Antigravity correction + independent Codex re-review rounds **per active slice**. Initial implementation/self-review and initial Codex review do not consume this budget. Codex takeover is last resort with separate independent reviewer.
- CRITICAL/HIGH/MEDIUM open defects or mandatory evidence NOT_VERIFIED block `READY_FOR_OWNER_COMMIT`; LOW/INFO backlog is consolidated at milestone/audit.
- Only Owner runs Git stage/commit/push/tag/PR publication. Agents do not perform these operations or a separate staged-tree review. Project hook/execpolicy are not proof against every shell/connector bypass until actual Windows integration verification.

**Current Non-RAG audit exception:** only a phase-specific Owner grant selects
distinct Codex inspector/author/independent-reviewer sessions. Use the existing
[audit skill](skills/codex-backend-audit/SKILL.md), its skill-local asset and audit
recovery route; no Antigravity audit dispatch. Every actionable in-scope LOW/INFO
needs resolution or STOP for Owner. Ordinary roles/budgets/backlog stay unchanged.

## Entry points

- Antigravity: `.agents/agents/backend-implementer/agent.md`, `.agents/skills/antigravity-implement-handoff/SKILL.md` and mandatory `.agents/skills/antigravity-self-review/SKILL.md`.
- Optional **read-only** targeted architecture audit: `.agents/agents/architecture-auditor/agent.md`; not an automatic step for every slice.
- Codex: `codex-pre-handoff-review`, `codex-create-handoff`, `codex-final-review` (slice and phase final), `codex-milestone-review` and `codex-review-coverage`. `codex-backend-audit` supports only explicitly granted current Non-RAG H/D/B/X roles; Phase 15 execution STOPs. Ordinary takeover still needs its own authority.
- Reuse actual applicable Java/Python/SOLID, design-pattern, Modulith, persistence, security and testing skills. Do not add an agent or subsystem because it appears in an old ADR.
- Documentation authoring: `.agents/skills/documentation-writing/SKILL.md`, `docs/templates/`. Complete skill/rule/agent inventory: [maintenance harness audit](../docs/implementation/maintenance/2026-10-25-post-phase18-simplification/harness-review-2026-10-25.md).

## Frontend harness discovery (GDI26-S3-H0)

For frontend planning, UI/accessibility, BFF security or testing, start at the [shared frontend workflow](../docs/workflow/frontend-development.md) and [frontend-implementer role](agents/frontend-implementer/agent.md). It routes to four task-bound skills and three thin rules, including explicit Codex reads versus observed Antigravity loading. Existing backend-only routing above remains applicable. A discovery link grants no app/design/activation authority: H0 is harness-only; H1 requires accepted/published H0, its own accepted/published preparation and executable handoff, and explicit Owner approval of the concrete visual version. Dated notices above are history where inconsistent with ACTIVE.

## Tools status

- `.agents/hooks.json` calls `hooks/repository_safety.py`; Maintenance B2 verifies Windows hook execution from root and .agents working directories, staging denial (`git add`), and Codex execpolicy rules (`.codex/rules/owner-git.rules`).
- `scripts/run-bounded-command.py`: Maintenance B1 published: default 300s ordinary cap, pre-authorized full-suite <=2,100s, progress/inactivity diagnosis and owned-tree cleanup. B2 output contracts verified.
- `logs/` is ignored by Git but Owner explicitly keeps all its existing contents unchanged.
- Graphify is optional navigation context, not authority or a prerequisite for every task.
