# Phase 17 RAG Foundation — Codex final re-review 2

- Date/reviewer: 2026-10-14 / Codex.
- Gate: `P17-RAG-FOUNDATION` was `IMPLEMENTED_AWAITING_CODEX_REVIEW`; implementation mode is valid.
- State: uncommitted remediation on `main`; HEAD/local `origin/main` = `24e6268eef1685866d919d882ecdf9f532d01961`.
- Verdict: **CHANGES_REQUESTED**. **FR17-1–10 remain OPEN / PARTIALLY RESOLVED (8 High, 2 Medium); FR17-11 remains CLOSED.**
- Authority: [existing handoff](../../handoffs/ACTIVE.md), accepted [owner decisions](../owner-decisions.md), ADR-0019 and P17-1/2/3 design contracts. Accepted preparation remains accepted; this is a runtime implementation verdict.

This report supersedes the current implementation verdict in [re-review 1](2026-10-14-phase-17-final-codex-re-review.md). Preserve that report and the [initial final review](2026-10-14-phase-17-final-codex-review.md). Findings retain their IDs; successfully repaired subcases are credited below. No new handoff, production or checked-in test code, commit or publishing was performed by Codex.

## Independent checks

| Check | Observed result and limits |
| --- | --- |
| Backend, from `backend/`: `mvn -ntp clean verify` | **PASS: 1,191 tests, 0 failures/errors/skips**, 04:37, finished 15:41:39 +07. Actual Surefire testcase nodes agree: RAG **37**, architecture **37**, full **1,191**. Ignored receipt: `logs/phase17-final-re-review-2/full-verify.log`. The earlier authentication instability is preserved in re-review 1; no auth changes or new failure in this run. |
| Python, existing `rag-engine/.venv` | Required pytest/Ruff lint/Ruff format/mypy commands all passed: **17 tests**, one Starlette/httpx deprecation warning; 10 files formatted, six source files type-checked. Python **3.14.4**, FastAPI **0.143.0**, Pydantic **2.14.1**, Uvicorn **0.54.0**, pytest **9.1.1**, Ruff **0.17.0**, mypy **2.4.0**. Submitted FastAPI/Pydantic/Uvicorn versions are still wrong. |
| Runtime counterexamples | Ignored `Final17LatestProbe.java`, `java-probe-complete.log`, `worker_boundary_probe.py`, `python-probe.log` in `logs/phase17-final-re-review-2/`. Disposable real PostgreSQL 18.6-alpine, current Spring beans, actual worker role, native NTFS files and a real loopback HTTP result fixture. Python cancellation uses a bounded barrier around the actual stage function, not mocked authorization. Fake text/identities only. |
| Changed tree / hygiene | Recursive IDE inventory: 53 RAG Java files, 12 populated packages, zero missing descriptors; six RAG test classes and six Python source modules/three test files. Generated/cache/venv/log/class artifacts remain excluded from the delivery inventory; `git diff --check` passes. FR17-11 remains closed. |
| Static diagnostics | Eleven critical Java/SQL/Python files: **0 errors, 17 warnings, 12 weak warnings**. Material: transactional self-invocation, unused production execution method and unused dependency repository. Also unused imports, duplicate traversal/cleanup, optional collection/clamp changes. SQL datasource and custom-header warnings are configuration/recognition issues, not reasons to rewrite valid SQL. No warning-free claim. |
| Frozen boundaries | No diff to frozen DBML, V1/V2, POM, review skills/hooks, historical Phase 15/SBA artifacts or authentication source/tests. Owning source projections/guards and additive RAG V3–V5 are authorized handoff changes. Architecture tests pass. No Frontend, Phases 18–22 product, Ideaverse, model/OCR/parser-runtime or deployment implementation found. |
| Unverified required gates | Exact pgvector-enabled PostgreSQL 18.6 environment, actual OS-level IPv4/IPv6/DNS/proxy-denied offline operation, measured resource/latency budgets, dependency license/advisory inventory and complete original R1–R8 recovery/purge/last-check races. These are not PASS. |

Java/Maven remain 25.0.2 / 3.9.15, Boot 4.1.1, Modulith 2.1.1, Testcontainers 2.0.5, Docker engine 29.8.1. The existing Graphify graph is unchanged navigation context; important facts were verified in current source. Only ignored audit fixtures and review/status documents were written.

## Verified improvements

- Native guard constructor injection now enforces the configured byte limit in the actual Spring context: a 100-byte fixture under a 50-byte limit rejects. Static ancestor-junction, local NTFS and remote-drive checks were added; success-path handles cover admitted external children through transaction completion.
- Newly admitted jobs store a bounded transitive child manifest. The new tests correctly reject an NSFW grandchild and changed external child in their serial arrangements. Finalization refreshes locked source/lease entities and captures time after principal source/lease locks. Public Vault guards now require a writable caller transaction.
- V5 removes worker staging-table/token/sequence reads and direct DML. An actual worker connection cannot read staging and can successfully invoke the token-scoped definer function. Nonempty/type/UTF-8 byte/root epoch/field-version checks improve the staging route.
- Normal fresh-context field reduction now invalidates source/ancestor generations and purges staging. UUID cancellation normalization and UTF-8 payload limits pass independent Python probes. Owner enumeration now uses real offsets/stable ID order; reverse-child and lease-source indexes were added.
- Basename mapping removes directory paths from source HTTP responses. Cancellation HTTP is deferred beyond transaction completion, although rollback semantics remain wrong.

These improvements do not close a finding whose remaining design-contract or runtime obligations still fail.

## Remaining blocking findings

Java application paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; SQL lives in `backend/src/main/resources/db/migration/`. Every correction is within the existing foundation authority. Preserve frozen owners/history and applied migration history; request a separate approved architecture change if a proposed fix would exceed that delta.

### FR17-1 — High — partial multi-file acquisition leaks a native guard

**Evidence:** `rag/internal/application/RagFinalizationGate.java:163–181` opens all handles before registering cleanup. If the second acquisition throws, the first never gets a transaction callback or lexical cleanup. An independent writer held the second external child open; finalization correctly rejected, but writing the first file still failed **after rollback and release of that writer**: `EARLIER_NATIVE_HANDLE_LEAKS_AFTER_GATE_ROLLBACK=true`.

**Consequence:** a failed job leaves an OS lock until process exit, blocking editor writes and leaking native resources. The previous root success-path lifetime fix remains valid, but failure ownership is broken.

**Correction/proof:** register ownership per successful acquisition or close all acquired handles on every failed acquisition/registration path; retain them through COMMIT only on the successful path. Consolidate native failure cleanup, require all mandatory bindings/attribute/drive checks to succeed, and verify approved root/ancestor identity without a path-stat/open race. The current `WindowsNativeFileGuard.java:160–193` accepts unknown drive/attribute outcomes and does not protect ancestor checks with handles. Add actual second-handle failure, native-failure, post-check replacement/reparse, already-open writer/mapping, hard-link and unsupported-volume receipts. No reproduced ancestor-replacement bypass is claimed here; that required proof remains missing.

### FR17-2 — High — substring veto is not fail-closed body/metadata authority

**Evidence:** `RagSourceService.java:197–215,393–407` accepts all readable .txt/.md content after a short substring blacklist. It performs no reviewed body/frontmatter/comment/embed split and treats unrecognized metadata as safe. Independent approved-and-published fixtures included `private:  true`, `visibility: 'private'` and `private: [true]`. The first expresses a restriction missed by whitespace matching; the latter cases must not positively certify classification. Root/child publication reuses this same helper.

**Consequence:** restrictive or unknown classification, unreviewed frontmatter/comments and unsafe body components can pass the NON_PRIVATE gate. Removing the generic metadata field does not remove metadata bytes from whole-file content.

**Correction/proof:** separate registration/version hashing from authorization to consume body bytes. Disable unsupported .md/metadata activation until a reviewed safe split exists; do not implement Phase 18 parsing to work around this gate. Any bounded foundation adapter must quarantine unknown/malformed/conflicting classification and emit only approved regions/fields. Test whitespace/quoting/unknown values, excluded frontmatter/comments/embeds, exact outbound fields and both authorization gates. Preserve the repaired domain allowlist/hash/description checks.

### FR17-3 — High — a complete grant/result contract is still optional or absent

**Evidence:** V5 adds `dependency_manifest TEXT NOT NULL DEFAULT ''` without invalidating existing active V4 leases. `RagFinalizationGate.java:131–147` silently skips malformed tokens and treats absent/empty manifest as no children; its dependency repository is now unused. A synthetic **V4-shaped lease using the V5 default**, with an existing NSFW child, still published. This is a legacy-state fixture, not a claim that an actual owner database was upgraded. Newly built manifests improve current admissions but do not make unknown historical grants trustworthy.

Also `:230–242,388–413` validates nonblank/size and an optional caller digest, then publishes without verifying a worker result's output digest, exact granted payload/fields, attempt/output identities or session-bound provenance. `RagJobService.java:318–322` stores the original caller input, not verified returned output. A real loopback response with a matching job/attempt but `status=REJECTED` still led to durable staging and publication.

**Consequence:** incomplete/restored grants and non-results can become active generations; source metadata is not proof of what generated the staged artifact.

**Correction/proof:** version and strictly parse the immutable complete grant, explicitly fence unknown/legacy jobs on upgrade/restart, and compare a typed verified result bound to job/attempt, exact source/field/revision/session manifest and output digest. Persist output provenance/receipt rather than accepting arbitrary text. Test malformed/default legacy manifests, missing/forged/changed result scope, rejected/duplicate/lost acknowledgements and strict output binding. No product parser/model is needed.

### FR17-4 — High — the safer SQL function does not complete identity/credential isolation

**Verified repair:** worker global token/payload reads, direct stage writes and all-sequence grants are revoked; V5's function/trigger correctly checks the root lease token, time, classification, epoch and field version.

**Remaining evidence:** V3 `:143–146` still creates/resets a committed known worker password and only changes the password of an existing role. Existing unsafe role attributes/memberships are not refused/hardened. Java/Python configuration retains committed service/worker defaults; separate migrator/non-owner application provisioning is absent/unproved. V5 functions still need an explicit PUBLIC-execute/owner/membership privilege audit. The actual Python runtime no longer has DB wiring, so its restricted-role staging integration is not demonstrated. Trigger scope checks root RAG policy only, not full child/session/owner authority; complete scoped input/result checks are absent at the application boundary.

**Correction/proof:** provision distinct external secrets/roles, validate pre-existing attributes/memberships and function grants, enforce the complete scoped grant at the worker boundary, and exercise the actual configured non-owner identities. Retain the single bounded definer route. Prove cross-job/forged grants, unsafe existing roles, actual staging/purge post-check COMMIT barriers and denied business/security/DDL/pointer access. Do not reintroduce global worker reads or rewrite applied history.

### FR17-5 — High — blacklist authentication and ignored worker responses remain unsafe

**Evidence:** `rag-engine/src/ppv_rag/worker.py:22–32` labels a few literal tokens forged; all other nonempty strings pass without checking issuance, source scope or expiry. A nonexistent job with a random unissued token returned STAGED. `RagEngineClient.java:69–98` treats any HTTP status below 400 as success and discards the response body/digest. `RagJobService.executeSyntheticStage` is unused by production entry points, performs HTTP then persists the input, and self-invokes its transactional staging method. A real loopback REJECTED result published; admission itself performed zero engine calls. There is still no started Java/Python/durable-stage E2E route or verified safe input projection.

**Consequence:** text shape and transport success are confused with authority and successful computation. The new helper is not a completed production workflow.

**Correction/proof:** replace the blacklist with actual scoped lease verification; return/validate typed status, version, job/attempt, grant and output proof before staging. Connect an authorized bounded synthetic entry path with fresh field-only input and the intended restricted persistence identity. Fix transaction self-invocation/ownership. Require external keys and safe typed errors/redacted DTO strings; basename mapping alone does not fix native raw-path diagnostics or token-bearing record strings. Test actual service E2E, arbitrary random forged tokens, expired/scope-mismatched grants and every non-success response without any persisted output.

### FR17-6 — High — cancellation acknowledgement, full purge and recovery are not established

**Evidence:** fresh-context field reduction works, but the cached consent counterexample in FR17-7 still publishes removed description data. `main.py:72–99,107–111` records cancellation while work continues and never rechecks before returning its result: a bounded actual-stage barrier showed CANCELLED acknowledgement, `quiescent=false`, then STAGED. `RagPurgeService` is unchanged: SQL deletion immediately sets PURGE_COMPLETE; no engine quiescence receipt, independent session expiry, retryable artifact cleanup, restart or restore anti-replay exists. The new /quiescence endpoint is not consumed by Java/purge and counts all workers, not the affected attempt.

`RagJobService.java:304–312` sends cancellation on **rollback as well as commit**. A real callback probe observed engine cancellation even though the canonical transaction rolled back and the job remained ADMITTED.

**Correction/proof:** acknowledge durable cancellation/denial separately from verified cleanup; fence in-flight results and obtain attempt-specific quiescence/deletion receipts before full completion. Notify only for the committed canonical transition. Implement the handoff's minimal session expiry, orphan/failed deletion recovery and restore/default-deny reconciliation with synthetic artifacts. Test both commit orders, rollback, late I/O, independent session TTL, lost notification/restart and restored consent replay. No later history/judge product is authorized.

### FR17-7 — High — authority mutators still reuse cached state and graph checks are not globally serialized

**Verified repair:** the main finalizer refreshes locked RAG entities, time is read after principal locks, and public Vault guards require a writable caller transaction.

**Remaining evidence:** `RagSourceService.approveSource:171–173,218–269` locks but does not refresh the policy row. In an outer transaction preloading title-only approval, an independent transaction approved description and minted/staged a grant. The outer title-only reduction reused the cached epoch/version, missed the reduction branch and wrote the **same** epoch as that description grant. After commit, current fields were only title, yet the old description grant published. This directly proves lost authority versioning, not merely a cache-warning hypothesis.

`:332–390` locks only the two edge endpoints while validating an entire graph. With existing B→C and D→A, disjoint concurrent A→B and C→D both completed checks before a disposable INSERT barrier, then both committed a cycle. Recursive depth helpers have no cycle/visited/memo protection. Pair sorting does not establish the graph invariant or the complete revoke/purge lock protocol.

**Correction/proof:** refresh or use fresh projections for every locked approval/revoke mutation, atomically preserve monotonic epochs and compare exact grant fields as defense in depth. Serialize complete graph validation/mutation with a simple RAG-owned protocol and bound traversal. Apply consistent closure ordering to all affected paths. Regress the cached mutator/removed-field publication and disjoint-edge cycle, and add actual last-authority-read→COMMIT, deadline-after-all-waits, savepoint, timeout/deadlock and physical-transaction identity cases. Preserve all existing owner behavior/SBA fixes.

### FR17-8 — High — tests and evidence still substitute easier cases for the accepted obligations

**Evidence:** the new expiry test `RagLifecycleIntegrationTest.java:417–438` sets expiry in the past before finalization; it contains no wait/interleaving. The worker forged-token test uses an exact blacklist literal. No cached policy-mutator, disjoint-edge, partial native acquisition, in-flight cancellation, rejected-result/legacy-grant or actual-role post-check purge regression exists. The R1–R8 evidence still relabels R3 as SQL staging and R8 as architecture, rather than accepted generate/revoke and field reduction. Its 29 rows remain a different all-PASS checklist. Domain tests still do not pause the real final gate after its last owner read; worker fence tests remain serial, and native tests lack the required physical finalization/crash/namespace barriers.

**Correction/proof:** implement the original handoff's R1–R8 foundation equivalents using controlled synthetic artifacts; preserve their identities, exact production boundaries and risk-oriented assertions. Add failing-before/passing-after cases for the new probes, actual worker vs purge in both orders, source last-check barriers and lifecycle/egress receipts. Correct all environment/closure/performance assertions and use the canonical 29 statuses. This is implementation plus tests, not a test-only slice; no frozen authentication edits are authorized.

### FR17-9 — Medium — some caps/indexes/pagination improve, but bounded execution is incomplete

**Verified repair:** configured native file cap, UTF-8 Python/SQL payload cap, owner offset/order and reverse/source lookup indexes.

**Remaining evidence:** a lease can accumulate arbitrarily many 1 MiB stage rows; finalization loads all payloads with `findByJobIdAndAttemptId`. Selected-source/queue/attempt/total-stage/temp-disk/hash/lock/transaction/deadline budgets are absent. Whole-file `Files.readString` reopens/rehashes work inside source DB locks. Owner `findSources` adds a bulk entity fetch but still invokes per-ID refresh/Vault/assignment queries, and finalization uses individual lookups. Recursive graph depth repeats paths and fails on the concurrently admitted cycle. No credible queue-inclusive latency/peak resource/SQL-count receipt exists.

**Correction/proof:** enforce validated count/total-byte/queue/deadline/disk budgets at admission and under the lease fence, reject over-budget before loading all outputs, use bounded cycle-safe traversal with memoization where needed, and actually batch fresh owner/assignment reads. Avoid duplicate file work under DB guards while maintaining same-version authority. Measure p50/p95, lock wait/hold, file I/O, peak memory/disk and SQL counts on the stated desktop. No speculative broker, dual engine or frozen-wide optimization is warranted.

### FR17-10 — Medium — reproducibility, offline operation and architecture inventories remain incomplete

**Evidence:** V3 still conditionally skips vector; tests/probes use plain 18.6-alpine and report no vector extension. Compose retains mutable `pgvector/pgvector:pg18`; exact 18.6/extension/digest compatibility is not proved. README's `uv pip install` does not enforce the project lock while claiming network isolation; no OS-level egress-denied runtime or new-dependency license/advisory receipt exists. FastAPI/Pydantic/Uvicorn evidence remains stale. C4 removed the worker SQL edge and Python removed DB configuration, while accepted P17-1/handoff still requires restricted worker staging integration; changing the diagram does not complete that workflow. Current data/owner/root/tree instructions and affected DOT/diagram exports remain stale.

**Correction/proof:** supply the accepted real boundary first, then synchronize current inventories/exports with it; any architecture-contract change needs owner review rather than silently deleting an edge from a diagram. Provide locked install/start instructions, exact compatible DB/extension/runtime receipts, real offline/security evidence and bounded dependency/license/advisory diagnostics. Preserve historical records and deferred product/deployment scope.

## Complete canonical 29-dimension ledger

| # | Dimension | Status | Evidence / practical limit |
| --- | --- | --- | --- |
| 1 | Owner intent/acceptance/source selection/non-goals | FINDING | Foundation scope preserved, but explicit source/result/session authority incomplete; FR17-2/3/5/6. |
| 2 | Requirement/contract/test traceability | FINDING | R1–R8 substitutions and wrong all-PASS checklist; FR17-8. |
| 3 | ADR/C4/source/schema/workflow consistency | FINDING | Additive ADR-approved module remains valid; diagram removal does not fulfill worker contract and current inventories drift; FR17-10. |
| 4 | Module ownership/named interfaces/cycles/encapsulation | PASS | RAG leaf only uses approved public vault/fiction/film contracts; 37 architecture tests pass. Data graph cycles are separately failed under 15/17. |
| 5 | Recursive repository/package/folder structure | PASS | Actual 53-file/12-package RAG tree/descriptors, owner source contracts and Python/test subtrees inspected; FR17-11 remains closed. |
| 6 | Shared HTTP/envelope/exception contract placement | PASS | Root ApiResponse/ApiResponses retained per ADR-0016; cohesive module-local DTO/controller/advice, no duplicate envelope/new common module. Safe messages assessed under 14/27. |
| 7 | Concrete SOLID/cohesion/boundary contracts | FINDING | Real owner/facade/native roles appropriate; void worker adapter cannot express rejection and execution stores input as output; reproduced FR17-3/5. |
| 8 | DRY/KISS/YAGNI/immutability/determinism | FINDING | No speculative framework; duplicate graph algorithms and permissive string manifests/blacklists violate authority invariants; FR17-2/3/7. |
| 9 | Patterns/anti-patterns | PASS | Native Adapter, module Facade and Repository serve concrete roles; no events/broker/generic agent framework or ServiceImpl hierarchy. Improvements need no new pattern platform. |
| 10 | Domain correctness/state/idempotency/edges | FINDING | Cached removed-field publication, legacy child omission, rejected computation and cancellation rollback reproduced; FR17-3/5/6/7. |
| 11 | Public wire/API/version/errors/pagination/provenance | FINDING | Offsets/order fixed and 224-route inventory green; unverified response/version/result scope and output provenance remain; FR17-3/5. |
| 12 | Hostile inputs/format/path/size validation | FINDING | Byte limits improve; unknown/restrictive metadata publishes and native failure/path proof incomplete; FR17-1/2/9. |
| 13 | Authentication/authorization/privacy/consent/privilege | FINDING | Owner/API and real SQL privilege positives useful; unissued token acceptance, cached field authority and metadata bypass remain; FR17-2/4/5/7. |
| 14 | Credentials/leakage/retention/egress/supply chain | FINDING | Known defaults, token-bearing DTO strings/native path diagnostics and lifecycle gaps; actual egress proof NOT_VERIFIED; FR17-4/5/6/10. |
| 15 | Transactions/freshness/lock identity/order/COMMIT races | FINDING | Finalizer refresh/mandatory Vault guard improves; mutator epoch reuse, concurrent graph cycle and handle failure leak reproduced; required last-check barriers missing. |
| 16 | Failure/leases/cancel/restart/restore/recovery | FINDING | Cancel returns before quiescence, notifies on rollback; no independent session/recovery/anti-replay contract; FR17-6. |
| 17 | PostgreSQL/Flyway/JPA/constraints/permissions/query shape | FINDING | V1–V5/real-role function succeed and indexes improve; legacy live grants, role provisioning and graph invariants remain; FR17-3/4/7/9. |
| 18 | Time/memory/I/O/lock/hardware/admission complexity | FINDING | Unbounded aggregate staging/queues, per-ID refresh queries and repeated recursion/file reads; FR17-9. No GPU/model work applicable. |
| 19 | Retrieval/ANN/reranker/answer/recommendation quality | N/A | Those products are later phases. Corpus/grant/result safety is assessed under 10/13/15, not excused. |
| 20 | Extraction/OCR/layout/multilingual/locator fidelity | N/A | Actual Phase 18 ingestion is unauthorized. Current unsafe external approval is failed under 12/13; no parser fidelity PASS. |
| 21 | Adversarial/security/concurrent/OS/worker tests | FINDING | Fresh real probes fail security/concurrency boundaries not covered by 37+17 green tests; FR17-8. |
| 22 | Risk-oriented test/E2E/assertion/determinism/fixtures | FINDING | Fabricated real-PG tests useful; named wait test is serial and token test mirrors blacklist; real pipeline/recovery/after-check coverage absent. |
| 23 | Hardware/resource/latency benchmark credibility | NOT_VERIFIED | No measured queue-inclusive p50/p95, lock/hash budgets or peak memory/disk/volume receipt; mandatory FR17-9 gate. |
| 24 | Held-out labels/evaluation leakage/judge calibration | N/A | Actual ML evaluation deferred; accepted P17-N1 remains later preparation obligation. Synthetic safety is mandatory under 21/22. |
| 25 | Dependency compatibility/licenses/advisories/lock lifecycle | NOT_VERIFIED | Lock exists/current versions measured; exact vector environment, reproducible enforced install and license/advisory checks absent; FR17-10. |
| 26 | Build/static/format/test/tooling/architecture evidence | FINDING | Independent 1,191/17 and required tools pass; material IDE self-invocation/unused execution warnings and unsupported submitted coverage assertions remain. |
| 27 | Useful safe logging/errors/metrics/incident operations | FINDING | Aggregate metrics/quiescence endpoint added; not an attempt-specific cleanup proof, raw-path diagnostics and false purge-complete semantics remain. |
| 28 | Docs/ADR/governance/history/status/links/inventories | FINDING | Review history preserved/status synchronized; all-CLOSED claims, versions, original coverage and source/export inventories require correction; FR17-8/10. |
| 29 | Delivery hygiene/artifacts/frozen scope/clean diff | PASS | Descriptors/egg-info fix retained; generated/cache/log files excluded, no frozen DBML/old migration/POM/history or deferred implementation delta. |

**Totals: 5 PASS, 19 FINDING, 3 N/A, 2 NOT_VERIFIED.** Critical applicable privacy/authority/worker/recovery proof is still failed or missing; passing suite counts cannot authorize readiness.

## Proportionate implementation priorities

1. Repair canonical authority first: fresh locked consent mutation, monotonic epochs, strictly versioned complete grants and one concurrency-safe bounded graph protocol. Put recurring graph/closure logic inside RAG ownership; no cross-owner repository access or generic common module.
2. Give every native handle one explicit cleanup owner from acquisition through failure/COMMIT. Consolidate repeated close/log branches and verify namespace checks without releasing/reopening authority.
3. Connect one bounded synthetic worker route with real lease verification and typed verified results. Use the existing SQL fence; no blacklist, arbitrary caller-input publication, disconnected execution placeholder or new workflow platform.
4. Separate synchronous deny from committed notification and verified retryable purge. Add minimal independent session/restore/restart fencing before later product work.
5. Enforce aggregate budgets and batch actual fresh reads; benchmark these paths before choosing optimizations. Retain the useful pagination/index fixes; remove unused repository/import/overload debris once the real route is coherent.

**Next step:** Antigravity `/antigravity-implement-handoff` completes FR17-1–10 implementation and required regression/runtime receipts under the existing active handoff, then returns to `$codex-final-review`. No test-only remediation or owner commit approval. Phases 18–22, Phase 16 and Ideaverse retain separate gates; frozen authentication and closed SBA scope remain untouched.

