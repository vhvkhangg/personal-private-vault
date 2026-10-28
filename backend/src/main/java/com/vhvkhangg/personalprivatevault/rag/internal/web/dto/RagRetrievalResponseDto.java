package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalOutcome;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Response containing verified Markdown retrieval evidence")
public record RagRetrievalResponseDto(
        @Schema(description = "Request UUID identifying reader", example = "13f7f818-0cd0-4a74-b523-2b4b8b6d5bbf")
        UUID requestId,

        @Schema(description = "Retrieval outcome (EVIDENCE or NO_EVIDENCE)", example = "EVIDENCE")
        RagRetrievalOutcome outcome,

        @Schema(description = "Ranked list of verified citation matches")
        List<RagRetrievalMatchDto> matches
) {
    public static RagRetrievalResponseDto fromResponse(RagRetrievalResponse response) {
        List<RagRetrievalMatchDto> matchDtos = response.matches() != null
                ? response.matches().stream().map(RagRetrievalMatchDto::fromMatch).toList()
                : List.of();
        return new RagRetrievalResponseDto(response.requestId(), response.outcome(), matchDtos);
    }
}
