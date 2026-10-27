# Phase 19 — Grounded Markdown Q&A: preparation dossier

**Date:** 2026-10-27  
**State:** `READY_FOR_HANDOFF` (**preparation only**) after [independent Codex re-review 2](reviews/2026-10-27-phase-19-independent-pre-handoff-re-review-2.md), 2026-10-27. P19-PR-01–04 and P19-L01 are independently CLOSED for preparation. Runtime DB/HTTP/model/quality/resource evidence remains `NOT_VERIFIED / NOT_RUN`; no code, migration, activation or executable handoff is authorized by this review.  
**Owner authority:** explicit 2026-10-27 request to **prepare** P19 after six rounds of requirements decisions; no source implementation, external-index activation, executable handoff, Phase 20 or frontend authorization.  
**Verified PPV source:** GitHub `main` [`685b869a9d789c6e0a42198d1f59bce62b7e0613`](https://github.com/vhvkhangg/personal-private-vault/commit/685b869a9d789c6e0a42198d1f59bce62b7e0613), `docs(maintenance): record cleanup B publication closeout`.  
**Verified sample-corpus inventory:** [Ideaverse public `main`](https://github.com/vhvkhangg/Ideaverse/tree/b96d8f76a655a40c934cb73f68ca97d2d5cd929b) at `b96d8f76a655a40c934cb73f68ca97d2d5cd929b`; inventory only, **no copy/import/index operation performed**.  
**Execution handoff:** [`ACTIVE.md`](../handoffs/ACTIVE.md) remains `NO_ACTIVE_HANDOFF`. Phase 17 frozen; Phase 18 closed with CPU-only `.md` acceptance; indexing INACTIVE and original-file reads DISABLED/DEFERRED.

## Read in order

1. [Owner decisions and product scope](owner-decisions-and-scope.md) — accepted behaviors, out-of-scope features and separate Ideaverse-module future.
2. [Real source reuse and feasibility](source-reuse-and-feasibility.md) — examined code paths, verified capabilities, unresolved engineering feasibility; do **not** treat designs as implemented.
3. [Testable acceptance matrix](acceptance-matrix.md) — positive, negative, privacy and corpus-specific cases; no implementation asserted.
4. [Query consumer lifetime, canon/source selection and whole-file consent](query-consumer-lifetime-and-selection.md) — selected prospective design, races and precise authorization boundary.
5. [Owner decision P19-OD-01](owner-decision-p19-reader-exclusion.md) — **OWNER APPROVED option A (prospective design only)** for the minimum durable consumer exclusion / mutation guard delta; implementation remains separately gated.
6. [Codex pre-handoff review brief](preparation-review.md) — review checklist and proposed *not-yet-approved* slices.
7. [Changed-only correction manifest](PACKAGE_CONTENTS.md) — overlay inventory and exclusions.
8. [Current independent re-review 2](reviews/2026-10-27-phase-19-independent-pre-handoff-re-review-2.md) — preparation readiness; [original review](reviews/2026-10-27-phase-19-independent-pre-handoff-review.md) and [re-review 1](reviews/2026-10-27-phase-19-independent-pre-handoff-re-review-1.md) remain unaltered history. These independent reviews are separate from the author's bounded correction ZIP.

## Controlling precedence

- [Current Owner delivery policy](../../workflow/current-owner-delivery-policy.md) and [Docs Sync contract](../../workflow/documentation-synchronization.md) govern preparation and review.
- [Phase 18 D27 closeout](../phase-18/phase-18-final-owner-closeout-2026-10-25.md) governs accepted Markdown pipeline and limitations; [Phase 17 field eligibility](../phase-17/source-eligibility-and-allowlists.md) and [revocation fencing](../phase-17/revocation-and-derived-data.md) remain binding.
- [Historical RAG phase plan](../rag-phase-plan.md) is a phase-allocation guide, **not** a fresh mandate for OCR, GPU, reranking, asynchronous judges, dashboards or automatic indexing. Where it conflicts with the later Owner decisions and current policy, seek a bounded prospective decision; do not silently rewrite accepted ADRs.

## Minimal end-user result

Given a manually approved, currently valid index of Markdown snapshots, an authenticated Owner can ask a free-form question in Vietnamese (or another input language), optionally select particular indexed sources, and receive a **non-streamed** answer containing only directly documented facts, with resolvable file/heading/evidence locators. Multi-file extraction and short bounded client-supplied conversational context are allowed; unsupported claims and contradictions are flagged rather than invented. Responses must not contain unauthorized sources or stale/revoked content. No persistent chat history, new Ideaverse module or Fiction/Film recommendation belongs in P19.

## Approval flow — stop at this boundary

1. Owner imports this **documentation-only overlay** locally; it does not modify runtime, DB, configs, accepted history or `logs/`.
2. An **independent Codex** `$codex-pre-handoff-review` verifies exact baseline/local diff, requirements necessity, privacy/lifetime design, feasibility of source/retrieval/model path, finite slice/testing plan and Docs Sync Check. A review artifact and disposition may follow; this dossier is **not** the review result.
3. If blocked, resolve concrete findings/Owner decisions within explicitly approved docs scope; no unrequested source changes. If `READY_FOR_HANDOFF` is independently granted, the Owner publishes reviewed preparation using normal Git actions, then separately invokes `$codex-create-handoff` for the **first** approved implementation slice. Codex does not stage/commit/push. Follow existing policy on Owner phase/slice authorization; readiness is not executable permission.
4. Keep [ACTIVE](../handoffs/ACTIVE.md) at `NO_ACTIVE_HANDOFF` until a separately approved executable implementation handoff is deliberately issued. Phase 20/21/22, Ideaverse frontend/module, GitHub sync and GPU remain separate future questions.

## Docs Sync Check (author preparation, not independent certification)

| Disposition | Evidence / limit |
| --- | --- |
| `UPDATED` | New P19 prep dossier only, against GitHub PPV SHA and explicit Owner conversation decisions; no existing current-status file altered. |
| `ALREADY_CONSISTENT` | P17/P18 accepted boundary; indexing is not activated; current ACTIVE is `NO_ACTIVE_HANDOFF`. |
| `DEFERRED_OUT_OF_SCOPE` | Broad dated roadmap/historical notices, Ideaverse UI/sync, Fiction/Film recommendations, LOW backlog, later phase/milestone outcomes. Do not rewrite past reviews. |
| `NOT_VERIFIED` | Q&A consumer implementation/commit races, complete chunk provenance, real import, native CPU model quality/latency/RAM and local-tree changes: no runtime PASS claimed. |
| `UPDATED` | **P19-OD-01 option A explicitly Owner-approved on 2026-10-27** for the bounded prospective consumer/source-guard design; independent feasibility and any wider delta remain review gates, not assumed acceptance. |

**Next step:** Owner | publish reviewed preparation, then invoke Codex `$codex-create-handoff` for the Owner-approved first slice | `docs/implementation/phase-19/` + [independent re-review 2](reviews/2026-10-27-phase-19-independent-pre-handoff-re-review-2.md) | preparation published and first implementation slice explicitly approved; ACTIVE remains NO_ACTIVE_HANDOFF until the separate handoff | implementation remediation NOT_STARTED 0/3; no takeover.
