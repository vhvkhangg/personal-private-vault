package com.vhvkhangg.personalprivatevault.knowledge.api;

/**
 * Command for creating an information item via the parent Knowledge API.
 */
public record CreateKnowledgeInformationItemCommand(
        String title,
        KnowledgeInformationType type,
        String description,
        String contentMarkdown,
        String example,
        String sourceName,
        String sourceUrl
) {}
