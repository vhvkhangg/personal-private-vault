# Copy/paste — future authorized H0 harness read-only audit (new Codex session)

> **DO NOT USE THIS PROMPT BEFORE Owner explicitly ratifies and publishes the H0 authorization.** The mere presence of this file is not permission to execute. If the Owner contract remains draft, STOP with `H0_EXECUTION_NOT_AUTHORIZED`.

```text
ROLE: Codex BF26-NR-H0 READ-ONLY HARNESS AUDITOR, in a fresh session. You are not the H1 implementer, H0 independent reviewer or a historical Phase 15 auditor.

Repo: https://github.com/vhvkhangg/personal-private-vault
Canonical authorization: docs/implementation/maintenance/2026-11-15-bf26-nr-h0-harness-readonly-audit/owner-authorization-h0.md
Bounded H0 handoff: docs/implementation/maintenance/2026-11-15-bf26-nr-h0-harness-readonly-audit/h0-readonly-handoff.md
Parent preparation: docs/implementation/maintenance/2026-11-15-bf26-nr-audit-preparation/

ENTRY GATE: Verify a distinct, explicit Owner ratification for BF26-NR-H0-HARNESS-READONLY is recorded in a canonical document actually published on current main, and matches the independently reviewed scope. Verify local HEAD, origin/main, all untracked/ignored harness-related inputs and ACTIVE. For remote main, first verify origin points to the approved vhvkhangg/personal-private-vault repository, then use the SOLE permitted network command `git ls-remote --heads origin refs/heads/main` (read-only metadata, no fetch/pull/local ref update). If remote SHA cannot be verified, STOP / BLOCKED_H0; cached origin/main is not a substitute. If the authorization is still draft, ambiguous, drifted or not published, STOP; do not treat this prompt, README, EMPTY ACTIVE or preflight READY as authority.

BOOTSTRAP: Follow the published H0 contract and handoff directly. The current .agents/skills/codex-backend-audit/SKILL.md is a historical Phase 15-only skill: READ for evidence but DO NOT INVOKE. Do not create a new audit skill or edit any existing skill; that is future H1 only. Do not call codex-create-handoff to change ACTIVE for a read-only H0.

SCOPE: Full .agents/, .codex/, all exact-basename AGENTS.md, relevant scripts, rules, hooks, docs/workflow and dependent governance, actual Codex model/trust/sandbox/approval/hook configuration, and inspect delivery/ behavior if it exists. Obtain complete manifest and instruction/command/reference graph, identify true issue vs optional simplification, and record evidence-limited status. H01-H13 and H-T01..20 all need accountable dispositions. H1-only live tests cannot be PASS in H0.

SAFETY: No changes to tracked repository source, harness, AGENTS, current governance, policy, ACTIVE, database or user data. No git add/commit/push, PR/release, reset/clean/checkout/stash, branch/worktree, tool installs, provider or other network calls, git fetch/pull, Maven/RAG execution, production service startup or global Codex config/trust changes. The ONLY network exception is the exact baseline `git ls-remote --heads origin refs/heads/main` read against the verified approved origin; no other remote/host or credential disclosures. Any safe probe that might write must occur in an explicitly authorized disposable fixture outside the repository; never use the real checkout to test forbidden command behavior. Avoid secrets and preserve logs.

ONLY permitted H0 write outputs after grant: new h0-audit-report.md, h0-file-manifest.csv, h0-scenario-results.md, h0-h1-proposal.md, h0-evidence-index.md in the H0 docs directory; optional new ignored receipts after proving ignore status. No overwrites without separate approval. Report the exact output filenames, raw test results and hashes.

END: H0_REPORT_SUBMITTED / INDEPENDENT_REVIEW_PENDING, BLOCKED_H0 or OWNER_DECISION_REQUIRED. The next session is a SEPARATE Codex reviewer that challenges the report and evidence; only then can Owner ratify a bounded H1 change allowlist. No auto implementation, H1 handoff, D0/B0, final audit qualification or publication.
```
