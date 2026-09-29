# Pre-Phase-4 Inspection Inventory

Status: **CLASSIFIED — 2026-09-29**

| Reported inspection | Classification | Action |
| --- | --- | --- |
| Native SQL columns in `VaultEntryTagRepository` | IDE schema-resolution false positive unless PostgreSQL verification fails | Keep SQL; verify against Flyway/Testcontainers |
| Native SQL columns in `FavoriteRepository` | IDE schema-resolution false positive unless PostgreSQL verification fails | Keep SQL; verify against Flyway/Testcontainers |
| PostgreSQL enum `person_role` and columns in `PersonRoleRepository` | IDE schema-resolution false positive unless PostgreSQL verification fails | Keep SQL; verify against Flyway/Testcontainers |
| Native SQL columns in `CreatorGroupMemberRepository` | IDE schema-resolution false positive unless PostgreSQL verification fails | Keep SQL; verify against Flyway/Testcontainers |
| `TagCreator` exposed through public constructor signature | Actionable Java visibility/API hygiene warning | Maintenance handoff |
| 13-line duplicate fragments in `VaultMetadataService` | Actionable only where genuinely duplicated after inspection | Minimal maintenance refactor |
| 6-line duplicate fragments in `PersonService` | Actionable maintainability warning | Minimal maintenance refactor |
| Markdown table formatting | Documentation formatting warning | Fixed in preparation/docs patch |
| JSON parser warnings in `antigravity-cli-permissions.md` | Real fenced-JSON syntax problem | Fixed by wrapping the properties in an object |
| `Cannot resolve symbol '..'` for Markdown relative paths | IDE false positive when the referenced path resolves | Keep valid relative links; local-link validation must pass |

All current Markdown links containing `..` were checked against the supplied repository tree and resolve to existing
files. Do not replace valid relative links with brittle absolute filesystem paths.
