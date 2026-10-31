package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaClaim;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Individual grounded claim with verified citation supports")
public record RagQaClaimDto(
        @Schema(description = "Claim identifier", example = "c1")
        String id,

        @Schema(description = "Claim text", example = "Nhân vật An sinh năm 1990 tại Hà Nội.")
        String text,

        @Schema(description = "List of citation supports")
        List<RagQaClaimSupportDto> supports
) {
    public static RagQaClaimDto fromClaim(RagQaClaim claim) {
        if (claim == null) return null;
        List<RagQaClaimSupportDto> supDtos = claim.supports() != null
                ? claim.supports().stream().map(RagQaClaimSupportDto::fromSupport).toList()
                : List.of();
        return new RagQaClaimDto(claim.id(), claim.text(), supDtos);
    }

    public RagQaClaim toClaim() {
        return new RagQaClaim(
                id,
                text,
                supports != null ? supports.stream().map(RagQaClaimSupportDto::toSupport).toList() : List.of()
        );
    }

    @Override
    public String toString() {
        return "RagQaClaimDto[id=" + id + ", text=[REDACTED], supportsCount=" + (supports != null ? supports.size() : 0) + "]";
    }
}
