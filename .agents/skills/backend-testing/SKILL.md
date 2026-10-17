---
name: backend-testing
description: Design backend tests with JUnit 5, AssertJ, Mockito where useful, Spring Modulith tests, Testcontainers PostgreSQL, and focused-to-final Maven verification.
---

# Backend Testing

Choose the narrowest test that proves the behavior:

- domain/value logic → JUnit 5 + AssertJ;
- collaborator behavior → Mockito only where useful;
- module integration → Spring Modulith tests;
- repository/schema behavior → Testcontainers PostgreSQL;
- HTTP → MockMvc when controllers exist.

Never use H2 as a PostgreSQL substitute. Test meaningful invariants, failure paths, and boundaries. Keep tests
deterministic and order-independent. Iterate focused tests while coding, then run the handoff final verification
command and retain exact evidence.

For Phase 18–22's mixed Java/Python RAG work, prefer focused pytest/Ruff/mypy before the one required complete gate. Use `bounded-command-execution`: ordinary commands <=1,200 s, explicitly required future full-suite commands <=2,100 s if the host CLI supports it. Preserve exact independent evidence and avoid duplicate full suite runs when only docs changed. Coverage should be proportional to the actual new risk, never a fixed arbitrary test-count quota.
