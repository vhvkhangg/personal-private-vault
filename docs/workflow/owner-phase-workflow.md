# Owner Phase Workflow

This is the owner's checklist between implementation phases.

## 1. Finish the current phase

Normal implementation loop:

```text
Codex $codex-create-handoff
    ↓
Antigravity /antigravity-implement-handoff
    ↓
Codex $codex-final-review
    ↓
CHANGES_REQUESTED → Antigravity remediation → Codex re-review
or
READY FOR OWNER COMMIT
    ↓
Owner commit + push
```

Do not immediately create the next handoff.

## 2. Ask ChatGPT to close the phase and prepare the next one

After committing/pushing Phase N, send ChatGPT the latest repository package and ask:

```text
Phase N has been committed/pushed.
Close/freeze Phase N, consolidate its docs/reviews/evidence, update roadmap/status,
and prepare Phase N+1 docs plus only the skills/agents/rules/hooks that are actually needed.
Do not create the Phase N+1 implementation handoff.
```

ChatGPT should:

- mark Phase N `COMPLETE — FROZEN`;
- archive its completed handoff under `docs/implementation/phase-N/`;
- keep phase test/build evidence inside that phase folder;
- keep phase Codex reviews under `docs/implementation/phase-N/reviews/`;
- clear `docs/implementation/handoffs/ACTIVE.md`;
- remove/synchronize stale status text and duplicate docs;
- update `docs/roadmap.md`;
- create/update Phase N+1 scope and `preparation-review.md`;
- add/update phase-specific skills/rules/module `AGENTS.md` only when useful;
- reuse existing agents rather than creating one agent per phase;
- change hooks only for a real new command/security requirement;
- not write next-phase production code.

## 3. Extra milestone review every 3 implementation phases (historical cadence through Phase 12)

Phase 0 does not count.

After Phase **3, 6, 9, and 12** is committed/pushed, run this additional review before the next handoff:

```text
$codex-milestone-review
```

This reviews the latest three implementation phases together for architecture drift, duplicate business logic,
SOLID/cohesion/coupling, pattern misuse/overengineering, security, persistence/concurrency, concrete performance
hazards, test health, documentation drift, and accumulated technical debt.

The milestone review does not silently reopen frozen phases. Blocking findings require an owner-approved
maintenance slice.

At a milestone boundary, ChatGPT may already prepare the next phase docs/tooling, but **do not run the next
`$codex-pre-handoff-review` until the milestone returns `MILESTONE_READY`**.

### If milestone review returns `MILESTONE_READY`

1. owner commits/pushes the milestone review/status-document changes;
2. send ChatGPT the latest repository package and say:

```text
The milestone is MILESTONE_READY and I committed/pushed the milestone review/status changes.
Please close/reset any completed maintenance handoff, archive it if needed, synchronize roadmap/status/docs,
and unblock the already-prepared next phase. Do not create the next implementation handoff.
```

3. ChatGPT verifies `ACTIVE.md = NO_ACTIVE_HANDOFF`, closes stale maintenance status, and sets the next prepared
   phase to `AWAITING CODEX PRE-HANDOFF REVIEW`;
4. only then run the next phase `$codex-pre-handoff-review`.

### If milestone review returns `CHANGES_REQUESTED`

When Codex requires frozen-phase implementation fixes:

1. owner explicitly approves a narrow maintenance scope;
2. ChatGPT prepares that scope under `docs/implementation/maintenance/`;
3. Codex runs `$codex-create-handoff` for the maintenance slice;
4. Antigravity implements/tests it;
5. Codex runs `$codex-final-review`;
6. owner commits/pushes after `READY FOR OWNER COMMIT`;
7. rerun `$codex-milestone-review`;
8. when it returns `MILESTONE_READY`, commit/push the milestone review/status changes;
9. send the latest package to ChatGPT for post-milestone closeout/status synchronization;
10. only after ChatGPT confirms the next phase is unblocked, run `$codex-pre-handoff-review`.

## 4. Phase 15 comprehensive backend audit exception

Phase 15 is not a normal feature implementation phase and intentionally supersedes the former post-Phase-15
milestone. After ChatGPT prepares Phase 15, first run:

```text
$codex-pre-handoff-review
```

For Phase 15, success is `READY FOR AUDIT` rather than `READY FOR HANDOFF`. Commit/push the accepted preparation,
then run:

```text
$codex-backend-audit
```

The audit covers all non-RAG backend work from Phases 0–14. It may return:

- `OWNER_DECISION_REQUIRED` — a finding would change a frozen baseline; owner decides before any handoff;
- `REMEDIATION_REQUIRED` — actionable authorized findings exist and Codex creates one bounded remediation handoff;
- `BACKEND_AUDIT_READY` — no unresolved actionable findings remain.

When remediation is required:

```text
/antigravity-implement-handoff
$codex-final-review
$codex-backend-audit
```

For Phase 15, a handoff-specific `READY FOR OWNER COMMIT` from `$codex-final-review` is not the final commit gate;
rerun `$codex-backend-audit` first. Only `BACKEND_AUDIT_READY` permits the owner to commit/push the complete Phase 15
slice. Phase 16/17 preparation remains owner-gated.

## 5. Codex reviews normal next-phase preparation before handoff creation

For normal implementation phases other than Phase 15, after ChatGPT preparation run:

```text
$codex-pre-handoff-review
```

Codex reviews:

- next-phase scope/non-goals;
- architecture/dependencies/frozen-baseline compatibility;
- docs and roadmap;
- added/changed skills;
- added/changed agents;
- added/changed rules;
- added/changed hooks;
- module `AGENTS.md`;
- test/evidence expectations;
- duplication/tooling overengineering.

If Codex returns `CHANGES_REQUESTED`, give the findings/package back to ChatGPT for preparation remediation.

If Codex returns:

```text
READY FOR HANDOFF
```

commit/push the preparation/docs/tooling slice using Codex's one Conventional Commit message.

## 6. Only then create the implementation handoff

After the preparation commit/push:

```text
$codex-create-handoff
```

Then:

```text
/agents
→ backend-implementer

/antigravity-implement-handoff
```

## Quick checklist

Use exactly one branch below. **Do not continue from the Phase 15 branch into the normal implementation branch.**

### Normal implementation phases (not Phase 15)

```text
[ ] Final code review = READY FOR OWNER COMMIT
[ ] Commit/push current implementation phase
[ ] Ask ChatGPT: close current phase + prepare next phase
[ ] If closing Phase 3/6/9/12: run $codex-milestone-review
[ ] If MILESTONE_READY: commit/push milestone docs, then give latest package to ChatGPT for post-milestone sync
[ ] Confirm ACTIVE.md = NO_ACTIVE_HANDOFF and next phase = AWAITING CODEX PRE-HANDOFF REVIEW
[ ] Run $codex-pre-handoff-review
[ ] Resolve preparation findings until READY FOR HANDOFF
[ ] Commit/push accepted preparation
[ ] Run $codex-create-handoff
[ ] Run Antigravity implementation
[ ] Run $codex-final-review until READY FOR OWNER COMMIT
[ ] Commit/push implementation
```

### Phase 15 audit-first path only

```text
[ ] Confirm Phase 14 = COMPLETE — FROZEN and ACTIVE.md = NO_ACTIVE_HANDOFF
[ ] Run $codex-pre-handoff-review
[ ] Resolve preparation findings until READY FOR AUDIT
[ ] Commit/push accepted Phase 15 preparation
[ ] Run $codex-backend-audit

[ ] If OWNER_DECISION_REQUIRED:
    record the owner decision without creating/implementing an unauthorized handoff
    rerun $codex-backend-audit

[ ] If REMEDIATION_REQUIRED:
    use only the remediation handoff created by $codex-backend-audit
    run /antigravity-implement-handoff
    run $codex-final-review until the handoff-specific result is READY FOR OWNER COMMIT
    DO NOT COMMIT YET
    rerun $codex-backend-audit as the mandatory repository-wide closure audit

[ ] If the closure audit finds new/remnant actionable findings:
    continue the same Phase 15 remediation handoff
    repeat Antigravity remediation → $codex-final-review → $codex-backend-audit

[ ] Only when $codex-backend-audit returns BACKEND_AUDIT_READY:
    commit/push the complete Phase 15 audit/remediation slice
    give ChatGPT the latest package for Phase 15 closeout only

[ ] Do not run normal $codex-create-handoff for Phase 15 preparation
[ ] Do not prepare Phase 16/17 until the owner separately authorizes it
```
