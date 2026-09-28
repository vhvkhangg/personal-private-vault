---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 3 People work."
---

# Backend Phase 3 — People

Canonical scope: `docs/implementation/phase-3/README.md`.

Before implementation require:

- `docs/implementation/phase-3/preparation-review.md` = `READY FOR HANDOFF`;
- a Phase 3 active Codex handoff.

Use `people-domain-modeling` plus normal Java/Spring, SOLID, reuse, modular-monolith, persistence, and testing skills.

Constraints:

- exact dependencies only: `vault::entry`, `vault::enums`, `vault::view`, `reference::catalog`, `reference::view`;
- Person ID equals its Vault Entry ID;
- duplicate creator-group name create is conflict; duplicate role/membership add is idempotent;
- uniqueness races must converge to the documented outcome without raw persistence errors;
- reads remain ID/parent-bounded; person/group deletion is out of scope;
- do not duplicate vault metadata/recycle-bin behavior;
- creator groups are not Vault Entries;
- no migration/controller/frontend work unless explicitly approved later;
- no speculative role hierarchy or generic CRUD/base-service framework.
