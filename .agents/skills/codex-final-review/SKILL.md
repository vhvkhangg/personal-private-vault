---
name: codex-final-review
description: Perform the final pre-commit review after Antigravity testing, verify architecture/correctness/security/docs, and record findings in docs/reviews without editing implementation code.
---

# Codex Final Review

In Codex CLI, invoke this skill with `$codex-final-review` (or select it from `/skills`). Codex does not create a `/codex-final-review` slash command from a skill name.

Use this skill for the formal review immediately before the repository owner commits/pushes.

## Default mode

Review-only. Do not edit production code or tests unless the owner explicitly converts the task from review to implementation.

Do not rerun tests by default. Review the Antigravity test evidence supplied in the repository/conversation.

## Review order

1. Read the relevant `AGENTS.md` files.
2. Identify the implementation slice and changed files.
3. Read the applicable ADRs, module dependency matrix, and frozen database baseline if persistence changed.
4. Review for:
   - correctness and business-rule gaps;
   - module-boundary/ownership violations;
   - API contract consistency and validation;
   - transaction/persistence correctness;
   - security and sensitive-data leakage;
   - error handling and logging;
   - test quality and missing high-value cases;
   - stale or inconsistent documentation.
5. Give findings concrete severity:
   - Critical
   - High
   - Medium
   - Low
6. Each finding should include:
   - file/path and line(s) when available;
   - observed problem;
   - concrete consequence;
   - recommended correction.
7. Separate confirmed defects from uncertain risks/questions.
8. If no blocking issue exists, state that explicitly rather than inventing findings.

## Review log

Create a Markdown review record under `docs/reviews/` using the template in `assets/review-template.md`.

Name it:

`YYYY-MM-DD-<short-scope>-codex-review.md`

The log should include the Antigravity test command/result if available.

Do not commit, push, tag, or create/merge a pull request.
