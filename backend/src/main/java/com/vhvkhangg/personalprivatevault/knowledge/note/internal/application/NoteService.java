package com.vhvkhangg.personalprivatevault.knowledge.note.internal.application;

import com.vhvkhangg.personalprivatevault.knowledge.note.internal.domain.Note;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot;
import com.vhvkhangg.personalprivatevault.knowledge.note.internal.infrastructure.persistence.NoteRepository;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.CreateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.InvalidNoteException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteOperations;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.UpdateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.view.NoteView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService implements NoteOperations {

    private final NoteRepository noteRepository;
    private final VaultEntryOperations vaultEntryOperations;

    @Override
    @Transactional
    public NoteView create(CreateNoteCommand command) {
        if (command == null) {
            throw new InvalidNoteException("CreateNoteCommand must not be null");
        }

        ValidatedNote validated = validateNote(
                command.title(),
                command.contentMarkdown(),
                command.summary(),
                command.sourceName(),
                command.sourceUrl(),
                command.importedFileName(),
                command.importedFileHash(),
                command.frontmatter(),
                null
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.NOTE);

        Note note = new Note(
                vaultEntry.id(),
                validated.title(),
                validated.contentMarkdown(),
                validated.summary(),
                validated.sourceName(),
                validated.sourceUrl(),
                validated.importedFileName(),
                validated.importedFileHash(),
                validated.frontmatter(),
                true
        );

        try {
            noteRepository.saveAndFlush(note);
        } catch (DataIntegrityViolationException ex) {
            String constraint = extractConstraintName(ex);
            if (constraint.contains("imported_file_hash")) {
                throw new NoteConflictException("A note with the specified imported file hash already exists");
            }
            throw new NoteConflictException("Note conflict occurred during creation");
        }

        return toView(note);
    }

    @Override
    @Transactional
    public NoteView update(Long id, UpdateNoteCommand command) {
        if (id == null) {
            throw new InvalidNoteException("Note ID must not be null");
        }
        if (command == null) {
            throw new InvalidNoteException("UpdateNoteCommand must not be null");
        }

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        ValidatedNote validated = validateNote(
                command.title(),
                command.contentMarkdown(),
                command.summary(),
                command.sourceName(),
                command.sourceUrl(),
                command.importedFileName(),
                command.importedFileHash(),
                command.frontmatter(),
                id
        );

        note.update(
                validated.title(),
                validated.contentMarkdown(),
                validated.summary(),
                validated.sourceName(),
                validated.sourceUrl(),
                validated.importedFileName(),
                validated.importedFileHash(),
                validated.frontmatter()
        );

        try {
            noteRepository.saveAndFlush(note);
        } catch (DataIntegrityViolationException ex) {
            String constraint = extractConstraintName(ex);
            if (constraint.contains("imported_file_hash")) {
                throw new NoteConflictException("A note with the specified imported file hash already exists");
            }
            throw new NoteConflictException("Note conflict occurred during update");
        }

        return toView(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NoteView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return noteRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NoteView> findByImportedFileHash(String importedFileHash) {
        if (importedFileHash == null || importedFileHash.isBlank()) {
            return Optional.empty();
        }
        return noteRepository.findByImportedFileHash(importedFileHash.trim()).map(this::toView);
    }

    private ValidatedNote validateNote(
            String rawTitle,
            String contentMarkdown,
            String rawSummary,
            String rawSourceName,
            String rawSourceUrl,
            String rawImportedFileName,
            String rawImportedFileHash,
            Map<String, Object> frontmatter,
            Long currentId
    ) {
        if (rawTitle == null || rawTitle.isBlank()) {
            throw new InvalidNoteException("Note title must not be blank");
        }
        String title = rawTitle.trim();
        if (title.length() > 500) {
            throw new InvalidNoteException("Note title must not exceed 500 characters");
        }

        if (contentMarkdown == null) {
            throw new InvalidNoteException("Content markdown must not be null");
        }

        String sourceName = trimOrNull(rawSourceName);
        if (sourceName != null && sourceName.length() > 500) {
            throw new InvalidNoteException("Source name must not exceed 500 characters");
        }

        String sourceUrl = trimOrNull(rawSourceUrl);
        if (sourceUrl != null && sourceUrl.length() > 2048) {
            throw new InvalidNoteException("Source URL must not exceed 2048 characters");
        }

        String importedFileName = trimOrNull(rawImportedFileName);
        if (importedFileName != null && importedFileName.length() > 500) {
            throw new InvalidNoteException("Imported file name must not exceed 500 characters");
        }

        String importedFileHash = trimOrNull(rawImportedFileHash);
        if (importedFileHash != null) {
            if (importedFileHash.length() > 64) {
                throw new InvalidNoteException("Imported file hash must not exceed 64 characters");
            }
            Optional<Note> duplicate = noteRepository.findByImportedFileHash(importedFileHash);
            if (duplicate.isPresent() && (currentId == null || !duplicate.get().getId().equals(currentId))) {
                throw new NoteConflictException("A note with the specified imported file hash already exists");
            }
        }

        Map<String, Object> isolatedFrontmatter = NoteFrontmatterSnapshot.deepCopy(frontmatter);

        return new ValidatedNote(
                title,
                contentMarkdown,
                trimOrNull(rawSummary),
                sourceName,
                sourceUrl,
                importedFileName,
                importedFileHash,
                isolatedFrontmatter
        );
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return cve.getConstraintName() != null ? cve.getConstraintName().toLowerCase(Locale.ROOT) : "";
            }
            cause = cause.getCause();
        }
        return "";
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private NoteView toView(Note note) {
        return new NoteView(
                note.getId(),
                note.getTitle(),
                note.getContentMarkdown(),
                note.getSummary(),
                note.getSourceName(),
                note.getSourceUrl(),
                note.getImportedFileName(),
                note.getImportedFileHash(),
                note.getFrontmatter()
        );
    }

    private record ValidatedNote(
            String title,
            String contentMarkdown,
            String summary,
            String sourceName,
            String sourceUrl,
            String importedFileName,
            String importedFileHash,
            Map<String, Object> frontmatter
    ) {}
}
