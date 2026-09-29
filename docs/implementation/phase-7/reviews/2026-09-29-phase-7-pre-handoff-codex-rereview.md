# Phase 7 Account pre-handoff Codex re-review — 2026-09-29

Status: **READY FOR HANDOFF**. This is a preparation/docs/test-contract review only; no production code or implementation handoff was created.

## Prior findings closed

1. The new external-ID uniqueness path now has an explicit PostgreSQL concurrent-conflict test contract: a distinctive private marker, observable database contention, captured logs across worker threads, absence of the marker and raw vendor `Detail: Key` output, and a stable Account-domain conflict. The existing privacy-safe logger policy is preserved without a logging redesign.
2. Snapshot entry duplicates now have a deterministic batch-only policy. Identical normalized historical copies for one target collapse; differing copies reject the whole command with no committed header or entries. Completed snapshots are immutable in Phase 7, so no concurrent public append policy is needed. No cross-snapshot uniqueness is invented.

The canonical Phase 7 README, Account skill, scoped rule, and module instructions agree on both points. The [initial review](2026-09-29-phase-7-pre-handoff-codex-review.md) remains the historical finding record.

## Gate and other checks

- Phase 6 and the Phase 4–6 milestone remain frozen/`MILESTONE_READY`; local `HEAD` matched `origin/main` at `547a43b`. The maintenance handoff archive exists and `ACTIVE.md` remains `NO_ACTIVE_HANDOFF`.
- The four Account-owned tables, checks, enums, and unique keys align with frozen DBML/Flyway V1. The Vault `EXTERNAL_ACCOUNT` type and public Reference platform lookup exist. The proposed Account module depends only on Vault/Reference public named interfaces; no frozen schema or architecture change is proposed.
- Scope and non-goals remain bounded: account/relationship/snapshot capabilities only; no live platform API, importdata/knowledge implementation, global search, HTTP/frontend, migration, or deletion. Relationship direction and same-pair coherence, historical snapshot isolation, bounded reads, PostgreSQL race tests, and final Java 25 verification are explicit.
- Existing implementer/auditor agents and hooks remain sufficient. Account skill/rule/module instructions have no competing implementation source. Account's `internal/.gitkeep` is appropriate while no production package exists; no stray Account implementation file was introduced.
- `git diff --check` passed. No IDE inspection was run. Prior Lombok/JDK `Unsafe`, Mockito/test-support, and deprecation notices remain non-blocking, configuration-dependent toolchain debt; this review does not claim IDE-clean status. No Maven run was needed for this docs/tooling-only preparation re-review; the future implementation handoff requires final `clean verify`.

No blocking preparation finding remains. The owner commits/pushes this preparation/docs/tooling slice, then invokes `$codex-create-handoff`. Codex must not create the implementation handoff before that owner commit/push.
