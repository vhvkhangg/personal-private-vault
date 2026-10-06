package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KnowledgeStudyResponse(
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
) {
}
