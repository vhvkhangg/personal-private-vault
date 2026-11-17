# BF26-NR-B0 rev. 1 — ImportData rollback evidence addendum

- **Independent verdict:** `ACCEPTED_SCOPED_EVIDENCE`, as reported in the independent evidence-review handback.
- **Audit SHA:** `dde8fc86e820b8f9a329a65cfe590191e027ef4c`
- **Published B0 handback:** `c6f9f410f213799551a1d85cc4ee0b8eb7ca4560`. This record supplements the B07/B12 target-failure row in the [published audit report](b0-audit-report.md).

## Accepted assertion

Both Note items were `VALID`. PostgreSQL showed the first Note and its Vault entry in the import transaction before the second `saveAndFlush` threw the exact injected exception. The test verified the order and count of those two calls. A fresh `REQUIRES_NEW` read confirmed that the job stayed `VALIDATED`, `importedItems` stayed at zero, both item views matched their pre-execution snapshots, and `notes` and `vault_entries` returned to their empty pre-execution state.

Credit is limited to rollback after an exception injected at the repository **before SQL was sent to write the second Note**. The run used Maven offline and a disposable PostgreSQL 18.6 database on `127.0.0.1:55483` with migrations V1, V2 and V25–V29. OS-level egress enforcement remains `NOT_VERIFIED`.

## Run evidence

The independent reviewer verified the single Maven invocation against the receipt and the single passing Surefire testcase `G5ImportDataRollbackEvidenceTest#rollsBackWholeJobWhenSecondTargetWriteFails`. The reviewed test's raw SHA-256 is `491693830d4ff3d82f94be30a720694cb4d442f3d9078ac8827bd728babf89a1`. Runner exit was 0 at 74.641 seconds within the 220-second deadline. Surefire recorded 1 test, 0 failures, 0 errors and 0 skips. Capture completed, and cleanup removed the temporary database, role and data directory and stopped PostgreSQL. `run-01` ended during database setup before Maven launched.

The following files are retained locally under the ignored directory `logs/bf26-nr/import-rollback/run-02/`.

| Artifact | Raw SHA-256 |
| --- | --- |
| `rollback.json` | `bece6946a53be4db86835709e66317e89b3b09643220bcbbcaeef2e8e65bc82a` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.audit.g5.G5ImportDataRollbackEvidenceTest.xml` | `6a0676789e3fb01e7c79ff3705b7f4c196f3b955900da3c95c776e43f02a718e` |
| `cleanup.json` | `1ea045563f5cffcc5e918484680302fe974c7a2ab5ce454f16314392655d5cad` |

## Disposition

Invocation authorization is consumed **1/1**. B07 execute-versus-cancel and all other G5 blockers retain their published dispositions. A1 remains deferred. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, and B0-U1 remains `NOT_RUN`.
