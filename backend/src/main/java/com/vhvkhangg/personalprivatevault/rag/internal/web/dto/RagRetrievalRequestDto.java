package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalMode;
import com.vhvkhangg.personalprivatevault.rag.api.RagSourceSelectionEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.CodePointLength;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to retrieve authorized Markdown evidence")
public record RagRetrievalRequestDto(
        @NotNull
        @Schema(description = "Caller-generated opaque request UUID for single-reader identity and idempotent cancel", example = "13f7f818-0cd0-4a74-b523-2b4b8b6d5bbf")
        UUID requestId,

        @NotBlank
        @CodePointLength(max = 4096)
        @Schema(description = "Natural language question", example = "Thuộc tính này được mô tả ở đâu?")
        String question,

        @NotNull
        @Schema(description = "Retrieval mode", example = "GENERIC")
        RagRetrievalMode mode,

        @Schema(description = "Explicit selection roster")
        List<@NotNull @Valid RagSourceSelectionEntryDto> selection,

        @CodePointLength(max = 128)
        @Schema(description = "Owner-declared roster version label", example = "v1.0")
        String rosterVersion,

        @CodePointLength(max = 128)
        @Schema(description = "Owner-declared collection label for Ideaverse modes", example = "my-markdown-collection")
        String collectionId,

        @Min(1)
        @Max(10)
        @Schema(description = "Maximum candidates to return (1-10)", example = "5", defaultValue = "5")
        Integer topK
) {
    public RagRetrievalCommand toCommand() {
        List<RagSourceSelectionEntry> selEntries = selection != null
                ? selection.stream().map(RagSourceSelectionEntryDto::toEntry).toList()
                : null;
        return new RagRetrievalCommand(
                requestId,
                question,
                mode,
                selEntries,
                rosterVersion,
                collectionId,
                topK != null ? topK : 5
        );
    }

    @Override
    public String toString() {
        return "RagRetrievalRequestDto[requestId=" + requestId
                + ", question=[REDACTED]"
                + ", mode=" + mode
                + ", selection=" + selection
                + ", rosterVersion=" + rosterVersion
                + ", collectionId=" + collectionId
                + ", topK=" + topK + "]";
    }
}
