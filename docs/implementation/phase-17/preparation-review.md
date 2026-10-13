# Phase 17 Pre-Handoff Preparation Review

**Status:** `READY FOR HANDOFF` — **Phase 17 foundation preparation only** (2026-10-13). The [focused P17-3 and complete coverage review](reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md) closes P17-1/2/3 at the design-contract level. All 29 dimensions are recorded; runtime evidence remains mandatory before implementation acceptance. Prior rejections remain historical. No implementation handoff exists.

## Preconditions to verify

- Completed/frozen backend Phases 0–15 with Phase 15 `BACKEND_AUDIT_READY` and owner-pushed commit `0b1ab11`.
- Owner explicitly instructed RAG-first, while Phase 16 Frontend stays deferred. Phase identifiers remain 17/16 (proposed ADR-0019 exception).
- `docs/implementation/handoffs/ACTIVE.md` status `NO_ACTIVE_HANDOFF`.
- Proposal changes preparation order only; no new committed Java module, migration, deployment or Python runtime yet.

## P17-1/2/3 remediation targets for next Codex review

Codex must specifically verify:

- [P17-1](integration-ownership-and-permissions.md): proposed `rag` module dependency matrix delta, exact initial sources, named-interface design, distinct migration/app/worker DB credentials, worker negative-grant tests, and precise later frozen-source synchronization/owner acceptance gate.
- [P17-2](source-eligibility-and-allowlists.md): independently authoritative owner+domain/file proofs, explicit Fiction/Film fields, denied unreviewed domains, external per-file approval and non-private Obsidian embed provenance; verify using existing `FictionView`, `FilmView`, `VaultEntryView` contracts rather than assumed generic Private column.
- [P17-3](revocation-and-derived-data.md): source authorization epoch/revision fences, **the common owning Vault row guard held from fresh proof until Java commit and the guarded Windows/NTFS file-handle proof**, deterministic after-last-read races, serialized revocation/publish, purge of all derived content and transitive embed dependencies, queued judges/history, actual interrupted-worker scenarios.
- Scope split validity and Phase 17 foundational-only handoff, future phases 18–22 separately gated. Cross-document correctness, **workflow performance/efficiency/reliability and SOLID/DRY/KISS/YAGNI**: identify redundant stages, excessive coordination or unnecessary infrastructure, hard-to-test coupling, publication/retrieval bottlenecks and missing recovery paths. Recommend concrete changes and reject optimizations that weaken privacy/grounding. This additional optimization review is explicitly requested by owner for the *next* run.

The first 2026-10-13 review is preserved as historical rejection; the latest Codex disposition is recorded below and in the linked re-review. Preparation remediation does not itself close findings or authorize implementation. No production code, module/migration change or handoff was performed for remediation.

## Review targets

- `docs/implementation/phase-17/README.md`
- `docs/implementation/phase-17/preparation-review.md`
- `docs/implementation/phase-17/benchmarks-and-acceptance.md`
- `docs/architecture/rag-architecture.md`
- `docs/adr/0019-local-rag-before-frontend.md`
- Phase/order synchronized `docs/roadmap.md`, `AGENTS.md`, `docs/workflow/owner-phase-workflow.md`, `.agents/skills/codex-pre-handoff-review/SKILL.md`, `.agents/skills/local-rag-engineering/SKILL.md`, `.agents/README.md`, indexes/deferred/active-hand-off status documents.

## Shared review coverage

Use [`codex-review-coverage`](../../../.agents/skills/codex-review-coverage/SKILL.md) for all 29 applicable review dimensions, documenting evidence and any N/A/NOT_VERIFIED items. The next review must inspect code/package structure in the relevant existing module boundaries and testable workflow efficiency, **without** rewriting ADR-0016 shared root HTTP types by assumption. Missing runtime tests are mandatory future gates, not preparation PASS evidence.

## Codex review checklist

1. Validate that the explicit Phase17-before-Phase16 exception is narrow, ordered, enforceable, and not a workaround for a frozen gate.
2. ADR-0019 decision scope vs accepted ADR-0014; confirm only RAG sequencing changes and no false implementation claim.
3. Verify no frozen database, Java module graph, `search` internals, portability JDBC exception or API semantics are altered in preparation; any proposed future module edge is subject to approval.
4. Check backend source projection ownership and field-level exclusion for Private/NSFW, finance/personal/auth payloads, trash, revoked and unknown scope; test non-leak through embeddings/metadata/counts/judges.
5. Confirm persistent and session-only file selection paths, symlink/zip-slip, Obsidian transclusion cycles, XLSX formula safety, PDF OCR, multilingual table/locator fidelity and no write-back.
6. Confirm service auth/loopback/offline-network policy, typed schema/timeouts, idempotency, retries, cancellation, reconciliation and atomically published index generations.
7. Verify hybrid retrieval + strict citation/answer abstention, recommendation catalog ID/constraint validation, typed SQL-free exact read APIs, bounded (non-agentic) tool routing.
8. Assess per-run evaluation vs asynchronous local judge and golden test methodology; separate threshold proposals from measured acceptance results.
9. Confirm documented hardware limits, model/version licensing and pgvector/PostgreSQL image compatibility are explicitly benchmark gates, not ungrounded support claims.
10. Assess SOLID/DRY/KISS/YAGNI and genuine Strategy/Adapter/Facade boundaries; reject speculative interfaces, service hierarchies, new agents/hooks or extra infrastructure without evidence.
11. Check docs cross-links, frozen documentation scope, no Ideaverse-specific implementation before owner permission, and no Frontend work.
12. Evaluate whether the staged source/project inventory requires a maintenance update; avoid frozen repository tree edits unless explicitly authorized.

## Verification evidence supplied by preparation author

- Package inspection: current uploaded archive `personal-private-vault(2).zip`; 19 implemented top-level Java modules, existing authentication/search/import/portability/contracts preserved.
- Documentation-only preparation: no code/tests/dependencies/migrations/diagram export changed. Perform diff/link/ZIP inventory checks on the preparation package.
- No model/OCR benchmark, network-isolation demonstration, Maven/Python regression, live GPU benchmark, or IDE inspection was executed. Do not claim such results.

## Expected review outcomes

- `CHANGES_REQUESTED`: Codex writes findings under `reviews/`, requires remediation, no handoff.
- `READY FOR HANDOFF`: Codex records acceptance under `reviews/` and updates this file, provides one Conventional Commit message. Owner commits/pushes, **then** invokes `$codex-create-handoff`.

## Historical Codex result — first review, 2026-10-13

Baseline: `main`, HEAD/local `origin/main` = `cbd5963086f41658ffe421695b546f5b1598a161`; Phase 15 remediation is an ancestor and the frozen predecessor gate passes. The submitted preparation changes Markdown only; frozen sources and implementation remain unchanged.

| Finding | Severity | Required preparation closure |
| --- | --- | --- |
| P17-1 | Medium | Bound the proposed architecture/module/storage delta, ownership and database permissions before handoff. |
| P17-2 | High | Define authoritative source classification and initial field/source allowlists, including external files and embeds. |
| P17-3 | High | Define revocation/version fences and invalidation for all derived artifacts and late workers; add race/failure acceptance scenarios. |

Full evidence, accepted design choices, IDE diagnostic triage and limitations are in the formal report. Model/runtime benchmarks remain later implementation gates; they were not executed or claimed passing here.

**Next step:** Give the findings and latest package to ChatGPT for preparation-only remediation, then rerun `$codex-pre-handoff-review`. Keep `ACTIVE.md = NO_ACTIVE_HANDOFF`; do not create a handoff or start Frontend.

## Historical Codex result — first re-review, 2026-10-13

Baseline remains `main`, HEAD/local `origin/main` = `cbd5963086f41658ffe421695b546f5b1598a161`; the Phase 15 prerequisite passes and frozen implementation/architecture artifacts remain unchanged.

| Finding | Current status | Disposition |
| --- | --- | --- |
| P17-1 / Medium | CLOSED — preparation | Bounded module/storage ownership, role isolation and later approval/synchronization gate now documented. Actual runtime enforcement still needs implementation evidence. |
| P17-2 / High | CLOSED — preparation | Two-authority classification, initial field matrix, file/embed approval and denial fixtures now documented. No source allowlist is implemented or active. |
| P17-3 / High | OPEN — partially resolved | Retention/dependency/session/worker rules are improved; define the owning-domain and file guard through final commit, with after-last-check interleavings. |

The [full re-review](reviews/2026-10-13-phase-17-pre-handoff-codex-re-review.md) also records the critical workflow assessment and eight concrete improvements covering scoped retrieval, immutable generation/fingerprints, batching/reuse, resource budgets/scheduling, parser/model efficiency, held-out evaluation, offline startup and simple recovery. These do not authorize extra scope or waive the remaining blocker. No RAG runtime/ML/role/egress result is claimed passing.

**Next step:** Give this re-review and the latest package to ChatGPT for focused P17-3 preparation remediation and applicable workflow refinements, then rerun `$codex-pre-handoff-review`. Keep `ACTIVE.md = NO_ACTIVE_HANDOFF`; do not create a handoff or start Phase 16.

**Historical preparation-author remediation note:** Owner requested multi-phase planning and thorough review coverage. P17-3 §1A proposed source-owner locks through COMMIT, guarded NTFS file lifetime and post-last-check barriers for this new Codex review. See [separate RAG phases 17–22](../rag-phase-plan.md).

## Current Codex result — focused P17-3 and complete coverage, 2026-10-13

**READY FOR HANDOFF — Phase 17 foundation only.** P17-1/2/3 are CLOSED at the design-contract level. The [formal review](reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md) records all 29 dimensions, closure evidence, implementation test obligations and concrete optimization decisions. No unresolved High/Critical preparation finding remains.

P17-N1 (Medium, nonblocking for foundation) requires independent held-out/calibrated evaluation at the first affected later capability's preparation gate. P17-N2 (Low) recommends explicitly historical labels on general Phase 15 workflow summaries. Neither waives privacy or implementation evidence.

The immediate handoff must be limited to source authority/guards, minimal worker/storage/job lifecycle and offline foundation. Use controlled synthetic stage outputs to test future-facing fences; actual OCR, QA, recommendations and source expansion require separate P-18 through P-22 gates. Real PostgreSQL/worker-role/Windows concurrency, purge and offline evidence must pass before accepting Phase 17 implementation.

**Next step:** Owner acceptance of the bounded integration delta is now recorded in [owner decisions](owner-decisions.md). Owner commits/pushes preparation using `docs(rag): prepare phase 17 foundation and phased delivery gates`, then invokes `$codex-create-handoff` for Phase 17 foundation only. `ACTIVE.md` remains `NO_ACTIVE_HANDOFF` until that separate action. Phase 16 stays deferred.
