package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable public view of a study item exposed by the parent Knowledge API.
 */
public record KnowledgeStudyItemView(
        Long id,
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
