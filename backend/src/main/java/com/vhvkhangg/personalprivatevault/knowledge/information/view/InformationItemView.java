package com.vhvkhangg.personalprivatevault.knowledge.information.view;

import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;

/**
 * Immutable view of an information item.
 */
public record InformationItemView(
        Long id,
        String title,
        InformationType type,
        String description,
        String contentMarkdown,
        String example,
        String sourceName,
        String sourceUrl
) {}
