package com.vhvkhangg.personalprivatevault.knowledge.note.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.knowledge.note.internal.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByImportedFileHash(String importedFileHash);

    boolean existsByImportedFileHash(String importedFileHash);
}
