package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditRole;

public record MusicCreditResponse(
        Long musicId,
        Long personId,
        CollectionMusicCreditRole role
) {}
