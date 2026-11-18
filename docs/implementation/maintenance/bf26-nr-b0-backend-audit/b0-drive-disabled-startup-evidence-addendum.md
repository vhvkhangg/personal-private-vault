# BF26-NR-B0 rev. 1 — Drive disabled startup evidence addendum

Audit SHA is `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This record supplements the B14/B18 invalid Drive flag assessment in the [B0 report](b0-audit-report.md).

The independently reviewed test used a synchronous ApplicationContextRunner with both production DriveConfiguration and DriveFilesConfiguration. With Drive disabled and Drive Files enabled, the context failed with root cause IllegalStateException and the exact message `Drive files require ppv.drive.enabled=true`. The token-key filesystem counter was zero, and the three construction trackers recorded zero DriveCredentialCrypto, DriveOAuthClient and DriveFileHttpClient objects.

This evidence covers access through Files at the configured synthetic token-key path and construction of those three classes on the refresh thread. Other filesystem APIs, paths, threads and static initialization are outside the tracker scope. OS-level egress enforcement remains `NOT_VERIFIED`.

One offline Maven invocation executed `DriveDisabledFilesStartupTest#filesEnabledWithoutDriveFailsBeforeKeyAccessOrProviderConstruction`. The reviewed test raw SHA-256 was `c8cc1a706572334ad90509ee4095d7ec09fc55b254ce4a2dc5e8d8a7a31614eb`. The receipt recorded PASS, exit 0, 61.125 seconds within a 220-second deadline, complete capture and complete owned-process cleanup. Surefire recorded one test with zero failures, errors or skips. Mockito inline instrumentation was active.

Raw evidence is retained locally under the ignored directory `logs/bf26-nr/drive-disabled/run-01/`.

| Artifact | Raw SHA-256 |
| --- | --- |
| `receipt.json` | `c4354e78c053732bfae8b236abd34252c8aa7848e71a7c910d363f1c1612c66a` |
| `snapshot/backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.drive.DriveDisabledFilesStartupTest.xml` | `56cde9aae5951b48bea7a3e582653655b280ae179aa3333addd0227c5b3d3d4c` |

Independent runtime evidence and document review are required before publication of this addendum. The executed invocation count is **1/1**. All remaining B04/B05, B07, B11 and B18 evidence gaps retain their current dispositions. The accepted [ImportData rollback](b0-importdata-rollback-evidence-addendum.md) and [Media response](b0-media-error-evidence-addendum.md) credits retain their scope. A1 remains deferred. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, and B0-U1 remains `NOT_RUN`.
