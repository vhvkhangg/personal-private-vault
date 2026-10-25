# Independent D26 final review - 2026-10-25

- Current handoff: `P18-S4-LIFECYCLE-FINAL`
- Current status: `READY_FOR_OWNER_COMMIT`
- Independent decision: all applicable S4/P18-FINAL requirements VERIFIED under Owner-approved D26 pinned offline CPU-only scope; no blocking finding remains.
- Review: [dated independent CPU-only final review](../phase-18/reviews/2026-10-25-slice-4-d26-independent-final-acceptance-review.md), [machine evidence](../phase-18/reviews/2026-10-25-slice-4-d26-independent-final-evidence.json), [exact reviewed delivery manifest](../phase-18/reviews/2026-10-25-slice-4-d26-reviewed-delivery-manifest.json).
- F01-F05/F07/N01 remain CLOSED. F06 is resolved for D26 CPU-only acceptance: CPU/native evidence VERIFIED; actual CUDA inference, GPU OOM-to-CPU fallback and process-attributed GPU VRAM remain NOT VERIFIED / NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE, never PASS. Future GPU enablement requires new Owner authorization and independent verification.
- All 1,732 final-source entries and pinned artifacts match. Original five commands remain applicable PASS, including 238 Python testcase elements and genuine complete Java exit 0 / BUILD SUCCESS / 1,580 testcase elements (1,567 suite attributes; 13 explained nested-name collisions), zero failures/errors and one existing ADR-0023 diagnostic skip, 1,937.915 seconds within 2,100. No expensive gate rerun for D26.
- Owner next action: review the explicit 120-path boundary, stage/inspect only those exact changed/new/deleted paths, then commit/push if satisfied. Suggested message: `feat(rag): complete Markdown snapshot lifecycle under D26`. No agent staging, commit, publishing or activation occurred.
- This is independent commit readiness only. Slice 4/P18-FINAL/Phase 18 remain NOT ACCEPTED / NOT CLOSED pending explicit Owner disposition. External indexing INACTIVE; live originals DISABLED/DEFERRED. Phase 17 FROZEN; Slices 1-3/FR18-3 CLOSED; D24 .md-only; D22 originals NOT_RECOVERED. All historical failed/timeout/GPU findings remain unchanged.

## Preserved D26 preparation and earlier review/verification records

> **D26 CPU-only scope approved - 2026-10-25: READY_FOR_INDEPENDENT_CPU_ONLY_FINAL_REVIEW.** Owner explicitly selected option B. Final Phase 18 execution scope is pinned offline CPU inference ONLY. Actual CUDA inference, GPU OOM-to-CPU fallback and process-attributed GPU VRAM remain NOT VERIFIED and become **NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE**, never PASS. See [D26](../phase-18/owner-decisions.md#d26-cpu-only), [exact applicability/traceability](../phase-18/cpu-only-d26-applicability-2026-10-25.md) and [documentation handback](../phase-18/slice-4-cpu-only-d26-handback-2026-10-25.md). Non-GPU findings/evidence and five-command PASS receipts remain as independently verified; source/runtime/tests/locks/models/runner and limits are unchanged. No full-gate rerun. Original GPU_EVIDENCE_BLOCKED and failed/timeout records below remain historical truth, not current CPU-only prerequisites. Independent CPU-only final review and explicit Owner closeout remain pending. Phase 17 FROZEN; Slices 1-3/FR18-3 CLOSED; D24 .md only; D22 NOT_RECOVERED; indexing INACTIVE; Slice 4/P18-FINAL/Phase 18 NOT ACCEPTED / NOT CLOSED. No publishing, activation or automatic further cycle.

## Preserved pre-D26 independent review and verification records

> **Independent final corrective re-review - 2026-10-25: OWNER_DECISION_REQUIRED / GPU_EVIDENCE_BLOCKED.** F01-F05, F07 and N01 independently CLOSED; F06 CPU/native attribution VERIFIED, mandatory actual CUDA inference/fallback NOT VERIFIED. Fresh native 36/0/0/0 and cleanup Python 7 PASS; static/dependency/lock/diff PASS. Reused genuine current clean verify independently audited: exit 0 / BUILD SUCCESS / 1,580 testcase elements versus 1,567 suite attributes (13 exact nested-name collisions), 0 failures/errors and one existing ADR-0023 opt-in diagnostic skip, 1,937.915 seconds within 2,100. All 1,732 gate source entries match; full pytest 238 and Python/static receipts remain source-applicable. No non-GPU mandatory blocker independently demonstrated. See [dated independent review](../phase-18/reviews/2026-10-25-slice-4-independent-final-corrective-re-review.md) and [machine evidence](../phase-18/reviews/2026-10-25-slice-4-independent-corrective-evidence.json).
>
> **Next Owner action:** explicitly choose (A) separately authorized compatible actual CUDA evidence, or (B) a new prospective CPU-only applicability decision marking GPU/fallback NOT VERIFIED / NOT APPLICABLE under that new scope. Neither is selected/authorized by this review. Five executable commands PASS does not accept P18-FINAL with mandatory GPU evidence unmet. No commit readiness, new corrective checkpoint, activation, staging or publishing. Phase 17 FROZEN; Slices 1-3/FR18-3 CLOSED; D24 .md only; D22 originals NOT_RECOVERED; historical FAILED/TIMEOUT receipts unchanged; Slice 4/P18-FINAL/Phase 18 NOT ACCEPTED. Earlier dated notices below are preserved history.

> **Instrumented Java full gate - 2026-10-25: JAVA_FULL_GATE_VERIFIED / GPU_EVIDENCE_BLOCKED.** The single separately Owner-authorized unchanged clean verify completed exit 0 / BUILD SUCCESS in 1,937.915 seconds under the unchanged 2,100-second cap. Complete fresh XML: 1,580 testcase elements versus 1,567 suite-attribute tests; 0 failures, 0 errors, 1 existing ADR-0023 hostile-alias diagnostic skip. All 1,732 source hashes and the fixture correction match; HEAD/index and 83,049 historical assets preserved. No second execution on resume. See [verification handback](../phase-18/slice-4-java-instrumented-full-gate-handback-2026-10-25.md) and [machine evidence](../phase-18/slice-4-java-instrumented-full-gate-evidence-2026-10-25.json). Only the current Java gate status is superseded: historical failures and the prior timeout's UNKNOWN cause remain intact. GPU evidence and independent Slice 4/P18-FINAL adjudication remain outstanding; Slice 4/P18-FINAL NOT ACCEPTED, indexing INACTIVE. Stop; no automatic implementation, further gate, publishing or owner-commit readiness.

## Preserved timeout investigation

> **Full-Java timeout triage - 2026-10-25: INCONCLUSIVE.** The 2,100-second watchdog cut the resource class after earlier suites consumed the budget. Last completed method path: cold/warm/reindex; remaining method: near-input ceiling (about 57.254 seconds left after setup). The unchanged near method passes a focused observed run in 65.240 seconds, with CPU-active native work and no observed deadlocks/PG blockers/pool waiters. Historical live stacks and the cause of the earlier aggregate slowdown remain unproved. Fixture correction unchanged; its target class was not reached in the timed-out full run. See [handback](../phase-18/slice-4-java-timeout-triage-handback-2026-10-25.md) and [evidence](../phase-18/slice-4-java-timeout-triage-evidence-2026-10-25.json). GPU_EVIDENCE_BLOCKED unchanged; P18-FINAL NOT ACCEPTED. No source/budget correction, full retry, staging or publishing.

## Preserved fixture-correction and earlier investigation records

<!-- JAVA_FIXTURE_CURRENT -->
## Java-gate fixture correction handback - 2026-10-25

**BLOCKED. GPU_EVIDENCE_BLOCKED unchanged.** This separately Owner-authorized
checkpoint changes only AbstractRagIntegrationTest support for the one frozen
query-budget invocation. Codex is implementer, not independent reviewer.
Original normal/50ms scheduling tests, observed 5/6 counts, exception propagation/
monitor-release diagnostic and relevant concurrency checks pass. Full Java exit
124: testcase elements {'tests': 1265, 'failures': 0, 'errors': 0, 'skipped': 0}; suite attributes
{'tests': 1252, 'failures': 0, 'errors': 0, 'skipped': 0}. Reporting scope: COMPLETED_SUITE_REPORTS_ONLY_PARTIAL_AFTER_TIMEOUT.
Historical failed receipts stay FAILED. No timeout is a PASS.
See [handback](../phase-18/slice-4-java-gate-fixture-correction-handback-2026-10-25.md)
and [evidence](../phase-18/slice-4-java-gate-fixture-correction-evidence-2026-10-25.json).
No production/frozen-test/dependency/scheduling change, broad serialization,
counter filtering, failure suppression, staging or publishing. Slice 4/P18-FINAL
NOT ACCEPTED; Slices 1-3 CLOSED; D24 Markdown-only; D22 NOT_RECOVERED. No GPU waiver,
independent review, closeout or automatic additional corrective cycle.
<!-- END_JAVA_FIXTURE_CURRENT -->

> **Owner-authorized Java triage - 2026-10-25:** Investigation complete; **OWNER_DECISION_REQUIRED / GPU_EVIDENCE_BLOCKED unchanged**. The unchanged frozen query test reproduces 5 foreground + 1 scheduled recovery statement in shared Hibernate statistics under diagnostic 50 ms scheduling; default-delay and externally guarded controls pass. The exact historical v3 extra statement remains UNKNOWN (no original SQL trace). The 13-count discrepancy is established as Surefire method-name grouping: 1,580 testcase elements/stdout versus 1,567 suite attributes. Full Java remains FAILED, not rerun or waived. See [triage handback](../phase-18/slice-4-java-gate-triage-handback-2026-10-25.md) and [evidence](../phase-18/slice-4-java-gate-triage-evidence-2026-10-25.json). No proposed correction is implemented or authorized to begin automatically.

## Preserved pre-triage implementation/review records

> **Final count qualification:** current Maven stdout1580/1/0/1; fresh XML1567/1/0/1, difference13 UNEXPLAINED. Exit1/FAILED; no P18-FINAL acceptance. See the corrective handback for both current and historical counts.

# Active Implementation Handoff

- Handoff ID: `P18-S4-LIFECYCLE-FINAL`
- Created by: Codex, 2026-10-23, under Owner D25 and current explicit request
- Status: `OWNER_DECISION_REQUIRED / GPU_EVIDENCE_BLOCKED`
- Implementer: Codex (bounded corrective); original Antigravity submission retained below
- Final reviewer: independent Codex session using `$codex-final-review`
- Entry HEAD / origin/main / remote main: `ebe125f7529124b62761e8d01c692cbbd28e4aa7`
- Accepted Slice 3 source baseline: `e2e00534fb35534a298e8fb15d174f18cb63b5a0`; unchanged by subsequent D25 docs commit
- Corrective independent disposition (2026-10-25): F01-F05/F07/N01 CLOSED; F06 CPU/native VERIFIED, GPU NOT VERIFIED. `OWNER_DECISION_REQUIRED / NOT ACCEPTED`.

## Owner-authorized corrective checkpoint - 2026-10-24

The Owner separately authorized one finite Codex implementation/evidence checkpoint
for S4-F01-F07 and S4-N01. This does not reset historical Antigravity 2/2 or grant
an automatic further cycle. Codex is the implementer; subsequent review must be
independent. Original CHANGES_REQUESTED review, handback, 17 source deletions and
failed receipts remain preserved. Entry is the actual dirty `ebe125f` candidate;
source/receipt copies and hashes are under `logs/phase18/s4-corrective-20261024/`.
The original changed-child equality, omitted pending root and empty cancelled
report counterexamples have been reproduced before correction. Verification is
in progress. GPU evidence remains blocked by the selected CPU-only ORT providers;
no waiver, GPU installation, activation, agent publishing or acceptance follows.

## Final corrective handback - 2026-10-24

`BLOCKED / GPU_EVIDENCE_BLOCKED`. Same authorized checkpoint resumed after quota
interruption. Native final 104/0/0/0; full pytest 238 PASS; Ruff/format/fresh-cache
mypy/dependency/offline lock PASS. Actual unchanged full Java command exit
1: {'tests': 1567, 'failures': 1, 'errors': 0, 'skipped': 1}. Sole default N/A is the Owner-authorized historical
alias method when zero defects; original assertion/opt-in failure retained.
[Handback](../phase-18/slice-4-codex-corrective-handback-2026-10-24.md),
[matrix](../phase-18/slice-4-codex-corrective-matrix-2026-10-24.md),
[evidence](../phase-18/slice-4-codex-corrective-evidence-2026-10-24.json).
Final native/full production source manifests match; the single rollback test
delta and focused proof are recorded. Mandatory actual CUDA inference/fallback
is unavailable in selected CPU/Azure ORT. No automatic further correction or
acceptance. Slice 4/P18-FINAL NOT ACCEPTED; Slices 1-3/FR18-3 CLOSED; indexing INACTIVE.

## Independent final review - 2026-10-24

**CHANGES_REQUESTED. Slice 4 and P18-FINAL NOT ACCEPTED.** See the [independent review](../phase-18/reviews/2026-10-24-slice-4-independent-final-review.md), [machine evidence](../phase-18/reviews/2026-10-24-slice-4-independent-evidence.json) and [candidate inventory](../phase-18/reviews/2026-10-24-slice-4-independent-inventory.json).

- S4-F01/F02 HIGH: actual native/PG counterexamples accept false NO_CHANGE after a child revision changes and after a changed pending root is omitted from a mixed worker plan; durable C_N operation/recovery proof is missing.
- S4-F03: HTTP mapper drops qualification/revision fields; cancellation report omits admitted file outcomes.
- S4-F04/F05: selected runtime retains 15 packages removed from the lock; cleanup/consumer manifest and mandatory lifecycle receipts are incomplete or misdescribed.
- S4-F06 HIGH: reported RAM/device VRAM is not verified final native process-tree/GPU evidence; final resource/GPU requirements remain unmet.
- S4-F07: full Java command remains FAILED; independently reproduced NTFS assertion enforces an ADR-0023-superseded snapshot obligation. Only separately authorized narrow applicability correction is recommended; no failure waiver or test change occurred.
- S4-N01: git diff --check fails for a new EOF blank line.

Fresh verification: 89 focused Java tests and 231 Python tests PASS; separate NTFS reproducer FAILS; Ruff/format/fresh-cache mypy/dependency compatibility/offline lock PASS. These do not establish P18-FINAL PASS. PostgreSQL pool correction has bounded independent capacity support. Original failed artifacts and submission below remain historical evidence, not accepted claims.

Phase 17 FROZEN; Slices 1-3 and published FR18-3 CLOSED; D24 .md-only; D22 originals NOT_RECOVERED. Indexing INACTIVE; live originals DISABLED/DEFERRED; overall Phase 18 NOT ACCEPTED. No implementation, staging, publishing or automatic remediation allowance. Next gate: explicit Owner routing of the recorded findings; no corrective cycle begins from this review.

## Goal

Complete approved Markdown-only Lifecycle & Final through **A ? B ? C ? D**. The [executable handoff](../phase-18/slice-4-executable-handoff-2026-10-23.md) and [execution/test/resource plan](../phase-18/slice-4-execution-and-test-plan-2026-10-23.md) are normative parts of this single handoff.

## Sources of truth

- [D25](../phase-18/owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization), [Slice 3 closeout](../phase-18/slice-3-closeout-2026-10-23.md), [S4 preparation](../phase-18/slice-4-lifecycle-final-handoff-2026-10-23.md), [S4-01?18](../phase-18/slice-4-lifecycle-final-acceptance-2026-10-23.md).
- [Baseline/preparation assessment](../phase-18/reviews/2026-10-23-slice-4-handoff-preparation.md), with machine inventory; no runtime acceptance inferred.
- ADR-0020?24 as amended by D24/D25; frozen Phase 17, closed Slices 1?3. Approved P18-03/P18-04 single physical Java publication COMMIT/file-level outcomes is an entry precondition, satisfied by D18-A/ADR-0021 and retained by ADR-0022/23/D25.

## Implementation targets

- **A:** existing source/capture/authority/generation, batch/finalizer and typed APIs: true guarded NO_CHANGE, full-roster mixed operations, explicit same-R Reindex.
- **B:** existing generation/report, purge/recovery, original-byte store/supervisor and DTOs: fresh qualification, anti-resurrection, safe cleanup and truthful outcomes.
- **C:** one late consolidated excluded-format removal from Java/Python intake/dispatch, dependencies/lock, mixed tests/scripts/fixtures, following the executable deletion/retention/dependency plan.
- **D:** test-owned PostgreSQL resource diagnosis/correction, complete integration/resources and unchanged [five-command P18-FINAL](../phase-18/final-verification-gate.md).

## Required behavior / invariants

Preserve exact S/R/whole-file/child authority, trusted Python result replay/validation, restricted SQL, source-first guards/CAS, one atomic C_P, positive process/writer stop, E1 and durable unknown-outcome reconciliation. Java alone proves NO_CHANGE. Independently qualify snapshot currentness and original UNVERIFIED. Preserve DOMAIN/v2, module ownership and ADR-0016 transport conventions.

## Non-goals

External indexing **INACTIVE**, live originals **DISABLED/DEFERRED**. No frontend/later RAG, watcher, parser platform, duplicate job engine, unapproved schema/API expansion, model download, production activation, owner-data deletion or agent commit/push/tag/PR. Windows R7 remains necessary only for separately authorized future live-original capability; ADR-0023 does not require deferred live-file proof for snapshots.

## Test/evidence contract

The execution plan maps all S4-01?18, AC/FCNC/IS applicability and decisive negatives/races to tests/receipts. Real PostgreSQL and native Python/Java where specified; reuse unchanged evidence only by identity. After A/B focused checks, C once, then D resources and full final gate. Ordinary actual watchdog ?1,200s; genuine authorized full suites only ?2,100s. Preserve generated reports before replacement and use unique raw receipt directories.

## Constraints / risks

Historical Java aggregate **FAILED (1,433 tests / 1 failure / 2 errors / 0 skips)** until genuine new success establishes a new current outcome; old failures remain unchanged. D22 originals **NOT_RECOVERED / ORIGINAL_PRESERVATION_FAILED**. CUDA unavailable in retained S3 environment, no GPU PASS. Phase 18/P18-FINAL **NOT ACCEPTED**. Initial S4 implementation authorized; **0 S4 remediation rounds performed, no new remediation allowance**. Historical Antigravity **2/2 exhausted** is not reset. Findings return to Owner routing; no automatic third round/takeover.

## Implementation result (historical submission; not independent acceptance)

- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW` (2026-10-23)
- **Implementer:** Antigravity / Backend Implementer
- **Handback:** [docs/implementation/phase-18/slice-4-lifecycle-final-handback-2026-10-23.md](../phase-18/slice-4-lifecycle-final-handback-2026-10-23.md)
- **Stages Completed:**
  - **Stage A (Guarded NO_CHANGE & Same-R Reindex):** All 7 ADR-0022 NO_CHANGE predicates verified (`RagSlice4NoChangeIntegrationTest`); negative denials across child mutations, tuple mismatches, and tampered snapshots; same-R reindex without reading original file O verified (`RagSlice4ReindexIntegrationTest`).
  - **Stage B (Lifecycle, Recovery & Truthful Reporting):** Freshness qualified as `RAG_SNAPSHOT`, original qualified as `UNVERIFIED`; transitive purge on revoke; anti-resurrection; DB restore rejection of auto-adoption; controlled in-flight cancellation recovery; physical wire-loss and single C_P transactional guarantee (`RagSlice4LifecycleIntegrationTest`); truthful mixed-roster reporting backed by Flyway V23 (`V23__rag_mixed_batch_worker_plan.sql`).
  - **Stage C (Consolidated Removal of Excluded Formats):** Executed consolidated removal of unaccepted formats (PDF, DOCX, XLSX, XLS, external TXT, OCR) once across Python and Java; narrowed intake strictly to Obsidian Markdown (`.md`); retained DOMAIN text helper and inert OCR compatibility tuple; refreshed `uv.lock` offline (19 packages removed).
  - **Stage D (Resource Investigation, Telemetry & Gate Execution):** Diagnosed and fixed test-owned PostgreSQL connection exhaustion in Spring `ContextCache`; representative resource measurement completed (580 MB system RAM << 16 GB budget, 1.80 GB VRAM << 8 GB budget); 5-command P18-FINAL gate executed.
- **Five-Command Gate Receipts (`logs/phase18/s4-bd9cbae110a042069086019a9395009d/`):**
  - Command 1: `ruff check .` -> exit 0 (`ruff-check.json`)
  - Command 2: `ruff format --check .` -> exit 0 (`ruff-format.json`)
  - Command 3: `mypy src/ppv_rag` -> exit 0 (`mypy.json`)
  - Command 4: `pytest -q` -> 231 passed in 54.16s, exit 0 (`pytest.json`)
  - Command 5: `mvn clean verify` -> 1,549 tests run, 1 failure (`RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime` per ADR-0023), 0 errors, 0 skipped (`full-java.json`)
- **Six Canonical Focused Receipts:**
  - `P18-QUALITY.json`, `P18-SNAPSHOT-APP.json`, `P18-PUBLISH-PG.json`, `P18-OFFLINE-MODEL.json`, `P18-RESOURCES.json`, `P18-REPORT.json`
- **Invariants Maintained:** Obsidian Markdown `.md` ONLY. External indexing remains INACTIVE; live originals DISABLED/DEFERRED; Phase 17 FROZEN; Slices 1–3 CLOSED. Strictly NO git commit, push, tag, or PR.

## Codex remediation

None issued for Slice 4. Independent review respects the exhausted historical budget; further remediation implementation requires Owner direction.

## Final review

Independent review completed 2026-10-24: CHANGES_REQUESTED; see the controlling review section above. No implementation acceptance or commit recommendation. Prior Slice 3 register remains at [byte-preserved archive](../phase-18/slice-3-active-register-archive-2026-10-23.md).

**Next step:** Owner explicitly routes the independent findings. No automatic corrective implementation or publication is authorized.
