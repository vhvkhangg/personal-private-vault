> **FORWARD POLICY (2026-10-25):** The authoritative [Current Owner Delivery Policy](current-owner-delivery-policy.md) supersedes historical 2-round, 20/35-minute blanket watchdog, human adjudication/OCR/GPU, and separate staged review requirements below. The historical Phase 17/18 acceptance, actual receipts, and code are **unchanged**. New user-required RAG targets are manual immutable `.md` search/Q&A + approved Fiction/Film Q&A and same-type recommendation, **not** a generic document-recommendation system. RAG evaluation remains small/risk-based. Phase 19 NOT AUTHORIZED; watchdog runtime still PENDING Maintenance B. Old text retained as date-specific history only.

> **CURRENT POST-PHASE-18 OWNER WORKFLOW — 2026-10-25.** Phase 18 is OWNER-CLOSED under D26/D27; Phase 19 is **NOT AUTHORIZED / NOT STARTED**. The Owner's explicit new working preferences supersede older procedural guidance below for future maintenance and slices. This notice does **not** amend frozen Phase 17/18 implementation contracts or reopen either phase. See [post-Phase-18 maintenance docket](../implementation/maintenance/2026-10-25-post-phase18-simplification/README.md). The adaptive watchdog is a **requested tooling change, NOT YET IMPLEMENTED**; the current executable runner still enforces old fixed caps. No new long-run gate is authorized merely by this notice.

## Effective new owner policy for future approved slices (2026-10-25)

**Three, not two, Antigravity remediation rounds:** initial implementation and its initial Codex review do not count as a remediation round. After a `CHANGES_REQUESTED`, Antigravity may implement corrective rounds 1, 2 and 3, each followed by independent Codex re-review of remaining findings. Stop immediately on acceptance. After the third independently blocked round, ask the Owner whether Codex takeover is warranted; neither the reviewer nor a handoff may self-authorize takeover. This policy is prospective; historical Phase 17/18 counters and decisions stay unchanged.

**Requirements before architecture:** every feature/mandatory test must cite a stated owner-visible requirement, an applicable approved invariant, or a concrete reproduced defect. Codex must specifically evaluate the smaller snapshot/manual/single-process option, the no-new-schema/no-new-agent option, and feature deletion where genuinely outside scope. Ask the Owner questions before freezing the design. Do not turn low-confidence or hypothetical future-scope risks into blocking acceptance criteria.

**Watchdog status:** existing `scripts/run-bounded-command.py` is still a fixed `proc.wait(timeout=...)` implementation; updating these docs does not implement new behavior. Requested future policy is progress-aware, with separate observed output, CPU/process activity, test/report/receipt milestones and targeted thread/DB evidence; finite kill/stop controls remain required. A silent healthy computation is not an automatic hang. Runtime implementation, exact thresholds, and negative tests require a separate Owner-approved maintenance handoff. Never leave a hung process uncontrolled.

**Review/test scale:** targeted deterministic verification first, full suites only after convergence or when explicitly required. The separate staging approval workflow is removed; the Owner performs normal Git add/commit/push after a clean independent final review. Do not replace independent code review with implementer self-review.

**Documentation and evidence:** new files follow `docs/templates/` and [documentation-writing skill](../../.agents/skills/documentation-writing/SKILL.md). Severity and BLOCKED/NOT_VERIFIED/OWNER_DECISION_REQUIRED distinctions are mandatory. Never demand human adjudication automatically. Keep original recorded receipts immutable even if obsolete working checklists are later deleted by an approved inventory.

**Phase boundary:** Phase 18 frozen. Phase 19 remains NOT_STARTED / NOT_AUTHORIZED. No speculative feature or new handoff is created by this document.

---

## Preserved original governance policy (historical; not current for future slices)

**D24 CURRENT - 2026-10-21:** Overall Phase 18 external document target is **Obsidian Markdown `.md` ONLY**. Owner authorizes the new initial **Slice 3 Verified Indexing D+F**; see [D24](../implementation/phase-18/owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing), [executable handoff](../implementation/phase-18/slice-3-verified-indexing-handoff.md), [baseline/applicability/cleanup](../implementation/phase-18/slice-3-markdown-scope-and-baseline-2026-10-21.md). Slice 2/D23 producer-local scope is CLOSED and prior work is **published** at verified main `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`, including unaccepted mixed changes; publication does not accept them. The former selective staging/commit workflow is superseded and must not be reopened. TXT/Office/PDF/OCR are excluded from Slice 3 and final Markdown acceptance; temporarily retain without use/certification, then remove unnecessary implementation/dependencies/tests/fixtures once at Slice 4 end before P18-FINAL. No Slice 4 implementation now. Phase 17 FROZEN; Slice 1/D14 CLOSED; D22 originals NOT_RECOVERED / original preservation FAILED; FR18-3 OPEN; Java publication NOT ACCEPTED; historical full Java gate FAILED until genuine successful rerun; indexing INACTIVE; live originals DISABLED/DEFERRED; P18-FINAL NOT ACCEPTED. Historical Antigravity remediation 2/2 remains exhausted; this initial slice grants no extra remediation rounds. Preparation only in this session; no activation or agent publishing.

## Preserved pre-D24 status and contract history

> **2026-10-19 updated authority:** [ADR-0023](../adr/0023-phase-18-application-level-snapshot-worker-trust.md) applies **only** to individually whole-file-approved external Phase 18 RAG snapshots. For that class, internally marked Private/NSFW/hidden content is within owner approval; the old global private/non-private exclusion below continues for frozen Phase 17 Vault sources. Same-Windows-user Python is permitted; OS-level sandbox/native ABA/DACL G1/G3 proof is not mandatory. Preserve source consent/child grants, tested document-content isolation by application behavior, PostgreSQL role/actual-result checks, and native process lifecycle. Phase 18 implementation requires separate owner approval, now granted by [D13](../implementation/phase-18/owner-decisions.md) **only for Slice 1 A+B+C**; no permission for remaining slices.

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
