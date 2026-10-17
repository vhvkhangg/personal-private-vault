# Personal Private Vault

Private, single-user personal information vault built as a backend-first modular monolith.

## Current status

**Current as of 2026-10-17:** Backend **Phases 0–15 and 17 are COMPLETE — FROZEN**.
Phase 17 single-job Local RAG Foundation (Alternative B / ADR-0020) was independently accepted
([Codex final re-review 3](docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md))
and committed/pushed by the owner as [`829d294`](https://github.com/vhvkhangg/personal-private-vault/commit/829d294aca7d916851348ff21004c631915d572e).
[Phase 17 closeout](docs/implementation/phase-17/closeout.md) archives the
[handoff](docs/implementation/phase-17/handoff.md); [ACTIVE](docs/implementation/handoffs/ACTIVE.md)
is `NO_ACTIVE_HANDOFF` after the owner-published Phase 17 closeout.
`AUTH-CLOCK` remains OPEN in Authentication under the owner-approved narrow exception (not used in
successful verification). Default containment and disabled live external-file capability remain.
Phase 16 Frontend is deferred. **Phase 18 preparation** is [Codex READY FOR HANDOFF](docs/implementation/phase-18/preparation-review.md), awaiting owner preparation commit/push; there is **no Phase 18 implementation handoff**. Phases 19–22 remain unprepared/owner-gated.

**Historical progression (superseded by the current status above):**

Backend Phase 0 and Backend Phase 1 are **complete and frozen**. Phase 1 delivered the executable PostgreSQL/Flyway Schema v1 plus the implemented and verified `reference` and `vault` foundation modules (57 tests, 0 failures against PostgreSQL 18.6 Testcontainers).

Backend Phases 2 (`authentication` + `settings`), 3 (`people`), 4 (`fiction`), 5 (`film`), 6
(`media` + `location`), 7 (`account`), 8 (`knowledge`), 9 (`collection`), 10 (`feed` + `importdata`),
11 (`finance` + `journal` + `personal`), 12 (`search`), 13 (shared REST/API + HTTP exposure), and 14
(backend integration hardening + portability/object storage closure) are **complete and frozen** after owner commit/push. The Phase 1–3 milestone remains
`MILESTONE_READY`. The Phase 4–6 milestone is also `MILESTONE_READY` after committed privacy-safe constraint-logging maintenance. Phase 7 (`account`) is complete/frozen after owner commit/push.

Development is agent-driven: Codex creates the handoff, Antigravity implements/tests it, Codex performs final review, and the owner commits/pushes.

Frozen baselines:

- Database Schema v1
- Module Boundary v1
- Functional Decomposition Diagram v1
- C4 System Context v1
- C4 Container v1
- Module Dependency Diagram v1
- Repository/Package Tree v1.1
- Backend Phase 0 bootstrap baseline

Frozen implementation phases:

- Backend Phase 0 — bootstrap baseline
- Backend Phase 1 — PostgreSQL/Flyway Schema v1 + Reference & Vault Foundation
- Backend Phase 2 — Authentication + Settings Foundation
- Backend Phase 3 — People Foundation
- Backend Phase 4 — Fiction Foundation
- Backend Phase 5 — Film Foundation
- Backend Phase 6 — Media + Location Foundations
- Backend Phase 7 — External Account & Relationship History Foundation
- Backend Phase 8 — Knowledge Foundation
- Backend Phase 9 — Collection Foundation
- Backend Phase 10 — Feed + ImportData Foundations
- Backend Phase 11 — Finance + Journal + Personal Foundations
- Backend Phase 12 — PostgreSQL-First Global Search
- Backend Phase 13 — Shared REST/API Contract + Module HTTP Exposure
- Backend Phase 14 — Backend Integration Hardening + Portability/Object Storage Closure
- Backend Phase 15 — Comprehensive Backend Audit & Remediation Gate
- Backend Phase 17 — Single-job Local RAG Foundation (Alternative B / E1)

Historical gates (as of the Phase 15 / Phase 17 preparation timeline):

- Phase 7–9 milestone — `MILESTONE_READY` and post-milestone synchronization complete
- Phase 10 Feed + ImportData — complete/frozen after owner commit/push
- Phase 11 Finance + Journal + Personal — complete/frozen after owner commit/push
- Phase 12 PostgreSQL-first Global Search — complete/frozen after owner commit/push
- Phase 10–12 milestone — `MILESTONE_READY`; milestone docs owner committed/pushed; post-milestone reset complete
- Phase 13 Shared REST/API Contract + Module HTTP Exposure — complete/frozen after owner commit `ef92d94`; 867-test final acceptance retained
- Phase 14 Backend Integration Hardening + Portability/Object Storage Closure — **complete/frozen** after
  [final acceptance](docs/implementation/phase-14/reviews/2026-10-06-phase-14-final-codex-acceptance.md), owner
  commit/push `3bb3f2e` (`feat(backend): add portable exports and managed image storage`), and ChatGPT closeout. All nine findings are closed; independent 920-test clean
  verification and 49 focused tests are retained. Its completed handoff is archived.
- Phase 15 Comprehensive Backend Audit & Remediation Gate — **COMPLETE — FROZEN** after
  [final acceptance](docs/implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md),
  [repository-wide closure audit](docs/implementation/phase-15/reviews/2026-10-08-phase-15-closure-backend-audit.md)
  (`BACKEND_AUDIT_READY`) and owner commit/push `0b1ab11` (`fix(backend): remediate phase 15 audit findings`).
  All 17 BA15 findings and FR15-9/10/11/12 are closed, with no unresolved actionable finding or accepted debt.
  Fresh full/coverage verification passed 1021 tests with zero failures/errors/skips; static reports were triaged.
  The [handoff](docs/implementation/phase-15/handoff.md) is archived; at that historical Phase 15 closeout ACTIVE was `NO_ACTIVE_HANDOFF`;
  [audit status](docs/implementation/phase-15/audit-status.md), reviews, owner decisions and evidence are preserved.
  **2026-10-13 update:** The owner authorized Phase 17 RAG foundation preparation before deferred
  Frontend Phase 16. The [Codex review](docs/implementation/phase-17/reviews/2026-10-13-phase-17-p17-3-and-coverage-codex-review.md)
  returned `READY FOR HANDOFF` for the foundation design, and the owner accepted the bounded
  integration delta and committed/pushed preparation as `dd414861`. The subsequent
  [supplemental backend maintenance](docs/implementation/maintenance/supplemental-backend-audit-2026-10-13/closeout.md)
  closed all SBA-001–007 and FR-SBA-1–4, with 1,152 passing tests independently verified by Codex;
  the owner committed/pushed implementation as `d195b626`. In this closeout, `ACTIVE.md` is reset
  to `NO_ACTIVE_HANDOFF`. **Next:** owner commit/push closeout docs, then create a separate Phase 17
  foundation handoff. At that historical preparation gate, Phase 17 was not implemented; Phases 18–22 have independent gates, and
  Frontend/Ideaverse remain deferred. See [P-17](docs/implementation/phase-17/README.md) and
  accepted [ADR-0019](docs/adr/0019-local-rag-before-frontend.md).

## Planned stack

- Backend: Java + Spring Boot + Spring Modulith + Spring Data JPA/Hibernate
- Database: PostgreSQL
- Build: Maven
- Frontend (Phase 16 deferred until after RAG, subject to owner authorization): Next.js + TypeScript + shadcn/ui + pnpm
- RAG: Phase 17 Local Java/Python/FastAPI/PostgreSQL Foundation complete/frozen; external-file parsing, OCR, model inference, retrieval and recommendations remain owner-gated for Phases 18–22
- API: REST/JSON + OpenAPI
- Media: S3-compatible object storage abstraction

Version policy: use the latest stable, mutually compatible release set at implementation time; prefer LTS runtimes when appropriate and framework-managed dependency versions over arbitrary overrides.

## Repository layout

See [`docs/repository/repository-package-tree.md`](docs/repository/repository-package-tree.md).

## Architecture

Architecture sources and exports are under [`docs/architecture`](docs/architecture).

The frozen database schema is under [`docs/database`](docs/database).

## Future RAG engineering workflow

The completed Phase 17 Foundation remains frozen. For the owner-authorized [Phase 18 preparation](docs/implementation/phase-18/README.md) and subsequent separately gated phases, use the [scope simplification review](docs/implementation/rag-scope-simplification-review.md) and [proportional delivery/escalation rules](docs/workflow/rag-phase-delivery-guardrails.md). These are **proposals/workflow constraints**, not a Phase 18 implementation handoff. The existing stable-compatible version policy remains [ADR-0013](docs/adr/0013-stable-compatible-version-policy.md); new major language/tool releases are not automatically required.

## Development workflow

Normal feature phases:

1. ChatGPT closes/freezes a committed phase and prepares the next phase.
2. Codex runs `$codex-pre-handoff-review`.
3. After `READY FOR HANDOFF` and preparation commit/push, Codex runs `$codex-create-handoff`.
4. Antigravity implements/tests with `/antigravity-implement-handoff`.
5. Codex runs `$codex-final-review`.
6. Owner commits/pushes after `READY FOR OWNER COMMIT`.

**Historical Phase 15 audit workflow (closed; not an active Phase 17–22 gate):** Phase 15 used `$codex-pre-handoff-review`; after `READY FOR AUDIT`, the owner committed/pushed its
preparation, ran `$codex-backend-audit`, remediated any Codex-created handoff, then reran the audit until
`BACKEND_AUDIT_READY`.

See [`docs/workflow/owner-phase-workflow.md`](docs/workflow/owner-phase-workflow.md) and [`docs/roadmap.md`](docs/roadmap.md).

Graphify is optional for token-efficient code navigation; see `.agents/README.md`.

Workflow details: [`docs/workflow/agent-development-workflow.md`](docs/workflow/agent-development-workflow.md).
