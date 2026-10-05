# Personal Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.personal`.

## Ownership

Personal owns only `personal_profiles`.

## Boundary

Allowed dependencies:

- public Reference catalog/view;
- public Location Address/view.

Never import Location/Reference internals. Personal is not Vault-backed.

## Invariants

- name and free-form relationship are required;
- gender is MALE/FEMALE when present;
- nationality is validated via Reference;
- address is validated via public AddressOperations;
- at most one non-deleted `is_self = true` profile;
- PostgreSQL partial uniqueness is the final race arbiter for active-self conflicts;
- self soft delete frees the slot; conflicting self restore fails stably;
- do not auto-demote another self profile;
- non-null notes Markdown is preserved exactly;
- soft delete/restore is Personal-owned;
- list reads are positive-bounded and deterministic.

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
