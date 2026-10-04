package com.vhvkhangg.personalprivatevault.journal.diary.command;

import java.time.LocalDate;

/**
 * Command for creating a diary entry.
 */
public record CreateDiaryEntryCommand(
        LocalDate entryDate,
        String title,
        String contentMarkdown
) {
}
