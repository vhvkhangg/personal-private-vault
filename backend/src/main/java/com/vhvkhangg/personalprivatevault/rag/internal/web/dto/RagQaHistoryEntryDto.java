package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaHistoryEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.CodePointLength;

@Schema(description = "Single turn in client-supplied conversation history")
public record RagQaHistoryEntryDto(
        @NotNull
        @Pattern(regexp = "^(user|assistant)$", message = "role must be user or assistant")
        @Schema(description = "Message sender role (user or assistant only)", example = "user")
        String role,

        @NotBlank
        @CodePointLength(max = 1024)
        @Schema(description = "Message text content", example = "Tài liệu nói gì về nhân vật An?")
        String content
) {
    public static RagQaHistoryEntryDto fromEntry(RagQaHistoryEntry entry) {
        if (entry == null) return null;
        return new RagQaHistoryEntryDto(entry.role(), entry.content());
    }

    public RagQaHistoryEntry toEntry() {
        return new RagQaHistoryEntry(role, content);
    }

    @Override
    public String toString() {
        return "RagQaHistoryEntryDto[role=" + role + ", content=[REDACTED]]";
    }
}
