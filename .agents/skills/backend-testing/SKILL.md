---
name: backend-testing
description: Design backend tests with JUnit 5, AssertJ, Mockito where useful, Spring Modulith tests, Testcontainers PostgreSQL, and focused-to-final Maven verification.
---

# Backend Testing

**Current execution policy:** review/test only affected requirements for approved slice and one phase-final gate when justified. Nonblocking LOW/INFO go to milestone/audit; open CRITICAL/HIGH/MEDIUM block commit. Watchdog executable remains old until Maintenance B; new 5-minute default/5-minute inactivity diagnosis is only *approved design*, not implemented. RAG P17/P18 immutable historical test receipts cannot be retroactively changed.


Choose the narrowest test that proves the behavior:

- domain/value logic → JUnit 5 + AssertJ;
- collaborator behavior → Mockito only where useful;
- module integration → Spring Modulith tests;
- repository/schema behavior → Testcontainers PostgreSQL;
- HTTP → MockMvc when controllers exist.

Never use H2 as a PostgreSQL substitute. Test meaningful invariants, failure paths, and boundaries. Keep tests
deterministic and order-independent. Iterate focused tests while coding, then run the handoff final verification
command and retain exact evidence.

For approved mixed Java/Python RAG work, prefer focused pytest/Ruff/mypy plus risk-relevant integration checks. Use the **currently implemented** `bounded-command-execution` with old <=1,200s/<=2,100s caps until Maintenance B replaces it with the approved 5-minute default and 5-minute inactivity diagnostic. One phase-final gate is justified when source/evidence require it, not every slice/fix. Preserve exact evidence and avoid duplicate full suite runs for only docs changes. Coverage should be proportional to actual risk, never an arbitrary test-count quota.
