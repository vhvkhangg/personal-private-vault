# Codex Pre-Handoff Review — Backend Phase 6 Media + Location

- Date: 2026-09-29
- Result: **READY FOR HANDOFF**
- Mode: preparation review only; no production code, tests, or implementation handoff created

## Prerequisites

Phase 5 is owner committed/pushed and documented as `COMPLETE — FROZEN`; its handoff is archived at
`docs/implementation/phase-5/handoff.md`. `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
Phase 5 is not a milestone phase, so no intervening milestone review is required. The owner-approved Phase 6
scope has a README and preparation-review record. Phase 6 itself triggers the Phase 4–6 milestone review
after implementation, final review, owner commit/push, and closeout.

## Review

- **Scope and frozen schema:** Media owns exactly `albums` and `images`; Location owns exactly `brands`,
  `location_categories`, `addresses`, `locations`, `location_category_assignments`,
  `location_dining_service_styles`, and `location_business_hours`. These nine tables, their checks, enum
  values, foreign keys, and uniqueness constraints align with frozen DBML and Flyway V1. No migration or
  frozen-baseline edit is proposed. Media and Location remain separate top-level modules.
- **Dependencies and public API:** The frozen matrix allows Media → Vault only and Location → Vault/Reference
  only. Expected named interfaces are narrow; no Media↔Location dependency or cross-module repository/entity
  access is proposed. Capability-oriented public packages and immutable views are appropriate; concrete
  dependency narrowing and meaningful `package-info.java` files belong to the later implementation handoff.
- **Integrity, security, and concurrency:** Album, Image, Brand, and Location each have their own transactional
  Vault identity. Image object-key and optional checksum duplicates are conflicts, including races. Image
  count is derived by database count, not stored or calculated by loading all images. Price/currency rules
  match the exact frozen checks, including currency-only state. Category and dining-style assignments are
  idempotent sets. The hours contract distinguishes unknown, closed, split, and overnight intervals and
  requires atomic parent-scoped replacement serialized per Location. Binary storage, geocoding, global search,
  deletion, and unrelated modules are explicitly deferred.
- **Testing and evidence:** The preparation requires PostgreSQL Testcontainers, rollback coverage for all four
  Vault-backed aggregates, deterministic uniqueness/set races, hours replacement contention, exact Modulith
  boundaries, unchanged Flyway/Hibernate validation, final Java 25 Maven verification, and `git diff --check`.
  H2 and timing-only race proof are excluded. Evidence must classify known diagnostics; no IDE inspection was
  run during this preparation review, so no IDE-clean claim is made.
- **Tooling and repository hygiene:** Separate Media and Location domain skills, one phase rule, and scoped
  module instructions capture real distinct invariants without new agents or hooks. Existing implementer and
  auditor routing is sufficient. Both module directories remain preparation stubs with `internal/.gitkeep`;
  implementation should remove each marker once real content exists. No stray Phase 6 Java/test source was
  found. Two editorial wording issues in the preparation docs were corrected during review; scope is unchanged.

No blocking findings. The preparation is precise enough for a subsequent Codex implementation handoff after
the owner commits/pushes it. Do not create that handoff automatically.

Suggested Conventional Commit message: `docs(phase-6): prepare media and location handoff`
