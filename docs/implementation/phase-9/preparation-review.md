# Phase 9 Pre-Handoff Preparation Review

Status: **READY FOR HANDOFF** after the [2026-09-30 Codex re-review](reviews/2026-09-30-phase-9-pre-handoff-codex-rereview.md); owner preparation commit/push is pending.

## Preconditions

- Phase 8 Knowledge is owner committed/pushed and `COMPLETE — FROZEN`.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
- No milestone review is required between Phase 8 and Phase 9.

## Scope

Review Phase 9 Collection preparation only. Do not create an implementation handoff during this review.

## Prepared artifacts

### Phase 8 closeout

- `docs/implementation/phase-8/README.md`
- `docs/implementation/phase-8/handoff.md`
- `docs/implementation/handoffs/ACTIVE.md`
- synchronized roadmap/root/backend/implementation status docs

### Phase 9 preparation

- `docs/implementation/phase-9/README.md`
- `docs/implementation/phase-9/preparation-review.md`
- `docs/implementation/phase-9/reviews/README.md`
- `.agents/skills/collection-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-9-collection.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/collection/AGENTS.md`
- existing implementer/auditor routing updated for the Collection skill

No Phase 9 production Java/test implementation, Flyway/DBML change, custom agent, or hook change is included.

## Required checks

Codex should verify:

- Phase 8 is consistently frozen and its completed handoff is archived;
- Collection owns exactly the five frozen Schema v1 tables through three nested modules;
- parent/nested topology is viable without nested internals or nested DTOs leaking through external parent signatures;
- representative external-consumer test contract proves parent-API-only consumption;
- whole-module dependency direction remains only Vault + People + Reference;
- exact operation matrix is fixed: create/update/find-by-ID for Music/Shopping/Software, add + bounded read for
  Music credits and Software platforms, with assignment removal/replace-all deferred;
- null/omitted Music `version` -> `ORIGINAL` and Shopping `status` -> `WISHLIST` on create and update; scalar update
  is full replacement rather than PATCH semantics;
- Music version/platform fields and no-natural-key uniqueness match Flyway V1;
- Music credits are Person-only, permit both roles per Person, and use idempotent/race-safe exact-tuple set semantics;
- Shopping price/currency and `status`/`purchased_at` rules match frozen checks without automatic timestamps;
- Shopping optional platform has no invented PlatformKind restriction;
- Software type/price/currency rules match Flyway V1;
- Software platform set may be empty/multiple, has no PlatformKind restriction, and duplicates are idempotent/race-safe;
- no title/name/URL/type deduplication is invented;
- Music-credit reads require positive explicit `limit`, at-most-limit results, and `person_id ASC, role ASC`;
- Software-platform reads require positive explicit `limit`, at-most-limit results, and `platform_id ASC`;
- public reads are parent/domain-scoped and bounded;
- composite-key set-write tests use observable PostgreSQL concurrency rather than timing-only sleeps;
- Phase 9 is marked as a milestone phase requiring a Phase 7–9 milestone review after owner commit/push;
- `.gitkeep` cleanup is required when implementation fills packages;
- existing agents/hooks remain sufficient;
- repository tree/status/docs/links are internally consistent.

## Invoke

```text
$codex-pre-handoff-review
```

Successful result:

```text
READY FOR HANDOFF
```

If Codex returns `CHANGES_REQUESTED`, return the findings/latest package to ChatGPT for narrow preparation
remediation.


## Next action

Rerun:

```text
$codex-pre-handoff-review
```

Do not create the Phase 9 implementation handoff unless the re-review returns `READY FOR HANDOFF`.
