package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateKnowledgeStudyRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @Size(max = 2048, message = "Poster URL must not exceed 2048 characters")
        String posterUrl,
        @NotNull(message = "Study type must not be null")
        KnowledgeStudyType type,
        @Size(max = 255, message = "Site domain must not exceed 255 characters")
        String siteDomain,
        Long youtubeChannelAccountId,
        Long authorPersonId,
        Long authorGroupId,
        LocalDate publishedDate,
        BigDecimal priceAmount,
        @Size(max = 3, message = "Currency code must not exceed 3 characters")
        String currencyCode,
        String description,
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String url,
        String review,
        KnowledgeStudyStatus learningStatus,
        BigDecimal progressPercent,
        @Size(max = 255, message = "Current progress text must not exceed 255 characters")
        String currentProgressText
) {
}
