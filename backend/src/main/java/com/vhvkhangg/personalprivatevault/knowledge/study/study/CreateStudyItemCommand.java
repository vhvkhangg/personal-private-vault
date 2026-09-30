package com.vhvkhangg.personalprivatevault.knowledge.study.study;

import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for creating a study item.
 */
public record CreateStudyItemCommand(
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
