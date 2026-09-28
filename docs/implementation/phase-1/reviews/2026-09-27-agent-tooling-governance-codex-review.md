# Codex Final Review — Agent Tooling and Governance

- Date: 2026-09-27
- Reviewer: Codex
- Scope: Governance/tooling-only working-tree changes after frozen Backend Phase 1, including custom agents, engineering skills, workflow routing, Graphify scripts, Antigravity permissions, the repository safety hook, and Phase 1 freeze/archive documentation
- Baseline / working tree: `main` at `b7f1b4bf35eb7c80ec0e06ef0f00e376f7d6afac` plus the current uncommitted governance/tooling changes
- Test evidence: No new retained execution result was supplied for the expanded `.agents/hooks/test_repository_safety.py` suite. The existing Phase 0 evidence records the previous 10-test version and does not cover `permissionOverrides`. No production, backend test, Flyway, Maven, frozen DBML, or module-boundary file changed, so Codex did not rerun the Maven business suite.

## Findings

### Critical

None.

### High

#### H-1 — Prefix-wide Docker Compose grants permit destructive or publishing commands outside the sandbox

- Files: `.agents/hooks/repository_safety.py:351`, `.agents/hooks/repository_safety.py:356`, `scripts/configure-antigravity.ps1:85`, `scripts/configure-antigravity.ps1:89`, `docs/implementation/antigravity-cli-permissions.md:24`, `.agents/hooks/test_repository_safety.py:145`
- Observed problem: Every command whose first two tokens are `docker compose` receives `command(docker compose)` and `unsandboxed(docker compose)`. The settings helper persists the same two prefix rules. Antigravity documents `command(...)` and `unsandboxed(...)` as literal token-prefix matches, and an `unsandboxed` match executes on the host without prompting. The hook only gates `docker system prune` and `docker volume prune`; therefore `docker compose down -v`, `docker compose rm -f`, `docker compose push`, and similar commands are evaluated as allowed and receive the broad host-execution override.
- Consequence: A command can remove Compose volumes/resources or publish images with host privileges without owner confirmation, contradicting the slice's claim that overrides are narrow and already safe.
- Required correction: Remove the broad Compose prefix grants from emitted and persistent permissions. Allow only exact, read-only Compose operations that require Docker-daemon access; ensure destructive/publishing Compose operations receive no override and require explicit owner confirmation. Add regression cases covering the final hook decision/JSON as well as the helper output.
- Primary documentation: [Antigravity permissions use literal token-prefix matching](https://antigravity.google/docs/permissions), and [unsandboxed matches execute outside containment](https://antigravity.google/docs/cli/sandbox).

#### H-2 — Reusable handoff skills still direct agents to reopen frozen Phase 1

- Files: `.agents/skills/codex-create-handoff/SKILL.md:19`, `.agents/skills/codex-create-handoff/SKILL.md:29`, `.agents/skills/antigravity-implement-handoff/SKILL.md:66`, `.agents/skills/codex-final-review/SKILL.md:63`, `docs/implementation/handoffs/ACTIVE.md:3`
- Observed problem: The repository correctly has `NO_ACTIVE_HANDOFF` and declares Phase 1 complete/frozen, but `codex-create-handoff` still identifies Phase 1 as the current phase and mandates a new handoff for its old skeleton targets. The reusable implementation and review skills also retain unconditional Phase 1-specific directions.
- Consequence: Invoking the documented next-slice workflow can recreate already-completed work or silently reopen a frozen baseline, violating the active-handoff and freeze rules.
- Required correction: Make the skills derive scope from an owner-approved active phase. When none exists, `codex-create-handoff` must stop and request scope rather than default to Phase 1. Remove or clearly mark Phase 1 instructions as historical/non-executable in reusable skills.

### Medium

#### M-1 — The claimed Graphify move and Phase 1 freeze documentation are incomplete

- Files: `.agents/setup-graphify.ps1`, `.agents/refresh-graphify.ps1`, `scripts/setup-graphify.ps1`, `scripts/refresh-graphify.ps1`, `.codex/README.md:22`, `docs/implementation/phase-1/README.md:60`, `docs/implementation/phase-1/README.md:142`, `docs/implementation/phase-1/implementation-targets.md:5`, `docs/implementation/README.md:9`
- Observed problem: The new root scripts are byte-identical copies while the tracked `.agents/` scripts remain, so there are two canonical-looking locations despite the summary saying they were moved. Live Codex/phase documentation still points to `.agents/`. The implementation index still lists Phase 1 as active/awaiting owner commit, the completed phase document still says its targets await owner commit, and the historical target list says the implemented files contain unresolved TODO contracts.
- Consequence: Setup instructions and phase state have competing sources of truth, undermining the slice's reuse/consistency goals and confusing the next workflow invocation.
- Required correction: Complete the move with one canonical script location, update live references, preserve historical evidence wording where appropriate, and synchronize all Phase 1 indexes/history with its committed frozen state.

#### M-2 — The changed safety hook has no current execution evidence

- Files: `.agents/hooks/test_repository_safety.py:138`, `docs/implementation/phase-0/governance-test-evidence.md:24`, `docs/implementation/agent-tooling-change-summary.md`
- Observed problem: The hook suite now contains an eleventh test for permission overrides, but the only retained evidence is the earlier 10-test Phase 0 run. The new test checks helper subsets only and does not prove the final emitted hook JSON or unsafe Compose exclusions.
- Consequence: The security-sensitive permission change has neither adequate regression coverage nor a supplied passing result.
- Required correction: Add end-to-end hook-output assertions and unsafe Compose cases, run the focused Python suite, and retain exact current evidence in a tooling-specific evidence record. A Maven rerun is unnecessary while backend production/test/build/database files remain unchanged.

### Low

None.

## Accepted Areas

- The custom-agent paths and YAML frontmatter fields match Antigravity's documented custom-subagent schema; `backend-implementer` and the read-only `architecture-auditor` have appropriately scoped tools and sandbox policies. See [Google Antigravity custom subagents](https://antigravity.google/docs/cli/subagents).
- The Java/Spring, persistence, modular-architecture, testing, reuse, and pattern-selection skills are concise, preserve module ownership, and discourage speculative abstraction.
- The active business handoff is correctly cleared, and no frozen Phase 1 production/test/database/build artifact is modified by this slice.
- The Antigravity settings path and `permissionOverrides` output field are supported by the current official CLI/hook documentation. See [hooks](https://antigravity.google/docs/hooks) and [CLI settings](https://antigravity.google/docs/cli-using).

## Residual Risks / Questions

- Windows permission behavior is documented upstream as transitional. Keep persistent grants minimal and treat the repository hook as defense in depth rather than a substitute for narrow permission resources.
- `command(mvn)` remains a broad prefix, but Maven is intentionally kept sandboxed and the hook separately gates the `deploy` phase. Future plugin-execution needs should not justify an unsandboxed Maven wildcard.

## Final Review Status

`CHANGES_REQUESTED`

Return the governance/tooling slice for the checklist in `docs/implementation/agent-tooling-change-summary.md`. Backend Phase 1 remains complete/frozen and is not reopened. No commit message is provided while blocking findings remain.

> This status is a governance review result, not an automated commit/push action.

## Re-review — 2026-09-27

### Resolved

- H-1: Docker Compose permissions are now narrow. The settings helper removes the two legacy broad prefixes,
  persists only exact read-only Compose grants, and the hook emits host overrides only for `ps`, `logs`, `images`,
  and `top`. The retained focused evidence records 13 passing tests, including final hook JSON and state-changing,
  destructive, publishing, and execution cases that require confirmation without overrides.
- H-2: Reusable handoff skills now derive work from an owner-approved current scope and stop when no valid active
  scope exists; they no longer default to frozen Phase 1.
- M-2: Current focused hook evidence is retained in
  `docs/implementation/agent-tooling-governance-test-evidence.md` with exit status `0` and all 13 tests passing.
- The live Phase 1 indexes and workflow text now consistently describe Phase 1 as complete/frozen.

### Remaining finding

#### M-1 — The Graphify move still leaves two live copies

- Files: `.agents/setup-graphify.ps1`, `.agents/refresh-graphify.ps1`, `scripts/setup-graphify.ps1`,
  `scripts/refresh-graphify.ps1`
- Observed problem: All four paths still exist. Git reports the two `.agents/` paths as tracked and the two
  `scripts/` paths as new; SHA-256 comparison confirms each old/new pair is byte-identical. Live documentation now
  points to `scripts/`, but the scope summary incorrectly states that `scripts/` is already the only live location.
- Required correction: Remove the two legacy tracked `.agents/` copies, retain the root `scripts/` copies, and
  return the governance scope summary to `AWAITING CODEX GOVERNANCE REVIEW`. Historical review, evidence, and
  archived-handoff references do not need rewriting.

### Re-review status

`CHANGES_REQUESTED`

Backend Phase 1 remains complete/frozen. No production code, backend test, Flyway migration, Maven configuration,
or frozen architecture/database artifact was reopened or retested. No commit message is provided while the one
remaining governance finding is unresolved.

## Second re-review — 2026-09-27

`CHANGES_REQUESTED`

The M-1 finding remains unchanged. Filesystem and Git-index checks confirm that both tracked legacy files still
exist under `.agents/`, while the corresponding root `scripts/` files remain untracked. The governance summary
was returned to `AWAITING CODEX GOVERNANCE REVIEW` even though its Graphify-removal and review-return checklist
items remain unchecked; its status has therefore been synchronized back to `CHANGES REQUESTED`.

No other scope was reopened, no tests were rerun, and no commit message is provided.

## Final re-review — 2026-09-27

### Findings

None.

### Verification

- The tracked `.agents/setup-graphify.ps1` and `.agents/refresh-graphify.ps1` files are deleted.
- The replacement files under `scripts/` have the same Git blob identities as the deleted files.
- Live documentation routes Graphify setup and refresh through `scripts/`; remaining legacy-path references are
  confined to historical review, evidence, and archived-handoff records.
- The working-tree scope contains no backend production/test source, Maven build, Flyway, frozen database,
  architecture, or ADR changes.
- Previously retained hook evidence still records all 13 focused tests passing. Tests were not rerun during this
  review-only pass.

### Final status

`READY FOR OWNER COMMIT`

Backend Phase 1 remains complete/frozen. Codex did not commit, push, tag, or publish any change.
