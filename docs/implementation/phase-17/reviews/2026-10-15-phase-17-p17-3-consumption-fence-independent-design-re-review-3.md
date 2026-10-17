# Phase 17 P17-3 consumption fence — independent design re-review of Revision 3

**Overall verdict: APPROVE DESIGN FOR OWNER DECISION.** The revised D5 ownership/quiescence protocol and **A+B, not C** V policy are acceptable at the specification level. Revision 3 now states the expiry counterexample and all three E1 consequences adequately for a concrete owner decision. No further cosmetic expiry revision is requested.

**Strict E2 remains impossible for a nontrivial enabled implementation under the stated arbitrary-pause model.** Keeping containment satisfies the safety restriction by refusing the affected work, but does not complete the accepted foundation deliverable. This verdict approves the decision material, not a working strict-E2 solution, any E1 waiver, runtime correctness or implementation.

The next gate is an **owner architectural/requirements decision on O5**, together with the scoped O1–O8 approvals. Existing `P17-RAG-FOUNDATION` remains CHANGES_REQUESTED; containment remains. FR17-5/8/9/10 are not closed, no other implementation finding is silently waived, and Phase 17 is not complete.

## 1. Scope, authority and evidence

Review date: 2026-10-15. Baseline: `main`, HEAD `24e6268eef1685866d919d882ecdf9f532d01961`, including the existing uncommitted implementation and containment. The owner authorized a new independent **design** review/report only. No production/test/migration/accepted-contract/ADR/ACTIVE edit, publishing operation, new handoff or later-phase work is authorized. No sub-agents were used.

Read all **1,197 lines** of the [Revision 3 proposal](../p17-3-consumption-fence-proposal.md), the [remediation plan](../remediation-plan.md) and the [independent Revision 2 review](2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-2.md). Compared [accepted P17-3](../revocation-and-derived-data.md), [P17-1 ownership/privileges](../integration-ownership-and-permissions.md), [owner decisions](../owner-decisions.md), [ACTIVE](../../handoffs/ACTIVE.md), [ADR-0019](../../../adr/0019-local-rag-before-frontend.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module boundaries](../../../architecture/module-boundaries.md) and the [dependency matrix](../../../architecture/module-dependency-matrix.md). Relevant source/privacy/lifecycle contracts and source inspections from the prior independent reviews were reused after verifying those files are unchanged.

Applied the repository/documentation AGENTS instructions and the applicable shared review coverage, local RAG and module/architecture review guidance. A new SHA-256 baseline covers **1,938 existing nonignored files**. Relative to the Revision 2 review's pre-report baseline, only the proposal and plan changed; the Revision 2 report was added and is preserved as history. Accepted contracts, owner decisions, ADRs, ACTIVE, production, tests and migrations are unchanged.

This review independently checked the protocol's interleavings and requirement implications and consulted primary PostgreSQL/Win32 documentation. **No new application test, PostgreSQL experiment, native process/fence test, benchmark, firewall operation or abstract model was executed.** The earlier reviewer models remain attributed abstract evidence, not runtime proof. The [implementation review](2026-10-15-phase-17-independent-final-codex-review.md) and [test evidence](../test-evidence.md) retain their original failures/limits; no passing implementation result is invented here.

Four gates remain distinct:

| Gate | This review's result |
| --- | --- |
| Design correctness/completeness | D5/V and the explicit alternative contracts are decision-ready under their stated assumptions. Strict E2 cannot be implemented as an enabled success path in this model. |
| Owner acceptance of changed requirements | **Pending.** E1(a+b+c), candidate-only scope and all new owner/API/schema/transport responsibilities require explicit decisions. Silence is not approval. |
| Runtime verification | **NOT_VERIFIED** for the new protocol; mandatory native, PostgreSQL, Java/Python, role, resource and offline proofs remain. |
| Implementation authorization | **Not granted.** This report changes no handoff/contract or containment behavior. |

## 2. D1–D6 and CF-1–4 dispositions

| Decision | Disposition | Independent explanation |
| --- | --- | --- |
| D1 | **ACCEPTABLE AS PROPOSED** | A consumer-owned shared F survives Java/DB failure and excludes participating source mutation COMMIT until consumption ends. Stable native identity and complete writer participation remain essential, unverified assumptions. |
| D2 | **ACCEPTABLE AS PROPOSED** | Q capture under guards, digest work outside SQL, fresh J under continuous F and no content network/model work under DB guards preserve the accepted bounded transaction principle. No live-file/parser/model enablement follows. |
| D3 | **ACCEPTABLE AS PROPOSED** | H/E retain per-source consent history; V orders pending/queued SQL; immutable tickets and fresh comparisons remain. A+B correctly separates execution invalidation from published-read authority. |
| D4 | **ACCEPTABLE AS PROPOSED** | Protected F-before-source-transaction/V-before-work entry, physical same-mode joins, ambient/suspend/savepoint rejection and one-use G/B/S state remain coherent. D5 explicitly adds a separate negative control prelude, not an independent business commit. |
| D5 | **ACCEPTABLE AS PROPOSED** | Vault owns the mechanical coordinator/registry/native/control interface; RAG retains jobs and purge. Positive process/no-spawn proof plus exclusive F and a **known committed V cutoff** establish only scoped quiescence. Unknown outcomes and duplicate operations are addressed without reverse feature calls. Native/SQL implementation proof remains outstanding. |
| D6 | **ACCEPTABLE AS PROPOSED — decision contract only** | The impossibility argument and E1(a/b/c)/E2/candidate-only choices are explicit. This does not accept an enabled strict-E2 implementation or select any waiver. O5 is a requirements blocker, not a request for another timer/trigger redesign. |

| Finding | Disposition | Exact limit |
| --- | --- | --- |
| P17-CF-1 | **RESOLVED IN DESIGN, preserved** | Owner mutation/H/V atomicity, retained H and explicit H/E-bound renewal prevent allowed→NSFW→allowed old-consent revival even with lost notifications. Actual participation/atomicity remains NOT_VERIFIED. |
| P17-CF-2 | **RESOLVED IN DESIGN, preserved** | Physical entry and lock-order rules exclude dirty/unregistered ambient work before even the new prelude. Actual Spring/JPA callers/proxies remain NOT_VERIFIED. |
| P17-CF-3 | **RESOLVED IN DESIGN, preserved** | Explicit SHARE/UPDATE modes, post-wait compare, immutable scope, G/B CAS and effective-role/legacy route restrictions are adequate specifications; actual SQL remains NOT_VERIFIED. |
| P17-CF-4 | **SPECIFICATION RESPONSE COMPLETE; STRICT E2 REQUIREMENT UNSATISFIED** | The previously omitted late Java transition is now explicit. The accepted strict requirement remains a blocker unless the owner changes requirements or keeps the capability contained. This is not implementation closure. |

Prior R2-1 is now fully disclosed and elevated to the owner decision; it is not technically repaired. R2-2's ownership gap is resolved in design by §9. R2-3's V/read ambiguity is resolved by the separate ExecutionOK/ReadOK predicates. Historical findings/reports are unchanged.

## 3. D5 interface, ownership and concurrency verdict

**D5 verdict: acceptable as an explicit additive Vault capability, requiring owner approval.** Vault tracks source-use mechanics, identities, process handles, drain outcomes and F/H/V; it does not interpret RAG grants/jobs or query RAG storage. Fiction/Film/Vault mutation entry uses the public Vault facade. RAG maps its own opaque consumer references to attempts and independently owns policy, receipts, publication and purge. The Python parent hosts a Vault-specified metadata supervisor without making Java Vault depend on RAG internals or an injected RAG callback.

This is a substantial new transport/storage/native responsibility for frozen Vault, beyond the original row-guard approval. Its explicit public interface and ownership make it reviewable; hosting the supervisor in rag-engine does not itself violate Spring Modulith ownership. Concrete implementation must preserve that distinction in imports, package descriptors, SQL ownership and control DTOs. No generic shared module or second RAG job scheduler is justified.

The critical design invariant is:

`ScopedQuiescent = closed admission + terminal positive proof for every covered reservation/launch + live exclusive F + known committed V cutoff`.

No single flag, ACK, PID, empty active count or acquired-but-uncommitted version proves it. Quiescence is scoped to covered references/boot/cutoff; it is neither source exclusion nor PURGE_COMPLETE. N and RAG erase completion require their own physical commits/evidence.

| Adversarial interleaving | Independent result under Revision 3 |
| --- | --- |
| DRAIN races reservation | L serializes OPEN check/reservation versus gate closure and covered sequence. Reservation-first is included; closure-first denies reservation. No untracked second launch is authorized. |
| RESERVED/SPAWNING launch delayed across closure | Covered set includes reserved and in-progress launches. Missing PID is not no-spawn. Terminal no-spawn CAS **and supervisor launch exclusion**, or actual bound-process death, are required before quiescence. Unknown spawn remains unavailable. |
| Parent dies between spawn and binding | Child cannot acquire F/READY/body before known identity-binding COMMIT. Unknown process identity blocks recovery/reopening; a replacement parent cannot discard it. A live unbound process is still not certified dead. |
| Two controllers/duplicate drain IDs | Unique operation/boot identity and L serialization prevent duplicate authority. One COMPLETE cannot reopen another pending operation; stale/reordered ACKs cannot manufacture global completion. |
| Child reports DEAD but is alive | Registry mirror is insufficient. Java must verify the actual process object/creation identity, signalled termination and controlled process tree. Native proof remains required. |
| Child dies, Java sender still owns shared F | Positive child death alone does not let N pass. Exclusive F still waits/fails, covering the sender's remaining guard. Arbitrary Java suspension can cause safe unavailability too. |
| Child dies while S holds V | F may release, but cutoff's V UPDATE waits for S's shared V transaction to resolve. No QUIESCENT while the backend is pending. |
| Old SQL is queued before cutoff but has not acquired V | After cutoff COMMIT, its immutable expected V mismatches and it rejects after its locking read. Obtaining exclusive V without committing the increment would not provide this durable fence. |
| Cutoff COMMIT unknown | No business callback/quiescence acknowledgement. Closed admission persists. Recover under the correct F/V barrier and inspect immutable cutoff evidence; no inference from timeout or a later larger V. |
| Business N COMMIT unknown | Recovery obtains exclusive F/V after the old holder resolves, then checks the immutable operation outcome marker. Presence proves commit; absence only after that barrier proves rollback for the specified non-prepared transaction. No automatic callback replay. |
| Cutoff commits, later N rolls back | H/E/business truth roll back; the **earlier execution cancellation cutoff persists**. Old tickets stay dead; unrelated published results survive if ReadOK. This side effect requires O3/O4 consent. |
| Business commits, completion/control ACK is lost | Report known business COMMITTED and control PENDING separately. Do not return a fabricated business rollback or automatically repeat the mutation. Admission stays closed pending reconciliation. |
| Restart/split parent/replayed COMPLETE | Authoritative control boot on V, closed startup, all old reservation/process/SQL/outcome resolution and F/V recovery prevent old boot authority. approved_control_boot makes old consent deny even before RAG runtime reconciliation. |
| L/registry quota, disk, F timeout or failed termination | Bounded busy/unknown outcome; never force unlock, drop unknown records or claim exclusion that did not commit. Safety can mean indefinite unavailability. |

The L lane ends before network/process/F waits and never acquires V/source/RAG locks. Source/cutoff lanes acquire F then V; outcome rows are reserved before later graph/source/attempt locks. L completion occurs after source SQL ends. This excludes an L→F/V wait cycle. Registered outer mutation joins the established unit without a second drain; shared-to-exclusive upgrade/suspension/savepoints reject. No SQL transaction may wait for a child/control operation needing its held rows.

Runtime proof must exercise **same-coordinator retained-F recovery**, not only process death: the manager must reuse or correctly serialize access to its own exclusive capability instead of waiting to reacquire a conflicting handle it still holds. Registry-only unknown outcomes must be reconciled in their own serialized lane; the V barrier proves V-participating cutoff/business outcomes, not arbitrary L state. These are concrete proof obligations consistent with the stated manager/lane rules, not a new specification blocker.

Known cutoff/outcome commit evidence must use the required durability profile. Worker/control ACKs are not durable-state oracles. Verify actual fsync/synchronous_commit settings, permitted overrides and recovery behavior in the selected runtime; a role name alone does not enforce GUC choices. The authoritative Java cutoff flush and immutable marker, rather than an earlier worker ACK, are the quiescence proof. PostgreSQL distinguishes logical success, WAL durability and client observation. [PostgreSQL WAL settings](https://www.postgresql.org/docs/18/runtime-config-wal.html), [asynchronous commit](https://www.postgresql.org/docs/18/wal-async-commit.html).

## 4. V policy verdict: A+B, not C

**Acceptable as proposed.** ExecutionOK uses immutable ticket.expected_V equality after locking current V. ReadOK for an already committed publication takes a fresh V barrier but does not compare historical V. It instead requires current per-source H/E/approval/control boot/runtime, exact fields/revisions/scoped dependency closure, active non-tombstoned generation, relevant noncancelled selection/session, authenticated scope and original expiry bounds.

| Event | Required consequence / independent assessment |
| --- | --- |
| Unrelated B changes V before A publication | Every old A ticket/candidate/VERIFIED-but-unpublished state is permanently unusable for advancement. No rebind, promotion or new lifetime; a new attempt needs fresh J. |
| Unrelated B changes V after A publication | A may remain readable with fresh ReadOK. Its original publication/provenance is retained; no new execution ticket or consent is created. No rebuild solely for V. |
| DRAIN cutoff, later owner rollback | Same pre-P invalidation, but no published invalidation solely from cutoff. H/E remain the source truth and the earlier cutoff does not impersonate N. |
| A or any used child changes H, even reversible content/NSFW | SourceOK fails immediately after the authoritative mutation COMMIT, regardless of lost events. Returning to old values cannot restore approved_H; explicit renewal changes E, so old published bindings still cannot revive. |
| Relevant E/fields/classification or scoped edges change | Full current binding/closure mismatch denies. Ignoring historical V does not ignore these changes. An unrelated edge outside A's closure must not be mistaken for A's graph authority. |
| Targeted cancel/revoke/tombstone | Durable targeted generation/selection/session state denies independently of V. V mismatch cannot be the sole evidence of a relevant cancellation once published reads stop testing it. |
| Original attempt/session/ticket deadline expires | ReadOK denies at the next fresh gate. This clarification does not convert a work lease into permanent result retention or extend D_G. E1's explicit check-to-action residual remains a separate choice. |
| Control boot/runtime rotation or uncertain restore | Current SourceOK boot/instance mismatch denies old approval/publication. Preserving unrelated published data across an ordinary V increment does not authorize recovery replay. |

The last relevant cancellation predicate and full closure must be present on **every** supported read/status/count/citation/internal use. A cached active flag, root-only H check, current hash alone or fallback to an old result would violate the design. This is a runtime test obligation, not evidence that those paths exist or have passed. N-versus-read uses fresh locks through the read authorization commit; the accepted nonretroactive delivery rule is not replaced with continuous-time revocation.

## 5. CF-4/D6: feasibility, waivers and functional alternatives

### Strict E2 cannot support enabled successful completion in this model

Let D be the original PG-authoritative min(lease, independent session, ticket expiry), equality expired. A normal trusted verifier/finalizer passes its **last** SQL/deferred check at t<D. Pause that backend after the check but before its commit record/durable completion, let independent PG time cross D, then resume. VERIFIED/P/active state commits after D with correct locks and provenance.

This is a legal model execution without malicious SQL, clock skew or ambient caller misuse. Another finite check sequence merely moves the point after which the adversary pauses. Private adapters, deferred triggers, watchdogs, statement/transaction timeouts, synchronous WAL and prepared transactions do not establish an upper bound under arbitrary pauses. A commit timestamp or later read observes an outcome; it cannot make that outcome earlier. A late ACK alone is not this counterexample: the test must distinguish a delayed observation of an earlier commit from an actually late transaction.

Therefore **strict E2 is not achievable together with nontrivial enabled publication under the proposed primitives and unbounded timing assumptions**. Containment/rejecting the work preserves safety without providing the required functionality. A hard real-time/atomic deadline-commit primitive or a commit-ordered rather than wall-clock authority model would change the system assumptions/requirements; neither is supplied or authorized here. This is a logical impossibility within this model, not a missing Windows/PostgreSQL benchmark that ordinary green tests could discharge.

P17-CF-4's ambiguity is now addressed; its strict accepted requirement remains unsatisfied. Do not request “one more expiry fix” or describe E2 as almost implemented. Retain the previously closed default lock-wait/deferred-check regressions and their historical verdicts; any new expiry contract must explicitly reassess affected FR17-3/7 expectations before tests/code change.

### E1 has three separate security changes

| Waiver | Consequence and limit |
| --- | --- |
| **E1(a)** transfer/read/delivery residual | A valid actual B and fixed deadline checks precede each bounded unit, but suspension after a passed check can cause a later read/send/delivery after D. At-most-64-KiB unit size does not bound elapsed residual under arbitrary suspension. Previously sent bytes may arrive late. A gate that observes expiry must deny; no fresh TTL or new expired operation. |
| **E1(b)** expired untrusted staging quarantine | Exact raw-free candidate/digest/provenance can commit after D, including early SET CONSTRAINTS timing. Sensitive metadata remains inaccessible pending verified cleanup; hashes are not anonymization. Cleanup has no wall-time erasure promise during outages. A subsequent Java gate beginning after that late S sees expiry and denies. |
| **E1(c)** late trusted Java state | A timely last validation can be followed by a late **trusted VERIFIED or actual P/active-generation COMMIT**. This violates the previous strict durable-transition invariant. It is not quarantine and cannot be retroactively authorized by later denial or purge. |

Fresh post-S Java verification addresses a late S, not its own last-check-to-COMMIT window. A fresh post-P ReadOK check can prevent disclosure at that gate, but cannot prove that the P transition never happened. Under E1, every owner response/status/count/history/citation and internal finalizer/scheduler/listener/cache/recovery path must have independent fresh authorization; no bit-only scheduling, notification or reuse. Delivery after a passed read gate still has waiver (a)'s residual. Scope/NSFW/Private/consent/revoke-COMMIT ordering remains strict through F/H/V/E; E1 does not authorize relaxing it.

**E1(a+b) alone cannot enable the existing durable verification/publication workflow.** Accepting (c) is a genuine architectural/security decision even for this raw-free digest-only foundation. The proposal now says so clearly. SET CONSTRAINTS can fire deferred checks early; it is not revocable merely by narrowing function EXECUTE grants. [PostgreSQL SET CONSTRAINTS](https://www.postgresql.org/docs/18/sql-set-constraints.html).

### Candidate-only alternative

Explicitly defer durable VERIFIED/P/active pointers and permit only candidate S plus read-only, freshly gated diagnostic observations. This removes the particular late trusted verification/publication transition because that transition is absent. It still needs waivers (a+b), has sensitive expiry/purge and check-to-diagnostic-delivery residuals, and does not complete the accepted end-to-end publication deliverable. It requires an owner-approved functional re-scope and separate acceptance criteria; it cannot inherit J-through-P success thresholds or be called the original foundation completed. No automatic assignment of the missing capability to a later phase is authorized.

**Recommended owner decision now:** record E2/containment with an explicit acknowledgement that this enabled protocol cannot meet the unchanged strict requirement. If useful enabled work is desired, choose either the fully disclosed E1(a+b+c) contract or the smaller candidate-only scope with (a+b), rather than commissioning another cosmetic expiry revision. The report supplies the technical consequences; only the owner can accept the changed requirements.

## 6. O1–O8 assessment and exact decisions needed

Every row below remains pending owner action. The design is sufficient to make these choices; none is presumed from review approval.

| Decision | Technical assessment | Exact owner decision required |
| --- | --- | --- |
| O1 | Justified additive authority metadata. H/tombstones retain information V/current hashes cannot; control/outcome records address unknown commits. | Approve the scoped Vault-owned H/V/control/outcome metadata principles and eventual reviewed additive schema; no resets, identity reuse, invented seeding consent or rewriting V1–V13. Concrete migration/API authorization remains separate. |
| O2 | Justified conservative source renewal. Reversal/no-op successful relevant commands invalidate H; indirect/shared inputs must participate or be disabled. | Approve explicit current H/E/runtime/control-boot-bound renewal only. No notification, retry, reconciliation or recovery may rebind old consent; old ticket/publication bindings cannot inherit new approval. |
| O3 | Protected entry and atomic owner work are sound, with a new explicit negative side effect. | Approve the audited participant/caller scope, F-before-source-TX/V-before-work, compatible physical joins and early rejection of unsupported ambient/suspend/savepoint/upgrade. **Explicitly accept that a committed D5 execution cutoff survives a later failed/rolled-back owner command**, without business/H/E success or callback replay. |
| O4 | D5 ownership and A+B V policy are acceptable; platform/availability cost is substantial. | Approve Vault's metadata supervisor/native/loopback/control registry boundary, RAG's separate job/purge ownership, one-host Windows profile, safe indefinite unavailability and no forced unlock/fairness promise. Select A+B: old tickets/unpublished work invalid forever; unrelated published reads require fresh ReadOK without historical V equality. |
| O5 | Decision-ready; strict E2 is not an enabled feasible implementation. This is the blocking requirements choice. | Choose one of the explicit wordings/options below. Partial approval of (a+b) cannot select (c). No general “approve the proposal” wording should conceal these waivers. |
| O6 | Useful prospective gates; no measured results. | Approve/revise the proposed 100% normal-fixture and ≥12/16 fault-free-wave useful-success floors, 2s operation/10s D5 request budgets, memory/process/connection/storage/headroom evaluation and lifetime metadata limits as **measurement gates**. E2 cannot pass enabled J-through-P tests; candidate-only needs different scoped criteria. |
| O7 | Supported trust/privacy/restore/clock assumptions are explicit, not universal guarantees. | Accept controlled trusted writers/native storage, deny uncertain identity/restore/clock history, new consent after recovery, no raw application logs/cache/scratch/dumps, and the stated hostile-admin/forensic zeroization exclusions. Actual paging/dump/clock and egress controls remain operating proof obligations. |
| O8 | Appropriate sequencing without reopening or replacing the handoff. | Record selected requirements and scoped authority first. Only then authorize concrete ADR/contract/canonical inventory/existing-handoff synchronization and, separately, any feasible implementation. No containment removal/finding closure/phase completion follows from design approval. |

Recommended O5 wording preserving current authority:

> I retain strict E2 and approve no expiry exception. The affected content execution remains contained. I acknowledge that the proposed enabled protocol cannot guarantee no post-deadline trusted verification/publication COMMIT under arbitrary pauses; this foundation deliverable remains unresolved. No further implementation is authorized to claim that requirement satisfied.

If the owner instead prioritizes the full bounded workflow, the decision must explicitly say:

> For the digest-only Phase 17 profile I replace strict expiry with E1(a), E1(b) and E1(c). I accept post-deadline read/transfer/delivery residual after a valid bounded gate, sensitive expired candidate retention pending purge, and late trusted Java VERIFIED/actual P COMMIT following earlier valid checks. Later denial/purge does not undo those transitions. Every read/internal consumer remains freshly gated. I relax no NSFW/Private/deletion/revocation-COMMIT, consent-history, exact-provenance or one-use guarantee. Affected accepted deadline/provenance contracts require explicit reassessment. This decision alone authorizes no code or containment removal.

A candidate-only decision must expressly authorize the reduced S/diagnostic deliverable, accept (a+b), reject/defer durable VERIFIED/P, and acknowledge incomplete original publication scope. No owner response, elapsed time or partial principle approval selects any option automatically.

## 7. Audit of all 28 adversarial acceptance cases

All entries are **runtime NOT_VERIFIED**. The matrix is consistent with the revised design/decision split; E2 cannot be made green as an enabled path by asserting away C25. C28 is conditional on E1 approval. Original R1–R8 and closed regression assertions remain preserved.

| Case | Independent coverage assessment |
| --- | --- |
| C01 | Correct lost-event NSFW round trip: old consent denies fresh admission/J/read; explicit renewal authorizes only new bindings. |
| C02 | Covers content/assignment/trash/deletion/child reversals and actual tombstone/FK/identity preservation. |
| C03 | Correctly distinguishes atomic business rollback from the independently committed D5 cutoff; unrelated consent survives. |
| C04 | Covers stale approval-form H/E/runtime CAS, both mutation orders and old S/ticket replay after renewal. |
| C05 | Requires rejection before prepare/spawn/body/ORM mutation/flush and before D5 prelude on unsupported ambient entry. |
| C06 | Includes physical resource identity, actual proxy/self-invocation, nesting/suspension/savepoint/unknown and guard lifetime. |
| C07 | Covers original OS/SQL inversion and standalone/indirect/bulk/reference fan-out; no partial business chunking. |
| C08 | Preserves the reproduced J-COMMIT→owner-N→worker gap, both orders, fresh H after Q, zero unauthorized T/C and no digest/network under DB guards. |
| C09 | Preserves child/fields/PRIVATE/restrictive metadata negatives; the maintained exact malformed-stage/input/output/provenance regressions must also remain. |
| C10 | Real SHARE/UPDATE waits, negative KEY SHARE/plain-read fixture and post-wait current V/H on the same transaction. |
| C11 | Correctly includes wire-level prefetch/Continue/fallback/retry/redirect/chunked/unsolicited input and B pause; application receive alone is insufficient. |
| C12 | Exact immutable candidate/receipt, delayed/forged ACK, duplicate/different-result replay and unknown S recovery. |
| C13 | Positive child death does not erase pending S; real V/attempt lifetime and both purge orders prevent resurfacing. |
| C14 | Symmetric owner-Java death before N resolves requires new checks to wait for V and observe actual COMMIT/rollback. |
| C15 | Actual B and bounded-unit expiry checks use one fixed budget; E1 residual must be recorded, not a continuous-time PASS. |
| C16 | Original lock-wait/default deferred rollback assertions retained; late S denied at later fresh gate without pretending Java's own C is timely. |
| C17 | Independent clock shifts, progression anomalies, delayed PG responses and last-check residual; no unproved clock-margin guarantee. |
| C18 | Native process/session/handle/namespace identity, pending/cancelled acquisition, no early close and every reader thread covered. |
| C19 | Failed terminate/controller outage/unknown orphan/drain starvation stay pending/busy; gate cannot reopen from missing health/death evidence. |
| C20 | Actual effective roles, definer chain/search path, malformed binding arrays/cross-ticket credentials and all legacy/direct-DML denials. |
| C21 | Transitive revoke/cancel/purge/erase failure and sensitive derivative/status/listener handling; no false COMPLETE or bit-only authority. |
| C22 | Useful successful workloads, lifetime H/control quotas, two-controller L/F/V contention/headroom and discarded in-flight work; no unrelated published rebuild. |
| C23 | Real supported native successful/denied/cancel/recovery workflow under enforced IPv4/IPv6/DNS/proxy egress denial; disabled/Linux-without-F smoke is insufficient. |
| C24 | Exact focused/full/static/architecture evidence and preserved failures/auth assertions; no abstract model substituted for runtime proof. |
| C25 | Explicitly demonstrates the strict-E2 counterexample after **all** final checks and before actual server commit, distinguishing ACK delay. Not a test to turn green by another check. |
| C26 | Covers D5 ownership, reservation/SPAWNING/BIND, positive no-spawn/death, two controllers, unknown/rollback cutoff/N, caps/disk and committed cutoff before quiescence. |
| C27 | Correct pre-P permanent invalidation versus post-P fresh ReadOK across unrelated V, scoped graph/relevant child/cancel changes and immutable provenance. |
| C28 | If E1 selected, audits every late-marker consumer/status/count/cache/listener/scheduler/recovery/response path and its explicit residual. |

C26 execution must expand the specified cases to delayed launch **after** attempted no-spawn closure, same-manager unknown outcome with retained F, L-versus-V recovery barriers, stale COMPLETE racing a new drain, control-boot rotation before RAG restart, immutable outcome-marker compaction and worker/control durability settings. C27 must exercise reapproval after relevant change: a newly valid source cannot resurrect an old published H/E binding. These are concrete adversarial realizations of the stated contract, not new design revisions or claims of exhaustive test coverage.

All original reproduced failures remain acceptance obligations: false quiescence on unavailable/duplicate worker, stale cached owner/graph authority, new dependencies, malformed digests/provenance, restrictive metadata, expired lock-wait publication, role bypasses and purge/recovery races. Native source-file R7 remains explicitly disabled; metadata F is not live-file enablement. No later model/parser/judge/retrieval product is authorized.

## 8. Runtime, resources and governance obligations

Native LockFileEx must be proven across Java/Python processes/sessions, file identities and handle ownership; pending acquisition is not ownership and early handle close cannot precede remaining reader access. Positive process death requires the bound process object/creation identity and process tree, not a successful termination request/PID scan. External termination is asynchronous; delayed release means continued denial. [Microsoft LockFileEx](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-lockfileex), [TerminateProcess](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-terminateprocess).

Actual PostgreSQL functions/roles must enforce immutable expected V/H/control boot, post-lock comparison, legal ticket CAS and exact producer/receipt scope on the same physical transaction; audit definer ownership, effective PUBLIC/membership rights and every alternate route. Actual owner/H/V atomicity and all physical source/attempt/graph lock orders require independent connection/process barriers. No H/V/business SQL grant to Python follows. These remain unexecuted gates.

The revised resource profile is explicit but unmeasured: 100 sources/depth 5/1 MiB/10,000 edges, one active child/16 pending, one 2s work budget, 5s startup/30s queue, 128/256 MiB child/tree private-commit caps, 16 MiB buffer target and pool/role/handle/process bounds. D5 separately proposes a 10s total controller request, 250ms control/L slices, 1s positive-death wait, bounded F acquisition, two active coordinator operations and at most three metadata probes; unknown backends/guards may outlive the caller. Deadline expiry must never become an unlimited zero-valued GUC or a forced safety release.

Normal 100% expected-authorized success and at least 12/16 useful fault-free-wave successes are prospective **selected-profile** gates. Include cold startup, queue time, all outcomes/outliers, p50/p95/max, CPU/private bytes/JVM attribution, connections/handles and low-disk behavior. A wall deadline is not a CPU quota. E2 containment cannot pass enabled publication throughput; candidate-only must have independently approved scope-specific success criteria.

H/control metadata has lifetime cardinality beyond the live RAG catalog. The proposed 1k/10k/100k history sizes, 512 MiB relation/index high-water and 1 GiB verified free-space thresholds require measurement on the actual PostgreSQL data volume. They are not reservations or guarantees against disk exhaustion. No tombstone/unknown-outcome deletion to meet quota; ENOSPC can make a requested source exclusion unavailable and cannot be reported as committed denial. Compaction needs anti-replay proof or closed quiescent maintenance.

Rollback/restart/restore remain safety-preserving: containment/admission-stop first; positive process resolution, exclusive F/V and committed cutoff; RAG-owned invalidation/purge under its guards; retain additive history/control/ticket state and deny old v2/unfenced writers. Unknown outcome stays unavailable, not forced open. Power-loss, forensic zeroization, concrete OS dump/paging controls, exhaustive bundled-library auditing, enabled native offline flow and configured-limit measurements remain unverified where applicable. The unchanged authentication clock boundary remains an FR17-8 environment/test blocker; no frozen auth alteration or retry-to-green is approved.

The design purpose stays within Phase 17 foundation, but requires explicit additive frozen-owner API/schema/native/transport/SQL approval. Future synchronization must cover the selected ADR, accepted P17-1/P17-3/owner decisions, named interfaces/module and SQL ownership, canonical additive data/package inventories/diagrams and **existing** handoff. Preserve V1–V13 and historical baselines. This review performs none of that synchronization and grants no Phase 18–22 scope.

## 9. Scope-aware 29-dimension ledger

PASS means the design obligation/decision is adequately specified, not that unbuilt implementation passed. FINDING below denotes the accepted strict-expiry requirements blocker; it is not a request for another cosmetic design iteration.

| # | Dimension | Status | Evidence / limit |
| --- | --- | --- | --- |
| 1 | Owner intent/scope | PASS | Design-only authority and four separate gates explicit. |
| 2 | Requirement traceability | FINDING | Strict E2 versus enabled publication requires O5; all exceptions now disclosed. |
| 3 | ADR/architecture/governance | PASS | New owner/API/schema/transport edges explicit and pending approval. |
| 4 | Modulith ownership/dependencies | PASS | Vault mechanical supervisor; RAG jobs/purge; no reverse internals/service/callback dependency. |
| 5 | Package/folder implementation | N/A | No production package change in this review; future placement remains scoped. |
| 6 | Shared DTO/error contracts | PASS | Typed public control/authority DTOs; root HTTP placement unchanged. |
| 7 | SOLID/GRASP/cohesion | PASS | Mechanical authority/process concern separated from RAG policy/job concern. |
| 8 | DRY/KISS/YAGNI/invariants | PASS | H/V/E have distinct purposes; one job engine, bounded mechanical outcome records. |
| 9 | Patterns/anti-patterns | PASS | Native/SQL adapters, immutable capabilities, CAS and state solve concrete failure cases. |
| 10 | Domain transitions/idempotency | PASS | Explicit renewal, retained identities, cutoff/business outcomes and no callback replay. |
| 11 | API/wire compatibility | PASS | Narrow Vault interface, fixed metadata control, v3 one-use body/receipt; runtime wire proof pending. |
| 12 | Input validation/bounds | PASS | Exact manifests/digests/kinds/bytes, scoped IDs and deny-by-default maintained. |
| 13 | Auth/privacy/consent | FINDING | F/H/V/E source safety specified; strict expiry remains unsatisfied without owner change. |
| 14 | Credentials/caches/logs/retention | PASS | No raw persistence/control payload; sensitive derivative and E1 retention consequences explicit. |
| 15 | Transactions/locks/time consistency | FINDING | Source COMMIT order coherent; no mechanism proves C<D under arbitrary pauses. |
| 16 | Failure/recovery/operations | PASS | Positive proofs, immutable outcome markers, closed recovery, pending/busy outcomes and rollback specified. |
| 17 | Database/migrations/permissions | PASS | Metadata ownership/SQL edge/append-only cutover and actual-role gates specified; implementation NOT_VERIFIED. |
| 18 | Algorithmic/resource cost | NOT_VERIFIED | Lifetime metadata, native limits and L/F/V contention require measurement. |
| 19 | Retrieval/recommendation quality | N/A | No such product in this design delta. |
| 20 | OCR/document/multilingual fidelity | N/A | No parsers/live-file enablement; transitive authority separately covered. |
| 21 | Adversarial security obligations | PASS | All C01–C28/R1–R8 mapped, including launch/SQL/time/consumer boundary distinctions. |
| 22 | Test quality/determinism | PASS | Actual roles/processes/server barriers, finite cleanup and preserved assertions required. |
| 23 | Benchmarks/thresholds | NOT_VERIFIED | Useful-success thresholds are proposed, not achieved. |
| 24 | Held-out evaluation science | N/A | No model/judge selection or evaluation capability. |
| 25 | Dependencies/licenses/lifecycle | NOT_VERIFIED | Concrete native packaging/dependencies not implemented/audited; earlier scans narrower. |
| 26 | Build/static/CI | PASS | Required full/static/architecture commands and retained failures remain gates. |
| 27 | Logging/observability | PASS | Typed safe outcomes and source-free operational measurements; no raw diagnostics. |
| 28 | Documentation/status | PASS | Explicit decision alternatives, unchanged accepted contracts/status and checked links. |
| 29 | Hygiene/preservation | PASS | New review artifact only; existing 1,938-file baseline hash-checked. |

## 10. Verification record and exact next owner decision

Executed read-only source/document/Git inspections and primary documentation checks. No application/runtime/model tests ran. The independent trace analysis above is logical design evidence; OS/SQL/Java/Python/offline properties remain NOT_VERIFIED. Ignored receipts are in `logs/phase17-consumption-design-r3-review-2026-10-15/`.

Final document/preservation commands from repository root:

```powershell
.\rag-engine\.venv\Scripts\python.exe logs/phase17-consumption-design-r3-review-2026-10-15/validate_review.py
git diff --check
```

Validation passed: **13 local Markdown targets, zero missing; six D/four CF/eight O dispositions; 28 case rows; 29 coverage rows** (19 PASS, 3 FINDING, 4 N/A, 3 NOT_VERIFIED). All **1,938 pre-existing file hashes are unchanged**; `git diff --check` and new-report whitespace checks passed. The ignored receipt records these results and the report SHA-256. Existing delivery has 183 paths before this report and 184 including it; older inventory counts remain attributed to their historical passes. Only this report is added to nonignored delivery. No existing file, historical review, production/test/migration/accepted contract/ADR/ACTIVE or containment is changed.

**Next owner decision:** record O5 explicitly. Recommended now: **retain strict E2 and containment, acknowledge that enabled completion is blocked by the system model, and stop treating it as an almost-fixed implementation issue.** If that incomplete deliverable is unacceptable, deliberately choose **all three E1 waivers for the full bounded workflow**, or **(a+b) with an explicitly reduced candidate-only deliverable**. Record the associated O1–O4/O6–O8 scope choices; no option is selected by this report. That requirements choice precedes any contract synchronization, implementation authorization or finding closure. No further cosmetic expiry revision is requested.
