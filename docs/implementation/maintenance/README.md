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

- [`milestone-1-3-concurrency/`](milestone-1-3-concurrency/README.md) — **COMPLETE / FROZEN**
- [`pre-phase4-code-hygiene/`](pre-phase4-code-hygiene/README.md) — **COMPLETE / FROZEN**

- [`milestone-4-6-privacy-safe-constraint-logging/`](milestone-4-6-privacy-safe-constraint-logging/README.md) —
  **READY FOR OWNER COMMIT**
