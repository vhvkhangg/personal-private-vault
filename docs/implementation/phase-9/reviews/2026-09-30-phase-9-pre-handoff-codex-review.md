# Phase 9 Collection pre-handoff Codex review — 2026-09-30

Status: **CHANGES_REQUESTED**. This is a preparation-only review; no implementation handoff or production-code change was made.

## Blocking findings

1. **Medium — operation and default semantics are not defined tightly enough for a handoff.** `docs/implementation/phase-9/README.md` describes entities, validations, set assignments, and ID reads, but does not say whether create/update/find-by-ID are required for each of Music, Shopping, and Software, whether credit/platform removal is in scope, or how omitted/null Music `version` and Shopping `status` commands relate to Schema v1's `ORIGINAL` and `WISHLIST` defaults. Shopping's purchase-state transition implies update, while the test contract omits a clear update/read operation matrix. State the exact Phase 9 operations and absent-value/default behavior, and explicitly defer any unsupported assignment removal rather than leaving implementer choice. Align the test contract with that decision; do not broaden beyond the owner-approved foundation.
2. **Medium — assignment-list bounds contradict the stated read contract.** The Music credit and Software platform sections require parent-scoped deterministic reads, while the same README says *all* Collection reads are bounded; only browse/recent/status/type reads explicitly require a positive limit. A single Music or Software item can accumulate an unbounded number of assignment rows. Define a positive explicit limit (and deterministic tie ordering) for per-item credit/platform reads, or explicitly justify a different bounded result contract, and require tests for the bound and ordering. Keep global lists/search deferred.

## Checks and non-blocking observations

- Preconditions passed: owner commit `bf8f199` contains Phase 8 implementation, Phase 8 is recorded frozen, `ACTIVE.md` is `NO_ACTIVE_HANDOFF`, and no Phase 8 milestone prerequisite exists.
- The five proposed Collection tables, composite keys, enums, Vault identities, price/currency checks, and Shopping purchase-time check match frozen DBML and Flyway V1. The `collection → vault, people, reference` direction matches the frozen dependency matrix and nested ownership table.
- The parent-owned API/nested-module strategy, PostgreSQL Testcontainers and observable contention expectations, milestone gate, and deferred-scope exclusions are appropriate. New Collection skill/rule/module instructions are narrow; existing agents and hook suffice. Existing `.gitkeep` files are placeholders until implementation and are explicitly slated for removal.
- `git diff --check` passed. No build or IDE inspection was run for this docs-only preparation review; no IDE-clean claim is made. The frozen `collection/package-info.java` still mentions future public search contracts, but the Phase 9 scope correctly defers Search; implementation should keep the parent API limited to the approved Collection operations.

## Gate

Preparation is **CHANGES_REQUESTED**. ChatGPT should resolve the two contract ambiguities in Phase 9 preparation, then return the package for Codex pre-handoff re-review. Do not create the implementation handoff or commit/push this preparation slice yet.
