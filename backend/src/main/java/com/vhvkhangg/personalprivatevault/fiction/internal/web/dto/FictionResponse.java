package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;

import java.util.Set;

public record FictionResponse(
        Long id,
        String title,
        String originalTitle,
        String nationalityCode,
        String posterUrl,
        FictionFormat format,
        boolean isNsfw,
        Long genreId,
        Long authorPersonId,
        Long authorGroupId,
        String description,
        Integer totalChapters,
        ProgressStatus progressStatus,
        ConsumptionStatus consumptionStatus,
        String currentProgressText,
        String review,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds
) {
}
