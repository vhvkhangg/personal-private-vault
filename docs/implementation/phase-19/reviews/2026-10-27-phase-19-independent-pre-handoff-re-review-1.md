# Independent pre-handoff re-review 1 — Phase 19

- **Date / reviewer:** 2026-10-27 / Codex, independent of the preparation/correction authorship.
- **Disposition: CHANGES_REQUESTED.** P19-PR-02, P19-PR-03 and P19-PR-04 are **CLOSED for preparation**. P19-PR-01 remains **HIGH / OPEN**, narrowed to the incident-to-reader-release handoff below. One LOW wording item is nonblocking.
- **Authority:** Owner invocation of $codex-pre-handoff-review; [P19-OD-01 option A](../owner-decision-p19-reader-exclusion.md) is explicitly approved **prospective design**, not implementation permission. No reconfirmation of option A is requested. Current [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md) and [ACTIVE](../../handoffs/ACTIVE.md) still govern execution.
- **Baseline:** local main/HEAD and observed remote main remain **685b869a9d789c6e0a42198d1f59bce62b7e0613**. No tracked or staged changes. At entry there are eight untracked preparation documents and the original review. Source, tests, migrations, build/lockfiles, skills, agents, hooks, config and ACTIVE are unchanged.
- **Preserved review:** [initial review](2026-10-27-phase-19-independent-pre-handoff-review.md), SHA-256 **cc1f33dd406b2fa03f416b7b6a8000d5accd8292cc7a6af007882be64dbfe591**, verified unchanged from the preceding invocation's final artifact. Its dated OPEN dispositions remain historical; this report records subsequent adjudication.
- **Counters / phase state:** preparation re-review 1, **not** an Antigravity remediation round. Implementation **NOT_STARTED, 0/3**; no takeover. Phase 17 FROZEN, Phase 18 CLOSED, Phase 19 implementation unapproved; indexing INACTIVE and live originals DISABLED/DEFERRED. No milestone is required before P19; the next RAG milestone remains after P19.

## Finding adjudication

| Finding | Independent disposition / evidence |
| --- | --- |
| **P19-PR-01 HIGH** | **OPEN, narrowed.** Option A resolves the material Owner choice. The durable source/dependency reader, normal 409/cancel-first behavior, emission boundary and no-TTL release are now specified. The emission-first incident path still lacks a durable, ordered handoff to denial before admission can resume. See below. |
| **P19-PR-02 MEDIUM** | **CLOSED for preparation.** R19-10, Q20/M16 and the whole-file section explicitly distinguish source-level denial from internal Private/NSFW/hidden markers; approved roots and children retain individual whole-file consent. This agrees with ADR-0022 N6/WF-1/WF-6 and ADR-0023. Runtime positive/negative consent tests remain NOT_RUN. |
| **P19-PR-03 MEDIUM** | **CLOSED for preparation.** R19-08 and the selection contract require an Owner-declared source-ID/selected-revision roster, check actual child/span provenance before ranking or metadata, reject unseparable mixed chunks, preserve distinct identities and keep generic mode free of a global CORE filter. No new server folder classifier or Ideaverse schema is assumed. Runtime mapping tests remain NOT_RUN. |
| **P19-PR-04 MEDIUM** | **CLOSED for preparation.** Q03 is a mandatory answerable Han case. The 24-slot protocol requires all M01–M16, at least six E cases and zero listed hard violations; positive-case abstention is a miss. Deterministic citation checks and finite semantic support assessment are separate. Development/held-out source groups and labels must be frozen before the two-model comparison. No quality result is claimed. |

These closures accept corrected **preparation contracts**, not source implementation, model quality, a deployed privacy guarantee or a waiver of future required tests.

## Remaining P19-PR-01 — durable incident handoff at emission

**Locations:** query-consumer-lifetime-and-selection.md:10–13 and 32; owner-decision-p19-reader-exclusion.md:19. The protocol says an incident that loses to emission waits until C_E/terminal exclusion release before its physical commit, while C_RL closes the reader/exclusion after quiescence. It does not specify persistent pending-incident state, which side owns the handoff, or how release and denial are ordered without a gap. Ordinary 409 edit/revoke is correctly specified and is not the issue.

**Source evidence:** RagRecoveryService.java:179–218 performs actual snapshot integrity reconciliation and updates the denial flag at lines 207–208 only when **reserved_by IS NULL**. RagPurgeService.java:198–205 and its cleanup paths separately invalidate or erase generations. Existing external reservation functions protect the source row but do not register a pending incident or own HTTP emission. Therefore merely reusing the reservation token and waiting does not implement the proposed incident behavior.

**Concrete allowed interleaving in the current draft:**

1. Reader A is EMITTING. An integrity/authority incident affecting its source or child is observed; the draft defers its C_D.
2. A completes emission and cleanup, then C_RL releases the exclusion.
3. Before the incident transaction commits, another admission can obtain the source guards. The draft has no durable pending-incident predicate for it to reject. With direct reservation reuse, the existing reconciliation update can instead have affected zero rows while the source was reserved.
4. A crash or failed/unknown database operation before the delayed C_D also has no specified incident handoff to recover. Proving the old reader/sender dead must not by itself erase that pending authority uncertainty.

This is a missing protocol branch, not a claim that the unimplemented Q&A endpoint has leaked content. A wait performed **while holding** the graph/source SQL guards would also prevent C_RL from acquiring those guards; a wait performed **outside** them needs durable handoff state. The correction must choose the latter without losing the incident. Existing ADR-0022 §§“Real incidents, normal revoke and recovery” distinguish actual C_D from observation and preserve unresolved-inspection recovery; do not replace them with an in-memory flag or a logged exception.

**Minimum bounded correction:** make the emission-first incident intent durable under the existing ordered guards, bound to the exact affected source/revision/reader closure, without prematurely claiming C_D or retracting emitted bytes. Retain the necessary exclusion until a short guarded finish can apply the authoritative denial **together with**, or before a guarded handoff that prevents new admission after, terminal release. Resolve an unknown outcome from PostgreSQL; preserve both unresolved reader and incident state until known. All sender/process waits occur outside SQL transactions. A pre-inspection protocol that demonstrably prevents the same gap is an acceptable equivalent. Use the already approved minimal reader state or existing incident state; no generic queue or second state platform is requested.

Document how source integrity reconciliation, source policy/dependency changes, generation invalidation/purge and reader recovery reach that same boundary. Normal busy mutations must still commit **none** of the requested change. A mandatory incident is not an ordinary mutation to silently drop on BUSY.

**Finite proof additions within R04/R05/R07/R09:** incident-first versus EMITTING-first; a source and a transcluded-child incident; a new admission placed exactly between attempted reader release and C_D; crash/unknown commit after durable incident intent; and a stalled sender requiring cancel/positive quiescence. Assert no fresh admission/cleanup completion across unresolved denial, no early release, and no SQL-lock wait spanning HTTP/process work. These are future implementation tests, not a requirement to implement them during docs repair.

**Authority:** this is completion of option A's stated incident/recovery invariant inside its minimal durable-state boundary. It does not require the Owner to choose A again. If the proposed remedy instead weakens accepted C_D/currentness semantics or exceeds that approved boundary, provide that exact prospective amendment for Owner decision.

## Source-based minimum integration map

This map identifies feasible ownership and the required guard touchpoints. It is a review result, not an executable handoff, installed SQL or permission to edit the listed code.

| Boundary | Existing primitive / minimum prospective integration |
| --- | --- |
| Durable reader | One RAG-owned content-free record with immutable full source/revision/generation closure and guarded state transitions; one admitted Q&A request is sufficient initially. No indexing lease masquerading as a query and no persistent chat. |
| Admission / final authorization | Reuse RagDependencyGraph's short ordered transaction guard, RagGenerationState's predicates and RagSourceAuthority.lockPreparedCurrent. Prepare expensive hashes outside locks, then recheck the captured proof and bind the reader in the authoritative transaction. An already returned state string is insufficient. |
| External source reservations | V18 rag.external_file_require_editable, external_file_reserve, external_file_lock_reserved, external_file_release and guard_external_file_sources_mutation. Direct reuse needs deliberate handling of the 100-source single-token scope and incident updates; it is not a ready-made arbitrary-size reader registry. Query corpus limits remain distinct from ingestion-job limits. |
| Policy / dependency backstops | Latest rag.guard_source_policy in V17 and rag.guard_dependency_change in V22 must enforce the prospective reader overlap through the same authority, including direct SQL. Do not broaden a bypass to normal policy mutations. |
| Java source / integrity paths | RagSourceService.snapshotPlan, verifySnapshotPlan, approveSource, revokeSource and addDependency; RagRecoveryService.reconcileSnapshots. Scope ordinary BUSY and incident handling separately; explicitly check an attempted denial's affected-row/commit outcome. |
| Generation lifetime / purge | RagPurgeService.invalidateGeneration, eraseGeneration and related retired-generation cleanup plus RagRecoveryService callers. Protect the exact contributing generation/closure and prove consumer quiescence before destructive completion; do not let a terminal indexing lease certify the Q&A reader. |
| Worker / recovery | Reuse NativeWorkerProcess ownership/positive-stop primitives and the supervisor's fail-closed recovery principles. RagWorkerSupervisor.recoverStopped accepts indexing leases; the query reader needs its own matching identity/reconciliation. The unchanged 2-GiB ingestion profile is not a measured generator profile. |
| HTTP sender | Keep one bounded non-streamed response and a writer lifecycle owned by RAG. Acquire the guarded EMITTING transition before the first body write; hold the durable exclusion through write/flush and completion/failure, then quiescence and guarded release. A controller-return callback alone is insufficient. |
| Migration / privileges | Highest migration **in source** is V23. A future additive migration after the verified actual last-applied version can supply the minimal RAG reader/guard delta. No migration was run and database Flyway history was not inspected. Keep worker rights unchanged and no broader business-schema rights. |

Because P19 accepts external Markdown evidence only, prefer an entirely RAG-owned reader delta. Vault/Fiction/Film public boundaries remain frozen; no new domain-source support or reverse owner dependency is justified. Any genuinely required owner guard change must be named and justified, not inferred from the contract's generic reference to “Vault/external-file owners.”

**HTTP feasibility limit:** synchronous bounded serialization/write with a request-scoped outer lifecycle is the smallest candidate to validate. If asynchronous handling is used, the writer's true completion must own release: Spring documents that filters can return while an async response remains open, and disconnect notification is not automatically available. See [Spring MVC processing/disconnect documentation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html). Select and verify a finite sender-abort/write mechanism on the pinned runtime; a standard connector connectionTimeout is documented for request-line/body reads, so it must not be asserted to prove response-write quiescence. See [Tomcat HTTP Connector](https://tomcat.apache.org/tomcat-11.0-doc/config/http.html). No Tomcat/Spring upgrade, global connector change, live-client experiment or successful native sender test is claimed here.

## Necessity, effort and finite delivery

The initial review's full **R19-01–16** capability inventory, existing-source inspection and KEEP/REUSE/SIMPLIFY/REMOVE/DEFER matrix remain applicable: no product function was added by this correction. Changed considerations are explicit below.

| Mechanism / obligation | Decision / simplest adequate scope | Relative implementation / verification effort |
| --- | --- | --- |
| Option A reader / source guards | KEEP minimum durable record and exact guards; reuse existing primitives where correct | High at the new lifetime boundary / real DB and sender races |
| Incident handoff | KEEP one durable pending-denial/finish relationship; fix the branch above | Bounded extension of the same reader / finite R04/05/07/09 cases |
| Canon selection | SIMPLIFY to explicit revision-bound Owner roster plus per-contributor filtering | Medium / deterministic identity and transclusion fixtures |
| Whole-file consent | REUSE existing external policy; no internal-marker redaction | Low / paired positive and denial tests |
| Evaluation | KEEP 24 frozen questions plus separate required boundary tests; at most two CPU models | Bounded / measured in authorized implementation, not this review |
| Pipeline, REST ownership and process safety | REUSE pinned 384d embedding and existing root envelope / RAG-local adapters / native ownership | Focused changes / only affected contracts and architecture |
| Extra agents/hooks/DB, live files, OCR/GPU, judge, UI, catalog or Ideaverse integration | REMOVE/DEFER from P19 | No current implementation or benchmark burden |

Prefer the existing **two separately committable slices**: S19-1 authorized retrieval/reader/citation boundary, then S19-2 one offline CPU generator and end-to-end acceptance. Resolve the incident branch before publishing preparation or creating S19-1's handoff. Do not require a third acceptance-only slice or new public endpoint just to mirror technical layers.

Each slice retains Antigravity implementation/tests and mandatory self-review followed by independent Codex review; after all slices one phase-final aggregation, then Owner publication/closeout and the separately authorized P17–19 milestone. Three future remediation rounds apply only after an actual initial blocking implementation review. Existing backend-implementer, Python/bounded-command skills and expressly authorized takeover path remain sufficient; no harness change is justified.

Reuse the implemented watchdog: **300s** ordinary commands, bounded inactivity diagnostics; **up to 2,100s** only for eligible explicitly approved full suites. CPU model measurements, actual HTTP sender/races, database roles and quality remain future mandatory evidence. Unchanged P18/harness receipts are not re-executed for this documentation review.

## Nonblocking wording — P19-L01 / LOW / OPEN

source-reuse-and-feasibility.md:12 still says P19-OD-01 is pending although the current decision/header records approval. acceptance-matrix.md:61 attaches future failed model cases to READY_FOR_HANDOFF, while the same dossier correctly places model trials in later implementation. Clarify “approved design, feasibility pending” and the future model-selection/implementation acceptance gate. The controlling status is unambiguous and no test has failed or been claimed PASS, so this is LOW, not another readiness blocker. Consolidate with the next approved correction/milestone; do not start a LOW-only loop.

## Scoped coverage and evidence

The unchanged architecture/package/harness review is reused from the initial review at the identical source commit. All eight current preparation documents were read; the original independent review was hash-verified. New code inspection was limited to the selected source/SQL/HTTP integration paths above. No unrelated historical phases were reopened.

| Review dimension | Preparation disposition / current evidence |
| --- | --- |
| 1 Owner intent | PASS recorded option A; implementation remains unapproved |
| 2 Traceability | PASS PR-02/03/04 corrected; PR-01 incident case still FINDING |
| 3 Frozen architecture / ADRs | FINDING PR-01 C_D compatibility; no applied amendment |
| 4 Module ownership | PASS existing RAG ownership; no new domain edge |
| 5 Package tree | PASS unchanged subtree inventory reused; no source/package edits |
| 6 Shared API / exceptions | PASS unchanged ADR-0016 root envelope and local advice reuse |
| 7 SOLID / cohesion | N/A new implementation; narrow reader responsibility preserved |
| 8 DRY / KISS | PASS minimum record / reuse; no duplicate pipeline |
| 9 Patterns | PASS no generic job/provider framework |
| 10 State / business behavior | FINDING PR-01 emission-first incident handoff |
| 11 HTTP contract | NOT_VERIFIED future API; actual writer boundary identified |
| 12 Input / locators | PASS corrected roster/revision/span contract; runtime pending |
| 13 Authorization / privacy | FINDING PR-01; marker and canon preparation issues closed |
| 14 Credentials / retention / offline | PASS unchanged content-free/offline constraints; runtime pending |
| 15 Transactions / races | FINDING PR-01; source mutation/incident guard owners traced |
| 16 Cancel / recovery | FINDING PR-01 pending-incident recovery; reader stop tests NOT_RUN |
| 17 Database / grants | PASS feasible RAG-local delta mapped; actual migration/query NOT_VERIFIED |
| 18 Complexity / resources | NOT_VERIFIED generator/sender budgets; no new benchmark project |
| 19 Retrieval / grounding | PASS corrected finite preparation criteria; runtime NOT_RUN |
| 20 Markdown / languages | PASS whole-file and answerable VI/EN/Han cases; other formats N/A |
| 21 Security tests | FINDING PR-01 incident order must join finite race cases |
| 22 Test adequacy | PASS corrected positive/negative protocol; implementation proof pending |
| 23 Performance | NOT_VERIFIED later CPU/headroom/latency/cancel measurements |
| 24 Evaluation | PASS 24-slot finite rule and disjoint source groups; outputs not yet generated |
| 25 Dependencies / pins | PASS unchanged baseline and initial candidate metadata reused; installed generator NOT_VERIFIED |
| 26 Build / IDE / tooling | N/A new build for docs-only correction; no IDE-clean or static-clean claim |
| 27 Safe diagnostics | PASS no-content operational logging retained; runtime evidence pending |
| 28 Documentation / governance | FINDING PR-01; LOW P19-L01; scoped Docs Sync below |
| 29 Hygiene / delivery | PASS baseline/index unchanged and local links checked; no publishing |

**Checks actually performed:** Git status/diff/cached diff/log and remote ls-remote; original-review preservation check; complete current-dossier reading; numeric migration inventory (V21–V23 tail); targeted Java/SQL/HTTP source inspection; local link/heading audit **67 links, zero missing files or unresolved heading anchors at entry**. The correction manifest explicitly describes its seven-file overlay relative to the uploaded correction ZIP; that is not a complete Git-main publication inventory and is not treated as one.

**Not performed:** no Maven, pytest, Ruff, mypy, IDE inspection, DB query/migration, import/index, CPU inference, sender prototype or deployment. No actual P19 implementation evidence exists. Prior external corpus/model observations retain their original provenance; no new claim of corpus import or target-host runtime compatibility.

## Docs Sync Check and next action

| Disposition | Scoped result / owner |
| --- | --- |
| UPDATED | This separate re-review and preparation-review.md's current finding/status/next-step only. Original independent review and Owner decision preserved. |
| ALREADY_CONSISTENT | Option A prospective approval; whole-file consent; explicit canon roster; finite quality rules; main/remote baseline and NO_ACTIVE_HANDOFF. |
| NOT_VERIFIED | Mandatory future DB/native sender/model/quality evidence; not claimed as current runtime PASS. |
| BLOCKED_OWNER_DECISION | None merely to restate option A. Return to Owner only if the proposed incident remedy needs a wider or weakened frozen contract. |
| DEFERRED_OUT_OF_SCOPE | Old global/scoped notices, existing LOW backlog, external corpus verification and broad cleanup. P19-L01 is nonblocking; ChatGPT owns routine correction. |

**Correction boundary:** ChatGPT completes only PR-01's durable incident/terminal-release protocol and corresponding existing race expectations/current references; preserve P19-OD-01's actual approval and both independent reviews. Do not implement, run migrations, alter historical receipts or open ACTIVE. If a wider amendment is necessary, present that exact delta for Owner decision. Re-review the corrected branch and changed evidence; do not reopen the three closed preparation findings without regression evidence.

**Next step:** ChatGPT | bounded P19-PR-01 incident-handoff documentation correction, then Codex $codex-pre-handoff-review | docs/implementation/phase-19/query-consumer-lifetime-and-selection.md and this re-review | durable incident-to-release ordering resolved under approved option A; ACTIVE remains NO_ACTIVE_HANDOFF | implementation remediation NOT_STARTED 0/3; no takeover.
