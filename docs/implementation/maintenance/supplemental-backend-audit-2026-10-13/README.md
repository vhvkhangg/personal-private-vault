# Supplemental backend audit maintenance — approved scope and retained contract

Status: **COMPLETE — FROZEN (owner commit/push `d195b6260274`, 2026-10-13)**.
See [closeout](closeout.md), [archived handoff](handoff.md),
[Codex final acceptance](reviews/2026-10-13-sba-001-007-final-codex-acceptance.md) and
[test evidence](test-evidence.md). All SBA-001–007 and FR-SBA-1–4 are **CLOSED**.
The planning/acceptance requirements below are retained as the **historical authorized scope**,
not a pending handoff.

Owner approval: **2026-10-13** — explicitly authorize remediation of **all seven** verified defects
`SBA-001` through `SBA-007` as **one bounded maintenance slice**, before starting Phase 17 RAG
implementation. See [owner approval](owner-approval.md). This is a scope authorization, **not** proof
that any defect has been repaired and **not** an active implementation handoff.

This recommendation follows the [2026-10-13 supplemental Phases 0–15 audit](../../phase-15/reviews/2026-10-13-supplemental-backend-audit.md):
**six Medium defects and one Low defect**, reproduced independently of the passing **1,021-test** full suite.
It was originally a maintenance proposal and is now the **owner-approved canonical scope** for one
future Codex handoff. It is not an implementation handoff and does not reopen historical Phase 15 findings.
No production code, tests, migrations, POM, accepted ADR or active handoff was changed.

## Priority and Phase 17 decision

| Priority | Finding | Smallest proposed scope | Phase 17 consequence |
| --- | --- | --- | --- |
| 1 | **SBA-001 / Medium — Vault stale trash/lifecycle gates** | One existing-row Vault guard plus authoritative refresh for lifecycle and protected metadata mutations; align with accepted source-fence design. | **Gate runtime implementation acceptance.** Preparation remains accepted. Can be a separate maintenance slice or an explicitly authorized part of the Phase 17 owning guard correction. |
| 2 | **SBA-002 / Medium — Settings lost partial updates** | Lock/refresh singleton before merging omitted fields; preserve initialization and defaults. | No current foundation blocker; repair current preference consistency independently. |
| 2 | **SBA-005 / Medium — Superseded current PIN accepted** | Atomic authoritative current-PIN check and change on existing owner row. | No current RAG blocker; repair credential consistency independently. |
| 3 | **SBA-004 / Medium — Markdown metadata loss** | Complete-line frontmatter delimiter recognition with exact raw-text preservation. | Gate any future reuse of this importer/structured metadata; no current foundation block. Later external-file ingestion has its own fidelity gate. |
| 3 | **SBA-006 / Medium — JSON trailing content ignored** | Require one complete JSON document; preserve safe errors and decimal/field rules. | Gate trusted reuse of affected parsed imports; no current foundation block. |
| 3 | **SBA-003 / Medium — Disabled Feed ingestion succeeds** | Recheck enabled immediately after existing locked refresh; atomically roll back staged batch on denial. | Fix before later Feed source integration. Feed is excluded from the initial RAG corpus. |
| 4 | **SBA-007 / Low — Person exponent width overflow** | Overflow-safe numeric digit check; unchanged numeric(6,2) schema and business bounds. | No current foundation block. |

The gate above is specific: source authority cannot be accepted at runtime while participating Vault lifecycle
operations make decisions from stale managed state. It does not require every unrelated defect to be fixed before
Phase 17 preparation or handoff work. It also does not reopen accepted P17-1/2/3 design contracts.

## Approved maintenance scope and execution order

The owner selects **one bounded maintenance slice covering SBA-001–007**, as opposed to merging any of these
repairs into the Phase 17 RAG handoff. Codex must create exactly one separate active maintenance handoff for
this approved scope, then Antigravity implements/tests it and Codex performs final review before the owner commits.
`SBA-001` is the first implementation priority and must be proven closed before Phase 17 runtime acceptance;
all six other findings must also be resolved to close **this** owner-approved maintenance slice.

**Authority boundary:** this approval grants the smallest corrections to existing behavior documented in the
[findings report](../../phase-15/reviews/2026-10-13-supplemental-backend-audit.md), not a repository-wide
refactor or a new RAG feature. It temporarily permits only finding-driven changes in the frozen owning modules
listed below, plus focused tests and directly affected documentation. The Phase 15 audit status, historic
`BA15-*` disposition and ADR-0018 are **not** reopened. `docs/implementation/handoffs/ACTIVE.md` remains
`NO_ACTIVE_HANDOFF` until Codex explicitly creates the separate maintenance handoff.

### Permitted target boundaries

| Finding | Owning code boundary | Bounded required correction |
| --- | --- | --- |
| `SBA-001` | `vault/internal/application/entry/VaultEntryService.java`; `vault/internal/application/metadata/VaultMetadataService.java`; `vault/internal/infrastructure/persistence/VaultEntryRepository.java`; minimum Vault-owned helper if necessary | Same existing physical `vault_entries` row guard; authoritative fresh state before trash/restore and metadata mutation decisions; parent-before-child lock order; transaction-through-COMMIT semantics. Align with Phase 17 source guard but do **not** implement the Phase 17 RAG guard here. |
| `SBA-002` | `settings/internal/application/configuration/AppSettingsService.java`; `settings/internal/infrastructure/persistence/AppSettingsRepository.java`; `settings/internal/domain/AppSettings.java` only if necessary | Atomic authoritative singleton read–merge–write with missing/default field preservation; maintain initialization path. |
| `SBA-003` | `feed/internal/application/FeedItemService.java`; existing Feed-owned persistence collaborator only if essential | Revalidate `enabled` **after** the existing final lock/refresh, reject disabled ingestion atomically without persisted batch or timestamp changes. |
| `SBA-004` | `importdata/internal/parsing/MarkdownImportParser.java`; local parser helper only if essential | Recognize whole-line YAML delimiters correctly, including empty blocks, LF/CRLF and EOF; preserve raw Markdown and unknown metadata. |
| `SBA-005` | `authentication/internal/application/privatepin/PrivatePinService.java`; `authentication/internal/infrastructure/persistence/AppUserRepository.java`; `authentication/internal/domain/AppUser.java` only if necessary | Atomically lock/refresh the one existing user before current-PIN verification and update; preserve password/login/refresh/PIN hash contracts. |
| `SBA-006` | `importdata/internal/parsing/JsonImportParser.java`; local parser configuration only if essential | Accept only one complete JSON document; reject all trailing tokens/garbage without leaking payloads. |
| `SBA-007` | `people/internal/application/PersonService.java`; existing person validation helper only if already applicable | Use overflow-safe `BigDecimal` digit-width validation; preserve numeric(6,2), existing business bounds and response behavior. |

Paths in this table are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.
**Testing permission:** add/modify narrowly targeted `backend/src/test/java/` files under those seven owning
modules, including real PostgreSQL Testcontainers concurrency tests. Changes to a directly affected
`AGENTS.md` or canonical implementation contract must be justified in the handoff/review; the default is no
other repository changes. If implementation proves additional production paths essential, Codex must identify
and justify the minimal new paths **within those owning modules** before Antigravity edits them. Additive module
edges, schema/POM changes, cross-owner general-purpose helpers, ADR revisions or public API/semantics changes
are **not preapproved**; stop for a new owner decision instead.

### Mandatory handoff and regression requirements

1. Trace every one of the seven `SBA-*` IDs to a production correction, at least one failing-before/fixed-after
   regression (when reproducible in automated tests), final database or HTTP assertion as appropriate, and a
   Codex disposition. Neither green preexisting tests nor the Codex disposable audit probes substitute for
   checked-in regression tests.
2. For `SBA-001`, prove guard freshness after a preloaded first-level JPA cache; trash/restore in both
   serial orders; **all six** protected metadata operations; denial/no-write/idempotent outcomes; actual
   lock-contending transactions with deterministic barriers, no sleep-based timing assertions; documented
   shared physical guard and lock ordering compatible with the later Phase 17 RAG source authority.
3. For `SBA-002` and `SBA-005`, exercise real two-transaction interleavings in **both commit orders** plus stale
   managed-context fixtures; assert final persisted settings/PIN and unchanged unrelated fields. For `SBA-003`,
   test committed disable before authoritative refresh, actual contention, staged-batch rollback and unmodified
   fetch/schedule timestamps.
4. For `SBA-004`, cover the two exact audit counterexamples, frontmatter delimiter variants, nested/unknown
   fields and preserved raw text, with prepare/execute/persisted Note assertions. For `SBA-006`, cover multiple
   JSON roots, arbitrary trailing data, valid trailing whitespace and complete no-write error paths. For
   `SBA-007`, cover extreme exponents, height/weight create/update boundaries, HTTP/domain rejection and no
   database write.
5. Run focused tests, then a **clean full backend Maven verification** with PostgreSQL and MinIO where required;
   verify Spring Modulith boundaries, affected REST/OpenAPI contracts, authentication/authorization, import
   rollback and module isolation. Report test totals/failures/skips and reproduce exact commands/results.
6. Review for SOLID/cohesion, minimal necessary design patterns, DRY/KISS/YAGNI, structured logging (without
   sensitive values), transaction/lock consistency, regression risk and stale documentation. Do not expand
   this slice to stylistic or static-warning cleanup without a verified defect and owner approval.
7. Codex final review must explicitly resolve `SBA-001` through `SBA-007` with cited tests and implementation
   evidence; unresolved findings return `CHANGES_REQUESTED`. Only `READY FOR OWNER COMMIT` permits the owner
   to commit/push maintenance. Phase 17 RAG implementation remains paused until this maintenance is closed.

## Acceptance evidence for an eventual authorized slice

| Finding | Required regression proof | Material regression risk |
| --- | --- | --- |
| SBA-001 | PostgreSQL cached-entity counterexamples; actual competing lock barriers in both orders; trash/restore and all six metadata mutation gates; no-write denial and idempotency; Phase 17 final-check-to-commit source-fence proof if combined. | New parent/child lock order can cause deadlocks or change contention. Preserve existing rating/tag/set semantics. |
| SBA-002 | Concurrent disjoint partial updates preserve both changes; stale settings context; initialization/defaults; backup enable/interval and auto-lock preservation; final JDBC assertions. | Singleton initialization and null/default handling must remain compatible. |
| SBA-005 | Competing PIN changes with exactly one winner, both winner orders and cached-user case; wrong-current/anonymous denial; final PIN and unchanged password/email/username. | Lock duration and singleton/bootstrap/auth regressions; preserve hash compatibility. |
| SBA-004 | Prefix-looking YAML keys, adjacent empty block, LF/CRLF, delimiter EOF and body thematic breaks; exact Markdown; unknown/nested metadata and numeric precision; prepare/execute/persisted Note assertions. | Delimiter correction must not normalize text or reinterpret ordinary body content. |
| SBA-006 | Trailing second roots and garbage rejected, ordinary whitespace accepted; malformed first root, empty array and decimal fidelity controls; no target writes on rejection. | Tighten invalid document acceptance without weakening valid field/value rules. |
| SBA-003 | Committed disable before stale ingestion, disable contention, atomic no-item/no-timestamp changes on denial; permitted serial orders; existing scheduling/dual-key/unique-conflict tests. | An earlier broad lock could alter ingestion contention. Start with the existing final locked gate. |
| SBA-007 | Extreme exponents rejected before writes; height/weight create/update, ordinary width/scale boundaries and current HTTP rejection; no new Vault/Person rows. | Narrow arithmetic change; preserve legitimate scientific notation within physical limits. |

Run focused tests for the affected behavior, then one clean full Maven verification with PostgreSQL/MinIO and
Modulith/HTTP regressions. Preserve exact commands/results and final persisted/no-write assertions. Static and
coverage evidence should guide changed-risk review; do not expand the slice to clear all analyzer warnings.
Codex's disposable audit probes are evidence of the defect, not a substitute for implementation-owned regression tests.

## Decisions and explicit non-goals

- **Frozen baseline impact:** owner approval for the seven narrowly specified repairs was supplied on
  2026-10-13 in [owner approval](owner-approval.md). These corrections preserve Schema v1, module/public
  contract ownership, dependency directions, existing successful semantics and accepted ADRs.
- **ADR changes:** none needed for these owner-approved narrow corrections. A version-column/schema alternative, new
  dependency edge, shared technical module/package, or changed public behavior requires a separate justified ADR
  decision and synchronized canonical artifacts before implementation.
- **No automatic structural cleanup:** preserve ADR-0016 root HTTP types and justified owner/provider interfaces.
  CPD clones and unused imports are observations; do not turn this into a common-module, mapper or service rewrite.
- **No expansion:** no frontend, deployment, RAG production implementation, OCR/model selection, ingestion cap,
  arbitrary provider upgrade, schema widening, API pagination expansion or multi-user feature is authorized here.
- **History:** preserve all BA15/FR15 dispositions and reports; use immutable SBA IDs for new work. Leave
  `docs/implementation/phase-15/audit-status.md` and `ACTIVE.md` unchanged until a separate authorized workflow.

## Follow-up measurements outside the defect scope

Before claiming RAG-scale readiness, collect representative search/import/export memory, disk, latency and
failure-recovery evidence, and a reliable current dependency vulnerability/license/SBOM assessment. These were
**NOT_VERIFIED** by the supplemental audit. Agree workload-specific acceptance gates rather than inventing a
performance guarantee or a schema/index change.

An owner-approved future search-maintainability slice may first add a behavior matrix across the existing owner
snippet adapters. Consolidate only after a concrete drift/maintenance problem and an ADR-safe ownership choice.
Portability's full retained-data export and Vault timestamps must not substitute for Phase 17 eligible-source
allowlists and authoritative content revisions.

**Closeout transition:** maintenance implementation is final-reviewed, committed/pushed and archived;
`ACTIVE.md` has been reset in the [documentation closeout](closeout.md). This scope no longer
authorizes a new maintenance handoff. The next handoff is the **separate Phase 17 RAG foundation** handoff,
after the owner commits/pushes closeout documentation.
