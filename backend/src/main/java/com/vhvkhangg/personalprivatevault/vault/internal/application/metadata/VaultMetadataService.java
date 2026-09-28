package com.vhvkhangg.personalprivatevault.vault.internal.application.metadata;

import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;
import com.vhvkhangg.personalprivatevault.vault.view.TagView;
import com.vhvkhangg.personalprivatevault.vault.metadata.VaultMetadataOperations;
import com.vhvkhangg.personalprivatevault.vault.view.VaultMetadataView;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.Favorite;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.Rating;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.Tag;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultCapabilityMatrix;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntry;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntryTag;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntryTagId;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.FavoriteRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.RatingRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.TagRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VaultMetadataService implements VaultMetadataOperations {

    private final VaultEntryRepository vaultEntryRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final TagRepository tagRepository;
    private final VaultEntryTagRepository vaultEntryTagRepository;
    private final TagCreator tagCreator;
    private final Clock clock;

    @Autowired
    public VaultMetadataService(
            VaultEntryRepository vaultEntryRepository,
            FavoriteRepository favoriteRepository,
            RatingRepository ratingRepository,
            TagRepository tagRepository,
            VaultEntryTagRepository vaultEntryTagRepository,
            TagCreator tagCreator,
            @Autowired(required = false) Clock clock) {
        this.vaultEntryRepository = Objects.requireNonNull(vaultEntryRepository, "vaultEntryRepository must not be null");
        this.favoriteRepository = Objects.requireNonNull(favoriteRepository, "favoriteRepository must not be null");
        this.ratingRepository = Objects.requireNonNull(ratingRepository, "ratingRepository must not be null");
        this.tagRepository = Objects.requireNonNull(tagRepository, "tagRepository must not be null");
        this.vaultEntryTagRepository = Objects.requireNonNull(vaultEntryTagRepository, "vaultEntryTagRepository must not be null");
        this.tagCreator = Objects.requireNonNull(tagCreator, "tagCreator must not be null");
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public VaultMetadataService(
            VaultEntryRepository vaultEntryRepository,
            FavoriteRepository favoriteRepository,
            RatingRepository ratingRepository,
            TagRepository tagRepository,
            VaultEntryTagRepository vaultEntryTagRepository,
            TagCreator tagCreator) {
        this(vaultEntryRepository, favoriteRepository, ratingRepository, tagRepository, vaultEntryTagRepository, tagCreator, Clock.systemUTC());
    }

    public VaultMetadataService(
            VaultEntryRepository vaultEntryRepository,
            FavoriteRepository favoriteRepository,
            RatingRepository ratingRepository,
            TagRepository tagRepository,
            VaultEntryTagRepository vaultEntryTagRepository,
            Clock clock) {
        this(vaultEntryRepository, favoriteRepository, ratingRepository, tagRepository, vaultEntryTagRepository, new TagCreator(tagRepository), clock);
    }

    public VaultMetadataService(
            VaultEntryRepository vaultEntryRepository,
            FavoriteRepository favoriteRepository,
            RatingRepository ratingRepository,
            TagRepository tagRepository,
            VaultEntryTagRepository vaultEntryTagRepository) {
        this(vaultEntryRepository, favoriteRepository, ratingRepository, tagRepository, vaultEntryTagRepository, new TagCreator(tagRepository), Clock.systemUTC());
    }

    @Override
    public VaultMetadataView metadata(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        if (!vaultEntryRepository.existsById(vaultEntryId)) {
            throw new NoSuchElementException("Vault entry not found with id: " + vaultEntryId);
        }
        boolean favorite = favoriteRepository.existsById(vaultEntryId);
        RatingGrade rating = ratingRepository.findById(vaultEntryId)
                .map(Rating::getGrade)
                .orElse(null);
        List<TagView> tagViews = vaultEntryTagRepository.findTagsByVaultEntryId(vaultEntryId).stream()
                .map(t -> new TagView(t.getId(), t.getName(), t.getCreatedAt()))
                .toList();
        return new VaultMetadataView(favorite, rating, tagViews);
    }

    @Override
    @Transactional
    public void favorite(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = getEntryOrThrow(vaultEntryId);
        assertNotDeleted(entry);
        VaultCapabilityMatrix.assertCanFavorite(entry.getEntryType());

        if (!favoriteRepository.existsById(vaultEntryId)) {
            Instant now = clock.instant();
            favoriteRepository.save(new Favorite(vaultEntryId, now));
        }
    }

    @Override
    @Transactional
    public void unfavorite(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = getEntryOrThrow(vaultEntryId);
        assertNotDeleted(entry);
        VaultCapabilityMatrix.assertCanFavorite(entry.getEntryType());

        if (favoriteRepository.existsById(vaultEntryId)) {
            favoriteRepository.deleteById(vaultEntryId);
        }
    }

    @Override
    @Transactional
    public void setRating(Long vaultEntryId, RatingGrade grade) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        if (grade == null) {
            throw new IllegalArgumentException("grade must not be null");
        }
        VaultEntry entry = getEntryOrThrow(vaultEntryId);
        assertNotDeleted(entry);
        VaultCapabilityMatrix.assertCanRate(entry.getEntryType());

        Instant now = clock.instant();
        Optional<Rating> existing = ratingRepository.findById(vaultEntryId);
        if (existing.isPresent()) {
            Rating rating = existing.get();
            rating.updateGrade(grade, now);
            ratingRepository.save(rating);
        } else {
            ratingRepository.save(new Rating(vaultEntryId, grade, now));
        }
    }

    @Override
    @Transactional
    public void removeRating(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = getEntryOrThrow(vaultEntryId);
        assertNotDeleted(entry);
        VaultCapabilityMatrix.assertCanRate(entry.getEntryType());

        if (ratingRepository.existsById(vaultEntryId)) {
            ratingRepository.deleteById(vaultEntryId);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public TagView createTag(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Tag name must not be null");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        Optional<Tag> existing = tagRepository.findByNameIgnoreCase(trimmed);
        if (existing.isPresent()) {
            Tag tag = existing.get();
            return new TagView(tag.getId(), tag.getName(), tag.getCreatedAt());
        }
        try {
            Tag saved = tagCreator.createInNewTransaction(trimmed, clock.instant());
            return new TagView(saved.getId(), saved.getName(), saved.getCreatedAt());
        } catch (DataIntegrityViolationException ex) {
            return tagCreator.findByNameIgnoreCaseInNewTransaction(trimmed)
                    .map(tag -> new TagView(tag.getId(), tag.getName(), tag.getCreatedAt()))
                    .orElseThrow(() -> ex);
        }
    }

    @Override
    @Transactional
    public void attachTag(Long vaultEntryId, Long tagId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("tagId must not be null");
        }
        VaultEntry entry = getEntryOrThrow(vaultEntryId);
        assertNotDeleted(entry);
        VaultCapabilityMatrix.assertCanTag(entry.getEntryType());

        if (!tagRepository.existsById(tagId)) {
            throw new NoSuchElementException("Tag not found with id: " + tagId);
        }

        VaultEntryTagId id = new VaultEntryTagId(vaultEntryId, tagId);
        if (!vaultEntryTagRepository.existsById(id)) {
            vaultEntryTagRepository.save(new VaultEntryTag(id, clock.instant()));
        }
    }

    @Override
    @Transactional
    public void detachTag(Long vaultEntryId, Long tagId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        if (tagId == null) {
            throw new IllegalArgumentException("tagId must not be null");
        }
        VaultEntry entry = getEntryOrThrow(vaultEntryId);
        assertNotDeleted(entry);
        VaultCapabilityMatrix.assertCanTag(entry.getEntryType());

        if (!tagRepository.existsById(tagId)) {
            throw new NoSuchElementException("Tag not found with id: " + tagId);
        }

        VaultEntryTagId id = new VaultEntryTagId(vaultEntryId, tagId);
        if (vaultEntryTagRepository.existsById(id)) {
            vaultEntryTagRepository.deleteById(id);
        }
    }

    private VaultEntry getEntryOrThrow(Long vaultEntryId) {
        return vaultEntryRepository.findById(vaultEntryId)
                .orElseThrow(() -> new NoSuchElementException("Vault entry not found with id: " + vaultEntryId));
    }

    private void assertNotDeleted(VaultEntry entry) {
        if (entry.isDeleted()) {
            throw new IllegalStateException("Vault entry is in trash: " + entry.getId());
        }
    }
}
