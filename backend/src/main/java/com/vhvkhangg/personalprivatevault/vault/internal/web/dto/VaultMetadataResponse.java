package com.vhvkhangg.personalprivatevault.vault.internal.web.dto;

import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;

import java.util.List;

public record VaultMetadataResponse(
        boolean favorite,
        RatingGrade rating,
        List<TagResponse> tags
) {
}
