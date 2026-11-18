# BF26-NR-B0 rev. 1 — Drive download epoch-guard evidence addendum

The Owner-relayed independent evidence verdict is `ACCEPTED_SCOPED_EVIDENCE` for the credential-epoch guard before HTTP body publication in MockMvc standalone. Audit SHA remains `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This record supplements B11 in the [published B0 report](b0-audit-report.md); it does not modify that report or the HTTP matrix. This addendum awaits independent document review before publication.

One testcase executes two scenarios against the production `DriveFileController`, `DriveFileService.verify` and `DriveFileAuthority.verify`:

- With the credential epoch unchanged, the response is HTTP 200 with the complete expected payload.
- After the first media-buffer read changes the epoch in the synthetic repository entity, the response is HTTP 409 with the exact `DRIVE_STALE_AUTHORITY` envelope and no `Content-Disposition` or `Content-Length`. The stream still contains the complete payload and does not inject a read error or truncation; the body is rejected before the first attachment write.

Admission and media acquisition are stubbed on the service spy; repository and principal are synthetic. The authority verification code is real, but no database epoch change or real session admission occurs. Credit is limited to this pre-body guard in standalone MockMvc. Persisted sessions, PostgreSQL writes, actual disconnect, root-binding, already-committed streaming and other Drive routes remain outside this evidence. In particular, it does not resolve the actual-disconnect gap described in the [reused Drive download evidence](b0-drive-download-reused-evidence-addendum.md). OS-level egress enforcement remains `NOT_VERIFIED`.

The executed selector was `DriveDownloadEpochGuardTest#epochChangeAfterMediaReadPreventsAttachmentBodyPublication`; test raw SHA-256 is `4744f0826d42e52895b9f0cc65ee25a8fb0c9e9ba943de28917d69fa7ae647db`. The independent reviewer reconciled the snapshot's 3,563 audit files plus this one test. Eight CRLF files match the audit's `.gitattributes` conversion without other content differences. The command, launch record and receipt agree on one offline Maven invocation and the exact selector. No Boot application, PostgreSQL, Docker, provider or RAG was started.

The receipt records exit 0, 61.594 seconds within the 220-second deadline, complete capture and owned-process cleanup. Surefire reports one testcase with zero failures, errors or skips; the two scenarios are not counted as two testcases. The reviewer checked the log sizes of 7,352 stdout bytes and 329 stderr bytes, absence of owned processes and removal of the external temporary directory. Stream closure, zero active operations/spools and an empty spool directory are assertions in the passing testcase. All 3,582 repository checkpoint entries, including source/test/reports, index and `ACTIVE`, remained unchanged.

Raw evidence is retained locally under ignored `logs/bf26-nr/drive-download-epoch/run-01/`:

| Artifact | Raw SHA-256 |
| --- | --- |
| `execution-command.json` | `c09c9ad3633d1b3c9a41c0451720045d87a4cae74af05a620e399f19bf6117bc` |
| `receipt.json` | `eaab68cf59ddacc55c3b064209feb479a8808d047c7f1e3933c8c9fc40d504f4` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.drive.DriveDownloadEpochGuardTest.xml` | `4060e2c9a722b1e5da65194269dc424729841efc8c0f253b470b7f2d472afbc7` |
| `cleanup.json` | `b5a97d1ff5b258fe714cd55f83b80ecf957bf89bdf5a44add1d8d14bbe0f4340` |

The invocation allowance is **1/1 used**. No rerun or production change is authorized by this record. B11 and remaining G5 blockers retain their dispositions; previous addenda retain their accepted scopes. Historical correction/exception counters and remediation 0 remain unchanged. A1 remains deferred. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, and B0-U1 remains `NOT_RUN`. B1/X remain outside scope.

Docs Sync: one new addendum; published B0 reports and previous addenda are unchanged. The independent evidence verdict above applies to runtime evidence; document acceptance and publication remain separate steps.
