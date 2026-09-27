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
