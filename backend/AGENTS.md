# Backend Scope Instructions

These instructions apply to `backend/` and refine root `AGENTS.md`.

## Implementation authority

Antigravity may write substantial production implementation **only** when following the active Codex handoff at:

`docs/implementation/handoffs/ACTIVE.md`

If the handoff is missing, not marked `READY_FOR_IMPLEMENTATION`/`CHANGES_REQUESTED`, or does not cover the
requested production change, stop and request a Codex handoff instead of improvising scope.

Codex remains planning/review-only for production implementation unless the owner explicitly overrides the workflow.

Phase 17 Alternative B/E1 **Foundation is COMPLETE — FROZEN** after independent final re-review 3,
owner implementation commit/push `829d294aca7d916851348ff21004c631915d572e` and
[formal closeout](../docs/implementation/phase-17/closeout.md). Its `P17-RAG-FOUNDATION` handoff
is archived; current [ACTIVE](../docs/implementation/handoffs/ACTIVE.md) is `NO_ACTIVE_HANDOFF`.
FR17-1–11 and AB-1–6 are closed for bounded Foundation scope. `AUTH-CLOCK` remains separately OPEN
in Authentication; no blanket test waiver. Default containment and **disabled live external-file
indexing** remain until independent Windows/NTFS R7 proof and an accepted new handoff.

## Current RAG authorization — 2026-10-17

Owner approved Phase 18 **design/preparation**, including bounded D18-A in
[ADR-0021](../docs/adr/0021-phase-18-bounded-multi-file-verified-indexing.md); Codex pre-handoff
is **READY FOR HANDOFF** after [re-review 2](../docs/implementation/phase-18/reviews/2026-10-17-phase-18-pre-handoff-codex-re-review-2.md), with all four preparation findings closed. This is not a production handoff. Maintain
frozen [ADR-0020](../docs/adr/0020-phase-17-single-job-reservations-e1.md) semantics until a
separately accepted/owner-committed Phase 18 preparation and new Codex-created active handoff
specifically authorize implementation. No production work under a closed Phase 17 handoff, no
automatic live-file enablement, and no unapproved commit/push. After two actual Antigravity
implementation remediation + Codex re-review rounds, a separate Codex takeover/reviewer workflow
may apply **only per the existing authorized workflow**, not during preparation.

## Package structure

Keep code inside the owning business module.

Do not create global root technical packages such as `controller`, `service`, `repository`, `entity`, or `dto`.
Technical layers belong under each business capability/module.

## Module access

- Do not access another module's JPA repository/entity directly.
- Do not import another module's `internal` package.
- Cross-module access goes through exposed APIs/named interfaces.
- Validate dependency directions against the frozen matrix.
- Prefer synchronous APIs for immediate results and events for decoupled side effects.

## Persistence

- PostgreSQL is the production database.
- Flyway migrations define the physical schema.
- Frozen DBML is the logical v1 baseline.
- Do not use H2 assumptions.
- Keep transactional boundaries in application services/use cases.
- Avoid persistence/lazy-loading leakage into public API serialization.

## Lombok

Lombok is compile-time boilerplate support, not a design shortcut.

Allowed examples:

- `@Getter` on JPA entities where appropriate;
- protected `@NoArgsConstructor` for JPA;
- `@RequiredArgsConstructor` for Spring services with final dependencies;
- focused builders/value-object helpers when justified.

Avoid:

- `@Data` on JPA entities;
- automatic `toString`, `equals`, or `hashCode` across lazy associations;
- mutable public setters merely because Lombok can generate them.

## Build/test

- Maven is the backend build tool.
- Prefer Spring Boot-managed dependency versions.
- Antigravity may compile/test/fix iteratively within an active handoff.
- Use Testcontainers PostgreSQL for persistence integration tests.

## Phase 13 shared HTTP contract exception

During an accepted active Phase 13 handoff, ADR-0016 permits a small allowlisted set of shared HTTP envelope/error/
OpenAPI types directly in `com.vhvkhangg.personalprivatevault`.

Do not turn this into a global technical subpackage or generic utility layer. Business controllers/DTOs/mappers/advice
still belong under their owning module's `internal/web` package.

## Phase 14 portability/object-storage exception

Phase 14 is complete/frozen. Its accepted implementation contains:

- the `portability` leaf module and ADR-0017 read-only JDBC snapshot adapter;
- Media-owned S3-compatible binary storage infrastructure;
- the approved binary HTTP routes/configuration/health integration;
- directly required Maven/test dependencies.

The portability JDBC exception remains read-only and export-only. It does not authorize generic cross-module SQL,
repository/entity sharing, schema changes, provider-specific domain types, schedulers, hard delete, deployment,
frontend, or RAG work. Future changes require an explicit approved scope.

## Phase 15 backend audit

Phase 15 is audit-first. Codex may inspect the entire non-RAG backend and run audit-only tooling, but production
changes require the Phase 15 remediation handoff created from proven findings. Antigravity must not remediate audit
observations or speculative cleanup outside that handoff.
