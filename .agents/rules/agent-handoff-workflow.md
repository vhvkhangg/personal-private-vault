---
trigger: model_decision
description: "Apply when preparing, planning, implementing, testing, remediating, or reviewing a development phase."
---

**FORWARD OVERRIDE (2026-10-25):** [Owner policy](../../docs/workflow/current-owner-delivery-policy.md) governs future work. Independent Codex review **after each slice**; phase final review after all slices, milestone after RAG P19/P22, then RAG and RAG+Backend audits. Antigravity mandatory self-review does not count; **three** remediation + independent re-review rounds **per active slice** before takeover. CRITICAL/HIGH/MEDIUM block. Historical 2-round/20-minute instructions below are no longer active. Phase 19 NOT AUTHORIZED.



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
9. Historical milestone reviews run after Phase 3/6/9/12.
10. Phase 15 is an audit-first exception: run `$codex-pre-handoff-review` on its preparation; success is
    `READY FOR AUDIT`. After owner commit/push of accepted preparation, run `$codex-backend-audit`. Codex creates a
    remediation handoff only from proven authorized findings; after remediation final review, rerun the backend audit
    until `BACKEND_AUDIT_READY`.
11. Phase 15 supersedes the former post-Phase-15 milestone. Frontend/RAG are Phase 16/17 and remain owner-gated.

## Owner's future RAG workflow additions (2026-10-17)

- Phase 17 is closed/frozen. For future 18–22, pre-handoff review must inspect new and applicable old docs, skills, rules, hooks, agents, selected code and tests, and issue concrete KEEP/SIMPLIFY/DEFER/REJECT/OWNER DECISION REQUIRED verdicts before handoff.
- The initial Antigravity implementation does not use a remediation round. After **three** completed Antigravity remediation and independent Codex re-review rounds still block on the same active slice, use `$codex-implementation-takeover` in a separate Codex implementation session. A separate independent reviewer must accept that code.
- Codex Full Access / Antigravity `--dangerously-skip-permissions` are owner-preferred execution modes, not changes to commit/security/scope authority.
- Use `python-rag-coding` for Python and `bounded-command-execution` for long commands. **Currently installed runner** still enforces old 20/35-minute profiles; **approved future Maintenance B** will implement 5-minute ordinary cap and 5-minute inactivity diagnostics, with up to 35 minutes for preapproved complex tests. Store long custom tasks in `docs/implementation/agent-launcher-prompt.md` and send a short launcher.
- See `docs/workflow/rag-phase-delivery-guardrails.md`; Phase 18 still requires owner authorization.

Do not bypass preparation or the active handoff by expanding scope from TODOs, tests, Graphify, or inferred needs.


Every Codex/Antigravity workflow invocation must end by stating the exact **Next step** for the owner. The action must
come from the current canonical gate/status rather than a guessed future task.
