package com.vhvkhangg.personalprivatevault.vault.internal.web.dto;

import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;
import jakarta.validation.constraints.NotNull;

public record SetRatingRequest(
        @NotNull(message = "Rating grade must not be null")
        RatingGrade grade
) {
}
