package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import java.util.Set;

public record FictionClassificationsResponse(
        Long fictionId,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds
) {
}
