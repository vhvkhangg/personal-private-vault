## D26 prospective CPU-only applicability - 2026-10-25

**OWNER_APPROVED scope amendment; final acceptance NOT GRANTED.** [D26](owner-decisions.md#d26-cpu-only)
selects pinned offline CPU inference ONLY. Actual CUDA inference, GPU OOM-to-CPU fallback
and process-attributed GPU VRAM remain **NOT VERIFIED** and are
**NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE**, never PASS. See the
[exact execution profile and reconciliation](cpu-only-d26-applicability-2026-10-25.md).

**Work package D effective requirement:** representative native full-path offline CPU inference, CPU/wall/tree RAM/host headroom/temp disk/expansion/cancel/cleanup with exact producer/verifier/source identities. Missing actual CUDA/fallback/attributed GPU VRAM is historically NOT VERIFIED but no longer a CPU-only acceptance blocker. Existing successful gate and independent non-GPU review evidence are retained; no implementation or new measurement cycle starts.

This is prospective acceptance applicability only. Unaffected Markdown/authority/publication,
CPU/RAM/disk/deadline/cancellation/security and five-command requirements remain mandatory.
Future GPU enablement requires separate Owner authorization and independent verification.
No source/runtime/test/model/lock/runner change, activation or closeout follows. Original
GPU_EVIDENCE_BLOCKED and failed/timeout evidence remains unchanged historical truth.
Slice 4/P18-FINAL/Phase 18 remain NOT ACCEPTED / NOT CLOSED; indexing INACTIVE.

## Preserved pre-D26 text - unaffected rules remain effective

> **Corrective handback - 2026-10-24:** `BLOCKED / GPU_EVIDENCE_BLOCKED`. See [handback](slice-4-codex-corrective-handback-2026-10-24.md) and [S4 matrix](slice-4-codex-corrective-matrix-2026-10-24.md). Native 104 and Python 238 pass; actual full Java exit 1 ({'tests': 1567, 'failures': 1, 'errors': 0, 'skipped': 1}). CUDA inference/fallback remains unavailable and unwaived. Slice 4/P18-FINAL NOT ACCEPTED. Earlier dated results/statuses below are preserved history.

> **Codex executable handoff issued ? 2026-10-23:** [ACTIVE](../handoffs/ACTIVE.md) is now `P18-S4-LIFECYCLE-FINAL / READY_FOR_IMPLEMENTATION`. Use the [executable contract](slice-4-executable-handoff-2026-10-23.md), [complete execution/test/resource plan](slice-4-execution-and-test-plan-2026-10-23.md) and [baseline assessment](reviews/2026-10-23-slice-4-handoff-preparation.md). The preparation status below records its earlier pending state. No implementation or S4/P18-FINAL acceptance is claimed.

# P18-S4-LIFECYCLE-FINAL — Slice 4 implementation handoff preparation (E+H)

**Owner-authorized:** 2026-10-23, D25.
**Baseline:** published `main` `e2e00534fb35534a298e8fb15d174f18cb63b5a0`; recheck actual HEAD, origin/main, working tree and code at execution.
**Handoff status:** `PENDING_CODEX_HANDOFF` — explicit Owner authorization to start preparation/implementation routing, **not** a claim that the executable implementation handoff has been independently validated or implemented.
**Format target:** external **Obsidian `.md` only** under D24; DOMAIN v2/v3 behavior remains compatible as required.

## Required input and frozen boundaries

Read `AGENTS.md`, `docs/implementation/handoffs/ACTIVE.md`, D14, D17/D20/D21/D22/D23/D24/D25, Slice 1/2/3 closeouts, ADR-0020–24, the executable Slice 3 S3-01–16 matrix and 2026-10-23 independent review, `simplified-implementation-handoff.md`, `acceptance-and-evidence.md`, `final-verification-gate.md`, and the actual source/migrations/tests. Inspect existing lifecycle, sources, document batch, original-byte store, report, purge, recovery, job, SQL and worker mechanisms before proposing work. Reuse rather than rebuilding mechanisms already accepted. No cross-module internal imports, broad refactors, new job engine or rollback of frozen security contracts.

## Work package A — NO_CHANGE and same-R Reindex

Implement the required distinctions using existing user-visible and durable semantics:

- On a user-authorized repeated capture/update, treat **same bytes** as merely a NO_CHANGE *candidate*. True NO_CHANGE requires independently verified current selected immutable R (length/SHA-256 and source authority), original consent/reset/epoch/policy/retention, exact approved dependency graph and child revisions, selected G eligibility, valid pipeline/parser/chunker/model/vector identity, and no change that warrants reindex. No new R, sourceVersion, G or unnecessary Python job for genuine NO_CHANGE.
- A new/changed/unavailable child, changed pipeline/model, stale/missing/tampered selected R or G, revoked grant, source policy/version mismatch, or changed authorized target must never produce false NO_CHANGE; fail closed or take the explicitly permitted snapshot/reindex path. The live original is not silently reread except as part of an explicit validated new-capture request.
- **Same-R Reindex** uses the verified immutable selected R and currently approved immutable children without recapturing/replacing R. A successful reindex creates a new verified G only through the accepted Slice 3 result-authenticated `C_P` and CAS guards; old G visibility/cleanup follows source-of-truth selection. A failed reindex does not create/select a partial G or erase the correctly qualified previous one.

## Work package B — lifecycle, revocation, recovery, reporting

- Complete exact source/revision/generation qualification, stale/changed/withdrawn authority denial, revoke/purge/restore/reapprove semantics, parent/child grant loss, anti-resurrection, no orphan adoption, source-first fencing, cancellation and durable unknown-commit reconciliation. Preserve typed DOMAIN versus external UUID authority and owner identity isolation.
- Explicit failures and warnings per file: succeeded, failed, no-change, retained prior G versus new G, selected/pending R, qualification of `SNAPSHOT_CURRENT`, unknown freshness of original (`UNVERIFIED`), accurate cancellation, resource/fallback and cleanup outcomes. Never infer currentness solely from job terminal COMMITTED/COMPLETED.
- Protect still-live/unknown processes and temp files until positive stop proof; manage disk-full/partial copy/rollback/crash cleanup and bounded retry with the existing recovery authority. Preserve correct one-physical-C_P publication semantics; no new general transaction coordinator.

## Work package C — one consolidated excluded-format cleanup

**Do this once, near the END of Slice 4, BEFORE P18-FINAL**, after packages A/B are verified and before final clean test execution.

Inventory actual Java/Python source, handlers, parsers, dispatch, tests/fixtures, sample assets and dependency/lock configuration. Remove **unwanted unaccepted support for TXT, `.markdown`, PDF, DOCX, XLS/XLSX, OCR** rather than keeping inactive feature code for hypothetical reuse. Delete unreachable unaccepted adapters, optional OCR/model installation/provisioning code, obsolete format tests/fixtures and incompatible packages/lock groups *only when no required `.md`, DOMAIN, safety, v2 compatibility or native producer function depends on them*. Strip obsolete format dispatch/routes/claims; remove inert OCR identity metadata only by a coordinated version/compatibility decision if changing it affects Java/Python fingerprint and stored G eligibility.

Do NOT delete applied historical Flyway migration versions, executed original reviews/failed receipts, D22 loss records, or other irreplaceable audit proof merely because removed code is no longer accepted. Record a precise `before → after` deleted/retained inventory and explain shared-file dependency exceptions. Update current canonical docs prospectively, keeping dated history legible. This package is a removal/compatibility task, **not** an acceptance test program for discarded formats.

## Work package D — final integration gates and resource investigation

- Resolve the inherited PostgreSQL aggregate connection/slot-resource issue with bounded investigation and appropriate focused correction, retaining the earlier **1,433/1 failure/2 errors** receipt as FAILED. Run the actual full clean verification after cleanup; no focus-only substitute or D14 global exception.
- Verify authentic `.md` vi/en/zh-Hans/zh-Hant structures, permissions, child/grants, actual pinned ONNX embedding, source-backed locators, fail-closed active content, full integration quality, transaction/rollback, multi-root outcomes, lifecycle/reindex, offline behavior and SQL roles using actual PostgreSQL.
- Gather representative full-path CPU time, wall time, live owned-process working set/private RAM, host available RAM, temp disk, expansion, cancellation and cleanup evidence; separately measure device-wide VRAM versus process-attributable VRAM. If CUDA is unavailable, record `CUDA UNAVAILABLE`, actual local CPU fallback identity/results and any unmet GPU acceptance obligation explicitly. Never synthesize a GPU PASS, overstate maximum throughput, or label a failed command green.
- Run the **unchanged five P18-FINAL commands** in `final-verification-gate.md`: Python Ruff check, Ruff format check, mypy, full pytest and Java `mvn clean verify`, with real watchdog exit/timeout/coverage receipts. Resolve the applicability of old format-specific cases using D24's prospective amendment, not as fabricated historical PASS.

## Delivery / sequencing

1. Codex reviews the current published baseline and turns this preparation into a minimally scoped, executable Slice 4 handoff with tests mapped to [S4 acceptance matrix](slice-4-lifecycle-final-acceptance-2026-10-23.md). Correct the handoff, not accepted frozen contract, if mismatched.
2. Implement **A**, then **B**, with focused regressions and independent review as appropriate. Do not activate external indexing.
3. Execute **C exactly once** at the agreed late Slice 4 boundary; review deleted/retained inventories before deleting source and never destructively purge owner data or DB history.
4. Execute **D**, preserve receipts, independent final review and Owner commit/push/closeout. Full Phase 18 acceptance and indexing activation require separate explicit final disposition; Slice 4 handback alone cannot grant either.

## No-go constraints

Phase 17 frozen; Slice 1/D14, Slice 2/D23, Slice 3/D25 closed; FR18-3 closed only within its verified boundary; historical Java full gate FAILED until new real success; D22 originals NOT_RECOVERED; overall Phase 18/P18-FINAL NOT ACCEPTED; external indexing INACTIVE; live-original capability DISABLED/DEFERRED. Preserve Antigravity historical 2/2 remediation limit; no automatically replenished rounds. No agent commit/push/tag/PR, no internet model downloads, no OCR research, no unsupported file-format functionality, no unwarranted SQL migration/API change.

Return `READY_FOR_SLICE_4_IMPLEMENTATION_HANDOFF` only when the executable handoff, requirements, baseline and test mapping are internally consistent and reviewed. Otherwise return `OWNER_DECISION_REQUIRED` for a specific contract conflict. Do not claim Slice 4 implemented or P18-FINAL accepted during preparation.
