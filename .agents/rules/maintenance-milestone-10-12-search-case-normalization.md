---
trigger: model_decision
description: "Apply when creating, implementing, testing, or reviewing the owner-approved M10-12-1 Search case-normalization maintenance."
---

# Milestone 10–12 Maintenance — Search Case Normalization

Canonical scope:

`docs/implementation/maintenance/milestone-10-12-search-case-normalization/README.md`

This is an owner-approved maintenance exception over frozen Phase 12 Search behavior.

Implementation requires an active Codex maintenance handoff created by:

```text
$codex-create-handoff
```

Constraints:

- PostgreSQL is the sole case-folding authority for SQL Search comparisons;
- do not lowercase SQL comparison parameters in Java;
- preserve raw-query validation and literal LIKE escaping;
- keep stored-side `lower(column)` and V2 lower-expression index alignment;
- snippets must locate matches in original cleaned text without applying offsets from length-changing folded copies;
- default JVM locale must not affect text/tag/snippet behavior;
- retain rank buckets, type-name order, bounds, fuzzy threshold isolation, query counts, batching and privacy;
- no V1/V2/DBML/schema/dependency/public Search contract change;
- no unaccent/citext/collation/Unicode-normalization feature work;
- no generic cross-module normalization/persistence framework;
- no Phase 13 work.

After accepted maintenance owner commit/push, rerun `$codex-milestone-review`.
