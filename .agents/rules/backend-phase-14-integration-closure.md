---
trigger: model_decision
description: "Apply when preparing, implementing, testing, or reviewing Backend Phase 14 portability/object-storage/operational closure."
---

# Backend Phase 14 — Integration Closure

Canonical scope:

`docs/implementation/phase-14/README.md`

Implementation requires:

1. Phase 14 `$codex-pre-handoff-review` = `READY FOR HANDOFF`;
2. owner commit/push of accepted preparation;
3. active Phase 14 Codex handoff.

Core boundaries:

- new `portability` leaf module is the only ADR-0017 cross-table snapshot exception;
- snapshot access is read-only JDBC, explicit allowlist, PostgreSQL repeatable-read, no mutation;
- `media` owns S3-compatible binary storage;
- no storage provider SDK leaks outside Media infrastructure;
- no schema/Flyway/DBML change;
- no auth secret/security-state export;
- no binary media inside the portable ZIP;
- only approved binary success routes bypass `ApiResponse`; failures before response commitment use canonical JSON,
  while failures after committed binary bytes abort the transfer and never append/reset to JSON;
- portability snapshot transaction/resource lifetime must cover actual row consumption and clean up on success, failure,
  timeout, cancellation and client disconnect;
- managed upload success requires confirmed metadata commit; an uncertain commit may be resolved by an exact positive
  reconciliation, but absence/mismatch cannot authorize deletion; automatic compensation requires authoritative
  rollback/abort evidence from the original transaction boundary itself (not a later lookup/timeout/disconnect), while
  manual cleanup requires quiescent upload writers and
  settled transactions; object compensation is bounded (3 attempts) and failed cleanup must not mask the primary failure;
- no hard delete;
- no backup automation/provider choice;
- no live Feed scheduler/provider integration;
- no recurring Finance posting scheduler;
- no deployment/frontend/RAG work.

Relevant skills:

- `backend-integration-portability-storage`
- `media-domain-modeling`
- `rest-api-http-contracts`
- `authentication-security`
- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`

No custom agent/hook is required.
