package com.vhvkhangg.personalprivatevault.journal.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateDiaryEntryRequest(
        @NotNull LocalDate entryDate,
        @Size(max = 255) String title,
        @NotBlank String contentMarkdown
) {}
