package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaClaimSupport;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Citation support referencing verified excerpt text")
public record RagQaClaimSupportDto(
        @Schema(description = "Citation key referencing evidence item", example = "[1]")
        String citationKey,

        @Schema(description = "Exact verbatim excerpt quote", example = "Nhân vật An sinh năm 1990 tại Hà Nội.")
        String quote
) {
    public static RagQaClaimSupportDto fromSupport(RagQaClaimSupport support) {
        if (support == null) return null;
        return new RagQaClaimSupportDto(support.citationKey(), support.quote());
    }

    public RagQaClaimSupport toSupport() {
        return new RagQaClaimSupport(citationKey, quote);
    }

    @Override
    public String toString() {
        return "RagQaClaimSupportDto[citationKey=" + citationKey + ", quote=[REDACTED]]";
    }
}
