# Phase 8 Pre-Handoff Preparation Review

Status: **READY FOR HANDOFF**

Codex's [2026-09-30 pre-handoff review](reviews/2026-09-30-phase-8-pre-handoff-codex-review.md) recorded three
preparation findings. The [re-review](reviews/2026-09-30-phase-8-pre-handoff-codex-rereview.md) accepted their
remediation without Phase 8 production or frozen-baseline changes. Owner commit/push of preparation is next.

## Preconditions

- Phase 7 Account is owner committed/pushed and `COMPLETE — FROZEN`.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
- Phase 4–6 milestone remains `MILESTONE_READY`.
- No milestone review is required between Phase 7 and Phase 8.

## Scope

Review Phase 8 Knowledge preparation only. Do not create an implementation handoff during this review.

## Prepared artifacts

### Phase 7 closeout

- `docs/implementation/phase-7/README.md`
- `docs/implementation/phase-7/handoff.md`
- `docs/implementation/handoffs/ACTIVE.md`
- roadmap/root/backend/implementation status synchronization

### Phase 8 preparation

- `docs/implementation/phase-8/README.md`
- `docs/implementation/phase-8/preparation-review.md`
- `docs/implementation/phase-8/reviews/README.md`
- `.agents/skills/knowledge-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-8-knowledge.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/AGENTS.md`
- existing backend implementer/auditor routing updated for the Knowledge skill

No Phase 8 production Java/test implementation, Flyway/DBML change, custom agent, or hook change is part of this
preparation.

## Required checks

Codex should verify:

- Phase 7 is consistently frozen and its completed handoff is archived;
- Knowledge owns exactly the five frozen Schema v1 tables through four nested modules;
- parent/nested Spring Modulith topology is viable without exposing nested internals or nested API types through
  externally consumable parent method signatures;
- parent facade external signatures use parent-owned Knowledge API types, with a representative external top-level
  consumer test that imports only the parent API and passes architecture verification;
- whole-module dependency direction remains only Vault + People + Reference + Account;
- Study author XOR, Website fields, price/currency, progress range, and YouTube fields match Flyway V1;
- YouTube Study validation can be implemented solely through public Account/Reference contracts and the chosen
  canonical YouTube-platform identity is sufficiently explicit;
- `youtube_channel_account_id` uniqueness has stable race-safe and privacy-safe conflict semantics;
- Information does not gain unsupported uniqueness/classification behavior;
- Vocabulary does not invent word/language uniqueness;
- the explicit review-transition primitive is sufficient to keep item state/history atomic without freezing an
  unapproved SRS algorithm;
- concurrent Vocabulary reviews serialize and capture real previous committed state;
- due Vocabulary read semantics are explicit: inclusive cutoff, `NEW`+null due, other null-time states not due,
  `MASTERED` excluded, scheduled rows before unscheduled NEW rows, deterministic ID tie-break, positive bounded
  limit, and no read-side mutation;
- Note preserves Markdown/Obsidian content and arbitrary JSONB frontmatter;
- Note imported-file-hash duplicate semantics align with later explicit import duplicate handling;
- all new unique-conflict tests preserve the existing privacy-safe logging baseline;
- public reads are bounded and no global search/import pipeline leaks into Phase 8;
- package direction is coherent, `.gitkeep` cleanup is required when implementation fills packages, and no generic
  cross-nested abstraction is introduced;
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

Owner commits/pushes the approved preparation slice, then invokes `$codex-create-handoff`. Do not create the
Phase 8 implementation handoff before that owner action.
