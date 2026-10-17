# Phase 17 architecture simplification — independent design review

**Verdict: APPROVE DESIGN FOR OWNER DECISION. Recommend Alternative B in place of F+H+V+E.** Durable per-source edit reservations, an enforced single active attempt and actual RAG-owned process/SQL termination are sufficient synchronization mechanisms for the finalized local desktop indexing scope. They are materially simpler than the previous design without relying on “one user” to make concurrent requests disappear.

Approval should include the concrete consent-command and description rules in §4 and the exact contract changes in §8. In particular, the existing fields-only approval command cannot be carried forward unchanged. These are bounded contract requirements, not a request for another architecture redesign. **This report does not authorize implementation, accepted-contract edits or containment removal.** Native/SQL/runtime proofs remain mandatory. ACTIVE stays CHANGES_REQUESTED; FR17-5/8/9/10 remain OPEN; no Phase 17 completion or owner commit readiness is issued.

## 1. Scope, sources and evidence limits

Date: 2026-10-15. Baseline: `main`, HEAD `24e6268eef1685866d919d882ecdf9f532d01961`, with the existing uncommitted candidate and containment. The owner's finalized choices are authoritative: one user/account, one local Windows desktop, one active indexing job, per-item consent, manual indexing, no automatic resume, selected items uneditable during indexing, categorical NSFW/Private exclusion and E1 as the preferred expiry direction. **Those choices need no reconfirmation.** Exact E1/retention/API/schema changes still need explicit recorded approval.

Read all **550 lines** of the [simplification proposal](../architecture-simplification-proposal.md), its [remediation plan](../remediation-plan.md), the now-comparison [F+H+V+E proposal](../p17-3-consumption-fence-proposal.md), accepted [P17-3](../revocation-and-derived-data.md), [P17-1](../integration-ownership-and-permissions.md), [P17-2](../source-eligibility-and-allowlists.md), [owner decisions](../owner-decisions.md) and [ACTIVE](../../handoffs/ACTIVE.md). Compared [ADR-0019](../../../adr/0019-local-rag-before-frontend.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), module contracts and the [independent final implementation review](2026-10-15-phase-17-independent-final-codex-review.md) and [Revision 3 design review](2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-3.md), with their preceding evidence retained. Unchanged canonical material already inspected in those reviews was reused after hash comparison.

Applied repository/documentation AGENTS and scope-aware review/local-RAG/module-boundary guidance. Inspected the actual approval DTO, source-authority/recovery references and maintained expiry/restart/cancellation test bodies. Fresh snapshot: **1,940 pre-existing nonignored files**. Since the previous review baseline, only the comparison proposal/plan changed, with the simplification draft and prior review report added. Production/tests/migrations/accepted contracts/ADRs/ACTIVE and historical reports are unchanged.

Independent evidence here consists of source/document inspection, concrete interleaving analysis and primary PostgreSQL/Windows reference checks. **No application, native, PostgreSQL concurrency, benchmark, offline or abstract-model test ran.** The supplied proposal's checks are document evidence only. Earlier test receipts remain historical, including the unchanged authentication clock failure; none certifies Alternative B.

## 2. Why B is sufficient and simpler

| Concern | F+H+V+E comparison design | Alternative B assessment |
| --- | --- | --- |
| Live source use | Process-owned F excludes mutation; V spans pending SQL after process death | A **committed, nonexpiring reservation** excludes selected-source mutations even after the application/connection dies. No lifetime OS source lock is needed. |
| End of source use | Process death, global drain/cutoff, boot/outcome registries | Terminal sender/launcher tasks, actual worker/tree death, then the existing attempt fence and atomic finish/release. One owned job is a bounded lifecycle. |
| Consent history | H for every relevant source mutation plus RAG E | `source_version` plus `consent_reset_version` distinguishes ordinary edits from eligibility reset; existing RAG epoch handles policy/revoke. The history function remains, with simpler semantics. |
| Unrelated items | Global F/V can block or invalidate unrelated work | Unreserved items can be edited. Only generations whose actual versions/scope changed become inaccessible. |
| Job admission | Queue/control protocol and multiple preparation states | Static partial unique index over all nonterminal indexing phases; second start conflicts. No automatic work queue. |
| Worker boundary | Short-lived consumers, global coordinator and staged G/B tickets | One job-scoped process using the maintained typed loopback boundary; one-use grant claim and attempt seal remain. |
| Persistent indexes | Prior ReadOK inherited work/boot lifetime | Fresh index authority is separate from the completed execution lease and process boot, matching the actual requirement. |
| Failure/availability | Global safety-preserving unavailability | Unresolved job retains its slot and affected reservations; unrelated items remain editable. No timer-based takeover. |

B is simpler because the owner permits **denying edits for the whole indexing interval**. It records that denial durably instead of coordinating a live source write against a still-running consumer. The reservation is authorization state, not a long PostgreSQL row lock. Short source guards serialize changes to it; its committed effect survives their release.

The original post-issuance failure is conditionally closed: after admission COMMIT, owner NSFW/Private mutation cannot commit while the consumer can use the reserved source. After safe release, neither that consumer nor its attempt can consume/stage again. E1 time passing does not dissolve the reservation. This is a real ordering argument, not another pre-send SELECT.

Keep HTTP/FastAPI for this slice. Framed stdio is viable but would replace a tested transport without removing the required reservation, history or termination proofs. No second transport/fallback mode is needed. B preserves the bounded foundation workflow and does not delete the planned later ingestion/query/recommendation functionality. It does **not** certify future concurrent question/judge consumers: their actual source-use lifetime must receive the same reviewed protection when those capabilities are introduced. Live external files remain disabled under the existing R7 disposition; DB reservations cannot fence an external editor.

## 3. Source reservation, process and SQL ordering

### Admission and owner mutations

Admission reserves the **entire fixed, approved dependency closure** and inserts the single active job in one physical transaction. It locks the graph when needed, sorted stable source-state rows, then owner/lifecycle/policy/attempt data; fresh DTOs follow locks. No Python input is sent before known admission COMMIT. Unknown admission sends nothing and requires outcome reconciliation, not another start.

Every relevant mutation must lock/check the same stable source-state row before managed changes. A committed `reserved_by` produces **409 RAG_SOURCE_BUSY with no source/policy mutation committed**, regardless of job expiry, connection health or a worker ACK. No DB transaction waits for indexing to finish. Exact reservation-token/set matching prevents releasing another attempt's sources.

The mandatory owning-table/association/policy/dependency trigger backstop is justified. The repository has indirect callers/cascades and cached ORM state; API convention alone is insufficient. Normal Java paths acquire ordered guards first. A fallback trigger that already holds a target row must use NOWAIT for an earlier source guard, so it aborts rather than forming a backwards wait. Never use SKIP LOCKED to omit a selected source. NOWAIT only controls row-lock waiting; retain bounded statement/transaction limits for other waits. [PostgreSQL locking clauses](https://www.postgresql.org/docs/18/sql-select.html).

Implement the version/reset increment canonically in the owner SQL enforcement path or another single verified owner mechanism; do not scatter competing Java/trigger rules. Reservation changes must update only reservation state, not accidentally dirty cached versions or clear another token. Test OLD/NEW source IDs, assignment reparenting, deletes/cascades, missing state, overflow and shared inputs. If a projected reference change affects several sources, all affected reserved sources must cause atomic conflict; unprojected labels need no new reservation machinery. Unsupported privileged maintenance uses explicit suspension, not an enterprise trigger system for hostile administrators. Actual runtime roles must not be able to disable the backstop or use an unguarded maintained route; unsupported TRUNCATE/DDL may simply be denied.

The partial unique index must use a constant key and a **static complete nonterminal predicate**, not time, application counts or nullable uniqueness. Include STARTING/STOPPING/RECOVERY_REQUIRED/FINALIZING and uncertain outcomes. Normal cleanup/retention must never delete a nonterminal slot row or turn it terminal to free capacity. Bound supported statuses and terminal transitions; worker cannot mutate them. PostgreSQL supports uniqueness restricted to the indexed subset. [Partial unique indexes](https://www.postgresql.org/docs/18/indexes-partial.html).

### Mandatory adversarial interleavings

| Interleaving | Required result and independent reasoning |
| --- | --- |
| Edit acquires guard before admission | Admission waits/fails boundedly; after edit COMMIT it sees new version/reset/current eligibility. A prior transaction start or cached snapshot is not proof. Restricted writer-first means zero Python input. |
| Reservation commits before edit | Edit takes fresh guard and returns 409 without committing. Only the selected set conflicts; unrelated B remains editable. |
| Raw/indirect edit already holds owner row | Trigger's earlier source guard uses NOWAIT. Contention aborts the statement/unit rather than waiting backwards. If it obtains the guard first, admission cannot reserve until that edit commits/rolls back. |
| Java dies after reservation/grant COMMIT | Durable reservation remains. Python may retain buffered bytes; that does not allow an exclusion COMMIT first. Lease expiry, disconnect and startup UUID do not release state. |
| Worker dies while S COMMIT is pending | Actual process death stops future consumption; it does not end the PostgreSQL backend. Finish locks the attempt after source guards and waits for S's transaction to resolve. |
| Finish wins attempt guard before queued old S | It closes staging and commits terminal state/release. The queued write reloads current closed/terminal state after its lock and rejects; no stale pre-lock boolean or legacy route may bypass this. |
| S wins before finish | Finish waits, then verifies or discards the committed candidate while holding the attempt fence. No release/PURGE_COMPLETE is inferred from a missing ACK. |
| Cancel wins before finalization | Its short attempt-only stop TX commits before later source/process actions; P subsequently denies. Reservations remain until all task/process and SQL conditions are met. |
| P wins before cancel | P/seal/terminal/release commits atomically. Cancellation reports the actual completed outcome; it must not falsely claim cancellation won or revoke an unrelated previous/current generation. |
| Crash/unknown during P+release | All commit together or roll back. Recovery acquires the actual source/attempt guards, observes the terminal/publication marker, and never replays a finalizer callback on uncertainty. No automatic finalization of partial S. |
| Launch task or old Java controller can resume | No takeover/release based on timeout. Join same-JVM tasks; an accidental second JVM must refuse takeover of a live responsible controller. Genuine controller death, verified worker/tree termination and SQL closure permit maintenance recovery. |
| Process identity cannot be proved | Retain reservation/slot and report RECOVERY_REQUIRED/pending. This is an affected-item availability failure, not permission to mark NSFW successfully while the worker might resume. |

Safe release requires all three: **no remaining launch/send/consumer task, actual process/tree unable to continue, and attempt writers sealed/drained**. Neither a work lease nor a 409/HTTP/idle/death claim substitutes. The finish transaction must hold source and attempt guards through its physical COMMIT; it publishes or aborts, records terminal outcome and releases reservations atomically. Negative cleanup may finish after work expiry because it removes authority.

Physical erasure can remain pending after safe source release if consumption/writers are already impossible and outdated/revoked data is inaccessible. PURGE_COMPLETE additionally requires verified derivative erasure. Keeping an item locked solely because an already inaccessible file cannot be deleted is not necessary for the source-use invariant; never confuse this with an unresolved writer.

### Native scope that must remain

One RAG-owned process-tree adapter is justified. Launch suspended/confined, persist STARTING before spawn, record actual controller/worker creation identity before permitting input, use noninheritable correctly owned handles, no breakaway/descendant consumers, and wait for the actual process object. Kill-on-job-close is a useful backstop, not positive death or the release condition. Closing the last Job Object handle can terminate its tree; handle inheritance must not defeat that assumption. [Windows Job Objects](https://learn.microsoft.com/en-us/windows/win32/procthread/job-objects).

TerminateProcess is asynchronous when invoked externally; a successful request is not verified termination. Recovery after genuine controller death must still resolve worker/SQL outcomes. Sleep/hibernate and an application restart are not a Windows boot proof. [TerminateProcess](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-terminateprocess).

This needs no Vault process supervisor, global source file lock, drain/cutoff ledger or multi-controller leadership mechanism. Reject accidental duplicate ownership instead of supporting live takeover. Automatic **abort/cleanup** after failure is compatible with 3B; automatic resend/resume/reindex is not.

The HTTP bootstrap must bind the endpoint/grant to that actual job-owned process before source transfer. A port collision or stale listener plus a new, correctly recorded but failed-to-bind child must not make a generic health response authorize sending to the old process. Fail closed on bind/identity mismatch; no fallback to an existing daemon. Prove job-bound readiness/capability and no early body send with wire/process evidence. This is a local bootstrap requirement, not a reason to add a separate supervisor or enterprise transport.

## 4. Consent/reset model and concrete approval rules

The source-version/reset-watermark model is sufficient if enforced atomically:

- Ordinary eligible edit: increment source_version, preserve reset watermark and standing per-item consent. Old generations fail version equality even if a later edit restores the old hash. Only a manual job creates a new generation.
- NSFW/trash/delete/restore: increment version and set reset watermark to the new version. Any relevant committed eligibility loss permanently invalidates the old captured reset value, even with both notifications lost.
- RAG Private/revoke: advance the monotonic policy epoch and retain denied/revoked effective consent. Switching configuration back to NON_PRIVATE does not set APPROVED. Renewed approval changes authority; old generation/ticket bindings cannot inherit it.
- Missing/deleted metadata, reused identity, invalid type or overflow denies. Keep tombstones. Unrelated source/configuration remains unchanged. Do not bind standing consent to Java/worker boot.

### Required precision: reject stale approval intent

The proposal must not be implemented by merely adding “capture current reset watermark” to the existing fields-only approval API. Current [ApproveSourceRequest](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/ApproveSourceRequest.java) contains only approvedFields; current [RagSource.approve](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java) also sets NON_PRIVATE. Those behaviors do not express the new separation on their own.

Concrete single-user counterexample to that weakened variant: an approval request is created at reset=1/E=e, delayed; NSFW then allowed commits reset=3 with both events lost; the old request executes and blindly captures 3. It has renewed permission without a new owner decision after the reset. A delayed approval can similarly undo a later revoke/Private or field reduction if the policy epoch is not compared. No second user/host is needed.

**Include this exact bounded rule in the approved contract:** the explicit approve/renew command supplies the reset watermark and consent/policy version observed in its owner view. Under the same stable source and RAG policy guards, compare those expected values, current eligible/NON_PRIVATE configuration, exact requested fields and any description confirmation. A stale reset/epoch/policy request returns 409 SOURCE_CHANGED with no renewal. Approval cannot change a currently Private classification implicitly. Background work cannot refresh the expected values or retry approval automatically.

This is a small optimistic intent check, not another coordinator. It does **not** require invalidating participation after ordinary source-version changes or reintroducing H/V/runtime-bound consent. The distinction is renewed consent after a reset versus standing consent covering normal eligible edits. Add the delayed-approval race to B04; the earlier C04 stale-form case must not disappear merely because B has fewer rows. No larger architecture revision is necessary if the owner includes this rule in approval.

### Recommended description-revision rule

1. Retain an explicit description field opt-in and the last **confirmed exact description hash** per item/field policy. It is separate from item participation consent. Unselected description never leaves Java.
2. At manual start, if the selected description's current hash equals its confirmed hash, use it without another prompt. Ordinary title/other field edits do not require item reapproval or a description prompt.
3. If that hash changed, the manual-start action must explicitly confirm the **current exact hash shown in its preview**. The Index/Reindex action may carry this confirmation; do not require a second per-item approval dialog. Missing/stale confirmation returns SOURCE_CHANGED/confirmation-required; never silently include the new text or silently drop a requested field to report success.
4. Compare/persist the confirmation and create the version-bound reservation/job atomically under fresh source/policy proof. Reject restrictive/unknown metadata before consent can authorize anything. Confirmation does not renew consent after reset/Private/revoke, add an unapproved child, or expand the field set.
5. Null/empty/whitespace distinctions and the accepted authoritative hash encoding remain. If ordinary edits return to the exact previously confirmed description bytes, that existing hash confirmation may be reused after explicit manual start; the old generation remains OUTDATED by source_version until reindexed. An eligibility reset still requires renewed item consent regardless of equal bytes.

This preserves P17-2's separate revision-bound free-text consent while avoiding repeated prompts for unchanged text or ordinary eligible edits. It is a concrete owner approval choice, not implemented behavior.

Compute hashes outside DB guards from a coherent version-bound immutable snapshot; recheck that captured version under the guard before persisting confirmation/admission. Alternatively reserve first and safely abort before input if confirmation fails. No full content digest under SQL locks, stale hash relabelling or new global coordination is needed.

## 5. Published-index lifetime, OUTDATED and E1

**Published indexes should outlive execution leases and worker boot**, as proposed. IndexCurrent must freshly prove explicit item/field consent, current reset/epoch, source versions/hashes, exact scoped dependency nodes/edges, live/non-NSFW/NON_PRIVATE state, active/nonrevoked generation and legitimate independent retention/selection scope. Execution credentials are not permanent index permissions and are not needed to keep an already completed persistent index valid.

Keep immutable **job outcome** separate from effective **index freshness**: a historically SUCCEEDED job does not make its generation CURRENT forever. Ordinary edits cause effective OUTDATED at their COMMIT even when a cached flag still says PUBLISHED. Reset/restriction means inaccessible/needs renewed consent. Filter before ranking/counts/contents; no stale cached result while waiting for an event. Source version prevents hash-round-trip resurrection; reset/epoch prevents consent resurrection after reapproval.

Relevant dependency changes must also advance an existing per-source topology/policy epoch or source_version, including reversal. Comparing only today's edge set is insufficient for permanent OUTDATED semantics: remove/readd could otherwise restore an old generation without manual reindex. Keep the existing affected-ancestor epoch rule or its equivalent; no global graph counter is needed.

Manual reindexing alone regenerates indexes. Cleanup may detect mismatches, erase derivatives and retain sanitized state; it must never invoke admission/dispatch or resume a partial attempt. Use actual historical generation/attempt manifests: if A changes and an old B generation referenced A, purge that derivative without cancelling a new B attempt whose fixed manifest excludes A. A failed/cancelled new job preserves the previous generation only if it is still independently CURRENT.

Normal Java/worker restart preserves current completed indexes and consent after fresh checks; new process identities invalidate **execution** capabilities. Declared restore/uncertain history is different and requires suspension/reconciliation/renewal. No hostile undeclared-restore detection mechanism is demanded. Ad-hoc selection-session restrictions, if later introduced, remain separate and must not be converted into a public permanent index.

### Exact E1 consequences

| Change | Assessment and retained invariant |
| --- | --- |
| E1(a): action after a passed read/send/work gate | Suspension/in-flight buffering can cause post-deadline activity with unbounded elapsed residual. Gates that observe expiry still deny; local monotonic limits never renew. **Reservation remains**, so an earlier committed NSFW/Private/revoke cannot precede that continuing indexing consumption. |
| E1(b): expired S quarantine | Sensitive raw-free typed metadata may commit after expiry, including early constraint-trigger checks. It remains isolated; a new verification gate observing expiry denies. Actual writer fencing/cleanup remains mandatory. |
| E1(c): late trusted verification/P COMMIT | Last validation may be timely but physical Java VERIFIED/P commits late. This is actual trusted publication, not quarantine, and later denial/purge cannot make it timely. Source/reservation/consent/provenance guards still hold through C. |
| Independent published lifetime — additional explicit consequence | Unlike the previous comparison's expiring ReadOK, an otherwise valid late-published index **can remain usable after execution expiry**. Fresh IndexCurrent applies; the work deadline alone does not revoke it. This must be approved explicitly, not hidden inside “E1 selected.” |

E1 changes the deadline contract; it does not waive NSFW/Private exclusion, reset/consent, current source/dependency integrity, exact worker provenance or cancellation ordering. The finite last-check→COMMIT timing gap remains; no strict-E2 proof is claimed. Default post-wait/deferred checks are retained for ordinary failures, and no fresh expired gate may create publication from a leftover candidate. PostgreSQL permits deferred checks to fire early. [SET CONSTRAINTS](https://www.postgresql.org/docs/18/sql-set-constraints.html).

Cancel-first is a separate intentional interface change. During a reservation, an item/classification/consent edit returns **409 with no successful change**, not “revoked but still indexing.” Cancel and prove safe finish; the owner then submits the edit. If actual mutation COMMIT wins before reservation, indexing denies. If P wins before cancellation, report the actual completed outcome. Do not queue a denied Private change for automatic later application or silently turn Cancel(completed job) into participation revocation.

## 6. Audit of B01–B20 and retained regression coverage

All 20 are **proposed runtime tests, NOT_VERIFIED**. They cover the original finding classes, but broad row names do not replace the concrete subcases below. Add these to the same finite matrix, not another coordination architecture.

| Case | Independent coverage assessment / required detail |
| --- | --- |
| B01 | Correct owner-first/reservation-first source/privacy ordering. Test every relevant owner/RAG policy path, genuine COMMIT and zero T/U/receipt, not merely P denial. |
| B02 | Correct original post-issuance crash/buffer gap. Include live paused worker after Java/DB loss and prove no lease/disconnect-based reservation clearing. |
| B03 | Covers selected/unrelated/indirect/bulk writes. Include OLD/NEW assignment IDs, cascades, shared projected inputs and complete bulk rollback on conflict. |
| B04 | Correct reset/hash-round-trip/child/Private history. **Add delayed approval/reset/E/field-policy CAS**, explicit renewal without old job/generation resurrection, deletion ID/type reuse, missing/reset-overflow metadata. |
| B05 | Correct OUTDATED/description/no-auto-index case. Apply §4's exact hash confirmation rule; test normal title edit, unchanged description, stale confirmation and reset-plus-confirmation denial. |
| B06 | Correct concurrent start/claim/credential/outage scope. Verify complete static nonterminal index, no retention eviction of active slot, one body claim and actual no duplicate execution; wrong process/old listener/port collision cannot receive the body. |
| B07 | Correct pending S after child death and both writer-seal orders. Exercise **every** permitted/legacy staging route and actual role; queued pre-closure checks must reload after lock. |
| B08 | Correct launch/send/identity/failure safety. Add suspended spawn-to-bind, inherited Job Object handles, breakaway/descendant denial, late sender tasks and cancel before worker existed. |
| B09 | Correct atomic P/seal/terminal/release and unknown COMMIT. Observe actual DB states after server resolution; no release-only transaction or callback replay. |
| B10 | Appropriate accidental duplicate-JVM negative, not multi-host support. Live paused controller never loses ownership on TTL; reject takeover instead of adding a leader lease. |
| B11 | Correct normal-restart versus trusted OS restart/restore distinction. Completed consent/index survives normal restart; unfinished input/S is aborted, never finalized or relaunched. |
| B12 | Preserve expired-before-input/S/P and post-lock/default-deferred denials. Add explicit after-final-check late S/P observation; **late valid P remains usable** under IndexCurrent after work TTL. No original strict claim may be inferred. |
| B13 | Correct fresh version/closure/field/child access, lost notifications and unrelated independence. Add ordinary h→h2→h, dependency remove/readd with monotonic affected scope epoch, and published A before/after relevant reapproval. |
| B14 | Retain exact wrong/missing/extra/duplicate stage/kind/input/output/scope/producer tests, cross-job credentials, effective PUBLIC/role/search_path/legacy denials; worker has no reservation/slot/policy/pointer authority. |
| B15 | Correct cached/ambient/flush/savepoint/NOWAIT/rollback proof. Use actual trigger/direct maintained SQL paths; ensure no stale whole-entity state update clears reserved_by. |
| B16 | Correct restrictive quoted metadata, malformed/new transitive child and external-file denial. Package/folder hygiene from FR17-11 belongs in B20 too; metadata tests do not prove it. |
| B17 | Correct previous-current-index preservation and deletion failure. Add the old-B-manifest-containing-A/new-B-attempt-excluding-A cleanup race and cancellation that loses to successful finish. |
| B18 | Correct single-job bounds/storage/cold-resource plan. Use successful sequential jobs and one competing-start conflict; remove obsolete 16-job/F benchmarks. Include lifetime tombstones, no active-slot eviction and low-disk unknown finish. |
| B19 | Actual supported native success/deny/cancel/recovery with enforced IPv4/IPv6/DNS/proxy denial and connection attempts. Old disconnected/disabled smoke cannot pass this. |
| B20 | Exact focused/full/static/architecture/migration/role/package inventory evidence and finite cleanup. Preserve frozen auth assertions/failures; map each intentionally changed RAG expectation before modifying tests. |

Traceability remains: FR17-1/2 external/metadata → B16; FR17-3 output/dependency/provenance → B13/14/16; FR17-4 privileges → B14; FR17-5 real worker gap → B01/02; FR17-6 false quiescence/purge → B06–11/17; FR17-7 cached/physical transaction/graph/time waits → B03/09/12/15; FR17-8 full evidence → B20; FR17-9 resources → B18; FR17-10 offline/supply chain → B19; FR17-11 package hygiene → B20. No finding is closed by this mapping.

### Actual maintained assertions affected

In [RagWorkerBoundaryIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java):

- Keep `leaseExpiryDuringActualGenerationLockWaitDeniesPublication` and the ordinary default-deferred outcome in `workerExpiryAfterLastLeaseCheckRejectsPhysicalCommit`. E1's later-than-last-trigger window is a distinct case, not permission to weaken these gates.
- `expiryAfterFinalizerReturnsBeforeOuterPhysicalCommitRollsBackActivation` must either retain supported outer composition/default deferred protection or assert rejection **before dispatch effects** if orchestration forbids that entry. Do not replace it with a mocked success.
- Split `actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants`: ordinary restart now preserves fresh completed index/consent, while stale worker capabilities and declared restore still deny. It currently deliberately purges/denies all state; that expectation requires an approved change.
- Adapt `lostCompletionAckReturnsSameGenerationAndIndependentSessionExpiryDoesNotMergeConsent` to single active admission; preserve receipt/outcome identity and independent session scope. Add completed-index access after its own work TTL without a new publication.
- Retain actual attempt COMMIT/purge both orders, worker outage, delete failure/retry, effective-role and malformed-provenance tests.

In [RagLifecycleIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java), `cancellingJobRevokesActiveIndexGeneration` currently cancels an already completed job and deletes its generation. Define cancel-running-job separately from revoke participation/delete index. Under the proposed semantics, cancel of a new attempt cannot delete an otherwise valid previous generation; completed-job cancel must report already finished/conflict without pretending it won. Explicit source revoke still denies/purges, after cancel-first where reserved.

Add explicit tests for removed startup/TTL purge coupling in [RagRecoveryService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java) and expiry-first completed reads in [RagSourceAuthority](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java). Ordinary eligible revision tests must distinguish retained item consent from denied outdated input/index; changed-description confirmation remains separate. All old receipts/history stay preserved. Changed expected results require recorded contract amendments first; this review edits no test.

## 7. Boundaries, removals and runtime/resource proof

**Keep:** Vault-owned stable source/reservation/version/reset metadata and guarded public operations; owner-local mandatory trigger backstops; RAG policy/jobs/grants/generations/cleanup/process supervision; exact dependency manifests; one-use input grant; attempt-row writer seal; actual process/task proof; fresh published checks; offline/least-privilege boundaries.

**Remove from the planned repair:** global F/LockFileEx metadata consumption fence, global V/worker-to-Vault definer chain, Vault DRAIN/supervisor/L/cutoff/outcome tables, multi-stage v3 G/B ticket tables, two-controller drain queues, automatic execution queue/retry/resume, blanket boot-bound consent and work-TTL expiry of current published indexes. H's useful history role becomes the local source-version/reset watermark rather than another parallel counter. Keep existing source/attempt row guards; do not replace them with a JVM mutex.

Vault does not control Python or call RAG. Fiction/Film already depend on Vault; RAG uses their public projections and Vault APIs. RAG policy/dependency triggers may call the narrow Vault metadata SQL guard; that owner-to-Vault SQL edge needs review, but **worker SQL needs no Vault/business access**. Use fixed qualified SQL/search_path, actual effective runtime/worker/migrator privileges and inaccessible legacy bypasses. Reuse existing jobs/grants/staging/dependencies/generations; append migrations after V13 only. No second job engine/shared framework or arbitrary new REST feature.

Some native supervision and triggers are irreducible in this supported scope. A process ACK or a dropped connection cannot prove a buffered consumer cannot continue; a Java-only edit helper cannot cover existing indirect SQL/flush paths. They can be small adapters/backstops, not a new control plane. Do not support live controller takeover, model-cache daemons, multiple hosts or automatic indexing to solve problems the owner has excluded.

Required runtime evidence remains: real owner mutation/trigger inventory; both guard orders/caches/cascades and rollback; actual one-job uniqueness; Windows spawn/bind/controller/worker/tree/handle proof; terminal task joining; actual attempt-writer fences/legacy privilege denials; atomic P/release under failure; no false cleanup completion; reset/intent/description tests; fresh index predicates and no auto-index; supported enabled offline flow; complete Java/Python/static/architecture checks. Current test claims do not certify B, and the frozen authentication clock issue remains a diagnosed verification gate, not authorization to change auth code/assertions.

Retain the proposed 100-source/depth-5/1-MiB graph/input bounds, bounded output/stage/history, 3s/5s/10s DB ceilings and configurable work lease up to 15 minutes as distinct controls. **A 15-minute lease is neither a latency target nor a reservation TTL.** Use total remaining request/transaction budgets so per-lock waits cannot multiply indefinitely; native failures return bounded pending outcomes without forced release.

Recommended measurement plan: five warmups and at least 100 **sequential cold job runs** per small/100-source/depth-5/near-1-MiB fixture, recording all outcomes and requiring 100% authorized normal-fixture success/zero security violations. Measure startup, total indexing/P, guard/attempt wait/hold, stop response versus verified stop/release, recovery/cleanup, query counts, CPU/private bytes/JVM attribution, handles/connections and zero scratch/GPU use. Add below/at/above bounds, two simultaneous manual starts (one accepted/one conflict) and repeated leak checks. Do not import the obsolete 12-of-16 throughput floor, queue age or 2s F cap.

Choose concrete cold-start/total-job/stop/CPU/memory thresholds from the supported desktop baseline before accepting implementation; the draft has not measured them. A future prototype/measurement authorization is not production enablement. Storage includes all retained source identities/tombstones, independent of live catalog size; test headroom/ENOSPC without deleting history or releasing uncertain reservations. No exhaustive bundled-library, power-loss, native-firewall or forensic guarantee is inferred from orderly shutdown or package scans.

## 8. Exact owner approvals and canonical amendments

The finalized 1B/2B/3B/platform/one-job choices are already owner requirements. Remaining approval is this finite package:

1. **E1 plus independent index lifetime:** explicitly accept all three timing/retention/late-trusted-C consequences **and** that a source-current late P can remain usable after work expiry. Retain fresh observed-expiry denial before input/S/new verification/P; no source/privacy/provenance waiver. E1 direction alone is not this exact approval.
2. **Bounded owner/API/schema/trigger delta:** stable retained source state, nonexpiring reservations, source/reset versions, mandatory owner-local guard/version triggers, one static nonterminal attempt constraint, RAG process identities and attempt seal, one-use existing grant marker, atomic P/abort+release and restored-history handling. No F/V/DRAIN replacement layer.
3. **Consent/description command contract:** include §4's expected reset/epoch/policy CAS and independent Private configuration veto; approve changed-description hash confirmation combined with manual start, unchanged hashes without another prompt, and no reset renewal from a description confirmation.
4. **Cancel-first/availability semantics:** selected-source/classification/consent mutation conflicts with 409/no change; explicit cancellation/recovery before reissuing edit; unknown orphan/writer keeps reservation and active slot. Unrelated item edits/settings remain available; no automatic application of a denied mutation.

Recommended approval wording:

> I select Alternative B for the existing Phase 17 foundation and the finalized single-user Windows/manual-index/no-resume scope. I accept E1(a), E1(b), E1(c) and persistent published-index authority independent of execution lease/boot, including a valid late P remaining usable. I approve the bounded reservation/source-reset/trigger/single-job/process/attempt-seal design and the stale-approval/description rules in this review. Reserved-item edits, including NSFW/Private/revoke, return 409 without changing state until indexing is safely stopped; uncertainty retains the reservation. No source exclusion, consent reset, provenance, transitive freshness or offline guarantee is relaxed. Implementation and containment removal require their separate authorization and actual evidence gates.

Before implementation, a separately authorized documentation step must amend:

| Artifact | Required change |
| --- | --- |
| P17-1 §1–3; owner-decisions §1/3/4 | Add scoped Vault source-state/reservation/public/owner-trigger SQL responsibilities and RAG-owned native worker lifecycle; preserve one logical owner and worker least privilege. |
| P17-2 §1–2/source matrix | Persistent per-item consent across eligible ordinary edits, reset-bound renewal/stale-intent CAS, separate description rule, independent Private veto and cancel-first field/revoke interface. |
| P17-3 §1/§1A.1–4 | Durable reservations/common source guards, whole-closure edit conflict/backstops and short no-network/digest-under-lock transactions. |
| P17-3 §1A.5–7, §2, §3.1–6 | E1 physical-C exception, separate completed-index lifetime, positive task/process+attempt closure before atomic release, no auto resume/reindex, OUTDATED/cleanup and changed revoke availability. |
| ADR-0019 clauses 2–5 | Focused addendum/new ADR for job-scoped FastAPI, additive owner metadata, standing consent/index lifetime and explicit E1 changes. Do not silently retain its old “session expiry invalidates all generations/late publications rejected” wording as the new contract. |
| Existing handoff and canonical architecture/data/package/API/benchmark inventories | Synchronize the selected actual bounded surface, static one-job/no-queue model, typed busy/changed responses, process lifecycle and new measurements. Keep ACTIVE CHANGES_REQUESTED until actual later gates. |
| Tests/evidence | Explicit expectation map in §6; preserve historical receipts/frozen auth and all unchanged negative assertions. No claim that old green tests accept new semantics. |

ADR-0016 root envelopes and module-local HTTP adapters remain unchanged. Frozen V1–V13/history remain immutable; current-code/migration changes must be append-only and owner-authorized. No new handoff, next-phase scope or commit is needed from this design review.

## 9. Shared 29-dimension design ledger

PASS assesses the proposed design/obligation, not implementation execution. Runtime proof remains required; required refinements in §4 are part of the recommended owner approval package.

| # | Dimension | Status | Evidence / limit |
| --- | --- | --- | --- |
| 1 | Owner intent/business scope | PASS | Finalized 1B/2B/3B, one desktop/job and full bounded foundation respected. |
| 2 | Requirement traceability | PASS | Reservation/consent/E1/lifetime/cancel amendments and original findings mapped. |
| 3 | Architecture/ADRs | PASS | B selected on actual scope; explicit owner/ADR amendments, no silent baseline change. |
| 4 | Modulith/ownership | PASS | Vault state, owner triggers, RAG lifecycle; no reverse feature or worker business SQL. |
| 5 | Package/folder implementation | N/A | No production package change in this review. Future structure must be audited. |
| 6 | Shared DTO/errors/contracts | PASS | Narrow public DTOs and typed 409/changed errors; ADR-0016 retained. |
| 7 | SOLID/GRASP/cohesion | PASS | One local reservation concern and one RAG job/process lifecycle; no Vault supervisor. |
| 8 | DRY/KISS/YAGNI | PASS | Removes global control plane; canonical version/guard and no duplicate job engine. |
| 9 | Patterns/anti-patterns | PASS | Reservation, native Adapter, CAS, static uniqueness and attempt fence have concrete purpose. |
| 10 | Domain transitions/idempotency | PASS | Source/reset distinction, explicit stale-intent and description rules, one active job. |
| 11 | API/wire compatibility | PASS | Explicit busy/cancel/approval amendments, one-use grant, maintained typed loopback boundary. |
| 12 | Input validation/bounds | PASS | Exact fields/bytes/manifests and metadata vetoes preserved, B14/B16 required. |
| 13 | Auth/privacy/consent | PASS | Categorical exclusion retained; §4 intent CAS is mandatory, E1/lifetime changes explicit. |
| 14 | Credentials/logs/cache/retention | PASS | Raw-free staging still sensitive; verified purge/no raw diagnostics, independent retention scope. |
| 15 | Transactions/concurrency | PASS | Durable reservation and atomic seal/P/release proof; E1 does not claim C<D. |
| 16 | Failure/recovery/operations | PASS | No timeout release/takeover, actual death/tasks and SQL proof, abort-only recovery. |
| 17 | Database/migrations/privileges | PASS | Static partial unique index, owner backstop, restricted attempt SQL; actual behavior NOT_VERIFIED. |
| 18 | Resource/time/memory cost | NOT_VERIFIED | Proposed caps only; desktop process/DB/cleanup/headroom evidence required. |
| 19 | Retrieval/recommendation quality | N/A | No later product implementation/quality claim. |
| 20 | Document/OCR/multilingual fidelity | N/A | Live files/parsers not enabled; future source authority remains gated. |
| 21 | Security test obligations | PASS | B01–B20 mapped with concrete omitted stale-intent/launch/cleanup subcases restored. |
| 22 | Test quality/determinism | PASS | Actual owner/worker/native/COMMIT barriers, exact failures and preserved history required. |
| 23 | Benchmarks/thresholds | NOT_VERIFIED | New one-job successful workload required; obsolete queue/F numbers removed. |
| 24 | Held-out evaluation science | N/A | No model/parser/judge selection in this delta. |
| 25 | Dependencies/licenses/lifecycle | NOT_VERIFIED | Native/worker packaging and current audits not executed here. |
| 26 | Build/static/CI | PASS | Full Java/Python/static/architecture/role gates retained; no current green claim. |
| 27 | Observability/errors | PASS | Pending versus terminal/erased and historic job versus effective index state separated. |
| 28 | Documentation/governance | PASS | Exact contract/ADR/test amendments pending; historical reports unchanged. |
| 29 | Hygiene/preservation | PASS | New report only; baseline and local links checked below. |

## 10. Verification record and next action

Read-only commands included scoped UTF-8 document/source reads, test-method/assertion inspection, `git rev-parse HEAD` and a SHA-256 baseline over tracked/untracked nonignored files. Primary PostgreSQL/Windows references verify primitive semantics, not a working B implementation. No application/native/PostgreSQL/model tests or benchmarks ran.

Final review-only command from repository root:

```powershell
.\rag-engine\.venv\Scripts\python.exe logs/phase17-simplification-design-review-2026-10-15/validate_review.py
git diff --check
```

Validation passed: **18 local Markdown targets, zero missing; B01–B20 coverage; 29 ledger rows** (22 PASS, 4 N/A, 3 NOT_VERIFIED); four owner-approval parts; and ACTIVE still CHANGES_REQUESTED. All **1,940 pre-existing file hashes are unchanged**; only this report was added to nonignored delivery (185 paths before, 186 including it). `git diff --check` and new-report whitespace checks passed. The ignored receipt records these results and the report SHA-256. Production/tests/migrations/accepted contracts/ADRs/containment and every historical review are preserved.

**Next owner action:** approve Alternative B **with the finite four-part contract package in §8**, including the stale-approval CAS/description rule, E1 plus persistent index lifetime, and cancel-first/pending availability. Then authorize synchronization of those exact contracts and, separately, implementation under the existing handoff. B is ready for that owner decision; another F/H/V/DRAIN redesign is unnecessary. It is not yet implementation-authorized or runtime-accepted, and no open FR17 finding is closed.
