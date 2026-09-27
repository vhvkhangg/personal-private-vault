---
trigger: glob
globs: "backend/src/test/**/*.java"
description: "Tests written by Antigravity as part of an active Codex handoff."
---

# Test Rule

- Antigravity implements tests and production fixes within the active handoff.
- Prefer focused tests during implementation, then one final agreed Maven test command for evidence.
- Use JUnit 5 + AssertJ by default.
- Mockito only where isolation is valuable.
- Testcontainers PostgreSQL for persistence.
- Spring Modulith tests for module behavior/boundaries.
- No H2 substitution.
- Keep tests deterministic, independent, behavior-focused, and security-aware.
- Stop and report after repeated full-suite failures that do not converge; do not hide a blocker with weaker tests.
