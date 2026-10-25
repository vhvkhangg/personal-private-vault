# D26 - Phase 18 pinned offline CPU-only applicability

**OWNER_APPROVED prospective scope amendment, 2026-10-25.** Authority:
[D26](owner-decisions.md#d26-cpu-only), explicitly selecting option B of the unchanged
[independent final corrective re-review](reviews/2026-10-25-slice-4-independent-final-corrective-re-review.md).
This is approval of the final execution **scope**, not Slice 4, P18-FINAL or Phase 18 closeout.
The documentation package awaits independent CPU-only final review.

## Exact accepted execution profile and GPU applicability

Phase 18 final execution scope is **pinned offline CPU inference ONLY**. Preserve the actual
verified multilingual model, compatible finite 384-dimensional vectors and trusted replay;
no remote API/model fetch, automatic downloads or model/vector-space substitution.

| Capability-specific subcase | Evidence truth retained | D26 prospective acceptance applicability |
| --- | --- | --- |
| Actual CUDA inference | **NOT VERIFIED** | **NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE** |
| Actual GPU OOM-to-CPU fallback | **NOT VERIFIED** | **NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE** |
| Process-attributed GPU VRAM | **NOT VERIFIED**; missing attribution remains UNKNOWN | **NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE** |

These statuses are never PASS, measured zero, fulfilled GPU tests or a fabricated fallback.
Starting with CPU is not GPU-to-CPU fallback. Existing device-wide NVML observations and
original GPU_EVIDENCE_BLOCKED reviews/receipts remain unchanged historical evidence.
GPU-specific inference/provider/OOM/VRAM acceptance portions of the named clauses below are
outside this CPU-only profile; no CPU resource, security or error-handling obligation is removed.

Future GPU enablement, CUDA/provider/dependency/configuration changes or GPU acceptance require
**separate explicit Owner authorization and independent verification**. Existing unexecuted GPU
code paths are not promoted or certified; this documentation does not alter runtime selection.
The currently verified installed CPU-capable runtime is preserved, including its installed
provider inventory; availability of an Azure provider is not permission for remote execution.

## Retained mandatory requirements

- **D24 `.md` ONLY:** all twelve frozen D17 requirements, complete static Obsidian structure,
  native Vietnamese/English/Simplified/Traditional Chinese text, exact locators and fail-closed
  all-or-nothing fidelity. No TXT/Office/PDF/OCR requirement or implementation is reintroduced.
- Exact source/revision/hash/length, whole-file consent, independently approved child revisions,
  full dependency authority, parser/chunker/model/tokenizer identity, no unauthorized file/network
  access or embedded execution; immutable snapshot reuse and frozen DOMAIN/v2 guarantees.
- Actual producer -> trusted Java verification -> restricted PostgreSQL publication, finite
  correctly dimensioned vectors, source-first guards, one physical C_P, full outcomes/reservations,
  NO_CHANGE/C_N, explicit same-R reindex, lifecycle/revoke/purge, recovery and unknown-C handling.
- **CPU/RAM/disk/wall/deadline/cancellation/security remain mandatory:** representative native
  producer + verifier + Java/PG paths, actual owned PID/creation identities, simultaneous tree RAM
  versus individual peaks, host headroom, bounded CPU work/batches/expansion/transport/chunks/temp,
  stop latency, positive process/writer safety, failure cleanup and honest report attribution.
  Host memory/disk pressure remains a real failure condition; no admission or time limit is raised.
- Accurate reports retain absent/unknown measurements and truthful device/resource identity;
  no CPU result or unrelated device-wide VRAM sample is called process-attributed GPU evidence.
- The **unchanged five-command P18-FINAL gate**, its command working directories, selected pinned
  interpreter, watchdog profiles, individual test limits and exact diagnostic-skip applicability.
  Independent review and explicit Owner disposition still precede closeout or delivery readiness.

## Precise prospective clause reconciliation

Only the following **GPU-specific acceptance portions** are superseded; unaffected text remains
binding. Each controlling document carries its own explicit D26 amendment. D24/D25, historical
reviews, dated corrective matrices/handbacks and failed/timeout/GPU receipts are not rewritten.

| Controlling source | Named affected clause | D26 effective requirement |
| --- | --- | --- |
| ADR-0021 | D18-A.4 exact model/producer tuple and linked release/acceptance guarantees | Same tuple and Java/PG verification, pinned CPU-only execution; no GPU evidence imported by linked acceptance |
| ADR-0022 | N10 retained offline/native GPU/same-model fallback, separate VRAM; remaining-gates GPU wording | Offline CPU/model and all non-GPU quality/resources/final gates retained; only GPU subcases excluded |
| ADR-0023 | Mandatory feature set's GPU/same-model CPU fallback, RAM/VRAM; retained N10 row | CPU inference/resources and all application-level trust/publication guarantees retained; GPU subcases excluded |
| ADR-0024 | Overall final-gate GPU/fallback boundary preserved by D22/D24 notices | D26 is the separate overall execution-profile decision, without rewriting slice/format history |
| D24 applicability record | AC-05/06/07/08 retained column; resource boundary's GPU/fallback | D24 format narrowing unchanged; this later execution amendment removes only GPU-specific portions |
| Slice 4 preparation and executable handoffs | Work package D resources, GPU stop language and missing-GPU handback condition | Complete measured CPU path remains required; historical missing GPU is not a current CPU-only blocker |
| Slice 4 acceptance matrix | **S4-15** | Complete CPU/native resource/cancellation/cleanup evidence remains mandatory; GPU subcases take the exact D26 status above |
| Slice 4 execution/test/resource plan | S4-15; AC-05; final resource/VRAM/fallback protocol | Preserve scenarios/limits/CPU/host/temp/stop evidence; no actual CUDA/fallback/process-VRAM gate under CPU-only scope |
| Acceptance and evidence | **AC-05**, GPU portions of AC-08, FCNC-15/IS-26/27/30 | Pinned offline multilingual CPU/model/finite vectors, full CPU resources/lifecycle/report/final evidence; no GPU PASS |
| P18-FINAL | P18-OFFLINE-MODEL, P18-RESOURCES and GPU-specific P18-REPORT fields | Retain CPU/model/report evidence and all five commands; GPU evidence only is not applicable |
| ACTIVE, Phase 18 README/test-evidence and Phase 18 row of RAG phase plan | Current execution-profile/status routing | Route to D26 and independent CPU-only final review, preserving dated blockers and future-phase boundaries |

This changes acceptance applicability only: **no logical schema, migration, module ownership,
dependency direction, package structure, authentication/security model, storage strategy or
frozen Phase 17 contract change**. No DBML/diagram/code/lock/model/runner modification is needed.

## Existing evidence and review boundary

The independent review found **no non-GPU mandatory blocker**, independently CLOSED F01-F05/F07/N01,
VERIFIED F06 CPU/native attribution and S4-16 tested capacity, and audited all five source-applicable
commands. This amendment uses that finding without independently re-reviewing or self-closing it.

| Existing applicable command | Retained outcome, not a fresh execution here |
| --- | --- |
| Ruff check | final-ruff and independent Ruff: exit 0 |
| Ruff format check | final-format and independent format: exit 0, 67 files |
| Mypy | final-mypy and independent fresh-cache mypy: exit 0, 22 source files |
| Full Python pytest | final-pytest-v2: exit 0, 238 testcase elements, 0 failures/errors/skips |
| Full Java clean verify | exit 0 / BUILD SUCCESS, 1,937.915s under unchanged 2,100s; 1,580 testcase elements vs 1,567 suite attributes, 0 failures/errors, 1 existing ADR-0023 diagnostic skip |

The independent 36-case native selection, seven cleanup checks, accepted D17/D20 quality and
source-bound CPU resource receipts remain applicable with their original measurement limits.
The old aggregate timeout's underlying slowdown stays UNKNOWN; this is not a new performance claim.
No machine gate, dependency experiment or expensive full-suite rerun is needed for documentation-only
applicability. Source, model, lock, command blocks and historical receipt hashes are checked instead.

**Still open procedural gate:** independent consistency/final review of this D26 package, followed
by explicit Owner disposition. S4-18 is not self-completed. Slice 4/P18-FINAL/Phase 18 remain
**NOT ACCEPTED / NOT CLOSED**; external indexing **INACTIVE**, live originals DISABLED/DEFERRED;
Phase 17 FROZEN; Slices 1-3/FR18-3 CLOSED; D22 originals NOT_RECOVERED. No staging/publishing.
