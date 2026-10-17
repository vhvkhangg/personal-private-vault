# Phase 17 — architecture simplification proposal

> **HISTORICAL DESIGN MATERIAL — 2026-10-15 owner decision recorded.** Alternative B
> is approved in owner-decisions.md and ADR-0020. Current normative P17-1/2/3
> include the independent review's stale-intent/description refinements. Pending
> decisions or prior E2/F/H/V/DRAIN recommendations below are historical; this
> draft is not implementation authorization or the current contract.

**DRAFT / DESIGN ONLY / NOT CONTRACT APPROVAL — 2026-10-15.**
Existing handoff: [P17-RAG-FOUNDATION](../handoffs/ACTIVE.md), CHANGES_REQUESTED.
The owner's finalized single-user Windows / one-index-job requirements supersede
the assumptions used by the [F+H+V+E comparison draft](p17-3-consumption-fence-proposal.md).
E1 is the selected **direction**, subject to explicit security-contract review.
Nothing here enables execution, changes accepted contracts or closes FR17-5/8/9/10.

The [independent Revision 3 review](reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-3.md)
approved the previous decision material under its stated assumptions, not its
implementation. Its findings/evidence remain historical; this replacement needs
its own independent review rather than inheriting design acceptance.

## 1. Recommendation and what changes

**Replace F+H+V+E with B: durable per-source edit reservations, one durable active
job, ordinary short PostgreSQL transactions, explicit consent/version checks and
one RAG-managed Python process per indexing job.**

The reservation survives connection/JVM failure and has **no timeout-based
release**. Every participating source mutation checks the same stable source
guard and returns a conflict while reserved. RAG releases only after its sender/
launch tasks are terminal, the actual worker cannot continue and the attempt's
SQL writers are sealed. Unrelated items remain editable. This is the missing
ordering mechanism that allows global F/V/DRAIN to be removed.

Keep a small per-source version/history witness and the existing RAG consent
epoch; single-user operation cannot replace lost-notification history. Keep actual
process supervision; a disconnected worker can still consume buffered content.
Remove the global native source fence, coordination-version SQL chain, Vault
process supervisor, drain registries and proposed multi-stage v3 execution tickets.

Full bounded Foundation behavior remains: individually approved Fiction/Film
field projections, complete dependency authorization, actual Java/Python digest
work, restricted staging, exact provenance, Java publication, manual indexing,
cancellation, verified cleanup and offline operation. No parser/model/query UI/
Ideaverse/later-phase implementation. Live external files remain explicitly
disabled under the existing bounded review disposition; DB reservations cannot
stop an external editor. Their accepted per-file R7 capability/allowlist proofs
remain a separate requirement, not certified or deleted by this simplification.

## 2. Alternatives independently reconsidered

| Concern | A: prior F+H+V+E | B: reservations + job-scoped HTTP worker (preferred) | C: reservations + framed stdio worker |
| --- | --- | --- | --- |
| Source safety | Native F plus global V protocol | Committed source reservation survives DB/JVM loss until safe release | Same reservation proof; pipe alone supplies no exclusion |
| History/consent | H/E and explicit renewed approval | Minimal owner version/reset watermark plus RAG consent | Same irreducible per-source history |
| Database synchronization | Global V and source/policy/attempt fences | Stable per-source row, existing attempt fence, single-active-attempt unique index | Same; no further DB simplification |
| Process recovery | F, supervisor/drain/outcome/boot protocols | RAG records/controls one process/tree, verifies death and seals writers | Same process evidence and pending recovery still necessary |
| Modulith/ownership | Broad new Vault native/transport responsibility | Vault source/reservation state; RAG lifecycle/process adapter | Same Java ownership, new IPC adapter |
| Unrelated items/results | Global contention/in-flight invalidation | Only selected set conflicts; unrelated current indexes survive | Same |
| Operational complexity | Most components and interacting state | Reuses maintained FastAPI/typed HTTP contracts, introduces job bootstrap | Removes server/port/HTTP middleware, introduces bounded framed IPC/readiness and stdout discipline |
| Maintainability now | Excess generality for finalized needs | Changes the missing source-lifetime boundary and current lifecycle; preserves tested transport behavior | Viable and possibly fewer transport lines, but replaces existing Java/Python integration rather than removing another authority mechanism |
| Functional scope | Full bounded workflow, conditional | Same reviewed Foundation features; internal worker is job-scoped | Logical indexing/staging/cancel/status can remain; internal HTTP health/execution contract must be replaced explicitly |
| Future phases | More general than required | Fixed approved scope and stage manifest, process-tree extension per future handoff | Same; framing/streaming/observability protocol maintained locally |
| Required changes | Broad F/H/V/DRAIN/v3 owner schema/native/control | Minimal source/version/reservation/attempt fields, single-active-attempt unique index, owner triggers/process wrapper | All B authority changes plus Java/Python transport rewrite and accepted FastAPI-boundary amendment |

**Choose B for this working tree:** C is viable but does not eliminate reservation,
history, stage fencing or process proof. Reusing the current typed boundary is a
smaller maintainable remediation than replacing it to save an internal server.
C remains a bounded transport alternative, not a second supported runtime or
implementation workstream. No fallback to pipes after an HTTP failure.

A persistent content-consuming Python daemon is another option only if its
buffers/threads can be proved unable to continue before release. Stopping/restarting
it at each job boundary makes it essentially B; an “idle” ACK alone is insufficient.
Defer model-cache optimization until later-phase measurements require it.

Two superficially simpler choices do not satisfy the same invariants: a JVM mutex
disappears while Python can survive; DB locks held throughout HTTP/inference
release on connection failure and violate bounded transaction budgets. Transport
alone (pipe/file/HTTP) does not fix either. A Java-only digest/no publication
profile changes the required Python/full workflow and is not selected.
Single-user does not serialize concurrent HTTP requests or PostgreSQL backends.

## 3. Minimal authority and ownership

Three independent concepts must remain explicit:

| Concept | Proposed owner/data | Meaning |
| --- | --- | --- |
| Item configuration | Existing owning module; RAG classification/field preferences remain their existing RAG configuration | Not consent. Revocation does not delete content, preferences or unrelated settings. Approval does not silently set a Private item NON_PRIVATE. |
| Participation consent | RAG source access state, authorization_epoch and field_policy_version; approved_owner_reset_version | Explicit per-item approval (1B), current allowed fields and exclusion-history binding. No automatic restoration. |
| Index freshness | Generation's immutable source versions/hashes, consent epochs, fields and complete dependency manifest | CURRENT only while every binding remains valid. Ordinary edits make old generations OUTDATED; owner manually starts another job (2B). |

Proposed Vault-owned **source_state**, one retained row per eligible owning ID:
source_id bigint PK, immutable source_type, source_version bigint >0,
consent_reset_version bigint >0, reserved_by UUID nullable, deleted boolean.
Use this stable row as the common serialization target, retaining existing Vault
row lifecycle guards as needed. No FK cascade removes it on hard deletion;
no identity reuse/reset/missing-row-as-zero. It contains no source content.
An existing row is required before approval/reservation; new sources start
unapproved. All migration-seeded old approvals fail closed until explicit renewal.

source_version advances atomically with relevant committed projected-content,
availability or classification changes. On NSFW/trash/delete/restore transitions,
also set consent_reset_version to that new version. Ordinary eligible content
edits advance source_version only. Rollback rolls back both source and metadata.
Thus one monotonic counter plus its reset watermark distinguishes normal edits
from loss of eligibility; no full history/event log/global V is needed.

RAG approval captures the current reset watermark. Allowed -> NSFW -> Allowed
advances it even if both notifications vanish and hashes return to old values.
Old consent cannot pass a fresh index admission/read. Only explicit renewal can
capture the new watermark and increment the RAG epoch. Private/revoke transitions
owned by RAG similarly retain REVOKED access and increment their epoch; returning
to NON_PRIVATE cannot set APPROVED. A field/scope change invalidates old bindings.
Settings/field preferences survive these changes.

Approval never overrides NSFW/Private/trash, unknown/malformed or restrictive
quoted metadata, forbidden fields or unapproved children. Preserve P17-2's exact
allowlists and precedence before raw dispatch, including description confirmation.
No owner JWT/PIN goes to Python; no source/credential/path in logs or failures.

Ordinary eligible edits preserve per-item participation consent. A manual job
binds exact **current** versions, never silently refreshes an old job/generation.
Retain separate description-field approval: proposed manual-start DTO can include
an explicit current description revision confirmation for an already selected
description field; absent/stale confirmation denies that changed field. This
requires a P17-2 clarification. It must never substitute for renewed per-item
consent after a reset/Private/revoke. No automatic consent renewal on reindex.

Java dependency direction stays rag -> vault/fiction/film public contracts;
fiction/film -> vault already exists. Vault never calls RAG, controls Python or
updates RAG jobs. RAG alone owns the process adapter, consent, jobs, publication
and cleanup. Worker SQL retains no business/Vault/policy/pointer rights.

## 4. Enforcing editing restrictions at every entry point

Use the stable source_state row for source serialization. Normal owner commands
lock all affected source IDs in ascending order **before** managed mutation,
dirty flush, owner/association row locks or source deletion. Freshly inspect
reserved_by; any reservation returns HTTP 409 RAG_SOURCE_BUSY, independent of
execution expiry or worker health. Do not wait for a job to finish in a DB TX.
Locks from short concurrent admission/release transactions use NOWAIT or a small
bounded lock timeout and produce conflict/retry, never a stale permission.

Narrow proposed Vault API, using the caller's same writable physical transaction:

~~~java
void SourceEditGuard.requireEditable(SortedSet<Long> ids);
void SourceIndexReservations.reserve(SortedSet<Long> ids, UUID reservation);
SourceVersions SourceIndexReservations.lockReserved(SortedSet<Long> ids, UUID reservation);
void SourceIndexReservations.release(SortedSet<Long> ids, UUID reservation);
~~~

RAG is the sole maintained reserve/release caller; release is a trusted lifecycle
operation, not a worker/controller-supplied quiescent boolean. Vault validates the
exact token/IDs; RAG proves process/writer safety before calling. No reverse
callback, DRAIN or generic coordination framework is introduced.

Extend the existing vault.entry public boundary, not a new shared module.
reserve/lockReserved/release require MANDATORY participation in the same writable
physical transaction/connection as admission or finalization. No autocommit,
async or REQUIRES_NEW release. SourceVersions is an immutable Vault DTO, never
an owner entity/repository or RAG implementation type.

**Backstop against existing indirect/bulk/cascade paths:** propose append-only
owner-table/association guard triggers. Owning trigger identifies the affected
source IDs and calls a narrow Vault-owned SQL guard, checks busy and advances
version/reset metadata in the same business transaction. Cover Fiction/Film
updates/deletes, projected assignment tables, Vault lifecycle and RAG policy/
dependency mutation paths. Explicit frozen-owner/SQL review is required.
This is not permission for Python business SQL.

A trigger firing after PostgreSQL has locked a target row must acquire any
earlier source-state guard with **NOWAIT**, never wait backwards in the order.
Normal Java paths already hold it. Unsupported raw/bulk paths therefore fail
closed on contention rather than form a late-lock cycle. Trigger errors roll back
the mutation; tests must prove rollback and no swallowed partial bulk commit.
Missing source metadata denies. Source creation seeds state before eligibility;
uncommitted new identities cannot have prior consent/jobs. No per-job DB lock
is held across content transfer or hashing.

Inventory all public APIs, assignments, trash/restore/hard deletes, bulk/import/
maintenance, direct SQL helpers and reference cascades. The prior 13-path tests
are not the complete inventory. Shared-label/name edits not in an approved
projection need no reservation; changing projected assignment IDs does. An
indirect mutation affecting a reserved source must conflict atomically.
If a bounded affected-set proof is unavailable, refuse that operation/source;
do not silently update an unguarded dependent item.

RAG graph mutations take a short graph barrier first, then source guards for
parents/ancestors whose scope actually changes. Changing a selected node's
children conflicts; an unrelated X -> A edge may proceed if it cannot change
the admitted root A's closure or fields. No global graph-version invalidation
of unrelated published results. Unknown/new child content is never fetched
outside the fixed approved reserved set.

## 5. Indexing/editing lifecycle and transaction boundaries

No queue of automatic indexing work. A second manual start returns 409 RAG_JOB_BUSY.
Content transfer (T) means bytes crossing into Python; consumption (U) begins
with body parsing/inspection/hashing, not only a model call. Staging (S) is an
isolated raw-free candidate receipt; publication (P) is Java's trusted durable
transition at physical COMMIT (C). AI inference is neither staging nor ACK and
is outside Phase 17. A receipt/HTTP ACK proves none of worker termination,
eligibility, final verification or publication by itself.

Use a static partial unique index on the existing canonical job/attempt table to
allow only one nonterminal indexing attempt; no extra global slot/version table.
Its predicate includes RESERVED/STARTING/RUNNING/STOPPING/RECOVERY_REQUIRED/
FINALIZING independent of lease time. A CHECK restricts the complete status set;
only the safe finish transaction may make the attempt terminal. Concurrent inserts
are resolved by real PostgreSQL uniqueness, not a count-then-insert check. Map
these logical phases to the existing physical status names during migration review.
This uses PostgreSQL's [partial unique index semantics](https://www.postgresql.org/docs/18/indexes-partial.html),
with a constant indexed key and a static nonterminal-status predicate, not now()
or an application-only count. Actual role/concurrent-insert behavior requires tests.

~~~text
IDLE -> RESERVED -> STARTING -> RUNNING -> STOPPING -> FINALIZING -> SUCCEEDED
                         \-> STOPPING/RECOVERY_REQUIRED -> CANCELLED/ABORTED
Cleanup: NONE/PENDING/COMPLETE is separate from execution outcome.
~~~

| Step | Exact boundary |
| --- | --- |
| Manual start | Reject ambient dispatch TX before effects. In one short writable TX: graph snapshot; ordered source guards; fresh owner/consent/field/reset/version/full-closure proof; insert job under single-active-attempt uniqueness; scoped grant metadata; reserve all sources atomically. Known physical COMMIT before spawn/raw dispatch. Rollback/unknown sends nothing. |
| Prepare/input | RAG supervisor creates one bound worker, records identity before permitting input. Capture bounded selected values under short guards; hash/serialize outside DB. Fresh proof matches reservation/manifest; reservation persists. Header capability + one atomic single-use claim on the issued worker grant before application body consumption; duplicate/unknown claim never causes re-execution. Worker can consume only its own grant marker, not canonical Java job state. No G/B ticket protocol or retryable body cache. |
| Work/staging | One child consumes only exact selected bytes/fields under the issued scope. Typed stage/result/digests, source-free errors, exact actual rag_worker provenance. Worker stage function locks its attempt row, checks issued scope/current gate deadline/staging-open state and holds it through S physical COMMIT. No raw payload persistence. |
| Successful finish | Join terminal launch/send tasks and positively stop the actual worker/process tree. Then short ordered TX: lock reserved sources/fresh authority and attempt; wait for prior S writers; close staging; verify exact committed receipt and versions; Java publication CAS; terminal job marker; release reservations **in the same physical COMMIT**. Terminal status frees logical job exclusivity; no release before P COMMIT. |
| Cancellation/expiry/failure | Stop request can deny further staging/publication immediately without releasing reservations. Outside DB, stop/join sender/launch tasks and terminate/wait for worker. A short final TX locks reserved sources/attempt, drains S, closes staging, discards partial candidates, records terminal outcome and releases sources. Negative cleanup does not require an unexpired work lease. Unknown stays reserved/nonterminal/recovery-required. |
| After release | New edits can succeed. Any subsequent relevant change immediately makes the old generation noncurrent or revoked via durable version/consent predicates. No automatic reindex or unfinished-job resume. |

**Lock order:** short RAG graph barrier where needed -> sorted stable Vault
source_state rows -> sorted Vault lifecycle rows -> owner projection/association
rows where required -> sorted RAG policy -> job/attempt ->
generation/staging/run. Owner mutations omit all RAG locks. Worker SQL takes
attempt -> staging only, with no later source/graph/slot acquisition. A separate
stop-request TX may lock attempt only but must finish before any source/control
action. Trigger fallback uses NOWAIT for earlier-order acquisition. No network,
process wait, inference or full content digest under these locks.

EntityManager cached positives cannot prove current authority; use fresh bounded
DTOs after locks. Existing compatible REQUIRED owner composition can remain;
there is no new F-before-BEGIN rule. An outer caller that already dirtied/locked
protected entities in an unproven order is rejected/rolled back before effects;
DB triggers are the safety backstop. No hidden REQUIRES_NEW business commit,
savepoint escape of guards or logical service return treated as physical COMMIT.

## 6. Worker/process lifetime, failure and restart

Keep a **job-scoped consumer**, not one new consumer per stage. Prefer the existing
typed loopback HTTP/FastAPI transport hosted by that single process; no separate
content-proxy parent or Vault supervisor. RAG starts it only for an explicit job,
owns native process handles and the fixed endpoint/capability, and stops it on
success too. Owner job-status/cancellation remains Java-owned when Python is idle.
A framed pipe could simplify transport later, but is not necessary to change
for this repair; changing transport alone is not the security mechanism.

Use one small RAG-owned Windows process-tree adapter: controlled Job Object,
no breakaway, known image/creation identity, kill-on-controller-close and bounded
memory/process limits. Launch suspended/initially confined; no raw input until
identity-binding COMMIT is known. Persist STARTING before launch and process
identity before resume/dispatch. A spawn-to-binding crash is UNKNOWN, not proof
that no child exists. Native containment/assignment must precede source input.
No descendant consumer in Foundation; later libraries need reviewed tree coverage.

Completion/idle/HTTP ACK, kill request and connection loss are not process death.
Wait on the actual process object and verify no descendant/source sender/launch
task can continue. Windows termination can be asynchronous; kernel I/O may delay
it. Job notifications alone are insufficient. These are **unverified adapter
requirements**, not claims that current code implements them.
[Windows Job Objects](https://learn.microsoft.com/en-us/windows/win32/procthread/job-objects),
[TerminateProcess](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-terminateprocess).

No live-controller takeover based on a timeout. Job/slot records the responsible
Java process identity/runtime UUID as well as the worker. Another accidental JVM
cannot start another job or recover the old one while that controller can resume.
Same-JVM launch/stop tasks are serialized and joined before release; a stuck
launch task keeps reservations. After genuine controller death, recovery may
terminate/prove the worker stopped and seal SQL. PID alone/health failure is not
identity proof; inability to resolve an orphan leaves affected items reserved.
A trusted full Windows restart proves old user processes cannot resume, unlike
sleep/hibernate or a new Java startup UUID. No unsupervised leftover launcher.

Recovery is maintenance, not resumption (3B). Resolve old PostgreSQL transactions
by acquiring their actual guards; never infer rollback from lost ACK. A committed
terminal job/publication marker plus released reservations proves completed
finish. If finish rolled back or only partial S exists, abort/discard it; do not
auto-finalize or resend input. Seal attempt writers and release only in a known
commit after process/task safety. Old credentials fail staging-closed/attempt/
runtime checks. On DB outage or ambiguous identity/outcome: RECOVERY_REQUIRED,
bounded request failure, reservations/slot retained, unrelated items still editable.

Normal restart preserves otherwise valid completed indexes and per-item consent
after fresh checks; only execution credentials are process-boot bound. Remove
blanket startup revocation of all approvals/indexes. Declared backup restore,
uncontrolled SQL changes or uncertain source history requires RAG suspension,
reconciliation and renewed approval; no claim to detect hostile undeclared
administrative restore. No automatic job resumption, launch retry or reindex.

## 7. Consent, OUTDATED and verified cleanup

| Event | Consent/configuration | Index/cleanup |
| --- | --- | --- |
| Eligible ordinary projected-content edit, no job reservation | Preserve item participation/field settings; advance source version | Effective OUTDATED immediately at edit COMMIT; exclude before ranking/counts; purge obsolete derivatives through bounded cleanup; no automatic reindex |
| Nonprojected setting edit | Preserve settings/consent; no index version change required | Existing current index may remain current; selected item is still busy during its job |
| NSFW, Private, trash/delete/revoke successfully commits | Consent invalid/reset watermark or explicit REVOKED; never delete unrelated item settings | Immediate inaccessibility for every actual transitive dependency; verified purge pending |
| Return to eligible classification/restore | Consent remains invalid, even identical hashes/lost events | Explicit renewed per-item approval then explicit new indexing required |
| Manual cancel/new job fails | Does not revoke unrelated item consent or modify configuration | Discard only that attempt; preserve a previous index only if still genuinely CURRENT |
| Worker execution lease expires after completed P | No participation change | Does not stale/revoke/delete an otherwise CURRENT published index |
| A changed/restricted outside new job B's scope | No global consent invalidation | Purge old generations actually referencing A; do not cancel unrelated new B merely because an older B generation referenced A |

During indexing, NSFW/Private/consent/field changes affecting its set return
409/cancel-first; **no transition was committed**. Once cancellation safely
releases editing, the classification/revoke transaction commits and immediate
denial begins. Do not claim request-arrival revocation or show a failed Private
change as successful. This follows the owner's edit restriction and is an explicit
amendment of the prior unrestricted “revoke at any time” interface semantics.

IndexCurrent(G) requires current approved NON_PRIVATE/live/non-NSFW proof,
approved reset watermark, exact current owner versions/hashes, current consent/
field epochs, full scoped dependency nodes/edges, generation active/non-revoked
and its own legitimate retention/selection policy. It never uses a stale
materialized active flag, current hash alone, worker lease or runtime health.

Owner modules need no RAG callback: generation-versus-source_version/reset
mismatch is a **durable effective OUTDATED/revoked predicate at COMMIT**, even if
RAG's cached status still says PUBLISHED and every notification is lost. Expose
that effective status and exclude results before ranking/counting. A bounded RAG
reconciler scans known generation dependencies and creates/retries cleanup work;
the persisted mismatch means cleanup-required already exists, not permission to
serve until a listener runs. Cleanup may continue automatically; indexing cannot.

Purge all actual transitive derivative manifests, including retired/staged/orphan
records and controlled synthetic history/judge/cache artifacts. Seal the owning
attempt before deletion; verify no rows/files/writers before PURGE_COMPLETE.
Failure remains PENDING and inaccessible. Digests/provenance can be sensitive;
raw-free is not anonymous. Ordinary OUTDATED records may keep only sanitized
state/provenance after derivative erasure. No debug retention of raw content.
No claim of bounded physical erasure while services/storage are unavailable.

## 8. E1 and independent published-index lifetime

The old Revision 3 ReadOK bound every published result to
min(original worker attempt/session/ticket deadlines). **That is not the new
requirement.** These are distinct lifetimes:

- Item consent: until explicit revoke/restriction/reset/scope change, independent
  of worker startup or TTL.
- Execution lease/service credential: finite work/credential budget; checked at
  start/read-unit/stage/verification/P initiation after waits.
- Published index: useful until source/consent/dependency/profile freshness or
  its own explicitly defined retention/selection policy invalidates it. Ordinary
  persistent indexes have no borrowed worker TTL. Ad-hoc selection-session limits
  remain separate if later applicable; do not turn a private session index public.

Existing code evidence: RagSourceAuthority.lockAttempt checks expiry even for
COMPLETED; RagRecoveryService.reconcile routes those records through lockCompletion
and purges on failure. Startup denies all approvals and active generations.
RagFinalizationGate's idempotent completion also passes execution expiry first.
V8's stage/publication triggers check those work/session deadlines. There is no
implemented general retrieval/history product to certify; these current lifecycle
couplings would erase/deny otherwise valid completed indexes. Split
execution-proof from published-index proof and scope expiry cleanup to unfinished
work, not completed valid generations.

PostgreSQL clock_timestamp() is authoritative for persisted execution deadlines.
After lock waits every DB gate samples it afresh; transaction-start now() and
Java wall clocks do not prove remaining time. For local per-unit checks sample
monotonic m0 before requesting the PG remaining budget, then cap the deadline at
m0 + remaining - margin and at any earlier established deadline. Never extend
it on a new sample or compare monotonic origins across processes/restarts.
These checks are conservative gates, not immunity to a pause after a check.

E1 comparison point is the final actual guarded gate, not a promise of physical
COMMIT before deadline. Keep post-lock and default deferred checks; PG can pause
after the last one and COMMIT later, and SET CONSTRAINTS can fire checks early.
[PostgreSQL constraint timing](https://www.postgresql.org/docs/18/sql-set-constraints.html).
Do not claim a timeout or later read changes physical ordering.

| Explicit E1 consequence requiring reviewed amendment | Boundaries retained |
| --- | --- |
| A passed read/send/work-unit gate can be followed by action after expiry under suspension or buffered delivery; elapsed residual may be unbounded | Fresh actual input gate and conservative nonrenewing monotonic per-unit budget; no newly observed expired gate passes. Durable source reservation still prevents any earlier committed exclusion. |
| S may commit sensitive raw-free candidate metadata after expiry | Candidate remains untrusted, attempt-scoped and isolated; verification started after expiry denies; cleanup verified, not claimed instant. |
| Java verification/P may check before expiry and physically COMMIT after it | This is a real late trusted transition, explicitly allowed only by amended E1. Physical C remains publication. It is not relabelled quarantine or retroactively cured. All source/consent/version/reservation and provenance guards remain through C. |
| An otherwise valid late-published index can remain CURRENT after the execution deadline | Fresh IndexCurrent applies, **not** the expired work credential. This is a further explicit departure from Revision 3's expiring ReadOK; post-C expiry alone neither undoes P nor makes the index inaccessible. |

If a stop request wins the attempt guard before P, P denies; if P wins first,
its actual C precedes cancellation's state transition. No content source is
NSFW/Private merely because execution time passed. Cancel/revoke cannot release
editing before source consumers are unable to continue. No lease renewal/grace,
new grant from a stored candidate, expired-gate promotion or source-authority waiver.

Proposed exact amendment: “For bounded local indexing, execution leases constrain
fresh execution/staging/finalization gates, not the physical completion instant
after a valid final gate. I accept E1's suspension/in-flight residual, expired
isolated candidate retention and late Java verification/publication COMMIT.
Published-index authorization thereafter depends on current explicit item/field
consent, source and dependency versions and independent retention/selection scope,
not the expired execution lease or worker boot. A late P may therefore be usable.
No NSFW/Private/revocation-COMMIT, consent reset, exact provenance or freshness
protection is relaxed. Source mutations are rejected while reserved; reservations
are released only after verified process/task quiescence and SQL fencing.
This replaces the strict E2 time invariant; it does not prove it.”

That wording is proposed for contract review, **not already granted by E1 direction**.
If rejected, retain containment. Do not recreate F/DRAIN to claim strict E2 solved.

## 9. Concrete security/concurrency acceptance matrix

All rows are planned runtime obligations, **NOT_VERIFIED**. Real owner HTTP/API,
PostgreSQL roles/transactions, Windows process handles and bounded barriers;
no sleeps, mock-lock proof, skipped old regressions or abstract-model acceptance.

| ID | Counterexample / controlled interleaving | Required assertion |
| --- | --- | --- |
| B01 | NSFW/private/delete/revoke C before admission/body; both orders with reservation C | Writer first denies; reservation first returns 409 before mutation. Zero unauthorized Python transfer/consumption/staging, not just final P rejection |
| B02 | Java/DB connection dies after reservation/grant C, child paused with body | Reservation survives; owner cannot commit exclusion until child/task termination and SQL seal/release C |
| B03 | Selected A modification vs unrelated B modification; shared assignment/cascade/bulk/import; concurrent HTTP | A conflicts, B succeeds; all indirect affected-source writes covered, atomic rollback/no partial bulk commit |
| B04 | Allowed->NSFW->Allowed with both events dropped; old hashes restored; Private and child variants | Reset watermark/E prevents old consent/admission/read; explicit renewal permits only new job |
| B05 | Ordinary edit/description change, lost events, published old generation cached as active | OUTDATED/denied before ranking/counts; consent/config preserved; manual job only, changed description needs exact explicit confirmation |
| B06 | Two simultaneous starts, duplicate execute/body, old credential different job/attempt, unavailable worker | One slot/consumer; one execution claim; no false success or quiescence, no automatic resubmission |
| B07 | Child dies while S backend paused after last check; cancellation/finish/release both orders | Attempt guard drains prior write or rejects queued late write after staging_closed C; no resurfacing after cleanup |
| B08 | Failed termination, missing identity, queued/paused launch or sender, Java stops during spawn/bind | Keep reservation/slot pending until actual no-more-consumption proof; no ACK/PID/timeout substitution |
| B09 | Crash during P+seal+release transaction and lost COMMIT ACK | Atomic durable terminal marker/publication/reservations agree; no unlocked source with live consumer/open writer; no callback replay/auto-finalize |
| B10 | Same-JVM duplicate control and accidental second JVM; live old controller paused past TTL | No takeover/launch while old controller can resume; unknown remains busy; only genuine death/terminal task allows recovery |
| B11 | Normal restart, desktop restart, DB outage and declared restore | Abort unfinished attempts; no resumed work; valid completed index/consent survives ordinary restart, uncertain restore denies pending renewal |
| B12 | Lease expires before input/S/P gate, during lock wait, after final deferred check and after known successful P | Existing observed-expiry denial tests preserved. Explicit E1 late-C exception recorded honestly; completed valid index survives work TTL |
| B13 | Source/child/version/field/graph change after P, unrelated item edit, hash round trip | Relevant mismatch OUTDATED/revoked immediately; unrelated current index remains usable; no automatic rebuild |
| B14 | Actual rag_worker DML/function/view/legacy route, wrong stage/kind/input/scope/output/producer, role inheritance | Exact original provenance negatives preserved; worker cannot bypass attempt seal, read owner state or change policy/pointer/reservation |
| B15 | Cached EntityManager, ambient outer dirty/rollback/savepoint, row wait, direct owner SQL trigger backstop | Fresh proof, common guard, NOWAIT fallback and atomic rollback; no early flush bypass or backwards blocking cycle |
| B16 | Restrictive quoted metadata, malformed scope, new unapproved transitive target, external editor | Original FR17-1/2/4/11 assertions preserved; no new child/refetch/field permission; external live capability stays denied |
| B17 | Cancel/new-index failure with previous valid generation; erase/outage failure | Cancel only own attempt; no deletion of unrelated settings/current generation, no false PURGE_COMPLETE; source release distinct from physical erasure |
| B18 | At/below/above source/depth/byte bounds and one-job conflict, full history/low disk, repeated cold jobs | Useful success counts, bounded query/process/memory/CPU/disk/latency; no denial-only “benchmark pass” |
| B19 | Enabled native job success, stop/failure/recovery under blocked external IPv4/IPv6/DNS/proxy | Actual supported Java/Python/PG path offline with attempt capture and approved package/native licensing; no disabled/synthetic path substitute |
| B20 | Focused then clean full Java/Python/static/architecture/repository verification | Exact commands/counts/exits/failures, process cleanup and unchanged frozen tests; no claims from this design pass |

Proof of source ordering: admission and mutation serialize on the same stable row.
A committed reservation persists until release; every mutation during that interval
fails. Release requires positive worker/task termination and holds source guards
plus the attempt guard through physical C. Any S either C before closure (included
in verify/discard) or sees closed afterwards and rejects. Pending P/release holds
the row until actual commit/rollback. Thus a committed exclusion cannot precede
later source consumption or stale S; E1 time passing cannot dissolve a reservation.
This assumes complete trigger/API coverage and correct native proof, which tests
must establish. No global V is needed to bridge a vanished DB connection.

## 10. Components eliminated, retained and exact change surface

Eliminate F/LockFileEx **source-consumption** fence; global V/intra-attempt V SQL
edge; Vault DRAIN/L registry/supervisor/cutoff/outcome tables; v3 G/B multi-stage
ticket tables; two-controller drain queues; blanket boot-bound source consent;
worker-lease TTL on published indexes; automatic retry/reindex/resume. Remove the
old 16-job execution queue, since a second start conflicts. These are unnecessary
generality for finalized requirements, not mandatory security primitives.

Retain explicit per-source consent epochs, minimal source history/reset witness,
complete immutable dependency manifest, one-use scoped execution grant, restricted
attempt-row staging fence, physical COMMIT atomicity, real process supervision,
bounded cleanup, fresh per-source published reads and offline controls.
Retain a single job-scoped consumer (not per stage): currently the simplest
verifiable reset of buffers/threads. No native file lock is needed for DB-managed
sources; Windows process APIs remain needed for reliable termination/tree limits.
External per-file guards, if later enabled, solve a different authority problem.

This reservation proves the indexing consumer's lifetime only. It does not
preapprove a future parallel question/answer consumer holding cached context
while an owner changes classification. Later phases must extend the same
source-use exclusion through their actual consumption lifetime, or obtain a
separately reviewed boundary. There is no such concurrent consumer enabled here.

Proposed schema additions/reuse, not migrations: Vault source_state above;
RAG static single-active-attempt partial unique index; existing job/attempt fields for
controller/worker identity, source reservation token and staging_closed; a consumed
marker on the existing issued worker grant (no worker job-state mutation);
approved_owner_reset_version and persistent consent identity;
generation manifest/version/freshness and cleanup state. Reuse existing grants,
jobs/runs/staged_outputs/dependencies/generations; no second job engine or control
ledger. Worker functions use RAG attempt data only; owner guard triggers are a
new explicit owner-to-Vault SQL contract.

Expected affected files after approval: VaultSourceGuard/VaultEntryMutationGuard/
VaultEntryService and narrow new reservation API/state; FictionService/FilmService
and assignment/lifecycle entry points plus owner-local triggers; source projection
services; RagSource/RagSourceService/RagSourceAuthority, RagJobService/
RagFinalizationGate/RagRecoveryService/RagPurgeService, RagEngineClient/grants;
one RAG process adapter; Python contracts/transport/worker/db and regression
fixtures. Append migrations after V13 only. No change to historical migrations
or frozen business semantics beyond the listed guarded reservation integration.

## 11. Contracts requiring approval, bounded plan and owner decisions

| Accepted artifact | Exact amendment to review before implementation |
| --- | --- |
| P17-1 §1–3 / owner-decisions §1,3,4 | Vault stable source state/reservation/API and owner-local trigger guard; RAG owns process lifecycle, not Vault. Restricted worker uses no new Vault SQL guard. Add single-active-attempt unique constraint, actual process identities, attempt seal; preserve dependency direction/least privilege |
| P17-2 §1–2 | Separate classification/configuration from per-item consent; normal eligible edits preserve participation but invalidate index version; reset/restricted transitions require renewed item approval. Specify explicit changed-description confirmation at manual start; no automatic Private->NON_PRIVATE approval |
| P17-3 §1/§1A.1–4 | Durable nonexpiring edit reservation and common source guard instead of F/V; busy conflicts on all relevant owner/policy paths; short transactions/no network/model under DB locks |
| P17-3 §1A.5–7 / §2 / §3.1–6 | E1 gate-time versus late physical C; published-index lifetime independent of worker lease/boot; stop/positive death/attempt seal/release C; cancel-first restricted edits; OUTDATED/access/cleanup derivation; manual reindex/no auto-resume |
| ADR-0019 clauses 2–5 | Same local Python boundary/full Foundation capability, explicitly job-scoped process/single job and bounded owner metadata integration; E1/consent/index lifetime decisions. Use a new focused ADR or explicit addendum after approval, not historical rewrite |
| Existing ACTIVE, architecture/data/DBML/module matrix/package tree, worker/API/benchmark contracts | Synchronize approved additive implementation surface, single-job/no queue and resource evidence. ADR-0016 root HTTP envelopes/module-local adapters unchanged; no new shared module |
| Historical reviews/tests | Preserve evidence. Trace prior counterexamples to B01–B20 and explicitly review changed expiry/lifetime expectations; no passing old tests implies acceptance of these new semantics |

Implementation is bounded to five gated groups **after independent design review,
recorded contract decisions and separate implementation authorization**:

1. Source guard/state/trigger coverage, reset/consent/config separation; focused
   real PostgreSQL owner/entry-point/cascade/round-trip/busy tests B01/B03–05/B15.
2. One-job durable admission/reservations and RAG process supervisor; actual
   boundary, launch/send task, crash/termination/duplicate tests B02/B06/B08–11.
3. Attempt seal, exact staging, atomic P/release, E1/lifetime/OUTDATED/purge;
   both-order actual COMMIT tests B07/B09/B12–17; no unresolved focused failures.
4. FR17-8 full clean Maven, full pytest, Ruff check/format, mypy, architecture and
   applicable repository checks with bounded processes and exact receipts;
   investigate unchanged auth clock failure without changing frozen auth.
5. FR17-9 reproducible cold/normal/boundary single-job/source-set/query-count and
   cancellation/recovery/cleanup resource measurements; FR17-10 actual native
   enabled egress-denial proof. Record hardware, PG/package versions/licenses,
   sample counts/p50/p95/max/failures and NOT_VERIFIED gaps. New independent final
   implementation review only after all required evidence; never self-close.

Do not carry forward 2s F or 12-of-16 queue benchmarks: neither describes this
architecture. Initial Foundation keeps existing 100-source/depth-5/1-MiB caps,
3s/5s/10s DB ceilings and <=15m configurable work lease, one worker, no queue.
Before implementation select measured cold-start/active-process memory/CPU/
latency and cancellation limits on the actual desktop; no new performance SLA
or later inference budget is invented here. Stop/cancel requests have bounded
responses and may remain pending; failure to kill never releases reservations.
Rollback keeps containment, stops/proves consumers, seals writers, then releases
reservations in a known TX; retain history/tombstones and deny old protocol paths.

Finalized choices 1B/2B/3B and Windows/single-job deployment need no reconfirmation.
Remaining concrete approvals: (i) the exact E1 wording and independent published
lifetime; (ii) the bounded owner/API/SQL trigger/schema changes; (iii) changed
description revision confirmation semantics; (iv) review acknowledgment of
cancel-first 409 for affected consent/classification writes and safe pending
availability on an unprovable orphan. Proposed defaults are stated above; no
additional multi-host/concurrency/platform/model options are commissioned.

Draft verification on 2026-10-15: run
`.\rag-engine\.venv\Scripts\python.exe logs/phase17-architecture-simplification-2026-10-15/validate_simplification.py`
from the repository root. Exit 0; baseline 1,939 nonignored tracked/untracked
files, current 1,940, unchanged 1,937; only this new draft and the two linked
comparison/remediation drafts differ from the initial snapshot. All 18 local
Markdown targets resolved; 20 planned acceptance rows inventoried;
`git diff --check` exit 0. Ignored local receipt:
`logs/phase17-architecture-simplification-2026-10-15/validation.json`.
These checks establish document/scope integrity only. No new Java/Python tests,
PostgreSQL/native/egress probes, benchmarks or abstract-model proof were run.
ACTIVE, accepted contracts/ADRs, containment, production, tests, migrations and
historical reviews are unchanged by this design continuation.

**Recommendation: B should replace F+H+V+E. Ready to submit for INDEPENDENT DESIGN
REVIEW, not approved for implementation.** No Windows/native/PG/runtime/benchmark/
offline proof is claimed from this document; all new guarantees NOT_VERIFIED.
Containment and ACTIVE CHANGES_REQUESTED remain. No FR closure, commit/push,
new handoff or Phase 18–22 implementation.
