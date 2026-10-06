package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditRole;
import jakarta.validation.constraints.NotNull;

public record AddMusicCreditRequest(
        @NotNull Long personId,
        @NotNull CollectionMusicCreditRole role
) {}
