# B0/B1 — Full current Non-RAG Backend audit plan

**Status:** Future design, not an executed test plan. Before B0, Owner must approve exact audit scope and bounded diagnostic commands, H1 harness rewrite and related qualification (G2) **must be independently accepted and Owner-published without exception under this contract**, and D0 architecture expectations must be traceable. B0 may not bypass H1 by asserting it is unnecessary; a different order would require a new explicit Owner decision, amended contract and independent review before any execution. Full code inspection and runtime tests were **not** performed in this preparation.

## 1. Non-RAG inventory and module ownership

Derive all tracked files from the published approved SHA; enumerate `backend/**` recursively, `pom.xml` files and build/CI/script resources, `backend/**/AGENTS.md`, module descriptors, SQL/Flyway, static assets/config, test and Testcontainers fixtures. Classify every file by capability, effect, review depth and source/test pair. Include cross-cutting login/auth, Vault, Settings, Reference, People, Personal, Finance, Fiction, Film, Media, Collection, Knowledge/Vocabulary, Global Search, Feed/Radar, Saved Resources/Inbox, Google Drive, Import/Export/Portability and other modules found in actual source. **This list is illustrative, not an exhaustive confirmed package graph**; actual source inventory wins.

Functional **RAG/Python engine, suspended S19-2 QA/inference and external indexing** are excluded; read shared build/config/security/data boundaries only to ensure Non-RAG compatibility and exclusion. No Python model run, OCR/GPU or dataset ingestion authorized. Frontend H1-F remains paused; inspect consuming contract needs without declaring Frontend accepted.

## 2. Primary verification tracks

### B-API — Correctness and response contracts

Build per-route matrix `method/path/operationId/profile/DTO/status/error/owner invariants/pagination/cache/test`. Inspect generated OpenAPI against controllers and actual HTTP responses. Check list/detail design; do not unilaterally add `name` to all responses. Query complexity, null/missing semantics, backward compatibility, absence of unsafe transformations, unknown-profile default-deny, response headers and localized errors. Existing Public Fiction/Film 19-property responses, PRIVATE 18-property DTOs and eight PUBLIC GET pairs must be preserved unless Owner revises them.

### B-SEC — Fixed PUBLIC/PRIVATE profiles and information disclosure

Trace auth issuance, credential state, refresh/revoke, transition/lineage, persistence authority and filter ordering. Build negative tests for anonymous/expired/invalid tokens, stale PUBLIC sessions, route aliases, search, entity IDs, joined People references, NSFW flags, metadata, error messages, cache/no-store and DTO field filtering. Test both default-deny and exact positive Public allowlist. Do not require unrequested ABAC, distributed permissions or security hardware. Preserve auth integrity, credential confidentiality, no privilege escalation and basic owner-data protection.

### B-DB — PostgreSQL and transactional integrity

Compare Java entities/repositories, DDL/Flyway/DBML, constraints/indexes and actual Postgres migrations; verify singleton owner, referential integrity, keyset/offset semantics, query plans as warranted, transaction isolation boundaries, rollback after failure and concurrency (duplicate insert, lost updates, re-trash/restore, feed refresh, import/export). Prefer deterministic integration tests and captured SQL over theoretical concerns. No schema change during read-only audit.

### B-INT — Features and third-party providers

- **Vault:** accepted PRIVATE-only trash browse with `items,nextCursor`, default limit 25, range 1–100, ID-descending cursor, exact 3-field items, no-store, weak inter-request consistency, valid 401/403/400/500; old lifecycle unchanged.
- **Dashboard/Inbox/Radar/Vocabulary/Search:** compare actual API to BF26-S5 Option A's existing route/consumer matrix; no new aggregate by default. Distinguish cached data from live provider freshness and no silent background job activation.
- **Google Drive:** two-account authority, file/folder projections, pagination, filters, token/session boundaries, failure paths and service provider flags; operational/live configuration `NOT_VERIFIED` unless separately approved and actually exercised.
- **Media/object storage:** signed URL/file metadata, streaming cancellation/backpressure/cleanup, object-provider error ambiguity; actual remote provider not inferred from mocks.
- **Import/Portability/Backup:** contract differences between portable export and complete disaster recovery; archive path traversal, concurrency/rollback/partial publication, media binaries not in export, real restore testing deferred pending an exact operations plan.
- **Feed/RSS:** schedule disabled by default/opt-in controls, single-JVM overlap rule, bounded fetch, SSRF/network behavior if relevant, provider errors, refresh marker definitions.

### B-QUALITY — Simplification and tests

Find duplication, dead paths, unnecessary abstractions, excessive DTO mapping, module boundary leaks, potential N+1 and overbroad application services. Every proposed simplification must name **real** maintenance/performance cost and show no regression of security, transaction or contract behavior. Apply risk-weighted tests: expected behavior and negative/security tests, architecture validation, Testcontainers, deterministic concurrency faults, API schema checks and resource handling. Full Maven `verify` or equivalent **only if authorized and compatible**; prefer existing scripts/watchdog bounds. **Before B0 executes Maven:** enumerate Surefire/Failsafe selectors, inherited test fixtures and any RAG-enabling tests (reviewer specifically identified `RagLifecycleIntegrationTest` for later verification). Approve an explicit non-RAG test-selector manifest or document controlled isolation; an unqualified `mvn verify` must **not** be claimed as an isolated Non-RAG suite. Never activate suspended RAG or change build config merely to pass audit. Report exact tool versions, selected/excluded test identities, elapsed/capped evidence and environment limitations.

### B-OPS — Build, dependencies and deployment

Audit `pom.xml`, dependencies, version compatibility, deployment config, secrets, defaults, CI/CI history, logs/headers, JVM/Postgres clocks, migration readiness, backup/restore feasibility, port/connectivity constraints. Attempt no live deploy/cutover, secret read, credential rotation or installation without specific Owner grant. Preserve P17 AUTH-CLOCK historical NOT_VERIFIED unless newly reproduced.

## 3. Evidence classification and minimum regressions

- For each in-scope module: actual source paths, relevant requirement, inspected production/test classes, deepest negative condition, statuses `VERIFIED / PARTIAL / NOT_VERIFIED / N/A`, Git SHA and receipt.
- At least one adversarial **authorization path** for each API category, transaction failure path for changed transaction logic, exact schema/DTO test for changed API, and old behavior checks for changed source; do not invent a universal numeric coverage target.
- Test environments: capture OS/JDK/Maven, PostgreSQL/Testcontainers versions, compose/docker status, feature flags and clock/locale. When absent, distinguish **environment blocker** from confirmed source defect.
- Record raw evidence under existing ignored `logs/` only when granted; no deletion/rewrite. Tests may mutate temporary test databases if explicitly authorized, **never** Owner data or production tokens.

## 4. B0 report and B1 remediation entry

B0 findings must identify exact file/line/behavior, scenario reproduction or reasoned counterexample, current contract, impact, severity, confidence, smallest viable fix, affected tests and frozen/public API/DB approval gate. Optional improvements are separate proposals. Independent reviewer validates the audit coverage/findings before Owner selects batches.

B1 may begin only on an Owner-approved **exact changed-file allowlist** with a tested scope and contract. Each B1 Codex implementer is distinct from the B0 finding reviewer and B1 independent final reviewer. Requalify baseline, implement minimal fix, self-review, hand back diff and test receipts; independent Codex reviewer reruns or source-qualifies evidence and catches new leaks/regressions; Owner alone publishes.

## 5. Stop conditions

STOP on ambiguous Owner scope, dirty conflicting worktree, protected files without exception, tests unable to prove mandatory behavior, undisposed actionable findings including LOW/INFO, missing independent reviewer, unauthorized provider/data access, full-app evidence incorrectly claimed from a subset, or material drift between tested SHA and publish SHA.
