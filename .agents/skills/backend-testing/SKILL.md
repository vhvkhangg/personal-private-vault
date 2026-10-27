---
name: backend-testing
description: Design backend tests with JUnit 5, AssertJ, Mockito where useful, Spring Modulith tests, Testcontainers PostgreSQL, and focused-to-final Maven verification.
---

# Backend Testing

**Current execution policy:** review/test only affected requirements for approved slice and one phase-final gate when justified. Nonblocking LOW/INFO go to milestone/audit; open CRITICAL/HIGH/MEDIUM block commit. Maintenance B1/B2 are independently accepted and Owner-published: 300s ordinary hard cap, up to 2,100s for preapproved full suites, bounded inactivity diagnosis and scoped Windows Git/hook enforcement. RAG P17/P18 immutable historical test receipts cannot be retroactively changed.


Choose the narrowest test that proves the behavior:

- domain/value logic → JUnit 5 + AssertJ;
- collaborator behavior → Mockito only where useful;
- module integration → Spring Modulith tests;
- repository/schema behavior → Testcontainers PostgreSQL;
- HTTP → MockMvc when controllers exist.

Never use H2 as a PostgreSQL substitute. Test meaningful invariants, failure paths, and boundaries. Keep tests
deterministic and order-independent. Iterate focused tests while coding, then run the handoff final verification
command and retain exact evidence.

For approved mixed Java/Python RAG work, prefer focused pytest/Ruff/mypy plus risk-relevant integration checks. Use `bounded-command-execution` (Maintenance B1/B2 Owner-published: default 300s / approved full-suite 2,100s with inactivity diagnosis). One phase-final gate is justified when source/evidence require it, not every slice/fix. Preserve exact evidence and avoid duplicate full suite runs for only docs changes. Coverage should be proportional to actual risk, never an arbitrary test-count quota.
