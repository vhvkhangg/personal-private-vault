# Maintenance B — pre-handoff review entry checklist

- **State:** `READY_FOR_HANDOFF` — preparation independently reviewed by Codex, 2026-10-26. The [dated independent report](reviews/2026-10-26-maintenance-b-independent-pre-handoff-review.md) is the formal result; PASS below concerns preparation, not implemented runtime behavior.
- **Owner-approved scope:** [Maintenance B README](README.md), [B1](slice-b1-watchdog.md) and [B2](slice-b2-tooling.md).
- **GitHub main baseline:** `6fe8dbce24ae9ab43290d76172e2c39678246bbd`.
- **Active implementation handoff:** `NO_ACTIVE_HANDOFF` at preparation time.
- **Purpose:** Codex should resolve any *actual* execution-contract ambiguity before creating an implementation handoff, avoiding repeated Owner decisions during Antigravity work.

## Required independent Codex checks

| Area | Question / evidence Codex must supply | Outcome |
| --- | --- | --- |
| Owner scope | Are B1 and B2 bounded to approved watchdog and existing CLI guards/output, without Phase 19 or RAG runtime changes? | `PASS` — two bounded tooling slices; explicit exclusions retained. |
| Existing implementation | Confirm wrapper, profile eligibility, owned-tree kill, tests, `.codex` policy, Antigravity hook and exact Windows limitations using source, not old handoff claims. | `PASS` — current runner, tests, rules/config and hook inspected; runtime gaps remain implementation work. |
| Simplicity | Does the proposed B1 approach reuse Python stdlib/process APIs and existing wrapper with no daemon/new package unless necessary? Is B2 primarily reuse of existing policy/hook/config? | `PASS` — reuse existing wrapper/guards; no new resident service or agent. |
| Timeouts | 300s normal hard cap, at most 2100s only approved complex/large shape, 300s silence diagnostic, finite shutdown; distinguish `TIMEOUT`, `FAILED`, `HUNG_CONFIRMED`, `DIAGNOSTIC_INCONCLUSIVE` without fabricated success. | `PASS` — limits and conservative diagnosis specified; runtime verification remains mandatory in B1. |
| Windows | Is there a feasible safe native process-tree and subprocess-progress test strategy, including child/grandchild and unrelated sentinel? Is integration on Owner's actual Windows host specified? | `PASS` — native integration and isolation required; neither POSIX nor parser tests substitute for real-client evidence. |
| Security | No log overwrite/leak, no raw argv or environment in receipts, no unsafe real-repo git commit/push/tag probes, no accidental permission escalation. | `PASS` — unique new logs, collision tests, safe isolated probes and bounded permission scope specified. |
| Noise reduction | What tool output can the wrapper/agent control, and what client-rendered `Ran`/diff UI cannot be reliably suppressed? | `PASS` — saved full output and concise summaries separated from client-rendering limits. |
| Tests | Can every acceptance be proven with quick focused synthetic tests and targeted Windows integration checks instead of full backend/RAG suites? | `PASS` — scaled timer/process fixtures and scoped Windows evidence; no unconditional application suite. |
| Routes | No active handoff during preparation; exact `READY_FOR_HANDOFF` producer/consumer agreement; B1 first, B2 after reviewed/published B1; mandatory self-review and one Codex review per slice; final maintenance review only after both. | `PASS` — status contract matches; Owner approval remains separate; B2 remains blocked until B1 publication. |

## Owner decisions and non-goals

All material functional decisions were explicitly approved: time limits, quiet detection vs actual hang, Owner-only Git, retained `logs/`, concise console/full new evidence, two small slices, Antigravity implementation/Codex review, three remediation rounds, severity and milestone backlog. Do not reopen these merely to generate new decisions; ask the Owner only if an unavoidable factual environment constraint creates a *new* safety/function trade-off.

Do not demand application-wide test suites or new benchmark systems. No Phase 19, indexing activation, GPU, OCR, dashboard, agent, database or module design changes.

## Expected Codex result

- Write one dated independent pre-handoff report in this maintenance scope's `reviews/`.
- `READY_FOR_HANDOFF`: verified finite preparation, exact maintenance README status updated; then separately invoke `$codex-create-handoff` for B1 only under the already-approved Owner scope.
- `CHANGES_REQUESTED`: list CRITICAL/HIGH/MEDIUM evidence and the smallest document fixes; update canonical maintenance status and hand back to ChatGPT. Do not activate `ACTIVE.md` or implement code while blocked.
- Report short summary and one Conventional Commit message only if applicable to the accepted preparation.
