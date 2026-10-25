# Maintenance A — exact changed/new file inventory

**Base:** Owner-provided `personal-private-vault(20261025-120122).zip`; GitHub `main` commit `217cb5d24bf7a62d7348d31838a7a79b01a524bd`. These are only changes to docs, agent guides and declarative Codex rule. No Java/Python backend/source, data/schema, executable watchdog, runtime hook/tests, logs, prior Phase 17/18 acceptance or published receipt changed.

**Status:** Docs/harness policy preparation; Owner applies and commits/pushes. **Phase 19 NOT AUTHORIZED**. The new `.codex/rules` file is defense-in-depth and needs Windows CLI validation. The old 1,200/2,100s watchdog is **not** repaired in Maintenance A; the approved 300s/2,100s+inactivity version belongs to Maintenance B.

## Exact paths (relative to repository root)

- `.agents/README.md` (UPDATED)
- `.agents/agents/architecture-auditor/agent.md` (UPDATED)
- `.agents/agents/backend-implementer/agent.md` (UPDATED)
- `.agents/rules/agent-handoff-workflow.md` (UPDATED)
- `.agents/rules/backend-python.md` (UPDATED)
- `.agents/rules/code-reuse-and-patterns.md` (UPDATED)
- `.agents/rules/context-efficiency.md` (UPDATED)
- `.agents/rules/documentation.md` (UPDATED)
- `.agents/rules/tests.md` (UPDATED)
- `.agents/skills/antigravity-implement-handoff/SKILL.md` (UPDATED)
- `.agents/skills/antigravity-self-review/SKILL.md` (UPDATED)
- `.agents/skills/antigravity-test-slice/SKILL.md` (UPDATED)
- `.agents/skills/backend-testing/SKILL.md` (UPDATED)
- `.agents/skills/bounded-command-execution/SKILL.md` (UPDATED)
- `.agents/skills/codex-create-handoff/SKILL.md` (UPDATED)
- `.agents/skills/codex-final-review/SKILL.md` (UPDATED)
- `.agents/skills/codex-implementation-takeover/SKILL.md` (UPDATED)
- `.agents/skills/codex-milestone-review/SKILL.md` (UPDATED)
- `.agents/skills/codex-pre-handoff-review/SKILL.md` (UPDATED)
- `.agents/skills/codex-review-coverage/SKILL.md` (UPDATED)
- `.agents/skills/documentation-writing/SKILL.md` (UPDATED)
- `.agents/skills/local-rag-engineering/SKILL.md` (UPDATED)
- `.codex/README.md` (UPDATED)
- `.codex/rules/owner-git.rules` (NEW)
- `AGENTS.md` (UPDATED)
- `README.md` (UPDATED)
- `docs/AGENTS.md` (UPDATED)
- `docs/README.md` (UPDATED)
- `docs/implementation/README.md` (UPDATED)
- `docs/implementation/agent-launcher-prompt.md` (UPDATED)
- `docs/implementation/handoffs/README.md` (UPDATED)
- `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/README.md` (UPDATED)
- `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/harness-review-2026-10-25.md` (NEW)
- `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/owner-decision-docket.md` (UPDATED)
- `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/planned-doc-cleanup.md` (UPDATED)
- `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/rag-docs-decision-audit.md` (UPDATED)
- `docs/implementation/rag-phase-plan.md` (UPDATED)
- `docs/roadmap.md` (UPDATED)
- `docs/templates/README.md` (UPDATED)
- `docs/templates/adr.md` (UPDATED)
- `docs/templates/audit.md` (NEW)
- `docs/templates/finding.md` (UPDATED)
- `docs/templates/handoff.md` (UPDATED)
- `docs/templates/milestone-review.md` (NEW)
- `docs/templates/owner-decision.md` (UPDATED)
- `docs/templates/phase-final-review.md` (NEW)
- `docs/templates/pre-handoff-review.md` (UPDATED)
- `docs/templates/review.md` (UPDATED)
- `docs/templates/slice-closeout.md` (UPDATED)
- `docs/workflow/agent-development-workflow.md` (UPDATED)
- `docs/workflow/current-owner-delivery-policy.md` (NEW)
- `docs/workflow/owner-phase-workflow.md` (UPDATED)
- `docs/workflow/rag-phase-delivery-guardrails.md` (UPDATED)
- `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/PACKAGE_CONTENTS.md` (NEW — this manifest)

## Validation and intentional preservation

- Existing source hook tests: **13 PASS**; existing watchdog tests: **5 PASS**, but these only cover old behavior (not Windows CLI hook integration or progress detection).
- All relative Markdown links in the package checked against the complete ZIP tree; no missing target files.
- Original `logs/` remains untouched. No file deletions in this package. Historical `docs/D20-PATCH-MANIFEST.json` and D17 adjudication bundle are retained because dated receipts refer to them; no future human judge gate.
- The root `PATCH_TOOLS/`, `PATCH-MANIFEST.json` and `README_PATCH.txt` are absent from the latest Owner ZIP.
- No apply or closeout script is included. Owner extracts ZIP over repository root, reviews the diff and performs Git add/commit/push.

**Suggested commit:** `docs(governance): standardize slice reviews and agent workflow`
