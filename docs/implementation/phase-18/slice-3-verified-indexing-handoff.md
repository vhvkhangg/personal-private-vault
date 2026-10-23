# P18-S3-VERIFIED-INDEXING — Markdown D+F

## RR-01-03 corrective checkpoint - 2026-10-22

The separately Owner-authorized RR-01-03 checkpoint returns **READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW**. Final Java selections pass 249/0/0/0 on identical source; Python passes 198 tests. This is implementation evidence awaiting independent review.
Its [new handback](slice-3-rr-corrective-handback-2026-10-22.md),
[executed matrix](slice-3-rr-corrective-matrix-2026-10-22.md) and
[machine inventory](slice-3-rr-corrective-evidence-2026-10-22.json) record original
counterexamples, minimal corrections and preserved failures. Earlier submitted
readiness claims are historical; the independent CHANGES_REQUESTED review is
preserved. FR18-3 OPEN; external indexing INACTIVE; no Slice 3 acceptance,
activation, publishing or Slice 4 authorization.


> **Final additional checkpoint, 2026-10-22: READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW.**
> Codex implementation evidence is in the [new handback](slice-3-final-corrective-handback-2026-10-22.md),
> [executed S3-01-16 matrix](slice-3-final-corrective-matrix-2026-10-22.md) and
> [machine inventory](slice-3-final-corrective-evidence-2026-10-22.json).
> The prior BLOCKED checkpoint and original initial contract below are preserved.
> Codex is the implementer; separate independent review is required. No finding,
> FR18-3, Slice 3 or Phase 18 acceptance, activation, publishing or Slice 4 follows.

- Created by: Codex, 2026-10-21
- Status: `READY_FOR_IMPLEMENTATION`
- Authority: Owner D24; new initial bounded Slice 3, not a restarted historical remediation round
- Implementer: Antigravity `backend-implementer`; independent final reviewer: separate Codex session
- Entry baseline: `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`, published `main`

## Goal and sources of truth

Make Java transport only approved immutable `.md` snapshots to the actual D17 producer,
validate its actual result and publish through the existing trusted PostgreSQL path.
Close FR18-3 only through independent evidence. Normal external indexing remains INACTIVE;
exercise native integration only in explicit isolated tests, never live owner documents.

Use [D24](owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing),
[baseline/scope/cleanup](slice-3-markdown-scope-and-baseline-2026-10-21.md),
[mandatory matrix](slice-3-verified-indexing-test-matrix.md),
[D17](obsidian-markdown-d17-contract.md), [D23 closeout](slice-2-markdown-only-d23-closeout-2026-10-21.md),
ADR-0020–24, the existing [API/worker contract](../../architecture/rag-phase-18-api-extension.md),
[acceptance](acceptance-and-evidence.md) and [final gate](final-verification-gate.md).
Phase 18's accepted preparation report is linked in the baseline document; its runtime risks
are carried into the matrix, not asserted PASS here.

## Minimal work packages / permitted targets

Paths are under the existing RAG module; use its public source contracts, never another module's
`internal` package/repository. Preserve ADR-0016 root HTTP envelope/module-local advice.

1. **Snapshot authority and transport.** Extend existing `RagSourceAuthority`, `RagDocumentInput`,
   `RagDocumentBatchService`, `RagExternalFileAuthority`, grants/client and snapshot-store integration.
   Consume approved pending R for an update or selected R when permitted by the existing action;
   do not create a same-R Reindex API. Bind S/R, SHA-256/length, expected selected R/sourceVersion,
   approval/reset/epoch/policy and each independently approved child S/R/edge to the exact job input.
   Verify bounded stored bytes outside SQL row locks, then recheck prepared authority under guards.
   Use Java-owned bytes, never caller-supplied bodies, original paths or Python file discovery.
   Resolve only approved `.md` child payloads; reject unsupported children, cycles and escapes.
   Distinguish external UUID authority/reservations from Vault IDs. Reserve exact full union before
   transfer; keep v2 and current DOMAIN behavior compatible. Explicitly negotiate the snapshot
   protocol mode already required by ADR-0022; never reinterpret legacy v3.0 fields silently.
2. **Actual producer/result admission.** Extend existing Python `main.py`/protocol models/`worker.py`
   and Java client/grant/staged-result validator/finalizer; preserve accepted parser/chunker/model
   semantics. Admit only the exact reviewed D17 tuple after validating actual output. Reject non-MD
   snapshot formats before dispatch; no excluded adapter/OCR initialization. Pin constants/artifact
   hashes from the verified producer, not arbitrary request strings. Verify actual text, full
   source coverage, typed locators, structure/cell references, chunk boundaries and complete outcomes
   against the prepared root/child bytes. Where Markdown normalization differs from raw slices,
   verify the D17 source-backed representation and permitted transformations; substring matching
   alone is insufficient. Resolve compact metadata with finite bounds, exact source/revision scope,
   no dangling/cross-root/default-shadowed references or fabricated nodes/cells. Do not build a
   second general Markdown parser/framework: reuse the source-context contract with a small trusted
   validator and adversarial integration oracles. Bind canonical **actual result** digest separately
   from input/scope: exact roster, statuses/reasons, ordered chunks/text/metadata/locators/vectors and
   pipeline identity, job/attempt and R/closure. No input-derived placeholder or count-only digest.
   Retain model `paraphrase-multilingual-MiniLM-L12-v2`, model/tokenizer hashes and 384 dimensions;
   enforce vector count equals chunks, numeric finite values including after float conversion,
   exact pipeline fingerprint/provider and bounded totals. Existing OCR identity string is inert
   compatibility metadata, never permission to process OCR.
3. **Trusted finalize/recovery.** Reuse existing supervisor, staging repositories, `RagDocumentFinalizationGate`,
   `RagRecoveryService`, purge/current-state mechanisms and `RagExternalFileSource` CAS. Positive
   process/task-tree stop and writer drain/seal precede publication. Exact worker-only provenance
   must not be forgeable by runtime staging DML or an unconsumed/stale grant. Validate immutable
   sealed content against its actual receipt; test both writer/seal orders. Promote pending R,
   advance source version coherently, bind each G to its own root/children, install all validated
   successful G, record complete outcomes, terminalize and release the exact reserved union in
   **one Java physical PostgreSQL C_P**. No promotion/new G for a failed file; no partial success on
   common trust/fence/consent/SQL failure. Existing independently current prior G is not automatically
   invalidated by an ordinary failed replacement. Reconcile unknown commit outcome durably before
   any retry/release/terminal success; retain exclusivity while uncertain. Use existing statuses,
   `PARTIAL_SUCCESS` report-only, zero-new-G completion `ABORTED`, cancellation `CANCELLED`.
4. **FR18-3 evidence and integration.** Reproduce the retained named native test before repairs,
   preserve failure receipt and distinguish environment blockage from reproduced defect. Diagnose
   actual client/worker digest/provenance and identity mismatch; verify the repair with real Python,
   real pinned model, restricted roles and PostgreSQL/pgvector. Run the matrix, including separate
   `.md` 8/2 and all-failed batches. Preserve native DOMAIN coverage as compatibility; never convert
   it into synthetic DTO-only success or accept DOMAIN as external Markdown. Write the handback
   and evidence; leave closure to independent review.

Permitted source folders: `backend/.../rag/internal/{application,domain,infrastructure}` and
existing `rag/api`, `rag/source`, module-local web DTOs only for necessary existing-contract wiring;
`rag-engine/src/ppv_rag` worker/protocol and narrowly shared D17 result helpers; corresponding tests.
Reuse existing storage/columns. A necessary **function/grant/constraint repair within the already
approved RAG staging/fencing contract** may append after actual V21 (V22 if still unused), with
database-migration review and synchronized logical/API notes. Do not rewrite migrations or add
tables/columns, enums, routes, modules or architecture/dependencies. If those prove necessary,
stop with the exact unresolved contract/Owner decision instead of expanding this handoff.

## Entry invariants and non-goals

P18-03/P18-04's Owner-approved file-level outcomes amendment retains **one final physical
publication COMMIT**, source-first order, E1, v2 and exact union release. This is an entry
precondition, not an inferred new waiver. Hashing/parsing/model work/HTTP/process waits stay
outside SQL row-lock windows. Follow ADR-0021 graph → Vault source → external source → lifecycle
→ projections → policy → attempt → staging order, applicable subsets only; keep NOWAIT backstop.
Attempt-only stop request completes before source/process action. Recheck consent, reset,
versions, selection, child vector and attempt identity; no automatic consent renewal.

Phase 17 FROZEN; Slice 1/D14 and Slice 2/D23 CLOSED within bounds. D22 original evidence remains
NOT_RECOVERED. No global Phase 18 acceptance. No live-original input, source activation, deployment,
arbitrary disk discovery, new model, Office/PDF/OCR positive acceptance or later RAG feature.
Same-user application trust under ADR-0023 remains; no resurrected OS sandbox/R7 adversarial proof.

Defer final NO_CHANGE logic, same-R Reindex API, full lifecycle/purge/restore validation,
consolidated excluded-format removal, comprehensive resource profile and P18-FINAL to Slice 4.
Expose only necessary selected/pending R, G/closure and durable publication-outcome interfaces;
do not create a second job/lifecycle engine. Publication-local read eligibility/denial and unknown-C
recovery are mandatory now, not deferred under the lifecycle label.

## Test/evidence contract

All cases in the [matrix](slice-3-verified-indexing-test-matrix.md) are mandatory for this slice.
Focused Python/Java and actual native PostgreSQL checks form **S3-VERIFY** after convergence.
Do not add a duplicate full-suite gate for this intermediate slice: the single overall
five-command **P18-FINAL** runs after Slice 4 cleanup. Historical aggregate failure remains
FAILED until a genuine successful complete rerun; no D14 exception extension. Retained non-MD
positive quality experiments are not Slice 3 gates. Exact selected-scope runs must identify every
test selection/exclusion and must never be labelled full-suite success. Before P18-FINAL, Slice 4
must complete cleanup and the aggregate PostgreSQL resource investigation if still unresolved.

Use verified `.venv-d17/Scripts/python.exe` (currently Python 3.14.4), locked base/dev environment
for selected Markdown proof; inspect rather than overwrite legacy `.venv`. Run Ruff check/format,
fresh-cache mypy and applicable pytest from `rag-engine`. Configure Java native tests to launch
that verified selected runtime explicitly; record exact argv/environment selection. Missing models,
services, required tests, skips, nonzero exit or timeout are NOT PASS. No provisioning/upgrade or
model-artifact replacement is implicit.

Use `scripts/run-bounded-command.py`: 1,200s ordinary cap; 2,100s full-Java-suite only if supported
by invoking CLI. Preserve native owned-process cleanup receipts. Record input/output/fixture/model/
source hashes, test counts, role identities, physical commit observations and rollback/recovery
state in new unique directories under `logs/phase18/s3-<run-id>/`; never reuse D22 capture locations.
Required tracked handback: `slice-3-verified-indexing-handback.md` and machine evidence manifest
with exact commands/receipts and links. Sanitize content/paths/secrets; do not log private digests.
Measure Windows peak worker+owned-child **system RAM** separately from VRAM, time/CPU/temp disk
and bounded output-expansion/cancellation. Reuse unchanged D17 quality receipts by verified identity;
do not present them as new Java publication or comprehensive final resource proof.

**S3-VERIFY executable command shape**, from repository root after implementation has added its
matrix tests (update the new class/file names to their actual names and record the exact list):

```powershell
$S3Python = Join-Path (Get-Location).Path 'rag-engine/.venv-d17/Scripts/python.exe'
$S3Runner = Join-Path (Get-Location).Path 'scripts/run-bounded-command.py'
$S3Receipts = Join-Path (Get-Location).Path ('logs/phase18/s3-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $S3Receipts | Out-Null
# First reproduce retained failure before repairs; preserve this receipt even when nonzero.
& $S3Python $S3Runner --receipt (Join-Path $S3Receipts 'fr18-3-before.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagNativeDocumentBatchIntegrationTest#realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck' test
# After repairs: required existing trust/compatibility classes plus new matrix tests.
& $S3Python $S3Runner --receipt (Join-Path $S3Receipts 'java-s3.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagNativeDocumentBatchIntegrationTest,RagDocumentBatchIntegrationTest,RagDocumentInputCompatibilityTest,RagWorkerPostgresPrivilegeIntegrationTest,RagCommitAcknowledgementRecoveryIntegrationTest,RagSecondJvmRecoveryIntegrationTest,RagCancellationRecoveryIntegrationTest,RagSlice1SnapshotIntegrationTest,RagSnapshotCorrectiveIntegrationTest,RagSlice3*Test' test
if ($LASTEXITCODE -ne 0) { throw 'S3-VERIFY Java failed' }
Push-Location rag-engine
try {
  & $S3Python $S3Runner --receipt (Join-Path $S3Receipts 'ruff.json') -- $S3Python -m ruff check .
  if ($LASTEXITCODE -ne 0) { throw 'S3-VERIFY Ruff failed' }
  & $S3Python $S3Runner --receipt (Join-Path $S3Receipts 'format.json') -- $S3Python -m ruff format --check .
  if ($LASTEXITCODE -ne 0) { throw 'S3-VERIFY format failed' }
  & $S3Python $S3Runner --receipt (Join-Path $S3Receipts 'mypy.json') -- $S3Python -m mypy --cache-dir (Join-Path $S3Receipts 'fresh-mypy') src/ppv_rag
  if ($LASTEXITCODE -ne 0) { throw 'S3-VERIFY mypy failed' }
  & $S3Python $S3Runner --receipt (Join-Path $S3Receipts 'pytest.json') -- $S3Python -m pytest -q tests/test_obsidian_d17.py tests/test_d17_completion.py tests/test_d17_final_corrective.py tests/test_d17_r03_only.py tests/test_optional_ocr_dependency.py tests/test_worker.py tests/test_slice3_verified_indexing.py
  if ($LASTEXITCODE -ne 0) { throw 'S3-VERIFY pytest failed' }
} finally { Pop-Location }
```

The implementation must add the named Slice 3 matrix tests (or document exact replacements),
include applicable v2/module architecture regressions in the recorded selection, and explicitly
configure native worker executable/model paths as required by the existing fixture. Do not run
unmodified fixed-output historical D17 capture scripts; adapt new evidence paths first. An extra
test file name in a command is not proof its cases ran; report collected tests and map all matrix rows.

Relevant skills: `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`,
`modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`,
`python-rag-coding`, `local-rag-engineering`, `bounded-command-execution`; use
`database-migration-review` only for the bounded SQL repair. No redundant Python agent or new framework.

## Implementation result / review

Implementer fills handback and changes ACTIVE to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
Independent Codex verifies real results, all matrix rows and FR18-3 closure separately.
Historical Antigravity remediation **2/2 exhausted**, zero additional remediation rounds granted.
D24 grants this initial Slice 3 implementation only. On findings, stop for explicit Owner routing;
no automatic third round/takeover. No agent commit/push/tag/PR or self-acceptance.
