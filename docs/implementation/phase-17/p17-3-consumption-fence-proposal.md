# Proposed P17-3 delta — consumption fence, DESIGN REVISION 3

> **HISTORICAL DESIGN MATERIAL — 2026-10-15 owner decision recorded.** Alternative B
> is approved in owner-decisions.md and ADR-0020. Current normative P17-1/2/3
> include the independent review's stale-intent/description refinements. Pending
> decisions or prior E2/F/H/V/DRAIN recommendations below are historical; this
> draft is not implementation authorization or the current contract.

> **COMPARISON DRAFT — NO LONGER THE RECOMMENDED ARCHITECTURE.** The owner's
> finalized single-host/single-job requirements and E1 direction are assessed in
> the [architecture simplification proposal](architecture-simplification-proposal.md).
> Preserve this prior draft as decision history/comparison; its E2 recommendation
> and F/H/V/DRAIN implementation plan are not the current recommendation. Accepted
> contracts and historical reviews remain unchanged; neither design authorizes code.

**DRAFT / NOT APPROVED / NOT IMPLEMENTED — 2026-10-15.**
Existing handoff: P17-RAG-FOUNDATION, CHANGES_REQUESTED. Response to the
[independent Revision 2 re-review](reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-2.md).
It supersedes Revision 2 of this draft, not accepted contracts or historical reviews.
P17-CF-1/2/3 remain RESOLVED IN DESIGN and D1–D4 ACCEPTABLE AS PROPOSED, as
independently assessed. D5 is revised here; CF-4/D6 retain an explicit expiry
blocker. No new independent disposition is self-issued.

No production/test/migration/accepted-contract/ADR changes are authorized here.
Java issuance/dispatch denial and Python pre-body containment remain mandatory.
FR17-5/8/9/10 remain open; other findings retain their independent dispositions.

## 1. Owner decision in brief

**Recommend E2 and retain containment.** Strict no-after-expiry durable Java
verification/publication COMMIT is not provable with this protocol under arbitrary
backend/storage/VM pauses. A valid final check at D-epsilon can be followed by
COMMIT at D+delta. Read denial afterwards limits disclosure; it does not undo or
authorize that durable transition. This is an unresolved acceptance requirement,
not missing evidence that another trigger or test can fix.

Keep the independently accepted source-ordering design: same-host process-lifetime
F, durable per-source consent history H, global SQL version V, explicit RAG consent
E and immutable one-use tickets. The required D5 coordination now belongs to a
narrow Vault-owned source-consumer control API, with positive process termination,
a committed V cutoff and no Vault-to-RAG feature dependency.

Choose **A+B, not C** for unrelated V changes: invalidate all issued tickets and
all staged/verified-but-unpublished work, but preserve unrelated published
generations subject to fresh full per-source authority and existing expiry
limits. No immutable ticket is rebound. Historical V is provenance on a published
generation, not a permanent global read permission predicate.

E1 is an **unselected alternative requiring three explicit security relaxations**:
bounded transfer/read/delivery residual, expired untrusted staging retention, and
late durable Java VERIFIED/PUBLISHED transitions. E1 does not meet strict E2.
A candidate-only functional re-scope is a smaller alternative if the owner rejects
late trusted transitions; it also does not complete the accepted publication scope.
Neither alternative is authorized or runtime-proven here. Exact O5 wording appears
below; repeating cosmetic revisions cannot discharge this decision.

Read alongside [accepted P17-3](revocation-and-derived-data.md),
[P17-1](integration-ownership-and-permissions.md),
[owner decisions](owner-decisions.md), [ACTIVE](../handoffs/ACTIVE.md),
[implementation review](reviews/2026-10-15-phase-17-independent-final-codex-review.md),
[current evidence](test-evidence.md) and [plan](remediation-plan.md).

## 2. Revised D1–D6 and exact contract deltas

Every row remains a proposal requiring explicit owner approval. Review acceptance
of a principle is neither implementation authorization nor runtime evidence.

| ID | Accepted rule affected | Revision 2 proposal / review disposition |
| --- | --- | --- |
| D1 | P17-3 §1A.1–2 common owner guard; owner-decisions §1 bounded owner API | Preserve accepted principle: Vault owns a same-host global shared/exclusive OS metadata fence F. Actual consumer holds shared F until process death; participating owner/policy writers take exclusive F before DB entry and retain it through physical completion. All supported writers must participate; unsupported topology denies. |
| D2 | P17-3 §1A.4 no network/client/model work under DB guards | Preserve accepted principle: only bounded loopback transfer and foundation digest under F; no body/network/digest under PostgreSQL source/V/H/attempt locks, no parser/inference/live-file exception. F is not an external source-file handle. |
| D3 | P17-3 §1 monotonic authorization history, §1A.6 worker COMMIT fence; P17-1 §3 and owner-decisions §3 RAG-only persistence | REVISED: add Vault-owned monotonic per-source H and global V. Owner mutation atomically advances H and V. Explicit RAG approval captures H and advances E; no rebind on admission/recovery. Ticket contains immutable complete H/E/revision/field/graph bindings. V readers FOR SHARE, writers FOR UPDATE; compare after locks and hold through physical COMMIT. New owner schema/SQL edge needs approval. |
| D4 | P17-3 §1A.1–5 transaction composition/order and §3.1–3 execution/publication | REVISED: registered outer physical transaction entry takes F before begin and V before any ORM/query/flush/later lock. Unknown ambient entry rejects before side effects. Durable J -> G reservation -> B body claim -> S candidate -> Java verification -> P transitions; exact duplicates do not consume again. No hidden REQUIRES_NEW business commit, suspension or mode upgrade. |
| D5 | P17-3 §1A.7, §2, §3.6 revoke/purge/recovery; P17-1 ownership | REVISED in §9: Vault owns source-consumer coordination, bounded metadata-only public/control interfaces and consumer/drain registry. Standalone owner entry invokes it before F/DB business work; registered outer entry reuses it. Positive death plus exclusive V and a durably committed cutoff establish scoped quiescence. No owner-to-RAG call/table dependency, false completion or network wait under DB locks. New API/schema/transport edges require approval. |
| D6 | P17-3 §2/§3.2 expiry and §1A.5–6 physical COMMIT; FR17-3/7 accepted boundaries | REVISED analysis in §8, still BLOCKED for enabled strict E2: distinguish final SQL/deferred check, WAL durability, physical COMMIT and ACK. Strict trusted transition after-expiry exclusion is unprovable under arbitrary pauses. Recommend E2/containment; E1 needs all three explicit waivers including late Java VERIFIED/PUBLISHED state, not just staging. No later-read retroactive cure. |

D3 replaces the ambiguous claim that a single RAG epoch automatically advances
on an owner NSFW write. Effective authority is the tuple described below, with
**durable source history advanced synchronously by its owner**; RAG need not
receive a notification or acquire an owner-to-RAG dependency. D6 is not an implied
reinterpretation of earlier review acceptance. Any impact on closed FR17-3/7
deadline/provenance guarantees requires independent review before implementation;
passing their old tests alone cannot approve a changed contract.

## 3. Root causes, alternatives and distinct operations

The reproduced failure is J COMMIT -> owner NSFW COMMIT -> Python consumes ->
restricted staging succeeds -> final publication denies. Final denial is too
late. Holding ordinary source DB locks until an ACK still fails when Java/its DB
connection dies while Python retains buffered bytes. F orders the lifetime of
that actual consumer independently of Java/DB connection lifetime. V separately
orders a PostgreSQL backend still finishing after consumer death.

The first draft missed history: A allowed/H=h, NSFW, allowed again with identical
values can appear unchanged after both events are lost. Advancing only global V
cannot distinguish this from unrelated B changes while preserving A's approval.
H records the difference durably. Current-value hashes, updatedAt and notification
arrival cannot supply that missing information.

| Operation | Exact meaning |
| --- | --- |
| Transfer T | Raw selected bytes enter the actual child's transport/buffers; already a privacy exposure. Long-lived Python control/launcher processes never receive/proxy/cache them. Java necessarily holds one bounded authorized transient payload. |
| Consumption C | Any application read, decode or digest of those bytes, including later chunks. Only the bound child, while it owns F, after B. No reusable input cache. |
| Staging S | Restricted SQL persists only the fixed foundation result/digest/provenance candidate, never raw source bytes. A committed candidate has no authority by itself. |
| Staging acknowledgement A | Source-free, exact committed-receipt identity. Worker wire ACK is untrusted until Java verifies durable S and fresh authority. It is not a new grant. |
| Inference I | Model/parser/OCR/embedding/judge use. Absent and forbidden in this Phase 17 profile; no state here authorizes it. |
| Publication P | Java-only active-generation CAS with fresh complete owner/consent/lease proof, held through physical COMMIT. No early owner-visible output. Reads require fresh authority, including H, rather than a cached active flag. |

| Alternative | Assessment |
| --- | --- |
| Staging-only immutable raw snapshot | Later reads can occur after revoke; DB disconnect can leave buffered bytes. Adds raw persistence/purge. A separately consented snapshot changes the functional source contract. |
| Pipe, mmap, file, encryption or one-time key instead of HTTP | Does not revoke an already buffered copy/key. Can use the same F protocol, but transport alone closes no race. |
| ACK under ordinary DB locks / final check / TTL only | Connection death releases guards before later C; final P cannot undo C. ACK-only exception rejected. |
| F alone / V alone | F cannot order a backend's pending S after child death; V cannot stop a surviving buffered consumer. Neither retains per-source consent history. |
| Invalidate all approvals on every V change | Safe conservative H alternative, but unrelated writes require global renewed consent. Prefer per-source H for useful successful work and precise ownership. |
| Prepared PostgreSQL source-guard reservation | Potential alternative retaining locks independently of Java death. Needs a transaction coordinator, bounded slots, crash recovery and explicit long DB-lock/network budget exception. Reservation must not hold attempt locks needed by S. Not selected or declared impossible; ordinary transaction_timeout does not expire prepared reservations. |
| Java-only/hash-only worker input | Removes a content boundary by changing the approved end-to-end Foundation behavior. Requires explicit functional re-scoping; cannot count as Python-content workflow proof. |
| F + H + V + E and one-shot child | Preferred conditional design; broader owner/schema/native changes and availability cost. No claim of implementation proof. |
| Containment | Current safe choice until design approval and separately authorized implementation verification. Does not satisfy enabled FR17-8/9/10. |

See PostgreSQL [prepared transactions](https://www.postgresql.org/docs/18/sql-prepare-transaction.html)
and [timeout semantics](https://www.postgresql.org/docs/18/runtime-config-client.html).

## 4. Authority model: F, H, V, E and immutable tickets

**F:** one Vault-owned database-installation-specific protected metadata-file
byte-range lock, native LockFileEx on one Windows host/kernel. Shared for live
consumer/Java proofs; exclusive for participating source/policy/runtime mutations.
Consumer acquires its own actual lock, not an inherited-handle assumption, before
READY/body endpoint. Pending overlapped acquisition is not acquisition. No explicit
unlock-and-continue path; no descendants. Consumer exit ends application access.
Parent death must not close the child's independent guard. Stable volume/file and
installation identity, ACL, protected ancestors and no replacement/reparse/hardlink
split are prerequisites. Do not auto-recreate a missing F while old consumers may
exist. Kernel release can be delayed; delayed release denies, never permits bypass.
[Win32 reference](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-lockfileex).

**H_s:** Vault-owned positive bigint consent generation for each eligible owning
source identity (immutable Vault ID/type and installation), not a content hash or
timestamp. Keep a minimal content-free history row/tombstone after hard deletion;
no cascading deletion, generation reset, ID reuse or missing-row-as-zero rule.
This is a monotonic invalidation summary, not an unbounded payload/event log.
Overflow/unknown installation/restore history denies. A new source starts at 1
without approval; conflicting retained identity prevents reuse.

Every relevant owner transaction takes exclusive F/V before source locks/managed
changes and increments H_s in the **same physical transaction as the business
mutation**, even if final values equal the old approved values. Relevant writes:
NSFW in either direction, content/description, approved-field inputs and assignment
sets, trash/restore, deletion/availability, and any owner-side classification used
in eligibility/projections. Conservative invalidation on any such successful
mutation is preferred; unrelated fields not used by eligibility/allowed projections
are excluded only after a complete audited dependency inventory. Rollback of that business unit rolls back truth and its H/V changes. A prior
D5 control cutoff is a separate explicit execution-cancellation operation and
remains committed; it does not change H/consent/business truth. No asynchronous
event repairs authority.

For shared/reference data that affects a projection, enumerate all affected
eligible source IDs and advance their H in the same protected unit, or disable
that projection/write path until bounded participation is proven. No unchecked
shared-label edit may change approved bytes without history. This impact audit
may expose a broader frozen-owner change; report it for separate approval, do not
silently add reference-to-RAG dependencies. Revisions remain additional checks.

**E_s:** RAG-owned authorization_epoch. Explicit approval, revoke, field policy,
classification or scope changes advance E under exclusive F/V and existing RAG
guards. Approval freshly proves eligible current owner state and captures
(installation, runtime instance, H_s, exact fields/policy/revisions) in the same
transaction. A changed H makes old consent effectively denied **before any RAG
row is repaired**. Only the explicit owner approval API may set approved_H to
current H; admission, ticket minting, retry, reconcile and startup never do so.
PRIVATE or an ineligible source cannot be reapproved. H changes do not modify E
from an owner module. Explicit reapproval advances E and creates new authority;
it cannot authorize old tickets/staging.

**V:** Vault-owned global coordination version. Participating owner mutations,
RAG consent/graph/lease cancellation and runtime invalidation lock V FOR UPDATE
and advance it atomically with their own state change. D5 also adds an explicit,
separate committed source-use cutoff before the business unit; it only invalidates
old executions and never changes business truth, H or E. H advances for affected
owner sources; E advances for affected RAG policy. No owner locks a RAG row.
Readers J/G/B/S/verification/P take V FOR SHARE; compare the immutable expected V
after the locking read, retaining the lock until their own physical completion.
FOR KEY SHARE and plain SELECT are insufficient. V changes conservatively kill
all already issued tickets, including unrelated-source tickets. They do **not**
invalidate unrelated source approvals: a new A ticket can bind fresh V only if
approved_H_A still equals current H_A and all other authority remains valid.

**Manifest/ticket:** complete deduplicated root/child closure, each node's owner
identity/H/E, exact field-policy version/fields/revisions, graph edges/digest,
input digest/byte count and allowed output profile. Also job/attempt IDs, runtime,
consumer boot + process creation identity, service session, protocol v3, nonce
digest, stage/kind and PG deadlines. Expected values come from Java-issued immutable
rows, never from worker-chosen V/H/E parameters. Graph changes use exclusive F/V
and the graph barrier; invalidate affected existing scope/epochs and tickets.
New child consent and approved root scope must be explicitly established; a new
edge never inherits parent approval.

A fresh proof predicate for every node s is:
current eligible AND approved NON_PRIVATE AND current H_s = approved_H_s =
ticket_H_s AND current E_s = ticket_E_s AND exact fields/revisions/scope/boot.
For every pre-publication transition the entire closure AND ticket.expected_V =
current locked V must match. Already-published reads use the different predicate
below; historical V is not their current-version equality condition.

### V invalidation rule: A+B, not C

Define SourceOK(M): every node is currently eligible, explicitly NON_PRIVATE and
approved at its immutable H/E/fields/policy/revision/runtime binding; the freshly
derived complete root closure and its edges exactly equal M. Check scoped edges,
not an unrelated global graph digest. Missing/changed child denies. This repeats
the owner proof; stored source/active flags are insufficient.

ExecutionOK(T): SourceOK(T.manifest), correct immutable producer/boot/session and
one-use state, current live attempt/session/deadline, and T.expected_V = V read
AFTER FOR SHARE. Applies to J/G/B/S, candidate verification and P. All seal/death
records are lifecycle facts, not current lease/content authority, and may complete
after expiry; P still requires its own full ExecutionOK. Unknown physical outcome
is not execution success.

ReadOK(G): known physically committed publication/provenance record and active
non-tombstoned generation; SourceOK(G.manifest); noncancelled relevant selection/
session and the result's recorded expiry bounds; current authenticated read scope.
For this Foundation profile preserve D_G = min(original attempt/session/ticket
deadlines): V clarification neither extends a result's life nor converts a work
lease into permanent retention. Take F shared and a fresh V FOR SHARE barrier,
then fresh source/RAG locks/projections. **Do not compare G.historical_V or its
terminal ticket's expected_V to current V.** Current V is a transaction guard
here, not a reissued grant. No new ticket, content replay or scope enlargement.
O5/E1 would govern time residuals; E2 currently prevents enabling this path.

| Event | Issued ticket | Staged candidate | Verified, unpublished | Already published / reads |
| --- | --- | --- | --- | --- |
| Unrelated B increments V | Invalid forever; new attempt only | Cannot advance; deny/purge | Cannot P; deny/purge | A remains usable only if ReadOK(A); no rebuild/reapproval solely for V |
| Drain cutoff, no A change | Same invalidation | Same | Same | No change solely for cutoff |
| A or used child H changes, including NSFW round trip | Deny | Deny/purge transitively | Deny/purge | ReadOK false immediately after COMMIT; purge |
| Relevant E/fields/classification/scope changes | Deny | Deny/purge | Deny/purge | Affected bindings deny; unrelated published source unchanged |
| Targeted cancel/revoke or generation tombstone | Targeted attempt denies; V also fences old in-flight work globally | Deny/purge | Deny/purge | Targeted generation/selection denies independently of V; never resurrected by ignoring V |
| D/session expires without any V increment | Gate denies; E1 residual explicitly separate | No fresh verification; cleanup | No fresh P; cleanup | ReadOK false; retained markers cannot authorize access |
| Restart/uncertain restore/runtime rotation | Old boot/instance denies | Deny/purge | Deny/purge | Existing runtime/restore renewal rules still deny; no implicit H/V restoration |

Unrelated B cannot change any node/edge/field/expiry used by A, by definition.
At A's P, shared V and source guards serialized its actual COMMIT with relevant
mutators; after P, fresh H/E/closure and targeted generation/session state detect
every relevant change. Thus historical global V equality adds no security to A's
new read. Reading current V under lock still serializes that read with mutation.
Keeping obsolete V on immutable provenance does not rebind an execution ticket.
New attempts after unrelated churn require fresh J but may retain unchanged
per-source consent. Routine V/DRAIN reconciliation aborts only obsolete unpublished
work; it cannot cancel a completed published generation solely for historical V.
Actual restart/control-boot change remains a different invalidation event. V
mismatch never recycles verified work into a new ticket.

**History example:** approved_H_A=4; NSFW COMMIT H_A=5,V=8; allowed COMMIT
H_A=6,V=9; both events lost. Old ticket fails V/H; new ticket fails approved_H_A=4
versus H_A=6. Explicit fresh reapproval captures 6 and advances E; a new attempt
may then succeed. Two B changes leave H_A=4, so A consent survives but its old
V-bound attempt does not. Content h -> h2 -> h, trash/restore and child round trips
have the same result. No intervening RAG read is needed.

## 5. Physical transaction entry and global lock order

Proposed narrow Vault public facade: SourceAuthorityTransactions, limited to
these source-authority units, not a generic transaction framework. It is outside
transactional owner service proxies. Entry with no transaction: acquire the correct
F mode, start one writable READ COMMITTED physical transaction on the existing
datasource, immediately obtain V in the required mode, then invoke owner/RAG work.
No entity lookup, dirty managed mutation, auto-flush or later-order lock precedes
this registration. OS acquisition does not run inside an arbitrary caller TX.

Proposed Java public signatures (names subject to independent API review):

~~~java
<T> T SourceAuthorityTransactions.read(Function<SourceAuthorityUnit, T> work);
<T> T SourceAuthorityTransactions.mutate(Function<SourceAuthorityUnit, T> work);
SourceHistorySnapshot SourceAuthorityUnit.lockSources(SortedSet<Long> ids);
void SourceAuthorityUnit.invalidateSources(SortedSet<Long> ids);
void SourceAuthorityUnit.tombstoneSources(SortedSet<Long> ids);
~~~

mutate acquires exclusive V and advances it exactly once per physical unit before
work; each relevant source mutation also invalidates its declared H rows. The unit
is opaque, physically bound and non-transferable. Vault owns its manager,
native adapter and H/V access; other modules import public APIs only. A caller
cannot construct a marker or choose an arbitrary connection. Returned DTOs contain
only identity/generation proof, not entities. Existing owner APIs preserve business
semantics; their participating outer entry must move outside current REQUIRED
methods. No guard inserted only at today's acquireSourceGuard call is sufficient.

| Entry/composition | Proposed enforceable rule |
| --- | --- |
| RAG dispatch, verification and publication | Reject ANY ambient transaction before prepare/spawn/content acquisition or protected writes; their top-level orchestration owns each fresh J/verification/P unit. No suspended outer TX and no public callback allowing SQL between the unit's final check and COMMIT. |
| Standalone participating owner mutation | Nontransactional outer facade acquires exclusive F, then starts/binds physical unit and V; owner work joins it. Keep F through actual completion, not method return. |
| Registered outer batch/composition | Outer uses the facade before any DB/ORM work; nested owner calls may join only the same physical connection, transaction identity, EntityManager/resource holder and compatible pre-acquired mode. No mere thread-local boolean. Bind a transaction token plus database transaction identity and verify on join. |
| Existing unregistered ambient TX, even apparently clean | Reject before source/managed mutation, flush, ticket or child side effects. Do not guess cleanliness or suspend it. Caller must adopt the protected outer facade; rollback caller's unit on violation. |
| Shared -> exclusive, opposite mode, REQUIRES_NEW or suspend | Reject; no upgrade. A protected unit cannot suspend while retaining F/V or obtain a second independent protected physical TX. |
| Savepoints / NESTED / caught inner failure | Unsupported inside protected unit; reject/mark whole unit rollback-only. No surviving write may lose a V/attempt lock acquired after a rolled-back savepoint or PL/pgSQL exception block. |
| Same-mode REQUIRED nesting | Reuse established physical unit, acquire no new F, and retain all guards until outer physical completion. Determine complete sorted source set before locking/mutating; discovery cannot add lower-order locks later. Restart whole unit if the bounded set changes. |
| COMMIT/ROLLBACK | Release Java F only after confirmed physical completion. beforeCompletion, service return and logical inner completion do not suffice. No source-bearing response before known COMMIT. |
| Unknown connection outcome | Return UNKNOWN; quarantine the connection, retain owned F until rollback/commit is proven by transaction/DB reconciliation. Do not reuse authority from a callback. Process death may release F; pending V lock supplies the independent DB fence. |

Affected-ID discovery may perform fresh bounded non-locking projections after
V registration in a clean context, before any ordered source lock/ORM mutation.
All IDs then freeze. New source creation under exclusive F/V inserts a new H=1
row atomically with identity creation; it has no old consent/ticket. Retained
tombstones deny identity reuse. Bulk/import paths exceeding the bound reject;
no silent partial business commits or independently committing chunks.

Participation audit must enumerate public, indirect, assignment, bulk/import,
delete/restore, scheduled cleanup and cross-module callers, including Vault APIs
used by other frozen modules. Nonparticipating types must remain unchanged; any
existing outer caller that can reach eligible Fiction/Film/Vault mutations must
adopt protected entry or be explicitly unavailable for that operation. If this
requires broader frozen-module changes than approved, stop for a scoped decision.
The known 13 mutation tests are a starting inventory, not completeness proof.

D5's metadata control prelude is a separate bounded lane, completed before this
physical source/business unit begins (§9). It never holds a registry row while
waiting for F/V/control/process death; it does not hide an independent business
commit. Unsupported ambient entry rejects BEFORE that prelude too. The committed
V cutoff is explicit cancellation side effect even if subsequent business work
rolls back; business truth/H/E still change only in their original atomic unit.

**Source/business total order:** F -> V -> private unique Vault outcome row (when
used, before later locks) -> RAG graph barrier (when relevant) -> all Vault source
rows ascending ID -> H rows ascending ID -> owner rows / RAG policy rows in
documented ascending keys -> attempts -> tickets -> generation/staging/run rows.
Owners omit graph/RAG locks; no owner-to-RAG call/lock edge. All source sets are
fixed before source locking. Worker SQL omits business/graph/policy locks, using
V -> H -> attempt -> ticket -> staged row. It cannot later acquire an earlier lock.
Deferred validation may recheck already held locks, never acquire a new earlier
guard. No DB unit waits for READY, body, cancellation or a child holding a needed
lease row. Admission-stop control never holds DB locks while waiting for F/death.

Cleanup after proven child death may use a restricted SQL-only V -> attempt ->
ticket -> stage unit, without later acquiring F or owner/graph/policy locks. Full
policy purge/invalidation uses a new exclusive-F unit in total order. Unknown
consumer identity cannot use the cleanup exception to claim quiescence.

Spring's [physical/logical propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)
and [completion lifecycle](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionSynchronization.html)
are constraints, not evidence that an annotation implements this entry protocol.

## 6. Protocol, durable transitions and linearization points

READY/admission are metadata preparation, not ticket authority. One existing
canonical job engine owns attempts; the ticket is its execution substate, not a
second scheduler. All control/response objects are source-free and bounded.

| Phase | Locks, durable transition and required ordering |
| --- | --- |
| Admission/prepare | Fresh full authority in a protected Java read unit; metadata-only bounded queue. Commit admission. Reserve one worker slot; start one child without payload. Startup occurs before F and cannot consume anything. |
| READY | Actual child acquires shared F and validates identity, then opens its literal-loopback bound endpoint. Only actual native acquisition permits READY. Java verifies child boot, process creation identity, endpoint and attempt over authenticated control; no arbitrary URL from a response. |
| Q capture/preparation | After READY, Java also takes shared F. A short registered V-shared/source-guard unit captures clean bounded immutable owner values plus H/E/fields/eligibility and full closure; no body to Python. Release ALL DB guards, then canonicalize/hash the Java snapshot and compute input/expected synthetic output/manifest digests outside SQL. F remains continuously held; the child and Java share it. Q alone authorizes no transfer. |
| J authorization | New V-shared/ordered full owner proof: freshly read current eligibility, H/E/closure/policy/deadlines and compare Q identities/generations and computed exact revisions/fields against explicit approval. No cached positive proof and no content rehash under DB locks. Insert immutable AUTHORIZED ticket. **J physical COMMIT** creates transfer eligibility. Java retains shared F for send, releases every DB guard before network. J rollback/unknown means no body. |
| G reservation | Child, still holding F, creates a 32-byte CSPRNG permit locally and opens short SQL unit. V/H -> attempt -> ticket, post-lock freshness/expiry check, CAS AUTHORIZED -> RESERVED, store its SHA-256 digest bound to child/boot/attempt. **G COMMIT** reserves exactly one body opportunity. Metadata permit returned only after known COMMIT; SQL stores no plaintext permit. No new expiry/budget. |
| B actual body-start gate | Child's attempt-bound body handler validates headers/permit, before invoking any body receiver. Fresh V/H/lease/session PG check after waits, current remaining operation budget, CAS RESERVED -> BODY_CLAIMED in one SQL TX. **B COMMIT** plus immediate conservative monotonic check authorizes its first bounded read. Delayed/expired G permit denies before application body read. |
| T/C | No PostgreSQL locks held. Actual child has F for its whole lifetime. Bounded incremental reads/digest; fixed deadline/cancellation check immediately before each read/work unit and after every blocking return. Abort/discard on timeout/short/long/mismatched input. No parser, child descendants or arbitrary result text. |
| S candidate | V/H -> attempt -> ticket, fresh PG expiry after waits, BODY_CLAIMED -> CANDIDATE_COMMITTED, insert exact typed digest/provenance candidate. All guards held through **physical S COMMIT**. Existing deferred deadline checks retained; see D6 for early-trigger/absolute-time limits. Only known COMMIT permits worker ACK. |
| A verification | Wire ACK alone proves nothing. Java reads actual committed S in a new full-authority unit and CASes CANDIDATE_COMMITTED -> VERIFIED only after fresh exact proof. SQL comparison and physical verification COMMIT are distinct. If expiry is observed at its gate, deny; if COMMIT itself is late, strict E2 is violated and remains blocked. Only explicit E1 waiver (c) could allow that durable transition; read denial is not its cure. |
| Death/seal | Positive process death is recorded by Vault coordination. Normal successful-attempt sealing uses shared V/attempt guards and known same-ticket S outcome; this proves only no more content consumption for that child, not global writer quiescence. A global D5 quiescence claim additionally requires its exclusive V cutoff. Waits for processes/control occur outside SQL. No content retransmission. |
| P publication | New shared-F/V full owner/H/E/manifest/lease proof plus exact VERIFIED receipt and positive death/seal; Java active-generation CAS with existing deferred checks. **P physical COMMIT remains publication**, never quarantine. Strict E2 requires it before D and cannot be proven under arbitrary pauses; keep contained. E1 waiver (c) would explicitly permit late P, with every subsequent use gated; it does not meet E2. Published reads follow ReadOK, not historical V equality. |
| N owner/revoke mutation | Vault control prelude, positive death, exclusive F and explicit committed V cutoff (§9), then a separate ordered owner/policy physical unit under the same exclusive F. Atomically business change + H/V or consent/lease change + E/V. **N physical COMMIT** alone linearizes that source exclusion. Cutoff/quiescence does not claim business success; failed/unknown N is not acknowledged success. |

Q and J use new owner-owned immutable source-value snapshots and fresh authority
metadata projections: capture values under guards, compute their canonical
revision hashes outside DB transactions using the owner's pure revision encoder,
then compare with current H/approval under J. Existing source service hash builders
must not accidentally run content digest work while DB guards are held. Missing
mutation participation makes this split unsafe and blocks the eligible source;
J's fresh H comparison is mandatory, not an assumption that a Java cache is valid.
No Q data survives a failed J or becomes a retry cache. Final P can use another
bounded Q/J-style proof if values need recomputing, with its own shared F through
P COMMIT; no parser/model/digest loop under source DB locks.

B is executed in the actual body route, not in a reusable earlier ALLOW_BODY
endpoint. The child must not use an ASGI/framework middleware that reads/prefetches
the body before this handler. Java uses headers/strict Expect-Continue handling
with no automatic timeout fallback that sends raw bytes before B acceptance;
that adapter behavior requires wire-level tests. Socket/kernel buffers are part
of T exposure even if not application C; F and J must already protect T. After
B COMMIT, any delay before the first read is charged to the SAME fixed deadline.
A local atomic handler claim rejects duplicate streams even if an exact B status
lookup is repeated. No separate handler can attach to BODY_CLAIMED.

| Retry / failure | Exact state rule |
| --- | --- |
| Concurrent/serial G twice | One CAS wins. Later request returns reserved-status metadata only, no second permit/body authorization; cannot reset nonce/deadline. Lost G response aborts this consumption opportunity. |
| G/B rollback | No usable permit/body claim. Reconcile/abort ticket; no content. Do not infer rollback from a disconnected caller. |
| Unknown G/B COMMIT | No raw send/read; terminate/reap child and query bounded metadata. Abandon ticket; any later retry is a new admitted attempt and fresh J, never retransmission. |
| Duplicate B/T | Existing BODY_CLAIMED denies another stream. Partial body, HTTP retry or restart never resumes using cached input. |
| Duplicate S | Only an exact already committed same candidate may return its original receipt under current guards; different stage/digest/producer/body or terminal ticket denies. No overwrite, new row, renewed deadline or second computation. |
| Unknown S COMMIT / lost A | Stop child; unique row queried after writer drain. Verify fresh authority if still valid; otherwise purge. Absence before backend completion is not rollback proof. |
| VERIFIED -> SEALED -> PUBLISHED | Java-only transitions after positive death/drain and final fresh proof. Ticket terminal; repeat finalization returns exact existing scoped outcome only while authorized, never a different generation. |
| Any deny/cancel/expiry | ABORTED/PURGE_PENDING; tombstone retains anti-replay identity. Restricted worker cannot reset, approve, publish or renew any state. |

A retry after a transient outage may use existing explicit consent only if H/E,
fields/revisions/session remain unchanged. NSFW round trip, source/assignment
change or restore requires explicit renewal, not an automated retry.

## 7. Proposed API, schema, SQL, migrations and ADR impact

These are design specifications, **not migrations or executable patches**.

| Owner / proposed object | Exact proposed fields/invariants |
| --- | --- |
| Vault: vault_source_coordination | singleton boolean PK constrained true; installation_id/control_boot UUID NOT NULL; version bigint NOT NULL > 0; last_mutation_id UUID. One row, never delete/reset online. V lock remains until physical completion. |
| Vault: vault_source_consent_history | vault_entry_id bigint PK; immutable entry_type; installation_id UUID; consent_generation bigint > 0; lifecycle LIVE/DELETED; last_mutation_id UUID. No content, title, path or timestamps as authority. No cascading FK to deletable business row; retained tombstone prevents ID/type reuse. |
| RAG: sources additions | approved_owner_generation bigint nullable, approved_installation/approved_control_boot UUID nullable, bound to existing approval_instance/E/fields/revisions. Existing approvals initially DENIED; no invented consent. SourceOK compares current control boot too, so independent supervisor recovery cannot silently reuse old approvals before RAG recovery runs. |
| RAG: execution_tickets | ticket_id UUID PK; job/attempt FK + unique execution key; immutable installation/runtime/consumer boot/process identity/session/stage/manifest+input digests/byte length/expected_V/deadlines; nonce digest; state enum above. No raw body/endpoints/private paths. Immutable binding enforced by restricted update functions/trigger, not caller convention. |
| RAG: ticket_source_bindings | ticket_id + owning source ID PK; H/E/field-policy/revisions for every closure node, exact graph digest. Sorted canonical encoding, complete-count/digest checks. Worker cannot INSERT/UPDATE/SELECT arbitrary bindings. |
| RAG: staged_outputs/receipt linkage | Unique ticket/stage/output kind; exact bounded result digest/provenance. Candidate versus Java-verified authority explicit; immutable after insert. Existing stage byte/count ceilings apply. No raw text or arbitrary worker-controlled metadata. |
| RAG: lifecycle records | Existing attempt/job/generation/run engine reused; add ticket/verified/sealed and opaque Vault consumer_ref/control_boot references where required. No parallel scheduler/outbox. |
| Vault: source_use_control/source_consumers/source_drains | Source-free control singleton, finite spawn/process registry and drain records in §9; no RAG/domain FK. Control-only lane L separate from source unit; committed cutoff operation ID/V is DB proof. No worker direct table access. |

Use the existing schema's agreed physical namespace conventions in the approved
migration; table ownership above is logical module ownership. Vault objects are
not placed under RAG ownership to evade the frozen-owner decision.

Exact proposed v3 storage types (all authority columns NOT NULL except the
explicitly nullable fields; these are specifications, not DDL):

~~~text
execution_tickets:
  ticket_id/job_id/attempt_id/installation_id/runtime_id/consumer_boot_id/session_id: uuid
  consumer_ref/control_boot_id: uuid (immutable Vault control binding)
  consumer_pid: integer > 0; consumer_created_filetime: bigint > 0
  protocol_version: smallint = 3
  expected_v: bigint > 0; source_count: integer BETWEEN 1 AND 100
  input_bytes: integer BETWEEN 1 AND 1048576
  stage_type: varchar(20) IN ('SYNTHETIC_STAGE','SYNTHETIC_TEST')
  output_kind: varchar(20) = 'SYNTHETIC_DIGEST'
  manifest_digest/input_digest/expected_output_digest/credential_digest: bytea (32 bytes)
  ticket_until/session_until/lease_until: timestamptz (immutable upper bounds)
  state: varchar(24) IN (AUTHORIZED,RESERVED,BODY_CLAIMED,CANDIDATE_COMMITTED,
                        VERIFIED,SEALED,PUBLISHED,ABORTED,PURGE_PENDING)
  permit_digest: nullable bytea (32 bytes; absent until RESERVED)
  receipt_id: nullable bigint (exact staged_outputs row; absent before S)
  UNIQUE(job_id,attempt_id); existing attempt FK; no second ticket per attempt
ticket_source_bindings:
  ticket_id: uuid; rag_source_id/owner_id/owner_h/authorization_epoch: bigint > 0
  source_type: varchar(8) IN ('FICTION','FILM'); field_policy_version: integer > 0
  approved_fields: text[] (sorted distinct exact allowlist, no arbitrary strings)
  content_revision: char(64) lowercase hex
  description_revision: nullable char(64) lowercase hex (required iff approved)
  PRIMARY KEY(ticket_id,rag_source_id); UNIQUE(ticket_id,owner_id)
  complete manifest source count/edges must equal the immutable scope digest
bounded_metadata:
  ticket_id uuid, state varchar(24), pg_remaining_us bigint >= 0
bounded_receipt:
  ticket_id/job_id/attempt_id/consumer_boot_id uuid, staged_row_id bigint > 0,
  protocol_version smallint = 3, stage_type/output_kind as above,
  input_digest/scope_digest/output_digest bytea (32 bytes)
~~~

UNKNOWN is an operation outcome, not permission to reset ticket state. State
transitions and the limited nonce/receipt linkage are the only mutable ticket
columns; authority/digest/deadline bindings cannot change. Check/FK/unique and
audited trigger/function enforcement must reject malformed/partial states.
The typed_result JSON has exactly input_digest, scope_digest, output_digest and
output_kind (lowercase 64-hex strings plus the fixed kind). Job/attempt/stage/boot
are derived from the ticket. Output must equal J's expected SHA-256 of
UTF8(stage_type + ':' + exact canonical selected payload), preserving the existing
synthetic calculation; input is SHA-256 of those exact payload bytes. v3 changes
the envelope/manifest bindings, not the allowed synthetic stage/kind. Additional
JSON keys/text deny. Wire receipts add only the declared bounded identity fields;
no result content or credential is echoed. Java updates ticket verification only,
never rewrites a worker receipt/worker_receipt flag; preserve V12 writer isolation.

Proposed public API additions under vault.sourceauthority named interface:
SourceAuthorityTransactions and immutable SourceHistorySnapshot as above; extend
existing guarded source projections/authority proof with owner generation and
installation. Existing VaultEntryOperations/VaultSourceGuard implementation joins
only a proven physical unit for participating operations. Fiction/Film expose
fresh bounded DTOs, never repositories/entities; owner services call only Vault
public invalidation contract, not RAG internals. RAG approval DTO adds expected
observed owner generation, E/field-policy version and runtime identity for stale
approval-form CAS; server still proves current eligibility/H under its guards.
An old queued approval cannot undo a later revoke or approve an unseen newer
version. Stale request denies without automatic retry/rebinding. No new frontend
or later-phase API.

Proposed narrow SQL contract:

~~~sql
-- Specification only; schema qualification finalized with the approved ADR.
vault_guard.lock_read(expected_installation uuid,
                      expected_v bigint,
                      expected_control_boot uuid, consumer_ref uuid,
                      source_ids bigint[],
                      expected_h bigint[]) RETURNS void
-- Definer-only: SELECT singleton ... FOR SHARE;
-- compare installation/V AFTER locking; lock H rows FOR SHARE ORDER BY id;
-- compare exact count, identity, lifecycle and H AFTER locks; retain until COMMIT.

vault_guard.begin_mutation(operation_id uuid) RETURNS bigint
-- Trusted owner/RAG Java route only: SELECT singleton ... FOR UPDATE,
-- verify installation; advance V in same business/policy transaction.

vault_guard.invalidate_sources(operation_id uuid, source_ids bigint[],
                                hard_delete boolean) RETURNS void
-- Trusted owner route only, existing exclusive V/unit required;
-- lock sorted H rows FOR UPDATE, advance each H and retain tombstones on delete.
-- hard_delete marks DELETED; Java tombstoneSources passes true, ordinary
-- invalidateSources false. Deleted history cannot become LIVE or be reused.

rag.reserve_execution_v3(ticket uuid, child_credential bytea,
                         permit_digest bytea) RETURNS bounded_metadata
rag.claim_body_v3(ticket uuid, permit bytea, child_credential bytea) RETURNS bounded_metadata
rag.stage_candidate_v3(ticket uuid, child_credential bytea,
                       typed_result jsonb) RETURNS bounded_receipt
-- Each: non-locking lookup of immutable ticket routing fields;
-- lock_read(ticket-owned V/H), attempt FOR UPDATE, ticket FOR UPDATE;
-- reload/recompare immutable routing fields and all scope/PG deadlines;
-- CAS only legal state; fail if missing/changed; exact duplicate rule above.
~~~

No worker-supplied expected V/H is trusted. Initial immutable lookup is not proof;
post-lock reload must match it and the exact complete bindings. Missing/deleted/
aborted row denies. No source payload arrives via these functions. Reject unknown
JSON fields/types/kinds and enforce exact byte/count ceilings before persistence.
G/B/S return only source-free typed metadata. Their caller must physically COMMIT
before ACK, and no helper uses a separate connection to release V early. The
definer route verifies the actual authenticated session_user is rag_worker,
not a request producer flag/current_user alone, and matches the scoped child
credential. Java/runtime-created rows or a forged worker_receipt boolean cannot
establish worker provenance; preserve the existing independent receipt checks.

Worker SQL privilege contract: LOGIN role rag_worker has no owner/superuser/
BYPASSRLS/CREATEROLE/CREATE/TEMP/replication or effective inherited elevation; no
business/auth/owner H/V tables or direct Vault function EXECUTE, no policy,
canonical job, pointer, verification or sealing mutation. Grant only EXECUTE on
the three scoped v3 RAG functions and required schema USAGE. A dedicated NOLOGIN
RAG stage-definer role has only required RAG object privileges and EXECUTE on the
narrow Vault read guard, no business-table access. Vault guard owned by a separate
NOLOGIN role with only its metadata rights. All functions qualify objects, fix a
safe search_path (pg_catalog plus explicit trusted schemas, no writable public/
temp resolution), revoke PUBLIC EXECUTE and audit effective membership/ownership.
Do not assume NOINHERIT alone removes inherited PUBLIC privileges.

Direct staged DML, updatable views, old v2 functions, alternate triggers/cleanup/
judge-equivalent paths must be revoked or converted to V -> H -> attempt -> ticket
order before enabling v3. Deferred checks only recheck held guards. Subtransaction
exception handling cannot preserve a write after losing guards. Worker remains a
restricted producer, never an authority. Transaction-control SQL such as SET
CONSTRAINTS is **not falsely claimed revocable by this function grant model**;
D6 specifies the limitation and post-COMMIT rejection.

Proposed append-only migration sequence (numbers reserved only after approval):
owner history/coordination plus D5 control registry, seed identities as unapproved;
then immutable v3 ticket/receipt/SQL contracts, consumer bindings and effective
privilege revocations; then cutover denial/invalidation and integrity checks.
Re-review concrete V14-and-later filenames after the D5/expiry decision; earlier
draft numbers were provisional, not approved migrations. Never edit V1–V13. Migration runs only with content execution contained,
all consumers positively stopped and old SQL writers drained. New installation
identity on uncertain restore; invalidate old approvals/tickets/pointers. Missing
history or a conflicting installation fails startup. Do not mint approval while
seeding. Rollback does not drop history or enable v2.

Proposed ADR (next available number, not created/accepted here): context includes
the reproduced consumption race and CF1–4; decision records F/H/V/E ownership,
single-host/native trust model, protected entry, Vault-owned D5 control transport/
registry/cutoff side effects, SQL definer edge, A+B V policy and selected D6 semantics; alternatives include durable prepared guards and functional
re-scoping; consequences include synchronous consent renewal/global contention/
unavailable owner writes if child cannot die. After approval synchronize accepted
P17-3, P17-1, owner decision, existing ACTIVE, module named-interface/matrix/package
tree, canonical additive DBML/data inventory and diagram sources. No accepted
artifact is changed by this draft.

Implementation impact to review before authorization: current
vault.entry.VaultSourceGuard/VaultEntryOperations, VaultEntryMutationGuard and
participating Vault/Fiction/Film services and indirect callers; FictionSourceService/
FilmSourceService immutable snapshots/revision encoder and projections; RagSourceAuthority/RagSource/RagWorkerGrantService/
RagJobService/RagEngineClient, graph, finalizer/purge/recovery; append-only SQL;
Python contracts.py/transport.py/main.py/worker.py/db.py plus a one-shot native
consumer and F adapters; maintained Java/Python/native/PG regression fixtures.
Exact private class splitting is implementation review scope, not permission for
a generic transaction/native framework or unrelated frozen-module refactoring.

## 8. D6 — physical COMMIT proof, E2 blocker and explicit alternatives

### 8.1 Time points and authoritative linearization

D = min(original attempt lease, independent service session, ticket expiry).
Equality is expired. All real-time comparisons use PostgreSQL clock_timestamp(),
not transaction-stable now(), Java UTC or Python wall time. V/source lock ordering
does not serialize clock passage.

| Point | Meaning / limitation |
| --- | --- |
| t_validate | Last post-lock SQL authorization/deadline comparison in transaction execution. A pass proves only PG time at that point was < D. |
| t_deferred | Actual execution of a deferred constraint trigger, usually during COMMIT processing; SET CONSTRAINTS IMMEDIATE can move it earlier. It is another executable check, not the final durable instant. |
| t_wal | Commit WAL record reaches configured durable storage. SQL checks cannot atomically exclude a pause between their comparison and this event. A commit timestamp/LSN is not a prospective deadline fence. |
| C / physical outcome | PostgreSQL transaction has committed its durable state and completed server COMMIT processing. For this design require fsync=on and local synchronous_commit=on; client success must not precede required WAL flush. Exact visibility/WAL internals are not treated as an application clock oracle. |
| t_ack | Caller observes COMMIT success/HTTP result. Can be delayed or lost after C; missing ACK is UNKNOWN, not rollback. A later check is a new decision, never a revision of C. |

No equality between those points is assumed. A late ACK alone does not prove late
COMMIT; the adversarial test must stop the server before physical completion and
let independently observed PG time cross D. Even synchronous durability settings
do not upper-bound latency under arbitrary pauses or dishonest storage hardware.
[WAL durability settings](https://www.postgresql.org/docs/18/runtime-config-wal.html)
and [asynchronous commit](https://www.postgresql.org/docs/18/wal-async-commit.html)
distinguish durability from response timing. No runtime setting was changed here.

| Transition | Durable linearization / what it means |
| --- | --- |
| Admission, explicit approval, J | Their own physical C. Q/READY are not grants; approval captures H/E in its own C. |
| G, B | Their own known physical C establishes one reservation/claim; immediate local deadline check still precedes the body receiver. Not a lifetime permit. |
| S | Worker transaction C creates an **untrusted** exact candidate. No Java verification/publication authority follows from existence or wire A. |
| Java verification | Its own physical C creates the **trusted Java VERIFIED transition**. A prior valid check is not proof this C precedes D. |
| Successful seal | Its own C records known lifecycle facts for that attempt. Death/quiescence facts never grant content or extend a lease. Negative cancellation/drain/purge can complete after D. |
| P | Its own physical C activates/publishes the generation. This remains a real trusted publication, not relabelled quarantine. Strict accepted semantics require C < D. |
| Owner N / RAG revoke | Their physical C changes truth/H/E/V and excludes old use. No elapsed-time waiver weakens this ordering. |
| DRAIN | Ledger close C closes new reservations; separate cutoff C proves scoped quiescence, as §9 specifies. Neither is N or P. |
| Read/delivery | Fresh guarded authorization has its own comparison and transaction completion; an HTTP ACK/read does not alter earlier verification/P. No early output. E1 residual also applies between the last read-time check and actual bytes. |

### 8.2 Counterexample and feasibility

Let normal trusted verification or P execute all checks, including its normal
deferred check, at t_check < D. Pause that backend **after its final check and
before its commit record/completion**. Permit time to pass to D, then resume.
The transition commits at C > D. All locks and exact provenance may be correct;
there need be no clock jump, malicious SQL, ambient caller or code defect.

This is permitted by the proposed mechanisms: none conflicts with time passing.
A finite safety margin cannot cover an arbitrarily long pause. For any finite
last-check sequence before C, insert this pause after that sequence. Adding a
second verification transaction repeats the same argument for its own C.
Private top-level adapters, default deferred checks, SET CONSTRAINTS DEFERRED,
timeouts/watchdogs, PREPARE/COMMIT PREPARED, synchronous WAL and afterCompletion
callbacks do not eliminate it. A post-C read or DELETE cannot undo the committed
trusted transition or prove it was authorized at C.

Thus **strict E2 no-after-expiry trusted verification/publication COMMIT is
UNRESOLVED / NOT GUARANTEED under arbitrary pauses**. The same instruction-time
limit applies to zero reads/delivery after D. This is a fundamental timing/model
mismatch, not a native/SQL experiment being deferred. No implementation against
the unchanged strict requirement is recommended. E2/containment is the preferred
owner choice now, preserving the accepted security contract and existing tests.

Enforceable conditional properties remain: fresh gate-time comparison < D,
unchanged source/consent through actual C using F/H/V/E, one-use input, exact
producer/result provenance and post-expiry denial at every later fresh gate.
These do not imply C < D. Native/PG implementation of even these properties is
still NOT_VERIFIED.

### 8.3 E1 — unselected, three explicit requirement relaxations

E1 could provide a useful digest-only runtime **only if the owner separately
accepts all three changes below** and an independent review approves the resulting
contract. It cannot be called a fix satisfying strict E2.

| Exception | Enforceable boundary / security consequence |
| --- | --- |
| E1(a): bounded transfer, C and delivery gates | Fresh B plus fixed monotonic budget, at most 64-KiB work unit per check and no implicit renewal. A thread can be suspended after a valid check, resume/read after D, or previously sent bytes can arrive late. Residual wall time is unbounded under arbitrary suspension; no new operation whose actual gate observes expiry is allowed. |
| E1(b): expired untrusted S quarantine | S may physically commit after D, including forced-immediate trigger timing. Only exact raw-free typed derivative/provenance, never source text. Post-S fresh Java verification begun after that late C sees expiry and denies. Metadata/digests remain sensitive; isolation, durable cleanup and no ordinary success disclosure required. Erasure while services/storage are unavailable has no wall-time guarantee. |
| E1(c): late **trusted Java verification/publication** | Java may validate before D and physically commit VERIFIED or P/active pointer after D. This is explicitly a late trusted durable transition and violates the old invariant. It is not S quarantine. Stored verification attests exact provenance/authority at its successful check, not lease validity at C; P remains actual publication at C. Its existence cannot independently authorize reads, side effects or downstream work. |

Example separating (b)/(c): if S commits late and Java's next gate starts after D,
no verification can pass. If S was timely and Java's own last check is timely,
its own verification or P can still commit late. Fresh post-S verification fixes
only the first case. O5 must explicitly cover the second; Revision 2's two-item
wording is withdrawn.

Under E1 preserve all post-lock/default deferred checks and top-level Java entry;
they reduce ordinary expiry failures but never prove the strict instant. Never
relax the existing lock-wait/normal deferred rollback tests. Add the after-final-
check case as an explicit accepted-requirement-change test only after approval;
until then its counterexample is an acceptance blocker.

E1 requires an exhaustive consumer contract:

| Consumer of durable state | Mandatory rule under E1; no implicit authority from VERIFIED/active |
| --- | --- |
| Owner API result/first response/history/open citation | Fresh ReadOK and PG deadline after actual verification/P C and immediately before delivery; deny expired/revoked/unknown. No source body/count/identifier disclosure merely because P committed. Delayed response has E1(a)'s residual, not continuous delivery protection. |
| Status, progress, rankings/counts | Owner-visible eligibility/success counts use ReadOK; expired markers appear unavailable only within authorized scope. No per-restricted-source metric leakage. Physical late COMMIT counters may exist only as sanitized operational facts, never authorized-success counts. |
| Internal finalizer/scheduler/listener/cache | P requires ExecutionOK including immutable expected V. No direct use/scheduling/event/notification from an active bit or COMMIT callback. New content use requires fresh admission/J/B and independent current authority. No cache of positive proof across units. |
| Worker SQL / wire A | Exact candidate only; cannot mark VERIFIED, P or authorize another body. Lost ACK handled by durable lookup plus fresh proof. |
| Recovery / purge | No automatic republish/reverify/renewal. Inaccessible expired derivatives enter durable pending cleanup; deactivate late pointers and purge transitive derivatives. Cleanup is mitigation, **not retroactive authorization** of late verification/P. |
| Diagnostics, backups, operator SQL | Candidate/verification provenance is sensitive metadata, not anonymized by hashing. No raw dumps/log/cache/scratch. Operator inspection is outside normal owner-output authority; no broad worker grants. |

P's physical publication transition is still trusted state under E1, but its
read/use authorization is intentionally conditional. If the owner requires no
expired trusted state at all, E1(c) must be rejected; later denial cannot satisfy
that requirement. All actual readers/internal consumers must be inventoried and
tested before implementation approval, including future seams without adding
future products.

### 8.4 Smaller alternatives and exact O5 recommendation

| Option | Feasibility / recommendation |
| --- | --- |
| **E2 + containment (preferred now)** | Preserves strict accepted invariant by refusing the unproved enabled path. Does not deliver completed Foundation functionality. No more trigger/TTL refinement can prove it under arbitrary pauses. |
| E1(a+b+c) | Conditional implementable gate semantics; structural source fencing remains. Explicitly relaxes strict expiry for C, sensitive S retention AND durable trusted verification/P. Not approved; actual implementation/operational proofs still required. |
| Candidate-only functional re-scope | If useful enabled Python boundary work is desired without (c), defer durable VERIFIED/P/active pointer entirely and permit only S plus read-only diagnostic proof under approved (a+b). No publication success/end-to-end Foundation acceptance claimed. Needs explicit changed Phase 17 deliverable and independent scope review; not silently substituted for D4. |
| Prepared guards / extra final SQL check / timer / commit timestamp | Cannot satisfy the same strict time invariant; retain locks or observe time but leave a last-action interval. Prepared guards may order source mutations, not absolute expiry. |
| Commit-ordered revocation instead of wall-clock expiry | Can use conflicting F/V transactions to define expiry, but timeout then only requests revocation and may be delayed indefinitely. Another functional change, not preferred and not the accepted wall-clock requirement. |

**Recommended exact O5 wording now:** “I retain strict E2, including no durable
Java VERIFIED/publication COMMIT after the original lease/session deadline. I
approve no expiry exception. Content execution remains contained; that
requirement is unresolved for this protocol under arbitrary pauses. Further
implementation is not authorized to claim it satisfied.”

**If the owner deliberately chooses E1 instead, exact proposed wording:**
“I replace strict expiry for this bounded digest-only Phase 17 profile with
E1(a), E1(b) and E1(c) as defined in Revision 3. I explicitly accept (a) reads,
in-flight transfer and delivery after deadline following a valid bounded-unit
gate, with unbounded elapsed residual under suspension; (b) expired nonpublic
raw-free but sensitive candidate/provenance retention pending verified purge;
and (c) Java VERIFIED and actual P/active-generation transitions physically
committing after deadline when their final validation was earlier. I understand
that later denial/purge does not undo those trusted transitions, that every
read/internal consumer/notification needs its own fresh gate, and that the prior
strict physical-COMMIT invariant is relaxed rather than proven. No NSFW,
deletion, Private, revocation-COMMIT, consent-history, exact-provenance or
single-use guarantee is relaxed. Original tests/history are preserved; affected
FR17-3/7 contract expectations need independent reassessment. This decision
alone authorizes no code, containment removal or finding closure.”

If neither wording is acceptable, commission the explicitly candidate-only
re-scope or a different functional model; do not continue cosmetic expiry design
iterations as though the unchanged strict requirement were almost implemented.

### 8.5 Gate budget and clocks retained from Revision 2

Keep m0-based conservative PG remaining-time calculation: local deadline =
min(all earlier local limits, m0 + max(0, PG_remaining - margin), child F_start+2s).
Never m1+remaining, cross Java/Python monotonic origins, grace or per-phase reset.
B requires at least 250ms of that same original budget. Before/after each blocking
operation and before each <=64-KiB read/hash/send unit, check the deadline/latch.
A failing observed gate aborts/discards, never intentionally starts verification/P;
a gate already passed before a later pause has precisely the exceptions above.

The proposed 50ms margin is unmeasured, not protection against arbitrary PG wall
clock steps or backend delays. Forward/backward progression anomaly at a gate
denies and invalidates runtime; fresh PG time alone cannot detect every unseen
backstep, so trusted clock progression remains an explicit operating assumption.
A backstep is not a renewed lease. Maintain the earliest monotonic limit across
observations; uncertain clock history means denial/reconciliation. Independent
Java/Python wall-clock shifts do not change SQL time decisions. Frozen auth's
recorded 200–411ms host/VM discrepancy remains an FR17-8 environment issue, without
auth source/assertion changes or lucky rerun acceptance.

PostgreSQL [SET CONSTRAINTS](https://www.postgresql.org/docs/18/sql-set-constraints.html)
permits earlier constraint-trigger checks. These reference semantics and the
logical interleaving above are not a newly executed PostgreSQL/runtime proof.

## 9. D5 — Vault-owned DRAIN, quiescence and recovery

### 9.1 Ownership and public boundary

**Vault owns source-consumer coordination**, as a proposed addition to its
sourceauthority named interface. It owns F/H/V, the metadata consumer/drain
registry, native process-handle adapter and a private literal-loopback control
transport. Fiction/Film/Vault participating mutation entry calls that Vault public
facade. RAG calls the same public coordinator for prepare/revoke/recovery; Vault
never calls a RAG service, queries RAG tables, imports RAG internals or interprets
approval/job states. No callback implemented by RAG is injected into Vault.

The Python parent hosts a **Vault-specified source-use supervisor** adapter for
metadata control, separate from the RAG content handler. Hosting it in rag-engine
does not authorize Java Vault-to-RAG feature calls. This is an explicit new
Vault-to-local-process-control transport/storage responsibility requiring O4/ADR/
interface approval; it is not implied by the original owner decision. No new
top-level module/shared framework, broker or scheduler is proposed.

RAG alone maps opaque consumer_ref to canonical job/attempt/ticket, tracks their
active state and owns candidate/verification/generation rows. Vault tracks only
process use of sources, not RAG eligibility or business content.

Proposed narrow public DTO/API specification:

~~~java
record ConsumerRef(UUID installation, UUID controlBoot, UUID consumerId) {}
record ProcessIdentity(long pid, long createdFiletime, UUID consumerBoot) {}
record DrainRef(UUID installation, UUID operationId, UUID controlBoot) {}
record DrainStatus(DrainRef ref, DrainPhase phase, Long committedCutoffV) {}
// Phases: REQUESTED, ADMISSION_CLOSED, STOP_PENDING, CUTOFF_PENDING,
// QUIESCENT, COMPLETION_PENDING, COMPLETE, UNKNOWN.
// Ref/status is not authority; only a manager-owned live handle can guard work.
ConsumerRef SourceUseCoordinator.reserve(UUID correlationId);
DrainRef SourceUseCoordinator.requestDrain(UUID operationId);
DrainStatus SourceUseCoordinator.inspect(DrainRef ref);
DrainStatus SourceUseCoordinator.reconcile(DrainRef ref);
DrainStatus SourceUseCoordinator.recover(UUID operationId);
<T> MutationOutcome<T> SourceAuthorityTransactions.mutate(
    Function<SourceAuthorityUnit, T> work);
~~~

reserve runs before child launch; no source ID/text/job state in Vault DTOs.
Internal supervisor-only binding records ProcessIdentity before READY. Mutation
facade performs the drain prelude automatically for a standalone call; a supported
registered exclusive outer unit already owns its drain/F/V and nested calls join
without a second drain. Reject unsupported ambient/shared/suspended entry BEFORE
registry/control side effects. Owner code receives only SourceAuthorityUnit;
it cannot forge QUIESCENT or reopen the supervisor. MutationOutcome distinguishes
business COMMITTED/ROLLED_BACK/UNKNOWN from drain COMPLETE/PENDING/UNKNOWN;
post-COMMIT control failure cannot be reported as business rollback.

Private control API (no source bytes, response <=8 KiB):
PREPARE(ConsumerRef, fixed foundation profile), DRAIN(DrainRef),
INSPECT(DrainRef), COMPLETE(DrainRef, committed cutoff reference).
Use independent service credential, installation/control boot/operation binding
and replay checks; never owner JWT/PIN or RAG worker SQL credentials. Fixed
literal loopback host/port/protocol, no arbitrary URL/redirect/proxy. A supervisor
cannot choose source scope or reopen because a timeout/old credential expired.
Wire gate-closed ACK is not a quiescence, mutation or purge ACK.

### 9.2 Registry and exact tracking

Proposed **Vault-owned metadata**, no implementation: source_use_control singleton
(installation, control_boot UUID, admission OPEN/CLOSED, monotonic control_sequence
bigint); source_consumer records (opaque consumer_ref, reservation sequence,
supervisor boot, known ProcessIdentity, phase RESERVED/SPAWNING/BOUND/DEAD/
UNKNOWN, process-proof reference); source_drains (operation UUID, boot/sequence,
phase, immutable covered reservation cutoff, committed V cutoff nullable, outcome).
Unique control boot/consumer identity and operation key; no domain/RAG foreign
key, title/path/payload, raw credential or copied job engine. Registry is control
evidence; absence/DEAD flag alone is never positive death. A death certificate
must bind the verified signalled native process object/creation identity and
installation/boot; a parent-reported phase cannot manufacture that certificate.

Two immutable Vault-owned outcome relations avoid relying on V's overwritten
last_mutation_id: source_use_cutoffs(operation_id UUID PK, control_boot UUID,
covered_sequence bigint, cutoff_v bigint UNIQUE, transaction_id xid8), inserted
in the cutoff transaction; and source_use_mutation_outcomes(operation_id UUID PK,
mutation_id UUID UNIQUE, transaction_id xid8), inserted at entry to the subsequent
business unit. They become visible only if that physical unit commits, and roll
back with it. No business result/payload is stored. These are bounded mechanical
COMMIT evidence, not RAG jobs or an outbox. Do not add a FK into L that locks its
registry rows while V is held. Reserve the unique outcome row immediately after
V, before graph/source locks; no earlier-order lock at transaction exit.

On unknown outcome obtain the same exclusive F/V recovery guard before inspecting
the immutable operation marker: acquisition proves the earlier holder resolved.
Marker present proves that unit committed; absence only AFTER this barrier proves
rollback for these non-prepared transactions. Never replay a business callback
or infer its success from a later larger V/cutoff. Without a conclusive barrier
leave UNKNOWN. L phases mirror those facts; they are not the COMMIT authority.

Authoritative control_boot also belongs on the V singleton, changed only under
exclusive F/V during closed-gate recovery. L's boot is a control-plane mirror;
misalignment blocks reserve/startup. The private Vault SQL read guard compares
the ticket's control boot and immutable consumer identity after locking V, so
worker SQL cannot substitute a new boot/ref or trust a registry flag as a grant.
SourceOK also compares approved_control_boot; explicit approval captures it under
F/V. Recovery rotating this owner boot therefore denies old consent without a
Vault-to-RAG policy write, even before RAG has rotated its own runtime instance.

reconcile resolves control/physical outcomes only; it never replays a business
callback or renews consent. recover starts closed, resolves ALL old process/SQL/
operation records and obtains exclusive F/V before rotating the control boot.
Unknown original outcomes remain pending; recovery cannot erase them to reopen.
It returns scoped control readiness, not RAG publication/purge acceptance. Those
public operations reject ambient source transactions before any side effect.

A short **registry-only SQL lane L** serializes control singleton -> operation/
consumer keys in ascending order. It never takes V, source, RAG or F locks and
never waits on network/processes/F while holding a row. Every L transaction ends
before those actions. It may persist metadata before a source business unit, not
a hidden business commit. All existing owner truth/H/E changes remain atomic in
their original physical unit. New schemas, the control-only lane and the explicit
cutoff side effect below require owner approval; no silent change to D4 entry.

PREPARE reservation acquires the L gate, verifies current installation/boot and
OPEN, reserves the sole active slot and commits before spawn. Parent serialization
binds all spawn attempts to that reservation; record SPAWNING before creating the
child. Child cannot acquire F/READY/body until actual process identity is registered
and binding COMMIT is known. A crash in the spawn-to-bind interval is UNKNOWN,
not “never spawned”; startup/recovery remains closed until positive resolution.
After DRAIN closes new reservations, every earlier RESERVED/SPAWNING/BOUND entry
is included in its finite covered set, even a delayed launch/READY. This prevents
a lost starting child from slipping behind an empty active-worker counter.

Java native coordinator retains/opens and verifies actual process object handles
and creation identity; parent reports supplement, never replace, those handles.
Wait for actual signalled termination; no PID-name scan, health bool, successful
TerminateProcess return or duplicate attempt proves death. No descendants; inspect
the controlled Job Object/process tree. An unspawned reservation needs a proven
terminal no-spawn CAS plus supervisor launch exclusion, not merely a missing PID.
If lost handles/unknown identity cannot be recovered conclusively, stay UNKNOWN.

RAG records active attempts and their consumer_ref/control boot; SQL connections
record source-free backend identity/transaction observations for diagnosis.
Observations/counters are not quiescence proof. **Authoritative pending-writer
tracking is V lock participation:** every permitted G/B/S and candidate route
holds V FOR SHARE through physical completion, before H/attempt locks. SQL waiting
to obtain V has not yet authority to write. Obtaining exclusive V drains existing
holders; advancing and COMMITting V makes every queued/late old ticket fail its
post-lock immutable expected_V check. No need for Vault to enumerate or mark RAG
attempts. A missing legacy-route fence blocks the capability.

### 9.3 Exact DRAIN and owner mutation protocol

1. requestDrain creates/reuses a UUID-bound record in L and commits admission
   CLOSED with a covered reservation sequence. This C closes authoritative new
   reservations across controllers. A duplicate returns the same operation, not
   another child/lease or a global completion boolean. Parent DRAIN ACK separately
   confirms its gate is closed and late pre-close starts are tracked.
2. Outside all DB transactions, cancel the covered consumers and wait boundedly
   for verified death/no-spawn. Grace 250ms, termination request to bound handle,
   at most 1s wait per controller request; failed kill/unknown => pending/busy.
   No new reservation/launch can bypass the closed gate/boot binding. New parent
   always starts closed. Already reserved races remain in the covered set.
3. Acquire global F exclusive with bounded wait, after positive consumer proof.
   Retain its manager-owned handle through the following cutoff and business
   units. Failure/pending native release => SOURCE_BUSY; never force unlock.
4. Start a short **cutoff-only** writable physical transaction: V FOR UPDATE,
   fresh installation/boot checks, increment V and insert immutable cutoff record;
   no business/graph/RAG locks. COMMIT while holding F. Only known physical C
   proves existing SQL holders ended and late old writers are durably fenced.
   Unknown cutoff COMMIT => CUTOFF_PENDING/UNKNOWN, retain/quarantine F while
   possible, no quiescence ACK or business work. Fresh locked V/cutoff lookup
   reconciles; operation UUID is idempotent, never compare an uncommitted value.
5. Vault can now issue a **scoped QUIESCENT receipt**: covered refs/control boot,
   positive process-proof identities and durably committed V cutoff. Keep it
   bound to the live exclusive-F handle for subsequent work. It proves no covered
   consumer can read and no old-V writer can later persist successfully; it does
   not mean all RAG jobs are CANCELLED, source revoked, data erased or the entire
   runtime forever idle. New later-boot work is outside this historical receipt.
6. Under the SAME exclusive F, begin the ordinary protected owner/RAG transaction,
   obtain V FOR UPDATE first, advance it and insert its outcome marker, then the established sorted source/
   H/policy/attempt order. Owner alone changes business truth/H; RAG alone changes
   consent E/leases/pointers. Its own physical N COMMIT acknowledges that mutation.
   For RAG purge, cancel/tombstone own attempts and delete/check derivatives under
   attempt guards; PURGE_COMPLETE needs its own known C plus this scoped cutoff/
   death proof and verified erasure. Vault never marks RAG purge complete.
7. After physical business outcome and all SQL locks release, persist completion
   in L and send idempotent COMPLETE outside DB transactions. Reopen only if every
   outstanding drain resolved, F has been safely released and current boot/
   installation match. COMPLETE from one controller cannot reopen another's drain.
   Lost completion/control ACK leaves admission CLOSED; inspect/reconcile, no
   timer-based reopening. An owner N may be known committed while control completion
   remains pending: report those separate facts, never invent failed business work.

The cutoff is a deliberate independent **revocation of execution opportunities**
before the owner command. It can invalidate unrelated in-flight tickets even if
the later business transaction rolls back. H/business truth/E roll back normally;
unrelated published results are unaffected solely by V. This cancellation side
effect is explicitly part of O3/O4, not an independently committed domain write.
A rollback does not authorize reusing an old ticket. Fresh admission may use
unchanged consent after reopening.

L completion cannot erase unresolved records. Limit outstanding drains to 16,
one active consumer/spawn and the existing 16 metadata queue entries. Terminal
registry retention is bounded by the measured control metadata quota; when safe
anti-replay compaction cannot be proven, deny new control operations and perform
a fully quiescent boot reconciliation rather than discarding unknown records.
No TTL proves terminality. Disk/registry quota failure keeps the gate closed.

### 9.4 Lock order, failures and recovery

There is no L-row -> F/V wait edge: finish L before control/process/F waits.
Source units retain the accepted F -> V -> graph -> sorted Vault -> H -> owner/
policy -> attempt -> ticket -> generation/stage/run order, with only their private
unique outcome row reserved immediately after V. Cutoff uses F -> V -> cutoff
row. Workers use V -> H -> attempt -> ticket -> stage, never L or business.
No SQL unit calls/waits on Python/native death. Completion L runs after business
SQL ends; it never holds a row while acquiring F. Registered outer writes cannot
repeat the prelude while retaining source locks. No shared-to-exclusive upgrade.
This separates resource lanes without a Vault-to-RAG callback cycle.

Proposed D5 controller deadline is 10s total, inclusive of L/control, process waits,
F, cutoff and owner unit; lower caller deadlines win. L/control requests use at
most 250ms each; death wait at most 1s; F acquisition at most min(3s, remaining).
SQL/network/statement/transaction limits use min(existing 3s/5s/10s ceilings,
remaining request budget), never 0 as an accidental unlimited GUC. A finite
deadline guarantees bounded caller outcome, not actual backend/lock release.
Coordinator executor max 2 active operations with the 16-record admission cap;
at most 3 automatic metadata reconciliation probes per request, no business retry.
Pending guards are manager-owned records/handles, not an unbounded blocked caller
or a new thread per retry. Outage recovery is finite per scheduled pass.

All waits/requests have the smaller configured remaining operation timeout;
unknown/stalled COMMIT can outlive that client timeout. Manager-owned F/registry
slots then remain pending for recovery, not leaked into a retry or forced open.
Source-control cutoff/negative cleanup may complete after work-lease expiry:
they remove authority rather than grant it. This does not evade E2 for verification/P.

| Failure/interleaving | Required outcome |
| --- | --- |
| Parent/control unavailable or DB down before closure proof | New reservations fail closed; bounded SOURCE_BUSY/UNKNOWN. A cancellation signal alone never produces quiescence. |
| Two controllers/drains, duplicate or reordered ACK/COMPLETE | Same-operation idempotency and all-outstanding-record gate; no premature reopen or false global QUIESCENT. |
| Child alive/failed termination/native unlock delayed | Keep slot/gate/F pending. Owner exclusion cannot commit first then let that child consume. Finite request failure, no eventual-success promise. |
| Child dies but S backend still holds V | Exclusive cutoff waits or times out; no QUIESCENT until backend resolves and cutoff C known. Queued old S rejects after cutoff C. |
| Cutoff COMMIT unknown / Java dies | Closed admission and operation ID survive; pending V lock orders recovery. New coordinator obtains F/V, determines committed cutoff, never infers rollback from no ACK. |
| Owner rollback after known cutoff | Business/H/E unchanged; old execution tickets remain invalidated by cutoff. Complete control operation explicitly; no replay. |
| Owner N unknown / crash during COMMIT | Source F may release on process death, but N's V orders new checks. Gate remains closed; reconcile physical N outcome before reopen. |
| Restart/split parent/unknown spawn | Single authoritative installation/control boot; new parent closed, no second launcher. Retain unresolved records, recover handles or stay unavailable. New execution waits exclusive F/V and reconciliation. |
| Late receipt/purge failure | RAG fresh checks deny obsolete work and retain PURGE_PENDING; Vault receipt does not assert RAG state/erasure. |

Source N committed first -> H/V or E/V mismatch denies fresh J/B and old consent
even after an allowed round trip. Consumer first -> its own continuous F prevents
N COMMIT until positive death. Child death during S -> V orders cutoff/N after S
or rejects queued old S. Owner Java death during pending N -> exclusive V orders
new J/B after actual N resolution. These preserve D1–D4's accepted conditional
proof, not an elapsed-time COMMIT guarantee. No lost notification or counter can
replace the fences, positive death or durable cutoff.

On recovery/restore, close admission, positively resolve old consumers, exclusive
F/V/cutoff and pending SQL, then rotate runtime/control boot and invalidate old
tickets/approvals/pointers under existing recovery rules. No auto-publish of old S
or restoration of H/V as fresh consent. Unknown process/transaction/installation
stays unavailable. Raw source remains only one bounded Java payload and child RAM;
no parent copy/cache/scratch/logs. Paging/dump/forensic zeroization is not proved.

## 10. Proposed finite limits and meaningful success thresholds

Numbers below are **proposed, unmeasured**, not accepted SLAs. Native Windows 11,
one kernel/installation, native Java 25/Python, protected local NTFS control root,
PG 18.6 and no off-host writers is the preferred supported profile. A second host,
Linux substitute without equivalent F, network share or unverifiable writer
disables content execution. Read-only/non-RAG operations remain separately scoped.
Global F/V may delay unrelated eligible writes; H avoids global consent renewal.

| Class | Proposed limit / acceptance |
| --- | --- |
| Hard input/state caps | 100 distinct sources, depth 5, 1 MiB selected UTF-8 bytes, 10,000 graph edges; existing 1,000 catalog/retained jobs, 256 jobs/source, 16 pending, fixed existing attempt/stage caps. One active consumer; no descendants. Above limit denies. |
| Source-free protocol caps | Prepare 2 KiB; receipt/control response 8 KiB. No raw source control/log body. Bounded canonical manifest under existing request caps. |
| Startup/queue | Startup max 5s before F; consumes the single slot. Queue metadata only, max age 30s; expired queue item denies, never implies another child stopped. |
| Active F/operation | Single fixed 2s abort threshold starting at actual child F acquisition, including Q/J/G/B/transfer/digest/S and remaining consumer lifetime. No reset. It is not an enforceable release/death guarantee under arbitrary suspension. |
| DB | Preserve configured ceilings lock 3s / statement 5s / transaction 10s, each reduced to remaining operation/lease budget; no resetting after lock waits. Proposed worker pool max 2, worker role CONNECTION LIMIT 3, verify actual effective pool/role limits. Watchdog only aborts; pending COMMIT is resolved by guards/recovery. These timeouts do not prove a physically stalled backend ends by that instant or prevent a malicious client from changing user-settable timeout GUCs; stuck writer remains pending/busy. |
| Control/death | Control response target 250ms; cooperative stop grace 250ms, then terminate bound child, wait at most 1s per controller request for positive death. Failure remains busy/pending; finite request completion is distinct from successful revoke. |
| Memory/process | Enforce one active child/no descendants and proposed Windows Job Object private-commit caps 128 MiB child / 256 MiB combined Python process tree, subject to native measured proof. 16 MiB incremental content-buffer target; measure parent/interpreter baseline and whole worker tree. JVM attributable heap/private bytes measured separately. |
| Disk/CPU | Zero source scratch and GPU/model allocations; only metadata fence/control artifacts. Record CPU time/utilization, JVM/worker working sets/private commit, handle/process/DB-connection counts, zero leaked children/backend writers after finite runs. Wall deadline alone is not a hard CPU quota. |

Preferred normal-load acceptance: on the documented reference Windows machine,
5 warmups then at least 100 measured single-attempt successes for each small,
100-source/depth-5 and near-1-MiB fixture; **100% expected-authorized success, zero
security violations**, active-F p95 <=1.5s/max <=2s, cold startup p95 <=3s/max <=5s,
queue-inclusive isolated completion p95 <=6s/max <=8s, cancellation-response p95
<=250ms. Include cold starts and process startup, not prewarmed digest-only timing.
Threshold miss is a blocker for this proposed profile, not an excuse to classify
denial as fast success. Select lower supported cap only through explicit review.

For 16-pending saturation, record all 16 outcomes per wave, startup/queue latency,
expiry/busy/reject counts and useful successes; require at least **12 of 16 (75%)**
genuine J-through-P successes per fault-free reference-machine wave and no
starvation of admitted head work. This is an explicit overload acceptance floor,
**not** normal-load success acceptance or a worst-case availability promise.
Thirty-second queue age and 5s-per-child startup mean worst-case all-16 success
is not promised. If owner requires all 16 complete,
revise the measured profile/queue cap before approval, never extend a grant.
Run at least 100 completed attempts per workload/concurrency cell (denials counted
separately); report p50/p95/max/raw samples and confidence/sample limitations.
Measure revocation/purge/recovery in normal and injected contention cases with one/
two writers, F/V/row wait/hold, query counts/batching and disk. Source-unrelated
mutation invalidates tickets but must not erase unrelated consent.

Owner must accept Windows-only support, global write contention and possibly
unbounded safety-preserving unavailability if a consumer cannot die. Neither
read/write lock fairness nor a watchdog promises eventual success. If measured
useful work cannot meet these thresholds, retain containment and reconsider the
architecture/budgets through a new design review. No benchmark ran in this revision.


H/tombstones are one row per eligible owning identity ever created, not bounded
by 1,000 live RAG catalog entries. Evaluate 1k/10k/100k lifetime identities (live
and deleted), indexed PK lookups of at most 100 selected IDs, no full-history scan
at a gate, table/index/WAL size, seed time and latency. Do not claim constant
storage or discard tombstones to meet a quota. Proposed high-water evaluation:
512 MiB for owner control/history relations plus indexes, and at least 1 GiB
independently verified free space on the actual PostgreSQL data volume, subject
to O6 review. These are unmeasured admission thresholds, not disk reservations.

When verified headroom/quota is unavailable, deny new eligible identity/control
allocation and keep admission closed as necessary; never partially commit a
business mutation without history. Actual ENOSPC rolls back business/H/V together
and may make a requested NSFW write unavailable: acknowledge no exclusion COMMIT
that did not occur. No guarantee survives arbitrary disk exhaustion. Terminal
control compaction must preserve boot/cutoff anti-replay and unknown records;
otherwise fail closed for quiescent maintenance. No owner-history pruning/reset.

Measure L/parent DRAIN/cutoff/owner latency and two-controller contention separately.
Report V churn's discarded in-flight work/retry cost. A+B means no unrelated
published rebuild solely for a V increment. Include worker CPU/connection and
native handle/process ceilings, low-disk/record-cap outcomes and all raw samples.

## 11. Deterministic adversarial acceptance matrix

All cases are **proposed, NOT_VERIFIED**, not new passing tests. Use real public
owner APIs, native separate processes and actual restricted PG roles/transactions;
bounded latches/barriers after precise real checks, pg_blocking_pids/transaction
identity and process-handle evidence, no arbitrary sleeps. Expiry barriers release
when PG clock reaches a fixture deadline within a finite test timeout; exercise
equality explicitly. Always clean up threads/processes/connections in finally.
Keep original R1–R8 identities and every previously closed security regression.

| ID / finding | Controlled interleaving | Required assertion |
| --- | --- | --- |
| C01 / CF-1 R2 | Allowed -> NSFW -> Allowed, both events dropped, no RAG read in between; same selected hashes | H advances twice. Old consent denies fresh admission AND J, ticket, active/history read; explicit fresh approval permits only a new attempt. Fiction and Film, each participating API. |
| C02 / CF-1 | Content h->h2->h, description/assignment removal-readd, trash/restore, delete/recreate ID; child B round trips in A closure | Same H/consent denial; tombstone prevents reuse, missing H denies. Parent full closure cannot hide changed child. No implicit parent/child approval. Prove actual FK/tombstone preservation, duplicate ID/type conflict and seed/restore identity checks. |
| C03 / CF-1 | Two unrelated B mutations; rollback owner A mutation; lost events; process restart/DB restore | B changes do not invalidate A consent but invalidate A's old V ticket. Rollback leaves A H/V/truth unchanged. Restart/restore rejects old approval instance; never resets/rebinds H. Unchanged-V rollback assertions scope to the business unit; the explicit D5 cancellation cutoff remains committed. C27 covers published A. |
| C04 / CF-1 | Approval races NSFW/content change; stale observed-H approval form; mutation begins after approval locks | Total order: mutation-first denies stale approval; approval-first captures old H and subsequent mutation invalidates it. No approve-by-background-reconciliation. After explicit renewal old S/ticket/boot replay still denies; absent/wrong runtime and stale approval epoch fail CAS. |
| C05 / CF-2 | Dispatch with clean/dirty ambient transaction; owner entry from unregistered outer; cached/dirty managed source | Reject before prepare/spawn/body/entity mutation/flush. Registered clean outer takes F/V before SQL, same physical identity through COMMIT. |
| C06 / CF-2 | Same-mode join, opposite mode/upgrade, REQUIRES_NEW/suspend, NESTED/savepoint/inner caught failure, outer rollback/unknown outcome | Allowed join uses one physical TX; prohibited paths reject/rollback whole unit. F not released by service return/savepoint/beforeCompletion; unknown not success. No orphan body/ticket. Observe actual proxy/self-invocation and resource-holder/DB transaction identity; no early guard release. |
| C07 / CF-2 | Recreate R-held -> F and graph-held -> V inversions, public/indirect/bulk/import/cleanup/reference-projection paths | Entry rejects rather than waiting on F after DB locks; complete participation inventory, no OS/SQL cycle. Bounded deadlock/timeout failure remains fail-closed. Include standalone Vault/Fiction/Film -> Vault DRAIN, shared reference fan-out and above-bound atomic rejection. |
| C08 / FR17-5 R2 | Reviewer barrier J COMMIT before body, owner NSFW/trash/revoke/deletion in both orders; child waiting for input; pause between Q and J | Writer-first denies fresh J/B with zero T/C. Consumer-first owner cannot COMMIT until actual child death; no C after earlier exclusion COMMIT. Include stale cached eligibility; observe no content hashing/network under Q/J/P DB guards and mandatory fresh H proof after Q. |
| C09 / R5 R8 | New child, restrictive quoted metadata, field reduction/PRIVATE/classification while dispatch waits | Complete H/E/closure/allowlist proof; no new transitive/metadata authorization; old tickets/candidates deny/purge. Preserve closed FR17-1/2/4/11 regressions. |
| C10 / CF-3 | Actual-role V FOR SHARE versus owner UPDATE/FOR UPDATE, both orders; negative fixture plain SELECT/KEY SHARE | Genuine wait/COMMIT order and fresh post-lock comparison. Negative fixture must reproduce failure, not be mistaken for production proof. H/attempt guards same connection. |
| C11 / CF-3 | Concurrent/serial G, G COMMIT unknown, duplicate B/stream, delayed body, child retry/restart | One durable reservation and one body claim/consumer; no second permit, body retry or lease reset. Unknown aborts consumption; fresh attempt requires fresh proof. Wire trace: pre-handler prefetch, automatic/duplicate Continue, Expect timeout fallback, redirects/retries/chunked/unsolicited bytes and B post-COMMIT pause. Trusted producer boundary: arbitrary unsolicited local send cannot be excluded from every kernel buffer. |
| C12 / CF-3 | Concurrent/delayed S, lost/forged A, S COMMIT unknown, differing replay result or terminal ticket | One immutable exact receipt; metadata recovery after writer drain, no overwrite/renewal/early success. Wire ACK alone never verifies. |
| C13 / CF-3 R1 | Child dies during server-side S COMMIT, pause after last SQL guard; N/purge races in both orders | Shared V/attempt locks survive client death to physical end. N first -> old S denies; S first -> purge deletes it; zero resurfacing after PURGE_COMPLETE. |
| C14 / CF-2/3 | Owner Java dies after submitting N COMMIT before PG resolves; new child/J/G/B waits | F release alone cannot authorize; exclusive V blocks until N resolves, then fresh H/V sees COMMIT/rollback. Symmetric crash covered, no cached pre-wait proof. |
| C15 / CF-4 | Delay G before COMMIT/response, B before/after COMMIT, first receive and each 64-KiB unit independently past D; session vs lease/equality | Expired delayed permit rejected before application body read; fixed m0-based deadline never reset. Resume after pause checks before next unit. Record E1 instruction residual explicitly, never assert continuous expiry. |
| C16 / CF-4 FR17-7 | Original lock-wait/outer COMMIT/default deferred expiry and effective-role SET CONSTRAINTS IMMEDIATE before/after S | Preserve original default-path rollback assertions exactly. Late S must fail fresh subsequent verification. That does not prove Java's own C timely; C25 covers that distinct blocker. Any E1 expectation needs prior explicit approval. |
| C17 / CF-4 | Shift Java/Python wall clocks ±5min, PG clock progression anomaly, delayed PG response, pause after final SQL clock check | PG authoritative; elapsed request counted from m0, no per-phase renewal. Anomaly denies. Explicitly observe/document final-check-to-WAL/CPU residual; no fabricated strict-instant proof. |
| C18 / D1 D5 | Native cross-process/session locks, pending acquisition, parent/child death, inheritance, file/root replacement/reparse/hardlink/ACL mismatch | One stable F identity, READY only after acquisition; death handle evidence, no manual unlock-and-live path or leaked handle. Unsafe identity disables. Live external files stay disabled. Two Java controllers/sessions, same-process handles, already-open conflicts, cancelled pending overlapped acquisition and every thread/handle lifetime. |
| C19 / D5 R4 | Failed terminate, paused unkillable child, controller outage, fake health/death/duplicate cancel, orphan startup; writer starvation/drain | Busy/unknown/pending, slot not reused, no false purge/revoke ack. New runtime waits exclusive F/V and old writer drain; never recreates F to bypass orphan. C26 covers registry/spawn/drain API and cutoff COMMIT; reopening requires all outstanding operations and known outcomes. |
| C20 / CF-3 R1 R6 | Worker actual grants/inheritance, business/auth/H/V/direct Vault function, stage DML/views/legacy v2, policy/verify/pointers, CREATE/TEMP/SET ROLE | Only scoped v3 functions succeed; no bypass/forged worker provenance or receipt verification. All alternate SQL routes audited V-first or inaccessible. Actual session_user/definer chain, PUBLIC/membership, cross-ticket credentials, malformed/duplicate H arrays and untrusted search_path. |
| C21 / R1–R8 | Revoke, cancellation, failed erase, synthetic derived/history/judge artifacts and late SQL against recovery/purge | Fresh H/E denies immediately after exclusion COMMIT regardless events; transitive purge, bounded retries, no unauthorized persistence/log/scratch or false COMPLETE. No later-phase product implementation. Sensitive hashes/IDs remain derivatives; late Java markers, status/counters/listeners and outage cleanup follow §8, never bit-only authority. |
| C22 / FR17-9 | Small, 100/depth-5, near-1-MiB, 16-pending waves, one/two writers, below/at/over caps, cold startup | Section 10 meaningful success counts and limits; all latency/resource/query/raw sample evidence, denials separate. No digest-only/success-disabled substitute. Include lifetime H/control quota/low-disk/seed metrics and V discarded-in-flight cost; no unrelated published rebuild. |
| C23 / FR17-10 | Real native supported J->G->B->T/C->S->verified A->death->P, denial/recovery with external IPv4/IPv6/DNS/proxy blocked | Authorized Windows OS egress enforcement plus connection-attempt capture, empty runtime caches/pinned dependencies/licenses; actual successes and zero external leaks. Linux without F/disabled smoke is not equivalent. |
| C24 / FR17-8 | Focused maintained cases, clean full Java/Python, architecture, Ruff/mypy, repository/source/log/schema checks | Exact commands/environment/counts/exits including failures; no skips/weakened assertions/retry-until-green. Diagnose clock flake. Every unexecuted runtime guarantee NOT_VERIFIED. Trace R2-1/2/3 to C25–C28 and approved requirement changes; no abstract-model runtime proof. |
| C25 / CF-4 R2-1 | S timely; verification and P separately paused on the server after ALL final/deferred deadline checks but before WAL/physical completion; independent PG observer crosses D, resume | Observe actual late durable VERIFIED and P/active state, not just denied HTTP. Strict E2 fails and containment remains. Only approved E1(c) changes the requirement; read denial cannot retroactively authorize C. Also delay post-C ACK before first response. |
| C26 / D5 R2-2 | Standalone and registered owner calls, two controllers/drain IDs, delayed SPAWNING/BIND, missing handle, parent/DB outage, cap/full disk, cutoff/owner COMMIT unknown/rollback | Trace owner -> Vault-only API, L complete before F/V wait, no RAG callback/table dependency. Positive death/no-spawn + exclusive V cutoff C before QUIESCENT; late old SQL rejects. Other COMPLETE cannot reopen; failed command retains explicit cutoff but no business/H change. |
| C27 / V R2-3 | B unrelated V change after A ticket/S/verification and separately AFTER A P, before D; B-only graph change then A-child change | Pre-P A work invalid and cannot rebind/promote. Post-P A still ReadOK without new ticket/rebuild if H/E/closure/expiry valid. A/child/targeted cancel changes deny even without historical V comparison. Provenance V immutable. |
| C28 / E1 conditional consumer audit | Only if E1 approved: late Java transition then owner status/count/history/cache/listener/internal scheduler/recovery paths; post-C and pre-response pauses | No bit-only authority or expired-result side effect/output. Late trusted transition recorded as contract exception, not “never happened.” Next expired gate denies; residual after passed send/read gate remains explicit. No continuous-delivery PASS. |

Native/PG tests must use finite process-level deadlines and positive cleanup proof,
not leave pytest/Maven/worker processes running. No future fixture can replace an
original closed counterexample with a mocked success. A contract choice requiring
a changed expected result is separately reviewed as a requirement change first.

## 12. Finding disposition and exact owner decisions

| Finding | Independent baseline / Revision 3 response | Remaining condition |
| --- | --- | --- |
| P17-CF-1 High | **RESOLVED IN DESIGN**, preserved: atomic H, explicit consent, no lost-event rebind | Actual mutation/atomicity proof NOT_VERIFIED; control cutoff never resets H/consent |
| P17-CF-2 High | **RESOLVED IN DESIGN**, preserved: protected physical entry/mode/ambient rules | Actual caller/manager proof NOT_VERIFIED; D5 prelude explicitly separate metadata, not business work |
| P17-CF-3 Medium | **RESOLVED IN DESIGN**, preserved: V modes/order, immutable ticket/G/B/S/provenance | Actual effective roles/routes/locks NOT_VERIFIED |
| P17-CF-4 High / R2-1 | **PARTIALLY RESOLVED; strict invariant UNRESOLVED**. Full last-check-to-C counterexample and E1 consequences explicit | Recommend E2/containment. O5 requires an owner-level requirement decision, not another trigger |
| P17-R2-2 / D5 | Proposed Vault coordination API/registry, positive death and committed cutoff | New owner API/schema/transport scope approval plus C26 real proof; independent disposition pending |
| P17-R2-3 / V | **A+B, not C**: immutable ticket V pre-P; fresh current V plus per-source predicate post-P | Explicit O4 choice and C27; no unrelated published rebuild |

| Decision | Revision 3 disposition |
| --- | --- |
| D1 | **ACCEPTABLE AS PROPOSED**, independent baseline preserved; native proof pending |
| D2 | **ACCEPTABLE AS PROPOSED**, Q/J and no content network/digest under DB guards preserved |
| D3 | **ACCEPTABLE AS PROPOSED**, H/V/E preserved; historical V read ambiguity clarified A+B |
| D4 | **ACCEPTABLE AS PROPOSED**, protected source unit/CAS preserved; D5 control-only prelude/cutoff explicitly scoped |
| D5 | **REVISED FOR INDEPENDENT REVIEW**: §9 ownership/DTOs/death/cutoff/recovery, not self-accepted |
| D6 | **REQUIRES OWNER DECISION / STRICT E2 BLOCKED**: no proof of physical C<D under arbitrary pauses |

No implementation finding is closed. CF-1–3 dispositions are attributed to the
independent re-review, not implementation acceptance.

| Owner decision | Preferred choice / technical justification | Exact proposed wording |
| --- | --- | --- |
| O1: history/schema | H/V/tombstones retain missing information without global consent renewal | “I approve H/V history principles and retained identity without reset/reuse/automatic consent seeding. Concrete additive schema/API needs scoped approval; this is not implementation authorization.” |
| O2: renewal | All relevant successful commands advance H, including reversal/no-op | “Only explicit current H/E/runtime consent renews. No notification/retry/reconcile rebinds. Shared projection inputs participate atomically or remain disabled; old tickets cannot inherit reapproval.” |
| O3: caller/transaction scope | Protected outer entry and audited participants; cancellation side effect explicit | “For the audited participant list I approve F-before-source-TX/V-before-work and physical same-mode joins; unsupported ambient/suspend/upgrade/savepoints reject before prelude. Business/H/E remain atomic. I separately accept D5's explicit execution cutoff even if the later owner command fails; no unlisted caller refactor or hidden business COMMIT.” |
| O4: DRAIN/platform/V | Vault control capability avoids reverse feature dependency; A+B avoids needless published rebuild | “I select §9's Vault-owned coordinator/registry/control transport and committed cutoff, RAG retaining jobs/purge. I approve that new metadata/runtime edge subject to architecture review. I accept one Windows host, potentially indefinite safe unavailability, no forced unlock/fairness promise, and A+B V invalidation: all old tickets and unpublished work invalid; unrelated published results use fresh H/E/closure authority without historical V equality.” |
| O5: expiry | **Retain E2/containment**, preserving strict accepted security | “I retain strict E2, including no durable Java VERIFIED/publication COMMIT after the original lease/session deadline. I approve no expiry exception. Content execution remains contained; that requirement is unresolved for this protocol under arbitrary pauses. Further implementation is not authorized to claim it satisfied.” Full unselected E1(a+b+c) and smaller functional re-scope are in §8.4. Approval of (a+b) alone cannot permit late Java state. |
| O6: resources | Existing useful success gates plus lifetime H/control/contending transactions | “Caps/100% normal and >=12/16 fault-free-wave success are measurement gates, not achieved performance. Measure lifetime H at 1k/10k/100k, control/headroom, L/F/V waits and discarded work. No tombstone pruning, denial-as-success, dropped failures or extension; proposed storage thresholds require measured approval.” |
| O7: trust/privacy | Controlled writers/native storage/clock; restore requires new consent | “Uncertain identity/restore/clock history denies. No raw logs/cache/scratch/application dumps. Hostile-admin/forensic zeroization is outside this capability; paging/dump controls and sensitive-derivative retention remain explicit operating duties, not waived proofs.” |
| O8: next gate | Resolve strict blocker before any implementation, no cosmetic loop | “I withhold implementation until O3/O4 scope and O5 outcome are explicit and independently reviewed. Then synchronize only approved ADR/contracts/canonical inventories/existing handoff. E2 means containment and an unresolved deliverable. E1/candidate-only scope needs new acceptance semantics before code/tests change.” |

All O1–O8 await actual owner decisions. Partial principle/measurement approval
cannot select E1 or enable runtime by implication. No later phases, Frontend,
Ideaverse, frozen auth change or unrelated refactoring is authorized.

## 13. Rollback, evidence and next review

If separately authorized/implemented: containment first; stop admission and retain/
restore unconditional Java issuance/dispatch and Python pre-body denial. Positively
stop consumers, exclusive F/V and committed cutoff, invalidate RAG tickets/leases/
pointers and retry verified purge. Unknown outcome stays unavailable. Keep additive
H/V/control/ticket schema and anti-replay state; revoke v3 EXECUTE, never enable v2/
older unfenced writers or decrement history. No schema removal/restore while any
consumer/backend is unresolved. Test rollback, roles and every late-writer path.

Revision 3 changes **only this proposal and the draft remediation plan**. Accepted
contracts/ADRs/owner-decisions/ACTIVE, runtime/tests/migrations and historical
reviews are preserved. changed-files.md is not rewritten under this two-document
authorization; its earlier counts stay historical. Current delivery is 183 paths,
including the pre-existing independent R2 re-review.

All new native/PG/concurrency/benchmark/offline/full-suite guarantees remain
**NOT_VERIFIED**; no such test/control runs in this revision. Earlier 36-assertion
implementer and 21-assertion reviewer models are attributed historical abstract
reasoning, not runtime proof. §8's strict counterexample is a logical argument
under unbounded pauses, not a newly executed PostgreSQL result. No abstract-model
assertion or future passing ordinary test can prove that missing invariant.

Revision-local validation artifacts are ignored under
logs/phase17-consumption-design-revision-3-2026-10-15/: original drafts, initial
hashes and final documentation/isolation receipt. Reproduce from repository root:

~~~powershell
.\rag-engine\.venv\Scripts\python.exe logs/phase17-consumption-design-revision-3-2026-10-15/validate_revision.py
git diff --check
~~~

Final documentation check exited 0: 13 local Markdown targets, zero missing;
four CF rows, six design decisions, eight owner choices and 28 adversarial cases;
git diff --check exited 0. SHA-256 comparison of all 1,938 pre-existing nonignored
files found exactly the two authorized drafts changed and 1,936 others unchanged,
zero added/deleted delivery paths. Receipt stores current document hashes and
unchanged HEAD 24e6268eef1685866d919d882ecdf9f532d01961. No runtime guarantee is
claimed from these documentation checks.

**Ready for independent DESIGN review of the concrete D5/V revisions and explicit
O5 choices; not ready for implementation authorization.** Strict E2 remains
fundamentally unprovable for the enabled protocol under arbitrary pauses.
Recommend recording E2/containment now. If enabled useful work is desired, choose
the explicitly different E1(a+b+c) contract or candidate-only deliverable for
separate review. Another cosmetic expiry revision is not a remedy.
FR17-5/8/9/10 remain open; no owner commit readiness or Phase completion.
