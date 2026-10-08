# Documentation

This directory is the canonical documentation baseline for **Personal Private Vault**.

Backend Phases 0–14 are **complete and frozen**. The Phase 1–3 milestone is `MILESTONE_READY`.
The Phase 4–6 milestone is `MILESTONE_READY` after owner-committed privacy-safe constraint-logging maintenance and
post-milestone synchronization/reset. Phase 7 (`account`), Phase 8 (`knowledge`), and Phase 9 (`collection`) are
complete/frozen after owner commit/push. The Phase 7–9 milestone is `MILESTONE_READY` after committed
integrity/query-shape maintenance `785dd7d`; the owner committed/pushed milestone docs and ChatGPT completed
post-milestone synchronization/reset. Phase 10 (`feed` + `importdata`) is complete/frozen after owner commit/push
and final 685-test verification. Phase 11 (`finance` + `journal` + `personal`) is complete/frozen after final
acceptance, owner commit/push, and 787-test verification. Phase 12 PostgreSQL-first Global Search is complete/frozen after final acceptance, owner commit/push, and
independent 815-test verification. The Phase 10–12 milestone is `MILESTONE_READY`; M10-12-1 closed after
accepted maintenance owner commit/push `a881540` and fresh 817-test verification.
Milestone review/status docs are owner committed/pushed as `4220ad4`, and post-milestone synchronization/reset is complete.
Phase 13 passed [final acceptance](implementation/phase-13/reviews/2026-10-06-phase-13-final-codex-acceptance.md),
was owner committed/pushed as `ef92d94`, and is complete/frozen with the retained 867-test verification. The earlier
baseline boundary-test failure remains preserved in historical evidence.
Phase 14 Backend Integration Hardening + Portability/Object Storage Closure passed
[final acceptance](implementation/phase-14/reviews/2026-10-06-phase-14-final-codex-acceptance.md), was owner
committed/pushed as `3bb3f2e` (`feat(backend): add portable exports and managed image storage`), and is **complete/frozen** after ChatGPT closeout. All nine findings are
closed; independent 920-test clean verification and 49 focused tests are retained. The completed handoff is archived
under `implementation/phase-14/handoff.md`; at Phase 14 closeout, ACTIVE.md was reset to `NO_ACTIVE_HANDOFF`.
Phase 15 Comprehensive Backend Audit & Remediation Gate preparation is
[READY FOR AUDIT](implementation/phase-15/reviews/2026-10-06-phase-15-pre-audit-codex-acceptance.md): P15-1 closed,
no blocking preparation findings. Preparation was committed; the
[initial audit](implementation/phase-15/reviews/2026-10-06-phase-15-backend-audit.md) returned
**OWNER_DECISION_REQUIRED** with 17 open findings. The [owner decisions](implementation/phase-15/owner-decisions.md)
now authorize bounded remediation; [re-review](implementation/phase-15/reviews/2026-10-07-phase-15-authorized-remediation-codex-review.md)
is **REMEDIATION_REQUIRED**. Prior [final acceptance](implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-acceptance.md)
remains historical. The [closure audit](implementation/phase-15/reviews/2026-10-07-phase-15-closure-backend-audit.md)
closes 13 findings and FR15-9, retaining BA15-2, BA15-9, BA15-14, BA15-15 at that gate. The
[latest 2026-10-08 final acceptance](implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md)
closes FR15-11/12 and retains FR15-10's closure; no blocking final-review findings remain.
Independent final-review verification passed 1021 tests, zero failures/errors/skips (03:53). The
[final closure audit](implementation/phase-15/reviews/2026-10-08-phase-15-closure-backend-audit.md)
returns **BACKEND_AUDIT_READY**: all 17 findings CLOSED; FR15-9/10/11/12 closed, no actionable remnant or debt.
Fresh full/coverage verification passed 1021/0/0/0 (04:07), and static/architecture/live-OpenAPI checks completed.
The [accepted handoff](implementation/phase-15/handoff.md) is archived; ACTIVE is NO_ACTIVE_HANDOFF.
Owner commit/push is permitted with `fix(backend): remediate phase 15 audit findings`; afterward give ChatGPT
the latest package for Phase 15 closeout only. Historical records are preserved; Phase 15 is not yet complete/frozen.
Frontend/RAG are Phase 16/17 and remain owner-gated.
Phase 1 delivered the
executable Flyway Schema v1 and verified `reference` and `vault` foundation modules. The frozen logical DBML/module baselines remain unchanged;
the owner-approved Repository/Package Tree v1.1 refinement is recorded by ADR-0015.

## Documentation map

| Area                                                                      | Purpose                                                                                                               |
| ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| [`architecture/`](architecture/README.md)                                 | System architecture, module boundaries, data, API, security, search, integration, storage, and workflow documentation |
| [`adr/`](adr/README.md)                                                   | Accepted Architecture Decision Records (ADRs)                                                                         |
| [`database/`](database/README.md)                                         | Frozen DBML logical schema baseline and Flyway executable-schema guidance                                             |
| [`repository/`](repository/repository-package-tree.md)                    | Frozen repository and Java package organization                                                                       |
| [`owner-phase-workflow.md`](workflow/owner-phase-workflow.md)             | Owner checklist from phase closeout to the next handoff                                                               |
| [`roadmap.md`](roadmap.md)                                                | 18-phase roadmap, prep/audit gates, status, milestone cadence                                                         |
| [`agent-development-workflow.md`](workflow/agent-development-workflow.md) | Canonical ChatGPT → Codex → Antigravity → Codex workflow                                                              |
| [`implementation/`](implementation/README.md)                             | Implementation-phase plans, completion records, test evidence, and operational guidance                               |

## Frozen baselines

- Database Schema v1
- Module Boundary v1
- Functional Decomposition Diagram v1
- C4 System Context v1
- C4 Container v1
- Module Dependency Diagram v1
- Repository/Package Tree v1.1
- Backend Phase 0 bootstrap baseline
- Backend Phase 1 reference/vault foundation baseline

A frozen baseline is not immutable forever. It means changes require a concrete new requirement, defect, or accepted architectural reason. Avoid speculative refactoring of frozen baselines.

## Documentation rules

1. Documentation is written in English.
2. Canonical editable diagram sources are committed beside their exports.
3. Do not edit generated/exported diagram files as the source of truth.
4. Accepted architectural changes must update the relevant document and, when they change a decision, add or supersede an ADR.
5. Once Flyway migrations exist, migrations become the executable database source of truth; the DBML remains the logical architecture baseline and must be kept synchronized with accepted schema changes.
6. Never commit credentials, tokens, personal vault data, private backups, or production secrets to this repository.

## Current development workflow

Normal feature phases use ChatGPT preparation → `$codex-pre-handoff-review` → preparation commit →
`$codex-create-handoff` → Antigravity → `$codex-final-review` → owner commit.

Phase 15 is the dedicated exception: `$codex-pre-handoff-review` first returns `READY FOR AUDIT`, then its committed
preparation is followed by `$codex-backend-audit`; Codex creates
one bounded remediation handoff only from proven authorized findings, and the repository is re-audited after
remediation until `BACKEND_AUDIT_READY`.

Backend Phases 0–14 remain frozen; Phase 15 may inspect them but cannot change frozen schema/architecture/module/API/
business baselines without explicit owner approval.
