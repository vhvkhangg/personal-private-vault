package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchReport;
import com.vhvkhangg.personalprivatevault.rag.api.RagFileOutcomeView;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagFileOutcome;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagGenerationRoot;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagFileOutcomeRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagGenerationRootRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagIndexGenerationRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagStagedDocumentOutputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Computes structured document indexing batch report with fresh live CURRENT derivation (P18-10, AC-08).
 * PARTIAL_SUCCESS is a report-only derived concept, never a DB enum.
 */
@Service
public class RagDocumentReportService {

    private final RagJobLeaseRepository leases;
    private final RagFileOutcomeRepository outcomes;
    private final RagGenerationRootRepository generationRoots;
    private final RagIndexGenerationRepository indexGenerations;
    private final RagGenerationState generationState;
    private final RagStagedDocumentOutputRepository stagedOutputs;

    public RagDocumentReportService(
            RagJobLeaseRepository leases,
            RagFileOutcomeRepository outcomes,
            RagGenerationRootRepository generationRoots,
            RagIndexGenerationRepository indexGenerations,
            RagGenerationState generationState,
            RagStagedDocumentOutputRepository stagedOutputs
    ) {
        this.leases = leases;
        this.outcomes = outcomes;
        this.generationRoots = generationRoots;
        this.indexGenerations = indexGenerations;
        this.generationState = generationState;
        this.stagedOutputs = stagedOutputs;
    }

    public RagDocumentBatchReport getReport(UUID jobId) {
        Objects.requireNonNull(jobId, "jobId must not be null");

        var leaseList = leases.findByJobId(jobId);
        if (leaseList.isEmpty()) {
            throw new NoSuchElementException("Job not found: " + jobId);
        }
        RagJobLease lease = leaseList.getFirst();

        List<RagFileOutcome> outcomeList = outcomes.findByJobId(jobId);
        List<RagFileOutcomeView> views = new ArrayList<>();

        int successCount = 0;
        int failCount = 0;

        for (RagFileOutcome fo : outcomeList) {
            String currState = "NONE";
            if (fo.getCandidateGenerationId() != null) {
                // Compute live index validity at query time
                currState = generationState.state(fo.getCandidateGenerationId());
            }

            if ("SUCCESS".equalsIgnoreCase(fo.getResult())) {
                successCount++;
            } else {
                failCount++;
            }

            views.add(new RagFileOutcomeView(
                    fo.getRootKey(),
                    fo.getResult(),
                    fo.getCandidateGenerationId(),
                    fo.getWarningCodes(),
                    fo.getChunkCount(),
                    fo.getPageCount(),
                    fo.getErrorCode(),
                    fo.getErrorMessage(),
                    currState
            ));
        }

        // Derive report-only summaryOutcome
        String dbStatus = lease.getStatus();
        String summary;
        if ("COMPLETED".equals(dbStatus)) {
            if (successCount > 0 && failCount > 0) {
                summary = "PARTIAL_SUCCESS";
            } else {
                summary = "COMPLETED";
            }
        } else {
            summary = dbStatus;
        }

        Long peakRam = null;
        Long peakVram = null;
        for (var st : stagedOutputs.findByJobIdAndAttemptId(jobId, lease.getAttemptId())) {
            try {
                var map = st.getResourceReport();
                if (map != null && !map.isEmpty()) {
                    if (map.get("peak_system_ram_bytes") instanceof Number num) {
                        long r = num.longValue();
                        if (r > 0 && (peakRam == null || r > peakRam)) peakRam = r;
                    }
                    if (map.get("peak_gpu_vram_bytes") instanceof Number num) {
                        long v = num.longValue();
                        if (peakVram == null || v > peakVram) peakVram = v;
                    }
                }
            } catch (RuntimeException unavailableMeasurement) {
                // Leave unavailable data absent; it is never a measured zero.
            }
        }

        RagGenerationRoot pipeline = outcomeList.stream().map(RagFileOutcome::getCandidateGenerationId)
                .filter(Objects::nonNull).map(indexGenerations::findByGenerationId)
                .flatMap(Optional::stream).map(g -> generationRoots.findById(g.getId()))
                .flatMap(Optional::stream).findFirst().orElse(null);

        return new RagDocumentBatchReport(
                jobId,
                dbStatus,
                summary,
                views,
                pipeline == null ? null : pipeline.getModelArtifactHash(),
                pipeline == null ? null : pipeline.getModelVersion(),
                peakRam,
                peakVram,
                lease.getCreatedAt(),
                Set.of("COMPLETED", "ABORTED", "CANCELLED", "PURGED").contains(dbStatus) ? lease.getUpdatedAt() : null
        );
    }
}
