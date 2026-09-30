---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 8 Knowledge work."
---

# Backend Phase 8 — Knowledge

Canonical scope:

`docs/implementation/phase-8/README.md`

Before Phase 8 implementation require:

1. `docs/implementation/phase-8/preparation-review.md` = `READY FOR HANDOFF`;
2. an active Phase 8 Codex implementation handoff.

Use `knowledge-domain-modeling` plus normal Java/Spring, pragmatic SOLID, reuse, modular-monolith,
PostgreSQL/JPA, and testing skills.

Constraints:

- parent `knowledge` is a facade; Study/Information/Vocabulary/Note nested modules own their persistence/rules;
- externally consumable parent facade signatures use parent-owned Knowledge API types only; do not leak nested
  command/view types to top-level callers;
- Study, Information, Vocabulary, and Note are Vault Entry-backed; Vocabulary Review is not;
- Study validates People/Reference/Account through public contracts only;
- preserve Study Website/YouTube/author/price/progress checks exactly;
- duplicate YouTube channel assignment is a stable, race-safe, privacy-safe Study conflict;
- Information remains simple content without invented uniqueness;
- Vocabulary word/language duplicates remain allowed;
- Vocabulary review transitions are explicit, atomic, and serialized per item; do not invent an automatic SRS
  algorithm;
- due Vocabulary reads use the frozen Phase 8 predicate/order: non-mastered scheduled rows with
  `next_review_at <= cutoff` plus null-time `NEW`, scheduled-first ordering, inclusive cutoff, bounded limit, no
  read-side mutation;
- Note Markdown/frontmatter is preserved; imported-hash conflicts are race-safe and privacy-safe;
- parent facade must not duplicate nested business logic;
- reads remain bounded; no global search/import/feed workflow;
- no migrations, controllers, frontend, RAG, deletion, or frozen foundation changes unless separately approved;
- avoid generic cross-nested common layers, CRUD bases, speculative events, and pattern-driven overengineering.
