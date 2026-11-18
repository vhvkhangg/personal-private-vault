# BF26-NR-B0 rev. 1 — Media error response evidence addendum

The independent evidence reviewer returned `ACCEPTED_SCOPED_EVIDENCE` for the response body of the selected pre-commit Media read failure. Audit SHA is `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This record supplements the B18 post-dispatch error-channel assessment in the [published B0 report](b0-audit-report.md).

The executed test observed the first read of a synthetic storage stream throwing `IOException` before any response body was written. Async dispatch through the production controller and exception advice returned HTTP 500 and the exact canonical `INTERNAL_ERROR` envelope. The body contained neither the synthetic error-detail marker nor the object key. The response reader rejected duplicate keys and trailing tokens.

Credit is limited to this response body in MockMvc standalone with mocked ImageOperations and MediaStoragePort and the real ImageDownloadService, ImageController and advice. Other B18 channels, session admission and deployment behavior remain outside this evidence. OS-level egress enforcement remains `NOT_VERIFIED`.

The reviewer verified the snapshot against the audit archive plus one test, and reconciled the offline Maven command, receipt and XML. Selector was `MediaDownloadErrorResponseTest#preCommitReadFailureDoesNotExposeStorageDetails`. Test raw SHA-256 was `76ac50087a354670fbe7e7f6eedafd94f67cc6f57ff5d4ed50b544f92617fc77`. One invocation completed with exit 0 in 71.688 seconds within a 220-second deadline. Surefire recorded one passing test, zero failures, errors or skips. Capture and owned-process cleanup completed according to the receipt.

Raw evidence is retained locally under the ignored directory `logs/bf26-nr/media-error/run-01/`.

| Artifact | Raw SHA-256 |
| --- | --- |
| `receipt.json` | `a1bbc751c4b619c49e4401965173fe94d2a94ce1a24c2a8afb6b21a188a0aebe` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.media.MediaDownloadErrorResponseTest.xml` | `d7f3728c2b2bc2341984ca66b5fe432574a4cc68cc469de85fec222b6fcff8a3` |

The executed invocation count is **1/1**. Remaining B18 and G5 blockers retain their existing dispositions. B07 execute-versus-cancel remains unverified at B0, while the [ImportData rollback credit](b0-importdata-rollback-evidence-addendum.md) remains accepted within its scope. A1 remains deferred. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, and B0-U1 remains `NOT_RUN`.
