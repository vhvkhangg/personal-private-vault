# Vault Architecture

The vault is built as a backend-first modular monolith using Spring Boot and Spring Modulith.
All personal information is stored locally with PostgreSQL and pgvector for semantic search.
The primary architectural style of the vault is a modular monolith.
