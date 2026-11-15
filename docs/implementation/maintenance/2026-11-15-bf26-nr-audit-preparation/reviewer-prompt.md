# Copy/paste prompt — independent preparation challenge (Codex or Claude)

> This file is **a future prompt**, not a launched review or authority for execution. The reviewer may place its report only at an explicitly allowed path; do not overwrite the preparation package, historical reports or files outside review-only scope.

```text
You are an INDEPENDENT PREPARATION REVIEWER of PPV's planned current Non-RAG comprehensive audit, NOT an audit executor or implementer.

Repository: https://github.com/vhvkhangg/personal-private-vault
Drafting remote main baseline: 4e4e2cb50ac84c86b0df93ad67f770648c7d32db
Package: docs/implementation/maintenance/2026-11-15-bf26-nr-audit-preparation/

Start with read-only inspection. Verify actual local HEAD, origin/main, remote main, working-tree/index and whether the package paths already exist. DO NOT assume the drafting SHA remains current after the Owner imports/publishes this package. Do not stash/reset/checkout/clean or alter protected files to reconcile drift. Read canonical current authority: root AGENTS.md, Owner delivery policy, ACTIVE.md, 2026-11-01 final non-RAG audit intent, Backend-first Owner decision, V1 actual-SHA closeout, existing codex-backend-audit and pre-handoff-review skills. Respect historical accepted evidence and newer superseding Owner decisions.

Review EVERY file in this **applied and subsequently corrected** preparation package. The prior independent review returned CHANGES_REQUESTED with NRP-PH-01 (ambiguous H1 bypass), NRP-PH-02 (missing earlier correction-archive provenance), and NRP-PH-03 (tracked AGENTS.md count 23 vs 22). Re-review those findings and affected acceptance criteria A01/A06/A08/A20/A21, while checking for new regressions. The author separately supplies original and correction ZIPs, extracted before/after preparation snapshots, hashes and a reproducible verifier in a standalone provenance evidence bundle. Read acceptance-and-reconciliation.md §A.1; compare verified archive bytes to actual local files, not only git diff for untracked files. If the external evidence bundle is unavailable, retain NOT_VERIFIED for relevant criteria. The requested architecture is:
(1) harness: entire .agents/, .codex/, all AGENTS.md, hooks, related scripts and workflow, assess real Codex and GPT-6.1 Sol compatibility, streamline only with behavioral proof;
(2) all docs/, design/product KISS/YAGNI, including adr/ architecture/ database/ implementation/ repository/ workflow/ and all remaining docs;
(3) full current Non-RAG Backend source, tests, migrations, contracts, dependencies, runtime boundaries;
(4) cross-layer reconciliation against actual published HEAD.

Codex-only audit and remediation, in separately identified sessions for author/read-only auditor/independent reviewer. NO Antigravity for this audit. **New superseding Owner decision (2026-11-15): REWRITE the EXISTING codex-backend-audit SKILL.md** during future separately approved H1, as one current Non-RAG harness/docs/backend/reconciliation audit. No new skill, and NO requirement to preserve runnable historical Phase 15 behavior; fail-closed if Phase 15 invocation is attempted. Preserve Phase 15 source/evidence/reviews/counters in historical docs/Git. This expressly supersedes only the keep-Phase-15-skill-mode clause of the 2026-11-01 Owner intent. Bootstrap H0 through explicit approved contract while the old skill is Phase 15-only. H1 must plan updates to all affected existing skills, `.codex`/AGENTS/rules/scripts/workflow and pointers, scoped by H0 findings and separate Owner approval. No execution authority follows from this preparation or an empty ACTIVE handoff.

Check the Owner's `delivery/` claim adversarially: the supplied repository ZIP has NO `delivery/` entries; this is not proof of local ignored/untracked state. Future H0 must inspect whether automatic review ZIP packaging actually exists, who triggers it, where the outputs land, and what files are included. If justified, require later approved H1 changes to relevant existing scripts/skills/docs and independent archive manifest tests. Never equate a ZIP with independent acceptance or let a review-only session silently implement packaging.

Product: permanently single Owner; fixed PUBLIC/PRIVATE profiles, no indirect leakage of NSFW/Personal/People in PUBLIC, preserve exact eight accepted PUBLIC business GET pairs and denied PUBLIC Film POST unless separately authorized. Evaluate UI-list names/displayName only where safe/cheap/useful; never silently change accepted PRIVATE-only V1 trash item contract {id,entryType,deletedAt}. Minimal sufficient design over extra abstractions without losing auth/data integrity. S5 remains DOCS_ONLY_API_READINESS; H1-F paused and FR-13 open; P17 frozen, P18 closed, S19-2 suspended/archival-only; no live provider/production/full-suite claims absent evidence.

Do not implement, modify code/harness/AGENTS/current governance, run live provider calls, install deps, create branch/worktree, commit/push or independently accept any work you authored. For this review only, follow maintenance docs-only gates. Do NOT invoke historical Phase 15 audit mode to produce a fake current audit.

Adversarially challenge the package against real repo contents and current OpenAI documentation. Focus on contradictions, authority drift, missing completeness proof, unenforceable steps, source vs design confusion, incorrect role routing, tests that cannot actually prove expected behavior, accidental unauthorized remediations and overengineering. Flag concrete reproducible defects with exact file/line/impact and smallest correction, but DO NOT generate style-only findings to inflate the count. Treat model/version and hooks claims as NOT_VERIFIED unless actual runtime tested.

Return:
- Verdict: READY_FOR_OWNER_COMMIT | CHANGES_REQUESTED | BLOCKED_REVIEW, for DOCS_ONLY_PREPARATION only.
- Baseline and inspection scope with actual SHAs.
- Specific findings NRP-PH-## with severity, exact paths/lines, evidence, reproduction and correction.
- NRP-A01..A23 acceptance matrix with PASS/FINDING/NOT_VERIFIED/N/A.
- Exact checks actually run and unverified items; no fabricated test results.
- Next step with Owner/Codex responsibility and permission boundary.

If READY_FOR_OWNER_COMMIT, STOP. Only the Owner can stage/commit/push and then grant a separate exact read-only H0 audit authorization. You are not permitted to start H0, H1, D0, B0 or implementation from this review.
```
