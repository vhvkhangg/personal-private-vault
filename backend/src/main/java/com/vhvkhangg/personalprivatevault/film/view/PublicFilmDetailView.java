package com.vhvkhangg.personalprivatevault.film.view;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;

import java.util.Set;

/**
 * Public immutable view of an eligible film work with full details and resolved director name (PROFILE_PUBLIC view).
 */
public record PublicFilmDetailView(
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
        Set<Long> worldSettingIds,
        String directorName
) {
    public PublicFilmDetailView {
        genreIds = genreIds != null ? Set.copyOf(genreIds) : Set.of();
        storyArchetypeIds = storyArchetypeIds != null ? Set.copyOf(storyArchetypeIds) : Set.of();
        worldSettingIds = worldSettingIds != null ? Set.copyOf(worldSettingIds) : Set.of();
    }

    public PublicFilmDetailView(FilmView view, String directorName) {
        this(
                view.id(),
                view.title(),
                view.originalTitle(),
                view.nationalityCode(),
                view.posterUrl(),
                view.format(),
                view.productionStyle(),
                view.isNsfw(),
                view.directorPersonId(),
                view.description(),
                view.totalEpisodes(),
                view.progressStatus(),
                view.consumptionStatus(),
                view.currentProgressText(),
                view.review(),
                view.genreIds(),
                view.storyArchetypeIds(),
                view.worldSettingIds(),
                directorName
        );
    }
}
