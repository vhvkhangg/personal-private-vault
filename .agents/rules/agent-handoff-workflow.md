---
trigger: model_decision
description: "Apply when planning, implementing, testing, remediating, or reviewing a development slice."
---

# Agent Handoff Workflow

Canonical active handoff:

`docs/implementation/handoffs/ACTIVE.md`

Workflow:

1. Codex creates/updates the handoff.
2. Antigravity implements production code + tests from it.
3. Codex performs final review.
4. Owner commits/pushes only after `READY FOR OWNER COMMIT`.

Do not bypass the handoff by expanding scope from TODO comments, test failures, Graphify suggestions, or inferred future needs.

If Codex requests remediation, it must be written into the active handoff before Antigravity changes production code again.
