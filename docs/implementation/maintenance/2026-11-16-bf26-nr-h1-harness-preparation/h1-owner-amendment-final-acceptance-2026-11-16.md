# BF26-NR-H1 — Owner amendment: final bounded source-and-test acceptance

- **Amendment ID:** `BF26-NR-H1-FINAL-ACCEPTANCE`
- **Owner decision date:** `2026-11-16` (UTC+07)
- **Status:** `OWNER_RATIFIED_2026-11-16 / PENDING_INDEPENDENT_DELTA_REVIEW_AND_PUBLICATION`
- **Parent authorization:** `BF26-NR-H1-HARNESS-REMEDIATION`, published at `802c31e715566272ef640c487942558717a54d34`
- **Published C1 amendment:** `044ae369738178359043655993a47dc18ff4946b`
- **Approved H1 acceptance contract:** `SOURCE_AND_TEST_VALIDATED / RUNTIME_LIMITATIONS_ACCEPTED`
- **This amendment's publication / actual published SHA:** `PENDING_OWNER_PUBLICATION / NOT_ASSIGNED`

## 1. Approved Owner decision and effectiveness

The repository Owner explicitly approves completing the H1 repository-harness remediation assessment through source-and-test acceptance, without comprehensive certification of the external Codex runtime environment. The decision source is the Owner's explicit instruction for this document in the final bounded acceptance amendment author session. This records that decision for publication; it is not a proposal, a new preparation phase or a request to ratify the same decision again.

The Owner accepts a bounded evidentiary limitation. Historical/current global-config hash stability, config-drift forensics, startup-traffic qualification and new native H-T07/H-T09 replay cease to be mandatory H1 acceptance gates under this limited objective. Their unverified properties remain unverified; acceptance does not establish that unknown security properties are safe.

**This amendment becomes effective only after an acceptable independent docs-only delta review and Owner commit/push with actual published-SHA verification.** Until then, the published parent/C1 contract and the completed C1 review's blocked H1 result retain their existing effect. This author cannot supply independent acceptance or publication. No additional ratification of this already explicit Owner decision is required.

After effectiveness, this document controls only the acceptance changes expressly stated in §§3–5. The [parent ratification](h1-owner-ratification-2026-11-16.md), [published C1 record](h1-owner-amendment-c1-2026-11-16.md), [17-path allowlist §A](h1-exact-path-allowlist.md#a-proposed-17-existing-file-write-allowlist), [author handoff](h1-codex-author-handoff-draft.md) and [acceptance rubric](h1-preflight-and-acceptance.md) retain every other restriction. Earlier contracts, reports and reviews are preserved as dated evidence, not rewritten into PASS.

## 2. Evidence basis and read-only repository qualification

The parent ratification, C1 amendment, allowlist, acceptance rubric, author handoff and all three author reports including their C1 addenda were read. The original independent H1 review and completed independent C1 re-review were read directly from their original local assistant outputs. They are chat-only records; this amendment summarizes their dispositions without manufacturing a repository review file.

| Original independent record | Session / final assistant event (UTC) | Text integrity | Historical outcome |
| --- | --- | --- | --- |
| H1 implementation review | `01a1e308-66a4-78d2-9439-fc1f7c287016` / `2026-11-16T06:44:12.103Z` | 14,115 characters; SHA-256 `5CB8FB256007FF5F6FCEE33078B01C8C3A00074F899D332ABE672F34D939097B` | `H1_CHANGES_REQUESTED`; FR01/FR02 OPEN |
| Completed C1 re-review | `01a1e469-103f-7dd1-b7f9-df8976423378` / `2026-11-16T13:00:38.532Z` | 9,830 characters; SHA-256 `BFF2F6C485C0A09DA24E0445BFF332B2E57EC34420D56FB4F10DBD9E771F1BB5` | `H1_C1_BLOCKED / OWNER_DECISION_REQUIRED`; FR01 CLOSED, FR02 OPEN under the prior runtime contract |

Original transcript locators, with the installation prefix redacted:

~~~text
<CODEX_HOME>/sessions/2026/11/16/rollout-2026-11-16T13-25-41-01a1e308-66a4-78d2-9439-fc1f7c287016.jsonl
<CODEX_HOME>/sessions/2026/11/16/rollout-2026-11-16T19-50-53-01a1e469-103f-7dd1-b7f9-df8976423378.jsonl
~~~

The completed C1 reviewer independently verified NRH0-F01 within source/caller and sampled-routing scope, NRH0-F02's supported evaluator/adapter correction, and NRH0-F03 within narrow guidance scope. That reviewer verified 30 distinct hook unittest methods PASS, exact 17-path scope, report-prefix/addendum integrity and preserved receipts. It recorded **1/3 completed correction pairs** and **18/20 consumed CLI invocations**. Its runtime blockers were evidentiary requirements of the then-published contract; this amendment does not retrospectively change that review.

Read-only checks for this document confirmed branch `main`, `HEAD = cached origin/main = 044ae369738178359043655993a47dc18ff4946b`, 3,552 tracked paths, zero staged paths, exactly 17 modified tracked paths and exactly the three existing author reports untracked before this amendment. Local Git objects confirm the named parent/C1 content; the C1 commit changes only its amendment. Publication SHAs are Owner-supplied and locally corroborated; cached refs are not a new live-remote observation. No network request was made.

All 17 current source hashes match the [C1 source snapshot](h1-source-test-evidence-index.md#corrected-source-snapshot-and-original-record-preservation). In particular:

| Supported correction source | Current C1 SHA-256 |
| --- | --- |
| [.agents/hooks/repository_safety.py](../../../../.agents/hooks/repository_safety.py) | `904F11C9E645C4AC3BDFAC63C885C5678ACC920BA5350A73A7BE9B93D43433F8` |
| [.agents/hooks/test_repository_safety.py](../../../../.agents/hooks/test_repository_safety.py) | `CC8E1FE8ACFA46B7E8148A8DF0AD9DC985924087A8478AA117F3612AD105E8B9` |

The existing [final test receipt](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/tests-final.json) and [raw unittest output](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/tests-final.stderr.log) record exit 0, 30 named methods PASS, unittest 0.880s and bounded elapsed 1.516s. Names match the current source and [correction-integrity record](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/correction-integrity.json). These are existing, source-qualified results, not fresh test execution by this amendment author. The original 27-method snapshot and corrected 30-method snapshot are not additive.

All three full reports, original prefixes and appended-byte digests match the existing [append attestation](../../../../logs/bf26-nr/h1/20261116-c1-qualification-05f7a8219e/append-attestation.json):

| Preserved report | Original prefix bytes | Current whole-file bytes | Current whole-file SHA-256 |
| --- | ---: | ---: | --- |
| [h1-implementation-handback.md](h1-implementation-handback.md) | 13,973 | 23,856 | `C2279FA1FF4E225CDA418F8323B052D1A4D1C395F0A24900F5121C16EED750A1` |
| [h1-self-review.md](h1-self-review.md) | 9,675 | 20,722 | `1759CD11FC7B49AF587CFF071D87D2DC88AA836A39ABBAD17C7B8E532B4C3D38` |
| [h1-source-test-evidence-index.md](h1-source-test-evidence-index.md) | 18,010 | 32,869 | `BD6B438C3C537C1671BD2F3541808DB537E149F74C89AC6CAB5193A02535AAF9` |

The [latest STOP receipt](../../../../logs/bf26-nr/h1/20261116-c1-resume-stop-df9a656931/resume-checkpoint.json) preserves the later recorded drift and incomplete runtime qualification. Its 91 prior receipt entries rehash correctly: 54 initial + 23 correction + 14 qualification; with STOP itself, 92 H1 artifacts exist. These are artifacts, not test counts. STOP SHA-256 is `93806D8E4642B8E0BDB9E5169456222D703DBAA2D5092B6C8BC59E3898B6FCEA`. No global configuration was reread or rehashed for this task. Local ignored receipt links identify existing evidence, not files newly published by this amendment.

## 3. Terminal finding dispositions under the amended contract

### H1-FR01 — CLOSED

Preserve the completed independent C1 review's acceptance of the supported PowerShell quoting correction. Literal payloads remain data; supported executable deletion remains denied through the evaluator/adapters without permission overrides. No additional correction is required unless a new concrete defect is demonstrated. This closure does not certify actual native hook loading or pre-dispatch enforcement.

### H1-FR02 — ACCEPTED_LIMITATION / NOT_VERIFIED

The Owner accepts the absence of complete global-config provenance and current-runtime qualification **only for H1 source/test acceptance**. FR02 is not technically resolved or technically CLOSED. Its disposition changes from a blocking evidence requirement to an explicitly accepted limitation for this narrower objective, effective only through §1.

| Retained property | Required disposition |
| --- | --- |
| Historical configuration equivalence | `NOT_ESTABLISHED` |
| Historical delta, including the recorded later drift | `NOT_VERIFIED` |
| Root cause | `ROOT_CAUSE_NOT_DETERMINED` |
| Effective merged configuration | `NOT_VERIFIED` where not directly observed |
| Native hook loading and pre-dispatch enforcement | `NOT_VERIFIED` |
| Automatic skill discovery/loading and instruction precedence | `NOT_VERIFIED` |
| Serving-model identity | `NOT_VERIFIED` |
| Startup network traffic boundary | `NOT_VERIFIED` |
| Historical/current global-config stability as a general property | `NOT_VERIFIED`; no new stable-hash claim |
| New C1 H-T07/H-T09 native replay | `NOT_VERIFIED`; invocation 19/20 packets were not executed |

These limitations must never be reported as PASS. Selected configuration observations remain limited to their original cohorts. Historical equivalence and retrospective runtime safety must not be inferred from a current hash, matching selected fields, an unchanged binary, parser/adapter tests or supplied-source model responses.

This acceptance of missing evidence is not a waiver for an independently demonstrated unsafe action or permission to bypass security controls. No forbidden startup traffic is established by the recorded uncertainty; its absence is not established either.

## 4. Final bounded H1 acceptance standard

A different independent H1 reviewer may return **`H1_ACCEPTED_WITH_RUNTIME_LIMITATIONS / READY_FOR_OWNER_COMMIT`**, and describe the contract as **`SOURCE_AND_TEST_VALIDATED / RUNTIME_LIMITATIONS_ACCEPTED`**, only when all eight conditions hold:

1. All H1 source changes remain within the ratified [17-path allowlist §A](h1-exact-path-allowlist.md#a-proposed-17-existing-file-write-allowlist). This separate amendment is the sole new decision-file exception; it does not enlarge the source allowlist.
2. H1-FR01 remains independently CLOSED.
3. NRH0-F01–F03 meet the source, caller, guidance and supported evaluator/adapter requirements, preserving the independently verified scope and its limits.
4. The existing 30-method test evidence and relevant receipts are intact and source-qualified; reused evidence retains its actual dependencies, failures and counting units.
5. The three existing author reports and their C1 addenda preserve required integrity, including complete original byte prefixes and existing addendum/full-file digests.
6. No actionable in-scope source defects remain unresolved, including actionable LOW/INFO.
7. ACTIVE, historical evidence, ordinary workflows and excluded source files remain unchanged.
8. All runtime limitations are disclosed accurately, without unsupported PASS claims or promotion of sampled routing to comprehensive runtime certification.

Use existing evidence and safe read-only source, diff, link, receipt and integrity checks. Historical config recovery, a stable global-config hash, startup-traffic qualification and additional native H-T07/H-T09 replay are not required for this limited standard. Previously collected native-routing evidence supports sampled routing conclusions only; the historical 36 decisions are not new C1 replay results.

The [H1A-01–12 rubric](h1-preflight-and-acceptance.md#e-future-h1-review-h1a-01h1a-12) remains applicable within this narrowed standard: authority/scope, refusal/routing source behavior, ordinary workflow, evaluator/adapters, guidance, integrity, independence and truthful runtime disclosure remain reviewable. H1A-01/11 no longer block solely on the accepted FR02 evidence gaps; H1A-03/04 require no new replay; H1A-08/10 preserve every unknown and evidence cohort. Publication remains subsequent to acceptance under H1A-12. No unqualified runtime-protection or full G2 certification is granted.

If the reviewer discovers a **new demonstrated in-scope source defect or concrete unsafe behavior**, report it with exact evidence and the affected criterion. Do not waive a genuine defect, self-author a fix or infer a new correction allowance. Report the specific blocker for Owner disposition. Existing runtime unknowns alone do not require another amendment, investigation or qualification round.

## 5. Exact supersession and preserved decision precedence

The reason for supersession is the Owner's deliberate change of objective to repository source/test acceptance, with external runtime limitations retained. Only the following acceptance-blocking effects are displaced after §1's effectiveness gate:

| Earlier clause | Superseded effect for limited H1 acceptance | Preserved requirement |
| --- | --- | --- |
| C1-D02 / C1 §5 | FR02 must await new current-baseline qualification before an acceptable disposition | Historical delta/cause/equivalence remain unknown; no technical closure |
| C1-D03 / C1 §6.1 | Mandatory new effective-configuration and startup-traffic qualification | Actual security/communication restrictions, truthful disclosure and no configuration bypass |
| C1-D03 / C1 §6.2 | Global-config hash-stability checks, drift forensics/recovery and runtime requalification as H1 acceptance blockers, including the later recorded drift | Source/test reuse qualification, receipt integrity, failed evidence and separate cohorts |
| C1-D03 / C1 §6.3 | Mandatory new invocation 19 H-T07 and invocation 20 H-T09 packets, and successful new-runtime completion | Existing packets remain unexecuted candidates; old responses remain sampled evidence only |
| C1-D06 / C1 §9 steps 4–7, and §12.2–12.3's preservation of those gates | Dependency of H1 acceptance/publication on successful Author Resume runtime qualification, replay and a FR02 disposition resting on that process | Independent review, amendment-before-source publication, exact scope and Owner-only actual-SHA publication; §7 below supplies the final lifecycle |
| Parent H1-D07 / ratification §4; handoff §0 gate 6 and §2; earlier acceptance criteria and H1 policy/skill summaries repeating them | Mandatory comprehensive global-config stability/runtime qualification or new H-T07/H-T09 evidence as a barrier to this specific limited acceptance | Source/caller/evaluator/guidance requirements, security boundaries, actual unsafe-behavior STOP and accurate NOT_VERIFIED reporting |

C1-D04's accounting and ceiling remain unchanged. Its requests for another amendment when mandatory qualification cannot fit no longer apply merely because the displaced runtime properties remain NOT_VERIFIED. C1-D05's original-prefix/addendum integrity remains binding; this decision permits no further append or report rewrite. C1-D01's former pending-review status is historical; the completed independent C1 review supplies FR01 closure, preserved in §3.

The [current Owner policy](../../../workflow/current-owner-delivery-policy.md), audit-specific independence, all-actionable-source-findings rule and [Docs Sync contract](../../../workflow/documentation-synchronization.md) continue within scope. Repeated H1 runtime-gate wording in unchanged policy/skills is read subject to this exact published exception only. Ordinary non-audit workflows and unrelated acceptance standards are unchanged. No older document, review or failed receipt is erased, edited or relabeled PASS.

## 6. Fixed budgets, output boundary and future safeguards

**Correction pairs: 1/3 completed. Native CLI: 18/20 consumed.** This document and its docs-only delta review do not form a source-correction pair. No reset, increase or additional invocation is authorized; the two unused invocations are not an instruction or permission to run them. No additional H1 source correction is requested.

This session writes exactly one new file:

~~~text
docs/implementation/maintenance/2026-11-16-bf26-nr-h1-harness-preparation/h1-owner-amendment-final-acceptance-2026-11-16.md
~~~

Do not modify any of the 17 H1 source paths, the three author reports, global/project Codex configuration, trust/approval/sandbox/execpolicy, [ACTIVE](../../handoffs/ACTIVE.md), historical H0/H1 records, previous receipts, delivery/ or unrelated source/documentation. ACTIVE remains `NO_ACTIVE_HANDOFF`. No packaging, new report, review file, receipt or delivery output is granted here.

The Owner explicitly authorizes **no additional global-config investigation, native model testing, historical-equivalence attempts or new native CLI calls**. No network request, source correction, test run, Git stage/commit/push or D0/B0/X execution occurs in this author session. The final H1 review uses existing evidence and safe read-only checks; it must not reopen runtime qualification merely because accepted unknowns remain.

No D0/B0/X, product, Backend, RAG, Frontend, provider activation or deployment authority follows from this amendment or H1 closure. Future audits require separate Owner decisions, must disclose which runtime safeguards remain unverified and must not rely on unverified native hooks as guaranteed protection.

## 7. Exact final lifecycle

1. This session writes and checks this single amendment, then stops.
2. One **DIFFERENT Codex session** independently reviews the amendment delta, **DOCS ONLY**, with **chat output**. It assesses the recorded decision, precise supersession, evidence attribution, integrity and boundaries; it does not perform H1 source acceptance, source edits or runtime diagnostics.
3. Owner commits/pushes **only this amendment** after an acceptable delta review and verifies its actual published SHA. The Owner decision is already ratified; no repeat ratification is requested.
4. One independent Codex H1 reviewer, separate from the H1/C1 and amendment authors, performs final source-and-test acceptance under the **published amended contract**, using existing evidence and safe read-only checks. Review output remains chat-only; author self-review and the amendment delta review are not final H1 acceptance.
5. If accepted, Owner commits/pushes the **17 H1 source changes and three existing author reports**, retaining their bytes, and verifies the actual source-publication SHA. Amendment-only publication is not H1 source publication.
6. H1 closes with explicit runtime limitations. **D0/B0/X remain unauthorized** until separate Owner decisions.

Do not introduce another global-config qualification round or a new amendment solely because historical/current runtime properties remain NOT_VERIFIED. A new demonstrated defect is handled as a concrete finding under §4, without expanding this lifecycle by inference.

## 8. Docs Sync Check and author handback

**Classification:** new current Owner decision awaiting independent delta review and publication; existing parent/C1 records, reviews, reports and receipts remain immutable evidence.

| Disposition | Exact affected scope / result |
| --- | --- |
| UPDATED | Only `docs/implementation/maintenance/2026-11-16-bf26-nr-h1-harness-preparation/h1-owner-amendment-final-acceptance-2026-11-16.md`, created fresh; decision, future effectiveness and exact supersession recorded |
| ALREADY_CONSISTENT | Parent/C1 local published content, allowlist, acceptance rubric, original independent review outputs, all 17 C1 source hashes, three complete reports/prefixes/addenda, 91 prior receipt hashes plus preserved STOP, ACTIVE=NO_ACTIVE_HANDOFF, ordinary workflows and excluded tracked files checked/preserved |
| ALREADY_CONSISTENT | Different author/reviewer roles, Owner-only publication, source publication still pending, correction 1/3 and CLI 18/20; no new invocation or source correction |
| NOT_VERIFIED | All §3 runtime limitations; amendment's future independent delta review/publication and final H1 acceptance/source publication; no live-remote observation in this task |
| DEFERRED_OUT_OF_SCOPE | Edits to README/navigation, policy/skills, ACTIVE, existing reports/history/config/receipts or delivery/; no extra sync artifact or cleanup. This amendment's explicit precedence governs only the displaced H1 runtime gates once published |
| BLOCKED_OWNER_DECISION | None for the already explicit acceptance decision; independent delta review and Owner publication remain required, not a request for the same ratification again. A newly demonstrated defect, if any, requires its specific disposition |

Markdown structure, relative local links/anchors, exact source references, status and decision precedence are checked within this one-file scope. These author checks are not independent delta review or H1 acceptance.

**Handback:** `READY_FOR_INDEPENDENT_DELTA_REVIEW / H1_FINAL_ACCEPTANCE_AMENDMENT`.

**Next step:** ONE different Codex session | independent DOCS-ONLY delta review, chat output | this single final acceptance amendment and its controlling evidence | acceptable review before Owner amendment-only publication | correction pairs 1/3; native CLI 18/20; no additional invocations.

**STOP after creating and checking this single amendment. No Git stage/commit/push.**
