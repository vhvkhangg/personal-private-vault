# Phase 17 RAG foundation — Codex final review, 2026-10-14

**CHANGES_REQUESTED.** Eight High and three Medium findings remain open. The implementation does not yet enforce the accepted P17-1/2/3 runtime contracts. Passing submitted and independently rerun suites do not establish those missing guarantees. No commit message or implementation acceptance is issued.

## Scope, authority and baseline

Implementation-mode gate passed: [ACTIVE](../../handoffs/ACTIVE.md), `P17-RAG-FOUNDATION`, was `IMPLEMENTED_AWAITING_CODEX_REVIEW`. Reviewed against [owner decisions](../owner-decisions.md), accepted ADR-0019, exact P17-1/2/3 contracts and the [READY FOR HANDOFF design review](2026-10-13-phase-17-p17-3-and-coverage-codex-review.md). The preparation findings remain closed **as design contracts**; the runtime findings below are separate.

HEAD/local origin/main: `24e6268eef1685866d919d882ecdf9f532d01961`; no fresh remote fetch. Accepted preparation and completed SBA maintenance/closeout are committed ancestors. Current patch includes the approved new RAG namespace/module, owning source projections/guards, Python service and associated tests/docs. Source changes were inspected, not rewritten by Codex. Old DBML/migrations and Phase 15/SBA histories have no diff. Phase 16 and later RAG products remain outside this review.

Applied codex-final-review, all 29 codex-review-coverage dimensions and the relevant Java/Spring, SOLID, reuse/pattern, module, JPA/migration, testing and secret-handling instructions. Graphify was queried but does not cover the new implementation reliably; canonical source/tests determine findings. No delegated agents.

## Independent verification

- `mvn -ntp clean verify` from backend: **1,170 tests, 0 failures/errors/skips, BUILD SUCCESS**, 04:24, finished 2026-10-14T08:43:01+07:00. Maven per-suite totals and **1,170 actual XML testcase nodes** agree; some nested-suite XML summary attributes undercount and were not used as the authoritative count. Java 25.0.2, Maven 3.9.15, Testcontainers 2.0.5, PostgreSQL 18.6-alpine. Receipt: `logs/phase17-final-review/2026-10-14-full-verify.log`.
- Existing Python environment: **3.14.4**, pytest **7 passed / 1 warning**, Ruff check/format and mypy all exit 0. The warning is Starlette TestClient/httpx deprecation. This differs from the submitted Python 3.14.0a3 description.
- Review-only probes under ignored `logs/phase17-final-review/`: `worker_probe.py` uses fabricated in-process HTTP requests; `Final17Probe.java` uses a new disposable PostgreSQL container, unchanged compiled production classes, actual Spring transactions and a synthetic local NTFS file. No owner database/source data or checked-in test was modified. Javac/classpath setup and both Java probe executions completed; the second confirms cached authority and wall-clock expiry as well.
- Recursive main RAG tree: **43 Java files, 12 populated packages, 9 missing package-info.java**; all six RAG test files and complete Python source/test subtrees inspected. Five generated egg-info files are unignored prospective Git additions. Relevant owner changes, source contracts, repositories, DTO/advice, migration/config and architecture diffs were inspected.
- Actual IDE inspections on 12 relevant Java/SQL/Python files: zero errors, **10 warnings / 2 weak warnings**, no timeouts. Two unused imports are cleanup; three unused stage/health methods corroborate the unconnected workflow; clamp/getLast suggestions are optional style; custom HTTP-header warnings and absent SQL datasource assistance are configuration issues. Maven Unsafe/native/agent warnings and Python deprecation were not treated as functional test failures. No warning-free or full IDE-clean claim.
- No genuine egress-denial/cold-start demonstration, foundation performance/resource measurements, dependency license/advisory/SBOM audit or pgvector-enabled integration receipt was supplied or independently produced. Those remain NOT_VERIFIED.

### Reproduced counterexamples

All requests/content were synthetic. These are observations, not hypothetical threats:

| Probe | Observed result |
| --- | --- |
| Approve denied fields | `review` and an unknown secret-named field accepted as approved fields. |
| Change separately approved description; finalize without staging | Publication succeeds; staged row count is zero. |
| Change title/originalTitle across delimiter boundary | Same revision hash for title/originalTitle pairs `A:B / C` and `A / B:C`; job admission succeeds under prior approval. |
| Reduce approved fields after admission | Old job publishes using the **new** source epoch rather than rejecting the old grant. |
| Cache approved source/active lease; independently revoke; finalize in original context | Revoked authority is republished as an active generation. |
| Make a child NSFW; finalize parent | Parent publishes despite the current restrictive child truth. |
| Revoke leaf C in A -> B -> C | Grandparent A remains active. |
| Cancel published job | Its active generation remains active. |
| Actual worker credential | Reads all source policies/leases, stages without presenting a token, deletes other-job staged rows. |
| Worker transaction started before expiry | Staging succeeds after a SQL wall-clock check confirms expiry. |
| Finalize external file within outer transaction; write before outer COMMIT | A new file write succeeds before physical DB commit. |
| pgvector in the exercised database | Extension is absent; migration succeeds anyway. |
| Worker malformed IDs/empty grant/unknown stage | HTTP 200 / STAGED. |
| Worker execute after cancellation | CANCELLED acknowledgement followed by STAGED. |
| Two-MiB worker payload / claimed metrics endpoint | Payload accepted; /metrics returns 404. |

## Required remediation

Every finding is blocking for the current implementation gate. All fixes stay within this foundation handoff; none authorizes parsers, models, QA, recommendation, Frontend or Ideaverse.

### FR17-1 — High: external-file guard does not protect the physical commit

Evidence: [RagFinalizationGate](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java) lines 83–197 closes the handle in method finally, before Spring's enclosing transaction completes. The outer-transaction probe proves a write can occur before COMMIT. [WindowsNativeFileGuard](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java) lines 97–127 permits an unlocked NIO fallback; lines 115/191 resolve/open/hash by path, follow links before checking the resolved target, and hash through a second path-open. Identity is captured but not persisted/compared as the approved identity. No allowed-root/NTFS/ancestor namespace proof exists. Error-close branches also discard an int-returning FFM result and risk masking errors/leaking a handle.

Correction: tie handle release to actual transaction completion, protect/hash the same verified object and approved path/volume identity, preserve deterministic file-before-DB ordering, reject unsupported/native-failure/reparse/namespace cases, and make failure cleanup reliable. A supported immutable snapshot must be explicitly distinct. Do not weaken the contract into freshness by rehashing. The method/proxy boundary must account for caller transaction composition. [Spring transaction proxy semantics](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html).

Proof: real after-last-check/before-COMMIT writer barriers, including outer transaction composition, native failure, already-open mappings/writers, same-byte replacement, root/ancestor rename/reparse/hard-link cases and safe failure cleanup. Unsupported live modes must be explicitly disabled.

### FR17-2 — High: field consent and revision proof are not enforced

Evidence: [RagSourceService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java) lines 143–180 and [RagSource](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java) lines 108–146 accept any field set, including empty/unknown/denied fields; field-policy version never advances. [FictionSourceService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceService.java) line 142 and [FilmSourceService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceService.java) line 153 use ambiguous colon concatenation. Description hash is stored separately but admission/finalization compare only the other hash. Observed forbidden-field approval, changed-description publication and a real delimiter collision bypass required renewal.

Correction: enforce P17-2 per-type field/region vocabulary and restrictive precedence, explicit description consent/version, canonical unambiguous typed hashing, and exact approved payload projection. Do not positively authorize external formats/metadata whose policy cannot yet be proven; keep those modes denied rather than implementing Phase 18 parsing here. Centralize the same eligibility/field proof used by approval, admission and finalization instead of divergent copies.

Proof: every denied/unknown field, empty selection policy, description-only changes, delimiter/null/Unicode/set-order cases and exact allowed-field output, plus restrictive external metadata/unsupported-format denial.

### FR17-3 — High: admission and publication lack immutable grant/output manifests

Evidence: [RagJobLease](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagJobLease.java) contains source ID/status/token/expiry but no admitted epoch, fields, revisions, session or dependency closure. [RagJobService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagJobService.java) lines 77–148 does unguarded admission. [RagFinalizationGate](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java) lines 147–177 checks only immediate child policy flags, trusts caller dependencyDigest and creates a generation without checking staged output. It stamps the current epoch onto old work. Observed empty publication, old-field grant publication and parent publication with a current NSFW child.

Correction: persist an immutable job/attempt grant and full versioned dependency manifest; validate all owning child/file authorities under their guards, exact stage/result/selection scope and current policy against admitted values. Compute/verify manifests server-side; never relabel old work. Activation must require an eligible complete stage/result and idempotent completion behavior.

Proof: field/grant reapproval, identical bytes across independent sessions, changed child/hash/NSFW/trash/fields, missing stage/manifest, forged digest, duplicate/lost acknowledgement and concurrent admission/revoke. Reject or return the same safe completion without duplicate publication.

### FR17-4 — High: worker privilege and expiry boundary is unsafe

Evidence: [V3 migration](../../../../backend/src/main/resources/db/migration/V3__rag_foundation.sql) lines 108–157 grants global SELECT on source policy and all leases/tokens, unrestricted stage DML/sequence access, a committed fixed login password, and no job/token RLS or scoped entry point. Its trigger checks only IDs/status and transaction-start CURRENT_TIMESTAMP. Actual worker probes read other policies, write without a token, delete another job's rows and stage after confirmed wall-clock expiry. Existing-role provisioning does not establish all required privilege flags/memberships. Runtime/migrator separation is not implemented in configuration.

Correction: provision external credentials with auditable role membership/grants; separate migrator and non-owner Java runtime while preserving the shared Java datasource/transaction. Restrict worker views/write routes to its currently authorized attempt/grant; prevent cross-job reads/deletes/reassignment. Check current source/epoch/lease authorization and real expiry, preserve DB lock through COMMIT, constrain state/FKs and audit definer ownership/search_path. Never rely on a caller-set variable alone. Do not rewrite an already applied shared migration; use an appropriate correction migration if necessary. CURRENT_TIMESTAMP is fixed at transaction start; it is not a fresh wall-clock expiry check. [PostgreSQL time functions](https://www.postgresql.org/docs/18/functions-datetime.html).

Proof: actual-role scoped success and cross-job/token/schema/policy/state/DDL/escalation denials, existing-role deployment, long transaction expiry, lease-check-to-COMMIT purge in both orders and no content resurrection.

### FR17-5 — High: worker boundary reports work/cancellation it has not performed

Evidence: [worker.py](../../../../rag-engine/src/ppv_rag/worker.py) lines 10–30 ignores grant/expiry/scope and returns STAGED without persistence; [main.py](../../../../rag-engine/src/ppv_rag/main.py) line 43 acknowledges cancellation without state change. [contracts.py](../../../../rag-engine/src/ppv_rag/contracts.py) strings do not validate UUIDs/stages/bounds. Java stage dispatch/health methods are unused; manual JSON escaping is incomplete. Committed service-key defaults, unconstrained engine URL and direct exception-message/path responses in [RagExceptionAdvice](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/RagExceptionAdvice.java) weaken the intended local/private boundary.

Correction: implement the actual minimal authenticated grant -> bounded synthetic stage -> scoped durable result -> Java verification flow, or explicitly disable unimplemented operations rather than claiming success. Cancellation must stop/fence the attempt before acknowledgement. Require externally supplied secrets, loopback validation, typed serialization/validation, safe semantic error codes and opaque source aliases. This requires no model/real parser.

Proof: genuine Java/Python/Postgres E2E, malformed/forged/expired/cancelled requests, unavailable service and result-status handling, newline/backslash serialization, sensitive-marker absence from errors/logs and real local-only egress/startup tests.

### FR17-6 — High: revocation, session lifetime and recovery are incomplete

Evidence: [RagSourceService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java) lines 208–216 traverses one parent level only; [RagPurgeService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java) lines 41–79 removes staged SQL rows and immediately records PURGE_COMPLETE. Cancel does not invalidate published generations; job/generation provenance is insufficient for it. No independent source-session expiry, worker quiescence/scratch reconciliation or restore/restart anti-replay protocol exists. Grandparent and cancelled-generation probes remain active.

Correction: implement bounded cycle-safe reverse dependency closure tied to generation/output provenance; invalidate every affected pointer/grant and purge all implemented content-bearing artifacts. Separate read denial, DB cleanup and verified full cleanup; require worker quiescence before the last. Add session expiry/reconciliation and defined restart/restore renewal behavior without expanding the existing portability exception.

Proof: three-level/cyclic manifests, two sessions sharing bytes, expired session with late synthetic result, cancellation after publication, native late scratch write/crash/delete failure, restart/lost event and restore anti-replay. No blanket success marker for partial cleanup.

### FR17-7 — High: lock order and managed authority are inconsistent

Evidence: [RagFinalizationGate](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java) locks lease -> source -> Vault/file -> unsorted child sources; revoke locks source/generations -> leases. This is a concrete deadlock cycle and violates the accepted global order (a forced deadlock was not run). Locked repository queries can return previously managed stale source/lease instances; the cached-authority probe republished after an independently committed revoke. Public Vault guard methods use ordinary REQUIRED transactions, permitting standalone calls whose lock ends on method return.

Correction: one consistent file -> sorted Vault -> sorted RAG/attempt/generation guard protocol across admission, approval, publication, revoke and purge; fresh authoritative DTO/refresh/CAS state after locks, with no swallowed savepoint release. Require the calling writable physical transaction for guard capability. Remove network/hash work from DB lock intervals; implement bounded lock/statement/transaction waits and retries.

Proof: cached source/lease and owner contexts, both commit orders using actual owning mutations, sorted mixed manifests, deadlock/timeout/interruption/savepoint and physical transaction identity/lifetime. Preserve the accepted SBA guard behavior.

### FR17-8 — High: required security proofs and supplied claims do not agree

Evidence: [submitted evidence](../test-evidence.md) renames R1–R8 (for example R3 becomes a staging trigger and R8 becomes architecture), lists nonexistent Python tests/metrics, and declares complete zero-tolerance verification. The domain suite tests two guard callers, not finalization versus owning mutations; staging tests are serial administrator inserts (one even uses an invalid CONFIDENTIAL classification); Windows tests only identity/hash, directory and missing path. They do not establish the required races, actual worker grant scope or complete lifecycle. Existing tests also tolerate timed-out holders and do not always await cleanup.

Correction: implement the original handoff/contract cases at their real boundaries, retain original R1–R8 meanings and map all 29 dimensions to actual receipts. Correct submitted environment/test names and explicitly distinguish observations, proposals and missing proof; preserve historical reviews. Do not merely add assertions for the current broken behavior.

Proof: new cases fail against this submitted version and pass after correction; include independent transactions/processes, last-check barriers, holder/waiter/lock evidence, leak assertions and reliable process/executor cleanup. Rerun focused then full commands. This is code-and-test remediation, not a test-only pass.

### FR17-9 — Medium: configured bounds and efficient reads are largely absent

Evidence: application limits for source count/depth/bytes are not consumed; file hashing is unbounded, worker accepts a two-MiB arbitrary payload, queue/stage/temp/retry limits and measurements are absent. Source findSources loops per ID through multiple queries; enumeration/list offset is rounded to a page, clamped silently and lacks deterministic ordering. Lease/dependency reverse lookups lack relevant bounded/indexed access. Cancel sends HTTP while holding the purge transaction.

Correction: enforce explicit source/dependency/byte/queue/staging/disk/deadline caps, real batch reads, truthful ordered pagination, proportional indexes/query plans for actual lookups and control-plane work outside lock scope. Measure guard/hash/wait/queue and memory/disk against selected limits; avoid speculative infrastructure.

Proof: maximum/over-limit requests, non-page offsets/stable ordering, counted SQL round trips, bounded graph/queue/load and queue-inclusive p50/p95/resource receipts on stated hardware.

### FR17-10 — Medium: database/dependency and architecture acceptance remains unproved

Evidence: V3 silently skips vector installation; the exercised PostgreSQL has no vector extension. compose uses a mutable pg18 tag without an exact 18.6/extension receipt. Python setup uses uv pip install rather than the lockfile/frozen install and claims network isolation without a denial test; license/advisory evidence is absent. C4 source describes ingestion/chunking/SQL staging as implemented and uses port 8000 while service configuration uses 8001. Data inventory/affected owner instructions/exports have not been fully synchronized.

Correction: reproducible compatible DB/image/extension and Python setup, explicit missing-capability failure or owner-approved disabled scope, actual dependency inventory/license/advisory checks and offline runtime receipt. Synchronize current approved implementation docs/source diagrams/affected exports without claiming deferred features or rewriting frozen history.

Proof: fresh reproducible environment using recorded pins/lock, exact DB/extension version and migration/runtime-role checks, no runtime download/egress, current route/port/table/module inventory and validated document links.

### FR17-11 — Medium: package and generated-artifact hygiene is incomplete

Evidence: populated `rag/internal/application`, `rag/internal/domain`, `rag/internal/infrastructure/{client,persistence,windows}` and `rag/internal/web/{advice,controller,dto,mapper}` lack package-info.java (nine packages); five rag-engine/src/ppv_rag.egg-info files are prospective unignored additions. No competing root HTTP envelope was introduced, and ADR-0016 placement should remain intact.

Correction: descriptors for meaningful packages, narrow ignore/removal of generated packaging artifacts, accurate source/test/tree inventory and actionable unused-code cleanup. Keep capability DTO/exception grouping coherent; do not create a common module or gratuitous ServiceImpl/State/Factory hierarchy.

Proof: recursive inventory against canonical tree, no generated additions or stale placeholders, scoped static diagnostics and clean diff.

## Complete 29-dimension ledger

| # | Dimension | Status | Evidence / limit |
| --- | --- | --- | --- |
| 1 | Owner intent / non-goals | PASS | Foundation footprint; no real parser/model/QA/recommendation/Frontend/Ideaverse shipped. Missing foundation behavior is separately finding-bearing. |
| 2 | Requirement traceability | FINDING | Original R1–R8 and handoff cases not mapped accurately or covered; FR17-8. |
| 3 | ADR/C4/schema/source/workflow consistency | FINDING | Approved additive edges, unchanged old schema/history; current data/diagram/setup claims drift, FR17-10. |
| 4 | Ownership/public APIs/cycles/encapsulation | PASS | Public owner guard/projections and leaf RAG; 37 architecture tests pass. Transaction enforcement deficiencies are FR17-7. |
| 5 | Recursive package/folder structure | FINDING | Full 43-file RAG/owner source/test/Python inventory; nine descriptors absent, FR17-11. |
| 6 | Shared contract / exception placement | PASS | Root ApiResponse/ApiError retained, owner-local web DTO/mapper/advice; no competing common module. Error safety is FR17-5. |
| 7 | SOLID and contract fidelity | FINDING | Guard/worker capabilities promise lifetime and stage/cancel semantics they do not enforce; unused dispatch boundary, FR17-1/5/7. |
| 8 | DRY/KISS/YAGNI/immutability/state | FINDING | Divergent repeated source-proof logic, ambiguous hashing and unbound grant state, FR17-2/3. |
| 9 | Purposeful patterns / anti-patterns | PASS | Modest facade/repositories/native boundary; no broker, agent framework or pattern-class expansion. Complete the actual boundary, not extra abstractions. |
| 10 | Domain transitions/idempotency/missing sources | FINDING | Stale publication, empty generation and cancellation/revocation errors reproduced, FR17-2/3/6. |
| 11 | API/wire/validation/pagination | FINDING | Untyped permissive worker IDs/stages, caller digest, false status and rounded offsets; FR17-3/5/9. |
| 12 | Hostile input/path/format/bounds | FINDING | Unsupported file/path protocol and unbounded bodies, FR17-1/2/5/9. Format parsing itself remains deferred. |
| 13 | Authentication/authorization/privacy/consent | FINDING | Fields and worker cross-job scope unsafe, FR17-2/4/5; owner JWT baseline is preserved. |
| 14 | Credentials/logs/retention/egress | FINDING | Committed credentials, raw path errors, incomplete cleanup and no isolation proof; FR17-4/5/6/10. |
| 15 | Transactions/isolation/COMMIT/races | FINDING | Pre-COMMIT file close, cached revoked authority, global lock inversion and stale manifests; FR17-1/3/7. |
| 16 | Failure/recovery/cancel/leases/restore | FINDING | False cancellation, transitive and session/recovery gaps, FR17-5/6. |
| 17 | Flyway/JPA/constraints/permissions/query plans | FINDING | Migration runs and JPA validates, but privilege/expiry/capability/schema guarantees fail; FR17-4/9/10. |
| 18 | Complexity/resources/contention/fairness | FINDING | Unused bounds, per-ID repeated queries and I/O under locks; FR17-9. |
| 19 | Retrieval/QA/recommendation quality | N/A | Those product paths are excluded from this foundation. Publication authority is reviewed under FR17-3, not treated as retrieval PASS. |
| 20 | OCR/document/multilingual fidelity | N/A | No real parser/OCR/chunking. Unsafe raw-file permission is FR17-1/2, not a deferred parser waiver. |
| 21 | Security/adversarial/OS concurrency tests | FINDING | Serial/happy-path fixtures do not prove real gates; review probes reproduce bypasses, FR17-8. |
| 22 | Test quality/risk/E2E/parity/determinism | FINDING | 1,170 + 7 passing tests; missing actual boundary and after-last-check cases. Synthetic work is allowed, synthetic security proof is not; FR17-8. |
| 23 | Performance/benchmarks | NOT_VERIFIED | No credible foundation p50/p95/hash/lock/queue/RAM/disk measurement; required before final acceptance. FR17-9 names observed risks. |
| 24 | ML evaluation independence/calibration | N/A | No ML capability; P17-N1 remains the owner-approved later preparation obligation. |
| 25 | Dependencies/pins/licenses/advisories/artifacts | NOT_VERIFIED | Lock exists, frozen install/image compatibility/license/advisory acceptance missing; FR17-10. No vulnerability claim is invented. |
| 26 | Build/tooling/static checks | PASS | Independent Maven/Python checks and 12 IDE inspections executed; warnings classified. Passing tools do not override findings. |
| 27 | Safe observability/status/incident recovery | FINDING | False stage/cancel/cleanup status, static run state, raw diagnostic paths; FR17-5/6. |
| 28 | Documentation/governance/evidence | FINDING | Submitted matrix/versions/test names and architecture claims mismatch inspected code; FR17-8/10. |
| 29 | Hygiene/delivery/frozen scope | FINDING | Generated egg-info/descriptors, FR17-11; no unauthorized old migration/history edit or publishing action found. |

## Recorded-document verification

The review/status documents have 95 local file targets, all present, with no trailing whitespace; git diff --check passes. Ledger IDs cover all 29 dimensions (5 PASS, 19 FINDING, 3 N/A, 2 NOT_VERIFIED); all 11 finding sections exist. Old schema/history/SBA records retain an empty diff. Review probe sources/receipts are confirmed ignored. Inspection of this report has zero errors, 38 parent-path resolution warnings and 2 compact-table style warnings, classified by the valid filesystem targets/GFM structure as tooling/style diagnostics. These checks do not certify the rejected implementation.

## Disposition and next step

**CHANGES_REQUESTED** on the existing foundation handoff; all FR17-1–11 are OPEN. Eight High and three Medium findings require proportionate implementation and regression remediation. Accepted preparation/owner decisions remain valid; there is no authorization to expand into later phases or reopen closed SBA work.

Codex changed review/status documentation and ignored review evidence only, not production or checked-in tests. No commit/push/tag/PR, no new implementation handoff, and no commit message.

**Next step:** Return to Antigravity `/antigravity-implement-handoff` for the precise same-scope corrections, update evidence and rerun focused/full checks, then invoke `$codex-final-review` again.

