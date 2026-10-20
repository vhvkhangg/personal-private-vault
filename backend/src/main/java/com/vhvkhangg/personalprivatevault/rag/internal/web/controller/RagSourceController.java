package com.vhvkhangg.personalprivatevault.rag.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.ApproveSourceRequest;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagSourceResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RegisterDomainSourceRequest;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RegisterFileSourceRequest;
import com.vhvkhangg.personalprivatevault.rag.internal.web.mapper.RagWebMapper;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/rag/sources")
@RequiredArgsConstructor
@Tag(name = "RAG Sources", description = "RAG source registration, explicit two-authority approval, and revocation")
public class RagSourceController {

    private final RagOperations ragOperations;

    @PostMapping("/register-file")
    @Operation(summary = "Register external file candidate", operationId = "registerFileCandidate")
    public ResponseEntity<ApiResponse<RagSourceResponse>> registerFile(@Valid @RequestBody RegisterFileSourceRequest request) {
        RagSourceView view = ragOperations.registerSnapshotCandidate(request.filePath());
        return ApiResponses.created(RagWebMapper.toResponse(view));
    }

    @PostMapping("/{id}/capture-revision")
    @Operation(summary = "Capture updated pending revision for external file", operationId = "capturePendingFileRevision")
    public ResponseEntity<ApiResponse<RagSourceResponse>> captureRevision(
            @PathVariable Long id,
            @Valid @RequestBody com.vhvkhangg.personalprivatevault.rag.internal.web.dto.CaptureSnapshotRequest request) {
        RagSourceView view = ragOperations.capturePendingRevision(id,
                new com.vhvkhangg.personalprivatevault.rag.api.RagSnapshotCaptureCommand(request.filePath(),request.expectedSnapshot()));
        return ApiResponses.ok(RagWebMapper.toResponse(view));
    }

    @PostMapping("/register-domain")
    @Operation(summary = "Register domain candidate (Fiction or Film)", operationId = "registerDomainCandidate")
    public ResponseEntity<ApiResponse<RagSourceResponse>> registerDomain(@Valid @RequestBody RegisterDomainSourceRequest request) {
        RagSourceView view = ragOperations.registerDomainCandidate(request.sourceType(), request.sourceId(),
                request.classification(), request.expectedAuthorizationEpoch(), request.expectedFieldPolicyVersion());
        return ApiResponses.created(RagWebMapper.toResponse(view));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get RAG source by ID", operationId = "getRagSource")
    public ResponseEntity<ApiResponse<RagSourceResponse>> get(@PathVariable Long id) {
        return ragOperations.findSource(id)
                .map(view -> ApiResponses.ok(RagWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "RAG_SOURCE_NOT_FOUND", "RAG source not found"));
    }

    @GetMapping
    @Operation(summary = "List RAG sources", operationId = "listRagSources")
    public ResponseEntity<ApiResponse<List<RagSourceResponse>>> list(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        List<RagSourceResponse> responses = ragOperations.listSources(offset, limit).stream()
                .map(RagWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(responses);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Explicit owner approval for source with exact field set", operationId = "approveRagSource")
    public ResponseEntity<ApiResponse<RagSourceResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveSourceRequest request) {
        var command = new com.vhvkhangg.personalprivatevault.rag.api.RagConsentCommand(request.approvedFields(),
                request.expectedSourceVersion(), request.expectedConsentResetVersion(), request.expectedAuthorizationEpoch(),
                request.expectedFieldPolicyVersion(), request.confirmedDescriptionHash(), request.expectedSnapshot());
        RagSourceView view = ragOperations.approveSource(id, command);
        return ApiResponses.ok(RagWebMapper.toResponse(view));
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke RAG source with synchronous deny and transitive invalidation", operationId = "revokeRagSource")
    public ResponseEntity<ApiResponse<RagSourceResponse>> revoke(@PathVariable Long id) {
        RagSourceView view = ragOperations.revokeSource(id);
        return ApiResponses.ok(RagWebMapper.toResponse(view));
    }
}
