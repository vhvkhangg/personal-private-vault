# Phase 17 — Offline Model/Parser Benchmark and Acceptance Plan

**Status:** Accepted Phase 17 Alternative B verification obligations, amended 2026-10-15 under [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md). No new benchmark/runtime test executed by synchronization. Numeric quality/latency thresholds for later models remain planning proposals; §9 governs current Foundation. Historical execution evidence stays in test-evidence.md. Separate implementation authorization required.

## 1. Hardware and operating mode

Source: owner-supplied `DxDiag.txt` (2026-10-13): i5-9600K (6 CPU cores), 16 GB RAM, NVIDIA RTX 2070 (8 GB dedicated VRAM), Windows 11 Pro. C: had ~40.5 GB free at report time; this is a point-in-time observation, not a storage guarantee. First deployment only on that desktop. Confirm Python/CUDA/driver/WSL or native-runtime compatibility experimentally; do not claim unsupported quantization or multi-model residency works. Foundation: one manual indexing job, no queue or automatic retry/resume/reindex; Windows-native supported path. Later inference/evaluation remains separately gated.

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
- Reliability: safe abort/manual restart/dead job recovery, duplicate event and replay, **embedding/publish/generation/judge races against revoke**, lost delete event after restart, stale-authority publication rejection and separately approved E1 late valid publication, transitive embed invalidation, session expiry with queued judge, source hash replacement with watcher loss, failed worker, offline network isolation, corrupted parser output, model unload/reload recovery. See [R1–R8 deterministic scenarios](revocation-and-derived-data.md).

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


## 9. Approved Alternative B Foundation verification profile

[B01–B20](alternative-b-verification-plan.md) replaces assumptions of live
revocation during reserved indexing, all-state startup invalidation, work-TTL
index expiry and strict physical-C deadline. Preserve observed-expiry/post-wait/
default-deferred denials and every unchanged security regression. Relevant edits
conflict until verified stop/release; no successful exclusion before consumption.
Later QA/model/judge tests above are allocation, not new implementation scope.
(Owner Decision 2026-10-17: narrow acceptance exception permits tracking `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` as an OPEN Authentication/environment issue outside Phase 17 RAG remediation when caused by Windows/WSL2 cross-clock skew with unchanged frozen code/assertions, accurate reporting, and all other Phase 17 tests passing; failed clean verify is never called PASS).

Bounds: 100 source nodes, depth 5, 1 MiB selected input; bounded stage/output/
history, one confined worker and no pending indexing queue. Retain initial total
3s lock/5s statement/10s transaction ceilings and configurable <=15m work lease.
Per-lock retries cannot multiply total request budget. Reservation has no TTL;
15m is neither a performance target nor completed-index retention. Remove 16-job,
12-of-16/F-hold/queue-age targets from current acceptance.

Use reproducible small, 100-source, depth-5 and near-1-MiB authorized fixtures;
five warmups and >=100 sequential cold job measurements per fixture. Record all
outcomes, requiring 100% normal authorized-fixture success and zero security
violations; denial-only workload cannot pass. Add below/at/above caps, simultaneous
two-start one-accept/one-conflict, repeated jobs/leaks, low disk and retained
tombstone growth. No deletion of history/uncertain active row to obtain capacity.

Capture supported desktop hardware/OS/Java/Python/PG/package/image/native versions,
fixture/config hashes, samples/concurrency/warmup/method and commands/exits. Measure
startup/input/stage/P latency p50/p95/max, guard/attempt wait/hold, stop response
versus verified death/seal/release, recovery/purge, batching/query counts,
CPU/private bytes/JVM/process-tree RAM, disk/handles/connections and zero Foundation
GPU/scratch use.

### 9.1 Measured Desktop Resource Acceptance Thresholds (420/420 Benchmark)

Empirical thresholds established from the verified 420/420 native benchmark executions (Windows 11 x64, Java 25.0.2, Python 3.14.4, PostgreSQL 18.6; 400 measured sequential cold-start single jobs + 20 warmups across four workloads, 100% successful jobs, 0 security/reservation violations; attributed to `logs/phase17-alternative-b-independent-re-review-2026-10-17/reused-independent-native-benchmark-results.json`, exit 0, 707.851s):

| Metric / Dimension | Measured p50 | Measured p95 | Measured Max | Acceptance Threshold | Disposition |
| --- | ---: | ---: | ---: | ---: | --- |
| Total job latency: small (1 node, 146 B) | 1,195.7 ms | 1,364.0 ms | 2,213.5 ms | <= 1,700 ms p95 | PASSED |
| Total job latency: max depth (5 nodes, 558 B) | 1,212.9 ms | 1,416.1 ms | 1,739.7 ms | <= 1,800 ms p95 | PASSED |
| Total job latency: max input (1 node, 1 MiB) | 1,287.0 ms | 1,656.5 ms | 1,753.1 ms | <= 2,000 ms p95 | PASSED |
| Total job latency: bulk sources (100 nodes, 10.8 KiB) | 2,129.5 ms | 2,900.8 ms | 5,029.1 ms | <= 3,500 ms p95 | PASSED |
| Cold worker startup + stage + stop | 1,040.0345–1,443.7374 ms | 1,168.9026–1,762.1168 ms | 2,993.9197 ms | <= 2,200 ms p95 | PASSED |
| Worker CPU (user + kernel) | 750.0–765.6 ms | 796.9–843.8 ms | 1,109.375 ms | <= 1,000 ms p95 | PASSED |
| Native worker committed memory | 49.4 MiB (p50) | 49.7–56.1 MiB (p95) | 56.53 MiB (max) | <= 64 MiB (max) | PASSED (cap 512 MiB, > 455 MiB headroom) |
| PostgreSQL client connections | 10 before | 10 at boundary | 10 after | Strictly 10 (0 leak) | PASSED (zero connection growth) |
| JVM process handles | 706 start | 688 end (net -18) | Range 688–755 | No handle growth | PASSED (zero handle leak) |
| Active source reservations / attempts at completion | 0 | 0 | 0 | Strictly 0 | PASSED (complete release) |

**Component timing, storage and headroom observations (from fixtures, probes & benchmark ledger)**:
1. *Admission & Publication round-trip upper bounds*: Admission round-trip (including reservation guard, dependency graph lock, lease initialization and run persistence): small-fixture p50 60.8 ms, p95 74.9595 ms, max 95.9 ms; global admission max across all four workloads is 1,302.6151 ms. Publication round-trip (including finalization gate verification, index generation commit, staging seal, and reservation release): small-fixture p50 94.9 ms, p95 133.1900 ms, max 258.9 ms; global publication max across all four workloads is 850.0647 ms. (Note: isolated SQL lock-hold durations are not factored separately in the benchmark schema; these full round trips conservatively bound those holds in the measured fixtures).
2. *Bounded native wait and exact process identity*: Win32 handle synchronization with exact creation FILETIME verification measured 202.3568 ms on a 200 ms requested timeout (`run_native_wait_probe.py`, exit 0, E/`native-wait-proof.json`), proving bounded live wait and fail-closed termination checking.
3. *Cancellation unproven-stop wait and terminal release*: Cancellation during active live consumer dispatch safely waits for positive termination across the 10,042.2651 ms deadline before reporting unproven stop, keeping staging unsealed and source reservations intact (`ReviewReservationProbe.java`, exit 0, E/`reservation-proof-v2.json`); upon positive worker quiescence, terminal CANCELLED commit, staging seal, and source reservation release complete in a measured 115.1269 ms round-trip.
4. *PostgreSQL storage growth*: Database size grew by 3,309,568 bytes (~3.156 MiB / 3.31 MB, from 12,859,071 bytes / ~12.26 MiB / 12.86 MB to 16,168,639 bytes / ~15.42 MiB / 16.17 MB) across 420 sequential jobs (~7.70 KiB retained metadata per completed job), consistent with immutable generation/audit history.
5. *Staging payload and memory headroom*: 1 MiB staging payload ceiling, typical fixture payloads <1 KiB (>99.9% headroom); native worker memory peaked at 56.53 MiB committed memory against the 512 MiB Windows Job Object ceiling (>455 MiB headroom / 89% headroom).
6. *Supervisor stop path and isolated stop latency*: Production `RagWorkerSupervisor` stop path directly invokes native tree termination (`terminateAndVerify` via Win32 `WaitForSingleObject`) and closes transport; the legacy `cancelWorkerJob` HTTP helper has no production caller and graceful HTTP stop duration is not claimed. The benchmark measures combined cold worker startup + staging + stop latency (1,040.0345–1,443.7374 ms p50); isolated stop-only duration is not factored separately in the benchmark schema.
7. *Resident working set vs committed memory*: Windows Job Object accounting tracks peak committed memory (max 56.53 MiB); sampled worker working set reached up to 71,688,192 bytes (~68.37 MiB).
8. *Storage failure simulation*: Verified deterministically at the database boundary via PostgreSQL SQLSTATE `53100` (`could not extend file: No space left on device`), verifying transactional rollback and reservation preservation without requiring physical drive fill.
9. *Strict safety invariant*: Unbounded uncertain recovery never falsely frees a reservation or bypasses terminal cleanup to satisfy an SLA.

FR17-10 requires actual job-owned endpoint/process path with successful enabled
work plus failure/cancel/recovery while authorized controls block external IPv4,
IPv6, DNS and proxy egress and record attempted connections. Distinguish app-level
tests from OS enforcement; allow necessary loopback/PG only. Verify no external
content/metadata/credentials, runtime downloads or telemetry; pin/audit actual
transitive packages/native licenses. Unavailable control/runtime proof is
NOT_VERIFIED, not a passed disabled-path smoke.

*(2026-10-16 Owner Amendment: The owner explicitly replaced strict OS-level WFP / elevated firewall enforcement with Practical Local Offline Operation. Requirements removed: Windows Firewall rules, WFP 5157 logging, UAC elevation, system DNS blocking, and global packet drops. Remaining requirements: local Java -> Python -> PostgreSQL execution, zero external API/cloud endpoints, no required Internet connectivity, pinned local dependencies, strict NSFW/Private exclusion, and valid crash recovery/cancellation. Verified via Python bound ingress/containment tests, local loopback integration tests, and static configuration audits. OS-enforced egress denial is no longer claimed.)*


After focused groups: `mvn -ntp clean verify` from backend; from pinned rag-engine
environment `python -m pytest -q`, `python -m ruff check .`,
`python -m ruff format --check .`, `python -m mypy src/ppv_rag`; architecture,
Flyway/effective roles, repository and applicable static checks. Preserve exact
counts/exits/failures, bounded timeouts, no lingering Maven/pytest/worker processes
and frozen auth assertions. No code/test execution is authorized by this docs pass.
