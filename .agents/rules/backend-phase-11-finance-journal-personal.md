---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 11 Finance + Journal + Personal work."
---

# Backend Phase 11 — Finance + Journal + Personal

Canonical scope:

`docs/implementation/phase-11/README.md`

Before implementation require:

1. `docs/implementation/phase-11/preparation-review.md` = `READY FOR HANDOFF`;
2. an active Phase 11 Codex implementation handoff.

Use `finance-journal-personal-domain-modeling` plus the standard Java/Spring, pragmatic SOLID, reuse,
modular-monolith, JPA/PostgreSQL, design-pattern-selection, and backend-testing skills.

Constraints:

- Finance -> public Reference only; Journal -> none; Personal -> public Reference + Location Address only;
- no Vault identity/capabilities for these modules;
- money uses exact BigDecimal semantics;
- ledger balance is derived, not cached as source of truth;
- transaction/recurring entry sign/shape and category compatibility follow the Phase 11 contract;
- same-transaction mutation must be serialized/fresh-state safe;
- wallet currency change is rejected only while any currently retained transaction/recurring entry references the
  wallet; after full replacement removes the last retained reference, currency may change again; currency mutation
  and entry assignment share wallet locks and use ascending wallet-ID ordering;
- do not invent ever-referenced history or a hidden marker absent from Schema v1;
- category-kind mutation must remain compatible with all historical references and use the same category guard
  as category assignment;
- recurring update/full replacement/delete/restore share one recurring-rule row guard with fresh parent/child
  state; update of a deleted rule is rejected until restore;
- concurrency tests observe real PostgreSQL lock/index competition, including Subscription unique-rule races
  driven past application pre-checks;
- recurring runtime scheduler/auto-posting is deferred;
- Journal and Personal Markdown is preserved exactly where non-null;
- Personal one-active-self uniqueness must be race-safe via PostgreSQL partial uniqueness;
- module-owned soft delete/restore is not Vault recycle behavior;
- all collection-valued reads are bounded/deterministic;
- no Search/REST/frontend/calendar aggregation/hard delete/schema redesign/Phase 12+ work;
- no generic framework or custom agent/hook.

After final acceptance, owner commit/push is followed by ChatGPT Phase 11 closeout and Phase 12 preparation.
