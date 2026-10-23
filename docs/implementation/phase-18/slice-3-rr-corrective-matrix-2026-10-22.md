# Slice 3 RR checkpoint — prospective S3-01–16 evidence matrix

2026-10-22 checkpoint; completed on resume 2026-10-23. Codex implementation evidence, **not independent acceptance**.
**READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW**. Final Java: 249 tests / 0 failures / 0 errors / 0 skips (Core 99, compatibility 150); Python 198 passed. Both Java partitions have identical 1,741-file manifests; current static/dependency/lock/diff checks pass. The [mandatory contract](slice-3-verified-indexing-test-matrix.md),
October 21/22 handbacks, prior readiness claims and the independent CHANGES_REQUESTED
re-review remain preserved. S3-F02/F04 stay independently CLOSED for their original
defects. RR-01–03 and FR18-3 await a separate independent review.

`Core` below means `RagSlice3CorrectiveIntegrationTest`; its final execution covers
the earlier 88 cases plus 11 new RR cases. `Compatibility` is the separate 150-case
legacy/architecture/API/native batch/recovery selection. Exact methods, selectors,
source hashes, outcomes and resources are recorded in the new handback/inventory.

| Gate | Final-candidate implementation proof / applicable retained evidence | Qualification |
| --- | --- | --- |
| S3-01 | Current native DOMAIN eight-success/two-failure test; original new vector/mixed-closure counterexamples reproduced before correction and rejected after correction. Retained D14 log/provenance failure remains available. | Original Antigravity pre-repair stdout is still missing; no cause or bytes reconstructed. FR18-3 remains OPEN. |
| S3-02 | Core immutable snapshots, deleted originals, exact revision/binding, Unicode/format negatives; new `domainClosureCannotReinterpretEitherSafeOrRejectedExternalMarkdown` denies unsupported closure during admission, before dispatch/reservation. | No external child is serialized as DOMAIN plaintext or stripped of mandatory snapshot identity. No live-original reading. |
| S3-03 | Compatibility `RagSlice3VerifiedIndexingIntegrationTest` real `.md` 8/2 with two exact unsupported-active-HTML failures; only eight promotions and atomic union release. Core multi-root rollback scenarios. | Existing independently verified scenario is rerun with real trusted inference; no premature partial common-failure completion. |
| S3-04 | Current native all-failed batch; explicit failed-root revision binding and zero-G behavior. | Ordinary all-failed results remain ABORTED, distinct from requested cancellation. |
| S3-05 | Core original content, missing/extra/reordered/duplicate roster/chunk, protected seal/grant and staging DML negatives; new norm-preserving vector negatives. | Admin re-seal injection is expressly beyond normal staging privileges and represents a corrupted producer result, not a regained runtime permission. |
| S3-06 | Existing exact source-context/compact metadata/Unicode/locator reference cases plus exact chunk-position vector comparison. | Source metadata still compares before inference; no shape-only or cosine-only admission. |
| S3-07 | Existing nested MD aliases/folders/anchors/revision/ambiguity/cycle/revoke/equal-byte identity cases; new safe and active-HTML DOMAIN→MD denials plus `independentDomainAndMarkdownRootsKeepTheirOwnTypedProcessingContracts`. | DOMAIN-only and MD→MD remain supported; independently mixed roots are allowed. A mixed processing-contract closure is not a newly invented feature. |
| S3-08 | Original native sign-flip probe now rejects zero G; `trustedInferenceRejectsNormPreservingNativeVectorForgery` covers sign/swap/permutation. New real CPU calibration and strict component comparison; existing dimension/nonfinite/model/tokenizer/provider/SQL-vector negatives. | Same pinned artifact/tokenizer/model. Explicit component tolerance, actual independent CPU delta 0.0. CUDA UNAVAILABLE; no GPU numeric/fallback PASS. |
| S3-09 | Core ten root/child authority barriers, exact pending-pointer identity, mutation/revoke busy denial, source-specific isolation and fresh-JVM qualification. | Original F02 eligibility code is unchanged; correct vectors do not renew consent or bypass source/CAS guards. |
| S3-10 | Core actual runtime/worker/PUBLIC privilege denials and invalid issuance; compatibility PostgreSQL privilege suite; migration/grant bytes unchanged. | No new database privileges, schema objects or migrations. Cancellation intent uses existing run operational JSON. |
| S3-11 | Core real writer-first/seal-first/post-wait-expiry orders with native vectors; real native staging cancellation; compatibility Foundation stage-fence cases. | SQL interleavings retain disclosed administrative fixture reset. No strict-E2 or hostile-same-user guarantee. |
| S3-12 | Core five native multi-root physical rollback locations preserving selected old R and independently eligible old G; unchanged publication callback. | Replayed inference remains outside C_P. No extra publication transaction or partial release. |
| S3-13 | Compatibility real connection-level committed acknowledgement loss and durable exactly-once reconciliation. | Actual PostgreSQL wire cut, not a synthetic exception after JDBC commit. |
| S3-14 | Compatibility unknown-uncommitted/runtime-DB-outage and second-JVM cases; Core real PRODUCING/STAGED/VALIDATING controller death/recovery. New durable cancellation intent survives controlled uncertain stop and automatic reconciliation. | New intent uncertainty test injects stop-proof uncertainty; existing wire faults remain actual connection faults. No claim of a dedicated cancellation-intent ACK-loss injection or machine-wide DB shutdown. |
| S3-15 | Core actual transfer/native CPU/staging/verifier cancellation, expansion/temp failures; three deterministic finish orders retain CANCELLED; ordinary failure remains ABORTED and later terminal cancellation stays idempotent without rewriting it. Current identity-bound resource observations cover both final Java selections. | Run marker commits under lease lock before process wait. Unknown state retains reservation. Host, device-wide VRAM, sampled RAM/disk and process lifetime peaks are distinct; sampling limits retained. |
| S3-16 | Complete two-part Java selection on the same final source; Python 198 and Ruff/format/fresh-cache mypy/dependency/offline lock; preservation and diff checks. Frozen D17 corpora reused only for unchanged parser/chunker/producer/model code. | No full clean backend/P18-FINAL inference. Historical Java 1,433/1/2/0 and fresh independent 94/1/0/0 remain FAILED. No test skipped or assertion weakened. |

The final machine inventory records completion/counts and preserved failures
without turning historical failed runs into PASS. This checkpoint does not close
findings, Slice 3, FR18-3 or Phase 18 and authorizes no follow-on work.

## Exact executable mapping

These preserved executable names are rerun in the final RR selected Maven runs; `Corrective` abbreviates
`RagSlice3CorrectiveIntegrationTest`, and `ConnectionLoss` abbreviates
`RagSlice3ConnectionLossIntegrationTest`. Parameter values and complete test-case
names are retained in the source and archived Surefire XML; the matrix does not
count an individual parameter as an independently reviewed acceptance.

| Row | Executable proof |
| --- | --- |
| S3-01 | `RagNativeDocumentBatchIntegrationTest.realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck`; retained D14 stdout comparison in handback. |
| S3-02 | Corrective `capturedFormatCannotBeSpoofedByMdTask`, `editedManifestNameCannotTurnCapturedTxtIntoMd`, `folderQualifiedAndFrontmatterAliasesBindExactNestedSnapshotsAfterOriginalDeletion`, `captureTargetHashesCannotBeGraftedOntoAnUnrelatedApprovedName`; `RagDocumentInputCompatibilityTest`. |
| S3-03/04 | `RagSlice3VerifiedIndexingIntegrationTest` real external batch and all-failed cases; Corrective `explicitFailedRootStillRequiresItsExactImmutableRevisionBinding`. |
| S3-05/06 | Corrective `corruptActualNativeResultsNeverPublish` (31 faults), `anExtraRootCannotHideBehindAnAdministrativelyResealedNativeReceipt`, `byteOffsetsCannotSubstituteForOriginalUnicodeCharacterRanges`, `nativePostgresFloatCanonicalizationRetainsExactOriginalPropertyText`. |
| S3-07 | Corrective `namedNestedAnchoredChildrenRetainSourceSpecificRevisionAndCurrentness`, `folderQualifiedAndFrontmatterAliasesBindExactNestedSnapshotsAfterOriginalDeletion`, `twoFrontmatterAliasesSelectOneLargeApprovedRevisionThroughNativePublication`, `missingOrAmbiguousDependencyNeverPublishesAndCycleIsDenied`, `ambiguousAndUnauthorizedNamesFailTheWholeRoot` (5 faults), `identicalChildBytesKeepDifferentSourcesAndUnknownEmbedCannotReadSibling`, `sourceSpecificRevocationPreservesIndependentSiblingGenerationFromTheSameBatch`; Python `test_slice3_dependency_names.py`. |
| S3-08 | Corrective `corruptActualNativeResultsNeverPublish`, `unavailableCudaCannotBeClaimedByAnOtherwiseGenuineNativeReceipt`, `actualRuntimeWorkerAndPublicRolesCannotForgeV3AdmissionAndVectorsFailDatabaseTypes`. |
| S3-09 | Corrective `concurrentChildAuthorityMismatchAfterVerificationCannotPublish` (10 barriers), `pendingPointerCannotSubstituteForTheApprovedRevisionEvenWithEqualHash`, `freshJvmReloadImmediatelyQualifiesTheExactPublishedExternalGeneration`, `legacyBasenameProofKeepsItsSelectedGenerationCurrentDuringPendingCapture`. |
| S3-10 | Corrective `runtimeCannotForgeOrEraseRealProducerProofAndGenuineGenerationIsCurrent`, `actualRuntimeWorkerAndPublicRolesCannotForgeV3AdmissionAndVectorsFailDatabaseTypes`, `realActiveV3AttemptRejectsAbsentUnconsumedAndMixedV2Admission`; `RagWorkerPostgresPrivilegeIntegrationTest`; catalog inventory. |
| S3-11 | Corrective `realV3WriterAndPublicationSealUseOneAttemptFenceThroughCommit` (writer-first/seal-first/expired-after-wait), `cancellationDuringRealWorkerStagingDrainsItsPhysicalWriterBeforeRelease`; `RagStagingCommitFenceIntegrationTest`. |
| S3-12 | Corrective `multiRootPhysicalRollbackPreservesPreviouslySelectedRevisionAndIndependentGeneration` (5 physical fault locations); unchanged callback and prior physical-commit observation. |
| S3-13 | ConnectionLoss `committedWireAcknowledgementIsLostAndDurableReconciliationDoesNotRepublish`; `RagCommitAcknowledgementRecoveryIntegrationTest`. |
| S3-14 | ConnectionLoss `uncommittedWireLossRetainsReservationsWhileDatabaseIsUnreachableThenSafelyAborts`; Corrective `actualControllerDeathAndFreshJvmRecoveryPreserveExclusiveSnapshotAuthority` (PRODUCING/STAGED/VALIDATING); actual `RagSlice3RecoveryProbeProcess` receipts. |
| S3-15 | Corrective `cancellationDuringActualSnapshotHttpTransferStopsTheConsumerBeforeRelease`, `cancellationDuringMeasuredRealNativeCpuWorkStopsBeforeAnyPublication`, `cancellationDuringRealWorkerStagingDrainsItsPhysicalWriterBeforeRelease`, `cancellingActualSourceVerificationJoinsItsProcessBeforeReleaseAndCleansManagedTemp`, `managedVerificationDiskFailureCannotPublishAndRecoveryCleansTheScope`, `realNativeTransclusionExpansionFailsTheWholeRootBeforeEmbedding`; current identity-bound process-tree observer. |
| S3-16 | Complete command/receipt selection in final evidence inventory; `ApplicationArchitectureTests`, `OpenApiRouteInventoryIntegrationTest`, `RagWebIntegrationTest`, Slice 1 snapshot/corrective, legacy native worker, D17 and optional-dependency selections. |


## Additional RR executable mapping

| Rows | Current executable proof |
| --- | --- |
| S3-05/08 | Corrective `trustedInferenceRejectsNormPreservingNativeVectorForgery` (sign flip, vector swap, component permutation); exact original reviewer probes before and after; Python `test_slice3_rr.py` (9 cases with actual pinned CPU inference). |
| S3-02/07 | Corrective `domainClosureCannotReinterpretEitherSafeOrRejectedExternalMarkdown` (safe/active-HTML child), `independentDomainAndMarkdownRootsKeepTheirOwnTypedProcessingContracts`; pre-dispatch no-lease/no-reservation/no-promotion assertions. |
| S3-14/15 | Corrective `requestedCancellationWinsBothOrderedAutomaticFinishPaths` (3 deterministic orderings), `durableCancellationIntentSurvivesUncertainStopAndAutomaticReconciliation`, `ordinaryAbortStaysAbortedWhenLateCancellationIsAnIdempotentNoOp`; original actual-staging CANCELLED method is unchanged. |

Exact parameterized invocation names, durations and outcomes are in each final
Surefire archive. This table is a test-to-contract mapping, not independent review.


Resource coverage qualification: Core measured 183/184 recorded native identities;
compatibility 4/4. The original short-lived cancelled verifier remains unmeasured.
`rr-verifier-cancel-native` adds same-source live-verifier cancellation with an
observer handle acknowledgement before API cancel: CANCELLED, zero G, no selected
R/reservation, terminated verifier and cleaned managed temp. This closes scenario
measurement coverage without backfilling the missing process or inflating the
249 selected test count. Sampling/host/device/disk limits remain explicit.
