# Phase 10 final re-review — scope conflict

- Date: 2026-10-01
- Handoff: `phase-10-feed-importdata-foundation`
- Outcome: **CHANGES_REQUESTED**
- Review stopped at the frozen-baseline/scope gate; this is not functional acceptance of the latest remediation.

## F7 — High — Remediation changes frozen Knowledge and shared serialization outside the approved handoff

The handoff explicitly prohibits modifying other modules' production code/public APIs and requires stopping
when frozen-baseline changes are necessary. The latest implementation nevertheless changes:

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/information/internal/application/InformationItemService.java:146`:
  replaces `trimOrNull(rawContentMarkdown)` with nonblank verbatim retention. This changes canonical Information
  create/update semantics for all callers, not just ImportData. Phase 8 Knowledge is frozen.
- `backend/src/main/resources/application.yml:19`: registers an application-wide Hibernate JSON format mapper.
  The new root-level `VaultJsonFormatMapper.java` configures untyped floating values as BigDecimal across JSONB
  mappings, including frozen modules, rather than only Phase 10 payloads.
- `application.yml:28`: adds application-wide Jackson decimal deserialization configuration, also outside the
  approved module-local parser scope.

The implementation may have a sound reason to preserve content/precision, but green tests do not authorize
these scope changes. Earlier review requests to preserve persisted content/precision did not revoke the active
handoff's frozen-module restrictions. If satisfying those requirements needs frozen/shared changes, that conflict
must be resolved with the owner before implementation continues.

## Required correction / decision

Do not commit this package. Choose one path:

1. Keep Phase 10 scope unchanged: return to Antigravity `/antigravity-implement-handoff` to remove only its
   unauthorized Knowledge/shared-configuration additions, preserving unrelated owner work, and investigate an
   owner-local solution. If no in-scope solution meets the approved end-to-end contract, report the conflict;
   do not silently weaken requirements or duplicate target behavior in ImportData.
2. Owner explicitly approves a narrow scope expansion for these baseline changes: document exact targets,
   behavior/type impact, rollback/compatibility expectations and regression requirements; synchronize the active
   handoff and affected canonical docs. Apply ADR/architecture-change governance where the approved decision
   affects a frozen architectural baseline. Only then may Antigravity implement/test that expanded slice and
   return it for final review.

The prior F2/F3/F5/F6 remediation is implementer-reported, not independently closed by this scope-only review.
After the scope decision and remediation, rerun the complete final-review dimensions and required verification.

## Evidence / limits

- Read ACTIVE first: valid `IMPLEMENTED_AWAITING_CODEX_REVIEW` gate.
- Inspected `git diff --name-only`, `git status --short`, exact tracked Knowledge/configuration diffs, and the
  newly added mapper source. The scope conflict is directly evidenced by these changes.
- Implementer reports 685 passing tests; Codex did not rerun the full build in this invocation because the
  handoff/root contract requires stopping at this conflict. No current independent test-pass claim is made.
- No production edits, reverts, commits, pushes or publishing actions by Codex. Only review/status docs changed.
- `modular-monolith-architecture` guidance and the handoff/root frozen-baseline contract require the pause;
  this review does not reopen Phase 8 or approve shared serialization changes.

## Next step

Repeat invocation on 2026-10-01: the exact Knowledge/configuration changes and new root mapper remain present;
ACTIVE had been reset to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, but no explicit owner scope approval or expanded
handoff was provided. F7 is unchanged; ACTIVE restored to `CHANGES_REQUESTED`. No full build rerun or production edits.

Resolve the owner scope decision above, then return the approved slice to Antigravity
`/antigravity-implement-handoff`. Return ACTIVE to `IMPLEMENTED_AWAITING_CODEX_REVIEW` only after remediation
and truthful evidence; rerun `$codex-final-review`. No commit message is issued.
