# Active implementation handoff workflow

**Canonical current state:** [ACTIVE.md](ACTIVE.md) = `NO_ACTIVE_HANDOFF`. Maintenance B1 and B2 are both **Owner-published**, with [B1 closeout](../maintenance/2026-10-26-watchdog-tooling/b1-owner-closeout-2026-10-26.md) and [B2 closeout](../maintenance/2026-10-26-watchdog-tooling/b2-owner-closeout-2026-10-26.md). The completed B2 handoff is preserved [at its publishing commit](https://github.com/vhvkhangg/personal-private-vault/blob/bf1683c8da88db2c6d30761cb2e76f6a77d67331/docs/implementation/handoffs/ACTIVE.md). **Maintenance B final aggregation: READY_FOR_OWNER_COMMIT**, [independent review, 2026-10-27](../maintenance/2026-10-26-watchdog-tooling/reviews/2026-10-27-maintenance-b-independent-final-review.md). Owner publication of the remaining documents and brief overall closeout are next. Phase 17 FROZEN; Phase 18 CLOSED; B3/Phase 19 NOT AUTHORIZED.

Follow [Current Owner Delivery Policy](../../workflow/current-owner-delivery-policy.md); use [handoff template](../../templates/handoff.md), [review template](../../templates/review.md) and [documentation skill](../../../.agents/skills/documentation-writing/SKILL.md) for future explicitly approved slices.

```text
Owner approves smallest functional slice
  -> Codex pre-handoff / detailed executable handoff
  -> READY_FOR_IMPLEMENTATION
  -> Antigravity implementation + focused tests + mandatory self-review
  -> IMPLEMENTED_AWAITING_CODEX_REVIEW
  -> independent Codex SLICE review
      -> CHANGES_REQUESTED (Critical/High/Medium) -> Antigravity correction -> independent re-review
         (at most 3 correction/re-review rounds PER slice)
      -> READY_FOR_OWNER_COMMIT -> Owner ordinary git add/commit/push
  -> short published slice closeout
all phase slices delivered -> independent Codex PHASE FINAL review -> Owner phase closeout
post RAG P19 & P22 -> independent milestone reviews
post all RAG -> RAG audit -> subsequent RAG + Backend full integration audit
```

- Maintenance B's **one Codex final aggregation is independently accepted**. Owner publishes the reviewed documentation, then records the brief overall closeout; no new implementation handoff or redundant per-slice review follows.
- Antigravity self-review is mandatory and never counts as an independent review/remediation round. Codex takeover requires exhausted reviewed remediation or specific Owner authorization, followed by a separate independent review.
- CRITICAL/HIGH/MEDIUM block; LOW/INFO backlog is consolidated at milestone/audit. Only Owner stages/commits/pushes; there is no extra staged-tree review session.
- Do not infer new implementation permission from frozen phases, old TODOs, archived handoffs or this maintenance closeout. Historical Phase 17/18 evidence, existing `logs/` and inactive indexing remain protected.
- [Historical handoff guidance at an earlier baseline](https://github.com/vhvkhangg/personal-private-vault/blob/217cb5d24bf7a62d7348d31838a7a79b01a524bd/docs/implementation/handoffs/README.md) remains available for provenance; its outdated review cadence is not current authority.
