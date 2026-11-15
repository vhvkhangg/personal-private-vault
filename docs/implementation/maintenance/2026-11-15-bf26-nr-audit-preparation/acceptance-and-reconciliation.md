# Preparation acceptance and future comprehensive audit exit gates

## A. Finite preparation acceptance criteria (before Owner publication)

Independent preparation reviewer shall evaluate **each** criterion with `PASS / FINDING / NOT_VERIFIED / N/A`; no blanket approval based on writing quality. These criteria judge the **package**, not the actual Backend quality.

| ID | Criterion | Required proof in package |
|---|---|---|
| NRP-A01 | Exact starting remote HEAD and local verification limitation | README + baseline receipt; no false local CLEAN claim |
| NRP-A02 | Correct boundary `OWNER_DIRECTED_FOR_PREPARATION_ONLY` | Owner contract and all documents do not authorize audit implicitly |
| NRP-A03 | Full harness `.agents/.codex/AGENTS` incl adjacent scripts, `delivery/` discovery and effective CLI | H01–H13 + H0/H1 plan |
| NRP-A04 | Full docs with required six directories **plus remaining docs** | D01–D14 and D0/D1 coverage tiers |
| NRP-A05 | All current Non-RAG Backend modules/boundaries | B01–B20, inventory on actual future HEAD |
| NRP-A06 | Ordered H → D → B → X with intermediate validation | Codex workflow stages |
| NRP-A07 | Codex-only: independent author/reviewer sessions | Explicit session roles, no Antigravity in execution |
| NRP-A08 | No new audit skill; **Owner-approved supersession removes runnable Phase 15 mode** while preserving all historical records | Existing skill H1 rewrite plan, 2026-11-01 conflict explicitly overridden, legacy invocation STOP |
| NRP-A09 | Avoid circular bootstrap reliance on unaudited skill | H0 direct read-only Owner contract before H1 skill edits |
| NRP-A10 | Prepared harness scenarios including V1 handoff discovery and legacy Phase 15 STOP | H-T01–20 matrix |
| NRP-A11 | Current GPT-6.1 Sol availability and hook behavior **not assumed** | Version/trust evidence gate, conditional language |
| NRP-A12 | Simplicity criteria allow KEEP/reuse; no compulsory refactor | Owner contract + D/B review plans |
| NRP-A13 | PUBLIC no NSFW/Personal/People leakage; eight GETs; Film POST deny | Product/privacy contracts and negative test mapping |
| NRP-A14 | List DTO name proposal differentiated from V1 frozen identity-only response | Owner contract + D12 and B-API |
| NRP-A15 | Current H1-F/FR-13, RAG suspension, providers and S5 docs-only status preserved | Baseline + owner contract |
| NRP-A16 | Findings all severities, legitimate non-defect dispositions, no auto LOW waiver | Findings contract |
| NRP-A17 | Exact frozen/ADR/API/schema Owner decision gates | Owner docket + workflow |
| NRP-A18 | Independent acceptance, bounded remediation, no agent git publication | Session matrix + findings/closeout |
| NRP-A19 | Future per-path evidence/coverage completeness with honest NOT_VERIFIED | Inventory and audit matrix |
| NRP-A20 | Original decision-correction ZIP changed only previously applied preparation Markdown (11/14), and this PH-01/02/03 overlay changes only already-present preparation Markdown | Independently reproduce original/correction/Owner-snapshot archive comparison in **§A.1** and check the new overlay file list against local repository; no `.agents`, `.codex`, root AGENTS, source, governance or historical edits |
| NRP-A21 | Relative links/filenames correct; overwritten preparation paths are **intentional** and match the previously applied draft | The external provenance evidence and independent local byte comparison in **§A.1**, plus fresh relative-link/path inspection; no `git diff`-only shortcut for untracked files |
| NRP-A22 | Reviewer prompt challenges assumptions and supports Claude independently | Independent review brief, adversarial prompt |
| NRP-A23 | Review `delivery/` reality, avoid invented auto-ZIP behavior, plan **coherent** skill/script/docs changes under H1 | H13, H-T19/20, Owner docket OD-09/10 and FD-12, workflow packaging evidence and explicit edit gates |


### A.1. Historical correction provenance and independent reproduction (NRP-PH-02)

**Nature of evidence:** Author-supplied archival byte comparison, **not** an independent PASS or a Git working-tree receipt. The earlier `CHANGES_REQUESTED` reviewer could not see the two original archives; the author now supplies a separate evidence bundle (`personal-private-vault-bf26-nr-ph02-evidence-20261115.zip`). The bundle is an **external reviewer input**, not an extra repository file and not a new audit result. If the reviewer does not receive/read the bundle, A20/A21 remain `NOT_VERIFIED`.

| Actual artifact observed this turn | SHA-256 (full archive) | Package facts |
|---|---|---|
| Original `personal-private-vault-bf26-nr-audit-preparation-docs-only-20261115.zip` | `4e641650a10b8d48abd1e33603a4645c72f7af7b83cdeb11252c10cd13c267a1` | 14 preparation Markdown files |
| Supersession correction `personal-private-vault-bf26-nr-preparation-owner-decision-correction-20261115.zip` | `969d37a365d64cb4e72738d92c27b52b59272c69c9dd5b91b49a8e2bcd672aaa` | 11 preparation Markdown paths, all from original 14; all 11 bytes changed |
| Owner-uploaded repository snapshot **before** correction `personal-private-vault(20261115-131515).zip` | `7c8141efdcab7941e9b294e75bad7fcc9e67c8a1872657810c5ac5eb4847f7a2` | Its 14 preparation Markdown files match the original package byte-for-byte |
| Owner-uploaded repository snapshot **after** correction `personal-private-vault(20261115-135820).zip` | `e3cfc59920a3dc7aa5ad3c8db2f8a46482c655a955c1a6b229dd966e25c8bd55` | Its 11 replaced files match correction ZIP byte-for-byte; other three remain identical to original |

**11 overwritten files (actual archive paths):** `README.md`, `acceptance-and-reconciliation.md`, `audit-coverage-matrix.md`, `baseline-and-scope-inventory.md`, `codex-only-workflow.md`, `external-reference-register.md`, `harness-audit-plan.md`, `independent-preparation-review-brief.md`, `owner-contract.md`, `owner-decision-docket.md`, `reviewer-prompt.md`. **Three untouched files:** `backend-audit-plan.md`, `docs-design-audit-plan.md`, `findings-and-remediation-contract.md`. All are nested under this preparation directory; no other tracked or source paths are in either preparation ZIP.

**How to reproduce:** Download/extract the separately supplied evidence bundle; read its `README-PROVENANCE.md` and `manifest.json`. Run `python verify_provenance.py` inside the extracted bundle to validate archive SHA-256, all 14 original-vs-before files, 11 replacement-vs-after files, and three unchanged-after files. To check the newly applied PH corrective overlay against actual local files, run `python verify_provenance.py --repo-root <path-to-PPV-root> --overlay <path-to-PH-correction-zip>`. The second command should be run **after** applying the corrective overlay. Without the two command-line arguments, the verifier checks **only historical evidence artifacts**, not local files. The reviewer must independently confirm fresh `git status`, file names, file hashes and that there were no unrelated edits.

**Limit:** The two uploaded repository ZIPs evidence only their included bytes, not local Git index status, owner filesystem state at another time, or whether a historical upload mechanism safely handled overwrites. This correction provides reproducible evidence of the **archive content and intended overwrite set**, not proof of a particular extraction operation. The NRP-A20/A21 verdict remains the independent reviewer's to determine.

**Readiness state for preparation:** if all required criteria are demonstrated and no scope-blocking errors remain, independent reviewer may return `READY_FOR_OWNER_COMMIT / DOCS_ONLY_PREPARATION`. It is **not** `READY_FOR_HANDOFF`, `READY_FOR_AUDIT`, `NON_RAG_BACKEND_AUDIT_READY` or `OWNER_PUBLISHED`. Owner only then commits/pushes. A failed criterion returns `CHANGES_REQUESTED` with stable ID, expected correction and evidence.

## B. Completion gates for each future phase

| Gate | Required observable condition | No-go |
|---|---|---|
| G0 — Prepared | Independent PREP review accepted; Owner published actual SHA; current links reconciled | No execution from package draft alone |
| G1 — Harness assessed | Separately authorized H0, complete path manifest, scenario evidence, independent review | Do not edit unaudited controls in H0 |
| G2 — Harness qualified | Exact H1 rewrite and related skill/rule/workflow/delivery updates approved/implemented, tests independently accepted and Owner published | Old Phase 15 execution explicitly disabled; archival Phase 15 evidence unchanged; no unchecked `danger-full-access` assumptions |
| G3 — Docs assessed | All docs paths dispositioned; source-dependent claims provisional; owner design docket | Do not label docs authoritative against unreviewed code |
| G4 — Docs remediation | Bounded D1 accepted/published; immutable history preserved | No silent contract or historical rewrite |
| G5 — Backend assessed | All current non-RAG modules mapped, required runtime/contract/security/db evidence or explicit gaps | Full-suite/live/deployment claims from mocks prohibited |
| G6 — Source remediation | All chosen batches reviewed independently and Owner published; no open actionable finding | No unapproved API/DB/frozen modifications |
| G7 — Final cross-layer | H/D/B findings ledger, runtime, documentation and actual SHA reconciled; evidence not double counted | No unresolved mandatory NOT_VERIFIED or unadjudicated actionable LOW/INFO |
| G8 — Audit closeout | Independent Codex X review accepts report; Owner publishes actual SHA; `ACTIVE` correct | Does **not** auto-authorize Frontend, providers, S19-2 or deployment |

## C. Cross-layer reconciliation method

Build a matrix whose rows are **user-visible behaviors** and essential developer/ops guarantees, whose columns include: `Owner decision`, `current ADR`, `diagram`, `DBML/migration`, `service/domain`, `web API/DTO/OpenAPI`, `security filter`, `integration & negative tests`, `session/harness policy`, `runtime environment`, `actual SHA`, `status`, `finding ids`. A missing cell is `NOT_VERIFIED` or `N/A with reason`, never assumed aligned.

For every design simplification considered in D0, repeat the decision after B0 with actual source/test evidence:

1. State incumbent mechanism and required guarantee.
2. Propose smallest alternative and which components would disappear/change.
3. Record **benefit** (fewer requests/queries/files/states, observed runtime, maintenance cost); distinguish measured vs estimated.
4. Record **new risk** (privacy, latency, migration, regressions, concurrency, rollback).
5. Require Owner ratification before changing accepted/frozen contracts. An architecture improvement is a choice, not inherently an actionable bug.

For all end-to-end behavior claims, trace a concrete positive and negative case; do not equate direct route denial with overall absence of cross-feature leakage. No live provider or real Owner-data suitability conclusion without corresponding test.

## D. Auditor quality guardrails

- **Evidence over prose:** paths/lines, SHA, actual command, observed result and limit.
- **Independent verification:** separate Codex session reviews source and fixture, not just producer's narrative. Claude can challenge the package/report; Claude agreement alone is not test evidence.
- **No arbitrary quality quotas:** no global 100% test coverage/strict zero warnings mandate absent a demonstrated risk. Full *audit path accounting* is still required.
- **No secret disclosure:** do not export owner secrets, real token, private production files or session credentials into logs or reports.
- **No fabricated audit progress:** this preparation is not H0/D0/B0; no test passed and no finding resolved by package generation.

## E. Owner next-step handback

After approved preparation is published and an **exact new Owner audit-execution authorization** names H0 allowed read-only operations and output paths, Codex H0 may start. Otherwise STOP at preparation closeout. The first audit acceptance is not a grant to proceed to source remediation or Frontend.
