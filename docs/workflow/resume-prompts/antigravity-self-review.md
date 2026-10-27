# Resume prompt — `antigravity-self-review`

**Application:** Antigravity  
**Invocation:** `/antigravity-self-review`  
**Use only after an interruption.** This file is a prompt for the existing skill, not a new skill or permission.

## Copy/paste prompt (English)

```text
Resume the mandatory implementer self-review of the current approved handoff, then return findings to the invoking Antigravity implementation/test workflow.

First verify the latest local HEAD, `AGENTS.md`, `docs/implementation/handoffs/ACTIVE.md`, the applicable Owner-approved phase/maintenance scope, the exact handoff/review, and any partial changes or receipts from the interrupted session. Read the parent/source files rather than trusting the prior session's narration. Classify which steps are completed, pending, invalidated by drift, or blocked.

Read `docs/workflow/resume-prompts/COMMON.md` and `docs/workflow/documentation-synchronization.md` from the repository root. Recover the last valid scope-specific Docs Sync Check, inspect only documentation changed or invalidated since that checkpoint, and record UPDATED, ALREADY_CONSISTENT, DEFERRED_OUT_OF_SCOPE, BLOCKED_OWNER_DECISION, or NOT_VERIFIED as applicable. Never use this resume prompt to authorize repository-wide cleanup B.

Continue **only** pending authorized work. Reuse verified evidence without overwriting historical logs. If a command was interrupted, confirm actual exit/cleanup before rerunning through the approved watchdog into a fresh evidence directory. Keep the same slice/remediation/Owner-authorized takeover attempt counts unless an actual completed and independently reviewed corrective round advances them.

Preserve the current skill's role and boundaries; do not implement during a review, never perform independent acceptance of your own implementation, do not stage/commit/push/tag/PR, and do not open a phase or handoff from an old TODO. If a CLI safety hook, project trust, config or permission guard is broken, STOP for diagnostics rather than bypassing it.

Apply this **scope-specific next-action routing**: Report to the invoking Antigravity implementation/test skill. Self-review is not independent acceptance. Do not route directly to Owner commit; the parent must finish its handback before separate Codex `$codex-final-review`.

When finished, report only: outcome/disposition; exact artifact path(s); focused verification with actual results; pending findings and counter; and **Next step: [application/person] | [exact skill or Owner action] | [scope + canonical artifact] | [gate/prerequisite] | [remediation/takeover counter or N/A]**. If blocked, provide the exact blocker and do not claim READY.
```

Read the [common recovery contract](./COMMON.md) for details. No new architecture, dependency, test quota, tool, daemon or audit is authorized by this prompt.
