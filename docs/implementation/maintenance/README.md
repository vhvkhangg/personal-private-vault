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
  **COMPLETE / FROZEN**

- [`milestone-10-12-search-case-normalization/`](milestone-10-12-search-case-normalization/README.md) —
  **COMPLETE / FROZEN** — owner committed/pushed `a881540`; FRM10-12-1/M10-12-1 closed;
  milestone `MILESTONE_READY`, independent 817-test verification passed. Milestone docs commit/reset next.

## Recent documentation and preparation scopes

- [CSA26 current-system audit/remediation](2026-11-01-current-system-readonly-audit/owner-publication-closeout-2026-11-01.md) — independently accepted and Owner-published at `4cf6204`; `CSA26-FR-L01` LOW remains open.
- [Spring-only Archify diagrams](2026-11-01-spring-only-archify/owner-publication-closeout-2026-11-01.md) — SAD-01–04 independently accepted/published at `84f1185`; `SAD-FR-L01` LOW remains open. Documentation-only; not an executable handoff.
- [Universal Inbox / Save for Later](2026-11-01-universal-inbox-preparation/owner-publication-closeout-2026-11-02.md) — `UI26-INBOX-BROWSE` independently accepted and Owner-published at `50aaf7d` (**browsing only**, no read/archive state). `UI26-FR-L02` LOW remains open; 2/3 correction rounds consumed, stopped. No active Inbox implementation handoff.
- [Daily Trends Radar S1](2026-11-02-daily-trends-radar-preparation/owner-publication-closeout-2026-11-04.md) — `DTR26-S1` independently accepted and Owner-published at `7438e7f` (manual Hacker News article refresh and cached rolling-24-hour preview, not RSS/scheduler). `FR-01`–`FR-07` CLOSED; `DTR26-FR-L01` LOW remains OPEN. Antigravity 3/3 exhausted; one Codex takeover accepted. Published S1 commit also contained the root ZIP outside the accepted delivery, acknowledged by Owner as inadvertently retained. `ACTIVE` was reset in the separate docs-only closeout.
- [Daily Trends Radar S2A](2026-11-04-dtr26-s2-preparation/owner-publication-closeout-2026-11-05.md) — independently accepted and Owner-published at `17f8a359` (manual RSS/Atom refresh and cached HN+RSS combined Radar). FR-01–06 CLOSED; 178 fresh independent RSS tests PASS, 361 prior independent tests reused. Antigravity 3/3 exhausted, one Codex takeover 1/1 accepted and stopped. Its former S2B-not-authorized statement was correct as of the S2A closeout; see the separately accepted S2B publication below. `ACTIVE` was reset in this separate S2A docs-only closeout.
- [Daily Trends Radar S2B](2026-11-05-dtr26-s2b-preparation/owner-publication-closeout-2026-11-05.md) — opt-in scheduled HN/RSS independently accepted and Owner-published at `54cb702d8b45`; FR-01–04 CLOSED; 14 fresh PostgreSQL tests PASS plus 236 previously independent tests reused; implementer 250 PASS separately. Antigravity 3/3 consumed, ACCEPTED/STOPPED, no takeover. **Default polling OFF; one-JVM/no-overlap deployment conformance NOT_VERIFIED, activation held.** `ACTIVE` resets in this subsequent docs-only closeout.
- [Future final non-RAG audit intent](2026-11-01-final-non-rag-audit-intent/owner-intent.md) — future gate after the remaining backend features, not active now; all actionable LOW/INFO or higher findings must be resolved.

The general maintenance implementation rules above apply only when an implementation handoff is separately approved; documentation-only preparation and publication closeout do not create an active handoff.
