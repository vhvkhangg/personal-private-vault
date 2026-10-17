# RAG Phases 18–22 — pre-preparation complexity review

**Review date:** 2026-10-17
**Baseline:** GitHub `main` at `d02ed14844a2fcc775145fa032d3bee7fc2f370c` (Phase 17 formal closeout).
**Status:** `RECOMMENDATIONS — OWNER APPROVAL REQUIRED FOR CONTRACT CHANGES`. No next phase is started. The normative plan remains [`rag-phase-plan.md`](rag-phase-plan.md), and [ADR-0019](../adr/0019-local-rag-before-frontend.md) and [ADR-0020](../adr/0020-phase-17-single-job-reservations-e1.md) remain unchanged.

## Findings by planned phase

| Phase | Existing planned breadth / likely cost driver | Recommendation (not yet a scope amendment) | Must keep |
| --- | --- | --- | --- |
| 18 | MD/Obsidian, TXT, DOCX, PDF scanned/layout/table OCR, XLS/XLSX, English/Vietnamese/Chinese, adaptive chunks + exact locators in one release | **SIMPLIFY** into tested vertical sub-slices inside Phase 18: text/Markdown and native-text PDF first, then Office/spreadsheets, then scanned OCR and complex tables with separate fixtures; share parser safety/provenance only where actually used. Ask owner before deferring any mandated format/language. Avoid building a general plugin engine. | Owner-selected file policy, non-private consent, source-dependency invalidation, safe paths, exact provenance, bounded resource behavior and tests for each supported format. |
| 19 | Local LLM, embeddings, hybrid lexical/dense retrieval, rerank, evidence spans across formats, abstention, async judge and QA metrics | **SIMPLIFY** to one measured compatible local runtime, scoped retrieval and cited grounded answer as the critical vertical slice. Candidate optional rerank/judge/retry should be enabled only if measured evidence shows value; labels and test baselines remain required where acceptance demands. | No answer from unauthorized sources, accurate citations, refusal on insufficient evidence, local offline operation, no prompt-injection-driven tools. |
| 20 | Multi-status recommendations, filter combinations, hybrid scorer, reranker, explanation, feedback, Precision@K and nDCG@K | **SIMPLIFY** by shipping deterministic catalog-ID filtering/ranking and grounded explanation first; add semantic rerank/feedback persistence only when concrete quality benefit exists. Keep modest reproducible labeled fixtures; avoid a separate ML recommendation platform. | Only existing eligible IDs; Fiction/Film and all statuses by default; no private/trashed/count leakage; validated filters and rationale. |
| 21 | Expansion across unspecified future Vault modules plus typed exact queries and reconciliation | **DEFER speculative connectors**. Approve a specific module/field slice only when owner names it; one owning-module API at a time. No generic connectors/sync framework. | Explicit per-source/field approval and Vault module source of truth; deny unknown/more-sensitive modules. |
| 22 | New metrics dashboards, error budgets, repeated evaluations, reliability experiments, system audit | **SIMPLIFY** to a finite local JSON/Markdown quality/performance report and regression matrix unless owner actually needs a dashboard. Reuse Phase 18–21 receipts. No arbitrary 420-job repeat, distributed observability stack or physical-power-loss certification absent a new concrete requirement. | Bounded end-to-end index/retrieval/recommendation evaluation, actual recovery/privacy proof and no runtime external AI dependency. |

## Cross-cutting overengineering triggers observed in planned documents

1. **Conflating the final six-phase product with each numbered phase.** Existing Phase 17 README contains end-state OCR/QA/recommendation requirements; they are not Phase 18 handoff authority by themselves. Use explicit per-phase MUST/SHOULD/DEFERRED and a testable one-slice success condition.
2. **Security proofs multiplying without a changed threat/authority boundary.** Preserve Phase 17 verified source reservations, E1, positive process stop and PostgreSQL fencing; only retest affected behavior. Do not invent OS-wide firewall/WFP/DNS, new multi-tenant, distributed worker or universal power-loss requirements.
3. **Mandatory evaluation too early.** P17-N1's source-grouped held-out labels/judge calibration is a real planning obligation, but a full local LLM judge infrastructure in Phase 18 (before an answer model exists) risks waste. Require the minimum dataset/labels for the current parser/index task, propose owner-approved timing for judge infrastructure.
4. **Performance tests becoming a parallel project.** A named target, representative samples, raw receipts and bounded test are enough. Separate measured from inferred/estimated. Do not mandate an extra benchmark simply because a column exists in a rubric.
5. **Tool-induced review growth.** A 29-dimension rubric should yield phase-scoped PASS/N/A/NOT_VERIFIED dispositions, not 29 feature requests. Findings must trace to an accepted contract or concrete defect. Two-round remediation cap prevents uncontrolled agent loops.
6. **Version freshness mistaken for stability.** The latest major may not have Windows CUDA/OCR wheels. Prefer the existing working environment and the newest compatible stable stack, with version review at the next *authorized* preparation gate.

## Phase 17 frozen baseline vs. current technology policy

| Component | Recorded current choice | Assessment for a next-phase preparation, not an upgrade command |
| --- | --- | --- |
| Java | Maven `java.version=25` | JDK 25 is LTS; Java 27 is newer feature release, not a requirement. |
| Spring Boot | Maven parent `4.1.1` | Stable 4.1 series; use Spring-managed BOM and confirm other Modulith versions when changing. |
| Spring Modulith | BOM property `2.1.1` | Pinned accepted compatibility; no incidental upgrade. |
| PostgreSQL | Actual reviewed Foundation environment `18.6` with pgvector `0.8.6` | 18.6 stable supported; verify extension/image ABI when choosing newer images. PostgreSQL 19 is not a required upgrade while GA is pending. |
| Python | `rag-engine/.python-version=3.14.4`, `requires-python>=3.11` | Python 3.15.0 is newly stable, but 3.14 remains a sound pinned runtime for current native wheels. Confirm before any interpreter migration. |
| Python web/DB | `fastapi==0.143.0`, `uvicorn==0.54.0`, `pydantic==2.14.1`, `psycopg[binary]==3.3.6` | Exact lock-backed Phase 17 environment; backend does **not** yet ship OCR, embedding, reranker or LLM dependencies. |

**Result:** ADR-0013 already says "latest stable *mutually compatible*". Add explicit cross-language/Windows GPU/lock selection language in agent workflow; do not overwrite frozen version files. Versions above are **what code records**, not a blanket claim that every pinned package is numerically the newest available. Public official release state must be rechecked only at the next authorized phase preparation.

## Before authorizing Phase 18

Ask owner to approve/reject the proposed staged delivery inside Phase 18, especially whether all OCR/language/table cases remain mandatory for the phase and the minimal local hardware acceptance envelope. Once approved, prepare Phase 18 README/preparation review, then run Codex pre-handoff review with the strengthened gate. No new worker/provider architecture, no Phase 18 code or handoff is created here.
