---
name: codex-backend-audit
description: Route an explicitly Owner-authorized current Non-RAG H/D/B/X audit, bounded Codex remediation or separate independent review; refuse historical Phase 15 execution and require exact phase, baseline, role, paths and evidence permissions.
---

# Codex Backend Audit

Invoke with $codex-backend-audit. The only active mode is current-non-rag.
This skill supplies **no execution grant**. A report, empty ACTIVE register or
successful test never authorizes the next audit phase.

## First dispatch: historical refusal, then exact authority

An explicit phase-15 / Phase 15 execution or recovery request returns
**STOP / HISTORICAL_ONLY** immediately. Do not substitute current mode, run old
tools, create a remediation handoff, change ACTIVE/status, or rewrite evidence.
Refer to the immutable [Phase 15 final acceptance](../../../docs/implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md)
and Git history for the former skill. BACKEND_AUDIT_READY is archival, not an
outcome this skill may issue. Missing/unknown mode or phase also STOPs.

For a current request, read the exact current Owner grant **before** diagnostics
or report generation, root/scoped AGENTS, [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md),
[ACTIVE](../../../docs/implementation/handoffs/ACTIVE.md), and the grant's
preparation/review/handoff and latest handback. Governing design:
[Owner contract](../../../docs/implementation/maintenance/2026-11-15-bf26-nr-audit-preparation/owner-contract.md)
and [Codex-only session workflow](../../../docs/implementation/maintenance/2026-11-15-bf26-nr-audit-preparation/codex-only-workflow.md).
Preparation headings are not grants. Later ratifications supersede only their
express scope; substantive contradiction requires STOP.

Require and record all of these with exact source paths:

1. **Authority:** dated, current, Owner-published authorization ID/revision and
   exact phase; independent preparation prerequisite; no revocation, supersession,
   conflicting approval or competing live handoff. Discover the named canonical
   maintenance decision even with empty ACTIVE. Draft, CHANGES_REQUESTED
   preparation and completed historical approval are insufficient.
2. **Baseline:** approved origin, branch, HEAD, origin/main and published source
   SHA under the grant's comparison rule; tracked/staged/untracked and relevant
   ignored inputs, approved overlay hashes and prior receipts. Network checks
   need their own exact permission. No fetch/pull or implicit sync. A behind/wrong
   baseline is STOP / BASELINE_MISMATCH, not missing approval.
3. **Role:** one exact phase/role below; session identity/provenance where
   observable; author/reviewer overlap. An author cannot independently accept
   affected work after compaction, a second prompt or role relabeling. Unknown
   independence is NOT_VERIFIED.
4. **Operations/outputs:** literal source/test/report/receipt paths, allowed
   reads/writes/commands, frozen exceptions, network/provider/fixture permissions,
   hard timeouts and finite correction budget. Verify parents, no-clobber, ignore
   rules and effective client safety. Wrong output, missing mandatory dependency
   or unsafe diagnostic means STOP.
5. **Prerequisites:** actual prior independent acceptances and publication required
   by this phase's grant. Reuse H1's independent source/test acceptance and
   publication with its [accepted runtime limitations](../../../docs/implementation/maintenance/2026-11-16-bf26-nr-h1-harness-preparation/h1-owner-amendment-final-acceptance-2026-11-16.md).
   D0/B0 still require their own grants. H1's
   expressly ratified fixture qualification is test-only: synthetic grants there
   never authorize a real H/D/B/X audit.

No approval -> STOP / OWNER_APPROVAL_REQUIRED; competing approvals ->
STOP / AMBIGUOUS_SCOPE. State the exact failing gate and minimum Owner action.
Do not reset/stash/checkout/clean, modify protections, create branches/worktrees
or activate ACTIVE to manufacture eligibility. Full access is capability only.

## Phase and session boundary

| Phase | Exact session role | Only under its own exact grant | Handback |
|---|---|---|---|
| H0 | Harness inspector | Read-only harness assessment; named reports/receipts | AUDIT_REPORTED or blocked |
| H1 | Harness remediation author | Selected harness findings and literal source/test/docs paths | IMPLEMENTED_AWAITING_INDEPENDENT_REVIEW or BLOCKED_H1 |
| HR | Independent harness reviewer | H1 diff/evidence; chat unless a distinct report path is granted | H1_ACCEPTED / READY_FOR_OWNER_COMMIT, H1_CHANGES_REQUESTED or BLOCKED_H1_REVIEW |
| D0 | Docs/design inspector | All scoped docs inventory, product/design/provenance assessment; reports only | AUDIT_REPORTED or blocked |
| D1 | Docs remediation author | Owner-selected current-doc/ADR/derived changes | IMPLEMENTED_AWAITING_INDEPENDENT_REVIEW or blocked |
| DR | Independent docs reviewer | D1 diff/evidence; no implementation | ACCEPTED, CHANGES_REQUESTED or blocked |
| B0 | Non-RAG Backend inspector | All scoped source/config/test/DB/integration assessment and allowed diagnostics; reports only | AUDIT_REPORTED or blocked |
| B1 | Backend remediation author | Selected finding batch, exact source/test/docs and frozen exceptions | IMPLEMENTED_AWAITING_INDEPENDENT_REVIEW or blocked |
| BR | Independent Backend reviewer | B1 diff and qualified fresh/reused evidence; no implementation | ACCEPTED, CHANGES_REQUESTED or blocked |
| X | Independent reconciliation reviewer | Final published H/D/B ledger and cross-layer contracts; named reports only | NON_RAG_BACKEND_AUDIT_READY only with every final gate; otherwise STOP / OWNER_DECISION_REQUIRED |

H0/H1 bootstrap use their direct published contracts, not a retroactive rerun
through this skill. HR has its separate review authorization.
The closed H1 records retain their original evidence and counters. D0 onward
does not repeat global-config stability checks, historical drift investigation
or native H1 replay for the accepted runtime limitations.

Inspection, authoring and independent review are **distinct Codex sessions for
this audit only**. Never dispatch audit remediation to Antigravity or use the
takeover skill to invent author authority. Ordinary non-audit Antigravity
implementation/self-review/review workflows remain unchanged.

## Scope-specific execution and evidence

Use the exact executable contract and existing
[skill-local report asset](assets/backend-audit-report-template.md) only at an
expressly granted path; no extra reports to fill a template. H1 uses its exact
three records and chat-only independent review. Audit dispatch returns here
before generic handoff/review templates and ordinary Antigravity routes. No
ACTIVE handoff creation unless a separate exact grant permits/requires that path.

- **H:** source/caller/reference graph, instructions/recovery, evaluator/adapters,
  supported native runtime and ordinary-workflow regressions.
- **D:** every granted docs path with review depth, provenance, real product needs
  and KEEP/REUSE/SIMPLIFY/REMOVE/DEFER reasoning under the approved docs plan.
- **B:** every granted Non-RAG source/integration boundary; correctness, privacy,
  HTTP/OpenAPI, persistence/transactions, ownership, performance, duplication,
  overengineering, tests and operations. Use the approved Backend plan and
  risk-applicable codex-review-coverage categories. Tests alone are not an audit.
- **X:** resolve provisional docs conclusions against final published source;
  qualify reused evidence by source/dependencies without double counting.

Inventory every in-scope path at approved SHA with blob/hash, depth, evidence or
exclusion reason. Choose the smallest sufficient existing mechanism. No full
Maven/static/provider run, install, dependency or configuration change without
exact permission. Use the existing bounded-command runner for permitted long
commands, shorter phase caps where required, owned external fixtures and fresh
ignored receipts without secrets. Unavailable native/model/hook/trust evidence
stays NOT_VERIFIED. Parser results, configured model and static inference do not
prove runtime enforcement. No RAG/Frontend/provider/deployment authority.

## Findings, remediation and final gates

Follow the [findings contract](../../../docs/implementation/maintenance/2026-11-15-bf26-nr-audit-preparation/findings-and-remediation-contract.md).
Keep existing IDs such as NRH0-F01–F03; new IDs follow the grant (normally
NR-H/D/B/X-###). Record type, severity, confidence, authority, path/line/SHA,
falsifiable observed vs expected behavior, root cause, consequence, smallest
remedy, regression evidence, frozen impact and independent disposition.
Distinguish FACT, verified runtime, static inference, assumption and NOT_VERIFIED.
Taste, duplicates and optional ideas are not defects.

Every substantiated actionable in-scope finding **including LOW/INFO** needs
evidence-backed resolution or **STOP / OWNER_DECISION_REQUIRED**. No automatic
severity waiver, historical counter/debt transfer or author claim of independent
closure. Frozen/API/schema/ADR changes need exact Owner decisions. Assessment
may propose a batch; only later published Owner selection authorizes edits.
Never write ungranted statuses, history or ACTIVE. A grant from D0 onward may
delegate publication through the [reviewed Non-RAG publication workflow](../../../docs/workflow/current-owner-delivery-policy.md#reviewed-non-rag-publication).

An author self-reviews and stops awaiting a different independent reviewer.
Reviewer checks actual diff, all criteria/actionable findings, evidence and
mandatory native gaps. Corrective pairs consume only the expressly granted
batch budget after actual correction plus independent re-review; crashes/retries
are not rounds. At exhaustion STOP, no automatic takeover. Independent acceptance
precedes Owner publication and actual-SHA qualification. X cannot issue readiness
with unresolved actionable findings, mandatory NOT_VERIFIED or unreconciled
current contracts.

## Interruption, Docs Sync and next action

Use the [audit resume prompt](../../../docs/workflow/resume-prompts/codex-backend-audit.md)
for **all audit roles**, including HR/DR/BR reached via final-review. Generic
create-handoff/final-review recovery dispatch is not selected for this audit.
Recover the same grant, phase, role, completed step, hashes, actual exits/cleanup,
outputs and counter. Reuse applicable evidence; never restart an old audit,
overwrite receipts or treat a crash as approval/a new round.

Apply [Docs Sync](../../../docs/workflow/documentation-synchronization.md) within
the exact grant. Its ordinary role tables/templates cannot enlarge audit output
permission. Record UPDATED / ALREADY_CONSISTENT / DEFERRED_OUT_OF_SCOPE /
BLOCKED_OWNER_DECISION / NOT_VERIFIED in the allowed handback or chat. If a
reachable shared rule still substantively conflicts and cannot be reconciled
inside the allowlist, STOP with the minimum additional path for Owner amendment
and independent re-review; never silently ship a contradictory executable route.

End with outcome, changed paths, evidence/limitations, findings, Docs Sync,
counter and one line:

Next step: <role> | <exact approved action> | <phase + canonical contract> | <gate> | <correction budget> | <new PPV Codex chat: yes/no>

Author -> different authorized independent reviewer. Reviewer -> approved Codex
author within budget, or accepted review -> authorized publisher. Blocked/ungranted
phase -> Owner. No automatic D0/B0/X launch, ZIP/delivery output or self-acceptance.
