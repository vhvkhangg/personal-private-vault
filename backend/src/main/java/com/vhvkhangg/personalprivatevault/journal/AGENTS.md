# Journal Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.journal`.

## Ownership

Journal owns only `diary_entries`.

Journal has no application-module dependency and is not Vault-backed.

## Invariants

- multiple diary entries per calendar day are allowed;
- non-null `content_markdown` is preserved exactly;
- title is optional and max 500;
- default reads exclude soft-deleted rows;
- soft delete/restore is Journal-owned;
- date/range reads require a positive bound and deterministic `entry_date DESC, id DESC`;
- no global/full-text Search implementation in Phase 11;
- no hard delete.

## Phase gate

Production changes require Phase 11 `READY FOR HANDOFF` plus an active Phase 11 handoff.

## Phase 13 HTTP adapter exception

An accepted active Phase 13 handoff may modify this otherwise-frozen module only to add the REST/JSON adapter work
authorized by `docs/implementation/phase-13/README.md`.

Allowed:

- owner `internal/web` controller/request/response DTO/mapper/advice packages;
- mapping to existing owner operations/facades;
- HTTP Bean Validation and OpenAPI annotations;
- module exception-to-HTTP translation;
- tests needed for this HTTP surface.

Not allowed:

- changing existing domain/application invariants or persistence behavior;
- importing another module's internal/repository/entity;
- exposing application entities or internal Search contracts;
- inventing new business operations solely for HTTP convenience;
- schema/Flyway changes;
- Phase 14+ work.

The active Phase 13 handoff, when present, is the temporary authority for this narrow adapter exception. Otherwise
the frozen-module rules remain in force.
