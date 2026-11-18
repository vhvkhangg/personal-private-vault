# BF26-NR-B0 rev. 1 — Drive thumbnail and root-list evidence addendum

This addendum records two independent `ACCEPTED_SCOPED_EVIDENCE` verdicts for B11 at audit SHA `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. It supplements the [published B0 report](b0-audit-report.md). This document awaits review in a separate Codex chat before publication.

## Thumbnail over HTTP — ACCEPTED_SCOPED_EVIDENCE

One offline Maven invocation ran `DriveH1BWebIntegrationTest#existingFileWithoutThumbnailLinkReturns404` in the embedded HTTP fixture with fake OAuth, Drive and thumbnail upstreams. The receipt records exit 0 after 67.125 seconds within the 220-second deadline. Surefire records one passing testcase with zero failures, errors or skips. The testcase sends two GETs for the same file and route using the same PRIVATE session created through the application login service.

- With `thumbnailLink` present, the control returns HTTP 200 with the exact fake PNG bytes and the thumbnail request count increases by one.
- For the second GET, the fake Drive provider serves a fresh copy of the metadata for the same file ID with the expected `thumbnailLink` removed. The test verifies that this happened before accepting HTTP 404 with a JSON-compatible content type, `error.code=THUMBNAIL_UNAVAILABLE`, `Cache-Control: no-store` and no increase in thumbnail requests.

The run used the reviewed test bytes. Only the snapshot's PostgreSQL fixture was adapted to bind on loopback, check the binding, disallow image pulls for the PostgreSQL container and label the container for cleanup. The log records `127.0.0.1:55480` after the binding check. Full Spring Boot initialized the shared application context, including RAG-related bean wiring, and Flyway applied 29 migrations to the disposable test database, including the RAG schema. Retrieval, QA and test-scoped RAG execution were disabled in the launch environment. This evidence covers the two thumbnail responses in this fixture. OS-level egress enforcement remains `NOT_VERIFIED`.

Ryuk was disabled for the run. The independent review confirmed cleanup by run label and container ID, absence of matching processes and listeners, and removal of the external temporary directory. All 3,592 worktree checkpoint entries matched. The thumbnail execution allowance is **1/1 used**.

Artifacts are relative to the local ignored directory `logs/bf26-nr/drive-thumbnail-no-link/run-01/`.

| Artifact | Locator or role | Raw SHA-256 |
| --- | --- | --- |
| `execution-command.json` | Offline command and selector | `76b47b612d253b44a804816d0e74566ea670a794475a661ae829e36e9a66670a` |
| `receipt.json` | Exit, duration and capture | `57b5d89fa3b0c53f366343b67aa619be29d723c85486efb112fefdaaafe1d907` |
| `receipt.stdout.log` | Loopback bind at line 77, migration result at line 143 | `290f00c0be5a79829ea4055ecfbc9d3998092b95065727bc62b2aea557170558` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.drive.DriveH1BWebIntegrationTest.xml` | Selected testcase result | `08dbb13ac6e54a6be328aa943c0666facd6d6c6295422b8210135225eec6772e` |
| `snapshot/backend/src/test/java/com/vhvkhangg/personalprivatevault/drive/DriveH1BWebIntegrationTest.java` | Test method at line 101 | `b34598494c4f247abf72a45ef46cb41528ce60473c7e5d1008c0da7b6095e9e2` |
| `snapshot/backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractPostgresIntegrationTest.java` | Snapshot-only fixture delta at lines 21–34 | `0b700a224afc7a79ffa57438029b8532c81f778ba75c1c46808462ce09e0b062` |
| `cleanup.json` | Container, process, temporary directory and checkpoint results | `536a853a43075233163890b5c9512e3ddeba7abf371c303e5cb15226f1a4b8f8` |

## Root-list at service level — ACCEPTED_SCOPED_EVIDENCE

The H1-B probe `CodexH1BReviewProbes#legacyRootListMustRejectChildMetadata` creates a file in a child folder, changes that folder's parent to `outside-ppv` in the fake provider, and forces the root-list page to contain the child file's metadata. It asserts that `DriveFileService.list(...).data().files()` is empty. The accepted credit is limited to that result for the sampled provider reply. Independent relocation detection, real PRIVATE session admission and the HTTP response for this scenario remain `NOT_VERIFIED`.

The historical [H1-B independent re-review](../2026-11-08-gdi26-s3-h1-preparation/reviews/2026-11-08-gdi26-s3-h1-b-independent-final-re-review-3.md) used the launcher and probe sources subsequently qualified for reuse. The relevant Drive implementation, service fixture and Drive migrations V25–V27 match between H1-B source commit `775564ff08e47640efcfe828560f40b12d54c7f3` and the audit SHA. Count **one historical probe** from the 12/12 PASS invocation, which exited 0 after 30.047 seconds.

Artifacts are relative to the local ignored directory `logs/gdi26/h1-b/codex-rereview3-73982ccfd6e2486abe18b466ba816356/`.

| Artifact | Locator or role | Raw SHA-256 |
| --- | --- | --- |
| `probes/CodexH1BReviewProbes.java` | Root-list stimulus and assertion at lines 65–76 | `5cd2a1d2d855f503737d43d8a75453fad799e9a1cac3dfa8294cc4b20b5c1172` |
| `full-probes.json` | Historical receipt | `79b3fc1899f74e164bb95b4aecceeb70af0ca3087d1615f97829a4c8af8ddc9e` |
| `full-probes.stdout.log` | JUnit summary at lines 192–197 | `24c6f92148c3ffab64620a77fb92f23bd114292060c43b87bb82964cc8747b90` |

Docs Sync records this addendum only. Published B0 reports, tests, `ACTIVE` and historical counters retain their existing state. B11 remains open. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, B0-U1 remains `NOT_RUN` and A1 remains deferred.
