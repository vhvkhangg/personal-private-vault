package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;

public record KnowledgeInformationResponse(
        Long id,
        String title,
        KnowledgeInformationType type,
        String description,
        String contentMarkdown,
        String example,
        String sourceName,
        String sourceUrl
) {
}
