# Agents, skills and rules — current owner workflow

**Phase 17 FROZEN; Phase 18 CLOSED; Phase 19 NOT AUTHORIZED.** Canonical: [Owner Delivery Policy](../docs/workflow/current-owner-delivery-policy.md); [ACTIVE handoff](../docs/implementation/handoffs/ACTIVE.md). Older agent status/runbook text remains accessible in the Git history, not as active permission.

## Roles and gates

- ChatGPT clarifies Owner decisions and prepares docs; Codex pre-handoff review + precise handoff; **Antigravity implements and always self-reviews**; Codex independently reviews **each slice**, conducts **phase final review** after all slices, **RAG milestones after P19 and P22**, then RAG-wide and RAG+Backend audit when authorized.
- Remediation: maximum **three** Antigravity correction + independent Codex re-review rounds **per active slice**. Initial implementation/self-review and initial Codex review do not consume this budget. Codex takeover is last resort with separate independent reviewer.
- CRITICAL/HIGH/MEDIUM open defects or mandatory evidence NOT_VERIFIED block `READY_FOR_OWNER_COMMIT`; LOW/INFO backlog is consolidated at milestone/audit.
- Only Owner runs Git stage/commit/push/tag/PR publication. Agents do not perform these operations or a separate staged-tree review. Project hook/execpolicy are not proof against every shell/connector bypass until actual Windows integration verification.

## Entry points

- Antigravity: `.agents/agents/backend-implementer/agent.md`, `.agents/skills/antigravity-implement-handoff/SKILL.md` and mandatory `.agents/skills/antigravity-self-review/SKILL.md`.
- Optional **read-only** targeted architecture audit: `.agents/agents/architecture-auditor/agent.md`; not an automatic step for every slice.
- Codex: `codex-pre-handoff-review`, `codex-create-handoff`, `codex-final-review` (slice and phase final), `codex-milestone-review` and `codex-review-coverage`. Historical `codex-backend-audit` remains for accepted Phase 15; takeover only after three failed correction reviews.
- Reuse actual applicable Java/Python/SOLID, design-pattern, Modulith, persistence, security and testing skills. Do not add an agent or subsystem because it appears in an old ADR.
- Documentation authoring: `.agents/skills/documentation-writing/SKILL.md`, `docs/templates/`. Complete skill/rule/agent inventory: [maintenance harness audit](../docs/implementation/maintenance/2026-10-25-post-phase18-simplification/harness-review-2026-10-25.md).

## Tools status

- `.agents/hooks.json` calls `hooks/repository_safety.py`; existing hook and tests remain unchanged in Maintenance A; actual Windows interception and complete owner-Git denial still require Maintenance B tests.
- `scripts/run-bounded-command.py`: **current** normal 1,200s/full-suite 2,100s fixed waits; approved **future** default 300s, preauthorized complex work <=2,100s, diagnose *every* 300s of no output/progress. **Not yet implemented**.
- `logs/` is ignored by Git but Owner explicitly keeps all its existing contents unchanged.
- Graphify is optional navigation context, not authority or a prerequisite for every task.
