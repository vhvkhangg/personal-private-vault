# Phase 18 Slice 2 — independent Markdown-only closeout review

**Date: 2026-10-21. Decision: CHANGES_REQUESTED.**

The D21 Markdown producer-local implementation requirements are independently satisfied. D17-R01–R05 and FR-01/02 remain **CLOSED**, and D20 accepts their independent quality evidence. No new implementation defect was found. **Evidence preservation failed during this review: its fresh test rerun overwrote three historical raw JSON captures (S2-D21-E01). Their original bytes have not been recovered.** Formal closeout is blocked by that preservation failure and also awaits explicit Owner disposition of ADR-0024's prospective higher-contract qualification. The reviewer does not accept that proposed ADR, declare Slice 2 CLOSED, or grant READY_FOR_OWNER_COMMIT.

## Scope, authority and integrity

This is the expressly requested D17/D20/D21 independent maintenance/governance review under `codex-final-review`, not implementation authorization. Read [ACTIVE](../../handoffs/ACTIVE.md), [Owner decisions](../owner-decisions.md), the complete [D17 contract](../obsidian-markdown-d17-contract.md), [D21 scope/matrix](../slice-2-markdown-only-d21-scope-and-acceptance.md), [governance traceability](../slice-2-markdown-only-d21-governance-traceability-2026-10-20.md), [proposed closeout](../slice-2-markdown-only-d21-proposed-closeout-2026-10-20.md), [ADR-0024](../../../adr/0024-phase-18-slice-profile-acceptance-boundaries.md), ADR-0020–23, original Slice 2 G+H boundaries, acceptance/oracle/P18-FINAL documents and the independent D17 review chain. Current explicit Owner scope controls over historical launcher/AGENTS status text; it authorizes no production change.

Baseline is **main / c3b46351507803053850e3c2711602ee9c9ec419**, with inherited uncommitted work. Reviewed the actual Git diff and recursively inventoried relevant code, tests, fixtures, configuration, evidence and documentation. Graphify's older 8,894-node cache was navigation only; current source and canonical contracts control this result. Reviewed the actual exception/element/worker/transport grouping and applicable shared-contract boundaries; no new module, persistence or API architecture change is involved.

Independent integrity verification **at entry, before the fresh rerun**, established:

- **111** producer source/test/script/configuration hashes exactly match the prior R03 independent candidate. The parser, chunker, model/cache, worker, limits, dependencies and tests have not changed.
- **23 existing D21 documentation changes + four new non-self-hashed documents** match their inventory. All 23 pre-D21 bodies were separately reconstructed and matched their original hashes, allowing original LF/CRLF encoding. Thus higher ADR clauses, P18-FINAL commands and historical decisions were retained rather than silently rewritten. **105** current/new local file links resolve.
- **2,415** other D21-entry files, D19's **193** package files, all **14** prior R03 bounded receipts and their recorded logs, **165** older independent log/artifact hashes, and the referenced independent review artifacts remain unchanged. No human signature was added. A total of **2,680 distinct file/artifact hashes** was verified; counts alone were not treated as proof.
- The actual model and tokenizer files were independently streamed through SHA-256: `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` and `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8`. No artifact download or dependency installation occurred.

The [independent evidence ledger](2026-10-21-slice-2-markdown-only-independent-closeout-evidence.json), SHA-256 **`88ab595e91e4532241bc223a988df443dd15634a4cd97feb7a58657a836e1919`**, contains exact commands, receipts, source/configuration hashes, current entry manifest and raw observations. [The dirty-path scope inventory](2026-10-21-slice-2-markdown-only-independent-closeout-scope.json) records every inherited dirty path and its hash/classification. Original handbacks, reviews and command/failure receipts remain immutable. The three raw JSON captures subsequently overwritten by this review are disclosed below; current all-artifact preservation is FAILED.

## D21 acceptance matrix

### All twelve frozen D17 requirements

The source-backed implementation is `parse_markdown(whole_file=True)` → `parse_obsidian` → `StructureFirstChunker` → the pinned local model → `compact_metadata` → producer-local output. The legacy field-filtered path remains separate. Current source inspection, exact candidate continuity and applicable independently executed receipts establish the rows below; D20 supplies their Owner quality acceptance. These are bounded executable proofs, not universal grammar or throughput claims.

| Requirement | Independent disposition | Actual implementation/evidence |
| --- | --- | --- |
| MD17-01 | VERIFIED / accepted under D20 | Maintained CommonMark/GFM parsing and Obsidian extensions; 1,329 official producer cases, mixed-note matrices and seven independently adjudicated interpretations. |
| MD17-02 | VERIFIED / accepted | GFM table structure, alignment/escapes, implicit/overflow cells and nested ancestry; independent exact cell/source intersections and lossless encoding reconstruction. |
| MD17-03 | VERIFIED / accepted | Source-backed safe YAML properties, scalar/list/nested values, aliases/cssclasses and original lexemes; malformed/duplicate/unsafe/expanding inputs reject whole roots. |
| MD17-04 | VERIFIED / accepted | Headings, tasks, lists/quotes and mixed containers; independently checked sibling/outdent/tab/CRLF ancestry, including current real producer replay. |
| MD17-05 | VERIFIED / accepted | Wikilinks/display aliases, heading/block selectors and definitions; ambiguous/missing selections deny; ordinary links grant no child authority. |
| MD17-06 | VERIFIED / accepted | Supplied immutable Markdown dependencies, exact child/parent versions and edges; conflict/cycle/depth/selection rejection; inside-HTML literal versus outside approved/unapproved bindings. |
| MD17-07 | VERIFIED / accepted | Arbitrary/nested/collapsible callouts retain titles, folded bodies and child context in realistic vault/producer fixtures. |
| MD17-08 | VERIFIED / accepted | Footnotes/comments/tags/highlights and their original source/metadata remain represented, including inactive syntax. |
| MD17-09 | VERIFIED / accepted | Fences, code, Mermaid and math remain authored inert data; executable query/template directives reject whole roots. |
| MD17-10 | VERIFIED / accepted | Native inert HTML, attributes/entities, RCDATA, optional/implied ends, non-void solidus, blank/EOF continuation and correct owning Markdown container boundaries; FR-01/02 remain closed. |
| MD17-11 | VERIFIED / accepted within measured bounds | Exact source/revision/line/character/block/cell provenance, source-aware/token-bounded chunks, cancellation and cleanup; independently measured resources, intersections and compact-reference reconstruction. |
| MD17-12 | VERIFIED / accepted | Complete-root validation before success, typed zero-chunk failures, consistent staged/failed counts/digest, independent-root batches, legacy/DOMAIN compatibility and Python 3.14.4 checks. |

The [R03 independent final review](2026-10-20-d17-r03-independent-final-re-review.md) and [earlier resource/intersection/accounting review](2026-10-20-d17-obsidian-markdown-independent-final-re-review.md) provide detailed test-to-source reasoning and measured limitations. The unchanged parser hashes remain `6dfa91355524da18c18ecfabb7cedc17cedcb2735f70e52feaf5daabae3ee33d` (`obsidian.py`) and `cd2f2f1c024dd7dd6674c53e4759c0447d5e11edbf442af2ccdd76590cd01172` (`obsidian_html.py`); `embeddings/model.py` remains `0761be34112e541ae758e9b4816e27dd46c0d6abb82024e5f8690db0e372389a`.

### Every D21 scope/governance clause

| D21 clause | Disposition | Independent assessment |
| --- | --- | --- |
| 1 — all MD17 obligations, approved dependencies, embedding/resources | VERIFIED | Twelve-row matrix above; no positive/negative requirement removed or accepted finding reopened. |
| 2 — TXT/DOCX/PDF/XLS/XLSX/OCR excluded; history preserved | EXCLUSION VERIFIED; PRESERVATION FAILED, E01 | Exact D21 wording; excluded implementation/tests remain present and unaccepted. Three historical D17 raw captures were overwritten by this reviewer after entry verification. No runtime-format restriction change is claimed. |
| 3 — D17/D20 acceptance and old human records | VERIFIED | D20 explicitly substitutes Codex independent quality adjudication. The 213 case + 12 profile human forms remain unsigned and **NOT_REQUIRED_UNDER_D20**, not fabricated ACCEPT results. |
| 4 — prospective Slice 2 rebase only, frozen guarantees | VERIFIED | D15/D16 format assignment is superseded only for this slice. Prior decisions/reviews, DOMAIN field privacy and Slice 1/D14 remain preserved. |
| 5 — higher contracts and future Java/P18 duties distinguished | MAPPING VERIFIED; OWNER CONTRACT DECISION OPEN | The map accurately identifies excluded versus future obligations. ADR-0024 is still PROPOSED; this review cannot promote its qualification into an accepted higher-contract rule. |
| 6 — evidence reuse, separate independent closeout, no commit readiness | REVIEW COMPLETED; PRESERVATION/CONTRACT BLOCKERS | Unchanged receipts are applicable; this separate review supplies the requested disposition. It grants no mixed-tree delivery acceptance. |
| 7 — Phase 18/P18-FINAL/FR18-3 and activation boundaries | VERIFIED as retained boundaries | Historical full Java failure and aggregate PostgreSQL issue remain open; external indexing and live originals stay inactive/disabled; Slices 3/4 remain unauthorized. |

## S2-D21-E01 - High - historical evidence overwritten by this review

The fresh runtime command reused the original independent pytest file without isolating its hard-coded `OUT` directory. It wrote new measurements/captures over these historical files under `logs/phase18/d17-r03-independent-2026-10-20/`:

| Historical file | Original SHA-256 (still recorded in the immutable prior ledger) |
| --- | --- |
| `fresh-producer-bound-True.json` | `48b1383e46bbf508bd25faa1075db857d1daac018efc8ccd4bef708a16bcc0b0` |
| `fresh-producer-bound-False.json` | `2909e393b18648b3b4b4ad3bf77b35e6e2ca1d7693505aabf022af99c26f72e1` |
| `fresh-negatives.json` | `3727b91410965e67df87074d0384e12a86e2f4e3d013cf056051845ae1ed91e4` |

**This was a reviewer execution mistake, not an implementation defect or an authorized evidence update.** The original hashes were independently verified before this command. The command receipt and its log are new and truthful, but the raw-result destinations were not isolated. The prior review/evidence ledgers were not edited to conceal the mismatches. Current rerun copies, both original/current hashes and the incident are preserved in the new evidence ledger and `current-rerun-captures/` under this review's own log directory.

A scoped search found no identical backup. D19 retains an unchanged documentation projection of all 52 cases; independent comparison confirms identical source, success/reason, chunks, locators, structural metadata and embedding-value digests. D19 intentionally omits original host/resource measurements, so those projections cannot recover the original JSON bytes. Recreating plausible measurements or replacing the old hashes would falsify history and was not done. A Windows shadow-copy inventory was unavailable; no exhaustive recovery claim is made.

**Required correction:** restore the three exact original files from a trusted backup and verify the hashes above. If originals are unavailable, the Owner must explicitly adjudicate an evidence-replacement/disposition rather than treating new bytes as the old receipt. Preserve the original ledgers, D19 package and all remaining failures. No code correction or new implementation cycle is needed or authorized by this finding. Until the preservation gate is restored or explicitly disposed of, D21 closeout is blocked. D17/D20's historical accepted decisions and CLOSED finding records remain unchanged; this review does not silently reopen or revoke them.

## ADR-0024 and precise remaining Owner decisions

### D21-OD-01 — Medium, governance — proposed higher-contract qualification needs explicit disposition before formal canonical closeout

**ADR-0024 correctly describes D21 in substance:** Markdown-only Slice 2, unchanged MD17-01–12 and D20, retained original history, explicit exclusion of other formats, no automatic return of those formats, no Java/publication acceptance and no broader Phase 18 waiver. No DBML, module-dependency, authentication or storage-strategy alteration is needed for that acceptance assignment.

However, its **Status is PROPOSED**. The proposed general rule that separately approved profiles determine a slice's formats and quality evidence, and its qualification of higher-level clauses, are not independently ratified merely because the preparation links to them. D21 approves the concrete Slice 2 rebase; it explicitly does not accept an overall Phase 18 rebase or rewrite frozen ADR history. The repository contract requires approved frozen-contract changes to be represented by ADR/canonical synchronization. **My review judgment is that the prospective higher-contract qualification should be explicitly adopted before formal canonical Slice 2 closure**, rather than treating the draft's timing/assertions as authority. This is the unresolved contract gate, not a demand to reapprove the already authorized Markdown scope or rerun excluded formats.

The exact retained clauses requiring a scoped applicability disposition are:

- [ADR-0021](../../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md), D18-A.4 and its implementation-acceptance proofs: complete protocol/model identity, producer provenance and physical publication remain overall integration obligations.
- [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), G2 and producer/finalization: all required format/language/OCR/table cases and FR18-3 remain unaccepted globally.
- [ADR-0023](../../../adr/0023-phase-18-application-level-snapshot-worker-trust.md), mandatory feature set and D16/D17 notices: its broad all-format target remains; D21 supersedes only the former Slice 2 format assignment.
- AC-01/02/04, the corresponding FCNC/IS format rows, and P18-FINAL's focused quality/model/resource/publication requirements remain broader obligations. No current Markdown test counts satisfy them automatically.

These clauses do **not** reveal a new missing Markdown producer feature. They require a clear distinction between an accepted bounded slice and the still-mandatory overall contract. It is valid to review the closeout while ADR-0024 is proposed; it is not valid for this reviewer or the preparer to accept that ADR. The draft's statement about review proceeding is credited; its suggestion to leave global-contract disposition until later is not used to self-authorize formal closeout now.

**Required Owner action:** explicitly approve the prospective slice-profile qualification in ADR-0024 for canonical synchronization, limited to D21 and retaining all stated security/provenance/resource/publication/final obligations, or provide different explicit higher-contract wording. Then record a separate bounded Slice 2 closeout decision. Approval of this qualification must not be recorded as approval of a Markdown-only **Phase 18 overall** target or as a general permission to weaken future quality gates. No implementation/remediation cycle is implied.

### Future overall-format decision — required before Phase 18 acceptance, not a new Markdown defect

The existing overall Phase 18 all-format target remains binding and unaccepted. Before P18-FINAL/Phase 18 acceptance, the Owner must explicitly choose an overall scope amendment or separately authorize any return of excluded formats. D21 assigns neither that work nor OCR to Slices 3/4. This future decision is distinct from D21-OD-01 and does not reopen D17/D20.

No proposed clause waives source authorization, immutable revision binding, locator correctness, cancellation, resource bounds, actual worker-output validation or trusted Java publication. Those guarantees must remain explicit when the Owner adopts the qualification. No new human acceptance gate is imposed: **D20 already resolves that policy decision for D17.**

## Lifecycle, publication and retained failures

The revised producer-local slice **can close without Java publication once its formal contract disposition is recorded**. The original G+H handoff §1 already excludes Java finalization/publication, generation/selection/version promotion and production selected-R consumption; §H permits honest GPU UNAVAILABLE/NOT_RUN evidence. Thus these are not newly hidden Slice 2 deliverables. D21 clauses 5/7 and the scope map explicitly retain them for separately authorized integration/final gates.

Current Java `RagDocumentFinalizationGate` still pins parser `ppv-parser-v18.1-pdfplumber-0.11.10` / chunker `structure-first-v18.1`; the candidate reports `ppv-parser-v18.6-d17-html-container-boundaries` / `structure-first-v18.5-compact-source-context`. Its new compact source metadata is not thereby trusted or admitted. Merely changing the allowlist would not satisfy FR18-3: actual text/locator/input/revision/closure/vector validation and PostgreSQL publication must still be proven. This review does not propose such a change. `RagSourceAuthority` still denies EXTERNAL_FILE indexing, and `RagSourceService` still disables live-file capability. Python's retained format dispatch is not evidence of an enabled external ingestion path.

The historical Java clean gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips, exit 1**. Its original evidence, unresolved aggregate PostgreSQL connection-resource investigation/repeat verification and invalid native batch provenance remain retained. D14 is not a global waiver. P18-FINAL still requires its five commands and focused quality, snapshot, publication, offline-model, resource and report evidence. The old `.venv` runner/environment must be reconciled at a separately authorized final run; this review did not substitute a filtered suite, change commands or call the final gate green.

## Independent verification and receipt reuse

New bounded receipts are in `logs/phase18/s2-d21-independent-closeout-2026-10-21/`. Every exact argv, cwd, wrapper budget, exit and receipt/log digest is recorded in the [evidence ledger](2026-10-21-slice-2-markdown-only-independent-closeout-evidence.json). All seven new executable watchdog receipts report **PASS / exit 0** for their executed commands. The end-of-review historical preservation audit is **FAILED** for E01; command success does not override that side effect.

| Receipt | Exact operation / result | Limit |
| --- | --- | --- |
| `integrity.json` | `python -X utf8 -B …/verify.py`: actual source/fixture/history/model hashes and entry inventory; PASS **at entry**, before the E01 overwrite | 120 s |
| `governance.json` | `python -X utf8 -B …/governance.py`: all 23 pre-D21 document bodies recovered/matched and 105 current links checked; PASS | 60 s |
| `producer-smoke.json` | `rag-engine/.venv-d17/Scripts/python.exe -m pytest logs/phase18/d17-r03-independent-2026-10-20/test_independent_r03.py -q -o pythonpath=rag-engine/src`: **25 passed in 8.41 s** | 90 s |
| `pip.json` | `uv pip check --python rag-engine/.venv-d17/Scripts/python.exe`: Python **3.14.4**, **67 compatible distributions** | 60 s |
| `lock.json` | `uv lock --directory rag-engine --check --offline`: **73 packages**, PASS | 60 s |
| `dependencies.json` | `…/python.exe rag-engine/scripts/dependency_inventory.py --output …/dependencies-results.json`: all 67 installed versions match lock; PASS | 60 s |
| `diff-check.json` | `git diff --check`: PASS, existing CRLF-normalization notices retained | 60 s |

The fresh test is a justified current-runtime CPU/JSON check, not a newly imposed acceptance program. It verifies source/chunker output, inside/outside HTML bindings, failures and original locators; grant/SQL staging remain explicit local captures, not Java/PG acceptance. Current denied roots emit zero chunks; independent successes remain separate. The original **170 regression**, **25 compatibility / 30 deliberate deferred-format deselections**, **25 fresh R03**, **1,267 supported + 62 required rejections** official corpus and Ruff/format/mypy receipts remain applicable because their source/configuration/artifact hashes match. Test groups are not claimed disjoint, and required rejections are not called successful extraction.

Resource evidence is reused honestly: the unchanged million-character repeated fixture has **689 chunks / TWO normalized texts**; prior separately authored unique content has **725 distinct chunks / 52.900 seconds** under the unchanged 60-second deadline. Prior independent singleton inference equivalence, copied vector ownership, cancellation, exact split ranges, independent compact-reference decoder, table serialization/allocation and scoped scratch cleanup remain applicable. Compaction reduced the unchanged near-cap table output from about **47.7 MB to 20.8 MB**, retaining verbose linear provenance; no universal performance, zero global OS writes or instantaneous native heap release is asserted. CUDA/native GPU acceptance is not fabricated. No million-character benchmark, full clean suite or OCR experiment was rerun.

## Delivery boundary and exact dirty-tree inventory

**No commit scope is currently eligible for owner-commit readiness**, owing to E01, the pending contract disposition and the deliberately mixed tree. No commit message is supplied. This does not undermine accepted D17 code; it prevents accidentally certifying excluded work through a whole-tree commit.

The [machine-readable scope inventory](2026-10-21-slice-2-markdown-only-independent-closeout-scope.json) supplies **every exact path and SHA-256** for **329 inherited dirty paths**, classified as:

- **22 Markdown implementation/test/fixture/support paths:** the three Markdown/HTML parsers, four D17 test files, both D17 fixture packages, five `d17_*` evidence scripts, dependency inventory and explicit development model-provisioning support. The latter is not runtime downloading.
- **12 shared/mixed files requiring an explicit hunk-level delivery boundary:** `chunking/chunker.py`, `embeddings/model.py`, `main.py`, `parsers/models.py`, `parsers/text.py`, `worker.py`, `limits.py`, `pyproject.toml`, `uv.lock`, `test_parsers.py`, `test_slice2_corrections.py`, `test_slice2_checkpoint_package.py`. Their Markdown/compatibility paths are reviewed; whole-file acceptance of Office/PDF/OCR/shared integration behavior is not implied. DOMAIN's text helper is preserved without certifying external TXT acceptance.
- **35 excluded-format/other unaccepted implementation paths:** Office/PDF/OCR modules, experiments, fixtures and native-OCR Java tests remain present. They are not deleted, staged, certified or marked PASS.
- **258 governance/history/documentation paths** and **two inherited policy paths** (`AGENTS.md`, `.gitignore`) remain separately identifiable. Historical documents can be retained as evidence without approving their old failed implementation or treating old launch text as current authority.

The selected Python dependencies are verified, not upgraded. Shared dependency files retain the explicitly deferred OCR environment group and other-format libraries. Models, venvs and logs remain ignored. After Owner contract disposition, an exact deliverable commit plan must name the intended Markdown/shared hunks and retained-history policy; the present file inventory is **not** an instruction to stage all members. Three new review artifacts accompany this report, including the report itself, its evidence ledger and the scope inventory.

## Shared 29-dimension ledger

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | PASS — explicit D21-only independent governance/closeout review. |
| 2 | Traceability | PASS executable map; FINDING formal higher-contract disposition D21-OD-01. |
| 3 | Architecture consistency | FINDING — ADR-0024 remains proposed; original higher-contract clauses are preserved and require scoped Owner disposition. No code architecture defect found. |
| 4 | Module ownership | PASS scoped — no new module/API/persistence ownership change; producer authority remains bounded. |
| 5 | Recursive tree | PASS — exact source/fixture/entry/dirty-path inventories, current changed-package inspection. |
| 6 | Shared contracts | PASS scoped — existing root ApiResponse/ADR-0016 and Python DTO/error boundaries retained; no common-module invention. |
| 7 | SOLID/cohesion | PASS retained implementation — parser/chunker/model/worker responsibilities unchanged. |
| 8 | DRY/KISS | PASS — reuse accepted producer path and receipts; no second parser/consent engine. |
| 9 | Patterns | PASS — no speculative framework introduced by D21/review. |
| 10 | Business/outcomes | PASS producer-local — actual complete-root failure and independent-root accounting; no publication claim. |
| 11 | API/wire compatibility | PASS bounded compatibility; future Java identity/output admission explicitly unaccepted. |
| 12 | Validation | PASS D17 — exact locators, typed negative/unsupported-content and bounded-source tests retained. |
| 13 | Privacy/consent | PASS scoped — supplied child payload authority, revisions and immutable source bindings; DOMAIN/Slice 1 rules unchanged. |
| 14 | Secrets/egress/artifacts | PASS scoped — actual pinned local artifacts, inert content, safe failures; no new fetch or execution. |
| 15 | Transactions/concurrency | N/A new SQL work — no changed/enabled publication capability. Frozen guards retained; FR18-3 remains open. |
| 16 | Failure/recovery/cancel | PASS D17 local controls and preserved receipts; no new end-to-end recovery acceptance. |
| 17 | Database/migrations | N/A D21 changes — no migration/entity edit; aggregate PostgreSQL liability retained. |
| 18 | Complexity/resources | PASS measured D17 scope — unchanged finite admission/deadline and independent allocation/scratch evidence, explicit limits. |
| 19 | Index/source quality | PASS source/locator/closure fidelity; retrieval and trusted published-index quality not in this slice. |
| 20 | Document/multilingual fidelity | PASS Markdown under D17/D20; excluded formats explicitly not certified. |
| 21 | Security tests | PASS applicable producer denials/cancellation; no superseded OS isolation proof required. |
| 22 | Tests/evidence quality | FINDING E01 — passing fresh tests overwrote three historical raw captures; entry verification and current failures are distinguished. Machine behavior remains verified. |
| 23 | Performance evidence | PASS reused applicable bounds — repeated and unique workloads distinguished; no universal throughput claim. |
| 24 | Evaluation independence | PASS under explicit D20 — prior independent adjudication sufficient, no fabricated human/held-out approval. |
| 25 | Dependencies | PASS selected 3.14.4 environment/model/lock; excluded OCR preserved, no upgrade. |
| 26 | Build/static/tooling | PASS executed new checks and reused unchanged static receipts; historical Java full gate remains FAILED. |
| 27 | Observability | PASS — exact argv/receipts/hashes, safe CPU/RAM/VRAM/temp measurement limits and retained failed evidence. |
| 28 | Documentation/governance | FINDING — E01 history preservation failure plus explicit ADR-0024 disposition required; D20 and original ledgers remain unchanged. |
| 29 | Hygiene/delivery | FINDING E01 — three ignored raw artifacts not preserved; tracked/untracked implementation and history bodies otherwise intact; no staging/publishing. |

## Decision and next Owner action

**CHANGES_REQUESTED — S2-D21-E01 blocks evidence preservation; D21-OD-01 remains an Owner contract decision.** There is no new Markdown implementation regression or human-signoff blocker under D20. The reviewer-introduced loss of three raw historical captures prevents a preservation PASS. Do not misattribute that loss to the implementer or start another implementation correction cycle.

**Next step:** restore the three original captures from a trusted backup and verify their original hashes; if unavailable, Owner explicitly decides their evidence disposition. Also obtain Owner disposition of ADR-0024's prospective higher-contract qualification, preserving D21's Markdown-only acceptance and every retained Phase 18 security/provenance/resource/publication/final obligation. Then record the separate bounded Slice 2 closeout and establish an exact mixed-tree delivery scope before any owner-commit gate. The later overall-format target remains a separate decision before Phase 18 acceptance; no excluded work or Slice 3/4 is authorized here.

Phase 17 **FROZEN**; Slice 1/D14 **CLOSED**; D17/D20 **ACCEPTED**; D21 **Markdown-only**; FR18-3 **OPEN**; external indexing **INACTIVE**; live originals **DISABLED/DEFERRED**; original broader target and P18-FINAL **NOT ACCEPTED**; historical Java gate **FAILED**; Slices 3/4 **NOT AUTHORIZED**. No implementation fix, dependency upgrade, OCR work, ADR approval, Slice 2 closure, READY_FOR_OWNER_COMMIT, staging, commit, push, tag or PR was performed.
