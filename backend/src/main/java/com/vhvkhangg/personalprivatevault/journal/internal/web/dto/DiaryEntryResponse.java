package com.vhvkhangg.personalprivatevault.journal.internal.web.dto;

import java.time.Instant;
import java.time.LocalDate;

public record DiaryEntryResponse(
        Long id,
        LocalDate entryDate,
        String title,
        String contentMarkdown,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {}
