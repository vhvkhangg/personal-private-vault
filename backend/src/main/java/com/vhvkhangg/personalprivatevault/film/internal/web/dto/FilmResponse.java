package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;

import java.util.Set;

public record FilmResponse(
        Long id,
        String title,
        String originalTitle,
        String nationalityCode,
        String posterUrl,
        FilmFormat format,
        FilmProductionStyle productionStyle,
        boolean isNsfw,
        Long directorPersonId,
        String description,
        Integer totalEpisodes,
        ProgressStatus progressStatus,
        ConsumptionStatus consumptionStatus,
        String currentProgressText,
        String review,
        Set<Long> genreIds,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds
) {
}
