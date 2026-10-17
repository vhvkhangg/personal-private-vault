# Phase 17 RAG Foundation — Codex final re-review after FR17 remediation

- Date: 2026-10-14
- Reviewer: Codex; implementation-mode gate verified as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Handoff: `P17-RAG-FOUNDATION`; [existing active authority](../../handoffs/ACTIVE.md).
- Reviewed state: uncommitted implementation/remediation on `main`; HEAD and local `origin/main` both `24e6268eef1685866d919d882ecdf9f532d01961`.
- Verdict: **CHANGES_REQUESTED**. **FR17-1–10 remain OPEN / PARTIALLY RESOLVED: 8 High, 2 Medium. FR17-11 is CLOSED.** No owner commit approval or commit message.
- Scope: Phase 17 foundation only. Accepted preparation, ADR-0019 and P17-1/2/3 design decisions remain accepted. Their runtime implementation is incomplete. Phases 18–22, deferred Phase 16, Ideaverse and deployment remain outside this review/implementation authority.

This report supersedes the implementation verdict, not the historical evidence, in the [initial final review](2026-10-14-phase-17-final-codex-review.md). Existing findings retain their IDs. The implementer's all-CLOSED/all-PASS assertions in [test evidence](../test-evidence.md) are not accepted as the review result.

## Independent verification and limits

| Check | Actual result / receipt |
| --- | --- |
| First `mvn -ntp clean verify`, from `backend/` | **FAIL**: 1,181 tests, 1 failure, 0 errors/skips; `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary:145`. 04:43, completed 13:33:29 +07. Ignored local receipt: `logs/phase17-final-re-review/2026-10-14-full-verify.log`. |
| Focused diagnostic rerun | `mvn -ntp '-Dtest=RefreshTokenLifecycleIntegrationTest' test`: **9 passed**, 0 failures/errors/skips; 29.292 seconds. Authentication source/tests have no diff. Initial failure remains recorded; no authentication remediation was authorized or performed. Root cause of the instability is **NOT_VERIFIED**. |
| Repeated full verification | **PASS**: 1,181 tests, 0 failures/errors/skips; 04:30, completed 13:46:05 +07. Receipt: `logs/phase17-final-re-review/2026-10-14-full-verify-repeat.log`. The earlier failure must not be omitted from evidence. |
| RAG / architecture tests in full run | Six RAG suites contain **27 tests**; `ApplicationArchitectureTests` **37 tests**. Green results establish the behaviors actually asserted, not the missing R1–R8 interleavings. |
| Python required checks, existing `.venv` | `python -m pytest -q`: **12 passed**, one Starlette/httpx deprecation warning; Ruff lint and format checks passed (10 files); mypy passed (6 source files). Actual Python **3.14.4**, FastAPI **0.143.0**, Pydantic **2.14.1**, Uvicorn **0.54.0**, pytest **9.1.1**, Ruff **0.17.0**, mypy **2.4.0**. Submitted environment versions are stale. |
| Runtime probes | Ignored `logs/phase17-final-re-review/Final17ReReviewProbe.java`, `java-probe.log`, and `worker_re_review_probe.py`: real disposable PostgreSQL 18.6-alpine, current Spring beans, actual `rag_worker` connection and native Windows handles; fabricated source text/IDs only. No production or checked-in test code edited. |
| IDE inspection | 12 critical Java/SQL/Python files: **0 errors, 15 warnings, 10 weak warnings**. Reviewed unused imports/methods, obsolete digest parameter, optional collection/clamp suggestions, duplicate helpers/cleanup logs, unknown custom-header and SQL datasource warnings. Synchronization-owned native handle must not be converted to lexical try-with-resources before commit. No repository-wide warning-free claim. |
| Recursive structure/hygiene | Actual RAG, owning source contracts, Python source/tests and architecture tree inspected through IDE inventory. **53 RAG Java files, 12 populated packages, 0 missing descriptors**. No egg-info/cache/class/log additions in the unignored delivery inventory. `git diff --check` passed. |
| Frozen compatibility | Frozen DBML, pre-existing migrations, backend POM, historical Phase 15/SBA artifacts and authentication source/tests have no diff. Approved additive owner guard/projection, route inventory, RAG migration/config and architecture-inventory changes are within the handoff. No Phase 16/18–22 or Ideaverse implementation found. |
| Not demonstrated | Live pgvector-enabled exact-18.6 image compatibility; OS-level offline/egress isolation; measured resource/latency thresholds; license/advisory inventory; required recovery/session/purge interleavings. These remain acceptance gates, not PASS. |

Java/Maven remain 25.0.2 / 3.9.15; Spring Boot 4.1.1, Modulith 2.1.1, Testcontainers 2.0.5, Docker Desktop engine 29.8.1. PostgreSQL test image is 18.6-alpine. Graphify was queried for navigation, but its graph does not represent this uncommitted RAG implementation; canonical source was inspected directly. Tests and probes were bounded and disposable. No model/parser/OCR runtime was installed.

Final artifact checks covered six review/status documents, **81 existing local link targets / 0 missing**, all 29 canonical ledger rows and active `CHANGES_REQUESTED` status. IDE document build succeeded. The new report's three relative-path resolver warnings disagree with verified filesystem targets; two table-format suggestions concern alignment. These do not establish a broken target or a runtime defect. Final `git diff --check` passed. The unchanged `diagrams/source/container.dot` and `module-dependencies.dot` omit the new worker/module depicted by the modified C4/current module inventory; affected source/export synchronization remains FR17-10.

## Verified progress

Independent probes confirm that denied Fiction/Film fields reject, empty staged publication rejects, changed approved descriptions reject, the old delimiter hash collision is resolved, and an uncached old root epoch rejects. V4 denies worker reads of policy/lease tables and worker DELETE/UPDATE on staging, and requires a token at insertion. Recursive revocation invalidates an existing three-level parent generation; job cancellation invalidates that job's generation.

The native guard now hashes the opened handle, has no permissive NIO fallback, stores file identity, and registers release after transaction completion. A real outer-transaction probe blocked file rewriting after finalization returned but before outer COMMIT, then allowed rewriting after COMMIT. An attempted ancestor-directory rename was also denied in that tested interleaving. These are positive receipts; this review does **not** claim that ancestor rename bypassed the guard.

FR17-11's blocking package/artifact hygiene is resolved: all nine previously missing internal package descriptors exist and egg-info is removed/ignored. Remaining unused imports are small cleanup opportunities; unused stage/health methods are evidence of the unresolved workflow in FR17-5, not a second structural blocker.

## Remaining blocking findings

### FR17-1 — High — external-source guard covers one root file, not the complete approved authority

**Partial fix:** root handle lifetime through physical outer COMMIT and same-handle hashing are verified.

**Remaining evidence:** `WindowsNativeFileGuard.java:130–151` checks only the terminal Java symbolic-link path, calls `toRealPath`, and then opens the resolved target. It does not validate an approved root/ancestor identity or supported local NTFS volume. `RagFinalizationGate.java:120–145` opens only the root external file; external children get no native handle or live hash/identity check. An actual NTFS junction ancestor was accepted (`REPARSE_ANCESTOR_ACCEPTED=true`). A root with a changed external child published (`CHANGED_EXTERNAL_CHILD_PARENT_PUBLISHED=true`). Required conflicting-writer/mapping, hard-link, reparse, unsupported-volume, crash/failure and mixed-manifest tests remain absent.

**Consequence:** an external-child version can be authorized without live source stability; the claimed supported-volume/root scope is unproven. A safe positive root-handle test does not close this contract.

**Correction/proof:** build the bounded complete immutable manifest before locks; validate or reject unsafe root/ancestor/reparse/volume cases; acquire every required native handle in deterministic path order before DB guards, compare approved identity/hash, and retain them until physical completion. Disable unsupported live-file modes. Add independent writer-first/guard-first, post-check, mixed-source and native failure receipts. Do not introduce a parser/model or silent immutable-copy substitution.

### FR17-2 — High — external body/metadata consent still has a permissive fallback

**Partial fix:** exact domain field vocabulary, non-empty approval, incremented field version and root description checks; length-prefixed domain revisions replace delimiter concatenation.

**Remaining evidence:** `RagFieldAllowlist.java:27–29` permits generic `content` and `metadata` for every external file. `RagSourceService.java:192–201` approves any readable bytes without reviewed body/metadata separation, restrictive-metadata precedence or supported-format enforcement. A Markdown fixture containing `private: true` was approved with both fields and published (`RESTRICTIVE_METADATA_FILE_APPROVED_AND_PUBLISHED=true`). No reviewed external parser split exists, as expected for the phase non-goals.

**Consequence:** explicit approval is treated as sufficient even where the accepted matrix requires UNKNOWN/deny for unparseable restrictions or unreviewed metadata.

**Correction/proof:** keep candidate hashing/registration separate from permission to consume bytes. Deny activation for unsupported or unsafely separable formats/metadata until an explicitly reviewed capability exists; any narrowly supported synthetic/body adapter must enforce exact regions/fields and restrictive vetoes. Test private/unknown/conflicting metadata, unreviewed components and exact outbound approved fields. Do not implement Phase 18 parsers to hide this foundation gap.

### FR17-3 — High — root grant columns do not bind the actual staged output and complete dependency manifest

**Partial fix:** admission persists root epoch/field version/fields/content and description hashes; zero staged rows reject; immediate Fiction/Film children are checked for eligibility/content; caller digest no longer sets the stored digest directly.

**Remaining evidence:** `RagJobService.java:145–157` has no immutable child revision/epoch/field/session manifest. `RagFinalizationGate.java:139–157,242–278` uses the current immediate dependency list, not the full admitted graph; child description/field epochs and external truth are omitted. The digest is computed after the fact from immediate IDs/content hashes, and the request digest is ignored. `:174–177` accepts a row count as output verification; V3 staging allows nullable arbitrary payload/type without generation/output provenance. An NSFW grandchild changed through the actual owning `FictionOperations.update` still allowed root publication (`NSFW_GRANDCHILD_PARENT_PUBLISHED=true`).

**Consequence:** old computation can be relabeled with a newly assembled dependency list; root publication does not establish which approved fields/versions actually produced the staged data.

**Correction/proof:** persist and compare a server-owned immutable full manifest including every used child's epoch, field/description revision and session lifetime; bind a validated synthetic staged result/output digest and completion key to that grant. Reject missing/mismatched/changed manifests and invalid or empty payloads, not just absent rows. Add transitive child, child field reduction, late dependency insertion, external child, forged result and idempotent completion tests.

### FR17-4 — High — worker token possession is exposed across jobs and the SQL fence omits grant scope

**Partial fix:** current wall-clock check at insertion, mandatory token and reduced table DML privileges.

**Remaining evidence:** V4 `:46–82` locks/checks only lease status/token/expiry and current source APPROVED; it never compares the admitted epoch/fields/revisions, and only rejects `PRIVATE`, permitting other classifications. V4 `:95–96` grants unrestricted SELECT on every staging row (including `lease_token` and payload) and all RAG sequences. The actual worker read another job's token and used it to INSERT an unreviewed, 2 MiB staged payload **after that source's field policy changed** (`WORKER_READS_OTHER_JOB_TOKEN_AND_WRITES_OLD_GRANT_OVERSIZE=true`). V3 `:143–146` still creates/resets a committed fixed password; an existing unsafe role's attributes/memberships are not validated. Compose/default datasource setup still lacks separately provisioned migrator/non-owner Java runtime identities.

**Consequence:** the credential is a global cross-job capability; knowing a token is insufficient isolation when every token is readable. Reduced DELETE privileges do not establish scoped input/staging rights.

**Correction/proof:** externally provision distinct identities/secrets and audit inherited attributes/memberships; expose one bounded attempt-scoped enforcement route or audited RLS/definer API without broad token/payload reads. Fresh checks must match immutable grant/epoch/field/session/dependency scope and require exactly NON_PRIVATE. Restrict sequence access to what that route needs. Prove actual-role cross-job reads/writes, stale fields/epochs, unsafe pre-existing roles, expiry and purge commit fences in both orders. Preserve applied migration history and use additive/provisioning corrections where required.

### FR17-5 — High — the real minimal worker workflow remains disconnected and does not verify authority

**Partial fix:** Jackson serialization, nominal loopback URL restriction, UUID-shaped inputs, stage vocabulary, character limit, metrics and same-string in-memory cancellation.

**Remaining evidence:** `RagEngineClient.java:69` is still unused by production orchestration; `RagJobService.admitJob` only saves rows. `worker.py:10–30` returns STAGED without consulting a lease, producing a durable output or persisting one; configured worker DB URL is unused. TestClient accepted a syntactically valid nonexistent job/forged token as STAGED. Cancelling a lowercase UUID and then executing its uppercase equivalent also returned STAGED. Python `main.py:53–65` releases its lock before execution and has no active-worker quiescence check. Service/worker credentials retain committed default values in Java config, Python config and migration. Source views/DTOs return raw file paths; regex-based exception sanitation and native failure logs do not supply a typed safe alias/error contract.

**Consequence:** the claimed Java → Python → durable staged result → Java gate path is not implemented. A string-valid request is mistaken for authorized work; cancellation state is restart-volatile and inconsistent across equivalent IDs.

**Correction/proof:** connect the actual narrow synthetic foundation workflow with typed immutable scoped grants, live lease validation, durable bounded staged output, verified result and real cancellation acknowledgement/fencing. Normalize IDs as UUID values. Require external secrets; use stable source aliases and content-free typed errors. Prove a started Java/Python service with actual restricted worker persistence, stale/forged/expired grants, cancellation in flight and local-only transport. No parser/model/product feature is needed or authorized.

### FR17-6 — High — field reduction and expiry/recovery can retain stale published derivatives

**Partial fix:** recursive ancestor generation invalidation on explicit revoke; cancellation invalidates that job's generation.

**Remaining evidence:** `RagSourceService.approveSource:201–203` overwrites fields/epoch without invalidating or purging prior grants/generations/outputs. Independent field reduction left both an old active generation and its derivative (`FIELD_REDUCTION_LEAVES_OLD_GENERATION_ACTIVE=true`, `FIELD_REDUCTION_LEAVES_OLD_DERIVATIVE=true`). `RagPurgeService.java:46–60,70–77` deletes SQL staging and declares PURGE_COMPLETE; there is no independent session lifecycle, worker I/O quiescence, disk/artifact cleanup receipt, failure retry, restart/restore anti-replay or generation-bound output closure. Cancellation claims completion before best-effort engine notification; revoke has no worker notification/quiescence protocol.

**Consequence:** removal of free-text consent is not the synchronous deny/purge transition promised by P17-3; a purge-complete status overstates the cleaned derivative surface.

**Correction/proof:** route restrictive consent changes through the same invalidation/fence protocol; separate synchronous deny from verified retryable full cleanup. Persist minimal session expiry/anti-replay/tombstone and synthetic artifact provenance needed for this phase. Require worker quiescence and deletion receipts before completion; restart/restore defaults deny until reconciliation. Test independent session expiration, retained old outputs, transitive purge, late I/O, deletion failure, orphan recovery and replay with synthetic artifacts.

### FR17-7 — High — lock acquisition does not refresh RAG authority and expiry is checked with an earlier timestamp

**Partial fix:** main final gate now obtains sorted Vault guards, sorted immediate RAG sources, then lease; owning content projections refresh their entities.

**Remaining evidence:** `RagFinalizationGate.java:109–116,159–181` peeks managed RAG entities before locking and reuses them; pessimistic JPQL in `RagSourceRepository`/`RagJobLeaseRepository` does not explicitly refresh them. A transaction preloaded source/lease, a separate transaction committed description field reduction, and the original transaction then published the old grant; generation epoch differed from authoritative source epoch (`CACHED_FIELD_REDUCTION_OLD_GRANT_PUBLISHED=true`, `CACHED_PUBLICATION_EPOCH_DIFFERS_FROM_AUTHORITY=true`). `now` is captured before guard waits. A finalizer blocked on the real Vault row until the lease expired, then published (`EXPIRED_DURING_GUARD_WAIT_PUBLISHED=true`). Public Vault source guards use REQUIRED and can start/end their own transaction instead of rejecting a missing writable caller transaction. Ancestor/dependency mutation/revocation paths do not use one complete sorted closure; concurrency tests do not establish their ordering.

**Consequence:** a successful lock is mistaken for fresh proof; stale permissions or stale time can survive the supposed final gate.

**Correction/proof:** obtain fresh DTO/native reads or explicitly refresh locked authority/lease entities; check current deadlines after waits and before activation. Require/verify the same writable physical caller transaction for public guards and cover read-only/savepoint behavior. Apply one bounded complete lock protocol to manifest changes and revoke/purge paths. Preserve the repaired root handle lifetime. Add cached-context, actual-row/transaction identity, both commit orders, deadline, timeout/deadlock and savepoint regression receipts. The unsorted closure's deadlock risk is a hypothesis requiring controlled verification, not a claimed reproduced deadlock.

### FR17-8 — High — claimed R1–R8/all-dimension coverage still omits the acceptance blockers

**Partial fix:** 11 new Java tests and five Python tests cover several ordinary negative inputs/transitions.

**Remaining evidence:** submitted evidence still substitutes R3=staging and R8=architecture for accepted R3=generate/revoke and R8=field reduction. Its 29 rows are a different checklist, all PASS, including unsupported performance/offline/environment assertions. The two domain guard tests only exercise callers of the Vault lock; none pause the real final gate after its last owner read. Seven SQL fence tests are serial admin INSERT checks, not actual worker after-check COMMIT vs purge barriers. Five Windows tests have no real finalization/outer-COMMIT hook, independent replacement/mapping/unsafe-volume or recovery fixture. The lifecycle NSFW-child test mutates then finalizes serially despite its concurrency wording. Probes now demonstrate several assertions this suite fails to enforce.

**Correction/proof:** implement the original handoff cases with controlled synthetic stages; retain original R1–R8 identities and explicitly map later-product assertions to foundation artifacts or N/A. Add actual worker, source/final gate and NTFS post-check interleavings, failure-safe bounded cleanup, recovery/expiry/restore and egress tests. Include failing-before/passing-after receipts for these counterexamples, truthful environments and all 29 canonical dimensions. This needs implementation plus tests, not a test-only slice. Do not patch/disable the frozen authentication test to manufacture a green receipt.

### FR17-9 — Medium — enforced resource and database bounds remain incomplete

**Partial fix:** RAG catalog listing now uses true offset and stable ID ordering; direct constructor file byte-limit test, configurable lease duration and some indexes exist. Normal cancellation notification occurs after its TransactionTemplate finishes.

**Remaining evidence:** the native component has both a no-arg and a value-injected constructor without constructor selection; in the actual Spring context configured for 50 bytes, a 100-byte file was accepted (`SPRING_CONFIGURED_50_BYTE_CAP_IGNORED=true`). Python limits characters, not UTF-8 bytes; a 1,800,000-byte synthetic payload returned STAGED. SQL staging has no byte/type cap. Selected-source/queue/disk/attempt/hash/lock/transaction-duration caps are not enforced. `addDependency:269–290` counts visited ancestors rather than longest full graph depth, ignores the child's existing depth and has an unlocked concurrent cycle check. Owner `findSources` remains repeated per-ID entity/refresh/Vault/set queries; `enumerateSources` still floors offset into an unsorted page. V4 does not add lease-source or reverse-child indexes. Cancellation can still join an existing outer transaction, so its HTTP call is not universally outside physical lock lifetime.

**Correction/proof:** correctly wire and validate the actual limits, enforce byte/batch/queue/temp/attempt/deadline budgets at every boundary, bound the full graph with a concurrency-safe simple update protocol, use true owner pagination/batched fresh reads and evidence-driven lookup indexes. Defer transport until actual transaction completion or enforce entry-point ownership of that transaction. Measure queue-inclusive p50/p95, lock wait/hold, hash time, SQL count and peak memory/disk with actual hardware/volume receipts. Avoid new brokers/job engines or speculative optimization.

### FR17-10 — Medium — environment, offline, dependency and current inventories remain unverified or stale

**Partial fix:** C4 port is now 8001, V4 is applied and a Python lock exists matching the measured major package versions.

**Remaining evidence:** V3 silently skips vector when unavailable; real tests/probes use plain Postgres and report `VECTOR_EXTENSION_PRESENT=false`. Compose still names mutable `pgvector/pgvector:pg18`, not a verified exact-18.6/digest environment. Worker README installs through `uv pip install` rather than enforcing its project lock, while claiming network isolation; no actual IPv4/IPv6/DNS/proxy-denied runtime receipt exists. Submitted Python/FastAPI/Pydantic/Uvicorn versions differ from the executed environment. There is no bounded license/advisory inventory for the new runtime. C4 now depicts SQL worker staging that the actual Python worker does not perform. Current data architecture has no RAG namespace/role/lifecycle inventory, package-tree introduction still says RAG is not initialized, root README/owner instructions and affected diagram exports have stale implementation/gate statements.

**Correction/proof:** provide reproducible locked install/start instructions, exact compatible DB/extension receipts, required offline and new-dependency diagnostics, and measured versions. Synchronize affected current data/module/owner/tree/diagram inventories and exports once actual implementation behavior is correct. Preserve historical acceptance and old migrations. No deployment/model installation or later-phase scope is authorized.

## Complete canonical 29-dimension ledger

Statuses assess this implementation, not an invented rubric or future product. `NOT_VERIFIED` is not PASS. Source paths above are relative to the repository; application files live under `backend/src/main/java/com/vhvkhangg/personalprivatevault/` and tests under the corresponding `src/test/java/` tree.

| # | Canonical dimension | Status | Evidence / limit |
| --- | --- | --- | --- |
| 1 | Owner intent/business acceptance/source selection/non-goals | FINDING | Scope stays foundation-only; source/session/worker acceptance incomplete (FR17-2/3/5/6). No later phase permission. |
| 2 | Requirement → contract → verification traceability | FINDING | ACTIVE acceptance vs renamed R1–R8 and replacement 29-row evidence; FR17-8. |
| 3 | ADR/C4/source/database/workflow consistency | FINDING | ADR-0019 delta respected; C4 depicts nonexistent worker SQL path; current data/tree/status inventories drift (FR17-10). |
| 4 | Module ownership/named interfaces/cycles/encapsulation | PASS | `rag` leaf public vault/fiction/film contracts; no cross-owner internals/entities/repositories; 37 architecture tests green. Source freshness is separately failed under 15. |
| 5 | Actual recursive repository/package/folder organization | PASS | 53 RAG files, 12 populated packages/descriptors; six test classes; six Python source modules/three test files; minimal owner source named interfaces. Cohesive API/source/internal/web grouping. |
| 6 | Shared envelopes/exceptions/contract placement | PASS | Controllers/advice reuse root ApiResponse/ApiResponses, correct ADR-0016 placement; no duplicate shared envelope or invented common module. Safe message behavior assessed under 14/27. |
| 7 | Concrete SOLID/contracts/cohesion/dependency direction | FINDING | Owner ports/facade appropriate; public transaction guard promises caller lifetime but REQUIRED can end its own transaction; stale proof breaks guard contract (FR17-7). |
| 8 | DRY/KISS/YAGNI/immutability/determinism/invariants | FINDING | No speculative engine/framework; root hash improved, but immutable full grant/output manifest and consent-state invariants missing; FR17-3/6/7. |
| 9 | Patterns/anti-patterns | PASS | Facade, owner projection ports, repository and native adapter have concrete roles. No event bus, generic agent platform, ServiceImpl hierarchy or new shared module. Cleanup/batching improvements below need no new pattern. |
| 10 | Domain correctness/transitions/idempotency/edge cases | FINDING | NSFW grandchild, changed external child, retained old field derivative and expired publication counterexamples; FR17-3/6/7. |
| 11 | Public API/wire/version/errors/pagination/provenance | FINDING | 224-route inventory passes, root RAG offset fixed; unused digest parameter, unverified grants, raw path identifiers and owner enumeration drift remain (FR17-3/5/9). |
| 12 | Hostile input/format/path/bounds validation | FINDING | Domain field/UUID negatives pass; arbitrary external metadata/reparse ancestors, character-only cap and unbounded SQL output remain (FR17-1/2/9). |
| 13 | Authn/authz/privacy/consent/least privilege | FINDING | Owner auth regression green; explicit domain field checks improved. Worker cross-job token read/write, stale grants and metadata veto gaps reproduced (FR17-2/4/5). |
| 14 | Credentials/leakage/retention/egress/supply chain | FINDING | Committed defaults/raw path/error/log surface and stale derivative retention remain. Actual egress-denied proof NOT_VERIFIED; FR17-4/5/6/10. |
| 15 | Physical atomicity/isolation/lock order/freshness/races | FINDING | Root NTFS outer-COMMIT lifetime passes; cached policy and post-wait expiry publish failures, incomplete child/closure guards and missing interleavings (FR17-1/7/8). |
| 16 | Failure/recovery/leases/cancel/restart/restore/cleanup | FINDING | Explicit cancel and recursive revoke improved; session expiry, worker quiescence, full cleanup, restart/restore anti-replay absent (FR17-5/6/8). |
| 17 | PostgreSQL/Flyway/JPA/constraints/permissions/query shape | FINDING | Real V1–V4 migrations validate; scoped role/grant enforcement and necessary reverse/source indexes missing; repeated owner reads (FR17-4/9). |
| 18 | Time/memory/I/O/locks/CPU/GPU/disk/admission complexity | FINDING | Configured native cap ignored in Spring, UTF-8/SQL limits missing, unbounded queue/disk/deadline and graph/read growth (FR17-9). GPU/model execution not in scope. |
| 19 | Retrieval/ANN/reranker/recommendation/answer quality | N/A | No authorized retrieval/ML/recommendation product or indexed search quality in foundation. Source/output isolation is assessed under 10/13/15, not excused. |
| 20 | Extraction/OCR/layout/multilingual/locator fidelity | N/A | Actual format/OCR ingestion belongs to Phase 18. Unsupported external activation must still deny (FR17-2); no fidelity PASS invented. |
| 21 | Adversarial/security/concurrent/OS/worker tests | FINDING | Seven serial admin fence tests and five lexical handle tests omit actual final-gate/purge/security boundaries; independent probes fail them (FR17-8). |
| 22 | Risk-oriented test quality/E2E/determinism/fixtures | FINDING | Real PG/fabricated fixtures and 27+12 tests are useful; no durable cross-language E2E, exact post-check barriers or full lifecycle assertions. Initial unchanged auth boundary failure retained (FR17-5/8). |
| 23 | Credible performance/hardware/latency/resource benchmark | NOT_VERIFIED | No queue-inclusive p50/p95, measured memory/disk, lock duration or volume/hardware threshold receipts. Required foundation gate remains FR17-9. |
| 24 | Held-out labels/leakage/evaluation/judge calibration | N/A | Real ML evaluation deferred; accepted P17-N1 remains first relevant later preparation obligation. Synthetic safety checks are required under 21/22. |
| 25 | Dependency compatibility/licenses/advisories/lock lifecycle | NOT_VERIFIED | Project lock exists; executed versions measured. Exact pgvector environment and new runtime license/advisory receipts absent (FR17-10). No model hashes applicable. |
| 26 | Build/test/static/tooling/format/architecture execution | FINDING | Repeat full build and all Python tools green; first full failure retained, IDE warnings triaged; submitted environment/coverage assertions not reliable (FR17-8/10). No new coverage percentage or Java formatting gate invented. |
| 27 | Safe useful logging/metrics/errors/incident operations | FINDING | Aggregate worker counters/run IDs useful; raw native path diagnostics, incomplete safe-error contract and false PURGE_COMPLETE remain (FR17-5/6). |
| 28 | Docs/governance/status/history/links/inventories | FINDING | Accepted history preserved and review statuses synchronized; implementer closure claims, setup/data/owner/tree/diagram inventory drift require remediation (FR17-8/10). |
| 29 | Delivery hygiene/caches/obsolete artifacts/frozen scope | PASS | Descriptors/egg-info fixed; cache/venv/log artifacts ignored; tracked diff whitespace clean; frozen DBML/history/old migrations and POM unchanged, no unrelated phase implementation or publishing. |

Ledger totals: **5 PASS, 19 FINDING, 3 N/A, 2 NOT_VERIFIED**. Critical applicable privacy/commit/worker/recovery checks remain failed or unverified; readiness is blocked regardless of green test counts.

## Proportionate improvements and next gate

1. Finish one immutable bounded grant/manifest and one scoped staging route. This removes the redundant ignored caller digest, token-discovery overload and disconnected stage placeholders while closing actual security gaps; no generic workflow framework is warranted.
2. Centralize native handle cleanup in one ownership/failure path, keep handles transaction-owned, and avoid six repeated close/log branches. Validate roots/volumes and all manifest files once per bounded gate; never cache positive authority across transactions.
3. Batch fresh owner projections/assignment sets, sort/deduplicate the full manifest and add only justified reverse/source indexes. Remove the duplicate staged-output index creation in a safe not-yet-applied/additive migration approach appropriate to deployment history. Current per-ID refresh plus separate set/Vault reads is the concrete bottleneck, not an ANN tuning problem.
4. Use a small explicit typed lifecycle/terminal-state policy for consent changes, attempts and purge receipts; avoid arbitrary string statuses and in-memory cancellation as the authority. Keep fast durable denial separate from acknowledged worker quiescence and retryable cleanup.
5. Preserve required R1–R8 identities and map each to exact executable assertions. Remove unsupported performance/offline/all-CLOSED statements and retain failed as well as passing receipts. Clean four unused imports; optional Math.clamp/getLast suggestions do not justify frozen refactors.

**Next step:** Antigravity runs `/antigravity-implement-handoff` against the existing active foundation handoff to complete FR17-1–10 implementation and regression proof, then returns to `$codex-final-review`. No test-only remediation, new handoff, owner commit, later phase or frozen authentication change is authorized by this report. READY FOR OWNER COMMIT cannot be issued until the remaining blocking findings and mandatory security/runtime proof are resolved.

