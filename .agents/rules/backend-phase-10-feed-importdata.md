---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 10 Feed + ImportData work."
---

# Backend Phase 10 — Feed + ImportData

Canonical scope:

`docs/implementation/phase-10/README.md`

Before Phase 10 implementation require:

1. `docs/implementation/phase-10/preparation-review.md` = `READY FOR HANDOFF`;
2. an active Phase 10 Codex implementation handoff.

Use `feed-import-workflow-modeling` plus normal Java/Spring, pragmatic SOLID, reuse, modular-monolith,
PostgreSQL/JPA, and testing skills.

Constraints:

- `feed` and `importdata` depend only on public Vault + parent Knowledge;
- no nested Knowledge imports/repositories;
- all JSONB boundaries use deep snapshot isolation;
- live feed HTTP adapters and scheduler runtime are deferred;
- FeedItem ingestion owns deterministic dual-key upsert/conflict rules;
- SavedResource is Vault-backed and URL-hash duplicates are race-safe stable conflicts;
- conversions support Study/Information/Note only through parent Knowledge;
- ImportData parse/preview/validate performs no target writes;
- execution is whole-job transactional and uses parent Knowledge only;
- parse/validate/execute/cancel share one owner-local job-row serialization guard; fresh status must be re-read
  under that guard even when the job was already managed in the persistence context;
- hold the guard through the whole transition; incompatible/repeated losers reject without target writes or
  terminal-status regression;
- concurrency tests must prove actual PostgreSQL lock waiting for execute-vs-execute, execute-vs-cancel and
  competing pre-import transitions;
- Note hash is the only Phase 10 database duplicate lookup supported by the frozen Knowledge facade;
- Markdown is preserved exactly and YAML/JSON parsing must be safe;
- reads are bounded/deterministic;
- no schema redesign, Search, REST, frontend, object-storage I/O, generic framework, or Phase 11+ work.

After final review, follow the normal owner commit/push → ChatGPT closeout → Phase 11 preparation workflow.
