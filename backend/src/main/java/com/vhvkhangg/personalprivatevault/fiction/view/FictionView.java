package com.vhvkhangg.personalprivatevault.fiction.view;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;

import java.util.Set;

/**
 * Public immutable view of a fiction work.
 *
 * @param id fiction ID (identical to underlying vault entry ID)
 * @param title primary title
 * @param originalTitle optional original/native title
 * @param nationalityCode optional 2-character country code
 * @param posterUrl optional cover or poster image URL
 * @param format format of the work (NOVEL or COMIC)
 * @param isNsfw whether the work is marked NSFW
 * @param genreId ID of the fiction-owned genre
 * @param authorPersonId ID of the author person (mutually exclusive with authorGroupId)
 * @param authorGroupId ID of the author creator group (mutually exclusive with authorPersonId)
 * @param description optional description / synopsis
 * @param totalChapters optional total chapter count (null or >= 0)
 * @param progressStatus publication progress status
 * @param consumptionStatus personal reading/consumption status
 * @param currentProgressText optional progress description text
 * @param review optional personal review notes
 * @param storyArchetypeIds set of assigned story archetype IDs
 * @param worldSettingIds set of assigned world setting IDs
 */
public record FictionView(
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
    public FictionView {
        storyArchetypeIds = storyArchetypeIds != null ? Set.copyOf(storyArchetypeIds) : Set.of();
        worldSettingIds = worldSettingIds != null ? Set.copyOf(worldSettingIds) : Set.of();
    }
}
