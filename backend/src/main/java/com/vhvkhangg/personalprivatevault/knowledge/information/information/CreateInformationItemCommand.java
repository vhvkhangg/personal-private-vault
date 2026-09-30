package com.vhvkhangg.personalprivatevault.knowledge.information.information;

import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;

/**
 * Command for creating an information item.
 */
public record CreateInformationItemCommand(
        String title,
        InformationType type,
        String description,
        String contentMarkdown,
        String example,
        String sourceName,
        String sourceUrl
) {}
