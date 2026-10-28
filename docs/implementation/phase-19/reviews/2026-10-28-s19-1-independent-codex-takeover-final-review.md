# Independent slice review — P19-S19-1 Codex takeover

- **Date / mode / reviewer:** 2026-10-28 / `SLICE` / Codex, independently reviewing corrections authored by a different Codex implementation session. This reviewing session made no production or implementation-test corrections.
- **Disposition: CHANGES_REQUESTED / STOPPED / OWNER_DECISION_REQUIRED.** F02/F08/F09 now CLOSED. **F04/F06 HIGH and F05/F11 MEDIUM remain OPEN/blocking.** The previous early-release defects in F05 are corrected; its remaining contract deviation is MEDIUM, not an asserted reproduced privacy bypass.
- **Authority:** [bounded takeover authorization](../owner-authorization-s19-1-codex-takeover-2026-10-27.md), [executable handoff](../s19-1-executable-handoff-2026-10-27.md), [Option A](../owner-decision-p19-reader-exclusion.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md), [review skill](../../../../.agents/skills/codex-final-review/SKILL.md).
- **Entry:** main/HEAD `ead5291bdffee09e53d0b41957ea751231015db2`; 68 unstaged candidate paths, empty index. All 55 files in the implementer's final source manifest match their recorded SHA-256. No publication or operational migration is claimed.
- **Counters:** Antigravity **3/3 EXHAUSTED**. **One authorized Codex takeover implemented and independently blocked.** This review is not a fourth Antigravity round or authorization for another corrective attempt. The authorization requires: “If the bounded takeover remains blocked, stop and return the precise residual evidence and another Owner decision.”
- **History:** [initial review](2026-10-27-s19-1-independent-final-review.md), [re-review 1](2026-10-27-s19-1-independent-final-re-review-1.md), [re-review 2](2026-10-27-s19-1-independent-final-re-review-2.md), [re-review 3](2026-10-27-s19-1-independent-final-re-review-3.md) and the Owner decision remain unchanged. P17 FROZEN; P18 CLOSED; S19-2+ unapproved; indexing INACTIVE; live originals DISABLED/DEFERRED.

## User requirement and simplest-mechanism check

The candidate now performs actual approved Markdown retrieval through the existing P18 producer/publication, pinned CPU query child, PostgreSQL cosine ranking and real HTTP sender. Complete canonical authority guards, exact incident tuples, source/reset/revision binding, locator fidelity, eligible discovery and Unicode validation are substantial verified corrections. No generator, model/dependency upgrade, broader privilege or generic job framework was introduced.

The remaining defects concern the approved small reader/inspection mechanism. An incident revokes unrelated admitted roots. Standalone unresolved inspections are invisible to existing currentness/index-reservation paths and are not reconciled by their durable identity. The sender also replaces the executable contract's nonblocking design and explicit wire adapters without an Owner amendment. These are bounded corrections or an explicit Owner disposition; no new feature, full application suite, answer benchmark or expanded audit is requested.

## Finding register

Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/`; V24 is `backend/src/main/resources/db/migration/V24__rag_markdown_retrieval_readers.sql`.

| ID | Severity / blocking / status | Independent adjudication |
| --- | --- | --- |
| F01 | HIGH / no / CLOSED retained | External UUID and canonical identity digest remain distinct; the fresh actual producer path passes. |
| F02 | HIGH / no / CLOSED | `RagRetrievalCurrentAuthority:39–85`, `RagRetrievalAuthority:28–38`, canonical `RagSourceAuthority` batch guards and V24:67–276 bind complete source/reset/epoch/field/revision/manifest/pipeline/runtime authority in C_A/C_E. Six mutation-after-prepare cases and actual grandchild mutation pass. Unknown admission/emission does not transfer or emit evidence; wire-loss tests pass. A targeted admission-versus-currentness probe also succeeds: the graph lock serializes the suspected lock upgrade; no deadlock is claimed. |
| F03 | HIGH / no / CLOSED retained | Original recovery/inspection lock inversion remains fixed; no demonstrated regression. |
| F04 | HIGH / yes / OPEN | Exact observed incident tuples and unresolved retention are fixed, but V24:661–663 revokes **every** generation in the reader payload when any incident exists. Independent PostgreSQL probe: reader includes independent roots A/B; only A has an incident; after guarded finish B is `REVOKED`, inactive, while B's integrity flag remains false. See residual detail below. |
| F05 | MEDIUM / yes / OPEN, narrowed | The unsafe release-before-writer/context completion and launch/cancel ownership defects are corrected by the new sole owner, container event, retained handles and reconciliation. However `RagRetrievalService:248–256` performs unconditional blocking output rather than the expressly required nonblocking sender; :232–233 serializes application response records directly while the approved wire DTO adapters are bypassed. The candidate ADR documents the changed implementation but does not approve an amendment. No early release is reproduced by the fresh sender tests. |
| F06 | HIGH / yes / OPEN | Exact controller FILETIME/child stop proof and durable busy-incident registration are credited. Existing currentness/indexing guards still ignore standalone unresolved inspections, and reconciliation creates new inspections instead of resolving the old operation. Fresh PostgreSQL/source probe reproduces both bypass and persistent unresolved state. |
| F07 | HIGH / no / CLOSED retained | Runtime table DML remains revoked; trusted functions, ordinary wrong-token/release/terminal/delete backstops and existing runtime/worker privilege regressions pass. This does not waive F04/F06 defects inside authorized functions. |
| F08 | HIGH / no / CLOSED | `RagRetrievalService:879–1009` resolves original producer S through the sealed input manifest, validates exact contributor/field/contiguous constituent spans, uses last-included-code-point end-line semantics, and omits unverified heading. Fresh newline/CRLF/supplementary Unicode and actual producer/transclusion/duplicate-name cases pass. |
| F09 | MEDIUM / no / CLOSED | Root `ApiResponses.error` envelope, wire/facade code-point limits, streamed/declared ingress bounds and eligible CURRENT discovery replace the reproduced failures. Fresh selection/HTTP tests and 101 approved-unindexed-source regression pass. The public cap remains 100. |
| F10 | MEDIUM / no / CLOSED retained | Bounded native response consumption, no proxy/redirect, exact artifact/reader/PID/nonce checks and positive native/transport stop remain intact. Fresh real native flow and source-applicable protocol-negative receipts support this disposition. |
| F11 | MEDIUM / yes / OPEN, narrowed | Actual producer/native/HTTP multilingual, consent/roster/provenance, physical denial/release boundary, wire-loss and resource evidence now exists. Remaining mandatory R07 production reconciliation of an outstanding inspection is neither supplied nor successful in the probe; tests manually classify its UUID. The actual old-controller/server crash with that obligation is not established by the sleeping second-JVM fixture. See limits below; the earlier blanket missing-native/quality finding is superseded. |
| F12 | MEDIUM / no / CLOSED retained | Fresh Python/private-error and Java redaction checks pass; no question or excerpt is added to operational logs by this review. |

**L01 CLOSED retained. L02 LOW / OPEN / nonblocking retained:** meaningful `internal/web` root containing the ingress filter still lacks `package-info.java`. Defer to milestone/audit; no LOW-only work or counter extension is authorized. Other existing LOW backlog is unchanged.

### F04 — deny only truly affected generations

**Reproduction:** fresh disposable migrated PostgreSQL; real `rag_runtime`; complete valid synthetic source/generation manifests; two independent roots in one reader; one exact A/revision inspection classified INCIDENT_PENDING; sole finisher called with positive no-sender test evidence. The actual finisher returns successfully and changes independent B to `{status=REVOKED, is_active=false, snapshot_integrity_failed=false}`. No forged incident array, administrator mutation of reader state or application mock establishes this result. The shared test fixture fabricates publication metadata, so this is a SQL lifecycle proof, not an embedding-quality proof.

**Consequence:** a generic multi-source query can revoke a valid unrelated index; the existing reconciler may subsequently purge its derivatives. This contradicts exact incident scope and independent-sibling behavior. Correct file/revision denial does not make whole-reader generation invalidation correct.

**Minimal correction:** restrict generation denial to exact incident contributors and their true dependent generations, preserving unaffected admitted roots. Keep C_D/C_RL atomic and preserve affected-row/token/read-back guards. Extend an existing PostgreSQL incident test with independent A/B and an actual dependent parent; assert only affected roots/dependents become unreadable and B remains CURRENT/retrievable. No new architecture is needed.

### F06 — integrate and reconcile the actual outstanding inspection

**Locations:** V24:320–385 deduplicates only the supplied inspection UUID, not an already unresolved operation on the same tuple; existing V18 `external_file_reserve`/`external_file_lock_reserved` are unchanged. `RagSourceAuthority:279–398` and `RagGenerationState:47–136` have no pending-inspection predicate. `RagRecoveryService:45–94` considers readers/indexing leases at runtime takeover but no standalone inspection inventory; :261–319 creates fresh inspection UUIDs during each snapshot scan.

**Fresh probe, without an active reader:** preregister an unresolved exact file/revision inspection against a valid generation. `RagGenerationState.state()` returns **CURRENT**. The real runtime can acquire/release an ordinary external indexing reservation. Calling production `reconcileSnapshots()` verifies the bytes and writes a new CLEAN observation, but the old UNRESOLVED row remains. Registering the identical producer/scope/file/revision with another UUID creates a second unresolved row. Observed summary: `CURRENT; reservation=ACCEPTED; old unresolved=1; new clean=1; duplicate unresolved=2`.

**Consequence:** the mandatory unresolved/pending predicate is not shared by existing currentness/indexing/promotion/cleanup owners. A preregistration/recording interruption can also leave a source permanently blocked for retrieval despite successful clean reinspection; repeated operations consume bounded capacity rather than reconcile the durable identity. Retention alone is necessary but is not recovery. The probe does not claim that it ran a complete new P18 indexing job or operational activation; it directly demonstrates the currentness and reservation defects.

**Minimal correction:** integrate the exact pending/unresolved-source predicate at the existing authoritative owners and SQL admission/backstop boundaries; reconcile/reuse outstanding operation identities under exact guarded read-back before takeover/admission. Preserve all unresolved/incident history until a positively proven classification/disposition; no TTL, blind CLEAN update, release-first or wider privilege. Cover an inspection without a reader and one attached to a stopped reader, both interruption/unknown-commit paths, existing index admission/currentness/promotion/purge, and unaffected revisions/siblings.

### F05 — approved sender and wire boundary still differ

The executable handoff's **Actual sender, cancellation and recovery** section explicitly requires a “Servlet async/nonblocking sender” with output readiness/WriteListener. The candidate now uses blocking `ServletOutputStream.write/flush` on a virtual thread; it has no output-readiness protocol. Fresh deadline/disconnect tests correctly retain exclusion while an application write barrier is blocked, but then **the test manually releases that barrier**. They do not establish the prescribed nonblocking mechanism or bounded cessation of an uncooperative production write. This review does not confuse timeout/notification with release proof: the retention improvements are accepted.

Separately, [accepted ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md) explicitly rejects exposing application command/view records as the wire contract. The async success path serializes `RagRetrievalResponse` directly. `RagRetrievalResponseDto.fromResponse` is used only in the synchronous fallback, which now always rejects. The present JSON shapes match, but the intended adapter separation is absent on the enabled path. Root envelope ownership itself is correct.

**Minimal correction/disposition:** use the approved owned nonblocking sender and existing explicit wire DTO adapters; retain every corrected lifetime/stop/finish invariant. Alternatively, the Owner must explicitly amend the sender contract based on concrete bounded stop/transport proof before this different design can be accepted. The candidate ADR alone cannot authorize that choice. No generic sender framework or container upgrade is requested. Severity is MEDIUM because this is a confirmed contract/adapter deviation, while the previous HIGH early-release behavior is no longer reproduced.

### F11 — remaining mandatory proof and truthful mapping

`RagRetrievalConnectionLossIntegrationTest:137–173` proves actual rollback/ACK loss and retention, but cleans/classifies the inspection by calling `authority.recordInspection(inspection, ...)` from the test. It does not exercise a production owner that discovers and reconciles that interrupted operation. `RagRetrievalTakeoverIntegrationTest:160+` runs real Windows controller/child processes, but its controller fixture sleeps; administrative fixture code binds the child and models EMITTING. It proves exact process-identity stop gates, not a crash of an actual HTTP-owning controller with unresolved inspection.

Keep the supplied positive tests/measurements and extend the existing focused classes to prove the remaining original R07 path through actual producers/recovery. A missing classification must remain fenced, then reach a proven safe disposition through the production owner rather than test-only manual cleanup. Correct the handback's scope of R07 and current documentation after that proof. This is not a demand for new distributed resilience, a full suite or broader evaluation.

## Original acceptance and race matrix

| Acceptance | Result | Evidence / limit |
| --- | --- | --- |
| S19-1-01 | PASS | Fresh generic/explicit/empty HTTP and eligible-scope cases; disabled/contextless execution rejects early. Incident branches are separately failed below. |
| S19-1-02 | PASS | Fresh real P18 producer → publication → pinned CPU query → PostgreSQL ranking → actual HTTP VI/EN/Han expected passages. Small synthetic quality scope only. |
| S19-1-03 | PASS | Canon/noncanon roster, duplicate/path/revision cases and actual duplicate-basename fixtures. |
| S19-1-04 | PASS | Actual parent/child/grandchild contributors and reverse CORE selection; reject inseparable/malformed constituents before ranking. |
| S19-1-05 | FAIL | Tuple/reset/epoch/R/pipeline negatives pass; F06 standalone unresolved inspection still reports CURRENT. |
| S19-1-06 | PASS | Actual approved private/nsfw-marked whole-file positive, identical unapproved negative, separate child consent/selection. |
| S19-1-07 | PASS | Exact newline/CRLF/code-point spans, original producer S, real contributor citations; unverified heading omitted. |
| S19-1-08 | PASS | Empty/rejected/waypoint fixtures do not expose linked unapproved content; no semantic-answer claim. |
| S19-1-09 | FAIL | Ordinary reader-first mutation guards pass; F04 revokes unrelated valid generations at incident finish. |
| S19-1-10 | FAIL | Actual C_I/C_E ordering and atomic release tests pass for one affected root; F04/F06 violate complete scoped incident behavior. |
| S19-1-11 | FAIL / NOT_VERIFIED | Early release/unknown-stop retention corrected; F05 sender deviation and F06/F11 interrupted-inspection recovery remain. |
| S19-1-12 | PASS within slice | Fresh pinned native flow/Python checks; exact-source-applicable native protocol negatives reviewed/reused. No downloads, activation or broader offline certification. |
| S19-1-13 | PASS | Finite facade/DTO/ingress/scope/output bounds, canonical error envelope and one-reader busy behavior. |
| S19-1-14 | FAIL | Existing role/ordinary reader backstops pass; unresolved inspection without a reader does not fence ordinary index reservation/currentness. |

| Race | Result / scope |
| --- | --- |
| R01 | PASS: ordinary reader-first guard and stale mutation-first canonical admission cases. |
| R02 | FAIL: contributor/child consent tests pass, but F04 unrelated-root invalidation violates sibling independence. |
| R03 | PASS for supplied actual owned stalled child stop/Java join and fresh sender retention; control-vector tests are not native quality proof. |
| R04 | PASS for supplied actual HTTP incident-first and C_E-before-first-delegate-write barriers. No client-ACK guarantee. |
| R05 | PASS during blocked writer; broader incident completion FAIL under F04. No SQL transaction spans the test writer pause. |
| R06 | NOT_VERIFIED for prescribed nonblocking/production blocked-writer cessation; positive retention/native stop portions credited under F05. |
| R07 | FAIL / NOT_VERIFIED: wire-loss retention and exact process stop pass; actual outstanding-inspection reconciliation/crashed HTTP owner remains F06/F11. |
| R08 | PASS: reset/equal-hash new R/epoch/pipeline/grandchild stale authority and snapshot regressions. |
| R09 | Physical no-gap/affected-row/wrong-token portions PASS; complete incident scope FAIL under F04, pending-index guard FAIL under F06. |
| R10 | PASS for one-reader/no queue, ID replay, cancellation and live/unknown controller retention. Standalone inspection startup handling remains R07. |

## Verification and applicability

Fresh evidence root: `logs/phase-19/s19-1/codex-independent-takeover-review-1793142642248/`. Entry/source hashes, recursive tree inventory, preserved selected Surefire XML, probe source/classpath, receipt summary and boundary checks are retained there. All successful commands have exit 0 and capture/cleanup complete.

Every Maven invocation used native `.venv-d17/Scripts/python.exe scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <unique.json> -- mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<exact classes below> test`.

| Fresh receipt | Exact class selection / result |
| --- | --- |
| `authority-incidents.json` | RagSourceAuthorityIntegrationTest,RagSnapshotCorrectiveIntegrationTest,RagRetrievalIncidentIntegrationTest,RagRetrievalConnectionLossIntegrationTest,RagRetrievalTakeoverIntegrationTest — **85**, zero failures/errors/skips; **118.439s**. |
| `core-sender.json` | RagRetrievalSelectionTest,RagRetrievalIntegrationTest,RagRetrievalSenderIntegrationTest — **33**, zero failures/errors/skips; **46.595s**. |
| `native-quality-guards-architecture.json` | RagRetrievalEmbeddingNativeIntegrationTest,RagSourceReservationsIntegrationTest,RagSlice4LifecycleIntegrationTest,RagSlice4ReindexIntegrationTest,RagRuntimeRoleIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest — **74**, zero failures/errors/skips; **224.043s**. |
| `python-query.json` | Native venv `-m pytest -c rag-engine/pyproject.toml rag-engine/tests/test_query_embedding.py rag-engine/tests/test_embeddings.py rag-engine/tests/test_transport.py -q` — **14 passed**, **23.774s** watchdog / 19.63s pytest. |
| `python-lint.json`, `python-format.json`, `python-types.json` | Native venv Ruff check / Ruff format --check on query module + query tests, Mypy on query module, explicit pyproject config — PASS; **0.557s / 0.536s / 1.046s**. |
| `adversarial-probe-fixed.json` | `java.exe @<root>/probe-classpath.args com.vhvkhangg.personalprivatevault.rag.ReviewTakeoverProbeFixed` — harness PASS, **30.814s**; intentionally asserts/reports F04/F06 defects, not candidate acceptance. Actual guarded admission/currentness completes without deadlock. |

Fresh total: **192 distinct Java cases**, **14 Python tests**, three Python static checks. **11 additional native protocol cases are reused, not freshly rerun**, from implementer `codex-takeover-ec4aeab6925548ef98e03b3cf963efe3/native-stop-race-4.json` and its selected ProtocolNative XML: zero failures/errors/skips, combined command exit 0/223.322s. This review inspected their actual native peer/artifact/identity/stop setup, receipt and XML, verified all 55 final source hashes unchanged, and freshly exercised native quality/sender changes. Reuse is explicit; 203 is not the fresh reviewer count.

Fresh actual producer-to-HTTP query metrics: VI/EN/Han startup **2.921–3.074s**, native total **3.001–3.158s**, peak process **1,144,131,584–1,147,502,592 bytes**, below the 2 GiB Job cap. These are synthetic request measurements, not p95, production recall or whole-server latency certification. Existing positive/false-stop metrics are reviewed with their original limits.

Probe provenance: disposable PostgreSQL 18.6/pgvector, migrated/validated frozen V1–V23 then V24, actual isolated runtime role and production Spring owners; no operational database or Owner files. Fresh Java uses JDK 25.0.2 and the existing native Python 3.14.4/artifacts. No full application suite or later-phase evaluation ran.

Harness corrections are preserved honestly: initial `probe-compile.json` failed exit 2 because javac does not accept the Java runtime native-access flag; corrected compile passes. Initial probe's third experiment incorrectly entered a REQUIRES_NEW adapter from an already locked transaction, producing a harness-induced lock timeout. It is **not a candidate finding**. The fixed probe uses the same physical transaction as production `admitPrepared` and does not reproduce a lock defect. Both independent F04/F06 reproductions succeed in both probe runs; prior logs are not overwritten.

## Shared coverage and static diagnostics

| Rubric dimensions | Result / rationale |
| --- | --- |
| 1–2, 10 (intent, traceability, business) | FINDING: F04/F06, with original acceptance/race mapping above. |
| 3–4, 6 (architecture, ownership, shared contracts) | FINDING: F05 approved sender/wire separation. Module graph and root ApiResponse ownership PASS; 37 architecture cases pass; no new module or arbitrary common layer demanded. |
| 5 (recursive tree/grouping) | FINDING, LOW only: L02. Actually inventoried 140 RAG subtree files and 23 Python source files; inspected all changed packages, public command/view/exception/enum/interface grouping, web adapters, persistence and additive inventory. Existing named API and retrieval capability organization are coherent. |
| 7–9 (SOLID, reuse, patterns) | PASS within slice: canonical authority/native/reservation ownership reused; one concrete currentness bridge and owned sender, no speculative strategy/base/framework. Large retrieval class alone is not a new blocking finding. |
| 11–12 (API/validation) | FINDING: F05 enabled wire adapter bypass; current envelope/code-point/scope bounds and OpenAPI inventory PASS. |
| 13–17 (privacy, lifecycle, transactions, persistence) | FINDING: F04/F06. Exact tuple/role/source/native guards and unchanged frozen migrations credited. New reader/inspection entities match additive V24/DBML. No new business privilege, cross-module repository or hostile-administrator guarantee. |
| 18–20, 23 (bounds, retrieval/fidelity, measured resources) | PASS for finite Markdown/CPU slice: paged bounded provenance, exact PostgreSQL distance, native multilingual fixtures and measured cap. No OCR/other formats/large corpus or production performance claim. |
| 21–22 (adversarial/tests) | FINDING: F11 remaining mandatory production recovery path. Fresh SQL/source probes expose gaps despite green named suites. |
| 24 (evaluation science) | N/A: answer generation, judge/held-out answer evaluation and later catalog recommendation are unapproved. No new benchmark gate imposed. |
| 25–27 (dependencies, tooling, logs) | PASS within executed scope: pins/lock/native implementation unchanged; focused native/Maven/Python checks and safe diagnostics reviewed. Mockito self-attach/dynamic-agent/CDS, benign migration notices and SpringDoc-enabled notices remain visible, not suppressed. No IDE inspection or warning-free claim. |
| 28 (docs/governance) | FINDING: current contract/ADR sender conflict and overstated production R07 mapping; exact Docs Sync below. |
| 29 (hygiene/delivery) | PASS: review/status-only edits, unique ignored probe/evidence files, no index/publishing/protected-source drift; final boundary validation retained. |

## Scope-bounded Docs Sync Check

- **UPDATED:** this independent report, [ACTIVE](../../handoffs/ACTIVE.md), and the executable handoff's current review/result/routing sections only. Current disposition and counter supersede the implementer handback without rewriting its evidence.
- **ALREADY_CONSISTENT:** actual HEAD, empty index, original Owner scope, disabled defaults, model/dependency pins, unchanged V1–V23/module edges and additive schema/package inventory; all five entry-listed review/Owner-decision artifacts remain unchanged. No publication claim is accepted from a test result.
- **BLOCKED_OWNER_DECISION:** further bounded correction/disposition after the independently blocked takeover; F05's blocking sender design is not an approved handoff amendment. No code correction or acceptance follows automatically.
- **NOT_VERIFIED:** F11's actual outstanding-inspection/controller recovery branch and any future corrected source/publication. Handback/notes are implementer claims superseded by this review; their correction belongs to a separately authorized continuation, not silent review edits.
- **DEFERRED_OUT_OF_SCOPE:** root/current governance and preparation-era routing notices assigned to ChatGPT by the executable handoff; L02 and existing LOW backlog for milestone/audit. No repository-wide cleanup or reopening of P17/P18.

## Owner next action

No commit message or readiness is supplied. The exact remaining scope is **F04/F06 source/incident correctness, F05 sender/wire contract disposition, and F11 production reconciliation proof/documentation**, preserving all closed fixes and historical evidence. Any additional corrective implementation needs another explicit bounded Owner decision and subsequent independent review; do not start a fourth Antigravity round or an automatic second takeover.

**Next step: Owner | decide and explicitly authorize any further bounded correction or contract disposition | P19-S19-1 ACTIVE + this independent takeover review | no implementation/publication until that decision; separate independent review after any correction | Antigravity 3/3 exhausted; Codex takeover 1 independently blocked.**
