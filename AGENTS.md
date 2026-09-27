# Personal Private Vault — Agent Operating Contract

This file defines repository-wide instructions for coding agents.

## Project state

- Private, permanently single-user personal vault.
- Backend-first modular monolith using Spring Boot and Spring Modulith.
- PostgreSQL + Flyway; Maven build.
- Frontend, RAG, and production deployment remain deferred.
- Backend Phase 0 is complete/frozen.
- Backend Phase 1 is active: executable Schema v1 + `reference` + `vault`.

Architecture-sensitive work must respect:

- `docs/architecture/`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `docs/repository/repository-package-tree.md`
- `docs/adr/`
- `docs/implementation/backend-phase-1.md`

## Development workflow

The owner no longer writes production implementation.

The default workflow is:

1. **Codex creates the implementation handoff.**
   - Invoke `$codex-create-handoff`.
   - Codex is planning/review-only at this stage.
   - It writes `docs/implementation/handoffs/ACTIVE.md`.
2. **Antigravity implements the handoff.**
   - Invoke `/antigravity-implement-handoff`.
   - Antigravity may write production code and tests within the approved handoff scope.
   - It may iteratively compile/test/fix its own implementation until green or blocked.
3. **Codex performs final review.**
   - Invoke `$codex-final-review`.
   - Codex does not modify production code during final review.
   - If changes are required, Codex writes a remediation checklist into the active handoff and Antigravity handles it.
   - If ready, Codex records `READY FOR OWNER COMMIT` and gives exactly one Conventional Commit message.
4. **Owner commits and pushes.**
   - Agents do not commit, push, tag, or create/merge PRs.

Phase 0 is not reopened by this workflow change. Its application/bootstrap baseline remains frozen.

## Handoff rule

Production implementation must be grounded in the active Codex handoff:

`docs/implementation/handoffs/ACTIVE.md`

Agents must not broaden scope beyond the handoff. If the handoff conflicts with a frozen architecture/database
baseline, stop and report the conflict instead of silently changing the baseline.

## Context/token discipline

Use the smallest context that can safely complete the task.

- Read the active handoff first.
- Prefer exact referenced architecture/docs files over broad repository scans.
- If `graphify` is available and `graphify-out/graph.json` exists, query Graphify before broad grep/read operations.
- Graphify is navigation context, never a source of truth; verify important details in canonical files.
- Avoid re-reading unchanged large files.
- Do not paste DBML, migrations, logs, or whole files into handoffs when a path + precise requirement is sufficient.
- Keep handoffs concise and link to canonical docs instead of duplicating them.

## Git workflow

- One branch: `main`.
- No pull-request workflow.
- Owner commits/pushes.
- Agents must never run `git commit`, `git push`, `git tag`, or equivalent publishing commands.
- Suggested commit messages use Conventional Commits.

## Frozen baselines

The following remain frozen unless the owner explicitly approves an architectural change:

- Database Schema v1
- Module Boundary v1
- Architecture Diagrams v1
- Repository / Package Tree v1
- Backend Phase 0 bootstrap baseline

A frozen-baseline change requires an ADR update/new ADR and synchronized affected docs.

## Module invariants

- Each application module owns its entities, repositories, and internals.
- Other modules use only exposed public APIs/named interfaces.
- Never import another module's `internal` package.
- Never share JPA repositories across module boundaries.
- `vault` and `reference` have no application-module dependencies.
- `search` is a leaf/orchestration module.
- Prevent module dependency cycles.
- Consult `docs/architecture/module-dependency-matrix.md` before new cross-module dependencies.

## Backend conventions

- REST/JSON + OpenAPI when HTTP work begins.
- Never expose JPA entities from controllers.
- Use explicit API DTOs.
- Flyway defines the physical schema; Hibernate auto-DDL is not the schema source of truth.
- Store timestamps in UTC.
- Media binaries stay outside PostgreSQL.
- Use SLF4J; never log passwords, PINs, JWTs, refresh tokens, secrets, or sensitive payloads.
- Lombok is allowed for boilerplate reduction, but do not use `@Data` on JPA entities.
- For JPA entities prefer targeted annotations such as `@Getter` and protected no-args construction; keep equality,
  association handling, and state transitions deliberate.
- `@RequiredArgsConstructor` is acceptable for stateless Spring services with final dependencies.

## Testing

Antigravity owns implementation tests for the active handoff.

Preferred tools:

- JUnit 5
- AssertJ
- Mockito where isolation adds value
- Spring Modulith Test / `@ApplicationModuleTest`
- Testcontainers PostgreSQL
- Spring Boot `@ServiceConnection` where useful
- MockMvc when HTTP work exists

Do not use H2 as a PostgreSQL substitute.

## Scope discipline

Do not implement deferred scope without an explicit Codex handoff:

- frontend;
- RAG;
- production deployment;
- multi-user support;
- 2FA/passkeys;
- unrelated refactors.
