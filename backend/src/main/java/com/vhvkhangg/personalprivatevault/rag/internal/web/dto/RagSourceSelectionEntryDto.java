package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagSourceSelectionEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.CodePointLength;

import java.util.UUID;

@Schema(description = "Explicit source selection roster entry")
public record RagSourceSelectionEntryDto(
        @NotNull
        @jakarta.validation.constraints.Positive
        @Schema(description = "Numeric RAG source ID", example = "41")
        Long sourceId,

        @NotNull
        @Schema(description = "Expected selected revision UUID", example = "27f9879f-6b50-49ad-a713-b7527cc485a3")
        UUID selectedRevisionId,

        @CodePointLength(max = 512)
        @Schema(description = "Owner-declared Ideaverse classification group (e.g. IDEAVERSE_CORE)", example = "IDEAVERSE_CORE")
        String group,

        @CodePointLength(max = 512)
        @Schema(description = "Relative display path within collection", example = "notes/architecture.md")
        String relativePath
) {
    public RagSourceSelectionEntry toEntry() {
        return new RagSourceSelectionEntry(sourceId, selectedRevisionId, group, relativePath);
    }
}
