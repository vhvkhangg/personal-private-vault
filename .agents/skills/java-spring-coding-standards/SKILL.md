---
name: java-spring-coding-standards
description: Apply Java 25 and Spring Boot coding standards, constructor injection, API clarity, exception/logging rules, immutability, and safe Lombok usage.
---

# Java + Spring Coding Standards

- Prefer explicit domain vocabulary over generic `Manager`, `Helper`, or `Util`.
- Prefer constructor injection and final dependencies.
- Encapsulate mutable state; expose intent-revealing behavior instead of public setters.
- Use `Optional` primarily as a return type.
- Validate input boundaries; enforce business invariants in the owning application/domain layer.
- Do not catch-and-ignore exceptions.
- Use parameterized SLF4J logging; never log secrets, credentials, tokens, PINs, or sensitive payloads.
- Prefer framework/BOM-managed dependency versions.

## Lombok

- Never use `@Data` on JPA entities.
- Prefer targeted annotations such as `@Getter`.
- Keep JPA no-args construction protected where practical.
- Do not auto-generate `toString`, `equals`, or `hashCode` across lazy associations.
- `@RequiredArgsConstructor` is appropriate for stateless Spring services with final dependencies.


## Logging discipline

- Use logs for meaningful state changes, security/operational events, external failures, and diagnosable exceptional
  paths.
- Routine read-only query services (for example `ReferenceCatalogService`) do not need entry/exit or per-query
  INFO logs; that adds noise without operational value.
- Never log secrets, raw tokens, passwords, PINs, or sensitive personal payloads.
