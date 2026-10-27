# Independent pre-handoff re-review 2 — Phase 19

- **Date / reviewer:** 2026-10-27 / Codex, independent of the preparation/correction authorship.
- **Disposition: READY FOR HANDOFF / READY_FOR_HANDOFF — preparation only.** P19-PR-01–04 and P19-L01 are **CLOSED for preparation**. No OPEN CRITICAL/HIGH/MEDIUM preparation finding or unresolved material Owner choice remains.
- **Authority / scope:** Owner-requested preparation review and [P19-OD-01 option A](../owner-decision-p19-reader-exclusion.md), prospective design only. This review does not authorize implementation, apply a migration, activate indexing/Q&A or accept Phase 19. [Current Owner policy](../../../workflow/current-owner-delivery-policy.md) and [Docs Sync](../../../workflow/documentation-synchronization.md) remain controlling.
- **Baseline:** local main/HEAD and observed remote main equal **685b869a9d789c6e0a42198d1f59bce62b7e0613**. Entry index/tracked tree unchanged; eight untracked preparation documents plus the two previous reviews. Source, tests, migrations, locks/builds, policies, agents/skills/hooks/config and [ACTIVE](../../handoffs/ACTIVE.md) are unchanged.
- **Phase / counters:** P17 FROZEN; P18 CLOSED; P19 implementation NOT_STARTED/unapproved; ACTIVE **NO_ACTIVE_HANDOFF**. Indexing INACTIVE; live originals DISABLED/DEFERRED. Preparation re-review 2 consumes **no** implementation remediation round: **0/3**, no takeover. Historical counters and LOW backlog remain unchanged.

## Closure of the remaining finding

**P19-PR-01 HIGH — CLOSED for preparation.** The [revised incident contract](../query-consumer-lifetime-and-selection.md#durable-incident-handoff-at-emission-p19-pr-01) now addresses each missing branch from [re-review 1](2026-10-27-phase-19-independent-pre-handoff-re-review-1.md):

| Required behavior | Verified preparation correction |
| --- | --- |
| Incident cannot disappear while an existing writer emits | Trusted Java registers scoped durable inspection/recovery state before authority-sensitive inspection, then a uniquely bound pending incident C_I under the same source/reader guards. |
| Incident-first and emission-first orders are distinct | C_I before the guarded EMITTING transition prevents body output. If EMITTING wins, only the already-authorized writer may finish; durable pending state blocks new admissions. No client receipt/ACK guarantee is claimed. |
| No release-before-denial interval | After positive sender/context/worker quiescence, authoritative C_D and terminal/exclusion release C_RL share one physical commit. A separately committed C_D is acceptable only with a proven continuous durable denial barrier through subsequent guarded release. |
| Producer and finisher cannot lose an observed/unresolved incident | Finisher checks committed pending incidents **and unresolved preregistered inspection operations** under the guards; reader terminality or an empty final-incident lookup is insufficient. |
| Failure, cancellation, crash and unknown commit preserve safety | Reader/incident obligations survive; guarded PostgreSQL read-back resolves actual outcomes. No timeout, process-exit assumption, ordinary cancellation or independent release callback clears unresolved denial. |
| Current reconciliation is not falsely credited | The contract explicitly identifies the reserved_by IS NULL zero-row path and requires a narrow matching-reader/source CAS/finisher, affected-row checks, generation denial and purge integration. |
| Required proof is finite and executable later | R04/R05/R07/R09 and the acceptance matrix specify actual Java/PostgreSQL/HTTP incident orders, the release/admission boundary, transcluded child, unknown outcomes and stalled-writer cases. All remain NOT_RUN. |

This closes a **design/preparation gap**, not an implemented runtime defect. The correction stays within option A's approved minimum durable-state/guard boundary. It does not require choosing A again or introducing another incident platform, queue or general job engine.

**P19-L01 LOW — CLOSED.** source-reuse-and-feasibility.md:12 now states approved prospective design with runtime feasibility pending. acceptance-matrix.md:61 correctly assigns failed quality cases to later model selection/implementation acceptance, not to an unexecuted documentation review.

**P19-PR-02–04 remain CLOSED for preparation.** The whole-file/internal-marker exception, revision-bound explicit canon roster and per-contributor selection, and mandatory answerable VI/EN/Han plus finite 24-slot quality rule remain intact. No regression or new blocking finding was identified.

## Source feasibility and bounded handoff conditions

The [source integration map in re-review 1](2026-10-27-phase-19-independent-pre-handoff-re-review-1.md#source-based-minimum-integration-map) remains applicable at the identical source commit. Critical compatibility points were rechecked:

- RagRecoveryService.reconcileSnapshots currently updates integrity denial only for unreserved sources. The corrected contract no longer treats this as an incident handoff for a Q&A reservation.
- V18's reservation release and external-source mutation guard provide a feasible RAG-owned transaction boundary. The guard distinguishes reservation changes from protected identity/version/epoch changes. A future trusted finisher can validate the exact source/token/reader proof and establish monotonic integrity denial plus matching release under a single transaction; an ordinary release followed by a later denial is prohibited. This is source-based feasibility, not executed SQL proof.
- RagGenerationState/RagSourceAuthority predicates, source/dependency guards, generation invalidation/purge and recovery must all honor the same pending-incident/admission predicate. No worker policy/publication rights or broad mutation bypass follows.
- The actual server write/flush lifecycle must retain the durable exclusion. The guarded EMITTING commit precedes the first body byte; actual sender completion or proved no-further-output precedes release. No SQL transaction spans HTTP, model or process waits. Existing indexing/native receipts do not certify this new boundary.

The executable handoff must name the minimum RAG reader/pending state, exact SQL/Java/CAS paths, API/cancellation/sender ownership and positive-stop/recovery tests. Highest migration **in source** remains V23; inspect actual applied history before assigning an additive migration version. Preserve applied migrations and frozen v1/business-module ownership. Prefer a RAG-only delta for external Markdown; a wider Vault/domain dependency or changed frozen contract returns to Owner.

Model/runtime installation, CPU quality/latency/RAM/headroom, exact retrieval measurements, real approved-file import and native DB/HTTP races remain **NOT_VERIFIED / NOT_RUN**. These are explicitly required implementation/release gates, not evidence that preparation itself failed. Never promote a speculative model, broad runtime profile or unrun case to PASS.

## Owner need, reuse, effort and non-goals

The original review's **complete R19-01–16 inventory** and necessity/effort matrix are reused at the unchanged source baseline. Every Owner-visible function remains covered: Markdown-only factual Q&A, precise citations, multilingual names/languages, partial/conflicting/no-answer outcomes, short request history, generic/narrow/canon selection, current snapshot consent, authenticated offline operation, finite CPU resources, at most two model candidates, finite evaluation, no persistent chat/content logs, test-only enablement and backend-first delivery.

| Feature / mechanism | Disposition / simplest adequate solution | Implementation / verification burden |
| --- | --- | --- |
| Snapshot/parser/chunker/384d embedding and pgvector | REUSE exact pinned pipeline and RAG-owned evidence; no new parser/vector store | Focused bridge/query work / locator and identity tests |
| Scope/canon and whole-file consent | KEEP explicit source/revision roster and actual child/span filter; reuse existing consent | Medium / paired authority and mixed-source cases |
| Consumer and incident lifetime | KEEP one minimum durable reader/pending-incident relationship with existing ordered guards | High at this new boundary / finite real DB/HTTP races |
| Generation, history and response | SIMPLIFY one non-streamed offline CPU model path and bounded request-local recent turns | Medium / groundedness, context, cancel and output checks |
| Evaluation | KEEP 24 frozen question slots plus separate risk-based boundary cases; at most two candidates | Bounded measured trials; no judge/signature/dashboard |
| APIs/packages/errors/process ownership | REUSE existing RAG internal/public boundaries, root HTTP envelope and native safety primitives | Focused changed-contract/architecture/native verification |
| Extra agents/hooks/frameworks, OCR/GPU/R7, UI, catalog and Ideaverse sync | REMOVE/DEFER from P19 | No current implementation or benchmark obligation |

No material scope choice remains unanswered for preparation. Exact implementation details may be selected within the approved boundary; any wider schema/security/activation delta needs an explicit Owner decision. CPU-first remains mandatory; no model download, dependency upgrade or live-file capability is authorized here.

## Finite delivery and verification gates

Prefer **two separately committable slices**: S19-1 authorized retrieval/reader/incident/citation boundary; S19-2 one offline CPU generator, bounded conversation and end-to-end acceptance. The dossier's S19-3 remains optional, not an extra gate. Do not add a public endpoint just to match a technical layer.

For S19-1 require focused source/child selection and citation tests, real PostgreSQL role/CAS/incident-release races, actual bounded HTTP sender behavior, default-disabled/authenticated exposure and affected architecture checks. For S19-2 require the frozen 24-slot protocol and actual sample-answer examination, pinned runtime/artifact checks, CPU/system-memory/cold/warm/queue-inclusive measurements, offline/missing-artifact/error/cancel/restart tests and changed end-to-end races. Test-only import uses the existing explicitly enabled disposable setup and individually approved snapshots; production indexing remains inactive.

Antigravity implements/tests and always self-reviews; independent Codex reviews each slice. After all slices, one phase-final aggregation reuses valid evidence and verifies changed risks. Owner publishes each accepted slice, then phase closeout and the separately authorized P17–19 milestone; later audits are not started by this preparation. Existing backend-implementer, python-rag-coding, bounded-command-execution and expressly authorized takeover workflow suffice; no new agent or hook is justified.

Use the **implemented** watchdog: ordinary cap **300s**, bounded progress diagnostics/owned-tree cleanup; **up to 2,100s** only for eligible explicitly approved full suites. The handoff must name exact changed-test commands and source-appropriate final gates. No repeated full suite is required for this docs-only review.

## Coverage and evidence limits

All 29 shared dimensions are accounted for below. PASS concerns **preparation adequacy**; future runtime NOT_VERIFIED is never a runtime PASS. Unchanged detailed evidence is reused from the two preserved reviews, rather than generating another audit.

| Dimensions | Status / evidence |
| --- | --- |
| 1 intent, 2 traceability, 10 state correctness | PASS corrected contracts, Owner option A and finite acceptance |
| 3 architecture, 4 ownership, 5 package tree, 6 shared APIs | PASS unchanged baseline/inventory and bounded RAG delta; no new module/edge |
| 7 SOLID | N/A new implementation; narrow reader/finisher responsibility specified |
| 8 DRY/KISS, 9 patterns | PASS pipeline reuse; no generic queue/agent/provider platform |
| 11 wire API, 12 validation, 13 privacy, 14 credentials/retention | PASS specified scope/consent/citation/no-content/offline contracts; new runtime proof NOT_VERIFIED |
| 15 transactions, 16 failure/recovery, 17 DB/roles | PASS source-feasible incident/release protocol; actual migration/CAS/native races NOT_RUN |
| 18 complexity/resources, 23 performance | PASS bounded measured plan; actual CPU/model/sender budgets NOT_VERIFIED |
| 19 retrieval/grounding, 20 Markdown/languages, 24 evaluation | PASS finite positive/negative/semantic criteria; actual quality NOT_RUN; OCR/other formats N/A |
| 21 security testing, 22 test adequacy | PASS concrete incident/child/unknown-outcome cases; implementation tests NOT_RUN |
| 25 dependencies | PASS unchanged lock/pipeline and bounded candidate-selection plan; installed generator NOT_VERIFIED |
| 26 build/static/IDE | N/A fresh build for docs; no IDE inspection or warning-free claim |
| 27 logging, 28 documentation/governance, 29 delivery hygiene | PASS scoped content-free evidence policy, reviewed docs/status/links and unchanged index/source |

Actual checks: local/remote Git baseline and empty tracked/index diff; all eight current preparation documents inspected; previous-review hash checks; original option A text verified unchanged apart from its expressly labeled clarification; critical source/reservation compatibility rechecked; entry link/heading audit **82 local links, zero missing targets or unresolved anchors**.

Preserved reviews: initial **cc1f33dd406b2fa03f416b7b6a8000d5accd8292cc7a6af007882be64dbfe591**; re-review 1 **ab40f07af674f14dd106e88512a89d959c9de1fe44799fb1bb92390ec9d13d76**. The original approval body reconstructs its prior SHA-256 **eeb541b7a6f9bd82c765159cf80c4ed1073715668e4d5f7d7b1b7f168cb93b0f** after excluding the new labeled clarification. No historical finding/evidence was rewritten.

No Maven, pytest, Ruff, mypy, IDE, DB/migration, model inference, actual sender test or import/index execution occurred. Earlier receipts retain their original scope. External corpus inventory remains author-supplied reference, not newly verified import or permission to access more files.

## Docs Sync Check and publication boundary

| Disposition | Scoped result / ownership |
| --- | --- |
| UPDATED | New independent re-review; preparation-review.md and README review-status/next-action entries only. No routine product-contract correction by reviewer. |
| ALREADY_CONSISTENT | Approved prospective option A; durable incident finish; consent/canon/evaluation rules; existing policies and NO_ACTIVE_HANDOFF distinguish preparation from implementation. |
| NOT_VERIFIED | All listed future runtime/quality/resource/native/DB proof; actual DB applied-migration history. These remain mandatory handoff/implementation acceptance conditions. |
| DEFERRED_OUT_OF_SCOPE | Older global/scoped notices and existing B1/B2/MA/DOCB LOW backlog; no repository-wide cleanup. Preserve B3-L01 CLOSED and all historical counters/receipts. |
| BLOCKED_OWNER_DECISION | None for this preparation; wider implementation/contract/activation changes return to Owner. |

The author's PACKAGE_CONTENTS remains a truthful changed-only overlay relative to its named Owner ZIP; it is not silently rewritten as a Git-main publication manifest. The **reviewed preparation publication scope** is the complete phase-19 directory: eight preparation documents (README, scope, source reuse, acceptance, consumer contract, Owner decision, preparation status, overlay manifest) and all **three** independent reviews. No source/tooling/ACTIVE/logs or other phase changes are included.

**Suggested Conventional Commit:** docs(rag): prepare phase 19 grounded Markdown Q&A

**Next step:** Owner | publish the reviewed P19 preparation, then invoke Codex $codex-create-handoff for the Owner-approved first slice | docs/implementation/phase-19/ and this review | preparation published and first implementation slice explicitly approved; keep ACTIVE NO_ACTIVE_HANDOFF until that separate handoff | implementation remediation NOT_STARTED 0/3; no takeover.
