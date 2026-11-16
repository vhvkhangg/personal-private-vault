---
trigger: model_decision
description: "Apply when preparing, planning, implementing, testing, remediating, or reviewing a development phase."
---

**Current authority:** [Owner policy](../../docs/workflow/current-owner-delivery-policy.md)
and [ACTIVE](../../docs/implementation/handoffs/ACTIVE.md), not old S19-1-only
checkpoints. Ordinary work retains independent slice/phase review and three
Antigravity corrective pairs; mandatory self-review is not independent.
Current Non-RAG H/D/B/X audits require exact phase grants and distinct Codex
inspector/author/reviewer sessions through the existing
[audit skill](../skills/codex-backend-audit/SKILL.md). Its skill-local contract and
audit resume precede generic templates/Antigravity dispatch. Every actionable
in-scope LOW/INFO needs resolution or STOP for Owner; no automatic D0/B0/X.
Historical 2-round/20-minute and Phase 15 execution routes are inactive.




# Agent Handoff Workflow

For ordinary non-audit Phase 3+ work (audit dispatch above comes first):

1. ChatGPT prepares next-phase docs/tooling.
2. Codex `$codex-pre-handoff-review`.
3. Owner commits/pushes preparation after `READY FOR HANDOFF`.
4. Codex `$codex-create-handoff`.
5. Antigravity implements/tests from `docs/implementation/handoffs/ACTIVE.md`.
6. Codex `$codex-final-review`.
7. Owner commits/pushes after `READY FOR OWNER COMMIT`.
8. ChatGPT closes/freezes the phase and prepares the next one.
9. Historical milestone reviews run after Phase 3/6/9/12.
10. Historical Phase 15 used an audit-first gate. It is now closed; execution or
    recovery requests STOP / HISTORICAL_ONLY without tools/ACTIVE mutation.
    Original steps/evidence remain in docs/implementation/phase-15/ and Git history.
11. Phase 15 supersedes the former post-Phase-15 milestone. Frontend/RAG are Phase 16/17 and remain owner-gated.

## Owner's future RAG workflow additions (2026-10-17)

- Phase 17 is closed/frozen. For future 18–22, pre-handoff review must inspect new and applicable old docs, skills, rules, hooks, agents, selected code and tests, and issue concrete KEEP/SIMPLIFY/DEFER/REJECT/OWNER DECISION REQUIRED verdicts before handoff.
- The initial Antigravity implementation does not use a remediation round. After **three** completed Antigravity remediation and independent Codex re-review rounds still block on the same active slice, use `$codex-implementation-takeover` in a separate Codex implementation session. A separate independent reviewer must accept that code.
- Codex Full Access / Antigravity `--dangerously-skip-permissions` are owner-preferred execution modes, not changes to commit/security/scope authority.
- Use `python-rag-coding` for Python and `bounded-command-execution` for long commands. **Published Maintenance B1 runner** enforces 300s ordinary cap and 300s no-progress diagnosis, with up to 2,100s only for approved extended verification; B2 native CLI/hook coverage is conditional on supported Windows/root/trusted startup. Store long custom tasks in `docs/implementation/agent-launcher-prompt.md` and send a short launcher.
- See `docs/workflow/rag-phase-delivery-guardrails.md`; Phase 18 still requires owner authorization.

Do not bypass preparation or the active handoff by expanding scope from TODOs, tests, Graphify, or inferred needs.


Every Codex/Antigravity workflow invocation must end by stating the exact **Next step** for the owner. The action must
come from the current canonical gate/status rather than a guessed future task.
