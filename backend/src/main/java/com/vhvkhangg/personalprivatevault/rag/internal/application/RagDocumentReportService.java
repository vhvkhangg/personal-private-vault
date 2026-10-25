package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchReport;
import com.vhvkhangg.personalprivatevault.rag.api.RagFileOutcomeView;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
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
    private final RagBatchRootRepository batchRoots;
    private final RagSourceRepository sources;
    private final RagRunRepository runs;
    private final RagExternalFileAuthority externalFileAuthority;

    public RagDocumentReportService(
            RagJobLeaseRepository leases,
            RagFileOutcomeRepository outcomes,
            RagGenerationRootRepository generationRoots,
            RagIndexGenerationRepository indexGenerations,
            RagGenerationState generationState,
            RagStagedDocumentOutputRepository stagedOutputs,
            RagBatchRootRepository batchRoots,
            RagSourceRepository sources, RagRunRepository runs,
            RagExternalFileAuthority externalFileAuthority
    ) {
        this.leases = leases;
        this.outcomes = outcomes;
        this.generationRoots = generationRoots;
        this.indexGenerations = indexGenerations;
        this.generationState = generationState;
        this.stagedOutputs = stagedOutputs;
        this.batchRoots = batchRoots;
        this.sources = sources;this.runs=runs;
        this.externalFileAuthority = externalFileAuthority;
    }

    public RagDocumentBatchReport getReport(UUID jobId) {
        Objects.requireNonNull(jobId, "jobId must not be null");

        var leaseList = leases.findByJobId(jobId);
        if (leaseList.isEmpty()) {
            return nonpublicationReport(jobId);
        }
        RagJobLease lease = leaseList.getFirst();

        List<RagFileOutcome> outcomeList = outcomes.findByJobId(jobId);
        List<RagBatchRoot> batchRootList = batchRoots.findByJobId(jobId);
        Map<String, RagBatchRoot> rootsByKey = new HashMap<>();
        for (var r : batchRootList) {
            rootsByKey.put(r.getRootKey(), r);
        }
        List<RagFileOutcomeView> views = new ArrayList<>();

        int successCount = 0;
        int failCount = 0;

        var byKey=new HashMap<String,RagFileOutcome>();outcomeList.forEach(o -> byKey.put(o.getRootKey(),o));
        // The immutable admitted roster determines cardinality even before physical finish.
        var ordered=new ArrayList<RagBatchRoot>();
        try {
            var parser=new com.fasterxml.jackson.databind.ObjectMapper();
            var run=runs.findByJobId(jobId).orElseThrow();var metrics=parser.readTree(run.getMetrics());
            if(metrics.has("roster_order")) {
                for(var key:metrics.path("roster_order")){var root=rootsByKey.get(key.asText());if(root!=null)ordered.add(root);}
            } else if(!"SCRUBBED".equals(lease.getDependencyManifest())) {
                for(var node:parser.readTree(lease.getDependencyManifest()).path("roots")) {
                    var root=rootsByKey.get(node.path("root_key").asText());if(root!=null)ordered.add(root);
                }
            } else {ordered.addAll(batchRootList.stream().sorted(Comparator.comparing(RagBatchRoot::getRootKey)).toList());}
        } catch(java.io.IOException invalid) {throw new IllegalStateException("Invalid report roster",invalid);}
        if(ordered.size()!=batchRootList.size())throw new IllegalStateException("Report roster is incomplete");
        for (RagBatchRoot root : ordered) {
            RagFileOutcome fo=byKey.get(root.getRootKey());
            if(fo==null) {
                String result=Set.of("CANCELLED","ABORTED","PURGED","RECOVERY_REQUIRED").contains(lease.getStatus())?lease.getStatus():"PENDING";
                fo=new RagFileOutcome(jobId,root.getRootKey(),result,null,List.of(),0,0,null,null,lease.getUpdatedAt());
            }
            RagSource source = (root != null) ? sources.findById(root.getSourceId()).orElse(null) : null;
            UUID effectiveGenId = fo.getCandidateGenerationId();
            if (effectiveGenId == null && source != null) {
                effectiveGenId = indexGenerations.findBySourceId(source.getId()).stream()
                        .filter(RagIndexGeneration::isActive)
                        .findFirst()
                        .map(RagIndexGeneration::getGenerationId)
                        .orElse(null);
            }

            String currState = "NONE";
            String freshnessBasis = null;
            String snapshotIndexState = null;
            String originalFreshness = null;
            boolean publishedNewGeneration = "SUCCESS".equalsIgnoreCase(fo.getResult());
            UUID selectedRevisionId = null;
            UUID pendingRevisionId = null;

            if (source != null && source.getSourceType() == RagSourceType.EXTERNAL_FILE) {
                freshnessBasis = "RAG_SNAPSHOT";
                originalFreshness = "UNVERIFIED";
                UUID extId = UUID.fromString(source.getSourceId());
                RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(extId);
                if (ext != null) {
                    selectedRevisionId = ext.getSelectedRevisionId();
                    pendingRevisionId = ext.getPendingRevisionId();
                }
                if (effectiveGenId != null) {
                    currState = generationState.state(effectiveGenId);
                    snapshotIndexState = "CURRENT".equals(currState)?"SNAPSHOT_CURRENT":currState;
                } else {
                    currState = "NONE";
                    snapshotIndexState = "ABSENT";
                }
            } else {
                if (effectiveGenId != null) {
                    currState = generationState.state(effectiveGenId);
                }
                freshnessBasis = (source != null) ? "VAULT" : null;
            }

            if ("SUCCESS".equalsIgnoreCase(fo.getResult())) {
                successCount++;
            } else if ("NO_CHANGE".equalsIgnoreCase(fo.getResult())) {
                // NO_CHANGE is an unmutated/retained disposition, not a failure
            } else {
                failCount++;
            }

            views.add(new RagFileOutcomeView(
                    fo.getRootKey(),
                    fo.getResult(),
                    effectiveGenId,
                    fo.getWarningCodes(),
                    fo.getChunkCount(),
                    fo.getPageCount(),
                    fo.getErrorCode(),
                    fo.getErrorMessage(),
                    currState,
                    freshnessBasis,
                    snapshotIndexState,
                    originalFreshness,
                    publishedNewGeneration,
                    selectedRevisionId,
                    pendingRevisionId
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
    private RagDocumentBatchReport nonpublicationReport(UUID operation) {
        var run=runs.findByRunId(operation).filter(r -> r.getJobId()==null).orElseThrow(() -> new NoSuchElementException("Operation not found"));
        var views=new ArrayList<RagFileOutcomeView>();
        try {
            var metrics=new com.fasterxml.jackson.databind.ObjectMapper().readTree(run.getMetrics());
            if(!"NONPUBLICATION".equals(metrics.path("kind").asText()))throw new NoSuchElementException("Operation not found");
            String disposition="COMPLETED".equals(run.getStatus())?"NO_CHANGE":"ADMITTED".equals(run.getStatus())?"RECOVERY_REQUIRED":"CANCELLED".equals(run.getStatus())?"CANCELLED":"ABORTED";
            for(var root:metrics.path("roots")) {
                var source=sources.findById(root.path("source_id").asLong()).orElse(null);
                UUID gen=UUID.fromString(root.path("generation_id").asText());String state=generationState.state(gen);
                boolean external=source!=null && source.getSourceType()==RagSourceType.EXTERNAL_FILE;
                var ext=external?externalFileAuthority.findByFileSourceId(UUID.fromString(source.getSourceId())):null;
                views.add(new RagFileOutcomeView(root.path("root_key").asText(),disposition,gen,List.of(),0,0,null,null,state,
                    external?"RAG_SNAPSHOT":"VAULT",external?("CURRENT".equals(state)?"SNAPSHOT_CURRENT":state):null,external?"UNVERIFIED":null,false,
                    ext==null?null:ext.getSelectedRevisionId(),ext==null?null:ext.getPendingRevisionId()));
            }
            // No indexing job status is fabricated for a C_N operation.
            return new RagDocumentBatchReport(null,null,disposition,views,null,null,null,null,run.getCreatedAt(),"ADMITTED".equals(run.getStatus())?null:run.getUpdatedAt(),operation);
        } catch(java.io.IOException invalid) {throw new IllegalStateException("Invalid operation report",invalid);}
    }

}
