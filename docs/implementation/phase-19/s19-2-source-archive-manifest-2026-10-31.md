# Suspended S19-2 source archive manifest — 2026-10-31

**Disposition:** `READY_FOR_OWNER_ARCHIVE_COMMIT`. This is source archival under the [one-off Owner decision](owner-decision-s19-2-source-archival-only-2026-10-31.md) and [task](s19-2-source-archival-task-2026-10-31.md), **not implementation, independent acceptance, release, deployment or a new handoff**. The [suspension](owner-decision-s19-2-suspension-2026-10-30.md) remains controlling: `NO_ACTIVE_HANDOFF / S19-2_SUSPENDED_BY_OWNER / NOT_ACCEPTED`.

## Baseline and inventory basis

- Local branch/HEAD at entry: `main / d5e0a044b98652ae79faffa2caa1588645c12a81`; index empty. No staging, commit, push, tag, PR or branch creation by Codex.
- Accepted S19-1: `5e84e7096137b15528bb09114d7358f719c7bdc7`; docs closeout `beda8211f2b6244bdfc31f5b07fe5a3df6e7432b`. Local ancestry was checked, and both committed `backend` and `rag-engine` source subtrees at HEAD match the accepted implementation. Publication authority comes from the preserved Owner records; no new remote-publication check was needed or asserted.
- The actual working tree, empty index, all untracked paths and ignored directory inventory were inspected before editing. The supplied `personal-private-vault.zip` was not used to infer completeness. A read-only entry snapshot captured 2,786 project-file hashes and 29,999 existing S19-2 evidence-file hashes.
- Classification: accepted S19-1/frozen files remain the baseline; the listed new QA files and shared-file deltas are unaccepted S19-2; the listed documents preserve suspension/archival authority and historical S19-2 decisions/reviews; excluded unrelated/generated/sensitive material stays local.
- No S19-2 migration, backend dependency, CI, IAM/security or frozen-schema change is present. Existing migration V24 and all earlier migrations are preserved. No repository tree/manifest or frozen architecture artifact was rebased.

## Default-off boundary and narrowly authorized edits

All five Spring components — `RagQaController`, `RagQaService`, `RagQaSupervisor`, `RagQaContextPacker`, `RagQaValidator` — now use:

```java
@ConditionalOnProperty(name = "ppv.rag.test-scoped-execution", havingValue = "true")
```

This reuses the existing test-only control, missing/false by default. No new production enable switch was introduced. Even `ppv.rag.qa.enabled=true` and CUDA QA settings alone cannot register those components. Test-scoped execution grants no current permission to activate or evaluate QA.

| Boundary | Evidence and consequence |
| --- | --- |
| QA HTTP registration | Real MVC mapping check imports all five QA component classes and the unchanged S19-1 controller. QA answer/cancel mappings are absent and both requests return 404 in that isolated MVC context. Normal application authentication may separately reject a request before routing. |
| Generator/native worker startup | No QA service/supervisor bean exists under default/non-test settings; the QA supervisor is the only production Java reference to `ppv_rag.grounded_qa`. Default Python modules do not import either QA module. No generator, model, native child or CUDA runtime was initialized by this task. |
| Shared retrieval/recovery hooks | Existing uncommitted `RagRetrievalService.java` and `RagRecoveryService.java` use optional `ObjectProvider<RagQaService>`; the absent QA bean resolves to null. Retrieval sender exclusion and reader quiescence use the original retrieval path when QA is absent. Their pre-task deltas were inspected and are retained without additional edits. |
| Shared ingress | Existing S19-2 extension in `RagRetrievalIngressFilter.java` adds a QA body-size check; it does not register a route or launch a worker. The accepted retrieval branch is unchanged. This pre-task delta is retained. |
| Accepted S19-1 surface | Both retrieval/citation response and cancellation routes remain mapped. The actual retrieval controller's cancel endpoint returns 200 through a mocked retrieval collaborator. The default OpenAPI inventory was restored to its exact HEAD content: 230 accepted operations, excluding the two unaccepted QA routes. |
| External indexing/live originals | The unchanged accepted `RagWorkerSupervisor` remains contained by `ppv.rag.test-scoped-execution:false`. No indexing activation, consent/source mutation or live-original capability change occurred. |
| Python dependencies | QA remains an optional extra; default dependency declarations and all previously locked third-party packages are unchanged. Four new packages belong to the optional QA extra. No install, download, lock regeneration or runtime repair occurred. The existing unaccepted `mypy cache_dir = "nul"` setting is retained as configuration, without further tuning. |

The only production behavior edits made by this task are the five QA registration conditions. The OpenAPI test restoration aligns the default accepted surface with that isolation. All other task source changes are comments/docstrings.

The exact archival label `P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY`, with the Owner-decision path, appears at these boundaries:

- Java: `api/RagQaOperations.java`, `internal/application/qa/package-info.java`, `RagQaService.java`, `RagQaSupervisor.java` and `internal/web/controller/RagQaController.java` under the Java production prefix below. The packer/validator inherit the package label and each has the actual registration guard.
- Python: `rag-engine/src/ppv_rag/grounded_qa.py`, `qa_generation.py`, and `rag-engine/scripts/provision_qa_model.py`.
- Configuration: `backend/src/main/resources/application.yml` at the QA block; `rag-engine/pyproject.toml` at the optional QA extra.
- Tests: `RagQaNativeIntegrationTest.java`, `RagQaEvaluationNativeIntegrationTest.java`, `RagQaCudaNativeIntegrationTest.java`, `RagQaProspectiveEvaluationNativeIntegrationTest.java`, and the new `RagQaArchiveGuardTest.java` under the Java test prefix below; `rag-engine/tests/test_grounded_qa.py`.

S19-1 and the whole RAG module are not labeled suspended. Historical QA harnesses remain unfinished source and are **not authorized to run**.

## Bounded check results and limits

| Check | Actual result |
| --- | --- |
| Narrow Spring/MVC archive guard | PASS: `RagQaArchiveGuardTest`, 3 tests, 0 failures/errors/skips; default settings, explicit non-test settings, and QA enablement without test scope. |
| Exact test invocation | `mvn.cmd -o -ntp -f backend/pom.xml -Dtest=RagQaArchiveGuardTest -DargLine=-Xmx512m test`, through the existing watchdog, normal profile, hard cap 300s. |
| Receipt | [guard.json](../../../logs/phase-19/s19-2-source-archive-84adfabc5cf6400d95b8b4d0051b3431/guard.json): exit 0; 46.328s; capture/cleanup complete. Full stdout/stderr retained beside it. Existing Lombok/JDK/Mockito warnings were observed, not reclassified as warning-free. |
| Static isolation/configuration | PASS: five conditions; no other Java QA worker entry or imports from default Python modules; optional dependencies/core lock separation; restored accepted route inventory. Modified Python syntax checked with AST parsing only; no QA module imports. |
| Publication inventory | 123 candidate files before adding this manifest were inspected as UTF-8 text, without symlinks/NUL binaries; no binary, secret file, dump or ZIP is selected. Synthetic fixture scope and shared-file changes were inspected, not evaluated. Final exact commit lists below contain 124 files. |
| Preservation/whitespace | PASS: [integrity-v3.json](../../../logs/phase-19/s19-2-source-archive-84adfabc5cf6400d95b8b4d0051b3431/integrity-v3.json), exit 0, 33.547s, normal 300s cap, capture/cleanup complete. All 29,999 historical S19-2 evidence hashes, all 46 fixture/manifest hashes, both QA runtime metadata files, and all 2,768 pre-existing project files outside the 18 authorized archival edits are unchanged. `git diff --check` exits 0. Ten pre-existing Markdown whitespace lines were preserved. |
| Omitted checks | NOT_RUN: full application/database startup, full OpenAPI suite, full Java/Python suites, real retrieval/citation/native behavior, CUDA, C1/C2 load, 24 slots, M10 replay, resource/quality qualification and independent final review. The focused MVC context is the authorized startup/route proof; it does not claim any of those omitted checks passed. |

Two preliminary ad hoc inventory reads failed on Windows default text/console encoding; rerunning with explicit UTF-8 completed those reads. The first bounded integrity-helper attempt failed because it stripped Git porcelain's leading status whitespace. A second stopped on preserved pre-existing Markdown whitespace. These helper failures are retained as `integrity.json` and `integrity-v2.json`; neither is a product test or QA execution. The corrected helper preserves Git's status prefix, limits its source-whitespace assertion to source files, and leaves historical Markdown unchanged; its final `integrity-v3.json` result is PASS. Receipt links here are local ignored evidence and will not resolve on GitHub; the receipts are excluded from both commits.

## Owner-only commit 1 — documentation suspension/archival record

Suggested message: `docs(rag): record S19-2 suspension and archival disposition`.

Exactly **25 documents**, relative to `docs/implementation/` below. Historical decisions/reviews remain issuance evidence; current pointers retain suspension and no active handoff. This manifest describes the inspected local tree before the later source archival commit; it is not a source-acceptance record.

| Git | Path relative to `docs/implementation/` |
| --- | --- |
| M | `handoffs/ACTIVE.md` |
| M | `handoffs/README.md` |
| M | `phase-19/README.md` |
| M | `phase-19/owner-decisions-and-scope.md` |
| U | `phase-19/owner-authorization-s19-2-codex-takeover-2026-10-29.md` |
| U | `phase-19/owner-decision-s19-2-bounded-cuda-dll-load-diagnostics-2026-10-30.md` |
| U | `phase-19/owner-decision-s19-2-bounded-cuda-feasibility-trial-2026-10-29.md` |
| U | `phase-19/owner-decision-s19-2-bounded-cuda-harness-launch-correction-2026-10-30.md` |
| U | `phase-19/owner-decision-s19-2-c2-finite-resource-recovery-2026-10-29.md` |
| U | `phase-19/owner-decision-s19-2-f04-controlled-prospective-rebaseline-2026-10-29.md` |
| U | `phase-19/owner-decision-s19-2-m10-read-only-forensics-2026-10-30.md` |
| U | `phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md` |
| U | `phase-19/owner-decision-s19-2-standing-bounded-takeover-authorization-2026-10-30.md` |
| U | `phase-19/owner-decision-s19-2-suspension-2026-10-30.md` |
| U | `phase-19/reviews/2026-10-28-s19-2-independent-final-review.md` |
| U | `phase-19/reviews/2026-10-29-s19-2-independent-final-re-review-1.md` |
| U | `phase-19/reviews/2026-10-29-s19-2-independent-final-re-review-2.md` |
| U | `phase-19/reviews/2026-10-29-s19-2-independent-final-re-review-3.md` |
| U | `phase-19/s19-2-executable-handoff-2026-10-28.md` |
| U | `phase-19/s19-2-implementation-handback.md` |
| U | `phase-19/s19-2-implementation-notes.md` |
| U | `phase-19/s19-2-m10-read-only-forensic-report-2026-10-30.md` |
| U | `phase-19/s19-2-model-selection.md` |
| U | `phase-19/s19-2-source-archival-task-2026-10-31.md` |
| U | `phase-19/s19-2-source-archive-manifest-2026-10-31.md` |

## Owner-only commit 2 — default-off unaccepted source archive

Suggested message: `chore(rag): archive suspended S19-2 source with default-off guards`.

Exactly **99 source/test/config/fixture paths**, grouped below. `M` means a tracked change from HEAD; `U` means untracked at this task's close. New QA APIs/DTOs, services, worker, generator, tests, fixtures and optional runtime metadata are all **suspended/unaccepted**. Shared files are identified above. Each group prefix plus its listed relative path is an exact repository path.

### Root/configuration

| Git | Path |
| --- | --- |
| M | `.gitignore` |
| M | `backend/src/main/resources/application.yml` |

### Java production source

| Git | Path relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/` |
| --- | --- |
| M | `internal/application/RagRecoveryService.java` |
| M | `internal/application/retrieval/RagRetrievalService.java` |
| M | `internal/web/RagRetrievalIngressFilter.java` |
| U | `api/RagQaCancelResult.java` |
| U | `api/RagQaClaim.java` |
| U | `api/RagQaClaimSupport.java` |
| U | `api/RagQaCommand.java` |
| U | `api/RagQaConflictPair.java` |
| U | `api/RagQaHistoryEntry.java` |
| U | `api/RagQaOperations.java` |
| U | `api/RagQaOutcome.java` |
| U | `api/RagQaResponse.java` |
| U | `internal/application/qa/RagQaContextPacker.java` |
| U | `internal/application/qa/RagQaService.java` |
| U | `internal/application/qa/RagQaSupervisor.java` |
| U | `internal/application/qa/RagQaValidator.java` |
| U | `internal/application/qa/package-info.java` |
| U | `internal/web/controller/RagQaController.java` |
| U | `internal/web/dto/RagQaCancelResponseDto.java` |
| U | `internal/web/dto/RagQaClaimDto.java` |
| U | `internal/web/dto/RagQaClaimSupportDto.java` |
| U | `internal/web/dto/RagQaConflictPairDto.java` |
| U | `internal/web/dto/RagQaHistoryEntryDto.java` |
| U | `internal/web/dto/RagQaRequestDto.java` |
| U | `internal/web/dto/RagQaResponseDto.java` |

### Java test source

| Git | Path relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/` |
| --- | --- |
| M | `AbstractRagIntegrationTest.java` |
| U | `RagQaArchiveGuardTest.java` |
| U | `RagQaBehaviorTest.java` |
| U | `RagQaCitationTest.java` |
| U | `RagQaCudaNativeIntegrationTest.java` |
| U | `RagQaEvaluationNativeIntegrationTest.java` |
| U | `RagQaIntegrationTest.java` |
| U | `RagQaLifecycleIntegrationTest.java` |
| U | `RagQaNativeIntegrationTest.java` |
| U | `RagQaNativeSafetyIntegrationTest.java` |
| U | `RagQaProspectiveEvaluationNativeIntegrationTest.java` |
| U | `RagQaSenderIntegrationTest.java` |
| U | `RagRecoveryQuiescenceUnitTest.java` |
| U | `internal/application/qa/RagQaProtocolTest.java` |

### Python source, optional dependencies and runtime metadata

| Git | Path relative to `rag-engine/` |
| --- | --- |
| M | `pyproject.toml` |
| M | `uv.lock` |
| U | `qa-cuda-runtime-lock.json` |
| U | `qa-runtime-lock.json` |
| U | `scripts/provision_qa_model.py` |
| U | `src/ppv_rag/grounded_qa.py` |
| U | `src/ppv_rag/qa_generation.py` |
| U | `tests/test_grounded_qa.py` |
| U | `tests/test_qa_cuda_profile.py` |
| U | `tests/test_qa_generation.py` |
| U | `tests/test_qa_model_paths.py` |
| U | `tests/test_qa_runtime_profile.py` |

### Preserved synthetic fixture set v1

| Git | Path relative to `rag-engine/tests/fixtures/phase19-qa-v1/` |
| --- | --- |
| U | `dev_amber_relic.md` |
| U | `dev_architecture.md` |
| U | `dev_conflict_a.md` |
| U | `dev_conflict_b.md` |
| U | `dev_core_parent.md` |
| U | `dev_han.md` |
| U | `dev_observatory.md` |
| U | `dev_private_marker.md` |
| U | `dev_solar.md` |
| U | `dev_stations_a.md` |
| U | `dev_stations_b.md` |
| U | `dev_vietnam.md` |
| U | `heldout_adversarial.md` |
| U | `heldout_headings.md` |
| U | `heldout_literature.md` |
| U | `heldout_materials.md` |
| U | `heldout_ocean_han.md` |
| U | `heldout_specs_a.md` |
| U | `heldout_specs_b.md` |
| U | `manifest.json` |

### Preserved synthetic fixture set v2

| Git | Path relative to `rag-engine/tests/fixtures/phase19-qa-v2/` |
| --- | --- |
| U | `dev_amber_relic.md` |
| U | `dev_architecture.md` |
| U | `dev_conflict_a.md` |
| U | `dev_conflict_b.md` |
| U | `dev_core_parent.md` |
| U | `dev_empty.md` |
| U | `dev_han.md` |
| U | `dev_noncore_child.md` |
| U | `dev_observatory.md` |
| U | `dev_private_marker.md` |
| U | `dev_solar.md` |
| U | `dev_stations_a.md` |
| U | `dev_stations_b.md` |
| U | `dev_travel_index.md` |
| U | `dev_vietnam.md` |
| U | `dev_waypoint.md` |
| U | `heldout_adversarial.md` |
| U | `heldout_headings.md` |
| U | `heldout_literature.md` |
| U | `heldout_materials.md` |
| U | `heldout_ocean_han.md` |
| U | `heldout_specs_a.md` |
| U | `heldout_specs_b.md` |
| U | `manifest.json` |
| U | `one/Place.md` |
| U | `two/Place.md` |

The previously modified `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` is now identical to HEAD and contributes **no file delta to either commit**. Only its unaccepted QA rows/count changes were undone for default-off safety; the frozen accepted route contract was preserved.

## Exclusions, preserved provenance and publication risks

These existing working-tree paths are **excluded from both commits** and remain untouched by Codex:

| Git | Path |
| --- | --- |
| M | `.agents/skills/antigravity-implement-handoff/SKILL.md` |
| M | `.agents/skills/antigravity-self-review/SKILL.md` |
| U | `personal-private-vault.zip` |

All ignored material stays local: `logs/` (including the retained test DB/container/dumps, runtime/native binaries and all old/new receipts), `backend/.env`, `rag-engine/models/`, `rag-engine/.venv/`, `.venv-d17/`, `.venv-qa/`, build `target/` trees, IDE/Graphify caches, Python bytecode/egg-info and test/type/lint caches. No force-add, deletion, cleanup, environment change or runtime publication occurred. The ZIP is uninspected and is not an approved GitHub artifact. Runtime JSON files in commit 2 contain pinned metadata/hashes, not the native binaries or model weights.

No secret/private-source files were selected; dummy credentials and private markers in synthetic QA tests/fixtures are test material. Old logs and the ZIP have not been cleared for publication. Do not use blanket staging or include any ignored/unlisted path.

Preserve [M10 forensics](s19-2-m10-read-only-forensic-report-2026-10-30.md), all prior decisions/reviews/handbacks and all source/model/fixture/ledger/counter evidence. The labels and registration guards intentionally change some current source hashes relative to the historical inference freeze; **the old seals/hashes were not regenerated or represented as describing the annotated archive**. The two runtime metadata files and both fixture manifests remain unchanged. Those historical records must not be used to imply a new qualification run.

Unresolved status remains:

- C1 `REJECTED`; original contaminated 15/9 source-group evidence and overwritten original ledger `NOT_RECOVERED`; E24 failure retained.
- C2 `NOT_QUALIFIED`; M01–M09 implementer-assessed PASS; mandatory M10 HTTP503/`CANDIDATE_INVALID` FAIL, exact root cause `NOT_VERIFIED`; M11–E24 NOT_RUN. No raw completion/traceback was recreated.
- F01–F05/F08/F09 OPEN; F06/F07/F10 independently CLOSED. No finding closure or independent acceptance here.
- Antigravity 3/3 exhausted; same takeover1 incomplete/stopped; resource corrections 3/3; C2 starts 2/2; restart 1/1 consumed; independent Codex corrective passes 0/3. This archival task changes none of those counters.
- P17 FROZEN; P18 CLOSED; S19-1 independently accepted/published; Phase 19 OPEN. External indexing INACTIVE and live originals DISABLED/DEFERRED. No S19-3+/later RAG/UI authorization.

Publishing commit 2 would preserve unfinished inert source on `main`; it is **not release, deployment, operational readiness or permission to run the archived QA/native/evaluation suites**. Model defects, Windows/runtime assumptions and incomplete QA tests remain. The scope of new proof is default registration/routing and preservation only.

## Docs Sync Check and final routing

- **UPDATED:** only this new manifest and the narrowly listed source annotations/QA guards/guard test; default OpenAPI test restored to its accepted content. Existing suspension/archival documents were not rewritten.
- **ALREADY_CONSISTENT:** `docs/implementation/handoffs/ACTIVE.md` remains `NO_ACTIVE_HANDOFF` with the archival-only exception; suspension/Owner decision/task, current handback, forensic report, existing current navigation and dated S19-1 acceptance/publication records retain their meanings.
- **ALREADY_CONSISTENT:** final preservation receipt and exact two-commit inventory; all local manifest links resolve. Final Git inventory is 127 paths: 14 modified and 113 untracked. Of these, 124 are listed for the two Owner commits and 3 are explicitly excluded; index empty, HEAD unchanged.
- **NOT_VERIFIED:** M10 exact cause, complete C2 quality, full application/native/DB behavior and any new Owner publication. Omitted checks above are not PASS.
- **DEFERRED_OUT_OF_SCOPE:** the two unrelated skill edits, older AGENTS/workflow status drift, LOW backlog, later phases and any source correction/evaluation.

**Next step: Owner | verify this manifest and exact Git status, then optionally publish the two explicitly bounded archival commits | no QA implementation/evaluation/deployment follows; no active handoff, new attempt, finding closure or independent acceptance.** Codex performed no staging or publication.

`READY_FOR_OWNER_ARCHIVE_COMMIT`

