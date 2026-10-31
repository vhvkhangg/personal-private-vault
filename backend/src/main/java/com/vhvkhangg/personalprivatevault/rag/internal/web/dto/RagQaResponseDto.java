package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaOutcome;
import com.vhvkhangg.personalprivatevault.rag.api.RagQaResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Response containing grounded answer and verified citation provenance")
public record RagQaResponseDto(
        @Schema(description = "Request UUID identifying reader", example = "13f7f818-0cd0-4a74-b523-2b4b8b6d5bbf")
        UUID requestId,

        @Schema(description = "QA outcome", example = "ANSWERED")
        RagQaOutcome outcome,

        @Schema(description = "Rendered grounded answer", example = "Nhân vật An sinh năm 1990 tại Hà Nội [1].")
        String answer,

        @Schema(description = "Individual grounded claims")
        List<RagQaClaimDto> claims,

        @Schema(description = "Parts of the question that could not be answered from evidence")
        List<String> unansweredParts,

        @Schema(description = "Pairs of conflicting claims identified across evidence")
        List<RagQaConflictPairDto> conflictPairs,

        @Schema(description = "Cited verified evidence matches")
        List<RagRetrievalMatchDto> citations
) {
    public static RagQaResponseDto fromResponse(RagQaResponse response) {
        if (response == null) return null;
        List<RagQaClaimDto> claimDtos = response.claims() != null
                ? response.claims().stream().map(RagQaClaimDto::fromClaim).toList()
                : List.of();
        List<RagQaConflictPairDto> conflictDtos = response.conflictPairs() != null
                ? response.conflictPairs().stream().map(RagQaConflictPairDto::fromConflictPair).toList()
                : List.of();
        List<RagRetrievalMatchDto> citationDtos = response.citations() != null
                ? response.citations().stream().map(RagRetrievalMatchDto::fromMatch).toList()
                : List.of();
        return new RagQaResponseDto(
                response.requestId(),
                response.outcome(),
                response.answer(),
                claimDtos,
                response.unansweredParts() != null ? response.unansweredParts() : List.of(),
                conflictDtos,
                citationDtos
        );
    }

    @Override
    public String toString() {
        return "RagQaResponseDto[requestId=" + requestId
                + ", outcome=" + outcome
                + ", answer=[REDACTED]"
                + ", claimsCount=" + (claims != null ? claims.size() : 0)
                + ", unansweredPartsCount=" + (unansweredParts != null ? unansweredParts.size() : 0)
                + ", conflictPairsCount=" + (conflictPairs != null ? conflictPairs.size() : 0)
                + ", citationsCount=" + (citations != null ? citations.size() : 0) + "]";
    }
}
