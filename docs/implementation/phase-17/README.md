# Phase 17 — Local RAG Foundation and Source Authority (RAG-first execution)

**Status:** `READY FOR HANDOFF` — **Phase 17 foundation preparation only**, per the [focused P17-3 and 29-dimension Codex review](reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md) (2026-10-13). P17-1/2/3 are CLOSED at the design-contract level; actual runtime enforcement remains a mandatory implementation gate. Earlier rejections are preserved as historical evidence. **No implementation handoff has been created.**

## Authorization and sequence

The owner explicitly starts RAG preparation **before** Frontend. Retain original identifiers: **Phase 17 = RAG foundation**, **Phases 18–22 = later RAG delivery**, **Phase 16 = Frontend (deferred)**. See accepted [ADR-0019](../../adr/0019-local-rag-before-frontend.md) and [roadmap](../../roadmap.md). Backend Phases 0–15 are complete/frozen, Phase 15 result is `BACKEND_AUDIT_READY`, and [`ACTIVE.md`](../handoffs/ACTIVE.md) must remain `NO_ACTIVE_HANDOFF`. Phase 17's exceptional immediate predecessor is Phase 15; Codex verified and the owner accepted this sequence without inventing retroactive Phase 16 completion.

## Owner-approved RAG end-state requirements (not all Phase 17 deliverables)

- Operate on the owner's **desktop only**, 100% offline at runtime; no external AI API/telemetry/data egress; fewer than 500 document files initially.
- Grounded question answering and semantic retrieval from user-selected external `.md/.txt/.docx/.pdf/.xlsx/.xls` files and authorized **non-private** records from suitable Vault modules. Persistent selected-source indexing and ad-hoc ephemeral source analysis; explicit per-query scope/filter choice.
- Preserve Obsidian syntax (frontmatter, wikilinks, embed/link-target rules, callouts, tags, block refs, comments) and prepare clean future extension for Ideaverse **without adding Ideaverse integration yet**. Ideaverse is not currently in this repository and will be one-way/read-only if/when later authorized.
- OCR scanned PDF in Vietnamese, English, Simplified/Traditional Han with table/layout recognition and source coordinates; treat Hán Việt as Vietnamese terminology rather than a standalone OCR alphabet. Handle English UK/US variations.
- Structure-adaptive chunking, reproducible index versions, hybrid search + multilingual reranking, source-linked answers and **non-persistent evidence/search-term highlights**; fail safely on insufficient evidence.
- Recommendation ranking across appropriate domain items (first Fiction and Film) in **all** reading/viewing states, extensible to other domains. Only existing eligible catalog IDs may be returned, with validated rationale and optional owner-specified filters.
- Deterministic bounded routing between grounded QA, typed source-of-truth read-only structured queries and catalog recommendations; no full autonomous Agentic RAG in baseline.
- Per-ingestion, per-query and per-recommendation telemetry/evaluation summaries, async local-judge results and reproducible offline regression/benchmark dataset.
- **Private/NSFW never indexed**, and no passwords/PIN/tokens or unsupported sensitive fields copied into RAG. No write-back to business data, Obsidian, or external documents.

## Canonical design references

- [Proposed RAG architecture](../../architecture/rag-architecture.md)
- [Accepted ADR-0019](../../adr/0019-local-rag-before-frontend.md)
- [Frozen Module boundaries](../../architecture/module-boundaries.md) and [dependency matrix](../../architecture/module-dependency-matrix.md)
- [Global Search](../../architecture/search-architecture.md), [Security](../../architecture/security-architecture.md), [Existing import/export](../../architecture/storage-backup-import.md)
- [Quality attributes](../../architecture/quality-attributes.md), [Existing API contract](../../architecture/api-architecture.md)

## Codex-approved design contracts (runtime enforcement still unverified)

- [P17-1 proposed module/storage ownership and DB roles](integration-ownership-and-permissions.md): one proposed Java `rag` leaf, bounded `vault`/`fiction`/`film` edges, Flyway authority and actual least-privilege worker-credential test gate.
- [P17-2 authoritative classification and field matrix](source-eligibility-and-allowlists.md): individually approved non-private Fiction/Film and external-file sources only in the first slice; unreviewed modules, secret/sensitive fields and embedded children deny by default.
- [P17-3 revocation and derived artifact fencing](revocation-and-derived-data.md): synchronous deny, source epochs/content revisions, transclusion dependency invalidation, active/retired/staged content purge and deterministic late-worker/judge tests.

The end-state requirements above are allocated across Phases 17–22; only the **Phase 17 foundation** is eligible for the immediate handoff after owner acceptance/commit. Codex closes all three preparation findings; this does not certify runtime enforcement or grant later-phase permission. See the latest review for mandatory tests, nonblocking follow-ups and optimization decisions. Owner approval of the bounded additive guard/role/inventory delta and preparation commit precede separate handoff creation.

## Scope boundaries and implementation ownership (owner-approved for handoff, not implemented)

- Proposed thin Spring orchestration/source-read facade remains owner-authorized and publicly authenticated; all domain access via owning module read-only contracts. No domain repository/entity leakage and no arbitrary Python DB reads. The initial proposed module name/dependency edges, division of durable state and worker DB permissions are bounded in P17-1, now have Codex/owner design acceptance but still require a separate handoff, runtime verification and later integration synchronization; frozen Phase 0–15 code/schema/matrix stay unchanged in this preparation package.
- Python/FastAPI owns extraction, parsing, local ML inference, retrieval, reranking, staging and evaluation computation **only**; Java owns authorization, source policy and atomic activation/owner-visible output. PostgreSQL/pgvector proposed pending verified Postgres image and migration compatibility. Local model runtime benchmark decides actual dependencies.
- Preserve imported domain record semantics: indexing a `.md` or `.xlsx` does **not** automatically create a Vault Note/Information record and cannot sidestep existing ImportData preview/validation policy.

## Approved planning request: split RAG into real numbered phases (proposal)

The owner's planning decision replaces the earlier 17-A through 17-E *sub-slices* with **separate numbered phases 17–22**; see [RAG Phase Plan](../rag-phase-plan.md). This document's active P-17 review is now solely for foundational source authority/privacy/guard, worker and persistence/lifecycle. **Later work is intentionally deferred**: Phase 18 ingestion/OCR/Obsidian; Phase 19 QA/retrieval/highlighting; Phase 20 Fiction/Film recommendation; Phase 21 approved module expansion; Phase 22 full reliability/regression closure. Each later phase needs separate P-N preparation and handoff, not implementation permission from this P-17 review.

## Engineering quality gate

Apply repository skills `pragmatic-solid-design`, `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`, `backend-testing`, and proposed `local-rag-engineering` (subject to Codex acceptance). Prefer explicit roles and typed DTOs, dependency direction, ports at genuine provider/source boundaries, deterministic pure transform functions, KISS/DRY/YAGNI, meaningful exception classes, idempotent retries, fail-closed privacy, readable observability and versioned contracts. No gratuitous `ServiceImpl`, generalized agent frameworks or cross-module repository access.

Tests must cover full Java + Python contract, concurrency/retries, PostgreSQL semantics, privacy/source revocation, content-derivative purge and stale-worker publication fences, secure archive/path handling, OCR/tables/citations, recommendation catalog validation, offline enforcement, performance and local regression. Do not claim CI/IDE clean for this docs-only preparation. No test counts are invented; Phase 15's 1021-test count is historical evidence only.

## Owner acceptance — 2026-10-13

The owner accepted only the bounded P17-1/2/3 foundation integration delta described in [owner decisions](owner-decisions.md). Codex has already returned `READY FOR HANDOFF` at design level. This does not indicate a handoff has been created or any runtime guard/role test has passed. Commit/push preparation first, then invoke `$codex-create-handoff` scoped to foundation only.

## Gate and next step

1. Codex **already completed** `$codex-pre-handoff-review` with `READY FOR HANDOFF` for foundation only; the owner acceptance is recorded in [owner decisions](owner-decisions.md). Do not reopen the closed preparation findings without new evidence.
2. Owner commits/pushes the **preparation-only** slice using Codex's Conventional Commit message.
3. Only afterward run **`$codex-create-handoff`** for Phase 17 foundation; review the bounded handoff before Antigravity implementation.
4. Do not start or redesign Frontend. Ideaverse integration remains an explicit later owner gate.
