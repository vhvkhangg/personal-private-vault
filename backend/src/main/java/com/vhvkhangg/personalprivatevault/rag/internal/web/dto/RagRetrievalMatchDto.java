package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagLocator;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalMatch;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Individual verified citation match")
public record RagRetrievalMatchDto(
        @Schema(description = "Response-local citation key", example = "[1]")
        String citationKey,

        @Schema(description = "Exact verified text excerpt from snapshot", example = "Architecture documentation...")
        String excerpt,

        @Schema(description = "Contributing numeric RAG source ID", example = "41")
        Long sourceId,

        @Schema(description = "External file source UUID", example = "27f9879f-6b50-49ad-a713-b7527cc485a3")
        UUID fileSourceId,

        @Schema(description = "Selected revision UUID", example = "9b642bf1-9ce4-4d8e-ba34-c7ad1de7f950")
        UUID selectedRevisionId,

        @Schema(description = "Public generation UUID", example = "4ea4f483-356b-4e69-9523-a55e4bfa0ebc")
        UUID generationId,

        @Schema(description = "Approved display name or relative path", example = "notes/architecture.md")
        String displayName,

        @Schema(description = "Exact span and line locator")
        RagLocator locator,

        @Schema(description = "Ideaverse group classification label if applicable", example = "IDEAVERSE_CORE")
        String group,

        @Schema(description = "Freshness basis indicator", example = "RAG_SNAPSHOT")
        String freshnessBasis,

        @Schema(description = "Snapshot index state", example = "SNAPSHOT_CURRENT")
        String snapshotIndexState,

        @Schema(description = "Original filesystem freshness check status", example = "UNVERIFIED")
        String originalFreshness
) {
    public static RagRetrievalMatchDto fromMatch(RagRetrievalMatch match) {
        return new RagRetrievalMatchDto(
                match.citationKey(),
                match.excerpt(),
                match.sourceId(),
                match.fileSourceId(),
                match.selectedRevisionId(),
                match.generationId(),
                match.displayName(),
                match.locator(),
                match.group(),
                match.freshnessBasis(),
                match.snapshotIndexState(),
                match.originalFreshness()
        );
    }
}
