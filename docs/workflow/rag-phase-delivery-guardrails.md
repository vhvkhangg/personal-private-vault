# RAG Phases 18–22 — proportional delivery and escalation policy

**Owner decisions: 2026-10-17 (1A / 2A / 3A).** Workflow/tooling guidance only, **not Phase 18 implementation authorization**, a new handoff, or an amendment to frozen Phase 17 contracts. `ACTIVE.md` remains `NO_ACTIVE_HANDOFF` until a separate approved gate.

## Objective and baseline

This is one owner's Windows desktop, one permanent account, one active manual RAG job; do not model a distributed SaaS, Kubernetes/control-plane estate, multiple organizations or speculative provider/plugin system. Retain explicit source consent, Private/NSFW never indexed, limited approved fields, current source guard/reservations, positive worker/process lifetime, guarded publication/revocation/recovery, no unauthorized network/source consumption, and local PostgreSQL authority. **Practical Local Offline** remains the approved level; do not resurrect strict WFP/Firewall/global DNS packet isolation. Preserve accepted Phase 17 E1 and default containment. No new ingestion/query/recommendation feature becomes authorized before its own phase handoff.

## Pre-handoff review: mandatory evidence and proportionality

Codex MUST inspect *new and applicable existing* docs, README, ADRs, rules, hooks, `.agents/skills/`, `.agents/agents/`, root/scoped `AGENTS.md`, selected source and tests, package tree, version/lock/config files, acceptance/evidence criteria and historical approved phase boundaries. A generic "review 29 dimensions" is not enough: trace **which** documents/skills were read, what applies to this phase and what is intentionally N/A.

For each proposed deliverable and nonfunctional verification, return a short matrix with these decisions:

| Decision | Meaning |
| --- | --- |
| KEEP — mandatory | Necessary for current user-visible behavior, correctness/privacy or an accepted contract; specific evidence/test. |
| SIMPLIFY | Same guarantee with a smaller implementation/test, explain the tradeoff. |
| DEFER — optional | Not required for current phase or measured problem; candidate later, not a new blocker. |
| OWNER DECISION REQUIRED | Would amend approved phase contract, frozen boundary, privacy/security semantics, significant resource budget or acceptance gate. Do not apply automatically. |
| REJECT | Generic overengineered machinery or duplicate infrastructure with no concrete current requirement. |

Evidence threshold scales with risk: cheap deterministic parser fixtures for ordinary formats; native/real DB concurrency only when the changed behavior crosses those boundaries; benchmark samples only for a **named acceptance target** and realistic local workload. Reuse unchanged independent evidence with hashes/clear provenance. A missing speculative metric is not automatically a blocker. Never claim an unrun test passed. Security/privacy/correctness guarantees cannot be waived by the efficiency policy; unresolved essential tests remain blocking.

Before writing the handoff, cap scope to the smallest usable *vertical slice* within that numbered phase. Clearly distinguish MUST/SHOULD/DEFERRED, permitted integrations, synthetic fixtures, and one named final verification command. Full back-to-back monolithic suite reruns are not the default debugging loop. Do **not** add acceptance obligations during final review unless they trace to the approved contract or a demonstrated serious defect; new owner choices get an explicit proposal.

## Two-remediation-round policy (global per phase)

1. Initial Antigravity implementation → initial independent Codex final review (not a remediation round).
2. On initial `CHANGES_REQUESTED`, Antigravity remediation **round 1** → independent Codex re-review **1**.
3. If still `CHANGES_REQUESTED`, Antigravity remediation **round 2** → independent Codex re-review **2**.
4. If still blocking: **stop Antigravity implementation** and escalate unresolved approved findings to `$codex-implementation-takeover` in an implementation session. Reviewer session must be independent of its own implementation. No third Antigravity remediation loop unless the owner explicitly changes policy.

Each reviewer must state immutable review report paths, remaining blocking findings, whether they are defects/contract gaps/environmental issues and the elapsed remediation round. Track the counters in the *active phase handoff*, not in frozen Phase 17 or in global arbitrary state. A clean acceptance at any earlier step exits immediately. When findings require an owner architecture/contract/security decision, pause before additional loops; the round limit does not grant unilateral contract changes. Recognize unrelated pre-existing environmental issues separately and never mislabel a failed gate PASS.

## Agent execution/permission preferences

The owner uses **Codex CLI Full Access** and **Antigravity CLI `--dangerously-skip-permissions`** by choice. These flags change prompts/OS tool approvals, **not** instruction authority. Agents still respect accepted handoff targets, preserve sensitive content, do not commit/push/tag/PR, and do not change OS security/firewall or run unrestricted installers without explicit need. Codex review remains read-only except writing review/status artifacts, while a separately authorized Codex takeover session can implement.

## Timeouts and test budgets

Ordinary external command cap is **1,200 seconds**; only future-phase required full-suite tests can use **2,100 seconds**. Launch potentially long commands via [bounded-command-execution](../../.agents/skills/bounded-command-execution/SKILL.md) and `scripts/run-bounded-command.py`, not just a comment. The host CLI must allow 35 minutes when selected. On timeout, record failure, stop *owned* processes best-effort, inspect evidence and resolve the blocker; do not rerun identical commands to get lucky PASS. Frozen Phase 17's 20-minute contract stays unchanged. Run relevant focused tests first, affected-module tests second, clean suite once after convergence when specified.

## Stable compatible version rule

Apply accepted [ADR-0013](../adr/0013-stable-compatible-version-policy.md): prefer the newest **stable mutually compatible** set, normally LTS JVM; confirm Windows x64 wheel/ABI/GPU/CPU runtime availability, PostgreSQL+pgvector image pairing, Spring Boot/Modulith BOM, offline licensing/redistribution and pinned `uv.lock`. Do not interpret "newest" as automatically migrating JDK 25 LTS → 27 feature release or Python 3.14 → just-published 3.15. Exact version changes require an approved phase reason and compatibility/regression evidence. Latest-stable availability is checked at future phase preparation; historical Phase 17 baseline is not silently upgraded.

## Prompt-file workflow and next steps

Prefer the named skills. If a task needs a long custom prompt, place the complete, versioned task in [`agent-launcher-prompt.md`](../implementation/agent-launcher-prompt.md) (or an agreed phase-scoped file), then issue only a **short launcher** specifying that exact repo path and skill/role. Rewrite the prompt body for each new task rather than appending stale instructions; never launch from a placeholder. Keep cited acceptance contracts authoritative over generated prompts.

**This document does not create Phase 18 scope or a Codex implementation handoff.** The owner must explicitly authorize preparing Phase 18 before those files or gates are created.
