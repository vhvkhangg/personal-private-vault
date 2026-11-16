# BF26-NR-H1-PREP — H0 closeout and bounded H1 harness preparation

- **Prepared:** 2026-11-16 (Asia/Ho_Chi_Minh).
- **State:** `H0_INDEPENDENTLY_ACCEPTED_PER_OWNER_SUPPLIED_REVIEW / H0_OWNER_PUBLICATION_PENDING / H1_PREPARATION_DRAFT / H1_NOT_AUTHORIZED`.
- **Observed published starting HEAD:** [`7c521fb9998bc1ad0a86e7225aa605f0de17e261`](https://github.com/vhvkhangg/personal-private-vault/commit/7c521fb9998bc1ad0a86e7225aa605f0de17e261), `docs(audit): ratify bounded H0 harness audit`, verified on GitHub `main` during drafting.
- **Source of H0 independent assessment:** Owner-provided text dated 2026-11-16, verdict `H0_ASSESSMENT_ACCEPTED / OWNER_H1_DECISION_REQUIRED`. Its complete original reviewer output was supplied via chat attachment, **not** yet independently verified as a published repository report. The five H0 producer reports were reported untracked at the reviewer checkpoint. Do not imply Owner publication, H1 approval, or runtime qualification.

## Objective

Retain the accepted H0 evidence and prepare one finite, verifiable, **Codex-only H1 harness remediation** addressing `NRH0-F01` (Owner-mandated audit-skill replacement and callers), `NRH0-F02` (branch-deletion semantic evaluator gap), and `NRH0-F03` (narrow stale operational guidance). This is a preparation/decision package: **no source, harness, Git or runtime change is authorized by its existence**.

## Files

| File | Purpose |
|---|---|
| [h0-evidence-publication-gate.md](h0-evidence-publication-gate.md) | Preserve independent H0 verdict with correct provenance and publish the five H0 reports before H1 execution |
| [h1-owner-decision-draft.md](h1-owner-decision-draft.md) | Exact proposed Owner decisions, defaults and remaining decision gates |
| [h1-exact-path-allowlist.md](h1-exact-path-allowlist.md) | Seventeen existing-file candidates proposed for approval; excluded conditional/deferred paths |
| [h1-codex-author-handoff-draft.md](h1-codex-author-handoff-draft.md) | Single-session bounded implementation handoff, outputs, testing and STOP rules |
| [h1-preflight-and-acceptance.md](h1-preflight-and-acceptance.md) | Independent docs preflight and later independently verified H1 acceptance criteria |
| [independent-h1-preflight-prompt.md](independent-h1-preflight-prompt.md) | Copy/paste prompt for an **independent review of the draft**, not H1 execution |

## Source of truth and authority

1. [Published H0 authorization](../2026-11-15-bf26-nr-h0-harness-readonly-audit/owner-authorization-h0.md) and [H0 handoff](../2026-11-15-bf26-nr-h0-harness-readonly-audit/h0-readonly-handoff.md).
2. Five H0 **local reports**, which are NOT in published HEAD as this draft was composed: `h0-audit-report.md`, `h0-file-manifest.csv`, `h0-scenario-results.md`, `h0-h1-proposal.md`, `h0-evidence-index.md` in the H0 directory; actual local hashes and reviewer evidence must be checked before Owner publication.
3. [Published Non-RAG owner contract](../2026-11-15-bf26-nr-audit-preparation/owner-contract.md), which supersedes retention of a runnable Phase 15 skill mode while preserving historical evidence, and [Codex-only stages](../2026-11-15-bf26-nr-audit-preparation/codex-only-workflow.md).
4. Current [Owner delivery policy](../../../workflow/current-owner-delivery-policy.md) and [ACTIVE](../../handoffs/ACTIVE.md), **read** for reconciliation, not silently edited.

## Sequence / STOP gates

**G-H0:** independent H0 acceptance is Owner-supplied but must be preserved with traceable source; Owner publishes five H0 reports and authentic independent-review evidence on `main`, then obtains the actual SHA. If acceptance or content changes materially, STOP and re-review.

**G-H1P:** separately review this H1 preparation; reconcile the exact path allowlist and conditional exclusions against then-current Git HEAD; receive an explicit dated Owner H1 ratification **after review**. Publishing a draft alone is not ratification.

**G-H1X:** only after ratification and publication of an executable H1 contract, a **new Codex author session** may edit the approved 17 paths and write exact allowed new handback/receipts. A different Codex reviewer independently assesses the changes, then Owner publishes. D0/B0 are blocked until this H1 result is independently accepted and Owner-published.

Do not invoke the historical Phase 15 `$codex-backend-audit` during preparation or H1 implementation. Do not create a new audit skill. Do not change `ACTIVE` merely to make this work discoverable.
