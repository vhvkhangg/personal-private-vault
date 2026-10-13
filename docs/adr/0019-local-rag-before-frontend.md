# ADR-0019 — Owner-approved local RAG preparation before deferred frontend

- **Status:** Accepted — owner-approved 2026-10-13 following `READY FOR HANDOFF` Phase 17 foundation review; implementation not yet started
- **Date:** 2026-10-13
- **Decision owner:** Repository owner
- **Supersedes:** Only the *sequencing/deferral* of RAG in ADR-0014; the original reasons for deferral and all unrelated frozen decisions remain historical.

## Context

Backend Phases 0–15 are complete/frozen with the Phase 15 closure audit `BACKEND_AUDIT_READY` and owner commit `0b1ab11`. The historical roadmap labeled Phase 16 Frontend and Phase 17 as a single RAG phase. The owner now requests six distinct proposed RAG phases 17–22 **before** starting deferred Frontend Phase 16; completed 0–15 identifiers remain unchanged. Phase 16 remains deferred.

The owner requires a local-only personal RAG system on a Windows 11 desktop (i5-9600K, 16 GB system RAM, RTX 2070 with 8 GB dedicated VRAM), initially fewer than 500 document files. It must handle approved non-private Vault module data and user-selected external documents; support `.md` (including Obsidian syntax), `.txt`, `.docx`, `.pdf` (including scanned pages and tables), `.xlsx` and legacy `.xls` via a suitable adapter; support Vietnamese (including Sino-Vietnamese terminology), English (UK/US spellings), simplified/traditional Han scripts; answer with grounded evidence and source highlights; and recommend **existing** catalog items across relevant domains/statuses. Ideaverse is not yet integrated; future Obsidian-to-Vault sync must be one-way/read-only.

## Accepted decision and guardrails

1. **Out-of-order phase gate:** Prepare/implement Phase **17–22 (RAG)** before Phase **16 (Frontend)** after owner approval and a successful **separate pre-handoff review for each numbered phase** (`P-17` for foundation, `P-18` for ingestion, etc.). Retain phase numbers and histories; this is an explicit exception to the ordinary previous-number prerequisite. Phase 15 is the required completed predecessor for P-17. `ACTIVE.md` must be `NO_ACTIVE_HANDOFF` before Codex creates a handoff. The exception does not waive preparation/implementation reviews, owner commits, or frozen-baseline gates.
2. **Runtime separation:** Existing Spring Boot remains the source-of-truth and sole business access/policy enforcement boundary. Add a minimal future Java RAG orchestration/adapter capability, subject to module-boundary review, to expose approved read-only source projections to a loopback-only **Python/FastAPI** service. Python owns parsing, local inference, retrieval, reranking, evaluation, and disposable derived indexes. Neither Python nor other Java modules may access owning modules' internal repositories/entities. No generic service mesh, agent framework, or microservice expansion.
3. **Bounded future module and ownership delta:** Propose one new Java `rag` leaf module with only `rag -> vault`, `rag -> fiction`, `rag -> film` dependencies via reviewed public source projections. Java `rag` owns owner approval/classification, grants, jobs and final generation publication; owning domain modules keep NSFW/deletion/content authority; Python owns disposable extraction/index staging and evaluation computation, not final access decisions. A new RAG DB namespace is migrated only by Spring/Flyway; a distinct, least-privilege `rag_worker` PostgreSQL role must be blocked by grants from business/security schemas and DDL, and from authoritative RAG policy/publish tables. See [P17-1 approval boundary](../implementation/phase-17/integration-ownership-and-permissions.md). **These bounded new edges/permissions are owner-approved for the separately gated Phase 17 foundation handoff, but are not implemented**; synchronize the affected module matrix/tree/diagram artifacts with the authorized implementation, never by changing frozen history.
4. **Persistence:** PostgreSQL remains the source for RAG metadata and indexes; pgvector and lexical retrieval are proposed subject to PostgreSQL-version/extension compatibility and performance benchmarks. Only append-only migrations for new RAG-owned data are permitted *after implementation authorization*. Existing frozen DBML/ownership and the implemented 19-module matrix are unchanged in preparation. The bounded Phase 17 `rag` dependency edges are explicitly owner-approved for later implementation only, and must be synchronously recorded in the architecture/matrix as implemented; any additional edge requires separate approval.
5. **Local privacy:** Query, corpus, OCR, embeddings, model inference, reranking, and local evaluation never transmit document content to outside AI services; disable telemetry and runtime downloads. Private/NSFW content is **excluded from ingestion/indexing** rather than gated by a PIN, and secrets/security state are prohibited. Unknown classification fails closed. File scopes are explicit; no blanket filesystem scan. The **initial** eligible module source list is Fiction/Film, with a per-source NON_PRIVATE owner grant, owning-module `isNsfw` + Vault deletion proof and a narrow field whitelist; free-text description needs an extra revision-bound approval. External file selection also requires **individual owner classification**; an approved parent cannot authorize an unclassified embed. All other modules/fields remain denied until their reviewed source projection and owner-approved field matrix are added ([P17-2](../implementation/phase-17/source-eligibility-and-allowlists.md)). No write-back to user-managed external sources, including future Obsidian/Ideaverse. On revoke, revision change or session expiry, Java synchronously denies and **fences active jobs, generations, answers/history, caches, highlights and queued/running judges**, including transclusion dependencies. Late publications are rejected and all content-bearing derivatives purged despite longer TTL settings ([P17-3](../implementation/phase-17/revocation-and-derived-data.md)).
6. **Separate capability paths:** Grounded Q&A uses hybrid retrieval, evidence filtering, bounded retry and citation/grounding checks; catalog recommendation uses candidate eligibility, feature/semantic scoring, reranking, grounded explanations and validated item IDs; structured factual questions may use **allowlisted read-only APIs**, not LLM-generated SQL. Full autonomous Agentic RAG is out of scope until measured evidence justifies it.
7. **Continuous evaluation:** Each ingest/query/recommendation run records typed outcome, provenance, metrics and error codes, with fast synchronous safety checks and bounded asynchronous local judging. Regression/benchmark comparisons against curated ground truth are required before model/parser/chunking/index changes are promoted.
8. **Ideaverse deferment:** Preserve adapter seams for eventual one-way sync from an owner-selected Obsidian directory, but do **not** add an Ideaverse module, schema, watcher, seeded content, or implementation in RAG Phases 17–22 without later explicit owner approval.

## Consequences

- Authorizes the **Phase 17 preparation and its bounded future integration scope**, not production implementation without a separate Codex handoff. No new backend services, modules, migrations, dependencies, model downloads or code are shipped by this change.
- Update roadmap and pre-handoff tooling instructions for RAG Phases 17–22 before deferred Phase 16, with a separate preparation/review/handoff per phase. Phase 16 remains unstarted/owner-gated after Phase 17; it may only be reconsidered after RAG Phase 22 closes.
- Every numbered RAG phase requires its own preparation, handoff and reviews; implementation handoffs must be bounded and include empirical hardware, OCR/table, multilingual, retrieval, recommendation, privacy and lifecycle evidence.
- Model and parser choices are **benchmarked candidates**, not frozen product dependencies.

## Considered alternatives

- **Wait for Phase 16:** rejected by owner's explicit RAG-first direction.
- **Renumber phases:** rejected to avoid destabilizing completed phase history and references.
- **Pure Spring AI:** workable but less suitable for this project's chosen Python-centered multilingual document/evaluation experimentation; do not claim Spring AI lacks RAG capability.
- **Full autonomous agent first:** rejected due to uncertain gains, resource limits, and increased privacy/tool safety surface.
- **Separate vector database/cloud API:** rejected as premature for the initial under-500-file workload; benchmark before revisiting.

## Owner acceptance and implementation gate

The owner explicitly accepted the bounded P17-1/P17-2/P17-3 integration delta on 2026-10-13, recorded in [Phase 17 owner decisions](../implementation/phase-17/owner-decisions.md). The [Codex design review](../implementation/phase-17/reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md) is `READY FOR HANDOFF` for the **foundation only**; it is not a runtime security certification. The accepted preparation must be committed/pushed before a separate `$codex-create-handoff` can authorize implementation. Later phases have independent gates.

## Related documents

- [Multi-phase RAG plan](../implementation/rag-phase-plan.md)
- [Phase 17 Preparation](../implementation/phase-17/README.md)
- [Proposed RAG Architecture](../architecture/rag-architecture.md)
- [ADR-0014](0014-defer-frontend-rag-and-deployment.md)
- [P17-1 bounded integration delta](../implementation/phase-17/integration-ownership-and-permissions.md)
- [P17-2 source eligibility/fields](../implementation/phase-17/source-eligibility-and-allowlists.md)
- [P17-3 revocation/derived data](../implementation/phase-17/revocation-and-derived-data.md)
- [Frozen module matrix](../architecture/module-dependency-matrix.md)
