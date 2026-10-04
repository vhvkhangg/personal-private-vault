# Phase 12 Codex resubmission check

- Date: 2026-10-04
- Scope: active `phase-12-global-search` resubmitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Baseline: `44fdaa9137472b44217c159e87e5bc2b89ca54d4`.
- Outcome: **CHANGES_REQUESTED** — FR12-1–FR12-3 remain closed; FR12-4–FR12-6 remain open.

## Findings retained

The required test/evidence remediation from [final re-review 2](2026-10-04-phase-12-final-codex-rereview-2.md)
is not present. The targeted checks confirm:

- **FR12-4 — Medium:** `GlobalSearchIntegrationTest.java:1183`, `:1216`, `:1246` still use hand-written EXPLAIN SQL;
  threshold checks still manually set 0.3 without a differing prior threshold and real proxied search/result proof.
- **FR12-5 — High:** `GlobalSearchIntegrationTest.java:1327` still removes required tags before calling People search,
  then post-filters the limited results. API counters still do not measure the real qualification/JDBC path. No real
  >=601-result maximum-lookahead dataset has been added.
- **FR12-6 — Medium:** `test-evidence.md` still assigns secondary prefix to 400 and secondary substring to 350,
  claims null ID sets are rejected, and describes the uncorrected tests as actual production query/count proof.
  Required measured plans/counts and observed warnings are absent.

The inspected test file's modification time is 18:45:29, before the previous 19:00:39 review report; its relevant
code also directly confirms the outstanding behaviors. Modification times alone were not used as proof of correctness.
Detailed required corrections remain exactly those in final re-review 2 and ACTIVE; no new production scope or
additional findings introduced.

## Verification / limits

Inspected ACTIVE, current evidence and the exact outstanding test paths; HEAD remains the preparation baseline.
No fresh Maven run: the unchanged blockers prevent acceptance regardless of another green build. The previous
independent 815-test clean verify at 18:57:05+07:00 remains historical evidence only. Previously reviewed architecture,
security, maintainability, persistence and warning assessments remain in final re-review 2; this check does not reopen
the closed findings or assert a new full-suite result. Relevant review skills applied using retained canonical scope.
Only review/status documentation changed by Codex; no production/test implementation, commit or push.

## Next step

Run Antigravity `/antigravity-test-slice` to complete the already specified FR12-4–FR12-6 test/evidence-only remediation,
then return ACTIVE as `IMPLEMENTED_AWAITING_CODEX_REVIEW` with updated tests/evidence and invoke `$codex-final-review`.
No owner commit/push acceptance or commit message. Phase 10–12 milestone remains due after eventual acceptance,
owner commit/push and phase closeout, before Phase 13.
