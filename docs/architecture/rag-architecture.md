> **2026-10-19 effective scope:** [ADR-0023](../adr/0023-phase-18-application-level-snapshot-worker-trust.md) amends ADR-0022 for G1/G3 and same-Windows-user Python. **No** native NTFS root-to-stream/ABA proof, Restricted Token/LPAC/DACL worker confinement or forensic OS acceptance is required for the new external snapshot class. Java performs ordinary approved-file capture, SHA-256/no-overwrite R checks and bounded crash recovery; existing source-first SQL/producer-result/pgvector/citation correctness remains binding. The historical architecture paragraphs below (including 'confines' and external live/currentness) describe Phase 17 or pre-D12 design **where inconsistent** and must not be read as re-imposing the superseded security guarantee. Phase 18 code unverified, snapshot ingest inactive.

> **Current authority, 2026-10-18:** Phase 17 is COMPLETE — FROZEN. [ADR-0022](../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) is the consolidated OWNER_APPROVED / CANONICALLY_SYNCHRONIZED external snapshot/WHOLE_FILE/NO_CHANGE target. Its explicit applicability matrix prospectively replaces this document's broad external content-classification/field-safe/live-original rules only for that source class; the retained general/Phase 17 narrative below does not reintroduce those gates. Vault fields/privacy and unaffected ownership/E1/reservations/process/producer/physical-publication/quality/resources remain frozen. Historically [ACTIVE](../implementation/handoffs/ACTIVE.md) was STOPPED at the time of this 2026-10-18 snapshot. As of 2026-10-19, owner D13 separately authorizes [Phase 18 Slice 1](../implementation/phase-18/slice-1-import-snapshot-handoff.md) for Antigravity implementation and independent Codex review. Live-original indexing remains DISABLED/DEFERRED; snapshot indexing inactive, full Phase 18 NOT_VERIFIED.

# Local RAG Architecture — approved Alternative B contract

**2026-10-15 owner-approved contract, not accepted implementation.** See accepted
[ADR-0020](../adr/0020-phase-17-single-job-reservations-e1.md) and
[owner decisions](../implementation/phase-17/owner-decisions.md#2026-10-15--alternative-b-owner-design-approval).
Current task synchronizes documentation only. Existing candidate stays contained,
ACTIVE CHANGES_REQUESTED; FR17-5/8/9/10 OPEN, closed findings preserved.

Permanently one user/account, local Windows desktop, one active manual indexing
job. Durable whole-closure source edit reservations, small source/reset history,
explicit standing consent/CAS, one supervised job-scoped FastAPI worker, one-use
grant and SQL-writer seal replace F+H+V+E. No global native source lock/V/Vault
DRAIN/ticket ledgers, distributed coordination, queue or automatic resume/reindex.
Unrelated items remain editable. Normal completed CURRENT indexes and consent
survive work expiry/boot after fresh proof. Full bounded Foundation remains;
live external files remain disabled pending R7; no later product is enabled.

The later-phase pipeline/model/query paragraphs below allocate future capabilities
only. They do not authorize live consumers or background indexing; any later
consumer must receive reviewed source-use lifetime protection. Historical
[implementation evidence](../implementation/phase-17/test-evidence.md) does not
certify this architecture. Existing Global Search remains separate.

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

Architecture design principle: the modular monolith does not become a distributed application simply to deploy a Python ML runtime. All RAG components run on the single supported Windows desktop; durable work state and process boundaries are explicit. One job-scoped Python child must be verified unable to continue before source editing is released.

## 2. Eligibility and source catalog

**Normative approved contracts:** [P17-2 source/field matrix](../implementation/phase-17/source-eligibility-and-allowlists.md), [P17-3 revocation fences](../implementation/phase-17/revocation-and-derived-data.md), and [P17-1 module/storage/worker-role delta](../implementation/phase-17/integration-ownership-and-permissions.md). If a generic paragraph here sounds more permissive than these specific allowlists/fences, use the narrower fail-closed contract.

An **explicit SourceSelection** belongs to each ingest/query/recommendation job:
- `scope`: an allowlisted collection of approved module/entity types, specific document IDs, or explicit user-selected external files/directories; defaults to the owner's approved non-private corpus, never the whole drive;
- `ingestion_mode`: persistent indexing or ad-hoc ephemeral session (separate namespace, TTL/deletion after session);
- `source_kind`, `source_id`, `entity_type`, `owner_module`, version/checksum, timestamps and `access_state`;
- `eligible`: public/non-private, live (not trashed/soft-deleted), within source scope and not a secret-bearing/security-state field.

**Fail closed:** unknown private classification, unavailable source, unknown module-field privacy, unexpected symlink/path or stale policy -> refuse ingestion/answer and record sanitized error. `isNsfw=true` in Fiction/Film is excluded. No private or NSFW source can enter embeddings, full-text index, candidate pools, metrics contents or local judge prompts. Finance, Personal, Journal, Account, authentication/security state and **all other unreviewed modules are excluded in the first integration slice**; only approved Fiction/Film fields and individually approved external documents are initial candidates. Future modules require owner/Codex approval of type-specific classification and field-level projections, never broad DTO export. Structured finance calculations must call typed read-only domain operations; do not rely on chunk approximations. Private-state and deletion filtering is verified at ingestion **and** query/candidate/answer time.

Explicitly prohibit indexing credentials, PIN/password hashes, JWT/refresh tokens, API keys, authentication/session metadata and sensitive operational logs. No embeddings or derivative caches of excluded content. While reserved, relevant classification/revoke edits return 409 with no change until safe cancellation/recovery. When an authoritative change commits, synchronously deny new access and invalidate grants; purge **all** dependent content-bearing derivatives (including answer/history/judge/cache/session artifacts and transcluded copies), not only chunks/embeddings. Apply per-source epoch/source-revision fencing to in-flight jobs and publication (P17-3); do not wait for a scheduled full refresh. Same for trash, deletion and source revocation. Enforce authorization/scope before score calculation to prevent leaks through aggregate counts or ranking.

Sources from the filesystem require **per-file, per-version owner classification and field approval** even when a directory was selected, plus canonical path verification; disallow traversal, symlink escape, untrusted archives, network paths and external file execution. The application must never write back to selected files. Directory watchers/periodic reconciliation may identify changed candidates or cleanup; indexing remains explicit manual work. No automatic background reindex or crawl. Watcher events are hints, not authoritative state. **Ideaverse is not yet in the Vault**; no Ideaverse-specific watcher/module/ingestion obligation is active. Future owner authorization may enable one-way Obsidian-to-Vault synchronization with source directories strictly read-only.

## 3. Ingestion pipeline and idempotency

`ACCEPTED -> VALIDATING -> EXTRACTING -> NORMALIZING -> CHUNKING -> EMBEDDING -> STAGING -> VERIFYING -> PUBLISHED`; terminal `FAILED`, `CANCELLED`, `SUPERSEDED`, or `QUARANTINED` with typed error. Statuses are design proposals, not DB enum names.

- Deduplicate work by `(source_kind, source_id, content_hash, parser_version, normalization_version, chunk_strategy_version, embedding_model_id, privacy_policy_version)`; reject duplicate execution and preserve outcome identity; no automatic indexing retry/resume from events.
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

## 6. Service and storage contracts (approved target; not implemented)

Java rag -> vault/fiction/film public edges only. Vault owns retained source state,
version/reset watermark and reservation operations; original modules keep content/
NSFW/trash authority. RAG owns policy/jobs/process lifecycle/grants/staging seal/
publication/cleanup. Worker has no business/Vault SQL, policy/job/release/pointer/
DDL authority. No reverse owner dependency or reused portability exception.

Canonical [API inventory](api-architecture.md#16-phase-17-alternative-b-contract-amendment)
defines the bounded existing source/job actions, expected approval version/reset/
epoch/policy CAS, exact description-hash confirmation, typed 409 busy/changed and
historical outcome versus effective index freshness. ADR-0016 remains unchanged.
No query/chat/recommendation endpoint implementation is authorized in Phase 17.

Canonical [additive DBML](../database/phase-17-alternative-b.dbml) records the target:
stable Vault source metadata and reuse of RAG sources/job_leases/worker_grants/
staged_outputs/dependencies/index_generations/runs. One static unique constant-key
partial index over all nonterminal attempts; no new global slot table. Append
migrations after V13 only; preserve Schema v1/history. PostgreSQL 18.6/pgvector
compatibility and actual privileges remain tested gates, not assumptions.

## Source-consumption, physical COMMIT and index lifetime

Admission physically commits exact source-set reservations/job/grant before any
input. Every relevant maintained owner/policy/dependency mutation checks the same
stable source guard plus mandatory SQL backstop; reserved changes conflict with
no mutation C. RAG confines/identifies/binds one actual job-owned FastAPI process,
claims the grant once before body use, and joins launch/send tasks and verifies
actual worker/tree death on success/cancel. No ACK/connection/timeout substitute.

Attempt-row locks fence every S route through its physical C. Finish drains and
seals writers and atomically commits P-or-abort, terminal status and exact source
release. Short SQL guards only; no HTTP/digest/inference/process wait under them.
Uncertain process/controller/SQL outcomes retain reservation and active-job
exclusivity; unrelated items remain available. No timer takeover or auto-resume.
Detailed lock order/process/recovery requirements are in
[P17-3](../implementation/phase-17/revocation-and-derived-data.md).

The owner accepts E1(a)/(b)/(c): passed gates can precede post-deadline transfer/
consumption, late untrusted S and late trusted verification/P physical C. Retain
observed-expiry and post-wait/default-deferred rejection; strict E2 is replaced,
not proved. A valid late P can remain CURRENT after completed work TTL/session/
boot. IndexCurrent always requires fresh live NON_PRIVATE/non-NSFW explicit
consent/reset/epoch/field/version/full topology and independent retention/selection
scope. No execution TTL on persistent current index; ad-hoc selection-session
expiry still applies to its own artifacts. Historical SUCCEEDED is not CURRENT.

Normal eligible edits preserve consent but permanently OUTDATE old versions;
reset/restriction commits deny and require renewed consent plus verified cleanup.
No automatic reindex; reset/epoch and source/scope monotonicity defeat lost events
and hash/edge round trips. Normal restart preserves fresh completed indexes and
consent; abort/discard unfinished work after positive recovery proof. External
live-file capability stays disabled; OS identity/handle proof is separate.

Later parsing/query/judge source-use lifetimes need their own reviewed protection;
the indexing reservation is not blanket acceptance for a future parallel consumer.

## 7. Models and evaluation

- All model runtimes, parsers and evaluation judges operate offline after separately verified download/installation; prohibit live model pulls, AI SaaS calls and telemetry during test/inference. Audit transitive network clients and egress. Model licenses/sizes/checksums recorded.
- Hardware: Windows 11 Pro, i5-9600K (6-core), 16 GB RAM, RTX 2070 **8 GB dedicated VRAM**. Use CPU/offloaded workflows, sequential model loading and memory budgets; Foundation has no indexing queue. Benchmark, do not promise arbitrary 7B/8B model performance.
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
- The owner accepted Alternative B contracts under ADR-0020 in the existing handoff. Current authorization is documentation synchronization only; separate implementation authorization, actual runtime evidence and independent review remain required. Phase 16 Frontend remains unstarted/owner-gated.

## Reference material (not a pinned dependency declaration)

- https://github.com/pgvector/pgvector — PostgreSQL vector extension and hybrid search examples
- https://docling-project.github.io/docling/ — structured document processing/chunking
- https://tesseract-ocr.github.io/tessdoc/Data-Files.html — OCR language packs
- https://github.com/PaddlePaddle/PaddleOCR — OCR/layout/table model candidates
- https://docs.ragas.io/ — RAG evaluation candidate
