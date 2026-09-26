# API Architecture

## 1. Style

The backend exposes a **REST/JSON** API documented with OpenAPI/Swagger.

GraphQL is not part of the v1 architecture because no current use case requires client-selected graph traversal or a second API model.

## 2. Ownership

REST controllers live inside the module that owns the use case. A controller must call its module's application layer; it must not orchestrate another module's repositories directly.

Cross-module work is performed through public module APIs.

## 3. Response consistency

The project requires a standardized `ApiResponse` JSON contract for success and error responses.

The exact field-level shape is intentionally **not frozen in architecture v1**. It must be finalized before the first REST controller so all controllers use the same contract from the start.

The contract should be able to represent:

- result data;
- stable machine-readable error information;
- human-readable error detail;
- request/correlation metadata when introduced;
- pagination metadata where applicable.

Do not invent module-specific response envelopes.

## 4. Validation

Validation occurs at boundaries and in the domain:

- request DTO validation rejects structurally invalid input;
- application/domain validation enforces business invariants;
- database constraints provide the final integrity layer.

Do not rely only on frontend validation.

## 5. Error handling

Use centralized exception-to-response translation. Internal exception classes, SQL details, stack traces, and secrets must never be exposed in production API responses.

Different failure classes should remain distinguishable, for example:

- invalid request;
- authentication failure;
- not found;
- conflict/duplicate;
- business rule violation;
- unexpected server failure.

Exact error codes and HTTP mapping are finalized during backend bootstrap.

## 6. Filtering and search

Resource APIs may provide module-specific filtering. Global search is handled by the dedicated `search` module.

Required global search behavior includes:

- case-insensitive search;
- partial matching;
- fuzzy matching for close names;
- filtering by module/type/tag where applicable;
- searchable text such as titles/names, original titles, descriptions, reviews, notes, Markdown content, character names, and tags.

Accent-insensitive Vietnamese matching is explicitly not required.

## 7. Pagination and sorting

List endpoints must be pageable when result size can grow materially. Pagination defaults are configuration-driven.

The exact wire format for page metadata is deferred until the shared `ApiResponse` contract is finalized.

## 8. OpenAPI

Controllers/operations must expose meaningful OpenAPI descriptions. Schema/endpoint descriptions should explain domain semantics rather than merely restating Java names.

OpenAPI documentation is generated from the running backend and should remain consistent with the actual DTOs and validation rules.

## 9. API versioning

A concrete URL versioning convention is intentionally not frozen yet. It must be decided before publishing the first externally consumed endpoint. Avoid adding version segments speculatively before the API surface exists.
