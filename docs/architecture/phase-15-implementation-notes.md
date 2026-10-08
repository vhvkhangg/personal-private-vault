# Phase 15 Implementation Notes

This document canonicalizes backend implementation patterns, lifecycle mechanisms, error contracts, and concurrency guards established during the Backend Phase 15 comprehensive audit and remediation.

## 1. Boot-Managed Flyway Startup and Hibernate Validation

### Context
To maintain reliable application startup across development, testcontainers, and production deployments, PostgreSQL migrations must be strictly applied and validated before JPA entity initialization.

### Mechanism
- Flyway migrations are managed natively by Spring Boot and execute immediately upon DataSource readiness.
- Hibernate runs with `spring.jpa.hibernate.ddl-auto: validate`, ensuring schema metadata is verified rather than mutated.
- The startup lifecycle is verified by `FlywayStartupIntegrationTest`, proving that clean container initialization applies all migrations before Hibernate validates entity mappings.

## 2. Authentication Password Encoding and Legacy Verification

### Context
User authentication must enforce password strength while supporting backward compatibility with legacy password hashes without truncating input characters.

### Mechanism
- The single-user vault enforces master password length between 12 and 128 characters during initial bootstrap (`BootstrapService`); login requests (`LoginRequest`) require `@NotBlank` and `@Size(max = 128)`. No public registration or self-service password update endpoints exist.
- A `DelegatingPasswordEncoder` strategy is configured:
  - Default password encoder uses PBKDF2 (`pbkdf2`) for full-input credentials without truncation (supporting ASCII 12–128 characters and multibyte strings > 72 bytes).
  - Legacy BCrypt hashes (`{bcrypt}`) remain verifiable transparently via Spring Security's delegation prefixing.
- Private PIN verification (`PrivatePinService.verifyPin`) validates a 6-digit numeric format and verifies hash equality in an authenticated context; the backend does not implement rate-limiting or server-side auto-lock timers (client-side auto-lock duration in settings serves as an inactivity threshold).

## 3. Standardized HTTP Error Contracts and Domain Translation

### Context
API consumers require predictable, structured error responses matching vault API conventions rather than internal exceptions or uninformative 500 status codes.

### Mechanism
- **Envelope Consistency**:
  - REST JSON API responses and all error responses strictly use the standard `ApiResponse<T>` envelope containing `data`, `error` (`ApiError(code, message, fieldErrors)`), and `meta` fields (not RFC 7807 Problem Details). Approved exceptions to the `ApiResponse` payload envelope are successful binary media streams (e.g., `ImageController` raw binary image downloads) and portability archives (`PortabilityController` zip streams), which stream raw binary response bodies directly with appropriate Content-Type headers.
- **Content Negotiation**:
  - Unsupported `Content-Type` headers produce HTTP 415 `UNSUPPORTED_MEDIA_TYPE`.
  - Unacceptable `Accept` headers produce HTTP 406 `NOT_ACCEPTABLE`.
- **Collection Member Validation**:
  - Null collection members (e.g., in batch import decisions, financial ledger entries, or location schedules) produce HTTP 400 `VALIDATION_ERROR` with explicit path/index pointers (such as `itemDecisions[0]`, `entries[0]`, `intervals[0]`).
- **Monetary and Numeric Bounds**:
  - Numeric amounts violating width (> 15 integer digits) or scale (> 4 decimal places) are rejected pre-persistence without rounding; HTTP endpoints return HTTP 422 with domain-specific error codes (`BRAND_INVALID`, `LOCATION_INVALID`, `KNOWLEDGE_INVALID`, `INVALID_COLLECTION`).
- **Public Knowledge Domain Translation**:
  - Ingestion and conversion flows (Feed-to-Knowledge and Import-to-Knowledge) translate public Knowledge exceptions into specific HTTP responses: HTTP 422 `INVALID_KNOWLEDGE_ITEM` (for invalid field combinations or study/vocabulary constraints), HTTP 409 `KNOWLEDGE_CONFLICT` (for duplicate note content hashes), and HTTP 404 `KNOWLEDGE_NOT_FOUND` where applicable, preventing unhandled domain exception leakage.
- **Binary Streaming Failures**:
  - Known storage failures retain their specific mappings (e.g. HTTP 409 `STORAGE_DISABLED` or `STORAGE_INTEGRITY_ERROR`).
  - Streaming I/O failures occurring on initial stream read prior to HTTP commitment clear binary headers and return HTTP 500 `INTERNAL_ERROR` JSON envelope; stream failures occurring after commitment terminate the connection safely without appending trailing JSON.

## 4. Bounded Import Review and Full Reachability

### Context
Large imports must provide bounded, paginated preview and review capabilities without unbounded database query overhead or missing items.

### Mechanism
- Bounded review endpoint `GET /api/v1/imports/jobs/{id}/items` accepts `page` (default 0, minimum 0) and `limit` (default 50, range 1–100).
- In compliance with vault API conventions for bounded lists, the envelope produces `meta == null`.
- Ordered deterministic traversal (`item_index ASC`) guarantees complete reachability across multiple consecutive pages without duplicate or omitted entries.
- Transactional rollback guarantees that execution failures occurring beyond the initial page preserve prior import job, item, user decision, and imported entity reference states, rolling back newly created entities atomically.

## 5. Narrow Cross-Module Concurrency Guard and Synchronous Coordination

### Context
Concurrent modifications between Account updates and Study references must preserve module encapsulation and enforce the invariant that Study items only reference valid external accounts.

### Mechanism
- Module dependency boundaries remain strictly intact: `knowledge` depends on `account`, with zero reverse dependency from `account` onto `knowledge`.
- The `account` module defines a public SPI: `ExternalAccountMutationGuard`.
- The `knowledge` module provides a concrete Spring bean: `StudyExternalAccountGuard` implementing `ExternalAccountMutationGuard`.
- When updating an external account's type or platform, `ExternalAccountService.update` acquires a pessimistic row lock on `external_accounts` in PostgreSQL (`SELECT ... FOR UPDATE`), refreshes the managed entity, and synchronously invokes `guard.validateMutation(accountId, newType, newPlatform)` on registered guards within the Account update transaction.
- `StudyExternalAccountGuard` executes a Study-owned query verifying whether any existing `StudyItem` references that account with incompatible type or platform requirements. If an invariant violation would occur, it throws `ExternalAccountConflictException`, which `AccountExceptionAdvice` translates to HTTP 409 `EXTERNAL_ACCOUNT_CONFLICT` to abort the Account mutation safely.
- Competing writer transactions between Account mutations and Study item creation/update are serialized deterministically via row-level locks on `external_accounts`: both update and creation paths acquire row-level locks on `external_accounts` before evaluating cross-module invariants, specifically serializing the exercised bidirectional contention orders between Account and Study writers so competing transactions block and either commit or safely roll back without invariant corruption.
