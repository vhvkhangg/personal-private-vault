---
trigger: model_decision
description: "Apply when adding behavior that may duplicate an existing rule, introducing shared helpers/abstractions, or choosing a design pattern."
---

**SIMPLIFY BY DEFAULT:** Before adding abstraction, ask if an existing single-user module function suffices. Antigravity must self-review for unnecessary patterns; Codex pre-handoff must reject unsolicited mechanisms before implementation. Accepted privacy/data-integrity guarantees are kept. See [Owner policy](../../docs/workflow/current-owner-delivery-policy.md).



# Code Reuse and Pattern Rule

Before introducing a new implementation of an existing-looking business capability:

1. inspect/search the owning module for the canonical behavior;
2. use `reuse-and-consistency`;
3. reuse/extend the canonical path if semantics match;
4. keep intentionally different concepts separate.

When introducing Strategy, Factory, Adapter, Facade, State, Specification, Template Method, Builder, or another
pattern, use `design-pattern-selection` and justify the concrete problem it solves.

Do not create generic common/base/service/utility abstractions merely to reduce superficial duplication.
