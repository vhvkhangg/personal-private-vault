package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFictionRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 500, message = "Title must not exceed 500 characters")
        String title,
        @Size(max = 500, message = "Original title must not exceed 500 characters")
        String originalTitle,
        @Size(max = 2, message = "Nationality code must not exceed 2 characters")
        String nationalityCode,
        @Size(max = 2048, message = "Poster URL must not exceed 2048 characters")
        String posterUrl,
        @NotNull(message = "Format must not be null")
        FictionFormat format,
        Boolean isNsfw,
        @NotNull(message = "Genre ID must not be null")
        Long genreId,
        Long authorPersonId,
        Long authorGroupId,
        String description,
        Integer totalChapters,
        ProgressStatus progressStatus,
        ConsumptionStatus consumptionStatus,
        @Size(max = 255, message = "Current progress text must not exceed 255 characters")
        String currentProgressText,
        String review
) {
}
