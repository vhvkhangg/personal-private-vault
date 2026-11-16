# Codex-only comprehensive non-RAG audit workflow — prospective

**Workflow is a future proposal, not an active handoff.** The existing policy currently describes Antigravity per-slice implementation. This package deliberately documents the **requested exception for this audit only**, and **does not amend those existing files or infer permanent Codex-only policy**.

From D0 onward, the [current Owner policy for reviewed Non-RAG publication](../../../workflow/current-owner-delivery-policy.md#reviewed-non-rag-publication)
controls publishing in this workflow and the associated audit plans. The Owner
may explicitly delegate publication for an approved batch after any required
independent review. Inspector, author and reviewer permissions come from their
grants. The preparation and H0/H1 publication records keep their original meaning.

## Session/permission matrix

| Session | Intent | Read/write allowance after its *own* authorization | Must NOT do | Exit |
|---|---|---|---|---|
| P — Preparation author (this package) | Produce docs-only audit design | New/revised draft Markdown within this preparation directory only | Audit, implement, self-accept, commit/push | `PREPARATION_DRAFT` |
| PR — Independent preparation reviewer | Challenge Owner/scope/coverage/workflow | Review report(s) only if approved | Implement or declare audit completed | `READY_FOR_OWNER_COMMIT` or `CHANGES_REQUESTED` |
| H0 — Harness inspector | Independent **read-only** baseline and bootstrap harness evaluation | Scoped reports + ignored diagnostic receipts, if authorized | Edit skills/hooks/AGENTS, provider calls, code | `AUDIT_REPORTED` or blocked |
| H1 — Codex harness remediation author | Rewrite existing audit skill for current Non-RAG audit and implement *exact approved* related skills/config/docs/delivery fixes | Explicit path allowlist, fixture tests, cross-reference updates and handback | Run old Phase 15 audit, self-independent-accept, publish | `AWAITING_INDEPENDENT_REVIEW` |
| HR — Independent harness reviewer | Audit H1 diff and functional scenarios | Review-only report/receipts | Accept own H1 changes, redesign unauthorized paths | `ACCEPTED` or `CHANGES_REQUESTED` |
| D0 — Docs/design inspector | All-docs inventory and risk-qualified analysis | Reports/receipts only | Rewrite immutable history or apply redesign | `AUDIT_REPORTED` or blocked |
| D1 — Codex docs remediation author | Make approved current-doc/ADR/derived updates | Exact authorized documents and verification | Change frozen contracts/archives implicitly | `AWAITING_INDEPENDENT_REVIEW` |
| DR — Independent docs reviewer | Link/provenance/design consistency and changed-file inspection | Review-only | Self-accept | `ACCEPTED` or `CHANGES_REQUESTED` |
| B0 — Non-RAG Backend inspector | Source/DB/HTTP/test/security/tooling audit | Reports; permitted bounded diagnostic logs | Production changes, unauthorized live provider access | `AUDIT_REPORTED` or blocked |
| B1 — Codex Backend remediation author | Bounded root-cause batches | Owner-approved source/test/docs path allowlist | Change frozen source beyond exception, self-accept, publish before review or without delegation | `AWAITING_INDEPENDENT_REVIEW` |
| BR — Independent Backend reviewer | Review B1 diff with fresh/reused qualified tests | Review-only | Implement or approve own changes | `ACCEPTED` or `CHANGES_REQUESTED` |
| X — Independent reconciliation | Recheck entire audit ledger and published HEAD | Reports/docs-only closeout after authorization | Invent missing evidence or self-grant next phase | `NON_RAG_BACKEND_AUDIT_READY` **only when all gates met**, else `STOP / OWNER_DECISION_REQUIRED` |

**Every named role is a fresh Codex session**, or independently refreshed context where distinct-session identity is verifiable. One Codex tool/model does not become independent merely because a second prompt says “review independently.” Every review must inspect the actual diff/evidence anew and declare overlap limitations.

## Stages and mandatory barriers

### P / PR — Preparation (this requested deliverable)

1. Record remote starting SHA, Owner preparation direction and scopes/exclusions.
2. Submit the new docs-only package; do not modify existing tools/skills/API.
3. Run an **independent** `$codex-pre-handoff-review` in **maintenance docs-only mode** (not an implementation/audit verdict). The reviewer may challenge missing prerequisites and recommend corrections, not rewrite/fix them in the same session.
4. Owner applies approved preparation, stages/commits/pushes. Recheck actual SHA; publish any needed *current navigation sync* under separately reviewed docs-only scope. `ACTIVE` remains `NO_ACTIVE_HANDOFF`.
5. **STOP**. Owner must separately approve exact **H0 read-only audit** authorization with permitted reports/diagnostics and baseline.

### H0 — Bootstrapped harness audit before skill changes

- Read the published preparation contract directly, not via a nonexistent `current-non-rag` skill mode. Confirm a *new, explicit Owner H0 grant*.
- Inspect all `.agents/`, `.codex/`, AGENTS/related scripts/config and environment; collect evidence, no writes other than approved reports/ignored receipts.
- Classify findings; propose a **replacement** of existing `codex-backend-audit` Phase 15-only routing, plus changes to `codex-create-handoff`, all relevant review/implementation skills, rules, hooks, workflow, configuration and packaging utilities. Inspect whether `delivery/` and review-to-ZIP behavior exist; if missing, report `NOT_VERIFIED` rather than inventing it. Mark version-dependent safety claims NOT_VERIFIED until tested.
- Independent reviewer challenges H0 report/coverage. Owner chooses the finite H1 package; frozen/behavioral changes require explicit exception.

### H1 — Harness remediation / skill mode enablement

- Codex author in a **different** session implements only approved H1 allowlisted paths, **rewriting the existing** `.agents/skills/codex-backend-audit/SKILL.md` to make `current-non-rag` the only supported active audit route. Remove obsolete runnable Phase 15 preconditions, handoff loops and status routing from that skill; keep the original historical records in `docs/implementation/phase-15/**` and Git history intact.
- An attempt to invoke historical Phase 15 must **STOP without executing**; accepted Phase 15 history remains consultable as evidence, not a current runnable mode. Current audit must require exact Owner approval, correct H/D/B/X phase, immutable baseline receipts, role/permission checks and fail-closed ambiguous scope handling.
- Perform a dependency-graph sweep and update **all demonstrably affected existing** skills (`codex-create-handoff`, `codex-final-review`, `codex-pre-handoff-review`, review coverage/implementation routing as applicable), `AGENTS.md`, `.codex/` policies, hook/scripts, workflow/resume prompts and navigation **within H1's explicit authorized allowlist**. Preserve unrelated Antigravity workflows if still active elsewhere; do not globally rename historical references.
- Treat any `delivery/` pipeline as a **conditional, separately verified workflow**: reproduce what currently generates ZIPs, what goes into them and whether it is scripted or ad hoc. If a packaging change is justified and approved, implement it in the responsible **existing** skill/script/docs with tested path/contents/manifest, safe exclusion of secrets/logs and no self-acceptance; do not create a new packaging framework by default.
- Run scenario-based positive/negative checks on the actual supported Codex host and disposable fixtures; independent Codex HR reviewer verifies both changed source and the packaging behavior, if applicable. Owner publishes on actual SHA.
- Only after HR acceptance and Owner publication may D0 and B0 invoke the rewritten skill. If H1 is blocked, do not route through legacy Phase 15 semantics as a workaround.

### D0/D1 — Docs and design

- Inspect all docs paths with exhaustive manifest and risk-based read-depth, reconcile current accepted product requirements with source snapshots without claiming code validation.
- All architecture API/schema changes remain proposals until Owner ratifies exact behavior/compatibility and allows corresponding code changes.
- Group approved documentation remediation into bounded D1 changes, independently review, then publish under current Owner policy. Cross-layer conflicts remain in X rather than prematurely declaring Docs “final.”

### B0/B1 — Source and runtime

- Requalify published SHA and accepted contracts; inspect every Non-RAG module and risk-applicable shared boundary, run *approved* tests/tooling under bounded safeguards.
- Findings map to minimal root causes; source-edit allowlists and test contracts require Owner approval for **each** executable batch. Codex B1 implements, a different Codex BR reviews, and the authorized publisher follows current Owner policy. Avoid one-finding-per-round proliferation.
- Code issues requiring contradictory docs/ADR change must route to an Owner decision, not unilateral fixes.

### X — Cross-layer final gate

- Repeat relevant affected checks on final published HEAD; aggregate unchanged qualified evidence without double counting.
- Require every in-scope actionable CRITICAL/HIGH/MEDIUM/LOW/INFO finding a legitimate closed disposition. Non-actionable design ideas are not forced into remediation.
- `NON_RAG_BACKEND_AUDIT_READY` is a **future audit result**, not equivalent to Frontend authorization, production readiness or RAG acceptance.

## Execution controls

- **Per-batch immutable evidence receipt:** approved contract revision/SHA, session ID/role, entry SHA, clean/drift status, changed path list, commands/exit codes/versions/timestamps, raw receipt paths, independent reviewer SHA, final state and next decision. If an approved `delivery/` ZIP is produced, record its explicit file allowlist, content manifest and SHA-256 separately; a ZIP is **not** independent acceptance.
- **No role blending:** a session that made any affected source change cannot provide independent review for that change. Author self-review remains mandatory for quality but has no independent verdict.
- **Protected Git operations:** Direct Git publication, tags and PRs are Owner-only. Codex uses the reviewed publisher only when the grant delegates publication. Do not rely on Codex prompt text or `approval_policy=never` as an actual safety boundary. Test hooks/policies in a disposable fixture, never by trying destructive commands in Owner's checkout.
- **Existing `ACTIVE` gate:** empty active handoff is normal; do not infer no Owner approval. Conversely, an old authorization or a proposed plan does not create execution permission.
- **Bounded remediation:** choose an Owner-approved finite re-review budget for each audit batch; propose **up to three corrective author/review rounds as a default**, not a grant or reset of past slice counters. If exhausted, STOP for Owner rather than silently bypassing or accepting unresolved findings.
- **Interruption:** resume from exact approved handoff/current SHA + receipts + git diff; verify last finished state before retrying with a *fresh* evidence path. Do not reset, clean, overwrite logs, rerun risky provider calls, or count a failed test as a PASS.
