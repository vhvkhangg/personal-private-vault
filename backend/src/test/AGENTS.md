# Test Scope Instructions

These instructions apply under `backend/src/test/`.

## Ownership

Antigravity owns test implementation for the active Codex handoff and may update production code in the same handoff
when tests reveal defects in its own implementation.

Codex final review does not rewrite tests by default.

## Test loop

Within `/antigravity-implement-handoff`:

1. implement the scoped production change;
2. add the narrowest meaningful tests;
3. run focused compile/tests;
4. fix implementation/test defects;
5. rerun as needed;
6. run the agreed final Maven test command;
7. retain test evidence for Codex.

Avoid unbounded loops. After three failed full-suite attempts for the same unresolved cause, stop and report the blocker.

## Test design

- Domain logic: JUnit 5 + AssertJ.
- Collaborator isolation: Mockito only when useful.
- Module integration: Spring Modulith test support.
- Persistence: Testcontainers PostgreSQL.
- Prefer Spring Boot `@ServiceConnection` when it reduces connection boilerplate.
- HTTP: MockMvc when controllers exist.
- Never substitute H2 for PostgreSQL.

Tests must cover behavior/invariants, failure paths, and meaningful boundary conditions rather than mirror implementation.
