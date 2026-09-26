# Testing and Review Architecture

## 1. Responsibility workflow

The agreed workflow is intentionally explicit:

```text
Repository owner
  -> writes implementation code
  -> hands completed slice to Antigravity

Antigravity
  -> writes tests
  -> runs tests once

Repository owner
  -> fixes reported failures/issues

Codex
  -> performs final review

If accepted
  -> commit
  -> push main
```

The repository owner is not expected to write the project test suite by default unless explicitly choosing to do so for a particular task.

## 2. Planned backend test stack

- JUnit 5
- AssertJ
- Mockito
- Spring Boot Test
- Spring Modulith Test
- Testcontainers with PostgreSQL
- MockMvc for HTTP/controller integration where appropriate

Exact dependency versions are selected from the stable Spring Boot-compatible set at implementation time.

## 3. Test categories

### Unit tests

For pure domain/application behavior with no Spring container where possible.

### Module tests

Use Spring Modulith module tests to bootstrap a bounded application module and allowed collaborators instead of the entire application where suitable.

### Persistence integration tests

Use PostgreSQL Testcontainers rather than H2 as a substitute for PostgreSQL behavior. This is especially important for PostgreSQL-specific constraints, JSONB, indexes/extensions, and transaction behavior.

### API integration tests

Validate request validation, HTTP status behavior, response contract, authorization boundaries, and exception mapping.

### Architecture verification

Verify Spring Modulith module boundaries and allowed dependencies. Cyclic dependencies and access to non-exposed module internals are architecture failures.

## 4. Test ownership rules

Tests may access internals only to the extent appropriate for testing the owning module; tests must not normalize production code that violates module boundaries.

A test passing is not evidence that cross-module repository access is acceptable.

## 5. Frontend E2E

Frontend testing is deferred with frontend implementation. The planned E2E tool is Playwright. Frontend unit/component tooling will be finalized when the frontend phase begins.

## 6. Review records

When Codex/Antigravity review notes become part of the development process, significant findings and architectural corrections should be recorded under a dedicated review-log structure introduced with the agent/workflow documentation phase. Do not mix transient review chatter into ADRs unless it results in an accepted architecture decision.
