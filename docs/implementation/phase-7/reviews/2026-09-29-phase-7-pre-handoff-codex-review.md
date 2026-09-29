# Phase 7 Account pre-handoff Codex review — 2026-09-29

Status: **CHANGES_REQUESTED**. This is a preparation/docs/test-contract review only; no implementation handoff was created.

## Entry gates

Phase 6 is frozen. The Phase 4–6 milestone is `MILESTONE_READY` in the canonical status document and owner-committed/pushed as `547a43b`; local `HEAD` matched `origin/main`. The completed maintenance handoff is archived, and `ACTIVE.md` is `NO_ACTIVE_HANDOFF`. Phase 7 has a README and preparation-review document. The owner-approved Account concept is represented by the prepared scope. The pre-handoff review may run; the findings below block its approval.

## Preparation findings

1. **Medium — privacy regression contract omits the new external-ID uniqueness path.** The frozen database uniquely indexes `(platform_id, external_id)` and the Phase 7 scope requires concurrent duplicate creation to translate to an Account-domain conflict. External IDs can be private business values. The Phase 4–6 logging maintenance explicitly did not claim to cover future paths, yet the Phase 7 test contract does not require captured-log absence for a distinctive external-ID marker or PostgreSQL vendor `Detail: Key` output. Add a PostgreSQL-backed regression requirement for this new conflict path, including worker-thread output where the race test is concurrent, while preserving a stable domain result and non-sensitive diagnostics. This is a test-contract clarification, not a logging redesign.
2. **Medium — conflicting duplicate snapshot copies have undefined results.** Phase 7 says repeated `(snapshot_id, target_account_id)` entries converge to one, but each entry also stores historical username/display name/external ID/profile URL copies. For duplicate targets with differing copied values—within one submitted snapshot or through independent concurrent adds—the preparation does not say which historical record survives or whether the command fails. Specify a deterministic, data-safe policy and its tests. Identical repeated entries can remain idempotent; do not allow an arbitrary or torn historical copy to be stored. Do not add a new uniqueness rule across snapshots.

## Checks without findings

- Phase 7's four owned tables, enum values, nullability, checks, and unique keys match frozen DBML and Flyway V1. Vault-backed `EXTERNAL_ACCOUNT` identity and public Reference platform lookup are feasible without schema or foundation changes. Username/URL uniqueness is correctly not invented.
- `account` depends only on Vault and Reference in the frozen matrix. The proposed capability packages and expected named interfaces exist in those foundation modules; implementation can narrow the current broad module descriptor in the future handoff. No cross-module repository/entity access or speculative API client is proposed.
- Relationship direction, self-check, unique owner/target pair, whole-command concurrency, separate historical snapshots, bounded reads, and non-goals are otherwise clear. PostgreSQL Testcontainers, observable contention, Flyway/Hibernate validation, and Modulith tests are required; no H2 is proposed.
- The Account skill, scoped rule, and module instructions are focused and routed through existing implementer/auditor agents. No new agent or hook is needed. The package tree has only the expected Account descriptor and deferred `internal/.gitkeep`; no stale Account production files were found.
- `git diff --check` passed. No IDE inspection was run. Previously reported Lombok/JDK `Unsafe`, Mockito/test-support, and deprecation notices remain non-blocking/toolchain-dependent debt, not evidence of IDE-clean status.
- The stale Phase 5 closeout sentence in `docs/roadmap.md` was corrected during status synchronization in this review; no further roadmap remediation is needed for that item.

## Gate

Give these findings and the latest package to ChatGPT for narrow preparation remediation. Then rerun `$codex-pre-handoff-review`. Do not create a Phase 7 implementation handoff or modify production code/frozen baselines for this review.
