---
name: codex-current-system-readonly-audit
description: One-off Owner-authorized read-only evidence audit of current PPV backend + RAG Python + docs, workflow and diagrams, without implementing fixes or reopening suspended S19-2.
---

# Codex — Current PPV System Read-Only Audit

Invoke with `$codex-current-system-readonly-audit` from the PPV repository root in Codex CLI.

**Controlling scope:** `docs/implementation/maintenance/2026-11-01-current-system-readonly-audit/owner-authorization.md`. Read the entire decision first; it is the explicit Owner grant and stop rule. Also read `AGENTS.md`, `docs/workflow/current-owner-delivery-policy.md`, `docs/implementation/handoffs/ACTIVE.md`, and the canonical architecture/ADR/status files relevant to each conclusion.

- Verify exact `main` commit and working tree. Do not silently proceed on another HEAD. Respect existing source-archive state and canonical `NO_ACTIVE_HANDOFF`.
- Reuse `.agents/skills/codex-backend-audit/SKILL.md` **only for technical coverage/checklist**, and `.agents/skills/codex-review-coverage/SKILL.md` for evidence categories. Do not use the Phase 15 skill's phase-specific prerequisites, remediation creation, frozen closure or `BACKEND_AUDIT_READY` outcome.
- Use `graphify-context`, `bounded-command-execution` and `documentation-writing` as applicable. Inspect source/receipts, document unverified points. Never treat prior reports or generated diagrams as current source proof.
- Inspect `backend/`, `rag-engine/`, docs, architecture sources and diagrams, governance, scripts, tests and security/resource boundaries. Identify which parts of S19-2 are archived **not accepted**, not runnable under this permission. Do not resume RAG, infer with model, trigger native C2/M10/24-slot, modify code/config/dependency or generate diagrams.
- The only writes permitted are the **two report documents** named in the Owner decision and uniquely named diagnostic receipts in ignored `logs/` if safe bounded diagnostics run. No modifying canonical docs/diagrams/`ACTIVE.md`, migrations or tests; no auto remediation, releases, commits/pushes.
- Check all relevant review dimensions with `VERIFIED/PARTIAL/NOT_VERIFIED/NOT_APPLICABLE` and concrete evidence. Report serious issues before frontend, separate deferred LOW hygiene, and flag genuine stale documentation. Suggest changes, including possible naming/Ideaverse layout, **only as proposals**.
- Stop if the branch baseline or required permission boundary differs materially. Return `AUDIT_REPORTED / OWNER_REVIEW_REQUIRED` when both reports complete, otherwise `BLOCKED_AUDIT`; never `BACKEND_AUDIT_READY`.
- Finish with `Next step: Owner | review docs/implementation/maintenance/2026-11-01-current-system-readonly-audit/{audit-report.md,architecture-diagram-gap-inventory.md} and authorize only selected remediation | audit-only; no implementation | no counters reset`.
