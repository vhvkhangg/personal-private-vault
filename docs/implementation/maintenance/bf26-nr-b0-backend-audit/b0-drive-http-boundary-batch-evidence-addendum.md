# BF26-NR-B0 rev. 1 — Drive HTTP boundary batch evidence addendum

Proposed scoped credit for B11 and B04 at audit SHA `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This addendum supplements the [B0 report](b0-audit-report.md) and [HTTP contract matrix](b0-http-contract-matrix.csv). Both the run evidence and this document await independent review.

## Executed assertions

The embedded HTTP fixture uses a persisted PRIVATE session and controlled OAuth and Drive upstreams. Each paired case retains the same request and resource conditions apart from its stated change. The source locators refer to the files in the execution snapshot.

| Testcase and source locator | Matrix group | Proposed credit |
| --- | --- | --- |
| `DriveS2WebIntegrationTest#rootListRetainsValidFileAndOmitsFileWithWrongParent` (line 77) | `HTTP-get-drive-connections-slot-files-private` | Both root files are listed in the `200` control. When the fake provider changes the parent of one entry in the same page, HTTP remains `200` and the response contains only the other file ID. This covers the sampled direct-parent mismatch at the HTTP boundary. |
| `DriveS2WebIntegrationTest#childDownloadRejectsMoveToRootBeforeReadingMedia` (line 116) | `HTTP-get-drive-connections-slot-files-fileid-download-private` | The control returns `200` and the exact child file bytes. The second GET receives metadata for the same file with its parent changed to root and returns `404 DRIVE_FILE_UNAVAILABLE`, with no `Content-Disposition` and no additional media request. |
| `DriveS2WebIntegrationTest#validMultipartRequiresIdempotencyKeyBeforeProviderOrIntentCreation` (line 168) | `HTTP-post-drive-connections-slot-files-private` | Removing just `Idempotency-Key` from a valid multipart request returns `400 UPLOAD_KEY_REQUIRED`. The provider call count stays unchanged and the intent table contains zero rows. The same multipart with the key returns `201` and a nonblank ID. |
| `DriveS2StreamingIntegrationTest#disconnectAbortsCommittedDownloadWithProviderAbleToFinish` (line 132) | `HTTP-get-drive-connections-slot-files-fileid-download-private` | Both downloads return `200`, advertise the full `Content-Length` and deliver the expected initial bytes. Releasing the provider in the connected control delivers all remaining bytes. In the second download, the connection service reports `DISCONNECTED` before the latch is released, and reading the remainder throws `IOException` before the fixture's two-second idle limit. The testcase checks two total media requests and cleanup of operations and spools. |

`FakeGoogleDriveServer.java:207` pauses after the first chunk and can send the rest after the latch is released. The passing control distinguishes this run from the provider truncation in the [historical disconnect observation](b0-drive-download-reused-evidence-addendum.md). Proposed disconnect credit covers the sampled committed transfer after a direct call to the production connection service. The HTTP DELETE route, bytes sent after revocation and other interleavings fall outside the assertions. The [earlier thumbnail and service-level root-list credit](b0-drive-thumbnail-and-root-list-evidence-addendum.md) retains its original scope.

## Execution and limits

One offline Maven `test` invocation used the four selected methods, `forkCount=1` and JUnit parallel execution disabled. The two Surefire reports identify one shared fork and exactly **four passing testcases**, with zero failures, errors or skips. The bounded runner exited `0` after **67.609 seconds** within the 220-second deadline. The invocation allowance is **1/1 used**. The NUL-separated argv SHA-256 is `25db2bfd028f1a2d8d5c12814d9e6586d02aaef9da8cff9b871cd812d45f8bff`.

The snapshot matches the 3,563 audit paths after eight declared CRLF conversions, the three reviewed test and fake-provider changes from tree `710b7b324695e437c56cb23237ed644f29e54192`, and one execution-only change to `AbstractPostgresIntegrationTest.java` at raw SHA-256 `f497edba1fe0e18f58369110d9c3eec0886f1b11dbe0ed307e01a3985287b8ae`. That fixture pins and verifies PostgreSQL loopback binding, disables image pulls and labels the container. The log records `127.0.0.1:49704` and 29 migrations on the disposable database, including the shared RAG schema. Ryuk was disabled for this run. The cleanup check confirms that the labeled container and owned processes had exited and the temporary directory had been removed. Shared Boot initialization carries zero functional RAG credit. OS-level egress enforcement remains `NOT_VERIFIED`.

Raw artifacts are relative to the local ignored directory `logs/bf26-nr/drive-batch/run-01/`.

| Artifact | Raw SHA-256 |
| --- | --- |
| `preflight.json` | `ee2b09d37c749ca3fc1f051b1954f56efecd08c601fdb2b65928d65b6cb73afd` |
| `snapshot-manifest.json` | `05e1e979730a525c0414d7fe4c890a15dccab2190579007c0c7baea7fb801b31` |
| `execution-command.json` | `b93a2d77fdc57632a2198b884c282298ca2e762bee6efec9b6148212bfdb12e3` |
| `launch.json` | `42decb516b91613e97795c6c0e63ba04318ec96b9a2e32384148e50b60ab9d19` |
| `receipt.json` | `158d1c77b1acd35762cc6dcfd913207466e90858862a4b3bdf0519dc3472339d` |
| `receipt.stdout.log` | `c38befcf8fa83f473e02563e6d7a5142b482fc7e4050cf6cc7a677bd489ab5d9` |
| `receipt.stderr.log` | `e0704b7893dfb0ed43cc4c3c3e6f41cf2cf5edfa961010d52005cc4ceff1d269` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.drive.DriveS2WebIntegrationTest.xml` | `7fd62ce48b4224afbca98a37418ce23c5171762795a28f622b4356023c3304f8` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.drive.DriveS2StreamingIntegrationTest.xml` | `ce770e461b95e371912540837eaae3e6f0920a2a5d0806aacdb570727f191a7b` |
| `cleanup.json` | `7cf739b090c9bb38e6297aed86cda11e9e7a9a76f15b2703a4015dad0f9196a0` |

B11 and B04 remain open. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, B0-U1 remains `NOT_RUN` and A1 remains deferred. Docs Sync covers this addendum. The published B0 reports, `ACTIVE` and historical counters retain their existing state.
