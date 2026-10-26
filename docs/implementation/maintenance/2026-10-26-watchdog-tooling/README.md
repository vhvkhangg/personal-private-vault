# Maintenance B — Watchdog & Tooling (post-Phase-18)

- **Status:** B1 stays **COMPLETE / CLOSED / OWNER-PUBLISHED**; see [closeout](b1-owner-closeout-2026-10-26.md). B2 is **READY_FOR_OWNER_COMMIT** after the [separate independent attempt-1 final review](reviews/2026-10-26-b2-independent-takeover-attempt-1-final-review.md). F01/F04 VERIFIED/CLOSED; F02/F03 closures/limits preserved. **F01 Codex attempt 1/2 accepted**, Antigravity stopped at 3/3; no counter reset, automatic attempt 2 or Option B acceptance change. Codex proof requires a trusted repository-root session with native hooks loaded. Contract, histories, logs and LOW backlog remain intact; Maintenance B is not complete.
- **Approved authority:** Owner's explicit authorization to start Maintenance B after publishing Maintenance A.
- **Current B2 entry:** independently accepted and Owner-published B1 commit [`a4cf43505ab48198e22799fecf5098c5c6870a79`](https://github.com/vhvkhangg/personal-private-vault/commit/a4cf43505ab48198e22799fecf5098c5c6870a79); the original preparation baseline below remains historical context.
- **Authoritative source baseline:** GitHub `main` commit [`6fe8dbce24ae9ab43290d76172e2c39678246bbd`](https://github.com/vhvkhangg/personal-private-vault/commit/6fe8dbce24ae9ab43290d76172e2c39678246bbd), verified 2026-10-26. All preparation and subsequent implementation must verify that the local branch matches the accepted baseline or account for drift before acting.
- **Active handoff:** [`MAINT-B2-CLI-GIT-HOOK-ENFORCEMENT / READY_FOR_OWNER_COMMIT`](../../handoffs/ACTIVE.md). Separate independent review is complete; Owner publication and brief B2 closeout are next. CLI interruption/resumption did not advance F01 beyond 1/2. B1 is not reopened; B3/Phase 19 remain unauthorized.
- **Owners:** Owner approves scope and alone stages/commits/pushes; ChatGPT authors preparation/governance docs; Codex independently pre-reviews and writes executable handoffs, then independently reviews each implementation slice and the completed Maintenance B; Antigravity implements/tests and always self-reviews. A Codex implementation takeover is not the default.
- **Canonical policy:** [Current Owner Delivery Policy](../../../workflow/current-owner-delivery-policy.md).

## Purpose and two-slice boundary

Fix the actual process execution watchdog and verify command-safety/short-output tooling on the Owner's Windows 11 environment. No production RAG feature, Java business module, schema, migration, model, indexing activation, file ingestion change, network service, dashboard, scheduler, monitoring daemon or new agent is in scope.

| Slice | Owner-approved scope | Primary implementer | Independent gate |
| --- | --- | --- | --- |
| [B1 — Progress-aware bounded commands](slice-b1-watchdog.md) | Existing Python wrapper, owned process-tree safety, bounded progress/diagnostics, focused tests and corresponding executable instructions | Antigravity; Owner-authorized Codex F02 takeover | Codex **slice review**; Owner commit/push |
| [B2 — CLI output and Git/hook enforcement](slice-b2-tooling.md) | Codex/Antigravity actual Windows client behavior, existing rules/hook and tests, concise tool output and status receipts | Antigravity; Owner-authorized Codex F01/F04 takeover | Codex **slice review**; Owner commit/push |

After the two Owner-published slices: **one Codex Maintenance B final review** aggregates the prior independent slice evidence, validates cross-slice contract consistency and only reruns relevant focused checks on demonstrated drift. Owner records a concise maintenance closeout. A new numbered RAG phase is **not** implicitly authorized.

### Historical implementation facts at preparation (before B1/B2)

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

Preparation, B1 acceptance/publication, three B2 corrective reviews and separate Codex takeover attempt-1 acceptance are complete; historical records remain unchanged. **Next: Owner commits/pushes B2, then a brief publication closeout and one Maintenance B final `$codex-final-review` after both slices are published.** All four B2 blockers are CLOSED. Antigravity remains stopped at 3/3; no fourth round, second takeover attempt or B1 restart. Overall Maintenance B final review remains a future gate.

1. Codex runs `$codex-pre-handoff-review` **in Owner-approved maintenance preparation mode**, applying [preparation checklist](preparation-review.md) to these two finite slices. Record its dated findings under `reviews/` here. On acceptance set this scope's exact status to **`READY_FOR_HANDOFF`**; this is readiness, **not** a new Owner authorization. On failure use `CHANGES_REQUESTED` and route docs-only corrections to ChatGPT.
2. After Owner publishes the accepted preparation if needed by the agreed workflow, Codex runs `$codex-create-handoff` for **B1 only**; it writes the one active executable handoff, including the chosen minimal algorithm and tests. It must not include B2 implementation by inference.
3. Antigravity implements B1, self-reviews; Codex reviews B1; Owner publishes B1. Only then is B2 individually handed off and implemented/reviewed/published.
4. Codex performs **Maintenance B final review**, reusing unchanged slice evidence. Owner remains the sole publisher. No staged-tree review session.

### Explicit non-goals and protection

- Do **not** rewrite accepted Phase 17/18 contracts, evidence or code, run unnecessary RAG/Java full suites, enable external indexing, initiate Phase 19, add GPU/OCR/PDF/TXT/Ideaverse integration/LLM judge or touch business data.
- Do **not** add a watchdog daemon, external monitoring service, distributed queue, enterprise observability stack or speculative framework.
- Do **not** substitute unverified scripts/rules for real Windows-host tests; do not infer previous log deletions or rewrite historical failed receipts.
- Prior Maintenance A LOW findings `MA-A03`–`MA-A06` remain recorded under the existing milestone/audit backlog; they are not B1/B2 blockers unless a concrete regression changes that classification.
