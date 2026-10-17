package com.vhvkhangg.personalprivatevault.rag.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.rag.api.RagFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobStatusView;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.AdmitJobRequest;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.FinalizeGenerationRequest;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagFinalizationResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagJobAdmissionResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagJobStatusResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.web.mapper.RagWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rag/jobs")
@RequiredArgsConstructor
@Tag(name = "RAG Jobs", description = "RAG durable jobs, bounded admission, status, cancellation, and guarded finalization")
public class RagJobController {

    private final RagOperations ragOperations;

    @PostMapping("/admit")
    @Operation(summary = "Admit RAG job with short-lived scoped lease", operationId = "admitRagJob")
    public ResponseEntity<ApiResponse<RagJobAdmissionResponse>> admit(@Valid @RequestBody AdmitJobRequest request) {
        RagJobAdmissionResult result = ragOperations.admitJob(request.sourceId(),
                new com.vhvkhangg.personalprivatevault.rag.api.RagIndexCommand(request.expectedSourceVersion(),
                        request.expectedAuthorizationEpoch(), request.expectedFieldPolicyVersion(), request.descriptionConfirmations()));
        return ApiResponses.created(RagWebMapper.toResponse(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sanitized RAG job status", operationId = "getRagJobStatus")
    public ResponseEntity<ApiResponse<RagJobStatusResponse>> getStatus(@PathVariable UUID id) {
        RagJobStatusView view = ragOperations.getJobStatus(id);
        return ApiResponses.ok(RagWebMapper.toResponse(view));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel RAG job and purge derivative staging data", operationId = "cancelRagJob")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable UUID id) {
        ragOperations.cancelJob(id);
        return ApiResponses.ok(null);
    }

    @PostMapping("/{id}/finalize")
    @Operation(summary = "Java-owned guarded finalization and index publication", operationId = "finalizeRagGeneration")
    public ResponseEntity<ApiResponse<RagFinalizationResponse>> finalize(
            @PathVariable UUID id,
            @Valid @RequestBody FinalizeGenerationRequest request) {
        RagFinalizationResult result = ragOperations.finalizeGeneration(id, request.attemptId(), request.dependencyDigest());
        return ApiResponses.ok(RagWebMapper.toResponse(result));
    }
}
