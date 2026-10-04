package com.vhvkhangg.personalprivatevault.journal.diary;

import com.vhvkhangg.personalprivatevault.journal.diary.command.CreateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.command.UpdateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.view.DiaryEntryView;

import java.time.LocalDate;
import java.util.List;

/**
 * Public capability operations for diary entries.
 */
public interface DiaryOperations {

    /**
     * Creates a new diary entry.
     *
     * @param command creation command
     * @return created entry view
     */
    DiaryEntryView createDiaryEntry(CreateDiaryEntryCommand command);

    /**
     * Fully updates an existing diary entry.
     *
     * @param command update command
     * @return updated entry view
     */
    DiaryEntryView updateDiaryEntry(UpdateDiaryEntryCommand command);

    /**
     * Finds a non-deleted diary entry by ID.
     *
     * @param id entry identifier
     * @return entry view
     */
    DiaryEntryView findDiaryEntryById(Long id);

    /**
     * Finds non-deleted diary entries within an optional date range, ordered by entry_date DESC, id DESC.
     *
     * @param fromDate optional lower bound (inclusive)
     * @param toDate optional upper bound (inclusive)
     * @param limit positive maximum count
     * @return list of entry views
     */
    List<DiaryEntryView> findDiaryEntries(LocalDate fromDate, LocalDate toDate, int limit);

    /**
     * Soft-deletes a diary entry. Idempotent.
     *
     * @param id entry identifier
     * @return entry view
     */
    DiaryEntryView softDeleteDiaryEntry(Long id);

    /**
     * Restores a soft-deleted diary entry. Idempotent.
     *
     * @param id entry identifier
     * @return entry view
     */
    DiaryEntryView restoreDiaryEntry(Long id);
}
