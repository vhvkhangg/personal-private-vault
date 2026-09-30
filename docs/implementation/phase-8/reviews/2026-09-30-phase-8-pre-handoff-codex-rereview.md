# Phase 8 Knowledge pre-handoff Codex re-review — 2026-09-30

Status: **READY FOR HANDOFF**. This is a preparation-only review; Codex did not create the implementation handoff or change production code.

## Findings closure

The three findings from the [initial pre-handoff review](2026-09-30-phase-8-pre-handoff-codex-review.md) are closed:

1. Phase 7 status is synchronized as owner committed/pushed and `COMPLETE — FROZEN`. Its archived handoff now identifies the earlier pending-commit statement as historical, not current state.
2. The parent Knowledge facade contract requires parent-owned types in all externally consumable signatures, including parameters, return/generic types, and public exceptions. Nested DTOs remain parent/sibling-facing; mapping is narrow and does not duplicate nested rules. The future handoff must verify a representative top-level consumer using only parent Knowledge imports under Spring Modulith architecture checks.
3. Vocabulary due reads have an inclusive cutoff and fixed predicate: non-mastered scheduled rows at/before cutoff plus null-time `NEW`; null-time `LEARNING`/`REVIEW` and all `MASTERED` are excluded. Scheduled rows sort by review time and ID before null-time `NEW` rows by ID. The bounded read is side-effect free and does not choose an SRS algorithm.

## Remaining preparation checks

- The prior-phase owner commit remains at local `HEAD` and `origin/main` (`63ebd23`); `ACTIVE.md` is `NO_ACTIVE_HANDOFF`. Phase 8 has its README and preparation review; no milestone review is due after Phase 7.
- Scope is limited to the frozen five Knowledge tables in four nested modules plus a small parent facade. Frozen DBML/Flyway, whole-module dependency direction, Vault ownership, public Account/People/Reference validation, race-safe privacy-preserving unique conflicts, atomic Vocabulary transitions, and Note content preservation remain explicit. No migration, production implementation, custom agent, or hook change is part of this preparation.
- The package/API direction and test contract require closed nested-module boundaries, meaningful `package-info.java`, removal of `.gitkeep` upon implementation, PostgreSQL Testcontainers, deterministic contention evidence, architecture verification, and `mvn -f backend/pom.xml -ntp clean verify` plus `git diff --check`.
- `git diff --check` passed for the preparation changes. No IDE inspection was run, and no new compiler warning was reported. A repository-validation script was not run because `scripts/validate_repository.py` is not present in this workspace; this is not a Phase 8 gate requirement.

## Gate

The preparation is **READY FOR HANDOFF**. The owner must commit/push this preparation/docs/tooling slice before invoking `$codex-create-handoff`. Phase 8 production code remains unauthorized until the active Codex handoff is created.
