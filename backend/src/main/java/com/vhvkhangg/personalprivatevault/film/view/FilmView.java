package com.vhvkhangg.personalprivatevault.film.view;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;

import java.util.Set;

/**
 * Public immutable view of a film work.
 *
 * @param id film ID (identical to underlying vault entry ID with type FILM)
 * @param title primary title
 * @param originalTitle optional original/native title
 * @param nationalityCode optional 2-character country code
 * @param posterUrl optional cover or poster image URL
 * @param format format of the work (MOVIE or SERIES)
 * @param productionStyle production style (LIVE_ACTION or ANIMATION)
 * @param isNsfw whether the work is marked NSFW
 * @param directorPersonId optional ID of the director person
 * @param description optional description / synopsis
 * @param totalEpisodes optional total episode count (null or >= 0)
 * @param progressStatus publication/release progress status
 * @param consumptionStatus personal viewing/consumption status
 * @param currentProgressText optional progress description text
 * @param review optional personal review notes
 * @param genreIds set of assigned film genre IDs
 * @param storyArchetypeIds set of assigned story archetype IDs
 * @param worldSettingIds set of assigned world setting IDs
 */
public record FilmView(
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
    public FilmView {
        genreIds = genreIds != null ? Set.copyOf(genreIds) : Set.of();
        storyArchetypeIds = storyArchetypeIds != null ? Set.copyOf(storyArchetypeIds) : Set.of();
        worldSettingIds = worldSettingIds != null ? Set.copyOf(worldSettingIds) : Set.of();
    }
}
