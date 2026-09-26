---
name: antigravity-test-slice
description: Write tests for a completed backend implementation slice, run the relevant tests exactly once, and report the result without modifying production code.
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
7. Run the narrowest relevant Maven test command exactly once.
8. Capture:
   - the exact command;
   - exit status;
   - passed/failed tests;
   - failure messages and likely source location.
9. If the run fails:
   - stop the test-run loop;
   - do not automatically rerun;
   - do not alter production implementation unless the owner explicitly asks.
10. Summarize missing test coverage separately from implementation defects.

Do not commit or push.
