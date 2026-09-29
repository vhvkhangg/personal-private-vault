---
name: codex-final-review
description: Final-review an implemented active handoff or explicit owner-approved maintenance/governance slice; request remediation or return READY FOR OWNER COMMIT with one commit message.
---

# Codex Final Review

Invoke with `$codex-final-review`. Codex is review-only.

## Review-mode gate

### Implementation mode

Use only when `docs/implementation/handoffs/ACTIVE.md` is `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

### Maintenance/governance mode

Use only for an explicit owner-approved scope document. A frozen phase may be touched only where that scope says so.

### No valid mode

If neither mode applies, stop and ask for the approved review scope. Never infer/reopen a frozen phase.

## Relevant skills

Apply the relevant review skills:

- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`
- `authentication-security` when relevant
- any phase/domain-specific skill named by the active handoff

## Mandatory final-review dimensions

### Business / logical correctness

- acceptance criteria and business rules are actually implemented;
- domain invariants hold for success, failure, boundary, repeated, and state-transition cases;
- no alternate path bypasses canonical behavior;
- null/empty/time/identity semantics are deliberate.

### Clean code / maintainability

- naming expresses domain intent;
- methods/classes are cohesive;
- control flow is understandable;
- duplicated business rules are centralized in the correct owner;
- helpers/abstractions reduce real complexity rather than hiding simple logic;
- logging is useful, non-noisy, and non-sensitive.

### Extensibility without speculation

- dependencies point toward stable narrow contracts;
- persistence/internal details do not leak;
- approved likely evolution is not blocked by avoidable coupling;
- no abstraction exists only for hypothetical future variants;
- YAGNI remains the default.

### SOLID / patterns / overengineering

- responsibilities, interface size, dependency direction, and substitutability are sensible;
- Strategy/Factory/Adapter/Facade/State/Specification/Event/Builder/etc. appear only for a concrete problem;
- flag both under-designed coupling and pattern-driven overengineering;
- generic bases, `ServiceImpl`, unnecessary interfaces/events/hierarchies are not introduced for convention alone.

### Performance / efficiency

Check concrete risks:

- N+1 or unnecessary eager loading;
- repeated DB/network/file work in loops;
- unbounded growing reads;
- avoidable scans when appropriate indexed lookup exists;
- excessive lock scope/contention;
- unnecessary repeated serialization/hash/computation on hot paths;
- realistic algorithmic or memory growth.

Do not demand speculative micro-optimization. Every performance finding must identify a plausible/measured
inefficient path and a proportionate correction.

### Persistence / transactions / concurrency

- JPA matches Flyway/PostgreSQL;
- transaction boundaries fit the use case;
- uniqueness/locking/race behavior is safe;
- failures cannot leave partial state;
- no cross-module entity/repository access;
- no silent frozen-schema rewrite.

### Security / privacy

- authn/authz boundaries are correct;
- secrets/passwords/PINs/tokens/private payloads do not leak through logs, DTO strings, exceptions, tests, or diagnostics;
- security-sensitive validation fails closed;
- no custom cryptography where platform/framework primitives exist.

### Tests / evidence

- tests prove behavior instead of mirroring implementation;
- negative/boundary/concurrency cases exist where risk warrants;
- PostgreSQL behavior uses PostgreSQL/Testcontainers, not H2;
- architecture verification remains green;
- final evidence records the handoff-required command/result/environment;
- tests do not leak secrets or rely on brittle timing/order.

### Repository tree / package hygiene

- package layout is coherent and capability-oriented;
- commands/exceptions/views/enums are grouped consistently when the API size justifies it;
- meaningful packages have `package-info.java`;
- no stale `.gitkeep`, duplicate source, obsolete moved copy, generated cache, or unexpected tracked artifact remains;
- repository/package-tree docs match the actual structure when the reviewed change affects the tree.

### Static diagnostics / warnings

- review compiler/build warnings and any IDE warnings explicitly reported by the owner;
- distinguish real code problems from IDE configuration/schema-resolution false positives;
- do not rewrite correct native SQL merely because an IDE has no database datasource attached;
- do not silence warnings with blanket suppression unless the reason is concrete and documented;
- do not state that the repository is “warning-free” unless the relevant inspection tool was actually executed.

### Scope / architecture / docs

- implementation stays inside handoff scope;
- module dependencies/named interfaces are valid;
- frozen baseline changes have explicit approval/ADR synchronization;
- docs, roadmap, status, handoff, reviews, and evidence match reality;
- no stale TODO or duplicate source of truth remains.

## Findings

For a numbered implementation phase, record formal findings in that phase's `reviews/` folder.

For maintenance/governance scopes, record the formal review in the canonical maintenance scope's `reviews/`
folder rather than attaching it to an unrelated feature phase.

Use Critical / High / Medium / Low severity.
Each finding identifies the concrete file/behavior, consequence, and required correction.

## CHANGES_REQUESTED

For implementation: set the active handoff to `CHANGES_REQUESTED` and add precise remediation.

For maintenance/governance: update the approved scope to `CHANGES_REQUESTED`.

Do not provide a commit message while blocking findings remain.

## READY FOR OWNER COMMIT

If no blocking finding remains:

1. record `READY FOR OWNER COMMIT`;
2. synchronize status;
3. provide exactly one Conventional Commit message;
4. do not commit/push.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement derived from the reviewed scope.

- `CHANGES_REQUESTED` → direct the owner back to Antigravity `/antigravity-implement-handoff` (or
  `/antigravity-test-slice` only when the review explicitly requires test-only remediation);
- `READY FOR OWNER COMMIT` → tell the owner to commit/push using the provided commit message, then state the
  scope-specific next gate:
  - completed numbered phase → give the latest package to ChatGPT for phase closeout/next-phase preparation;
  - milestone-triggered maintenance → rerun `$codex-milestone-review` after the maintenance commit/push;
  - other maintenance → follow its canonical scope/workflow.

Never leave the owner to infer the next workflow action.
