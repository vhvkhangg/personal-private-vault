---
trigger: glob
globs: "backend/**/*.java, backend/**/*.xml, backend/**/*.yml, backend/**/*.yaml, backend/**/*.properties"
description: "Java/Spring backend architecture, module ownership, persistence, API, and logging constraints."
---

# Backend Java Rule

- Respect the frozen Spring Modulith dependency matrix.
- Keep JPA entities/repositories internal to their owning module.
- Never import another module's `internal` package.
- Do not expose persistence entities through REST.
- Prefer explicit DTOs and application APIs.
- Use Flyway for physical schema evolution.
- Use SLF4J and exclude secrets/sensitive payloads from logs.
- Prefer framework-managed dependency versions.
- Do not introduce frontend, RAG, deployment, or multi-user behavior.
