# Backend Scope Instructions

These instructions apply to `backend/` and refine root `AGENTS.md`.

## Implementation authority

Antigravity may write substantial production implementation **only** when following the active Codex handoff at:

`docs/implementation/handoffs/ACTIVE.md`

If the handoff is missing, not marked `READY_FOR_IMPLEMENTATION`/`CHANGES_REQUESTED`, or does not cover the
requested production change, stop and request a Codex handoff instead of improvising scope.

Codex remains planning/review-only for production implementation unless the owner explicitly overrides the workflow.

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
