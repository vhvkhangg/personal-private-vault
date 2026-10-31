package com.vhvkhangg.personalprivatevault.rag.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.rag.api.RagQaCancelResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagQaResponse;
import com.vhvkhangg.personalprivatevault.rag.api.RagQueryScopeLimitException;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaService;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaCancelResponseDto;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaRequestDto;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; do not enable or deploy.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 */
@ConditionalOnProperty(name = "ppv.rag.test-scoped-execution", havingValue = "true")
@RestController
@RequestMapping("/api/v1/rag/qa")
@RequiredArgsConstructor
@Tag(name = "RAG QA", description = "Grounded offline Markdown question answering and cancellation")
public class RagQaController {

    private final RagQaService ragQaService;

    @PostMapping
    @Operation(summary = "Answer question grounded in permitted Markdown evidence", operationId = "answerRagMarkdownQuestion")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Grounded answer with claims and citations, or abstention"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request format or query bounds exceeded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Selected resource unavailable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "RAG reader busy or cancelled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Query service or authority unavailable")
    })
    public ResponseEntity<ApiResponse<RagQaResponseDto>> answer(
            @Valid @RequestBody RagQaRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getContentLengthLong() > 65_536) {
            throw new RagQueryScopeLimitException("Request body exceeds 64 KiB limit");
        }
        if (request.isAsyncSupported()) {
            AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(240_000);
            ragQaService.dispatchAsync(requestDto.toCommand(), asyncContext, RagQaResponseDto::fromResponse);
            return null;
        } else {
            RagQaResponse result = ragQaService.answer(requestDto.toCommand());
            return ApiResponses.ok(RagQaResponseDto.fromResponse(result));
        }
    }

    @PostMapping("/{requestId}/cancel")
    @Operation(summary = "Cancel grounded Markdown QA and achieve reader quiescence", operationId = "cancelRagMarkdownQuestion")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "QA reader cancelled and quiescent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reader unavailable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Reader quiescence or stop unproven")
    })
    public ResponseEntity<ApiResponse<RagQaCancelResponseDto>> cancel(
            @PathVariable UUID requestId
    ) {
        RagQaCancelResult result = ragQaService.cancel(requestId);
        return ApiResponses.ok(RagQaCancelResponseDto.fromResult(result));
    }
}
