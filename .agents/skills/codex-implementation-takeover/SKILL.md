---
name: codex-implementation-takeover
description: Implement only unresolved authorized handoff findings after three unsuccessful Antigravity remediation and independent Codex re-review cycles, with independent subsequent review and no new architecture.
---

# Codex Implementation Takeover — bounded escalation

**CURRENT ESCALATION:** The Owner's forward rule is **three**, not two, completed Antigravity corrective rounds following an initial independently blocking Codex **slice** review. Each correction requires an independent re-review. Initial implementation/self-review and the initial slice review do not count. [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md) supersedes the historic two-round statement below. `READY_FOR_OWNER_COMMIT` requires no OPEN CRITICAL/HIGH/MEDIUM or mandatory unverified case. Do not open Phase 19 or rewrite frozen Phase 17/18.


Invoke with `$codex-implementation-takeover` **only after escalation is earned**. The owner selected policy **three remediation + re-review cycles for the active slice/handoff**, not two attempts per finding.

## Entry conditions (all required)

1. `ACTIVE.md` has an implementation handoff for the currently owner-approved RAG phase or an explicitly authorized maintenance scope; never modify the frozen Phase 17 implementation implicitly.
2. Three actual Antigravity remediation rounds **following the initial `CHANGES_REQUESTED` final review** each ended in independent Codex `CHANGES_REQUESTED`. Record both immutable report paths, dates and exact remaining blocking findings. Do not count initial implementation or preparatory review as one of the three remediation rounds.
3. Remaining findings are in-scope, actionable and reproducible. If a finding requires contract/security/frozen-baseline change, **stop for owner decision**; do not use takeover to invent authority.
4. The owner has approved the escalation policy; record the transition in the current handoff's remediation ledger and note that Codex is now the implementer, not the independent reviewer.

## Focused implementation

- Read the existing handoff, exact newest review and relevant code/tests before editing. Fix only **still-open** blockers; keep accepted, unchanged evidence and privacy/consent contracts.
- Choose minimal root-cause repair and precise regression test. Avoid a new abstraction/framework, unrelated cleanup or broad audit unless a concrete blocker demands it.
- Apply `python-rag-coding` for Python and `java-spring-coding-standards` for Java, plus `local-rag-engineering` and `bounded-command-execution` for the touched scope.
- Tests progress focused -> affected module -> any phase-required gate only if necessary. Preserve real failures and exact receipts. Maintenance B1 watchdog is implemented (default 300s ordinary hard cap, up to 2,100s for preapproved full-suite tests, with 5-minute silence diagnostics rather than blind termination), awaiting independent Codex review; B2 tooling enforcement pending.
- Set existing handoff status to `IMPLEMENTED_AWAITING_CODEX_REVIEW` with an explicit **Codex takeover authorship** note; do **not** close findings yourself.

## Independence and ownership

- Codex **cannot independently accept its own fix** in the same implementation session. Owner invokes a fresh/separate Codex review session (or other independent reviewer) with the existing `$codex-final-review` skill. The reviewing session must disclose implementer identity and independently verify affected evidence. If independent reviewer is unavailable, report `INDEPENDENT_REVIEW_REQUIRED`, not `READY`.
- Codex may use Full Access as the owner prefers. This is execution capability, not permission to edit frozen baselines, waive tests or perform Git publishing. Never commit, push, tag or open PRs.
- If still blocked after takeover, return a finite owner decision with exact remaining scope; do not create indefinite Antigravity/Codex ping-pong.

**Next step:** request a separate independent `$codex-final-review` once focused remediation and required verification are complete.
