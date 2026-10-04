---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 12 PostgreSQL-first Global Search."
---

# Backend Phase 12 — Global Search

Canonical scope:

`docs/implementation/phase-12/README.md`

Before implementation require:

1. `docs/implementation/phase-12/preparation-review.md` = `READY FOR HANDOFF`;
2. an active Phase 12 Codex implementation handoff.

Use `global-search-domain-modeling` plus Java/Spring standards, pragmatic SOLID, reuse/consistency,
modular-monolith architecture, JPA/PostgreSQL, design-pattern selection, and backend testing.

Constraints:

- Search is a leaf/orchestrator and owns no business table;
- only frozen allowed dependencies: Vault/People/Fiction/Film/Media/Location/Knowledge/Collection/Account/Feed;
- Finance/Journal/Personal remain outside Phase 12 Search;
- frozen feature modules may receive read-only search contracts/queries only;
- Knowledge/Collection use parent search contracts only;
- Vault owns active/trash and tag qualification;
- source/global ordering is rank DESC, similarity DESC, textual `type_name` ASC, ID ASC; `primaryText` is
  display-only;
- SQL/native source queries must define `type_name` with `CAST(... AS text) COLLATE "C"`; Java/global ordering
  must compare `VaultEntryType.name()` strings, never native enum/ordinal order;
- Vault tag-origin qualification + duplicate-tag collapse happens before the exact tag-source top-K limit;
- feature text required-tag qualification may continue across bounded pages until K qualifying hits are filled;
- all candidate/tag/document operations are bounded/batched;
- literal case-insensitive partial + explicit pg_trgm fuzzy semantics follow the canonical README;
- no unaccent, Elasticsearch/OpenSearch, vector/RAG, search history, projection table, event indexing, HTTP, or frontend;
- Flyway V2 may add only pg_trgm + indexes, no logical schema redesign;
- no per-hit repository/API calls or load-all;
- raw search text/results/tags are not logged.

After accepted Phase 12 owner commit/push, do **not** prepare Phase 13 immediately. Close/freeze Phase 12, then run the
mandatory Phase 10–12 `$codex-milestone-review`.
