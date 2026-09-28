# Testing and Review Architecture

## 1. Responsibility workflow

The canonical development workflow is:

```text
Repository owner
  -> approves the next phase/slice and its constraints

Codex
  -> creates the implementation handoff
  -> identifies acceptance criteria, test/evidence requirements, and relevant engineering skills

Antigravity
  -> implements production code within the approved handoff
  -> writes/updates tests for the same behavior
  -> iteratively compiles/tests/fixes its implementation
  -> runs the handoff's final verification command
  -> retains exact test/build evidence

Codex
  -> performs final review
  -> requests scoped remediation when needed
  -> otherwise returns READY FOR OWNER COMMIT with one Conventional Commit message

Repository owner
  -> commits and pushes
```

The owner may run local environment-dependent verification or remediation commands when required, but production
implementation and routine test implementation are delegated to Antigravity after a Codex handoff.

Historical Phase 0/1 documents may describe the workflow that existed when those records were created; those
historical records do not override this current architecture contract.

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

Use Spring Modulith module tests to bootstrap a bounded application module and allowed collaborators instead of
the entire application where suitable.

### Persistence integration tests

Use PostgreSQL Testcontainers rather than H2 as a substitute for PostgreSQL behavior. This is especially important
for PostgreSQL-specific constraints, JSONB, indexes/extensions, and transaction behavior.

### API integration tests

Validate request validation, HTTP status behavior, response contract, authorization boundaries, and exception mapping.

### Architecture verification

Verify Spring Modulith module boundaries and allowed dependencies. Cyclic dependencies and access to non-exposed
module internals are architecture failures.

## 4. Test ownership rules

Antigravity owns tests for the production behavior it implements under the active handoff. Tests must independently
prove meaningful behavior/invariants rather than merely mirror implementation details.

During implementation, Antigravity may run focused tests repeatedly while converging. After convergence it runs
the handoff's agreed final verification command and retains the exact command/result/evidence for Codex.

Tests may access internals only to the extent appropriate for testing the owning module; tests must not normalize
production code that violates module boundaries.

A passing test is not evidence that cross-module repository access or another architecture violation is acceptable.

## 5. Frontend E2E

Frontend testing is deferred with frontend implementation. The planned E2E tool is Playwright. Frontend
unit/component tooling will be finalized when the frontend phase begins.

## 6. Review records

Significant Codex review findings, remediation results, and architecture-sensitive corrections are retained under
`docs/reviews/` and the relevant phase implementation folder.

Do not mix transient review chatter into ADRs unless it results in an accepted architecture decision.
