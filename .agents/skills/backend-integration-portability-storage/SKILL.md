---
name: backend-integration-portability-storage
description: Guide Phase 14 portability snapshots, S3-compatible media integration, binary HTTP transfer, compensation and operational readiness.
---

# Backend Integration, Portability, and Object Storage

Use for Backend Phase 14.

## Portability

- `portability` is a leaf module with no business-module dependency and no owned table.
- Direct JDBC is allowed only in its ADR-0017 read-only snapshot adapter.
- Snapshot transaction is PostgreSQL read-only `REPEATABLE READ`.
- Use an explicit application-table allowlist.
- Exclude authentication security-state tables and Flyway metadata.
- Stable table + primary-key ordering.
- Stream rows/ZIP entries; never load the complete vault.
- Keep the read-only repeatable-read transaction alive for actual row consumption; if async streaming is used, open
  the transaction on the worker thread that performs the reads.
- Always close JDBC cursors/transactions, ZIP streams and temporary files on success, failure, timeout, cancellation
  and client disconnect.
- Preserve BigDecimal, JSONB, enums, temporal values, nulls and Markdown.
- Export trash/soft-deleted/history rows as retained owner data.
- Never use Java serialization as the portable format.
- Never log exported data.

## Media object storage

- Media owns the storage port/adapter.
- Keep S3/AWS/MinIO types under Media infrastructure only.
- Production provider remains deferred.
- Server generates opaque object keys for managed uploads.
- Derive checksum and byte size from actual upload bytes.
- Avoid whole-file heap buffering.
- Treat metadata success only after the database commit is confirmed; do not make cleanup decisions inside an outer
  transaction whose commit is still pending.
- Confirmed rollback/not-committed metadata -> compensate only this attempt's key. For an uncertain commit, a fresh
  Media-owned lookup may prove commit only when the exact expected row/checksum/size is visible. Absence/mismatch does
  not prove rollback and must retain the object; automatic deletion requires authoritative rollback/abort evidence
  from the original transaction boundary itself. A later SELECT/inventory result, timeout, connection loss/closure,
  or missing row is not rollback proof. Never delete after confirmed commit.
- Compensation is bounded to 3 delete attempts per request. Failed compensation must not mask the primary failure;
  accept/document a residual orphan. Privileged manual cleanup requires quiescing managed-upload writers, settling or
  terminating all outstanding upload metadata transactions, then performing a fresh inventory-vs-`images.object_key`
  comparison and a final unreferenced-key re-check immediately before delete. If quiescence cannot be established, do
  not delete.
- Uncertain storage upload must be resolved/probed before metadata creation when possible; unresolved outages return
  failure and may leave only the accepted residual orphan, never partial Image/Vault metadata.
- Trash/restore retains binary objects; no hard-delete semantics.
- Never log credentials, raw provider errors or object keys.

## Binary HTTP

Approved binary success routes:

- `GET /api/v1/images/{id}/content`
- `POST /api/v1/portability/exports`

Before the servlet response is committed, failures use canonical JSON `ApiResponse`. After binary bytes are committed,
stream/read/write/timeout/client-disconnect failures abort the transfer; never append JSON or reset the response. A
portable export is complete only after ZIP finalization and normal body completion; a truncated ZIP is failed/incomplete.
Use `Cache-Control: no-store`/`private, no-store` as specified.

## Operational readiness

- Validate storage configuration only when enabled.
- Object storage affects readiness, not liveness.
- Public health must hide details.
- Do not add deployment/observability-provider architecture.

## Testing

Use PostgreSQL Testcontainers plus real S3-compatible integration storage.
Preserve the Phase 13 baseline and prove pre/post-commit stream failures, snapshot consistency during actual
consumption, cancellation cleanup, commit-time metadata failure, failed compensation, race cleanup, privacy,
architecture boundaries and OpenAPI/runtime binary contracts. Include a deterministic uncertain-commit race test:
hold the original PostgreSQL metadata transaction open, reconcile from another transaction while its row is invisible,
verify no object deletion, commit the original transaction, then verify retained Image/Vault metadata still has its
binary. Retain explicit confirmed-rollback, confirmed-commit, failed-cleanup and privacy-safe error coverage.
