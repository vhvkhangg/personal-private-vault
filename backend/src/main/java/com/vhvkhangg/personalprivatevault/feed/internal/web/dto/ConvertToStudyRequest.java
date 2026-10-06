package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConvertToStudyRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2048) String posterUrl,
        KnowledgeStudyType type,
        @Size(max = 255) String siteDomain,
        Long youtubeChannelAccountId,
        Long authorPersonId,
        Long authorGroupId,
        LocalDate publishedDate,
        @PositiveOrZero BigDecimal priceAmount,
        @Size(min = 3, max = 3) String currencyCode,
        String description,
        @Size(max = 2048) String url,
        String review,
        KnowledgeStudyStatus learningStatus,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal progressPercent,
        @Size(max = 255) String currentProgressText
) {}
