# Agent Development Workflow (current)

See **[Current Owner Delivery Policy](current-owner-delivery-policy.md)** for the authoritative cadence, scope, remediation budget, severity, Git protection and watchdog status. This is **future workflow only**; Phase 17/18 are frozen/closed and Phase 19 is **NOT STARTED**.

| Role | Responsibility |
| --- | --- |
| Owner | Decide behavior/tradeoffs; local Git stage/commit/push; approve any extra scope |
| ChatGPT | Collect decisions, simplify scope, maintain docs/templates, closeout |
| Codex | Pre-handoff review, detailed handoff, **slice review**, **phase final review**, milestone and audits |
| Antigravity `backend-implementer` | Implement/test; mandatory self-review; correct blocking findings (up to **3** rounds per active slice) |
| Antigravity `architecture-auditor` | Optional read-only targeted audit; not mandatory per slice |

```text
Owner approves smallest slice -> Codex preparation/handoff -> Antigravity implement + self-review
  -> independent Codex slice review -> [Critical/High/Medium correction & re-review if needed]
  -> READY_FOR_OWNER_COMMIT -> Owner ordinary Git add/commit/push -> brief slice closeout
  -> repeat next approved slice
all phase slices delivered -> Codex phase final review -> Owner phase closeout
RAG milestones at P19 (P17-P19) and P22 (P20-P22)
all RAG phases done -> RAG audit -> RAG + Backend integration-wide audit
```

Review evidence should be reused between slice/phase/milestone/audit when source hashes remain applicable. Do not create a separate staged-tree review or repeat complete test suites without a concrete need. Use `docs/templates/` for new artifacts.

**Tooling boundary:** Maintenance B1 progress-aware watchdog (300s ordinary / 2,100s full-suite cap with inactivity diagnostics) is **implemented**, awaiting independent Codex review; B2 tooling enforcement remains pending. `logs/` remains untouched.

**Historical record:** old two-round/20-minute guidance and Backend milestone 3/6/9/12 procedures are available in prior committed versions and dated phase reports. They are not active for future work. Earlier Phase 15 backend audit remains closed; no reopening of accepted Phase 17/18 is authorized.
