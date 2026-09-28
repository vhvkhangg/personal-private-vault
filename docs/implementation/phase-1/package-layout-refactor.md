# Phase 1 Foundation — Package Layout Refactor

Status: **READY FOR OWNER COMMIT**

This owner-approved maintenance slice changes package/document organization only. Backend Phase 1 business behavior
and database semantics remain frozen.

## Scope

- organize `reference` public API into `catalog/`, `view/`, and `enums/` named interfaces;
- organize `vault` public API into `entry/`, `metadata/`, `view/`, and `enums/` named interfaces;
- mirror capabilities under `internal/application/<capability>/`;
- keep capability-oriented interface names and descriptive internal implementations; do not introduce generic
  `Service` / `ServiceImpl` pairs;
- add `package-info.java` to every new meaningful package;
- update imports/tests for package moves;
- remove redundant `.gitkeep` files;
- record ADR-0015 and synchronize architecture/repository package documentation;
- reorganize Phase 0/1 implementation documentation into per-phase folders;
- prepare Phase 2 documentation/agent guidance without implementing Phase 2 production code.

## Explicit non-goals

- no database schema/DBML/Flyway change;
- no Phase 1 business-rule change;
- no new Phase 2 production implementation;
- no new runtime dependency;
- no logging added to routine `ReferenceCatalogService` reads.

## Verification required before owner commit

Run locally with Java 25 / Maven 3.9.x:

```text
mvn -f backend/pom.xml clean verify
```

Expected review focus:

- Spring Modulith named-interface visibility and module verification;
- all moved imports/tests compile;
- behavior remains unchanged;
- no duplicate legacy source files remain after running `scripts/apply-package-layout-migration.ps1`;
- docs/ADR/package-tree consistency;
- Phase 2 remains preparation-only.

## Verification evidence

The required post-refactor local verification has now been completed successfully:

```text
mvn -f backend/pom.xml clean verify
```

Canonical retained evidence:
[`package-layout-refactor-test-evidence.md`](package-layout-refactor-test-evidence.md).

The supplied 2026-09-28 build artifacts contain 57 passing tests (0 failures, 0 errors, 0 skipped) generated on
Java 25.0.2 / Windows 11, followed by the packaged Spring Boot JAR.

## Review command

After local Maven verification, run `$codex-final-review` and explicitly review this maintenance scope. Phase 2
implementation must wait until this refactor is `READY FOR OWNER COMMIT`, committed/pushed, and then receives a new
Codex implementation handoff.

## Codex remediation

- [x] Retained package-layout-specific evidence for the exact `mvn -f backend/pom.xml clean verify` run in
  [`package-layout-refactor-test-evidence.md`](package-layout-refactor-test-evidence.md), including successful
  exit/result, toolchain/environment record, and the 57-test post-refactor totals.
- [x] Synchronized `docs/architecture/testing-and-review.md` and the test-workflow wording in
  `docs/repository/repository-package-tree.md` with the current owner-approved Codex/Antigravity workflow.
- [x] Corrected module-specific public-package examples: `reference` advertises `catalog`/`view`/`enums`, while
  `vault` advertises `entry`/`metadata`/`view`/`enums`.
- [x] Returned this scope to **AWAITING CODEX REVIEW**. Phase 2 implementation remains blocked until this review passes.
