# Phase 18 — Codex pre-handoff re-review 2

- **Date:** 2026-10-17
- **Verdict:** **READY FOR HANDOFF** — no blocking preparation findings remain.
- **Baseline:** HEAD / `main` / remote `refs/heads/main` = `86b08a1a44d7672b111af1977098ef6c4c3e800a`; read-only remote verification repeated.
- **Scope:** review-only acceptance of the Phase 18 preparation and owner-approved D18-A design, not production implementation or runtime certification.
- **Candidate:** 29 changed/new files before this report/status edits; the only residual design repair since [re-review 1](2026-10-17-phase-18-pre-handoff-codex-re-review-1.md) is source-first ordering in ADR-0021, its API inventory and additive DBML. Both earlier reports remain immutable.
- **Implementation rounds:** zero. Two preparation re-reviews do **not** constitute the two Antigravity implementation remediation rounds or authorize takeover.

## 1. Prerequisites and finding closure

Phase 17 is accepted, owner committed/pushed and frozen. ACTIVE remains `NO_ACTIVE_HANDOFF`. Phase 18 concept/preparation and all eight original D18-A clauses have recorded owner approval. Phase 16 remains deferred; Phase 15 is not reopened and no historical milestone gate is newly due.

| Finding | Final preparation disposition | Evidence |
| --- | --- | --- |
| **P18-PH-01 — High** | **CLOSED** | ADR-0021 clause 2 now preserves graph → stable Vault source guards → new sorted RAG external-file source guards → Vault lifecycle → owner projection/assignment → sorted RAG policy → job/attempt → generation/staging/run. API inventory §“Mixed-source lock compatibility” and DBML header repeat the same order. The unresolved order-reconciliation clause is removed. |
| **P18-PH-02 — Medium** | **CLOSED, retained from re-review 1** | Source-grouped dev/held-out protocol, hashes, labels, complete denominators and mandatory per-format locator/failure oracle remain in `held-out-quality-and-locators.md` and AC-02/04. |
| **P18-PH-03 — Medium** | **CLOSED, retained from re-review 1** | `final-verification-gate.md` retains one P18-FINAL with five exact bounded commands, six linked focused receipts, real terminal-state mapping, fail-fast aggregation and no inherited AUTH-CLOCK waiver. |
| **P18-PH-04 — Medium** | **CLOSED, retained from re-review 1** | Root/scoped backend/worker status correctly distinguishes accepted frozen Foundation, Phase 18 preparation and later implementation. Default containment/live-file denial remains. |

The PH-01 correction was compared again to frozen P17-3 §1A.3 and unchanged `RagSourceAuthority.lockPrepared/lockPreparedAttempt`, `RagSourceService` approval/guard paths and the established finalizer/worker boundaries. Existing guard relative order is preserved. Only an external-file guard slot is added before downstream policy/attempt locks. Workers retain attempt → staging without owner-source locks; attempt-only stop requests complete before source/process actions; later-row trigger paths retain NOWAIT rather than waiting backwards. Hash/document/model/HTTP/process work stays outside SQL row-lock intervals. This is the bounded compatibility repair requested by re-review 1, within approved D18-A preservation intent; no further broad owner approval is needed.

No unresolved actionable preparation finding, frozen-baseline conflict or unapproved scope extension was found. This conclusion does not assert that not-yet-existing implementation passes its runtime gates.

## 2. Proportional scope and handoff constraints

| Feature / API / dependency / evidence | Disposition | Bounded requirement |
| --- | --- | --- |
| All selected TXT, MD/Obsidian, DOCX, native/scan/mixed PDF, XLSX/XLS; VI/EN/Hans/Hant | **KEEP** | Owner-approved formats/languages; positive useful supported inputs and unsafe fixtures by meaningful slice, without a Cartesian dataset or separate phase per format. |
| Tagged external authority, consent and R7 | **KEEP** | RAG-owned external identities/history/reservations; native NTFS proof through physical C before live enablement, no fake Vault ID or arbitrary worker refetch. |
| Multi-root reservation, per-file generations, final Java publication | **KEEP** | One job/worker, union reserved before input; generation-specific actual closure; all validated successes or none in one terminal/seal/release COMMIT. |
| Source-first mixed guard order | **KEEP** | Corrected shared order; reuse existing guard/fence APIs, no new coordinator/global lock table. |
| Additive storage/API/v3 worker contract with v2 compatibility | **KEEP / SIMPLIFY** | RAG-owned logical responsibilities may consolidate physically; `_phase18` names are not a required five-table implementation. Append after V17, preserve legacy API/receipts and root HTTP envelope. |
| Structure-first chunks and actual format adapters | **KEEP / SIMPLIFY** | One normalized representation/chunker, exact source attribution/locators and measured tokenizer limits; no speculative plugin platform. |
| Native-text-first OCR / one targeted retry | **KEEP** | One viable selected offline stack. Compare a second candidate only for a concrete language/table gap. |
| One multilingual embedding model / one same-model CPU fallback | **KEEP** | Finite valid dimension/model/tokenizer/artifact binding; small local feasibility measurements before promotion. No mixed vector spaces. |
| Parser/model/runtime dependencies | **KEEP measured selection** | Same-day primary candidate metadata from the initial review remains navigation evidence; no new selection/install/pin/upgrade. Windows 3.14/native/CPU/GPU/driver/license compatibility must be demonstrated, not inferred. |
| Held-out fidelity and mandatory locators | **KEEP / SIMPLIFY** | Small sanitized source-family split, human/deterministic references, failures/skips/full denominators and positive published supported inputs. No LLM judge in Phase 18 or invented corpus quota. |
| RAM/VRAM/time/temp/input limits | **KEEP** | Small Windows/i5-9600K measurements; 16 GB system RAM/process-tree headroom separately from RTX 2070 8 GB VRAM. Finite measured caps, honest library buffering, safe failure. |
| Final verification | **KEEP** | Focused text/Obsidian → Office → PDF/OCR → verified indexing slices, then one P18-FINAL after convergence; reuse unchanged accepted receipts. |
| Retrieval/reranker/ANN/QA/judge/recommendations | **DEFER** | Phase 19/20; Phase 18 verifies document indexes, vectors and sufficient source locators. |
| Extra agent/hook/queue/model registry/dashboard, 420-job repeat, enterprise/WFP project | **REJECT** | No current need. Existing implementer, Python/RAG skills and bounded runner suffice; no duplicated job engine. |
| Future relaxation of privacy/source/process/E1/frozen limits or broader scope | **OWNER DECISION REQUIRED if proposed** | None is requested or approved by this gate. Limits/model defaults must follow the documented measured approval path before activation. |

The existing `backend-implementer`, `python-rag-coding`, `local-rag-engineering`, `bounded-command-execution` and independent final-review/takeover route are sufficient. No new custom agent or hook is justified. The eventual handoff must carry the named gates and explicit runtime NOT_VERIFIED obligations below, rather than treating this review as their proof.

## 3. Review inventory and 29-dimension ledger

**Fresh correction inspection:** full ADR-0021, `docs/architecture/rag-phase-18-api-extension.md` and `docs/database/phase-18-document-indexing.dbml`; Phase 18 preparation status; exact corresponding P17-3/source-order paths. Historical review wording is retained as historical evidence, not searched as an operative order requirement.

**Complete candidate/applicable inventory retained and checked for scope drift:** all changed paths from the [initial report](2026-10-17-phase-18-pre-handoff-codex-review.md#4-review-inventory-and-29-dimension-coverage) and [re-review 1](2026-10-17-phase-18-pre-handoff-codex-re-review-1.md#4-review-inventory-and-coverage-ledger): root/docs/backend/test/Vault/Fiction/Film instructions; `.agents/README.md`, both agent roles, applicable workflow/review/create/final/takeover, Python/RAG/watchdog, Java/architecture/persistence/test/API/SOLID/reuse skills and rules; safety hooks/config and `.codex/`; scoped worker/backend operating docs; scripts/test changes; README/roadmap/RAG plan and canonical ADR/architecture/API/database/package/module inventories. Newly added Phase 18 scope, owner decisions, acceptance, held-out, final gate and review records remain as independently reviewed in the earlier passes. Large unchanged applicable files were not redundantly reread. No new production/build/lock/migration change appears in the candidate.

Fresh recursive inventory still finds 69 Java RAG files, 29 Java RAG test/support files, eight Python source files and seven Python test files, excluding caches. All 12 Java RAG package descriptors remain; no stale `.gitkeep` occurs in these trees. Existing `rag/api`, `rag/source`, module-local internals/web and `ppv_rag` ownership is preserved. ADR-0016 root transport types are reused; no generic shared module or cosmetic package refactor is required. Graphify navigation from the original source review is reused only for unchanged source; new preparation is verified directly.

Unrelated historical feature domains/skills, closed Phase 15 audit/milestone work, new authentication remediation, frontend/deployment and future QA/recommendation/LLM-judge implementation are **N/A**. Schema v1 re-audit is **N/A** because it remains unchanged; the additive Phase 18 logical target was reviewed. No subagent was used.

The table assesses **preparation coverage**, not runtime success:

| # | Dimension | Status / evidence |
| --- | --- | --- |
| 1 | Owner intent/non-goals | **PASS** — approved scope retained and later phases excluded. |
| 2 | Traceability | **PASS** — P18 decisions → ADR/API/DBML → AC-01–08 and focused/final gates; all four findings closed. |
| 3 | Architecture | **PASS** — corrected source-first order, additive design distinct from frozen deployed Foundation. |
| 4 | Module/transaction boundaries | **PASS** — existing Java edges/public owners; external authority stays RAG-owned; worker restricted. |
| 5 | Package/folder structure | **PASS** — recursive inventory/descriptors; no stray `.gitkeep` or new technical-root package. |
| 6 | Shared API/exceptions | **PASS** — ADR-0016 root envelope and module-local DTO/advice reused. |
| 7 | SOLID | **PASS** — real source/parser/chunk/model/lifecycle responsibilities; no speculative abstractions. |
| 8 | DRY/KISS/YAGNI | **PASS** — one pipeline/model/chunker and reused job/process authority. |
| 9 | Patterns | **PASS** — real format/library adapters only, framework/queue/registry rejected. |
| 10 | Business/state correctness | **PASS** — file outcomes versus job terminal state; independent CURRENT, failed replacement and durable reconciliation specified. |
| 11 | API/wire compatibility | **PASS** — additive typed batch/report/v3, unchanged v2 and standard HTTP envelope. |
| 12 | Validation | **PASS** — finite typed payload/vector/input bounds, unsafe format/path/archive/dependency cases and mandatory locators planned. |
| 13 | Consent/privacy | **PASS** — explicit file/field/child grants, Private/NSFW/unknown exclusion and R7 enablement gate retained. |
| 14 | Secrets/egress/retention | **PASS** — practical offline/no runtime downloads, sensitive chunks/vectors/hashes and transitive temp/content cleanup. |
| 15 | Locks/atomicity | **PASS** — shared corrected guard order, attempt-through-stage-C fence and one publication/terminal/seal/release C; runtime proof later. |
| 16 | Failure/recovery | **PASS** — positive native stop, retained uncertainty, durable outcome reconciliation and scoped cleanup planned. |
| 17 | Database | **PASS** — RAG-owned logical additions, V18+ append-only, old migrations/Schema v1 untouched; physical constraints/roles still require tests. |
| 18 | Complexity/resources | **PASS** — measured finite bounds and separate RAM/VRAM evidence; no unsupported unlimited streaming claim. |
| 19 | Index/retrieval quality | **PASS** — vector/model/tokenizer/chunk checks; retrieval/ANN/recommendation metrics **N/A** here. |
| 20 | Document/OCR fidelity | **PASS** — format-specific text/table/geometry/child locator oracle and language coverage. |
| 21 | Security tests | **PASS** — deterministic real NTFS/PG/process race/negative-grant obligations in focused receipts. |
| 22 | Test quality | **PASS** — source-grouped independent oracle, positive supported behavior and complete failure accounting. |
| 23 | Benchmarks | **PASS** — small target-host feasibility/resource runs; no arbitrary 420-job replay or invented tiny-sample p95. |
| 24 | Evaluation science | **PASS** — frozen dev/held-out split, labels/hashes/denominators; LLM-judge calibration **N/A** in P18. |
| 25 | Dependencies/lifecycle | **PASS** — frozen baseline/pins preserved, primary metadata retained; selected artifacts/ABI/license evidence gated before use. |
| 26 | Tooling/diagnostics | **PASS** for preparation — P18-FINAL parses, real bounded runner/evidence reused; IDE/full-runtime checks **NOT_VERIFIED** here. |
| 27 | Logging/reporting | **PASS** — sanitized per-file/job report, resource and live CURRENT distinction, no raw paths/text. |
| 28 | Documentation/governance | **PASS** — ADR/API/DBML order synchronized, four findings closed; current status synchronized by this review. |
| 29 | Hygiene/delivery | **PASS** — whitespace/local-target/TOML checks, no production/ACTIVE changes; historical reports preserved. |

## 4. Actual verification and future acceptance

Fresh `git diff --check` PASS; candidate Markdown local file targets resolve; `.codex/config.toml` parses; the exact P18-FINAL PowerShell block has **zero AST parse errors**. The remote baseline matches, and source/build/lock/migration/ACTIVE diffs confirm no implementation change. Anchor/remote-page validation and IDE inspection are not claimed.

The earlier independent native Windows CPython 3.14.4 watchdog **5/5 PASS** and safety-hook **13/13 PASS** remain creditable unchanged evidence, not new executions. Original receipts: `logs/phase18-pre-handoff/watchdog-windows.json` and `safety-hooks.json`, exit 0, 2026-10-17. Hashes rechecked unchanged:

- watchdog test: `5e867559a99f1c58384080b5a97c9a609585263985f07af5cdf49548c59ef586`;
- bounded runner: `e9643978b8dc708b86b4014bc83805e44336fe16fee796c901c564d69cdf058d`;
- worker lock: `6902a8569ec299bd26f6fd10d030055124cdcc00c6682412f379c02b95b241ae`.

No Maven/pytest/static Python suite, ML benchmark, dependency installation or live-file activation was run in this review. No Antigravity implementation evidence exists for Phase 18. Previous package metadata is not a runtime CUDA/RTX 2070 compatibility proof.

**Mandatory future handoff/final-acceptance conditions, NOT_VERIFIED now:**

- Actual R7 native NTFS identity/root/reparse/hard-link/pre-existing writer/mapping and source lifetime through physical Java C; live files remain disabled until independently accepted proof.
- Actual mixed-source admission/approval/revoke/dependency/finalization ordering, stage-writer versus seal in both orders, exact union reservation/release, restricted-role content staging, rollback/lost-ACK reconciliation and independent per-generation CURRENT/purge.
- Positive useful published supported formats/languages, held-out source-group disjointness/labels/full results, exact mandatory locators, one model/vector/tokenizer/parser identity, bounded retry/fallback and no source write-back.
- Selected Windows ABI/driver/CPU/GPU/offline artifacts/license hashes; separately measured process-tree/system RAM/host headroom and VRAM, finite measured limits approved before activation.
- All five commands and linked focused receipts in **P18-FINAL** after focused convergence. A missing/failed required component is not PASS; AUTH-CLOCK is not automatically waived.

Long commands use the actual watchdog: 1,200 seconds ordinarily, 2,100 only for owner-approved complete suites when the outer host allows it. Retain the two actual Antigravity implementation-remediation rounds, then separately authorized Codex takeover with an independent reviewer; these preparation reviews do not consume that budget. No extra suite/benchmark is requested for unchanged documentation.

## 5. Acceptance and owner action

**READY FOR HANDOFF** for the Phase 18 preparation/docs/tooling slice. **P18-PH-01–04 are CLOSED for preparation.** No blocking finding remains. This is not permission to implement without a new handoff, enable live sources, remove containment, deploy or enter later phases. ACTIVE remains `NO_ACTIVE_HANDOFF`; agents did not commit/push/tag or create a PR.

Suggested preparation commit message:

```text
chore(phase-18): prepare bounded document indexing
```

**Next step:** the owner commits/pushes the accepted preparation using that message, then invokes `$codex-create-handoff` separately. No implementation handoff was created automatically.
