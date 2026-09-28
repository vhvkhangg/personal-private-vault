---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 4 Fiction work."
---

# Backend Phase 4 — Fiction

Canonical scope:

`docs/implementation/phase-4/README.md`

Before Phase 4 implementation require:

1. Phase 1–3 milestone status = `MILESTONE_READY`;
2. `docs/implementation/phase-4/preparation-review.md` = `READY FOR HANDOFF`;
3. an active Phase 4 Codex implementation handoff.

Use `fiction-domain-modeling` plus normal Java/Spring, SOLID, reuse, modular-monolith, persistence, and testing skills.

Constraints:

- Fiction is Vault Entry-backed;
- favorite/rating/tag/recycle behavior stays in Vault;
- exactly one author source: Person XOR Creator Group;
- use only public People/Vault/Reference named interfaces;
- Fiction owns `fiction_genres`; Reference owns shared story archetypes/world settings;
- repeated story-archetype/world-setting assignment is idempotent set behavior;
- do not invent link uniqueness, global search/listing, delete behavior, migrations, or controllers;
- avoid generic CRUD bases and speculative patterns.
