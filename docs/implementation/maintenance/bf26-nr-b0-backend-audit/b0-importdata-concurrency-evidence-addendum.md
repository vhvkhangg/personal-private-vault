# BF26-NR-B0 rev. 1 — ImportData concurrency evidence addendum

Audit SHA is `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This record supplements the B07 execute-versus-cancel assessment in the [B0 report](b0-audit-report.md).

The independently reviewed test used the existing Non-RAG PostgreSQL fixture and ImportData configuration with real owner services and repositories. Both scenarios retained an uncommitted winning transaction, preloaded VALIDATED in the losing transaction, then required the exact loser PID to wait on the winner PID according to pg_blocking_pids and pg_stat_activity before releasing the winner to commit.

- When cancellation won, the waiting execute rejected the refreshed CANCELLED state with the exact job and transition exception. A fresh transaction verified zero imported items, unchanged item views and unchanged Information and Vault table contents.
- When execution won, the waiting cancellation rejected the refreshed IMPORTED state with the exact job and transition exception. A fresh transaction verified one imported item, exactly one linked Information and Vault entry with the expected fields, correct counters and unchanged contents for pre-existing target rows.

One offline Maven invocation ran both methods of `G5ImportDataConcurrencyEvidenceTest`: `cancelWinsAndWaitingExecuteCreatesNoTargets` and `executeWinsAndWaitingCancelPreservesImportedTargets`. Test raw SHA-256 was `ddbaa23bc2b49fcf8a205370bd7c2edb69d1b9a58dcb8e0277512a6208331e40`. The receipt recorded PASS, exit 0, 71.109 seconds within a 220-second deadline and complete capture and owned-process cleanup. Surefire recorded two tests with zero failures, errors or skips.

The disposable PostgreSQL 18.6 database listened on `127.0.0.1:55483` and applied V1, V2 and V25–V29. Both worker executors terminated. Database and role removal were checked by SQL, PostgreSQL stopped, the data directory was removed and the port was confirmed closed.

This evidence covers the two forced orderings for a single INFORMATION item in the controlled fixture. Other interleavings, full migration-chain parity and deployment behavior remain outside its scope. OS-level egress enforcement remains `NOT_VERIFIED`.

Raw evidence is retained locally under the ignored directory `logs/bf26-nr/import-concurrency/run-01/`.

| Artifact | Raw SHA-256 |
| --- | --- |
| `receipt.json` | `7fb9ea0ef177333a65fa0c25942ca31c63bd44a7a3f5ca5e806aa36bb8550b2f` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.audit.g5.G5ImportDataConcurrencyEvidenceTest.xml` | `3cf3f1766d70d0410df3e2565ea8ddca97bb5cc0f7b7d50b4318f8f35faf3142` |
| `cleanup.json` | `1ea045563f5cffcc5e918484680302fe974c7a2ab5ce454f16314392655d5cad` |

Independent runtime evidence and document review are required before publication. The executed invocation count is **1/1**. Remaining G5 gaps retain their dispositions. The accepted [rollback](b0-importdata-rollback-evidence-addendum.md), [Media](b0-media-error-evidence-addendum.md) and [Drive startup](b0-drive-disabled-startup-evidence-addendum.md) evidence retains its scope. A1 remains deferred. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, and B0-U1 remains `NOT_RUN`.
