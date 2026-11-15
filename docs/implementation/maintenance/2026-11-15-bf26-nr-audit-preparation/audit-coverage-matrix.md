# Comprehensive audit coverage matrix — contract for future execution

**State:** `PREPARATION_DESIGN_ONLY`; none of the checks below has been executed by creation of this file. The matrix is a mandatory **plan and disposition register template**, not a PASS report. For an authorized audit, each row needs an accountable session, affected file manifest, executed check, evidence URI/path/SHA, remaining gaps and a `PASS / FINDING / NOT_VERIFIED / N/A` outcome. `N/A` requires rationale; absence of evidence is **NOT_VERIFIED**, not PASS.

## Coverage ledger fields

`ID | Scope/path inventory | Question / approved invariant | Minimum verification | Evidence receipt | Outcome | Finding IDs | Follow-up owner | Audit HEAD`. A future executor must create and reconcile the ledger against the exhaustive tracked-file inventory; this table supplies dimensions only. Do **not** mark every file deeply reviewed merely because its path was enumerated.

### A. Harness and instructions (H)

| ID | Scope | Review question / observable verification | Completion condition |
|---|---|---|---|
| H01 | `.agents/**`, `.codex/**`, all `AGENTS.md`, adjacent scripts | Exact path/blob inventory; no ignored nested AGENTS overrides, links or executable dependencies | 100% enumerated and dispositioned |
| H02 | Root/nested AGENTS | Precedence, actual Codex discovery, size/duplication; current vs historical instructions | Effective instructions qualified in representative cwd sessions |
| H03 | All skills | Metadata triggers, accidental overlap, stale pointers, dependency/references, missing skill and dormant skills | All skills classified KEEP/SIMPLIFY/MERGE/DEPRECATE/REMOVE with evidence |
| H04 | Existing `codex-backend-audit` skill rewrite | Explicit 2026-11-15 Owner supersession: remove runnable Phase 15 mode; current-only H/D/B/X audit and fail-closed historical invocation | New routing and old-mode STOP verified; Phase 15 evidence unchanged |
| H05 | `codex-create-handoff` | Approved maintenance scope discoverable when ACTIVE is empty; deny stale/ambiguous scope | Replay historical failure + counterexample cases; no silent authority loss |
| H06 | Reviews/implementation routing | Session identities, no self-independent-acceptance, Codex-only workflow, no accidental Antigravity dispatch | Review/implementation/approval gates exercised in dry-run scenarios |
| H07 | `.codex/config.toml`, global CLI config | Effective model/approval/sandbox/MCP/hook/trust settings on target host; no assumption from file text | Actual CLI/version/trust inspection, safe proof and limitations |
| H08 | Git rules + safety hooks | Denial of forbidden Git/gh publication, wrappers/aliases/MCP/GUI bypass limits, benign commands | Adversarial positive/negative tests in disposable fixture; no false claims of total protection |
| H09 | Hooks/scripts/watchdogs | Correct timeout, process cleanup, Windows/PowerShell/WSL behavior; preserve logs | Focused repeatable tests incl. failure/interrupt paths |
| H10 | Rules/roles/resume prompts | Contradictions, overconstraint, drift, current modes, complexity cost | Regression-backed simplification proposal and verified rerun |
| H11 | Current Codex/GPT-6.1 Sol | Model actually available? CLI supports flags and hooks? outcomes across clean sessions | Explicit environment/version plus observed outcomes; otherwise NOT_VERIFIED |
| H12 | Harness change security | Rewritten skill cannot self-authorize code or bypass Owner locks | Permission matrix + forbid-write tests pass |
| H13 | `delivery/` and ZIP/report packaging dependencies, if present locally | Determine actual existence, producer/consumer triggers, review ZIP contents, secret/ignored-artifact inclusion, provenance and accepted/rejected semantics | Evidence-backed PRESENT/ABSENT/NOT_VERIFIED; any approved skill/script changes tested and independently reviewed, no auto acceptance |

### B. Documentation, product and design (D)

| ID | Scope | Review question / observable verification | Completion condition |
|---|---|---|---|
| D01 | All tracked `docs/**` | Complete path inventory, broken links, missing assets, spellcheck/structure, `CURRENT` duplication | Every file receives scope tier; static checks and manual risk review recorded |
| D02 | Owner decisions, ACTIVE, roadmap, indexes | Conflicting status, missing approval, inaccurate publication SHA, superseded grant | Single current truth with preserved immutable history |
| D03 | `docs/adr/**` | Requirements, decision rationale, current acceptance and supersession graph | Each ADR classified CURRENT/HISTORICAL/SUPERSEDED/PROPOSED and fit assessed |
| D04 | `docs/architecture/**` | Business/REST/module/auth data flows and C4 source/export fit | Diagram sources and generated outputs/source boundaries reconciled |
| D05 | `docs/database/**` | DBML, DDL/Flyway, FKs/indexes, schema versions, backward compatibility | Actual mapping and migration evolution supported by evidence |
| D06 | `docs/implementation/**` | Published accepted state, historical provenance, review logs, counters, handoff authority | Current artifacts deep-reviewed, historical records preserved and link/status checked |
| D07 | `docs/repository/**` | Full package tree vs code; misplaced classes, obsolete packages | Reconcile with actual package manifest |
| D08 | `docs/workflow/**` | Minimal Codex-only audit workflow, resumption, authority boundaries, Docs Sync | No conflicting route/auto-authorization, interrupted-run scenario |
| D09 | Other docs/templates/AGENTS | Ownership, overlap, stale instructions, maintainability and discoverability | No unclassified path; proposals bound to user need |
| D10 | Product behavior inventory | Each approved feature's requirement vs accepted evidence, missing/frontend-only/operational gaps | KEEP/REUSE/SIMPLIFY/REPLACE/REMOVE/DEFER + rationale and Owner decision requirements |
| D11 | Privacy/session contract | Public may not reveal NSFW/Personal/People/PRIVATE through any path | Allowlist-to-contract and negative-case mapping |
| D12 | API design for UI | List/preview DTO display labels, query burden, optional join N+1, migration cost | Targeted id-only vs displayName evaluation; no global field-add mandate |
| D13 | Simplicity assessment | Real simpler alternative vs regression/effort/dataloss risk | Objective comparison; speculation labeled, no pattern-by-taste finding |
| D14 | Cross-doc audit evidence | Definitions and timestamps, source vs historical artifacts; docs claims backed by SHA | Every material modern claim has source/test/authority reference |

### C. Non-RAG Backend source, tests and runtime (B)

| ID | Scope | Review question / observable verification | Completion condition |
|---|---|---|---|
| B01 | All Backend modules | Full production/test/resources/build path inventory and module ownership map | Every tracked path dispositioned; all business modules deeply assessed |
| B02 | Business domains | Contract, transitions, invariants, edge cases, lifecycle, owner-visible behavior | Requirement-to-handler-to-test trace, cases checked |
| B03 | Spring/Modulith architecture | Module boundaries, layering, cohesive packages and minimal abstractions | Targeted source/module tests; no speculative refactor finding |
| B04 | HTTP/API/OpenAPI | DTO/error semantics, auth, status codes, list/search/pagination, generated spec | Generated schema and actual HTTP contract match approved behavior |
| B05 | PUBLIC/PRIVATE | Session identity, tokens, profile transitions, default deny, indirect leaks | Negative cases across API/metadata/search/cache/exception paths |
| B06 | PostgreSQL/JPA/Flyway/DBML | Constraints, relations, indexes, migrations, owner and transaction isolation | Real PostgreSQL/Testcontainers evidence; no H2-only substitute |
| B07 | Transactions/concurrency | Races, lost updates, retries, idempotency, isolation and rollback | Deterministic fault-injection/concurrency tests or explicit NOT_VERIFIED |
| B08 | Vault lifecycle | Recycle Bin browse/cursor/restore/trash, cache-control, identity disclosure | Exact accepted V1 route contract still passes |
| B09 | Fiction/Film/Reference | Eight PUBLIC GETs, 19-field safe Public details, Private compatibility; Film POST denied | Route inventory + typed negative/positive DTO checks |
| B10 | Inbox/Feed/Vocabulary/Search | S5 Option A reuse and no accidental new aggregates; Feed scheduler and refresh | Source/DTO/tests and on-demand/provider distinctions verified |
| B11 | Google Drive | Connection authority, folder/file reads, two-account behavior, tokens, provider OFF | Contract tests; live provider classified NOT_VERIFIED unless explicit authorization |
| B12 | Portability/backup/ingestion | Snapshot/export/import integrity, archive safety, absent DR restore claims | Bounded fixtures and fault paths; no simulated real backup recovery claim |
| B13 | Media/object storage | S3 key/media metadata, streaming, ACL/errors, lifecycle | Testcontainers/compatible evidence or transparent NOT_VERIFIED |
| B14 | Reliability/ops | Timeouts, scheduler overlap, cancellation, connection leaks, failed startup, monitoring | Executed environmental tests with version/conditions; ops gaps tracked |
| B15 | Build/dependencies/CI | Maven, Java/Spring compatibility, SCA, unused dependencies, lockfiles/CI | Reproducible report with tool versions; no speculative upgrade requirement |
| B16 | Test quality | Assertions, coverage of risk, flaky fixtures, test source fidelity, negative paths | Specific test-case gaps and nondeterminism evidence |
| B17 | Performance/resources | Query N+1, request counts, paging bounds, large dataset behaviors, memory/latency | Measured or profiled relevant risks; otherwise ESTIMATE/NOT_VERIFIED |
| B18 | Secrets/network/config | Credentials, log/trace, debug endpoints, headers, deployment assumptions | Threat-bound review and negative tests; no unauthorized live probing |
| B19 | Shared RAG boundary only | Non-RAG dependencies, auth/config/build/shared DB and API bleed; suspended RAG protected | Boundary evidence only, **no RAG functional tests/changes** |
| B20 | Docs/source consistency | Actual changed source vs ADR, API, DBML, diagram and package tree | Reconciled; gaps made explicit with Owner decisions |

### D. Cross-layer and closeout (X)

| ID | Scope | Review question / observable verification | Completion condition |
|---|---|---|---|
| X01 | All phases | Every H/D/B path and mandatory case assigned a reviewer/evidence and disposition | No silent holes or evidence double-counting |
| X02 | Design ↔ code | Re-open provisional design verdicts on actual implementation/tests | Conflicts corrected or blocked for Owner decision |
| X03 | Existing LOW/INFO | Requalify applicable old backlogs and close only with proof | All substantiated in-scope actionable items dispositioned |
| X04 | Remediation | Exact authorizations, independent review per changed-source batch, counters | No unapproved changes, failed attempts retained |
| X05 | Final HEAD | Actual published SHA vs all source/test/docs evidence and no material drift | Consistent post-publication reconciliation receipt |
| X06 | Final qualification | No unadjudicated mandatory NOT_VERIFIED or unresolved actionable findings | `NON_RAG_BACKEND_AUDIT_READY` only if contract satisfied |

## Applicability, not dogma

- Tool selection is risk-driven. Do not require SpotBugs/PMD/JaCoCo on an incompatible stack merely to check a box. Document exact version, attempted command, limitation and substitute. No dependency or POM changes from audit-only scope.
- RAG is excluded **functionally**, but its shared security/build/API/persistence boundaries must be considered. Frontend is not independently audited; only consuming API needs and FR-13/boundary status are inspected.
- `historical provenance` means integrity and accuracy of statements *as statements made at the time*. It is not a request to retrospectively rewrite dated reviews.
- The eventual auditor must record **false positives, low-confidence ideas, and accepted design alternatives separately** from actionable defects.
