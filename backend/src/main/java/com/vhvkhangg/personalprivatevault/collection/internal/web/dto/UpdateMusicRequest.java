package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicVersion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Full replacement update for a music item. Nullable fields omitted or null will be cleared; version defaults to ORIGINAL if null.")
public record UpdateMusicRequest(
        @NotBlank @Size(max = 500) String title,
        @Schema(description = "Music version: ORIGINAL, COVER, PARODY, or MIX. Defaults to ORIGINAL if null.")
        CollectionMusicVersion version,
        Long platformId,
        @Size(max = 2048) String url
) {}
