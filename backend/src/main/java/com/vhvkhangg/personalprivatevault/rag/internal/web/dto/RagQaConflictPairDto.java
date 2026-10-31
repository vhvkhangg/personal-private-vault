package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaConflictPair;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pair of conflicting claims across permitted sources")
public record RagQaConflictPairDto(
        @Schema(description = "First conflicting claim ID", example = "c1")
        String claimIdA,

        @Schema(description = "Second conflicting claim ID", example = "c2")
        String claimIdB,

        @Schema(description = "Conflict description", example = "Nguồn tài liệu ghi nhận khác nhau về năm sinh")
        String description
) {
    public static RagQaConflictPairDto fromConflictPair(RagQaConflictPair pair) {
        if (pair == null) return null;
        return new RagQaConflictPairDto(pair.claimIdA(), pair.claimIdB(), pair.description());
    }

    public RagQaConflictPair toConflictPair() {
        return new RagQaConflictPair(claimIdA, claimIdB, description);
    }

    @Override
    public String toString() {
        return "RagQaConflictPairDto[claimIdA=" + claimIdA + ", claimIdB=" + claimIdB + ", description=[REDACTED]]";
    }
}
