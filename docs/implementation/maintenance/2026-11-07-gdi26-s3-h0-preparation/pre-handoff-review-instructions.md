# GDI26-S3-H0 — Codex independent pre-handoff review launcher

## Invocation

Use a **new independent Codex review session** from the trusted repository root after applying this **docs-only** package:

```text
$codex-pre-handoff-review

Review the Owner-authorized GDI26-S3-H0 FRONTEND HARNESS PREPARATION at:
  docs/implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/README.md

Review mode: explicitly authorized MAINTENANCE PREPARATION (not Phase 16 and not H1).
Current expected main baseline: d8da1677f6700c308aec750dcc714fc8c3983185.
ACTIVE must stay NO_ACTIVE_HANDOFF.

Read Owner D00–D10 and this preparation's D11–D22, existing root/scoped
AGENTS.md, .agents agents/rules/skills/hooks, .codex owner-git guard,
Owner Delivery Policy, current S1/S2 backend HTTP/OAuth contracts, and
frontend architectural baseline. Verify actual HEAD and git diff.

Review exact candidate H0 harness inventory, scope/minimal reuse,
external skill provenance and license/installer risks, BFF security,
callback cookie binding, upload idempotency, accessible glassmorphism,
review/evidence plan and preservation of frozen contracts. Apply
KEEP/REUSE/SIMPLIFY/REMOVE/DEFER and classify gaps by severity.

CRITICAL: H0 must create NO frontend/ application. H1 implementation
is BLOCKED until H0 publication + independently accepted/published H1
preparation/executable handoff AND explicit Owner DESIGN_APPROVED for a
specific inspected design artifact/version. Owner design preferences,
mockups, agent feedback and automated screenshots alone do not satisfy
DESIGN_APPROVED. Only Owner may approve visuals; no fixed revision cap.

Do not write source, scaffold Next.js, install skills/dependencies,
change .codex/config.toml, alter trust/hooks globally, create an
executable handoff, enable Google/Feed/RAG, or stage/commit/push.

Write an independent dated review with findings and clear result:
CHANGES_REQUESTED or READY_FOR_HANDOFF. Current H0 tests NOT_RUN;
future H0/H1 tests are plans, not accepted results. End with the exact
Owner next step. No auto-H1 or auto-design approval.
```

## Expected response states

- `CHANGES_REQUESTED`: correct only review-supported preparation Markdown and rerun independent pre-handoff review; preserve dated original reviewer result.
- `OWNER_DECISION_REQUIRED`: identify an actual unapproved product/security choice, with alternatives, before handoff. Avoid asking Owner to approve ordinary implementation details.
- `READY_FOR_HANDOFF`: Owner manually stages/commits/pushes the independently accepted **preparation docs**. After actual publication, invoke separate `$codex-create-handoff` **for H0 ONLY**, not H1. Keep visual gate absent/unapproved until actual design review.

Never treat the current doc author's checks as independent Codex review.
