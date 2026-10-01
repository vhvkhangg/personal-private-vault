# Phase 10 Pre-Handoff Preparation Review

Status: **READY FOR HANDOFF**

Current formal review: [`reviews/2026-10-01-phase-10-pre-handoff-codex-rereview.md`](reviews/2026-10-01-phase-10-pre-handoff-codex-rereview.md).

The High ImportData transition-concurrency preparation finding is closed. No blocking preparation finding remains.
Owner committed/pushed preparation as `0e530f2`. The subsequently created [active handoff](../handoffs/ACTIVE.md)
is now `READY_FOR_OWNER_COMMIT` after implementation final acceptance; this preparation acceptance and milestone
`MILESTONE_READY` remain unchanged. See [`reviews/2026-10-01-phase-10-final-codex-acceptance.md`](reviews/2026-10-01-phase-10-final-codex-acceptance.md).

## Preconditions

- Phase 9 Collection is `COMPLETE — FROZEN`.
- Phase 7–9 milestone is `MILESTONE_READY`.
- milestone review/status documents were owner committed/pushed.
- post-milestone synchronization/reset is complete.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.

## Scope

Review Phase 10 Feed + ImportData preparation only. Do not create an implementation handoff during this review.

## Remediated Codex finding

The 2026-10-01 review found one preparation blocker: same-job transition concurrency for ImportData.
The canonical Phase 10 contract now requires one owner-local guarded transition path for parse/validate/execute/
cancel, fresh database state after the guard is acquired, stable invalid-transition loser behavior, and
deterministic PostgreSQL contention regressions. No production/schema change is part of this remediation.

## Prepared artifacts

### Post-milestone synchronization

- completed maintenance handoff archived;
- maintenance marked `COMPLETE — FROZEN`;
- `ACTIVE.md` cleared;
- milestone current-state prose synchronized;
- archived Phase 8 low-risk wording corrected without production changes.

### Phase 10 preparation

- `docs/implementation/phase-10/README.md`
- `docs/implementation/phase-10/preparation-review.md`
- `docs/implementation/phase-10/reviews/README.md`
- `.agents/skills/feed-import-workflow-modeling/SKILL.md`
- `.agents/rules/backend-phase-10-feed-importdata.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/AGENTS.md`
- existing implementer/auditor routing updated for the Phase 10 skill

No Phase 10 production Java/test implementation, Flyway/DBML change, custom agent, or hook change is included.

## Required Codex checks

Verify at minimum:

- Feed owns exactly four frozen tables and ImportData owns exactly two;
- both modules depend only on public Vault + parent Knowledge;
- no nested Knowledge package/repository access is required by the prepared contracts;
- JSONB command/entity/view boundaries have explicit deep-isolation semantics;
- FeedSource scheduling checks/defaults/due predicate are consistent with Flyway V1;
- live network adapters/scheduler runtime are correctly deferred while normalized ingestion remains usable;
- FeedItem SHA-256 URL hash and dual-key update/conflict semantics are deterministic and race-safe;
- SavedResource is Vault-backed `SAVED_RESOURCE`, global URL-hash duplicates are stable conflicts, and loser Vault
  creation rolls back;
- SavedResource conversion supports Study/Information/Note only and uses the parent Knowledge API transactionally;
- ImportData target/format matrix and job/item state transitions fit frozen enums/check constraints;
- parsing stores no final Knowledge records before execution;
- structural validation does not duplicate Knowledge business validation;
- Note hash is the only Phase 10 database duplicate lookup supported by the frozen parent Knowledge API;
- decision matrix and whole-job transactional import semantics are explicit;
- parse/validate/execute/cancel share one job-level serialization guard and recheck fresh authoritative status
  after lock acquisition, including already-managed-entity cases;
- terminal/repeated losers use stable invalid-transition rejection and cannot perform target writes or regress
  `IMPORTED`/`FAILED`/`CANCELLED`;
- execute-vs-execute, execute-vs-cancel (both winner orders), parse-vs-cancel, and validate-vs-cancel have
  deterministic PostgreSQL lock-contention test contracts with target/Vault/job/item/count assertions;
- `imported_items = IMPORTED + UPDATED` is compatible with frozen count checks;
- parser safety/privacy and Markdown/frontmatter preservation are explicit;
- all collection-valued reads are bounded/deterministic;
- Phase 10 does not reopen Phases 7–9 or introduce Search/REST/frontend/object-storage/provider integrations;
- existing agents/hooks are sufficient;
- status docs/tree/links are internally consistent.

## Invoke

```text
$codex-pre-handoff-review
```

Successful result:

```text
READY FOR HANDOFF
```

If Codex returns `CHANGES_REQUESTED`, give the findings/latest package to ChatGPT for narrow preparation remediation.


## Next action

Preparation is owner committed/pushed and handoff creation is complete. Run Antigravity:

```text
/antigravity-implement-handoff
```

Commit message: `docs: prepare phase 10 feed and import workflows`
No implementation handoff was created by this review.
