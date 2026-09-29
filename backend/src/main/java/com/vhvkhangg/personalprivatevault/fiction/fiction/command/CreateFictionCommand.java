package com.vhvkhangg.personalprivatevault.fiction.fiction.command;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;

import java.util.Set;

/**
 * Command to create a new fiction work.
 *
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
 * @param storyArchetypeIds optional set of story archetype reference IDs
 * @param worldSettingIds optional set of world setting reference IDs
 */
public record CreateFictionCommand(
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
        String review,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds
) {
    public CreateFictionCommand(
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
            String review) {
        this(
                title,
                originalTitle,
                nationalityCode,
                posterUrl,
                format,
                isNsfw,
                genreId,
                authorPersonId,
                authorGroupId,
                description,
                totalChapters,
                progressStatus,
                consumptionStatus,
                currentProgressText,
                review,
                Set.of(),
                Set.of()
        );
    }
}
