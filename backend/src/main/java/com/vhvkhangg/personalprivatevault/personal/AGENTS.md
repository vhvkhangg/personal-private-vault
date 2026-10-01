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
