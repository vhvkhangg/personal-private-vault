package com.vhvkhangg.personalprivatevault.knowledge.api;

/**
 * Command for updating an existing information item via the parent Knowledge API.
 */
public record UpdateKnowledgeInformationItemCommand(
        String title,
        KnowledgeInformationType type,
        String description,
        String contentMarkdown,
        String example,
        String sourceName,
        String sourceUrl
) {}
