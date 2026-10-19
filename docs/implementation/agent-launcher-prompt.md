# Agent launcher — Phase 18 Slice 1 Import & Snapshot

**Status:** `OWNER_AUTHORIZED_FOR_ANTIGRAVITY_ONLY`, 2026-10-19. This file replaces the former placeholder for the active **P18-S1-IMPORT-SNAPSHOT** handoff. Do not reuse this prompt after independent Codex review or for Slice 2 without a new owner authorization.

## Task prompt — Antigravity `backend-implementer`

1. From repository root, read `AGENTS.md`, `docs/AGENTS.md`, applicable scoped `AGENTS.md`, `docs/implementation/handoffs/ACTIVE.md`, `docs/implementation/phase-18/slice-1-import-snapshot-handoff.md`, `docs/implementation/phase-18/owner-decisions.md` (D12/D13), `docs/adr/0023-phase-18-application-level-snapshot-worker-trust.md` and the relevant compatibility portions of ADR-0021/0022; read bounded test/run rules. Honor all higher-priority instructions. Inspect `git status --short`, HEAD, code/tests and migration tip first; never assume remote SHA `afeeb82` matches local checkout.
2. Implement **only Phase 18 Slice 1 A+B+C**: owner-controlled whole-file candidate/consent, ordinary app-level safe acquisition, temp copy plus SHA-256/length, original byte no-overwrite R + manifest, minimal durable pending source/revision metadata, expected selected-R/authority/version/revoke guards (no R/sourceVersion/G promotion before future C_P), crash/orphan cleanup needed for these operations, and small compatible HTTP/API seams. Preserve Phase 17 frozen behavior and existing Phase 18 working components. **Reuse-first; no wholesale rewrites.**
3. Keep native NTFS adversarial proof, OS ACL and Restricted Token obligations **out of scope**, without deleting useful existing native code merely because unnecessary. Keep PostgreSQL authority and current process/worker/final transaction safeguards intact. `EXTERNAL_FILE` index publication remains inactive; do not wire parser/OCR/embedding, no full NO_CHANGE or same-R reindex, do not close FR18-3, and do not start Slice 2/Phase 19.
4. Add focused deterministic Java/store/API/database tests for selected/unselected source, copy/hash/size/mtime detectable changes, invalid path/reparse, revoked/stale approval, duplicate R/no overwrite, failed DB/filesystem finalization, restart/orphans, cancellation/concurrency and legacy domain regression. Use append-only new Flyway migration only when justified. Run commands via `scripts/run-bounded-command.py` and record real results. Do not claim full Phase 18 acceptance or P18-FINAL PASS.
5. Produce a dated handback under `docs/implementation/phase-18/` documenting KEEP/SIMPLIFY/CHANGE, changed files, required migrations, actual test commands/counts/timeouts, outstanding risks/failures, review readiness, and exact Code SHA/dirty worktree. Do not edit historical independent reviews. **Do not commit, push, tag or create PR.** Do not automatically move on to Slice 2 or request permission for irrelevant tooling.

If a true technical blocker requires violating D12/D13, modifying frozen Phase 17 semantics, or adding a new security/transaction framework, do not do it; report `OWNER_DECISION_REQUIRED` with narrowly supported alternatives. If implementation remains incomplete, state `INCOMPLETE` with evidence, not PASS.

## Short launcher for Antigravity

```text
/antigravity-implement-handoff
Read docs/implementation/handoffs/ACTIVE.md and the complete docs/implementation/agent-launcher-prompt.md. Implement only owner-authorized P18-S1-IMPORT-SNAPSHOT (A+B+C), preserving Phase 17 frozen and reusing existing code. Run bounded focused tests, produce a dated handback, and do not commit/push. Independent Codex review comes next.
```

## Independent Codex review after Antigravity handback

Use a separate Codex review session; read the same owner/handoff contract, Antigravity handback and complete actual diff/tests. Run `$codex-final-review` **for P18-S1 only**, return `READY FOR OWNER COMMIT`, `CHANGES_REQUESTED` or `OWNER_DECISION_REQUIRED` with concrete findings/evidence in `docs/implementation/phase-18/reviews/`. Do not implement fixes in the same review session. Do not certify Slices 2–4 or P18-FINAL; do not commit/push.
