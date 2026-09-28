# Codex Final Review — Backend Phase 1 Reference/Vault Foundation

- Date: 2026-09-27
- Reviewer: Codex
- Scope: Final re-review of handoff `backend-phase-1-reference-vault-foundation` after transaction-race and documentation remediation
- Baseline / working tree: `main` at `faeeaf294f5252fc7af14daf04a405c819c3252c` plus the current uncommitted Phase 1 working tree
- Test evidence: `docs/implementation/phase-1/test-evidence.md` records `mvn -f backend/pom.xml clean verify`, exit status 0, with 57 tests, 0 failures, 0 errors, and 0 skipped against PostgreSQL 18.6. Retained Surefire reports match those per-suite counts and show the expected `23505` path in the tag-concurrency regression. The last remediation changed documentation only, so the retained evidence remains applicable. Codex did not rerun tests.

## Findings

### Critical

None.

### High

None.

### Medium

None.

### Low

None.

No blocking findings remain.

## Architecture / Database Conformance

- Flyway/JPA: The frozen DBML and Flyway V1 SHA-256 values still match the manifest. Retained PostgreSQL evidence verifies 41 named enums, 69 application tables, 103 foreign keys, and Hibernate validation of all 11 Phase 1 entities.
- Native enums: `platform_kind`, `vault_entry_type`, and `rating_grade` use Hibernate PostgreSQL named-enum mappings rather than varchar emulation.
- Module ownership: `reference` and `vault` retain `allowedDependencies = {}`; entities, repositories, services, and the package-private `TagCreator` remain internal. The retained Spring Modulith architecture test passed.
- Vault behavior: The explicit capability matrix remains fail-closed, including `FILM_CREDIT` favorite-only and `BRAND` rating/tag support. Recycle-bin transitions, metadata retention/write blocking, deterministic tag ordering, and public immutable views conform to the handoff.
- Transaction remediation: Tag insert conflicts roll back in an isolated `REQUIRES_NEW` boundary, and canonical lookup runs in a fresh transaction. The retained concurrent PostgreSQL test exercises and recovers from the actual unique-index conflict.
- Lombok/JPA safety: Entities use narrow Lombok annotations with protected no-args construction; only the embeddable composite key has generated value equality/hash semantics.
- Documentation: Phase 1 and repository/backend indexes now accurately state that implementation and verification are complete and awaiting owner commit. Phase 1 remains active rather than prematurely frozen, and the `TagCreator` source link resolves.

## Residual Risks / Questions

- The concurrency test's worker transaction commits outside the test method's main-thread rollback, and its cleanup participates in the main transaction. Isolated names and a fresh Testcontainer prevent this from affecting the retained result, but independent-transaction cleanup would improve future test isolation.
- Favorite and tag-assignment existence-check/insert sequences could be hardened for concurrent callers in a later scoped change; this does not violate the current handoff's tested single-user behavior or its explicit tag-creation race requirement.

## Final Review Status

`READY FOR OWNER COMMIT`

The owner may commit and push the reviewed Phase 1 slice. Phase 1 should be marked complete/frozen only after that owner action.

> This status is a code-review workflow result, not an automated commit/push action.
