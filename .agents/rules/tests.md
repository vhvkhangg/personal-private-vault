---
trigger: glob
globs: "backend/src/test/**/*.java"
description: "Test-generation rules for the Antigravity test phase and Codex review."
---

# Test Rule

- The owner writes production implementation; Antigravity writes tests for the completed slice.
- Run the relevant test command once per requested test pass.
- On failure, report it; do not silently retry.
- Do not rewrite production code from the test task unless explicitly asked.
- Use JUnit 5 and AssertJ by default.
- Use Mockito only where isolation is valuable.
- Use Testcontainers PostgreSQL for persistence integration tests.
- Do not use H2 as a PostgreSQL replacement.
- Use Spring Modulith tests for module-level integration/boundary behavior.
- Keep tests deterministic, independent, and behavior-focused.
