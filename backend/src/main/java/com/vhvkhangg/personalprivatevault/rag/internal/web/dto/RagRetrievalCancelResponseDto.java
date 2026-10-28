package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalCancelResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response confirming retrieval reader cancellation")
public record RagRetrievalCancelResponseDto(
        @Schema(description = "Request UUID identifying reader", example = "13f7f818-0cd0-4a74-b523-2b4b8b6d5bbf")
        UUID requestId,

        @Schema(description = "Terminal disposition of the reader", example = "CANCELLED")
        String status,

        @Schema(description = "Cancellation message", example = "Retrieval reader cancelled")
        String message
) {
    public static RagRetrievalCancelResponseDto fromResult(RagRetrievalCancelResult result) {
        return new RagRetrievalCancelResponseDto(result.requestId(), result.status(), result.message());
    }
}
