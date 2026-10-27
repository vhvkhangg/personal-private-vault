# P19 — Independent Codex pre-handoff review brief (NOT a review result)

**Status:** `READY_FOR_HANDOFF` (**READY FOR HANDOFF**) — [independent re-review 2, 2026-10-27](reviews/2026-10-27-phase-19-independent-pre-handoff-re-review-2.md). **P19-PR-01–04 and P19-L01 CLOSED for preparation.** The durable incident/reader-release contract is independently ready as prospective design; runtime implementation, quality and DB/HTTP evidence remain `NOT_VERIFIED / NOT_RUN`. [P19-OD-01 option A](owner-decision-p19-reader-exclusion.md) remains the approved design boundary. Previous reviews are preserved history. No implementation, migration, activation, executable handoff or takeover is authorized by readiness. Implementation remediation NOT_STARTED 0/3.  
**Owner authorization:** preparation/documentation only, 2026-10-27; source behavior decisions complete.  
**Baseline:** PPV GitHub `main` `685b869a9d789c6e0a42198d1f59bce62b7e0613`, Ideaverse public `main` `b96d8f76a655a40c934cb73f68ca97d2d5cd929b` (inventory reference only).  
**No active implementation handoff:** [ACTIVE](../handoffs/ACTIVE.md) `NO_ACTIVE_HANDOFF`.

## Required user-visible capabilities and necessity check

| Capability | Required by Owner? | Simplest candidate | Reuse | Evidence to demand |
| --- | --- | --- | --- | --- |
| Exact, cited Markdown Q&A, multilingual with abstention | YES | Scope-checked vector top-K + small CPU local model + strict citations | P18 snapshot/parser/chunks/embedding, P17 source authority | Testable cited/partial/conflict/unknown outcomes |
| Short context without persistence | YES | Bounded client-supplied recent turns only | Existing HTTP/validation conventions | Context/hijack/size negative tests |
| Canon `CORE/` default, optional noncanon selection | YES | Owner-reviewed source-ID/revision CORE roster per request, applied **to every contributing evidence span before ranking** | Approved external snapshot IDs + manifest and locator | Reject mixed-source chunk if provenance not separable; no implicit folder metadata |
| Source-use read lifetime against revoke during slow inference | YES (privacy invariant) | **Prospective** durable active-reader exclusion C_A→C_E→C_RL + stop-before-change; see [full contract](query-consumer-lifetime-and-selection.md) | Existing source/mutation guards as primitives only | Owner P19-OD-01 option A APPROVED prospectively; independent DB/HTTP feasibility review and later real race evidence still required, no long SQL locks |
| One local CPU model | YES | At most two measured candidates; one pinned offline runtime | Existing process safety principles | Bounded RAM/offline/cancel/quality proof |
| Hybrid reranker, LLM judge, agent, extra DB, GPU, UI | NO | DEFER / REMOVE | None required | No speculative expansion |

## Mandatory source-based review tasks

1. Verify exact working-tree vs entry SHA, edited docs, manifest, linked current policy, no `ACTIVE` activation or frozen Phase 17/18 rewrite. Do not trust assumptions from this author's GitHub spot inspection.
2. Inspect existing Java RAG snapshot/index/currentness, `rag_worker` permissions, mutation guards and P17 R1–R8. Independently scrutinize proposed **durable source-scoped active-reader C_A→C_I→C_E→C_D/C_RL** (event order varies) contract, source mutation 409, pre-emission versus `EMITTING`-first incident ordering, durable pending incident/inspection handoff, zero-row `reserved_by IS NULL` compatibility, atomic real denial-and-release, context/sender disposal and crash/unknown reconciliation. Do not mistake last status check for lifetime. P19-OD-01 option A prospectively approved; do not implement or expand frozen SQL/schema delta without Owner choice. Actual DB/HTTP races still NOT_RUN. No long SQL locks.
3. Trace actual chunk contributor source-ID/revision/locator, parent and embed dependencies. No trusted CORE folder label exists in source, so evaluate **Owner-declared source-ID/revision roster**, exact per-span `CORE` membership, mixed-chunk whole-rejection, noncanon opt-in, duplicate basenames and generic mode. Fail closed if provenance/roster cannot be established; no implicit folder-group or Ideaverse schema.
4. Trace Python pinned 384d query embedding, existing Java/Python loopback/job worker and DB roles; do not assume indexing worker is an already authorized QA daemon. Compare minimal options and reject unneeded service abstractions/network clients.
5. Check CPU memory/latency and generation format feasibility on supported Windows hardware; model not yet selected. Propose exact ≤2 candidate shortlist with license, checksums, runtime and bounded evaluation, **not** unverifiable claims of model quality.
6. Evaluate [frozen 24-slot quality fixture protocol](acceptance-matrix.md#frozen-proposed-qa-fixture-contract--24-slots-not-yet-executed) including **mandatory answerable VI/EN/Han, multi-source, context, partial, conflict**. Answerable abstention is FAIL. Keep deterministic citation/locator/source rejection separate from finite sampled semantic material-claim support. Review source-grouped development/held-out and exact acceptance thresholds; no automatic LLM judge.
7. Explain exact manual **test-only** import/index activation and non-activation of production external indexing; no live original reads, no runtime model download. Any need to change accepted P17/P18 invariants or enable production is an **Owner decision**, not reviewer discretion.
8. Validate Docs Sync Check dispositions, future docs impacts, historical review preservation and retained LOW backlog. No invented review readiness status.

## Proposed finite slice ordering — review/change freely BEFORE handoff

- **Candidate S19-1 — Read-only authorized retrieval API and citation contract:** minimum scopes, source/currentness/lifetime, pinned query vector, small exact top-K, locator mapping, structured abstention. No LLM/network/model generation yet. Must demonstrate secure read against concurrent revoke and document source selection; Codex may reorder if consumer lifetime needs a different vertical slice.
- **Candidate S19-2 — Bounded offline CPU grounded-answer path:** candidate model evaluation/lock, small context + few turns, non-streaming grounded answer/conflict/partial/abstention, no leak before post-generation gate, safe cancellation/cleanup.
- **Candidate S19-3 — End-to-end acceptance/enablement (only if genuinely needed):** verify selected approved real Markdown with test-only import/index and full 20–30 Q&A corpus; focused Windows runtime/DB/privacy races and documentation. **Do not invent separate large slice** if this is proportionately included in S19-2. Do not enable general production indexing without Owner authorization.

This is a **candidate plan**, not approval for all slices. Codex should prefer two slices if all mandatory gates can be verified there; avoid multiple layers just to mirror phase numbering.

## Questions requiring Owner decisions

Functional questions are resolved; **P19-OD-01 option A is OWNER_APPROVED at design level** for the smallest durable consumer state and source-guard delta. Treat source implementation feasibility P19-F01–F07 as NOT_VERIFIED until independently assessed; Codex must identify the exact minimal additive migration/guard updates, ensure they fit the approved design and flag any **wider** delta for Owner decision. This approval does not authorize code changes, indexing activation or a new Ideaverse module, model-fetch network, format expansion or QA activation.

## Independent output contract

Provide review markdown per [pre-handoff review template](../../templates/pre-handoff-review.md): exact local/remote baseline, evidence, capability necessity, code reuse, missing minimal mechanism, meaningful severity, finite slice plan, per-issue Docs Sync Check, and one of `READY_FOR_HANDOFF`, `CHANGES_REQUESTED`, `OWNER_DECISION_REQUIRED`. The reviewer writes only within its approved review artifact scope; **no implementation or git staging/commit/push**. `READY_FOR_HANDOFF` is not `READY_FOR_IMPLEMENTATION` or P19 acceptance.

**Next step:** Owner | publish reviewed preparation, then invoke Codex `$codex-create-handoff` for the Owner-approved first slice | `docs/implementation/phase-19/` + [independent re-review 2](reviews/2026-10-27-phase-19-independent-pre-handoff-re-review-2.md) | preparation published and first implementation slice explicitly approved; keep `ACTIVE = NO_ACTIVE_HANDOFF` until the separate handoff | implementation remediation NOT_STARTED 0/3; no takeover.
