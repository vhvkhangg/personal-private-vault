# Independent pre-handoff review — Phase 19 preparation

- **Date / reviewer:** 2026-10-27 / Codex, independent of the submitted preparation authorship.
- **Final status: CHANGES_REQUESTED.** Four OPEN preparation findings: one HIGH and three MEDIUM. No implementation handoff, implementation acceptance or activation follows.
- **Mode / authority:** numbered-phase **preparation review only**, requested by the Owner through $codex-pre-handoff-review. The [submitted scope record](../owner-decisions-and-scope.md) records preparation decisions; this invocation does not approve production implementation or amend a frozen contract. [Current Owner policy](../../../workflow/current-owner-delivery-policy.md) and [Docs Sync](../../../workflow/documentation-synchronization.md) remain controlling.
- **Baseline:** local HEAD, local main and observed remote main equal **685b869a9d789c6e0a42198d1f59bce62b7e0613**. No tracked or staged changes at entry; six untracked P19 preparation files, exactly matching the author's manifest. Source, tests, migrations, scripts, hooks, configs and accepted ADRs match HEAD.
- **Predecessor / routing:** Phase 18 implementation **3158f402c7dba9c29a5276d5ffb3a9349e022f55** is an ancestor of HEAD; [D27 closeout](../../phase-18/phase-18-final-owner-closeout-2026-10-25.md) records COMPLETE/CLOSED/OWNER-PUBLISHED. Phase 17 remains FROZEN. The next three-phase RAG milestone is **after P19**, not a prerequisite invented before P19. [ACTIVE](../../handoffs/ACTIVE.md) remains **NO_ACTIVE_HANDOFF**.
- **Counters:** initial preparation review; no implementation remediation round consumed. P19 Antigravity remediation **NOT_STARTED (0/3)**; no takeover authorized. Historical B1/B2 3/3, accepted B2 takeover 1/2 and Phase 17/18 counters are unchanged.

## Findings

All four findings concern the submitted preparation, not a claim that an already deployed Q&A path failed. Repository-relative path:line locations refer to the inspected entry files.

### P19-PR-01 — HIGH / OPEN / blocking: the consumer lifetime is still an unresolved pre-handoff gate

**Evidence:** source-reuse-and-feasibility.md:12–14, 26, 45 and preparation-review.md:22 explicitly require a reviewed lifetime before handoff but provide no selected mechanism. In current source, RagGenerationState.java:114–124 completes a short transaction and returns a status; its source/generation locks end there. RagSourceAuthority.java:242–315 supplies short ordered guards. RagWorkerSupervisor.java:67–109 binds a worker to an admitted indexing attempt, one-use dispatch and an ingestion memory/process profile. These are not a Q&A consumer registration or a response lifetime.

**Concrete consequence:** calling state(), then retrieving/generating, then calling state() again does not protect source consumption between those checks or order a revoke occurring after the final check but before response emission. Revoke/purge can remove stored derivatives without proving a new model consumer has stopped. P17-3 §3.4 explicitly leaves this later-consumer protection unaccepted; its indexing receipts cannot discharge it.

**Smallest correction:** ChatGPT must document one source-backed, bounded lifetime contract covering admission, every contributing child/revision, model/context consumption, final authorization/response boundary, revoke/reset/update/purge, cancellation, restart and unknown termination. Compare reuse of durable source exclusion/cancel-first behavior with a bounded stop-before-revoke approach; state the actual Owner-visible busy/cancel behavior and the precise release/linearization point. Do not hold SQL locks across inference or client delivery, invent authority from a TTL, or treat a late status check as the mechanism. Any change to accepted mutation semantics or additive frozen persistence requires a precise prospective decision/ADR for Owner approval.

**Proof for correction / later implementation:** preparation must resolve the mechanism and identify its source owners and finite race cases. Later implementation must test both actual PostgreSQL commit orders and barriers before consumption, during inference and between final authorization and emission, including a revoked transcluded child, reapproval with a new epoch and an unproven process stop. This review did not run those experiments.

### P19-PR-02 — MEDIUM / OPEN / blocking: privacy wording does not distinguish source denial from internal file markers

**Evidence:** owner-decisions-and-scope.md:18 says to deny Private/NSFW content; acceptance-matrix.md:28 combines that wording with unapproved/revoked Markdown. Neither states the whole-file exception. [ADR-0022 N6, WF-1/WF-6 and currentness](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), qualified by [ADR-0023 G1.3](../../../adr/0023-phase-18-application-level-snapshot-worker-trust.md), permits internal Private/NSFW/hidden markers inside an individually approved external snapshot. The current Owner policy retains this exception.

**Consequence:** an implementer could satisfy Q20 by filtering internal Markdown markers, silently changing accepted consent semantics and losing approved evidence. A PRIVATE/UNCLASSIFIED source registration is a different authority condition from private-marked text inside an approved whole file.

**Smallest correction:** explicitly retain source-level denial and whole-file internal-content consent as separate cases throughout R19-10/Q20. Add the paired positive case for a correctly approved/current marked file and negative cases for an unapproved/private-classified source and unapproved child. Do not infer consent from a marker or remove existing source guards. This clarification needs no new Owner choice; categorical exclusion of approved file content would need a new decision.

### P19-PR-03 — MEDIUM / OPEN / blocking: canon/folder selection has no complete identity and transclusion contract

**Evidence:** owner-decisions-and-scope.md:15–16, 29–30 and source-reuse-and-feasibility.md:21–22, 47 propose folder/group selection but leave its mapping unverified. Current captured authority carries a source UUID, source_name and lexical suffix digests (RagSourceAuthority.java:149–184; RagOriginalBytesStore.java:478–500). It has no readable folder-group/canon flag. document_source.py:67–113 uses those digests to resolve supplied embed targets, not to discover CORE membership. Chunks preserve the actual contributing source identity and child locators, independently of the root generation.

**Concrete counterexample:** an approved CORE parent can contain an approved non-CORE transclusion. Filtering only the parent's generation admits that child's noncanon evidence. Conversely, a global CORE filter incorrectly removes ordinary Markdown from generic Q&A. Q14/Q16 do not specify the outcome for this combined case.

**Smallest correction:** define the simplest explicit Owner-supplied source-ID selection and canon/noncanon assignment, how it is bound to the selected snapshots, and how every chunk's actual contributing source is intersected with that selection **before ranking, context construction and metadata output**. Define approved dependencies versus query-selected evidence separately. Use existing source/revision identity; do not infer a folder from a basename, opaque digest, title or model answer. If automatic server folder grouping is actually required, first specify its minimum metadata/approval impact rather than assuming it exists.

**Proof:** include CORE parent/non-CORE child, noncanon parent/CORE child, duplicate basenames in different approved groups, explicit noncanon opt-in, and generic mode with no CORE restriction. Default corpus discovery must remain a bounded database operation, with no query-time filesystem scan.

### P19-PR-04 — MEDIUM / OPEN / blocking: quality acceptance can waive a requested answerable-language capability

**Evidence:** R19-05 requires multilingual support including an applicable Han-script case. acceptance-matrix.md:11 accepts either a correct answer or insufficient evidence even when the answer is present. Lines 40–44 leave all examples selectable and do not specify which positive behaviors are mandatory. Q24 also combines deterministic citation rejection with semantic correctness without defining the finite semantic evaluation decision.

**Consequence:** a model/retriever that always misses the known Han evidence can pass the listed Han case; different implementers/reviewers can choose incompatible meanings of “acceptable.” A valid citation key alone still cannot reject a fabricated assertion citing that key.

**Smallest correction:** identify mandatory positive Vietnamese/English/applicable Han, multi-source, follow-up, partial and conflict cases; label answerable cases and expected evidence explicitly. An answerable-case abstention must count as a retrieval/answer-quality miss, not PASS. Freeze the small fixture split, labels and finite pass/fail rule before candidate comparison. Separate deterministic source/citation/locator denial from sampled semantic support evaluation and specify which failed mandatory cases block selection. Retain genuine no-answer abstention and the explicit limit that no universal future-prompt truthfulness proof is claimed. No judge, human signature, new dashboard or arbitrary speed SLO is required.

## Required user-visible capabilities and necessity

The requirement inventory below covers every R19 requirement; “covered” means reviewed preparation intent, not runtime PASS. Effort is relative implementation / verification burden, not an elapsed-time estimate.

| Requirement | Simplest adequate mechanism / reuse | Disposition; effort; remaining gate |
| --- | --- | --- |
| R19-01 Markdown Q&A | Existing immutable snapshot/parser/chunks; no new import adapter | REUSE; low / focused; approved test corpus only |
| R19-02 documented facts | Small evidence-bound generation; no tools or model-memory authority | KEEP; medium / medium; finite support labels, PR-04 |
| R19-03 citations | Actual chunk source UUID, generation/revision, heading and constituent locators | REUSE; medium / medium; validate excerpts against exact snapshot, never invented line offsets |
| R19-04 partial/conflict/unknown | Explicit response outcomes on supporting/conflicting evidence | KEEP; medium / medium; mandatory positive and no-answer cases |
| R19-05 languages/names | Same pinned query embedding; small multilingual generation trial | KEEP; medium / medium; PR-04; no parser/model upgrade implied |
| R19-06 short conversation | Bounded request history only; fresh evidence remains authoritative | SIMPLIFY; low / focused; size, ambiguous antecedent and misleading prior-answer cases |
| R19-07 generic/narrow scope | Explicit IDs or current eligible Markdown from DB; bounded selection | KEEP; medium / medium; PR-03; no silent truncation to the 100-source ingestion-job limit |
| R19-08 canon/noncanon | Explicit approved source sets, checked on actual chunk origins | SIMPLIFY; medium / medium; PR-03; no Ideaverse module |
| R19-09 snapshot currentness | Reuse manifest/epoch/reset/revision/pipeline checks | REUSE; high / high only at changed race boundary; PR-01 |
| R19-10 privacy/offline | Existing authenticated Java boundary; no new worker SQL privileges | KEEP; medium / high at changed boundary; PR-01/02 |
| R19-11 finite CPU resources | One admitted Q&A operation, finite budgets, owned cancellation | KEEP; medium / measured; no GPU or invented sub-30-second SLO |
| R19-12 model choice/session | At most two CPU candidates; one bounded model instance | SIMPLIFY; medium / measured; no populated prompt/KV reuse across revoked contexts |
| R19-13 evaluation | 20–30 labeled questions plus separate deterministic boundary tests | KEEP; low-medium / bounded; PR-04 |
| R19-14 retention/logs | No DB chat history; safe typed operational outcomes | REUSE; low / focused; native stderr/crash/error and context cleanup included |
| R19-15 enablement | Existing opt-in isolated test setup; feature defaults disabled | KEEP; low / native integration; no production indexing activation |
| R19-16 backend first | Existing module-local REST adapters/root envelope | REUSE; low / focused; frontend and recommendations deferred |

| Mechanism / alternative | Decision and reason | Approval / non-goal |
| --- | --- | --- |
| Exact scoped pgvector top-K | KEEP/REUSE baseline; current repository has generation reads and single-vector distance helpers, not ranked Q&A | New query is future implementation; authorize sources before scoring/counts; specify metric/tie order and null-vector denial |
| Existing parser/chunker/tokenizer/384d vectors | REUSE exact identities; reindexing the corpus is unnecessary for a query feature | No incidental dependency or vector-space change |
| One Java-owned query process boundary | KEEP narrowly; reuse native process/transport protections where semantics fit | Current job-scoped worker is not a reusable QA daemon; no weakened ingestion grants |
| Native CPU generator versus Python model wheel | Prefer evaluating a narrow native adapter; avoids assuming a CPython 3.14 model wheel exists | Runtime compatibility still NOT_VERIFIED; no installer or runtime download in this review |
| Hybrid/reranker/ANN/retries | DEFER unless exact retrieval fails a required labeled case and a bounded change is justified | No mandatory infrastructure from historical plans |
| New agents/hooks/framework/second vector DB | REMOVE from proposed scope; existing implementer, skills and takeover path suffice | No tooling, trust or hook change justified |
| OCR/PDF/Office/TXT/GPU/live originals | DEFER/REJECT for P19 | No R7 reopening, GPU proof or format expansion |
| Fiction/Film Q&A/recommendation, Ideaverse UI/sync | DEFER | No catalog-source expansion, named integration, frontend or automatic Git access |

## Source feasibility and empirical limits

- **Verified source reuse:** Java RAG remains a leaf with only vault::entry, fiction::source and film::source edges. Q&A stays in RAG's existing API/internal application, persistence and module-local web boundaries. Reuse the root ApiResponse family and module RagExceptionAdvice under ADR-0016; no new common module or copied error envelope.
- **Verified embedding identity:** model SHA-256 **98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f**, tokenizer SHA-256 **2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8**, 384 dimensions, tokenizers **0.23.3**, ONNX Runtime **1.31.0**, four CPU intra-op threads. Identity and lockfile are inspected, not freshly executed inference.
- **Verified local hardware:** CIM reports i5-9600K / six cores and **17,112,092,672 bytes (15.94 GiB)** physical RAM. No GPU measurement was necessary or performed.
- **Process constraint:** the present supervisor uses a **2 GiB / one-process** document-worker profile. It cannot be assumed sufficient for an LLM or a Python-plus-native-child arrangement. Profile and process-tree changes need their own measured, bounded query contract; do not raise the existing ingestion cap globally.
- **F01/F03:** blocking preparation gaps PR-01/03. **F02/F06:** embedding entry point and Java/API/native building blocks are available, but the new query transport/lifecycle remains proposed. **F04/F05/F07:** retrieval quality, model resources and real approved corpus import remain future measured gates, not preparation tests falsely marked PASS.

### At most two model candidates — proposal only

These are small existing native-GGUF candidates, not a selection or a claim that either is the newest/best model or passes Vietnamese/Han Q&A. Official file metadata was inspected on 2026-10-27; no weights were downloaded or locally hashed.

| Candidate | Published artifact SHA-256 / metadata |
| --- | --- |
| [Qwen3-1.7B, Q8_0](https://huggingface.co/Qwen/Qwen3-1.7B-GGUF/blob/main/Qwen3-1.7B-Q8_0.gguf) | Apache-2.0; approximately 1.83 GB; **061b54daade076b5d3362dac252678d17da8c68f07560be70818cace6590cb1a** |
| [Qwen3-4B, Q4_K_M](https://huggingface.co/Qwen/Qwen3-4B-GGUF/blob/main/Qwen3-4B-Q4_K_M.gguf) | Apache-2.0; approximately 2.5 GB; **7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5** |

The [official llama.cpp stable release v0.7.0](https://github.com/ggml-org/llama.cpp/releases/tag/v0.7.0) points to [build b11669](https://github.com/ggml-org/llama.cpp/releases/tag/b11669), commit **620d2225cc436ab0b001e3200e590e3b286ef9ab**, with a Windows x64 CPU artifact. This is a candidate compatibility baseline; target-host execution and the runtime archive/executable checksum are **NOT_VERIFIED**. Before any authorized trial, record the exact immutable model revision, verified local weight hash, runtime asset/hash, license, non-thinking prompt/template settings, CPU-only invocation and finite context/output/RAM/time budgets. Do not follow automatic-fetch quickstarts at runtime.

Artifact sizes do not prove peak RAM/headroom or latency. Freeze a small common development/held-out set before trials, compare no more than these two configurations, and stop if neither meets mandatory cases. Prefer one admitted request/no queue initially; reuse only the safe model instance, discard content-bearing session/KV state after each request or affected denial. Split measured trials into finite normal-profile commands where needed; do not relabel a benchmark as a full suite to obtain 35 minutes.

## Finite slice proposal and future verification

This is a preparation recommendation, not an executable handoff or authorization of either slice.

| Candidate slice | Bounded work / useful result | Focused gate before independent slice review |
| --- | --- | --- |
| S19-1 | Source selection/lifetime, pinned query embedding bridge, exact ranked evidence and citation contract, within existing RAG ownership | Real PostgreSQL role/authority tests, both-order source/child races, scoped retrieval/citation/Unicode cases, authenticated/default-disabled API behavior and ApplicationArchitectureTests. Resolve PR-01/03 first. Do not add an extra public product API solely to mirror a technical slice. |
| S19-2 | One pinned offline CPU generator, bounded recent turns, grounded/partial/conflict/abstention responses, end-to-end acceptance | Same 20–30 labeled cases after PR-04; actual independent sample-answer examination; CPU/RAM/cold/warm/queue-inclusive timings; cancel/restart/missing-artifact/offline and final-emission/revoke races. Resolve PR-02, hash/runtime selection and every mandatory safety case before READY. |

Prefer **two separately committable slices**; fold the dossier's optional S19-3 acceptance work into S19-2 unless actual implementation size warrants an Owner-approved split. Antigravity implements/tests and always self-reviews; separate Codex reviews each slice. After all slices, one phase final review aggregates valid receipts and verifies changed risks, then Owner publication/closeout and the separately authorized P17–19 milestone. No milestone or future audit is started here.

For future verification, use the **implemented** scripts/run-bounded-command.py: ordinary cap **300 seconds**; only eligible, explicitly approved full-suite invocations may use **up to 2,100 seconds**. Its 300-second no-progress diagnostics and owned-tree cleanup are bounded mechanisms, not universal OS containment. Focused Maven selectors must name the eventual changed tests plus the existing architecture test; Python uses the pinned .venv-d17 environment with Ruff check/format, mypy and changed pytest cases. The implementation handoff must name those exact commands once test names exist. A full Maven clean verify is risk-driven if shared authority/migrations warrant it; it is not required merely to review these six docs.

Test-only import can reuse AbstractEnabledRagIntegrationTest / RagSlice4LifecycleIntegrationTest's explicit temporary-file registration, approval, admit/execute/finalize path against disposable PostgreSQL with per-source grants and scoped native worker configuration. Use synthetic sanitized Markdown by default. Real creative files require explicit selected-file approval; do not copy a repository wholesale, enable indexing globally, read live originals on query, or reuse migrator credentials in a worker. Captured evaluation outputs must be limited to approved fixtures in deliberate review evidence, not application operational logs.

## Approval docket and correction ownership

| Remaining item | Exact disposition / owner |
| --- | --- |
| Query-time revoke/use/response semantics | ChatGPT prepares the minimal source-backed contract and alternatives for PR-01; Owner resolves any material change to busy/cancel, persistence or frozen guarantees together before handoff. No design approval is inferred here. |
| Marker consent and canon selection | ChatGPT clarifies the existing whole-file exception and explicit source-ID/child rules. Return to Owner only if a different product/frozen-contract behavior is proposed. |
| Model/runtime and resource profile | Future authorized bounded measurement, then pin one runtime/model and safe finite budgets. No runtime install, model trial or activation authorized by this review. |
| Scope status | Preparation review is authorized; Phase 19 implementation remains unapproved. A future current notice must distinguish preparation readiness from execution authorization without rewriting historical records or opening ACTIVE. |

## Audit of the existing harness and repository

Reviewed all six submitted files and the actual relevant recursive tree: **110 Java RAG source files, 50 Java RAG test/helper files, 22 Python source files and 67 Python test/retained-fixture files**. This is a path/package inventory, not a claim to line-review every unchanged implementation. Relevant source/authority, transport, schema and test paths cited above were inspected directly after Graphify navigation. No .gitkeep appears in those subtrees; meaningful Java package descriptors and Python package initializers remain present.

Applicable governing material inspected: root AGENTS.md; docs/AGENTS.md; backend/AGENTS.md; backend/src/test/AGENTS.md; current policy/Docs Sync/guardrails; roadmap/RAG plan; ADR-0016/0019 and applicable ADR-0022/0023 clauses; module matrix/package tree; P17 source-eligibility and revocation contracts; P18 D27 closeout and D26 independent acceptance; templates. No RAG-scoped AGENTS.md exists.

Harness inspection covers .agents/README.md, backend-implementer, pre-handoff/review-coverage/documentation-writing, Python/local-RAG/bounded execution, backend-testing/modular-monolith-architecture/reuse-and-consistency, self-review and takeover skills; workflow/Java/Python/package/security/tests/documentation/reuse rules; .agents/hooks.json and hook path inventory; .codex config/README/rule inventory; backend/pom.xml, Python version/project/lock and existing native/role/lifecycle/locator tests. No new agent, skill or hook is justified. The existing backend-implementer plus python-rag-coding, bounded-command-execution, mandatory self-review, independent review and expressly authorized takeover path suffice.

Legacy guardrail/agent sections contain old two-round, timeout, OCR/private-content or “awaiting B review” wording. Current policy overrides historical workflow; ADR-0022/23 govern external whole-file content. Keep an explicit applicability pointer in corrected preparation; do not repair the harness or old evidence within this review. No hook execution/trust repair or claim of universal interception was made. Supported Windows trusted-root/native-hook limits remain intact.

## Review coverage ledger

PASS below refers to the preparation check only. NOT_VERIFIED records runtime evidence that does not yet exist; it is not an implementation PASS or an automatic extra subsystem.

| Dimension | Status / evidence and limit |
| --- | --- |
| 1 Owner intent/scope | PASS for preparation-only scope record; execution remains gated |
| 2 Traceability | FINDING PR-02/03/04; every R19 item inventoried above |
| 3 Architecture/ADRs/schema | FINDING PR-01/02; preserve frozen source-use/consent and explicit amendment gate |
| 4 Module ownership | PASS proposed existing RAG boundaries and dependency matrix; no new edge |
| 5 Folder/package structure | PASS relevant recursive inventory; no source relocation requested |
| 6 Shared API/exceptions | PASS reuse root envelope / module advice under ADR-0016 |
| 7 SOLID/cohesion | N/A new implementation; narrow owned query path proposed |
| 8 DRY/KISS/YAGNI/invariants | PASS necessity matrix; no duplicate pipeline/framework |
| 9 Patterns/anti-patterns | PASS one concrete process adapter only if needed; generic provider layers rejected |
| 10 Domain/state correctness | FINDING PR-01/02/03 |
| 11 Public API/wire | NOT_VERIFIED new API; existing authenticated envelope inspected; exact future DTO/errors required |
| 12 Input/locator validation | PASS preparation boundary cases; runtime Unicode/bounds/citation checks NOT_VERIFIED |
| 13 Authorization/privacy | FINDING PR-01/02/03; no current Q&A certification |
| 14 Credentials/retention/egress | PASS stated no-content logs/offline/least privilege; new runtime proof pending |
| 15 Transactions/concurrency | FINDING PR-01; actual later-consumer races NOT_VERIFIED |
| 16 Recovery/cancel/cleanup | FINDING PR-01; job supervisor evidence does not certify query/model reuse |
| 17 Database/query/roles | PASS existing RAG-owned storage/grant inspection; new exact query/any migration NOT_VERIFIED |
| 18 Complexity/resources | NOT_VERIFIED model/admission budgets; host measured, no fabricated performance PASS |
| 19 Retrieval/grounding quality | FINDING PR-03/04; exact baseline favored, quality unmeasured |
| 20 Markdown/languages | FINDING PR-04; P18 locators/whole-file source path inspected; OCR/other formats N/A |
| 21 Adversarial/security tests | FINDING PR-01/03; future deterministic/native cases listed, not run |
| 22 Test adequacy | FINDING PR-04; existing PostgreSQL/locator fixtures reusable, not new QA proof |
| 23 Performance/benchmarks | NOT_VERIFIED; finite CPU/headroom/cancel trials deferred to authorized implementation |
| 24 Evaluation | FINDING PR-04; finite labeled cases, no automatic judge/human-signature gate |
| 25 Dependencies/licenses/pins | PASS existing lock/embedding identity inspection; proposed model metadata above; installed runtime NOT_VERIFIED |
| 26 Build/static/IDE/tooling | N/A fresh build for docs-only review; no IDE inspection, warning-free claim or fresh static-clean claim |
| 27 Safe logging/observability | PASS preparation prohibition; runtime/native-output redaction and cleanup remain tests |
| 28 Documentation/governance | FINDING in-scope consent/scope/acceptance gaps; Docs Sync below |
| 29 Hygiene/delivery | PASS entry manifest/path/link checks and unchanged tracked source; review writes limited to two artifacts |

## Checks performed and preserved evidence

- Git status/diff/cached diff, log, remote ls-remote and predecessor ancestor check: baseline confirmed; index untouched.
- In-memory local-link audit: **31 local links, zero missing targets** across the original six preparation files. This is not certification of every external URL.
- Post-write verification: **40 local links, zero missing targets**, all 29 coverage rows present and valid UTF-8 in both review artifacts. The five other author documents retain their exact entry hashes; no staged or tracked source/tooling/ACTIVE change exists.
- Native CIM hardware inspection and direct source/lock/schema/test inspection: completed. Graphify graph was used only for navigation; its older/truncated results were verified in source, not treated as authority.
- **No Maven, pytest, Ruff, mypy, IDE, model inference, DB mutation or import/index run was performed.** No Antigravity P19 implementation evidence exists. Accepted P18 results are historical and reused only for their original scope.
- External model file metadata and stable runtime release pages were inspected. The Ideaverse tree/content/inventory could not be independently fetched in this session; its author-reported counts/public visibility remain **NOT_VERIFIED**, not a proven broken link or an imported corpus. A metadata-only Python HTTPS attempt failed certificate verification; no certificate bypass, trust/config repair, credential search or model download followed.
- Existing logs, raw receipts, accepted reviews, failures and LOW backlog remain unchanged. No additional LOW remediation round is created.

Entry dossier SHA-256 values, before this review's status edit:

| File | SHA-256 |
| --- | --- |
| README.md | 6d91778a42684187a42260dd43a56dbb29383504ec6d7de14b3a89d27b9108d7 |
| owner-decisions-and-scope.md | 94feda069cd77b13a4b219b59d8e36af031fac5d0e5b02900269bc8958c2398c |
| source-reuse-and-feasibility.md | 8c1e669ef853ad2b8543a603b00d281b80f40e29a2f862d9ccd06833594cd14c |
| acceptance-matrix.md | 5b561b9ab997908eae9f3cf2571da98d331c818a1326eacd414640ef3b44f1fd |
| preparation-review.md | 3329b72fb5dbb6a3ea28b177f9dbf697a8229454270ae65c9af13ee959a329aa |
| PACKAGE_CONTENTS.md | 3b44b7800a9320d8a7da97678b72f111cf181b0a088bdd59b556621e2b1d3e90 |

## Docs Sync Check

| Disposition | Scope / impact / owner |
| --- | --- |
| UPDATED | This new independent review and preparation-review.md status/next action only. No correction of requirements or source behavior by the reviewer. |
| ALREADY_CONSISTENT | Actual HEAD/remote baseline; NO_ACTIVE_HANDOFF; P17 frozen/P18 closed; no Phase 19 implementation or indexing activation. |
| BLOCKED_OWNER_DECISION | Any PR-01 proposal that changes frozen source-use/mutation/persistence semantics must receive a concrete prospective Owner decision before handoff. The review does not select or approve such a change. |
| NOT_VERIFIED | New Q&A runtime/quality/performance/races, installed generator/runtime hashes and independently accessible real sample corpus. Future measurement gates remain explicit. |
| DEFERRED_OUT_OF_SCOPE | Old status notices in AGENTS/scoped instructions, roadmap, package-tree/worker README, .agents and guardrails; author README/manifest's pre-review wording awaits ChatGPT's bounded correction package. No repository-wide cleanup. Preserve B1-L01/B2-L01/MA-A03–06/DOCB-L01/L02 OPEN and B3-L01 CLOSED. |

**Correction boundary:** ChatGPT repairs the six P19 preparation files against P19-PR-01–04, updates their status/manifest and identifies any exact prospective decision/ADR; preserve this independent review. Any broader policy/ADR amendment must be explicitly scoped for Owner approval. Then a separate Codex preparation re-review checks only corrected findings and changed applicable evidence. Production/tooling work still requires approved preparation publication and a separately invoked $codex-create-handoff.

**Next step:** ChatGPT | bounded P19 preparation correction for P19-PR-01–04, then Codex $codex-pre-handoff-review | docs/implementation/phase-19/ and this independent review | resolve lifetime/consent/canon/acceptance blockers and any material Owner decision; keep ACTIVE = NO_ACTIVE_HANDOFF | P19 implementation remediation NOT_STARTED (0/3); no takeover.
