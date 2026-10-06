# Phase 14 — Codex Pre-Handoff Re-Review 1

Date: 2026-10-06

Verdict: **CHANGES_REQUESTED**

Scope: preparation/documentation/tooling only. No production implementation or handoff creation.

## Gates and scope

- Local `HEAD` and `origin/main` both remain `ef92d94e4b551ec6c7449f251f5186f3e376e0c3`; no remote fetch was performed.
- Phase 13 is complete/frozen. Its dated reviews and retained 867-test evidence have no diff.
- ACTIVE remains `NO_ACTIVE_HANDOFF`; Phase 14 production code/tests and dependencies have not started.
- The owner's explicit Phase 14 concept/ADR-0017 preparation approval remains recorded and valid for this scope.
  No further owner approval is needed to resolve the remaining preparation finding without broadening scope.
- The preceding phase is 13, so no additional milestone gate is due.
- The [initial review](2026-10-06-phase-14-pre-handoff-codex-review.md) remains unchanged as historical evidence.

## Finding disposition

| Finding | Re-review disposition |
| --- | --- |
| P14-1 — streaming failure/resource lifetime | Closed for preparation. README, API architecture, integration skill and rule now distinguish pre-commit JSON errors from post-commit transfer abort. Snapshot lifetime covers actual consumption/worker execution; cancellation, timeout, disconnect and resource cleanup have explicit implementation-test obligations. |
| P14-2 — commit/compensation handling | Partially remediated; one narrowed Medium deletion-safety issue remains below. Explicit commit boundary, bounded three-attempt cleanup, primary-failure preservation and residual-orphan recovery are accepted. |
| P14-3 — canonical architecture/package synchronization | Closed. Package tree, boundary/matrix/overview documents and module diagram sources/exports identify portability as planned, not implemented. Original 18 module vertices and all 38 dependency edges are preserved; the only new module vertex is portability, with no incident dependency edge. |

## Remaining P14-2 — Medium: an absent reconciliation row is not proof that an uncertain write cannot commit

Locations:

- `docs/implementation/phase-14/README.md`: lines 267–270, uncertain metadata commit; lines 282–286, manual recovery.
- `.agents/skills/backend-integration-portability-storage/SKILL.md`: Media object storage, uncertain-commit lookup and
  inventory reconciliation guidance.
- `docs/implementation/phase-14/preparation-review.md`: P14-2 disposition and required check 11.

The README now instructs a fresh Media-owned read following uncertain commit and allows compensation when absence
is confirmed. It does not distinguish “absent in this read's snapshot” from “the original writer has definitively
terminated without committing.” Those are not equivalent. PostgreSQL SELECT visibility excludes concurrent
uncommitted rows. [PostgreSQL transaction isolation](https://www.postgresql.org/docs/17/transaction-iso.html#XACT-READ-COMMITTED).

Inferred failure sequence if the original writer is still pending:

1. The uploaded object exists and the metadata transaction has inserted its Image/Vault rows, but is not committed.
2. A timeout/connection failure leaves the outcome uncertain; a fresh lookup sees no committed Image row.
3. Compensation deletes the binary based only on that absence.
4. The original transaction subsequently commits, leaving retained metadata pointing to a deleted object.

A positive matching row can establish that metadata committed; a negative snapshot alone cannot establish rollback
or rule out a later commit. The manual inventory-vs-committed-row procedure has the same race with in-flight uploads
unless writers are quiesced and outstanding outcomes settled. This is a preparation-contract issue, not a claim that
unimplemented Phase 14 code already exhibits the failure.

Required narrow remediation:

- Make the conservative default explicit: unresolved commit plus an absent/mismatched fresh lookup must retain the
  object, return a safe failure, and use the already accepted manual-recovery path. Automatic deletion requires
  independent confirmation that the original metadata transaction cannot subsequently commit. A fresh positive
  matching lookup may still resolve the outcome to committed success.
- Make manual deletion require a quiescent writer state and settled outstanding transactions, followed by a fresh
  unreferenced-object check. Do not delete solely from a potentially racing inventory snapshot.
- Add a deterministic PostgreSQL/S3 regression obligation: hold a metadata transaction open, reconcile while its row
  is invisible, verify no object deletion, then commit and verify the retained Image/Vault row still has its binary.
  Also retain coverage for confirmed rollback, confirmed commit, failed cleanup and privacy-safe errors.
- Synchronize README, preparation checklist, integration skill/rule and future handoff requirements. No new table,
  write exception, two-phase-commit framework, scheduler, hard-delete endpoint or generic reconciliation platform is
  required or authorized.

## Cross-scope assessment

- Scope/non-goals remain bounded to portable export, Media S3-compatible I/O and configuration/readiness; provider,
  deployment, backup automation, schedulers, frontend/RAG and round-trip archive import remain deferred.
- The approved JDBC exception stays export-only, read-only, allowlisted, repeatable-read, with authentication/Flyway
  exclusion and no foreign JPA/repository/internal access or schema change. Original ownership/dependency directions
  remain intact, and the planned leaf cannot introduce a business-module cycle.
- Existing Media create/domain invariants are reused. Provider SDK types stay in Media infrastructure; public
  interfaces are not introduced merely for same-module controllers/tests. Package descriptors are required for
  meaningful packages actually introduced, not placeholder scaffolds.
- The 211-route/867-test baseline, new binary routes, secret/privacy requirements, typed configuration, readiness vs
  liveness separation and real PostgreSQL/S3 testing remain appropriate subject to the remaining P14-2 requirement.
- One focused integration skill/rule is justified; existing agents/hooks and domain/engineering guidance are reused.
  No new custom agent, hook, framework or unnecessary abstraction is proposed.
- Unaffected C4/BFD/schema artifacts remain unchanged. Existing migration/test-root `.gitkeep` files remain accepted
  out-of-scope baseline hygiene debt; no new production package or stray scaffold exists.
- Current review/status references are synchronized by this re-review. Handoff precision remains blocked only by
  narrowed P14-2.

## Verification and diagnostics

- Safety-hook regression command `python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py`:
  **13 tests passed**.
- `git diff --check`: passed, with existing CRLF/LF normalization notices.
- Pre-edit scoped relative Markdown links: **83 checked, 0 broken**; no trailing whitespace in 18 reviewed documents.
- Final link check after status/report edits: **87 checked, 0 broken**; no trailing whitespace in 19 reviewed documents.
- Draw.io original/prepared edge sets: **38 vs 38**, exact match. Only portability was added to the module vertices.
  SVG inventory: **19 nodes / 38 edges**. PNG was visually inspected; planned portability is legible and disconnected.
- No Java/pom/resource/schema/hook changes are present. Historical Phase 13 dated reviews/test evidence have no diff.
  Backend Maven/IDE inspections were not rerun for this preparation-only review; 867 is retained evidence, not a fresh
  test claim. No IDE-clean claim is made.
- Git text conversion for a diagram file emitted missing `file`/`cat` helper errors. This is configuration-dependent,
  not an architecture defect; raw `--no-textconv` diff, XML edge comparison and visual inspection verified the content
  without changing Git configuration. CRLF/LF notices are also configuration-dependent. P14-2 is actionable.
- Graphify was bounded navigation only; the unimplemented module is absent from the existing graph, and conclusions
  were verified against canonical preparation documents and existing Media source.

## Review changes and next step

Added this report/index entry and updated current preparation/governance statuses. The owner approval, initial
review, production code, frozen schema, diagram/package remediation, `.agents` files and ACTIVE were not changed by
Codex in this re-review.

Give ChatGPT this report and the latest preparation package to remediate only narrowed P14-2, then rerun
`$codex-pre-handoff-review`. Do not create the handoff or treat preparation as accepted until `READY FOR HANDOFF`.
