# Maintenance Slices

Maintenance slices are owner-approved, narrowly scoped corrections that may touch an otherwise frozen phase without
reopening that phase as active feature development.

Rules:

- each slice has one canonical scope document;
- a slice must name the frozen phase(s) and exact permitted behavior/files;
- unrelated refactors, schema changes, and next-phase feature work are forbidden;
- Codex creates a separate active handoff from the approved maintenance scope;
- Antigravity implements/tests only that handoff;
- Codex final-reviews the maintenance implementation;
- the owner commits/pushes only after `READY FOR OWNER COMMIT`;
- any milestone that required the maintenance must then be rerun.

Slices:

- [`supplemental-backend-audit-2026-10-13/`](supplemental-backend-audit-2026-10-13/README.md) —
  **COMPLETE / FROZEN (2026-10-13)** — all `SBA-001`–`SBA-007` and `FR-SBA-1`–`FR-SBA-4` closed;
  Codex independently verified 1,152 passing tests; owner committed/pushed `d195b6260274`.
  See [closeout](supplemental-backend-audit-2026-10-13/closeout.md).

- [`milestone-1-3-concurrency/`](milestone-1-3-concurrency/README.md) — **COMPLETE / FROZEN**
- [`pre-phase4-code-hygiene/`](pre-phase4-code-hygiene/README.md) — **COMPLETE / FROZEN**
- [`milestone-4-6-privacy-safe-constraint-logging/`](milestone-4-6-privacy-safe-constraint-logging/README.md) —
  **COMPLETE / FROZEN**

- [`milestone-7-9-integrity-and-query-shape/`](milestone-7-9-integrity-and-query-shape/README.md) —
  **COMPLETE / FROZEN** — owner committed `785dd7d`; milestone re-review `MILESTONE_READY`.

- [`milestone-10-12-search-case-normalization/`](milestone-10-12-search-case-normalization/README.md) —
  **COMPLETE / FROZEN** — owner committed/pushed `a881540`; FRM10-12-1/M10-12-1 closed;
  milestone `MILESTONE_READY`, independent 817-test verification passed. Milestone docs commit/reset next.

## Recent documentation and preparation scopes

- [CSA26 current-system audit/remediation](2026-11-01-current-system-readonly-audit/owner-publication-closeout-2026-11-01.md) — independently accepted and Owner-published at `4cf6204`; `CSA26-FR-L01` LOW remains open.
- [Spring-only Archify diagrams](2026-11-01-spring-only-archify/owner-publication-closeout-2026-11-01.md) — SAD-01–04 independently accepted/published at `84f1185`; `SAD-FR-L01` LOW remains open. Documentation-only; not an executable handoff.
- [Universal Inbox / Save for Later preparation](2026-11-01-universal-inbox-preparation/README.md) — proposed feature/maintenance preparation awaiting independent review; no implementation authorization.
- [Future final non-RAG audit intent](2026-11-01-final-non-rag-audit-intent/owner-intent.md) — future gate after the remaining backend features, not active now.

The general maintenance implementation rules above apply only when an implementation handoff is separately approved; documentation-only preparation and publication closeout do not create an active handoff.
