# Phase 19 — Owner authorization for S19-1 handoff

- **Owner decision date:** 2026-10-27.
- **Decision:** `OWNER_APPROVED — PHASE_19 / S19-1_ONLY`.
- **Owner statement:** The Owner explicitly approved beginning implementation of Phase 19, **initially only S19-1**, following the independently accepted P19 preparation. This document records that direct approval; it is not a Codex review result, implementation handoff, runtime test or permission for later slices.
- **Accepted preparation:** [independent Codex pre-handoff re-review 2](reviews/2026-10-27-phase-19-independent-pre-handoff-re-review-2.md), `READY_FOR_HANDOFF`, with P19-PR-01–04 and P19-L01 closed **for preparation only**.
- **Published preparation:** [`17f66bb2d589f9cb4fde8aba821d96a1851a54c9`](https://github.com/vhvkhangg/personal-private-vault/commit/17f66bb2d589f9cb4fde8aba821d96a1851a54c9) (`docs(rag): prepare phase 19 grounded Markdown Q&A`), verified before preparing this record.
- **Execution state now:** [`ACTIVE.md`](../handoffs/ACTIVE.md) = `NO_ACTIVE_HANDOFF`; Codex has **not** issued a P19 implementation handoff, and Antigravity is **not** authorized to start work from this decision alone. Implementation `NOT_STARTED`, remediation `0/3`, takeover none.

## Exact approved first slice — S19-1

**Name:** `P19-S19-1 — Authorized Markdown Retrieval and Citation Boundary`.

The Owner approved only the independently prepared first slice of [Phase 19](README.md), as reviewed in the [P19 pre-handoff dossier](preparation-review.md):

1. A minimal, authenticated, **read-only backend/API retrieval** route for previously approved, correctly indexed **Markdown immutable snapshots**. Source selection may be limited by approved source IDs/selected revisions; generic Markdown queries must not be subjected to an implicit Ideaverse CORE-only rule.
2. Reuse Phase 17/18 source and dependency authorization, snapshot/current-generation predicates, approved existing 384-dimensional multilingual query embedding, PostgreSQL/pgvector and actual chunk contributor/locator provenance. Favor simple bounded **exact top-K**; do not introduce a second vector DB, automatic reindexing or speculative retrieval layers.
3. An Owner-declared, revision-bound Ideaverse canon roster limits canon-mode evidence to approved `CORE/` contributors; embedded/transcluded children remain independently authorized and canon-classified. No new Ideaverse module, repository sync or implicit filesystem folder authority.
4. **P19-OD-01 Option A** — one minimal, RAG-owned durable active-reader/source/dependency exclusion extending through authorized response emission, source-guarded lifecycle, incident registration/denial handoff, quiescence, cancellation/crash/unknown-commit recovery, and guard-compatible generation invalidation/purge. Normal overlapping edit/revoke is busy/cancel-first; mandatory integrity/authority incidents must never disappear behind busy or a release/admission gap. Preserve accepted P17/P18 semantics and do not hold database locks across HTTP/process waits. Follow the [prospective consumer contract](query-consumer-lifetime-and-selection.md) and [approved reader design](owner-decision-p19-reader-exclusion.md).
5. Return **authorized evidence snippets and resolvable source/heading/span citations**, or an explicit structured no-evidence/denied/unavailable outcome. Reject stale/unapproved/mixed-unattributable chunks and citations. No model-generated answer in S19-1.
6. Focused positive/negative **Java/PostgreSQL/HTTP integration tests** appropriate to this slice, including source/child revoke and incident races, C_I versus emission, no gap between C_D and C_RL, unknown database outcomes, sender cancellation/quiescence, direct SQL guard bypass attempts, permission failures, provenance and citation correctness. Require evidence rather than recording speculative `PASS`; respect Windows resource/cancellation limits and implemented bounded runner.

Codex may choose minimal classes, API DTOs, RAG-owned schema/guard changes and narrow test organization in the executable handoff **only when necessary** to meet these approved behaviors; it may not broaden domain-module permissions or silently amend a frozen security contract. Any necessary material exception returns for a separately explicit Owner decision.

## Not approved by this decision

- **S19-2** local LLM/generation, model selection, conversational answering/abstention evaluation, and full end-to-end multilingual answer acceptance; S19-3 is merely an optional proposal and **not** approved. Later slices need their own explicit Owner scope approval and handoff.
- Phase 20–22, Fiction/Film Q&A or recommendations, Phase 16 Frontend, Ideaverse module/frontend, GitHub sync/watchers, OCR/Office/PDF/TXT, GPU work, second DB, agents, model judge or a general RAG deployment.
- Automatic **production external indexing activation**, live originals, bypass of snapshot/consent/Private/NSFW/whole-file approvals or any historical Phase 17/18 accepted invariant. Bounded, explicitly approved fixture-based import/index tests under existing controls are permissible where indispensable; no automatic operational rollout.
- Running implementation from a prepared dossier, marking acceptance `PASS` without tests, taking over implementation, staging/committing/pushing by an agent, or editing immutable independent review artifacts/logs.

## Gates and exact next action

1. **Owner** publishes this narrow documentation/status synchronization with normal Git staging, commit and push. Until publication, local docs reflect an Owner decision awaiting repository publication.
2. **Codex** runs **`$codex-create-handoff` only** in repository root. It reads this canonical Owner authorization plus [P19 preparation readiness](preparation-review.md), checks `ACTIVE = NO_ACTIVE_HANDOFF` and the actual working tree, then creates a **single** S19-1 executable handoff using [handoff standard](../../templates/handoff.md). No supplementary free-form prompt is needed.
3. Only after Codex has created the accepted, explicit `READY_FOR_IMPLEMENTATION` handoff does **Antigravity** run `/antigravity-implement-handoff` for that scope. Initial self-review then independent Codex slice review; implementation remediation still `0/3` at authorization.
4. Owner-only publication follows accepted independent implementation review. Neither handoff creation nor readiness authorizes S19-2, blanket indexing activation or phase closeout.

## Docs Sync Check — scope authorization only

- **UPDATED:** current S19-1 authorization and the *current-status pointers* in workflow/agent/phase/handoff navigation. This is not a new review finding, skill invocation or runtime feature.
- **ALREADY_CONSISTENT:** independent P19 preparation `READY_FOR_HANDOFF`; P17 frozen / P18 closed; `ACTIVE = NO_ACTIVE_HANDOFF`; no unresolved preparation findings. Prior P19-OD-01 approval retained.
- **NOT_VERIFIED:** S19-1 runtime/security/HTTP/migration/quality tests remain not run; any SQL/migration change requires an actual applied-version inspection during implementation.
- **DEFERRED_OUT_OF_SCOPE:** historical review artifacts, immutable receipts, Phase 20+, old maintenance status snapshots and unrelated LOW backlog.

**Next step:** Owner | commit/push S19-1 authorization documentation, then Codex `$codex-create-handoff` | [this S19-1 decision](owner-authorization-s19-1-2026-10-27.md) + [`READY_FOR_HANDOFF` preparation](preparation-review.md) | published authorization and `ACTIVE = NO_ACTIVE_HANDOFF`; implementation remediation 0/3, no takeover.
