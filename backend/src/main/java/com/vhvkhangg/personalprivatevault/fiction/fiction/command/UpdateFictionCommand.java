package com.vhvkhangg.personalprivatevault.fiction.fiction.command;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;

/**
 * Command to update an existing fiction work.
 *
 * @param id required fiction ID
 * @param title required fiction title (up to 500 characters)
 * @param originalTitle optional original title (up to 500 characters)
 * @param nationalityCode optional 2-character ISO country code
 * @param posterUrl optional poster/cover image URL (up to 2048 characters)
 * @param format required format (NOVEL or COMIC)
 * @param isNsfw optional NSFW flag (defaults to false if null)
 * @param genreId required fiction-owned genre ID
 * @param authorPersonId author person ID (mutually exclusive with authorGroupId)
 * @param authorGroupId author creator group ID (mutually exclusive with authorPersonId)
 * @param description optional description
 * @param totalChapters optional total chapter count (null or >= 0)
 * @param progressStatus optional progress status (defaults to ONGOING if null)
 * @param consumptionStatus optional consumption status (defaults to UNCONSUMED if null)
 * @param currentProgressText optional progress text (up to 255 characters)
 * @param review optional review text
 */
public record UpdateFictionCommand(
        Long id,
        String title,
        String originalTitle,
        String nationalityCode,
        String posterUrl,
        FictionFormat format,
        Boolean isNsfw,
        Long genreId,
        Long authorPersonId,
        Long authorGroupId,
        String description,
        Integer totalChapters,
        ProgressStatus progressStatus,
        ConsumptionStatus consumptionStatus,
        String currentProgressText,
        String review
) {
}
