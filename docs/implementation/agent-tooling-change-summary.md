# Agent Tooling / Workflow Change Summary

Status: **COMPLETE — REVIEWED / COMMITTED (historical tooling slice)**

This slice is separate from the frozen Backend Phase 1 business implementation.

## Changed

- moved Graphify PowerShell scripts to the root `scripts/` directory;
- added the Antigravity Windows settings/configuration helper;
- added the `backend-implementer` and `architecture-auditor` workspace agents;
- added focused engineering skills for:
  - Java/Spring coding standards;
  - pragmatic SOLID design;
  - modular-monolith/Spring Modulith architecture;
  - JPA/Hibernate/PostgreSQL/Flyway persistence;
  - backend testing;
  - reuse and consistency / canonical business-rule ownership;
  - design-pattern selection with an anti-overengineering bias;
- added `code-reuse-and-patterns` conditional guidance so agents check for an existing canonical implementation before duplicating business behavior;
- updated Codex and Antigravity workflow skills so relevant engineering skills are explicitly used through progressive disclosure;
- updated the `backend-implementer` and `architecture-auditor` agents to route implementation/review work through the relevant engineering skills;
- added the canonical agent development workflow documentation;
- added safe-command `permissionOverrides` to the repository PreToolUse safety hook for common Maven, Java, Docker, Git-read, and read-oriented PowerShell commands;
- updated Antigravity Windows permission documentation and configuration guidance;
- froze Backend Phase 1 documentation and archived its completed implementation handoff;
- retained Graphify as an optional local code-navigation aid rather than an architecture source of truth.

## Not changed

- Backend Phase 1 production Java code;
- Backend Phase 1 tests;
- Flyway V1;
- `backend/pom.xml`;
- frozen DBML;
- frozen module boundaries;
- ADR decisions;
- architecture diagrams.

## Historical review scope

Run:

```text
$codex-final-review
```
as a governance/tooling-only review.
Codex should review:
- agent role separation and handoff workflow;
- custom-agent configuration;
- engineering-skill scope and routing;
- reuse/canonical-ownership guidance;
- design-pattern guidance and overengineering safeguards;
- Graphify integration and context-efficiency rules;
- Windows Antigravity permission guidance;
- repository safety-hook behavior and permission overrides;
- Phase 1 freeze/archive documentation consistency.
Do not reopen or re-review the frozen Backend Phase 1 business implementation unless this tooling/governance
diff unexpectedly modifies production code, tests, Flyway migrations, Maven build configuration, or frozen
architecture/database artifacts.
A full Maven Phase 1 test-suite rerun is not required solely for these agent/tooling/documentation changes.
Expected outcome
If no blocking governance/tooling findings remain, Codex should:
1. record the tooling review under `docs/implementation/phase-1/reviews/`;
2. return READY FOR OWNER COMMIT;
3. provide exactly one Conventional Commit message;
4. leave Backend Phase 1 in its existing COMPLETE — FROZEN state.
If blocking findings exist, Codex should report only the required governance/tooling remediation without reopening
the completed Phase 1 implementation scope.

## Codex remediation

- [x] Narrowed Docker Compose permissions. Broad Compose prefix grants are removed from emitted/persistent
  permissions; only exact read-only host operations (`ps`, `logs`, `images`, `top`) receive host overrides.
  State-changing/destructive/publishing Compose commands require owner confirmation and receive no override.
- [x] Extended safety regressions to validate helper behavior and final hook JSON:
  [`agent-tooling-governance-test-evidence.md`](agent-tooling-governance-test-evidence.md) — **13 tests PASS**,
  exit status `0`.
- [x] Made reusable workflow skills prospective. No skill defaults to frozen Phase 1; handoff creation requires
  owner-approved current scope and implementation/review stops when no valid scope exists.
- [x] Completed the Graphify move. The tracked legacy copies under `.agents/` are deleted, the byte-identical
  replacements live under `scripts/`, and live references use `scripts/...`. Historical review, evidence, and
  archived-handoff wording remains untouched.
- [x] Synchronized live Phase 1 docs/indexes with its committed `COMPLETE — FROZEN` state and removed stale
  unresolved-TODO/awaiting-owner-commit wording.
- [x] During remediation, returned this summary to **AWAITING CODEX GOVERNANCE REVIEW** for the required re-review.

## Historical final outcome

The final governance/tooling re-review returned **READY FOR OWNER COMMIT** and the slice was subsequently committed.
The retained record is
[`phase-1/reviews/2026-09-27-agent-tooling-governance-codex-review.md`](phase-1/reviews/2026-09-27-agent-tooling-governance-codex-review.md).

No governance review is pending for this historical slice.
