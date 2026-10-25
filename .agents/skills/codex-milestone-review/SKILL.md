---
name: codex-milestone-review
description: Independently review quality, architecture and accepted requirements after each three completed phases; legacy Backend 3/6/9/12 are historical, current RAG milestones after P19 and P22.
---

# Codex Milestone Review

Follow [current Owner policy](../../../docs/workflow/current-owner-delivery-policy.md). **Phase 19 is NOT AUTHORIZED / NOT STARTED**; this skill does not create work from a milestone schedule.

## Trigger and scope

- Historical Backend milestones following Phases 3, 6, 9, 12 remain complete and immutable; Phase 15 comprehensive Backend audit superseded a post-15 milestone.
- Future RAG milestone **after Phase 19**: aggregate **RAG Phases 17–19** only when 19 is Owner-closed/published.
- Future RAG milestone **after Phase 22**: aggregate **RAG Phases 20–22** only when 22 is Owner-closed/published.
- After all RAG phases, a **separately authorized RAG-wide audit** covers 17–22, followed by a **separately authorized RAG + Backend integration/repository-wide audit**. Do not treat a milestone review as either full audit.

Check the slice reviews/phase final reviews and current source for accumulated regressions, duplicated business rules, hard-coded owner-specific failures, real data-integrity/security issues, clean architecture/code, SOLID/DRY/KISS/YAGNI, resource leaks, missing regression coverage, docs/harness contradictions and approved user-visible behavior. Reuse unchanged hash-backed evidence. Keep the 29-category `codex-review-coverage` rubric proportional to changed/active code; do not invent 29 new test subsystems.

## Backlog and findings

Resolve any CRITICAL/HIGH/MEDIUM blocker before `MILESTONE_READY` through an approved narrow maintenance slice. Consolidate previously recorded LOW/INFO into **one prioritized correction pass** if they remain applicable; if there is no actionable correction, close the observation with evidence rather than generating pointless code. Do not silently change frozen requirements.

Write a dated record under the milestone-closing phase's `reviews/` and update its milestone status. Return `MILESTONE_READY` only when mandatory scope is verified and no blocker remains, otherwise `CHANGES_REQUESTED` or `OWNER_DECISION_REQUIRED`. Only Owner stages/commits/pushes. Next step must be one concrete Owner action; never self-start Phase 19 or any other phase.
