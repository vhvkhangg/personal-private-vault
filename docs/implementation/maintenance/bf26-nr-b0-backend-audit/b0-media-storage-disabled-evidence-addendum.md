# BF26-NR-B0 rev. 1 — Media storage OFF evidence addendum

The independent reviewer returned `ACCEPTED_SCOPED_EVIDENCE` for two Media OFF responses at audit SHA `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This addendum supplements the [published B0 report](b0-audit-report.md) and the two storage-OFF rows in the [HTTP contract matrix](b0-http-contract-matrix.csv).

| Matrix group | Accepted assertion in MockMvc standalone |
| --- | --- |
| `HTTP-post-images-upload-storage-off` | `POST /api/v1/images/upload` calls the real upload service exactly once with the submitted multipart file. The service rejects the upload before interacting with storage or metadata. |
| `HTTP-get-images-id-content-storage-off` | `GET /api/v1/images/{id}/content` looks up the synthetic image metadata by ID and calls the real disabled adapter with the expected object key. |

Both requests assert HTTP `409`, a JSON-compatible content type and the exact `STORAGE_DISABLED` envelope. Credit applies to these two responses in the standalone fixture. Real PRIVATE session admission, full application wiring and OS-level egress enforcement remain `NOT_VERIFIED`. B04/B05 remain open.

The reviewer verified the snapshot against 3,563 audit files plus the single reviewed test. One offline Maven invocation ran `MediaStorageDisabledResponseTest#disabledStorageReturnsConflictForUploadAndDownload` and exited `0` in 59.625 seconds within the 220-second deadline. Surefire recorded **one passing testcase covering both requests**, with zero failures, errors or skips. The reviewer confirmed that the owned processes had stopped and the external temporary directory had been removed. The invocation allowance is **1/1 used**.

The artifacts below are relative to the local ignored directory `logs/bf26-nr/media-storage-disabled/run-01/`.

| Artifact | Raw SHA-256 |
| --- | --- |
| `execution-command.json` | `dd626c7aa22dd2c088451a0fe9bd83abc4eb9560b0e34edb544e96d88d579038` |
| `receipt.json` | `4e776d2477a2f4281b843cd123d6bf8e23ca0f798eaa45119329690cc8a2050c` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.media.MediaStorageDisabledResponseTest.xml` | `a69a76f7a7039e31975ff493209da5c50a5b949a85de010d3c43799dba62d921` |
| `snapshot/backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageDisabledResponseTest.java` (assertions at lines 62–95) | `9caeb53dce8468b3efc2e4e9577185516bc1cae301ec2bd98d06e985edbeacbc` |
| `cleanup.json` | `7332cbff53116b61cb88f9ca6067f488f92fb5b9deb49c919933b11c14c3ae6a` |

B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, B0-U1 remains `NOT_RUN` and A1 remains deferred. This addendum awaits independent document review. The published B0 reports, test, `ACTIVE` and historical counters retain their existing state.
