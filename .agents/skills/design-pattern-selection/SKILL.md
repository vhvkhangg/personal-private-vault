---
name: design-pattern-selection
description: Select GoF/enterprise/domain patterns only when they solve a concrete recurring design problem, with explicit tradeoffs and an anti-overengineering bias.
---

# Design Pattern Selection

Use this skill when a change introduces a non-trivial abstraction, multiple implementations, workflow states,
construction complexity, eventing, or behavior that may benefit from a recognized pattern.

## Rule

Do not start from a pattern name. Start from the concrete design problem.

Apply a pattern only when it measurably improves one or more of:

- cohesion;
- replaceability;
- testability;
- invariant protection;
- dependency direction;
- readability of a recurring variation;
- isolation of infrastructure/volatile behavior.

## Common patterns and when they fit

### Strategy

Use when multiple real algorithms/policies implement the same contract and are selected at runtime or by data.

Do not use for one implementation with a hypothetical future second implementation.

### Factory / Factory Method

Use when construction has meaningful branching/invariants or callers should not know concrete creation details.

Do not wrap trivial constructors.

### Adapter

Use to translate an external provider/API/library into a stable internal contract.

Good fit for object storage, import sources, or external APIs.

### Facade

Use when a module needs a small public API hiding multiple internal collaborators.

This often fits Spring Modulith module boundaries.

### State

Use only when an entity has genuine state-dependent behavior with transitions that otherwise become sprawling
conditionals.

Do not use for simple enums with a few validation checks.

### Specification / Policy

Use when a business predicate/rule is independently meaningful, composable, reused, and testable.

Do not turn every boolean condition into a class.

### Domain Event

Use for decoupled side effects after a meaningful business fact occurred.

Do not replace a required synchronous call with events just to reduce visible coupling.

### Template Method / inheritance

Prefer composition first. Use inheritance only when subtype substitution is natural and stable.

### Builder

Use for complex construction with many optional values or readable test fixtures.

Do not use builders for tiny immutable records/classes.

## Repository-specific constraints

Patterns must not bypass:

- module ownership;
- allowed dependency matrix;
- internal/public package boundaries;
- Flyway/JPA persistence rules;
- active Codex handoff scope.

A pattern is not justification for adding a new cross-module dependency.

## Review checklist

Before accepting a pattern, answer:

1. What concrete problem does it solve now?
2. What simpler design was considered?
3. Does it reduce or increase cognitive load?
4. Does it centralize a real variation/invariant?
5. Can another developer infer why the pattern exists from the code?
6. Does it preserve module ownership and testability?

If the answers are weak, use the simpler design.
