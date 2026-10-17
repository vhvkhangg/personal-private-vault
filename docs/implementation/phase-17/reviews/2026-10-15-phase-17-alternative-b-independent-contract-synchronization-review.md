# Phase 17 Alternative B — independent contract synchronization review

**Verdict: ACCEPT CONTRACT SYNCHRONIZATION.** No blocking contract contradiction or unauthorized expansion was found in the 26 modified and four added documents. The synchronized package implements the owner's approved four-part decision, including the consent-command refinements, all three E1 exceptions, independent published-index lifetime and cancel-first availability semantics.

This accepts documentation synchronization only. It does **not** authorize implementation or execution enablement, certify runtime correctness, close FR17-5/8/9/10, issue owner commit readiness or complete Phase 17. **Separate IMPLEMENTATION authorization is the next owner gate.** ACTIVE remains CHANGES_REQUESTED and containment remains mandatory.

## 1. Scope, authority and independent evidence

Reviewed 2026-10-15 on `main`, owner HEAD `24e6268eef1685866d919d882ecdf9f532d01961`, including the inherited uncommitted candidate. The current owner instruction explicitly approves Alternative B and its complete four-part contract package. The approval baseline is [independent design review §4 and §8](2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments), recorded as seven decisions in [owner-decisions](../owner-decisions.md#2026-10-15--alternative-b-owner-design-approval). This review checks synchronization against that decision; it does not reconsider the selected architecture.

Applied root/backend/docs AGENTS, the repository review-coverage rubric and relevant RAG/Modulith guidance within this expressly documentation-only scope. Inspected the current normative contracts, affected canonical amendments, inventory/status references, actual Git diff/status and recursive relevant package inventory. Unchanged historical material was reused with hash verification. The two superseded proposal bodies match their prior reviewed hashes after removing only the six-line historical banner; their pending decisions cannot override accepted ADR-0020.

Independent evidence consists of document/source inspection, approval-to-contract comparison and fresh local validation. **No Java/Python application suite, database/native concurrency test, migration, benchmark, dependency audit or offline enforcement test ran.** Previous runtime receipts remain historical, including the authentication clock-boundary failure and contained worker execution failures. The implementer's validator was not used as independent acceptance evidence; a separate checker was written under ignored review logs.

## 2. Exact inspected synchronization inventory

Independent SHA-256 comparison against the previous review's 1,941-file delivery state confirms exactly **26 modifications, four additions, no deletion and 1,915 unchanged files**. This is the synchronization delta, not the much larger inherited diff against owner HEAD. All paths below were inspected for current content/affected sections, historical precedence and cross-reference integrity.

| Change | Inspected path | Assessment |
| --- | --- | --- |
| Modified | [AGENTS.md](../../../../AGENTS.md) | Specific contract-only authorization overrides the earlier implementation-cycle permission. |
| Modified | [backend/AGENTS.md](../../../../backend/AGENTS.md) | Same authorization boundary; no code/test/migration enablement. |
| Modified | [ADR-0019](../../../adr/0019-local-rag-before-frontend.md) | Clauses 2–5 explicitly amended; sequencing and later capability gates retained. |
| Modified | [ADR index](../../../adr/README.md) | ADR-0019 amended; ADR-0020 accepted contract, not implementation. |
| Modified | [Architecture index](../../../architecture/README.md) | Approved target versus contained candidate distinguished. |
| Modified | [API architecture](../../../architecture/api-architecture.md#16-phase-17-alternative-b-contract-amendment) | Existing bounded actions, approval CAS, description confirmation, 409 and lifecycle amendments. |
| Modified | [Data architecture](../../../architecture/data-architecture.md) | Logical additive target separated from existing V3–V13 candidate. |
| Modified | [Module boundaries](../../../architecture/module-boundaries.md) | Vault state/public operations, owner SQL backstops and RAG lifecycle ownership. |
| Modified | [Dependency matrix](../../../architecture/module-dependency-matrix.md) | No new Java edge, worker business access or reverse Vault dependency. |
| Modified | [RAG architecture](../../../architecture/rag-architecture.md) | B, E1, IndexCurrent, manual indexing and future-capability boundaries synchronized. |
| Modified | [Structurizr source](../../../architecture/structurizr/workspace.dsl) | Target annotation; existing candidate topology retained. |
| Modified | [Database index](../../../database/README.md) | Historical migrations versus logical target; future append after V13. |
| Modified | [ACTIVE](../../handoffs/ACTIVE.md) | Existing handoff, CHANGES_REQUESTED, contract-only authority and open findings retained. |
| Modified | [Phase README](../README.md) | Approved contracts without implementation acceptance or premature closeout. |
| Modified | [Simplification proposal](../architecture-simplification-proposal.md) | Historical banner; prior reviewed body preserved. |
| Modified | [Benchmarks and acceptance](../benchmarks-and-acceptance.md) | Single-job successful workload; obsolete queue/F thresholds replaced prospectively. |
| Modified | [Changed-files inventory](../changed-files.md) | Current 30-file delta linked separately; prior per-pass counts remain attributed history. |
| Modified | [P17-1](../integration-ownership-and-permissions.md) | Public contracts, logical ownership, transaction and effective SQL-role requirements. |
| Modified | [Owner decisions](../owner-decisions.md) | Seven decisions faithfully express the complete four-part approved package. |
| Modified | [Former consumption-fence proposal](../p17-3-consumption-fence-proposal.md) | Historical banner; no active F/H/V/DRAIN authorization. |
| Modified | [Remediation plan](../remediation-plan.md) | Current dependency-aware B plan supersedes the retained prior diagnosis. |
| Modified | [P17-3](../revocation-and-derived-data.md) | Reservations, physical transactions, E1, recovery, IndexCurrent and purge. |
| Modified | [P17-2](../source-eligibility-and-allowlists.md) | Exact original field matrix, reset/CAS and description rules. |
| Modified | [RAG phase plan](../../rag-phase-plan.md) | Bounded Foundation and manual indexing; later phases independently gated. |
| Modified | [Package-tree inventory](../../../repository/repository-package-tree.md) | Current candidate and future additions distinguished; existing owners retained. |
| Modified | [Roadmap](../../../roadmap.md) | Existing handoff/remediation status; no new handoff or Phase 17 completion. |
| Added | [ADR-0020](../../../adr/0020-phase-17-single-job-reservations-e1.md) | Exact accepted contract amendment, not implementation authorization. |
| Added | [Alternative B DBML](../../../database/phase-17-alternative-b.dbml) | Logical additive inventory; no executable DDL/schema acceptance claim. |
| Added | [Verification plan](../alternative-b-verification-plan.md) | Complete B01–B20 and explicit maintained-test expectation changes. |
| Added | [Synchronization record](../contract-synchronization.md) | Exact independently confirmed inventory and bounded authorization/evidence claims. |

Additional comparison sources included the prior independent design review, preserved implementation/test evidence, ADR-0006, ADR-0016, security architecture, candidate diagram sources and current source registration DTO/controller signatures. All 470 local Markdown links in the submission were resolved, including historical inventory links. The relevant Java RAG subtree contains 61 files, 12 package descriptors and zero `.gitkeep`; the migration inventory contains V1–V13 and its README. This is structural evidence, not a renewed code-quality or Modulith execution verdict.

## 3. Owner-decision compliance and ADR consistency

| Owner decision | Independent contract assessment | Evidence |
| --- | --- | --- |
| OD1 — B, one user/account/Windows/job; 1B/2B/3B | PASS. No automatic indexing queue/resume or expanded deployment. Full bounded synthetic Foundation retained. | ADR-0020 decisions 1/6; P17-3 admission/recovery; phase plan. |
| OD2 — complete E1 and persistent index lifetime | PASS. Late consumption, untrusted S and actual trusted VERIFIED/P physical C explicitly permitted; late valid P can remain CURRENT. Fresh expired gates still deny. | ADR-0020 decisions 4/5; P17-3 §1/§1A.5; API finalization/status. |
| OD3 — bounded owner/schema/API/trigger/process delta | PASS. Vault retained state, common guards and SQL backstop; RAG process/job/grant/attempt authority; one atomic finish. No replacement global control plane. | P17-1 §1–3; P17-3 §1A; DBML. |
| OD4 — stale intent, Private veto, description hash | PASS. Expected source/reset/authorization/policy CAS, independent Private configuration and exact confirmed hash rule are normative. Confirmation never renews consent. | P17-2 §1; API §16; verification B04/B05. |
| OD5 — cancel-first conflicts and uncertain availability | PASS. Reserved relevant mutations return 409/no C, no queued mutation; unknown consumers/writers retain reservation and slot. Unrelated items remain editable. | ADR-0020 decision 2; P17-3 §1A.1/4/7; ACTIVE. |
| OD6 — ordinary edits/OUTDATED/manual regeneration | PASS. Participation/configuration retained; monotonic source/scope changes permanently stale old generations, including round trips. Committed exclusion immediately denies derivatives. | P17-3 §1/§2/§3; P17-2; B05/B13/B17. |
| OD7 — original security, ownership, offline and phase scope | PASS. NSFW/Private/unknown/secret exclusions, field matrix, provenance, dependency/purge and least privilege remain. No later product enabled. | ADR-0019/0020; P17-1/2/3; B14/B16/B19/B20. |

The prior review's four approval parts are all represented: OD2 supplies E1/lifetime; OD3 supplies the bounded owner/API/schema/trigger delta; OD4 supplies consent/description; OD5 supplies cancel-first/availability. OD1/6/7 constrain their functional scope and retained protections. Requiring expectedSourceVersion as well as reset/epoch/policy protects observed approval intent; it does not revoke standing consent on an ordinary edit.

ADR-0020 is an explicit focused amendment, not a claim that the original strict timing/boot/revoke rules were satisfied. ADR-0019 labels the amended clauses and retains its context, alternatives, RAG-first sequencing and separate later-phase gates. ADR-0016 root envelopes/module-local adapters and the permanent single-account decision remain unchanged. Existing rag -> vault/fiction/film public edges remain; no owner imports RAG internals. Owner-local trigger -> Vault metadata SQL responsibility is expressly authorized and does not grant Python access or create a Java module cycle.

## 4. Exact expiry and published-index lifetime assessment

| Approved amendment | What is now permitted | What is still forbidden |
| --- | --- | --- |
| E1(a) | A timely read/send/work gate can precede transfer/consumption after expiry under suspension/buffering; elapsed residual may be unbounded. | Passing a gate that observes expiry, implicit renewal, reservation TTL release or consumption after an earlier authoritative exclusion C. |
| E1(b) | Sensitive raw-free isolated untrusted S can physically commit late, including an after-final-check pause. | Treating S as trusted, fresh expired verification/promotion, bypassing attempt fences or claiming raw-free means anonymous. |
| E1(c) | A timely final Java gate can precede late **trusted VERIFIED/P physical COMMIT**. This is real publication. | Relabelling P quarantine, claiming later denial makes it timely, or waiving source/consent/provenance/cancellation guards through C. |
| Independent index lifetime | A valid completed persistent index, including valid late P, can remain usable after its worker lease/service session/boot. | Serving OUTDATED/revoked/unknown-source results, inheriting stale approvals, or making an ad-hoc expired selection artifact permanent. |

P17-3 distinguishes transaction checks, deferred execution, physical C and response/ACK. It retains post-wait/default-deferred expiry rejection, authoritative PG time and nonrenewing local budgets. The after-last-check physical-C gap is accepted explicitly; neither timeout nor another read is presented as strict E2 proof.

IndexCurrent freshly requires live/right-type/non-NSFW/NON_PRIVATE sources, valid item/field consent, matching reset/epochs/policies/source versions and full dependency scope, active nonrevoked generation and legitimate independent retention/selection. It excludes before scoring/counts/content. Historical SUCCEEDED is separate. Normal restart preserves fresh consent/indexes; only unfinished execution credentials are boot-bound. Selection-session expiry remains distinct from worker execution expiry.

These are the exact owner-approved timing, lifetime, consent and cancel/revoke availability changes. There is no additional NSFW, Private, field, source-integrity, provenance, offline or transitive-freshness waiver.

## 5. Consent, reservations, finish and recovery

| Counterexample / boundary | Synchronized required outcome |
| --- | --- |
| Allowed -> NSFW -> Allowed, both notifications lost | Atomic retained source/reset updates invalidate captured consent even with equal bytes; only explicit fresh CAS renewal can approve. |
| Old approval delayed across reset/revoke/field reduction | Compare all owner-observed expected values under source/policy guards; mismatch 409 SOURCE_CHANGED/no renewal. Missing expected fields never mean latest. |
| Ordinary eligible edit or hash/edge remove/readd | Preserve participation but advance source/affected scope version. Old generation remains OUTDATED; equal current bytes/topology cannot revive it. |
| Changed description selected for manual reindex | Explicit exact current-hash confirmation, atomically bound to fresh source proof/admission; unchanged confirmed bytes need no repeated prompt. No consent renewal/field or child expansion. |
| Owner transaction starts before admission | Transaction start/cache is insufficient. Common guarded mutation and reservation serialize; owner-first exclusion C denies input, reservation-first edit conflicts without C. |
| Indirect/bulk/cascade write already holds owner row | Mandatory owner-local backstop, OLD/NEW affected sets and NOWAIT earlier guard; rollback entire conflicting command, never skip selected sources. |
| Java/connection dies with Python/buffered input alive | Durable exact whole-closure reservation remains; TTL/disconnect/ACK/health cannot release it. No live-controller timeout takeover. |
| Worker dies while S backend C is pending | Positive process death alone is insufficient. Finish obtains attempt fence after source guards, drains prior writers, seals all routes; queued late writer reloads closed state and denies. |
| Cancellation races P or launch/send tasks | Stop-request attempt-only TX ends before other actions. Stop wins: P denies. P wins: report actual completed outcome. Join tasks and prove actual child/tree death before release. |
| Unknown admission/finish C or restart | No input on unknown admission; no callback replay/automatic finalization. Resolve actual guarded DB state. Unknown stays nonterminal/reserved; otherwise abort unfinished work after process/task/writer safety. |
| Publication versus source release | P-or-abort, staging seal, terminal marker and exact token/set release share **one physical C**. No async/autocommit/REQUIRES_NEW release or release-first interval. |
| Physical erasure fails after writers are impossible | Access stays denied and purge PENDING. Safe edit release and verified erasure are separate; never falsely claim PURGE_COMPLETE. |

P17-1/3 require MANDATORY same-writable-physical-transaction reserve/lock/release and fresh projections after ordered guards. The lock order is graph where needed -> sorted stable sources -> Vault lifecycle -> owner projections -> RAG policy -> attempt -> generation/staging/run. Workers acquire attempt -> staging only. Ambient dispatch rejection precedes external effects; existing compatible owner composition cannot use stale ORM state, savepoint escape or hidden independent commits as proof. No HTTP/hash/process wait under source SQL guards.

Native supervision is still RAG-owned and bounded to one job. Actual controller/worker creation identity, confinement before input, noninheritable handles, no breakaway/unreviewed descendant, real endpoint/capability binding and positive stop/wait are explicit. A stale listener, port collision, kill request, idle ACK or new JVM UUID cannot establish safety. Unknown outcomes retain only the affected reservation set and single active slot. Recovery is abort/cleanup, never automatic resume/resend/retry/reindex. Declared restore/uncertain history requires suspension/reconciliation/renewal.

## 6. API, schema, ownership and B01–B20 traceability

The additive DBML deliberately describes logical required fields, reuse and constraints rather than executable DDL or certified physical mappings. Vault source ID/type/history has no deletion cascade/reuse or reverse RAG FK. RAG reuses sources/job_leases/worker_grants/staging/dependencies/generations/runs. One complete static nonterminal constant-key unique constraint, one-use claim, process identity and attempt seal suffice; no F/V/DRAIN/H/ticket/global slot table is introduced. Old unbound approvals must fail closed on migration. V1–V13 and frozen Schema v1 remain unchanged; authorized implementation must append after V13.

API §16 explicitly amends existing candidate actions and safe views; it does not assert those DTOs already exist. Current registration DTO/controller inspection confirms implementation work remains. Final URI/DTO/configuration mapping and safe preview, validation, authentication, OpenAPI/error translation and backward compatibility must be proved in implementation. Registration is not approval; approval cannot implicitly change Private configuration. No new query/classification subsystem is authorized by this review. Worker SQL remains limited to audited own-grant claim and scoped raw-free staging; no canonical job/slot/policy/reservation/pointer/DDL authority. Effective definer/PUBLIC/inherited/legacy privileges require actual-role proof.

Every acceptance row is present once, with the prior independent review's concrete subcases retained:

| Cases | Independently confirmed obligation |
| --- | --- |
| B01/B02 | Original post-issuance owner-C/real Python gap, both orders and Java/DB/buffer failure; zero unauthorized input/receipt, not just P denial. |
| B03 | Selected/unrelated, bulk/import/cascade/OLD-NEW/shared input guards and complete rollback. |
| B04/B05 | Lost-event resets, stale approval CAS, identity/overflow, description confirmation and ordinary OUTDATED semantics. |
| B06 | Static one-job constraint, one body claim, stale/wrong listener/endpoint, no active-slot eviction. |
| B07/B08 | Pending SQL after death, every legacy writer, actual-role seal orders, suspended spawn/late tasks/handles/descendants/failed kill. |
| B09/B10/B11 | Atomic physical outcome, unknown ACK, no replay/live takeover, completed-index normal restart versus unfinished abort/restore. |
| B12 | Expired observed gates/post-wait/deferred rejection plus distinct permitted after-final-check late S/trusted P and persistent valid index. |
| B13 | Full scoped dependency/field/version freshness, hash/edge round trips, no reapproval resurrection, unrelated index independence. |
| B14/B15 | Exact stage/digest/kind/producer, effective privilege denials, dirty ORM/ambient/savepoint/trigger/NOWAIT rollback. |
| B16/B17 | Quoted restrictive metadata/unapproved children/disabled files; previous CURRENT preservation, manifest-specific cleanup and erasure failures. |
| B18/B19/B20 | Useful single-job resources, supported enabled native offline proof, complete suites/static/architecture/roles/inventory and FR17-11. |

The expectation map names actual expiry-lock/deferred, outer transaction, restart, lost-ACK/session and completed-job cancellation tests. It preserves ordinary post-wait/deferred denials while adding the separately permitted E1 case; splits normal restart preservation from stale execution/restore denial; distinguishes cancel-running-job from revoke participation; preserves previous CURRENT generations. Frozen authentication assertions cannot be changed or retried to green. FR17-1–11/R1–R8 traceability remains; mapping does not close findings.

The one-job benchmark profile retains 100 nodes/depth 5/1 MiB and total 3s/5s/10s DB ceilings, bounded output/history and <=15m execution lease. No reservation TTL. Five warmups and >=100 sequential cold runs per representative fixture require 100% normal authorized success and zero security violations, plus one competing-start conflict. Old 16-job/12-of-16/F-hold targets are explicitly removed. Actual cold-start/job/stop/CPU/RAM/disk/handle/connection/tombstone evidence and measured thresholds remain future acceptance work, not approved measured results.

## 7. Contradiction and historical-evidence reconciliation

**No unresolved blocking contradiction found.** Current normative P17-1/2/3, ADR-0020, owner decisions, API/data inventories, README, roadmap and ACTIVE express the same approved target and separate implementation gate.

Apparent conflicts were checked rather than silently adopted:

- Former F/H/V/E and E2 recommendations are explicitly historical; both proposal bodies remain intact apart from their banners. No old pending decision is reintroduced as a current gate.
- ACTIVE's historical submissions/reviews, owner-decisions' 2026-10-13 acceptance and earlier changed-file counts are attributed records. Current opening sections govern status/authority; no new handoff or reset to NO_ACTIVE_HANDOFF is directed.
- The unchanged implementation report/test evidence describe the contained prior candidate and its failures. Their original contract-decision diagnosis is history, not a new rejection of ADR-0020. Current canonical documents expressly separate that evidence from the newly approved target.
- Generic future pipeline references to stale leases/session expiry are read with the explicit E1 execution-gate and independent selection-retention definitions. They do not reinstate physical-C-before-deadline, completed-index work TTL or blanket startup revocation. Future parser/query/judge consumer protection remains separately gated.
- Existing topology exports remain candidate context. Alternative B changes lifecycle/authority without changing those edges; the Structurizr annotation does not claim a newly implemented or rendered lifecycle diagram. No export was altered in this synchronization.

The ADR amendment preserves unchanged exclusion, provenance, full dependency, offline and worker-privilege requirements. Cancel-first changes **whether an edit can commit during indexing**, not denial after a successful exclusion C. E1 changes time semantics, not source authority. Persistent lifetime changes which credential expires an index, not its fresh authorization predicate.

## 8. Independent documentation validation and preservation

Commands executed from repository root included scoped UTF-8 reads, `git rev-parse HEAD`, `git status --short`, `git status --porcelain=v1 -uall`, scoped `git diff`/`git diff --numstat`, recursive file/descriptor inventory and SHA-256 comparison of `git ls-files -c -o --exclude-standard -z` file bytes. The existing Graphify graph was queried only for navigation; its budget-limited results were not contract evidence and it was not rebuilt.

Fresh independent checker and whitespace command:

```powershell
.\rag-engine\.venv\Scripts\python.exe logs/phase17-alternative-b-contract-review-2026-10-15/validate_contract_review.py
git diff --check
```

Submission validation passed: **470 local Markdown links, 19 heading fragments, zero missing targets/anchors; exactly B01–B20 once each; exact 26-modified/four-added claim; ACTIVE CHANGES_REQUESTED and contract-only authority.** No application test count is claimed. Git whitespace checking exited 0; ordinary CRLF-to-LF notices were present and no warning-free claim is made.

The prior review's 1,940-file snapshot plus its independently recorded report hash supplies the 1,941-file pre-synchronization state. The new 1,945-file review-start snapshot matches the claimed 30-file delta. All 1,915 other prior files are unchanged, including production, tests, migrations, frozen DBML, historical reviews, implementation/test evidence and containment. Explicit protected checks cover 1,389 backend main files, 123 backend test files, Python source/tests/lock metadata, all 13 prior Phase 17 review-directory files and both evidence records. Current delivery against owner HEAD contains 199 changed/untracked files before this report, not merely 30; those inherited changes are not attributed to synchronization.

This review adds only this report to nonignored delivery. Final validation passed: all **1,945** review-start hashes unchanged, exactly this one report added, 1,946 files and 200 delivery paths; **502 local links and 22 heading fragments** including this report, zero missing; 29 ledger rows and zero report whitespace errors. The independent receipt records the report hash and results at ignored `logs/phase17-alternative-b-contract-review-2026-10-15/validation.json`. No canonical document, ADR, owner decision, ACTIVE, historical review, code, test or migration is changed by the reviewer.

## 9. Scope-aware 29-dimension ledger

PASS below evaluates contract synchronization or explicitly specified proof obligations, not unexecuted implementation. Runtime NOT_VERIFIED items are mandatory later gates, not documentation defects or waivers.

| # | Dimension | Status | Evidence / limit |
| --- | --- | --- | --- |
| 1 | Owner intent/acceptance/scope | PASS | Seven owner decisions match all four approved parts; bounded Windows/manual/one-job Foundation. |
| 2 | Requirement traceability | PASS | Approval matrix, B01–B20, FR17-1–11/R1–R8 and exact changed-test map. |
| 3 | Architecture/ADRs/diagrams | PASS | ADR-0020 explicitly amends 0019; target/candidate distinction, unchanged topology. |
| 4 | Module/public ownership | PASS | P17-1; existing public edges, Vault metadata and RAG lifecycle, no worker business SQL. |
| 5 | Repository/package/folder structure | PASS | Recursive 61-file RAG inventory, 12 descriptors/zero gitkeep; future additions remain in existing owners. |
| 6 | Shared DTOs/errors/exceptions | PASS | API §16 preserves ADR-0016 and specifies typed 409/CAS/confirmation changes. |
| 7 | SOLID/cohesion/dependency direction | PASS | State/guard owner distinct from process/policy orchestration; no reverse dependency. |
| 8 | DRY/KISS/YAGNI | PASS | Canonical version/backstop; existing grants/jobs reused; no parallel control plane. |
| 9 | Pattern fit | PASS | Durable reservation, CAS, partial uniqueness, native adapter and attempt fence retain concrete purposes. |
| 10 | Domain states/idempotency | PASS | Consent versus config/freshness; sealed terminal outcome and no replay/resurrection. |
| 11 | API/wire compatibility | PASS | Existing actions amended explicitly; final DTO/OpenAPI mapping remains implementation verification. |
| 12 | Validation/bounds/hostile inputs | PASS | Exact field matrix and metadata/child/type/missing/overflow/provenance denial obligations retained. |
| 13 | Authentication/authorization/privacy | PASS | Private/NSFW categorical; reset-bound consent/CAS; no authentication waiver. |
| 14 | Credentials/leakage/retention/purge | PASS | Worker privilege separation, raw-free still sensitive, verified purge and selection lifetime. |
| 15 | Transactions/concurrency | PASS | Common guards, NOWAIT fallback, physical admission/finish, E1 explicitly replaces strict time rule. |
| 16 | Failure/recovery/operations | PASS | Positive tasks/tree/writer proof; uncertain stays reserved; abort-only recovery. |
| 17 | Database/migrations/permissions | PASS | Logical target/additive post-V13 mapping and real effective-role/trigger tests required. |
| 18 | Actual resource consumption | NOT_VERIFIED | B18/profile specifies caps and measurements; no desktop workload executed. |
| 19 | Retrieval/recommendation quality | N/A | No query/model/recommendation capability or quality decision in this contract delta. |
| 20 | OCR/document/multilingual fidelity | N/A | No parser implemented; live files disabled and later fidelity/source-proof gates retained. |
| 21 | Adversarial security testing obligations | PASS | All 20 cases and prior counterexample details retained; no runtime pass implied. |
| 22 | Test quality/determinism | PASS | Actual role/process/COMMIT barriers, both orders, bounded cleanup and preserved negative assertions required. |
| 23 | Measured benchmarks/thresholds | NOT_VERIFIED | Single-job success profile is prospective; measured thresholds required before acceptance. |
| 24 | Evaluation science | N/A | No parser/model/judge selected; P17-N1 first-capability gate retained. |
| 25 | Runtime dependencies/licenses/offline artifacts | NOT_VERIFIED | No fresh dependency/native audit; B19/evidence gates explicitly remain. |
| 26 | Build/static/CI obligations | PASS | Full Java/Python/Ruff/mypy/architecture/migration/role checks retained, none run here. |
| 27 | Errors/observability | PASS | Safe typed conflicts/pending, historical job versus effective index and verified erasure distinguished. |
| 28 | Documentation/governance | PASS | All 30 inspected; explicit owner amendment, valid links, existing handoff and historical precedence. |
| 29 | Hygiene/delivery preservation | PASS | Exact delta, SHA-256 protection, diff check and report-only review delivery. |

Ledger: **23 PASS, three N/A, three NOT_VERIFIED, zero FINDING**. There is no runtime acceptance assertion behind these contract-level PASS dispositions.

## 10. Remaining implementation gates and exact next owner action

The following remain **NOT_VERIFIED runtime obligations**, not reasons to reject this documentation-only synchronization:

1. Inventory every maintained direct/indirect owner/policy/dependency mutation; implement actual guard/backstop/version/CAS semantics and prove rollback, reset history, description confirmation and cache/ambient/savepoint behavior.
2. Implement/prove real PG one-job uniqueness, one-use grant, actual endpoint binding, effective runtime/worker/definer privileges, all staging routes and physical P-or-abort/seal/terminal/release under unknown outcomes. Review exact additive migrations and seeding; no legacy approval inheritance.
3. Prove native Windows controlled launch, handles/controller/child/tree identity, terminal tasks, positive termination, no stale listener/takeover and pending SQL writer fencing. Actual recovery must fail closed without falsely releasing reservations.
4. Prove E1 observed-expiry negatives and permitted late physical transitions separately; completed-index lifetime/fresh checks, OUTDATED/round trips, manual-only regeneration, previous-index preservation and manifest-specific purge.
5. Execute the meaningful successful single-job resource profile and choose measured desktop thresholds before implementation acceptance. Exercise low disk/history growth, stop/recovery/purge failures and bounded requests without timer-based safety claims.
6. Prove the actual enabled supported native Java/Python/PG workflow under authorized external IPv4/IPv6/DNS/proxy denial with attempted-connection capture, actual dependency/native license/version evidence and no runtime downloads/telemetry. Existing disconnected/contained smokes are insufficient.
7. Run focused then clean full Java/Python/static/architecture/migration/role/repository verification with exact counts/exits/failures and finite cleanup. Diagnose the existing clock/environment authentication failure without frozen-auth edits, weakened assertions or lucky retries. Obtain a NEW independent implementation final review.

Live external-file R7 capability remains disabled; no power-loss, exhaustive bundled-library, native firewall or performance guarantee is inferred from this review. Later ingestion/query/judge/recommendation consumers remain independently gated and need their own actual source-use lifetime proofs.

**Exact next owner action:** separately authorize implementation of the accepted ADR-0020/P17-1/2/3 Alternative B contract under the **existing P17-RAG-FOUNDATION handoff**, referencing this contract acceptance, the current remediation plan and B01–B20. State any execution/containment-change authority explicitly; contract acceptance itself enables nothing. No further architecture decision or new handoff is required. Keep ACTIVE CHANGES_REQUESTED and FR17-5/8/9/10 open until the authorized implementation and independent runtime acceptance gates are satisfied.
