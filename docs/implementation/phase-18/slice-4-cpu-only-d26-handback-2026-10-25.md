# Slice 4 D26 CPU-only applicability handback - 2026-10-25

**READY_FOR_INDEPENDENT_CPU_ONLY_FINAL_REVIEW.** This is the Owner-authorized documentation-only
amendment adopting option B of the unchanged independent final corrective re-review. It is not
the independent CPU-only final review and does not close Slice 4, P18-FINAL or Phase 18.

## Recorded decision and exact applicability

The next unused decision ID was **D26**, following D25. The
[new Owner Decision](owner-decisions.md#d26-cpu-only) approves pinned offline **CPU inference ONLY**
as the final Phase 18 execution scope. The [canonical applicability/traceability matrix](cpu-only-d26-applicability-2026-10-25.md)
defines the exact prospective changes and retained requirements.

| GPU-specific subcase | Evidence status | New prospective applicability |
| --- | --- | --- |
| Actual CUDA inference | NOT VERIFIED | NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE |
| GPU OOM-to-CPU fallback | NOT VERIFIED | NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE |
| Process-attributed GPU VRAM | NOT VERIFIED; missing attribution stays UNKNOWN | NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE |

None becomes PASS, zero measured VRAM or an actual fallback. Original GPU_EVIDENCE_BLOCKED
findings, device-wide measurements, failed/timeout receipts and independent reviews remain intact.
Future GPU enablement requires separate explicit Owner authorization and independent verification.
Existing unexecuted GPU paths and installed provider inventory are unchanged, not newly certified.

## Synchronized controlling documents

Additive prospective amendments cover **16 existing documents**:

- Owner decision ledger and ACTIVE.
- ADR-0021, ADR-0022, ADR-0023 and ADR-0024, with precise GPU-only clause qualifications.
- D24 overall Markdown applicability record; its original decision/baseline is preserved.
- Slice 4 preparation handoff, executable handoff, acceptance matrix and execution/resource plan.
- Acceptance-and-evidence (AC-05, GPU portions of AC-08 and FCNC-15/IS-26/27/30).
- Final-verification-gate (OFFLINE-MODEL, RESOURCES and GPU-specific REPORT applicability).
- Phase 18 README, test-evidence and the Phase 18-only notice in the RAG phase plan.

Every prior file body is preserved **byte-for-byte as a suffix**. D24/D25 decision history and
dated corrective matrices/handbacks/reviews are not rewritten. The effective S4-15 CPU/native
resource requirement stays mandatory and independently VERIFIED within its reviewed limits;
only its GPU subcases take the new applicability. AC-05 still requires the pinned multilingual
offline CPU model, finite compatible 384-dimensional vectors and trusted actual-output proof.
No CPU/RAM/disk/deadline/cancellation/security, source/revision/consent/dependency, Markdown,
NO_CHANGE/reindex/lifecycle/recovery or Java/PostgreSQL requirement is removed.

No schema, module/dependency direction, repository structure, authentication/storage strategy or
frozen Phase 17 guarantee changes. No DBML, migration, runtime, source, test, model, dependency,
plugin, lockfile, Maven configuration, runner or limit change is required or made.

## Applicable existing evidence - no reruns

The [independent final corrective re-review](reviews/2026-10-25-slice-4-independent-final-corrective-re-review.md)
and its machine evidence independently CLOSED F01-F05/F07/N01, VERIFIED F06 CPU/native attribution,
and found **no non-GPU mandatory blocker**. The new scope is prepared for separate independent
final review, not self-adjudicated finding closure.

| Unchanged P18-FINAL command | Existing applicable receipt |
| --- | --- |
| Ruff check | final-ruff exit 0; independent Ruff also exit 0 |
| Ruff format check | final-format exit 0; independent format exit 0, 67 files |
| Mypy | final-mypy exit 0; independent fresh-cache result exit 0, 22 source files |
| Full pytest | final-pytest-v2 exit 0; 238 actual XML testcase elements / 0 failures/errors/skips |
| Full Java clean verify | genuine exit 0 / BUILD SUCCESS; 1,937.915 seconds under unchanged 2,100s; 142 archived suite XML files, 1,580 testcase elements versus 1,567 suite-attribute tests, 0 failures/errors, 1 existing ADR-0023 diagnostic skip |

The 13 Java-count difference remains the exact nested-name collision, not missing execution.
The independent native 36-case selection, cleanup 7, accepted D17/D20 quality and source-bound
CPU/resource/cancellation receipts retain their original scope and measurement limits. They are
reused, not newly executed or double-counted. The earlier aggregate timeout's cause stays UNKNOWN.

Existing PASS receipts/log hashes and the original full Java archive/Python XML are checked.
All **1,732** entries of the successful Java source manifest still match, including the verified
fixture correction; the retained Python command manifests retain their applicable source identity.
All five final command/code blocks remain byte-for-byte unchanged. No test, static/dependency
machine gate, model inference, GPU experiment or full Java gate was rerun in this checkpoint.

| Preserved artifact | SHA-256 |
| --- | --- |
| Source manifest | `015cc6d027b15c3b0743fcf434a9371edb60fff6e29b4304bafe12a2ef0bf478` |
| Fixture correction | `9291ce7b3b0c4ae753873cbd3108ebf85b802cef554317f393953f9cbb97bb47` |
| model.onnx | `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` |
| tokenizer.json | `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` |
| uv.lock | `5d5ab94a66875c51c35840e37aec76a2efa5e193a1fa1210347691c96c3517cc` |

## Documentation checks and preservation

New raw evidence is isolated under `logs/phase18/s4-cpu-only-d26-20261025-4d33d1b7/`.
The [machine-readable D26 evidence](slice-4-cpu-only-d26-evidence-2026-10-25.json) records exact
documentation-check commands/receipts, clause/file traceability, before/after source/document/index
hashes, receipt applicability, archive counts, model/runtime/lock identity and link/diff checks.
Only the 16 stated existing documents and the three new D26 profile/handback/evidence documents
change relative to checkpoint entry; inherited dirty implementation is preserved.

Entry HEAD **ebe125f7529124b62761e8d01c692cbbd28e4aa7** and raw/semantic Git index remain unchanged.
The entry inventory contains **2,522** existing nonignored files and **83,955** historical evidence
assets; non-amended files/history are verified unchanged. Original GPU, Java failures/timeouts,
independent reviews and D24/D25 bodies remain intact. Documentation link/applicability/command-block
checks and `git diff --check` pass; these are preparation checks, not independent acceptance.

Two initial documentation-check helper attempts exited 1: the first matched only the prefix of
the D26 heading; the second incorrectly treated the 17 inherited tracked deletions as new files.
Their scripts, failed receipts and logs are preserved. The corrected checker verifies the full
heading and independently preserves the exact inherited deletion set, then runs the complete
source/receipt/history/consistency checks. No product assertion, acceptance criterion or test was
changed to obtain a result. These are documentation-check attempts, not additional gate executions.

## Stop and remaining Owner gates

No technical blocker is demonstrated under the approved CPU-only applicability. The next gate
is **independent CPU-only final review** of D26 consistency and the unchanged applicable evidence,
then explicit Owner disposition. S4-18 and final closeout are not self-completed.

Phase 17 FROZEN; Slices 1-3/FR18-3 CLOSED; D24 `.md` only; D22 originals NOT_RECOVERED;
external indexing INACTIVE, live originals DISABLED/DEFERRED. Slice 4/P18-FINAL/Phase 18
**NOT ACCEPTED / NOT CLOSED**. No indexing activation, staging, commit, push, tag, PR or further
implementation/verification cycle. Stop after this documentation handback.
