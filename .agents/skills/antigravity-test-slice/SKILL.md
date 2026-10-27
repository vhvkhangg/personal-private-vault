---
name: antigravity-test-slice
description: Run a focused regression/test-only pass for an already implemented handoff without expanding implementation scope.
---

# Antigravity Test Slice

**Current policy:** [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md). A test-only correction against blocking findings counts as one of up to **three** Antigravity remediation + independent Codex re-review rounds on the active slice. Self-review is mandatory but not counted. CRITICAL/HIGH/MEDIUM block; LOW/INFO are logged for milestone/audit. Do not rerun unrelated full suites merely to increase green counts.


This is a secondary/test-only skill. The primary development workflow is `/antigravity-implement-handoff`.

Use this skill when the active handoff is already implemented and only a targeted regression pass is requested.

1. Read `docs/implementation/handoffs/ACTIVE.md`.
2. Add tests only if the handoff/review explicitly requires missing coverage.
3. Do not expand production scope.
4. Use PostgreSQL Testcontainers for persistence.
5. Run the requested focused/final command.
6. Preserve command/result evidence.
7. If a production defect is found, report it or return to `/antigravity-implement-handoff` when the handoff status permits remediation.

Do not commit or push.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement.

- requested test-only remediation/evidence completed → tell the owner to rerun `$codex-final-review`;
- a production defect is discovered → tell the owner to return to `/antigravity-implement-handoff` when the active
  handoff permits production remediation, otherwise request Codex/owner scope clarification;
- blocked test execution → state the exact prerequisite needed before retrying.

Never leave the owner to infer the next workflow action.

For future Phase 18–22 RAG test-only cases, use `python-rag-coding` for Python fixtures and `bounded-command-execution` for long test calls; do not rerun full suites unchanged to obtain green. A test-only correction made by Antigravity after a `CHANGES_REQUESTED` review is a remediation round for the **per-slice three-round limit** and requires an independent re-review.

## Documentation synchronization checkpoint — Maintenance B3 amendment

Follow the shared [Documentation synchronization contract](../../../docs/workflow/documentation-synchronization.md) **at this skill's handoff/review boundary**, within Owner-approved scope and without a repository-wide cleanup. For test-only work, verify changed test results and evidence claims against impacted current documentation. Edit docs only when explicitly within approved test-only scope; otherwise report drift for ChatGPT/Owner without expanding the remediation round. Record the concise **Docs Sync Check** in the existing handback/review artifact (or identify why no docs are affected). Keep the **Next step** routed to the exact remaining documentation action when necessary.

## Forward next action and interruption recovery — Maintenance B3

At the end of **every completed, blocked or interrupted invocation**, output one concrete line with all fields:

`Next step: <application/person> | <exact skill command or Owner action> | <scope + canonical artifact> | <gate/prerequisite> | <remediation or takeover counter, if applicable>`

**Route for this skill:** For completed authorized test-only remediation: separate Codex `$codex-final-review`; for a production defect: return to `/antigravity-implement-handoff` only if ACTIVE and remediation budget permit; otherwise stop for Owner decision.

**Interrupted session:** the Owner may paste the English [resume prompt](../../../docs/workflow/resume-prompts/antigravity-test-slice.md). Resume only from inspected files/evidence, with the current approved scope and existing counter; capacity/tool errors do not authorize a new round or reset evidence. Do not overwrite logs or bypass native safety hooks. A standalone skill command (without a special prompt) remains sufficient for an uninterrupted run.
