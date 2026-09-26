# Test Scope Instructions

These instructions apply whenever creating or reviewing backend tests.

## Workflow

Antigravity owns test generation for completed implementation slices unless the owner explicitly requests otherwise.

For the normal workflow:

1. read the implementation diff and relevant module documentation;
2. add tests only for the completed slice;
3. run the relevant test command exactly once;
4. report the exact command, exit status, and failures;
5. do not automatically rerun after a failure;
6. do not change production code to satisfy a failing test unless explicitly asked.

Codex final review should inspect these tests and the recorded result, but should not rerun them by default.

## Test design

Use the narrowest test type that verifies the behavior correctly.

- Pure domain/business logic: JUnit 5 + AssertJ.
- Collaborator behavior: Mockito only when isolation is useful.
- Application module integration: Spring Modulith testing / `@ApplicationModuleTest`.
- Repository/database integration: Testcontainers PostgreSQL.
- HTTP/API behavior: MockMvc with appropriate Spring context.
- Full application tests only when cross-module behavior genuinely requires them.

Do not use H2 as a PostgreSQL substitute.

## Quality

- Test externally meaningful behavior, invariants, failure paths, and boundary conditions.
- Avoid tests that merely mirror implementation statements.
- Avoid excessive mocking of value objects/entities.
- Use deterministic data and clocks where time affects behavior.
- Do not depend on test execution order.
- Keep test names readable and behavior-focused.
- Verify security-sensitive negative cases when the slice touches authentication/authorization.
