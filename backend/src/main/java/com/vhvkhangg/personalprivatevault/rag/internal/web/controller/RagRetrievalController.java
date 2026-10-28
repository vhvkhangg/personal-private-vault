package com.vhvkhangg.personalprivatevault.rag.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalCancelResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagRetrievalCancelResponseDto;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagRetrievalRequestDto;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagRetrievalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rag/retrievals")
@RequiredArgsConstructor
@Tag(name = "RAG Retrieval", description = "Authorized read-only Markdown evidence retrieval and cancellation")
public class RagRetrievalController {

    private final RagRetrievalService ragRetrievalService;

    @PostMapping
    @Operation(summary = "Retrieve authorized Markdown evidence with citations", operationId = "retrieveRagMarkdownEvidence")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authorized Markdown retrieval matches or empty evidence"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request format or query bounds exceeded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Selected resource unavailable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "RAG reader busy or cancelled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Query service or authority unavailable")
    })
    public ResponseEntity<ApiResponse<RagRetrievalResponseDto>> retrieve(
            @Valid @RequestBody RagRetrievalRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getContentLengthLong() > 65_536) {
            throw new com.vhvkhangg.personalprivatevault.rag.api.RagQueryScopeLimitException("Request body exceeds 64 KiB limit");
        }
        if (request.isAsyncSupported()) {
            AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(120_000);
            ragRetrievalService.dispatchAsync(requestDto.toCommand(), asyncContext, RagRetrievalResponseDto::fromResponse);
            return null;
        } else {
            RagRetrievalResponse result = ragRetrievalService.retrieve(requestDto.toCommand());
            return ApiResponses.ok(RagRetrievalResponseDto.fromResponse(result));
        }
    }

    @PostMapping("/{requestId}/cancel")
    @Operation(summary = "Cancel RAG Markdown retrieval and achieve reader quiescence", operationId = "cancelRagMarkdownRetrieval")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reader terminal after verified stop, or already terminal"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reader not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Reader stop or finish remains unproven; exclusion retained")
    })
    public ResponseEntity<ApiResponse<RagRetrievalCancelResponseDto>> cancel(
            @PathVariable UUID requestId
    ) {
        RagRetrievalCancelResult result = ragRetrievalService.cancel(requestId);
        return ApiResponses.ok(RagRetrievalCancelResponseDto.fromResult(result));
    }
}
