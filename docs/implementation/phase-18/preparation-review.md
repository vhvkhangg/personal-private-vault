# Phase 18 — Pre-Handoff Preparation Review

**Status:** `READY FOR HANDOFF` after independent re-review 2. **Baseline:** `86b08a1a44d7672b111af1977098ef6c4c3e800a` (owner-pushed HEAD, 2026-10-17). **Preparation author:** ChatGPT; **independent reviewer:** Codex. **No Phase 18 implementation, ML benchmark or live-file activation has run.**

## Latest independent result — re-review 2, 2026-10-17

[Codex re-review 2](reviews/2026-10-17-phase-18-pre-handoff-codex-re-review-2.md): **READY FOR HANDOFF**. **P18-PH-01–04 are CLOSED for preparation; no blocking finding remains.** ADR-0021 clause 2, the Phase 18 API inventory and additive DBML now share the frozen Phase 17-compatible source-first order, including the external-file guard slot, separate attempt-only stop request and NOWAIT backstop. Owner-approved D18-A scope is preserved.

Fresh documentation/source-order, whitespace/local-target/TOML and P18-FINAL PowerShell syntax checks pass. Prior unchanged native Windows watchdog **5/5** and safety hooks **13/13** evidence is explicitly reused, not rerun. Actual R7, PostgreSQL/content/vector/finalization, parser/model/held-out quality, RAM/VRAM and P18-FINAL execution remain **NOT_VERIFIED mandatory implementation gates**. This is preparation acceptance only; ACTIVE remains `NO_ACTIVE_HANDOFF`. No Antigravity implementation remediation round has occurred.

## Re-review 1 result — 2026-10-17 (historical)

[Codex re-review 1](reviews/2026-10-17-phase-18-pre-handoff-codex-re-review-1.md): **CHANGES_REQUESTED**, with only **P18-PH-01 (High) OPEN**. ADR-0021 clause 2 places RAG policy/attempt locks before Vault source guards, contradicting the frozen and implemented source-first order. Correct the exact mixed guard order in preparation; do not defer reconciliation to implementation. The report supplies a finite compatible sequence within the approved D18-A preservation intent.

**P18-PH-02, P18-PH-03 and P18-PH-04 are CLOSED for preparation.** Held-out quality/locator rules, P18-FINAL and scoped operating status are remediated. Runtime R7, PostgreSQL, ML/resource and final-suite evidence remain mandatory future gates. Owner approval of all eight original D18-A clauses is recognized; no repeat broad scope approval is requested.

Fresh checks: `git diff --check`, changed/new Markdown local file targets, TOML parsing and P18-FINAL PowerShell syntax PASS; production/build/lock/migration/ACTIVE unchanged. Previous unchanged native Windows watchdog **5/5** and safety hooks **13/13** results are explicitly reused, not rerun or relabeled. No implementation remediation round is consumed.

## Initial independent review result — 2026-10-17 (historical)

[Codex pre-handoff review](reviews/2026-10-17-phase-18-pre-handoff-codex-review.md): **CHANGES_REQUESTED**. Four blocking preparation findings:

- **P18-PH-01 (High):** record and obtain owner approval of the exact additive multi-root/file-authority/content-staging/per-generation-lifetime contract; the report supplies finite proposal D18-A. Frozen Phase 17 is unchanged.
- **P18-PH-02 (Medium):** carry forward P17-N1 source-grouped held-out parser/OCR evaluation and distinguish mandatory provenance failure from optional locator warnings.
- **P18-PH-03 (Medium):** name and specify the single P18-FINAL gate and actual existing terminal/report status mapping.
- **P18-PH-04 (Medium):** synchronize applicable backend/worker operating instructions with the frozen Phase 17 baseline and separate Phase 18 handoff route.

Codex independently ran the changed watchdog tests **5/5 PASS on native Windows / CPython 3.14.4**, including owned parent/child/grandchild termination and unrelated sentinel survival; safety hooks **13/13 PASS**. The report records exact commands, hashes, receipts, the complete 29-dimension coverage ledger and KEEP/SIMPLIFY/DEFER/REJECT/OWNER DECISION matrix. This preparation review does not consume an Antigravity remediation round. `ACTIVE.md` remains `NO_ACTIVE_HANDOFF`.

## ChatGPT remediation candidate after owner approved D18-A

The owner **explicitly approved all eight clauses of D18-A** after the initial independent review and supplied `personal-private-vault(9).zip` as the remediation candidate. This is a **design contract** authorization, not implementation/release authorization. Re-review 1 closed P18-PH-02/03/04; the subsequent source-first correction and re-review 2 close P18-PH-01. All four are now CLOSED for preparation. Earlier reports remain preserved unchanged.

| Finding | Current proposed remediation / exact review target |
| --- | --- |
| P18-PH-01 High | [ADR-0021](../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md), separate [Phase 18 DBML](../../database/phase-18-document-indexing.dbml) and [typed API/worker inventory](../../architecture/rag-phase-18-api-extension.md): multi-root union reservation, RAG-owned external-file authority, R7 and mixed guard order, document staging, per-file CURRENT/purge, one physical Java finalization, version/limits/old API compatibility. |
| P18-PH-02 Medium | [P17-N1 held-out split and locator oracle](held-out-quality-and-locators.md) with source-group disjointness, human labels, per-format/language denominators, fail-vs-warning rules and measured defaults before acceptance. |
| P18-PH-03 Medium | [P18-FINAL](final-verification-gate.md) exact pinned Windows/PowerShell Python + Maven bounded commands, required focused receipt set, truthful existing job-status mapping and strict failure policy. |
| P18-PH-04 Medium | **Operating docs** `backend/AGENTS.md` and `rag-engine/README.md` synchronized to frozen Phase 17 / Phase 18 preparation only; no legacy production changes. Codex must compare scoped/root contracts and verify this status has no contradiction. |

The uploaded source ZIP contains `docs/`, scripts and agent tooling but **omits** `backend/` and `rag-engine/`; these two scoped operating files were retrieved from **exact pinned baseline HEAD** for this patch. Reviewer must verify them against the actual current checkout and report any local divergence rather than silently replacing newer content.

## Preconditions and authority

- Phase 17 Alternative B/E1 is [COMPLETE — FROZEN](../phase-17/closeout.md) under [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md); predecessor gate satisfied. Frontend Phase 16 remains deferred, `AUTH-CLOCK` remains an independent open Authentication issue, and [ACTIVE](../handoffs/ACTIVE.md) remains `NO_ACTIVE_HANDOFF`.
- Owner explicitly approved [P18-01–P18-10](owner-decisions.md) and AC-01–AC-08 *as design/preparation intent*. Implementation remains blocked until this review passes, owner commits/pushes accepted preparation and Codex separately creates an implementation handoff.
- New scope is contained in [Phase 18 README](README.md), [owner decisions](owner-decisions.md) and [acceptance evidence](acceptance-and-evidence.md). Historical/frozen Phase 17 artifacts are evidence, **not** editable remediation targets.

## Exact review targets

1. New/modified Phase 18 `README.md`, `owner-decisions.md`, `acceptance-and-evidence.md`, `preparation-review.md`, `held-out-quality-and-locators.md`, `final-verification-gate.md` and `reviews/README.md`; [ADR-0021](../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md), [additive DBML](../../database/phase-18-document-indexing.dbml), [typed API/worker inventory](../../architecture/rag-phase-18-api-extension.md), canonical architecture/database/ADR indexes and narrow scoped `backend/AGENTS.md`, `rag-engine/README.md` status-only synchronization.
2. Accepted source/privacy contracts: [P17 source/allowlists](../phase-17/source-eligibility-and-allowlists.md), [P17 revocation, reservations, E1 and finalization](../phase-17/revocation-and-derived-data.md), [P17 integration/worker role](../phase-17/integration-ownership-and-permissions.md), [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md), [RAG architecture](../../architecture/rag-architecture.md), [ADR-0019](../../adr/0019-local-rag-before-frontend.md), [RAG phase plan](../rag-phase-plan.md), [ADR-0013](../../adr/0013-stable-compatible-version-policy.md).
3. Relevant implemented Phase 17 `rag` Java/Python source, migrations, test and exact runtime/lock versions; [root/scoped AGENTS](../../../AGENTS.md), `.agents/agents/`, `.agents/skills/`, `.agents/rules/`, `.agents/hooks/`, `.codex/`, `scripts/`; [proportionality/escalation policy](../../workflow/rag-phase-delivery-guardrails.md). Inventory **all newly modified** items and **all applicable existing** items with read path and N/A justification; do not silently omit applicable roles or rules.
4. The tool-governance gap fixes in this preparation: Python/RAG/bounded command skills are explicitly selected by relevant implementation handoff/agent paths; watchdog parent/child/grandchild regression tests were independently verified 5/5 PASS on native Windows by Codex (see original report); Codex verbosity/full-access config is verified as existing and not needlessly overwritten.

## Mandatory focused Codex findings / decisions

- **Approved D18-A exact contract (PREPARATION ACCEPTED; RUNTIME PROOF REQUIRED):** P18-03/P18-04 expands file-level outcomes while retaining one final physical COMMIT, whole source-set reservation, E1, SQL fences and positive worker stop. The owner-approved [ADR-0021](../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md), [logical DBML](../../database/phase-18-document-indexing.dbml) and [API/worker delta](../../architecture/rag-phase-18-api-extension.md) passed preparation review against actual Phase 17 boundaries. Carry their exact constraints into the eventual handoff and prove implementation behavior. Any necessary *broader* exception returns to the owner; no implicit change. Never amend historical ADR-0020 silently, publish early or grant worker publication authority.
- **R7 live external-file gate:** Phase 17 leaves live-file ingestion disabled. Determine actual NTFS handle/file identity, source changed/revoke-to-COMMIT proof, unsupported paths and fallback exclusion; do not classify an untested live path as safe.
- **RAM + VRAM feasibility:** on the owner Windows 11 / 16 GB system RAM / 8 GB VRAM target, distinguish peak system process-tree RAM from GPU VRAM, bounded temp disk and OCR/model load; choose actual finite limits *after* a small representative benchmark. Do not invent universal thresholds or claim runtime proof from documents.
- **Functional completeness:** each approved parser/format and Obsidian semantics; PDF table boxes/read order and multilingual OCR; structure-first chunker; one model vector space; per-file fail-safe, CPU fallback and report; all eight AC groups without importing Phase 19 retrieval/chat/judge or Phase 22 dashboards.
- **Overengineering:** issue concise `KEEP / SIMPLIFY / DEFER / REJECT / OWNER DECISION REQUIRED` dispositions for major parser/OCR/model/integration obligations. Treat rubric items as review *questions*, not demanded libraries or fixed benchmark counts. Reject new parser plugin platforms, duplicate orchestration/locks, distributed services, multiple production models and repeated unaltered Phase 17 suites.

## Expected output and gate

Produce a dated, immutable Codex report under `reviews/` with paths/line evidence, risks categorized by severity and whether genuinely blocking, list of files/skills/rules/hooks reviewed (including N/A), concrete smallest repairs, open owner decisions, and exact status `CHANGES_REQUESTED` or `READY FOR HANDOFF`. Then update **this** review status accordingly. No handoff is created in this step.

**Evidence status at preparation:** ChatGPT supplied Linux watchdog **5/5 PASS**. Codex independently confirmed native Windows watchdog **5/5 PASS**, safety hooks **13/13 PASS**, remote `main` matching the baseline SHA, TOML syntax, changed Markdown local file targets and `git diff --check`; see the report for exact limits. Existing `.codex/config.toml` was inspected and preserved. `py -3` is unavailable on this host's PATH; the existing native `rag-engine/.venv/Scripts/python.exe` (3.14.4) ran verification successfully. **NOT VERIFIED:** Phase 18 runtime code/tests, OCR+embedding feasibility on owner hardware, live-file R7, PostgreSQL subset-finalization and real RAM/VRAM peaks, final full/static gate and IDE diagnostics. These remain *future handoff/implementation* evidence obligations. The successful watchdog fixture does not certify universal process-tree cleanup or the RAG worker's new runtime behavior.

**Next step:** Owner commits/pushes the accepted preparation using the single commit message in [re-review 2](reviews/2026-10-17-phase-18-pre-handoff-codex-re-review-2.md), then invokes `$codex-create-handoff` separately. This review does not create the implementation handoff or start implementation.
