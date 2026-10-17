# Phase 17 P17-3 consumption fence — independent design re-review of Revision 2

**Verdict: REVISE DESIGN. Revision 2 is not yet sufficiently complete for explicit OWNER DESIGN APPROVAL as one package.** P17-CF-1, P17-CF-2 and P17-CF-3 are resolved at the specification level, subject to the stated runtime proofs. P17-CF-4 remains partially resolved: O5 does not fully specify the consequences of a late physical Java verification/publication COMMIT. Mandatory drain control also needs a concrete ownership contract.

This is a **design review**, not an implementation final review or permission to implement. D1–D6/O1–O8 remain unapproved. FR17-5/8/9/10 remain OPEN; existing containment and `P17-RAG-FOUNDATION` / CHANGES_REQUESTED remain. No other implementation finding is reopened or silently waived. No owner commit readiness or commit message is issued.

## 1. Authority and independently verified evidence

Date: 2026-10-15. Baseline: `main`, HEAD `24e6268eef1685866d919d882ecdf9f532d01961`, including its existing uncommitted implementation, containment and design documents. The owner authorized only a new independent review/report. Repository/documentation AGENTS and applicable review/29-dimension/local-RAG/module-governance instructions were applied. No sub-agents were used.

Read all **832 lines** of the [Revision 2 proposal](../p17-3-consumption-fence-proposal.md), the [revised remediation plan](../remediation-plan.md), [changed-file inventory](../changed-files.md), [first independent design review](2026-10-15-phase-17-p17-3-consumption-fence-independent-design-review.md), accepted [P17-3](../revocation-and-derived-data.md), [P17-1](../integration-ownership-and-permissions.md), [owner decisions](../owner-decisions.md) and [ACTIVE](../../handoffs/ACTIVE.md). Compared accepted [ADR-0019](../../../adr/0019-local-rag-before-frontend.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module boundaries](../../../architecture/module-boundaries.md), [dependency matrix](../../../architecture/module-dependency-matrix.md) and the applicable Phase 17 source/privacy/lifecycle contracts already inspected in the preceding review. Unchanged canonical sources were reused after hash verification, not reinterpreted as a new approval.

The fresh snapshot contains **1,937 existing nonignored files**. Relative to the first review's pre-report snapshot, exactly the proposal, remediation plan and changed-files document changed; the first design report was added and is now historical. Production, tests, migrations, accepted contracts, ADRs and ACTIVE remain byte-identical. Review-only evidence is under ignored `logs/phase17-consumption-design-r2-review-2026-10-15/`.

Independently executed a new finite abstract model, rather than crediting the implementer's 36-assertion model as proof. **21 abstract assertions** completed: H-bound old/new consent, explicit renewal, stale approval CAS, unrelated changes, child closure, rollback, deletion/restart, G/B state transitions and expiry orderings. It confirms the remaining logical COMMIT counterexample as well as the consent correction. These are **zero application, PostgreSQL or native tests**; the model assumes the specified atomicity/participation and cannot prove them. The supplied model was inspected; it does not model physical COMMIT, transport or native ownership.

No expensive application suite, container, firewall, native runtime, benchmark or power-loss experiment was run. Earlier test results remain attributed historical evidence in the [implementation review](2026-10-15-phase-17-independent-final-codex-review.md) and [test evidence](../test-evidence.md), including the failed full Java clock-boundary run and containment-only Python failures. No unexecuted guarantee is marked passed.

## 2. Prior finding dispositions and D1–D6

| Prior finding | Independent disposition | Reason / remaining proof |
| --- | --- | --- |
| P17-CF-1 / High | **RESOLVED IN DESIGN** | Proposal §§4/7, lines 117–185: atomic per-source H plus explicit approved_H and E, retained deletion identity and no automatic rebind distinguish restricted round trips from unrelated mutations. New tickets under old approval now deny. Actual owner/H/V atomicity and exhaustive participation remain NOT_VERIFIED. |
| P17-CF-2 / High | **RESOLVED IN DESIGN** | §5 specifies F before begin, V before any ORM/query/flush, physical identity-bound same-mode joins, unregistered ambient rejection and no suspension/savepoints/upgrades. This addresses the earlier cross-resource cycle rather than relying on an annotation. Actual Spring/JPA enforcement and all callers remain NOT_VERIFIED. The new drain ownership issue below is separate. |
| P17-CF-3 / Medium | **RESOLVED IN DESIGN** | §§6/7 specify FOR SHARE/FOR UPDATE, post-lock comparisons, same-transaction lifetime, G reservation/B claim CAS, immutable ticket/candidate bindings, exact duplicate rules and legacy privilege revocation. Actual SQL functions, grants, ordering and single-use runtime behavior remain NOT_VERIFIED. |
| P17-CF-4 / High | **PARTIALLY RESOLVED / OPEN IN DESIGN** | Actual B, m0-based deadlines, no resets and explicit E1 exceptions address delayed permits and disclose continuous-time limits. However late Java VERIFIED/PUBLISHED COMMIT semantics are not fully included in O5's two consents; read denial does not undo those writes. See R2-1. |

“Resolved in design” closes neither implementation findings nor runtime acceptance obligations. The model proves only that the revised predicates distinguish the modeled histories.

| Decision | Disposition | Assessment |
| --- | --- | --- |
| D1 | **ACCEPTABLE AS PROPOSED** | Consumer-owned lifetime F is the right separate synchronization point for the one-host profile. All supported writers must participate; unstable identity or unsupported writers disable execution. No native correctness has been established. |
| D2 | **ACCEPTABLE AS PROPOSED** | Q captures immutable values under guards; canonicalization/digests run outside SQL; J freshly compares H/approval under continuous F. This preserves the no-network/digest/model-under-DB-guards rule. Q/J requires new owner snapshot/pure encoder contracts and complete mutation history. |
| D3 | **ACCEPTABLE AS PROPOSED** | H/E repair consent history; V orders pending SQL after either process dies; immutable scope/provenance and actual-role definer restrictions are coherent. As written, §4 also applies historical V matching to active/history reads: the broader invalidation cost must be explicitly accepted or revised under R2-3. No current owner/schema authorization is implied. |
| D4 | **ACCEPTABLE AS PROPOSED** | Protected entry, physical same-mode joins, immutable J and distinct G/B CAS supply a sufficiently concrete design. Every original caller and exception path must be audited before implementation scope approval. Unsupported ambient callers cannot be silently refactored or independently committed. |
| D5 | **REQUIRES REVISION** | Positive actual death plus V/attempt drain remains acceptable; failed termination must preserve unavailability. The new mandatory DRAIN protocol lacks an assigned Java/public ownership boundary compatible with the stated no owner-to-RAG call edge; see R2-2. |
| D6 | **REQUIRES REVISION** | E1 accurately admits read/transfer and staging residuals, but its treatment of Java verification/publication state after a delayed COMMIT is incomplete. E2 remains the present safe default. Explicit owner semantics, not another passing existing test, are required. |

## 3. Authorization and protocol assessment

### F + H + V + E and immutable consent

For approved A at H=4/E=e, owner NSFW COMMIT advances H=5 and V; allowed COMMIT advances H=6 and V again. With both notifications lost, current eligibility can be true and content hashes unchanged, but approved_H=4 differs from current H=6. Old tickets fail H/V; fresh admission and J fail approval history. Only explicit fresh approval, with current observed H/E/runtime, can capture H=6 and advance E. It cannot modify old tickets, candidate bindings or old generation authority. Two unrelated B changes preserve H_A and therefore A's consent, while invalidating A's old V ticket. This resolves the original information-loss problem.

Atomic owner truth/H/V commit and rollback are necessary. No-op successful relevant commands also invalidate H conservatively; separate relevant commands that restore original values cannot erase history. H tombstones survive deletion, missing history denies, IDs/type/installation cannot be reused, and overflow denies. A hard-delete FK must not cascade away anti-replay state. H is a retained monotonic summary, **not a complete append-only log of every historical consent event**. Immutable ticket/approval-version bindings plus current monotonic H/E suffice for the stated anti-replay property; do not claim a richer immutable audit ledger has been specified.

Every actually used child needs current H/E/approval/fields/revisions and exact closure/edges; parent approval does not authorize a new child. Reference/assignment/shared-label inputs that affect a projection must advance all affected H atomically, or that path/source stays disabled. This is a real impact-analysis gate: source history cannot compensate for an unenumerated indirect writer. Runtime/restart/restore invalidation and new installation identity on uncertain restore prevent reuse of restored counters; restoring old H/V is not fresh consent.

### Q/J/G/B/T/C/S/A/P

The Q/J split is logically safe against owner mutation **because continuous F excludes participating mutations and J still freshly compares H/E**, rather than trusting a prepared Java object. Q does not grant Python access. J rollback/unknown sends nothing. Q material must be discarded on failed J and never become a retry cache.

G reserves one opportunity with an immutable permit digest; B performs a fresh guarded lease/session check and CAS at the actual receiving route. RESERVED can transition to BODY_CLAIMED once. B's local atomic handler claim is also necessary: a repeated status lookup is not permission to attach another stream. Unknown G/B commits abandon the opportunity after stop/reap/metadata reconciliation; they cannot resend source bytes. These rules prevent logical duplicate-body authority if implemented as specified.

**Wire proof remains mandatory.** ASGI handler assertions alone prove only that application code did not call receive. HTTP server/framework middleware, automatic `100 Continue`, TCP/TLS buffers and Java timeout/retry behavior can precede that call. The supported client/server adapter must send no source body until B's actual committed acceptance and local deadline check, emit no early Continue from middleware, and never fall back to sending after an Expect timeout. Reject unsupported HTTP versions/retries/redirects or prove their equivalent behavior. Use wire/process instrumentation to measure T as well as C, with both auth-denied and delayed/unknown B cases. F/J must cover any permitted transport exposure. This is a runtime implementation proof obligation, not evidence from the abstract CAS model. Unsolicited bytes voluntarily sent by an arbitrary local process cannot be excluded from every OS socket buffer by an application handler; the exact trusted-producer threat boundary must be stated.

S is append-only, exact typed output/provenance, and cannot rewrite ticket authority. A is not trusted because it arrived over HTTP: Java must find the exact committed candidate and freshly prove scope/session/time before its own transition. Different-result replay, old boot/session, wrong stage, stale H/E/V and missing receipt deny. Death/seal and pending writer drain precede P. P freshly checks the entire manifest and exact verified/sealed receipt; an old VERIFIED bit alone never authorizes P or a read.

For NSFW/content/revoke COMMIT ordering, the revised design is logically stronger than another pre-send check: consumer F survives Java/DB loss; S's V/attempt guards survive client death until SQL ends; owner death during pending N is symmetrically ordered by exclusive V. It conditionally closes the **original FR17-5 owner-COMMIT counterexample** without Python business SQL or network under source DB guards. It does not yet close FR17-5 as an implementation finding, and it does not order clock expiry like an owner transaction.

## 4. Remaining findings and exact counterexamples

### P17-R2-1 — High: late Java verification/publication COMMIT is outside O5's complete exception list

Continuation of P17-CF-4. Proposal lines 278–280 and 553–566 combine fresh Java checks, deferred deadline constraints, known physical COMMIT and post-COMMIT read gates. O5 at line 781 asks for two consents: bounded consumption/transfer residual and late **untrusted worker staging**. It does not clearly authorize a late Java VERIFIED/SEALED/PUBLISHED state or change the accepted publication linearization at physical COMMIT.

Counterexample with trusted code, no SET CONSTRAINTS abuse, no clock jump and every lock correctly held:

1. S committed while valid. Java verification/P starts while the independent session and lease remain valid.
2. Java's last PG check and normal deferred deadline trigger run at D−epsilon and pass.
3. The PostgreSQL backend/storage/VM pauses after the last check but before completion of the transaction's commit. Time passes beyond D.
4. COMMIT completes at D+delta, persisting VERIFIED or the P active-generation/state write.
5. Fresh response/history checks run after COMMIT and deny because the grant is expired.

Step 5 is useful disclosure protection. It does **not** establish that no expired result became VERIFIED/PUBLISHED in durable state: step 4 already occurred. Private top-level adapters and resetting constraints DEFERRED prevent an arbitrary ambient caller from deliberately checking early; they do not remove this ordinary last-check-to-COMMIT interval. A later PG read cannot retract a transaction or change its physical ordering. The independent model enumerates all three orders with check before commit and retains `check < expiry < commit`.

Conversely, for **S that actually commits after D**, a later fresh Java check under the same monotonic PG-time assumptions sees expiry and denies verification. This part of E1's argument is valid. The gap is the verification/P transaction's **own** final interval. A previously valid VERIFIED receipt also does not authorize a P gate that begins after expiry. These distinctions must not be flattened into a claim that post-S checks solve every expiry race.

Required revision: define separately the time linearization/meaning of Java verification, publication and delivery. Either retain the strict physical-COMMIT invariant and containment, or propose an explicit additional exception: durable verification/active markers can commit after D, remain nonauthoritative without fresh read-time eligibility, and cannot independently cause downstream work/notifications/history exposure. The latter changes accepted behavior and needs owner approval beyond the current “untrusted staging” consent. Define all readers, status/counters, internal consumers and cleanup accordingly, not only the final HTTP response. Add exact tests with a server barrier **after the final deferred check** for verification and P, plus post-COMMIT/first-response pauses. Do not relabel an active generation as quarantine while still calling its COMMIT publication.

### P17-R2-2 — Medium: mandatory DRAIN has no complete ownership/call contract

Proposal lines 281 and 600–620 require admission-stop/cancellation before owner N and a canonical Python-parent DRAIN gate shared by Java controllers. Lines 189–214 put source-authority entry/native coordination in Vault; lines 243–248 forbid owner-to-RAG calls/locks. The public signatures list F/V/H transaction operations, not a consumer-control ownership boundary.

Counterexample to completeness: a standalone `FictionOperations.update` must enter via Vault before DB work. Who issues authenticated DRAIN and tracks/completes its operation if the call did not originate in a RAG controller? Calling the Java RAG service from Vault/owner breaks the stated edge; silently omitting DRAIN abandons the claimed cross-controller admission priority. Creating an unmentioned callback/port/native coordinator implicitly expands the public/operational contract. None is demonstrated by this draft.

Specify the logical owner, approved public/transport interface, credential/boot/operation binding, all standalone/registered callers, lost-ACK reconciliation and completion/reopen rules. A Vault-owned source-consumer coordination capability may be a feasible design; an explicit outer orchestration layer is another possibility, but neither may conceal a prohibited reverse feature dependency or change business atomicity. Alternatively make drain a clearly optional availability aid, retain bounded fail-closed F acquisition, and remove success/fairness claims that depend on it. DRAIN ACK must never mean death, revocation or purge completion. No DB transaction may wait on DRAIN/death. This is a governance/design gap, not a reproduced production dependency defect.

### P17-R2-3 — Low / explicit owner clarification: global V also affects accepted results as written

Proposal lines 174–178 require the entire closure **and V** to match on publication **and active/history reads**. A result for A can be validly published at V=7; an unrelated B mutation advances V=8 while H_A/E_A/fields remain unchanged. A's historical ticket no longer matches V, so applying that predicate denies A's active/history read too. This is safe conservative denial, but broader than merely killing in-flight work or preserving unrelated consent.

Owner wording must include that already-published outputs can become unavailable after unrelated writes and need rebuilding, or the design must distinguish an execution ticket's expected V from a fresh read transaction's current V barrier while retaining full H/E/closure proof. Do not silently rebind immutable tickets. Add an A-published/B-mutated/read-A case and measure rebuild/failure costs if that is the chosen policy. This is not a privacy blocker by itself; it materially affects O4/O6 and later use of the foundation.

## 5. E1 versus E2 and accepted guarantees

| Accepted requirement / finding boundary | E1 effect and independent assessment |
| --- | --- |
| P17-3 §2 stop reads on session expiry; §3.2 leased preapproved input | Changes to bounded read/work checks. One at-most-64-KiB unit can execute after a passed check even if the process resumes after D; buffered in-flight T can arrive after D. Wall-clock residual can be unbounded under suspension. Explicit functional exception, not a two-second guarantee. |
| P17-3 §1A.6 staging/attempt COMMIT fence | Owner revoke/purge ordering remains protected by V/attempt. Automatic elapsed-time expiry is different: SET CONSTRAINTS IMMEDIATE or a final-check pause can leave a nonpublic candidate committed after D. This is a genuine expired-staging retention exception, not just better naming. |
| Accepted FR17-3 exact receipt/provenance | Exactness and actual worker provenance must remain. New candidate-versus-Java-verified distinction changes what a successful receipt means; old assertions must be retained and explicitly mapped, not weakened to “some candidate existed.” |
| P17-3 §1A.5 and FR17-7 physical publication/deadline checks | Post-wait/default deferred checks remain valuable. They cannot establish absolute physical-COMMIT deadline under every backend stall. R2-1 requires an explicit third consequence/semantics decision; Java-only code composition is insufficient proof of the stronger claim. |
| Fresh active/history/status/output gates | Can deny expired rows despite their stored state, if every consumer applies fresh full authority. Logical privacy benefit; no executed coverage here, no undo of a late publication COMMIT and no continuous instruction-time delivery guarantee. |
| NSFW/revoke source mutation COMMIT | No relaxation justified. F/H/V/E ordering and synchronous exclusion remain required, including lost events and pending SQL. E1 time exceptions must not permit a C after an earlier owner exclusion COMMIT. |
| Purge and retention | A digest is raw-free, not automatically anonymous: low-entropy titles may be inferred from deterministic hashes and identifiers/provenance remain sensitive metadata. Quarantine is still a derivative, requiring inaccessible storage, no ordinary success/status leakage, durable pending cleanup, role isolation and purge. “Limited” currently limits data kind; it does not guarantee wall-clock erasure while services are down. |

**Recommendation:** select **E2/containment for the present unapproved contract**. E2 is an honest refusal to claim an unattainable strict-instant property, not a practical replacement implementation. Do not commission a supposed TTL/trigger/watchdog fix for an arbitrary instruction-time guarantee.

A revised bounded-gate alternative can be technically useful for this digest-only foundation, but require three separately stated choices: (a) read/transfer suspension residual; (b) raw-free expired candidate quarantine; (c) the meaning of late Java verification/publication markers and mandatory fresh read authorization. If the owner requires no expired durable VERIFIED/PUBLISHED transition, reject (c) and keep containment until another approved functional model exists. If publication is instead defined as a fresh eligibility operation over nonauthoritative stored candidates, redesign and name the state/linearization accordingly before approval. This review does not approve any exception or expand it to model/ingestion work.

Primary references independently checked: [PostgreSQL SET CONSTRAINTS](https://www.postgresql.org/docs/18/sql-set-constraints.html) permits early checks; [CREATE PROCEDURE](https://www.postgresql.org/docs/18/sql-createprocedure.html) forbids transaction control in a SECURITY DEFINER procedure. Neither privilege-restricted EXECUTE nor a private adapter makes arbitrary physical COMMIT timing enforceable. These facts support the design limits, not a claim of a newly executed SQL exploit.

## 6. Transaction, concurrency and least privilege

The revised order F → V → graph → sorted Vault rows → sorted H → owner/policy rows → attempt → ticket → generation/stage/run is coherent when complete affected IDs are fixed before later locks. Owners omit RAG locks; worker uses V → H → attempt → ticket → stage, never business/graph locks. Nonlocking immutable ticket lookup is routing only; post-lock reload must match. No guard can be acquired in an autocommit/helper connection and released before the dependent write's transaction ends.

FOR SHARE conflicts with the version writer, including a non-key UPDATE; FOR KEY SHARE does not suffice. Post-lock comparison must observe the locked current version, not a pre-wait snapshot. Deferred checks may recheck held guards, not introduce an earlier-order acquisition. Savepoint/PLpgSQL exception handling cannot preserve a surviving write after its protection rolls back. [PostgreSQL locking](https://www.postgresql.org/docs/18/explicit-locking.html).

Protected entry resolves the previously described row-R → F versus child-F → J → R cycle only if **all** eligible mutation entry points reject unregistered ambient transactions before mutation/flush. REQUIRES_NEW retains outer resources; NESTED can roll back to savepoints. The explicit rejection rules are appropriate; runtime transaction-manager/proxy/self-invocation proof is still necessary. Unknown outcomes retain F/quarantine until DB reconciliation; process death releasing F is separately covered by pending V. [Spring propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html).

Consumer-first: shared F prevents owner N until the actual reader is dead. N-first: fresh H/E/V deny old authority. Child death during S releases F, but shared V/attempt orders N/purge after S or rejects stale queued S after N. Owner death during pending N releases F, but its exclusive V orders new J/B after commit/rollback. These are conditional logical proofs, not distributed ACKs. Delayed receipts never substitute for current authority. Missing/failed termination, controller loss, unknown writer state and incomplete erase all remain pending/busy rather than false quiescence.

The proposed SQL ownership chain is justified but new: worker LOGIN → RAG NOLOGIN definer → narrowly scoped Vault NOLOGIN metadata guard. Worker gets only scoped G/B/S EXECUTE and schema USAGE; no H/V/business/auth access, direct staged DML, canonical verification/seal/pointer mutation, CREATE/TEMP, inherited elevation or v2 bypass. All objects/search paths and actual session_user/child credential must be checked; a request producer flag is not provenance. Cross-job credential abuse, malformed binding arrays, row-count/duplicate/order errors, terminal ticket reuse and effective PUBLIC grants remain negative tests. Timeout GUCs and SET CONSTRAINTS are not falsely considered revoked by this grant model. Actual roles and functions do not exist for this design and remain NOT_VERIFIED.

Prepared guard reservations remain technically possible but retain database locks beyond session death and require a durable coordinator, prepared-slot/recovery policy and exception to no-network-under-source-DB-guards. They do not solve absolute time between a check and later CPU/WAL actions. F/H/V/E is a proportionate candidate for the bounded native profile once the remaining contract issues are resolved; neither alternative is approved here.

## 7. Owner decisions O1–O8

Wording below is a recommendation for a future recorded decision, **not approval by this report**. Partial decisions may be recorded now, but they do not make the full package ready or authorize code/contract changes by implication.

| Owner decision | Technical assessment and readiness | Recommended exact approval wording / required revision |
| --- | --- | --- |
| O1 | **Justified; principle may be approved now.** H repairs lost-event history without forcing unrelated consent renewal; V has a separate pending-SQL purpose. New Vault persistence is beyond original RAG-only authorization. | “I approve designing the additive Vault-owned H/V metadata and retained content-free identity tombstones, with atomic owner updates, no reset/reuse or automatic consent seeding. This does not authorize migration or implementation.” Final signatures/data inventory still require the revised contract gate. |
| O2 | **Justified; policy may be approved now.** Conservatively invalidating on all successful relevant commands, including reversal/no-op, avoids hidden history gaps. Actual affected-field/writer inventory remains required. | “Every successful participating source/projection mutation advances its H; only explicit current H/E/runtime-bound approval may renew consent. No notification, retry or reconciliation may rebind it. Unproven indirect writers/projections remain disabled.” |
| O3 | **Sound transaction model; final scope approval waits for R2-2/caller inventory.** Rejecting existing ambient callers can change frozen API composition and batch availability. | After exact scope is listed: “I approve protected F-before-begin/V-before-work entry and physically registered same-mode joins for the listed participants. Unknown ambient, suspension, upgrade and savepoints reject atomically. No hidden independent business commit or unlisted caller refactor is authorized.” |
| O4 | **Platform/safety trade-off may be approved now; drain mechanism needs revision.** Single native host is feasible in principle. Stuck-consumer unavailability can be indefinite; V also has R2-3's published-read cost as written. | “I accept one Windows kernel/installation, protected local native F, no off-host writers, and bounded busy/unknown responses with potentially indefinite safety-preserving unavailability. I do not accept force-unlock or an eventual-success promise.” Separately choose the drain owner/interface and explicitly accept or revise global V invalidation of already-published reads. |
| O5 | **NOT READY; requires R2-1 revision.** Two consents do not fully cover late Java state COMMIT. Expired staging retention is a security change, not an implementation detail. | Current safe choice: “I retain E2 and containment; no expiry exception is approved.” A future E1 decision must separately enumerate read/in-flight residual, expired raw-free candidate quarantine, and late Java verification/publication-state semantics, with precise visible/read authorization and cleanup. Do not approve the present two-item wording as sufficient. |
| O6 | **Useful measurement targets; may be approved for evaluation now.** Success floors prevent denial-only green benchmarks. Values are unmeasured and can fail. | “I approve section 10's caps/100% normal-fixture and ≥12/16 fault-free-wave success criteria as measurement gates, not achieved performance or enablement. Every miss blocks this profile pending review; no grant extension, denial-as-success or dropped outlier is permitted.” Add the H/tombstone disk and V-rebuild measurements below. |
| O7 | **Trust/deployment boundaries justified; may be approved explicitly now.** Restore must be quiescent/new consent; F is not hostile-admin or forensic protection. Dump/paging controls still need an operational design. | “I accept trusted controlled writers and supported native storage only; uncertain identity/restore fails closed, old approvals never restore implicitly, and hostile-administrator/forensic memory zeroization is outside this capability. No raw application dumps/cache/scratch/logs are permitted; stronger OS guarantees are not claimed.” This cannot waive required role/egress/native proofs. |
| O8 | **Workflow justified; may approve sequencing only.** Full design and concrete contract synchronization are not ready yet. | “After independent acceptance of the revised design and my recorded choices, synchronize the specific ADR/accepted contracts/canonical inventories and existing handoff. Implementation needs a separate explicit authorization; containment and finding status remain until actual verification and independent final review.” |

## 8. Complete C01–C24 adversarial matrix audit

Every case is a **proposed obligation, runtime NOT_VERIFIED**. “Adequate” below evaluates specification coverage, not execution. The original implementation counterexamples remain necessary: post-issuance owner NSFW/worker receipt, malformed provenance/stage/digests, added transitive dependency, expired-during-lock-wait publication, false quiescence on unavailable/duplicate worker, restrictive quoted metadata, cached authority and graph mutation, purge/recovery races, effective-role bypasses and budget/offline gaps.

| Case | Design coverage assessment / exact addition |
| --- | --- |
| C01 | Adequate for allowed→NSFW→allowed, both events lost, fresh admission/J and reads under old consent. Require actual Fiction and Film owner COMMITs, not only model flags. |
| C02 | Adequate reversal/deletion/child matrix. Add tombstone integrity under actual FK/delete actions and duplicate ID/type conflict on migration/restore. |
| C03 | Adequate rollback/unrelated/restart/restore predicates. Add A already PUBLISHED, B unrelated change, read A to decide R2-3; restored counters alone must fail. |
| C04 | Adequate stale approval-form H/E race in both orders. Include explicit reapproval after round trip with old ticket/S replay and absent/wrong runtime binding. |
| C05 | Adequate early rejection of clean/dirty ambient entry; observe no SQL auto-flush/entity mutation/child/queue side effect, not only an exception. |
| C06 | Adequate nesting/suspension/savepoint/unknown rules. Include self-invocation/proxy bypass, actual resource/DB transaction identity and no guard release on inner completion. |
| C07 | Adequate order-inversion/indirect-caller inventory. Add DRAIN ownership/caller path, shared reference impact fan-out and above-bound atomic rejection. |
| C08 | Covers the original FR17-5 post-J owner-COMMIT gap and Q/J split. Observe actual no T/C, genuine writer blocking and both orderings; include delayed parent/Java death while body is buffered. |
| C09 | Covers new child/fields/PRIVATE/quoted metadata. Preserve all exact malformed-stage/input/scope/output/digest/provenance negatives from the prior suite, not just the named closed findings. |
| C10 | Adequate real V SHARE/UPDATE conflict and negative KEY SHARE/plain-read fixture. Assert post-wait H/V and same connection through COMMIT; include missing/overflow H/V and multi-source sorted locks. |
| C11 | Adequate G/B CAS/unknown/duplicate states. Add explicit wire trace for pre-handler prefetch, early/duplicate 100 Continue, Expect timeout/fallback, redirects/retries/chunked/unsolicited content and B after-COMMIT pause. Application receive counters alone are insufficient. |
| C12 | Adequate unique immutable candidate/receipt and lost/forged ACK recovery. Include stale E/H/V/boot/session reapproval replay and same-input/different-stage/output substitution. |
| C13 | Adequate child-death/pending-S and both-order purge. Observe real server lock lifetime after disconnect, not an HTTP acknowledgement; zero post-PURGE_COMPLETE resurfacing. |
| C14 | Adequate symmetric owner-death/pending-N case; check both COMMIT and rollback, fresh reads after V wait and no pre-wait cached authority. |
| C15 | Good actual B and per-unit expiry checks. Define allowed in-flight T versus prohibited newly initiated T, local origin/margin and exactly what a paused 64-KiB unit may do under E1; no continuous-read PASS. |
| C16 | **Incomplete acceptance semantics.** Add verification/P server pause after last deferred check, late durable VERIFIED/PUBLISHED observation and all consumer/read denial assertions. Retain original normal-path rollback tests; do not change their result to accept expired candidates. |
| C17 | Clock/response/final-check residual recognized. Add backstep/forward-step/discontinuity outcomes and a recorded conservative margin; no inference from shifted Java/Python wall clocks that PG progression is bounded. |
| C18 | Adequate native identity/handle/acquisition matrix. Include two Java controllers/sessions, same-process handles, already-open conflicting handles, canceled pending overlapped acquisition and no early handle close while any thread can read. |
| C19 | Good failed-death/orphan/drain safety coverage. Add concurrent DRAIN IDs, record-cap exhaustion, lost ACK, owner rollback/unknown, parent restart closed and reopening only after every pending operation resolves. No deadlock waiting for control while holding SQL rows. |
| C20 | Adequate actual-role negative boundary. Include actual authenticated session_user/definer chaining, effective PUBLIC/membership grants, cross-ticket credentials, partial/duplicate H arrays, untrusted search path and every legacy/updatable route. |
| C21 | Adequate lifecycle/purge intent. Distinguish expiry from committed revoke; include late quarantined candidate during controller outage, sensitive-digest retention and R2-1's internal consumers/counters. |
| C22 | Useful successful-work thresholds. Add H/tombstone lifetime cardinality/low-disk behavior, multiple controllers/drain, V-triggered rebuild costs and measured CPU time/connection/handle ceilings. Report all wave outcomes and failed samples. |
| C23 | Adequate **supported native successful** offline path, plus denied/cancel/restart. Actual OS enforcement and attempted IPv4/IPv6/DNS/proxy connections required; network-none failed-DB/Linux-without-F substitutes do not pass. |
| C24 | Adequate focused/full/static/governance gate. Retain exact failures/exits and frozen auth assertions. Add explicit traceability for R2-1/2/3 and any owner-approved changed contract expectation, without deleting historical tests. |

Original R1–R8 remain traced. R7 live external files stays disabled and is not certified by the metadata F tests. Later parser/model/judge/retrieval product quality remains deferred; controlled foundation fixtures test only applicable lifecycle invariants. The 24 rows form a strong test plan but are not exhaustive proofs. Added assertions above are needed to cover the revised design, not permission to modify tests in this review.

## 9. Resources, Windows/native assumptions, rollback and governance

Section 10 now separates normal success from saturation failures: five warmups and at least 100 measured successes per normal fixture, **100% expected-authorized success**, active F p95≤1.5s/max≤2s, cold startup p95≤3s/max≤5s, isolated queue-inclusive p95≤6s/max≤8s; **at least 12/16 genuine successes in every fault-free saturation wave**. These are useful prospective gates, not measurements or a guarantee of every admitted success. Count all attempts, denials, outliers and useful completions; do not select only completed samples. Per-wave success and per-workload sample counts both apply. Single-slot cold startup and 30s queue age constrain whether the floor is feasible; it must be measured on the reference host.

The 2s threshold starts at child F acquisition and spans Q/J/G/B/T/C/S/lifetime; startup uses the sole slot beforehand. One elapsed budget, conservatively anchored to each process's local m0 and PG observation, must shrink all waits/statements/transaction budgets. A 50ms safety margin is an assumption awaiting measurement/justification, not proof against unseen PG clock jumps. Default 3s/5s/10s database ceilings do not enlarge the remaining 2s budget and cannot terminate an arbitrarily stalled backend or prevent a credential holder changing user-settable GUCs.

Memory/process limits are concrete proposed Windows Job Object private-commit limits (128 MiB child/256 MiB tree), one active child/no descendants and a measured 16 MiB incremental-buffer target. They need native proof, baseline interpreter/import accounting and failure cleanup. A wall deadline is not a CPU quota; record CPU as proposed and decide any additional hard CPU requirement before acceptance. Zero source scratch/model/GPU allocations remains mandatory. Native egress controls require authorization and actual enabled success; no controls were changed here.

H is one row per owning identity ever created, including retained deletion tombstones; the 1,000 RAG catalog cap does not necessarily bound that lifetime owner metadata. Specify expected cardinality/disk headroom, low-disk behavior, indexes/query bounds and migration/seed costs. Do not discard H to satisfy a quota or partially commit business mutation without it. Reference fan-out over 100 sources may make an existing owner operation unavailable; audit and obtain explicit scope approval rather than silently chunking independent commits. Global V and R2-3's read invalidation may add rebuild cost even with H preserving unrelated consent.

Stable local NTFS metadata identity, ACLs, protected ancestors and installation bootstrap prevent split F objects; no auto-recreate while an old process may exist. Native tests must prove actual cross-process/session lock compatibility, acquisition completion, handle lifetime and exit cleanup. A close can release a lock before process death; code must have no close-and-continue reader path. OS cleanup delays only deny. [LockFileEx](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-lockfileex).

Positive death requires the actual bound process object/creation identity, not a PID scan, health boolean or successful termination request. Termination can complete asynchronously and wait on pending I/O. Failed termination/unresolved handles keep slot/F busy and require pending recovery. [TerminateProcess](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-terminateprocess).

Rollback proposal remains safety-preserving: containment first, stop/reap consumers, exclusive F/V and writer drain, invalidate tickets/approvals/leases/pointers, retry purge, keep additive history/tombstones and deny v2/older unfenced writers. Unknown completion stays unavailable; it cannot justify schema removal or history decrement. Quiescent restore and new consent/runtime are required. Power-loss and forensic zeroization remain NOT_VERIFIED, not implied by orderly termination. Operating controls for raw dumps/paging must be documented separately from application no-scratch claims.

The purpose still fits one bounded **Phase 17 foundation** delta; the API/schema/caller impact is substantial and must be enumerated. Required future owner-authorized changes: Vault F/H/V ownership and protected named interface; fresh owner snapshots/pure revision encoder; RAG approved_H/runtime/stale-form CAS; immutable v3 ticket/binding/candidate/verification/seal state; narrow SQL definer edge/effective privilege revocations; selected expiry/retention/publication semantics; drain ownership and supported topology/budgets. Append-only migrations after V13, canonical additive data/DBML inventory, ADR, source contracts, module/SQL dependency docs, package tree and existing ACTIVE must then synchronize. No existing migration/frozen history is rewritten; no generic transaction framework, reverse RAG dependency, public chat/query API or Phase 18–22 capability is approved.

## 10. Shared design-coverage ledger

PASS below means a design obligation is adequately specified, **not executed implementation acceptance**. All proposed native/SQL/runtime guarantees remain NOT_VERIFIED until the mandated tests run.

| # | Dimension | Status | Evidence / limit |
| --- | --- | --- | --- |
| 1 | Owner intent/scope | PASS | Explicit design-only authority, containment, no later capability or implicit code approval. |
| 2 | Requirements/traceability | FINDING | CF-4/R2-1 time semantics and C16 expectations incomplete. |
| 3 | ADR/architecture/governance | FINDING | R2-2 drain boundary and exact owner changes require revision/approval. |
| 4 | Modulith/public ownership | FINDING | Core RAG→owners direction sound; mandatory DRAIN caller/owner undefined. |
| 5 | Package/folder structure | N/A | No production package added; proposed owner placement assessed, actual new structure unimplemented. |
| 6 | Shared DTO/errors/root envelope | PASS | ADR-0016 preserved; narrow v3 typed boundaries, no arbitrary common module. |
| 7 | SOLID/GRASP/cohesion | PASS | Distinct owner authority, lifecycle, native and worker responsibilities; no new implementation certified. |
| 8 | DRY/KISS/YAGNI/invariants | PASS | H replaces lost information, one canonical job engine, no duplicated scheduler. |
| 9 | Patterns/anti-patterns | PASS | Adapter/fence/CAS/state solve concrete failures; prepared coordinator remains an explicit alternative. |
| 10 | Domain transitions/idempotency | PASS | H/E renewal, tombstones, immutable G/B/S bindings; actual atomic behavior still future proof. |
| 11 | API/wire contracts | FINDING | R2-2 drain interface plus pre-B transport proof details required. |
| 12 | Input validation/bounds | PASS | Exact allowed digests/kinds/fields/counts/bytes and default deny remain specified. |
| 13 | Auth/privacy/consent | FINDING | Consent history repaired; R2-1 expiry/publication exception incomplete. |
| 14 | Credentials/log/cache/egress | FINDING | Raw-free quarantine is sensitive metadata; E1 retention/read semantics and dump controls need explicit contract. |
| 15 | Transactions/locks/consistency | FINDING | F/V/H physical ordering sound conditionally; time-to-COMMIT claim remains R2-1. |
| 16 | Failure/recovery/operations | PASS | Positive death, pending SQL, restore/replay, safe busy/unknown and rollback obligations concrete. |
| 17 | Database/migrations/privileges | PASS | Narrow role/SQL chain and append-only cutover specified; runtime effective grants NOT_VERIFIED. |
| 18 | Complexity/resources/admission | NOT_VERIFIED | Proposed limits, tombstone lifetime and global V costs require configured-limit evidence. |
| 19 | Retrieval/recommendation quality | N/A | No such product in this delta. |
| 20 | OCR/document/multilingual fidelity | N/A | No parser or live-file enablement; transitive authority covered separately. |
| 21 | Adversarial security obligations | FINDING | Full C01–C24 audited; R2-1/2 and explicit wire/history additions missing. |
| 22 | Test quality/determinism | PASS | Real roles/processes/post-check barriers, finite deadlines and preserved original assertions required. |
| 23 | Benchmarks/thresholds | NOT_VERIFIED | 100% normal/12-of-16 floors unmeasured; no denial-only PASS. |
| 24 | Held-out evaluation science | N/A | No model/parser/judge evaluation selection authorized. |
| 25 | Dependencies/licenses/lifecycle | NOT_VERIFIED | New native adapters/packaging not built/audited; historical scans not a new certificate. |
| 26 | Build/static/CI | PASS | C24 retains full/static/architecture gates, exact failures and frozen auth tests. |
| 27 | Logging/observability | PASS | Source-free operational correlation and full waits/outcomes/resources required; no payload diagnostics. |
| 28 | Docs/status/links | PASS | Draft and report separate from accepted contracts; local links and status preservation verified. |
| 29 | Hygiene/history/delivery | PASS | New report only; 1,937 pre-existing files preserved by SHA-256 verification. |

## 11. Verification, preservation and next action

New independent command from repository root:

```powershell
.\rag-engine\.venv\Scripts\python.exe logs/phase17-consumption-design-r2-review-2026-10-15/independent_model.py
```

Exit 0, **21 abstract assertions**, including finding the remaining time-order counterexample; no application/native/PostgreSQL tests. The implementer's 36-assertion model was read, not credited as independent execution. Primary technical documents were checked for SQL constraint/definer, Spring propagation and Win32 semantics; all inferences are stated as logical arguments rather than runtime observations.

Final read-only validation command:

```powershell
.\rag-engine\.venv\Scripts\python.exe logs/phase17-consumption-design-r2-review-2026-10-15/validate_review.py
git diff --check
```

Validation passed: **14 local Markdown targets, zero missing; four CF dispositions, six D dispositions, eight O assessments, 24 matrix cases and 29 ledger rows**. Ledger counts are **14 PASS, 8 FINDING, 4 N/A, 3 NOT_VERIFIED**. All **1,937 baseline file hashes are unchanged**; the only new nonignored file is this report. `git diff --check` and new-report whitespace checks passed. The receipt records these results and the report SHA-256.

The 182-path existing delivery count reconciles with Git; this report makes 183 paths. One inventory precision limit remains: changed-files describes the first independent design report as preserved in prose but does not explicitly link that path in its file lists. The validator records that omission rather than silently claiming the explicit links alone form a complete 182-path list. This review does not edit the inventory to imply approval. Production/tests/migrations/accepted contracts/ADRs/ACTIVE and all prior reports remain unchanged. No commit/push/new handoff.

**Exact next action:** obtain a **design-only Revision 3** resolving P17-R2-1 and P17-R2-2, and explicitly choosing the R2-3 V/read policy plus the added acceptance assertions. The owner may record the ready principles/measurement choices in O1/O2/O4-platform/O6/O7 and O8's sequencing, but must withhold full O3/O4-drain/O5 contract approval until revised. Re-review that concrete design independently, then record the owner's exact selected contracts and separately authorize any implementation. E2/containment remains the present default; no FR17-5/8/9/10 closure or phase advance follows from this report.
