# Phase 18 Slice 4 / P18-FINAL — independent D26 CPU-only final review

**2026-10-25 — READY_FOR_OWNER_COMMIT.**

All applicable Slice 4 and P18-FINAL requirements are independently verified under **Owner-approved D26 pinned offline CPU-only scope**. No implementation, documentation or mandatory evidence blocker remains. This is commit readiness, **not** Owner acceptance/closeout of Slice 4, P18-FINAL or Phase 18, and not indexing activation.

GPU truth is unchanged: actual CUDA inference, GPU OOM-to-CPU fallback and process-attributed GPU VRAM remain **NOT VERIFIED**. Their prospective acceptance status is exactly **NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE**. None is PASS, measured zero or an inferred fallback. Future GPU enablement requires new explicit Owner authorization and independent verification.

Review only: no production/test/runtime/dependency/contract correction, installation, test-suite rerun, staging, commit, push, tag, PR, activation or phase closure. Only this review, its machine evidence/delivery inventory and ACTIVE's additive independent disposition are written.

## Authority and actual candidate

Read AGENTS, ACTIVE, D24/D25/D26, the [canonical D26 applicability](../cpu-only-d26-applicability-2026-10-25.md), D26 handback/evidence, applicable ADR-0021–24, S4 handoffs/matrices/execution-resource plan, AC/FCNC/IS and P18-FINAL contracts. Reconciled the original independent review, corrective/Java fixture/triage/timeout/instrumented handbacks and the [prior independent final corrective re-review](2026-10-25-slice-4-independent-final-corrective-re-review.md), including original raw receipts and their limitations.

HEAD is **`ebe125f7529124b62761e8d01c692cbbd28e4aa7`**, with the reviewed dirty Slice 4 candidate. Accepted Slice 3 source remains `e2e00534fb35534a298e8fb15d174f18cb63b5a0`. All **1,732** final successful Java source-manifest entries independently match current files; source/configuration/fixture drift is **zero**. Model/tokenizer/lock/runner/fixture/pom/pyproject hashes match. Selected runtime is still Python **3.14.4**, 52 distributions at applicable locked versions, ORT **1.31.0**, Azure/CPU providers and no CUDA provider.

This is the explicitly approved D26 governance/final-review scope permitted by `codex-final-review`; it does not require restarting the implementation handoff. Reused the prior 29-dimension/source/native review where hashes and applicability match. No closed finding is reopened without regression evidence. The D26 amendment changes acceptance applicability only, not database/module/package/security/storage architecture or frozen Phase 17. No new DBML/diagram/runtime change is needed for D26.

New exclusive raw root: **`logs/phase18/s4-d26-independent-20261025/`** (`O`). The [machine evidence](2026-10-25-slice-4-d26-independent-final-evidence.json) records independent checks and exact hashes; the [delivery manifest](2026-10-25-slice-4-d26-reviewed-delivery-manifest.json) enumerates exact reviewed paths, statuses, hashes and line endings. These do not stage anything.

## D26 independent applicability verdict

**VERIFIED / valid and sufficiently bounded Owner authority.** D26 expressly selects option B of the prior review; D26 is uniquely recorded after D25 with a stable explicit anchor. It approves the CPU-only execution profile, not technical GPU success or phase closeout. It retains D24 `.md` scope, all twelve D17 requirements, actual trusted publication, CPU resources and all five final commands. D24/D25 and earlier acceptance/failure history remain intact.

Independently checked all **16** controlling amendments, not just the canonical matrix. At review entry each file matches its recorded D26 hash; removing its exact prefix reconstructs the original file hash, also matched against the **previous independent review's** entry evidence (and its recorded post-review ACTIVE hash). Original file bodies are therefore preserved **byte-for-byte as suffixes**. ACTIVE receives only the subsequent additive review disposition. All 41 new prefix/profile/handback links resolve; the D26 anchor exists. Every affected header preserves NOT VERIFIED, the exact N/A token and separate future Owner authorization.

| Controlling amendment | Exact retained boundary / affected GPU clause |
| --- | --- |
| Owner Decisions | D26 prospective option B only; unchanged D24/D25 suffix. |
| ACTIVE | CPU-only final review routing; historical GPU/failed/timeout notices retained. |
| ADR-0021 | D18-A.4 / linked acceptance: exact model/producer/Java/PG tuple unchanged. |
| ADR-0022 | N10 / remaining GPU gates only; snapshot, consent, seven-predicate NO_CHANGE and physical finish retained. |
| ADR-0023 | Feature-set GPU/fallback/VRAM and retained N10 only; application-level trust/SQL/source/process safety retained. |
| ADR-0024 | Explicit overall execution-profile approval; D21/D22 slice-only and D24 overall format history retained. |
| D24 scope/baseline record | GPU portions of AC-05/06/07/08 and resource boundary only; `.md` applicability unchanged. |
| Slice 4 preparation handoff | Package D requires complete measured CPU/native resources; no GPU prerequisite for this new profile. |
| Executable handoff | Same CPU scenarios/limits/stop/cleanup; only old GPU stop condition superseded. |
| Slice 4 acceptance matrix | S4-15 CPU/native mandatory; GPU subcases exact N/A, not PASS. S4-14/17/18 guarantees retained. |
| Execution/test/resource plan | S4-15, AC-05 and GPU protocol qualified; cold/warm/child/mixed/reindex/near-bound/cancel/failure scenarios remain. |
| Acceptance-and-evidence | AC-05, GPU portions of AC-08/FCNC-15/IS-26/27/30; CPU vectors/identity/resources/reporting retained. |
| Final-verification-gate | OFFLINE-MODEL/RESOURCES/REPORT GPU portions only; exact executable block unchanged. |
| Phase 18 README | Current execution/evidence routing to D26, not self-acceptance. |
| Phase 18 test-evidence | Same D26 routing with all historical receipts retained. |
| RAG phase plan | Phase 18 CPU-only profile only; no Phase 17 or later-phase authorization change. |

The canonical clause reconciliation matches these amendments. Pre-D26 GPU-blocked records remain true dated history, explicitly distinguished from current applicability. Older dated AGENTS/handoff-navigation preparation notices still link canonical ACTIVE and do not override the later explicit D26 authority. No contradictory **effective** GPU requirement or obsolete active D26 pointer was found. Historical NOT_RUN/FAILED wording is not silently edited into success. ACTIVE's final review notice supplies the subsequent current readiness status.

Unchanged executable P18-FINAL code block SHA-256: **`bc4582d5c5a66b714c11a27b1ec5acac85fc5f7484080b056a2c239236f792c9`**. D26 does not waive a failed command, expand a timeout, change a skip, weaken a security check, change model/vector space or authorize remote execution. Installed Azure-provider availability is not permission for remote inference. No unresolved Owner contract decision remains within this reviewed CPU-only profile.

## Findings — final scoped dispositions

| Finding | Final independent disposition |
| --- | --- |
| **S4-F01** | **CLOSED, preserved.** Full exact selected-R/child/current-G authority and durable existing-row C_N, no new R/G/sourceVersion/job/worker. Original changed-child/stale-G counterexample rejects; physical outcome/restart proof remains applicable. |
| **S4-F02** | **CLOSED, preserved.** Immutable full roster and explicit Java-proved noChangeRoots bind the smaller worker plan. Pending-root omission/reordering rejects before dispatch; invalid unchanged proof rolls back mixed C_P. |
| **S4-F03** | **CLOSED, preserved.** Six qualification/revision fields reach authenticated HTTP DTOs; full cancellation/failure/unknown rosters and independently qualified retained G are reported. |
| **S4-F04** | **CLOSED, preserved.** Exact cleaned install, 17 deletions and retained-consumer manifest; no renewed excluded-format intake. |
| **S4-F05** | **CLOSED, preserved.** Real controlled restore/revoke/retry/recovery/C_N/C_P/HTTP evidence and source-applicable genuine final gate, with truthful limits. No missing test is inferred from counts. |
| **S4-F06** | **CLOSED FOR D26 CPU-ONLY ACCEPTANCE.** CPU/native resources were independently VERIFIED before D26 and remain applicable. GPU evidence is **NOT VERIFIED / NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE**. This resolves applicability, not the technical GPU evidence gap; the old OPEN/GPU_EVIDENCE_BLOCKED finding is preserved. |
| **S4-F07** | **CLOSED, preserved.** Exact ADR-0023 historical opt-in diagnostic remains skipped by default; original assertion/opt-in failure preserved. Current genuine Java command passes. No D14 global waiver. |
| **S4-N01** | **CLOSED, preserved.** Current `git diff --check` exits zero; CRLF advisories are separate. |

## Full final S4-01–18 matrix

VERIFIED denotes the applicable independent requirement, not formal Owner closeout. Prior source-backed/native proofs are reused by identity, not newly executed or double-counted.

| Row | D26 final disposition | Decisive applicable proof |
| --- | --- | --- |
| **S4-01** | **VERIFIED** | Genuine equal capture/all-unchanged operation preserves R/G/version and creates durable nullable-job C_N without worker/indexing lease. Exact source-first reservation/physical finish. |
| **S4-02** | **VERIFIED** | Root expectations/integrity/consent/pipeline controls; changed child, four child/edge CAS faults, retained G/pipeline post-replay faults and omitted pending-root/order negatives. All seven predicates remain required. |
| **S4-03** | **VERIFIED** | Actual native same-R new-G publication with approved nested children and originals deleted; R/sourceVersion retained, trusted verification before C_P. |
| **S4-04** | **VERIFIED** | Failed reindex retains only independently eligible prior G; five physical rollback locations, revocation and forged vector/provenance rejection. |
| **S4-05** | **VERIFIED** | Exact selected bytes/hash/length, dependency graph, consent/reset/epochs/history and pinned pipeline at use; tamper/child/restart tests, no cached terminal-job CURRENT shortcut. |
| **S4-06** | **VERIFIED** | Deleted/changed originals never silently reopened; HTTP RAG_SNAPSHOT / SNAPSHOT_CURRENT with original UNVERIFIED and selected/pending R. |
| **S4-07** | **VERIFIED** | Both C_N/revoke durable orders, transitive denial/purge/retry, sibling preservation and anti-resurrection. Not instantaneous backup/WAL erasure. |
| **S4-08** | **VERIFIED bounded restore scope** | Controlled DB row-image versus missing files, restored files versus revoked DB; no orphan adoption/stale reapproval. New explicit authority required. Not physical PITR or malicious coordinated rollback proof. |
| **S4-09** | **VERIFIED** | Actual producer/transfer/staging/verifier cancellation, durable job intent, C_N finish/precommit cancellation, fresh-JVM live-owner refusal/dead-owner recovery and positive stop/fencing. |
| **S4-10** | **VERIFIED** | Real committed/precommit C_P and C_N wire cuts, deferred rollback/DB outage; durable reconciliation, one/zero commit, no blind retries/duplicate G or unsafe early release. |
| **S4-11** | **VERIFIED** | Actual HTTP/PG mixed SUCCESS/FAILURE/NO_CHANGE, all-unchanged operation, full cancel/abort/unknown rosters, retained G; established 8/2 and all-failed cases remain in complete gate. |
| **S4-12** | **VERIFIED bounded fault/cleanup scope** | Owned temp cleanup/capture cancellation, injected controlled I/O/erasure failure and retry, managed storage recovery/process stop. Not physical host disk exhaustion or zero global OS writes. |
| **S4-13** | **VERIFIED** | 17 exact excluded adapters/scripts/tests/fixtures remain deleted, no obsolete execution/import/install/locator route; retained DOMAIN text helper/inert tuple/history justified. |
| **S4-14** | **VERIFIED** | Python 3.14.4, exact applicable cleaned lock/install, pinned offline CPU artifacts/finite 384-vectors and strict captured `.md` identity; DOMAIN/v2 and cross-type denial preserved. |
| **S4-15** | **VERIFIED UNDER D26** | Applicable CPU/native source-bound representative resources, finite limits, cancellation/identity/cleanup are VERIFIED. GPU-specific subcases remain NOT VERIFIED with exact D26 N/A status, not PASS. |
| **S4-16** | **VERIFIED tested configuration** | Independent pool-capacity mechanism plus test-only Hikari limits and genuine complete aggregate; live PG observations retained. No production pool/discovery/parallelism weakening. Historical slowdown cause remains UNKNOWN. |
| **S4-17** | **VERIFIED** | All unchanged five commands genuinely exit zero on applicable source; exact Java/Python counts and sole historical diagnostic skip audited below. |
| **S4-18** | **INDEPENDENT GATE VERIFIED / READY FOR OWNER COMMIT** | This independent D26 final review completes the reviewer gate. Explicit Owner delivery/acceptance/closeout remains procedural and unperformed. |

AC-05 remains pinned multilingual offline CPU/model/tokenizer identity, source-derived trusted verification, compatible finite 384-dimensional pgvector. AC-08 and FCNC-15/IS-26/27/30 retain exact authority, NO_CHANGE/reindex/lifecycle, CPU/RAM/disk/deadline/cancellation/report/cleanup evidence. D17/D20 quality stays accepted without fabricated human labels. No security, provenance or Java/PostgreSQL obligation moves to a later slice.

## Actual P18-FINAL receipts and applicability

Reopened and checked original executable receipts/logs, archived full Java XML/JAR and Python XML rather than relying on D26 handback claims. All 1,732 successful-gate source entries match; successful Python manifests have zero current Python drift. No expensive unchanged gate was rerun for this documentation-only scope amendment.

| Operation | Applicable original result |
| --- | --- |
| Ruff `check .` from rag-engine | `final-ruff.json`, **exit 0**; prior independent Ruff also exit 0. |
| Ruff `format --check .` | `final-format.json`, **exit 0**, 67 files; prior independent format also exit 0. |
| Mypy `src/ppv_rag` | `final-mypy.json`, **exit 0**, 22 source files with isolated fresh cache; prior independent fresh-cache check also passes. |
| Full pytest `-q` | `final-pytest-v2.json`, **exit 0**; independently recounted **238 actual testcase elements, 0 failures/errors/skips**. Python tree unchanged. |
| Maven full `clean verify` | `s4-java-instrumented-20261025-201e93c2/full-java.json`, **exit 0 / PASS / BUILD SUCCESS**, **1,937.915s** within **2,100s**, cleanup not needed. |

Python raw root is `logs/phase18/s4-corrective-20261024/`. Java raw stdout/stderr and final target ZIP are under `logs/phase18/s4-java-instrumented-20261025-201e93c2/`. Exact wrapper/argv/cwd/profiles are preserved in machine evidence. The Java command is:

```text
mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED clean verify
```

| Independent Java archived count, 142 XML files | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| Actual testcase elements / final stdout | **1,580** | **0** | **0** | **1** |
| Sum of suite attributes | **1,567** | **0** | **0** | **1** |

The **13** difference is actual nested-class method-name grouping: Collection 2, Fiction 3, Knowledge 1, Location 6, Media 1. Exact collision groups were independently reconstructed again. The sole skip is `attemptsAliasCreationAndRemovalDuringProtectedLifetime`, the pre-existing ADR-0023 opt-in diagnostic. No new selection/exclusion/skip, assertion relaxation or timeout increase. Clean, compilation, Surefire, JAR/Boot repackaging and requested verify endpoint completed; no nonexistent extra verify plugin is claimed. Archived JAR SHA is `ad6a380e7d313b7429ff4a69ca60d182a2a664cccb35528848bfc7a8c8cd3bde`.

The narrow fixture coordination still matches only the frozen query-budget method and holds the existing recovery monitor; it does not disable scheduling or alter counters/assertions/production code. Its raw hash and the frozen test remain unchanged. The prior independent **36 native PASS**, seven cleanup PASS and static/lock results remain applicable, not newly executed here. They tested original changed-child/pending-omission/cancel-report counterexamples, physical C_N cuts/rollback/new JVM, native vector forgery and actual SQL privilege denial. D17 corpus proof remains separate from native SQL proof; no corpus storage mock is substituted for publication verification.

P18-FINAL executable and applicable focused evidence is **VERIFIED for Owner disposition under D26**. Earlier failed/timeout receipts remain FAILED/TIMEOUT, including the partial 2,100-second run. A successful later run does not establish the earlier slowdown's missing cause. D14 is not used as a final-gate waiver; D26 changes only GPU-specific applicability.

## Resource, CPU/GPU and preservation audit

Retained independently verified CPU resource evidence covers actual Java → native producer → trusted verifier → PostgreSQL, approved children, shared-session cold/warm roots, mixed roots, same-R reindex, cancellation/error and near-bound work. The prepared near-bound input is **1,020,964 bytes / 97.3667%** of the unchanged 1 MiB ceiling, not a universal million-unique-character claim.

The 104-case measured window remains **669.663s wall / 958.641 owned-tree CPU seconds**; simultaneous sampled working set **2,139,361,280 bytes**, private memory **2,309,246,976**, host minimum available **1,682,743,296**. Owned temp peak **12,283,399**, managed temp **10,896,701**, revision storage **512,193** bytes; no observed owned survivor/managed capture temp, with three retained fixture files disclosed. Sampling targets/gaps, individual lifetime peaks, JVM heap points, host/Docker/observer boundaries and injected-I/O limitations remain explicit. No universal resource maximum or zero global OS writes is claimed.

The successful aggregate sampled approximately **3.014 GiB gate-owned working set**, **3.483 GiB private**, 21 main-PG client sessions excluding observer, and tight host free memory (minimum **268,680 KiB**). Different stopped clean-startup DB warnings/pool waits are retained, not hidden or equated with main DB exhaustion. These bounded tested observations and prior 95→12 idle-connection experiment support S4-16; they do not prove absence of every future leak/contention scenario.

Device-wide VRAM observations remain **device-wide**. Process-attributed GPU memory is UNKNOWN/NOT VERIFIED. CPU provider/model identity and actual offline inference are verified; CPU startup is not GPU fallback. D26 removes only the three named GPU acceptance subcases from this final profile. No unexecuted GPU code is certified, provider/dependency changed or GPU experiment performed.

All 17 cleanup deletions remain absent. `.md` is the sole external format; excluded aliases/TXT/Office/PDF/OCR routes do not reappear. The exact inert `rapidocr-1.4.4-en-hans-script-admission-v18.2` tuple remains compatibility metadata without OCR installation/execution. DOMAIN `parse_text` remains required. Current 52 installed distributions match their applicable locked versions; the remaining two lock entries are the virtual project and Emscripten-only package. Historical mixed fixtures and reviews are evidence, not executable format certification.

| Independently checked artifact | SHA-256 |
| --- | --- |
| Final-source manifest, 1,732 entries | `015cc6d027b15c3b0743fcf434a9371edb60fff6e29b4304bafe12a2ef0bf478` |
| model.onnx | `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` |
| tokenizer.json | `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` |
| uv.lock | `5d5ab94a66875c51c35840e37aec76a2efa5e193a1fa1210347691c96c3517cc` |
| AbstractRagIntegrationTest fixture | `9291ce7b3b0c4ae753873cbd3108ebf85b802cef554317f393953f9cbb97bb47` |
| Bounded command runner | `e9643978b8dc708b86b4014bc83805e44336fe16fee796c901c564d69cdf058d` |

Original independent review conclusions, GPU_EVIDENCE_BLOCKED records and failed/timeout evidence are unchanged. All D26 raw-artifact hashes were independently checked. Entry inventory contains **2,525 nonignored files** and **83,981 existing ignored evidence assets**; preservation is recorded in machine evidence. HEAD/index/source remain unchanged by review; only ACTIVE's additive disposition and three new review records differ. D22 originals remain **NOT_RECOVERED / ORIGINAL_PRESERVATION_FAILED**; current preservation is not recovery or retrospective PASS.

## Review coverage and remaining findings

The prior actual recursive inventory (110 RAG Java production / 50 RAG Java tests / 22 Python production / 13 scripts / 67 Python test/fixture files) remains source-applicable. The shared 29-dimension ledger is retained with this final applicability update:

| Dimension | Final status and basis |
| --- | --- |
| 1 Scope | PASS — D24/D25/D26 valid, CPU-only/profile boundaries explicit. |
| 2 Traceability | PASS — 16 amendments, canonical clause matrix and full S4 mapping. |
| 3 Architecture | PASS — no D26 schema/module/security/storage change, original guarantees retained. |
| 4 Module ownership | PASS — unchanged reviewed source and applicable architecture gate. |
| 5 Recursive structure | PASS — prior full tree/source inventory remains exact; no new implementation. |
| 6 Shared contracts | PASS — root ApiResponse and corrected module DTOs unchanged. |
| 7 SOLID | PASS — canonical no-change service/cohesion reused, no new abstraction. |
| 8 Principles | PASS — exact proof/plan rules retained; no duplicate governance authority. |
| 9 Patterns | PASS — existing guards/repositories/services, no new engine. |
| 10 Domain semantics | PASS — C_N/reindex/G/currentness/outcomes reviewed and unchanged. |
| 11 Wire | PASS — authenticated full roster/qualification/operation transport proof retained. |
| 12 Validation | PASS — exact source/child/result/vector negatives unchanged. |
| 13 Authorization | PASS — source-first guards/revoke/least privilege; D26 waives none. |
| 14 Privacy/offline | PASS — pinned local CPU; no remote permission or OS sandbox claim added. |
| 15 Transactions | PASS — actual C_N/C_P cuts/rollback/fencing and physical publication retained. |
| 16 Recovery | PASS — bounded restore/restart/cancel/cleanup proof, limits disclosed. |
| 17 Database | PASS — V1–V22 preserved, V23 narrow function/ACL and native privilege evidence. |
| 18 Resources | PASS for applicable bounded CPU scope; GPU N/A under D26 and NOT VERIFIED. |
| 19 Index quality | PASS for Phase 18 exact eligible G; retrieval/reranking N/A to this slice. |
| 20 Fidelity | PASS — accepted D17/D20 identity and applicable corpus/native evidence; excluded formats N/A_BY_D24. |
| 21 Security testing | PASS — actual native/provenance/vector/role/authority denials retained. |
| 22 Test quality | PASS — behavior-backed prior independent checks, original XML audited; no count-only inference. |
| 23 Benchmarks | PASS bounded CPU evidence with sampling limits; no GPU/universal performance claim. |
| 24 Adjudication | PASS accepted D17/D20 reuse; no fabricated human labels/signatures. |
| 25 Dependencies | PASS — exact applicable install/lock/model hashes unchanged. |
| 26 Build/static | PASS — five exact source-applicable commands, current diff hygiene. |
| 27 Observability | PASS — raw identities/resources/warnings and unknown attribution preserved. |
| 28 Governance | PASS — Owner resolves prior sole decision prospectively; no phase self-closeout. |
| 29 Delivery | PASS — exact path/hash/CRLF inventory, no unrelated hunks or index mutation; Owner staging pending. |

**Remaining blocking findings: none under D26.** Genuine CUDA inference/fallback/attributed VRAM remains unverified outside this accepted execution scope; it is not technical debt silently waived for an enabled GPU capability. Future enablement is separately Owner-gated.

## Reviewed Owner commit boundary and next action

The explicit delivery manifest contains **120 paths**: **117 entry dirty paths plus these three final review records**. Entry is **68 modified, 17 deleted, 32 new**; final review adds three new documentation/evidence files. Scope comprises:

- 23 Java production/V23 paths, 21 Java test/fixture paths, 19 Python implementation/tooling paths, nine Python test/fixture paths and two dependency files (including their recorded deletions).
- 43 existing dirty/new governance/history/evidence paths, including all 16 controlling D26 amendments, the D26 profile/handback/machine evidence, S4 preparation/correction/triage/timeout/full-gate handbacks and all independent review records.
- This final review, its machine evidence and exact delivery manifest.

All these paths are in the reviewed S4/D26 scope. Whole-file staging is eligible **only for the individually enumerated paths at their reviewed contents**; no unrelated mixed hunk is included. Root AGENTS' only entry diff is the dated two-line D25 preparation notice, with no inherited policy rewrite. Non-RAG Flyway/portability test hunks only update the actual expected migration tip to V23. Shared application/Python files are the previously independently reviewed lifecycle/strict-MD/cleanup corrections; D26 introduces no new source hunk. Do not stage the entire workspace or any directory.

**Excluded from Git delivery:** ignored `logs/`, generated `backend/target`, `.venv-d17`, model binaries/caches, temporary database/process/resource output, diagnostic agent/probe bytecode, observation scripts under logs, host/model/download artifacts and owner data. They are not automatically stage-eligible because referenced by evidence. Preserve raw receipts/XML/ZIP/resources and historical captures in the existing controlled evidence archive separately; the tracked governance/evidence JSON and review records preserve the indices, hashes, exact outcomes and limitations. Do not force-add ignored trees. No evidence deletion or fictitious D22 recovery is part of this commit.

Owner should verify HEAD and the manifest hashes, explicitly stage only enumerated changed/new/deleted paths, inspect the exact staged diff/path set (including CRLF/fixture bytes), and commit/push if satisfied. Review records' self-referential hashes are recorded externally in O's final summary rather than claiming impossible embedded self-hashes. A changed source/runtime candidate requires affected revalidation; this readiness does not cover later edits.

Use exactly this Conventional Commit message:

```text
feat(rag): complete Markdown snapshot lifecycle under D26
```

**Next step:** Owner reviews and commits/pushes this exact boundary, then supplies the resulting package for a separate explicit Slice 4/P18-FINAL/Phase 18 acceptance and closeout decision. This review performs none of those actions. Phase 17 FROZEN; Slices 1/D14, 2/D23, 3/D25 and FR18-3 CLOSED; D24 `.md` only; D22 originals NOT_RECOVERED. External indexing remains INACTIVE, live originals DISABLED/DEFERRED, and Slice 4/P18-FINAL/Phase 18 remain NOT ACCEPTED / NOT CLOSED pending Owner disposition.
