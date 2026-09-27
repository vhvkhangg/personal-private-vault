---
name: reuse-and-consistency
description: Prevent duplicated business behavior and scattered implementations by enforcing canonical ownership, single sources of truth, deliberate reuse, and consistency checks without creating premature abstractions.
---

# Reuse and Consistency

Use this skill when implementing or reviewing behavior that may already exist elsewhere in the repository.

## Goal

A business capability should have one canonical implementation path unless there is a documented reason for
multiple independent implementations.

This skill is specifically intended to prevent the same operation or rule from being reimplemented across
multiple services, controllers, modules, utility classes, or tests.

## Before adding behavior

1. Search for the business concept, not only the proposed method name.
2. Check the owning module and its public API.
3. Check Graphify when available for existing callers/implementations.
4. Determine whether an existing operation already represents the same business rule.
5. Reuse or extend the canonical operation when the semantics are truly the same.

## Canonical ownership

Prefer:

```text
one business rule
    ↓
one owning module/service/domain operation
    ↓
multiple callers
```

Avoid:

```text
controller A → duplicate rule
service B    → duplicate rule
import path  → duplicate rule
scheduled job→ duplicate rule
```

Examples of rules that should normally have one canonical implementation include:

- capability checks;
- favorite/rating/tag state transitions;
- soft-delete/restore semantics;
- normalization/validation rules;
- balance/ledger calculations;
- external-account relationship transitions;
- duplicate-detection rules;
- import validation.

## DRY with domain meaning

Deduplicate when two code paths implement the **same business rule**.

Do not deduplicate merely because code looks syntactically similar. Two concepts that can evolve independently
should stay separate even if their current implementation resembles each other.

## Reuse hierarchy

Prefer reuse in this order:

1. existing domain/application operation in the owning module;
2. small domain value object/policy with clear business meaning;
3. focused private helper inside the same class;
4. shared utility only when the behavior is truly cross-domain, stateless, and terminology-neutral.

Avoid generic `CommonService`, `BaseService`, `Utils`, or giant helper classes.

## Consistency review

When modifying an existing business operation:

- identify all callers;
- check whether parallel implementations now diverge;
- update tests around the canonical behavior;
- remove obsolete duplicate paths when safe;
- do not leave two competing sources of truth.

## Anti-overengineering

Do not create an abstraction merely to remove two or three similar lines.
Create/reuse an abstraction when it centralizes a real rule or invariant that must remain consistent.

If two implementations differ intentionally, document the semantic difference rather than forcing them together.
