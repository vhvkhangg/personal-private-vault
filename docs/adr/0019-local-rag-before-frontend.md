# ADR-0019 — Owner-approved local RAG preparation before deferred frontend

- **Status:** Accepted 2026-10-13; clauses 2–5 amended by accepted [ADR-0020](0020-phase-17-single-job-reservations-e1.md), owner-approved 2026-10-15. Current candidate is CHANGES_REQUESTED, not accepted implementation.
- **Date:** 2026-10-13
- **Decision owner:** Repository owner
- **Supersedes:** Only the *sequencing/deferral* of RAG in ADR-0014; the original reasons for deferral and all unrelated frozen decisions remain historical.

## Context

Backend Phases 0–15 are complete/frozen with the Phase 15 closure audit `BACKEND_AUDIT_READY` and owner commit `0b1ab11`. The historical roadmap labeled Phase 16 Frontend and Phase 17 as a single RAG phase. The owner now requests six distinct proposed RAG phases 17–22 **before** starting deferred Frontend Phase 16; completed 0–15 identifiers remain unchanged. Phase 16 remains deferred.

The owner requires a local-only personal RAG system on a Windows 11 desktop (i5-9600K, 16 GB system RAM, RTX 2070 with 8 GB dedicated VRAM), initially fewer than 500 document files. It must handle approved non-private Vault module data and user-selected external documents; support `.md` (including Obsidian syntax), `.txt`, `.docx`, `.pdf` (including scanned pages and tables), `.xlsx` and legacy `.xls` via a suitable adapter; support Vietnamese (including Sino-Vietnamese terminology), English (UK/US spellings), simplified/traditional Han scripts; answer with grounded evidence and source highlights; and recommend **existing** catalog items across relevant domains/statuses. Ideaverse is not yet integrated; future Obsidian-to-Vault sync must be one-way/read-only.

## Accepted decision and guardrails

1. **Out-of-order phase gate:** Prepare/implement Phase **17–22 (RAG)** before Phase **16 (Frontend)** after owner approval and a successful **separate pre-handoff review for each numbered phase** (`P-17` for foundation, `P-18` for ingestion, etc.). Retain phase numbers and histories; this is an explicit exception to the ordinary previous-number prerequisite. Phase 15 is the required completed predecessor for P-17. `ACTIVE.md` must be `NO_ACTIVE_HANDOFF` before Codex creates a handoff. The exception does not waive preparation/implementation reviews, owner commits, or frozen-baseline gates.
2. **Runtime separation (amended ADR-0020):** Spring remains sole business/policy
   authority. Phase 17 uses one RAG-owned job-scoped loopback Python/FastAPI process
   for the actual bounded synthetic workflow. The supported runtime is one local
   Windows desktop, one permanent user/account and one active indexing job.
   Later parser/model/query functions retain separate phase gates. No distributed
   execution, daemon fallback, generic framework or remote worker.
3. **Bounded owner delta (amended ADR-0020):** rag -> vault/fiction/film public
   edges only; Vault owns stable retained source/version/reset/reservation state
   and narrow transaction operations; owning mutations have mandatory SQL guard/
   version backstops. RAG owns consent/policy/dependencies, single-job admission,
   native process lifecycle, grants, exact staging and Java P. Worker has no
   business/Vault SQL, canonical job/policy/publish/release/DDL rights. See P17-1.
4. **Persistence (amended ADR-0020):** PostgreSQL remains metadata/index authority,
   pgvector subject to measured compatibility. Accepted additive logical target is
   [Phase 17 DBML](../database/phase-17-alternative-b.dbml); preserve Schema v1 and
   V1–V13. Future implementation appends after V13, separately authorized. Durable
   source reservations span the entire indexing lifetime without long SQL locks;
   one static nonterminal attempt constraint, one-use grant and attempt writer seal
   suffice, not F/V/DRAIN/v3 tickets. P-or-abort/seal/terminal/release share physical C.
5. **Privacy/consent/expiry (amended ADR-0020):** Private/NSFW and unknown/secret
   content categorically excluded; exact initial source/field matrix retained.
   Explicit per-item consent uses reset/version/epoch/policy CAS; ordinary eligible
   edits preserve it but OUTDATE dependent indexes. Eligibility loss/restore never
   renews consent. Exact description-hash confirmation may accompany manual start.
   Reserved relevant edits/revoke/classification return 409 with no change; after
   safe cancel/recovery release the owner retries. Committed exclusion immediately
   denies transitive artifacts before verified purge. Accept E1(a)/(b)/(c), including
   post-deadline activity and late trusted P after valid final gates; do not claim
   strict physical-C-before-expiry. A current persistent published index, including
   valid late P, is independent of completed execution lease/service session/boot.
   Independent ad-hoc selection retention still applies. No automatic reindex or
   resume; normal restart preserves fresh consent/completed indexes. No external
   network/AI/telemetry/runtime downloads or source write-back. See P17-2/P17-3.

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

The following preparation sequencing is historical; the existing handoff is now CHANGES_REQUESTED. ADR-0020 and the 2026-10-15 owner record approve contract synchronization only; separate implementation authorization is required. The owner explicitly accepted the bounded P17-1/P17-2/P17-3 integration delta on 2026-10-13, recorded in [Phase 17 owner decisions](../implementation/phase-17/owner-decisions.md). The [Codex design review](../implementation/phase-17/reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md) is `READY FOR HANDOFF` for the **foundation only**; it is not a runtime security certification. The accepted preparation must be committed/pushed before a separate `$codex-create-handoff` can authorize implementation. Later phases have independent gates.

## Related documents

- [Multi-phase RAG plan](../implementation/rag-phase-plan.md)
- [Phase 17 Preparation](../implementation/phase-17/README.md)
- [Proposed RAG Architecture](../architecture/rag-architecture.md)
- [ADR-0014](0014-defer-frontend-rag-and-deployment.md)
- [P17-1 bounded integration delta](../implementation/phase-17/integration-ownership-and-permissions.md)
- [P17-2 source eligibility/fields](../implementation/phase-17/source-eligibility-and-allowlists.md)
- [P17-3 revocation/derived data](../implementation/phase-17/revocation-and-derived-data.md)
- [Frozen module matrix](../architecture/module-dependency-matrix.md)
