package com.vhvkhangg.personalprivatevault.fiction.view;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;

import java.util.Set;

/**
 * Public immutable view of an eligible fiction work with full details and resolved author name (PROFILE_PUBLIC view).
 */
public record PublicFictionDetailView(
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
        Set<Long> worldSettingIds,
        String authorName
) {
    public PublicFictionDetailView {
        storyArchetypeIds = storyArchetypeIds != null ? Set.copyOf(storyArchetypeIds) : Set.of();
        worldSettingIds = worldSettingIds != null ? Set.copyOf(worldSettingIds) : Set.of();
    }

    public PublicFictionDetailView(FictionView view, String authorName) {
        this(
                view.id(),
                view.title(),
                view.originalTitle(),
                view.nationalityCode(),
                view.posterUrl(),
                view.format(),
                view.isNsfw(),
                view.genreId(),
                view.authorPersonId(),
                view.authorGroupId(),
                view.description(),
                view.totalChapters(),
                view.progressStatus(),
                view.consumptionStatus(),
                view.currentProgressText(),
                view.review(),
                view.storyArchetypeIds(),
                view.worldSettingIds(),
                authorName
        );
    }
}
