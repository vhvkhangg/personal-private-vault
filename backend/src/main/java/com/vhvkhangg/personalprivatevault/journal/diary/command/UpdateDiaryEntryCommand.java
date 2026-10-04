package com.vhvkhangg.personalprivatevault.journal.diary.command;

import java.time.LocalDate;

/**
 * Command for updating an existing diary entry.
 */
public record UpdateDiaryEntryCommand(
        Long id,
        LocalDate entryDate,
        String title,
        String contentMarkdown
) {
}
