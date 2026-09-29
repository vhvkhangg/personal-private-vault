# Phase 7 Pre-Handoff Preparation Review

Status: **BLOCKED — PHASE 4–6 MILESTONE MAINTENANCE IN PROGRESS**

## Gate

Do not run `$codex-pre-handoff-review` until:

```text
docs/implementation/phase-6/milestone-review.md
```

has status:

```text
MILESTONE_READY
```

After `MILESTONE_READY`:

1. owner commits/pushes milestone review/status changes;
2. owner sends the latest package to ChatGPT;
3. ChatGPT performs post-milestone synchronization/reset and changes this status to
   `AWAITING CODEX PRE-HANDOFF REVIEW`;
4. only then run `$codex-pre-handoff-review`.

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
- relationship direction fields and one-row-per-owner/target semantics are explicit and race-safe;
- snapshots remain historical records and do not silently mutate current relationship state;
- snapshot header/entries are transactionally consistent and duplicate entries use set semantics;
- public reads are explicitly bounded/account-scoped;
- live platform/API/scraping/scheduler work is excluded;
- PostgreSQL race tests require observable contention rather than timing-only sleeps;
- package direction is coherent and existing agents/hooks remain sufficient;
- repository tree/status/docs/links are internally consistent.

## Next command

Phase 7 review is not authorized yet. The current workflow command is:

```text
$codex-create-handoff
```

for the approved `milestone-4-6-privacy-safe-constraint-logging` maintenance slice.
