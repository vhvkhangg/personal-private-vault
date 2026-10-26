# Maintenance B — Watchdog & Tooling (post-Phase-18)

- **Status:** **B1 `READY_FOR_OWNER_COMMIT`** after the [separate independent Codex takeover final review](reviews/2026-10-26-b1-independent-takeover-final-review.md), 2026-10-26. All seven findings are independently CLOSED; LOW backlog remains recorded. Antigravity counter stays **`3/3` exhausted**; Owner-authorized Codex F02 takeover is independently accepted, without a fourth round. Owner publication of B1 is next. B2 remains gated on actual B1 publication and its own handoff; Maintenance B overall is not complete.
- **Approved authority:** Owner's explicit authorization to start Maintenance B after publishing Maintenance A.
- **Authoritative source baseline:** GitHub `main` commit [`6fe8dbce24ae9ab43290d76172e2c39678246bbd`](https://github.com/vhvkhangg/personal-private-vault/commit/6fe8dbce24ae9ab43290d76172e2c39678246bbd), verified 2026-10-26. All preparation and subsequent implementation must verify that the local branch matches the accepted baseline or account for drift before acting.
- **Active handoff:** [`MAINT-B1-PROGRESS-WATCHDOG / READY_FOR_OWNER_COMMIT`](../../handoffs/ACTIVE.md), created after accepted preparation was Owner-published at `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`. B1 is independently accepted and awaits Owner publication; this handoff remains B1 only.
- **Owners:** Owner approves scope and alone stages/commits/pushes; ChatGPT authors preparation/governance docs; Codex independently pre-reviews and writes executable handoffs, then independently reviews each implementation slice and the completed Maintenance B; Antigravity implements/tests and always self-reviews. A Codex implementation takeover is not the default.
- **Canonical policy:** [Current Owner Delivery Policy](../../../workflow/current-owner-delivery-policy.md).

## Purpose and two-slice boundary

Fix the actual process execution watchdog and verify command-safety/short-output tooling on the Owner's Windows 11 environment. No production RAG feature, Java business module, schema, migration, model, indexing activation, file ingestion change, network service, dashboard, scheduler, monitoring daemon or new agent is in scope.

| Slice | Owner-approved scope | Primary implementer | Independent gate |
| --- | --- | --- | --- |
| [B1 — Progress-aware bounded commands](slice-b1-watchdog.md) | Existing Python wrapper, owned process-tree safety, bounded progress/diagnostics, focused tests and corresponding executable instructions | Antigravity; Owner-authorized Codex F02 takeover | Codex **slice review**; Owner commit/push |
| [B2 — CLI output and Git/hook enforcement](slice-b2-tooling.md) | Codex/Antigravity actual Windows client behavior, existing rules/hook and tests, concise tool output and status receipts | Antigravity | Codex **slice review**; Owner commit/push |

After the two Owner-published slices: **one Codex Maintenance B final review** aggregates the prior independent slice evidence, validates cross-slice contract consistency and only reruns relevant focused checks on demonstrated drift. Owner records a concise maintenance closeout. A new numbered RAG phase is **not** implicitly authorized.

### Current implementation truth (before B1/B2)

- `scripts/run-bounded-command.py`: `NORMAL_CAP = 1200`, `FULL_CAP = 2100`; `proc.wait(timeout=timeout)`; no inactivity monitor, output/health sampling or diagnostic gate. Its existing full-suite shape guard and best-effort **owned-tree only** cleanup remain important; see [B1](slice-b1-watchdog.md).
- `scripts/test_run_bounded_command.py` contains existing smoke, timeout, profile and process-tree regression tests. Prior Windows evidence is historical; new behavior must be tested, not inferred.
- `.codex/config.toml`: `model_verbosity = "low"`, `approval_policy = "never"`, `sandbox_mode = "danger-full-access"`; verbosity does not cap CLI tool output. `.codex/rules/owner-git.rules` is a limited direct-token policy and must not be treated as complete interception.
- `.agents/hooks.json` executes `python hooks/repository_safety.py`; hook tests cover parser decisions, not real Antigravity Windows client invocation. Need actual integration evidence, not just a unit-test pass; see [B2](slice-b2-tooling.md).
- `logs/` is intentionally ignored by Git **and retained by Owner**. Preserve every existing path and byte; new uniquely named receipts/logs may be added when necessary. Never delete, truncate, move or overwrite prior logs.

## Bounded acceptance and decisions already resolved

1. **Default ordinary command:** hard wall-time cap **300 seconds**. If still running at that cap, collect a short, bounded diagnostic snapshot before terminating only the launched owned tree. Never exceed the cap indefinitely just to diagnose; cleanup/grace time must be finite and reported separately.
2. **Explicitly approved large/complex verification:** wall-time cap **at most 2,100 seconds**. The command/shape and rationale must appear in the approved slice handoff. It is not an unrestricted 35-minute bypass for arbitrary commands, downloads or scripts.
3. **Silence/progress check:** after **300 seconds without observable command output or meaningful progress**, diagnose process existence, source-safe logfile/test report movement and where available CPU/I/O/thread state. Missing stdout alone does **not** prove a hang; do not kill an otherwise progressing long test. A repeat check remains bounded and cannot overrun the hard cap. A verified hang may be terminated before that cap; unsupported/ambiguous evidence must be reported, not fabricated.
4. **Prompt failure reporting:** propagate nonzero child exit and clear result; capture one actionable error plus a path to complete output. The user should not have to read thousands of console or diff lines.
5. **Agent publishing permissions:** Owner alone can `git add/commit/push/tag` and create/merge PRs. Tool guards are defense-in-depth; prove what they intercept on Windows, report gaps honestly and do not claim absolute security when bypasses remain. No unsafe publishing probe in the real repository.
6. **Remediation/review:** mandatory Antigravity self-review after implementation and corrections (not an independent review). Codex independently reviews each slice; CRITICAL/HIGH/MEDIUM and mandatory unverified acceptance cases block. At most three Antigravity remediation + Codex re-review rounds per active slice. LOW/INFO backlog is consolidated at milestone/audit.

## Controlled delivery sequence

Preparation and B1 independent implementation/takeover reviews are complete; the dated [preparation report](reviews/2026-10-26-maintenance-b-independent-pre-handoff-review.md) and earlier blocking reviews remain unchanged. **Next action is Owner commit/push of accepted B1.** B2 requires actual B1 publication and its own handoff; no new implementation is activated by this status update.

1. Codex runs `$codex-pre-handoff-review` **in Owner-approved maintenance preparation mode**, applying [preparation checklist](preparation-review.md) to these two finite slices. Record its dated findings under `reviews/` here. On acceptance set this scope's exact status to **`READY_FOR_HANDOFF`**; this is readiness, **not** a new Owner authorization. On failure use `CHANGES_REQUESTED` and route docs-only corrections to ChatGPT.
2. After Owner publishes the accepted preparation if needed by the agreed workflow, Codex runs `$codex-create-handoff` for **B1 only**; it writes the one active executable handoff, including the chosen minimal algorithm and tests. It must not include B2 implementation by inference.
3. Antigravity implements B1, self-reviews; Codex reviews B1; Owner publishes B1. Only then is B2 individually handed off and implemented/reviewed/published.
4. Codex performs **Maintenance B final review**, reusing unchanged slice evidence. Owner remains the sole publisher. No staged-tree review session.

### Explicit non-goals and protection

- Do **not** rewrite accepted Phase 17/18 contracts, evidence or code, run unnecessary RAG/Java full suites, enable external indexing, initiate Phase 19, add GPU/OCR/PDF/TXT/Ideaverse integration/LLM judge or touch business data.
- Do **not** add a watchdog daemon, external monitoring service, distributed queue, enterprise observability stack or speculative framework.
- Do **not** substitute unverified scripts/rules for real Windows-host tests; do not infer previous log deletions or rewrite historical failed receipts.
- Prior Maintenance A LOW findings `MA-A03`–`MA-A06` remain recorded under the existing milestone/audit backlog; they are not B1/B2 blockers unless a concrete regression changes that classification.
