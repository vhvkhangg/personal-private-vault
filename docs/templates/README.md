> **Effective cadence and blocker policy:** [Current Owner Delivery Policy](../workflow/current-owner-delivery-policy.md). Every slice receives independent Codex review; every completed phase receives a final review, three-phase RAG milestones after P19/P22, then RAG and RAG+Backend audits. A mandatory unverified acceptance item or any CRITICAL/HIGH/**MEDIUM** open finding blocks delivery. LOW/INFO are tracked until milestone/audit. Historical completed reviews are never rewritten.

# Documentation templates

Use these templates for **new** records, not to retroactively rewrite frozen Phase 17/18 artifacts. The canonical current status is `docs/implementation/handoffs/ACTIVE.md`; dated history stays dated. Each template is deliberately short.

| Kind | Template | Home |
| --- | --- | --- |
| Owner decision | [owner-decision.md](owner-decision.md) | `docs/implementation/phase-N/owner-decisions.md` |
| ADR | [adr.md](adr.md) | `docs/adr/NNNN-topic.md` |
| Pre-handoff review | [pre-handoff-review.md](pre-handoff-review.md) | `docs/implementation/phase-N/reviews/` |
| Executable handoff | [handoff.md](handoff.md) | `docs/implementation/handoffs/ACTIVE.md` plus phase-scoped contract |
| Independent review | [review.md](review.md) | `docs/implementation/phase-N/reviews/YYYY-MM-DD-...md` |
| Finding | [finding.md](finding.md) | Usually embedded in a review; stable ID and evidence |
| Slice closeout | [slice-closeout.md](slice-closeout.md) | `docs/implementation/phase-N/` |
| Phase final review | [phase-final-review.md](phase-final-review.md) | `docs/implementation/phase-N/reviews/` |
| Three-phase milestone | [milestone-review.md](milestone-review.md) | Closing phase `reviews/` |
| RAG / combined audit | [audit.md](audit.md) | Approved audit scope under `docs/implementation/maintenance/` or dedicated audit folder |

Severity: CRITICAL/HIGH/MEDIUM/LOW/INFO. Finding disposition: OPEN/CLOSED/NOT_VERIFIED/OWNER_DECISION_REQUIRED. A severity label does not create a new product requirement. Keep immutable failures and truthful not-run evidence. An accepted slice gets its own Owner commit; its closeout cites that published commit. No separate agent staging gate.
