---
name: documentation-writing
description: Write accurate, consistent project ADRs, owner decisions, phase/slice handoffs, independent reviews, findings and closeouts using docs/templates without duplicating history or claiming unverified acceptance.
---

# Documentation Writing

**Current template/status contract:** [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md). Independent Codex review after **each slice**, final review after **each phase**, milestone after **three RAG phases** (P19/P22), then separately authorized RAG and RAG+Backend audits. CRITICAL/HIGH/MEDIUM OPEN block; LOW/INFO accumulate for milestone/audit. Direct changed-file docs packages, no closeout scripts or redundant staged-tree review; historic Phase 17/18 reports never rewritten to claim PASS.


Use `docs/templates/README.md` and the matching template. The user owns scope and only ChatGPT owns routine docs patch composition; this skill guides documentation validation, not permission to edit or close frozen artifacts.

1. Identify exact document type, canonical directory, target commit and approved user scope.
2. Record **source fact / agent inference / proposal / Owner-approved decision** distinctly.
3. Write one concise current status and links to dated history; avoid multi-page stacks of old CURRENT notices. Never rewrite failed logs or missing evidence.
4. Findings have stable IDs, severity CRITICAL/HIGH/MEDIUM/LOW/INFO, blocking flag grounded to approved scope, and explicit lifecycle OPEN/CLOSED/NOT_VERIFIED/OWNER_DECISION_REQUIRED.
5. Check every URL/path/ADR/review reference, template sections, case and relative-link consistency.
6. Keep docs structure: `docs/adr`, `docs/workflow`, `docs/implementation/phase-N`, phase `reviews`, and `docs/templates`. Historical artifact names need not be renamed merely for cosmetics.
7. Plan deletion/move only with explicit inventory and cross-reference/test/hash impact; do not silently discard accepted evidence.
8. Package only new/modified files as a plain archive, without scripts for the Owner to execute. Git staging/publishing remains the Owner's action.
9. No new acceptance rule, product feature or future phase is authorized by a documentation edit.

## Scope-bounded Docs Sync Check — Maintenance B3 amendment

Apply [Documentation synchronization](../../../docs/workflow/documentation-synchronization.md) when preparing new/current documentation. Classify the touched file as current, derived, immutable history or proposal; inspect the approved authority, relevant evidence and links. In the existing deliverable record, give `UPDATED / ALREADY_CONSISTENT / DEFERRED_OUT_OF_SCOPE / BLOCKED_OWNER_DECISION / NOT_VERIFIED` with exact affected paths. Do not rewrite unrelated stale repository records (cleanup B), invent a published SHA, self-approve a docs package or create a new acceptance gate. Route material out-of-scope corrections to the Owner for a separately authorized cleanup.
