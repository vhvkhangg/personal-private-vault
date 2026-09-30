package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for updating an existing study item via the parent Knowledge API.
 */
public record UpdateKnowledgeStudyItemCommand(
        String title,
        String posterUrl,
        KnowledgeStudyType type,
        String siteDomain,
        Long youtubeChannelAccountId,
        Long authorPersonId,
        Long authorGroupId,
        LocalDate publishedDate,
        BigDecimal priceAmount,
        String currencyCode,
        String description,
        String url,
        String review,
        KnowledgeStudyStatus learningStatus,
        BigDecimal progressPercent,
        String currentProgressText
) {}
