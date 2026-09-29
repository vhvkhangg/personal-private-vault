---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 7 Account work."
---

# Backend Phase 7 — Account

Canonical scope:

`docs/implementation/phase-7/README.md`

Before Phase 7 implementation require:

1. Phase 4–6 milestone = `MILESTONE_READY`;
2. `docs/implementation/phase-7/preparation-review.md` = `READY FOR HANDOFF`;
3. an active Phase 7 Codex implementation handoff.

Use `account-domain-modeling` plus normal Java/Spring, pragmatic SOLID, reuse, modular-monolith,
PostgreSQL/JPA, and testing skills.

Constraints:

- External Account is Vault Entry-backed with type `EXTERNAL_ACCOUNT`;
- Account depends only on Vault + Reference public contracts;
- validate platform through Reference;
- preserve at-least-one identifier and non-null platform/external-ID uniqueness semantics;
- one current relationship row exists per owner/target pair, with separate follower/follow directions;
- snapshots are historical captures and do not silently mutate relationship state;
- snapshot header + entries commit atomically;
- snapshot entries use idempotent set semantics;
- reads are account/snapshot-scoped and bounded;
- no platform API/OAuth/scraping/browser automation/scheduling;
- no Knowledge/feed/importdata implementation, migrations, controllers, frontend, global search, or deletion unless
  separately approved;
- avoid generic CRUD bases and speculative event/adapter abstractions.
