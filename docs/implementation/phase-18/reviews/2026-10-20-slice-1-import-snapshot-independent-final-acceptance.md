# P18-S1-IMPORT-SNAPSHOT — independent final acceptance, 2026-10-20

**Decision: READY_FOR_OWNER_COMMIT — Slice 1 only, under owner exception D14.**

**S1-01–S1-07 remain independently CLOSED. S1-RR-01 is RESOLVED BY OWNER EXCEPTION D14**, independently checked here. This resolves the Slice 1 verification-gate disposition; it does not turn the failed full clean run into PASS or close its deferred producer/resource issues. No blocking finding remains for the exact reviewed Slice 1 scope under that exception.

## Scope and authority

This is the owner's explicitly requested final acceptance check using `codex-final-review`, following [the independent corrective re-review](2026-10-20-slice-1-import-snapshot-independent-re-review.md). The entry status was `OWNER_EXCEPTION_RECORDED_AWAITING_INDEPENDENT_FINAL_CHECK`, an explicit bounded review/governance scope under [ACTIVE](../../handoffs/ACTIVE.md) and [the handoff](../slice-1-import-snapshot-handoff.md). The corrective implementation was authored by a separate Codex takeover session. This review changes documentation/status only.

[D14 — P18-S1 verification-gate exception](../owner-decisions.md#2026-10-20--d14-p18-s1-verification-gate-exception-s1-rr-01) records the owner's explicit decision for **P18-S1-IMPORT-SNAPSHOT only**, exactly addressing S1-RR-01. It accepts the independently verified seven findings closures and original 210-test evidence despite the failed aggregate clean gate, while retaining each failure and its future obligation. It is neither a global skill-policy change nor permission to change code/tests, weaken security, activate indexing or start another slice. The user's present request explicitly invokes acceptance against that recorded exception.

D12/D13 and ADR-0023 remain controlling. No renewed native alias/Restricted Token/OS adversarial proof is required. Phase 17 remains COMPLETE — FROZEN; accepted v2/domain behavior is preserved. Pending R is not selected/current G, and sourceVersion/selected R/G do not advance during Slice 1. External indexing remains INACTIVE, original-live mode DISABLED / DEFERRED; Slices 2–4 remain inactive/unauthorized. FR18-3, P18-FINAL and Phase 18 remain OPEN / NOT ACCEPTED.

## No implementation change since independent re-review

HEAD remains `edac5f466935fdd29698acbe89d4f129efce1701` on `main`, with the existing reviewed dirty worktree preserved.

- Rehashed every entry in `logs/phase18/s1-takeover-final-source-manifest.json`: **35/35 match, zero mismatches**. These are the same production, migration and test bytes checked during the previous independent re-review and its 210-test run.
- Compared the manifest's file set with **all tracked changes against HEAD plus all nonignored untracked files outside docs**: exactly the same **35 files**, no added/removed non-documentation change. This checks beyond the tracked diff and includes the new command/expectation/acquisition/request/migrations/tests.
- No new application configuration, dependency, worker/Python or feature-flag delta appears. V20/V21 and synchronized API/DBML/package inventories retain the previously reviewed implementation. V1–V19 and unrelated frozen production baselines remain unchanged.
- Rechecked current registration/capture/approval guards, pending setters, pre-commit checks and external-index denial. `advanceVersion`, `setSelectedRevision` and `promotePendingToSelected` still have **no production callers**; they are declarations, not active promotion paths.
- Recursively re-enumerated the production RAG tree: api 17, source 6, application 22, domain 15, client 3, persistence 13, storage 5, windows 2, web advice 2/controller 3/dto 17/mapper 2 files. Every content-bearing package retains its descriptor; layout matches the prior inspected tree. No convention-only refactor or new shared transport module is needed. ADR-0016 root `ApiResponse` and module-local adapters remain intact.
- `git diff --check` passed. This reviewer edited no production/test/migration file, executed no test suite, and performed no commit/push/tag/PR. Source/evidence hashes were checked again after status synchronization.

## Original focused evidence verified, not rerun

The original independent receipt remains `logs/phase18/s1-independent-re-review-2026-10-20-focused.json`: **PASS, exit 0, 210 tests / 0 failures / 0 errors / 0 skips**, 351.235 seconds, UTC `2026-10-20T00:03:48.960`–`00:09:40.197`, no timeout or process cleanup. Its argv digest is `0e298b2d762bb884b1c3a5c3e79dbc56c0c5e44eed1eb652c8b6507e9a8aab8c`. The exact bounded command and environment remain in the unchanged independent re-review; they are not represented as a new invocation here.

Read the **13 exact-class original Surefire XML reports**, their failures/errors/skips and modification times, all within that receipt's run interval. Their totals agree with the original Maven console result and previous independent review:

| Class | Tests | Failures / errors / skips |
| --- | ---: | --- |
| ApplicationArchitectureTests | 37 | 0 / 0 / 0 |
| FlywayStartupIntegrationTest | 2 | 0 / 0 / 0 |
| PortabilityIntegrationTest | 15 | 0 / 0 / 0 |
| RagLifecycleIntegrationTest | 19 | 0 / 0 / 0 |
| RagOriginalBytesStoreTest | 16 | 0 / 0 / 0 |
| RagPgVectorCompatibilityIntegrationTest | 2 | 0 / 0 / 0 |
| RagSlice1SnapshotIntegrationTest | 11 | 0 / 0 / 0 |
| RagSnapshotAcquisitionServiceTest | 15 | 0 / 0 / 0 |
| RagSnapshotCorrectiveIntegrationTest | 42 | 0 / 0 / 0 |
| RagSourceAuthorityIntegrationTest | 9 | 0 / 0 / 0 |
| RagSourceReservationsIntegrationTest | 16 | 0 / 0 / 0 |
| RagWorkerBoundaryIntegrationTest | 25 | 0 / 0 / 0 |
| OpenApiRouteInventoryIntegrationTest | 1 | 0 / 0 / 0 |
| **Total** | **210** | **0 / 0 / 0** |

The substantive deterministic assertions, reachable production paths and proof limits were independently inspected in the prior review and are incorporated by reference. Byte-for-byte source continuity preserves those conclusions; a count alone is not the closure basis.

| Finding | Closure retained and unchanged substantive proof |
| --- | --- |
| S1-01 | CLOSED: exact pending UUID/hash/length and authority expectation, captured consent association, before/after guarded CAS; authenticated A→B→stale-A approval and verification/replacement regressions. |
| S1-02 | CLOSED: first-import-only registration and one guarded existing-S mutation path; simultaneous import, capture/register/approve/revoke, actual lock-wait and reservation-denial regressions. |
| S1-03 | CLOSED: startup/scheduled bounded recovery, protected active/reference inventory, post-outcome fresh DB checks and runtime fencing; PG failure/rollback/unknown outcome, fresh recovery objects, real Windows deletion denial/retry and missing-pair regressions. |
| S1-04 | CLOSED: independent registered pending/selected integrity verification before equality, persistent denial on corruption; missing/partial/tampered and valid equal/changed cases, without complete NO_CHANGE. |
| S1-05 | CLOSED: production finite capture context, timed reads, interruption/revoke cancellation and pre-commit checks; actual mid-copy barriers, mutation, stalled/failed read, cancellation/deadline and junction/locality cases. |
| S1-06 | CLOSED: native no-replace seal and no-clobber fallback; forced-native-unavailable orig-only collision preserves exact bytes, with normal seal/manifest-collision coverage. |
| S1-07 | CLOSED: safe DEBUG/error/diagnostic output and tombstoning; actual DEBUG sensitive-locator/digest assertions and path-bearing failed-read cause tests. |

## Full clean failure and future obligations retained

The original `logs/phase18/s1-takeover-clean-verify.json` still says **COMMAND_FAILED, exit 1**, 847.672 seconds. The preserved console log/summary still record **1,433 tests / 1 failure / 2 errors / 0 skips**; package/verify stages were not reached. The different XML-container aggregate is not substituted for that console total. **No full suite was rerun for this final check and the original gate remains FAILED.**

| Result | Disposition required by D14, verified in current docs |
| --- | --- |
| Historical native held-lifetime alias failure | Excluded from Slice 1 acceptance under ADR-0023; failed evidence preserved, not relabeled PASS or repaired by this review. |
| Native document batch invalid worker provenance | **OPEN** for future separately authorized Slice 3 / FR18-3 verification. No validator weakening, finding closure or producer/publication acceptance follows. |
| Aggregate PostgreSQL connection-slot exhaustion | **OPEN / unresolved verification limitation**. Focused worker-boundary 25/25 is not aggregate stability/root-cause evidence. Bounded aggregate resource investigation and repeat verification remain mandatory **before P18-FINAL**. |

The obligations are synchronized in [D14](../owner-decisions.md), [P18-FINAL](../final-verification-gate.md), [test evidence](../test-evidence.md), [README](../README.md), ACTIVE and the slice handoff. No baseline-only comparison is claimed. D14 is the explicit owner-approved exception that resolves S1-RR-01 for this slice; the underlying failed results remain unresolved where indicated.

### Evidence fingerprints checked

| Preserved artifact | SHA-256 |
| --- | --- |
| Independent focused receipt | `9a4247b7be251bb20cf8854f6bb40a35e35754adc1008939a3c8a15e68186600` |
| Final 35-file source manifest | `b5f64d4e7b26022665d0b6cf9432ba80e2da3ebf57d87c2b785e30a6c08dd607` |
| Failed full clean receipt | `763c3b3327612fc31b2d4e2ec2b2b225a86870a7250afe2780efef7ea8bc0d11` |
| Failed full clean log | `8aebbdf10e1e4561e7ea59a4dc128b8d957a87c2b3ece7a16ed885dc80f5bc50` |
| 2026-10-19 original independent review | `62c5800c428c234dcbd6fe4324e692382718a3fab64539cc557be7ebf4b78ccb` |
| 2026-10-20 independent corrective re-review | `8bd005dd9b6d2e217a0c9aea8bdbd545230586a83f5d9cd3093a48d4191b2f84` |
| Codex corrective handback | `535177f45d90cc5f02f7866869b8fb67ca2b329fa359ce0d000a08cf105b80a4` |

Paths for logs are under `logs/phase18/`; historical reviews and handback are linked above. No receipt, historical review or handback was rewritten by this acceptance.

## 29-dimension final coverage ledger

The previous independent source/test ledger remains applicable because the complete reviewed file set and bytes are unchanged. This final check independently verifies continuity and the new exception; it does not invent new runtime evidence.

| # | Dimension | Status | Final basis / limit |
| ---: | --- | --- | --- |
| 1 | Owner intent / scope | PASS | Exact one-time D14 exception, Slice 1 only; no activation or later slice. |
| 2 | Traceability | PASS | Seven preserved code/test closures; S1-RR-01 maps precisely to D14. |
| 3 | Architecture / consistency | PASS | Unchanged reviewed code/schema/API inventory; D12/D13/ADR-0023 preserved. |
| 4 | Module boundaries | PASS | Same public dependency edges and original 37 architecture tests. |
| 5 | Recursive package tree | PASS | Actual tree re-enumerated, same file counts/descriptors and semantic grouping. |
| 6 | Shared transport / exceptions | PASS | Existing root response family and module-local DTO/mapper/advice; ADR-0016. |
| 7 | SOLID / cohesion | PASS | Unchanged centralized guards and acquisition/store/recovery responsibilities. |
| 8 | DRY / invariants | PASS | Same exact owner-intent/CAS and pending-only lifecycle verified previously. |
| 9 | Patterns / overengineering | PASS | No new abstraction, queue, engine, module or scheduler since reviewed implementation. |
| 10 | Business transitions | PASS | Closures and original deterministic tests preserved; no promotion caller introduced. |
| 11 | API compatibility | PASS | Original authenticated preview/regression and 228-operation inventory evidence retained. |
| 12 | Validation / bounds | PASS | Same practical locality/reparse/size/deadline checks and regression bytes. |
| 13 | Consent / authorization | PASS | Same guarded source/pending association; no exception to privacy/consent. |
| 14 | Secrets / retention / egress | PASS | Same redaction/recovery closures; no download/worker/config change. Backup limits remain. |
| 15 | Transactions / concurrency | PASS | Same final guarded CAS, runtime fence and barrier/rollback proof. |
| 16 | Recovery / cancellation | PASS | Same production hooks, bounded inventory and deterministic regressions; controlled-crash limits retained. |
| 17 | Database / permissions | PASS | V20/V21 unchanged; original PG startup/upgrade/mapping/worker-denial proof retained. |
| 18 | Complexity / resources | PASS | Same finite copy and bounded recovery; aggregate-suite resource issue separately OPEN under D14. |
| 19 | Retrieval/index quality | N/A | External indexing inactive; FR18-3/producer and future generation acceptance remain open. |
| 20 | OCR/document fidelity | N/A | No parser/OCR/model changes or acceptance in this slice. |
| 21 | Security/concurrency tests | PASS | Original substantive negative/barrier evidence retained; superseded OS proof excluded. |
| 22 | Test quality / evidence | PASS | Original exact 13 reports/210 tests checked, substantive prior review and 35-file continuity; no rerun claimed. |
| 23 | Performance benchmarks | N/A | Later measured resource/quality work not accepted; D14 carries aggregate investigation. |
| 24 | Held-out evaluation | N/A | No new evaluation/quality gate enabled or waived. |
| 25 | Dependencies / lifecycle | PASS | No dependency/runtime/lockfile delta; no new vulnerability audit claim. |
| 26 | Build / verification | PASS | Slice 1 acceptance permitted by explicit D14; focused evidence unchanged. Full clean gate is still FAILED, aggregate stability NOT VERIFIED. |
| 27 | Logs / diagnostics | PASS | Original privacy proof retained; previously observed fixture/runtime warnings not relabeled warning-free. |
| 28 | Docs / governance | PASS | D14 independently verified, S1-RR-01 resolved by exception, status synchronized; original history preserved. |
| 29 | Hygiene / delivery | PASS | File-set/hash continuity, diff check, no code/test changes or publishing by this review. |

## Commit and next step

Exact owner commit message:

```text
feat(rag): add guarded pending snapshot imports and recovery
```

**Next step:** owner may commit/push the reviewed Slice 1 scope using that message, then provide the updated package for Slice 1 closeout/status synchronization only. The deferred producer and aggregate resource obligations remain OPEN; any Slices 2–4 work requires separate owner authorization. Agents do not commit/push; this review neither accepts Phase 18 nor authorizes Slice 2.
