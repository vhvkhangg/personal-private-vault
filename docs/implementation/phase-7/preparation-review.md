# Phase 7 Pre-Handoff Preparation Review

Status: **READY FOR HANDOFF**

The [initial Codex pre-handoff review](reviews/2026-09-29-phase-7-pre-handoff-codex-review.md) found two
docs/test-contract gaps. Both were remediated without production or frozen-baseline changes, and the
[Codex re-review](reviews/2026-09-29-phase-7-pre-handoff-codex-rereview.md) accepted the preparation.

## Gate

All milestone prerequisites are satisfied:

- `docs/implementation/phase-6/milestone-review.md` = `MILESTONE_READY`;
- milestone review/status documents are owner committed/pushed;
- completed maintenance handoff is archived;
- `docs/implementation/handoffs/ACTIVE.md` = `NO_ACTIVE_HANDOFF`.

The entry gates and preparation re-review passed. The owner must commit/push this preparation slice before Codex
creates the Phase 7 implementation handoff.

## Scope once unblocked

Review only Phase 7 Account preparation. Do not create an implementation handoff during the preparation review.

## Prepared artifacts

### Phase 6 closeout / milestone

- `docs/implementation/phase-6/README.md`
- `docs/implementation/phase-6/handoff.md`
- `docs/implementation/phase-6/milestone-review.md`
- `docs/implementation/handoffs/ACTIVE.md`
- synchronized roadmap/current-status docs

### Phase 7 preparation

- `docs/implementation/phase-7/README.md`
- `docs/implementation/phase-7/preparation-review.md`
- `docs/implementation/phase-7/reviews/README.md`
- `.agents/skills/account-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-7-account.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/AGENTS.md`
- existing implementer/auditor routing updated for the Account skill

No Phase 7 production Java/test implementation, Flyway/DBML change, custom agent, or hook change is included.

## Required preparation checks once milestone-ready

Codex should verify:

- Phase 6 is consistently frozen and its handoff is archived;
- Phase 4–6 milestone is `MILESTONE_READY`;
- Account owns exactly the four frozen Schema v1 account/history tables;
- dependency direction remains only Vault + Reference;
- expected named-interface dependencies are sufficient;
- External Account uses `EXTERNAL_ACCOUNT` Vault identity transactionally;
- identifier and `(platform_id, external_id)` uniqueness semantics match Flyway V1 without inventing username/URL
  uniqueness;
- the external-ID conflict test contract requires a real concurrent PostgreSQL uniqueness conflict plus captured
  worker-thread logs proving the private marker and raw vendor `Detail: Key` text are absent;
- relationship direction fields and one-row-per-owner/target semantics are explicit and race-safe;
- snapshots remain historical records and do not silently mutate current relationship state;
- snapshot creation is batch-only/immutable; identical duplicate target copies collapse while conflicting historical
  copies reject the whole snapshot command with no partial commit;
- public reads are explicitly bounded/account-scoped;
- live platform/API/scraping/scheduler work is excluded;
- PostgreSQL race tests require observable contention rather than timing-only sleeps;
- package direction is coherent and existing agents/hooks remain sufficient;
- repository tree/status/docs/links are internally consistent.

## Next action

The owner commits/pushes this preparation/docs/tooling slice. Then run `$codex-create-handoff`.
Do not create the Phase 7 implementation handoff before that owner commit/push.
