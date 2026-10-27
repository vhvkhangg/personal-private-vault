# Workflow interruption recovery prompts (English)

**Scope:** copy/paste recovery messages for Codex CLI and Antigravity when a **previously authorized** skill run was interrupted. These are convenience prompts, **not substitute skills or new authorization**. Normal uninterrupted workflow needs only the named skill command. Read [Owner policy](../current-owner-delivery-policy.md) and [current ACTIVE](../../implementation/handoffs/ACTIVE.md).

## How to use

1. Fix a genuine CLI startup problem before submitting a resume prompt. Restarting the model does not fix malformed TOML or a broken safety hook.
2. Open the **same session** only when that preserves implementer/reviewer independence; otherwise open a separate session and instruct it to check existing artifacts. A Codex implementer must never independently review its own takeover code.
3. Copy the relevant prompt below in full. No custom task prompt is needed for normal workflow; you use these **only after interruption**.
4. Keep interruption retries **within the same approved slice and existing correction/attempt counter**; never silently increment or reset based on a crash.

The shared [Documentation synchronization contract](../documentation-synchronization.md) applies to resumed runs through [COMMON](./COMMON.md); recover the prior documentation checkpoint without starting repository-wide cleanup.

## Prompts

| Skill | Resume prompt | Role |
| --- | --- | --- |
| `antigravity-implement-handoff` | [antigravity-implement-handoff](./antigravity-implement-handoff.md) | Antigravity |
| `antigravity-self-review` | [antigravity-self-review](./antigravity-self-review.md) | Antigravity |
| `antigravity-test-slice` | [antigravity-test-slice](./antigravity-test-slice.md) | Antigravity |
| `codex-backend-audit` | [codex-backend-audit](./codex-backend-audit.md) | Codex |
| `codex-create-handoff` | [codex-create-handoff](./codex-create-handoff.md) | Codex |
| `codex-final-review` | [codex-final-review](./codex-final-review.md) | Codex |
| `codex-implementation-takeover` | [codex-implementation-takeover](./codex-implementation-takeover.md) | Codex |
| `codex-milestone-review` | [codex-milestone-review](./codex-milestone-review.md) | Codex |
| `codex-pre-handoff-review` | [codex-pre-handoff-review](./codex-pre-handoff-review.md) | Codex |
| `codex-review-coverage` | [codex-review-coverage](./codex-review-coverage.md) | Codex |

**Note:** `codex-review-coverage` is a helper called by another Codex review; its recovery prompt resumes the parent, not an unauthorized independent gate. `antigravity-self-review` resumes implementer QA and returns to its parent. `codex-backend-audit` is the historical Phase 15 route, not the future RAG/combined audits.

[Common recovery principles](./COMMON.md) distinguish the command result, review evidence, and current execution gate. Avoid duplicating these longer principles in every skill's startup context.
