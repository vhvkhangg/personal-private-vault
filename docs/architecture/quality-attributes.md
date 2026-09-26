# Quality Attributes and Priorities

The project optimizes for a personal long-lived vault, not for high-traffic public SaaS scale.

## 1. Data integrity — highest priority

Incorrect or internally inconsistent personal data is more harmful than small performance costs.

Implications:

- PostgreSQL constraints and foreign keys are preferred where rules can be enforced safely at the database level.
- Domain validation still exists for business semantics that cannot be represented cleanly in DBML/SQL constraints.
- Finance uses a ledger model instead of storing a mutable balance as the primary truth.
- Derived values such as album image counts are computed rather than treated as independent authoritative values.

## 2. Evolvability

Requirements are expected to change.

Implications:

- business-capability modules isolate change;
- module internals are not shared;
- Flyway provides append-only executable schema evolution;
- external integrations are behind application/infrastructure abstractions;
- object-storage provider choice is not embedded into domain models.

## 3. Maintainability and understandability

The codebase is also a learning project.

Implications:

- prefer explicit, readable module APIs over clever abstractions;
- avoid premature generic frameworks;
- avoid repository-wide `controller/service/repository/entity` packages;
- avoid `common`, `shared`, or `util` dumping grounds;
- use design patterns only when a real problem justifies them.

## 4. Privacy and secret hygiene

The application stores sensitive personal information, but the repository contains only source code and architecture artifacts.

Implications:

- credentials, refresh tokens, database passwords, backups, and vault content must never be committed;
- passwords and PINs are stored only as hashes;
- binary media is stored outside PostgreSQL;
- production hardening is revisited before Internet exposure.

## 5. Portability

The owner must be able to leave the application without losing access to personal data.

Implications:

- export to portable formats is a first-class requirement;
- Markdown content remains Markdown where practical;
- raw/unknown Obsidian frontmatter is preserved;
- object storage uses an S3-compatible abstraction rather than a provider-specific domain contract.

## 6. Performance — adequate, evidence-driven

The system is permanently single-user. Optimize proven bottlenecks rather than designing for hypothetical multi-tenant scale.

Initial choices:

- PostgreSQL-first search;
- relational indexes based on real query patterns;
- no Elasticsearch/OpenSearch until justified by measurements;
- no microservices for scaling that is not required.

## 7. Testability

Module boundaries and application contracts must make it practical for Antigravity to create focused unit, integration, persistence, module, and API tests without depending on private implementation details.
