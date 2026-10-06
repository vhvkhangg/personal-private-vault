# ADR-0017 — Use a Read-Only Portability Module for Lossless Export Snapshots

- **Status:** Accepted
- **Date:** 2026-10-06

## Owner approval and implementation gate

The owner explicitly approved this ADR and the prepared Phase 14 concept on 2026-10-06 for pre-handoff preparation
review. Approval covers the `portability` Spring Modulith leaf module and the narrowly scoped, read-only JDBC
snapshot exception exclusively for portable export operations.

It does not permit cross-module JPA/repository access, database writes, unrelated architectural bypasses, or changes
to otherwise-frozen architecture, schema, and module ownership boundaries. This approval does not authorize Phase
14 implementation or production code changes.

Implementation still requires `READY FOR HANDOFF`, owner commit/push of accepted preparation, and an active Codex
implementation handoff. ADR acceptance and owner approval are not substitutes for those gates.

## Context

ADR-0012 requires owner-controlled portable export in structured JSON, Markdown, and media manifests. Phase 14 is
the final non-RAG backend closure before frontend work.

A full export must include retained/trash/soft-deleted records and association rows, not merely the bounded
active-only reads exposed by normal business APIs. Adding a second broad export API to every business module would
create extensive public-contract churn solely for one cross-cutting portability use case.

Using only `pg_dump` would provide backup fidelity but would not satisfy the portable JSON/Markdown/media-manifest
requirement.

## Decision

Add one top-level Spring Modulith module:

```text
portability
```

`portability`:

- owns no business table;
- is a leaf capability: no business module depends on it;
- owns portable export orchestration only;
- may use JDBC for a **strictly read-only snapshot exception** across an explicit allowlist of application tables;
- runs the logical export snapshot in one PostgreSQL read-only `REPEATABLE READ` transaction;
- never mutates another module's table and never imports another module's entity/repository/internal package;
- excludes authentication secret/security-state tables (`app_users`, `refresh_tokens`) from portable export;
- excludes `flyway_schema_history`;
- exports all other explicitly approved retained application rows, including soft-deleted/trash/history rows;
- emits a versioned format with deterministic table and primary-key ordering.

This is a narrow exception to normal table-ownership rules for **read-only portability export only**. It does not
authorize cross-module writes, generic reporting SQL, or a shared repository layer.

The module has no Spring Modulith application-module dependency on business modules because the export is based on
the executable Flyway schema rather than business Java APIs.

## Portable artifact

Phase 14 export produces a ZIP stream with a versioned manifest and portable content, including:

```text
manifest.json
tables/<table>.jsonl
markdown/knowledge/notes/<id>.md
markdown/knowledge/information/<id>.md
markdown/journal/diary-entries/<id>.md
media/manifest.jsonl
```

The JSONL representation uses database column names and portable scalar values; JSONB remains JSON, decimals retain
decimal fidelity, enums are strings, and temporal values use ISO forms.

Binary media is not embedded in the portable ZIP. `media/manifest.jsonl` contains the object key/checksum/type/size
metadata needed to coordinate provider-level binary backup separately.

## Security/privacy

Portable exports are private owner data.

- No password/PIN hashes, refresh-token hashes, raw JWTs, signing material, storage credentials, or runtime secrets.
- No export payload contents are logged.
- The HTTP response uses `Cache-Control: no-store`.
- Export filenames contain no private record names/content.

## Consequences

- `module-boundaries.md`, `module-dependency-matrix.md`, architecture tests, and application-module inventory gain the
  `portability` leaf module.
- Phase 14 may use direct JDBC only inside the portability snapshot adapter and only for the explicit read-only
  allowlist.
- Format compatibility is controlled through `formatVersion`; Java serialization is not an export format.
- Round-trip import of the entire archive is not promised by Phase 14.
- PostgreSQL dump generation, backup retention/encryption/destination, and automated backup scheduling remain
  deployment decisions.

## Alternatives considered

- **Per-module export interfaces:** rejected for v1 because a lossless snapshot would require broad read-contract
  additions across almost every frozen module.
- **`importdata` owns export:** rejected because it would combine import-job lifecycle state with an unrelated
  whole-vault read orchestrator.
- **`pg_dump` only:** rejected because it is not the portable owner-readable JSON/Markdown format required by ADR-0012.
- **Direct cross-module JPA/repository access:** rejected; the exception is raw read-only JDBC snapshotting, not
  entity/repository sharing.
