# Codex Pre-Handoff Review — Backend Phase 3 People Foundation

- Date: 2026-09-28
- Reviewer: Codex
- Scope: Phase 3 preparation and its accompanying phase-closeout/governance/tooling slice
- Baseline: `main` at `6c67f5d` plus the current uncommitted preparation working tree
- Gate: Phase 2 is committed/frozen and `ACTIVE.md` is `NO_ACTIVE_HANDOFF`
- Execution: Review-only; no production implementation, Maven run, handoff creation, commit, or push

## Findings

### Critical

None.

### High

None.

### Medium

#### M-1 — Cross-module access is described only at whole-module granularity

- Files: `docs/implementation/phase-3/README.md:22`,
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/package-info.java:34`
- Observed problem: The preparation says only “public APIs,” while the existing module descriptor permits the
  complete `vault` and `reference` modules. Phase 1 already exposes the exact named interfaces needed by People:
  vault entry/enums/view and reference catalog/view.
- Consequence: A handoff based on this preparation can preserve broad access and weaken Spring Modulith's ability
  to reject accidental dependencies on unrelated public capabilities.
- Required correction: Name the exact allowed interfaces (or a verified narrower set) and require the handoff to
  update the People module descriptor accordingly. Internal packages remain forbidden.

#### M-2 — Duplicate and concurrent set semantics are left to the implementer

- Files: `docs/implementation/phase-3/README.md:39`, `docs/implementation/phase-3/README.md:76`,
  `.agents/skills/people-domain-modeling/SKILL.md`
- Observed problem: Schema v1 uniquely constrains creator-group names, `(person_id, role)`, and
  `(creator_group_id, person_id)`, but the preparation does not say whether repeated additions/creates are
  idempotent or conflicts, nor require racing calls to produce the same stable outcome. The testing contract names
  role/membership uniqueness but omits creator-group-name uniqueness and concurrency.
- Consequence: The handoff would need to invent externally visible behavior, and check-then-insert implementations
  could pass sequential tests while failing unpredictably under races.
- Required correction: Choose and document repeat/race outcomes for all three uniqueness boundaries; require
  database-backed PostgreSQL concurrency tests and no partial state. Also bound the current lookup contract and
  explicitly defer person/group deletion if it is not part of this foundation.

#### M-3 — The review-relocation cleanup helper can delete without proving an exact copy

- File: `scripts/apply-phase-review-migration.ps1:21`
- Observed problem: The script checks only that each destination exists, then deletes the legacy source. It does not
  verify byte/content equality. The current working-tree destinations do happen to have the exact Git blob hashes
  of all ten sources, so the prepared move is sound; the retained helper itself does not enforce that safety.
- Consequence: A later or partial run can discard the canonical source while preserving an incomplete or altered
  destination. Git recovery exists, but the helper contradicts the repository's cautious destructive-action policy.
- Required correction: Prefer removing the one-time helper once Git represents the migration. If it remains,
  compare content/hashes before every deletion, fail closed on mismatch, and document its purpose/usage.

#### M-4 — Review relocation and preparation scope documentation are incomplete

- Files: `docs/implementation/phase-0/README.md:16`, `docs/implementation/phase-0/build-fix-notes.md:13`,
  `docs/implementation/phase-1/README.md:17`, `docs/implementation/phase-3/preparation-review.md:9`,
  `docs/implementation/agent-tooling-change-summary.md:3`
- Observed problem: Phase 0/1 link labels and one historical code path still point to `../../reviews/...` after the
  phase-local relocation. The preparation inventory omits the migration script, moved review corpus/indexes, phase
  closeout/status edits, and several workflow artifacts being reviewed. The tooling summary says both
  `READY FOR OWNER COMMIT` and “AWAITING CODEX GOVERNANCE REVIEW.”
- Consequence: The preparation gate cannot be audited from its declared artifact list, and canonical/historical
  documents present competing locations or workflow states.
- Required correction: Synchronize all live path text, make the prepared-artifact scope complete by category, and
  resolve the tooling-summary status contradiction. Preserve historical review bodies byte-for-byte.

### Low

None.

## Accepted preparation areas

- Phase 2 is committed at `6c67f5d`, frozen consistently across current status documents, and its handoff/review
  evidence is archived under Phase 2.
- Phase 3 owns exactly `persons`, `person_roles`, `creator_groups`, and `creator_group_members`, with no migration
  proposed; this matches frozen DBML, Flyway V1, and the module boundary/dependency matrix.
- Person identity correctly uses a public `VaultEntryOperations.create(PERSON)` call and shared transaction rather
  than vault entities/repositories. Favorite/rating/tag/recycle behavior remains in Vault.
- Optional profile fields remain optional; positive height/weight and public reference-catalog nationality
  validation match Schema v1. Creator groups correctly remain outside shared vault identity.
- REST, frontend, search, migrations, and unrelated modules are excluded. The PostgreSQL/Testcontainers,
  cross-module rollback, constraint, and Modulith evidence direction is otherwise appropriate.
- `people-domain-modeling` adds useful domain-specific guidance. The phase rule and scoped module `AGENTS.md` route
  that guidance without introducing a new custom agent. Existing implementer/auditor roles remain sufficient.
- No hook change was introduced, which is appropriate for this phase.
- Graphify was used only for navigation; all material facts were verified in canonical docs/source.
- The ten relocated historical review files currently match their original Git blob hashes exactly.
- `git diff --check` is clean.

## Final status

`CHANGES_REQUESTED`

Do not create the Phase 3 handoff. Return the preparation slice for the four remediation items above, then invoke
`$codex-pre-handoff-review` again. No commit message is provided while blocking findings remain.

## Remediation re-review — 2026-09-28

### Resolution of original findings

- M-1 resolved in the preparation contracts: the exact Vault and Reference named interfaces are specified.
- M-2 resolved: duplicate, race, lookup, and deletion-boundary semantics are explicit and testable.
- M-3 resolved: the retained migration helper verifies SHA-256 equality before each mapped source deletion and
  preserves unexpected files.
- M-4 resolved: the artifact inventory, live review paths, and historical tooling status are synchronized.

### New findings

#### Medium — Phase 3 production source was changed before the handoff gate

- Files: `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/package-info.java:1`,
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/AGENTS.md:56`,
  `docs/implementation/phase-3/preparation-review.md:58`
- Observed problem: The People module descriptor was changed from whole-module allowances to the planned named
  interfaces even though the preparation explicitly contains no Phase 3 production implementation and the scoped
  module instructions require both `READY FOR HANDOFF` and an active approved handoff. `ACTIVE.md` remains
  `NO_ACTIVE_HANDOFF`.
- Consequence: Accepting this tree would bypass the repository's owner-approved implementation workflow and make
  the preparation inventory's no-production assertion false.
- Required correction: Restore this source file to the committed Phase 2 baseline. Keep the exact named-interface
  target in the preparation docs and require Antigravity to make and test the descriptor change under the future
  active Phase 3 handoff.

#### Low — Phase 3 names the Vault Entry enum incorrectly

- File: `docs/implementation/phase-3/README.md:39`
- Observed problem: The dependency explanation names `EntryType.PERSON`; the exposed enum is
  `VaultEntryType.PERSON`.
- Required correction: Use the source-accurate enum name so the implementation handoff is unambiguous. This typo
  was corrected as a review-documentation edit during this re-review.

### Re-review evidence

- The stale-path scan found only intentional historical/explanatory mentions of `docs/reviews`.
- The actual exposed enum was verified in `vault/enums/VaultEntryType.java`.
- The People source diff and scoped module phase gate were verified directly.
- `git diff --check` remains clean.

### Re-review status

`CHANGES_REQUESTED`

Do not create the Phase 3 handoff. Restore the premature People production-source edit, then invoke
`$codex-pre-handoff-review` again. No commit message is provided while the blocking finding remains.

## Final remediation re-review — 2026-09-28

### Finding resolution

- The People `package-info.java` production descriptor matches `HEAD` at the committed Phase 2 baseline.
- The named-interface narrowing remains a precise future handoff requirement rather than a preparation change.
- The enum reference is source-accurate as `VaultEntryType.PERSON`.
- All four original findings and both remediation findings are resolved.

### Final evidence

- Previous phase: `6c67f5d feat(backend): add authentication and settings foundation`.
- Active handoff: `NO_ACTIVE_HANDOFF`.
- Production/test source diff: empty.
- Historical review relocation: all ten destination blobs exactly match their committed legacy-source blobs.
- Migration helper: parses as PowerShell and retains hash verification, literal paths, unknown-file refusal, and
  non-recursive cleanup behavior.
- Stale review-path scan: only intentional migration/history mentions remain.
- Whitespace/conflict-marker check: `git diff --check` passed.
- Maven was not run because this is a documentation/governance preparation review with no production/test-source
  changes.

### Final status

`READY FOR HANDOFF`

Do not create the implementation handoff automatically. The owner commits/pushes this approved preparation slice
first, then invokes `$codex-create-handoff`.
