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

## 3. Extra milestone review every 3 implementation phases

Phase 0 does not count.

After Phase **3, 6, 9, 12, and 15** is committed/pushed, run this additional review before the next handoff:

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

## 4. Codex reviews next-phase preparation before handoff creation

After ChatGPT preparation, run:

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

## 5. Only then create the implementation handoff

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

```text
[ ] Final code review = READY FOR OWNER COMMIT
[ ] Commit/push current implementation phase
[ ] Ask ChatGPT: close current phase + prepare next phase
[ ] If closing Phase 3/6/9/12/15: run $codex-milestone-review
[ ] If MILESTONE_READY: commit/push milestone docs, then give latest package to ChatGPT for post-milestone sync
[ ] Confirm ACTIVE.md = NO_ACTIVE_HANDOFF and next phase = AWAITING CODEX PRE-HANDOFF REVIEW
[ ] Run $codex-pre-handoff-review
[ ] Resolve preparation findings
[ ] Commit/push preparation after READY FOR HANDOFF
[ ] Run $codex-create-handoff
[ ] Run Antigravity implementation
```
