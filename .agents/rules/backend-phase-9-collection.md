---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 9 Collection work."
---

# Backend Phase 9 — Collection

Canonical scope:

`docs/implementation/phase-9/README.md`

Before Phase 9 implementation require:

1. `docs/implementation/phase-9/preparation-review.md` = `READY FOR HANDOFF`;
2. an active Phase 9 Codex implementation handoff.

Use `collection-domain-modeling` plus normal Java/Spring, pragmatic SOLID, reuse, modular-monolith,
PostgreSQL/JPA, and testing skills.

Constraints:

- parent `collection` is a closed facade; nested modules own persistence/rules;
- external parent signatures use parent-owned Collection API types only;
- Music/Shopping/Software are Vault Entry-backed;
- require create/update/find-by-ID for each domain; scalar updates are full replacement commands;
- null/omitted Music version -> `ORIGINAL`; null/omitted Shopping status -> `WISHLIST` on create and update;
- Software type is required on create and update;
- Music credits are Person-only exact-tuple set assignments and allow both roles for one Person;
- Shopping `WISHLIST` forbids purchase timestamp, `PURCHASED` allows null/non-null timestamp, and no auto-now behavior;
- Software supports zero/many Reference platforms with idempotent/race-safe assignment;
- Music-credit and Software-platform APIs expose add + bounded read only; removal/replace-all is deferred;
- Music-credit reads require positive limit and `person_id ASC, role ASC`; Software-platform reads require positive
  limit and `platform_id ASC`;
- no PlatformKind restrictions or title/name/URL/type uniqueness are invented;
- composite assignment writes are atomically race-safe;
- parent facade does not duplicate nested business logic;
- reads are bounded; global search is deferred;
- no migrations, controllers, frontend, RAG, future external integrations, deletion, or frozen-foundation changes
  without separate approval.

After Phase 9 final review and owner commit/push, do not start Phase 10 pre-handoff review until the required
Phase 7–9 `$codex-milestone-review` returns `MILESTONE_READY` and post-milestone synchronization is complete.
