package com.vhvkhangg.personalprivatevault.film.film.command;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;

import java.util.Set;

/**
 * Command to create a new film work.
 *
 * @param title required film title (up to 500 characters)
 * @param originalTitle optional original title (up to 500 characters)
 * @param nationalityCode optional 2-character ISO country code
 * @param posterUrl optional poster/cover image URL (up to 2048 characters)
 * @param format required format (MOVIE or SERIES)
 * @param productionStyle required production style (LIVE_ACTION or ANIMATION)
 * @param isNsfw optional NSFW flag (defaults to false if null)
 * @param directorPersonId optional director person ID
 * @param description optional description
 * @param totalEpisodes optional total episode count (null or >= 0)
 * @param progressStatus optional progress status (defaults to ONGOING if null)
 * @param consumptionStatus optional consumption status (defaults to UNCONSUMED if null)
 * @param currentProgressText optional progress text (up to 255 characters)
 * @param review optional review text
 * @param genreIds optional set of film-owned genre IDs
 * @param storyArchetypeIds optional set of story archetype reference IDs
 * @param worldSettingIds optional set of world setting reference IDs
 */
public record CreateFilmCommand(
        String title,
        String originalTitle,
        String nationalityCode,
        String posterUrl,
        FilmFormat format,
        FilmProductionStyle productionStyle,
        Boolean isNsfw,
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
    public CreateFilmCommand(
            String title,
            String originalTitle,
            String nationalityCode,
            String posterUrl,
            FilmFormat format,
            FilmProductionStyle productionStyle,
            Boolean isNsfw,
            Long directorPersonId,
            String description,
            Integer totalEpisodes,
            ProgressStatus progressStatus,
            ConsumptionStatus consumptionStatus,
            String currentProgressText,
            String review
    ) {
        this(
                title,
                originalTitle,
                nationalityCode,
                posterUrl,
                format,
                productionStyle,
                isNsfw,
                directorPersonId,
                description,
                totalEpisodes,
                progressStatus,
                consumptionStatus,
                currentProgressText,
                review,
                Set.of(),
                Set.of(),
                Set.of()
        );
    }
}
