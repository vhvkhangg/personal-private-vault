# Phase 4–6 Milestone Privacy-Safe Constraint Logging Maintenance

Status: **COMPLETE / FROZEN** after Codex final re-review and owner commit/push `9449b9e`.

Owner approval: **2026-09-29**

The completed maintenance handoff remains in [`../../handoffs/ACTIVE.md`](../../handoffs/ACTIVE.md) until
post-milestone synchronization/reset; its `READY_FOR_OWNER_COMMIT` field is historical.

Source finding:
[`../../phase-6/reviews/2026-09-29-phase-4-6-milestone-codex-review.md`](../../phase-6/reviews/2026-09-29-phase-4-6-milestone-codex-review.md)

This is a single, narrowly scoped maintenance slice for the blocking Phase 4–6 milestone finding: expected
PostgreSQL uniqueness conflicts currently cause Hibernate to emit vendor-detail warning lines containing private
business values.

Phase 7 remains blocked.

## Goal

Prevent expected uniqueness conflicts from writing raw private values into application/test logs while preserving:

- the current domain-level conflict behavior;
- useful non-sensitive diagnostics;
- PostgreSQL/Flyway behavior;
- existing transaction and concurrency guarantees.

The maintenance must not become a general logging rewrite.

## Confirmed leak

The milestone review observed `org.hibernate.orm.jdbc.error` warnings such as PostgreSQL:

```text
Detail: Key (object_key)=(...) already exists.
Detail: Key (checksum_sha256)=(...) already exists.
```

The raw values can include:

- media object-storage keys;
- content checksums/fingerprints;
- Fiction/Film genre names;
- Location category names;
- names/identifiers from earlier expected uniqueness paths.

Exception-to-domain translation does not stop Hibernate from logging the JDBC exception before application code
translates it.

## Privacy-safe diagnostic policy

For **expected, handled constraint conflicts**:

1. logs must not contain the rejected business value, SQL bind value, raw PostgreSQL `Detail:` value, raw SQL text
   containing private values, or `Throwable#getMessage()`/root-cause text that contains those values;
2. safe diagnostics may contain non-sensitive metadata such as:
   - application/module operation name;
   - stable event code;
   - SQLState (for example `23505`);
   - known constraint name/schema identifier when useful;
   - exception class/type;
3. the public/domain conflict result must remain unchanged;
4. the rejected value may remain in an existing domain exception message only if that exception is not automatically
   logged by this backend path; do not broaden this maintenance into a public exception-contract redesign unless a
   test proves the exception itself is logged;
5. unexpected persistence failures must still propagate/fail visibly. Do not hide all Hibernate/JDBC diagnostics
   globally without preserving an actionable non-sensitive failure signal.

The implementation should choose the narrowest configuration/code combination that satisfies this policy.

## Preferred implementation direction

First evaluate a targeted logging configuration for the Hibernate JDBC error category that currently emits the raw
PostgreSQL detail:

```text
org.hibernate.orm.jdbc.error
```

If raising/suppressing that category's expected WARN output is sufficient, keep the configuration narrow. Do not
disable root logging or all Hibernate logging.

If a replacement application-level warning is needed to retain useful telemetry, emit only sanitized metadata such
as the operation/event code, SQLState, and recognized constraint name. Never interpolate the rejected value or the
raw persistence exception message.

Do **not** introduce a repository-wide generic logging framework, custom Logback/TurboFilter stack, AOP layer, or
cross-module "common" utility unless Codex proves the narrow configuration/service solution cannot satisfy the
regression contract.

## Required audit scope

Audit the currently implemented expected uniqueness/conflict paths that intentionally catch/translate persistence
constraint failures, including at least:

- Authentication bootstrap account uniqueness;
- Vault tag creation;
- People creator-group name uniqueness;
- Fiction genre name uniqueness;
- Film genre name uniqueness;
- Location category name uniqueness;
- Media Image `object_key` uniqueness;
- Media Image `checksum_sha256` uniqueness.

The audit is to verify that the logging fix covers them. It does **not** authorize unrelated refactors of those
services.

Set-like `INSERT ... ON CONFLICT DO NOTHING` paths are not conflict-log targets unless evidence shows they emit the
same private-value warning.

## Likely implementation targets

Keep the handoff minimal. Expected targets are:

- `backend/src/main/resources/application.yml`;
- focused logging/privacy regression tests, preferably under existing integration-test support;
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaIntegrationTest.java` or a dedicated
  persistence-logging integration test if that is cleaner.

Frozen service classes listed in the audit may be touched only if a narrowly scoped sanitized application log is
required to preserve diagnostics after the Hibernate category is restricted.

Do not change public APIs, entities, repositories, SQL, DBML, or Flyway for this maintenance.

## Mandatory regression tests

Use the real Spring Boot logging configuration and PostgreSQL Testcontainers.

### Image object-key privacy regression

Force the **database** unique-conflict path, not a pre-check-only path:

1. create/hold a competing Image insert so the service call reaches PostgreSQL's unique constraint;
2. use an object key containing a distinctive private marker such as `PPV_PRIVATE_OBJECT_KEY_MARKER`;
3. verify the competing call still returns/throws the existing `ImageConflictException`;
4. capture application logs across the conflicting operation, including the worker thread;
5. assert the private marker does **not** appear anywhere in captured output;
6. assert raw PostgreSQL `Detail: Key (object_key)=(` output is absent;
7. assert committed database/Vault rollback behavior remains unchanged.

Use deterministic PostgreSQL contention/lock observation already established by the existing Phase 6 tests; do not
replace it with timing-only sleeps.

### Image checksum privacy regression

Repeat the same database-conflict proof for a distinctive valid checksum marker/value and assert neither the
checksum nor raw `Detail: Key (checksum_sha256)=(` appears in logs.

### Cross-path coverage

Because the proposed logger change may be global to Hibernate's JDBC-error category, add at least one additional
name-based expected uniqueness conflict regression (Fiction genre, Film genre, Location category, Creator Group, or
Vault tag) proving a distinctive private name is not emitted in logs while the existing domain result is preserved.

If implementation instead changes each conflict path individually, test every modified path.

### Diagnostic preservation

Prove the maintenance does not silently swallow the conflict:

- existing domain exception/result remains observable;
- if sanitized application telemetry is introduced, assert the safe event/constraint metadata is present and the
  private value is absent;
- do not assert or retain raw vendor detail as "useful diagnostics."

## Evidence contract

After focused regressions pass, run:

```text
mvn -f backend/pom.xml clean verify
```

and:

```text
git diff --check
```

Record:

- exact commands and exit status;
- Java/Maven versions;
- PostgreSQL/Testcontainers version;
- total tests/failures/errors/skips;
- focused privacy-log test names/results;
- whether `org.hibernate.orm.jdbc.error` is configured and at what effective level;
- confirmation that the distinctive private markers are absent from captured logs;
- Spring Modulith and Flyway/Hibernate validation results.

Evidence file:

`docs/implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/test-evidence.md`

## Non-goals

Do **not**:

- implement Phase 7;
- change DBML, Flyway, database constraints, or SQL semantics;
- redesign domain exception messages without evidence that they are themselves logged;
- change uniqueness/business semantics;
- remove deterministic concurrency coverage;
- suppress root logging or all Hibernate logging;
- blanket-catch unexpected persistence failures;
- add a generic logging/exception framework, AOP, event system, or custom agent/hook;
- refactor frozen Phase 1–6 services except where strictly necessary for sanitized diagnostics.

## Required engineering skills

Use existing skills:

- `java-spring-coding-standards`
- `jpa-postgresql-persistence`
- `backend-testing`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`

Also apply the repository security/privacy rule from root `AGENTS.md`: never log passwords, PINs, JWTs, refresh
tokens, secrets, or sensitive payload values.

No new domain skill, custom agent, or hook is required.

## Workflow

1. Codex: `$codex-create-handoff` from this approved maintenance scope.
2. Antigravity: `/antigravity-implement-handoff`.
3. Codex: `$codex-final-review`.
4. Owner: commit/push only after `READY FOR OWNER COMMIT`.
5. Codex: rerun `$codex-milestone-review` for Phases 4–6.
6. Phase 7 remains blocked until the milestone is `MILESTONE_READY`, the owner commits/pushes milestone status docs,
   and ChatGPT performs post-milestone synchronization/reset.
