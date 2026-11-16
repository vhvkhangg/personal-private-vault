# Owner Grant — BF26-NR-D0 Documentation Audit

- **Decision date:** 2026-11-16 (UTC+07)
- **Revision:** 1
- **Owner decision:** Approved in the PPV workflow chat on 2026-11-16. The Owner approved this grant's scope, two D0 outputs, and delegated publication of the grant and reports after their respective independent reviews.
- **Activation:** Independent grant review accepted, then the exact reviewed grant published and verified on GitHub `main`. The verified commit and provenance of the independent acceptance identify the effective grant.
- **Execution:** `current-non-rag / D0 / Docs/design inspector`, in a new Codex chat in the PPV project.

## Authority and entry

Use the [audit skill](../../../../.agents/skills/codex-backend-audit/SKILL.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md#reviewed-non-rag-publication), [D0 audit plan](../2026-11-15-bf26-nr-audit-preparation/docs-design-audit-plan.md), and [coverage matrix D01–D14](../2026-11-15-bf26-nr-audit-preparation/audit-coverage-matrix.md). This file is the executable grant for D0. It controls D0's exact writes and publication rights under the current Owner policy.

- Preparation was published at `b5a51d40887017b50817adaf037207839d9959ec` after independent `READY_FOR_OWNER_COMMIT / DOCS_ONLY_PREPARATION` disposition in chat `01a1df67-3d44-7413-aeb1-5e85bdcdb3ba`.
- Reuse the [published H0 independent assessment](../2026-11-15-bf26-nr-h0-harness-readonly-audit/reviews/2026-11-16-h0-independent-review.md). H1 source/tests were accepted in independent chat `01a1e4aa-f3fb-7aa1-9492-5fc169dc62f9` and the implementation was published at `e9752aaaaeec2ad97841d826cfb8f1a812727a00`. Carry forward the [H1 accepted runtime limitations](../2026-11-16-bf26-nr-h1-harness-preparation/h1-owner-amendment-final-acceptance-2026-11-16.md). Repeat qualification is outside this grant.
- The grant commit must have sole parent `9f0cb2a09094ef539c470839bf4eadc193aec7c9` and add only this file. Its actual published SHA is the D0 audit baseline. At entry, verify local branch `main`, `HEAD`, cached `origin/main`, and live `origin/main` all identify that grant commit.
- Verify the approved `origin` is `https://github.com/vhvkhangg/personal-private-vault.git`, the checkout and index are clean, both output paths are absent, and relevant ignored inputs do not affect the audit. Read root/scoped `AGENTS.md`, confirm [ACTIVE](../../handoffs/ACTIVE.md) is `NO_ACTIVE_HANDOFF`, and check for competing, amended or revoked authority. Record entry checks and the audit SHA in the report. Stop on a material entry mismatch or missing authority. Leave existing Git state intact.

## Audit scope and evidence

1. Account for every tracked `docs/**` path and every tracked `AGENTS.md` once at the audit SHA. Deep-review current canonical and derived contracts by risk, including decisions, architecture, API, data, security, and workflow. Check historical material for event-time provenance and references. Check local links and diagram source/export pairs, and record an evidence-based disposition or reason for each path.
2. Assess D01–D14 using the existing plan. Compare current product guarantees with the simplest adequate alternatives, including the evidence, tradeoffs, and approval needed. Map PUBLIC/PRIVATE by method/path and field exposure, including indirect exposure through metadata, links, search and caches. Trace the documented API, persistence and schema contracts to the published sources as needed.
3. Read selected tracked source, migrations, configuration, OpenAPI and existing test sources or published evidence when cited by a D0 check. Label runtime or implementation claims still requiring B0 verification `PENDING_SOURCE_CORROBORATION`. Record exact path, line or blob, applicable authority and concrete impact for findings. Use `NOT_VERIFIED` for missing evidence. Prioritize substantiated actionable defects, including LOW/INFO, and keep design proposals distinct.

## Operations and exact writes

- Analysis is read-only on the audited inputs. Permitted checks are local Git reference/status/tree/blob/history reads, search, file reads, hashing, and static link or structured-document parsing with existing local tools. Use short checks with a maximum of 120 seconds per check. Record material commands and results in the report. No new diagnostic files or receipts are authorized during inspection.
- The inspector may run only `git ls-remote --exit-code origin refs/heads/main` for the entry network check, once after validating the remote URL. Publication checks run separately under the delegated publisher. No fetch/pull, arbitrary external link probes, installs, application or test execution, provider calls, native H1 replay, or reads of Owner data or global Codex configuration.
- During grant preparation, the sole deliverable is the new file `docs/implementation/maintenance/bf26-nr-d0-documentation-audit/owner-grant-d0.md`. During D0 execution, the only writable deliverables are these two new files:

  - `docs/implementation/maintenance/bf26-nr-d0-documentation-audit/d0-file-manifest.csv` — path, blob SHA, review depth, result and evidence.
  - `docs/implementation/maintenance/bf26-nr-d0-documentation-audit/d0-audit-report.md` — D01–D14 coverage, current authority and provenance, link/diagram status, product and privacy contracts, evidence, simplification proposals, findings and Owner decisions needed.

- Preserve audited docs, source, historical evidence, frozen contracts, and `ACTIVE`. D1, B0 and X require separate grants. No finding remediation is authorized by D0.

## Handback, review and publication

- D0 returns `AUDIT_REPORTED` when D01–D14 and the manifest are accounted for, evidence and limitations are explicit, and verified findings and provisional claims are traceable. A reported finding can remain open for an Owner decision. A failure of a mandatory D0 gate is reported as blocked, with the exact missing evidence or authority. Neither outcome opens another phase.
- A different Codex chat reviews the grant before activation. A chat independent of the D0 inspector reviews coverage, evidence and the exact two outputs before publication. Both reviews are read-only, report in chat, and record the reviewed candidate Git tree SHA if accepted. The inspector may make one focused report correction and return it for independent re-review if needed. Remediation budget for audited findings is zero.
- The Owner expressly delegates two separate publications via the [reviewed publisher](../../../../scripts/publish-non-rag.py). After independent acceptance, publish exactly this grant against its approved parent. After independent acceptance of D0 outputs, publish exactly the two reviewed reports together against the published grant commit. Verify each actual SHA on GitHub `main`. A changed baseline, candidate tree or file set requires reconciliation under the grant and current policy before any push.
- At each handback, state whether a new project chat is needed. Use a different reviewer chat for independence, the same author chat for publication of the reviewed grant, and a new PPV chat for the D0 inspector after activation.
