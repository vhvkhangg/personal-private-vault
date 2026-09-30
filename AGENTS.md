# Personal Private Vault — Agent Operating Contract

This file defines repository-wide instructions for coding agents.

## Project state

- Private, permanently single-user personal vault.
- Backend-first modular monolith using Spring Boot and Spring Modulith.
- PostgreSQL + Flyway; Maven build.
- Frontend, RAG, and production deployment remain deferred.
- Backend Phase 0 is complete/frozen.
- Backend Phase 1 is complete/frozen: executable Schema v1 + `reference` + `vault`.
- Backend Phase 2 is complete/frozen after owner commit/push.
- Backend Phase 3 (`people`) is complete/frozen after owner commit/push.
- The Phase 1–3 milestone is `MILESTONE_READY`.
- The pre-Phase-4 code-hygiene maintenance is complete/frozen after owner commit/push.
- Backend Phase 4 (`fiction`) is complete/frozen after owner commit/push.
- Backend Phase 5 (`film`) is complete/frozen after owner commit/push.
- Backend Phase 6 (`media` + `location`) is complete/frozen after owner commit/push.
- The Phase 4–6 milestone is `MILESTONE_READY` after committed privacy-safe constraint-logging maintenance and
  post-milestone synchronization/reset.
- Backend Phase 7 (`account`) is complete/frozen after owner commit/push. Its completed handoff is archived under
  `docs/implementation/phase-7/handoff.md`.
- Backend Phase 8 (`knowledge`) is complete/frozen after owner commit/push.
- Backend Phase 9 (`collection`) preparation is `READY FOR HANDOFF` after Codex re-review; owner commit/push is
  pending, and no implementation handoff is active.

Completed maintenance scope:

- `docs/implementation/maintenance/milestone-1-3-concurrency/README.md`
- `docs/implementation/maintenance/pre-phase4-code-hygiene/README.md`
- `docs/implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md`

Architecture-sensitive work must respect:

- `docs/architecture/`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `docs/repository/repository-package-tree.md`
- `docs/adr/`
- `docs/implementation/phase-1/README.md`

## Development workflow

The owner no longer writes production implementation.

The default workflow is:

1. Owner approves the next phase concept.
2. ChatGPT prepares next-phase docs/tooling without production implementation.
3. Codex runs `$codex-pre-handoff-review`.
4. Owner commits/pushes preparation after `READY FOR HANDOFF`.
5. Codex runs `$codex-create-handoff`.
6. Antigravity implements/tests with `/antigravity-implement-handoff`.
7. Codex runs `$codex-final-review`.
8. Owner commits/pushes after `READY FOR OWNER COMMIT`.
9. ChatGPT closes/freezes the phase and prepares the next.
10. After Phase 3/6/9/12/15, Codex runs `$codex-milestone-review`.

Agents never commit, push, tag, or create/merge PRs.

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
- Repository / Package Tree v1.1
- Backend Phase 0 bootstrap baseline
- Backend Phase 1 reference/vault foundation baseline
- Backend Phase 2 authentication/settings foundation baseline
- Backend Phase 3 people foundation baseline
- Backend Phase 4 fiction foundation baseline
- Backend Phase 5 film foundation baseline
- Backend Phase 6 media/location foundation baseline
- Backend Phase 7 account foundation baseline
- Backend Phase 8 knowledge foundation baseline

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
