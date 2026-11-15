# BF26-NR-H0 — Harness read-only audit: authorization and bounded handoff

**Date:** 2026-11-15 (Asia/Ho_Chi_Minh)
**Document state:** `OWNER_REQUESTED_H0_DOCUMENT_PREPARATION / DRAFT_FOR_INDEPENDENT_PREFLIGHT`
**Execution:** `NOT_YET_AUTHORIZED`; no H0 inspection has started. This is a proposal for a *future* read-only audit, not an audit report, a reviewed executable or a modification to `ACTIVE`.

## Baseline and authority

- Remote `main` was queried when this draft was created: [`b5a51d40887017b50817adaf037207839d9959ec`](https://github.com/vhvkhangg/personal-private-vault/commit/b5a51d40887017b50817adaf037207839d9959ec), `docs(audit): prepare current non-rag comprehensive audit`, 14 newly published Markdown files. Parent is the earlier V1 closeout `4e4e2cb50ac84c86b0df93ad67f770648c7d32db`.
- Owner reports the temporary PH-02 evidence folder has been deleted. **The local checkout, worktree and folder deletion have not been inspected here**; H0 must check them anew.
- Parent preparation: [`BF26-NR-AUDIT-PREP`](../2026-11-15-bf26-nr-audit-preparation/README.md), especially its [Owner contract](../2026-11-15-bf26-nr-audit-preparation/owner-contract.md), [Codex-only workflow](../2026-11-15-bf26-nr-audit-preparation/codex-only-workflow.md), [harness plan](../2026-11-15-bf26-nr-audit-preparation/harness-audit-plan.md), and [coverage matrix](../2026-11-15-bf26-nr-audit-preparation/audit-coverage-matrix.md).
- Earlier [2026-11-01 audit intent](../2026-11-01-final-non-rag-audit-intent/owner-intent.md) remains applicable except for the **superseded** requirement to preserve runnable Phase 15 skill behavior. The newer Owner contract authorizes future rewriting the **existing** audit skill, not executing it now.
- [`ACTIVE`](../../handoffs/ACTIVE.md) remains `NO_ACTIVE_HANDOFF` at the published baseline. This H0 is a **read-only maintenance audit**, not an implementation handoff; do not change `ACTIVE` merely to make H0 discoverable.

## Exact lifecycle

1. Author this H0 **draft docs-only** under the new maintenance directory; no changes to the current harness, Backend, Git state, existing accepted docs, or audit skill.
2. Independent Codex **preflight** reviews all H0 draft files and checks their consistency with the published preparation and actual Git HEAD. Verdict is **only** for H0 documentation (`READY_FOR_OWNER_RATIFICATION` / `CHANGES_REQUESTED` / `BLOCKED_REVIEW`).
3. Owner explicitly ratifies the **exact H0 read-only authority**, including permitted report paths and fixture/log boundaries, through a canonical Owner authorization record and independently verifies/publishes its final content. A draft, review PASS or a commit alone is **not** execution authorization. See [Owner authorization proposal](owner-authorization-h0.md) §Activation.
4. Only after exact ratification, independent preflight and Owner publication: a **new Codex H0 auditor session** follows [read-only handoff](h0-readonly-handoff.md) using its [auditor prompt](h0-auditor-prompt.md). No call to the historical Phase 15 `$codex-backend-audit` skill.
5. Independent **different** Codex session reviews H0 report/manifest/scenarios. Owner selects a bounded H1 implementation/remediation scope and explicitly authorizes that separately. H0 may **not** rewrite skill/rules/hooks/AGENTS or open H1 itself.

## Contents

| File | Purpose |
|---|---|
| `owner-authorization-h0.md` | Exact read-only Owner contract **draft** and activation gates |
| `h0-readonly-handoff.md` | Bounded, executable-in-the-future H0 audit procedure |
| `h0-coverage-and-evidence.md` | Coverage H01–H13, file-depth and evidence register requirements |
| `h0-scenario-protocol.md` | H-T01–H-T20 safe harness scenario definitions and expected outputs |
| `h0-preflight-and-acceptance.md` | Preflight, H0 exit conditions, independent H0 report review and STOP logic |
| `h0-h1-decision-docket.md` | Findings-to-H1 choices, no speculative edit allowlist |
| `h0-preflight-review-prompt.md` | Copy/paste prompt for independent **docs-only** preflight |
| `h0-auditor-prompt.md` | Copy/paste future H0 read-only auditor prompt; **not an invocation now** |

## Fixed exclusions

No Antigravity in this audit; different Codex sessions for H0 producer, H1 implementer and independent reviews. No source/skill edit, no Git publication by agents, no branch/worktree creation, no provider activation, no full Backend audit, no RAG functional work, no Frontend restart, no acceptance of Phase 15 as live mode. `delivery/` ZIP workflow is **to be discovered**, not assumed or created by H0.

**Next step:** Owner applies this new-files-only draft; separate Codex session runs **H0 docs-only preflight**. If accepted, Owner ratifies and publishes the exact H0 read-only authorization. **STOP: H0 must not execute from this draft.**
