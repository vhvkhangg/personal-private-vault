# Independent re-review 2 — CSA26-AUDIT-REMEDIATION

- **Date / reviewer / mode:** 2026-11-01 / independent Codex / SLICE, approved maintenance implementation.
- **Baseline:** main at 04528f76c32380f07377f470e1920a31583d690d; uncommitted working tree, empty index. Entry: 35 modified tracked paths and five untracked handoff/review records.
- **Authority:** [Owner selection](../owner-decision-csa26-selected-remediation-2026-11-01.md), [scope](../remediation-scope.md), [executable contract](../executable-handoff-2026-11-01.md), [ACTIVE](../../../handoffs/ACTIVE.md), [initial review](2026-11-01-csa26-independent-final-review.md) and [re-review 1](2026-11-01-csa26-independent-re-review-1.md).
- **Status:** **READY_FOR_OWNER_COMMIT — READY FOR OWNER COMMIT.**
- **Finding disposition:** CSA26-001/002/003/004 and CSA26-FR-01/02 **CLOSED**. No Critical/High/Medium or mandatory unverified acceptance remains. One new **LOW / nonblocking** layout observation is tracked below.
- **Counter:** **2/3 remediation rounds consumed**; correction loop stops on acceptance. One round is unused, not transferred or automatically authorized for further work. Takeover NOT_AUTHORIZED; historical S19-2 counters unchanged.
- **Fresh evidence:** logs/csa26-maintenance/codex-rereview-2-20261101-144812-e965195f/. Previous source-applicable independent tests/static evidence is reused after drift checks. No publication is claimed.

## User requirement and simplest-mechanism check

The selected maintenance now isolates archived QA tests before initialization/import, corrects the nine current status/setup entry points, reconciles the existing container/module representations and aliases, and resolves the four Ruff import-order diagnostics. Standard Jupiter conditions, pytest pre-import skips, existing synthetic/MVC checks and local rendering tools remain sufficient; no production/runtime/schema/dependency or module-boundary change was introduced.

The accepted delivery is bounded maintenance. It neither accepts nor resumes S19-2, enables indexing/live originals, completes Phase 19, or triggers its milestone.

## Finding register and closure evidence

| ID | Disposition | Independent evidence |
| --- | --- | --- |
| CSA26-FR-01 | **CLOSED** | All four remaining concrete corrections from re-review 1 are verified below. Container topology/status is visible and readable in SVG/PNG. |
| CSA26-FR-02 | **CLOSED / retained** | All module-family file hashes match accepted re-review 1. DOT/draw.io/SVG node/edge sets still agree: 20 modules, 41 edges, all three RAG arrows; both alias pairs remain byte-identical. |
| CSA26-FR-L01 | **LOW / OPEN / NONBLOCKING** | The feed connector's new vertical segment at x=980 still crosses the small reader/archival edge-annotation boxes in container.svg:191. Their text remains readable; the worker nodes and query-worker status badge are clear. This is limited layout hygiene, not a missing capability, incorrect contract or illegible required view. Track for a future authorized milestone/audit pass; no extra slice correction round is required under Owner LOW policy. Optional minimal improvement: route the vertical leg through unoccupied space and regenerate PNG. |

**CSA26-FR-01 closure details:**

1. **Dimension:** container.svg:107 and container.dot:11 show **384-dim**, matching model.py:20, RagDocumentFinalizationGate.java:36,486–489 and V24__rag_markdown_retrieval_readers.sql:198. No model/code/schema was changed.
2. **Storage:** container.svg:61 says **Configured snapshot root / Java authority**; container.dot:20 now uses the configured-root wording. This accurately abstracts RagOriginalBytesStore.java:148,190–195 and removes the invented data/snapshots path.
3. **Reader lifetime:** container.svg:177 names positive **sender/child** stop; container.dot:24 and workspace.dsl:68 preserve reader exclusion through async output and positive sender/child stop outside SQL transactions. The SVG also explicitly states that no SQL lock spans compute or async emission. Together the compact view/ADR-0025 reference communicates the accepted boundary without a new sequence diagram.
4. **Worker status legibility:** the feed path now travels at y=855 below the workers, clearing the Query Embedding Worker's badge. The former horizontal badge strikethrough is gone. Nodes, status badges and required labels are readable in the delivered PNG and fresh SVG render. The minor annotation-box intersection is disclosed separately as LOW, rather than asserting universal zero-overlap.

The four original selected findings are therefore independently **CLOSED**. No older backlog or S19-2 finding is closed by this result.

## Acceptance matrix

| Cases | Result / evidence |
| --- | --- |
| A01 | **PASS / reused:** all eight Java archive conditions; missing/false/legacy selections each skip 32 tests with no archived class initialization. Initial JVM initialization logs remain applicable to identical source. |
| A02 | **PASS / reused:** all five pre-import Python guards; direct absent/false selection skips all five with expected pytest exit 5; project-default absent/false collection finds 242 accepted items and skips the five QA modules, zero blocked import attempts or loaded QA modules. |
| A03 | **PASS / reused:** 3 archive MVC guard cases + 3 pure authority cases = 6 Java tests; exactly the six approved synthetic Python modules = 42 tests. No failures/errors/skips in those regression selections. |
| A04 | **PASS / reused and drift-checked:** 131 finite fixture/control/writer paths unchanged; all 14 reviewed test hashes and both full test-folder inventories unchanged. No new native/model/evaluation action was run. |
| A05–A06 | **PASS:** nine phase/setup entry points remain accurate; supported command paths/links are checked. Current maintenance status/counter is synchronized with this acceptance. Historical notices and actual publication claims remain distinguished. |
| A07 | **PASS:** corrected source facts, full reader annotation, accepted/inactive/suspended/proposed distinctions, current readable container SVG/PNG; both fresh rasterizations are byte-identical to the delivered PNG. LOW cosmetic observation above does not leave required information unreadable or unverified. |
| A08 | **PASS / retained:** 20-node/41-edge source graph equality; all three RAG arrows; unchanged accepted module renders and identical SVG/PNG aliases. |
| A09 | **PASS / reused:** named-four Ruff, full src/tests/scripts Ruff and format receipts against unchanged source/configuration; 120 files already formatted. Line-local E402 exceptions remain limited to guarded imports. |
| A10 | **PASS:** completed self-review, authorized path inventory, truthful independent render evidence/limits, current saved statuses/counter/next action, links and whitespace. The independent disposition qualifies the submitted absolute zero-crossed-label claim with CSA26-FR-L01. |

## Verification and applicability

- Fresh recursive diagram inventory hashes **17 files**. Exactly four diagram paths changed since re-review 1: container.dot, workspace.dsl, container.svg and container.png. Module/system-context/functional-decomposition families are unchanged. XML parses; module DOT/draw.io/SVG graph **sets** agree on 20 nodes/41 edges and aliases compare byte-for-byte.
- Fresh inspection confirms the four container corrections. Delivered container.png was visually inspected. Installed Edge headless rendered the local SVG through run-local HTML with isolated profiles, normal bounded runner and a 120s cap. Both captures match the delivered PNG **byte-for-byte**: SHA-256 **b668aceed42656d6a0d30ce59cee81b3a49944e2ca3fb1cae4bed79bcfd3988b**. The second capture adds a 3000ms virtual-time budget; it produces the same image. No blank/missing render is treated as PASS.
- Receipts: render-container.json and render-container-v2.json, exit 0, complete capture/cleanup, no termination needed. Screenshots: container-svg-render.png and container-svg-render-v2.png. Command: existing scripts/run-bounded-command.py, normal/120s, followed by installed msedge.exe headless/disable-gpu/disable-background-networking/no-first-run/hide-scrollbars, unique user-data-dir and screenshot paths, window-size 2360,1180, local container-render.html URI. The second invocation adds virtual-time-budget=3000. No installation/download or externally hosted diagram service was used.
- inspection.json verifies **14 unchanged test hashes**, unchanged recursive inventories (**74 Java RAG test-subtree files / 119 Python test-subtree files**), **131 preserved finite paths**, and **12 prior successful bounded receipts** with complete capture/cleanup. Initial independent run: logs/csa26-maintenance/codex-review-20261101-130152-32afc6f9/. Re-review 1 run: logs/csa26-maintenance/codex-rereview-1-20261101-140821-e2e13b0c/. Their passing evidence remains source-applicable; no duplicate Maven/pytest/Ruff run is needed for a diagram-only correction.
- No changes to accepted production code, Maven/Python runtime/dependencies, uv.lock, database/migrations, API/root transport, authentication or source authority. Initial ADR-0016/root ApiResponse and exception/interface/enum/command/view grouping assessment remains applicable; no new package or abstraction exists.
- Formal Graphviz/Structurizr semantic validation remains **NOT_VERIFIED under the specifically permitted handoff tooling limitation**. XML/source comparison/rasterization is not mislabeled as a formal DSL validator. This is the existing allowed limitation, not an unresolved mandatory case.
- Original compiler/deprecation/unchecked/JDK/Mockito warning assessment remains applicable. No IDE inspection or warning-free claim is made. No full suite, DB/native QA, inference, model/GPU probe, provisioning or publishing was performed.
- Final links, saved status/counter, unchanged source/export hashes, whitespace and empty index are checked after status writes in final-check.json. All evidence uses fresh ignored paths; historical logs/reviews are preserved.

## Shared review coverage

All 29 dimensions remain covered by the initial review and drift checks, with current dispositions:

| Dimensions | Result / rationale |
| --- | --- |
| 1–5, 7–10 | **PASS:** selected requirements/acceptance, factual container source trace, ownership/graph invariants, actual recursive folder inventories, minimal cohesive test guards and no speculative abstraction. CSA26-FR-L01 is a nonblocking cosmetic exception. |
| 6, 11, 15, 17 | **N/A to correction; baseline checked:** no API/error, transaction or persistence edit. Accepted ADR-0016 root HTTP envelope/module-local adapters remain intact. No common module or new interface/exception/command/view grouping is warranted. |
| 12–14, 16, 18, 21–22, 27 | **PASS for the changed/preserved boundary:** reused initialization/import denial, archive MVC routes, synthetic tests, bounded checks and finite preservation. No new credential/logging, recovery, performance or I/O path. |
| 19–20, 23–25 | **N/A:** no ingestion/OCR, retrieval/answer quality, evaluation/benchmark, model/dependency or runtime change; no new live-file/source-commit/worker proof is required. |
| 26 | **PASS** for applicable source/XML/render and reused test/lint evidence; formal DOT/DSL semantic validation **NOT_VERIFIED**, explicitly permitted as above. |
| 28 | **PASS with LOW tracked:** scoped current docs/status/links and source/export facts now agree; no unsupported publication claim or historical rewrite. |
| 29 | **PASS:** allowed changed-path inventory, ignored evidence, unchanged accepted artifacts/tests/fixtures, whitespace and empty index. Required aliases are intentional. |

## Scoped Docs Sync Check

- **UPDATED — review/status only:** this acceptance; ACTIVE; executable handoff current disposition; remediation-scope current disposition; handback reviewer disposition; current maintenance status/counters in affected entry-point notices. Self-review/submission and prior independent reviews remain identifiable history. Codex did not repair tests or diagrams.
- **ALREADY_CONSISTENT:** all nine phase/setup entry points; corrected container and accepted module representations; P17 frozen/P18 closed/S19-1 accepted-published versus S19-2 suspended/not accepted/archival only; inactive indexing and disabled live originals.
- **TRACKED LOW:** CSA26-FR-L01 is recorded here and in the current maintenance register; submitted zero-overlap wording is qualified by the independent observation, with no additional correction round.
- **DEFERRED_OUT_OF_SCOPE:** ten older LOW IDs, AUTH-CLOCK and historical/nested agent/rule/navigation drift. No unrelated cleanup, acceptance, evidence recovery or counter reset.
- **Publication:** READY_FOR_OWNER_COMMIT only. No maintenance publication SHA exists yet; Owner performs normal staging/commit/push. Brief closeout must use the real published SHA.

## Owner next action

Owner may stage, commit and push this accepted maintenance delivery using the single suggested message below. There is no extra staged-tree review or further implementation/re-review round for this accepted slice. After publication, ChatGPT performs the brief maintenance closeout referencing the actual SHA; it does not reopen Phase 19 or expand scope.

Suggested commit message:

~~~text
fix(maintenance): remediate CSA26 audit findings
~~~

Next step: Owner | stage, commit and push the accepted delivery, then ChatGPT brief maintenance closeout | CSA26-AUDIT-REMEDIATION; [ACTIVE](../../../handoffs/ACTIVE.md), this acceptance and [handback](../implementation-handback.md) | READY_FOR_OWNER_COMMIT; closeout uses actual published SHA, no extra staging-review session | remediation 2/3 consumed; accepted and stopped; takeover NOT_AUTHORIZED; historical S19-2 counters unchanged.
