# Phase 6 Pre-Handoff Preparation Review

Status: **READY FOR HANDOFF**

## Preconditions

- Phase 5 is committed/pushed and `COMPLETE — FROZEN`.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
- No milestone review is required before Phase 6 implementation; Phase 6 itself triggers the next milestone after
  owner commit/push.

## Scope

Review Phase 6 `media` + `location` preparation only. Do not create an implementation handoff during this review.

## Prepared artifacts

### Phase 5 closeout

- `docs/implementation/phase-5/README.md`
- `docs/implementation/phase-5/handoff.md`
- `docs/implementation/handoffs/ACTIVE.md`
- roadmap/root/backend/implementation status synchronization

### Phase 6 preparation

- `docs/implementation/phase-6/README.md`
- `docs/implementation/phase-6/preparation-review.md`
- `docs/implementation/phase-6/reviews/README.md`
- `.agents/skills/media-domain-modeling/SKILL.md`
- `.agents/skills/location-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-6-media-location.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/media/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/location/AGENTS.md`
- existing backend implementer/auditor routing updated for both domain skills

No Phase 6 production Java/test implementation, Flyway/DBML change, custom agent, or hook change is part of this
preparation.

## Mandatory preparation checks

Codex should verify:

### Cross-cutting

- Phase 5 is consistently frozen and its completed handoff is archived;
- Media and Location remain separate top-level modules despite sharing one roadmap phase;
- exact table ownership matches frozen Schema v1/Flyway V1;
- no new cross-module dependency or schema change is needed;
- proposed package/API boundaries are capability-oriented without duplicating generic infrastructure;
- reads remain bounded and global search remains deferred;
- existing agents/hooks remain sufficient;
- repository tree/status/docs/links are internally consistent.

### Media

- Album and Image are separate Vault Entry-backed identities;
- Media depends only on public Vault named interfaces;
- Image `album_id` is optional and internal to Media;
- object-key/checksum uniqueness has explicit conflict/concurrency behavior;
- object-storage I/O is correctly deferred;
- image count is derived, not persisted, and does not require loading all images.

### Location

- Brand and Location are Vault Entry-backed; Address/Category/Hours are not;
- Location depends only on public Vault + Reference interfaces and does not depend on Media;
- Brand/Location names remain nonunique;
- price/currency validation matches the exact database checks without extra restrictions;
- Address Country, optional Brand nationality, and currencies use Reference public contracts;
- category uniqueness/set-assignment/dining-style concurrency semantics are explicit;
- business-hours known/unknown/closed/split/overnight behavior is unambiguous;
- schedule replacement is atomic and has a concurrency serialization requirement;
- no unsupported category-name/F&B coupling, coordinate/geocoding logic, or delete behavior is invented.

### Test/evidence

- PostgreSQL race tests require observable contention, not timing-only sleeps;
- transaction rollback is covered for all four Vault-backed aggregate types (`ALBUM`, `IMAGE`, `BRAND`, `LOCATION`);
- Spring Modulith exact named-interface dependencies are verified;
- final clean verify/evidence contract is complete;
- known warnings/diagnostics are classified without claiming IDE-clean status unless an IDE inspection actually ran.

## Milestone after Phase 6

If Phase 6 eventually reaches `READY FOR OWNER COMMIT` and the owner commits/pushes it, the next ChatGPT closeout must
mark Phase 6 frozen and prepare the Phase 4–6 milestone review. Phase 7 pre-handoff review remains blocked until that
milestone returns `MILESTONE_READY` and the post-milestone ChatGPT synchronization step is complete.

## Invoke

```text
$codex-pre-handoff-review
```

Successful result:

```text
READY FOR HANDOFF
```

If Codex returns `CHANGES_REQUESTED`, return the findings/latest package to ChatGPT for preparation remediation.

## Codex review result — 2026-09-29

**READY FOR HANDOFF.** See [`reviews/2026-09-29-phase-6-pre-handoff-codex-review.md`](reviews/2026-09-29-phase-6-pre-handoff-codex-review.md).

The owner commits/pushes this preparation slice before invoking `$codex-create-handoff`. This review did not
create a Phase 6 implementation handoff or authorize production changes before that owner action.
