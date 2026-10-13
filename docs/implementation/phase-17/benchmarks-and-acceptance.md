# Phase 17 — Offline Model/Parser Benchmark and Acceptance Plan

**Status:** Preparation proposal only. Numeric quality/performance thresholds below are illustrative planning gates unless explicitly marked zero-tolerance security invariants. No benchmark has yet been executed.

## 1. Hardware and operating mode

Source: owner-supplied `DxDiag.txt` (2026-10-13): i5-9600K (6 CPU cores), 16 GB RAM, NVIDIA RTX 2070 (8 GB dedicated VRAM), Windows 11 Pro. C: had ~40.5 GB free at report time; this is a point-in-time observation, not a storage guarantee. First deployment only on that desktop. Confirm Python/CUDA/driver/WSL or native-runtime compatibility experimentally; do not claim unsupported quantization or multi-model residency works. Sequential ingestion/inference/evaluation and a bounded local worker queue preferred.

## 2. Candidates, not pinned choices

| Capability | Candidate(s) | Comparative decision criteria |
| --- | --- | --- |
| LLM | Quantized small multilingual Qwen3-class ~3B/4B (candidate) | groundedness, Vietnamese/Han terms, abstention, GPU RAM and response time |
| Embedding | BGE-M3 or a small multilingual embedding model | recall@k, query language, memory, vector dimension, license |
| Reranking | multilingual compact cross-encoder (e.g. BGE-reranker-v2-m3) | nDCG@k improvement vs latency/VRAM |
| PDF/layout | Docling text/layout/table; native PDF text-first | reading order, source mapping, table fidelity |
| OCR | Tesseract `vie`, `eng`, `chi_sim`, `chi_tra` versus verified-language PaddleOCR/table pipeline | CER/WER, layout, merged-cell table structure, CPU/VRAM usage |
| Vector index | PostgreSQL + pgvector versus pgvector exact scan | extension availability, query recall/latency on initial corpus |
| Evaluation | deterministic checks + local judge (Ragas-style metrics) | calibration vs human labels, cost and repeatability |

Download model weights and language packs deliberately before offline testing; verify file checksums, licenses, and whether libraries attempt network calls during runtime.

## 3. Curated dataset plan

Create **50–100 labeled cases** from synthetic or separately owner-approved sanitized samples, covering:
- Vietnamese diacritics, Hán Việt concepts, English UK/US, Han Simplified/Traditional and mixed-language queries;
- 2+ scanned PDF quality levels, 10+ table fixtures including merged cells, multilingual cells, PDF tables, XLSX multi-sheet ranges/formulas;
- Obsidian frontmatter, callout, wikilink, embeds, block anchor, cyclic/missing links, Dataview code refusal, **approved A transcluding unclassified/revoked child B**, hidden spreadsheet sheets;
- module projections, exact structured values, trash/restore, `isNsfw`, **missing owner NON_PRIVATE approval even when `isNsfw=false`**, denied sensitive/unlisted field, changed content hash and explicit source-selection denial;
- recommendation across all Fiction/Film statuses, incomplete metadata, no matches, hard requirements, changed/delete candidate and novel-query request outside catalog;
- unsupported question, malicious prompt-injection embedded in file, unauthorized file path, zip-slip and archive bomb.

Each labeled case includes query, allowed source IDs, expected answer/abstention or candidate IDs, supporting evidence locators, constraints, language, source version and expected privacy state. Store real personal content outside Git and avoid plaintext logging in runs.

## 4. Measurement and promotion

- Ingestion: parse success, content/heading/table extraction coverage, OCR CER/WER vs ground truth, table cell/structure fidelity, source locator correctness, duplicate/idempotent update behavior, index publish atomicity, total time and peak RAM/VRAM.
- Retrieval: Recall@5/10, MRR/nDCG, language-specific false negatives, leakage checks, p50/p95 latency. Compare lexical, vector-only, hybrid, hybrid+rerank and chunk sizes independently (one change at a time).
- QA: correctness labels, unsupported claim rate, citation precision/locator resolvability, abstention behavior, local judge agreement, p50/p95 response latency and memory use.
- Recommendation: Catalog Validity, eligible coverage, hard-filter satisfaction, Precision@K/nDCG@K, grounded explanations, behavior with all reading/viewing statuses and no-match refusal.
- Reliability: restart/retry/dead job recovery, duplicate event and replay, **embedding/publish/generation/judge races against revoke**, lost delete event after restart, stale worker late-publication rejection, transitive embed invalidation, session expiry with queued judge, source hash replacement with watcher loss, failed worker, offline network isolation, corrupted parser output, model unload/reload recovery. See [R1–R8 deterministic scenarios](revocation-and-derived-data.md).

Zero-tolerance blockers (regardless of average scores): actual `rag_worker` role accessing business/security tables, worker DDL or RAG policy/publication mutation; stale-worker republish or retained answer/judge after revoke; access/embedding/citation leak of Private or security-state fields, unauthorized external path, use of remote AI services, recommendation not in eligible catalog, write-back to source files, or unsupported citation falsely marked precise. Nonzero quality/latency thresholds are **not yet approved**: collect a baseline on the owner hardware, then present evidence and propose thresholds to the owner/Codex before accepting implementation.

## 5. Reporting contract

For every job/run capture **non-content-bearing** `authorization_epoch`, field-policy version, source/dependency revision digests and `purge_status` (not raw paths or content) together with `run_id`, source-selection digest (not raw secret paths), policy/config/model/parser version, lifecycle state, timings, token/chunk/result counts, typed warnings and error codes, cited locator IDs, synchronous checks and separate `judge_status` + metrics. Avoid storing source content in general logs. Keep regression runs identified by immutable dataset and pipeline revision. Evaluation results are retained locally under a configurable policy **only while their source dependencies remain approved**; source revoke/session expiry overrides any judge/history TTL, leaving only sanitized non-identifying metrics and tombstones. Ad-hoc source bytes and temporary artifacts expire. Validate query count/ranking non-leakage under source filtering.

## 6. P17-1/2/3 mandatory acceptance gates

1. **Actual PostgreSQL `rag_worker` credential** (not an admin mock): cannot `SELECT` business/auth/security tables, perform schema DDL, change RAG approval/epochs/job publication; can perform only permitted leased staging. Enforce grants with real Postgres, inspect `search_path`/definer functions and Spring Modulith verification for the proposed edges. See [P17-1](integration-ownership-and-permissions.md).
2. **Classification/allowlist denial fixtures:** unclassified text/scanned PDF, unapproved Fiction with `isNsfw=false`, known NSFW, deleted or stale records, unapproved `review`/credential metadata, directory-added file, forbidden Obsidian embed and missing classification; prove no data in vector, prompt, judge, report, citations, counts or recommendations. See [P17-2](source-eligibility-and-allowlists.md).
3. **Version/revocation barriers:** run all R1–R8 deterministic concurrency/restart/dependency/session tests. Verify stale publish fenced, full content-bearing derivative purge, no unauthorized count/rank effect or later history/judge visibility. See [P17-3](revocation-and-derived-data.md).
4. **Workflow efficiency review:** assess redundant model loads, unnecessary re-embedding of identical revisions, over-eager OCR, table/chunk fidelity trade-offs, too-frequent full-file rehash, excessive Java↔Python round trips, unnecessary source-wide locks, premature async infrastructure and evaluation cost; optimize only with measured benchmarks while preserving the *mandatory* final freshness/security fence. Record approval of any alternative equivalent enforcement.

## 7. Review gating

No model is chosen solely by benchmark claims from the internet. Antigravity collects local evidence in the implementation phase, Codex reviews the evidence and any architecture/security deviations; actual version/threshold decisions require recorded acceptance. Do not add GPU-only hard requirements that fail on a desktop lacking CUDA, and do not allocate multiple ML models concurrently without empirical VRAM fit.

## Mandatory last-check-to-commit fault injection (P17-3)

The Phase 17 foundation cannot pass on a fresh-read, epoch-only CAS or mocked transaction alone. Run PostgreSQL Testcontainers with independently controlled owning mutation + Java RAG transactions and deterministic latch **after final authoritative read, before COMMIT** for Fiction/Film NSFW/content and Vault trash. Run Windows/NTFS writer processes after final external-file hash/identity check and before the DB commit, with lost watcher events. Verify both commit orders, no streaming before authorization, sorted mixed-source lock ordering, deadline failures, stale worker writes while purge is completing, and correct anti-replay after restore. Reference [P17-3 guard protocol](revocation-and-derived-data.md); record runtime Win32 share behavior and unsupported filesystems as release evidence. These are **future tests**, not executed preparation results.

## 8. P17-N1 — held-out evaluation before first affected capability acceptance

This is a **nonblocking Phase 17 preparation follow-up** from the [2026-10-13 Codex review](reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md), but a **blocking acceptance condition when the affected capability first appears** (P-18 parser/OCR/ingestion quality, P-19 retrieval/QA/local judge, P-20 recommendation). Do not postpone independence/calibration to Phase 22.

- Assign a clearly versioned **development/tuning split** and a separately **frozen held-out split** by *source/document group*, not randomly by chunk or question, before comparative tuning. Prevent near-duplicate/source/embedded-document leakage across splits.
- Record consent, source/version/config/dataset hashes, human reference labels and adjudication instructions. Use synthetic or owner-approved non-private fixtures only; no Private/NSFW data committed to repository.
- Specify per-language/per-format/table/no-answer and adversarial slices for each introduced capability. Report coverage and denominators, including invalid, failed, skipped and pending runs; missing judges cannot count as passing.
- Calibrate local LLM-judge outcomes against blind human labels and document disagreement/uncertainty before relying on automated acceptance scoring. Security invariants remain deterministic zero-tolerance checks, not probabilistic judge scores.
- Codex must verify split disjointness and calibration **at the first relevant Phase 18/19/20 acceptance gate** and recheck any revised dataset or model. Phase 22 aggregates, audits and compares the prior frozen results without retroactively changing the held-out splits.
