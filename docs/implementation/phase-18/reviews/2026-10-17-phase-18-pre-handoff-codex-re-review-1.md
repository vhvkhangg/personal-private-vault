# Phase 18 — Codex pre-handoff re-review 1

- **Date:** 2026-10-17
- **Result:** **CHANGES_REQUESTED** — one residual blocking contract inconsistency.
- **Baseline:** HEAD / `main` / remote `refs/heads/main` = `86b08a1a44d7672b111af1977098ef6c4c3e800a`; remote checked again with read-only `git ls-remote`.
- **Candidate:** 28 changed/new files before this report/status edits, including the original immutable review. Production Java/Python/tests, build/lockfiles, V1–V17 and ACTIVE are unchanged.
- **Scope:** independent re-review of ChatGPT's preparation remediation under `$codex-pre-handoff-review` and `codex-review-coverage`. This is not an implementation review or an Antigravity remediation round.
- **Previous report:** [initial preparation review](2026-10-17-phase-18-pre-handoff-codex-review.md), preserved unchanged.

The predecessor, owner concept and `NO_ACTIVE_HANDOFF` prerequisites still pass. The recorded subsequent owner approval of all eight D18-A clauses is recognized: see `owner-decisions.md:30–34` and ADR-0021. No repeat approval of the overall feature scope is requested. Phase 17 remains frozen, Phase 16 deferred, and Phase 18 implementation/live-file activation remain unauthorized.

## 1. Disposition of the original findings

| Finding | Result | Evidence / reason |
| --- | --- | --- |
| **P18-PH-01 — High** | **OPEN: residual lock-order contradiction** | D18-A approval, additive ADR/DBML/API, source identities, content staging, independent generation lifetime and one physical finish are now recorded. ADR-0021 clause 2 nevertheless reverses the retained source-before-policy/attempt order and leaves reconciliation to later. See section 2. |
| **P18-PH-02 — Medium** | **CLOSED for preparation** | `held-out-quality-and-locators.md:7–31` specifies source-family/dependency grouped dev/held-out separation before tuning, hashes, human labels, full slice denominators, mandatory format locators, failure/warning rules and measured defaults before acceptance. AC-04 and AC-02 agree. No LLM judge or full Cartesian dataset is demanded. Runtime results remain future evidence. |
| **P18-PH-03 — Medium** | **CLOSED for preparation** | `final-verification-gate.md:9–54` supplies exact five bounded commands, fail-fast PowerShell, one P18-FINAL aggregation and six focused receipt groups. It distinguishes command limits, real terminal states, report-only partial success and the nontransferable AUTH-CLOCK exception. PowerShell syntax parses without errors; commands were not executed. |
| **P18-PH-04 — Medium** | **CLOSED for preparation** | Actual checkout diffs for `backend/AGENTS.md` and `rag-engine/README.md` now describe frozen Phase 17, synthetic-only current worker, disabled live files and the separate preparation/handoff/takeover route. Existing setup commands and unrelated module instructions were preserved. The source ZIP's omissions did not silently replace production files. |

These are preparation closures, not claims that R7, OCR/embedding, subset publication or resource acceptance have passed.

## 2. P18-PH-01 residual — incompatible lock order in the approved-design transcription

**Severity:** High design/consistency blocker. **Location:** `docs/adr/0021-phase-18-bounded-multi-file-verified-indexing.md:15` (clause 2).

The new clause instructs acquisition of “RAG job/policy/attempt serialization” **before** Vault source guards, then says to reconcile if Phase 17 differs before implementation. Phase 17 does differ, and compatibility is required by the same ADR:

- Frozen `phase-17/revocation-and-derived-data.md:91–98`: graph barrier → sorted stable source-state IDs → Vault lifecycle → owner projection/assignment → sorted RAG policy → job/attempt → generation/staging/run. The attempt-only stop request is a separate transaction that ends before earlier-order operations.
- `RagSourceAuthority.java:147–164`: graph, then Vault source guards/reservations, then RAG source/policy rows. `lockPreparedAttempt` takes the attempt row only after that source proof.
- `RagSourceService.java:121–126,145–148,182–185`: approval/revocation obtains owner source guards before locking RAG source policy.
- `RagFinalizationGate.java:65–68` reuses `lockPreparedAttempt`; it does not move attempt locks ahead of source guards. The worker's attempt → staging path remains separate and takes no owner-source locks.

**Concrete consequence if implemented literally:** a legacy approval/finalizer can hold a Vault source guard while waiting for a RAG policy row; a new batch operation can hold that policy row while waiting for the same Vault guard. The proposed order introduces a backwards wait between real local paths. One active indexing job does not exclude concurrent approval/owner commands. This is an inconsistent future design, **not a claim that the unchanged production implementation currently deadlocks**. No destructive/runtime reproduction is necessary to establish the documented order mismatch.

**Smallest correction:** replace clause 2's order with one explicit extension of the existing order. A bounded compatible proposal is:

> Where needed, take the short RAG graph barrier first; then sorted stable Vault source-state guards; then sorted stable RAG-owned external-file source guards; then existing Vault lifecycle and owner projection/assignment guards; then sorted RAG policy rows; then job/attempt; then generation/staging/run. Preserve the relative order of all existing Phase 17 guards. Owner commands use their applicable subset. Workers retain attempt → staging only. A separate attempt-only stop-request transaction completes before any source/process action. Retain the existing NOWAIT backstop for triggers already holding later-order target rows; never wait backwards. No digest, OCR/model, HTTP or process wait runs under SQL row locks.

The new external-file guard placement is a proposed additive slot; use one consistent order in the ADR, API/DBML notes and the eventual handoff. Remove the unresolved “reconcile before implementation” escape hatch once the preparation states the actual compatible order. No extra coordinator, global lock table or frozen Phase 17 rewrite is needed.

**Authority:** the owner's original D18-A approval requires preservation of Phase 17 fences and a documented deterministic mixed order; it did not authorize incidental reversal of the existing order. Correcting the transcription to preserve that order stays within the approved intent. If a different order is intentionally desired, present its exact affected-path exception for explicit owner approval instead of treating the existing approval as permission.

**Proof required to close preparation:** verify the corrected sequence against the named existing source/approval/finalization/worker/stop paths and synchronize any repeated ordering statements. Documentation/source comparison is sufficient at this gate. **Future implementation proof:** reuse focused real PostgreSQL barrier tests for mixed batch admission/finish versus source approval/revoke/dependency mutation in both orders, plus stage-writer/seal and rollback/unknown-C behavior. Do not add an independent benchmark subsystem or rerun full Maven for a documentation correction.

## 3. Proportionality, completeness and retained runtime gates

| Feature / API / dependency / evidence | Disposition | Current-phase justification |
| --- | --- | --- |
| All selected TXT/MD/Obsidian/DOCX/native/scan/mixed PDF/XLSX/XLS and VI/EN/Hans/Hant | **KEEP** | Owner scope; positive and unsafe fixtures by meaningful supported slice, no format/language removed. |
| RAG-owned external authority and native R7 | **KEEP** | Tagged identity avoids fake Vault IDs; actual NTFS source lifetime through physical C is mandatory before enablement. |
| Multi-root union reservation / per-generation closure / one finish | **KEEP**, with **PH-01 correction required** | Approved D18-A; independent A/B versus true-child lifetime and exact whole-union release, no early publication. |
| Additive DBML records | **SIMPLIFY physical representation where useful** | Logical responsibilities may consolidate in existing RAG owners; proposed `_phase18` names are not five mandatory new production tables. Append after V17; fixed selected vector dimension and restricted audited staging. |
| Typed v3 document protocol / existing v2 single-root API | **KEEP** | Strict discrimination, legacy compatibility, no worker path refetch/publication rights or new shared envelope. |
| One normalized representation and structure-first chunker | **KEEP / SIMPLIFY** | Stable actual locators, minimal format handling; no parser plugin platform or LLM chunker. |
| Native-text-first OCR / one targeted retry | **KEEP** | Current fidelity and resource needs; one viable pipeline, second engine only for a demonstrated gap. |
| One embedding model / one same-model CPU fallback | **KEEP** | Verified indexing now; artifact/dimension/tokenizer identity and finite vectors. GPU/CPU feasibility is measured before promotion. |
| Dependency/runtime selection | **KEEP measured selection; DEFER final pins until justified** | Reuse the same-day primary package metadata and frozen baseline from the initial report. No new dependency choice/upgrade/install occurred; candidate ABI/CUDA/license compatibility remains a required measured gate. |
| Source-grouped held-out ingestion evaluation | **KEEP / SIMPLIFY** | PH-02 is remediated; small human/deterministic oracle, no LLM judge, corpus quota or Cartesian expansion. |
| Resource/quality thresholds | **KEEP** | Freeze finite measured defaults before acceptance; separate 16 GB system RAM/process-tree headroom from 8 GB VRAM, honest library buffering/streaming, time/temp bounds. |
| P18-FINAL and focused receipts | **KEEP** | One complete final gate after focused convergence, retained receipts; no full suite per internal slice. |
| Retrieval/reranker/QA/judge/recommendation quality | **DEFER** | Phase 19/20; Phase 18 preserves locators and verifies vectors, without building later consumers. |
| Extra agents/hooks/queue/model registry/dashboard/420-job replay/enterprise/WFP controls | **REJECT** | No demonstrated new owner need; existing implementer, Python/RAG skills, watchdog and independent takeover path suffice. |
| Intentional change to frozen source-first order or unreviewed limits | **OWNER DECISION REQUIRED** | Not authorized by general design approval. Recommended repair preserves the existing order and scope. |

**Implementation guard against vacuous quality results:** carry AC-01's positive supported-format cases through actual successful published indexes when producing P18-QUALITY/P18-PUBLISH-PG. The “no material misread among published files” rule is not permission to pass all scope by rejecting every valid input. Record valid inputs rejected and useful successful coverage alongside the existing full denominators. This clarifies already approved functionality; it does not add a corpus-size, success-rate or broad new benchmark requirement and is not a new preparation blocker.

Unknown finish acknowledgement must be reconciled against durable state: an already physically committed `COMPLETED` row must not be rewritten to `RECOVERY_REQUIRED`. Read the new design's unknown-outcome wording with its explicit durable-reconciliation rule and the existing terminal-state immutability. This is a future changed-boundary test, not a new state machine.

## 4. Review inventory and coverage ledger

**New/remediated documents read in full:** ADR-0021, `docs/database/phase-18-document-indexing.dbml`, `docs/architecture/rag-phase-18-api-extension.md`, Phase 18 `held-out-quality-and-locators.md`, `final-verification-gate.md`, `README.md`, `owner-decisions.md`, `acceptance-and-evidence.md`, `preparation-review.md`, `reviews/README.md`.

**All changed tracked paths reviewed:** `.agents/README.md`, implementer agent, Antigravity implementation/handoff-creation skills; root `AGENTS.md`/README; `backend/AGENTS.md`; docs README/ADR index/API architecture/RAG architecture/database index/implementation index/RAG plan/roadmap; `rag-engine/README.md`; watchdog test. Original candidate changes remain within the initial report's inventory; new architecture/index and scoped-operating diffs were inspected against actual HEAD. The original report is historical, not a remediation target.

**Applicable existing coverage retained:** the initial report's exact inventory of `codex-review-coverage`, review/create/final/takeover skills, Python/RAG/bounded-command/general Java/architecture/persistence/testing/API skills; both agent roles; applicable workflow/security/package/Python/Java/test/docs/migration rules; safety hooks/config; root/docs/backend/test/Vault/Fiction/Film instructions; ADR-0013/0016/0019/0020, P17-1/2/3/N1, frozen module/package/API/DBML inventories, phase plan/closeout/final acceptance, delivery policy and prompt placeholder. Unchanged large files were not reread wholesale. Targeted unchanged source-order paths above were rechecked. `.codex/config.toml` was reparsed and preserved. No new role/hook/skill was added or needed; takeover still requires two actual Antigravity remediation + independent re-review rounds and an independent post-takeover reviewer. No subagent was used.

The same recursive inventory is present: **69** Java RAG files, **29** Java RAG test/support files, **8** Python source files and **7** Python test files, excluding caches; all **12** Java package descriptors exist, no stale `.gitkeep` in those trees. Existing topology/shared HTTP ownership and selected source/test responsibilities remain as verified by the initial review; this is not a second full production audit. Graphify navigation from that review is reused only for unchanged source, while new preparation is read directly as authority.

Unrelated historical feature domains/skills/agents, frontend/deployment, new authentication remediation and the closed Phase 15 audit are **N/A**. Full Schema v1 re-audit is **N/A** because it is unchanged; the new RAG-owned logical delta is in scope. IDE diagnostics and new runtime behavior are **NOT_VERIFIED**, not implicit passes.

The following statuses evaluate preparation coverage; future-runtime obligations are listed separately below.

| # | Dimension | Status / evidence |
| --- | --- | --- |
| 1 | Owner intent/non-goals | **PASS** — original scope retained, D18-A approval recorded, no later-phase authorization. |
| 2 | Traceability | **FINDING** — only PH-01 order compatibility remains unresolved; PH-02/03 plans are concrete. |
| 3 | Architecture consistency | **FINDING** — ADR-0021:15 versus frozen P17-3/source order. |
| 4 | Module/transaction ownership | **FINDING** — new source ownership/edges are sound in scope, but mixed lock order needs correction. |
| 5 | Folder/package structure | **PASS** — full relevant inventories/descriptors unchanged; existing owners reused. |
| 6 | Shared API/exceptions | **PASS** — root ApiResponse and RAG-local DTO/advice preserved; no shared-module invention. |
| 7 | SOLID | **PASS** — source/lifecycle ownership reused; actual new implementation remains unverified. |
| 8 | DRY/KISS/YAGNI | **PASS** — one pipeline, model and chunker, no duplicate coordinator. |
| 9 | Patterns | **PASS** — real format/library boundaries only, no speculative framework. |
| 10 | Business/state correctness | **PASS** — per-file versus job state, independent CURRENT and durable-reconciliation requirements specified. |
| 11 | API compatibility | **PASS** — additive batch/report/v3, preserved v2, unknown/mixed-version denial planned. |
| 12 | Input validation | **PASS** — typed bounds, dangerous document/path/archive handling and mandatory locators planned. |
| 13 | Consent/privacy | **PASS** — per-file/field/child approval, R7 fail-closed gate and exclusions retained; execution proof later. |
| 14 | Secrets/egress/purge | **PASS** — no runtime downloads, sensitive chunks/vectors/hash handling and transitive cleanup required. |
| 15 | Locks/atomicity/concurrency | **FINDING** — PH-01; one final physical COMMIT otherwise explicit. |
| 16 | Failure/recovery | **PASS** — writer/process stop, unknown-C reconciliation and per-generation cleanup obligations explicit; lock-order fix still applies. |
| 17 | Database | **PASS** — logical RAG-only additions after V17, no old migration rewrite; actual constraints/roles/DDL remain future tests. |
| 18 | Complexity/resources | **PASS** — bounded measured admission and separate RAM/VRAM, no invented unbounded streaming. |
| 19 | Index/retrieval quality | **PASS** — model/vector/chunk identity required; ANN/retrieval/recommendation scoring **N/A** here. |
| 20 | Document/OCR fidelity | **PASS** — PH-02 oracle has exact format-specific locators, labels and warning/failure rules. |
| 21 | Security tests | **PASS** — focused real NTFS/PG/process races and negative grants retained in named receipts. |
| 22 | Test quality | **PASS** — independent held-out oracle and complete gate; no fixed test-count target. |
| 23 | Benchmarks | **PASS** — small target-host selection/resource evidence, no replayed 420-job burden. |
| 24 | Evaluation science | **PASS** — grouped disjoint dev/held-out, hashes/denominators/adjudication; LLM judge calibration **N/A** in P18. |
| 25 | Dependencies/lifecycle | **PASS** — baseline unchanged; same-day metadata reused, actual candidate compatibility/pins/artifacts gated before use. |
| 26 | Build/tooling/diagnostics | **PASS** for preparation definition — P18-FINAL parses; earlier unchanged 5/5 and 13/13 evidence retained. Full builds/IDE checks **NOT_VERIFIED** this review. |
| 27 | Logging/reporting | **PASS** — sanitized per-root outcome/resource report, historical outcome versus live CURRENT separated. |
| 28 | Docs/governance | **FINDING** — PH-01 ADR clause; PH-04 scoped status fixed, local link targets resolve. |
| 29 | Hygiene/delivery | **PASS** — diff check, unchanged production/build/lock/migration/ACTIVE, ignored receipts, immutable original report. |

## 5. Verification performed and limits

**Fresh read-only checks:** `git diff --check` PASS; changed/new Markdown local target existence PASS; PowerShell AST parse of the exact P18-FINAL fenced script **0 errors**; TOML syntax PASS; remote `main` equals baseline; recursive source/test inventory and production/build/lock/ACTIVE diff checks confirm no implementation change. Link-anchor/remote-page crawling and IDE inspection were not claimed.

**Reused, not rerun:** the initial review's native Windows/CPython 3.14.4 watchdog **5/5 PASS** and safety hooks **13/13 PASS**, with original receipts at `logs/phase18-pre-handoff/{watchdog-windows,safety-hooks}.json` (both exit 0, 2026-10-17). The runner/test and runtime lock hashes are unchanged:

- watchdog test `5e867559a99f1c58384080b5a97c9a609585263985f07af5cdf49548c59ef586`;
- runner `e9643978b8dc708b86b4014bc83805e44336fe16fee796c901c564d69cdf058d`;
- `uv.lock` `6902a8569ec299bd26f6fd10d030055124cdcc00c6682412f379c02b95b241ae`.

No Maven, pytest, ML/model benchmark, install/lock regeneration, native R7 activation or production command was run. Static PowerShell parsing does not prove executables/environment or runtime acceptance. Prior package metadata is selection context, not CUDA/RTX 2070 feasibility proof.

**Future mandatory acceptance, still NOT_VERIFIED:** native Windows R7 through physical C; actual batch/role/worker staging/finalization/migration/lock tests; true independent per-generation CURRENT/purge; parser/OCR/chunker/model/vector quality with positive published supported inputs; held-out labels/disjointness/results; offline artifacts/ABI/driver compatibility; 16 GB RAM/process-tree headroom versus 8 GB VRAM measurements and finite caps; all five P18-FINAL commands and its focused receipts. These are already planned gates, not reasons to invent preparatory runtime success. Keep the real 1,200-second watchdog, 2,100 seconds only for approved full suites when the outer host permits; never waive a failure using Phase 17's unrelated AUTH-CLOCK exception.

## 6. Result / next step

**CHANGES_REQUESTED** solely for residual **P18-PH-01**. P18-PH-02/03/04 are closed at preparation level. Preserve the original review and accepted owner intent. No production code, ADR contract, handoff, Git commit or push was changed by this review; only review/status artifacts are written. ACTIVE remains `NO_ACTIVE_HANDOFF`, with containment and live-file denial intact.

**Next step:** give this report and the latest package to ChatGPT for the finite source-first lock-order correction in preparation, then rerun `$codex-pre-handoff-review`. A passing later review must precede owner preparation commit/push and a separate `$codex-create-handoff`.
