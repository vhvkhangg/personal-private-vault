package com.vhvkhangg.personalprivatevault.journal.view;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Read model representing an immutable diary entry.
 */
public record DiaryEntryView(
        Long id,
        LocalDate entryDate,
        String title,
        String contentMarkdown,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
