# Phase 18 — Document Ingestion, OCR, Embedding and Verified Indexing

**Preparation status:** `READY FOR HANDOFF` after [independent Codex re-review 2](reviews/2026-10-17-phase-18-pre-handoff-codex-re-review-2.md) (2026-10-17): P18-PH-01–04 closed for preparation. **Implementation:** `NOT_STARTED`; **active handoff:** `NO_ACTIVE_HANDOFF`. Owner approved [P18-01–P18-10 design decisions](owner-decisions.md) and D18-A. This is preparation acceptance, **not implementation authorization or runtime certification**.

**Source baseline:** owner-pushed GitHub HEAD `86b08a1a44d7672b111af1977098ef6c4c3e800a`; Phase 17 Alternative B/E1 is [COMPLETE — FROZEN](../phase-17/closeout.md). Phase 16 Frontend is deferred. Live external-file ingestion remains disabled pending the narrowly bounded, tested R7/NTFS proof and an approved Phase 18 handoff.

## Goal and delivered behavior

Create complete, trustworthy, current **document indexes** for individually owner-approved sources using the existing Java `rag` orchestrator, restricted job-scoped Python worker, PostgreSQL/pgvector, immutable staging, current eligibility/fencing and Java-only final publication. No new backend job engine.

**Required supported input:** Markdown/Obsidian (`.md`), plain text (`.txt`), Word (`.docx`), PDF native text, PDF scanned/mixed/layout/table OCR, Excel (`.xlsx`, `.xls`). Handle Vietnamese, English, Simplified/Traditional Chinese. Obsidian must preserve approved frontmatter/heading/tags/wikilinks/aliases/anchors/block refs/callouts and separately approved embed dependency/child provenance. Office tables, PDF page/bounding boxes and Excel workbook/sheet/A1 cell-range locators are required where technically recoverable; if a mandatory locator cannot be established, fail safely rather than invent it.

**Manual selection only:** explicit file/directory selection yields a candidate manifest, never automatic whole-folder approval; every file/version and indexed body/metadata field needs its own current owner grant and authoritative classification. Do not index private/NSFW, unknown, trashed/revoked, credentials, comments/hidden spreadsheet sheets/unapproved YAML properties or remotely referenced materials. The system never writes back to external files.

## Single pipeline; no new infrastructure

```text
Java verified approval + whole source/dependency reservation and job admission
      -> existing supervised one-use Python worker / scoped input
      -> format-specific extraction and safe dependency checks
      -> page/region OCR only when native text is insufficient (<=1 justified targeted retry)
      -> normalized structured elements with exact source locators
      -> ONE structure-first chunker with minimal format-specific grouping
      -> ONE measured multilingual embedding model (GPU, optionally one same-model CPU fallback)
      -> staged file-level success/failure receipts, sealed committed candidate data
      -> ONE trusted Java finalization transaction after the job finishes
         (validated successful files published together OR no publication)
      -> final job summary + per-file report; independent effective CURRENT checks
```

- **Failure policy:** material extraction/OCR/table/provenance failure aborts **that file**. A file with nonmaterial warnings may succeed with reported uncertainty. Failed file's old generation may be served only if it still passes fresh `CURRENT` proof; ordinary file edits make it `OUTDATED` and unavailable as current evidence.
- **Job policy:** partial *file* success is permitted. One job/one worker/whole-closure reservations remain. If worker identity, cancellation, source-set fencing, staging integrity or finalization are compromised, **abort the whole publication**. Successful-file publication is all-or-nothing in one final Java physical COMMIT with terminal state and reservation release; no early per-file publish. Obsidian transclusions must be attributed to approved child sources; failed child invalidates dependent candidates unless the exact valid dependency proof is established.
- **D18-A contract gate:** the owner **explicitly approved** Codex’s full eight-part proposal as Phase 18 **design/preparation**. [ADR-0021](../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md) specifies the bounded additive extension, [Phase 18 logical DBML](../../database/phase-18-document-indexing.dbml) and [API/worker delta](../../architecture/rag-phase-18-api-extension.md) document it. [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md) still governs frozen production; only independent Codex acceptance and a later separate handoff can authorize implementing the extension.

## Small, sequential internal implementation slices — one numbered phase

1. **Text/Obsidian:** safe selected-file path admission; TXT, Markdown/Obsidian syntax, independent embed grants/dependency manifests, normalized blocks and locators.
2. **Office:** DOCX paragraphs/headings/tables, XLSX and legacy XLS sheet/cell/range fidelity without execution of macros/formulas/external fetches.
3. **PDF/OCR:** native-text-first layout extraction, targeted OCR for scan/mixed page/region, tables/reading order/coordinates, viable auto language detection plus explicit language selection and bounded retry.
4. **Verified indexing:** unified structure-first chunking, chosen offline multilingual embedding model, version-aware dedup/reindex, safe file-level staged results, one job finalization, cancellation/restart and accurate report.

Slices are **engineering order only**: do not create four separate phases, independent pre-handoff cycles or four monolithic final suites. One separately accepted Phase 18 handoff and exactly one named [**P18-FINAL**](final-verification-gate.md) after focused convergence.

## Technology selection — avoid premature freezes

- Reuse the accepted Phase 17 Java/Spring, PostgreSQL/pgvector and Python/FastAPI worker. For candidates: Python stdlib TXT; a maintained Markdown AST parser with small Obsidian extension; `python-docx`, `openpyxl` and `xlrd` where compatible; PDF extractor/Docling-like layout option; benchmark a Tesseract-based option against a PaddleOCR-based option only as needed to satisfy Vietnamese/English/Han/table requirements.
- Benchmark at most a **small justified shortlist** of multilingual embedding candidates (for example E5-small vs BGE-M3), with representative Vietnamese/English/Chinese samples and semantic sanity cases. Select **one** model, fixed vector dimension, tokenizer version, artifact hash, licensing and Windows CPU/GPU ABI compatibility. Do not make a particular candidate mandatory before measurements. Never silently download models at runtime or change the frozen Phase 17 lockfile as a side effect.
- One normalized element representation and one structure-first chunker; no speculative plugin framework, orchestration queue, model registry, hot swap, multi-OCR production fallback, distributed worker or persistent retrieval/QA components. The smallest necessary parser-specific handling is allowed.
- Latest **stable mutually compatible** versions per [ADR-0013](../../adr/0013-stable-compatible-version-policy.md), with Windows/Python 3.14 ABI/native wheel, memory, offline/license and exact regression evidence. Do not force the numerically newest major or an implicit baseline upgrade.

## Resources and runtime boundaries

Hardware target: Windows 11, Intel i5-9600K, **16 GB system RAM** and RTX 2070 **8 GB dedicated VRAM**. Measure **peak RAM** (worker and owned subprocess working set/peak RSS and host free-memory impact where practical) separately from **peak VRAM**; also record elapsed time, limited temp-disk use, CPU/GPU fallback and cancellation responsiveness. Bound source bytes, inflated archive size, pages/sheets/rows, chunk count, batch, CPU time, RAM, VRAM, disk and per-file/job time. Process incrementally where the actual library supports it; otherwise enforce admission limits/reject safely. Choose finite values after small representative local measurements, **not arbitrary thresholds or infinite input sizes**. Agent CLI command timeouts (20 min normal/35 min full-suite) do not set end-user indexing timeouts.

## Not in Phase 18

No retrieval endpoint, hybrid/reranker/HNSW tuning, LLM answers, chat, local judge, catalog recommendations, generic future-domain connectors, auto-crawling/watchers that index, frontend UI, dashboard, production telemetry service, external model/cloud dependency, distributed system or Ideaverse-specific integration. Retrieval and user-facing citation/highlight rendering belong to Phase 19, while Phase 18 must preserve sufficient **source locators** for that phase.

## Acceptance, risk and review gate

See [acceptance-and-evidence.md](acceptance-and-evidence.md) for **AC-01–AC-08**, the independent [P17-N1 held-out/mandatory-locator oracle](held-out-quality-and-locators.md) and the executable [P18-FINAL gate](final-verification-gate.md). Critical new risks: Windows/NTFS R7 source identity and version proof; per-file successes in one final P17-compatible physical COMMIT; dependency closure isolation; RAM peaks on a 16 GB host; OCR/table/locator correctness across four OCR language packs; deterministic model/version identity; offline source restrictions and native worker quiescence.

Use [preparation-review.md](preparation-review.md) as the review entrypoint, [owner decisions](owner-decisions.md) as design authority, and [RAG delivery guardrails](../../workflow/rag-phase-delivery-guardrails.md) for proportional KEEP/SIMPLIFY/DEFER review, two Antigravity remediation rounds and bounded verification. All independent Codex reports will live in [`reviews/`](reviews/README.md).

**Next gate:** Owner commits/pushes the accepted preparation, then invokes `$codex-create-handoff` separately. All four preparation findings are closed, but **no Phase 18 implementation** is authorized until that new handoff exists. Live-file R7 and all runtime evidence remain mandatory future gates.
