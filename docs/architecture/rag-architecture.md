# Local RAG Architecture — Proposed Phase 17 contract (not implemented)

**Status:** Preparation design for Codex review; no implementation, migration, extra module, or frozen baseline change has occurred. Governing proposal: [ADR-0019](../adr/0019-local-rag-before-frontend.md). Existing [Global Search](search-architecture.md) remains separate and implemented.

## 1. Scope and topology

```
Owner-selected non-private files  ---->  Spring RAG owner adapter  <---- Public module projections
   (session or persistent scope)                |                    (approved data only)
                                               v
                                  Python FastAPI, loopback-only
                                  parse/OCR/normalize/chunk
                                  embed/index/retrieve/rerank
                                  local LLM/evaluate/recommend
                                         |              |
                                         v              v
                                 PostgreSQL RAG data  local model runtime
                                  (pgvector candidate) (model cache)
```

The Spring modular monolith remains canonical for data, authentication and domain semantics. RAG is **not** permitted to query arbitrary business tables, copy finance/auth secrets, or depend on internal Java packages. For any module lacking a safe read-only export/public contract, mark it **not indexable yet** until the owning module gains an explicitly reviewed projection. Do not bypass the boundary with a JDBC snapshot (the portability-specific ADR-0017 exception is not reusable by RAG). Python must not accept an arbitrary URL or path from a remote caller. The internal HTTP interface is loopback-bound and service-authenticated independently of user JWTs. Browser/remote clients eventually talk to Spring only.

Architecture design principle: the modular monolith does not become a distributed application simply to deploy a Python ML runtime. All local components can run on the same machine; durable work state and process boundaries are explicit.

## 2. Eligibility and source catalog

**Normative preparation contracts:** [P17-2 source/field matrix](../implementation/phase-17/source-eligibility-and-allowlists.md), [P17-3 revocation fences](../implementation/phase-17/revocation-and-derived-data.md), and [P17-1 module/storage/worker-role delta](../implementation/phase-17/integration-ownership-and-permissions.md). If a generic paragraph here sounds more permissive than these specific allowlists/fences, use the narrower fail-closed contract.

An **explicit SourceSelection** belongs to each ingest/query/recommendation job:
- `scope`: an allowlisted collection of approved module/entity types, specific document IDs, or explicit user-selected external files/directories; defaults to the owner's approved non-private corpus, never the whole drive;
- `ingestion_mode`: persistent indexing or ad-hoc ephemeral session (separate namespace, TTL/deletion after session);
- `source_kind`, `source_id`, `entity_type`, `owner_module`, version/checksum, timestamps and `access_state`;
- `eligible`: public/non-private, live (not trashed/soft-deleted), within source scope and not a secret-bearing/security-state field.

**Fail closed:** unknown private classification, unavailable source, unknown module-field privacy, unexpected symlink/path or stale policy -> refuse ingestion/answer and record sanitized error. `isNsfw=true` in Fiction/Film is excluded. No private or NSFW source can enter embeddings, full-text index, candidate pools, metrics contents or local judge prompts. Finance, Personal, Journal, Account, authentication/security state and **all other unreviewed modules are excluded in the first integration slice**; only approved Fiction/Film fields and individually approved external documents are initial candidates. Future modules require owner/Codex approval of type-specific classification and field-level projections, never broad DTO export. Structured finance calculations must call typed read-only domain operations; do not rely on chunk approximations. Private-state and deletion filtering is verified at ingestion **and** query/candidate/answer time.

Explicitly prohibit indexing credentials, PIN/password hashes, JWT/refresh tokens, API keys, authentication/session metadata and sensitive operational logs. No embeddings or derivative caches of excluded content. When classification changes, synchronously deny new access and revoke grants; purge **all** dependent content-bearing derivatives (including answer/history/judge/cache/session artifacts and transcluded copies), not only chunks/embeddings. Apply per-source epoch/source-revision fencing to in-flight jobs and publication (P17-3); do not wait for a scheduled full refresh. Same for trash, deletion and source revocation. Enforce authorization/scope before score calculation to prevent leaks through aggregate counts or ranking.

Sources from the filesystem require **per-file, per-version owner classification and field approval** even when a directory was selected, plus canonical path verification; disallow traversal, symlink escape, untrusted archives, network paths and external file execution. The application must never write back to selected files. Persistent directory sync must use watched allowlisted directories plus periodic reconciliation; watcher events are hints, not authoritative state. **Ideaverse is not yet in the Vault**; no Ideaverse-specific watcher/module/ingestion obligation is active. Future owner authorization may enable one-way Obsidian-to-Vault synchronization with source directories strictly read-only.

## 3. Ingestion pipeline and idempotency

`ACCEPTED -> VALIDATING -> EXTRACTING -> NORMALIZING -> CHUNKING -> EMBEDDING -> STAGING -> VERIFYING -> PUBLISHED`; terminal `FAILED`, `CANCELLED`, `SUPERSEDED`, or `QUARANTINED` with typed error. Statuses are design proposals, not DB enum names.

- Deduplicate work by `(source_kind, source_id, content_hash, parser_version, normalization_version, chunk_strategy_version, embedding_model_id, privacy_policy_version)`; handle retried/duplicate events safely.
- Preserve provenance through parse/chunk/embedding, with stable IDs anchored to source content and structure; keep source snapshots bounded to needed data and never copy private fields.
- Publish an index generation atomically only after checks pass; failed attempts must not corrupt last known-good eligible version. An eligibility revocation is **immediate deny**, even if cleanup is retrying. Publication and revocation must serialize on authoritative RAG source policy; worker staging cannot activate itself. Every transcluded child appears in the generation dependency manifest; a stale epoch, source hash, field policy, lease or child revocation prevents late publish.
- Emit update/delete/restore triggers only after owning domain transactions commit; event loss is addressed through **periodic reconciliation**. Evaluate a transactional outbox only if measured failure modes require stronger guarantees; do not assert that an existing outbox exists.
- A schema/index/model upgrade uses a versioned side-by-side generation, validated before swap; planned rollback retains only allowed non-private data and removes orphan embeddings.
- Provide scoped/manual reindex, cancellation, retry with bounded backoff, rate limiting and progress reports; no background constant high-CPU polling.

### Parser and chunking policy

| Source | Parser plan | Chunk boundary and provenance |
| --- | --- | --- |
| Markdown / Obsidian `.md` | Markdown AST + Obsidian extension adapter | headings, block IDs, YAML properties, wikilinks, callouts and spans |
| Plain `.txt` | Encoding detection and guarded text parser | paragraphs/sections; original line and byte/character offsets |
| DOCX | Structural document parser | sections, paragraphs, table/row groups; relationship and table locators |
| Text PDF | Layout/text extraction first | page number, bounding rectangles, table region |
| Scanned PDF | OCR + table structure/reading-order recovery | page, box, cell grid, confidence; warn on failures |
| XLSX | Spreadsheet parser (e.g. openpyxl) | workbook, sheet, table ID, header, row group, cell range, formulas vs values |
| XLS | Explicit compatible adapter/conversion | sheet, row/column; decline when conversion fidelity is insufficient |
| Module record | Owning-module read-only RAG projection | `module/entity/id/field`, typed metadata and source revision |

Obsidian adapter must preserve `[[wikilinks]]`, aliases, anchors `#heading`, `#^block-id`, `![[embeds]]`, YAML frontmatter, nested tags, `> [!type]` callouts, and comments `%%...%%` as appropriately scoped metadata. Resolve embeds **only within the approved root and source selection**; cycle detection, depth/size limits and fallback unresolved-link handling are mandatory. Do not execute Dataview, plugin scripts, JavaScript, macros, external URL fetches or formula evaluation. Archive/import parsing must reject zip-slip and decompression bombs. Ideaverse-specific graph rules remain deferred.

Chunking: structural first; token budgets/overlap configurable per **parser strategy**, not one forced global size. Split large tables with repeated header metadata and overlap only where it preserves meaning. Store configuration versions and benchmark lexical/semantic recall for several candidate chunk sizes before freezing defaults. Preserve characters/word order for Vietnamese with diacritics, Hán Việt, English US/UK, and Han scripts (Simplified/Traditional) without assuming they share the same tokenizer.

OCR: benchmark **Docling-assisted layout/table extraction + Tesseract** with installed `vie`, `eng`, `chi_sim`, `chi_tra` packs against a **PaddleOCR/table pipeline** selected for verified Vietnamese/Chinese language coverage on the specific model version. A model advertised as multilingual is not automatically good at Vietnamese tables. Compare text error rate and table structure fidelity on native samples; record GPU/CPU/RAM use and offline prerequisites. Prefer text-layer extraction when available; keep OCR bounding boxes and source coordinates.

## 4. Query routes and answer integrity

`authenticate -> source selection -> scope/eligibility check -> route -> retrieve/typed read -> rerank -> assemble evidence -> local generation -> verify citations -> return -> evaluate`.

Three bounded read-only routes:
1. **Grounded Q&A:** lexical (Postgres text/trigram) + vector retrieval (pgvector candidate), RRF/weighted merge tuned on Vietnamese/Han/English benchmark, multilingual reranker, dedup, prompt-injection-resistant context builder. One bounded conditional retry if evidence fails checks. The response abstains when sources do not substantiate a claim. Every substantive answer claim should have supported source locators. The model must not turn source text into tool instructions.
2. **Catalog recommendations:** deterministic candidate set restricted to existing, currently eligible Vault items and optional status/domain filters. **All reading/viewing statuses included by default**, including unwatched, in progress, completed, paused and abandoned; source item optionally excluded. Combine structured attributes (title, genre, tags, review, status, content warnings, ratings) and semantic similarity; rerank, validate returned IDs against allowed candidate set, then explain using field-level evidence. No fabricated item or outside suggestion. Generalize candidate adapters to further domains only where meaningful, starting Fiction/Film; feedback never mutates original records automatically.
3. **Structured factual lookups:** predefined allowlisted read-only module operations with typed inputs and validated filters; no raw SQL, Python JDBC fan-out or uncontrolled LLM tool calls. Prefer exact source-of-truth data over generated approximation for counts/money.

**No full autonomous Agentic RAG** in the initial release. An explicit router with one or two bounded retrieval refinements is easier to benchmark and operate locally. Escalate to agentic planning only if fixed regression cases demonstrate better quality within acceptable latency and privilege boundaries.

## 5. Provenance, highlights and citation contract

Every evidence span must include stable `source_kind/source_id/source_version`, `locator_type`, `locator`, `text_span` or `region`, and `extraction_confidence` when OCR is used; return only owner-authorized source text. Locators:
- text: exact line/character/byte offsets and heading/block IDs when possible;
- PDF: page + bounding polygon/box in original page coordinates; clearly flag OCR-only positions;
- spreadsheet: workbook, sheet, A1 cell range, optionally table/header mapping;
- module record: domain/entity ID + approved field name/revision.

Return `cited_evidence[]` and `highlight_spans[]` separately from display text, never only a generated quote substring. A client may render search-term highlight and evidence highlight; **persistent annotation editing is out of scope**. If stable exact source mapping is impossible, return a `locator_quality` warning and do not pretend a precise highlight exists. Source version changes invalidate stale offsets until recalculated.

## 6. Service and storage contracts (proposal; no endpoints exist yet)

**Bounded initial delta:** new Java `rag` leaf (`rag -> vault, fiction, film` via public interfaces), Java-owned eligibility/jobs/grants/publish, Python-owned disposable computation/staging, Java/Flyway-only migration authority and a **distinct least-privilege `rag_worker` PostgreSQL credential**. The initial source/field allowlist is Fiction/Film plus individually approved external files. See [P17-1](../implementation/phase-17/integration-ownership-and-permissions.md). These are proposed future changes to be owner-accepted and synchronized with the frozen matrix/tree/diagrams in an authorized integration slice, **not** existing Java packages/DB roles.

- Spring public endpoints later: owner JWT-protected orchestration for source selection, ingest/reindex jobs, query/recommend, run details, cancellation, and read-only source resolution; conform to existing versioned `ApiResponse`, error and OpenAPI contracts. The eventual frontend is out of scope.
- Spring->Python: loopback-only service-authenticated private HTTP protocol with typed schema/version, `request_id`, `idempotency_key`, `scope_token` (opaque short-lived bound selection, not owner JWT), `source_version` and strict deadlines; streamed contents only from authorized Spring adapter or explicit owner-selected locally accessible file adapter. Python must not bypass Spring policy to fetch module data or arbitrary files. Every worker request and staged result must carry a source-grant/epoch/lease/dependency fence; the Java gateway alone approves final answer/history, publication and source resolution.
- Prefer **opaque source grants** and scoped canonical refs over user-controlled arbitrary filesystem paths. Require binding to source selection in every worker and every retrieval call, including local background jobs.
- RAG tables proposed for **future new namespace/schema**: `rag_sources`, `rag_source_versions`, `rag_chunks`, `rag_embeddings` (or co-located vector), `rag_index_generations`, `rag_jobs`, `rag_job_attempts`, `rag_runs`, `rag_run_metrics`, `rag_evaluation_cases`, `rag_evaluation_results`, and minimal `rag_recommendation_feedback`. Do not add tables without data-lifecycle and migration review; this is a logical conceptual inventory, not a required 1:1 physical schema.
- Do not reuse a broad app/migration DB credential for Python: grant worker only leased staging and vetted RAG retrieval projections, with no business/security table access, schema DDL, policy or active-pointer mutation; prove this via negative PostgreSQL role tests. Use PostgreSQL transactional consistency, timestamps UTC, stable PKs/FKs/uniques, partial/index filters where measured. Index vectors with appropriate dimension/model version; validate extension support for repository's exact PostgreSQL image before Flyway migrations. Do not modify frozen Schema v1 or use Hibernate DDL to create tables. Estimate disk and use mounted storage: Windows C: has limited free headroom in the provided DxDiag.
- Source revocation and session expiry **override TTL/history retention** and invalidate all content-bearing derivatives; cleaned operational metrics may remain. Maintain explicit TTL for ad-hoc documents, generated answer histories, local-judge evidence and temp files; content logging off by default. Backup/portability of **derived RAG data** is a separate reviewed policy; derived indexes may be rebuilt from authorized sources, whereas feedback/evaluation history needs deliberate retention.

## Source-commit guard and revocation invariant

Before index activation, final answer/history authorization, highlight/source resolution and purge-completion checkpoints, Java must satisfy the **same final-check-to-COMMIT fence** as the owning source authority. For Fiction/Film/Vault, this requires a `vault`-owned transaction-scoped row lock shared by all participating mutations and the RAG final gate; RAG-only CAS or a fresh read outside the lock is insufficient. For approved mutable external files, Windows/NTFS native guarded handles must prevent write/replacement **until commit**, or live-file eligibility fails closed. Model/OCR work remains *outside* these short gates; no tokens stream before authorization commit. Exact ordering, failure/retry and deterministic after-last-check races are specified in [P17-3](../implementation/phase-17/revocation-and-derived-data.md). The bounded owner mutation/guard integration delta has been [accepted by the owner](../implementation/phase-17/owner-decisions.md) following Codex design review; it still needs a separate implementation handoff and verified runtime tests, not silent modifications to original modules.

## 7. Models and evaluation

- All model runtimes, parsers and evaluation judges operate offline after separately verified download/installation; prohibit live model pulls, AI SaaS calls and telemetry during test/inference. Audit transitive network clients and egress. Model licenses/sizes/checksums recorded.
- Hardware: Windows 11 Pro, i5-9600K (6-core), 16 GB RAM, RTX 2070 **8 GB dedicated VRAM**. Use CPU/offloaded workflows, sequential model loading/queues and memory budgets. Benchmark, do not promise arbitrary 7B/8B model performance.
- Candidate LLM: multilingual small quantized class (~3B–4B first); candidate embedding: BGE-M3 or small multilingual alternative; candidate reranker: multilingual cross-encoder. A specific version is chosen after offline VRAM/latency/accuracy tests. Spring AI vs Python is a responsibility choice, not a blanket capability claim.
- Per-ingestion: parse/coverage/schema/table checks, source/eligibility proofs, dedup, pipeline timing and checked publish. Per-query: source filter pass, citation resolution, abstention, latency, error status, selected evidence IDs. Per-recommendation: catalog membership, constraints/status correctness, evidence grounding, ranking timings. Use asynchronous local judge for semantic metrics and distinguish **PENDING/FAILED/COMPLETE** judge status from passing synchronous checks.
- Regression corpus target: 50–100 consented manually labeled questions/tasks covering Vietnamese/Hán Việt/English/Han, PDF OCR/tabular, DOCX/Obsidian/Excel, module structured answers, no-answer, Private/NSFW exclusion, deletion/revocation and recommendation across all statuses. Include 10+ table extraction fixtures and adversarial prompt-injection/zip-slip/path traversal fixtures as appropriate. Store only fabricated or owner-approved sample data in git; never commit actual private files.
- Metrics: retrieval Recall@K/MRR/nDCG, answer groundedness, citation precision and locator accuracy, OCR CER/WER, table structure accuracy, chunk coverage, recommendation Precision@K/nDCG@K/catalog validity, privacy violations and p50/p95 timings. Agree numeric gates from baseline measurement before implementation acceptance; **privacy leak, unauthorized source access, fabricated recommended ID and broken citations are release blockers (zero tolerance)**. A local LLM judge is an estimate, not proof of correctness.

## 8. Engineering principles and tests

- SRP: separate source authorization, extraction, chunking, retrieval, recommendation and evaluation; do not create a single huge RAG service.
- DIP/Ports-and-Adapters only for real boundaries (module source adapters, parser variants, model runtime, index provider). Strategy when formats differ, Adapter for external format/inference, Facade for reviewed orchestration, explicit bounded state machine for async jobs. Avoid generic factories/interfaces for one implementation, global `utils`, magic orchestrators or speculative multi-agent frameworks.
- Reuse existing Spring module APIs, DTO/error/security contract, transaction semantics, PostgreSQL infrastructure; no competing Vault import, Search or Portability implementations.
- Required deterministic revocation races: embedding vs revoke, publication CAS vs revoke, generation vs revoke, lost deletion event and worker restart, child embed revoked, expired session with queued judge, changed file with missed watcher; follow [P17-3 test matrix](../implementation/phase-17/revocation-and-derived-data.md). Contract/security/unit/property tests for malformed files, chunk determinism, tokenizer variation, table coordinates, link cycles, duplicate job delivery, restarts, cancellation, failing index publication, delete/trash/restore, NSFW toggles, external path safety, mock local model failures and API timeouts. Java tests JUnit/Mockito/Modulith/Testcontainers; Python pytest + typed schema/contract tests; integration and end-to-end local tests of service boundary and offline network denial.
- For the first release, keep concurrent inference capacity intentionally small (default one generation job) and ingestion/model loads bounded by measured RAM/VRAM. Prefer simple in-process workers with durable DB jobs and reconciliation; avoid Kafka/Celery/Redis unless a specific measured lifecycle problem requires them.

## 9. Deferred and owner gates

- **No Ideaverse data integration until the owner later supplies/authorizes it.** Preserve only a generic external-directory read-only adapter concept, no feature-specific code.
- No private content indexing, private PIN unlock for RAG, cloud API, frontend, mobile/laptop runtime, remote public network access, multiuser, annotations, write tools, external web scraping, or automatic outside recommendations.
- Codex has accepted the bounded Phase 17 public-contract/DB/worker-role delta at the design level and the owner recorded [approval](../implementation/phase-17/owner-decisions.md). Before handoff, the owner commits/pushes accepted preparation; the implementation slice must synchronize affected architectural artifacts and pass actual runtime tests. Phase 16 Frontend remains unstarted/owner-gated.

## Reference material (not a pinned dependency declaration)

- https://github.com/pgvector/pgvector — PostgreSQL vector extension and hybrid search examples
- https://docling-project.github.io/docling/ — structured document processing/chunking
- https://tesseract-ocr.github.io/tessdoc/Data-Files.html — OCR language packs
- https://github.com/PaddlePaddle/PaddleOCR — OCR/layout/table model candidates
- https://docs.ragas.io/ — RAG evaluation candidate
