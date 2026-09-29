package com.vhvkhangg.personalprivatevault.fiction.view;

import java.util.Set;

/**
 * Public immutable view of narrative classifications assigned to a fiction.
 *
 * @param fictionId ID of the parent fiction
 * @param storyArchetypeIds set of assigned story archetype IDs from the reference module
 * @param worldSettingIds set of assigned world setting IDs from the reference module
 */
public record FictionClassificationsView(
        Long fictionId,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds
) {
    public FictionClassificationsView {
        storyArchetypeIds = storyArchetypeIds != null ? Set.copyOf(storyArchetypeIds) : Set.of();
        worldSettingIds = worldSettingIds != null ? Set.copyOf(worldSettingIds) : Set.of();
    }
}
