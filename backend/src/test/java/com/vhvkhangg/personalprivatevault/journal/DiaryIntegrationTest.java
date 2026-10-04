package com.vhvkhangg.personalprivatevault.journal;

import com.vhvkhangg.personalprivatevault.journal.diary.DiaryOperations;
import com.vhvkhangg.personalprivatevault.journal.diary.command.CreateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.command.UpdateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.exception.DiaryEntryNotFoundException;
import com.vhvkhangg.personalprivatevault.journal.diary.exception.InvalidDiaryEntryException;
import com.vhvkhangg.personalprivatevault.journal.view.DiaryEntryView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiaryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private DiaryOperations diaryOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM diary_entries");
    }

    @Test
    @DisplayName("Creates, updates, and reloads diary entry preserving exact markdown formatting")
    void createUpdateAndReloadPreservingMarkdownExactly() {
        String exactMarkdown = "  # Morning Reflection\n\n- Line 1 with spaces  \n- Line 2\twith tab\n\n```java\nSystem.out.println(\"hello\");\n```\n   ";

        DiaryEntryView created = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                LocalDate.of(2026, 10, 1),
                "My Morning",
                exactMarkdown
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.entryDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(created.title()).isEqualTo("My Morning");
        assertThat(created.contentMarkdown()).isEqualTo(exactMarkdown);
        assertThat(created.createdAt()).isNotNull();
        assertThat(created.updatedAt()).isNotNull();
        assertThat(created.deletedAt()).isNull();

        // Reload by ID
        DiaryEntryView reloaded = diaryOperations.findDiaryEntryById(created.id());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.contentMarkdown()).isEqualTo(exactMarkdown);

        // Update with another exact markdown string
        String updatedMarkdown = "\n\nUpdated Content with leading newline\nand trailing spaces   ";
        DiaryEntryView updated = diaryOperations.updateDiaryEntry(new UpdateDiaryEntryCommand(
                created.id(),
                LocalDate.of(2026, 10, 2),
                "Updated Title",
                updatedMarkdown
        ));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.entryDate()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(updated.title()).isEqualTo("Updated Title");
        assertThat(updated.contentMarkdown()).isEqualTo(updatedMarkdown);

        DiaryEntryView reloadedUpdated = diaryOperations.findDiaryEntryById(created.id());
        assertThat(reloadedUpdated).isNotNull();
        assertThat(reloadedUpdated.contentMarkdown()).isEqualTo(updatedMarkdown);
    }

    @Test
    @DisplayName("Allows multiple diary entries on the same date")
    void allowsMultipleEntriesOnSameDate() {
        LocalDate date = LocalDate.of(2026, 10, 5);

        DiaryEntryView entry1 = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                date, "Entry 1", "Content 1"
        ));
        DiaryEntryView entry2 = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                date, "Entry 2", "Content 2"
        ));
        DiaryEntryView entry3 = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                date, null, "Content 3"
        ));

        assertThat(entry1.id()).isNotEqualTo(entry2.id());
        assertThat(entry2.id()).isNotEqualTo(entry3.id());

        List<DiaryEntryView> found = diaryOperations.findDiaryEntries(date, date, 10);
        assertThat(found).hasSize(3);
        // Order: entry_date DESC, id DESC
        assertThat(found.get(0).id()).isEqualTo(entry3.id());
        assertThat(found.get(1).id()).isEqualTo(entry2.id());
        assertThat(found.get(2).id()).isEqualTo(entry1.id());
    }

    @Test
    @DisplayName("Filters by date range with optional bounds and positive limit")
    void filtersByDateRangeWithBounds() {
        diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(LocalDate.of(2026, 9, 28), "D1", "C1"));
        diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(LocalDate.of(2026, 9, 29), "D2", "C2"));
        diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(LocalDate.of(2026, 9, 30), "D3", "C3"));
        diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(LocalDate.of(2026, 10, 1), "D4", "C4"));

        // All entries with null bounds
        List<DiaryEntryView> all = diaryOperations.findDiaryEntries(null, null, 10);
        assertThat(all).hasSize(4);
        assertThat(all.get(0).entryDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(all.get(3).entryDate()).isEqualTo(LocalDate.of(2026, 9, 28));

        // Limited
        List<DiaryEntryView> limited = diaryOperations.findDiaryEntries(null, null, 2);
        assertThat(limited).hasSize(2);
        assertThat(limited.get(0).entryDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(limited.get(1).entryDate()).isEqualTo(LocalDate.of(2026, 9, 30));

        // Subrange
        List<DiaryEntryView> range = diaryOperations.findDiaryEntries(
                LocalDate.of(2026, 9, 29), LocalDate.of(2026, 9, 30), 10
        );
        assertThat(range).hasSize(2);
        assertThat(range.get(0).entryDate()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(range.get(1).entryDate()).isEqualTo(LocalDate.of(2026, 9, 29));
    }

    @Test
    @DisplayName("Soft delete excludes from default list and findById, restore returns it")
    void softDeleteAndRestoreLifecycle() {
        DiaryEntryView entry = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                LocalDate.of(2026, 10, 1), "T", "C"
        ));

        // Soft delete
        diaryOperations.softDeleteDiaryEntry(entry.id());

        // findById throws DiaryEntryNotFoundException
        assertThatThrownBy(() -> diaryOperations.findDiaryEntryById(entry.id()))
                .isInstanceOf(DiaryEntryNotFoundException.class);

        // findDiaryEntries returns empty
        assertThat(diaryOperations.findDiaryEntries(null, null, 10)).isEmpty();

        // Idempotent soft delete
        diaryOperations.softDeleteDiaryEntry(entry.id());

        // Restore
        diaryOperations.restoreDiaryEntry(entry.id());

        // Now accessible again
        DiaryEntryView restored = diaryOperations.findDiaryEntryById(entry.id());
        assertThat(restored).isNotNull();
        assertThat(restored.deletedAt()).isNull();

        assertThat(diaryOperations.findDiaryEntries(null, null, 10)).hasSize(1);

        // Idempotent restore
        diaryOperations.restoreDiaryEntry(entry.id());
    }

    @Test
    @DisplayName("Validates input constraints: entry date, content, title length, limit, range bounds")
    void validationConstraints() {
        // Missing entryDate
        assertThatThrownBy(() -> diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(null, "T", "C")))
                .isInstanceOf(InvalidDiaryEntryException.class)
                .hasMessageContaining("entryDate must not be null");

        // Null content
        assertThatThrownBy(() -> diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(LocalDate.now(), "T", null)))
                .isInstanceOf(InvalidDiaryEntryException.class)
                .hasMessageContaining("contentMarkdown must not be null");

        // Title too long
        String longTitle = "a".repeat(501);
        assertThatThrownBy(() -> diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(LocalDate.now(), longTitle, "C")))
                .isInstanceOf(InvalidDiaryEntryException.class)
                .hasMessageContaining("title must not exceed 500 characters");

        // Non-positive limit
        assertThatThrownBy(() -> diaryOperations.findDiaryEntries(null, null, 0))
                .isInstanceOf(InvalidDiaryEntryException.class)
                .hasMessageContaining("Limit must be positive");

        assertThatThrownBy(() -> diaryOperations.findDiaryEntries(null, null, -5))
                .isInstanceOf(InvalidDiaryEntryException.class)
                .hasMessageContaining("Limit must be positive");

        // Invalid date range fromDate > toDate
        assertThatThrownBy(() -> diaryOperations.findDiaryEntries(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1), 10))
                .isInstanceOf(InvalidDiaryEntryException.class)
                .hasMessageContaining("fromDate cannot be after toDate");

        // Not found update
        assertThatThrownBy(() -> diaryOperations.updateDiaryEntry(new UpdateDiaryEntryCommand(999999L, LocalDate.now(), "T", "C")))
                .isInstanceOf(DiaryEntryNotFoundException.class);

        // Not found soft delete
        assertThatThrownBy(() -> diaryOperations.softDeleteDiaryEntry(999999L))
                .isInstanceOf(DiaryEntryNotFoundException.class);

        // Not found restore
        assertThatThrownBy(() -> diaryOperations.restoreDiaryEntry(999999L))
                .isInstanceOf(DiaryEntryNotFoundException.class);
    }
}
