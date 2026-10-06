package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateKnowledgeInformationRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @NotNull(message = "Information type must not be null")
        KnowledgeInformationType type,
        String description,
        String contentMarkdown,
        String example,
        @Size(max = 255, message = "Source name must not exceed 255 characters")
        String sourceName,
        @Size(max = 2048, message = "Source URL must not exceed 2048 characters")
        String sourceUrl
) {
}
