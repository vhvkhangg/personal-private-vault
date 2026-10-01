package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResource;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.Sha256Util;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.SavedResourceRepository;
import com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateFeedSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.resource.exception.InvalidSavedResourceException;
import com.vhvkhangg.personalprivatevault.feed.resource.exception.SavedResourceConflictException;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementing Vault-backed saved resource persistence and queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SavedResourceService implements SavedResourceOperations {

    private final SavedResourceRepository savedResourceRepository;
    private final FeedItemRepository feedItemRepository;
    private final FeedSourceRepository feedSourceRepository;
    private final VaultEntryOperations vaultEntryOperations;

    @Override
    @Transactional
    public SavedResourceView saveManual(CreateManualSavedResourceCommand command) {
        if (command == null) {
            throw new InvalidSavedResourceException("Command must not be null");
        }
        if (command.kind() == null) {
            throw new InvalidSavedResourceException("Saved resource kind must not be null");
        }
        if (command.title() == null || command.title().isBlank()) {
            throw new InvalidSavedResourceException("Saved resource title must not be blank");
        }
        if (command.title().trim().length() > 1000) {
            throw new InvalidSavedResourceException("Saved resource title must not exceed 1000 characters");
        }
        if (command.resourceUrl() == null || command.resourceUrl().isBlank()) {
            throw new InvalidSavedResourceException("Saved resource URL must not be blank");
        }
        String trimmedUrl = command.resourceUrl().trim();
        if (trimmedUrl.length() > 2048) {
            throw new InvalidSavedResourceException("Saved resource URL must not exceed 2048 characters");
        }
        if (command.author() != null && command.author().trim().length() > 500) {
            throw new InvalidSavedResourceException("Author must not exceed 500 characters");
        }
        if (command.sourceName() != null && command.sourceName().trim().length() > 500) {
            throw new InvalidSavedResourceException("Source name must not exceed 500 characters");
        }
        if (command.sourceUrl() != null && command.sourceUrl().trim().length() > 2048) {
            throw new InvalidSavedResourceException("Source URL must not exceed 2048 characters");
        }
        if (command.externalId() != null && command.externalId().trim().length() > 500) {
            throw new InvalidSavedResourceException("External ID must not exceed 500 characters");
        }

        String urlHash = Sha256Util.computeSha256(trimmedUrl);
        if (savedResourceRepository.findByResourceUrlHash(urlHash).isPresent()) {
            throw new SavedResourceConflictException("A saved resource with this URL already exists");
        }

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.SAVED_RESOURCE);

        try {
            SavedResource resource = new SavedResource(
                    vaultEntry.id(),
                    null,
                    command.kind(),
                    trim(command.sourceName()),
                    trim(command.sourceUrl()),
                    trim(command.externalId()),
                    command.title().trim(),
                    trimmedUrl,
                    urlHash,
                    trim(command.author()),
                    command.summary(),
                    command.publishedAt(),
                    null,
                    command.rawMetadata(),
                    true
            );
            SavedResource saved = savedResourceRepository.saveAndFlush(resource);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate saved resource URL hash conflict");
            throw new SavedResourceConflictException("A saved resource with this URL already exists");
        }
    }

    @Override
    @Transactional
    public SavedResourceView saveFeedItem(CreateFeedSavedResourceCommand command) {
        if (command == null) {
            throw new InvalidSavedResourceException("Command must not be null");
        }
        if (command.feedItemId() == null) {
            throw new InvalidSavedResourceException("feedItemId must not be null");
        }
        if (command.kind() == null) {
            throw new InvalidSavedResourceException("Saved resource kind must not be null");
        }

        FeedItem item = feedItemRepository.findById(command.feedItemId())
                .orElseThrow(() -> new FeedItemNotFoundException(command.feedItemId()));

        if (savedResourceRepository.findByResourceUrlHash(item.getUrlHash()).isPresent()) {
            throw new SavedResourceConflictException("A saved resource with this URL already exists");
        }

        FeedSource source = feedSourceRepository.findById(item.getFeedSourceId()).orElse(null);

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.SAVED_RESOURCE);

        try {
            SavedResource resource = new SavedResource(
                    vaultEntry.id(),
                    item.getId(),
                    command.kind(),
                    source != null ? source.getName() : null,
                    source != null ? source.getSourceUrl() : null,
                    item.getExternalId(),
                    item.getTitle(),
                    item.getUrl(),
                    item.getUrlHash(),
                    item.getAuthor(),
                    item.getSummary(),
                    item.getPublishedAt(),
                    item.getFetchedAt(),
                    item.getRawMetadata(),
                    true
            );
            SavedResource saved = savedResourceRepository.saveAndFlush(resource);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate saved resource URL hash conflict");
            throw new SavedResourceConflictException("A saved resource with this URL already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SavedResourceView> findSavedResourceById(Long id) {
        return savedResourceRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SavedResourceView> findSavedResourceByUrlHash(String resourceUrlHash) {
        if (resourceUrlHash == null || resourceUrlHash.isBlank()) {
            return Optional.empty();
        }
        return savedResourceRepository.findByResourceUrlHash(resourceUrlHash.trim().toLowerCase())
                .map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedResourceView> findRecentSavedResources(int limit) {
        if (limit <= 0) {
            throw new InvalidSavedResourceException("Limit must be positive");
        }
        return savedResourceRepository.findRecent(PageRequest.of(0, limit))
                .stream()
                .map(this::toView)
                .toList();
    }

    private String trim(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private SavedResourceView toView(SavedResource r) {
        return new SavedResourceView(
                r.getId(),
                r.getFeedItemId(),
                r.getKind(),
                r.getSourceName(),
                r.getSourceUrl(),
                r.getExternalId(),
                r.getTitle(),
                r.getResourceUrl(),
                r.getResourceUrlHash(),
                r.getAuthor(),
                r.getSummary(),
                r.getPublishedAt(),
                r.getFetchedAt(),
                r.getSavedAt(),
                r.getRawMetadata()
        );
    }
}
