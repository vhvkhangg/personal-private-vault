# Phase 17 RAG Foundation — Codex final re-review 3

- Date/reviewer: 2026-10-14 / Codex.
- Gate: `P17-RAG-FOUNDATION` was `IMPLEMENTED_AWAITING_CODEX_REVIEW` with Antigravity's third remediation submission; implementation mode is valid.
- State: uncommitted implementation on `main`; HEAD remains `24e6268eef1685866d919d882ecdf9f532d01961`.
- Verdict: **CHANGES_REQUESTED**. **FR17-1–10 remain OPEN / PARTIALLY RESOLVED (8 High, 2 Medium); FR17-11 remains CLOSED.**
- Authority: [existing handoff](../../handoffs/ACTIVE.md), accepted [owner decisions](../owner-decisions.md), ADR-0019 and P17-1/2/3 design contracts.

This report supersedes the implementation verdict in [re-review 2](2026-10-14-phase-17-final-codex-re-review-2.md). Preserve it, [re-review 1](2026-10-14-phase-17-final-codex-re-review.md) and the [initial review](2026-10-14-phase-17-final-codex-review.md). Accepted preparation remains accepted; implementation is not approved. Codex changed only review/status documents and ignored synthetic audit fixtures.

## Evidence and verification limits

| Check | Actual result / limit |
| --- | --- |
| Existing backend receipts | Reused current Surefire XML: **1,199 testcase nodes, zero failures/errors/skips**, including **45 RAG and 37 architecture tests**. Sample implementation/migration timestamps precede the latest suite receipts (18:51:54 +07). Submitted command is `mvn -ntp test`; no new exact `mvn -ntp clean verify` completion receipt was established for this candidate. The older 1,191-test clean-verify result remains historical. No full suite was rerun. |
| Focused changed Python checks | `python -m pytest -q tests/test_worker.py -k 'forged or in_flight or quiescence or multibyte or case_insensitive'`: **6 passed, 10 deselected**, one Starlette/httpx deprecation warning. Required Ruff lint/format and mypy checks pass (10 formatted files, six source files). The **19-test full-suite pass is submitted evidence**, not an independently repeated full run. |
| Fresh runtime probes | `logs/phase17-final-re-review-3/Final17ThirdProbe.java`, `java-probe-complete.log`, `worker_probe.py`, `python-probe.log`: real disposable PostgreSQL 18.6-alpine, current Spring/JPA beans, Windows NTFS, a real loopback HTTP fixture and current Python endpoint/stage code. Two Java runs exited zero; the expanded run adds an actual post-check row-lock/expiry barrier. Python duplicate execution uses bounded events around the real stage function, not mocked authority. Fabricated content/identities only. |
| Static diagnostics | Eleven critical current Java/SQL/Python files: **0 errors, 22 warnings, 22 weak warnings**. Material: duplicate payload/execution paths and unused bypass overloads. Also unused imports/parameters, optional collection/clamp changes, duplicate cleanup/traversal. SQL datasource/custom-header diagnostics are configuration/recognition issues. Self-invocation warning was repaired; no warning-free claim. |
| Recursive tree / scope | Actual IDE traversal: **53 RAG Java files / 12 descriptors**, six RAG test classes, six Python source modules / three test files; owner source packages each have three files and a descriptor. FR17-11 remains closed. The five new owner repository methods are bounded-purpose assignment lookups (15 added lines), within the authorized source-projection delta. |
| Frozen baseline | No tracked diff to DBML, V1/V2, POM, review skills/hooks, authentication source/tests, Phase 15 or closed SBA records. RAG remains an approved leaf using public owner contracts; architecture receipts pass. No later-phase product, Frontend, Ideaverse, model/OCR runtime or deployment implementation is authorized/found. |
| Processes | The initially classified Maven-looking process is IntelliJ's `org.jetbrains.jps.cmdline.BuildMain`, parent `idea64.exe`, not a Maven CLI/Surefire runner. It was left untouched. No duplicate suite started. Disposable audit JVMs/Python tasks completed and closed their resources. |
| Environment | Java 25.0.2, Maven 3.9.15, Boot 4.1.1, Modulith 2.1.1. Actual Python packages remain FastAPI **0.143.0**, Pydantic **2.14.1**, Uvicorn **0.54.0**, pytest **9.1.1**, Ruff **0.17.0**, mypy **2.4.0**; submitted first-three versions are still incorrect. Probe PostgreSQL has **no vector extension**. Exact pgvector/offline/benchmark/license-advisory gates remain unverified. |

The existing Graphify cache was queried for navigation, then current source was inspected directly. Original risk proof is not replaced by suite counts, serial scenarios or copied test-body cleanup.

## Finding dispositions

### FR17-1 — High — PARTIALLY RESOLVED: native cleanup repaired; full live-path stability unproved

**Repaired:** `RagFinalizationGate.java:194–225` closes earlier handles when later acquisition fails. The fresh real-gate probe confirms the first file is immediately writable after the second file conflicts. Direct content caching removes the previous file reread. Native volume/attribute failure paths now deny unknown results.

**Remaining:** `WindowsNativeFileGuard.java:179–205` checks ancestors before resolving/opening the file; these checks do not themselves establish protected namespace lifetime. The required independent-process post-last-check writer, ancestor/root rename/reparse, hard-link alias, writable mapping and crash/physical-COMMIT proofs remain absent. No ancestor-swap exploit was reproduced in this review; this is an unresolved essential safety contract, not an asserted exploit.

**Correction/proof:** retain the cleanup fix, implement/prove the accepted stable-path protocol, or disable live external-file publication until it is proved. Cover both orders and actual finalizer COMMIT; immutable snapshots require separately identified source authority. Do not add Phase 18 parsing.

### FR17-2 — High — PARTIALLY RESOLVED: quoted restrictive metadata still activates

**Repaired:** Markdown approval/publication is disabled. Frontmatter/comments/embeds and several whitespace/value cases now veto.

**Failed:** `RagSourceService.java:409–429` still uses a permissive known-key regex. A fabricated `.txt` containing `{"visibility":"PRIVATE","body":"Synthetic text"}` is approved and published: **QUOTED_RESTRICTIVE_METADATA_STILL_PUBLISHED=true**. The closing quote between the key and colon defeats the regex. Calling the file plain text does not remove its restrictive metadata.

**Correction/proof:** define a fail-closed foundation text contract that rejects metadata-looking/unknown encodings or classifications when reviewed separation is unavailable; do not solve this with another blacklist or unauthorized parser product. Add exact quoted-key/unknown-schema negative tests at approval, worker input and final publication, with genuine plain-text positives.

### FR17-3 — High — PARTIALLY RESOLVED: version prefix does not prove complete dependencies or outputs

**Repaired:** V6 cancels unversioned active leases; the gate rejects unversioned/count-mismatched manifests and checks direct root dependencies. Attempt staging caps now exist.

**Failed:** `RagFinalizationGate.java:169–175,364–468` does not compare the full current closure with the admission manifest. Admit root→child, add child→grandchild, then make the grandchild NSFW: the old attempt still publishes (**NEW_TRANSITIVE_NSFw_CHILD_OMITTED_AND_PUBLISHED=true**). `RagSourceService.addDependency:327–383` changes graph authority without advancing affected grant versions/invalidation.

The final gate validates nonblank bounded stage payloads, not a typed verified result binding its content/digest to fields/source/version/session/dependencies. A loopback `STAGED` response with wrong stage and invalid digest still publishes through the retained four-argument executor (**WRONG_STAGE_AND_DIGEST_STILL_PUBLISHED=true**).

**Correction/proof:** version/fence graph changes and compare a strict complete admitted closure under the same authority protocol. Reject missing/extra/duplicate/malformed children and changed topology. Bind typed actual worker output and verified digest to the admitted grant; remove unchecked publication/execution alternatives. Prove descendant additions after admission, both concurrent orders, negative result provenance and upgrade compatibility.

### FR17-4 — High — PARTIALLY RESOLVED: SQL privilege hardening is not complete runtime isolation

**Repaired:** `V6__rag_security_hardening.sql:4–6` explicitly revokes function EXECUTE from PUBLIC. Existing real-worker role tests and the V5 scoped function provide useful least-privilege proof; cumulative stage limits run inside the lease fence.

**Remaining:** V3's fixed worker password and existing-role branch (`V3__rag_foundation.sql:140–146`) still do not reset/verify unsafe existing attributes and memberships. Separate migrator/non-owner Java runtime provisioning is not supplied/proved. Python still has no real restricted-role staging connection; the accepted worker/SQL enforcement boundary cannot be fulfilled by deleting its C4 edge. Root-only SQL source checks do not verify the complete child/session/owner authority contract.

**Correction/proof:** supply the minimal accepted role configuration and real worker integration, audit effective memberships/function ownership/search_path/grants, and deny privilege escalation/arbitrary jobs/owner tables. Prove an actual worker transaction paused after its last lease check against purge in both orders. Any alternative ownership contract needs owner approval; no frozen business-schema rewrite.

### FR17-5 — High — PARTIALLY RESOLVED: HMAC and typed status improve; source/result authority still bypasses

**Repaired:** arbitrary unsigned tokens now reject; typed HTTP status/job/attempt handling rejects the former `REJECTED` result. Admission token DTO strings now redact the token. The real probe confirms **REJECTED_WORKER_RESULT_NOW_DENIED=true**.

**Failed:** `RagJobService.java:363–399` reads fresh owner content without checking projection eligibility, admitted revision/fields or complete grant authority before sending it. After admission, mark the Fiction NSFW, then execute: **CURRENT_NSFw_SOURCE_SENT_TO_WORKER=true**. Publication rejection cannot undo this worker exposure.

`RagEngineClient.java:103–121` accepts any nonblank digest and does not validate returned stage. The alternate executor at `RagJobService.java:420–436` persists caller input, while the main path persists a digest string as output. Payload JSON is manually escaped and omits approved fields beyond title/description. Python HMAC covers only job/attempt with the shared service key; a signed nonexistent job is accepted (**SIGNED_UNISSUED_JOB_STATUS=STAGED**) without issuance, expiry, epoch, fields, source or session proof.

**Correction/proof:** use one canonical typed source-input/result route, reject ineligible/stale/removed-field content before worker admission, bind a verifiable short-lived issued grant to its complete scope, verify result stage/digest/content, and remove bypass overloads. Use normal typed serialization. Test all approved field/status combinations, NSFW/trash/version change before execution, expired/replayed grants and real Java→Python→restricted staging integration.

### FR17-6 — High — PARTIALLY RESOLVED: cancellation improved; purge completion fails open

**Repaired:** rollback no longer emits cancellation HTTP (**ROLLBACK_CANCEL_NOTIFICATION_FIXED=true**). Cached consent reduction now invalidates the old grant, and Python fences an in-flight result after cancellation (**INFLIGHT_CANCEL_RESULT=CANCELLED**).

**Failed:** `RagEngineClient.java:130–147` treats a missing quiescent field as true and a communication exception as true. `RagPurgeService.java:60–65,75–84` then records PURGE_COMPLETE: **MALFORMED_QUIESCENCE_ACCEPTED**, **UNAVAILABLE_ENGINE_TREATED_AS_QUIESCENT**, and **MALFORMED_QUIESCENCE_MARKS_PURGE_COMPLETE** are true.

Python `main.py:28,94–96,123–125` tracks a set of attempts, not execution counts. With duplicate requests for the same job/attempt, finishing one removes the other's entry: **DUPLICATE_ATTEMPT_QUIESCENT_WHILE_ACTIVE=True**, while active_workers=1. Cancellation is notified after the purge check, and source revoke/reduction does not establish a retryable committed worker cleanup acknowledgement. Independent session expiry, restart/orphan cleanup and restore anti-replay remain absent.

**Correction/proof:** unknown/unavailable/malformed acknowledgement must remain pending; deny synchronous authority first, notify only after commit, then verify bounded attempt-specific cleanup and persist retryable completion. Enforce idempotent attempt execution or correct reference counts. Test duplicate execution, worker outage/restart, late output/temp artifacts, deletion failure and restored stale grants. Network calls should not hold privacy row locks for each historical attempt.

### FR17-7 — High — PARTIALLY RESOLVED: cache/graph fixes credited; later lock wait bypasses expiry

**Repaired:** locked approve/revoke refresh prevents the former stale epoch reuse (**CACHED_REMOVED_FIELD_GRANT_NOW_DENIED=true**). The global transaction advisory lock before endpoint locks serializes dependency additions; the former disjoint-edge write-skew mechanism is addressed at source level. Vault's existing guard still locks and refreshes in the caller transaction.

**Failed:** the gate captures/checks time at `RagFinalizationGate.java:263–269`, then can wait for generation locks at `476` and publish using that old time. The expanded real PostgreSQL probe locks an existing generation row, observes the actual finalizer's post-check lock wait in pg_stat_activity, waits for clock_timestamp() to exceed the attempt deadline, releases the blocker, and gets **LEASE_EXPIRED_DURING_LATER_LOCK_WAIT_STILL_PUBLISHED=true**. This uses no sleep/mock gate.

**Correction/proof:** acquire publication locks in one documented order and revalidate expiry/fresh authority after all waits immediately before activation; retain locks through physical COMMIT and propagate rollback/timeout. Add this actual gate barrier and the accepted both-order owner mutation/savepoint/deadlock cases. Preserve source-level serialization; shared visited sets in depth traversal need a memoized longest-path protocol rather than silently truncating revisited DAG branches.

### FR17-8 — High — OPEN: new regression names still overstate actual boundary coverage

**Evidence:** `RagLifecycleIntegrationTest.java:600–624` calls the purported concurrent edge test serially. `628–641` never preloads a persistence context while another transaction changes consent. `RagWindowsNtfsFileGuardTest.java:150–175` performs cleanup in the test body, not in RagFinalizationGate. The named lease lock-wait test is still serial (`417–438`). Domain tests lock a Vault row, not the actual finalizer after its last authoritative read. Real worker/purge, lifecycle and independent Windows-process barriers remain missing.

The submitted R1–R8 matrix still substitutes SQL staging and architecture for the original accepted generate/revoke and field-reduction identities. Canonical dimension names improved, but 26 PASS claims include unmeasured performance, unverified dependency diagnostics and failed privacy/recovery behavior.

**Correction/proof:** make the actual production boundary fail before/pass after each correction using deterministic barriers and safe cleanup. Preserve original R1–R8 identities; test the fresh counterexamples and complete required worker/OS/session/recovery cases. Correct the evidence rather than renaming serial tests as concurrency. This requires implementation plus tests, not a test-only slice; frozen authentication stays untouched.

### FR17-9 — Medium — PARTIALLY RESOLVED: aggregate staging/batching helps; admission/I/O/locks remain unbounded

**Repaired:** V6 and finalizer impose 50 rows / 10 MB per attempt plus 1 MB per row. Assignment queries are batched; native file bytes are cached. Retain these useful improvements.

**Remaining:** `FictionSourceService.java:105–109` and `FilmSourceService.java:115–119` still refresh each entity separately; the claimed four/five total queries are not proved. Admission still performs per-child owner reads. Up to 100 allowed external dependencies at a 50 MB file cap can retain roughly **5 GB of raw bytes**, before decoded strings/other data; no aggregate handle/hash/memory admission budget is enforced. Sources/jobs/queued work and cancellation tracking can grow without a global bound. Purge performs serial HTTP checks under lease/source transactions. Recursive traversal uses repeated queries/shared visited sets rather than bounded reusable closure/depth logic.

**Correction/proof:** enforce aggregate file/handle/queue/job/storage/transaction deadlines and batch genuinely fresh owner projections without removing refresh safety. Centralize graph invariants inside RAG ownership, use memoization/bounded traversal, and move retryable external cleanup outside locked transactions. Measure queue-inclusive p50/p95, lock hold/wait, hash I/O, peak RAM/temp disk on the actual hardware. Suite runtime is not a workflow benchmark.

### FR17-10 — Medium — OPEN: reproducible environment and accepted inventories still incomplete

**Evidence:** the actual PostgreSQL probe has no vector extension; `compose.rag.dev.yml` uses mutable `pgvector/pgvector:pg18`, not a demonstrated exact 18.6 compatible image. `rag-engine/README.md` still calls an unconstrained `uv pip install` network-isolated/reproducible; the lock is not enforced by those instructions. No actual IPv4/IPv6/DNS/proxy-denied runtime or license/advisory receipt exists. Submitted package versions remain wrong. Test-suite timings and lockfile existence do not establish ledger dimensions 23/25. Current data/migration/root-state inventories and C4 worker SQL boundary remain unsynchronized with the accepted contract.

**Correction/proof:** provide exact compatible PostgreSQL/extension and locked setup/start receipts, real offline/security checks and bounded dependency/license/advisory evidence. Synchronize only affected current inventories/diagrams after implementing the accepted boundary. Preserve historical acceptance/SBA/auth records and separate later-phase gates; do not silently redesign the worker contract.

## Canonical 29-dimension ledger

| # | Dimension | Status | Evidence / practical limit |
| --- | --- | --- | --- |
| 1 | Owner intent/acceptance/source selection/non-goals | FINDING | Foundation scope retained; unsafe metadata/input/dependency authority, FR17-2/3/5. |
| 2 | Requirement/contract/test traceability | FINDING | Original R1–R8 replaced and claimed closures exceed proof, FR17-8. |
| 3 | ADR/C4/source/schema/workflow consistency | FINDING | Approved additive leaf valid; worker SQL contract/inventories drift, FR17-4/10. |
| 4 | Module ownership/named interfaces/cycles/encapsulation | PASS | Public owner contracts, no internal cross-owner imports; 37 architecture receipts pass. Data graph authority is assessed under 15/17. |
| 5 | Actual recursive repository/package/folder organization | PASS | 53 Java files/12 descriptors, full relevant Python/test/owner source trees inspected; FR17-11 stays closed. |
| 6 | Shared envelopes/exceptions/contract placement | PASS | Root ApiResponse family retained per ADR-0016, module-local adapters/advice, no duplicated common module. Error privacy assessed under 14/27. |
| 7 | Concrete SOLID/contracts/cohesion/dependency direction | FINDING | Adapters/facade useful; competing execution overloads and incomplete result/grant contracts violate responsibilities, FR17-3/5/6. |
| 8 | DRY/KISS/YAGNI/immutability/determinism/invariants | FINDING | Duplicate payload/traversal logic, regex authority and incomplete immutable closure, FR17-2/3/5/7/9. |
| 9 | Patterns/anti-patterns | PASS | Native Adapter, module Facade and Repository have concrete roles; no speculative event/broker/agent framework. Repair needs no new platform. |
| 10 | Domain correctness/transitions/idempotency/edges | FINDING | New-child omission, expired publication and false purge completion reproduced. |
| 11 | Public API/wire/version/errors/pagination/provenance | FINDING | Pagination and typed rejection fixes retained; stage/digest/field/result contract incomplete, FR17-3/5. |
| 12 | Hostile input/format/path/bounds validation | FINDING | Markdown veto and caps improve; quoted metadata publishes, aggregate/namespace limits missing. |
| 13 | Authn/authz/privacy/consent/least privilege | FINDING | Owner auth and SQL role positives; NSFW worker exposure, unissued signed grants and incomplete role isolation. |
| 14 | Credentials/leakage/retention/egress/supply chain | FINDING | Java token strings redact; known default keys/passwords, Python token-bearing model repr and unproved egress/lifecycle remain. |
| 15 | Transactions/lock order/freshness/last-check/COMMIT races | FINDING | Cached consent refresh repaired; real post-check generation lock wait bypasses expiry. Complete graph/source COMMIT proofs missing. |
| 16 | Failure/leases/cancel/restart/restore/cleanup | FINDING | Rollback notification and in-flight result fixed; duplicate quiescence/fail-open purge and session/recovery remain. |
| 17 | PostgreSQL/Flyway/JPA/constraints/permissions/query shape | FINDING | V1–V6 real migration succeeds; PUBLIC execute revoked/caps useful, complete roles/child authority and query bounds remain. |
| 18 | Time/memory/I/O/locks/hardware/admission complexity | FINDING | 10 MB staging cap improves; multi-file bytes/queues/per-ID refresh/locked HTTP checks lack aggregate budgets. |
| 19 | Retrieval/ANN/reranker/answer/recommendation quality | N/A | Products belong to later phases; source/result safety remains mandatory under 10/13/15. |
| 20 | Extraction/OCR/layout/multilingual/locator fidelity | N/A | Phase 18 parser/OCR unauthorized; current metadata safety failed under 12/13. |
| 21 | Adversarial/security/concurrent/OS/worker tests | FINDING | Fresh counterexamples succeed despite green suite; actual OS/worker/owner barriers incomplete. |
| 22 | Risk-oriented test/E2E/assertion/determinism/fixtures | FINDING | Fake real-PG fixtures useful; serial names/manual test cleanup do not prove production concurrency, FR17-8. |
| 23 | Hardware/latency/resource benchmark credibility | NOT_VERIFIED | Suite times are not queue/lock/I/O/memory benchmarks; no credible p50/p95/peak-budget evidence. |
| 24 | Held-out labels/evaluation leakage/judge calibration | N/A | Actual ML evaluation deferred; synthetic privacy/lifecycle proof remains required. |
| 25 | Dependency compatibility/licenses/advisories/lock lifecycle | NOT_VERIFIED | Actual versions measured; exact vector, enforced locked install, license/advisory/offline artifact evidence absent. |
| 26 | Build/static/format/test/tooling/architecture evidence | FINDING | Reused 1,199 receipts and fresh six focused tests/quality checks pass; exact new clean-verify receipt absent, warnings/coverage claims need triage. |
| 27 | Safe logging/metrics/operational completion/incident handling | FINDING | Redaction improves; false quiescence/PURGE_COMPLETE, arbitrary worker message propagation and missing retry/incident cleanup. |
| 28 | Documentation/governance/status/history/links/inventories | FINDING | Historical reviews preserved/current status synchronized; submitted all-CLOSED/26-PASS/version/benchmark assertions require correction. |
| 29 | Hygiene/artifacts/frozen scope/clean diff | PASS | No generated/cache delivery or unapproved frozen/deferred delta; descriptors retained, git diff --check passes. |

**Totals: 5 PASS, 19 FINDING, 3 N/A, 2 NOT_VERIFIED.** Passing tests do not authorize unresolved High privacy/consistency or missing essential runtime proof.

## Bounded next implementation priorities

1. Repair source/grant/complete dependency authority and the final post-wait expiry check; preserve existing owner guards and consent refresh.
2. Use one typed allowed-field input and verified result route; connect the accepted restricted worker fence, remove unchecked alternatives.
3. Make synchronous deny, committed worker notification and verified retryable purge distinct; unknown cleanup stays pending and duplicate attempts cannot fake quiescence.
4. Finish physical-COMMIT/independent OS and original lifecycle barriers. Enforce aggregate budgets and measure workflow costs; retain batching/caching/caps already repaired.
5. Correct current evidence/setup/inventories and record the exact required final commands after remediation, preserving historical reports.

**Next step:** Antigravity `/antigravity-implement-handoff` completes FR17-1–10 implementation and deterministic regression/runtime evidence under the existing Phase 17 handoff, then returns to `$codex-final-review`. No test-only slice, new handoff or owner commit approval. Phases 18–22, Phase 16 and Ideaverse retain separate gates; frozen authentication and closed SBA scope stay untouched.

