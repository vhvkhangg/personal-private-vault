# Phase 17 Alternative B — binding verification and expectation map

**Contract approved 2026-10-15; all new runtime guarantees NOT_VERIFIED.**
This maps [independent design review §6–8](reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md)
to [P17-1](integration-ownership-and-permissions.md), [P17-2](source-eligibility-and-allowlists.md)
and [P17-3](revocation-and-derived-data.md). The subsequent explicit owner
implementation authorization covers Groups A–D and isolated test-scoped execution;
default containment remains. Historical reviews/receipts and frozen auth assertions
remain immutable. FR17-5/8/9/10 OPEN; FR17-11 and other closed fixes preserved.

## Deterministic runtime obligations

Real PG 18.6 roles/transactions and Windows process handles; controlled barriers
with bounded overall timeouts, both orders and failure-safe process cleanup.
No arbitrary sleeps, mock/native success, skipped assertions or retries until green.

| ID | Required actual boundary/interleavings and assertion |
| --- | --- |
| B01 | Every owner/RAG exclusion path COMMIT before admission/body versus reservation-first: zero unauthorized transfer/consumption/receipt, or 409 with no mutation C |
| B02 | Java/DB loss after reservation/grant C with live paused worker/body: reservation survives, no lease/disconnect release |
| B03 | Selected versus unrelated HTTP; bulk/import/cascade/OLD-NEW assignment IDs/shared projected inputs: all affected guards and full command rollback |
| B04 | Allowed->NSFW->Allowed lost both events, equal hashes, child/Private variants; delayed reset/source-version/epoch/policy approval CAS; missing/overflow/type/ID reuse; renewal does not resurrect old job/index |
| B05 | Ordinary title edit preserves consent; exact unchanged description no prompt; changed/stale/missing confirmation denies; exact byte round trip can reuse confirmation but old index remains OUTDATED; reset+confirmation cannot renew |
| B06 | Concurrent manual starts enforce complete static nonterminal index; no active-slot eviction; one actual input claim; duplicate/cross-job/outage/stale listener/port collision/wrong-process binds deny without body |
| B07 | Worker dead but S backend pending, every allowed/legacy route and actual role, both attempt-seal orders: prior C drained or late writer reloads closed state and rejects |
| B08 | Suspended spawn/bind crash, inherited handles, breakaway/descendant denial, late launch/send tasks, cancel before worker existed, failed kill/unknown identity: no false quiescence/release |
| B09 | Actual P-or-abort/seal/terminal/release C, low-disk/unknown ACK/crash/rollback: durable states agree, no release-only TX or callback replay |
| B10 | Same JVM duplicate control/accidental second JVM/live paused controller past TTL: no launch/takeover/release until genuine ownership termination |
| B11 | Normal restart preserves fresh consent/completed index; trusted full OS restart distinct from sleep/hibernate; unfinished S aborted not finalized/resumed; DB outage/declared restore fail closed |
| B12 | Expiry before input/S/P, lock wait/default deferred checks deny; separately observe E1 after-last-check late S/trusted P; valid late P stays usable past work TTL/session/boot under fresh IndexCurrent |
| B13 | Source/child/field/version/topology change, lost events, hash h->h2->h and dependency remove/readd permanently OUTDATED; reapproval cannot revive old generation; unrelated index survives |
| B14 | Wrong/missing/extra/duplicate stage/kind/input/output/scope/actual producer; cross-job, PUBLIC/inherited/definer/search_path/legacy denials; no worker job/slot/reservation/policy/pointer rights |
| B15 | Cached ORM/outer ambient dirty/early flush/savepoint/lock timeout; direct maintained SQL trigger backstop, NOWAIT earlier guard and atomic rollback; reservation/version not overwritten by stale entity |
| B16 | Restrictive quoted/malformed metadata, unapproved new transitive child, field/scope mismatch, external editor: original exclusions and disabled live-file proof preserved |
| B17 | Failed/cancelled new attempt preserves independently current previous index; cancel loses to P reports completed; old B manifest with A purged without cancelling new B excluding A; erasure failure never false PURGE_COMPLETE |
| B18 | Below/at/above 100 nodes/depth5/1MiB; sequential cold useful successes and one competing-start conflict; tombstone lifetime growth/headroom/low disk/leak/resource measurements, no active-slot eviction |
| B19 | Practical local offline verification: Java -> local Python -> PostgreSQL execution, zero external/cloud AI endpoints, no required Internet connectivity, pinned local dependencies (`uv.lock`), bound ingress/socket containment, strict NSFW/Private exclusion, and valid cancellation/recovery. (Historical: strict OS-level WFP/firewall outbound denial was superseded by 2026-10-16 owner decision). |
| B20 | Focused then clean full Java/Python, Ruff check/format, mypy, architecture/migration/effective-role/repository checks, exact counts/exits/failures; package descriptors/FR17-11 retained; bounded process cleanup. (Owner Decision 2026-10-17: narrow acceptance exception permits tracking `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` as an OPEN Authentication/environment issue outside Phase 17 RAG remediation when caused by Windows/WSL2 cross-clock skew with unchanged frozen code/assertions, accurate reporting, and all other Phase 17 tests passing; failed clean verify is never called PASS). |

## Exact maintained expectation changes

- Keep RagWorkerBoundaryIntegrationTest.leaseExpiryDuringActualGenerationLockWaitDeniesPublication
  and default-deferred rejection in workerExpiryAfterLastLeaseCheckRejectsPhysicalCommit.
  E1's after-final-trigger pause is a distinct permitted case, not a weakened gate.
- expiryAfterFinalizerReturnsBeforeOuterPhysicalCommitRollsBackActivation keeps
  supported outer/deferred protection, or asserts ambient dispatch rejection before
  external effects if that entry is unsupported. No mocked success substitution.
- Split actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants:
  ordinary restart preserves fresh completed consent/index; old execution tokens
  and declared uncertain restore still deny. Remove blanket startup purge coupling
  from the target, not from historical receipts.
- Adapt lostCompletionAckReturnsSameGenerationAndIndependentSessionExpiryDoesNotMergeConsent
  to one active job; keep exact outcome/receipt identity and independent selection
  scope. Completed persistent index access after its own work TTL is a new positive.
- RagLifecycleIntegrationTest.cancellingJobRevokesActiveIndexGeneration currently
  cancels a completed job. Target cancel reports already finished/conflict;
  participation revoke is distinct. Cancel new job cannot delete prior CURRENT G.
- RagRecoveryService/RagSourceAuthority need explicit completed-index/no-work-TTL/
  normal-restart positives. Eligible content edits preserve item consent while
  old version/grant/index denies; description confirmation remains independent.
- Retain every unchanged wrong provenance/role/attempt COMMIT-purge both orders/
  unavailable worker/delete-failure/restrictive metadata/FR17-11 negative assertion.
  Frozen authentication timing failures remain diagnosed verification blockers,
  not permission to change auth/assertions or retry until green.

Traceability: FR17-1/2 -> B16; FR17-3 -> B13/14/16; FR17-4 -> B14;
FR17-5 -> B01/02; FR17-6 -> B06–11/17; FR17-7 -> B03/09/12/15;
FR17-8 -> B20; FR17-9 -> B18; FR17-10 -> B19; FR17-11 -> B20.
No finding is closed by mapping or passing tests alone. Required commands and
resource/offline measurements are in [benchmark acceptance](benchmarks-and-acceptance.md).
