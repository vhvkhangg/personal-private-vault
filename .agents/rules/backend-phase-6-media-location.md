---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 6 Media + Location work."
---

# Backend Phase 6 — Media + Location

Canonical scope:

`docs/implementation/phase-6/README.md`

Before Phase 6 implementation require:

1. `docs/implementation/phase-6/preparation-review.md` = `READY FOR HANDOFF`;
2. an active Phase 6 Codex implementation handoff.

Use both `media-domain-modeling` and `location-domain-modeling` as applicable, plus normal Java/Spring, pragmatic
SOLID, reuse, modular-monolith, PostgreSQL/JPA, and testing skills.

Constraints:

- keep Media and Location as separate Spring Modulith application modules;
- Media depends only on Vault; Location depends only on Vault + Reference;
- Album, Image, Brand, and Location each use their own Vault Entry identity;
- Vault owns metadata/recycle behavior;
- no object-storage SDK/I/O in Phase 6;
- no Media↔Location dependency;
- Category/Dining assignment is idempotent set behavior;
- Image unique metadata and Location Category names have stable race-safe conflict semantics;
- business-hours replacement is atomic/parent-scoped and preserves unknown/closed/split/overnight semantics;
- keep reads bounded; no global search/listing;
- no schema migrations/controllers/frontend/geocoding/aggregate deletion unless separately approved;
- after committed Phase 6 closeout, the Phase 4–6 milestone review is mandatory before Phase 7 pre-handoff review.
