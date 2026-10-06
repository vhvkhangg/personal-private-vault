package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicVersion;

public record MusicResponse(
        Long id,
        String title,
        CollectionMusicVersion version,
        Long platformId,
        String url
) {}
