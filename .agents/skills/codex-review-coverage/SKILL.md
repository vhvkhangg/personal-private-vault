---
name: codex-review-coverage
description: Evidence-driven shared review coverage and repository/package-tree audit rubric; invoked by Codex pre-handoff, final, milestone and full-backend audit skills as applicable.
---

# Codex Review Coverage — shared mandatory rubric

**Proportional applicability (current):** use the 29 dimensions as a *thought checklist*, not 29 compulsory projects, test suites, artifacts or blocking findings. For a slice, document a few relevant categories with evidence; summarize unchanged/irrelevant ones as `N/A`. Reused source-applicable evidence is acceptable. Mandatory approved requirement NOT_VERIFIED and open CRITICAL/HIGH/MEDIUM block; LOW/INFO go to milestone/audit. Future RAG Phase 19 must not be inferred from the historical Phase 18 OCR/GPU/judge rubric. See [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md).


**Scope-aware, not mechanically exhaustive:** This is a **checklist of dimensions to consider** for each review. Classify each item `PASS / FINDING / N/A / NOT_VERIFIED`, with exact paths and evidence for high-risk dimensions. Only declare `N/A` with a reason, e.g. "no API code in docs-only preparation". `NOT_VERIFIED` is **not** `PASS`. A docs-only review MUST inspect whether evidence obligations are specified; it must not pretend to have run runtime tests. Historical frozen phases remain untouched without owner-approved maintenance.

## Proportionality rule for single-user RAG Phases 18–22

**29 dimensions are review questions, not 29 implementation requirements.** Before marking a finding blocking, distinguish: (a) a concrete defect in an existing approved behavior, (b) missing mandatory evidence for the **current phase**, (c) a proposed optional optimization, and (d) an acceptance/architecture change requiring explicit owner consent. State smallest correction and actual proof; prefer reusing unchanged independent tests/benchmarks. Do not demand full distributed/enterprise resilience, all-future model evaluation or new OS Firewall/WFP controls for Practical Local Offline. Use the [owner's KEEP/SIMPLIFY/DEFER review gate](../../../docs/workflow/rag-phase-delivery-guardrails.md) and keep current RAG privacy/consent and process/transaction correctness mandatory.

## 29 review dimensions

1. Owner intent, business acceptance criteria, changed behavior, permitted source selection and explicit non-goals.
2. Requirement traceability: each acceptance requirement -> affected contract -> test/verification; detect requirements silently dropped or contradictory.
3. Architecture consistency across accepted ADRs, C4/Structurizr/diagrams, current source, DBML, module graph, and workflow; distinguish historical decisions from active approvals.
4. Module ownership/public NamedInterface contracts, dependency cycles, encapsulation, transaction/data-flow boundaries, unintended dependency or service layering.
5. **Repository/package/folder structure**: recursively inspect actual relevant subtree, not a sample; check capability-oriented names, grouping of `command`, `view`, `exception`, `enum`, `api`, `internal`, web/controller, repository and tests; mixed responsibilities, excessively large flat packages/classes, wrong visibility/placement and redundant wrappers; compare canonical package-tree inventory.
6. **Shared contracts/exception placement**: find duplicate or drifted envelopes, `ApiResponse`, OpenAPI conventions, error catalogs, exception hierarchies, adapters/helpers across owners; assess whether they belong at existing root/shared transport boundary versus module-local. Obey **ADR-0016**: it deliberately puts application-wide HTTP envelope at application root; do **not** demand `common/` purely by taste or create a new Spring Modulith module without approved ADR. Evaluate `common` or subpackage only if an actual pain point and module-boundary-safe change warrant it.
7. SOLID in concrete classes: SRP/cohesion, OCP where variation exists, LSP contracts, ISP boundary size, DIP dependency direction. Check interfaces/classes/exceptions for real role and correct placement.
8. Programming principles: DRY, KISS, YAGNI, separation of concerns, least knowledge, immutability, invariants, deterministic behavior, explicit null and state semantics.
9. **Patterns and anti-patterns**: Adapter, Strategy, Facade, Factory, State, Specification, Repository, events, ports only with real purpose; avoid gratuitous `ServiceImpl`, single-implementation abstraction with no boundary, generic `utils`, god objects, cycles or framework cosplay.
10. Business/domain correctness including statuses, transitions, idempotency, read-only constraints, edge cases, timezone/locale/currency, duplicates, missing-source behavior.
11. Public API/wire compatibility: REST/OpenAPI descriptions/examples/errors, contract versioning, pagination/filter/sorting, validation, backward compatibility, source provenance and deterministic error codes.
12. Input/data validation: malformed/hostile inputs, bounds, formats, localization, UTF-8/UTF-16 offsets, overflow, path/archive/link traversal, schema and file format drift.
13. Authentication, authorization, privacy, consent, field-level allowlists, least privilege, record visibility, tenant/single-owner policy, no-count/no-score leakage.
14. Credential/key handling, source-to-log/trace/history leakage, backups, caches, retention and purge, network egress/telemetry, supply-chain/download behavior.
15. Transaction atomicity, isolation, lock identity/order/lifetime, optimistic checks, concurrent writes, last-check-to-commit races, idempotent retry, lost events and duplicate work.
16. Failure/recovery/operations: timeouts, cancellation, job leases/heartbeats, restart, cleanup/orphans, rollback/restore, migrations, compatibility across service restarts.
17. Database: Flyway/PostgreSQL/JPA mappings, constraints/indexes, FK/cascade, permissions/RLS, query plans, N+1, pagination, evolution of schema and data.
18. Algorithmic/time/memory/I/O complexity: bounded loops, batching, memory budgets, lock contention, latency profiles, CPU/GPU/VRAM/disk, resource admission/fairness.
19. Data/index/recommendation quality: eligible corpus isolation, hybrid retrieval, ANN-vs-exact evidence, reranker, catalog-ID validation, hallucination/unsupported-claim/abstention, cite/locator precision.
20. Document/OCR/multilingual fidelity: native extraction vs OCR, layout/table order, language coverage, PDF page/box coordinates, XLSX formulas/caches, DOCX hidden text, Obsidian embeds and transitive scope.
21. Security testing: adversarial fixtures, privilege denials, negative filters, prompt injection, deterministic concurrent/lost-event barriers, OS source stability, unauthorized active worker publication.
22. Tests quality: unit, integration, contract, property/fuzz as justified, E2E, environment parity, assertion adequacy, determinism, coverage *of risk* not just percent, fixture privacy.
23. Performance tests/benchmarks: credible hardware/test volume, p50/p95 including queue time and cold start, failure thresholds, before/after evidence; avoid unsupported performance claims.
24. Evaluation science: independent held-out set, labels, leakage, precision/recall by language/format/status, no-answer false accepts, evaluation cost, judge calibration and failed/pending coverage.
25. Dependencies and lifecycle: compatibility, licenses, vulnerabilities, SBOM where applicable, pin/upgrade policy, offline model artifact hashes, lockfiles and unused dependencies.
26. Build/test/tooling/CI: Maven/Python packaging, static analysis, warnings, formatting, duplicate analyzer, architecture verification, data/service integration tests; distinguish executed tools from suggestions.
27. Logging/observability: structured error severity, useful safe trace correlation, redacted telemetry, operational alerts, measurements, non-sensitive execution reports and incident recoverability.
28. Documentation and governance: README/ADR/roadmap/handoff status/review history, spelling, stale TODO, link integrity, source-of-truth drift, package tree, diagrams, environment setup reproducibility.
29. Hygiene and delivery: `.gitkeep`/generated or cache artifacts, obsolete/moved duplicates, filesystem casing, line endings, ignored files, clean diff, only changed files in patch and no unapproved frozen edits.

## Required evidence protocol and reporting

- **Preparation:** for each applicable dimension, verify it is explicitly covered by scope/acceptance/test plan; never pretend not-yet-existing runtime code passed. Separate blocking design flaws from future measured-selection gates.
- **Final implementation review:** substantiate business paths and changed subtrees by inspecting source, tests and migrations; run compatible build/architecture/integration/static checks where available; record commands/versions and actual results. Deep-check **directory/package tree** against current files and ADR (not documentation claims).
- **Full audit / milestone:** inventory cross-phase and cross-module duplicates; check entire relevant repository tree. Make **coverage ledger** with each dimension, `PASS/FINDING/N/A/NOT_VERIFIED`, evidence pointers, limits and owner follow-up. Verify uninspected/blocked dimensions rather than treating them as green.
- For each material finding: immutable ID, severity (Critical/High/Medium/Low), evidence path+line/behavior, consequence, smallest proportional fix, explicit regression proof, frozen-boundary/owner-decision impact. Label lower-confidence inference as hypothesis and demand verification, not as proven defect.
- **Blocking rule:** no `READY` when any applicable *critical* security/consistency dimension is `NOT_VERIFIED` without a separately owner-accepted, explicitly deferred verification gate, and never when an essential privacy/correctness contract is unresolved. Missing runtime proof in a docs-only gate becomes a mandatory handoff/release acceptance condition, not a fabricated test result.
- Optimize **correctness and maintainability**, not maximization of abstractions/checklist counts. A structural refactor across frozen modules requires concrete evidence and explicit owner authorization.
