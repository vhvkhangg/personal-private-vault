# BF26-NR-H1 — Draft Owner decision: bounded Codex-only harness repair

- **ID:** `BF26-NR-H1-HARNESS-REMEDIATION`.
- **Status:** `PROPOSED_FOR_OWNER_SELECTION / H1_NOT_RATIFIED / H1_EXECUTION_NOT_AUTHORIZED`.
- **Starting baseline:** `7c521fb9998bc1ad0a86e7225aa605f0de17e261` (published H0 authorization). This is **not** the execution baseline; it must be replaced with the actual Owner-published H0 reports/review SHA after G-H0.
- **Independent H0 result:** Owner-supplied verdict `H0_ASSESSMENT_ACCEPTED / OWNER_H1_DECISION_REQUIRED` dated 2026-11-16. No H1 reviewer acceptance or Owner H1 ratification yet.

## Proposed exact Owner choices — not yet ratified

| Decision | Recommended outcome | Why / safeguard |
|---|---|---|
| H1-D01 Scope | Authorize remediation of `NRH0-F01`, `NRH0-F02` and the independently narrowed `NRH0-F03` only | All three have an Owner-bounded basis; no speculative cleanup |
| H1-D02 Existing skill | Rewrite `.agents/skills/codex-backend-audit/SKILL.md` to **current Non-RAG H/D/B/X only**; historical `phase-15` invocation must explicitly STOP | Supersedes old runnable mode without rewriting Phase 15 records; no new skill |
| H1-D03 Routing strategy | Use **existing audit skill-local asset**, coupled with explicitly scoped review/handoff/resume routes; preserve ordinary non-audit Antigravity behavior | Avoid editing every generic template or new workflow engine |
| H1-D04 Write paths | Select the **17 existing files** in [allowlist](h1-exact-path-allowlist.md). These are proposed as the *complete initial edit boundary*; no conditional file may be edited without additional exact Owner approval | Finite auditable patch, no opportunistic refactor |
| H1-D05 F02 fix | Extend existing semantic evaluator and tests to deny parsed branch deletion through supported option/wrapper forms, including permission overrides, while preserving benign reads | Observed direct, -C, -c and wrapper gap; parser-only prefixes insufficient |
| H1-D06 F03 scope | Correct current guidance pointers only; preserve explicitly labeled historical sections and unrelated module AGENTS | Independent review narrowed backend/AGENTS applicability |
| H1-D07 Models & hooks | Propose **scoped native Codex CLI/model calls in an isolated fixture**, only if the later Owner H1 ratification expressly permits the exact Codex-service network purpose and bounded scenarios; all app providers and other network access stay prohibited. Until that grant, use static/offline checks only | Actual serving model, effective hook trust and end-to-end enforcement cannot be marked PASS without observed native execution |
| H1-D08 Sandbox/config | Keep `.codex/config.toml`, `.codex/rules/owner-git.rules`, global trust/settings unchanged | H0 did not show sufficient evidence to mandate configuration change |
| H1-D09 Delivery | Do not create a ZIP producer or use `delivery/` for automated packaging | Observed directory empty; no validated automatic trigger |
| H1-D10 Correction budget | Propose **maximum three** bounded author-correction + independent re-review rounds after initial implementation/self-review, tracked for **this H1 only** | Finite STOP, never reset historical counters or authorize takeover |
| H1-D11 Owner-only publication | Owner alone stages/commits/pushes; Codex author and independent reviewer use distinct sessions | Preserve publication and review independence |
| H1-D12 Future phases | H1 acceptance/publication only qualifies harness for subsequently **separately authorized** D0/B0; no automatic audit continuation | Correct phase gates and paused RAG/Frontend boundaries |

## Approval semantics

**This table is a proposal, not the Owner's ratification.** It may be ratified by an explicit **subsequent, dated Owner decision** after independent preparation review and H0 evidence publication. The final Owner approval must identify the reviewed revision, actual published H0 SHA, precise source and output allowlists, test permissions and finite correction budget. A verbal “start” before those gates cannot be interpreted by Codex as unlimited source authority.

Do not convert conditional paths into approved ones because a Codex implementer notices a reference. STOP and present the minimal **path expansion request** including reason, affected callers, desired test and review impact. If full skill correctness cannot be achieved within 17 paths, H1 must be explicitly amended and independently re-reviewed rather than silently shipping an incomplete workflow.

## Prohibited operations

No API/DTO/schema/DB/Java/Frontend/RAG changes, no Phase 15 history edits, no additional skills, no Antigravity audit implementation, no branch/worktree creation, no Git stage/commit/push/PR, no package install or unapproved network/provider calls, no global config/trust modification, no ZIP automation, no deletion of `delivery/` or of previous reports/receipts. Preserve H0 findings/LOW backlog provenance and accepted historical counters.
