# Owner authorization — Spring-only interactive architecture diagrams

**Date:** 2026-11-01  
**Decision:** `SPRING_DIAGRAM_SCOPE_OWNER_APPROVED / EXECUTION_GATED_ON_REVIEWED_PREPARATION / NO_ACTIVE_HANDOFF`  
**Baseline:** remote `main` at `4cf62041796d0b510255ee7014261221cafe3d5d` (CSA26 accepted/published). Later prep/diagram commits must explicitly record their own HEAD; never relabel this baseline as an execution artifact.

## Goal

Prepare an independently reviewable, source-grounded **interactive Spring Boot/Spring Modulith architecture** documentation slice using [Archify](https://github.com/tt-a1i/archify), with static fallback, for the backend **excluding all RAG architecture and behavior**. This is intended to support upcoming backend features and later Frontend Phase 16 without changing production behavior.

The prior CSA26 maintenance independently accepted synchronized *existing* C4/module views. Do not reopen CSA26-003 merely to recreate accepted diagrams. Use new additive, scoped views and keep canonical source/exports stable unless a separate source-backed correction is specifically approved.

## Scope boundaries

**IN:** 19 Java application modules other than `rag`: `authentication`, `settings`, `reference`, `vault`, `people`, `fiction`, `film`, `media`, `location`, `knowledge`, `collection`, `account`, `feed`, `importdata`, `finance`, `journal`, `personal`, `search`, `portability`; actual named interfaces, observed dependencies, request/security lifecycle and persistence/transaction boundaries. Confirm exact count/edges against current sources at execution time.

**OUT:** the `rag` module, Python `rag-engine/`, RAG jobs, snapshots, embeddings, retrieval, incident/consent pipelines, C1/C2, S19-2, CUDA, external indexing, Ideaverse, Drive and unimplemented frontend. Do **not** include a RAG node, RAG edges, overlay, worker or RAG-specific DB schemas in the produced Spring diagrams. A generic Browser/Client actor is fine, but a deployed frontend must not be claimed.

**ALSO OUT:** production Java/Python code, tests, POM/lock/dependencies, SQL/Flyway/DBML, OpenAPI contracts, schema, security policy, runtime config, platform integration, new business functionality, renamed directories, CI/hook changes and modification of issued historical ADRs/reviews. No S19-2 repairs, native/model inference, QA worker or RAG test execution. No blanket `git add`, publishing by agents, or changing accepted frozen artifacts.

## Owner-approved bounded deliverables (execution gated by preparation review)

A bounded set of Archify HTML + typed editable source + static SVG fallback, with an index under `docs/architecture/diagrams/interactive/spring/` (or a reviewed, clearly justified equivalent). Four views: (1) Java module boundaries/dependencies, (2) authenticated REST request flow, (3) login/refresh/revoke/PIN lifecycle, and (4) JPA/Flyway/transaction and integration boundaries. Prioritize usability, readable scope, correct source links and restrained diagram complexity, not maximum node count. The [diagram plan](diagram-plan.md) gives exact evidence and acceptance.

## Third-party tool and content security

Archify repository reference verified on 2026-11-01: `tt-a1i/archify`, reviewed upstream HEAD `a4065941f12be7967436261d9934784e381d423c` (not an installed pin). Its skill currently describes typed JSON IR, `finalize` gates and standalone HTML. **Before installation/execution**, Codex must inspect the actual selected upstream commit, license files (including any license differences between root and skill), CLI/package lock, script behavior, update/network checks and output dependencies. Record the exact vetted version/hash and installation mechanism. Prefer a pinned/local execution and no network egress for project data. The upstream `ARCHIFY_UPDATE_CHECK_DISABLED=1` control may disable optional update checks; verify its behavior from the selected build rather than assuming universal offline operation. No installation or remote service call is authorized in this preparation stage.

Never send private source, passwords, tokens, local absolute paths, corpus content or DB data to a remote diagram service. Generated HTML is executable content: check external URLs/CDNs/scripts and unsafe embedded data; portable/self-contained when feasible. Cite code paths with repository-relative links; avoid absolute Windows paths.

## Review and completion criteria

- First gate: independent `$codex-pre-handoff-review` in **documentation-only maintenance** mode; on success the expected disposition is `READY_FOR_OWNER_COMMIT`. Review may update scoped preparation/review files but may not implement diagrams or issue a live handoff.
- After the Owner publishes reviewed preparation, the approved [one-off diagram authoring task](diagram-authoring-task.md) may be run by Codex. No second approval is needed if scope/security preconditions remain unchanged. If tool vetting or source drift requires a materially different scope, STOP for Owner decision. Keep independent Codex review distinct from the diagram author.
- Acceptance requires exact source/ADR provenance, correct non-RAG exclusion, no speculative runtime edges, interactive navigation/link correctness, static fallback render legibility, privacy/offline checks, reproducible typed source, no unexpected tracked changes, source/export version alignment, and a factual finding/limit ledger.
- If Archify cannot meet security or provenance requirements, stop with `OWNER_DECISION_REQUIRED` and propose another local output route; do not bypass validation or silently publish static substitutes as Archify artifacts.

**S19-2 remains** `SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY`. All historical exhausted counters and model failures remain unchanged; the Spring-only documentation work grants no new RAG permission.

**Next step:** Owner | apply package, invoke `$codex-pre-handoff-review`, then publish independently reviewed preparation | only Spring-only diagrams maintenance scope | execution awaits reviewed and Owner-published preparation; no active handoff.
