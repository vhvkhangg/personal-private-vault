---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 5 Film work."
---

# Backend Phase 5 — Film

Canonical scope:

`docs/implementation/phase-5/README.md`

Before Phase 5 implementation require:

1. `docs/implementation/phase-5/preparation-review.md` = `READY FOR HANDOFF`;
2. an active Phase 5 Codex implementation handoff.

Use `film-domain-modeling` plus normal Java/Spring, SOLID, reuse, modular-monolith, persistence, and testing skills.

Constraints:

- Film and Film Credit are separate Vault Entry-backed identities;
- Film metadata capabilities stay in Vault;
- `FILM_CREDIT` is favorite-only; no rating/tags;
- optional director validates a Person but does not mutate Person roles;
- Film owns Film genres and their assignments;
- shared story archetypes/world settings remain Reference-owned;
- genre/archetype/world-setting duplicate assignment is idempotent and race-safe;
- Film links have no Fiction-style `link_type`;
- credit creation creates distinct records; do not invent dedupe without a schema/business requirement;
- use only public Vault/People/Reference named interfaces;
- keep reads bounded;
- no migrations/controllers/frontend/search/delete behavior unless separately approved;
- avoid generic CRUD bases and speculative patterns.
