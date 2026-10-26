# Active implementation handoff workflow

**Canonical current state:** [ACTIVE.md](ACTIVE.md) = `MAINT-B2-CLI-GIT-HOOK-ENFORCEMENT / READY_FOR_OWNER_COMMIT`, 2026-10-26. The [separate independent final review](../maintenance/2026-10-26-watchdog-tooling/reviews/2026-10-26-b2-independent-takeover-attempt-1-final-review.md) closes F01/F04 and preserves F02/F03 closures/limits. **F01 Codex attempt 1/2 accepted**; Antigravity stays stopped at 3/3, no second attempt or Option B applied. Verified Codex operation requires a trusted repository-root session with native hooks loaded. Next: Owner publishes B2, brief publication closeout, then Maintenance B final `$codex-final-review` after both slices are published. B1 remains published/CLOSED, Phase 17 FROZEN, Phase 18 CLOSED, Phase 19/B3 unauthorized; LOW backlog retained.

Follow [Current Owner Delivery Policy](../../workflow/current-owner-delivery-policy.md); use [handoff template](../../templates/handoff.md), [review template](../../templates/review.md) and [documentation skill](../../../.agents/skills/documentation-writing/SKILL.md) for **future approved slices**.

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

- Antigravity self-review is always required and **does not** count as an independent review or a remediation round.
- Codex only takes over correction if **three** reviewed Antigravity remediation rounds remain blocked, or if Owner explicitly approves another bounded exception; separate independent Codex review then required.
- CRITICAL/HIGH/MEDIUM are delivery blockers; LOW/INFO are recorded and consolidated at milestone/audit.
- Only Owner stages/commits/pushes; no agent publishing and **no extra staged-tree review**. The Owner's normal Git staging step still exists.
- Every instruction derives from an **explicit approved active** phase/slice or approved maintenance scope; old TODOs, expired handoffs and frozen phase plans are not authorization.
- Do not interpret old Phase 18 R7/OCR/GPU/human judging as new requirements; all historical Phase 17/18 evidence remains unchanged. `logs/` unchanged, indexing inactive.
- [Historical handoff guidance at committed baseline](https://github.com/vhvkhangg/personal-private-vault/blob/217cb5d24bf7a62d7348d31838a7a79b01a524bd/docs/implementation/handoffs/README.md) is available for provenance. Do not run that dated 2-round procedure in a new phase.
