package com.vhvkhangg.personalprivatevault.knowledge.note.note;

import com.vhvkhangg.personalprivatevault.knowledge.note.view.NoteView;

import java.util.Optional;

/**
 * Public capability-oriented contract for Note operations.
 */
public interface NoteOperations {

    NoteView create(CreateNoteCommand command);

    NoteView update(Long id, UpdateNoteCommand command);

    Optional<NoteView> findById(Long id);

    Optional<NoteView> findByImportedFileHash(String importedFileHash);
}
