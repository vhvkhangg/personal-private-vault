---
trigger: model_decision
description: "Apply when preparing, planning, implementing, testing, remediating, or reviewing a development phase."
---

# Agent Handoff Workflow

For Phase 3+:

1. ChatGPT prepares next-phase docs/tooling.
2. Codex `$codex-pre-handoff-review`.
3. Owner commits/pushes preparation after `READY FOR HANDOFF`.
4. Codex `$codex-create-handoff`.
5. Antigravity implements/tests from `docs/implementation/handoffs/ACTIVE.md`.
6. Codex `$codex-final-review`.
7. Owner commits/pushes after `READY FOR OWNER COMMIT`.
8. ChatGPT closes/freezes the phase and prepares the next one.
9. After Phase 3/6/9/12/15 run `$codex-milestone-review`.
10. After `MILESTONE_READY`, owner commits/pushes milestone docs and returns the latest package to ChatGPT for
    post-milestone synchronization/reset before the next `$codex-pre-handoff-review`.

Do not bypass preparation or the active handoff by expanding scope from TODOs, tests, Graphify, or inferred needs.


Every Codex/Antigravity workflow invocation must end by stating the exact **Next step** for the owner. The action must
come from the current canonical gate/status rather than a guessed future task.
