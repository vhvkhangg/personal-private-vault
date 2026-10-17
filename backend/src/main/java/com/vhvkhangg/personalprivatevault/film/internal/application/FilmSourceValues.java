package com.vhvkhangg.personalprivatevault.film.internal.application;

import com.vhvkhangg.personalprivatevault.film.enums.*;

/** Fresh owner-local scalar query result; never a managed persistence entity. */
public record FilmSourceValues(Long id,String title,String originalTitle,FilmFormat format,String nationalityCode,
        FilmProductionStyle productionStyle, ProgressStatus progressStatus,ConsumptionStatus consumptionStatus,
        Integer totalEpisodes,String description,boolean isNsfw) {}
