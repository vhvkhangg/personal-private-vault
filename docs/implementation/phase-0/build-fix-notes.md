# Backend Phase 0 — Build Fix History

Status: **HISTORICAL / SUPERSEDED**

This file records intermediate fixes made while completing Backend Phase 0. It is retained only as
development history and is **not** the canonical source for current tool permissions or Phase 0 status.

Current sources:

- Phase 0 baseline/status: `README.md`
- Antigravity permission guidance: `docs/implementation/antigravity-cli-permissions.md`
- Governance regression evidence: `governance-test-evidence.md`
- Final Codex review: `../../reviews/2026-09-27-backend-phase-0-final-governance-codex-review.md`

## Historical build fixes

1. Testcontainers dependencies were updated from obsolete 1.x-style artifact names:
   - `org.testcontainers:postgresql` → `org.testcontainers:testcontainers-postgresql`
   - `org.testcontainers:junit-jupiter` → `org.testcontainers:testcontainers-junit-jupiter`

   Spring Boot 4.1.1 manages the Testcontainers 2.0.5 versions, so a separate Testcontainers BOM was not required.

2. Maven Enforcer was relaxed from an exact latest-patch minimum to the supported Maven 3.9 line
   (`[3.9.0,4.0.0)`). Maven 3.9.16 remained recommended while Maven 3.9.15 was accepted.

3. The first Antigravity pass stopped during Maven model validation and executed zero tests. After the build
   fix, a new explicit test pass was run and the architecture verification test passed.

4. Antigravity permission and repository-hook guidance evolved during subsequent governance reviews.
   **Do not use earlier broad Git permission recommendations from Phase 0 history.** The canonical current
   guidance is `docs/implementation/antigravity-cli-permissions.md`.
