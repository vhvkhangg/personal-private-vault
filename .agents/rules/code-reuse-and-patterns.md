---
trigger: model_decision
description: "Apply when adding behavior that may duplicate an existing rule, introducing shared helpers/abstractions, or choosing a design pattern."
---

# Code Reuse and Pattern Rule

Before introducing a new implementation of an existing-looking business capability:

1. inspect/search the owning module for the canonical behavior;
2. use `reuse-and-consistency`;
3. reuse/extend the canonical path if semantics match;
4. keep intentionally different concepts separate.

When introducing Strategy, Factory, Adapter, Facade, State, Specification, Template Method, Builder, or another
pattern, use `design-pattern-selection` and justify the concrete problem it solves.

Do not create generic common/base/service/utility abstractions merely to reduce superficial duplication.
