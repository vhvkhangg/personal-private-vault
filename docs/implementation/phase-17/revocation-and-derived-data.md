# P17-3 — Alternative B reservations, E1 and derived-data lifetime

**Status:** Owner-approved contract amendment, 2026-10-15; NOT implementation
acceptance. [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md),
[owner decisions](owner-decisions.md#2026-10-15--alternative-b-owner-design-approval)
and [independent checklist §8](reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments)
replace the prior F+H+V+E proposal and strict E2 time requirement. Full source,
NSFW/Private, provenance and freshness protections remain. Containment stays;
ACTIVE CHANGES_REQUESTED and FR17-5/8/9/10 OPEN. No runtime proof is claimed.

## 1. Authority and lifetimes

Only explicit per-item/field consent plus current owning-source eligibility can
authorize content. See [P17-2](source-eligibility-and-allowlists.md). Vault owns
retained source_version and consent_reset_version; RAG owns authorization_epoch,
field_policy_version, classification and standing consent. An ordinary projected
edit advances source_version only. Eligibility loss/trash/delete/restore advances
source_version and its reset watermark atomically. RAG Private/revoke/policy
changes advance the applicable epoch and retain effective denial. Restoring an
eligible classification never renews consent, even if events and hashes repeat.
No new global V or separate H/control ledger is required.

Every job/generation binds an immutable full source/child/field/dependency manifest,
versions/hashes, captured reset values and policy epochs. Relevant dependency
mutation advances the affected ancestor scope epoch/source_version, including
remove/readd; today's equal edge set cannot resurrect a stale generation.
Compare actual historical manifests, not all jobs with the same root ID.

| Object | Authoritative lifetime |
| --- | --- |
| Standing item consent | Until reset/revoke/Private/field-policy change or its own explicit consent limit; normal eligible edit/worker restart/work TTL does not revoke it |
| Execution grant/lease/service credential | Finite gate-time work budget, job/attempt/process/scope bound; one-use input claim; no renewal/replay |
| Durable source reservation | From known admission COMMIT through verified finish COMMIT; no TTL/disconnect/ACK/health release |
| Untrusted stage S | Attempt-isolated candidate only, never active content or authority; seal and verify/discard |
| Historical job outcome | Immutable execution result after known terminal COMMIT; SUCCEEDED is not an index freshness claim |
| Published generation | CURRENT only under fresh source/reset/epoch/field/full-closure and independent retention/selection proof; OUTDATED/revoked/absent otherwise |

IndexCurrent(G) freshly requires live/right-type/non-NSFW/NON_PRIVATE sources,
valid explicit item/field consent, matching captured reset/epoch/policy values,
exact source versions/hashes and dependency topology, active nonrevoked generation
and legitimate independent retention/selection scope. It does **not** require an
unexpired completed worker lease, service session, current worker boot or worker
health. Filter before scoring/counts/content/history. No cached positive flag,
old hash or lost notification can authorize access. Missing proof denies.

## 1A. Durable reservation and physical transaction contract

### 1. Admission and edit exclusion

Only one active indexing attempt, no automatic queue. A static partial unique
index on a constant key over every nonterminal status enforces this in PostgreSQL;
include RESERVED/STARTING/RUNNING/STOPPING/RECOVERY_REQUIRED/FINALIZING and uncertain
outcomes. Closed supported-status constraints and trusted transitions prevent
eviction or terminalizing an active row to free capacity. A second start is 409
RAG_JOB_BUSY; count-then-insert and time predicates are insufficient.

In one short writable admission TX, obtain fresh entire approved closure, lock
ordered sources, prove current owner/consent/field/reset/version state, insert
the job and scoped grant and reserve every source atomically. Known physical
COMMIT precedes spawn/input. Unknown admission sends nothing and reconciles the
actual outcome, not a duplicate start. No partial reservation/source omission.

All selected-source modifications, deletion, classification, revoke, fields and
relevant dependency changes return **409 RAG_SOURCE_BUSY with no requested
change committed**. Do not queue/retry the denied mutation automatically. Owner
cancels, waits for safe release and retries. Unrelated items remain editable.
No lock is held through the indexing interval; the committed reservation is
durable authorization state. Unknown process/SQL outcomes retain it and job
exclusivity. Cannot promise immediate successful revocation during indexing.

### 2. Common guards, ambient entry and SQL backstop

Use stable Vault source_state rows, not a deletable vault_entries row as the
sole lifetime witness. Public reserve/lockReserved/release join the caller's same
writable physical transaction/connection (MANDATORY). Existing Vault lifecycle
locks remain where required. No REQUIRES_NEW/async/autocommit release, swallowed
savepoint rollback or stale EntityManager proof. Reject ambient dispatch TXs
before external effects; compose owner REQUIRED calls only in proven order.

Every relevant owner command obtains guards before managed mutation/flush.
Mandatory owner-local triggers canonically maintain version/reset history and
busy checks for direct/indirect/bulk/import/cascade/OLD-NEW assignment paths.
Do not duplicate competing Java/trigger version rules or whole-entity-overwrite
reservation/version metadata. A fallback trigger already holding a target row
acquires an earlier source guard with NOWAIT; conflict aborts the command, not
backwards waiting or SKIP LOCKED. Missing/overflow metadata denies. Runtime roles
cannot disable/evade enforcement. Unsupported admin writes/restore require
suspension/reconciliation, not hostile-administrator protection.

### 3. Global lock order and budgets

Short RAG graph barrier (only where needed) -> sorted stable source_state IDs ->
sorted Vault lifecycle rows -> owner projection/assignment rows -> sorted RAG
policy -> job/attempt -> generation/staging/run. Owner mutations omit RAG locks;
worker takes attempt -> staging only. A separate attempt-only stop-request TX
finishes before any source/process/control action. Never wait backwards from a
target-row trigger. Ancestor affected-set proof is bounded; unsafe sets deny.

Fresh DTO/projection reads follow locks. Hash bounded coherent snapshots outside
DB guards, then recheck captured version before admission/confirmation. No full
digest, HTTP, client delivery, model or process wait under SQL locks. Use total
remaining lock/statement/TX budgets (initial 3s/5s/10s ceilings) rather than
multiplying per-row waits. Timeout returns bounded conflict/pending, never safe
release. Graph/source caps remain 100 nodes/depth 5/1 MiB selected input.

### 4. Transfer, ACK, consumption and process ownership

T is bytes transferred into Python. U begins with parsing/inspection/hashing the
body, not inference. S is isolated raw-free staging. ACK is transport/receipt
acknowledgement only. AI inference is a separate later-phase capability, not
enabled here. P is trusted Java verification/publication, effective at physical
COMMIT C. An ACK proves neither permission, quiescence nor publication.

RAG owns one job-scoped loopback Python/FastAPI process/tree adapter. Persist
STARTING before spawn, launch suspended/confined, bind actual controller/worker
creation identity and native containment before allowing input. No fallback to an
existing daemon/port listener. Prove endpoint/readiness/capability belongs to the
actual job-owned child; bind failure/stale health/port collision denies input.
Use one atomic issued-grant claim before body consumption; duplicate/unknown
claim never reexecutes. Worker only mutates its audited claim marker, not job state.

Use correctly owned noninheritable Windows handles/Job Object, no breakaway or
unreviewed descendant consumer. Kill-on-controller-close is a backstop, not death
proof. Stop/join all launcher/sender tasks and wait on actual process/tree identity
on success as well as cancellation. Kill request/ACK/PID/connection loss/timeout
is not positive termination; stuck tasks keep reservations. No live-controller
takeover on TTL, including accidental second JVM. No F, Vault DRAIN or leader lease.

### 5. E1(a), E1(b), E1(c): approved expiry changes

PG clock_timestamp() is authoritative for persisted deadlines; sample after
waits, not transaction-start now(). Java/Python wall clocks cannot certify it.
For local units sample monotonic m0 before requesting PG remaining time, cap at
m0 + remaining - safety margin and any prior earlier deadline; never renew or
compare monotonic origins across restarts/processes. Credential/service session
expiry remains an execution gate, distinct from source selection retention.

**E1(a):** a passed read/send/work gate may be followed by post-deadline transfer/
consumption under arbitrary pause/buffering. Elapsed residual can be unbounded.
**E1(b):** sensitive raw-free S can physically commit after expiry, including
checks triggered early; S stays untrusted/isolated. Fresh verification observing
expiry denies; do not grant new authority to stored expired candidates.
**E1(c):** Java's final verification/P gate may pass before expiry yet physical
trusted VERIFIED/P COMMIT complete after it. The owner explicitly accepts that
late transition and that the resulting CURRENT index can remain usable.

Retain observed-expiry rejection before input/S/new verification/P, post-lock and
default deferred checks. Transaction-time validation and deferred-constraint
execution are not the final WAL durability/physical COMMIT instant. Arbitrary
pause can follow the last check. Physical C remains P's linearization point;
response/ACK follows known outcome and is not C. Later read denial never makes
an earlier unauthorized durable transition timely. This amends strict E2 rather
than proving it. E1 time passing cannot release source reservations or relax
NSFW/Private, consent, source/dependency, producer or cancellation proof.

### 6. Staging fence and atomic finish

Every permitted/legacy worker SQL route locks the attempt, freshly reloads
scope/state/staging_closed/current execution identity/observed deadline after
waiting and holds the guard through S physical COMMIT. No pre-lock/RLS-only
boolean, direct unfenced DML, arbitrary-job access or worker publication right.

After actual terminal launch/send tasks and worker/tree death, a short finish TX
takes source/policy/attempt guards in order, drains prior S writers, seals staging,
checks exact committed typed stage/digests/actual rag_worker provenance and current
manifest, and CASes Java P or discards on abort. **P-or-abort, seal, terminal job
and exact reservation release share one physical COMMIT.** No release-only TX.
If S wins, finish waits then verifies/discards; if seal wins, queued old writers
reload closed state and reject. Negative abort cleanup requires no live work lease.

Stop-request TX can deny future S/P without release. If stop wins the attempt
guard, P denies. If P wins, its actual C precedes cancellation state change;
report already completed, not fictitious cancellation or participation revoke.
Cancellation of a new job never deletes an independently CURRENT previous index.

### 7. Recovery and safe release

No automatic resume/resend/retry/reindex. Startup resolves actual PG outcomes
under source/attempt guards; lost ACK is not rollback. Terminal marker + sealed
attempt + released reservations/publication in one resolved C proves finish.
Otherwise discard unfinished candidates and abort only after real process/task
and SQL safety. Never auto-finalize partial S or replay a completion callback.

Spawn-to-bind uncertainty, live paused old controller, failed termination,
unresolved SQL/backend, DB outage or missing identity keeps RECOVERY_REQUIRED,
reservation and active slot. Other items remain editable when DB is available.
Only genuine controller death plus verified worker/tree death and writer closure
permits maintenance recovery; hibernation/new Java UUID is not Windows boot proof.
Trusted full OS restart can establish old processes cannot resume, but actual
adapter/boot/SQL proofs remain implementation obligations, not asserted facts.

Normal restart preserves standing consent and completed CURRENT indexes after
fresh proof; only execution credentials are boot-bound. Declared restore or
uncertain source history suspends affected RAG for reconciliation/renewed consent.
Do not claim detection of hostile undeclared administrator restore.

## 2. OUTDATED, access denial and verified purge

An ordinary eligible edit makes dependent published versions **effectively
OUTDATED at its COMMIT**, even if cached status still says PUBLISHED. Preserve
standing consent/configuration but never serve stale results as current. Hash or
edge remove/readd cannot restore the old generation; explicit manual reindex is
required. Restriction/reset/revoke commits immediately deny every affected
dependency and require renewed consent; cleanup follows, not access permission.

Owner modules need no RAG callback. Persistent source/reset/epoch mismatch is
durable cleanup-required evidence even when notifications vanish. A bounded RAG
reconciler scans actual generation/attempt manifests, materializes/retries purge
and exposes effective status. It never dispatches indexing. An old B derivative
containing A can be purged without cancelling new B whose fixed scope excludes A.

### 2.1 Operator Declared Restore and Authority Suspension Procedure

When an operator restores the database from backup, recovers from an ungraceful hardware fault, or encounters uncertain source history across an administrative boundary:

1. **In-Flight Cancellation and Quiescence**:
   Any in-flight indexing job is cancelled via `rag.cancelJob(jobId)`. The system verifies the native worker process is stopped, seals any untrusted candidate staging rows, and marks the job lease terminal (`CANCELLED`), releasing all source reservations.
2. **Authority Revocation & Index Suspension**:
   The operator calls `rag.revokeSource(sourceId)`. This marks the RAG source registration as `REVOKED` and advances the source's authorization epoch / reset watermark.
   Any existing published index is immediately downgraded from `CURRENT` to `REVOKED` and denied for retrieval.
3. **Stale Consent Rejection**:
   Any attempt to admit a job using the stale pre-restore consent or reset version is rejected with `ILLEGAL_STATE` / CAS failure.
4. **Operator Inspection & Renewed Approval**:
   The operator inspects the fresh source state and dependency topology via `rag.findSource(sourceId)`.
   Upon verification of content integrity, the operator explicitly approves renewed consent via `rag.approveSource(sourceId, new RagConsentCommand(approvedFields, expectedSourceVersion, expectedConsentResetVersion, expectedAuthorizationEpoch, expectedFieldPolicyVersion, confirmedDescriptionHash))`.
5. **Fresh Admission & Publication**:
   A fresh indexing job is admitted with updated source and policy versions via `rag.admitJob(sourceId, new RagIndexCommand(expectedSourceVersion, expectedAuthorizationEpoch, expectedFieldPolicyVersion, descriptionConfirmations))`.
   The supervised worker executes, stages output, and upon successful finalization the index transitions cleanly back to `CURRENT`.


| Artifact | Denial/cleanup requirement | Safe retained state |
| --- | --- | --- |
| Raw uploads/managed copies/OCR/temp snapshots | Stop reads; erase controlled copies, original external files untouched | No raw debug retention; live external capability disabled pending R7 |
| Text/transclusions/chunks/vectors/lexical/features and active/retired/staged/orphan generations | Deny before ranks/counts; purge actual transitive derivatives | Sanitized OUTDATED/revoked tombstone only |
| Context/prompts/caches/worker memory | No reuse/new publication; stop consumer, discard buffers and persistent artifacts | No persistent raw prompt cache |
| Answers/history/snippets/citations/highlights/recommendation artifacts | Future reads deny and content removed for every dependent child | Sanitized provenance only if nonidentifying/nonleaking |
| Judge queues/evidence/prompts/results | Stop before further consumption, seal late result writers, erase dependent evidence | Validated irreversible nonidentifying aggregate only |
| Grants/selection aliases | Close execution; apply independent selection retention where applicable | Minimal anti-replay state; no work-TTL deletion of current persistent index |
| Logs/metrics | Never log raw source/query/credentials/private paths; remediate unexpected sensitive artifacts | Safe typed codes/timestamps/nonleaking aggregate |

Purge completion requires verified rows/files/orphans gone and no eligible
writers, under the attempt/generation fence. Failed erasure remains PENDING and
inaccessible. Safe editing release may precede physical erasure only after all
consumers/tasks and SQL writers are impossible; do not confuse inaccessible file
deletion failure with unresolved writer safety. No bounded erasure guarantee
during storage/service outage. Sensitive digests are not automatically anonymous.

## 3. Published reads and later capabilities

1. Persistent completed index uses IndexCurrent, never completed work TTL/boot.
   Preserve historical job success separately from effective index state.
2. Explicit ad-hoc selection-session retention can independently expire its own
   artifacts; it does not revoke another persistent generation/consent solely
   because a worker credential expired.
3. No owner-visible streaming before successful authorization COMMIT. Every new
   stored history/source/highlight access needs fresh dependency proof. A later
   restriction cannot retract bytes already delivered before its authoritative C.
4. Future QA/judge/model consumers are not enabled by this indexing contract.
   Their actual context-consumption lifetime must receive reviewed source-use
   protection; a fresh pre-inference SELECT alone is not sufficient. No claim
   that indexing reservations already certify parallel later-phase consumers.
5. Live external files remain fail-closed/disabled. R7 protected Win32 identity/
   root/ancestor/reparse/hard-link/writer/mapping/hash lifetime proofs are still
   required before enablement; DB reservations do not stop external editors.
   Approved immutable snapshot is a separately identified source, not the live path.
6. Offline, original field exclusions, actual producer provenance and least
   privilege remain unchanged. No model/parsing/inference product added here.

## 4. Deterministic acceptance scenarios (actual boundaries)

[Alternative B verification plan](alternative-b-verification-plan.md) is the
normative B01–B20 obligation and changed-expectation map. Preserve R1–R8 coverage:

| Legacy case | Required Alternative B interpretation |
| --- | --- |
| R1 — work vs revoke | Reservation-first revoke conflicts; cancel, prove death/seal/release, retry revoke; no later old input/S/P |
| R2 — final owner read vs mutation | Stable source row and physical C in both orders, admission/body/finish barriers; owner-first denies, reserved-first conflicts |
| R3 — generation vs revoke | Synthetic later-output artifacts only; no future consumer acceptance; fresh dependency/output denial after authoritative revoke |
| R4 — lost deletion + restart | Fresh denial, durable history, no partial auto-resume; normal completed consent/index survives restart |
| R5 — child dependency | Actual transitive manifest denial/purge; no unapproved/new child, no unrelated new-generation cancellation |
| R6 — selection expiry/judge | Independent ad-hoc selection expiry denies its own artifacts; completed worker TTL alone does not invalidate persistent index |
| R7 — external replacement | Real supported Windows/NTFS proof or explicit disabled capability; no DB-only/rehash-close substitute |
| R8 — field reduction | Reserved mutation conflicts; after safe release/reduction C, old fields/epochs/artifacts deny and purge |

Use bounded latches/barriers and real PG connections/roles/Windows processes,
both orders, actual physical COMMIT and unknown outcome recovery. No sleeps,
mock/native assertion or abstract model substitutes. E1 changes only explicitly
mapped deadline/lifetime expectations; all other security regressions remain.
