# ADR-0006 — Model the Application as Permanently Single-User

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The owner explicitly requires the system to remain single-user permanently. Multi-tenant/user sharing is not a future requirement.

## Decision

Keep one authentication account and do not add `user_id` tenant columns to every domain table. Provide no public registration or user-management CRUD. Initialize the account through a one-time bootstrap/setup flow.

## Rationale

Repeated tenant keys would add schema/code complexity without representing a real domain requirement.

## Consequences

- Authorization does not need tenant filtering throughout repositories.
- Converting to multi-user in the future would require a deliberate redesign/migration; this is accepted because multi-user support is explicitly out of scope.

## Alternatives considered

- **Design for hypothetical multi-user later:** rejected as speculative complexity that contradicts the stated permanent single-user requirement.
