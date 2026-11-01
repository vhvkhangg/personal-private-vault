# Architecture and diagram gap inventory — 2026-11-01

**Disposition: `AUDIT_REPORTED / OWNER_REVIEW_REQUIRED`.** Companion to the [audit report](audit-report.md), under the [Owner authorization](owner-authorization.md). Baseline: `main / 51240841c91b0a2b94f665d7d66c432d36c063c9`, matching remote `main` at entry and final check. This inventory proposes changes; it creates, regenerates, renames and accepts **no diagram or application component**.

`EXISTING` means an artifact is present and useful for its stated scope; it is not runtime proof. `STALE` means a specific representation conflicts with later source/status. `MISSING` means the inspected canonical architecture directory has no dedicated view for an existing behavior. `PROPOSED` means an optional future view or product/layout choice, not an accepted requirement. Missing diagrams do not themselves create implementation blockers.

## Source-of-truth ownership

| Concern | Controlling source / owner | Derived representation and limits |
| --- | --- | --- |
| Permission, acceptance, current work | Owner decisions and [ACTIVE](../../handoffs/ACTIVE.md); P17/P18/S19-1 closeouts; S19-2 suspension/archive decision | Diagram badges must distinguish accepted, inactive/default-off, archival/not accepted and future. A generated graph cannot grant execution permission. |
| C4 structure | [`workspace.dsl`](../../../architecture/structurizr/workspace.dsl), under existing architecture governance | DOT/SVG/PNG and an eventual Archify view should derive from one reconciled model. The DSL is the intended editable C4 model, not proof its old descriptions are current. |
| Module ownership | Actual `package-info.java`, named interfaces and imports; [dependency matrix](../../../architecture/module-dependency-matrix.md), ADR-0001/0002/0015/0016/0017 and RAG amendments | draw.io is the canonical visual module source; DOT is also listed as a current export source. Both must agree with source/matrix. No directory rename grants a new module boundary. |
| Data model | Frozen [Schema v1 DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml), accepted additive P17/P18/P19 DBML, immutable executable Flyway V1–V24 | DBML records logical design; migrations define executable evolution. JPA mappings and a diagram are checked against them, not substituted for them. |
| Snapshot ingestion | ADR-0022/0023/0024, P18 D24/D26/D27, Java source authority/original-bytes storage/finalization and restricted Python staging | Depict the accepted Markdown snapshot pipeline and Java publication responsibility; indexing remains inactive. No live-original or OS-sandbox guarantee. |
| Evidence retrieval | ADR-0025, accepted S19-1 contract, V24 reader/inspection functions, retrieval/sender/recovery and query-worker source | Explicit reader lifetime, currentness and actual output completion; endpoint existence does not mean deployment/activation. |
| QA generation | S19-2 suspension and source archive manifest | Source exists but is not accepted. Ordinary Java startup lacks its guarded components. QA should be an archival overlay, not an active C4 container or supported product path. |
| Functional grouping | Existing functional-decomposition draw.io and frozen v1 capabilities | Useful product grouping; not an exhaustive current runtime or delivery-status catalog. |

The actual architecture subtree contains **33 tracked files**: 16 Markdown documents, seven editable diagram sources (four DOT, two draw.io, one DSL), and ten exports (five SVG plus five PNG). SVG/draw.io XML labels and relationships were inspected as text/structure. No visual-layout certification, rendering, export determinism, DSL validator or executable-architecture test is claimed.

## Existing sources and outputs

All paths in the following table are relative to `docs/architecture/` and were checked on disk.

| View / status | Editable source(s) | Outputs | Evidence and drift |
| --- | --- | --- | --- |
| Functional decomposition — EXISTING, frozen-v1 scope | `diagrams/source/functional-decomposition.drawio`; `diagrams/source/functional-decomposition.dot` | `diagrams/exported/functional-decomposition.svg`, `.png` | Covers access/settings, libraries, knowledge, collection, accounts/feeds, finance/journal and data operations. Useful baseline grouping. Does not contain accepted RAG ingestion/retrieval or distinguish archival QA. Add a dated optional overlay rather than recasting the frozen product baseline as a runtime inventory. |
| System context — EXISTING, baseline/planned integrations | `structurizr/workspace.dsl` SystemContext; `diagrams/source/system-context.dot` | `diagrams/exported/system-context.svg`, `.png` | Owner, PPV and external metadata/feed/storage relationships. External account metadata links do not imply automated media downloading. Some external interactions are product targets; current Feed code consumes supplied normalized batches, with no source-provider HTTP fetch adapter established by this audit. Mark planned versus implemented connections if updating. |
| C4 container — STALE current-runtime text | `structurizr/workspace.dsl` Containers; `diagrams/source/container.dot` | `diagrams/exported/container.svg`, `.png` | DSL:26,53–54 and DOT:7 describe non-root/synthetic/digest-only worker and one fixed loopback :8001 link. Accepted P18 produces real Markdown chunks/vectors; S19-1 uses a separate request-bound embedding process. No approved frontend implementation exists. The drawing omits immutable ORIGINAL_BYTES storage and reader/output lifetimes. See CSA26-003. |
| Module dependency — EXISTING edges, STALE delivery label | `diagrams/source/module-dependencies.drawio`; `diagrams/source/module-dependencies.dot` | `diagrams/exported/module-dependencies.svg`, `.png` | All 20 module nodes are present, including RAG and portability. RAG label remains “Phase 17 candidate.” The full source import scan found 41 edges, no cycle/matrix violation, and only vault/fiction/film as RAG's Java dependencies. Edge agreement does not make the candidate label current or prove dynamic enforcement. |
| Module dependency overview — EXISTING alias, same stale label | No separately named overview source; same module graph family | `diagrams/exported/module-dependency-overview.svg`, `.png` | These exports duplicate the corresponding module-dependencies files byte-for-byte in the inspected tree. Treat them as aliases, not independent models. Choose a preferred link only in a later authorized cleanup; do not delete aliases without a reference inventory. |
| Logical database — EXISTING source, dedicated current runtime ER view MISSING | `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`; `phase-17-alternative-b.dbml`; `phase-18-document-indexing.dbml`; `phase-19-markdown-readers.dbml` (outside this table's architecture prefix) | No dedicated checked-in combined RAG schema SVG/PNG found in the architecture export directory | V1 and its migration both name 69 tables; all 83 JPA entity mappings have migration table declarations. This is table-name consistency only. Preserve additive ownership and applied migration history rather than rewriting V1 to absorb later RAG tables. |

The [architecture index](../../../architecture/README.md):34–59 identifies DSL/draw.io canonical sources **and** DOT candidate export sources, but no checked-in deterministic diagram export script/CI job was found in the repository's `scripts/` inventory. Source-to-export reproducibility is therefore **NOT_VERIFIED**. A future update should specify which source generates each export; it need not add a large generation framework.

No dedicated architecture Mermaid sequence/state diagrams were found in `docs/architecture/` or the P17–P19 Markdown contract documents by fenced-diagram search. Mermaid text in old D17 HTML/text fixtures is parser evidence, not a maintained architecture model. No class/sequence/flow correctness is inferred from fixture diagrams.

## Source-verified runtime map to depict in a future update

The following is a textual edge inventory, **not a generated diagram**. Source abbreviations J/T/P are defined in the main report.

| Edge / boundary | Source evidence | Required interpretation |
| --- | --- | --- |
| Owner/browser → Java REST | `J/authentication/internal/infrastructure/security/SecurityConfiguration.java`; module-local controllers; root ApiResponse family | One permanent account, bearer-protected business routes, explicit public auth/health/docs exceptions. Frontend is prospective; browser token storage/CORS remain decisions. |
| Java application modules → PostgreSQL | Module-owned repositories/entities; V1–V24; `application.yml`; `application-rag-runtime.yml` | Java modular monolith with a separate restricted runtime/migrator profile for RAG. Do not depict the default development datasource as proof of production privilege configuration. |
| Java media adapter → S3-compatible store | `J/media/internal/infrastructure/storage/MediaStorageConfiguration.java` and `S3MediaStorageAdapter.java` | Existing optional integration, disabled by default. Binaries remain outside PostgreSQL; no selected production provider/deployment implied. |
| Java capture → immutable ORIGINAL_BYTES snapshots | `J/rag/internal/infrastructure/storage/RagOriginalBytesStore.java`; source/snapshot services; ADR-0022/0023 | Explicit approved-file capture and immutable revision/hash/length, Java-owned authority. Snapshot storage is a data artifact boundary, not a new microservice. Live originals remain disabled/deferred. |
| Issued Java indexing job → Python producer → restricted staging | `J/rag/internal/application/RagWorkerSupervisor.java`; `NativeWorkerProcess.java`; `P/worker.py`, `document_source.py`, `db.py`; V22/V23 | Job-scoped local process; one-use grant before input; prepared approved content; parser/chunker/CPU embedding; restricted SQL staging/seal. Java independently validates and publishes. Capability accepted under P18 but operational indexing remains inactive. |
| Java finalization → PostgreSQL published generation | `J/rag/internal/application/RagDocumentFinalizationGate.java`; V22 result-authority SQL; generation/chunk mappings | Correct actor is Java. Python does not publish, mutate business records, choose consent, or acquire broad business-table access. |
| Retrieval reader → pinned CPU query process → Java ranking → HTTP output | `J/rag/internal/application/retrieval/RagRetrievalService.java`, `RagRetrievalAuthority.java`, `RagQueryEmbeddingSupervisor.java`; `P/query_embedding.py`; V24 | Accepted retrieval/citations, default retrieval enablement false. Separate source-use lifetime and exact contributors; no generated answer or QA model implied. Reader exclusion extends through output/container/native quiescence. |
| Source inspection/incident → reader stop or deferred denial → guarded release | V24 `retrieval_inspection_record`, `retrieval_reader_begin_emission`, `retrieval_reader_finish`; ADR-0025; snapshot inspections/recovery | Pre-emission incidents prevent evidence. Already-started emission follows the approved finish rule while fresh admissions are denied. Unknown stop/commit retains exclusion; not TTL/PID-only release. |
| Portability → allowlisted DB snapshot → private ZIP download | `J/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java`; ADR-0017 | One read-only REPEATABLE READ snapshot, auth/security/system exclusions, Markdown/media manifests; no full archive reimport or operational backup guarantee. |
| Archived QA overlay | Five conditional Java QA beans; `P/grounded_qa.py`, `qa_generation.py`; optional QA locks/extra; source archive manifest | SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY. Default application routes/workers absent. Test discovery remains a separate gap, CSA26-001. An interactive UI must not present a “run” action for it. |

ADR-0023 supersedes earlier external-snapshot OS-confinement claims: same-Windows-user Python/application-level trust, finite capture/integrity/process controls. A label such as “non-root worker” is not an accurate Windows privilege guarantee. No native live-file R7, firewall, LPAC/Restricted Token, CUDA, whole-RAG readiness or deployment guarantee should be added from historical topology alone.

## Proposed diagram work, in useful order

These are selectable documentation proposals. The first three would resolve most review confusion; the rest should be added only when a concrete reviewer/frontend need warrants them.

| Priority / status | View and smallest useful content | Canonical contracts/source to verify | Likely Archify review value |
| --- | --- | --- | --- |
| 1 — STALE → proposed update | C4 container: Java API, PostgreSQL, optional S3, immutable snapshot artifact, job-scoped producer and separate query worker. Frontend/future integrations visually distinct; archived QA separate. | Runtime edge inventory above; ADR-0017/0020/0022/0023/0025; default properties and source archive. | HIGH: toggle accepted/inactive/archive/future layers and inspect edge provenance. No need to add more deployment units. |
| 2 — MISSING | Retrieval/incident/output sequence: C_A admission/reservation, compute, C_I before/after C_E, nonblocking output, positive native/transport/container stop and C_D+C_RL. Include unknown-commit/stop branch. | V24 and current retrieval/sender/recovery source, accepted S19-1 contract. | HIGH: follow one request and alternate incident timing without obscuring lifetime. Link directly to source/accepted tests. |
| 3 — MISSING | Snapshot/data-flow view: approved selection/dependencies → ORIGINAL_BYTES/revision → Java issued prepared input → Python parse/chunk/vector staging → Java verification/physical publication → eligibility/retrieval. Label trust/data ownership. | ADR-0022/0023, P18 final contracts, V20–V23, source/storage/finalizer/worker. | HIGH: show which process may read bytes or write which table/function; select a root/true child and see contributing provenance. |
| 4 — EXISTING/STALE | Module map: retain allowed 20-module graph, update candidate status; optionally expose named interfaces on demand and highlight narrow portability JDBC exception. | All module descriptors/imports plus matrix; maintain 41 observed edges separately from all permitted future edges. | MEDIUM/HIGH: filter a selected module and distinguish allowed from observed dependencies. Static SVG remains enough for an unfiltered overview. |
| 5 — MISSING | RAG data/state view: source/revision/consent, generation/root/chunk, job/grant/stage/seal, retrieval reader/inspection. Separate Vault-owned source metadata from RAG-owned tables. | Additive DBML + V14–V24 + entity mappings; do not merge into frozen V1. | MEDIUM: expand a capability and trace identity/FK relationships. Avoid a single unreadable 80-table canvas. |
| 6 — MISSING | Auth/frontend boundary sequence: bootstrap/login/rotate/revoke, bearer validation and PIN verification; annotate private-mode UI locking and outstanding browser transport decision. | Security/source APIs, ADR-0008/0016, frontend Owner design decision when authorized. | MEDIUM: useful during frontend preparation, especially token lifetime/error-path decisions. Not a new auth implementation mandate. |
| 7 — MISSING | Media uncertainty sequence: bounded spool/upload → metadata commit → confirmed rollback compensation versus uncertain-commit positive reconciliation/retain. Separate stream close after disconnect. | ImageUploadService/ImageController/S3 adapter and existing Phase 14 acceptance. | MEDIUM: clarify user-visible retry/error behavior and accepted residual orphan risk. |
| 8 — MISSING | Portability/backup boundary: read-only export snapshot → private ZIP, auth exclusion, media manifest versus separate binary backup. | ADR-0017 and snapshot adapter. | LOW/MEDIUM: a static sequence/data-flow view is probably sufficient. No full restore contract added. |
| 9 — PROPOSED only | Future frontend and Ideaverse/Drive context overlay after product decisions; reuse existing snapshot admission rather than inventing live synchronization. | Future explicit Owner requirements/ADR/handoff, not the current archived QA design. | Conditional: useful once real user journeys/source authority exist. Do not build placeholders now. |

Suggested interactivity is an **affordance**, not a tool selection or acceptance claim. Archify was not invoked, installed or supplied repository/private data. An eventual implementation should keep a simple static export, one canonical editable source per view, visible source/commit/date/status links and inspectable edges. Interactive coherence alone cannot prove correctness or justify accepting an inferred dependency.

## Directory naming and prospective placement

### Existing `backend/` and `rag-engine/`: KEEP

`backend/` is the Java API and business-authority application, including the RAG orchestration module; `rag-engine/` is the Python local compute/producer/query runtime. Both are server-side code, but their names convey distinct build/runtime ownership. A sibling Python worker does not make the business application a microservice fleet or violate package-by-capability inside Java.

There is no demonstrated defect that requires renaming either directory. The current monorepo layout is proportionate:

| Path | Current responsibility / proposed disposition |
| --- | --- |
| `backend/` | Java Maven application, module-owned REST/domain/persistence and orchestration — KEEP. |
| `rag-engine/` | Python lock/package, worker/embedding/parser code and archived QA sources — KEEP, with accurate accepted/archive setup documentation. |
| `docs/` | Canonical architecture/decisions/delivery evidence indices — KEEP. |
| `scripts/`, `.agents/`, `.codex/` | Bounded execution, workflow/skills and client-specific controls — KEEP. Not runtime business modules. |
| `frontend/` | PROPOSED later sibling for an authorized browser client; currently absent, no scaffold created. |

Alternatives such as `services/api/` + `services/rag/`, `apps/backend/` + `workers/rag/`, or renaming `backend/` to `vault-api/` are cosmetic proposals today. They would affect Maven commands, Python model/runtime resolution, test fixtures, native process working directories, hooks, docs and many historical evidence references. Frozen/historical path references and published hashes should not be rewritten merely to regularize labels. Approve a concrete migration only if a real packaging/deployment/team ownership problem emerges.

### Ideaverse: future input corpus, not a new business module by name

The Owner has not authorized integration, source synchronization, GitHub acquisition, live-original reads or another module. Ideaverse currently belongs in a **product-design proposal**, not a runtime box labeled implemented.

If separately authorized later, the smallest starting point is the existing Java-controlled, read-only **external Markdown snapshot** flow: select an approved root/file set, explicitly consent whole files and dependencies, capture immutable revisions, then use only independently authorized consumers. Names/aliases may be source metadata; they do not grant content authority. An “Ideaverse module” is unnecessary unless a distinct capability/domain with its own invariant is actually approved.

Raw owner corpus content should remain in an explicitly chosen local data/source area, outside public tracked implementation and fixtures. Its path, retention and import/sync behavior remain undecided. A literal future `data/ideaverse/` directory could be an ignored local storage choice, but this audit neither creates it nor prescribes a trusted root. Do not repurpose the private vault repository as a content mirror. Keep connector/runtime infrastructure within its real owner when and only when a handoff authorizes it; do not add a generic integration framework now.

Google Drive is likewise only a future product-design candidate. OAuth, remote content acquisition, quotas and source authority require a later explicit decision. Automated social-media downloading, fiction copying/backup and Hermes/OpenClaw are **dropped**, not boxes awaiting implementation or gaps in this inventory.

## Documentation synchronization and selected next gate

`UPDATED`: this new inventory and the audit report. `ALREADY_CONSISTENT`: module-edge ownership, canonical DBML/migration ownership and source default-off controls within their inspected static scope. `DEFERRED_OUT_OF_SCOPE`: stale C4/candidate labels, current navigation and any diagram export/source reconciliation. `NOT_VERIFIED`: render/layout/export reproducibility, live topology and any proposed Archify interaction. Frozen diagrams, current canonical docs, historical failures, ACTIVE and all counters remain unchanged.

The only MEDIUM issue in the main report is test discovery reaching archived QA, CSA26-001. Diagram additions and directory naming do not supersede that issue, authorize frontend implementation or create a new review budget. Owner selection of a small documentation update can follow separately; no automatic handoff or implementation is created by either report.

Next step: Owner | review docs/implementation/maintenance/2026-11-01-current-system-readonly-audit/{audit-report.md,architecture-diagram-gap-inventory.md} and authorize only selected remediation | audit-only; no implementation | no counters reset
