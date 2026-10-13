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
