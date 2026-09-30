package com.vhvkhangg.personalprivatevault.knowledge.api;

/**
 * Immutable public view of an information item exposed by the parent Knowledge API.
 */
public record KnowledgeInformationItemView(
        Long id,
        String title,
        KnowledgeInformationType type,
        String description,
        String contentMarkdown,
        String example,
        String sourceName,
        String sourceUrl
) {}
