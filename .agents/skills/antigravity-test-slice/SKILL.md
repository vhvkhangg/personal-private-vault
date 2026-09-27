---
name: antigravity-test-slice
description: Write tests for a completed backend implementation slice, run one explicit test pass, and report the result without modifying production code.
---

# Antigravity Test Slice

Use this skill after the repository owner finishes an implementation slice and asks Antigravity to test it.

## Procedure

1. Read root `AGENTS.md`, `backend/AGENTS.md`, and `backend/src/test/AGENTS.md`.
2. Identify the changed module and its accepted architecture/database constraints.
3. Inspect the implementation before choosing test types.
4. Add only the tests necessary for the completed slice.
5. Prefer:
   - JUnit 5 + AssertJ for domain behavior;
   - Mockito for useful collaborator isolation;
   - `@ApplicationModuleTest` for module integration;
   - Testcontainers PostgreSQL for persistence;
   - MockMvc for HTTP behavior.
6. Never replace PostgreSQL integration behavior with H2.
7. Run the narrowest relevant Maven test command once for this explicit test pass. For Backend Phase 0 from repository root, run the direct command `mvn -f backend/pom.xml test`. On Windows, do not wrap approved read-only Git/Maven commands in `powershell -Command` or `cmd /c`, and do not add `git -c` / `git -C` unless actually required; direct commands match the curated Antigravity CLI permission rules.
8. Capture:
   - the exact command;
   - exit status;
   - passed/failed tests;
   - failure messages and likely source location.
9. If the run fails:
   - stop the test-run loop;
   - do not automatically rerun in the same pass;
   - do not alter production implementation unless the owner explicitly asks.
   - if the owner fixes the reported implementation/build issue and explicitly invokes this skill again, that is a new test pass and may execute the command once.
10. Summarize missing test coverage separately from implementation defects.

Do not commit or push.

For one-time safe-command auto-approval, follow `docs/implementation/antigravity-cli-permissions.md`.
