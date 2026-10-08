package com.vhvkhangg.personalprivatevault.journal.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateDiaryEntryRequest(
        @NotNull LocalDate entryDate,
        @Size(max = 500) String title,
        @NotNull String contentMarkdown
) {}
