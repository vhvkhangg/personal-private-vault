# Phase 17 — Owner decisions

## 2026-10-17 — Owner decision: AUTH-CLOCK narrow acceptance exception

The owner explicitly approved the narrow AUTH-CLOCK exception proposed in [Codex Independent Re-Review 2 §4](reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-2.md#4-independent-authentication-issue-and-owner-scope-decision):

> I explicitly track AUTH-CLOCK, limited to `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` on the documented Windows/WSL2 environment with unchanged frozen Authentication hashes and independent evidence of the same cross-clock failure, as an OPEN Authentication/environment issue outside Phase 17 RAG remediation. Its implementation/assertions stay enabled and unchanged; every actual failure and command exit remain reported. This does not establish Authentication acceptance, authorize its code changes, or waive any RAG privacy/lifecycle/resource requirement. All other mandatory Phase 17 evidence must pass. Any future Phase 17 acceptance despite this exact failure alone requires this explicitly recorded narrow gate exception; a failed `mvn clean verify` must still be called failed.

**Narrow Acceptance Rules:**
1. Permitted ONLY when `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` fails due to independently evidenced Windows/WSL2 PostgreSQL–JVM clock skew.
2. Frozen Authentication implementation and test assertions remain enabled and unchanged.
3. Every other mandatory Phase 17 test and acceptance requirement passes.
4. The real Maven exit code and all failures are reported accurately; a failed `clean verify` must NEVER be called PASS.
5. This exception does NOT waive any RAG defect, privacy, lifecycle, recovery, resource, or security requirement, and does NOT declare Authentication fixed or accepted.

## 2026-10-16 — Scope simplification: 20-minute command timeout & practical offline requirement

The owner approved two explicit decisions to eliminate unnecessary verification complexity under Alternative B and the existing `P17-RAG-FOUNDATION` handoff:

1. **Maximum Command Timeout: 20 Minutes (1200 seconds)**:
   - Supersedes the previous 600-second limit.
   - Applies to Maven, Java, Python, pytest, Docker, benchmarks, and all external processes.
   - Enforce via tool-level execution timeouts or process-tree watchdogs; at timeout, terminate only the tracked process tree and retain stdout/stderr/exit codes.
   - Diagnose timeouts rather than increasing beyond 1200 seconds.

2. **Practical Offline Contract Amendment (Superseding Strict OS-Enforced WFP Rules)**:
   - Supersedes the strict OS-level Windows Filtering Platform (WFP) / firewall elevation requirement.
   - In the single-user personal vault, RAG workers operate strictly offline by design: zero external endpoints are configured or accessed, all Python dependencies are pinned and self-contained locally, and workers communicate exclusively via local loopback (`127.0.0.1`) and restricted PostgreSQL connection.
   - Practical offline runtime checks and socket containment tests are accepted in lieu of operating-system-wide network disruption or elevated UAC firewall modifications.
   - If complete physical network disconnection is not active during an automated test run, report that exact limitation honestly instead of claiming OS-level firewall enforcement.

   **Separation of Requirements and Guarantees:**
   - *Requirements removed by explicit owner approval*:
     - Windows Firewall outbound-denial rules.
     - WFP 5157 event collection.
     - Administrator elevation for network enforcement tests.
     - System DNS/Dnscache blocking.
     - Global IPv4/IPv6/proxy denial enforcement.
     - Windows firewall or DNS audit-policy modifications.
     - Mandatory OS-level proof that every possible RAG outbound connection is blocked.
   - *Remaining practical-offline requirements*:
     - Java -> local Python -> PostgreSQL execution on the local machine.
     - No required Internet connection for indexing.
     - No external cloud AI service dependency.
     - No intentional external transmission of source content by the indexing pipeline.
     - Existing RAG consent, source eligibility, strict NSFW/Private exclusion, and provenance controls.
     - Durable reservations, PostgreSQL transaction consistency, and safe recovery.
     - Existing working native process supervision.
     - Approved E1 contract and independent persistent index lifetime.
   - *Tests that actually verify remaining requirements*:
     - Python worker tests and socket containment tests (`tests/test_bound_ingress.py`, `tests/test_consumption_containment.py`).
     - Java RAG integration tests verifying local loopback communication (`http://127.0.0.1:{port}`), opaque issued single-use capability verification, and database staging/publication.
     - Static code and configuration audits verifying zero external network endpoints or cloud clients.
   - *Guarantees that are no longer claimed*:
     - Operating-system-enforced egress isolation via Windows Filtering Platform rules or packet-level kernel drops.


3. **Operator Declared-Restore and Authority Suspension Procedure**:
   - Approved operator recovery procedure for database restores, ungraceful hardware faults, or uncertain source history:
     1. In-flight jobs cancelled and quiesced via `rag.cancelJob(jobId)`.
     2. Source index suspended / marked `REVOKED` via `rag.revokeSource(sourceId)`, immediately denying retrieval access to previously published index generations.
     3. Stale pre-restore consent and reset watermarks fail closed against CAS verification on subsequent admission attempts.
     4. Operator inspects fresh source state via `rag.findSource(sourceId)` and approves renewed consent via `rag.approveSource(sourceId, new RagConsentCommand(approvedFields, expectedSourceVersion, expectedConsentResetVersion, expectedAuthorizationEpoch, expectedFieldPolicyVersion, confirmedDescriptionHash))`.
     5. Fresh indexing job admitted with explicit versions via `rag.admitJob(sourceId, new RagIndexCommand(expectedSourceVersion, expectedAuthorizationEpoch, expectedFieldPolicyVersion, descriptionConfirmations))` and published to `CURRENT`.


## 2026-10-15 — Separate Alternative B implementation authorization

The owner accepted the independent contract synchronization review and explicitly
authorized Codex to implement Groups A–D and all mandatory real-boundary/full/
resource/offline verification under the EXISTING P17-RAG-FOUNDATION handoff.
This supersedes the contract-only authorization recorded below for the earlier
synchronization session; it does not alter the approved design decisions.
Default containment stays active; isolated test-scoped enablement only until
safe enabled behavior is proven. Keep ACTIVE CHANGES_REQUESTED and FR17-5/8/9/10
OPEN. No architecture redesign, new handoff, deferred scope, self-approval,
historical review rewrite, commit or push. Stop any affected path whose mandatory
security invariant cannot be established, and report actual evidence/blockers.

## 2026-10-15 — Alternative B owner design approval

**APPROVED CONTRACT; NOT IMPLEMENTATION AUTHORIZATION.** The repository owner
explicitly approved the complete four-part package in
[independent design review §8](reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments),
including its §4 stale-intent and exact-description-hash rules, in seven decisions:

1. Alternative B replaces proposed F+H+V+E: permanently one user/account, one
   Windows desktop, one active indexing job; explicit per-item consent (1B),
   manual reindex (2B), no automatic resumption (3B). Full Foundation scope remains.
2. Accept E1(a) post-deadline transfer/consumption, E1(b) late isolated untrusted
   S, E1(c) late trusted verification/publication after a valid gate, and persistent
   CURRENT index lifetime independent of completed execution lease/service session/
   boot. No strict-E2 physical-C-before-expiry guarantee remains. Every newly
   observed expired gate still denies; no implicit renewal or privacy waiver.
3. Approve bounded Vault-owned source state/version/reset/durable reservations
   and mandatory SQL backstops; RAG-owned single-job constraint, job-scoped Python
   supervision, one-use existing grant, stage-writer seal and atomic terminal
   P-or-abort/reservation release. No F/V/DRAIN or second job engine.
4. Approve explicit expected reset/source-version/authorization-epoch/field-policy
   CAS on approval/renewal, separate Private configuration veto and no restored
   consent after eligibility restoration. Description uses exact-hash confirmation
   from review §4: unchanged bytes reusable, changed bytes confirmed in manual
   Index/Reindex without redundant dialog; confirmation never renews item consent.
5. Approve cancel-first 409/no mutation C for relevant reserved-source edits,
   classifications/revocations/dependencies. Owner retries after verified safe
   cancellation/recovery. Unknown process/SQL keeps reservations and active slot;
   unrelated items remain editable. Denied mutations are never queued for later.
6. Ordinary eligible edits preserve participation/configuration but permanently
   OUTDATE old versions; no automatic reindex/resume. Restriction/reset/revoke
   COMMIT denies affected derivatives immediately and requires verified cleanup.
   Item configuration remains independent from RAG consent.
7. Preserve full original bounded Phase 17 Foundation, Spring Modulith ownership,
   restricted worker privileges/offline operation and all security requirements
   not explicitly amended. No later-phase implementation or new handoff.

Recorded in accepted [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md),
amending ADR-0019 clauses 2–5; P17-1/2/3 are synchronized normative contracts.
This approval supersedes contrary prior design proposals and the specific 2026-10-13
timing/revoke/lifetime rules. It is not a claim of runtime verification.

**Current authorized action: contract synchronization only.** No production/test
change, migration execution, execution enablement or containment removal. EXISTING
ACTIVE remains CHANGES_REQUESTED; FR17-5/8/9/10 OPEN. Separate IMPLEMENTATION
authorization then actual evidence and independent acceptance are still required.
No commit/push, new handoff, completion/freeze or owner commit readiness.

## Historical 2026-10-13 preparation acceptance (preserved record)

The record below describes the original preparation gate. Its pending-handoff/
workflow wording is historical; that handoff already exists. Its affected guard,
expiry, consent and revoke rules are amended above and in P17-1/2/3/ADR-0020;
it cannot authorize automatic implementation in this documentation-only session.


# Phase 17 — Owner Acceptance of Bounded RAG Foundation Integration

- **Decision:** APPROVED — bounded Phase 17 foundation *implementation scope*, not completed implementation.
- **Decision date:** 2026-10-13.
- **Authority:** Repository owner, following the [Phase 17 pre-handoff Codex acceptance](reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md).
- **Gate:** Preparation is `READY FOR HANDOFF`; the owner will commit/push the accepted preparation **before** Codex creates a separate implementation handoff. [`ACTIVE.md`](../handoffs/ACTIVE.md) remains `NO_ACTIVE_HANDOFF` until that action.

## Authorized, narrowly bounded integration delta

The owner accepts **P17-1, P17-2 and P17-3 as reviewed design contracts**, including the following additive implementation changes **only within the Phase 17 foundation handoff**:

1. **Module/API ownership:** add one Java `rag` orchestration module and reviewed `rag -> vault`, `rag -> fiction`, `rag -> film` public-contract dependencies. Add *minimal* owning-module read-only, versioned RAG projections and a Vault-owned transaction-scoped source-row guard. All relevant Fiction/Film/Vault content, classification, trash, restore and assignment mutations participating in eligibility must use the **same physical PostgreSQL transaction guard through COMMIT**, as specified in [P17-3](revocation-and-derived-data.md). No reverse dependency, generic JDBC access, or Python business-schema access.
2. **Source authorization:** explicit per-source NON_PRIVATE approval, current owning-domain proof, exact field allowlists, deny-by-default and revocation rules from [P17-2](source-eligibility-and-allowlists.md). Initial eligible sources are approved Fiction, Film and individually approved external files only; *no blanket approval for all records, fields or later modules*.
3. **RAG-owned persistence and worker isolation:** a bounded `rag` persistence namespace, Java-owned policy/jobs/generation authority, Flyway append-only migrations and a distinct least-privilege `rag_worker` role, subject to actual PostgreSQL negative-grant tests; no worker DDL, owner-domain SQL reads, policy grants or active-generation publication. Follow [P17-1](integration-ownership-and-permissions.md).
4. **Revocation and concurrency:** last-check-to-COMMIT protection, Windows/NTFS live-file handle/identity protocol or fail-closed exclusion, worker staging/attempt-row COMMIT fencing, purge and deterministic interleaving tests. Unsupported file systems/writers or unsafe mutation routes must be disabled until proven safe, not downgraded to best-effort checks.
5. **Synchronized implementation documentation:** in the authorized Phase 17 implementation slice, update affected module boundaries, dependency matrix, package tree, architecture/C4/Structurizr diagram *sources*, migration/data inventory and affected named-interface docs alongside actual code. Historical artifacts, frozen Schema v1 and Phase 0–15 acceptance records remain untouched; additive changes are explicitly traced to this owner decision and [ADR-0019](../../adr/0019-local-rag-before-frontend.md).

**Approval means these deltas may be included in a future, separately reviewed Phase 17 handoff**; it does *not* mean any guard, role, source approval, NTFS behavior or privacy enforcement already exists. Actual security, transaction, filesystem, role and recovery tests are mandatory acceptance blockers. Codex must refuse an incomplete or wider handoff.

## Exclusions and future follow-ups

- No production changes in this owner-acceptance documentation slice, no module/schema/SQL rewrite of Phases 0–15, no automatic GitHub push, no implementation handoff yet, no Phase 16 Frontend or Ideaverse integration.
- No actual PDF/OCR/Excel ingestion, local QA chatbot, recommendation features, module expansion or full system evaluation are approved by this Phase 17 gate; Phases 18–22 each need their own preparation, Codex review, handoff and owner commit.
- **P17-N1 (nonblocking):** establish source-grouped, disjoint development/frozen held-out evaluation splits, human labels, judge calibration and reproducible dataset hashes **before acceptance of the first parser/model/judge/recommendation capability**, not only Phase 22. Record this in P-18/P-19/P-20 preparation as applicable; Phase 22 consolidates results. See [benchmarks and acceptance](benchmarks-and-acceptance.md).
- **P17-N2 (nonblocking):** general workflow summaries must label Phase 15 `READY FOR AUDIT` routing as *historical*; it is not a valid active Phase 17–22 preparation branch. Original historical review records are immutable.

## Owner workflow after this decision

1. Apply this documentation-only acceptance patch to the latest repository and inspect `git diff`/`git status`.
2. Owner commits and pushes Phase 17 foundation preparation using Codex's approved message: `docs(rag): prepare phase 17 foundation and phased delivery gates`.
3. In the same Phase 17 Codex context, run **`$codex-create-handoff` for Phase 17 foundation only**, referencing this decision, ADR-0019 and the accepted review; do not start implementation until Codex creates the bounded handoff.
