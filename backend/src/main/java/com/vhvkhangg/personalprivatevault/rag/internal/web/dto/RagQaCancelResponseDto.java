package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaCancelResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response confirming grounded QA reader cancellation")
public record RagQaCancelResponseDto(
        @Schema(description = "Reader request UUID", example = "13f7f818-0cd0-4a74-b523-2b4b8b6d5bbf")
        UUID requestId,

        @Schema(description = "Terminal reader disposition", example = "CANCELLED")
        String disposition,

        @Schema(description = "Status description", example = "QA reader stopped")
        String message
) {
    public static RagQaCancelResponseDto fromResult(RagQaCancelResult result) {
        if (result == null) return null;
        return new RagQaCancelResponseDto(result.requestId(), result.disposition(), result.message());
    }
}
