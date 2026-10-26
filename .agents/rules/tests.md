---
trigger: glob
globs: "backend/src/test/**/*.java"
description: "Tests written by Antigravity as part of an active Codex handoff."
---

**CURRENT TEST POLICY:** Scope tests to approved source/risks; Antigravity self-review before Codex; independent Codex review per slice, final review per phase. Unchanged receipts may be reused by hash. LOW/INFO backlog once at milestone/audit, CRITICAL/HIGH/MEDIUM block. Maintenance B1 watchdog is published (default 300s ordinary cap, 2,100s full-suite cap, 5-minute inactivity diagnostics); B2 tooling enforcement implemented. See [Owner policy](../../docs/workflow/current-owner-delivery-policy.md).



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
