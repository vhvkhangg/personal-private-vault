# Phase 17 — Local RAG Foundation and Source Authority (RAG-first execution)

**Historical 2026-10-15 owner-approved Alternative B contract:** the owner approved the
complete [independent review §8 package](reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments),
recorded in [owner decisions](owner-decisions.md#2026-10-15--alternative-b-owner-design-approval)
and [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md). Contract
synchronization was independently accepted. The subsequent owner authorization covered Groups A–D implementation and tests
under the same handoff. That authorization was fulfilled and the handoff is now archived.
Default containment and live external-file disablement remain in force.

**Current status (2026-10-17): COMPLETE — FROZEN** for the approved Alternative B
single-job Local RAG Foundation. The owner committed/pushed the reviewed implementation
as [`829d294`](https://github.com/vhvkhangg/personal-private-vault/commit/829d294aca7d916851348ff21004c631915d572e),
then ChatGPT prepared the [formal closeout](closeout.md) and [archived handoff](handoff.md).
The [independent final re-review 3](reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md) passed **1,308 distinct Java tests**
(including 154 RAG tests), **37 Python tests**, **13 safety tests** and retained the independently
verified **420-job** bounded native resource evidence. Findings FR17-1–11 and AB-1–6
are closed only for Foundation scope. **AUTH-CLOCK remains OPEN** outside RAG; its exact-case
exception was not needed in the successful exit-0 clean run. Default containment and
live external-file disablement remain active; Phase 18 is not started or authorized.

Supplemental backend maintenance (SBA-001–007) is [closed and owner committed/pushed](../maintenance/supplemental-backend-audit-2026-10-13/closeout.md)
as `d195b626`; the maintenance closeout resets `ACTIVE.md` for a new handoff.

## Authorization and sequence

The owner explicitly starts RAG **before** Frontend. Retain original identifiers: **Phase 17 = RAG foundation**, **Phases 18–22 = later RAG delivery**, **Phase 16 = Frontend (deferred)**. See accepted [ADR-0019](../../adr/0019-local-rag-before-frontend.md) and [roadmap](../../roadmap.md). Backend Phases 0–15 are frozen with `BACKEND_AUDIT_READY`. The preparation's `NO_ACTIVE_HANDOFF` gate was satisfied before creating the current foundation handoff; it is now active for remediation. No later-phase implementation is authorized.

## Owner-approved RAG end-state requirements (not all Phase 17 deliverables)

- Operate on the owner's **desktop only**, 100% offline at runtime; no external AI API/telemetry/data egress; fewer than 500 document files initially.
- Grounded question answering and semantic retrieval from user-selected external `.md/.txt/.docx/.pdf/.xlsx/.xls` files and authorized **non-private** records from suitable Vault modules. Persistent selected-source indexing and ad-hoc ephemeral source analysis; explicit per-query scope/filter choice.
- Preserve Obsidian syntax (frontmatter, wikilinks, embed/link-target rules, callouts, tags, block refs, comments) and prepare clean future extension for Ideaverse **without adding Ideaverse integration yet**. Ideaverse is not currently in this repository and will be one-way/read-only if/when later authorized.
- OCR scanned PDF in Vietnamese, English, Simplified/Traditional Han with table/layout recognition and source coordinates; treat Hán Việt as Vietnamese terminology rather than a standalone OCR alphabet. Handle English UK/US variations.
- Structure-adaptive chunking, reproducible index versions, hybrid search + multilingual reranking, source-linked answers and **non-persistent evidence/search-term highlights**; fail safely on insufficient evidence.
- Recommendation ranking across appropriate domain items (first Fiction and Film) in **all** reading/viewing states, extensible to other domains. Only existing eligible catalog IDs may be returned, with validated rationale and optional owner-specified filters.
- Deterministic bounded routing between grounded QA, typed source-of-truth read-only structured queries and catalog recommendations; no full autonomous Agentic RAG in baseline.
- Per-ingestion, per-query and per-recommendation telemetry/evaluation summaries, async local-judge results and reproducible offline regression/benchmark dataset.
- **Private/NSFW never indexed**, and no passwords/PIN/tokens or unsupported sensitive fields copied into RAG. No write-back to business data, Obsidian, or external documents.

## Canonical design references

- [Approved Alternative B architecture](../../architecture/rag-architecture.md)
- [Accepted ADR-0019](../../adr/0019-local-rag-before-frontend.md)
- [Frozen Module boundaries](../../architecture/module-boundaries.md) and [dependency matrix](../../architecture/module-dependency-matrix.md)
- [Global Search](../../architecture/search-architecture.md), [Security](../../architecture/security-architecture.md), [Existing import/export](../../architecture/storage-backup-import.md)
- [Quality attributes](../../architecture/quality-attributes.md), [Existing API contract](../../architecture/api-architecture.md)

## Approved contracts and bounded Phase 17 runtime evidence

- [P17-1 approved ownership, reservations and DB roles](integration-ownership-and-permissions.md): one Java `rag` leaf, bounded public edges, Vault source/version/reset/reservation APIs and SQL backstops, RAG-owned process supervision and actual restricted-role test gate; target not runtime proof.
- [P17-2 authoritative classification and field matrix](source-eligibility-and-allowlists.md): individually approved non-private Fiction/Film and external-file sources only in the first slice; unreviewed modules, secret/sensitive fields and embedded children deny by default.
- [P17-3 revocation and derived artifact fencing](revocation-and-derived-data.md): synchronous deny, source epochs/content revisions, transclusion dependency invalidation, active/retired/staged content purge and deterministic late-worker/judge tests.

The end-state requirements above are allocated across Phases 17–22; only the **Phase 17 foundation** is authorized by the current handoff. All three preparation findings remain closed at design level; their design-level acceptance is historical, while bounded Foundation runtime enforcement was separately accepted in the 2026-10-17 final re-review. This is not permission for later phases. Owner preparation commit `dd414861` and reviewed implementation commit `829d294` are complete.

## Scope boundaries and implementation ownership (accepted and frozen Foundation)

- Approved Alternative B keeps public source projections, durable source-set reservations, one job-scoped worker, consent intent CAS, exact description confirmation, cancel-first conflicts and E1. Completed CURRENT indexes outlive execution credentials; historical jobs remain separate. No owner internals/worker business SQL. The former active handoff is archived. Later capabilities require separate preparation and explicit owner authorization.
- In the Foundation, Python/FastAPI operates the local supervised worker and restricted staging; Java owns authorization, source policy, job supervision and atomic publication. PostgreSQL 18.6 / pgvector compatibility was independently verified. Actual extraction/OCR, embeddings, retrieval, reranking, local model inference and answer evaluation are **deferred** to the separately owner-gated later phases.
- Preserve imported domain record semantics: indexing a `.md` or `.xlsx` does **not** automatically create a Vault Note/Information record and cannot sidestep existing ImportData preview/validation policy.

## Approved planning request: split RAG into real numbered phases (proposal)

The owner's planning decision replaces the earlier 17-A through 17-E *sub-slices* with **separate numbered phases 17–22**; see [RAG Phase Plan](../rag-phase-plan.md). This document's active P-17 review is now solely for foundational source authority/privacy/guard, worker and persistence/lifecycle. **Later work is intentionally deferred**: Phase 18 ingestion/OCR/Obsidian; Phase 19 QA/retrieval/highlighting; Phase 20 Fiction/Film recommendation; Phase 21 approved module expansion; Phase 22 full reliability/regression closure. Each later phase needs separate P-N preparation and handoff, not implementation permission from this P-17 review.

## Engineering quality gate

Apply repository skills `pragmatic-solid-design`, `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`, `backend-testing`, and proposed `local-rag-engineering` (subject to Codex acceptance). Prefer explicit roles and typed DTOs, dependency direction, ports at genuine provider/source boundaries, deterministic pure transform functions, KISS/DRY/YAGNI, meaningful exception classes, idempotent retries, fail-closed privacy, readable observability and versioned contracts. No gratuitous `ServiceImpl`, generalized agent frameworks or cross-module repository access.

The accepted Foundation tests cover Java/Python contracts, PostgreSQL authority, concurrency, privacy/consent, revocation, staging and native recovery; their exact evidence is in the final review. Future phases must prove OCR/tables/citations/retrieval/recommendation/model quality within their own scopes. Do not substitute Foundation digest tests for those later capabilities.

## Owner acceptance — 2026-10-13

The owner accepted only the bounded P17-1/2/3 foundation integration delta described in [owner decisions](owner-decisions.md). The preparation's `READY FOR HANDOFF` and commit/push steps are historical and complete. The accepted preparation and the later implemented Foundation are now complete and frozen. The archived handoff remains historical evidence; no current implementation handoff exists. Runtime dispositions come from the 2026-10-17 independent final re-review.

## Formal closeout and next step

1. Phase 17 preparation (`dd414861`) and reviewed implementation
   ([`829d294`](https://github.com/vhvkhangg/personal-private-vault/commit/829d294aca7d916851348ff21004c631915d572e)) are committed/pushed.
2. [Independent Final Re-review 3](reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md) returned `READY FOR OWNER COMMIT`:
   **1,308 Java / 37 Python / 13 safety cases PASS**; B01–B20 bounded Foundation acceptance satisfied.
3. [Formal closeout](closeout.md) freezes Phase 17, archives [P17-RAG-FOUNDATION](handoff.md),
   and resets [ACTIVE](../handoffs/ACTIVE.md) to `NO_ACTIVE_HANDOFF` in this separate documentation patch.
   The owner still needs to **commit/push this closeout patch**.
4. `AUTH-CLOCK` remains OPEN in Authentication; containment and disabled live external-file capability remain.
5. Phase 18 and other later phases are **not started** and require explicit separate owner authorization.
