# BF26-NR-B0 rev. 1 — Drive download reused-evidence addendum

**Proposed credit; awaiting independent evidence and document review.** Audit SHA remains `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This addendum supplements B11 in the [B0 report](b0-audit-report.md) without changing the published report or HTTP matrix.

Two existing DP-A G5-C test identities concern `GET /api/v1/drive/connections/{slot}/files/{fileId}/download` in the controlled fake-provider fixture. Wrong-slot rejection is proposed for credit; the streaming case records observations only and does not establish the effect of disconnect:

| Existing passing method | Assertion proposed for reuse | Boundary of credit |
| --- | --- | --- |
| `DriveS2WebIntegrationTest#crossAccountFileIdDoesNotReadMediaEvenWhenProviderWouldAllowIt` | Upload a file through slot 1, request it through slot 2 with both foreign metadata and media allowed by the fake provider, then require HTTP 404 and zero provider media calls. | Wrong-slot download rejection for these distinct connected subjects. Does not assert the complete error envelope, absence of response attachment headers, or zero metadata/provider activity. |
| `DriveS2StreamingIntegrationTest#disconnectWhileMediaIsPausedCannotCompleteAnAuthorizedAttachment` | Observed sequence only: receive HTTP 200 and 1,024 bytes, call the production connection service's `disconnect(1)` while provider media is paused, release it, and require the remaining client read to throw `IOException`. Require exactly one media call and clean operation/spool state. | `Mode.PARTIAL` independently closes the provider stream after its first chunk when the latch is released, leaving the body shorter than the uploaded file. The assertions do not exclude the same outcome without disconnect. The causal effect of disconnect/epoch revocation remains `NOT_VERIFIED`; this case receives no credit for that effect. Partial bytes have already been delivered. It also does not prove denial before headers, the HTTP disconnect endpoint, or every credential/epoch change. |

Witnesses are [the wrong-slot test](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/drive/DriveS2WebIntegrationTest.java), [the streaming test](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/drive/DriveS2StreamingIntegrationTest.java), and their [shared HTTP fixture](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/drive/fixture/DriveS2HttpTestSupport.java). The fixture uses real HTTP, persisted PRIVATE login sessions, production owner services, and local fake OAuth/Drive providers. Slots 1 and 2 connect distinct synthetic subjects. `uploaded()` requires HTTP 201 through slot 1; `awaitClean()` requires zero active operations, zero active spools, and an empty spool directory.

In the streaming case, the [fake provider's `media()` implementation](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/drive/fixture/FakeGoogleDriveServer.java) executes `await(bodyRelease); break;` in `Mode.PARTIAL`. Provider truncation is therefore a competing explanation for the client exception. The historical PASS and cleanup observations remain recorded, but do not distinguish truncation from disconnect enforcement. Proving that effect would require a separate witness with a provider able to complete the body after release and assertions that distinguish the connected and disconnected outcomes; no such test or execution is authorized by this document.

## Source applicability and historical provenance

The [DP-A independent final review](../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) qualified G5-C and the final source hashes. DP-A source commit is `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`. Comparing that commit with the audit SHA shows no change in Drive production/test sources, shared test support, main resources or `backend/pom.xml`. Authentication changes only add eight PUBLIC Fiction/Film/Reference GET matchers; the Drive PRIVATE fallback and session/filter code remain unchanged. Backend files at preparation HEAD `73f87b058d3faa2541a2ca5579d3208fc34b9089` also match the audit SHA.

Both current test files match their raw source SHA-256 values in the historical evidence manifest:

| Source file | Raw SHA-256 |
| --- | --- |
| `DriveS2WebIntegrationTest.java` | `679f30f6677c2d2b3d8980c9affcd50bc4fb661c41177a962c4259f985671f1d` |
| `DriveS2StreamingIntegrationTest.java` | `d7c6475d93bf23c3a08e6ac591304a8963c1c243ba22e24bb2a53cb2b6fa2b8d` |

Raw historical artifacts remain local under ignored `logs/bf26-dp-a/codex-takeover-1794408167866608000/`:

| Artifact | Raw SHA-256 |
| --- | --- |
| `evidence-manifest.json` | `cd7061eae3c0e6e1c1de6ddab49f0293433068c1ce2cc02ee2940884340090f4` |
| `g5-c.json` | `801c914aee98119e23f2db0d6d64898dd57bc80c93a8f919d4b8503776802185` |
| `g5-c-reports-v2/TEST-com.vhvkhangg.personalprivatevault.drive.DriveS2WebIntegrationTest.xml` | `71c32c8b8efd16d6e013a8cddad9dcd6f844ebb02611f52e58891d934cd7e0f3` |
| `g5-c-reports-v2/TEST-com.vhvkhangg.personalprivatevault.drive.DriveS2StreamingIntegrationTest.xml` | `2484db3dadab4409e970516adf6000e35ba8d08a8440b4084d4505b2eac9d6ed` |

The historical receipt records PASS, exit 0, 114.688 seconds, complete capture and cleanup. Each named method appears once in its XML without failure, error or skip. These are two existing identities within G5-C, not fresh executions or additions to historical test totals. No Maven, application, database or provider was started for this reassessment. OS-level egress enforcement remains `NOT_VERIFIED`.

## Remaining scope and handback

Independent re-review must confirm the correction for the streaming confound before publication. The review of the previous candidate found the wrong-slot witness eligible for scoped credit and requested correction of the streaming claim. Credit is proposed only for wrong-slot HTTP 404 and zero media calls; the causal effect of disconnect/epoch revocation remains `NOT_VERIFIED`. B11 is not closed by this addendum. Other routes, upload/folder operations, credential rotation and root-binding changes require their own applicable witnesses. Remaining B04/B05 and other G5 gaps retain their dispositions; no HTTP matrix row is silently marked complete.

Docs Sync: one new proposed addendum; published reports, earlier addenda, source, fixtures, index and `ACTIVE` are unchanged. Historical correction and exception counters are unchanged; no invocation allowance is consumed. A1 remains deferred. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, B0-U1 remains `NOT_RUN`, and B1/X remain outside scope. Next role: independent reviewer in a separate PPV Codex chat; evidence and document may be reviewed together.
