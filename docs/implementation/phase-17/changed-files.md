# Exact Phase 17 candidate file inventory

## 2026-10-16 Alternative B Implementation and Review Remediation Inventory

This inventory captures the complete file delta for the owner-authorized Phase 17 Alternative B implementation under the existing handoff `P17-RAG-FOUNDATION` (ADR-0020), including the remediations for findings FR17-AB-1 through FR17-AB-5 from the 2026-10-16 independent final review.

### Baselines and Attribution Scope

1. **Complete Inherited Delivery Baseline (versus owner HEAD `24e6268eef1685866d919d882ecdf9f532d01961`)**:
   - Review-start delivery baseline contained **244 paths** across production Java, Flyway migrations (V1–V17), backend tests/resources, Python source/tests/scripts, and Phase 17 documentation.
   - Current delivery contains **246 paths** with the addition of the 2026-10-17 Independent Final Re-Review 3 report (`docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md`).
2. **Implementation Delta Baseline (versus accepted contract-review snapshot `2026-10-15`)**:
   - Review-start delta contained **113 paths (74 modified + 39 added, 0 deletions)**.
   - Current delta contains **115 paths (74 modified + 41 added, 0 deletions)**.
   - Includes ADR-0020, `benchmarks-and-acceptance.md`, `antigravity-continuation-prompt.md`, `revocation-and-derived-data.md`, and the independent review reports.

### Implementation Delta Paths versus Contract Review Baseline (115 paths: 74 modified, 41 added)
- `AGENTS.md`
- `backend/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/ApiExceptionHandler.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobStatusView.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagOperations.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDependencyGraph.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagJobService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagLimits.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeIdentity.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagJobLease.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/RagExceptionAdvice.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagJobController.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagSourceController.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/AdmitJobRequest.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/ApproveSourceRequest.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobStatusResponse.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagSourceResponse.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterDomainSourceRequest.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/RagWebMapper.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceView.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/package-info.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/FlywayStartupIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/migration/FlywayV1SchemaManifestIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLegacyRuntimeDeniedIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPgVectorCompatibilityIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRuntimeRoleIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceAuthorityIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagStagingCommitFenceIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWebIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerFixture.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerPostgresPrivilegeIntegrationTest.java`
- `backend/dev/rag-db/README.md`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java`
- `docs/implementation/handoffs/ACTIVE.md`
- `docs/implementation/phase-17/README.md`
- `docs/implementation/phase-17/alternative-b-verification-plan.md`
- `docs/implementation/phase-17/benchmarks-and-acceptance.md`
- `docs/implementation/phase-17/implementation-report.md`
- `docs/implementation/phase-17/owner-decisions.md`
- `docs/implementation/phase-17/remediation-plan.md`
- `docs/implementation/phase-17/test-evidence.md`
- `docs/implementation/phase-17/changed-files.md`
- `rag-engine/scripts/dependency_inventory.py`
- `rag-engine/src/ppv_rag/config.py`
- `rag-engine/src/ppv_rag/db.py`
- `rag-engine/src/ppv_rag/main.py`
- `rag-engine/src/ppv_rag/transport.py`
- `rag-engine/tests/test_transport.py`
- `rag-engine/tests/test_worker.py`

### Added paths (37)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagConsentCommand.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagIndexCommand.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobBusyException.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagSourceChangedException.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagGenerationState.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagManifestScope.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerSupervisor.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceEditGuard.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceIndexReservations.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersion.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersions.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultSourceReservations.java`
- `backend/src/main/resources/db/migration/V14__rag_source_state_and_reservations.sql`
- `backend/src/main/resources/db/migration/V15__rag_single_job_and_sealed_worker.sql`
- `backend/src/main/resources/db/migration/V16__rag_current_read_and_terminal_scrub.sql`
- `backend/src/main/resources/db/migration/V17__rag_retained_consent_history.sql`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractEnabledRagIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCancellationRecoveryIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCommitAcknowledgementRecoveryIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagE1ExpiryIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFoundationBenchmark.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagNativeWorkerProcessTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPythonCommitTerminationIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRecoveryProbeProcess.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSecondJvmRecoveryIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSingleJobRuntimeIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceReservationsIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagTestCommands.java`
- `backend/src/test/resources/rag/retained_consent_history.sql`
- `backend/src/test/resources/rag/single_job_staging.sql`
- `backend/src/test/resources/rag/source_state_reservations.sql`
- `docs/adr/0020-phase-17-single-job-reservations-e1.md`
- `docs/implementation/phase-17/antigravity-continuation-prompt.md`
- `docs/implementation/phase-17/revocation-and-derived-data.md`
- `docs/implementation/phase-17/reviews/2026-10-16-phase-17-alternative-b-independent-final-review.md`
- `docs/implementation/phase-17/reviews/2026-10-16-phase-17-alternative-b-independent-final-re-review.md`
- `docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review.md`
- `docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-2.md`
- `docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md`
- `rag-engine/scripts/worker_boundary_check.py`
- `rag-engine/tests/conftest.py`
- `rag-engine/tests/test_bound_ingress.py`

## Consumption-fence design revision 2 — draft only

This revision changes exactly three existing documentation paths relative to
its own initial SHA-256 snapshot: this inventory, the [draft proposal](p17-3-consumption-fence-proposal.md)
and [remediation plan](remediation-plan.md). No production/test/migration,
accepted contract/ADR/owner decision, ACTIVE or historical review changes.
The independent design-review report was already present at this revision's
start and is preserved byte-for-byte; it is not authored by this pass.
Delivery contains 182 paths (the previous 181 plus that independent review).
Earlier per-pass counts below remain historical attributions.

- [Revised draft — not approved](p17-3-consumption-fence-proposal.md)
- [Current draft remediation plan](remediation-plan.md)
- [This inventory](changed-files.md)

## Subsequent design-only proposal — not an approved contract delta

This planning pass changes exactly four documentation paths compared with its
own initial SHA-256 snapshot: ACTIVE.md, remediation-plan.md, this inventory and
the new [P17-3 consumption-fence proposal](p17-3-consumption-fence-proposal.md).
No production, test, accepted security contract, historical review, migration or
containment file changes in this pass. The delivery now contains 181 paths
(173 candidate paths plus eight preserved inputs). The containment continuation
and earlier candidate inventories below remain attributed to those earlier passes.

- [New design proposal — not approved](p17-3-consumption-fence-proposal.md)
- [Current plan](remediation-plan.md)
- [Existing ACTIVE handoff](../handoffs/ACTIVE.md)
- [This inventory](changed-files.md)

## 2026-10-15 blocked containment continuation

This continuation changes exactly the following 14 paths compared with its
initial working-tree SHA-256 snapshot; two tests are new. The initial 178-path
delivery therefore becomes 180 paths (172 candidate paths, eight preserved
inputs). The previous lists remain the inherited submission inventory, not a
claim that this continuation authored it. Historical review reports are unchanged.

- [ACTIVE.md](../handoffs/ACTIVE.md)
- [Phase README](README.md)
- [Implementation report](implementation-report.md)
- [Remediation plan](remediation-plan.md)
- [Test evidence](test-evidence.md)
- [Changed-file inventory](changed-files.md)
- [RagWorkerGrant.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagWorkerGrant.java)
- [RagWorkerGrantService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java)
- [RagEngineClient.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java)
- [RAG module descriptor](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/package-info.java)
- [RagConsumptionContainmentTest.java — new](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagConsumptionContainmentTest.java)
- [Worker README](../../../rag-engine/README.md)
- [transport.py](../../../rag-engine/src/ppv_rag/transport.py)
- [test_consumption_containment.py — new](../../../rag-engine/tests/test_consumption_containment.py)

This is a blocked security-containment submission. It neither implements the
proposed synchronization amendment nor establishes enabled benchmark/offline
acceptance. See the current plan and fresh evidence.

Baseline: `24e6268eef1685866d919d882ecdf9f532d01961` (unchanged owner HEAD). This lists the full uncommitted candidate relative to owner HEAD, including inherited Phase 17 submission files; it does not attribute every file to this remediation cycle. No commit/push was performed.

## Candidate implementation and current documentation

Count:
170
.

- [AGENTS.md](../../../AGENTS.md)
- [backend/AGENTS.md](../../../backend/AGENTS.md)
- [backend/compose.rag.dev.yml](../../../backend/compose.rag.dev.yml)
- [backend/dev/rag-db/provision.ps1](../../../backend/dev/rag-db/provision.ps1)
- [backend/dev/rag-db/ProvisionRagDevelopment.java](../../../backend/dev/rag-db/ProvisionRagDevelopment.java)
- [backend/dev/rag-db/README.md](../../../backend/dev/rag-db/README.md)
- [backend/dev/rag-postgres/Dockerfile](../../../backend/dev/rag-postgres/Dockerfile)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceValues.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceValues.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionStoryArchetypeRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionStoryArchetypeRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionWorldSettingRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionWorldSettingRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceOperations.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceOperations.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceProjection.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceProjection.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceValues.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceValues.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmGenreAssignmentRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmGenreAssignmentRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmStoryArchetypeRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmStoryArchetypeRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmWorldSettingRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmWorldSettingRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceOperations.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceOperations.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceProjection.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceProjection.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagFinalizationResult.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagFinalizationResult.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobAdmissionResult.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobAdmissionResult.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobStatusView.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobStatusView.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagOperations.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagOperations.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDependencyGraph.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDependencyGraph.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagJobService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagJobService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagLimits.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagLimits.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeIdentity.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeIdentity.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeSecurity.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeSecurity.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagDependency.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagDependency.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagFieldAllowlist.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagFieldAllowlist.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagIndexGeneration.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagIndexGeneration.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagJobLease.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagJobLease.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagRun.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagRun.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagStagedOutput.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagStagedOutput.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagWorkerGrant.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagWorkerGrant.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagDependencyRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagDependencyRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagIndexGenerationRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagIndexGenerationRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagJobLeaseRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagJobLeaseRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagRunRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagRunRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagSourceRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagSourceRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagStagedOutputRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagStagedOutputRepository.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/RagExceptionAdvice.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/RagExceptionAdvice.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagJobController.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagJobController.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagSourceController.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagSourceController.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/AdmitJobRequest.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/AdmitJobRequest.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/ApproveSourceRequest.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/ApproveSourceRequest.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/FinalizeGenerationRequest.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/FinalizeGenerationRequest.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagFinalizationResponse.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagFinalizationResponse.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobAdmissionResponse.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobAdmissionResponse.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobStatusResponse.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobStatusResponse.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagSourceResponse.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagSourceResponse.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterDomainSourceRequest.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterDomainSourceRequest.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterFileSourceRequest.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterFileSourceRequest.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/RagWebMapper.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/RagWebMapper.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/package-info.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/package-info.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagAccessState.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagAccessState.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagClassification.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagClassification.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceType.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceType.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceView.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceView.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultEntryOperations.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultEntryOperations.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultSourceGuard.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultSourceGuard.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java)
- [backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java)
- [backend/src/main/resources/application.yml](../../../backend/src/main/resources/application.yml)
- [backend/src/main/resources/application-rag-runtime.yml](../../../backend/src/main/resources/application-rag-runtime.yml)
- [backend/src/main/resources/db/migration/V10__rag_complete_worker_authority.sql](../../../backend/src/main/resources/db/migration/V10__rag_complete_worker_authority.sql)
- [backend/src/main/resources/db/migration/V11__rag_disabled_source_tombstones.sql](../../../backend/src/main/resources/db/migration/V11__rag_disabled_source_tombstones.sql)
- [backend/src/main/resources/db/migration/V12__rag_exact_worker_dependency_proof.sql](../../../backend/src/main/resources/db/migration/V12__rag_exact_worker_dependency_proof.sql)
- [backend/src/main/resources/db/migration/V13__rag_effective_database_privileges.sql](../../../backend/src/main/resources/db/migration/V13__rag_effective_database_privileges.sql)
- [backend/src/main/resources/db/migration/V3__rag_foundation.sql](../../../backend/src/main/resources/db/migration/V3__rag_foundation.sql)
- [backend/src/main/resources/db/migration/V4__rag_worker_security.sql](../../../backend/src/main/resources/db/migration/V4__rag_worker_security.sql)
- [backend/src/main/resources/db/migration/V5__rag_staging_definer_and_indexes.sql](../../../backend/src/main/resources/db/migration/V5__rag_staging_definer_and_indexes.sql)
- [backend/src/main/resources/db/migration/V6__rag_security_hardening.sql](../../../backend/src/main/resources/db/migration/V6__rag_security_hardening.sql)
- [backend/src/main/resources/db/migration/V7__rag_issued_worker_grants.sql](../../../backend/src/main/resources/db/migration/V7__rag_issued_worker_grants.sql)
- [backend/src/main/resources/db/migration/V8__rag_commit_deadlines_and_consent_instances.sql](../../../backend/src/main/resources/db/migration/V8__rag_commit_deadlines_and_consent_instances.sql)
- [backend/src/main/resources/db/migration/V9__rag_runtime_authority.sql](../../../backend/src/main/resources/db/migration/V9__rag_runtime_authority.sql)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/ApplicationArchitectureTests.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/ApplicationArchitectureTests.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/FlywayStartupIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/FlywayStartupIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagAuthorityContractTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagAuthorityContractTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLegacyRuntimeDeniedIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLegacyRuntimeDeniedIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPgVectorCompatibilityIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPgVectorCompatibilityIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRuntimeRoleIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRuntimeRoleIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceAuthorityIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceAuthorityIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagStagingCommitFenceIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagStagingCommitFenceIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWebIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWebIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWindowsNtfsFileGuardTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWindowsNtfsFileGuardTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerFixture.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerFixture.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerPostgresPrivilegeIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerPostgresPrivilegeIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java)
- [backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java)
- [docs/architecture/data-architecture.md](../../../docs/architecture/data-architecture.md)
- [docs/architecture/diagrams/exported/container.png](../../../docs/architecture/diagrams/exported/container.png)
- [docs/architecture/diagrams/exported/container.svg](../../../docs/architecture/diagrams/exported/container.svg)
- [docs/architecture/diagrams/exported/module-dependencies.png](../../../docs/architecture/diagrams/exported/module-dependencies.png)
- [docs/architecture/diagrams/exported/module-dependencies.svg](../../../docs/architecture/diagrams/exported/module-dependencies.svg)
- [docs/architecture/diagrams/exported/module-dependency-overview.png](../../../docs/architecture/diagrams/exported/module-dependency-overview.png)
- [docs/architecture/diagrams/exported/module-dependency-overview.svg](../../../docs/architecture/diagrams/exported/module-dependency-overview.svg)
- [docs/architecture/diagrams/source/container.dot](../../../docs/architecture/diagrams/source/container.dot)
- [docs/architecture/diagrams/source/module-dependencies.dot](../../../docs/architecture/diagrams/source/module-dependencies.dot)
- [docs/architecture/diagrams/source/module-dependencies.drawio](../../../docs/architecture/diagrams/source/module-dependencies.drawio)
- [docs/architecture/module-boundaries.md](../../../docs/architecture/module-boundaries.md)
- [docs/architecture/module-dependency-matrix.md](../../../docs/architecture/module-dependency-matrix.md)
- [docs/architecture/rag-architecture.md](../../../docs/architecture/rag-architecture.md)
- [docs/architecture/README.md](../../../docs/architecture/README.md)
- [docs/architecture/structurizr/workspace.dsl](../../../docs/architecture/structurizr/workspace.dsl)
- [docs/database/README.md](../../../docs/database/README.md)
- [docs/implementation/handoffs/ACTIVE.md](../../../docs/implementation/handoffs/ACTIVE.md)
- [docs/implementation/phase-17/changed-files.md](../../../docs/implementation/phase-17/changed-files.md)
- [docs/implementation/phase-17/implementation-report.md](../../../docs/implementation/phase-17/implementation-report.md)
- [docs/implementation/phase-17/README.md](../../../docs/implementation/phase-17/README.md)
- [docs/implementation/phase-17/remediation-plan.md](../../../docs/implementation/phase-17/remediation-plan.md)
- [docs/implementation/phase-17/test-evidence.md](../../../docs/implementation/phase-17/test-evidence.md)
- [docs/repository/repository-package-tree.md](../../../docs/repository/repository-package-tree.md)
- [docs/roadmap.md](../../../docs/roadmap.md)
- [rag-engine/.dockerignore](../../../rag-engine/.dockerignore)
- [rag-engine/.python-version](../../../rag-engine/.python-version)
- [rag-engine/Dockerfile.offline-check](../../../rag-engine/Dockerfile.offline-check)
- [rag-engine/pyproject.toml](../../../rag-engine/pyproject.toml)
- [rag-engine/README.md](../../../rag-engine/README.md)
- [rag-engine/scripts/dependency_inventory.py](../../../rag-engine/scripts/dependency_inventory.py)
- [rag-engine/scripts/offline_check.py](../../../rag-engine/scripts/offline_check.py)
- [rag-engine/src/ppv_rag/__init__.py](../../../rag-engine/src/ppv_rag/__init__.py)
- [rag-engine/src/ppv_rag/config.py](../../../rag-engine/src/ppv_rag/config.py)
- [rag-engine/src/ppv_rag/contracts.py](../../../rag-engine/src/ppv_rag/contracts.py)
- [rag-engine/src/ppv_rag/db.py](../../../rag-engine/src/ppv_rag/db.py)
- [rag-engine/src/ppv_rag/main.py](../../../rag-engine/src/ppv_rag/main.py)
- [rag-engine/src/ppv_rag/security.py](../../../rag-engine/src/ppv_rag/security.py)
- [rag-engine/src/ppv_rag/transport.py](../../../rag-engine/src/ppv_rag/transport.py)
- [rag-engine/src/ppv_rag/worker.py](../../../rag-engine/src/ppv_rag/worker.py)
- [rag-engine/tests/test_contracts.py](../../../rag-engine/tests/test_contracts.py)
- [rag-engine/tests/test_health.py](../../../rag-engine/tests/test_health.py)
- [rag-engine/tests/test_transport.py](../../../rag-engine/tests/test_transport.py)
- [rag-engine/tests/test_worker.py](../../../rag-engine/tests/test_worker.py)
- [rag-engine/uv.lock](../../../rag-engine/uv.lock)

## Pre-existing inputs preserved

These edits/review inputs existed before this cycle and were preserved. Historical reviews were not rewritten.
- [.codex/config.toml](../../../.codex/config.toml)
- [.gitignore](../../../.gitignore)
- [docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review.md](../../../docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review.md)
- [docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-2.md](../../../docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-2.md)
- [docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-3.md](../../../docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-3.md)
- [docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-review.md](../../../docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-review.md)
- [docs/implementation/phase-17/reviews/README.md](../../../docs/implementation/phase-17/reviews/README.md)

## Independent review-only delivery addition — 2026-10-15

The reviewed submission above contains 170 candidate paths plus seven pre-existing inputs (177 total). The new report below brings the current delivery inventory to 178 paths. Documentation synchronization modifies already listed paths; production code/tests remain unchanged by the reviewer. Historical review reports remain unchanged.

- [docs/implementation/phase-17/reviews/2026-10-15-phase-17-independent-final-codex-review.md](../../../docs/implementation/phase-17/reviews/2026-10-15-phase-17-independent-final-codex-review.md)

## 2026-10-15 — owner-approved Alternative B contract synchronization

The separate [exact documentation delta and consistency record](contract-synchronization.md) inventories 26 modified and four added contract/instruction/inventory documents relative to the pre-session snapshot. This does not change the inherited application candidate or certify implementation. ACTIVE remains CHANGES_REQUESTED; FR17-5/8/9/10 OPEN; containment remains. No code/tests/migrations/historical review or evidence file changed in this pass.
