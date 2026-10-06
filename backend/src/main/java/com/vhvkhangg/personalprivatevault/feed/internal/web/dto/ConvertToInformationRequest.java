package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConvertToInformationRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull KnowledgeInformationType type,
        String description,
        String contentMarkdown,
        String example,
        @Size(max = 255) String sourceName,
        @Size(max = 2048) String sourceUrl
) {}
