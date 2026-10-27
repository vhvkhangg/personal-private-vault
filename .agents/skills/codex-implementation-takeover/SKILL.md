---
name: codex-implementation-takeover
description: Implement only unresolved authorized handoff findings after three unsuccessful Antigravity remediation and independent Codex re-review cycles, with independent subsequent review and no new architecture.
---

# Codex Implementation Takeover — bounded escalation

**CURRENT ESCALATION:** The Owner's forward rule is **three**, not two, completed Antigravity corrective rounds following an initial independently blocking Codex **slice** review. Each correction requires an independent re-review. Initial implementation/self-review and the initial slice review do not count. [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md) supersedes historical two-round records; no obsolete two-round instruction is operative here. `READY_FOR_OWNER_COMMIT` requires no OPEN CRITICAL/HIGH/MEDIUM or mandatory unverified case. Do not open Phase 19 or rewrite frozen Phase 17/18.


Invoke with `$codex-implementation-takeover` **only after escalation is earned**. The owner selected policy **three remediation + re-review cycles for the active slice/handoff**, not two attempts per finding.

## Entry conditions (all required)

1. `ACTIVE.md` has an implementation handoff for the currently owner-approved RAG phase or an explicitly authorized maintenance scope; never modify the frozen Phase 17 implementation implicitly.
2. Three actual Antigravity remediation rounds **following the initial `CHANGES_REQUESTED` final review** each ended in independent Codex `CHANGES_REQUESTED`. Record all three immutable corrective/re-review report paths, dates and exact remaining blocking findings. Do not count initial implementation or preparatory review as one of the three remediation rounds.
3. Remaining findings are in-scope, actionable and reproducible. If a finding requires contract/security/frozen-baseline change, **stop for owner decision**; do not use takeover to invent authority.
4. The Owner has explicitly approved this particular bounded takeover scope (policy alone is not sufficient authorization); record the transition in the current handoff's remediation ledger and note that Codex is now the implementer, not the independent reviewer.

## Focused implementation

- Read the existing handoff, exact newest review and relevant code/tests before editing. Fix only **still-open** blockers; keep accepted, unchanged evidence and privacy/consent contracts.
- Choose minimal root-cause repair and precise regression test. Avoid a new abstraction/framework, unrelated cleanup or broad audit unless a concrete blocker demands it.
- Apply `python-rag-coding` for Python and `java-spring-coding-standards` for Java, plus `local-rag-engineering` and `bounded-command-execution` for the touched scope.
- Tests progress focused -> affected module -> any phase-required gate only if necessary. Preserve real failures and exact receipts. Maintenance B1/B2 are independently accepted and Owner-published: ordinary 300s hard cap, approved full-suite up to 2,100s with inactivity diagnosis, and scoped Windows hook enforcement.
- Set existing handoff status to `IMPLEMENTED_AWAITING_CODEX_REVIEW` with an explicit **Codex takeover authorship** note; do **not** close findings yourself.

## Independence and ownership

- Codex **cannot independently accept its own fix** in the same implementation session. Owner invokes a fresh/separate Codex review session (or other independent reviewer) with the existing `$codex-final-review` skill. The reviewing session must disclose implementer identity and independently verify affected evidence. If independent reviewer is unavailable, report `INDEPENDENT_REVIEW_REQUIRED`, not `READY`.
- Codex may use Full Access as the owner prefers. This is execution capability, not permission to edit frozen baselines, waive tests or perform Git publishing. Never commit, push, tag or open PRs.
- If still blocked after takeover, return a finite owner decision with exact remaining scope; do not create indefinite Antigravity/Codex ping-pong.

**Next step:** request a separate independent `$codex-final-review` once focused remediation and required verification are complete.

## Documentation synchronization checkpoint — Maintenance B3 amendment

Follow the shared [Documentation synchronization contract](../../../docs/workflow/documentation-synchronization.md) **at this skill's handoff/review boundary**, within Owner-approved scope and without a repository-wide cleanup. Inspect affected current docs against the bounded corrective changes, update only docs explicitly covered by takeover authorization, and hand off any other drift. Preserve implementer/reviewer separation and do not claim publication or close historical findings without evidence. Record the concise **Docs Sync Check** in the existing handback/review artifact (or identify why no docs are affected). Keep the **Next step** routed to the exact remaining documentation action when necessary.

## Forward next action and interruption recovery — Maintenance B3

At the end of **every completed, blocked or interrupted invocation**, output one concrete line with all fields:

`Next step: <application/person> | <exact skill command or Owner action> | <scope + canonical artifact> | <gate/prerequisite> | <remediation or takeover counter, if applicable>`

**Route for this skill:** After implementation + own self-review, return `INDEPENDENT_REVIEW_REQUIRED`: Owner opens a SEPARATE Codex session using `$codex-final-review`. On incomplete work, stop with remaining blockers and attempt counter; never independently accept own fix or increment the counter for a CLI interruption.

**Interrupted session:** the Owner may paste the English [resume prompt](../../../docs/workflow/resume-prompts/codex-implementation-takeover.md). Resume only from inspected files/evidence, with the current approved scope and existing counter; capacity/tool errors do not authorize a new round or reset evidence. Do not overwrite logs or bypass native safety hooks. A standalone skill command (without a special prompt) remains sufficient for an uninterrupted run.
