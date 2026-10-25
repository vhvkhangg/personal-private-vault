---
name: antigravity-self-review
description: Lightweight implementer self-check before independent Codex review, scoped to Owner-approved behavior, reuse, cleanliness, architecture, and focused test evidence.
---

# Antigravity Self-Review — before Codex

**Invoked automatically by** `/antigravity-implement-handoff` after implementation/testing and before independent Codex *slice* review. Not a remediation round and not independent acceptance. Owner's forward policy requires Critical/High/Medium blockers resolved, Low/Info listed once for milestone/audit; no speculative extra feature. After all slices, Codex performs one **phase final** review. See [current policy](../../../docs/workflow/current-owner-delivery-policy.md).


This is an implementer QA checklist, not independent acceptance and not permission for extra features.

1. Re-read exact Owner requirement and current executable handoff. For each requirement, point to an actual implemented behavior and executable test or mark NOT_VERIFIED.
2. Verify no unrequested features, generic platforms, extra schema/dependencies or speculative security mechanisms. Choose existing/reused code and the simplest design that preserves every approved behavior.
3. Inspect own diff for clean code/architecture: readable methods/names, cohesion, minimal interfaces, DRY, KISS, YAGNI, SOLID where useful, Spring Modulith ownership and correct Java/Python data boundaries; no unnecessary patterns.
4. Verify all changed-source paths and their callers; focused positive, negative, cancelled/error/cleanup cases at actual changed trust boundaries. Check true results/HTTP response, not only DTO or mocks where DB/native behavior matters.
5. Verify no secret/private payload logging, unsupported `.md` paths, frozen baseline modification, stale docs, broken links, obsolete files, whitespace problems.
6. Diagnose failing tests promptly; never run a failing full suite again unchanged for luck. Preserve receipt and honest status.
7. Summarize to Codex in <=12 short lines plus links to concise evidence: feature matrix, changed paths, focused tests, risks/NOT_VERIFIED. Do not dump raw command output into chat; preserve logs off-chat.
8. Stop for unresolved Owner decisions. No staging, commit, push, release or self-approval.
