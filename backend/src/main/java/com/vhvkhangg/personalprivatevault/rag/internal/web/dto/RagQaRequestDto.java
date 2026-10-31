package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagQaHistoryEntry;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalMode;
import com.vhvkhangg.personalprivatevault.rag.api.RagSourceSelectionEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.CodePointLength;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to answer question grounded strictly in permitted Markdown evidence")
public record RagQaRequestDto(
        @NotNull
        @Schema(description = "Caller-generated opaque request UUID for single-reader identity and idempotent cancel", example = "13f7f818-0cd0-4a74-b523-2b4b8b6d5bbf")
        UUID requestId,

        @NotBlank
        @CodePointLength(max = 4096)
        @Schema(description = "Natural language question", example = "Nó có thuộc tính nào đã được ghi rõ?")
        String question,

        @Schema(description = "Retrieval mode", example = "GENERIC", defaultValue = "GENERIC")
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
        @Max(50)
        @Schema(description = "Maximum evidence candidates to consider (1-50)", example = "5", defaultValue = "5")
        Integer topK,

        @Pattern(regexp = "^(vi|en|zh|zh-Hans|zh-Hant)$", message = "responseLanguage must be vi, en, zh, zh-Hans, or zh-Hant")
        @Schema(description = "Optional response language code (vi, en, zh, zh-Hans, zh-Hant)", example = "vi")
        String responseLanguage,

        @Schema(description = "Bounded recent conversation turns (max 4 turns, max 1024 cp each, max 3072 cp total)")
        List<@NotNull @Valid RagQaHistoryEntryDto> history
) {
    public RagQaRequestDto {
        if (mode == null) {
            mode = RagRetrievalMode.GENERIC;
        }
        if (topK == null) {
            topK = 5;
        }
        if (history != null) {
            if (history.size() > 4) {
                throw new IllegalArgumentException("History exceeds maximum allowed turns (4)");
            }
            int totalCp = 0;
            for (RagQaHistoryEntryDto entry : history) {
                if (entry == null) {
                    throw new IllegalArgumentException("History entry must not be null");
                }
                String role = entry.role();
                if (!"user".equalsIgnoreCase(role) && !"assistant".equalsIgnoreCase(role)) {
                    throw new IllegalArgumentException("History role must be user or assistant");
                }
                String content = entry.content();
                if (content != null) {
                    int cp = content.codePointCount(0, content.length());
                    if (cp > 1024) {
                        throw new IllegalArgumentException("History entry exceeds maximum allowed code points (1024)");
                    }
                    totalCp += cp;
                }
            }
            if (totalCp > 3072) {
                throw new IllegalArgumentException("History total content exceeds maximum allowed code points (3072)");
            }
        }
    }

    public RagQaCommand toCommand() {
        List<RagSourceSelectionEntry> selEntries = selection != null
                ? selection.stream().map(RagSourceSelectionEntryDto::toEntry).toList()
                : null;
        List<RagQaHistoryEntry> histEntries = history != null
                ? history.stream().map(RagQaHistoryEntryDto::toEntry).toList()
                : List.of();
        return new RagQaCommand(
                requestId,
                question,
                mode,
                selEntries,
                rosterVersion,
                collectionId,
                topK != null ? topK : 5,
                responseLanguage,
                histEntries
        );
    }

    @Override
    public String toString() {
        return "RagQaRequestDto[requestId=" + requestId
                + ", question=[REDACTED]"
                + ", mode=" + mode
                + ", selection=" + selection
                + ", rosterVersion=" + rosterVersion
                + ", collectionId=" + collectionId
                + ", topK=" + topK
                + ", responseLanguage=" + responseLanguage
                + ", historyCount=" + (history != null ? history.size() : 0) + "]";
    }
}
