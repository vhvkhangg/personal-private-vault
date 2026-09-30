package com.vhvkhangg.personalprivatevault.knowledge.information.information;

import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;

/**
 * Command for updating an existing information item.
 */
public record UpdateInformationItemCommand(
        String title,
        InformationType type,
        String description,
        String contentMarkdown,
        String example,
        String sourceName,
        String sourceUrl
) {}
