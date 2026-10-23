# Slice 3 — final bounded corrective implementation handback

**2026-10-22. Codex is the implementer, not an independent reviewer.**
**Disposition: READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW.** All mandatory rows have
current implementation evidence; separate independent adjudication remains required.
This is the one additional finite Owner-authorized checkpoint for remaining
S3-F03/S3-F06 obligations. The prior BLOCKED corrective handback, original
Antigravity submission, independent CHANGES_REQUESTED review, historical failed
gates and Antigravity 2/2 history are preserved. No staging, commit, push, tag,
PR, activation or Slice 4 work occurred.

Entry main/HEAD: `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5` plus the existing dirty
candidate. Entry hashes/copies, prior Surefire reports and all new isolated
receipts are under `logs/phase18/s3-final-corrective-20261021/`. Exact changed
paths/hashes, commands and preservation results are in the final machine inventory.

## Corrections

**S3-F03 naming and selected dependency scope.** Java previously transferred the
same child repeatedly under basename/stem/UUID keys and could not distinguish
folder-qualified duplicate basenames or frontmatter aliases. New captures retain
sorted lexical suffix hashes in the existing immutable manifest. Existing
`sources.file_identity` binds the canonical-path digest and captured naming proof;
the 128-character column is unchanged. No raw original path is transferred.
The internal snapshot tuple carries optional `target_digests`; Java sends each
approved child S/R once, keyed by UUID. The adapter reuses D17's source tree to
read inert aliases and actual embed targets. Ambiguity never chooses a source.
Only names actually expanded by D17 require resolution: an unknown target outside
a selected heading/block remains inactive. Exact approved alias labels, including
punctuation, are not interpreted as filesystem paths. There is no child filesystem
or network lookup and no second Markdown parser.

Two directly related regressions were reproduced and corrected. D17 charged one
immutable child twice when two aliases selected it; supplied-input accounting now
counts one S/R once, still rejects conflicting revisions before extraction, and
still charges every selected expansion. The source bound remains before UTF-8
digest allocation. Selected anchors could clip a neighbouring unit to one newline,
which produced an unembeddable chunk. Only contiguous whitespace from the same
S/R, field and dependency occurrence is coalesced within the existing chunk budget;
both original constituent locators/metadata remain. No source text is discarded,
synthetic separator added, or cross-source/revision grouping permitted. The model,
tokenizer, ONNX implementation, split routine and all frozen gold remain unchanged.

Legacy basename-only proofs remain valid for basename/alias use and pending updates,
including an independently CURRENT selected G during capture. Missing historical
directory facts are not inferred or silently added to old sealed manifests. Existing
untyped captures remain subject to the prior fail-closed format policy.

**S3-F06 actual admission.** Native adversarial evidence exposed acceptance of an
extra pipeline fingerprint field and a claimed CUDA provider unavailable in the
selected runtime. Admission now requires exactly the pinned tuple plus provider;
the trusted process checks actual available providers. Every result, including an
explicit failed root, must retain its exact immutable input binding and valid pipeline
identity. Real PostgreSQL round-trip also exposed signed floating zero normalization:
comparison now treats `-0.0` and `0.0` consistently with jsonb while preserving float
versus integer types, fractional-index rejection and original source text.

No new SQL migration/grant/table/column/enum/route was introduced. V22 from the
previous checkpoint is unchanged. The publication callback is unchanged; additional
checks precede it. The existing one physical publication C_P, source-first guards,
attempt fencing, supervisor, bounded native process controls and recovery remain.

### Exact changed code paths in this checkpoint

Java paths below are relative to
`backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/`; test paths are
relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/`.
The entry-relative patch and complete SHA/byte inventory are preserved at
`logs/phase18/s3-final-corrective-20261021/checkpoint-code-changes.patch` and
`checkpoint-changed-code-paths.json`. These distinguish this checkpoint from
pre-existing uncommitted corrections.

| Path | Bounded change |
| --- | --- |
| Java `internal/infrastructure/storage/RagOriginalBytesStore.java` | Optional sealed target fingerprints; legacy overload/manifest compatibility. |
| Java `internal/application/RagSourceService.java` | Bind naming facts on capture; preserve old naming facts on pending updates. |
| Java `internal/application/RagSourceAuthority.java` | Capture lexical fingerprints and independently authenticate the naming proof. |
| Java `internal/application/RagDocumentInput.java` | Transport each approved child once under its UUID. |
| Java `internal/application/RagDocumentFinalizationGate.java` | Exact pipeline key set and pipeline validation for failed roots before C_P. |
| `rag-engine/src/ppv_rag/contracts.py` | Optional bounded sorted target digests; omit empty legacy field in canonical transport. |
| `rag-engine/src/ppv_rag/document_source.py` | Existing D17 namespace/aliases, safe supplied target matching and selected-scope resolution. |
| `rag-engine/src/ppv_rag/parsers/obsidian.py` | Charge each immutable supplied S/R once across aliases; retain occurrence expansion bounds and conflicting-revision denial. |
| `rag-engine/src/ppv_rag/chunking/chunker.py` | Preserve contiguous whitespace source context in embeddable same-occurrence units. |
| `rag-engine/src/ppv_rag/document_verification.py` | Actual provider availability, every-root input binding, strict jsonb signed-zero consistency. |
| Test `AbstractRagIntegrationTest.java` | Optional bounded runtime-only proxy for real connection-loss tests; defaults retained. |
| Test `RagSlice3ConnectionLossIntegrationTest.java` (new) | Framed real PostgreSQL wire-loss proxy and independent durable observations. |
| Test `RagSlice3CorrectiveIntegrationTest.java` | Native target, authority, privilege, race, cancellation, rollback and restart cases. |
| Test `RagSlice3Evidence.java` (new) | Actual producer-returned/DB result, scope, roster, source, process and digest receipts. |
| Test `RagSlice3RecoveryProbeProcess.java` (new) | Real controller halt and fresh-JVM durable recovery/current-G observations. |
| Test `RagSnapshotCorrectiveIntegrationTest.java` | Match new seal overload in existing fault spies; assertions retained. |
| Test `RagSlice3VerifiedIndexingIntegrationTest.java` | Strengthen S3-03 from two empty files to two unsupported active-HTML files; assert exact native failure reasons. Separate all-empty coverage remains. |
| `rag-engine/tests/test_slice3_dependency_names.py` (new) | Deterministic supplied namespace/alias/selection/bound/canonical regressions. |

Documentation changes are this new handback, executed matrix and evidence JSON;
ACTIVE, Phase 18 README/test-evidence and the two current executable status notices;
API inventory and Phase 18 DBML comments. Owner decisions, historical reviews/handbacks and existing unrelated
AGENTS/configuration changes are preserved. No migration changes in this checkpoint.

## Native proof and recovery

The current matrix maps S3-01–16 to actual source and observations. It includes
ordinary API capture/approval/dependency edges, deleted originals, nested/anchored
children, identical-byte distinct S/R, genuine 8/2 and all-failed batches, source-backed
forgery rejection, complete roster, strict metadata and real pinned CPU vectors.

Separate restricted worker connections exercise both writer/publication-seal orders
and actual PostgreSQL blocking/commit observations. Their payloads come from a real
native producer; a test-only administrative reset creates the unsealed starting state.
Native Python staging cancellation independently exercises the writer's attempt lock.
Post-wait expiry uses a bounded administrative deadline fault, not a production deadline
extension. Source/root/child authority changes are normal API busy denials or explicitly
labelled administrative corruption at a post-verification/pre-publication barrier.

Two-root failures at generation, chunks, outcomes, promotion and deferred physical
COMMIT leave zero partial candidate/outcome/release visible to an independent connection.
Prior selected R and an independently eligible sibling G survive. Actual PostgreSQL
COMMIT acknowledgement is dropped on the wire before JDBC receives it; durable
reconciliation observes one committed result without resend or duplicate publication.
A separate cut before COMMIT leaves RECOVERY_REQUIRED and reservations while all runtime
database proxy connections are unavailable, then safely aborts after restored access.
The observer stays reachable; this is not claimed as a machine-wide server shutdown.

Real controller halt during production, after staging and during verification proves
recorded worker/verifier death and fresh-JVM durable recovery. No automatic resume or
reparse occurs. A separate fresh JVM immediately qualifies a genuine published G.
Actual partial transfer, native CPU work, staging and validation cancellation, managed
temporary-path failure and cumulative Markdown expansion rejection are exercised.

## Verification and failed history

Final counts, resource values and disposition are added below only from completed
receipts. Exact argv/cwd/watchdog/source identities for **every** command are retained
in `commands.jsonl` and the machine inventory; full logs and Surefire archives are linked
there. The selected environment is Python 3.14.4 `.venv-d17`, pinned local ONNX/tokenizer,
Java 25 and native PostgreSQL 18.6/pgvector 0.8.6. No dependency/model download or upgrade.

Preserved failures include cached extended-protocol COMMIT injection, an invalid reset
fault fixture, receipt column/count parsing errors, an incorrect test attribute,
read-only manifest fault setup, live resource-accounting API misuse, duplicate alias
input accounting, clipped whitespace embedding, unavailable CUDA claims, genuine PG
signed-zero admission and unselected-target namespace resolution. Assertions were not
weakened or skipped. The first combined Java gate is **FAILED: 236/0/1/0** because the
new collector parsed a 1,001-digit rejection fixture. Its outer runner returned zero
without propagating Maven's failure; that outer PASS is orchestration only, not a Java
PASS. Both records are preserved. Later runner/profile receipts propagate the bounded
Maven exit and use a raw JSON/hash plus SQL array-length collector.

The next complete source-bound selection passed **238/0/0/0**, but predates the final
selected-anchor namespace correction. It is preserved as intermediate evidence,
not relabelled final. Final Java/Python/static/corpus selections are rerun after that
correction. This directly addresses the earlier 178-test source-continuity gap.

The subsequent 238/0/0/0 selection also passed, but the retained mixed-batch test
still used empty failure inputs. Final contract mapping identified this narrower
fixture mismatch. Its two failures now contain unsupported `<script>`/`<form>`
content, and the focused real API/native publication test passes with exactly two
`MD_UNSUPPORTED_ACTIVE_HTML` outcomes and eight publications. Assertions are stronger;
the independent all-empty batch is retained. The complete final Java selection is
rerun after this last test-only edit. Python/static/corpus source hashes remain
identical to their passing v3 runs and are explicitly compared in the inventory.

Resource profiles v1-v3 are **INVALID for resource acceptance**: their observer
followed numeric parent PIDs after exit/reuse and could include pre-existing Windows
processes. This is an evidence-observer defect, not proof of surviving application
workers. Raw profiles and PASS Java receipts remain unchanged and are classified
separately. The final v4 observer retains handles keyed by PID/creation time and
admits children only when their creation lies within the identified parent's
lifetime. The short observer smoke check passes with zero owned survivors or
unreadable owned processes. Only the completed v4 profile supports final resources.

The retained D14 full log records `Invalid worker batch result provenance` at the
native DOMAIN boundary. Current named test and actual-result receipts are compared
with that real record. Original Antigravity pre-repair stdout remains missing; no
inner failure cause is inferred from its exit-only receipt. No history is rewritten.

Independent Windows measurements report simultaneous sampled tree RAM/private memory,
per-process OS lifetime peak working sets and CPU, host headroom, actual owned temporary
disk, managed capture/validation temps and process cleanup separately. Lifetime peaks
summed across sequential processes are not simultaneous RAM. Device-wide NVML VRAM is
not worker VRAM; CUDA inference is UNAVAILABLE in this selected build. Sampling limits,
role attribution, PID creation-time binding and fixture/orchestration overhead remain
explicit. No claim of zero global Windows writes or final Slice 4 maximum-profile proof.

### Reproducible final selections

The exact executable paths, working directories, all selectors, time bounds and
original/failed command invocations are in
`logs/phase18/s3-final-corrective-20261021/commands.jsonl`, also embedded in the new
[machine inventory](slice-3-final-corrective-evidence-2026-10-22.json). Each name below
identifies the corresponding isolated `.json` watchdog receipt and `.log` output.
The inner Maven receipt controls Java status; orchestration success alone does not.

| Receipt | Final command / selection | Observed result |
| --- | --- | --- |
| `python-final-v3` | Selected Python 3.14.4 `-B -m pytest` on 14 D17/Slice 3/legacy/optional-backend test files, isolated cache and basetemp | 189 passed, 37.57 s; no failed or skipped tests. |
| `ruff-v3` | Selected Python `-B -m ruff check rag-engine` | PASS. |
| `format-v3` | Selected Python `-B -m ruff format --check rag-engine` | PASS, 79 files already formatted. |
| `mypy-v3` | Selected Python `-B -m mypy --config-file rag-engine/pyproject.toml --cache-dir <new fresh-mypy-v3> rag-engine/src` | PASS, 26 source files. |
| `dependency-v3` | `uv pip check --python <selected Python>` | PASS, 67 packages compatible. |
| `lock-v3` | `uv lock --directory rag-engine --check --offline` | PASS, 73 packages resolved; no lock/dependency edits. |
| `official-parser-v3` | Isolated copy of structural conformance command, original official/profile gold and recorded R03 resolution, new output path | PASS: 1,329 examples, 1,267 supported and 62 mandatory rejections. |
| `official-producer-v3` | Same frozen corpus through parser/chunker/real pinned CPU producer with isolated output | PASS: same 1,329 dispositions; no fabricated independent/human assessment. |

The Java command is `mvn.cmd -ntp -f backend/pom.xml
-DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<selection> test`, with this
unchanged final selection (1,150 s inner bound):

```text
RagNativeDocumentBatchIntegrationTest,RagDocumentBatchIntegrationTest,RagDocumentInputCompatibilityTest,RagWorkerPostgresPrivilegeIntegrationTest,RagCommitAcknowledgementRecoveryIntegrationTest,RagSecondJvmRecoveryIntegrationTest,RagCancellationRecoveryIntegrationTest,RagSlice1SnapshotIntegrationTest,RagSnapshotCorrectiveIntegrationTest,RagSlice3*Test,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest,RagWebIntegrationTest,RagNativeWorkerProcessTest,RagStagingCommitFenceIntegrationTest
```

`java-resource-final-v4` wraps the real selection with independent owned-process
and temporary-disk observation (1,200 s outer bound). The final Maven source
manifest is captured before execution, compared after completion, and hashed in
the machine inventory. Final counts/measurements follow its completed receipts.

### Final completed results and resources

`java-current-final-v4`: **238 passed, 0 failures, 0 errors, 0 skipped**; Maven
18:46, watchdog exit 0. This includes the stricter unsupported-content 8/2 case,
88 corrective invocations, actual wire loss and all selected architecture/API/
legacy checks. `java-resource-final-v4` also exits 0. Python/static/corpus v3
receipts remain current: all **120** `rag-engine/` manifest files match the final
tree byte-for-byte. No redundant Python rerun is claimed after the Java-only
fixture edit.

The final Java manifest covers **1,740** unchanged-current code/config/test files:
SHA-256 `b9f31a881cf390a0a3b60487cd0058fa49ef58da6e208e4ee0c73e4e14b3f18b`.
Actual individual file/model/tokenizer hashes and original source/input/result/
receipt bindings are in the new machine inventory. Final-run native records are
selected by their unique artifact write times within the completed Maven receipt
window, with exact file hashes; prior-run artifacts remain separately inventoried.

| Final independent observation | Actual value / interpretation |
| --- | --- |
| Execution wall / observed summed process CPU | 1,128.858 s / 1,623.109375 CPU s; CPU is the measured sum, not an exact unsampled system total. |
| Sampled simultaneous owned-tree working set / private memory | 2,844,975,104 / 3,083,436,032 bytes. |
| Sum of per-process lifetime working-set peaks | 130,957,000,704 bytes across sequential processes; **not simultaneous RAM**. |
| Host total / minimum sampled headroom | 17,112,092,672 / 776,765,440 bytes; machine-wide, separate from application limits. |
| Device-wide NVML peak | 1,678,184,448 bytes; not attributable worker VRAM. CUDA inference **UNAVAILABLE**; ORT exposes Azure/CPU only. |
| Actual sampled owned temporary tree / managed capture-verification temp / managed revision storage | 8,623,477 / 2,389,542 / 500,155 bytes. Evidence files are durable receipts, not application temporary storage. |
| Observed process identities | 474 `(PID, creation FILETIME)` records, including all 175 bound identities: 96 producers and 79 verifiers. Parent/child creation bindings are retained. |
| Bound producer / verifier maximum OS lifetime working sets | 640,548,864 / 604,192,768 bytes. Remaining 299 records include JVMs, wrappers and descendants; their full metrics are retained, not relabelled workers. |
| Completion | Zero owned survivors, unreadable admitted owned processes and remaining managed capture/verification temps. |

The observer took 13,623 samples (target 50 ms; actual mean approximately 82.9 ms).
Very short processes/disk peaks can be missed. Two candidate PIDs (7280, 12432)
could not be identified before exit; no creation time, peak or CPU is invented for
them. Neither occurs in the final bound producer/verifier identities; all 175 of
those identities were independently observed. The separate identity-coverage
receipt records this qualification. Device samples, host headroom, OS lifetime
peaks, sampled simultaneous values and finite process-control enforcement are
distinct evidence; no exact global peak or zero-global-write claim is made.

The [final S3-01–16 matrix](slice-3-final-corrective-matrix-2026-10-22.md) records
passing implementation evidence for every mandatory row. Original Antigravity
pre-repair stdout is still missing, while the real retained D14 failure log and
current named native run are available; no historical bytes or inner failure cause
are invented. The complete current source-bound gate, not the old 178-test or
intermediate 238-test reports, controls this handback.

No known unresolved implementation blocker remains within this checkpoint.
Independent re-review of all rows and the stated evidence limits is pending;
this is not independent acceptance or FR18-3/Slice 3 closure.

`git diff --check` passes (`diff-check-final`, exit 0). Preservation checks confirm
199 historical review files, 3,765 prior-checkpoint assets, 107 independent-review
assets, 24 original submission assets, all four prior handback/inventory records
and the semantic Git index remain unchanged. The one-physical-C_P publication
callback also matches its entry hash. The mandatory S3 table body is unchanged;
its normalized-text SHA-256 is
`d818c773db4f852682dce62397236ed97b30b29390f4cb8897e6c050cd930002`.

Two inventory diagnostics are preserved separately: the first contract precheck
lacked a physical copy of the untracked entry matrix (its complete original bytes
were reconstructed and verified against the already recorded entry SHA); the
first assembled inventory used Windows separators when selecting final-run
artifacts, resulting in an empty association list. That draft is preserved;
final inventory paths are normalized and nonempty source/result associations are
required. These are evidence-assembly corrections, not new Java/Python gate passes.

## Review boundary

Recommended independent scope: naming-proof/legacy policy, source-specific aliases and
selected anchors, whitespace/source locator preservation, signed-zero strictness,
worker/runtime/PUBLIC privileges, actual sealed result and receipt binding, source-first
and writer interleavings, one physical C_P, real wire loss, fresh-controller verification
recovery, resource accounting, and final source/hash continuity for every matrix row.

Phase 17 FROZEN; Slice 1/D14 CLOSED; Slice 2/D23 Markdown-only CLOSED; D24 Markdown-only;
D22 original captures NOT_RECOVERED; FR18-3 OPEN pending independent closure; indexing
INACTIVE; live originals DISABLED/DEFERRED; historical full Java gate FAILED (1,433 tests,
1 failure, 2 errors, 0 skips); P18-FINAL NOT ACCEPTED. Slice 4/excluded-format cleanup remain
unauthorized. No independent acceptance, finding closure or Owner commit readiness is
claimed. Stop after this handback; no automatic further corrective checkpoint.
