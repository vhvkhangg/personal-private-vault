---
name: pragmatic-solid-design
description: Apply SOLID, cohesion/coupling, DRY, YAGNI, composition, and patterns pragmatically without speculative interfaces or overengineering.
---

# Pragmatic SOLID Design

Use SOLID as a diagnostic tool, not a target class/interface count.

1. Preserve correct behavior and module ownership.
2. Maximize cohesion and minimize unnecessary coupling.
3. Apply SRP only when responsibilities genuinely change independently.
4. Add interfaces/extension points only for a real boundary, variants, or concrete volatility/testing value.
5. Prefer composition when inheritance/substitution is unnatural.
6. Keep public APIs narrow and capability-oriented.
7. Remove duplication only when it represents the same business rule.
8. Prefer YAGNI over speculative strategies, factories, events, or plugin systems.

Avoid generic base services/repositories that erase domain language.
