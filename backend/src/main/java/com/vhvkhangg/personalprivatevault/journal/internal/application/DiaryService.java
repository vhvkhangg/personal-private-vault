package com.vhvkhangg.personalprivatevault.journal.internal.application;

import com.vhvkhangg.personalprivatevault.journal.diary.DiaryOperations;
import com.vhvkhangg.personalprivatevault.journal.diary.command.CreateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.command.UpdateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.exception.DiaryEntryNotFoundException;
import com.vhvkhangg.personalprivatevault.journal.diary.exception.InvalidDiaryEntryException;
import com.vhvkhangg.personalprivatevault.journal.internal.domain.DiaryEntry;
import com.vhvkhangg.personalprivatevault.journal.internal.infrastructure.persistence.DiaryEntryRepository;
import com.vhvkhangg.personalprivatevault.journal.view.DiaryEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Application service implementing {@link DiaryOperations}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService implements DiaryOperations {

    private final DiaryEntryRepository diaryEntryRepository;

    @Override
    @Transactional
    public DiaryEntryView createDiaryEntry(CreateDiaryEntryCommand command) {
        if (command == null) {
            throw new InvalidDiaryEntryException("Command must not be null");
        }
        validateEntry(command.entryDate(), command.title(), command.contentMarkdown());
        String normalizedTitle = normalizeTitle(command.title());

        DiaryEntry entry = new DiaryEntry(command.entryDate(), normalizedTitle, command.contentMarkdown());
        DiaryEntry saved = diaryEntryRepository.save(entry);
        return toView(saved);
    }

    @Override
    @Transactional
    public DiaryEntryView updateDiaryEntry(UpdateDiaryEntryCommand command) {
        if (command == null) {
            throw new InvalidDiaryEntryException("Command must not be null");
        }
        if (command.id() == null) {
            throw new InvalidDiaryEntryException("Diary entry id must not be null");
        }
        validateEntry(command.entryDate(), command.title(), command.contentMarkdown());

        DiaryEntry entry = diaryEntryRepository.findByIdAndDeletedAtIsNull(command.id())
                .orElseThrow(() -> new DiaryEntryNotFoundException(command.id()));

        String normalizedTitle = normalizeTitle(command.title());
        entry.update(command.entryDate(), normalizedTitle, command.contentMarkdown());
        return toView(entry);
    }

    @Override
    public DiaryEntryView findDiaryEntryById(Long id) {
        if (id == null) {
            throw new InvalidDiaryEntryException("Diary entry id must not be null");
        }
        DiaryEntry entry = diaryEntryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DiaryEntryNotFoundException(id));
        return toView(entry);
    }

    @Override
    public List<DiaryEntryView> findDiaryEntries(LocalDate fromDate, LocalDate toDate, int limit) {
        if (limit <= 0) {
            throw new InvalidDiaryEntryException("Limit must be positive");
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new InvalidDiaryEntryException("fromDate cannot be after toDate");
        }
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<DiaryEntry> entries;
        if (fromDate != null && toDate != null) {
            entries = diaryEntryRepository.findByDeletedAtIsNullAndEntryDateBetweenOrderByEntryDateDescIdDesc(
                    fromDate, toDate, pageRequest);
        } else if (fromDate != null) {
            entries = diaryEntryRepository.findByDeletedAtIsNullAndEntryDateGreaterThanEqualOrderByEntryDateDescIdDesc(
                    fromDate, pageRequest);
        } else if (toDate != null) {
            entries = diaryEntryRepository.findByDeletedAtIsNullAndEntryDateLessThanEqualOrderByEntryDateDescIdDesc(
                    toDate, pageRequest);
        } else {
            entries = diaryEntryRepository.findByDeletedAtIsNullOrderByEntryDateDescIdDesc(pageRequest);
        }
        return entries.stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional
    public DiaryEntryView softDeleteDiaryEntry(Long id) {
        if (id == null) {
            throw new InvalidDiaryEntryException("Diary entry id must not be null");
        }
        DiaryEntry entry = diaryEntryRepository.findById(id)
                .orElseThrow(() -> new DiaryEntryNotFoundException(id));
        entry.softDelete();
        return toView(entry);
    }

    @Override
    @Transactional
    public DiaryEntryView restoreDiaryEntry(Long id) {
        if (id == null) {
            throw new InvalidDiaryEntryException("Diary entry id must not be null");
        }
        DiaryEntry entry = diaryEntryRepository.findById(id)
                .orElseThrow(() -> new DiaryEntryNotFoundException(id));
        entry.restore();
        return toView(entry);
    }

    private void validateEntry(LocalDate entryDate, String title, String contentMarkdown) {
        if (entryDate == null) {
            throw new InvalidDiaryEntryException("entryDate must not be null");
        }
        if (contentMarkdown == null) {
            throw new InvalidDiaryEntryException("contentMarkdown must not be null");
        }
        if (title != null && title.length() > 500) {
            throw new InvalidDiaryEntryException("title must not exceed 500 characters");
        }
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title;
    }

    private DiaryEntryView toView(DiaryEntry entry) {
        return new DiaryEntryView(
                entry.getId(),
                entry.getEntryDate(),
                entry.getTitle(),
                entry.getContentMarkdown(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                entry.getDeletedAt()
        );
    }
}
