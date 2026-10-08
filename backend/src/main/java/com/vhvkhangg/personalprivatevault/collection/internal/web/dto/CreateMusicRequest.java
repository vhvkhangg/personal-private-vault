package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicVersion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMusicRequest(
        @NotBlank @Size(max = 500) String title,
        CollectionMusicVersion version,
        Long platformId,
        @Size(max = 2048) String url
) {}
