package com.vhvkhangg.personalprivatevault.vault.internal.application.metadata;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.Tag;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Internal transaction-isolated helper for tag insertion and lookup recovery.
 *
 * <p>Isolates tag persistence in a separate transaction so that concurrent unique-index
 * conflicts can roll back cleanly without poisoning the caller's transaction context.</p>
 */
@Component
@RequiredArgsConstructor
class TagCreator {

    private final TagRepository tagRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Tag createInNewTransaction(String name, Instant createdAt) {
        return tagRepository.saveAndFlush(new Tag(name, createdAt));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<Tag> findByNameIgnoreCaseInNewTransaction(String name) {
        return tagRepository.findByNameIgnoreCase(name);
    }
}
