package com.vhvkhangg.personalprivatevault.film.film.command;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;

/**
 * Command to update an existing film work.
 *
 * @param id required film ID
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
 */
public record UpdateFilmCommand(
        Long id,
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
}
