package com.vhvkhangg.personalprivatevault.knowledge.study.view;

import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable view of a study item.
 */
public record StudyItemView(
        Long id,
        String title,
        String posterUrl,
        StudyType type,
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
        StudyStatus learningStatus,
        BigDecimal progressPercent,
        String currentProgressText
) {}
